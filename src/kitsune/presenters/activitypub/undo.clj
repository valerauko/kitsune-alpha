(ns kitsune.presenters.activitypub.undo
  (:require [kitsune.uri :as uri])
  (:import [java.util UUID]))

(defn undo
  [{{actor :actor} :object :as activity}]
  {:type "Undo"
   :id (str (uri/url "/undo/" (UUID/randomUUID)))
   :actor actor
   :object activity})
