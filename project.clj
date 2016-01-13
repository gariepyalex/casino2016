(defproject casino2016 "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [org.clojure/core.async "0.2.374"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [compojure "1.4.0"]
                 [com.taoensso/sente "1.7.0"]
                 [reagent "0.5.1"]
                 [reagent-utils "0.1.7"]
                 [secretary "1.2.3"]
                 [http-kit "2.1.18"]
                 [quil "2.3.0"]
                 [hiccup "1.0.5"]]

  :min-lein-version "2.5.3"
  :source-paths ["src/clj"]
  :resource-paths ["resources"]

  :main casino2016.core

  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-figwheel "0.5.0-3"]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {:builds [{:id "dev"
                        :figwheel {:on-jsload "casino2016.core/on-js-reload"}
                        :source-paths ["src/cljs"]
                        :compiler {:main casino2016.core
                                   :asset-path "js/compiled/out"
                                   :output-to "resources/public/js/compiled/app.js"
                                   :output-dir "resources/public/js/compiled/out/"
                                   :optimizations :none
                                   :source-map-timestamp true}}
                       {:id "min"
                        :source-paths ["src/cljs"]
                        :compiler {:output-to "resources/public/js/compiled/app.js"
                                   :main casino2016.core
                                   :optimizations :advanced
                                   :pretty-print false}}]}

  :figwheel {:css-dirs ["resources/public/css"]
             :ring-handler casino2016.handler/app}

  :profiles {:dev {:dependencies [[com.cemerick/piggieback "0.2.1"]
                                  [figwheel-sidecar "0.5.0-3"]]

                   :source-paths ["dev" "src/cljs"]

                   :repl-options {:init-ns user
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}})

