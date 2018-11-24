(ns kitsune.db.relationship
  (:require [hugsql.core :refer [def-db-fns]]
            [kitsune.db.core :refer [conn]]))

(def-db-fns "sql/relationship.sql")
