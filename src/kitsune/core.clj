(ns kitsune.core
  (:require [aleph.http :as http]
            [mount.core :refer [defstate start stop]]
            [muuntaja.middleware :refer [wrap-format]]
            [clojure.tools.logging :as log]
            [ring.logger :refer [wrap-log-response]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [kitsune.instance :refer [config]]
            [kitsune.env :as env]
            [kitsune.routes.core :as routes]
            [kitsune.db.migrations :as migrations])
  (:gen-class))

(defn log-transformer
  [{{:keys [request-method uri status ring.logger/ms]} :message :as opt}]
  (assoc opt :message
    (str request-method " " status " in " (format "%3d" ms) "ms: " uri)))

(defstate ^{:on-reload :noop} http-server
  :start
    (http/start-server
      (-> routes/handler
          env/wrap
          wrap-format
          (wrap-defaults api-defaults)
          (wrap-log-response {:transform-fn log-transformer}))
      {:port (get-in config [:server :port])
       :compression true}))

(defn stop-kitsune
  []
  (doseq [component (:stopped (stop))]
    (log/info component "stopped"))
  (shutdown-agents))

(defn start-kitsune
  []
  (doseq [component (:started (start))]
    (log/info component "started"))
  (.addShutdownHook (Runtime/getRuntime) (Thread. ^Runnable stop-kitsune)))

(defn -main [& args]
  (cond
    (some #{"migrate"} args) (do (start #'kitsune.instance/config
                                        #'kitsune.db.core/conn)
                                 (migrations/migrate)
                                 (System/exit 0))
    (some #{"rollback"} args) (do (start #'kitsune.instance/config
                                         #'kitsune.db.core/conn)
                                  (migrations/rollback)
                                  (System/exit 0))
    :else (start-kitsune)))
