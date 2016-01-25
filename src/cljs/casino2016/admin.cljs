(ns casino2016.admin
  (:require [taoensso.sente :as sente]
            [reagent.core :as r]
            [reagent.session :as session]
            [ajax.core :refer [POST]]
            [clojure.string :refer [blank?]]))

(defn add-player!
  [chsk-send! username]
  (when (not (blank? username))
    (chsk-send! [::add-player username])))

(defn choose-move-for-player
  [chsk-send! player-name choice]
  (chsk-send! [::choose-move {:player-name player-name :choice choice}]))

(defn add-player-view
  [chsk-send!]
  (let [name (r/atom nil)]
    (fn []
      [:div
       [:h4 "Ajouter un joueur"]
       [:input {:placeholder "Nom du joueur"
                :on-change #(reset! name (-> % .-target .-value))
                :value @name}]
       [:button {:on-click #(do (add-player! chsk-send! @name) (reset! name nil))} "Ajouter"]])))

(defn players-in-game-view
  [chsk-send!]
  (fn []
    (let [{:keys [number-of-players max-player players]} (session/get-in [:game-state :game])]
      [:div
       [:h3 (str "Joueurs (" number-of-players "/" max-player ")")]
       [:ul (for [name (keys players)]
              (into [:li.admin-player-entry]
                    [[:p.admin-accepted-player name]
                     [:button {:on-click #(choose-move-for-player chsk-send! name :left)} "\u2190"]
                     [:button {:on-click #(choose-move-for-player chsk-send! name :right)} "\u2192"]
                     [:button {:on-click #(chsk-send! [::kick-player name])} "kick"]]))]])))

(defn pending-players-view
  [chsk-send!]
  (fn []
    [:div
     [:h3 "En attente"]
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
      [:button {:on-click #(chsk-send! [:casino2016.game/kick-loosers])} "Kick loosers"]
      [:button {:on-click #(chsk-send! [::reset])} "Reset"]
      [:button {:on-click #(chsk-send! [::play-turn])} "NEXT TURN"]]]))

(defn admin-view
  [chsk-send!]
  (fn []
    [:div
     [:h1 "Admin"]
     [(add-player-view chsk-send!)]
     [(player-view chsk-send!)]
     [(admin-actions chsk-send!)]]))

(defn login-view
  [chsk]
  (let [password (r/atom nil)
        error?   (r/atom nil)]
    (fn []
      [:div
       (when @error?
         [:p.error "Entrez un mot de passe valide"])
       [:input {:placeholder "Mot de passe admin"
                :on-change #(reset! password (-> % .-target .-value))
                :value @password}]
       [:button {:on-click (fn [] (POST "/admin-login"
                                      {:method :post
                                       :params {:password @password}
                                       :handler #(do (session/put! :is-admin? true)
                                                     (sente/chsk-reconnect! chsk))
                                       :error-handler #(reset! error? true)}))}
        "Login"]])))

(defn page
  [chsk chsk-send!]
  (fn []
    (if (session/get :is-admin?)
        [(admin-view chsk-send!)]
        [(login-view chsk)])))

