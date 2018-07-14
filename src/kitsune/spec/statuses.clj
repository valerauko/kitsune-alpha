(ns kitsune.spec.statuses
  (:require [clojure.spec.alpha :as s]))

(s/def ::text
  (s/and string?
         #(< (count %) 500)))

; TODO: use proper check
(s/def ::in-reply-to
  int?)
(s/def ::attachments
  coll?)
(s/def ::to
  coll?)
(s/def ::cc
  coll?)

(s/def ::create-status-request
  (s/keys :req-un [::text]
          :opt-un [::in-reply-to ::attachments ::to ::cc]))
