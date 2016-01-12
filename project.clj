(defproject casino2016 "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [compojure "1.4.0"]
                 [reagent "0.5.1"]
                 [http-kit "2.1.18"]
                 [quil "2.3.0"]
                 [hiccup "1.0.5"]]

  :min-lein-version "2.5.3"
  :source-paths ["src/clj"]
  :resource-paths ["resources"]

  :main casino2016.core

  :plugins [[lein-cljsbuild "1.1.1"]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {:builds {:app {:source-paths ["src/cljs"]
                             :compiler {:main casino2016.core
                                        :output-to "resources/public/js/compiled/app.js"
                                        :output-dir "resources/public/js/compiled/out/"
                                        :source-map "resources/public/js/compiled/out.js.map"
                                        :asset-path "js/compiled/out"
                                        :warning true
                                        :optimizations :none
                                        :source-map-timestamp true
                                        :pretty-print true}}}}

  :profiles {:dev {:dependencies [[figwheel "0.5.0-1"]
                                  [com.cemerick/piggieback "0.2.1"]
                                  [figwheel-sidecar "0.5.0-1"]]

                   :source-paths ["dev"]

                   :plugins [[lein-figwheel "0.5.0-1"]]

                   :repl-options {:init-ns user
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}})

