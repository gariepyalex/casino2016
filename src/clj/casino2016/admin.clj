(ns casino2016.admin
  (:require [clojure.set :as set]
            [casino2016.game :as game]))

(def max-players 8)

(defn new-game
  []
  {:game (game/new-game max-players)
   :taken-names #{}
   :pending-players #{}
   :sessions {}})

(def state (ref (new-game)))

(defn reset
  []
  (dosync (ref-set state (new-game))))

(defn admin-add-player
  [player-name]
  (dosync
   (when-not (some #{player-name} (:taken-names @state))
     (do (commute state update :taken-names conj player-name)
         (commute state update :game game/add-player player-name)))))

(defn sign-up
  [id player-name]
  (dosync
   (when-not (some #{player-name} (:taken-names @state))
     (do (commute state update :taken-names conj player-name)
         (commute state update :pending-players conj player-name)
         (when (contains? (:sessions @state) id)
           (let [old-name (get-in @state [:sessions id])]
             (commute state update :taken-names     set/difference #{old-name})
             (commute state update :pending-players set/difference #{old-name})))
         (commute state assoc-in [:sessions id] player-name)))))

