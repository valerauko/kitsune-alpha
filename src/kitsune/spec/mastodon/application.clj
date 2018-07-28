(ns kitsune.spec.mastodon.application
  (:require [clojure.spec.alpha :as s]
            [kitsune.spec.common :as common]))

(s/def ::name string?)
(s/def ::website ::common/url)

(s/def ::application
  (s/keys :req-un [::name]
          :opt-un [::website]))
