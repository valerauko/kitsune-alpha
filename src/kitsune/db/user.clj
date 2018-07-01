(ns kitsune.db.user
  (:require [hugsql.core :refer [def-db-fns]]
            [buddy.core.hash :as hash]
            [kitsune.db.core :refer [conn]]
            [buddy.core.codecs :refer [bytes->hex]]))

(def-db-fns "sql/users.sql")

(defn hash-pass
  [str]
  (-> str hash/sha3-512 bytes->hex))

(defn process-for-create
  [user]
  (-> user
    (assoc :pass-hash (-> user :pass hash-pass))
    (dissoc :pass :pass-confirm)))

(defn for-login
  [name pass]
  (find-for-auth
    conn
    {:name name :pass-hash (hash-pass pass)}))
