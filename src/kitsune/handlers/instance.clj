(ns kitsune.handlers.instance
  (:require [ring.util.http-response :refer :all]
            [kitsune.handlers.core :refer [defhandler]]
            [kitsune.db.core :refer [conn]]
            [kitsune.db.stats :as db]))

(defhandler node-schema
  [_]
  (ok {:links [{:rel "http://nodeinfo.diaspora.software/ns/schema/2.0"
                :href "https://example.org/nodeinfo/2.0"}]})) ; TODO

(defhandler node-info
  [_]
  (ok {:version "2.0"
       :software {:name "Kitsune"
                  :version "0.1.0"}
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
