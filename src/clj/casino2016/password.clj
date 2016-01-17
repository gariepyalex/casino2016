(ns casino2016.password
  (:require [clojure.java.io :as io]))

(def password-path "resources/password")

(defn set-admin-password!
  [password]
  (spit password-path password))

(defn is-admin?
  [password]
  (= password (slurp password-path)))
