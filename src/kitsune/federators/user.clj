(ns kitsune.federators.user
  (:require [aleph.http :as http]
            [org.bovinegenius [exploding-fish :as uri]]
            [clojure.tools.logging :as log]
            [jsonista.core :as json]))

(defn refetch-profile
  [user-uri]
  (log/info (str "Fetching profile of " user-uri))
  (try
    (let [result (-> @(http/get user-uri
                                {:headers {:accept "application/activity+json"}})
                     :body
                     json/read-value)]
      result)
    (catch Throwable ex
      (log/error ex (str "Error while fetching profile " user-uri)))))
