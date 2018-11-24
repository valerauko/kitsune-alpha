(ns kitsune.env
  (:require [ring.middleware.reload :refer [wrap-reload]]))

(def wrap wrap-reload)
