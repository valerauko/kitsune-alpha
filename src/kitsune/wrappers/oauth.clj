(ns kitsune.wrappers.oauth
  (:require [kitsune.db.oauth :as db]
            [kitsune.db.core :refer [conn]]
            [kitsune.handlers.core :refer [url-decode]]))

(defn bearer-auth
  "Inserts :app into request with client info from Bearer token."
  [handler]
  (fn [req]
    (let [raw (some-> req :headers :authorization
                          (#(second (re-matches #"^Bearer (.+)" %)))
                          url-decode)
          token (db/find-bearer conn {:token raw})]
      (handler (assoc req :auth token)))))
