(ns kitsune.db.migrations
  (:require [ragtime.jdbc :as jdbc]
            [ragtime.repl :as repl]))

(defn config [conn]
  {:datastore  (jdbc/sql-database conn)
   :migrations (jdbc/load-resources "migrations")})

(defn migrate [conn]
  (repl/migrate (config conn)))

(defn rollback [conn]
  (repl/rollback (config conn)))
