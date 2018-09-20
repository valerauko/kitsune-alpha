(ns kitsune.handlers.statuses
  (:require [ring.util.http-response :refer :all]
            [kitsune.handlers.core :refer [defhandler]]
            [kitsune.db.core :refer [conn]]
            [kitsune.db.statuses :as db]
            [kitsune.db.user :as user-db]
            [kitsune.presenters.mastodon :as mastodon]
            [markdown.core :as markdown]))

(defhandler create
  [{{raw-text :status} :body-params
    {actor-id :user-id} :auth}]
  (let [max-length 420 ; TODO: move max-length to some config
        {:keys [length text mentions]} (markdown/process raw-text)
        to ["https://www.w3.org/ns/activitystreams#Public"]
        cc (if (empty? mentions) nil (into [] mentions))]
    (if (> length max-length)
      (request-entity-too-large
        {:error (str "Your post is longer than the allowed "
                     max-length " characters.")})
      (if-let [new-status (db/create-status! :content text
                                             :actor actor-id
                                             :to to
                                             :cc cc)]
        (ok (mastodon/status :object (:object new)
                             :actor (user-db/find-by-id conn {:id actor-id})))
        (bad-request {:error "Unable to save status"})))))

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
      (ok (mastodon/status :object result :actor user)))
    (not-found {:error "Status not found"})))

; TODO: this needs authentication but i don't have visibility yet
(defhandler account-statuses
  [{{id :id} :path-params}]
  (if-let [result (db/user-activities conn {:user-id id})]
    (let [preloaded (db/preload-stuff result)
          formatted (map #(mastodon/status :object (dissoc % :actor)
                                           :actor (:actor %))
                         preloaded)]
      (ok formatted))
    (not-found {:error "User not found"})))
