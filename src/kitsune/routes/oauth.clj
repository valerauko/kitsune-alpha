(ns kitsune.routes.oauth
  (:require [kitsune.handlers.oauth :refer [auth-form authorize exchange-token]]
            [kitsune.wrappers.oauth :as oauth-wrapper]
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
            :parameters {:body ::oauth-spec/authorize-params}
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
    {:post {:summary "OAuth token exchange / refresh"
            :swagger {:produces #{"application/json"}}
            :parameters (merge {:body ::oauth-spec/exchange-request}
                               oauth-spec/header-params)
            :responses {403 {:description "Authn/authz failed."
                             :body {:error string?}}
                        200 {:description "On success returns new token"
                             :body any?}}
            :handler exchange-token}}]])
