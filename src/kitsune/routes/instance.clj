(ns kitsune.routes.instance
  (:require [kitsune.handlers.instance :refer [node-schema node-info]]))

(def routes
  [["/api"
    ["/v1"
     ["/instance" {:handler constantly}]
     ["/custom_emojis" {:handler constantly}]]]
   ["/.well-known"
    ["/nodeinfo"
     {:swagger {:tags ["NodeInfo"]}}
     ["" {:summary "Nodeinfo schema"
          :get {:responses {200 {:body {:links vector?}}}
                :handler node-schema}}]
     ["/2.0" {:summary "Nodeinfo"
              :get {:responses {200 {:body map?}}
                    :handler node-info}}]]]])
