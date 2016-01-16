(ns casino2016.admin
  (:require [reagent.session :as session]))

(defn player-view
  []
  (let [state (session/get :game-state)]
    [:div.admin-player-container
     [:div
      [:h3 (str "Players ("
                (:number-of-players state)
                "/"
                (:max-number-of-players state)
                ")")]
      [:ul (for [{name :name} (:players state)]
             (into [:li.admin-player-entry]
                   [[:p.admin-accepted-player name]
                    [:button "kick"]]))]]
     [:div
      [:h3 "Pending"]
      [:ul (for [{name :name} (:pending-players state)]
             (into [:li.admin-player-entry]
                   [[:p.admin-pending-player name]
                    [:button "accept"]]))]]]))

(defn page
  [chsk-send!]
  (fn []
    [:div
     [:h1 "Admin"]
     [player-view]
     [:h3 "Actions"]
     [:div.admin-actions
      [:button "Start game"]
      [:button "Reset"]]]))
