(ns kitsune.core
  (:require [aleph.http :as http]
            [reitit.ring :as ring]
            [reitit.ring.coercion :as coerce]
            [reitit.coercion.spec :as spec]
            [reitit.swagger :refer [swagger-feature create-swagger-handler]]
            [reitit.swagger-ui :refer [create-swagger-ui-handler]]
            [muuntaja.middleware :refer [wrap-format]]
            [ring.logger :refer [wrap-with-logger]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [kitsune.handlers.user :as user]))

(def routes
  (ring/ring-handler
    (ring/router
      [;["/auth" ]
       ["/people"
        {:summary "ActivityPub based user accounts API"}
        [""
         {:post {:summary "Create new user"
                 :parameters {:body {:user :kitsune.spec.user/registration}}
                 :responses {200 {:body {:name :kitsune.spec.user/name}}}
                 :handler user/create}}]
        ["/:name"
         {:parameters {:path {:name :kitsune.spec.user/name}}
          :get {:summary "User profile"
                :responses {200 {:body :kitsune.spec.user/show}
                            404 {:body {:error :kitsune.spec.user/name}}}
                :handler user/show}
          :put {:summary "Update user profile"
                :parameters {:body {:user :kitsune.spec.user/profile-update}}
                :responses {200 {:body :kitsune.spec.user/show}
                            404 {:body {:error :kitsune.spec.user/name}}}
                :handler user/update-profile}
          :delete {:summary "Delete account"
                   :responses {404 {:body {:error :kitsune.spec.user/name}}}
                   :handler user/destroy}}]]
       ["/swagger.json"
        {:get {:no-doc true
               :swagger {:info {:title "kitsune API"}}
               :handler (create-swagger-handler)}}]]
      {:data {:coercion spec/coercion
              :swagger {:id ::api}
              :middleware [wrap-format
                           coerce/coerce-exceptions-middleware
                           coerce/coerce-request-middleware
                           coerce/coerce-response-middleware
                           swagger-feature
                           wrap-with-logger]}})
    (create-swagger-ui-handler {:path "/swagger"})))

(defn -main []
  (http/start-server (wrap-defaults routes api-defaults) {:port 3000
                                                          :compression true}))
