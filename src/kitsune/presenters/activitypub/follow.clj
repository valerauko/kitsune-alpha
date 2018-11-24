(ns kitsune.presenters.activitypub.follow)

(defn follow
  [{:keys [uri followed-uri follower-uri]}]
  {:type "Follow"
   :id uri
   :object followed-uri
   :actor follower-uri})

(defn accept
  [{:keys [accept-uri followed-uri] :as record}]
  {:type "Accept"
   :id accept-uri
   :actor followed-uri
   :object (follow record)})
