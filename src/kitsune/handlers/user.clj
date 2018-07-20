(ns kitsune.handlers.user
  (:require [ring.util.http-response :refer :all]
            [kitsune.handlers.core :refer [defhandler]]
            [kitsune.spec.user :as u]
            [kitsune.db.user :as db]
            [kitsune.db.core :refer [conn int!]]
            [kitsune.presenters.mastodon :refer [account self-account]]))

(defhandler create
  [{{user :user} :body-params :as req}]
  (let [result (db/create! conn (db/process-for-create user))]
    (ok result)))

(defhandler show
  [{{id :id} :path-params :as req}]
  (if-let [result (db/find-by-id conn {:id (int! id)})]
    (ok (account result))
    (not-found {:error "User not found"})))

(defhandler self
  [{{:keys [user-id]} :auth :as req}]
  (if-let [result (db/find-by-id conn {:id user-id})]
    (ok (self-account result))
    (bad-request {:error "Couldn't fetch your profile. Sorry."})))

(defhandler mastodon-update
  [{{:keys [display-name note locked]} :body-params
    {:keys [user-id]} :auth :as req}]
  ; TODO: consider all-empty request? should spec
  (if-let [result (db/update! conn {:id user-id :display-name display-name})]
    (ok (account result))
    (bad-request {:error "Couldn't update your profile. Sorry."})))

(defhandler destroy
  [{{name :name} :path-params :as req}]
  (if-let [result (db/destroy! conn {:name name})]
    (ok result)
    (not-found {:error name})))
