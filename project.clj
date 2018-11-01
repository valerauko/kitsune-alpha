(defproject social.kitsune/kitsune "0.1.0"
  :description "Very fox microblogging service"
  :url "http://example.com/FIXME"
  :license {:name "GNU AFFERO GENERAL PUBLIC LICENSE"
            :url "https://www.gnu.org/licenses/agpl-3.0.en.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.4.474"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [commons-codec/commons-codec "1.11"]
                 [org.bouncycastle/bcprov-jdk15on "1.60"]
                 [org.bouncycastle/bcpkix-jdk15on "1.60"]
                 [cprop "0.1.13"]
                 [mount "0.1.14"]
                 [aleph "0.4.6"]
                 [byte-streams "0.2.4"]
                 [ring-logger "1.0.1"]
                 [camel-snake-kebab "0.4.0"]
                 [metosin/reitit "0.2.5"]
                 [metosin/spec-tools "0.8.0"]
                 [metosin/muuntaja "0.6.1"]
                 [metosin/ring-http-response "0.9.0"]
                 [metosin/jsonista "0.2.2"]
                 [ring/ring-defaults "0.3.2"]
                 [org.bovinegenius/exploding-fish "0.3.6"]
                 [org.postgresql/postgresql "42.2.5"]
                 [com.layerware/hugsql "0.4.9"]
                 [hikari-cp "2.6.0"]
                 [ragtime "0.7.2"]
                 [social.kitsune/vuk "0.1.0"]]
  :main ^:skip-aot kitsune.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :source-paths ["config/prod"]
                       :uberjar-name "kitsune.jar"}
             :dev {:dependencies [[ring/ring-devel "1.7.1"]
                                  [org.clojure/test.check "0.9.0"]
                                  [proto-repl "0.3.1"]]
                   :source-paths ["config/dev"]
                   :plugins [[jonase/eastwood "0.2.9"]
                             [lein-ancient "0.6.15"]]}})
