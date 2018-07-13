(ns kitsune.handlers.statuses
  (:require [ring.util.http-response :refer :all]
            [kitsune.handlers.core :refer [defhandler]]
            [kitsune.db.statuses :as db]))

(defn process-status-text
  [text]
  text)

(defhandler create-status
  [{{:keys [text in-reply-to attachments to cc]
     :or {to ["https://www.w3.org/ns/activitystreams#Public"]}} :body-params
    :as req}]
  (let [people {:user-id (-> req :auth :user-id) :to to :cc cc}
        fields {:content (process-status-text text)}]
    (ok (db/create-status people fields)))) ; this is a dummy
