(ns casino2016.game)

(defn player [name]
  {:name name :tickets 0 :choice :nochoice})

(defn give-n-tickets [player n]
  (update player :tickets + n))

(defn choose [player choice]
  (assoc player :choice choice))

(defn new-game
  [max-player]
  (assoc {} :players {} :max-player max-player :number-of-players 0))

(defn add-player [game player]
  (let [max-player (:max-player game)]
    (if (= max-player (count (:players game)))
      game
      (if (some #{(:name player)} (keys (:players game)))
        game
        (-> game
            (assoc-in [:players (:name player)] player)
            (update :number-of-players inc))))))

(defn play-turn-player [player choice ticket-prize]
  (if (= choice (:choice player))
    (update player :tickets + ticket-prize)
    (assoc player :lost true)))

(defn play-turn-game [game choice]
  (let [losers (filter (fn [[k v]] (not= choice (:choice v))) (:players game))
        winners (filter (fn [[k v]] (= choice (:choice v))) (:players game))
        nb-winners (count winners)
        available-tickets (+ (apply + (map #(:tickets (second %)) losers)) (get game :free-tickets 0))]
    (let [ticket-prize (if (zero? nb-winners)
                         0
                         (quot available-tickets nb-winners))
          free-tickets (if (zero? nb-winners)
                         available-tickets
                         (mod available-tickets nb-winners))
          player-turn-with-choice (fn [[name-id player]]
                                    [name-id (play-turn-player player choice ticket-prize)])]
      (-> game
          (update :players #(into {} (map player-turn-with-choice %)))
          (assoc :free-tickets free-tickets)))))

(defn kick-losers [game]
  (update game :players (fn [players]
                          (->> players
                               (filter (fn [[k v]] (not (:lost v))))
                               (into {})))))

(defn kick-player [game player-name]
  (update game :players dissoc player-name))


(defn player-choose [game player-name choice]
  (if (not-empty
       (filter #(= player-name %)
               (keys (:players game))))
       (assoc-in game [:players player-name :choice] choice)
       game))

(defn player-bet [game player-name tickets]
  (if (not-empty
       (filter #(= player-name %)
               (keys (:players game))))
    (assoc-in game [:players player-name :tickets] tickets)
    game))
