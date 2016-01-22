(ns casino2016.admin
  (:require [reagent.session :as session]))

(defn players-in-game-view
  [chsk-send!]
  (fn []
    (let [{:keys [number-of-players max-player players]} (session/get-in [:game-state :game])]
      [:div
       [:h3 (str "Players (" number-of-players "/" max-player ")")]
       [:ul (for [name (keys players)]
              (into [:li.admin-player-entry]
                    [[:p.admin-accepted-player name]
                     [:button {:on-click #(chsk-send! [::kick-player name])} "kick"]]))]])))

(defn pending-players-view
  [chsk-send!]
  (fn []
    [:div
     [:h3 "Pending"]
     [:ul (for [name (session/get-in [:game-state :pending-players])]
            (into [:li.admin-player-entry]
                  [[:p.admin-pending-player name]
                   [:button {:on-click #(chsk-send! [::accept-player name])} "accept"]]))]]))

(defn player-view
  [chsk-send!]
  (fn []
    [:div.admin-player-container
     [(players-in-game-view chsk-send!)]
     [(pending-players-view chsk-send!)]]))

(defn admin-actions
  [chsk-send!]
  (fn []
    [:div
     [:h3 "Actions"]
     [:div.admin-actions
      [:button "Start game"]
      [:button {:on-click #(chsk-send! [::reset])} "Reset"]]]))

(defn page
  [chsk-send!]
  (fn []
    [:div
     [:h1 "Admin"]
     [(player-view chsk-send!)]
     [(admin-actions chsk-send!)]]))

