(ns kitsune.spec.oauth
  (:require [clojure.spec.alpha :as s]
            [clojure.string :refer [split]]
            [kitsune.spec.user :as user]))

(s/def ::name string?)
(s/def ::scopes string?)
(s/def ::scope-array
  (s/coll-of #{"read" "write" "follow"} :distinct true
             :min-count 1 :max-count 3))
; TODO: proper url validation
(s/def ::website string?)
(s/def ::redirect-uri string?)
(s/def ::redirect-uris string?)

(s/def ::create-app
  (s/keys :req-un [::name ::scopes]
          :opt-un [::website ::redirect-uris]))

(s/def ::random-hash
  (s/and string?
         #(-> % count (= 44))))

(s/def ::id int?)
(s/def ::client-id ::random-hash)
(s/def ::secret ::random-hash)
(s/def ::client-secret ::secret)

(s/def ::register-response
  (s/keys :req-un [::id ::client-id ::secret]))

(s/def ::authorization
  (s/and string?
         #(re-matches #"(?:Bearer|Basic) \S+" %)))

(def auth-header-opt
  {:header (s/keys :opt-un [::authorization])})

(def auth-header-req
  {:header (s/keys :req-un [::authorization])})

(defn valid-scope
  [input]
  (let [scopes (split input #"\s+")]
    (if (s/valid? ::scope-array scopes)
      ; need to sort it for certain equality
      (sort scopes))))

(s/def ::password ::user/pass)
(s/def ::grant-type #{"authorization_code" "password" "refresh_token"})

(s/def ::state string?)
(s/def ::authorize-params
  (s/keys :req-un [::user/email ::password ::client-id]
          :opt-un [::redirect-uri ::scopes ::state]))

(s/def ::exchange-by-auth
  (s/keys :req-un [::user/email ::password]))

(s/def ::code string?)

(s/def ::exchange-by-code
  (s/keys :req-un [::code]))

(s/def ::exchange-request
  (s/merge (s/or :code ::exchange-by-code
                 :pass ::exchange-by-auth)
           (s/keys :req-un [::grant-type]
                   :opt-un [::client-id ::client-secret])))
