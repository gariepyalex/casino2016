(ns casino2016.player
  (:require [reagent.session :as session]
            [reagent.cookies :as cookies]
            [clojure.string :refer [blank?]]))

(defn- user-in-game?
  []
  (nil? (session/get :username)))

(defn sign-up-game!
  [chsk-send! username]
  (when (not (blank? username))
    (chsk-send! [::sign-up username])
    (session/put! :username username)))

(defn form-sign-up-for-game
  [chsk-send!]
  (let [name (atom nil)]
    (fn []
      [:div.sign-up-form
       [:input.sign-up-name {:placeholder "Your name"
                             :on-change #(reset! name (-> % .-target .-value))}]
       [:button.sign-up-button {:on-click #(sign-up-game! chsk-send! @name)}
        "Enter game"]])))

(defn playing-arrows
  []
  (let [state (session/get :game-state)
        name  (session/get :username)]
    [:div
     [:h1 name]
     (if (contains? (:pending-players state) name)
       [:div
        [:h2 "En attente d'approbation"]]
       [:div
        [:h2 "Choisir une direction"]
        [:div.player-container
         [:div.player-arrow.player-arrow-left {:on-click #(js/alert "left")}]
         [:div.player-arrow.player-arrow-right {:on-click #(js/alert "right")}]]])]))

(defn page
  [chsk-send!]
  (fn []
    [:div.player-container
     (if (user-in-game?)
       [(form-sign-up-for-game chsk-send!)]
       [playing-arrows])]))
