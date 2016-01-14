(ns casino2016.admin
  (:require [reagent.session :as session]))

(defn player-view
  []
  (let [state (:game-state session)]
    [:div
     [:h3 (str "Players ("
               (:number-of-players state)
               "/"
               (:max-number-of-players state)
               ")")]
     [:ul (for [{:keys [name status id]} (:players state)]
            (into [:li.admin-player-entry]
                  (if (= status :pending)
                    [[:p.admin-accepted-player name] [:button "accept"]]
                    [[:p.admin-pending-player name] [:button "kick"]])))]]))

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
