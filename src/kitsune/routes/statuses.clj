(ns kitsune.routes.statuses
  (:require [kitsune.wrappers.oauth :as oauth]
            [kitsune.spec.statuses :as spec]
            [kitsune.handlers.statuses :refer [create-status]]))

(def routes
  ["/api"
   ["/v1"
    ["/statuses"
     {:post {:summary "Post new status"
             :scopes #{"write"}
             :middleware [oauth/bearer-auth
                          oauth/enforce-scopes]
             :parameters {:body ::spec/create-status-request}
             :responses {200 ::spec/create-status-response}
             :handler create-status}}
     ["/:id"
      {:get {:summary "Show one status"}
       :delete {:summary "Delete status"}}
      ["/reblog"
       {:post {}}]
      ["/unreblog"
       {:post {}}]
      ["/favourite"
       {:post {}}]
      ["/unfavourite"
       {:post {}}]
      ["/context"
       {:get {}}]
      ["/card"
       {:get {}}]
      ["/favourited_by"
       {:get {}}]
      ["/reblogged_by"
       {:get {}}]]]]])
