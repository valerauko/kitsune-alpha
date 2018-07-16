(ns kitsune.handlers.webfinger
  (:require [kitsune.handlers.core :refer [defhandler]]
            [kitsune.db.user :as db]
            [kitsune.db.core :refer [conn]]
            [ring.util.http-response :refer :all]
            [vuk.core :as vuk]))

(def env-host "localhost")

(defn host-meta
  [& _]
  (ok (vuk/host-meta (str env-host "/.well-known/webfinger?resource={uri}"))))

(defn extract-type
  [content-type]
  (->> content-type (re-find #"/(?:.+\+)?(.+)") rest first keyword))

; TODO: these are dummies, need to do this properly
(defn user-map
  [{:keys [name] :as data}]
  {:subject (str "acct:" name "@" env-host)
   :aliases [(str env-host "/@" name)
             (str env-host "/people/" name)]})

(defhandler resource
  [{{resource :resource :or {resource ""}} :query-params
    {content-type :accept :or {content-type "application/json"}} :headers}]
  (let [[user host] (->> resource (re-matches #"(?i)acct:(\w+)@(.+)") rest)]
    (if (= env-host host)
      (if-let [data (db/lookup conn {:name user})]
        (ok (vuk/represent (user-map data) :as (extract-type content-type)))
        (not-found ""))
      (not-found ""))))
