(ns kitsune.spec.common
  (:require [clojure.spec.alpha :as s]
            [org.bovinegenius [exploding-fish :refer [absolute?]]]))

(s/def ::id pos-int?)

(s/def ::url
  (s/with-gen
    absolute?
    #(s/gen uri?)))

(s/def ::bool
  (s/or :bool boolean?
        :str #{"true" "false"}))

(s/def ::created-at inst?)
