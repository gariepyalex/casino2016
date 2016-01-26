(ns casino2016.game)

(defn player [name]
  {:name name :tickets 0 :choice :nochoice})

(defn give-n-tickets [player n]
  (update player :tickets + n))

(defn choose [player choice]
  (assoc player :choice choice))

(defn new-game
  [max-player]
  (assoc {}
         :players {}
         :max-player max-player
         :number-of-players 0
         :in-preparation true
         :losers #{}))

(defn started-game
  [new-game]
  (assoc new-game :in-preparation false))

(defn add-player [game player]
  (let [max-player (:max-player game)]
    (if (= max-player (count (:players game)))
      game
      (if (some #{(:name player)} (keys (:players game)))
        game
        (-> game
            (assoc-in [:players (:name player)] player)
            (update :number-of-players inc))))))

(defn fill-with-bots
  [game]
  (loop [filled-game game
         x (- (:max-player filled-game) (:number-of-players filled-game))]
    (if (> x 0)
      (recur
       (add-player filled-game "Bot")
       (- x 1)))))

(defn play-turn-player [player choice ticket-prize]
  (if (= :nochoice (:choice player))
    (recur (assoc player :choice (rand-nth [:left :right])) choice ticket-prize)
    (if (= choice (:choice player))
      (update player :tickets + ticket-prize)
      (assoc player :lost true))))

(defn- wrong-choice
  [good-choice]
  (get {:left :right, :right :left} good-choice))

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
          (assoc :free-tickets free-tickets)
          (assoc :good-choice choice)
          (assoc :wrong-choice (wrong-choice choice))
          (assoc :animation-state true)))))

(defn- assoc-last-man-standing
  [game]
  (if (= 1 (:number-of-players game))
    (assoc game :last-man-standing (-> (:players game) keys first))
    game))

(defn kick-losers [game]
  (-> game
      (update :losers #(->> game
                            (:players)
                            (filter (fn [[k v]] (:lost v)))
                            (map first)
                            (apply conj %)))
      (update :players (fn [players]
                         (->> players
                              (map (fn [[k v]] [k (assoc v :choice :nochoice)]))
                              (filter (fn [[k v]] (not (:lost v))))
                              (into {}))))
      (dissoc :wrong-choice)
      (dissoc :good-choice)
      (dissoc :animation-state)
      (#(assoc % :number-of-players (count (:players %))))
      (assoc-last-man-standing)))

(defn kick-player [game player-name]
  (if (contains? (:players game) player-name)
    (-> game
        (update :players dissoc player-name)
        (update :number-of-players dec))
    game))


(defn- valid-choice?
  [choice]
  (or (= choice :left)
      (= choice :right)))

(defn player-choose [game player-name choice]
  (if (and (contains? (:players game) player-name)
           (valid-choice? choice))
    (if (get game :animation-state)
      game
      (assoc-in game [:players player-name :choice] choice))
    game))

(defn player-bet [game player-name tickets]
  (if (not-empty
       (filter #(= player-name %)
               (keys (:players game))))
    (assoc-in game [:players player-name :tickets] tickets)
    game))
