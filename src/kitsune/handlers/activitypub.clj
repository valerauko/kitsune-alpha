(ns kitsune.handlers.activitypub
  (:require [clojure.core.async :as async]
            [ring.util.http-response :as status]
            [kitsune.handlers.core :refer [defhandler]]
            [kitsune.federators.inbox :as federator]))

(defhandler inbox
  [{{{:keys [id type object actor]
      :as activity} :body} :parameters
    :as request}]
  (if-not type
    (status/bad-request {:error "Activities must have a type"})
    (do
      (async/go (federator/record request))
      (status/accepted))))
