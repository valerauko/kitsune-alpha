(ns kitsune.presenters.mastodon)

(defn account
  [record]
  {:id (:id record)
   :username (:name record)
   :acct ""
   :display-name (:display-name record)
   :locked false
   :created-at (:create-at record)
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
