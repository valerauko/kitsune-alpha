(ns kitsune.federators.follow
  (:require [kitsune.db.core :refer [conn]]
            [kitsune.db.user :as users]
            [kitsune.db.relationship :as rel]
            [kitsune.uri :as uri]
            [kitsune.federators.outbox :refer [send-activity]]
            [kitsune.presenters.activitypub.follow :as json]))

(defn accept
  ([args] (accept args :existing))
  ([{:keys [id follower followed]} should-exist]
   (let [accept-uri (str (uri/url "/accept/" (java.util.UUID/randomUUID)))]
     (if (= should-exist :existing)
       (rel/accept-follow! conn {:uri id :accept-uri accept-uri}))
       ; TODO: handle case if Follow record doesn't exist
     (if-not (:local follower)
       (send-activity (:inbox follower)
                      followed
                      (json/accept {:accept-uri accept-uri
                                    :uri id
                                    :follower-uri (:uri follower)
                                    :followed-uri (:uri followed)})))
     accept-uri)))

(defn request
  [{:keys [id followed follower]}]
  ; don't "federate" if object is local
  (if-not (:local followed)
    (send-activity  (:inbox followed)
                    follower
                    (json/follow {:id id
                                  :followed-uri (:uri followed)
                                  :follower-uri (:uri follower)}))))

(defn receive
  [{:keys [id type object actor] :as activity}]
  (let [object-uri (or (:id object) object)
        actor-uri (or (:id actor) actor)]
    ; since we're past signature checking, we can assume we have the actor in db
    (if (uri/local? object-uri) ; ignore if the object isn't local
      (if-let [followed (users/find-by-uri conn {:uri object-uri})]
        (let [follower (users/find-by-uri conn {:uri actor-uri})
              accept-uri (if-not (:approves-follow followed)
                           (accept {:id id
                                    :followed followed
                                    :follower follower}
                                   :new-record))]
          (rel/follow! conn {:uri id
                             :followed (:id followed)
                             :follower (:id follower)
                             :accept-uri accept-uri}))))))
