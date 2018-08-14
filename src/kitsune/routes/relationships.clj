(ns kitsune.routes.relationships
  (:require [kitsune.spec.mastodon.relationship :as models]
            [kitsune.wrappers.oauth :as oauth]
            [kitsune.spec.oauth :refer [auth-header-opt]]
            [kitsune.handlers.relationships :refer [follow unfollow]]))

(def routes
  ["/api/v1"
   ["/accounts"
    ["/:id"
     {:parameters (merge {:path {:id int?}}
                         auth-header-opt)
      :scopes #{"follow"}
      :middleware [oauth/bearer-auth
                   oauth/enforce-scopes]}
     ["/follow"
      {:swagger {:tags ["Relationships"]}
       :post {:summary "Follow user"
              :responses {200 {:body ::models/relationship}}
              :handler follow}}]
     ["/unfollow"
      {:swagger {:tags ["Relationships"]}
       :post {:summary "Unfollow user"
              :responses {200 {:body ::models/relationship}}
              :handler unfollow}}]]]])
