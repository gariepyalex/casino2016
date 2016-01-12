(ns casino2016.game
  (:require [reagent.core :as reagent]
            [quil.core :as quil :include-macros true]
            quil.middleware))

(defn setup []
  (quil/frame-rate 60)
  (quil/background 260)
  (quil/rect-mode :center)
  {:r 0.0
   :col 0})

(defn tick [state]
  (update-in state [:r] + 5.0))

(defn flip [state]
  {:r 0.0
   :col (if (= 0 (:col state)) 255 0)})

(defn update-state [state]
  (if (< (:r state) 300)
    (tick state)
    (flip state)))

(defn draw-state [state]
  (quil/stroke (:col state))
  (let [hw (* 0.5 (quil/width))
        hh (* 0.5 (quil/height))]
    (dotimes [_ (quot (quil/width) 10)]
      (let [rand-ang (quil/random 0 quil/TWO-PI)
            r (:r state)]
        (quil/line hh
                hw
                (+ hh (* (quil/sin rand-ang) r))
                (+ hw (* (quil/cos rand-ang) r)))))))

(quil/defsketch hyper
  :host "game-canvas"
  :no-start true
  :size [500 500]
  :setup setup
  :update update-state
  :draw draw-state
  :middleware [quil.middleware/fun-mode])

(defn game-container
  []
  [:div [:canvas#game-canvas]])

(defn page
  []
  (reagent/create-class
   {:reagent-render game-container
    :component-did-mount hyper}))
