(ns kitsune.routes.statuses
  (:require [kitsune.wrappers.oauth :as oauth-wrapper]))

(def routes
  ["/api"
   ["/v1"
    ["/statuses"
     {:post {:summary "Post new status"
             :middleware [oauth-wrapper/bearer-auth]}}
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
