(ns casino2016.util
  (:require [reagent.cookies :as cookies]))

(defn session-cookie
  []
  (cookies/get "ring-session"))
