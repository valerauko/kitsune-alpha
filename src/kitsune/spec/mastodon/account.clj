(ns kitsune.spec.mastodon.account
  (:require [clojure.spec.alpha :as s]
            [kitsune.spec.common :as common]))

(s/def ::username string?)
(s/def ::acct string?)
(s/def ::display-name string?)

(s/def ::url ::common/url)

(s/def ::avatar ::url)
(s/def ::avatar-static ::url)
(s/def ::header ::url)
(s/def ::header-static ::url)

(s/def ::locked ::common/bool)
(s/def ::bot ::common/bool)

(s/def ::followers-count nat-int?)
(s/def ::following-count nat-int?)
(s/def ::statuses-count nat-int?)

(s/def ::note string?)

(s/def ::moved ::url)

(s/def ::name string?)
(s/def ::value string?)
(s/def ::field
  (s/keys :req-un [::name ::value]))
(s/def ::fields
  (s/and (s/coll-of ::field)
         #(< (count %) 5)))

(s/def ::account
  (s/keys :req-un [::common/id ::username ::acct ::display-name ::url ::locked
                   ::avatar ::avatar-static ::header ::header-static
                   ::common/created-at ::followers-count ::following-count
                   ::statuses-count ::note]
          :opt-un [::moved ::fields ::bot]))
