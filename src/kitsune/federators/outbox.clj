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
        ; for some reason RFC_1123_DATE_TIME isn't good enough. not sure where
        ; the problem is but mastodon fails to validate the date if the day
        ; number doesn't have a leading zero (and it doesn't in the rfc format)
        formatter (DateTimeFormatter/ofPattern "EEE, dd MMM uuu HH:mm:ss zzz")
        timestamp (ZonedDateTime/now gmt)]
    (.format formatter timestamp)))

(def content-type
   "application/ld+json; profile=\"https://www.w3.org/ns/activitystreams\"")

(def ap-context
  {(keyword "@context") ["https://www.w3.org/ns/activitystreams",
                         "https://w3id.org/security/v1"]})

(defn send-activity
  [inbox actor activity]
  (let [start (System/nanoTime)
        body (-> (merge ap-context activity) json/write-value-as-bytes)
        headers {"host" (uri/host inbox)
                 "date" (header-time)
                 "digest" (str "SHA-256=" (csele.hash/hash-base64 body))
                 "content-type" content-type}
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
        (log/warn e (str "Failed to send " (:type activity) " to " inbox " -- "
                         ; if the error's not from aleph it won't have .data
                         (try (-> e .data :body byte-streams/to-string)
                           (catch Exception e nil))))))))
