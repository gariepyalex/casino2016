(ns casino2016.handler
  (:require [compojure.core :include-macros true :refer [defroutes GET]]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [hiccup.page :as hiccup]))

(def page (hiccup/html5
           [:html
            [:head
             [:title "IFT-GLO CASINO 2016"]
             [:meta {:charset "UTF-8"}]
             [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]]
            [:body
             [:canvas#quil-canvas]
             (hiccup/include-js "js/compiled/app.js")]]))

(defroutes handler
  (GET "/" [] page)
  (GET "/toto" [] "balblalbal")
  (route/resources "/"))

(def app (wrap-defaults handler site-defaults))
