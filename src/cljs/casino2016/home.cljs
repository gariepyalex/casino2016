(ns casino2016.home)

(defn page
  []
  [:div
   [:h1 "Casino IFT-GLO 2016"]
   [:p [:a {:href "/#/admin"} "admin"]]
   [:p [:a {:href "/#/game"} "game"]]
   [:p [:a {:href "/#/player"} "player"]]])
