(ns kitsune.db.instance
  (:require [hugsql.core :refer [def-db-fns]]))

; TODO: 今は毎回COUNT走らせてるからけっこう効率悪い。Railsのcounter_cacheみたいなのが欲しい
(def-db-fns "sql/instance.sql")
