(ns kitsune.handlers.instance
  (:require [ring.util.http-response :refer :all]
            [org.bovinegenius [exploding-fish :as uri]]
            [kitsune.handlers.core :refer [defhandler]]
            [kitsune.db.core :refer [conn]]
            [kitsune.db.instance :as db]
            [kitsune.db.user :as user-db]
            [kitsune.presenters.mastodon :as present]
            [kitsune.instance :refer [url version config]]))

(defhandler node-schema
  [_]
  (ok {:links [{:rel "http://nodeinfo.diaspora.software/ns/schema/2.0"
                :href (str (url "/.well-known/nodeinfo/2.0"))}]}))

(defhandler node-info
  [_]
  (ok {:version "2.0"
       :software {:name "Kitsune"
                  :version version}
       :metadata {:nodeName "Kitsune"
                  :source "https://github.com/valerauko/kitsune"}
       :protocols ["activitypub"]
       :openRegistrations false
       :usage {:localPosts (:local (db/count-local-statuses conn))
               :users {:total (:local (db/count-local-users conn))
                       :activeHalfyear
                        (:local (db/count-local-users-active conn {:months 6}))
                       :activeMonth
                        (:local (db/count-local-users-active conn {:months 1}))}}
       :services {:inbound []
                  :outbound []}}))

(defhandler instance-info
  [_]
  (let [admin (user-db/find-by-id conn
                {:id (get-in config [:instance :admin-account])})]
    (ok {:uri (str (url))
         :title (get-in config [:instance :name])
         :description (get-in config [:instance :description])
         :email (get-in config [:instance :admin-email])
         :version (str "2.3.3 (compatible; Kitsune " version ")")
         :urls {:streaming-api (str (uri/scheme (url) "wss"))}
         :languages #{"en"}
         :contact-account (present/account admin)})))

(defhandler emoji
  [_]
  (ok [])) ; TODO
