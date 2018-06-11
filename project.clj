(defproject kitsune "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [ring-logger "0.7.7"]
                 [metosin/reitit "0.1.2"]
                 [metosin/muuntaja "0.5.0"]
                 [ring/ring-defaults "0.3.2"]
                 [migratus "1.0.0"]]
  :plugins [[lein-ring "0.9.7"]
            [migratus-lein "0.5.4"]]
  :ring {:handler kitsune.handler/app}
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
