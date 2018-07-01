(ns kitsune.db.core
  (:require [hikari-cp.core :refer [make-datasource]]
            [hugsql.core]
            [clojure.java.jdbc :as jdbc]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [camel-snake-kebab.core :refer [->kebab-case-keyword]])
  (:import org.postgresql.jdbc.PgArray))

(def options
  {:server-name   "kitsune_db"
   :adapter       "postgresql"
   :database-name "kitsune"
   :username      "kitsune"
   :password      "whatever"})

(def conn
  {:datasource (make-datasource options)})

(extend-protocol jdbc/IResultSetReadColumn
  org.postgresql.jdbc.PgArray
  (result-set-read-column [value metadata index]
    (vec (.getArray value))))

; these are from luminus too
(defn result-one-snake->kebab
  [this result options]
  (->> (hugsql.adapter/result-one this result options)
       (transform-keys ->kebab-case-keyword)))

(defn result-many-snake->kebab
  [this result options]
  (->> (hugsql.adapter/result-many this result options)
       (map #(transform-keys ->kebab-case-keyword %))))

(defmethod hugsql.core/hugsql-result-fn :1 [sym]
  'kitsune.db.core/result-one-snake->kebab)

(defmethod hugsql.core/hugsql-result-fn :one [sym]
  'kitsune.db.core/result-one-snake->kebab)

(defmethod hugsql.core/hugsql-result-fn :* [sym]
  'kitsune.db.core/result-many-snake->kebab)

(defmethod hugsql.core/hugsql-result-fn :many [sym]
  'kitsune.db.core/result-many-snake->kebab)
