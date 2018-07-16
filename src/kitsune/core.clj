(ns kitsune.core
  (:require [aleph.http :as http]
            [muuntaja.middleware :refer [wrap-format]]
            [ring.logger :refer [wrap-log-response]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [kitsune.instance :refer [server-config]]
            [kitsune.routes.core :as routes])
  (:gen-class))

(defn log-transformer
  [{{:keys [request-method uri status ring.logger/ms]} :message :as opt}]
  (assoc opt :message
    (str request-method " " status " in " (format "%3d" ms) "ms: " uri)))

(def handler
  (-> routes/handler
      wrap-format
      (wrap-defaults api-defaults)
      (wrap-log-response {:transform-fn log-transformer})))

(defn -main []
  (http/start-server
    handler
    {:port (server-config :port) :compression true}))
