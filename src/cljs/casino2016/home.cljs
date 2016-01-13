(ns casino2016.home)

(defn page
  []
  [:div
   [:h1 "Casino IFT-GLO 2016"]
   [:p [:a {:href "/#/admin"} "Admin"]]
   [:p [:a {:href "/#/game"} "Game"]]
   [:p [:a {:href "/#/player"} "Player"]]])
