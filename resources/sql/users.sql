-- :name create! :<! :1
-- :doc Creates a new user record and returns its ID
insert into users
  (name, email, pass_hash)
  values (:name, :email, :pass-hash)
  returning name

-- :name lookup :? :1
select name, display_name, created_at from users where name = :name

-- :name update! :<! :1
update users
  set display_name = :display-name
  where name = :name
  returning name, display_name, created_at

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

-- :name find-by-id :? :1
select * from users where id = :id
