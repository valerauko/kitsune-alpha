(ns kitsune.db.user
  (:require [clojure.string :refer [trim]]
            [clojure.java.jdbc :as jdbc]
            [csele.hash :refer [hash-hex]]
            [csele.keys :refer [generate-keypair]]
            [hugsql.core :refer [def-db-fns]]
            [kitsune.db.core :refer [conn]]
            [kitsune.uri :refer [url]]
            [org.bovinegenius [exploding-fish :as uri]]))

(def-db-fns "sql/users.sql")

(defn update-user-keypair
  [user-id]
  (let [{:keys [public private]} (generate-keypair)]
    (jdbc/with-db-transaction [tx conn]
      (update-public-key! tx {:user-id user-id :public-key public})
      (update-private-key! tx {:user-id user-id :private-key private}))
    {:public-key public :private-key private}))

(defn public-key
  [uri]
  (if-let [result (find-by-uri conn {:uri uri})]
    (:public-key result)))

(defn key-map
  [user-record]
  {:key-id (str (:uri user-record) "#main-key")
   :pem (:private-key user-record)})

(defn for-login
  [email pass]
  (find-for-auth
    conn
    {:email email :pass-hash (hash-hex pass)}))

(defn known-remote?
  [uri]
  (->> uri (find-by-uri conn) :local not))

; TODO: move this all to webfinger federator
(defn uri-to-acct
  [uri]
  (let [host (uri/host uri)
        path (uri/path uri)
        name (last (re-find #"([^@/]+)$" (or path "")))]
    (str name "@" host)))

(defn register
  "Creates an user and an account record within a transaction."
  [user]
  (jdbc/with-db-transaction [tx conn]
    (let [{:keys [public private]} (generate-keypair)
          name (:name user)
          uri (->> name (str "/people/") url str)
          acct (uri-to-acct uri)]
      (if-let [user-rec (create-user! tx {:email (:email user)
                                          :name name
                                          :pass-hash (-> user :pass hash-hex)
                                          :private-key private})]
        (if-let [account-rec (create-account! tx {:user-id (:id user-rec)
                                                  :acct acct
                                                  :uri uri
                                                  :local true
                                                  :public-key public
                                                  :display-name name
                                                  :inbox (str uri "/inbox")
                                                  :shared-inbox (-> "/inbox"
                                                                    url str)})]
          {:name name
           :user-id (:id user-rec)
           :account-id (:id account-rec)
           :uri uri})))))

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
