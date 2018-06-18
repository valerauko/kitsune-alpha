(defproject kitsune "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "GNU AFFERO GENERAL PUBLIC LICENSE"
            :url "https://www.gnu.org/licenses/agpl-3.0.en.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [aleph "0.4.6"]
                 [ring-logger "1.0.1"]
                 [camel-snake-kebab "0.4.0"]
                 [buddy/buddy-core "1.4.0"]
                 [metosin/reitit "0.1.2"]
                 [metosin/muuntaja "0.5.0"]
                 [metosin/ring-http-response "0.9.0"]
                 [ring/ring-defaults "0.3.2"]
                 [org.postgresql/postgresql "42.2.2"]
                 [com.layerware/hugsql "0.4.9"]
                 [hikari-cp "2.5.0"]
                 [ragtime "0.7.2"]
                 [vuk "0.0.1"]]
  :aliases {"migrate"  ["run" "-m" "kitsune.db.migrations/migrate"]
            "rollback" ["run" "-m" "kitsune.db.migrations/rollback"]}
  :main ^:skip-aot kitsune.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[ring/ring-devel "1.7.0-RC1"]]}})
