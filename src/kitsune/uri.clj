(ns kitsune.uri
  (:require [org.bovinegenius [exploding-fish :as uri]]
            [kitsune.instance :refer [config]]))

(defn local?
  "Checks if the host of the input URI is the same as the configured local host"
  [input]
  (= (get-in config [:server :host])
     (uri/host input)))
