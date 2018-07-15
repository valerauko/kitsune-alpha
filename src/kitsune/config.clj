(ns kitsune.config
  (:require [cprop.core :refer [load-config cursor]]
            [org.bovinegenius [exploding-fish :refer [uri path]]]))

(def version "0.1.0")

(def config
  (load-config))

(def db-config
  (cursor config :db))

(def server-config
  (cursor config :server))

(defn url
  ([]
    (uri {:scheme (server-config :protocol)
          :host (server-config :host)
          :port (server-config :port)}))
  ([new-path]
    ; HACK: exploding fish won't update path unless string
    (path (str (url)) new-path)))
