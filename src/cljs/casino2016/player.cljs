(ns casino2016.player
  (:require [clojure.string :refer [blank?]]
            [taoensso.sente  :as sente]
            [reagent.cookies :as cookies]
            [reagent.core :as r]
            [reagent.core :as reagent]
            [reagent.session :as session]
            [casino2016.game-state :as state]))

(defn- user-in-game?
  []
  (nil? (session/get :username)))

(defn choose-move!
  [chsk-send! move]
  (chsk-send! [::choose-move move]))

(defn sign-up-game!
  [chsk-send! username error-atom]
  (when (not (blank? username))
    (chsk-send! [::sign-up username]
                3000 (fn [signed-up?]
                       (if (and (sente/cb-success? signed-up?) (true? signed-up?))
                         (session/put! :username username)
                           (reset! error-atom true))))))

(defn form-sign-up-for-game
  [chsk-send!]
  (let [name  (r/atom nil)
        error (r/atom false)]
    (fn []
      [:div.sign-up-form
       (when @error
         [:h3.error-message "Choisissez un autre nom"])
       [:input.sign-up-name {:placeholder "Votre nom"
                             :on-change #(reset! name (-> % .-target .-value))}]
       [:button.sign-up-button {:on-click #(sign-up-game! chsk-send! @name error)}
        "Jouer"]])))

(defn- arrows-properties
  [player-name direction chsk-send!]
  (let [default {:on-click #(choose-move! chsk-send! direction)}]
    (if (= direction (get-in @state/state [:game :players player-name :choice]))
      (assoc default :class "player-arrow-selected")
      default)))

(defn- playing?
  [name]
  (contains? (get-in @state/state [:game :players]) name))

(defn- won?
  [name]
  (let [winner-name (get-in @state/state [:game :last-man-standing])]
    (= name winner-name)))

(defn- lost?
  [name]
  (contains? (get-in @state/state [:game :losers]) name))

(defn playing-arrows
  [chsk-send!]
  (fn []
    (let [name (session/get :username)]
      [:div
       [:h2 name]
       (cond
         (won? name)
         [:div
          [:h3.success-message "Vous avez gagné"]
          [:img.lost-image {:src "/img/win.gif"}]]
         (playing? name)
         [:div
          [:h3 "Choisir une direction"]
          [:div.player-container
           [:div.player-arrow.player-arrow-left (arrows-properties name :left chsk-send!)]
           [:div.player-arrow.player-arrow-right (arrows-properties name :right chsk-send!)]]]
         (lost? name)
         [:div
          [:h3.error-message "Vous avez perdu"]
          [:img.lost-image {:src "/img/loser.gif"}]]
         :else
         [:div
          [:h3.error-message "En attente d'approbation"]
          [:p "Pour rejoindre la partie, paie tes jetons à la table!"]])])))

(defn page
  [chsk-send!]
  (fn []
    [:div.player-container
     (if (user-in-game?)
       [(form-sign-up-for-game chsk-send!)]
       [(playing-arrows chsk-send!)])]))
