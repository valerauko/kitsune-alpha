(ns kitsune.spec.mastodon.attachment
  (:require [clojure.spec.alpha :as s]
            [kitsune.spec.common :as common]))

(s/def ::shortcode string?)
(s/def ::static-url ::common/url)
(s/def ::emoji
  (s/keys :req-un [::shortcode ::common/url ::static-url]))
