(ns kitsune.presenters.mastodon
  (:require [kitsune.uri :refer [url]]
            [org.bovinegenius [exploding-fish :refer [host]]]))

(defn account
  [record]
  {:id (:id record)
   :username (:name record)
   :acct (:acct record)
   :display-name (or (:display-name record) (:name record))
   :locked false
   :created-at (:created-at record)
   :followers-count 0
   :following-count 0
   :statuses-count 0
   :note ""
   :url (:uri record)
   :avatar "http://kitsune.social/dummy"
   :avatar-static "http://kitsune.social/dummy"
   :header "http://kitsune.social/dummy"
   :header-static "http://kitsune.social/dummy"})

(defn self-account
  [record]
  (merge
    (account record)
    {:source
     {:privacy "public"
      :sensitive false
      :note ""
      :fields {}}}))

(defn status
  [& {:keys [object actor]}]
  {:id (:id object)
   :uri (:uri object)
   :account (account actor)
   :content (:content object)
   :created-at (:created-at object)
   :emojis []
   :in-reply-to-id (:in-reply-to-id object)
   :in-reply-to-account-id (:in-reply-to-user-id object)
   :reblogs-count 0
   :favourites-count 0
   :spoiler-text (str (:summary object))
   :sensitive false
   :visibility "public"
   :media-attachments []
   :mentions []
   :tags []})
