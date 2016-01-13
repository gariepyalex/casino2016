(ns casino2016.player)

(defn page
  []
  [:div
   [:h2 "Choose a direction"]
   [:div.player-arrow-container
    [:div.player-arrow.player-arrow-left {:on-click #(js/alert "left")}]
    [:div.player-arrow.player-arrow-right {:on-click #(js/alert "right")}]]])
