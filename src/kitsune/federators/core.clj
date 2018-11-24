(ns kitsune.federators.core
  (:require [clojure.tools.logging :as log]
            [aleph.http :as http]
            [jsonista.core :as json]))

(defn parse-json
  [input]
  (json/read-value
    input
    (json/object-mapper {:decode-key-fn keyword})))

(defn fetch-resource
  [uri]
  (try
    (-> @(http/get uri {:headers {:accept "application/activity+json"}
                        :connect-timeout 1000
                        :read-timeout 2000})
        :body
        parse-json)
    (catch Throwable ex
      (log/warn
        ; ex
        (str "Error while fetching resource " uri)))))
