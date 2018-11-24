(ns kitsune.core
  (:require [clojure.tools.logging :as log]
            [clojure.string :refer [upper-case]]
            [aleph.http :as http]
            [mount.core :refer [defstate start stop]]
            [muuntaja.middleware :refer [wrap-format]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [kitsune.instance :refer [config]]
            [kitsune.env :as env]
            [kitsune.db.core :refer [conn]]
            [kitsune.routes.core :as routes]
            [kitsune.db.migrations :as migrations]
            [clojure.tools.namespace.repl :refer [refresh]])
  (:gen-class))

(defn wrap-logging
  [handler]
  (fn [{:keys [request-method uri remote-addr]
        {fwd-for :X-Forwarded-For} :headers
        :as request}]
    (let [start (System/nanoTime)
          response (handler request)]
      (log/info (format "%d %s %s for %s in %.3fms"
                        (:status response)
                        (-> request-method name upper-case)
                        uri
                        (or fwd-for remote-addr)
                        (/ (- (System/nanoTime) start) 1000000.0)))
      response)))

(defstate ^{:on-reload :noop} http-server
  :start
    (http/start-server
      (-> routes/handler
          env/wrap
          wrap-format
          (wrap-defaults api-defaults)
          wrap-logging)
      {:port (get-in config [:server :port])
       :compression true})
  :stop
    (.close ^java.io.Closeable http-server))

(defn stop-kitsune
  []
  (doseq [component (:stopped (stop))]
    (log/info component "stopped")))

(defn start-kitsune
  []
  (doseq [component (:started (start))]
    (log/info component "started"))
  (.addShutdownHook (Runtime/getRuntime) (Thread. ^Runnable stop-kitsune)))

(defn reload
  []
  (stop-kitsune)
  (refresh)
  (start-kitsune))

(defn -main [& args]
  (cond
    (some #{"migrate"} args) (do (start #'kitsune.instance/config
                                        #'kitsune.db.core/datasource
                                        #'kitsune.db.core/conn)
                                 (migrations/migrate conn)
                                 (System/exit 0))
    (some #{"rollback"} args) (do (start #'kitsune.instance/config
                                         #'kitsune.db.core/datasource
                                         #'kitsune.db.core/conn)
                                  (migrations/rollback conn)
                                  (System/exit 0))
    :else (start-kitsune)))
