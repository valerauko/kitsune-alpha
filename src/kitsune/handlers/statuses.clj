(ns kitsune.handlers.statuses
  (:require [ring.util.http-response :refer :all]
            [kitsune.handlers.core :refer [defhandler]]
            [kitsune.db.core :refer [conn]]
            [kitsune.db.statuses :as db]
            [kitsune.db.user :as user-db]
            [kitsune.presenters.mastodon :refer [status-hash]]))

(defn process-status-text
  [text]
  text)

(defhandler create
  [{{:keys [text in-reply-to attachments to cc]
     :or {to ["https://www.w3.org/ns/activitystreams#Public"]}} :body-params
    :as req}]
  (let [people {:user-id (-> req :auth :user-id) :to to :cc cc}
        fields {:content (process-status-text text)}]
    (if-let [new (db/create-status! people fields)]
      (ok (status-hash {:object (:object new)
                        :actor (user-db/find-by-id conn
                                                   {:id (:user-id people)})}))
      (bad-request {:error "Unable to save status"}))))

(defhandler delete
  [{{id :id} :path-params :as req}]
  (let [rows (db/delete-object! conn {:id (read-string id)
                                      :user-id (-> req :auth :user-id)})]
    (if (> rows 0)
      (ok {})
      (not-found {:error "You don't seem to have a status like that"}))))
