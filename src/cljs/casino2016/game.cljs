(ns casino2016.game
  (:require [reagent.core :as reagent]
            [clojure.set :as set]
            [clojure.string :refer [upper-case]]
            [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [casino2016.game-state :as state]))

(def animation-state (atom {}))
(def canvas-width 800)
(def canvas-height 400)
(def cam-pos [(- (/ canvas-width 2)) (- (* 5 (/ canvas-height 6)))])
(def ship-pos-x (map #(- (* 32 %) 100) (range)))
(def ship-move-offset 250)

(defn on-screen? [x y]
  (let [margin 100]
    (and (<= (- margin) x (+ margin (q/width)))
         (<= (- margin) y (+ margin (q/height))))))

(defn pulse [low high rate]
  (let [diff (- high low)
        half (/ diff 2)
        mid (+ low half)
        s (/ (q/millis) 1000.0)
        x (q/sin (* s (/ 1.0 rate)))]
    (+ mid (* x half))))

(defn rand-between [low high]
  (let [diff (- high low)]
    (+ low (rand diff))))

(defn rand-coord [size]
  [(rand-between (- size) size)
   (rand-between (- size) size)])

(defn translate-v2 [[x y] [dx dy]]
  [(+ x dx) (+ y dy)])

(defn render-ship
  [ship]
  (q/fill 50 80 50)
  (q/rect -2 0 5 14)
  (apply q/fill (:color ship))
  (q/triangle 0 -10 25 0 0 10)
  (q/fill 30 100 30)
  (q/ellipse 8 0 8 8)
  (q/fill 255 255 255)
  (q/text-size 15)
  (q/text (:name ship) -30 -10))

(defn create-ship
  [name taken-pos-x]
  (let [base-pos [(first (filter #(not (contains? taken-pos-x %)) ship-pos-x))
                  (- 10 (rand-int 20))]]
    {:name name
     :base-pos base-pos
     :color (into [] (take 3 (repeatedly #(+ 100 (rand-int 156)))))
     :pos base-pos
     :dir (- (/ q/PI 2))
     :dir-change 0.0
     :speed 0.1
     :z 1.0
     :smoke []
     :render-fn render-ship}))

(defn render-star [star]
  (let [size (:size star)]
    (q/fill 255)
    (q/rect 0 0 size size)))

(defn create-star [pos]
  {:pos pos
   :speed 0.6
   :dir (rand q/TWO-PI)
   :size (+ 1.0 (rand 3.0))
   :z (rand-between 0.2 0.7)
   :render-fn render-star})

(defn random-star []
  (create-star (rand-coord (/ (q/width) 2))))

(defn render-smoke [smoke]
  (let [age (:age smoke)
        size (max 0.0 (- 10.0 (* 5.0 age)))
        [r g b] (:col smoke)]
    (q/fill r g b 200)
    (q/ellipse 0 0 size size)))

(defn create-smoke [[x y]]
  {:pos [(+ x (rand-between -3 3))
         (+ y (rand-between -3 3))]
   :dir 0.0
   :age 0.0
   :z 1.0
   :col [(rand-between 150 255)
         (rand-between 100 200)
         (rand-between 0 100)]
   :speed 1
   :render-fn render-smoke})

(defn render-planet [planet]
  (let [size (:size planet)
        [r g b] (:color planet)]
    (q/fill r g b)
    (let [rs (:rs planet)
          step (/ q/TWO-PI (count rs))]
      (q/begin-shape)
      (doseq [[angle radius] (map vector
                                  (range 0 q/TWO-PI step) rs)]
        (q/vertex (* size radius (q/cos angle))
                  (* size radius (q/sin angle))))
      (q/end-shape))))

(defn generate-radiuses []
  (into [] (take (+ 5 (rand-int 7))
                 (repeatedly #(rand-between 0.5 1.0)))))

(defn random-planet-color
  []
  [(rand-between 0 255)
   (rand-between 50 150)
   (rand-between 50 150)])

(defn create-planet [pos-x]
  {:pos [pos-x (- (+ canvas-height (rand-int 100)))]
   :dir (rand q/TWO-PI)
   :dir-change (rand-between -0.03 0.03)
   :size (+ 80.0 (rand 50.0))
   :drift [0 7]
   :color (random-planet-color)
   :z 1.0
   :rs (generate-radiuses)
   :render-fn render-planet})

(defn setup []
  (swap! animation-state assoc :run-animation? true)
  (q/rect-mode :center)
  (q/frame-rate 30)
  {:ships {}
   :stars (take 200 (repeatedly random-star))
   :death-animations []})

(defn auto-rotate [entity]
  (let [dir-change (:dir-change entity)]
    (update-in entity [:dir] #(+ % dir-change))))

(defn wiggle-ship [ship]
  (let [speed (:speed ship)
        a (+ 0.01 (* 0.03 speed))]
    (update-in ship [:dir] #(+ % (pulse (- a) a 0.1)))))

(defn wiggle-ships
  [ships]
  (reduce
   (fn [ships [name ship]] (assoc ships name (wiggle-ship ship)))
   {}
   ships))

(defn drift-planet [planet]
  (let [[dx dy] (:drift planet)]
    (-> planet
        (update-in [:pos] translate-v2 [dx dy])
        (update-in [:dir] + (:dir-change planet)))))

(defn emit-smoke
  [ship]
  (if (< (rand) 0.2)
    (update-in ship [:smoke] conj (create-smoke (:pos ship)))
    ship))

(defn age-smoke [smoke]
  (update-in smoke [:age] #(+ % 0.033)))

(defn old? [smoke]
  (< 3.0 (:age smoke)))

(defn remove-old-smokes [smokes]
  (remove old? smokes))

(defn move-objects
  [objects]
  (map (fn [o] (update o :pos
         (fn [[x y]] [x (+ y (* (:z o) (:speed o)))])))
       objects))

(defn- random-star-position
  [[x y :as old-position]]
  [x (- (second cam-pos) (rand-int 100))])

(defn move-star-in-screen
  [stars]
  (map (fn [{[x y] :pos :as star}]
         (if (< y (+ canvas-height (second cam-pos) 10))
           star
           (update star :pos random-star-position)))
       stars))

(defn update-smoke-all-ships
  [ships]
  (reduce
   (fn [ships [name ship]]
     (assoc ships name
            (-> ship
                emit-smoke
                (update :smoke #(map age-smoke %))
                (update :smoke move-objects)
                (update :smoke remove-old-smokes))))
   {}
   ships))

(defn add-new-players
  [ships players]
  (let [new-players (set/difference (set (keys players))
                                    (set (keys ships)))]
    (reduce (fn [ships new-player]
              (let [taken-ship-pos-x (set (map #(first (:base-pos %)) (vals ships)))]
                (assoc ships new-player (create-ship new-player taken-ship-pos-x))))
            ships
            new-players)))

(defn remove-players-not-in-game-anymore
  [ships players]
  (let [players-to-remove (set/difference (set (keys ships))
                                          (set (keys players)))]
    (apply dissoc ships players-to-remove)))

(defn move-ship-left
  [ship]
  (assoc ship :pos (update (:base-pos ship) 0 - ship-move-offset)))

(defn move-ship-right
  [ship]
  (assoc ship :pos (update (:base-pos ship) 0 + ship-move-offset)))

(defn move-ships-to-player-choice
  [ships players]
  (reduce (fn [ships player-name]
            (condp = (get-in players [player-name :choice])
              :left (update ships player-name move-ship-left)
              :right (update ships player-name move-ship-right)
              ships))
          ships
          (keys ships)))

(defn create-death-meteor
  [direction]
  (let [x-positions (if (= :right direction)
                      (range 100 (/ canvas-width 2) 60)
                      (range -100 (- (/ canvas-width 2)) -60))]
    (shuffle (map create-planet x-positions))))

(defn create-animations
  [animations]
  (let [wrong-choice (get-in @state/state [:game :wrong-choice])]
    (if (and (empty? animations) (not (nil? wrong-choice)))
      (create-death-meteor wrong-choice)
      animations)))

(defn delete-old-animations
  [animations]
  (remove #(< 150 (second (:pos %))) animations))

(defn kill-ships!
  [animations]
  (when (not (empty? animations))
    (let [average-y (/ (apply + (map #(second (:pos %)) animations))
                       (count animations))]
      (when (< 0 average-y)
        ((:channel-send-fn @animation-state) [:casino2016.game/kick-loosers]))))
  animations)

(defn update-death-animations
  [animations]
  (->> animations
       create-animations
       (map drift-planet)
       delete-old-animations
       kill-ships!
       (into [])))

(defn update-state [state]
  (let [players (get-in @state/state [:game :players])]
    (-> state
        (update :ships add-new-players players)
        (update :ships remove-players-not-in-game-anymore players)
        (update :ships move-ships-to-player-choice players)
        (update :ships wiggle-ships)
        (update :ships update-smoke-all-ships)
        (update-in [:stars] move-objects)
        (update-in [:stars] move-star-in-screen)
        (update-in [:death-animations] update-death-animations))))

(defn draw-entity [entity [cam-x cam-y]]
  (let [[x y] (:pos entity)
        dir (:dir entity)
        z (:z entity)
        render-fn (:render-fn entity)
        screen-x (- x cam-x)
        screen-y (- y cam-y)]
    (when (on-screen? screen-x screen-y)
      (q/push-matrix)
      (q/translate screen-x screen-y)
      (q/rotate dir)
      (render-fn entity)
      (q/pop-matrix))))

(defn draw-state [state]
  (when-not (:run-animation? @animation-state) (q/exit))
  (q/background (pulse 20 40  15.0)
                (pulse 40 60 40.0)
                (pulse 50 70 5.0))
  (q/no-stroke)
  (doseq [star (:stars state)]
    (draw-entity star cam-pos))
  (doseq [[_ ship] (:ships state)]
    (doseq [smoke (:smoke ship)]
      (draw-entity smoke cam-pos))
    (draw-entity ship cam-pos))
  (doseq [meteor (:death-animations state)]
    (draw-entity meteor cam-pos)))

(q/defsketch nanoscopic
  :host "game-canvas"
  :features [:no-start]
  :size [canvas-width canvas-height]
  :setup setup
  :update update-state
  :draw draw-state
  :middleware [m/fun-mode])

(defn game-container
  []
  [:div.game-page
   [:h1.game-title "2016: A Casino Odyssey"]
   (when (get-in @state/state [:game :in-preparation])
     [:div.qrcode
      [:h3 "Join the game now!"]
      [:h4 "Instructions"]
      [:ol
       [:li "Scanne le QR code"]
       [:li "Paie tes jetons à la table"]
       [:li "Essaie de survivre!"]]])
   [:canvas#game-canvas]
   (when-let [winner (get-in @state/state [:game :last-man-standing])]
     [:div.blink.game-winner
      [:div.game-winner-logo]
      [:p.game-winner-text (upper-case (str winner " gagne"))]
      [:div.game-winner-logo]])])

(defn page
  [chsk-send!]
  (swap! animation-state assoc :channel-send-fn chsk-send!)
  (reagent/create-class
   {:reagent-render game-container
    :component-did-mount nanoscopic
    :component-will-unmount #(swap! animation-state assoc :run-animation? false)}))
