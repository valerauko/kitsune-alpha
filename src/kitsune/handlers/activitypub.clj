(ns kitsune.handlers.activitypub
  (:require [clojure.core.async :as async]
            [ring.util.http-response :as status]
            [kitsune.db.user :as db]
            [kitsune.handlers.core :refer [defhandler]]
            [bark.inbox :as federator]
            [kitsune.federators.follow :as follow]))

(def handler
  (federator/inbox-handler
    {:find-object db/public-key
     :handlers
      {"Follow" follow/follow-handler
       "Accept" {"Follow" follow/accept-handler}
       "Undo" {"Follow" follow/undo-handler}}}))

(defhandler inbox
  [request]
  (handler request)
  (status/accepted))
