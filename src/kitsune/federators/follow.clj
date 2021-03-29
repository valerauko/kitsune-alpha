(ns kitsune.federators.follow
  (:require [kitsune.db.core :refer [conn]]
            [kitsune.db.user :as users]
            [kitsune.db.relationship :as rel]
            [kitsune.uri :as uri]
            [bark.send :refer [send-activity]]
            [kitsune.presenters.activitypub.follow :as json]
            [kitsune.presenters.activitypub.undo :as undo]))

(defn send-accept
  ([args] (send-accept args :existing))
  ([{:keys [id follower followed]} should-exist]
   (let [accept-uri (str (uri/url "/accept/" (java.util.UUID/randomUUID)))]
     (if (= should-exist :existing)
       (rel/accept-follow! conn {:uri id :accept-uri accept-uri}))
       ; TODO: handle case if Follow record doesn't exist
     (if-not (:local follower)
       (send-activity {:inbox (:inbox follower)
                       :key-map (users/key-map followed)
                       :activity
                         (json/accept {:accept-uri accept-uri
                                       :uri id
                                       :follower-uri (:uri follower)
                                       :followed-uri (:uri followed)})}))
     accept-uri)))

(defn send-undo
  [{:keys [uri followed follower]}]
  (if-not (:local followed)
    (send-activity {:inbox (:inbox followed)
                    :key-map (users/key-map follower)
                    :activity (-> {:uri uri
                                   :followed-uri (:uri followed)
                                   :follower-uri (:uri follower)}
                                  json/follow
                                  undo/undo)})))

(defn send-follow
  [{:keys [uri followed follower]}]
  ; don't "federate" if object is local
  (if-not (:local followed)
    (send-activity {:inbox (:inbox followed)
                    :key-map (users/key-map follower)
                    :activity (json/follow {:uri uri
                                            :followed-uri (:uri followed)
                                            :follower-uri (:uri follower)})})))

; TODO: make some general pre-processing for activities
; * normalize uri-or-object stuff to either
; * check if accept/undo actor and object actor are the same
(defn accept-handler
  [{{accept-uri :id {follow-uri :id follower-uri :actor} :object} :body-params}]
  (if (uri/local? follower-uri)
    (rel/accept-follow! conn {:uri follow-uri :accept-uri accept-uri})))

(defn follow-handler
  [{{:keys [id object actor] :as activity} :body-params}]
  (let [object-uri (or (:id object) object)
        actor-uri (or (:id actor) actor)]
    ; since we're past signature checking, we can assume we have the actor in db
    (if (uri/local? object-uri) ; ignore if the object isn't local
      (if-let [followed (users/find-by-uri conn {:uri object-uri})]
        (let [follower (users/find-by-uri conn {:uri actor-uri})
              accept-uri (if-not (:approves-follow followed)
                           (send-accept {:id id
                                         :followed followed
                                         :follower follower}
                                        :new-record))]
          (rel/follow! conn {:uri id
                             :followed (:id followed)
                             :follower (:id follower)
                             :accept-uri accept-uri}))))))

(defn undo-handler
  [{{activity-actor :actor
    {object-actor :actor :keys [id object]} :object} :body-params}]
  ; TODO: move the actor identity check to a general Undo handler
  (if (= activity-actor object-actor)
    (let [followed (users/find-by-uri conn {:uri object})
          follower (users/find-by-uri conn {:uri object-actor})]
      (if (and followed follower)
        (rel/unfollow! conn {:follower (:id follower)
                             :followed (:id followed)})))))
