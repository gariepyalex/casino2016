(ns casino2016.game)

(defn player [name]
  {:tickets 0 :name name :choice :nochoice})

(defn give-n-tickets [n player]
  (update player :tickets + n))

(defn bet [bid player]
  (assoc player :bid bid))

(defn choose [choice player]
  (assoc player :choice choice))

(defn new-game
  []
  (assoc {} :players []))

(defn add-player [player game]
  (update game :players conj player))
