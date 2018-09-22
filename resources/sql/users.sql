-- :name create! :<! :1
-- :doc Creates a new user record and returns its ID
insert into users
  (name, email, uri, acct, pass_hash)
  values (:name, :email, :uri, :acct, :pass-hash)
  returning name

-- :name update! :<! :1
update users
  set display_name = :display-name
  where id = :id
  returning *

-- :name update-keys! :<! :1
update users
  set public_key = :public-key, private_key = :private-key
  where id = :id
  returning id, public_key, private_key

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
  from users left join follows on users.id = follows.followed
  where users.name = :name and users.local = true
  group by users.id
  limit 1

-- :name followers-of :? :*
select uri from users
  right join follows on users.id = follows.follower
  where follows.followed = :id
  order by follows.created_at desc
  offset :offset
  limit :limit

-- :name find-for-auth :? :1
select id from users where name = :name and pass_hash = :pass-hash

-- :name find-by-name :? :1
select * from users where name = :name and local = true limit 1

-- :name find-by-id :? :1
select * from users where id = :id limit 1

-- :name find-by-uri :? :1
select * from users where uri = :uri limit 1

-- :name find-by-acct :? :1
select * from users where acct = :acct limit 1

-- :name load-by-id :? :*
select * from users where id in (:v*:ids)
