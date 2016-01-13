(ns casino2016.game-test
  (:require [casino2016.game :refer :all]
            [clojure.test :refer :all]))

(def ^:const a-name "a-name")
(def ^:const zero-tickets 0)

(def new-player (player a-name))

(deftest new-player-has-zero-tickets
    (is (= zero-tickets (:tickets new-player))))

(deftest new-player-has-a-name
    (is (= a-name (new-player :name))))

(deftest new-player-has-no-choice
  (is (= :nochoice (new-player :choice))))

(deftest new-player-has-not-lost-yet
  (is (not (:lost new-player))))

