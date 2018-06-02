(ns konkon.handler
  (:require [reitit.ring :as ring]
            [reitit.swagger :refer [create-swagger-handler]]))

(def routes
  (ring/router
    [ mastodon/routes
      webfinger/routes
      ["/swagger.json"
        {:get {:no-doc true
               :swagger {:info {:title "Konkon API"}}
               :handler (create-swagger-handler)}}]]))
