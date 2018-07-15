(ns kitsune.handlers.core
  (:require [ring.util.http-response :refer [internal-server-error]]
            [kitsune.config :refer [env]]
            [clojure.tools.logging :as log])
  (:import java.net.URLDecoder))

(defmacro defhandler
  "Handler macros -- from github.com/yogthos/krueger"
  [fn-name args & body]
  `(defn ~fn-name ~args
     (try
       ~@body
       (catch Throwable e#
         (internal-server-error {:error (do
                                          (log/error e#)
                                          ; don't respond with error details unless in dev
                                          (if (= env "dev")
                                            (str e#)
                                            "Unexpected error. Sorry."))})))))

(defn url-decode [str] (URLDecoder/decode str "UTF-8"))
