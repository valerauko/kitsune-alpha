(ns kitsune.handlers.webfinger
  (:require [kitsune.handlers.core :refer [defhandler]]
            [kitsune.db.user :as db]
            [kitsune.db.core :refer [conn]]
            [kitsune.uri :refer [url]]
            [ring.util.http-response :refer :all]
            [vuk.core :as vuk]
            [csele.keys :refer [salmon-public-key]]))

(defn host-meta
  [{{:keys [accept] :or {accept "application/xrd+xml"}} :headers}]
  (case accept
    ("application/xrd+xml" "application/xml")
      {:status 200
       :body (vuk/host-meta (str (url "/.well-known/webfinger?resource={uri}")))
       :headers {"Content-Type" "application"}}
    (not-acceptable)))

(defn extract-type
  [content-type]
  (->> content-type (re-find #"/(?:.+\+)?(.+)") rest first keyword))

(defn user-map
  [{:keys [id acct name public-key] :as data}]
  (let [profile (str (url "/people/" name))]
    {:subject (str "acct:" acct)
     :aliases [(str (url "/@" name)) profile]
     :links
       [{:href profile ; TODO: maybe this should be separate for ease of UI?
         :rel "http://webfinger.net/rel/profile-page"
         :type "text/html"}
        {:href profile
         :rel "self"
         :type "application/activity+json"}
        {:href profile
         :rel "self"
         :type "application/ld+json; profile=\"https://www.w3.org/ns/activitystreams\""}
        {:href (str "data:application/magic-public-key,"
                    (salmon-public-key
                      (or public-key
                          (-> id db/update-user-keypair :public-key))))
         :rel "magic-public-key"}]}))

(defhandler resource
  [{{resource "resource" :or {resource ""}} :query-params
    {content-type :accept :or {content-type "application/json"}} :headers}]
  (let [[user host] (->> resource (re-seq #"(?i)(?:acct:)?@?([^@]+)@([^:\pZ]+)") first rest)]
    (if (= (:host (url)) host)
      (if-let [data (db/find-by-name conn {:name user})]
        {:status 200
         :body (vuk/represent (user-map data) :as (extract-type content-type))
         :headers {"Content-Type" content-type}}
        (not-found ""))
      (not-found ""))))
