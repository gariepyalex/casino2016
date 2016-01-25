(ns casino2016.core
  (:gen-class)
  (:require [environ.core :refer [env]]
            [clojure.core.async :refer [close!]]
            [org.httpkit.server :as httpkit]
            [casino2016.handler :as handler]))

(def PORT 8000)
(defonce server (atom nil))
(defonce server-event-loop (atom nil))

(defn start-server
  [port]
  (reset! server-event-loop (handler/event-loop))
  (reset! server (httpkit/run-server handler/app {:port port}))
  (println "server started on port " port))

(defn stop-server
  []
  (when-not (nil? @server)
    (@server :timeout 100)
    (close! @server-event-loop)
    (reset! server nil)
    (reset! server-event-loop nil)))

(defn -main
  [& port]
  (start-server (Integer. (or port (env :port) PORT))))
