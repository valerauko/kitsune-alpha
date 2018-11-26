(ns kitsune.federators.inbox
  (:require [clojure.tools.logging :as log]
            [csele.headers :as headers]
            [kitsune.db.user :as user-db]
            [kitsune.db.statuses :as activity]
            [kitsune.federators.user :as user]
            [kitsune.federators.follow :as follow]))

(defmacro benchmark-inbox
  "Logs the time it took to process an activity"
  [type id ip body]
  `(let [start# (System/nanoTime)
         result# ~body]
     (log/info (format "Processed %s %s for %s in %.3fms"
                       ~type ~id ~ip
                       (/ (- (System/nanoTime) start#) 1000000.0)))
     result#))

(defn signed?
  "Checks HTTP header signature of the activity's request.
   Refetches the user's public key if it doesn't work at first try.
   Depending on the user's instance that request can be pretty slow."
  [{{sig-header :signature} :headers
    {{actor :actor} :body} :parameters
    :as request}]
  (or
    ; first see if the key in the db (if any) can validate the sig
    (let [key (user-db/public-key actor)]
      (and key (headers/verify request key)))
    ; if not then refetch the actor's key and use that to validate
    (let [refetched-key (-> actor user/refetch-profile :public-key)]
      (and refetched-key (headers/verify request refetched-key)))))

(defn record
  [{{{{object-type :type}:object
      :keys [id type] :as activity} :body} :parameters
    {fwd :X-Forwarded-For} :headers :keys [remote-addr]
    :as request}]
  (benchmark-inbox type id (or fwd remote-addr)
    (if (and (signed? request)
             (not (activity/known-activity? id)))
      (case type
        "Follow" (follow/receive activity)
        "Accept" (case object-type
                   "Follow" (follow/incoming-accept activity)
                   (clojure.pprint/pprint activity))
        "Undo" (case object-type
                 "Follow" (follow/receive-undo activity)
                 (clojure.pprint/pprint activity))
        (clojure.pprint/pprint activity))
      (clojure.pprint/pprint activity))))
