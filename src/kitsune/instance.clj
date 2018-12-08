(ns kitsune.instance
  (:require [cprop.core :refer [load-config]]
            [cprop.source :refer [from-env from-system-props]]
            [org.bovinegenius [exploding-fish :refer [uri path]]]
            [mount.core :refer [defstate]]))

(defstate config
  :start (load-config :merge [(from-env)
                              (from-system-props)]))

(def version
  (:kitsune-version config))

(defn url
  ([]
    (uri (select-keys (config :server) [:scheme :host])))
  ([& new-path]
    (path (url) (reduce str new-path))))
