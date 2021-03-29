(ns kitsune.db.oauth
  (:require [hugsql.core :refer [def-db-fns]]
            [clojure.string :refer [split]]))

(def-db-fns "sql/oauth.sql")

(defn array-string
  [arr]
  (if (string? arr)
    (split arr #"\s+")
    arr))
