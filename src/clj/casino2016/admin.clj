(ns casino2016.admin
  (:require [clojure.set :as set]
            [casino2016.game :as game]))

(def max-players 16)

(defn new-game
  []
  {:game (game/new-game max-players)
   :taken-names #{}
   :pending-players #{}})

(def state (ref (new-game)))
(def sessions (ref {}))

(defn- remove-pending-player
  [player-name]
  (commute state update :pending-players set/difference #{player-name}))

(defn- add-player-to-game
  [player-name]
  (commute state update :game game/add-player (game/player player-name)))

(defn reset
  []
  (dosync (ref-set state (new-game))))

(defn start-game
  []
  (dosync
   (commute state update :game game/started-game)))

(defn fill-with-bots
  []
  (dosync (commute state update :game game/fill-with-bots)))

(defn admin-add-player
  [player-name]
  (dosync
   (when-not (contains? (:taken-names @state) player-name)
     (do (commute state update :taken-names conj player-name)
         (add-player-to-game player-name)))))

(defn admin-accept-player
  [name]
  (dosync
   (when (some #{name} (:pending-players @state))
     (remove-pending-player name)
     (add-player-to-game name))))

(defn admin-kick-player
  [name]
  (dosync
   (commute state update :game game/kick-player name)
   (commute state update :taken-names set/difference #{name})))

(defn admin-choose-move
  [player-name choice]
  (dosync
   (commute state update :game game/player-choose player-name choice)))

(defn sign-up
  [id player-name]
  (dosync
   (when-not (contains? (:taken-names @state) player-name)
     (do (commute state update :taken-names conj player-name)
         (commute state update :pending-players conj player-name)
         (when (contains? @sessions id)
           (let [old-name (get @sessions id)]
             (commute state update :taken-names set/difference #{old-name})
             (remove-pending-player old-name)))
         (commute sessions assoc id player-name)))))

(defn choose-move
  [id choice]
  (dosync
   (let [player-name (get @sessions id)]
     (commute state update :game game/player-choose player-name choice))))

(defn player-name->session
  [player-name]
  (get (set/map-invert @sessions) player-name))

(defn has-session?
  [user-id]
  (contains? @sessions user-id))

(defn random-choice
  []
  (rand-nth [:left :right]))

(defn play-turn
  []
  (dosync
   (commute state update :game game/play-turn-game (random-choice))))

(defn kick-loosers
  []
  (dosync
   (commute state update :game game/kick-losers)))
