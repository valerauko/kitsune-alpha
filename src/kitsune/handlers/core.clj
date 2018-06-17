(ns kitsune.handlers.core
  (:require [ring.util.http-response :refer [internal-server-error]]))

(defmacro defhandler
  "Handler macros -- from github.com/yogthos/krueger"
  [fn-name args & body]
  `(defn ~fn-name ~args
     (try
       ~@body
       (catch Throwable e#
         (internal-server-error {:error (str e#)})))))
