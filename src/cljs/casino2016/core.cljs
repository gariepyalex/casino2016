(ns casino2016.core
  (:require-macros [cljs.core.async.macros :as asyncm :refer (go go-loop)])
  (:require [cljs.core.async :as async :refer [timeout <! chan]]
            [secretary.core :as secretary :refer-macros [defroute]]
            [reagent.core :as reagent]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [taoensso.sente  :as sente :refer (cb-success?)]
            [casino2016.home :as home]
            [casino2016.admin :as admin]
            [casino2016.game :as game]
            [casino2016.player :as player])
  (:import goog.History))


(enable-console-print!)

(defonce sente-socket (sente/make-channel-socket! "/chsk" {:type :auto}))
(defonce chsk       (:chsk sente-socket))
(defonce ch-chsk    (:ch-recv sente-socket)) ; ChannelSocket's receive channel
(defonce chsk-send! (:send-fn sente-socket)) ; ChannelSocket's send API fn
(defonce chsk-state (:state sente-socket))   ; Watchable, read-only atom
(def app-dom-mount (js/document.getElementById "app"))

(defn current-page
  []
  [:div [(session/get :current-page)]])

(secretary/defroute "/"
  []
  (session/put! :current-page home/page))

(secretary/defroute "/admin"
  []
  (session/put! :current-page (admin/page chsk-send!)))

(secretary/defroute "/game"
  []
  (session/put! :current-page game/page))

(secretary/defroute "/player"
  []
  (session/put! :current-page (player/page chsk-send!)))

(defn hook-browser-navigation!
  []
  (doto (History.)
    (events/listen
     HistoryEventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn mount-root
  []
  (reagent/render [current-page] app-dom-mount))

(defn init!
  []
  (secretary/set-config! :prefix "#")
  (hook-browser-navigation!)
  (mount-root))

(defn on-js-reload
  []
  (mount-root))

(init!)
