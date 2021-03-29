(defproject social.kitsune/kitsune "0.2.0"
  :description "Very fox microblogging service"
  :url "https://kitsune.social"
  :license {:name "GNU AFFERO GENERAL PUBLIC LICENSE"
            :url "https://www.gnu.org/licenses/agpl-3.0.en.html"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/tools.namespace "1.1.0"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [ch.qos.logback.contrib/logback-json-classic "0.1.5"]
                 [ch.qos.logback.contrib/logback-jackson "0.1.5"]
                 [cprop "0.1.17"]
                 [mount "0.1.16"]
                 [aleph "0.4.6"]
                 [byte-streams "0.2.4"]
                 [camel-snake-kebab "0.4.2"]
                 [metosin/reitit "0.5.12"]
                 [metosin/spec-tools "0.10.5"]
                 [metosin/muuntaja "0.6.8"]
                 [metosin/ring-http-response "0.9.2"]
                 [metosin/jsonista "0.3.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring-cors "0.1.13"]
                 [org.bovinegenius/exploding-fish "0.3.6"]
                 [org.postgresql/postgresql "42.2.19"]
                 [com.layerware/hugsql "0.5.1"]
                 [hikari-cp "2.13.0"]
                 [ragtime "0.8.1"]
                 [social.kitsune/vuk "0.2.1"]
                 [social.kitsune/csele "0.5.0"]
                 [social.kitsune/karak "0.1.2"]
                 [cljstache "2.0.6"]]
  :main ^:skip-aot kitsune.core
  :target-path "target/%s"
  :jvm-opts ["-Xmx600m" "-server"]
  :profiles {:uberjar {:aot :all
                       :source-paths ["config/prod"]
                       :uberjar-name "kitsune.jar"}
             :dev {:dependencies [[org.clojure/test.check "1.1.0"]]
                   :source-paths ["config/dev"]
                   :plugins [[jonase/eastwood "0.3.14"
                              :exclusions [org.clojure/clojure]]
                             [lein-kibit "0.1.8"]
                             [lein-cloverage "1.2.2"
                              :exclusions [org.clojure/clojure]]
                             [lein-ancient "0.7.0"]]}})
