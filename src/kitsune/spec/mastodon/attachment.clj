(ns kitsune.spec.mastodon.attachment
  (:require [clojure.spec.alpha :as s]
            [kitsune.spec.common :as common]))

(s/def ::shortcode string?)
(s/def ::static-url ::common/url)
(s/def ::emoji
  (s/keys :req-un [::shortcode ::common/url ::static-url]))

(s/def ::type #{"image" "video" "gifv" "unknown"})
(s/def ::remote-url ::common/url)
(s/def ::preview-url ::common/url)
(s/def ::text-url ::common/url)
(s/def ::meta map?) ; TODO: this gonna need some investigation
(s/def ::description string?)

(s/def ::attachment
  (s/keys :req-un [::common/id ::type ::common/url ::preview-url]
          :opt-un [::remote-url ::text-url ::meta ::description]))
