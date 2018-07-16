(ns kitsune.routes.core
  (:require [reitit.ring :as ring]
            [reitit.ring.spec :as ring-spec]
            [reitit.ring.coercion :as coerce]
            [reitit.coercion.spec :as spec]
            [reitit.swagger :refer [swagger-feature create-swagger-handler]]
            [reitit.swagger-ui :refer [create-swagger-ui-handler]]
            [kitsune.instance :refer [version]]
            [kitsune.routes.user :as user]
            [kitsune.routes.oauth :as oauth]
            [kitsune.routes.webfinger :as webfinger]
            [kitsune.routes.statuses :as statuses]
            [kitsune.routes.instance :as instance]))

(def handler
  (ring/ring-handler
    (ring/router
      [user/routes
       webfinger/routes
       oauth/routes
       instance/routes
       statuses/routes
       ["/swagger.json"
        {:get {:no-doc true
               :swagger {:info {:title "Kitsune API"
                                :description "Very fox microblogging"
                                :version version}
                         :basePath "/"}
               :handler (create-swagger-handler)}}]]
      {:validate ring-spec/validate-spec!
       :data {:coercion spec/coercion
              :swagger {:id ::api}
              :middleware [swagger-feature
                           coerce/coerce-exceptions-middleware
                          ; coerce/coerce-request-middleware
                           coerce/coerce-response-middleware]}})
    (ring/routes
      (create-swagger-ui-handler {:path "/swagger"})
      (fn [& req] {:status 404 :body {:error "Not found"} :headers {}}))))
