(ns bark.fetch
  (:require [clojure.tools.logging :as log]
            [manifold.deferred :as md]
            [aleph.http :as http]
            [jsonista.core :as json]))

(defn parse-json
  [input]
  (json/read-value
    input
    (json/object-mapper {:decode-key-fn keyword})))

(defn fetch-resource
  [uri]
  (md/chain
    (http/get uri {:headers {:accept "application/activity+json"}
                   :connect-timeout 1000
                   :read-timeout 2000})
    :body
    parse-json))
