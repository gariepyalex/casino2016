(ns casino2016.handler
  (:require [clojure.core.async :as async :refer [<! <!! chan go go-loop thread]]
            [compojure.core :include-macros true :refer [defroutes GET POST]]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.transit :refer [wrap-transit-body]]
            [ring.util.response :as response]
            [hiccup.page :as hiccup]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer [sente-web-server-adapter]]
            [casino2016.admin :as admin]
            [casino2016.password :as password]))

(def web-app (hiccup/html5
              [:html
               [:head
                [:title "IFT-GLO CASINO 2016"]
                [:meta {:charset "UTF-8"}]
                [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]]
               [:body
                [:div#app]
                (hiccup/include-js "js/compiled/app.js")
                (hiccup/include-css "css/style.css")]]))

;;======================================================
;; Ring/Compojure route
(defn uuid-as-user-id
  [request]
  (str (java.util.UUID/randomUUID)))

(let [{:keys [ch-recv
              send-fn
              ajax-post-fn
              ajax-get-or-ws-handshake-fn
              connected-uids]}
      (sente/make-channel-socket! sente-web-server-adapter {:user-id-fn uuid-as-user-id})]
  (defonce ring-ajax-post                ajax-post-fn)
  (defonce ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (defonce ch-chsk                       ch-recv)
  (defonce chsk-send!                    send-fn)
  (defonce connected-uids                connected-uids))

(defn admin-login!
  [request]
  (let [{:keys [session body]} request
        {:keys [password]}     body]
    (if (password/is-admin? password)
      {:status 200 :session (assoc session :role :admin)}
      {:status 401})))

(defroutes handler
  (GET "/" [] web-app)
  (GET  "/chsk" req (ring-ajax-get-or-ws-handshake req))
  (POST "/chsk" req (ring-ajax-post                req))
  (POST "/admin-login" req (admin-login! req))
  (GET "/*" [] (response/redirect "/"))
  (route/resources "/"))

(def app (-> handler
             (wrap-defaults (-> site-defaults
                                (assoc-in [:responses :content-types] true)
                                (assoc-in [:session :flash] false)
                                (assoc-in [:security :anti-forgery] false)))
             (wrap-transit-body)))

;;======================================================
;; Game loop
(defonce game-state (atom nil))

(defn send-kick-message
  [user-id]
  (go (chsk-send! user-id [:casino2016.admin/kicked])))

(defn kick-everyone
  []
  (doseq [client (:any @connected-uids)]
    (send-kick-message client)))

(defn wrap-verify-role
  [handler]
  (fn [{:keys [id ring-req] :as event}]
    (if (= (namespace id) "casino2016.admin")
      (let [role (get-in ring-req [:session :role])]
        (when (= :admin role)
          (handler event)))
      (handler event))))

(defmulti event-handler
  (fn [event] (:id event)))

(defmethod event-handler :casino2016.player/sign-up
  [{user-id :uid username :?data reply-fn :?reply-fn}]
  (admin/sign-up user-id username)
  (when (admin/has-session? user-id)
    (reply-fn true)))

(defmethod event-handler :casino2016.player/choose-move
  [{user-id :uid choice :?data}]
  (admin/choose-move user-id choice))

(defmethod event-handler :casino2016.admin/reset
  [{ring-request :ring-req}]
  (kick-everyone)
  (admin/reset))

(defmethod event-handler :casino2016.admin/accept-player
  [{player-name :?data}]
  (admin/admin-accept-player player-name))

(defmethod event-handler :casino2016.admin/add-player
  [{player-name :?data}]
  (admin/admin-add-player player-name))

(defmethod event-handler :casino2016.admin/kick-player
  [{player-name :?data}]
  (when-let [session (admin/player-name->session player-name)]
    (send-kick-message session))
  (admin/admin-kick-player player-name))

(defmethod event-handler :casino2016.admin/start-game
  [{ring-request :ring-req}]
  (admin/start-game))

(defmethod event-handler :casino2016.admin/play-turn
  [_]
  (admin/play-turn))
(defmethod event-handler :casino2016.admin/play-turn
  [_]
  (admin/play-turn))

(defmethod event-handler :casino2016.admin/choose-move
  [{user-id :uid {:keys [player-name choice]} :?data}]
  (admin/admin-choose-move player-name choice))

(defmethod event-handler :casino2016.game/kick-loosers
  [_]
  (admin/kick-loosers))

(defmethod event-handler :default
  [event]
  nil)

(defn broadcast-state!
  []
  (doseq [client (:any @connected-uids)]
    (go (chsk-send! client [:game/state @admin/state]))))

(defn event-loop
  []
  (broadcast-state!)
  (let [handler (wrap-verify-role event-handler)]
    (go-loop [event (<! ch-chsk)]
      (handler event)
      (broadcast-state!)
      (recur (<! ch-chsk)))))

