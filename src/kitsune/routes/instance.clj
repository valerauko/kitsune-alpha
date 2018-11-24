(ns kitsune.routes.instance
  (:require [kitsune.handlers.instance :refer [node-schema node-info
                                               instance-info emoji]]
            [kitsune.spec.mastodon.application :as spec]))

(def routes
  [["/api"
    ["/v1"
     {:swagger {:tags ["Instance info"]}}
     ["/instance" {:summary "Basic information about the instance"
                   :get {:responses {200 {:body map?}}
                         :handler instance-info}}]
     ["/custom_emojis" {:summary "List of custom emoji on this instance"
                        :get {:responses {200 {:body ::spec/object-vector}}
                              :handler emoji}}]]]
   ["/.well-known"
    ["/nodeinfo"
     {:swagger {:tags ["Instance info"]}}
     ["" {:summary "Nodeinfo schema"
          :get {:responses {200 {:body {:links ::spec/object-vector}}}
                :handler node-schema}}]
     ["/2.0" {:summary "Nodeinfo"
              :produces #{"application/json; profile=\"http://nodeinfo.diaspora.software/ns/schema/2.0#\"; charset=utf-8"}
              :get {:responses {200 {:body map?}}
                    :handler node-info}}]]]])
