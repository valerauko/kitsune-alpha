(ns kitsune.spec.mastodon.account
  (:require [clojure.spec.alpha :as s]
            [org.bovinegenius [exploding-fish :as uri]]))

(s/def ::id pos-int?)
(s/def ::username string?)
(s/def ::acct string?)
(s/def ::display-name string?)

(s/def ::url
  (s/with-gen
    uri/absolute?
    #(s/gen uri?)))

(s/def ::avatar ::url)
(s/def ::avatar-static ::url)
(s/def ::header ::url)
(s/def ::header-static ::url)

(s/def ::bool
  (s/or :bool boolean?
        :str #{"true" "false"}))

(s/def ::locked ::bool)
(s/def ::bot ::bool)

(s/def ::created-at inst?)

(s/def ::followers-count nat-int?)
(s/def ::following-count nat-int?)
(s/def ::statuses-count nat-int?)

(s/def ::note string?)

(s/def ::moved ::url)

(s/def ::name string?)
(s/def ::value string?)
(s/def ::field
  (s/and (s/keys :req-un [::name ::value])
         #(< (count %) 5)))
(s/def ::fields (s/coll-of ::field))

(s/def ::account
  (s/keys :req-un [::id ::username ::acct ::display-name ::url ::locked
                   ::avatar ::avatar-static ::header ::header-static
                   ::created-at ::followers-count ::following-count
                   ::statuses-count ::note]
          :opt-un [::moved ::fields ::bot]))
