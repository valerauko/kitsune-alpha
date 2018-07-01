(ns kitsune.handlers.oauth
  (:require [ring.util.http-response :refer :all]
            [org.bovinegenius [exploding-fish :as uri]]
            [clojure.string :refer [split join]]
            [kitsune.handlers.core :refer [defhandler url-decode]]
            [kitsune.db.oauth :as db]
            [kitsune.db.user :as user-db]
            [kitsune.db.core :refer [conn]]
            [kitsune.spec.oauth :as spec])
  (:import java.util.Base64))

; TODO: return spec errors https://tools.ietf.org/html/rfc6749#section-5.2

(defhandler register-app
  [{{:keys [scopes redirect-uris] :as params} :body-params :as req}]
  (println params)
  (if-let [scope-array (spec/valid-scope scopes)]
    (if-let [result (db/create-app! conn
                      (assoc (select-keys params [:name :website])
                             :redirect-uris (db/array-string redirect-uris)
                             :scopes scope-array))]
      (ok result)
      ; REVIEW: this really shouldn't happen unless the fields are wrong type
      ; or don't fit. maybe make it a 422 instead?
      (internal-server-error {:error "Something went very wrong. Sorry."}))
    (unprocessable-entity {:error "Requested scope(s) not acceptable."})))

(defhandler auth-form
  [{params :query-params :as req}]
  (ok req))

(defhandler auth-result
  [auth]
  ; TODO: return an actual html page
  ({:auth-code (:auth-code auth)}))

(defn uri-for-redirect
  [input {code :auth-code} state]
  (-> input
    uri/uri
    (uri/param "code" code) ; add the code
    (#(if state (uri/param % "state" state) %)) ; echo state if present
    (#(if (or (= (uri/scheme %) "http") ; if it's http or empty make it https
              (= nil (uri/scheme %)))
      (uri/scheme % "https") %)) ; TODO: don't change in development
    str)) ; has to be string for redirect

(defhandler authorize
  [{{:keys [name password client-id scopes redirect-uri state] :as params} :body-params}]
  (let [user (user-db/for-login name password)
        app (db/find-for-auth conn {:client-id client-id
                                    :redirect-uri redirect-uri})
        scope-array (spec/valid-scope scopes)]
    ; TODO: make sure that the requested scopes are a subset of the app's
    (if-let [auth (and user app scope-array
                       (db/create-auth! conn {:user-id (:id user)
                                              :app-id  (:id app)
                                              :scopes  scope-array}))]
      (if (= redirect-uri "urn:ietf:wg:oauth:2.0:oob")
        (ok (auth-result auth))
        (let [target-uri (uri-for-redirect redirect-uri auth state)]
          (found target-uri)))
      (auth-form {:query-params params
                  :user user :app app :scopes scope-array})))) ; render the auth page again if failed

(defn base64-padfix
  [^String str]
  (->> str
    (.decode (Base64/getDecoder))
    (.encodeToString (Base64/getEncoder))))

(defn app-from-request
  "According to the OAuth spec, passing client credentials in Basic HTTP header
  is preferable to passing them in the request body."
  [req]
  (db/find-for-session conn
    (let [[id secret] (some-> req :headers :authorization
                                  (#(rest
                                      (re-matches #"^Basic ([^:]+):(.+)" %)))
                                  (#(map url-decode %)))]
      (if (and id secret)
        {:client-id id :client-secret secret}
        ; not sure if merge + select-keys would be better?
        {:client-id (-> req :body-params :client-id)
         :client-secret (-> req :body-params :client-secret)}))))

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
        (select-keys authz [:user-id :auth-id :scopes])))))

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
      ; if ok
      (ok {:token-type "Bearer"
           :access-token (:token token)
           :refresh-token (:refresh token)
           :expires-in 600
           :scope (join " " (:scopes token))})
      (forbidden {:error "Invalid credentials"}))
    (forbidden {:error "Invalid client credentials"})))
