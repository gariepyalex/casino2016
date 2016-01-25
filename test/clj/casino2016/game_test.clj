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
    (is (empty? (:players a-new-game)))
    (is (zero? (:number-of-players a-new-game)))))

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
      (let [game-with-player (add-player a-new-game a-player)]
        (is (= a-player (get-in game-with-player [:players (:name a-player)])))))
    (testing "Given a full game when adding a player then a player is not present in the game"
      (is (not (contains? (add-player full-game a-player) (:name a-player)))))
    (testing "Given a game with a player when adding a player with same name then do not add player"
      (let [game (-> a-new-game
                     (add-player (-> (player "player-name")
                                     (give-n-tickets 1)))
                     (add-player (-> (player "player-name")
                                     (give-n-tickets 2))))]
        (is (= 1 (get-in game [:players "player-name" :tickets])))))
    (testing "Given a game when adding a player then the number of players is incremented"
      (let [game-with-two-players (-> a-new-game
                                      (add-player (player "toto"))
                                      (add-player (player "tata")))]
        (is (= 2 (:number-of-players game-with-two-players)))))))

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
        number-of-losers (count (filter #(= wrong-choice (:choice %)) (vals (:players a-game))))
        number-of-winners (count (filter #(= right-choice (:choice %)) (vals (:players a-game))))
        number-of-tickets (apply + (conj (map :tickets (vals (:players a-game))) (get :free-tickets a-game 0)))]
    (testing "When play turn the winner has not lost"
      (is (= number-of-winners (count
                                (filter #(not (:lost %))
                                        (vals (:players played-turn-game)))))))
    (testing "When play turn then number of tickets is preserved"
      (is (= number-of-tickets (reduce + (get played-turn-game :free-tickets 0)
                                       (map #(:tickets %)
                                            (filter #(not (:lost %))
                                                    (vals (:players played-turn-game))))))))
    (testing "When play turn then losers has lost"
      (is (= number-of-losers (count
                               (filter #(:lost %)
                                       (vals (:players played-turn-game)))))))
    (testing "When play turn last good and last bad move are in in game map"
      (is (= wrong-choice (:wrong-choice played-turn-game)))
      (is (= right-choice (:good-choice played-turn-game))))))

(deftest kick-losers-test
  (let [a-cleaned-game (-> a-game
                           (play-turn-game :right)
                           (kick-losers))
        losers (filter #(= :left (:choice %)) (vals (:players (play-turn-game a-game :right))))]
    (testing "Given losers when kick losers then they are not in the game anymore"
      (is (empty?
           (filter :lost
                   (vals (:players a-cleaned-game))))))
    (testing "When kick loosers last good and wrong choices are removed from game map"
      (is (nil? (:wrong-choice a-cleaned-game)))
      (is (nil? (:good-choice a-cleaned-game))))
    (testing "When kick players the number of players is adjusted"
      (is (= (:number-of-players a-cleaned-game) (- (:number-of-players a-game) (count losers)))))
    (testing "When kick players if the is only one player left then last man standing is set"
      (let [game (new-game 2)
            game-with-only-one-player (-> game
                                          (add-player (-> (player "toto")
                                                          (choose :left)))
                                          (add-player (-> (player "tata")
                                                          (choose :right)))
                                          (play-turn-game :left)
                                          (kick-losers))]
        (is (nil? (:last-man-standing game)))
        (is (= "toto" (:last-man-standing game-with-only-one-player)))))))

(deftest player-choose-test
  (let [a-choice :left]
    (testing "Given a valid game when a player choose a choice then changee player choice"
      (let [a-player-name "bibi"
            updated-game (player-choose a-game a-player-name a-choice)]
      (is (= a-choice (get-in updated-game
                              [:players a-player-name :choice]))))
    (testing "Given a game and an invalid player when choose a choice then don't add player"
      (let [invalid-name "kawabounga"
            updated-game (player-choose a-game invalid-name a-choice)]
        (is (empty? (filter #(= invalid-name %)
                (keys (get updated-game :players))))))))))


(deftest player-bet-test
  (let [n 123]
    (testing "Given a game when a player bet n tickets then player bet is n"
      (let [a-player-name "bibi"
            n 123
            updated-game (player-bet a-game a-player-name n)]
        (is (= n (get-in updated-game [:players a-player-name :tickets])))))
    (testing "Given a game when an invalid name bet n tickets then game is unchanged"
      (let [invalid-name "kawouabounga"
            updated-game (player-bet a-game invalid-name n)]
        (is (empty? (filter #(= invalid-name %)
                            (keys (get updated-game :players)))))))))

(deftest kick-player-test
  (testing "Given a game when kick a player then player is not in game anymore"
    (let [a-player-name "bibi"
          updated-game (kick-player a-game a-player-name)]
      (is (not (contains? updated-game a-player-name)))))
  (testing "Given a game when kick a player then the number of players is decremented"
    (let [a-player-name "bibi"
          old-number-of-players (:number-of-players a-game)
          updated-game (kick-player a-game a-player-name)]
      (is (= (dec old-number-of-players) (:number-of-players updated-game)))))
  (testing "Given a game when kick an invalid player then game has not changed"
    (let [an-invalid-player "kawouabounga"
          updated-game (kick-player a-game an-invalid-player)]
      (is (= a-game updated-game)))))
