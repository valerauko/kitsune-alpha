(ns konkon.handler
  (:require [reitit.ring :as ring]
            [reitit.swagger :refer [create-swagger-handler]]))

(defn todo [])

(def routes
  (ring/router
    [ ["/api"
        {:data {:summary "Mastodon compatible API"}}
        ["/v1"
          ["/accounts"
            ["/:user-id"
              {:get {:handler todo}}
              ["/followers"
                {:get {:handler todo}}]
              ["/following"
                {:get {:handler todo}}]
              ["/statuses"
                {:get {:handler todo}}]
              ["/follow"
                {:post {:handler todo}}]
              ["/unfollow"
                {:post {:handler todo}}]
              ["/block"
                {:post {:handler todo}}]
              ["/unblock"
                {:post {:handler todo}}]
              ["/mute"
                {:post {:handler todo}}]
              ["/unmute"
                {:post {:handler todo}}]]
            ["/relationships"
              {:get {:handler todo}}]
            ["/search"
              {:get {:handler todo}}]
            ["/verify_credentials"
              {:get {:handler todo}}]
            ["/update_credentials"
              {:patch {:handler todo}}]]]
        ["/v2"]]
      ["/.well-known"
        {:data {:summary "WebFinger endpoints"}}]
      ["/swagger.json"
        {:get {:no-doc true
               :swagger {:info {:title "Konkon API"}}
               :handler (create-swagger-handler)}}]]))
