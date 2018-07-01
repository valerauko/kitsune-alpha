(ns kitsune.spec.user
  (:require [clojure.spec.alpha :as s]))

(s/def ::name
  (s/and string?
         #(re-matches #"(?i)[a-z0-9][a-z0-9-_]{2,25}" %)))

(s/def ::display-name
  (s/or :empty nil?
        :string (s/and string?
                       #(-> % count (< 25)))))

(s/def ::email
  (s/and string?
         #(re-matches #"(?i).+@.+" %)))

(s/def ::pass
  (s/and string?
         #(re-matches #"\S.+\S" %) ; must start and end with nonspace chars
         #(re-find #"[a-z]" %) ; must contain at least one small letter
         #(re-find #"[A-Z]" %) ; must contain at least one capital letter
         #(re-find #"[0-9]" %) ; must contain at least one number
         #(re-matches #".{8,}" %))) ; must be at least 8 characters long

(s/def ::pass-confirm ::pass)
(s/def ::pass-match #(= (::pass %) (::pass-confirm %)))

(s/def ::created-at inst?)

(s/def ::show
  (s/keys :req-un [::name ::created-at]
          :opt-un [::display-name]))

(s/def ::registration
  (s/and
    (s/keys :req-un [::name ::email ::pass ::pass-confirm])
    ::pass-match))

(s/def ::profile-update
  (s/and
    (s/keys :opt-un [::name ::display-name ::pass ::pass-confirm])
    #(if (and (nil? (::pass %)) (nil? (::pass-confirm %)))
      true
      ::pass-match)))
