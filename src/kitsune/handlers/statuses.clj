(ns kitsune.handlers.statuses
  (:require [ring.util.http-response :refer :all]
            [kitsune.handlers.core :refer [defhandler]]))

(defhandler create-status
  [{{:keys [text in-reply-to attachments to cc]} :body-params :as req}]
  (ok "todo"))
