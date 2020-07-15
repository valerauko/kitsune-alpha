(defproject social.kitsune/kitsune "0.2.0"
  :description "Very fox microblogging service"
  :url "https://kitsune.social"
  :license {:name "GNU AFFERO GENERAL PUBLIC LICENSE"
            :url "https://www.gnu.org/licenses/agpl-3.0.en.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/core.async "1.2.603"]
                 [org.clojure/tools.namespace "1.0.0"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [commons-codec/commons-codec "1.14"]
                 [org.bouncycastle/bcprov-jdk15on "1.66"]
                 [org.bouncycastle/bcpkix-jdk15on "1.66"]
                 [cprop "0.1.17"]
                 [mount "0.1.16"]
                 [aleph "0.4.6"]
                 [byte-streams "0.2.4"]
                 [camel-snake-kebab "0.4.1"]
                 [metosin/reitit "0.5.5"]
                 [metosin/spec-tools "0.10.3"]
                 [metosin/muuntaja "0.6.7"]
                 [metosin/ring-http-response "0.9.1"]
                 [metosin/jsonista "0.2.6"]
                 [ring/ring-defaults "0.3.2"]
                 [org.bovinegenius/exploding-fish "0.3.6"]
                 [org.postgresql/postgresql "42.2.14"]
                 [com.layerware/hugsql "0.5.1"]
                 [hikari-cp "2.12.0"]
                 [ragtime "0.8.0"]
                 [social.kitsune/vuk "0.2.0"]]
  :main ^:skip-aot kitsune.core
  :target-path "target/%s"
  :jvm-opts ["-Xmx600m" "-server"]
  :profiles {:uberjar {:aot :all
                       :source-paths ["config/prod"]
                       :uberjar-name "kitsune.jar"}
             :dev {:dependencies [[ring/ring-devel "1.8.1"]
                                  [org.clojure/test.check "1.1.0"]
                                  [proto-repl "0.3.1"]]
                   :source-paths ["config/dev"]
                   :plugins [[jonase/eastwood "0.2.9"]
                             [lein-ancient "0.6.15"]]}})
