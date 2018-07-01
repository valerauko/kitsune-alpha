(ns kitsune.routes.mastodon
  (:require [kitsune.handlers.oauth :as oauth]
            [kitsune.spec.oauth :as oauth-spec]))

(def routes
  ["/api"
   {:swagger {:tags ["Mastodon"]
              :produces #{"application/json" "application/ld+json; profile=\"https://www.w3.org/ns/activitystreams\""}}}
   ["/v1"
    ["/apps"
     {:swagger {:tags ["OAuth"]}
      :post {:summary "OAuth app registration"
             :parameters {:body ::oauth-spec/create-app}
             :produces #{"application/json"}
             :responses {200 {:body ::oauth-spec/register-response}}
             :handler oauth/register-app}}]]])
