(ns kitsune.handlers.instance
  (:require [ring.util.http-response :refer :all]
            [kitsune.handlers.core :refer [defhandler]]))

(defhandler node-schema
  [_]
  (ok {:links [{:rel "http://nodeinfo.diaspora.software/ns/schema/2.0"
                :href "https://example.org/nodeinfo/2.0"}]}))

(defhandler node-info
  [_]
  (ok {:version "2.0"
       :software {:name "Kitsune"
                  :version "0.1.0"}
       :protocols ["activitypub"]
       :openRegistrations false
       :usage {:localPosts 0
               :users {:total 1}}
       :services {:inbound []
                  :outbound []}}))
