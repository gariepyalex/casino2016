(ns casino2016.handler
  (:require [compojure.core :include-macros true :refer [defroutes GET]]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.response :as response]
            [hiccup.page :as hiccup]))

(def web-app (hiccup/html5
              [:html
               [:head
                [:title "IFT-GLO CASINO 2016"]
                [:meta {:charset "UTF-8"}]
                [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]]
               [:body
                [:div#app]
                (hiccup/include-js "js/compiled/app.js")]]))

(defroutes handler
  (GET "/" [] web-app)
  (GET "/*" [] (response/redirect "/"))
  (route/resources "/"))

(def app (wrap-defaults handler site-defaults))
