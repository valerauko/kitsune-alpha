(ns kitsune.specs.user
  (:require [clojure.spec.alpha :as s]))

(s/def ::name
  (s/and string?
         #(re-matches #"?i[a-z0-9][a-z0-9-_]{2,15}")))

(s/def ::email
  (s/and string?
         #(re-matches #"?i.+@.+")))

(s/def ::user
  (s/keys :req [::name ::email]))
