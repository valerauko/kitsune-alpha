(ns kitsune.routes.oauth
  (:require [kitsune.handlers.oauth :refer [auth-form authorize exchange-token]]
            [kitsune.spec.oauth :as oauth-spec]
            [kitsune.spec.user :as user-spec]))

(def routes
  ["/oauth"
   {:swagger {:tags ["OAuth"]}}
   ["/authorize"
    {:summary "OAuth authorization form"
     :get {:swagger {:produces #{"text/html"}}
           :responses {200 {:description "Form to authorize app"
                            :body any?}}
           :handler auth-form}
     :post {:swagger {:produces #{"text/html"}}
            :parameters {:body {:name ::user-spec/name
                                :password string?
                                :client-id ::oauth-spec/client-id
                                :scopes ::oauth-spec/scopes
                                :redirect-uri string?
                                :state string?}}
            :responses
              {200 {:description "When no redirect URI is specified for the app
                                  in question, a HTML page is rendered with the
                                  token."
                    :body any?}
               302 {:description "If there was a redirect URI, the user is
                                  redirected."
                    :body any?}}
            :handler authorize}}]
   ["/token"
    {:post {:summary "OAuth token exchange"
            :swagger {:produces #{"application/json"}}
            :parameters {:body ::oauth-spec/exchange-request
                         :header {:authorization string?}}
            :responses {403 {:description "Authn/authz failed."
                             :body {:error string?}}
                        200 {:description "On success returns new token"
                             :body any?}}
            :handler exchange-token}}]])
