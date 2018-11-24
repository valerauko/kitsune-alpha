-- :name create-user! :<! :1
-- :doc Creates a new user record and returns its ID
insert into users
  (email, name, pass_hash, private_key)
  values (:email, :name, :pass-hash, :private-key)
  returning id

-- :name create-account! :<! :1
-- :doc Creates a new account record. Used both for local and remote
insert into accounts
  (user_id, acct, uri, local, public_key, display_name)
  values (
    --~ (if (:user-id params) ":user-id" "NULL")
    , :acct, :uri,
    --~ (if (:local params) "true" "false")
    , :public-key,
    --~ (if (:display-name params) ":display-name" "NULL")
  )
  returning id

-- :name update! :<! :1
update accounts
  set display_name = :display-name
  where user_id = :id
  returning *

-- :name update-account! :<! :1
update accounts set
/*~
(->> (dissoc params :uri)
     (camel-snake-kebab.extras/transform-keys
       camel-snake-kebab.core/->snake_case_string)
     (map #(clojure.string/join "=" %))
     (clojure.string/join ", ")))
~*/
where uri = :uri
returning *

-- :name update-private-key! :<! :1
update users
  set private_key = :private-key
  where id = :id

-- :name update-public-key! :<! :1
update accounts
  set public_key = :public-key
  where user_id = :user-id

-- :name touch-last-login! :<! :1
update users
  set last_login = now()
  where id = :id
  returning true as result

-- :name destroy! :<! :1
delete from users
  where name = :name
  returning true as result

-- :name count-followers :? :1
select users.id as id, uri, count(follows.id) as followers
  from users join accounts on accounts.user_id = users.id
  left join follows on users.id = follows.followed
  where users.name = :name
  group by users.id, uri
  limit 1

-- :name followers-of :? :*
select uri from users
  right join follows on users.id = follows.follower
  where follows.followed = :id
  order by follows.created_at desc
  offset :offset
  limit :limit

-- :name count-following :? :1
select users.id as id, uri, count(follows.id) as following
  from users join accounts on accounts.user_id = users.id
  left join follows on users.id = follows.follower
  where users.name = :name
  group by users.id, uri
  limit 1

-- :name followed-by :? :*
select uri from users
  right join follows on users.id = follows.followed
  where follows.follower = :id
  order by follows.created_at desc
  offset :offset
  limit :limit

-- :name find-for-auth :? :1
select id from users where email = :email and pass_hash = :pass-hash

-- :name find-by-name :? :1
select * from accounts
join users on accounts.user_id = users.id
where name = :name and local = true limit 1

-- :name find-by-id :? :1
select * from users where id = :id limit 1

-- :name find-by-uri :? :1
select * from accounts
left join users on accounts.user_id = users.id
where uri = :uri limit 1

-- :name find-by-acct :? :1
select * from accounts
join users on accounts.user_id = users.id
where acct = :acct limit 1

-- :name load-by-id :? :*
select * from users where id in (:v*:ids)
