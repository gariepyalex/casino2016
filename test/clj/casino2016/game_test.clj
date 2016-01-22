(ns casino2016.game-test
  (:require [casino2016.game :refer :all]
            [clojure.test :refer :all]))

(def ^:const a-name "a-name")
(def ^:const zero-tickets 0)
(def ^:const new-player (player a-name))
(def ^:const n 6)
(def ^:const a-player (give-n-tickets new-player n))
(def a-new-game (new-game 4))
(def a-game (-> (new-game 4)
                (add-player (-> (player "bob")
                                (give-n-tickets 3)
                                (choose :left)))
                (add-player (-> (player "banana")
                                (give-n-tickets 2)
                                (choose :right)))
                (add-player (-> (player "bibi")
                                (give-n-tickets 1)
                                (choose :right)))
                (add-player (-> (player "14x222")
                                (give-n-tickets 2)
                                (choose :left)))))

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
        new-number-of-tickets (:tickets (give-n-tickets a-player m))]
    (testing "Given a player when give n tickets player has n more tickets"
      (is (= (+ prev-number-of-tickets m) new-number-of-tickets)))))


(deftest new-game-initialization
  (testing "A new game has no players"
    (is (empty? (:players a-new-game)))))

(deftest add-player-test
  (let [max-player 8
        a-new-game (new-game max-player)
        full-game (-> (new-game 8)
                      (add-player (player "first"))
                      (add-player (player "second"))
                      (add-player (player "third"))
                      (add-player (player "fourth"))
                      (add-player (player "fifth"))
                      (add-player (player "sixth"))
                      (add-player (player "seventh"))
                      (add-player (player "eigth")))]
    (testing "Given a new game with max player then max player is set"
      (is (= max-player (:max-player a-new-game))))
    (testing "Given a new game when adding a player then a player is present in the game"
      (is (not-empty
           (filter #(= (:name %) (:name a-player))
                   (:players (add-player a-new-game a-player))))))
    (testing "Given a full game when adding a player then a player is not present in the game"
      (is (empty?
           (filter #(= (:name %) (:name a-player))
                   (:players (add-player full-game a-player))))))
    (testing "Given a game with a player when adding a player with same name then do not add player"
      (is (= 1 (count
                (filter #(= (:name %) (:name a-player))
                        (:players (-> a-new-game
                                      (add-player a-player)
                                      (add-player a-player))))))))))
(deftest play-turn-player-test
  (let [n-tickets 2
        right-choice :right
        wrong-choice :left
        ticket-prize 4
        a-winner (-> (player "a-player")
                     (give-n-tickets n-tickets)
                     (choose right-choice)
                     (play-turn-player right-choice ticket-prize))
        a-loser (-> (player "a-player")
                    (give-n-tickets n-tickets)
                    (choose wrong-choice)
                    (play-turn-player right-choice ticket-prize))]
    (testing "Given a winner when play turn then ticket prize is given to winner"
      (is (= (+ n-tickets ticket-prize) (:tickets a-winner))))
    (testing "Given a loser when play turn the loser has still n-tickets"
      (is (= n-tickets (:tickets a-loser))))
    (testing "Given a loser when play turn then loser has lost"
      (is (:lost a-loser)))))

(deftest play-turn-game-test
  (let [wrong-choice :right
        right-choice :left
        played-turn-game (play-turn-game a-game right-choice)
        number-of-losers (count (filter #(= wrong-choice (:choice %)) (:players a-game)))
        number-of-winners (count (filter #(= right-choice (:choice %)) (:players a-game)))
        number-of-tickets (apply + (conj (map :tickets (:players a-game)) (get :free-tickets a-game 0)))]
    (testing "When play turn the winner has not lost"
      (is (= number-of-winners (count
                                (filter #(not (:lost %))
                                        (:players played-turn-game))))))
    (testing "When play turn then number of tickets is preserved"
      (is (= number-of-tickets (reduce + (get played-turn-game :free-tickets 0)
                                       (map #(:tickets %)
                                            (filter #(not (:lost %))
                                                    (:players played-turn-game)))))))
    (testing "When play turn then losers has lost"
      (is (= number-of-losers (count
                               (filter #(:lost %)
                                       (:players played-turn-game))))))))

(deftest kick-losers-test
  (let [a-cleaned-game (-> a-game
                           (play-turn-game :right)
                           (kick-losers))
        losers (filter #(= :right (:choice %)) (:players (play-turn-game a-game :riht)))]
    (testing "Given losers when kick losers then they are not in the game anymore"
      (is (empty?
           (filter :lost
                   (:players a-cleaned-game)))))))
