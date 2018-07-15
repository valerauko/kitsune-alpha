(ns kitsune.presenters.mastodon
  (:require [kitsune.instance :refer [url]]
            [org.bovinegenius [exploding-fish :refer [host]]]))

(defn account
  [record]
  {:id (:id record)
   :username (:name record)
   :acct (if (:local record)
           (:name record)
           (str (:name record) "@" (host (:uri record))))
   :display-name (:display-name record)
   :locked false
   :created-at (:created-at record)
   :followers-count 0
   :following-count 0
   :statuses-count 0
   :note ""
   :url (:uri record)
   :avatar ""
   :avatar-static ""
   :header ""
   :header-static ""})

(defn status-hash
  [{:keys [object actor]}]
  {:id (:id object)
   :uri (:uri object)
   :account (account actor)
   :content (:content object)
   :created-at (:created-at object)
   :emojis []
   :reblogs-count 0
   :favourites-count 0
   :visibility "public"
   :media-attachments (:attachments object)
   :mentions (:mentions object)
   :tags (:tags object)})
