(ns kitsune.federators.inbox
  (:require [clojure.tools.logging :as log]
            [csele.headers :as headers]
            [kitsune.db.user :as user]
            [kitsune.db.statuses :as activity]
            [kitsune.federators.user :as fed]))

(defmacro benchmark-inbox [type id body]
  `(let [start# (System/nanoTime)
         result# ~body]
     (log/info (format "Processed %s %s in %.3fms"
                       ~type ~id
                       (/ (- (System/nanoTime) start#) 1000000.0)))
     result#))

(defn signed?
  "Checks HTTP header signature of the activity's request.
   Refetches the user's public key if it doesn't work at first try.
   Depending on the user's instance that request can be pretty slow."
  [{{sig-header :signature} :headers
    {{actor :actor} :body} :parameters
    :as request}]
  (if-let [key (user/public-key actor)]
    (headers/verify request key)
    (if-let [refetched-key (-> (fed/refetch-profile actor)
                               (get "publicKey")
                               (get "publicKeyPem"))]
      (headers/verify request refetched-key))))

(defn record
  [{{{:keys [id type object actor]
      :as activity} :body} :parameters
    :as request}]
  (benchmark-inbox type id
    (if (and (signed? request)
             (not (activity/known-activity? id)))
      (clojure.pprint/pprint activity))))
