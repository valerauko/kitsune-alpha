(ns kitsune.routes.user
  (:require [spec-tools.data-spec :as ds]
            [kitsune.handlers.activitypub :as ap]
            [kitsune.handlers.user :as user]
            [kitsune.handlers.statuses :refer [account-statuses]]
            [kitsune.wrappers.oauth :as oauth]
            [kitsune.spec.oauth :refer [auth-header-opt auth-header-req]]
            [kitsune.spec.user :as spec]
            [kitsune.spec.mastodon.status :as status-spec]))

(def routes
  [["/inbox" ; maybe this should go to its own file?
    {:summary "ActivityPub shared inbox"
     :swagger {:tags ["ActivityPub"]}
     :post {:responses {200 {:body any?}
                        400 {:body {:error string?}}
                        403 {:body {:error string?}}
                        404 {:body {:error string?}}}
            :parameters {:body {:id any?
                                :object any?
                                :type any?
                                :actor any?}}
            :handler ap/inbox}}]
   ["/people"
    {:summary "Kitsune-unique user endpoints"
     :swagger {:tags ["Users"]}
     :parameters auth-header-opt
     :responses {200 {:body any?}
                 400 {:body {:error string?}}
                 403 {:body {:error string?}}
                 404 {:body {:error string?}}}}
    [""
     {:post {:summary "Create new user"
             :parameters (merge auth-header-opt ; TODO: should be the local UI
                                {:body {:user ::spec/registration}})
             :responses {200 {:body {:name ::spec/name}}}
             :handler user/create}}]
    ["/:name"
     {:parameters {:path {:name ::spec/name}}}
     [""
      {:get {:summary "User's profile for ActivityPub"
             :handler user/ap-show}
       ; TODO: this needs massive rework
       :delete {:summary "Delete account"
                :middleware [oauth/bearer-auth
                             oauth/enforce-scopes]
                :handler user/destroy}}]
     ["/followers"
      {:get {:summary "List of accounts following the user"
             :parameters {:query (ds/spec ::opt-page {(ds/opt :page) pos-int?})}
             :handler user/ap-followers}}]
     ["/following"
      {:get {:summary "List of accounts followed by the user"
             :parameters {:query (ds/spec ::opt-page {(ds/opt :page) pos-int?})}
             :handler user/ap-following}}]
     ["/inbox"
      {:post {:summary "User-specific ActivityPub inbox"
              :parameters {:body {:id any?
                                  :type any?
                                  :actor any?
                                  :object any?}}
              :handler ap/inbox}}]
      ["/outbox"
       {:get {:summary "AP outbox of the user"
              :handler (constantly {:status 200 :body {}})}}]]]
   ["/api/v1/accounts"
    {:summary "Mastodon compatible user endpoints"
     :swagger {:tags ["Users"]}
     :parameters auth-header-opt
     :responses {200 {:body any?}
                 400 {:body {:error string?}}
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
            :handler user/search}}]
    ["/update_credentials"
     {:patch {:summary "Update user profile"
              :scopes #{"write"}
              :middleware [oauth/bearer-auth
                           oauth/enforce-scopes]
              :parameters (merge auth-header-req
                                 {:body ::spec/mastodon-update})
              :handler user/update-stuff}}]
    ["/verify_credentials"
     {:get {:summary "Account details of the current account"
            :scopes #{"read"}
            :middleware [oauth/bearer-auth
                         oauth/enforce-scopes]
            :parameters auth-header-req
            :handler user/self}}]
    ["/:id"
     {:parameters {:path {:id int?}}
      :get {:summary "User profile"
            :handler user/show}}
     ["/statuses"
      {:get {:summary "Statuses by user"
             :scopes #{"read"}
             :middleware [oauth/bearer-auth
                          oauth/enforce-scopes]
             :responses {200 {:body ::status-spec/statuses}}
             :handler account-statuses}}]]]])
