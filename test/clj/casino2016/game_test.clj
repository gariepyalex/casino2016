(ns casino2016.game-test
  (:require [casino2016.game :refer :all]
            [clojure.test :refer :all]))

(def ^:const a-name "a-name")
(def ^:const zero-tickets 0)
(def ^:const new-player (player a-name))
(def ^:const n 6)
(def ^:const a-player (give-n-tickets n new-player))
(def a-new-game (new-game))


(deftest new-player-initialization
  (testing "A new player has zero tickets"
    (is (= zero-tickets (:tickets new-player))))
  (testing "A new player has a name"
    (is (= a-name (new-player :name))))
  (testing "A new player has no choice"
    (is (= :nochoice (new-player :choice))))
  (testing "A new player has not lost... yet"
    (is (not (:lost new-player)))))

(deftest give-n-tickets-test
  (let [m 4
        prev-number-of-tickets (:tickets a-player)
        new-number-of-tickets (:tickets (give-n-tickets m a-player))]
    (testing "Given a player when give n tickets player has n more tickets"
      (is (= (+ prev-number-of-tickets m) new-number-of-tickets)))))


(deftest new-game-initialization
  (testing "A new game has no players"
    (is (empty? (:players a-new-game)))))

(deftest add-player-test
  (let [game (add-player a-player a-new-game)]
    (testing "Given a new game when adding a player then a player is present in the game"
      (is (not-empty
           (filter #(= (:name %) (:name a-player))
                   (:players game)))))))
