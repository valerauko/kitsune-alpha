(ns kitsune.instance
  (:require [cprop.core :refer [load-config cursor]]
            [org.bovinegenius [exploding-fish :refer [uri path]]]
            [mount.core :refer [defstate]]))

(def version "0.1.0")

(defstate config
  :start (load-config))

(defn url
  ([]
    (uri (select-keys (config :server) [:scheme :host])))
  ([& new-path]
    (path (url) (reduce str new-path))))
