(ns kitsune.handlers.oauth
  (:require [ring.util.http-response :refer :all]
            [org.bovinegenius [exploding-fish :as uri]]
            [clojure.string :refer [split join]]
            [cljstache.core :refer [render-resource]]
            [kitsune.handlers.core :refer [defhandler url-decode]]
            [kitsune.db.oauth :as db]
            [kitsune.db.user :as user-db]
            [kitsune.db.core :refer [conn]]
            [kitsune.spec.oauth :as spec])
  (:import [java.util Base64]))

; TODO: return spec errors https://tools.ietf.org/html/rfc6749#section-5.2

(defhandler register-app
  [{{:keys [scopes redirect-uris website client-name]} :body-params :as req}]
  (if-let [scope-array (spec/valid-scope scopes)]
    (if-let [result (db/create-app! conn
                      {:website website
                       :name client-name
                       :redirect-uris (db/array-string redirect-uris)
                       :scopes scope-array})]
      (ok result)
      ; REVIEW: this really shouldn't happen unless the fields are wrong type
      ; or don't fit. maybe make it a 422 instead?
      (internal-server-error {:error "Something went very wrong. Sorry."}))
    (unprocessable-entity {:error "Requested scope(s) not acceptable."})))

(def scope-meanings
  {"read" "to read every detail of your account"
   "write" "to edit your profile and post on your behalf"
   "follow" "(un)follow or (un)block users"
   "push" "receive push notifications for you"})

(defhandler auth-form
  [{{:keys [scope] :as query-params} :query-params :as req}]
  {:status 200
   :body (render-resource "templates/app_auth_form.html"
                          (merge (clojure.walk/keywordize-keys query-params)
                                 {:scopes (some->> scope
                                                   spec/valid-scope
                                                   (map scope-meanings))}))
   :headers {"Content-type" "text/html"}})

(defhandler auth-result
  [auth]
  ; TODO: return an actual html page
  {:auth-code (:auth-code auth)})

(defn uri-for-redirect
  [input {code :auth-code} state]
  (-> input
    uri/uri
    (uri/param "code" code) ; add the code
    (#(if state (uri/param % "state" state) %)) ; echo state if present
    (#(if (or (nil? (uri/scheme %)) ; if it's http or empty make it https
              (= (uri/scheme %) "http"))
      (uri/scheme % "https") %)) ; TODO: don't change in development
    str)) ; has to be string for redirect

(defhandler authorize
  [{{:keys [client-id redirect-uri email password scope state]
     :or {redirect-uri "urn:ietf:wg:oauth:2.0:oob"} :as params} :form-params}]
  (let [user (user-db/for-login email password)
        app (db/find-for-auth conn {:client-id client-id
                                    :redirect-uri redirect-uri})
        scope-array (spec/valid-scope scope)]
    ; TODO: make sure that the requested scopes are a subset of the app's
    (if-let [auth (and user app scope-array
                       (db/create-auth! conn {:user-id (:id user)
                                              :app-id  (:id app)
                                              :scopes  scope-array}))]
      (if (= redirect-uri "urn:ietf:wg:oauth:2.0:oob")
        (ok (auth-result auth))
        (let [target-uri (uri-for-redirect redirect-uri auth state)]
          (found target-uri)))
      (auth-form {:query-params params})))) ; render the auth page again if failed

(defn base64-padfix
  [^String str]
  (->> (url-decode str)
    (.decode (Base64/getDecoder))
    (.encodeToString (Base64/getEncoder))))

(defn app-from-request
  "According to the OAuth spec, passing client credentials in Basic HTTP header
  is preferable to passing them in the request body."
  [{{:keys [client-id client-secret redirect-uri]} :body-params :as req}]
  (let [[id secret] (some->> req :headers :authorization
                             (re-matches #"^Basic ([^:]+):(.+)")
                             rest
                             (map base64-padfix))]
    (if (and id secret)
      (db/find-for-session conn {:client-id id
                                 :client-secret secret
                                 :redirect-uri redirect-uri})
      ; not sure if merge + select-keys would be better?
      (db/find-for-auth conn {:client-id client-id
                              :redirect-uri redirect-uri}))))

(defn exc-pass
  "Password authentication is reserved for server-side web frontends and
  should not be used by other client apps. It gives the client full access,
  and the scopes can't be adjusted from user settings."
  [app {:keys [password username]}]
  (when-let [user (user-db/for-login username password)]
    ; the authorization is automatically created, so revoking it is meaningless
    ; REVIEW: the authz insert should maybe spun off into a manifold?
    (db/create-auth! conn {:user-id (:id user)
                           :app-id (:id app)
                           :scopes ["follow" "read" "write"]})
    (db/exchange-token! conn {:user-id (:id user)
                              :app-id (:id app)
                              :scopes ["follow" "read" "write"]})))

(defn exc-code
  [app {:keys [code]}]
  (let [auth-code (base64-padfix code)]
    (if-let [authz (db/use-authz! conn {:app-id (:id app)
                                        :auth-code auth-code})]
      (db/exchange-token! conn
        (select-keys authz [:user-id :app-id :scopes])))))

(defn exc-refresh
  [app refresh-token]
  (if-let [old-token (db/find-by-refresh-token conn
                       {:refresh-token refresh-token
                        :app-id (:id app)})]
    (db/refresh-token! conn {:id (:id old-token)})))

(defhandler exchange-token
  [{{:keys [grant-type username name refresh-token] :as params} :body-params
   :as req}]
  (if-let [app (app-from-request req)]
    (if-let [token (case grant-type
                     "authorization_code" (exc-code app params)
                     "refresh_token" (exc-refresh app refresh-token)
                     "password" (exc-pass app (assoc params :username
                                                     (or username name)))
                     nil)]
      (do
        ; update user's last seen timestamp async
        ; this includes token refreshes too so it's accurate to 10 minutes
        (future (user-db/touch-last-login! conn {:id (:user-id token)}))
        (ok {:token-type "Bearer"
             :access-token (:token token)
             :refresh-token (:refresh token)
             :expires-in 600
             :scope (join " " (:scopes token))}))
      (forbidden {:error "Invalid credentials"}))
    (forbidden {:error "Invalid client credentials"})))
