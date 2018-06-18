(ns kitsune.routes.user
  (:require [kitsune.handlers.user :as user]))

(def routes
  ["/people"
   {:summary "ActivityPub based user accounts API"
    :swagger {:tags ["Users"]}}
   [""
    {:post {:summary "Create new user"
            :parameters {:body {:user :kitsune.spec.user/registration}}
            :responses {200 {:body {:name :kitsune.spec.user/name}}}
            :handler user/create}}]
   ["/:name"
    {:parameters {:path {:name :kitsune.spec.user/name}}
     :get {:summary "User profile"
           :responses {200 {:body :kitsune.spec.user/show}
                       404 {:body {:error :kitsune.spec.user/name}}}
           :handler user/show}
     :put {:summary "Update user profile"
           :parameters {:body {:user :kitsune.spec.user/profile-update}}
           :responses {200 {:body :kitsune.spec.user/show}
                       404 {:body {:error :kitsune.spec.user/name}}}
           :handler user/update-profile}
     :delete {:summary "Delete account"
              :responses {404 {:body {:error :kitsune.spec.user/name}}}
              :handler user/destroy}}]])
