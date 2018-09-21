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

-- :name find-for-auth :? :1
select id from users where name = :name and pass_hash = :pass-hash

-- :name find-by-name :? :1
select * from users where name = :name limit 1

-- :name find-by-id :? :1
select * from users where id = :id limit 1

-- :name find-by-uri :? :1
select * from users where uri = :uri limit 1

-- :name find-by-acct :? :1
select * from users where acct = :acct limit 1

-- :name load-by-id :? :*
select * from users where id in (:v*:ids)
