(ns casino2016.game)

(defn player [name]
  {:name name :tickets 0 :choice :nochoice})

(defn give-n-tickets [player n]
  (update player :tickets + n))

(defn choose [player choice]
  (assoc player :choice choice))

(defn new-game
  [max-player]
  (assoc {} :players [] :max-player max-player))

(defn add-player [game player]
  (let [max-player (:max-player game)]
    (if (= max-player (count (:players game)))
      game
      (if (= 1 (count
                (filter #(= (:name %) (:name player))
                        (:players game))))
        game
        (update game :players conj player)))))

(defn play-turn-player [player choice ticket-prize]
  (if (= choice (:choice player))
    (update player :tickets + ticket-prize)
    (assoc player :lost true)))

(defn play-turn-game [game choice]
  (let [losers (filter #(not= choice (:choice %)) (:players game))
        winners (filter #(= choice (:choice %)) (:players game))
        nb-winners (count winners)
        available-tickets (+ (apply + (map :tickets losers)) (get game :free-tickets 0))]
    (let [ticket-prize (if (zero? nb-winners)
                         0
                         (quot available-tickets nb-winners))
          free-tickets (if (zero? nb-winners)
                         available-tickets
                         (mod available-tickets nb-winners))
          player-turn-with-choice (fn [player]
                                    (play-turn-player player choice ticket-prize))]
      (-> game
          (update :players #(map player-turn-with-choice %))
          (assoc :free-tickets free-tickets)))))

(defn kick-losers [game]
  (update game :players
          (fn [players]
            (filter #(not (:lost %)) players))))
