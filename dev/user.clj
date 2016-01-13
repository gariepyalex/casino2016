(ns user
  (:require [figwheel-sidecar.repl :as r]
            [figwheel-sidecar.repl-api :as ra]
             casino2016.handler))

(def default-config
  (let [builds (r/get-project-cljs-builds)]
    {:figwheel-options {:css-dirs ["resources/public/css"]
                        :server-port 3449
                        :ring-handler casino2016.handler/app}
     :build-ids ["dev"]
     :all-builds builds}))

(defn config-with-websocket
  [ip-address]
  (->> default-config
       :all-builds
       (map #(assoc % :websocket-host ip-address))
       (into [])
       (assoc default-config :all-builds)))

(defn start
  ([config]
   (casino2016.handler/event-loop)
   (ra/start-figwheel! config))
  ([]
   (start default-config)))

(defn start-with-websocket
  [ip-address]
  (start (config-with-websocket ip-address)))

(defn stop
  []
  (ra/stop-figwheel!))

(defn cljs
  []
  (ra/cljs-repl))
