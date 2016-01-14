(ns casino2016.game-test
  (:require [casino2016.game :refer :all]
            [clojure.test :refer :all]))

(def ^:const a-name "a-name")
(def ^:const zero-tickets 0)
(def ^:const new-player (player a-name))
(def ^:const n 6)
(def ^:const a-player (give-n-tickets n new-player))


(deftest new-player-has-zero-tickets
    (is (= zero-tickets (:tickets new-player))))

(deftest new-player-has-a-name
    (is (= a-name (new-player :name))))

(deftest new-player-has-no-choice
  (is (= :nochoice (new-player :choice))))

(deftest new-player-has-not-lost-yet
  (is (not (:lost new-player))))

(deftest given-a-player-when-give-n-tickets-player-has-n-more-tickets
  (let [m 4
        prev-number-of-tickets (:tickets a-player)
        new-number-of-tickets (:tickets (give-n-tickets m a-player))]
    (is (= (+ prev-number-of-tickets m) new-number-of-tickets))))


(deftest new-game-has-no-players
  (is (empty? (:players (new-game)))))

(deftest when-add-player-on-game-then-player-is-present
  (let [game (add-player a-player (new-game))]
    (is (not-empty
     (filter #(= (:name %) (:name a-player))
            (:players game))))))
