(ns kitsune.federators.user
  (:require [kitsune.federators.core :as fed]
            [kitsune.db.core :refer [conn]]
            [kitsune.db.user :as db]
            [clojure.tools.logging :as log]
            [jsonista.core :as json]))

(defn refetch-profile
  "Goes out to fetch a remote user's data based on their uri"
  [user-uri]
  (let [known-user (db/find-by-uri conn {:uri user-uri})]
    ; if the user is local don't update
    (if (:local known-user)
      known-user
      (do
        (log/info (str "Fetching profile of " user-uri))
        (if-let [result (fed/fetch-resource user-uri)]
          (let [data {:name (or (:name result)
                                (:preferredUsername result))
                      :acct (db/uri-to-acct (:id result))
                      :uri (:id result)
                      :public-key (-> result :publicKey :publicKeyPem)
                      :display-name (or (:preferredUsername result)
                                        (:name result))
                      :inbox (or (:inbox result) (:sharedInbox result))
                      :shared-inbox (or (:sharedInbox result) (:inbox result))}]
            (if known-user
              (db/update-account! conn data)
              (db/create-account! conn data)))
          ; if refetching failed return the current known data
          known-user)))))
