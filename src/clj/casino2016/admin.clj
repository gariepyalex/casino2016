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

(defn- remove-pending-player
  [player-name]
  (commute state update :pending-players set/difference #{player-name}))

(defn- add-player-to-game
  [player-name]
  (commute state update :game game/add-player (game/player player-name)))

(defn reset
  []
  (dosync (ref-set state (new-game))))

(defn admin-add-player
  [player-name]
  (dosync
   (when-not (some #{player-name} (:taken-names @state))
     (do (commute state update :taken-names conj player-name)
         (admin-add-player player-name)))))

(defn admin-accept-player
  [name]
  (dosync
   (when (some #{name} (:pending-players @state))
     (remove-pending-player name)
     (add-player-to-game name))))

(defn admin-kick-player
  [name]
  (dosync
   ;; TODO remove from actual game
   (commute state update :taken-names set/difference #{name})))

(defn sign-up
  [id player-name]
  (dosync
   (when-not (some #{player-name} (:taken-names @state))
     (do (commute state update :taken-names conj player-name)
         (commute state update :pending-players conj player-name)
         (when (contains? (:sessions @state) id)
           (let [old-name (get-in @state [:sessions id])]
             (commute state update :taken-names     set/difference #{old-name})
             (remove-pending-player old-name)))
         (commute state assoc-in [:sessions id] player-name)))))

