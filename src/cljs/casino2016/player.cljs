(ns casino2016.player
  (:require [clojure.string :refer [blank?]]))

(defn sign-up-game!
  [chsk-send! username]
  (when (not (blank? username))
    (chsk-send! [::sign-up username])))

(defn form-sign-up-for-game
  [chsk-send!]
  (let [name (atom nil)]
    (fn []
      [:div.sign-up-form
       [:input.sign-up-name {:placeholder "Your name"
                             :on-change #(reset! name (-> % .-target .-value))}]
       [:div.sign-up-button {:on-click #(sign-up-game! chsk-send! @name)}
        "Enter game"]])))

(defn playing-arrows
  []
  [:div
   [:h2 "Choose a direction"]
   [:div.player-container
    [:div.player-arrow.player-arrow-left {:on-click #(js/alert "left")}]
    [:div.player-arrow.player-arrow-right {:on-click #(js/alert "right")}]]])

(defn page
  [chsk-send!]
  (fn []
    [:h2 "Choose a direction"]
    [:div.player-container
     [(form-sign-up-for-game chsk-send!)]]))
