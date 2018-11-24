(ns kitsune.presenters.activitypub
  (:require [kitsune.uri :refer [url]]))

(def common-context
  {(keyword "@context") ["https://www.w3.org/ns/activitystreams",
                         "https://w3id.org/security/v1"]})

(defn account
  [{:keys [uri] :as record}]
  (merge
    common-context
    {:type "Person"
     :id uri
     :url uri
     :name (:display-name record)
     :preferredUsername (:name record)
     :following (str uri "/following")
     :followers (str uri "/followers")
     :inbox (str uri "/inbox")
     :outbox (str uri "/outbox")
     :endpoints {:sharedInbox (-> "/inbox" url str)}
     :publicKey {:id (str uri "#main-key")
                 :owner uri
                 :publicKeyPem (:public-key record)}}))

(defn follows-top
  [mode & {:keys [profile total]}]
  (let [path (if (= mode :followers) "followers" "following")]
    (merge
      common-context
      {:type "OrderedCollection"
       :id (str profile "/" path)
       :totalItems total
       }
      (if (pos-int? total) {:first (str profile "/" path "?page=1")}))))

(defn follows
  [mode & {:keys [items page next? profile total]}]
  (let [path (if (= mode :followers) "followers" "following")
        link (str profile "/" path)]
    (merge
      common-context
      {:type "OrderedCollectionPage"
       :id (str link "?page=" page)
       :totalItems total
       :partOf link
       :orderedItems (map :uri items)}
      (if next? {:next (str link "?page" (inc page))}))))
