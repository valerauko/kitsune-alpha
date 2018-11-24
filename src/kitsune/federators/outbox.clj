(ns kitsune.federators.outbox
  (:require [clojure.tools.logging :as log]
            [aleph.http :as http]
            [jsonista.core :as json]
            [org.bovinegenius [exploding-fish :as uri]]
            [csele.headers :refer [sign-request]]
            [kitsune.federators.core :as fed])
  (:import [java.io ByteArrayInputStream]
           [java.time ZonedDateTime ZoneId]
           [java.time.format DateTimeFormatter]))

(def default-headers
  ["(request-target)" "host" "date" "digest" "content-type"])

(defn header-time
  []
  (let [gmt (ZoneId/of "GMT")
        formatter (DateTimeFormatter/RFC_1123_DATE_TIME)
        timestamp (ZonedDateTime/now gmt)]
    (.format formatter timestamp)))

(def ap-context
  {(keyword "@context") ["https://www.w3.org/ns/activitystreams",
                         "https://w3id.org/security/v1"]})

(defn send-activity
  [inbox actor activity]
  (let [start (System/nanoTime)
        body (-> (merge ap-context activity) json/write-value-as-bytes)
        headers {"content-type" "application/activity+json"
                 "date" (header-time)
                 "host" (uri/host inbox)}
        key-map {:key-id (str (:uri actor) "#main-key")
                 :pem (:private-key actor)}
        signature (sign-request {:uri (uri/path inbox)
                                 :request-method :post
                                 :headers headers
                                 :body (ByteArrayInputStream. body)}
                                default-headers
                                key-map)]
    (try
      (let [response @(http/post inbox {:body body
                                        :headers (assoc headers :signature
                                                                signature)})]
        (log/info (format "Sent %s to %s in %.3fms (%.3fms)"
                          (:type activity)
                          inbox
                          (-> response :request-time float)
                          (/ (- (System/nanoTime) start) 1000000.0))))
      (catch Exception e
        (log/warn e (str "Failed to send " (:type activity) " to " inbox))))))
