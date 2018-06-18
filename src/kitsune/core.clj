(ns kitsune.core
  (:require [aleph.http :as http]
            [reitit.ring :as ring]
            [ring.middleware.params :as params]
            [reitit.ring.coercion :as coerce]
            [reitit.coercion.spec :as spec]
            [reitit.swagger :refer [swagger-feature create-swagger-handler]]
            [reitit.swagger-ui :refer [create-swagger-ui-handler]]
            [muuntaja.middleware :refer [wrap-format]]
            [ring.logger :refer [wrap-with-logger]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [kitsune.routes.user :as user]
            [kitsune.routes.webfinger :as wf]))

(def routes
  (ring/ring-handler
    (ring/router
      [user/routes
       wf/routes
       ["/swagger.json"
        {:get {:no-doc true
               :swagger {:info {:title "kitsune API"}}
               :handler (create-swagger-handler)}}]]
      {:data {:coercion spec/coercion
              :swagger {:id ::api}
              :middleware [wrap-with-logger
                           params/wrap-params
                           wrap-format
                           swagger-feature
                           coerce/coerce-exceptions-middleware
                           coerce/coerce-request-middleware
                           coerce/coerce-response-middleware]}})
    (create-swagger-ui-handler {:path "/swagger"})))

(defn -main []
  (http/start-server (wrap-defaults routes api-defaults) {:port 3000
                                                          :compression true}))
