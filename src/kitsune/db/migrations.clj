(ns kitsune.db.migrations
  (:require [ragtime.jdbc :as jdbc]
            [ragtime.repl :as repl]
            [kitsune.db.core :as db]))

(def config
  { :datastore  (jdbc/sql-database db/conn)
    :migrations (jdbc/load-resources "migrations") })

(defn migrate[]
  (repl/migrate config))

(defn rollback []
  (repl/rollback config))
