(ns casino2016.handler
  (:require [clojure.core.async :as async :refer [<! <!! chan go go-loop thread]]
            [compojure.core :include-macros true :refer [defroutes GET POST]]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.response :as response]
            [hiccup.page :as hiccup]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer [sente-web-server-adapter]]
            [casino2016.admin :as admin]))

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
(defn cookie-as-user-id
  [request]
  (:session/key request))

(let [{:keys [ch-recv
              send-fn
              ajax-post-fn
              ajax-get-or-ws-handshake-fn
              connected-uids]}
      (sente/make-channel-socket! sente-web-server-adapter {:user-id-fn cookie-as-user-id})]
  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv)
  (def chsk-send!                    send-fn)
  (def connected-uids                connected-uids))

(defroutes handler
  (GET "/" [] web-app)
  (GET  "/chsk" req (ring-ajax-get-or-ws-handshake req))
  (POST "/chsk" req (ring-ajax-post                req))
  (GET "/*" [] (response/redirect "/"))
  (route/resources "/"))

(def app (wrap-defaults handler site-defaults))

;;======================================================
;; Game loop
(defonce game-state (atom nil))

(defmulti event-handler
  (fn [event] (:id event)))

(defmethod event-handler :casino2016.player/sign-up
  [{user-id :uid username :?data}]
  (admin/sign-up user-id username))

(defmethod event-handler :default
  [_]
  nil)

(defn event-loop
  []
  (go-loop [event (<! ch-chsk)]
    (event-handler event)
    (println @admin/state)
    (recur (<! ch-chsk))))
