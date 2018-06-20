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
             :handler host-meta}}]
    ["/webfinger"
      {:get {:summary "WebFinger endpoint for users"
             :swagger {:produces #{"application/xrd+xml" "application/xml"
                                   "application/jrd+json" "application/json"}}
             :responses {200 {:body ::spec/json}}
             :parameters {:query {:resource ::spec/resource}}
             :handler resource}}]])
