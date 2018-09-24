(ns kitsune.federators.user
  (:require [aleph.http :as http]
            [byte-streams :as bs]
            [org.bovinegenius [exploding-fish :as uri]]
            [jsonista.core :as json]))

(defn refetch-profile
  [user-uri]
  (let [result (-> @(http/get user-uri
                              {:headers {:accept "application/activity+json"}})
                   :body
                   json/read-value)]
    result))
