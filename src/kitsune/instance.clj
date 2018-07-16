(ns kitsune.instance
  (:require [cprop.core :refer [load-config cursor]]
            [org.bovinegenius [exploding-fish :refer [uri path]]]
            [mount.core :refer [defstate]]))

(def version "0.1.0")

(defstate config
  :start (load-config))

(defstate instance
  :start (cursor config :instance))

(defstate db-config
  :start (cursor config :db))

(defstate server-config
  :start (let [opts (cursor config :server)]
           {:scheme (opts :protocol)
            :host (opts :host)
            :port (opts :port)}))

(defstate env
  :start (config :env))

(defn url
  ([]
    (uri server-config))
  ([new-path]
    (path (url) new-path)))
