(ns kitsune.handler
  (:require [reitit.ring :as ring]
            [reitit.coercion.spec :as spec]
            [reitit.swagger :refer [swagger-feature create-swagger-handler]]
            [reitit.swagger-ui :refer [create-swagger-ui-handler]]
            [muuntaja.middleware :refer [wrap-format]]
            [ring.logger :refer [wrap-with-logger]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]))

(def routes
  (ring/ring-handler
    (ring/router
      [["/people"
        {:summary "ActivityPub based user accounts API"
         :swagger {:tags ["ActivityPub"]}}
        [""
         {:post {:summary "Create new user"
                 :parameters {:body {:email string?}}
                 :handler (constantly :todo)}}]
        ["/:name"
         {:parameters {:path {:name string?}}
          :get {:summary "User profile"
                :handler (constantly :todo)}
          :put {:summary "Update user profile"
                :parameters {:body {:hello string?}}
                :handler (constantly :todo)}
          :delete {:summary "Delete account"
                   :handler (constantly :todo)}}]]
       ["/swagger.json"
        {:get {:no-doc true
               :swagger {:info {:title "kitsune API"}}
               :handler (create-swagger-handler)}}]]
      {:data {:coercion spec/coercion
              :swagger {:id ::api}
              :middleware [wrap-with-logger
                           wrap-format
                           swagger-feature]}})
    (create-swagger-ui-handler {:path "/swagger"})))

(def app
  (wrap-defaults routes api-defaults))
