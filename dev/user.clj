(ns user
  (:require [figwheel-sidecar.repl :as r]
            [figwheel-sidecar.repl-api :as ra]
             casino2016.handler))

(def figwheel-config
  (let [builds (r/get-project-cljs-builds)]
    {:figwheel-options {:css-dirs ["resources/public/css"]
                        :server-port 3449
                        :ring-handler casino2016.handler/app}
     :build-ids ["dev"]
     :all-builds builds}))

(defn start
  []
  (casino2016.handler/event-loop)
  (ra/start-figwheel! figwheel-config))

(defn stop
  []
  (ra/stop-figwheel!))

(defn cljs
  []
  (ra/cljs-repl))
