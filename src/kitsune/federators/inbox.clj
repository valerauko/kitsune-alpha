(ns kitsune.federators.inbox
  (:require [clojure.tools.logging :as log]
            [csele.headers :as headers]
            [kitsune.db.user :as user]
            [kitsune.db.statuses :as activity]
            [kitsune.federators.user :as fed]))

(defn signed?
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
  [{{{:strs [id type object actor]
      :as activity} :body} :parameters
    :as request}]
  (if (and (signed? request)
           (not (activity/known-activity? id)))
    (clojure.pprint/pprint activity)))
