(defproject social.kitsune/kitsune "0.1.0"
  :description "Very fox microblogging service"
  :url "http://example.com/FIXME"
  :license {:name "GNU AFFERO GENERAL PUBLIC LICENSE"
            :url "https://www.gnu.org/licenses/agpl-3.0.en.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.4.474"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [cprop "0.1.11"]
                 [aleph "0.4.6"]
                 [ring-logger "1.0.1"]
                 [camel-snake-kebab "0.4.0"]
                 [buddy/buddy-core "1.5.0"]
                 [metosin/reitit "0.1.3"]
                 [metosin/muuntaja "0.5.0"]
                 [metosin/ring-http-response "0.9.0"]
                 [ring/ring-defaults "0.3.2"]
                 [org.bovinegenius/exploding-fish "0.3.5"]
                 [org.postgresql/postgresql "42.2.4"]
                 [com.layerware/hugsql "0.4.9"]
                 [hikari-cp "2.6.0"]
                 [ragtime "0.7.2"]
                 [social.kitsune/vuk "0.0.2"]]
  :aliases {"migrate"  ["run" "-m" "kitsune.db.migrations/migrate"]
            "rollback" ["run" "-m" "kitsune.db.migrations/rollback"]}
  :main ^:skip-aot kitsune.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :source-paths ["config/prod"]}
             :dev {:dependencies [[ring/ring-devel "1.7.0-RC1"]]
                   :source-paths ["config/dev"]
                   :plugins [[jonase/eastwood "0.2.6"]
                             [lein-ancient "0.6.15"]]}})
