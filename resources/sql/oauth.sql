-- :name create-app! :<! :1
insert into apps (name, redirect_uris, scopes, website)
  values (:name, array[:v*:redirect-uris], array[:v*:scopes], :website)
  returning id, client_id, secret

-- :name find-for-auth :? :1
select id from apps
  where client_id = :client-id and :redirect-uri = any(redirect_uris)

-- :name find-for-session :? :1
select id from apps
  where client_id = :client-id and secret = :client-secret

-- :name create-auth! :<! :1
insert into oauth_auths (user_id, app_id, scopes)
  values (:user-id, :app-id, array[:v*:scopes])
  on conflict do nothing
  returning auth_code

-- :name use-authz! :<! :1
update oauth_auths
  set used = true
  where
    (not used) and
    client_id = :client-id and
    auth_code = :auth-code and
    expires_at > now()
  returning user_id, app_id, scopes

-- :name exchange-token! :<! :1
insert into oauth_tokens (user_id, app_id, scopes)
  values (:user-id, :app-id, array[:v*:scopes])
  returning token, refresh, scopes

-- :name find-by-refresh-token :? :1
select id from oauth_tokens
  where refresh_token = :refresh-token and app_id = :app-id

-- :name refresh-token! :<! :1
update oauth_tokens
  set
    token = default,
    refresh = default,
    expires_at = default,
    updated_at = default
  where id = :id
  returning token, refresh, scopes
