(ns kitsune.spec.statuses
  (:require [clojure.spec.alpha :as s]
            [kitsune.spec.mastodon.status :as mastodon]))

(s/def ::status
  (s/and string?
         #(< (count %) 500)))
(s/def ::media-ids
  (s/coll-of nat-int?))

(s/def ::create-status-request
  (s/keys :req-un [::status]
          :opt-un [::mastodon/in-reply-to-id ::media-ids
                   ::mastodon/sensitive ::mastodon/spoiler-text
                   ::mastodon/visibility ::mastodon/language]))
