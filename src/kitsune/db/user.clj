(ns kitsune.db.user
  (:require [clojure.string :refer [trim]]
            [csele.hash :refer [hash-string]]
            [csele.keys :refer [generate-keypair]]
            [hugsql.core :refer [def-db-fns]]
            [kitsune.db.core :refer [conn]]
            [kitsune.instance :refer [url]]
            [org.bovinegenius [exploding-fish :as uri]]))

(def-db-fns "sql/users.sql")

(defn make-user-keypair
  [user-id]
  (let [{:keys [public private]} (generate-keypair)]
    (update-keys! conn {:id user-id :public-key public :private-key private})))

(defn for-login
  [name pass]
  (find-for-auth
    conn
    {:name name :pass-hash (hash-string pass)}))

; TODO: move this all to webfinger federator
(defn uri-to-acct
  [uri]
  (let [host (uri/host uri)
        path (uri/path uri)
        name (last (re-find #"([^@/]+)$" (or path "")))]
    (str name "@" host)))

(defn process-for-create
  [user]
  (let [uri (-> user :name (#(str "/people/" %)) url str)
        acct (uri-to-acct uri)]
    (-> user
      (assoc :pass-hash (-> user :pass hash-string))
      (assoc :uri uri)
      (assoc :acct acct)
      (dissoc :pass :pass-confirm))))

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
      (re-matches #"^([^/]+)@([^@]+)$" query)
        (find-by-acct conn {:acct query})
      ; @admin
      (re-matches #"(?i)^@?[a-z0-9][a-z0-9-_]+$" query)
        (find-by-name conn {:name (last (re-find #"(?i)@?(.+)" query))})
      ; kitsune.social/people/admin yes sadly that // is important
      :else
        (search-by-uri (str "//" query)))))
