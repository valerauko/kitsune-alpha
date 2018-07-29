(ns kitsune.handlers.statuses
  (:require [ring.util.http-response :refer :all]
            [kitsune.handlers.core :refer [defhandler]]
            [kitsune.db.core :refer [conn]]
            [kitsune.db.statuses :as db]
            [kitsune.db.user :as user-db]
            [kitsune.presenters.mastodon :refer [status-hash]]))

; a status is an Announce or Create activity wrapping a Note

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
      (not-found {:error "Status not found"}))))

(defhandler load-status
  ; The ID in the path is that of the Activity on the timeline.
  ; Loads a single status with all its accessories.
  [{{id :id} :path-params}]
  (if-let [result (db/activity-with-object conn {:id id})]
    ; TODO: load attachments, tags, mentions
    ; TODO: handle boosts (different user)
    ; TODO: calculate visibility
    ; TODO: do urls correctly
    (let [user (user-db/find-by-id conn {:id (:user-id result)})]
      (ok (status-hash {:object result :actor user})))
    (not-found {:error "Status not found"})))

; TODO: this needs authentication but i don't have visibility yet
(defhandler account-statuses
  [{{id :id} :path-params}]
  (if-let [result (db/user-activities conn {:user-id id})]
    (ok (map
          (fn [row]
            (let [user (user-db/find-by-id conn {:id (:user-id row)})]
              (status-hash {:object row :actor user})))
          result))
    (not-found {:error "User not found"})))
