(ns kitsune.db.user
  (:require [hugsql.core :refer [def-db-fns]]
            [buddy.core.hash :as hash]
            [kitsune.db.core :refer [conn]]
            [kitsune.instance :refer [url]]
            [buddy.core.codecs :refer [bytes->hex]]
            [org.bovinegenius [exploding-fish :as uri]]))

(def-db-fns "sql/users.sql")

(defn hash-pass
  [str]
  (-> str hash/sha3-512 bytes->hex))

(defn process-for-create
  [user]
  (-> user
    (assoc :pass-hash (-> user :pass hash-pass))
    (assoc :uri (-> user :name (#(str "/people/" %)) url str))
    (dissoc :pass :pass-confirm)))

(defn for-login
  [name pass]
  (find-for-auth
    conn
    {:name name :pass-hash (hash-pass pass)}))

; TODO: move this all to webfinger federator
(defn uri-to-acct
  [uri]
  (let [host (uri/host uri)
        path (uri/path uri)
        name (last (re-find #"([^@/]+)$" (or path "")))]
    (str name "@" host)))

(defn search-by-uri
  [uri]
  ; first check if the uri's an exact match of something in the db already
  (if-let [uri-match (find-by-uri conn {:uri uri})]
    uri-match
    ; next try looking by user@host (webfinger acct) name
    (if-let [acct-match (find-by-acct conn {:acct (uri-to-acct uri)})]
      acct-match
      ; TODO: do webfinger federated lookup
      )))

(defn search
  [raw-query]
  (let [query (trim raw-query)]
    (cond
      ; kill empty lookups
      (empty? query)
        nil
      ; https://kitsune.social/people/admin
      (uri/absolute? query)
        (search-by-uri query)
      ; admin@kitsune.social
      (re-matches? #"^([^/]+)@([^@]+)$" query)
        (find-by-acct conn {:acct query})
      ; @admin
      (re-matches? #"(?i)^@?[a-z0-9][a-z0-9-_]+$" query)
        (find-by-name conn {:name (last (re-find #"(?i)@?(.+)"))})
      ; kitsune.social/people/admin yes sadly that // is important
      :else
        (search-by-uri (str "//" query)))))
