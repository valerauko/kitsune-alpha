(ns kitsune.routes.webfinger
  (:require [kitsune.handlers.webfinger :refer :all]
            [kitsune.spec.webfinger :as spec]))

; TODO: response types don't show up in the swagger spec correctly,
; it doesn't show the dropdown menu to pick and i've got no idea why
; maybe needs individual :responses defined?
(def routes
  ["/.well-known"
    {:summary "WebFinger endpoints"
     :swagger {:tags ["WebFinger"]}}
    ["/host-meta"
      {:get {:summary "Host metadata"
             :swagger {:produces #{"application/xrd+xml" "application/xml"}}
             :responses {200 {:body any?}}
             :handler host-meta}}]
    ["/webfinger"
      {:get {:summary "WebFinger endpoint for users"
             :swagger {:produces #{"application/xrd+xml" "application/xml"
                                   "application/jrd+json" "application/json"}}
             :parameters {:query {:resource ::spec/resource}}
             :responses {200 {:body any?}}
             :handler resource}}]])
