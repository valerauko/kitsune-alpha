(ns kitsune.core
  (:require [aleph.http :as http]
            [reitit.ring :as ring]
            [reitit.spec :as router-spec]
            [reitit.ring.spec :as ring-spec]
            [reitit.ring.coercion :as coerce]
            [reitit.coercion.spec :as spec]
            [reitit.swagger :refer [swagger-feature create-swagger-handler]]
            [reitit.swagger-ui :refer [create-swagger-ui-handler]]
            [muuntaja.middleware :refer [wrap-format]]
            [ring.logger :refer [wrap-log-response]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [kitsune.routes.user :as user]
            [kitsune.routes.webfinger :as webfinger]
            [kitsune.routes.mastodon :as mastodon]))

(def routes
  (ring/ring-handler
    (ring/router
      [user/routes
       webfinger/routes
       mastodon/routes
       ["/swagger.json"
        {:get {:no-doc true
               :swagger {:info {:title "kitsune API"}}
               :handler (create-swagger-handler)}}]]
      {:validate ring-spec/validate-spec!
       :data {:coercion spec/coercion
              :swagger {:id ::api}
              :middleware [wrap-format
                           swagger-feature
                           coerce/coerce-exceptions-middleware
                           coerce/coerce-request-middleware
                           coerce/coerce-response-middleware]}})
    (ring/routes
      (create-swagger-ui-handler {:path "/swagger"})
      (fn [& req] {:status 404 :body {:error "Not found"} :headers {}}))))

(defn log-transformer
  [{{:keys [request-method uri status ring.logger/ms]} :message :as opt}]
  (assoc opt :message
    (str request-method " " status " in " (format "%3d" ms) "ms: " uri)))

(def handler
  (-> routes
      (wrap-defaults api-defaults)
      (wrap-log-response {:transform-fn log-transformer})))

(defn -main []
  (http/start-server
    handler
    {:port 3000 :compression true}))
