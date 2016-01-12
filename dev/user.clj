(ns user
  (:require [figwheel-sidecar.repl :as r]
            [figwheel-sidecar.repl-api :as ra]
             casino2016.handler))

(def figwheel-config
  (let [builds (r/get-project-cljs-builds)]
    {:figwheel-options {:css-dirs ["resources/css"]
                        :server-port 3448
                        :reload-clj-files true
                        :ring-handler casino2016.handler/app}
     :build-ids (into [] (map :id builds))
     :all-builds builds}))

(defn start
  []
  (ra/start-figwheel! figwheel-config))

(defn stop
  []
  (ra/stop-figwheel!))

(defn cljs
  []
  (ra/cljs-repl "dev"))
