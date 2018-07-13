(ns kitsune.db.statuses
  (:require [kitsune.db.core :refer [conn]]
            [hugsql.core :refer [def-db-fns]]
            [clojure.java.jdbc :as jdbc]))

(def-db-fns "sql/activitypub.sql")

(defn new-status-uri
  []
  (str "http://" "localhost:3000" "/objects/" "uuid"))

(defn new-activity-uri
  []
  (str "http://" "localhost:3000" "/activities/" "uuid"))

(defn create-status
  [people data]
  (jdbc/with-db-transaction [tx conn]
    (if-let [object (create-object! tx (merge {:type "Note"
                                               :uri (new-status-uri)}
                                              people
                                              data))]
      (when-let [activity (create-activity! tx (merge {:type "Create"
                                                     :uri (new-activity-uri)}
                                                    people
                                                    {:object-id (:id object)}))]
        (println object activity)
        {:object object :activity activity}))))
