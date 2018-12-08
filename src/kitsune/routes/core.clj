(ns kitsune.routes.core
  (:require [clojure.tools.logging :as log]
            [jsonista.core :as json]
            [reitit.ring :as ring]
            [reitit.ring.spec :as ring-spec]
            [reitit.ring.coercion :as coerce]
            [reitit.coercion.spec :as spec]
            [reitit.swagger :refer [swagger-feature create-swagger-handler]]
            [reitit.swagger-ui :refer [create-swagger-ui-handler]]
            [muuntaja.middleware :refer [wrap-format]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [muuntaja.core :as muuntaja]
            [muuntaja.format.plain-text :as text-format]
            [reitit.ring.middleware.muuntaja :as m-middleware]
            [kitsune.instance :refer [version]]
            [kitsune.routes.user :as user]
            [kitsune.routes.oauth :as oauth]
            [kitsune.routes.webfinger :as webfinger]
            [kitsune.routes.statuses :as statuses]
            [kitsune.routes.instance :as instance]
            [kitsune.routes.relationships :as relationships]))

(def negotiator
  (muuntaja/create
    (-> muuntaja/default-options
        (assoc-in [:formats "application/json" :matches]
                  #"^application/(.+\+)?json$")
    ;    (assoc-in [:formats "application/xml"] (text-format/format))
    ;    (assoc-in [:formats "text/html"] (text-format/format "text/html"))
    )))

(defn wrap-logging
  [handler]
  (fn [{:keys [request-method uri remote-addr]
        {fwd-for :X-Forwarded-For} :headers
        {route :template} :reitit.core/match
        :as request}]
    (let [start (System/nanoTime)
          response (handler request)]
      (log/info (json/write-value-as-string
                  {:status (:status response)
                   :method request-method
                   :path uri
                   :route route
                   :remote-addr (or fwd-for remote-addr)
                   :response-time (/ (- (System/nanoTime) start) 1000000.0)}))
      response)))

(defn default-middleware
  [handler]
  (wrap-defaults handler api-defaults))

(def router
  (ring/router
    [user/routes
     webfinger/routes
     oauth/routes
     instance/routes
     statuses/routes
     relationships/routes
     ["/swagger.json"
      {:get {:no-doc true
             :swagger {:info {:title "Kitsune API"
                              :description "Very fox microblogging"
                              :version version}
                       :basePath "/"}
             :handler (create-swagger-handler)}}]]
    {:conflicts identity ; mastodon routes conflict all over the place so don't even log
     :validate ring-spec/validate-spec!
     :data {:muuntaja negotiator
            :coercion spec/coercion
            :swagger {:id ::api}
            :middleware [wrap-logging
                         swagger-feature
                         m-middleware/format-middleware
                         coerce/coerce-exceptions-middleware
                         coerce/coerce-request-middleware
                         coerce/coerce-response-middleware]}}))

(def handler
  (-> (ring/ring-handler
        router
        (ring/routes
          (create-swagger-ui-handler {:path "/swagger" :jsonEditor true})
          (ring/redirect-trailing-slash-handler)
          (ring/create-default-handler
            {:not-found (constantly {:status 404 :body {:error "Not found"}})})))
      wrap-format
      default-middleware))
