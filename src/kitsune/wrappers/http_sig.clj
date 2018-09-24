(ns kitsune.wrappers.http-sig
  (:require [csele.signatures :as sig]
            [csele.hash :refer [hash-base64]]
            [clojure.string :refer [split join]]
            [org.bovinegenius [exploding-fish :as uri]]
            [kitsune.db.user :as db]
            [kitsune.federators.user :as fed]))

(defn sig-string
  [{method :request-method
    path :uri
    headers :headers
    body :body
    :as request}
   targets]
  (join "\n"
    (map
      (fn [header]
        (str header ": "
             (case header
               "(request-target)" (str (name method) " " path)
               "digest" (str "SHA-256=" (hash-base64 body))
               ; remove port from host header if present
               "host" (->> header (get headers) (re-find #"[^:]+"))
               (get headers header))))
      targets)))

(defn check-headers
  [{{sig-header :signature} :headers
    {{actor :actor} :body} :parameters  ; just donna assume that it's the id for now
    :as request}]
  (let [target-headers (split (->> sig-header
                                   (re-find #"headers=\"([^\"]+)\"")
                                   second)
                              #"\s+")
        signature (->> sig-header
                       (re-find #"signature=\"([^\"]+)\"")
                       second)
        key (db/public-key actor)
        computed-string (sig-string request target-headers)]
    (if (and key (sig/verify signature computed-string key))
      true
      (let [refetched-key (-> (fed/refetch-profile actor)
                              (get "publicKey")
                              (get "publicKeyPem"))]
        (sig/verify signature computed-string refetched-key)))))

; TODO: currently only support the mastodon / pleroma way
; that keys are host/actor-path#main-key. mastodon only
; works with keys like that or acct:s and pleroma just
; fetches the key of the actor of the activity in the
; payload
(defn verify-signature
  "Inserts :http-signed? in request map."
  [handler]
  (fn [request]
    (let [result (if (-> request :headers :signature) (check-headers request))]
      (handler (assoc request :http-signed? result)))))
