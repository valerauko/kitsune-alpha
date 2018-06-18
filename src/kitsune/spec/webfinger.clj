(ns kitsune.spec.webfinger
  (:require [clojure.spec.alpha :as s]))

(s/def ::resource
  (s/and string?
         #(re-matches #"(?i)acct:(\w+@(.+))" %)))
