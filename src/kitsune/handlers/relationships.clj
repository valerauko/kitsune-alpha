(ns kitsune.handlers.relationships
  (:require [kitsune.db.relationship :as db]
            [kitsune.db.core :refer [conn]]
            [kitsune.handlers.core :refer [defhandler]]
            [ring.util.http-response :refer :all]))

(defn present?
  [val]
  (if val true false))

(defn relationship
  [& {:keys [subject object]}]
  {:id subject
   :following (present? (db/follows? conn {:subject subject :object object}))
   :followed-by (present? (db/follows? conn {:subject object :object subject}))
   ; TODO
   :blocking false
   :muting false
   :muting-notifications false
   :requested false
   :domain-blocking false
   :showing-reblogs true})

(defhandler follow
  [{{object :id} :path-params
    {subject :user-id} :auth}]
  ; TODO: showing reblogs, blocking
  ; TODO: proper error messages
  ; reasons this might fail:
  ; - subject's not logged in (not null violation)
  ; - object doesn't exist (foreign key violation)
  ; - subject already follows object (unique constraint violation)
  (if (db/follow! conn {:subject subject :object object})
    (ok (relationship :subject subject :object object))))

(defhandler unfollow
  [{{object :id} :path-params
    {subject :user-id} :auth}]
  (if (db/unfollow! conn {:subject subject :object object})
    (ok (relationship :subject subject :object object))))
