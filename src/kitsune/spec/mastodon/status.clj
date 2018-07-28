(ns kitsune.spec.mastodon.status
  (:require [clojure.spec.alpha :as s]
            [kitsune.spec.common :as common]
            [kitsune.spec.mastodon.account :as account]
            [kitsune.spec.mastodon.application :as application]
            [kitsune.spec.mastodon.attachment :as attachment]))

(s/def ::uri ::common/url)
(s/def ::url ::common/url)

(s/def ::in-reply-to-id nat-int?)
(s/def ::in-reply-to-account-id nat-int?)

(s/def ::reblog ::common/url) ; actually ::status but that breaks gen

(s/def ::content string?)

(s/def ::reblogs-count nat-int?)
(s/def ::favourites-count nat-int?)

(s/def ::reblogged ::common/bool)
(s/def ::favourited ::common/bool)
(s/def ::muted ::common/bool)

(s/def ::pinned ::common/bool)

(s/def ::sensitive ::common/bool)
(s/def ::spoiler-text string?)

(s/def ::visibility #{"public" "unlisted" "private" "direct"})

(s/def ::media-attachments
  (s/coll-of ::attachment/attachment))
(s/def ::emojis
  (s/coll-of ::attachment/emoji))

(s/def ::mentions
  (s/coll-of ::account/mention))

(s/def ::language string?)

(s/def ::name string?)
(s/def ::tag
  (s/keys :req-un [::name ::common/url]))
(s/def ::tags
  (s/coll-of ::tag))

(s/def ::status
  (s/keys :req-un [::common/id ::account/account ::uri ::common/created-at
                   ::content ::reblogs-count ::favourites-count
                   ::emojis ::media-attachments ::mentions ::tags
                   ::sensitive ::spoiler-text ::visibility]
          :opt-un [::url ::in-reply-to-id ::in-reply-to-account-id
                   ::reblog ::reblogged ::favourited ::muted
                   ::application/application ::language ::pinned]))
