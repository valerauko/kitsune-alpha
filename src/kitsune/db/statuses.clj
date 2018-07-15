(ns kitsune.db.statuses
  (:require [kitsune.db.core :refer [conn]]
            [kitsune.instance :refer [url]]
            [hugsql.core :refer [def-db-fns]]
            [clojure.java.jdbc :as jdbc])
  (:import java.util.UUID))

(def-db-fns "sql/activitypub.sql")

(defn uuid
  []
  (.toString (UUID/randomUUID)))

(defn new-status-uri
  []
  (str (url (str "/objects/" (uuid)))))

(defn new-activity-uri
  []
  (str (url (str "/activities/" (uuid)))))

(defn create-status!
  [people data]
  (jdbc/with-db-transaction [tx conn]
    (if-let [object (create-object! tx (merge {:type "Note"
                                               :uri (new-status-uri)}
                                              people
                                              data))]
      (if-let [activity (create-activity! tx (merge {:type "Create"
                                                     :uri (new-activity-uri)}
                                                    people
                                                    {:object-id (:id object)}))]
        {:object object :activity activity}))))
