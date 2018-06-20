(ns kitsune.spec.webfinger
  (:require [clojure.spec.alpha :as s]))

(s/def ::resource
  (s/and string?
         #(re-matches #"(?i)acct:(\w+@(.+))" %)))

(s/def ::subject ::resource)

(s/def ::aliases
  (s/coll-of string?))

(s/def ::rel string?)
(s/def ::href string?)
(s/def ::template string?)
(s/def ::type string?)

(s/def ::links
  (s/coll-of (s/keys :req-un [::rel]
                     :opt-un [::href ::type ::template])))

(s/def ::json
  (s/keys :req-un [::subject]
          :opt-un [::aliases ::links]))
