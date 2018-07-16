(ns kitsune.instance
  (:require [cprop.core :refer [load-config cursor]]
            [org.bovinegenius [exploding-fish :refer [uri path]]]))

(def version "0.1.0")

(def config
  (load-config))

(def instance
  (cursor config :instance))

(def db-config
  (cursor config :db))

(def server-config
  (cursor config :server))

(def env
  (config :env))

(defn url
  ([]
    (uri {:scheme (server-config :protocol)
          :host (server-config :host)
          :port (server-config :port)}))
  ([new-path]
    (path (url) new-path)))
