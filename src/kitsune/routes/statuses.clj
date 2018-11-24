(ns kitsune.routes.statuses
  (:require [clojure.spec.alpha :as s]
            [kitsune.wrappers.oauth :as oauth]
            [kitsune.spec.oauth :refer [auth-header-opt]]
            [kitsune.spec.statuses :as spec]
            [kitsune.spec.mastodon.status :as models]
            [kitsune.handlers.statuses :refer
              [load-status create delete]]))

(def routes
  ["/api"
   ["/v1"
    ["/statuses"
     {:swagger {:tags ["Statuses"]}
      :parameters auth-header-opt
      :responses {400 {:body {:error string?}}
                  403 {:body {:error string?}}
                  404 {:body {:error string?}}}}
     [""
      {:post {:summary "Post new status"
              :scopes #{"write"}
              :middleware [oauth/bearer-auth
                           oauth/enforce-scopes]
              :parameters {:body ::spec/create-status-request}
              :responses {200 {:body map?}}
              :handler create}}]
     ["/:id"
      {:parameters {:path {:id int?}}
       :get {:summary "Show one status"
             :responses {200 {:body ::models/status}}
             :handler load-status}
       :delete {:summary "Delete status"
                :scopes #{"write"}
                :middleware [oauth/bearer-auth
                             oauth/enforce-scopes]
                :responses {200 {:body map?}}
                :handler delete}}]]]])
