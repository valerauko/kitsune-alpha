(ns kitsune.db.user
  (:require [hugsql.core :refer [def-db-fns]]))

(def-db-fns "sql/users.sql")
