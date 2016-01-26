(ns casino2016.player
  (:require [clojure.string :refer [blank?]]
            [reagent.cookies :as cookies]
            [reagent.core :as r]
            [reagent.core :as reagent]
            [reagent.session :as session]))

(defn- user-in-game?
  []
  (nil? (session/get :username)))

(defn choose-move!
  [chsk-send! move]
  (chsk-send! [::choose-move move]))

(defn sign-up-game!
  [chsk-send! username]
  (when (not (blank? username))
    (chsk-send! [::sign-up username])
    (session/put! :username username)))

(defn form-sign-up-for-game
  [chsk-send!]
  (let [name (r/atom nil)]
    (fn []
      [:div.sign-up-form
       [:input.sign-up-name {:placeholder "Votre nom"
                             :on-change #(reset! name (-> % .-target .-value))}]
       [:button.sign-up-button {:on-click #(sign-up-game! chsk-send! @name)}
        "Jouer"]])))

(defn- arrows-properties
  [player-name direction chsk-send!]
  (let [default {:on-click #(choose-move! chsk-send! direction)}]
    (if (= direction (session/get-in [:game-state :game :players player-name :choice]))
      (assoc default :class "player-arrow-selected")
      default)))

(defn playing-arrows
  [chsk-send!]
  (fn []
    (let [state (session/get :game-state)
          name  (session/get :username)]
      [:div
       [:h2 name]
       (if (contains? (get-in state [:game :players]) name)
         [:div
          [:h3 "Choisir une direction"]
          [:div.player-container
           [:div.player-arrow.player-arrow-left (arrows-properties name :left chsk-send!)]
           [:div.player-arrow.player-arrow-right (arrows-properties name :right chsk-send!)]]]
         [:div
          [:h2 "En attente d'approbation"]
          [:p "Pour rejoindre la partie, paie tes jetons à la table!"]])])))

(defn page
  [chsk-send!]
  (fn []
    [:div.player-container
     (if (user-in-game?)
       [(form-sign-up-for-game chsk-send!)]
       [(playing-arrows chsk-send!)])]))
