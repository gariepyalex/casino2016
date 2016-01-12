(ns casino2016.core
  (:require [org.httpkit.server :as httpkit]
            [casino2016.handler :as handler]))

(def PORT 8000)
(defonce server (atom nil))

(defn start-server
  [port]
  (reset! server (httpkit/run-server handler/app {:port port}))
  (println "server started on port " port))

(defn stop-server
  []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)))

(defn -main
  [& port]
  (start-server (or port PORT)))
