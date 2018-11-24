(ns kitsune.handlers.user
  (:require [ring.util.http-response :refer :all]
            [kitsune.handlers.core :refer [defhandler]]
            [kitsune.spec.user :as u]
            [kitsune.db.user :as db]
            [kitsune.db.core :refer [conn int!]]
            [kitsune.presenters.mastodon :as mastodon]
            [kitsune.presenters.activitypub :as activitypub]))

(defhandler create
  [{{user :user} :body-params :as req}]
  (if-let [result (db/register user)]
    (ok result)))

(defhandler show
  [{{id :id} :path-params :as req}]
  (if-let [result (db/find-by-id conn {:id (int! id)})]
    (ok (mastodon/account result))
    (not-found {:error "User not found"})))

; TODO: figure out how to deal with the mastodon / activitypub duality
;   also how to deal with the activitypub endpoints doubling as profile page etc
(defhandler ap-show
  [{{{name :name} :path} :parameters}]
  (if-let [result (db/find-by-name conn {:name name})]
    (ok (activitypub/account result))
    (not-found)))

(defhandler ap-followers
  [{{{name :name} :path
     {page :page} :query} :parameters}]
  (if-let [user (db/count-followers conn {:name name})]
    (if (pos-int? page)
      (let [per-page 10 ; config?
            result (db/followers-of conn {:id (:id user)
                                          :limit (inc per-page)
                                          :offset (* per-page (dec page))})
            next? (> (count result) per-page)]
        (ok (activitypub/follows :followers
                                 :items (if next? (butlast result) result)
                                 :next? next?
                                 :page page
                                 :profile (:uri user)
                                 :total (:followers user))))
      (ok (activitypub/follows-top :followers
                                   :profile (:uri user)
                                   :total (:followers user))))
    (not-found {:error "Unknown user"})))

; TODO: fix pasta code
(defhandler ap-following
  [{{{name :name} :path
     {page :page} :query} :parameters}]
  (if-let [user (db/count-following conn {:name name})]
    (if (pos-int? page)
      (let [per-page 10 ; config?
            result (db/followed-by conn {:id (:id user)
                                         :limit (inc per-page)
                                         :offset (* per-page (dec page))})
            next? (> (count result) per-page)]
        (ok (activitypub/follows :following
                                 :items (if next? (butlast result) result)
                                 :next? next?
                                 :page page
                                 :profile (:uri user)
                                 :total (:following user))))
      (ok (activitypub/follows-top :following
                                   :profile (:uri user)
                                   :total (:following user))))
    (not-found {:error "Unknown user"})))

(defhandler self
  [{{:keys [user-id]} :auth :as req}]
  (if-let [result (db/find-by-user-id conn {:id user-id})]
    (ok (mastodon/self-account result))
    (bad-request {:error "Couldn't fetch your profile. Sorry."})))

(defhandler update-stuff
  [{{:keys [display-name note locked]} :body-params
    {:keys [user-id]} :auth :as req}]
  ; TODO: consider all-empty request? should spec
  (if-let [result (db/update! conn {:id user-id :display-name display-name})]
    (ok (mastodon/account result))
    (bad-request {:error "Couldn't update your profile. Sorry."})))

(defhandler destroy
  [{{name :name} :path-params :as req}]
  (if-let [result (db/destroy! conn {:name name})]
    (ok result)
    (not-found {:error name})))

(defhandler search
  [{{raw-query "query"} :query-params :as req}]
  (if-let [result (db/search raw-query)]
    (ok result)
    (not-found {:error "No known accounts match your search"})))
