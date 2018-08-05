(ns kitsune.routes.user
  (:require [kitsune.handlers.user :as user]
            [kitsune.handlers.statuses :refer [account-statuses]]
            [kitsune.wrappers.oauth :as oauth]
            [kitsune.spec.oauth :refer [auth-header-opt auth-header-req]]
            [kitsune.spec.user :as spec]
            [kitsune.spec.mastodon.status :as status-spec]))

(def routes
  [["/people"
    {:summary "Kitsune-unique user endpoints"
     :swagger {:tags ["Users"]}
     :parameters auth-header-opt
     :responses {400 {:body {:error string?}}
                 403 {:body {:error string?}}
                 404 {:body {:error string?}}}}
    [""
     {:post {:summary "Create new user"
             :parameters (merge auth-header-opt ; TODO: should be the local UI
                                {:body {:user ::spec/registration}})
             :responses {200 {:body {:name ::spec/name}}}
             :handler user/create}}]
    ["/:name"
     ; TODO: this needs massive rework
     {:delete {:summary "Delete account"
               :middleware [oauth/bearer-auth
                            oauth/enforce-scopes]
               :responses {200 {:body any?}}
               :handler user/destroy}}]]
   ["/api/v1/accounts"
    {:summary "Mastodon compatible user endpoints"
     :swagger {:tags ["Users"]}
     :parameters auth-header-opt
     :responses {400 {:body {:error string?}}
                 403 {:body {:error string?}}
                 404 {:body {:error string?}}}}
    ["/search"
     {:get {:summary "Account search"
            :description "Tries to find local or remote user. Can find any of:
                          - username only
                          - username@domain
                          - URL (both human-readable and AP)
                          It attempts lookups in that order."
            :parameters {:query {:query string?}}
            :responses {200 {:body any?}}
            :handler user/search}}]
    ["/update_credentials"
     {:patch {:summary "Update user profile"
              :scopes #{"write"}
              :middleware [oauth/bearer-auth
                           oauth/enforce-scopes]
              :parameters (merge auth-header-req
                                 {:body ::spec/mastodon-update})
              :responses {200 {:body any?}}
              :handler user/mastodon-update}}]
    ["/verify_credentials"
     {:get {:summary "Account details of the current account"
            :scopes #{"read"}
            :middleware [oauth/bearer-auth
                         oauth/enforce-scopes]
            :parameters auth-header-req
            :responses {200 {:body any?}}
            :handler user/self}}]
    ["/:id"
     {:parameters {:path {:id int?}}
      :get {:summary "User profile"
            :responses {200 {:body any?}}
            :handler user/show}}
     ["/statuses"
      {:get {:summary "Statueses by user"
             :scopes #{"read"}
             :middleware [oauth/bearer-auth
                          oauth/enforce-scopes]
             :responses {200 {:body ::status-spec/statuses}}
             :handler account-statuses}}]]]])
