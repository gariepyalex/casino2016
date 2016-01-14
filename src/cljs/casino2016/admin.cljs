(ns casino2016.admin)

(def test-data (atom {:players [{:id 1
                                 :name "good"
                                 :status :pending}
                                {:id 2
                                 :name "bad"
                                 :status :accepted}
                                {:id 3
                                 :name "ugly"
                                 :status :pending}
                                {:id 4
                                 :name "blondie"
                                 :status :pending}
                                {:id 5
                                 :name "dude"
                                 :status :pending}
                                {:id 6
                                 :name "demarco"
                                 :status :accepted}
                                {:id 7
                                 :name "bart"
                                 :status :accepted}]

                      :number-of-players 3

                      :max-number-of-players 8}))

(defn player-view
  []
  [:div
   [:h3 (str "Players ("
             (:number-of-players @test-data)
             "/"
             (:max-number-of-players @test-data)
             ")")]
   [:ul (for [{:keys [name status id]} (:players @test-data)]
          (into [:li.admin-player-entry]
                (if (= status :pending)
                  [[:p.admin-accepted-player name] [:button "accept"]]
                  [[:p.admin-pending-player name] [:button "kick"]])))]])

(defn page
  [chsk-send!]
  (fn []
    [:div
     [:h1 "Admin"]
     [player-view]
     [:h3 "Actions"]
     [:div.admin-actions
      [:button "Start game"]
      [:button "Reset"]]]))
