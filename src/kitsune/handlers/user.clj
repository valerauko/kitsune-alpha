(ns kitsune.handlers.user
  (:require [clojure.spec.alpha :as s]
            [buddy.core.hash :as hash]
            [buddy.core.codecs :refer [bytes->hex]]
            [ring.util.http-response :refer :all]
            [kitsune.handlers.core :refer [defhandler]]
            [kitsune.spec.user :as u]
            [kitsune.db.user :as db]
            [kitsune.db.core :refer [conn]]))

(defn hash-pass
  [user]
  (-> user
    (assoc :pass-hash (-> user :pass hash/sha3-512 bytes->hex))
    (dissoc :pass :pass-confirm)))

(defhandler create
  [{{user :user} :body-params :as req}]
  (let [result (db/create! conn (hash-pass user))]
    (ok result)))

(defhandler show
  [{{name :name} :path-params :as req}]
  (if-let [result (db/find! conn {:name name})]
    (ok result)
    (not-found {:error name})))

(defhandler update-profile
  [{{{display-name :display-name} :user} :body-params
     {name :name} :path-params :as req}]
  (if-let [result (db/update! conn {:name name :display-name display-name})]
    (ok result)
    (not-found {:error name})))

(defhandler destroy
  [{{name :name} :path-params :as req}]
  (if-let [result (db/destroy! conn {:name name})]
    (ok result)
    (not-found {:error name})))
