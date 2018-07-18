(ns kitsune.routes.user
  (:require [kitsune.handlers.user :as user]
            [kitsune.wrappers.oauth :as oauth]
            [kitsune.spec.oauth :refer [header-params]]
            [kitsune.spec.user :as spec]))

(def routes
  [["/people"
    {:summary "Kitsune-unique user endpoints"
     :swagger {:tags ["Users"]}
     :parameters header-params
     :responses {400 {:body {:error string?}}
                 403 {:body {:error string?}}
                 404 {:body {:error string?}}}}
    [""
     {:post {:summary "Create new user"
             :parameters {:body {:user ::spec/registration}}
             :responses {200 {:body {:name ::spec/name}}}
             :handler user/create}}]
    ["/:name"
     {:delete {:summary "Delete account"
               :middleware [oauth/bearer-auth
                            oauth/enforce-scopes]
               :responses {200 {:body any?}}
               :handler user/destroy}}]]
   ["/api/v1/accounts"
    {:summary "Mastodon compatible user endpoints"
     :swagger {:tags ["Users"]}
     :parameters header-params
     :responses {400 {:body {:error string?}}
                 403 {:body {:error string?}}
                 404 {:body {:error string?}}}}
    ["/:id"
     {:parameters {:path {:id int?}}
      :get {:summary "User profile"
            :responses {200 {:body any?}}
            :handler user/show}
      :patch {:summary "Update user profile"
              :middleware [oauth/bearer-auth
                          oauth/enforce-scopes]
              :parameters {:body ::spec/mastodon-update}
              :responses {200 {:body any?}}
              :handler user/mastodon-update}}]]])
