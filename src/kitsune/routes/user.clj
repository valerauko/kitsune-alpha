(ns kitsune.routes.user
  (:require [kitsune.handlers.user :as user]
            [kitsune.wrappers.oauth :as oauth]
            [kitsune.spec.oauth :refer [auth-header-opt auth-header-req]]
            [kitsune.spec.user :as spec]))

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
             :parameters (merge auth-header-req ; should be a known app
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
    ["/update_credentials"
     {:patch {:summary "Update user profile"
              :middleware [oauth/bearer-auth
                           oauth/enforce-scopes]
              :parameters (merge auth-header-req
                                 {:body ::spec/mastodon-update})
              :responses {200 {:body any?}}
              :handler user/mastodon-update}}]
    ["/verify_credentials"
     {:get {:summary "Account details of the current account"
            :middleware [oauth/bearer-auth
                         oauth/enforce-scopes]
            :parameters auth-header-req
            :responses {200 {:body any?}}
            :handler user/self}}]
    ["/:id"
     {:parameters {:path {:id int?}}
      :get {:summary "User profile"
            :responses {200 {:body any?}}
            :handler user/show}}]]])
