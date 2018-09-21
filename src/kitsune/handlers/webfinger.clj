(ns kitsune.handlers.webfinger
  (:require [kitsune.handlers.core :refer [defhandler]]
            [kitsune.db.user :as db]
            [kitsune.db.core :refer [conn]]
            [kitsune.instance :refer [url]]
            [ring.util.http-response :refer :all]
            [vuk.core :as vuk]))

(def env-host "localhost")

(defn host-meta
  [& _]
  (ok (vuk/host-meta (url "/.well-known/webfinger?resource={uri}"))))

(defn extract-type
  [content-type]
  (->> content-type (re-find #"/(?:.+\+)?(.+)") rest first keyword))

; TODO: these are dummies, need to do this properly
(defn user-map
  [{:keys [name] :as data}]
  (let [profile (url "/people/" name)]
    {:subject (str "acct:" name "@" (:host (url)))
     :aliases [(url "/@" name) profile]
     :links [{:href profile
              :rel "http://webfinger.net/rel/profile-page"
              :type "text/html"}
             {:href profile
              :rel "self"
              :type "application/activity+json"}
             {:href profile
              :rel "self"
              :type "application/ld+json; profile=\"https://www.w3.org/ns/activitystreams\""}]}))

(defhandler resource
  [{{resource :resource :or {resource ""}} :query-params
    {content-type :accept :or {content-type "application/json"}} :headers}]
  (let [[user host] (->> resource (re-matches #"(?i)acct:(\w+)@(.+)") rest)]
    (if (= env-host host)
      (if-let [data (db/find-by-name conn {:name user})]
        (ok (vuk/represent (user-map data) :as (extract-type content-type)))
        (not-found ""))
      (not-found ""))))
