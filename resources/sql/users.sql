-- :name create! :<! :1
-- :doc Creates a new user record and returns its ID
insert into users
  (name, email, uri, pass_hash)
  values (:name, :email, :uri, :pass-hash)
  returning name

-- :name update! :<! :1
update users
  set display_name = :display-name
  where id = :id
  returning *

-- :name touch-last-login! :<! :n
update users
  set last_login = now()
  where id = :id

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
