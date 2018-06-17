-- :name create! :<! :n
-- :doc Creates a new user record and returns its ID
insert into users
  (name, email, pass_hash)
  values (:name, :email, :pass-hash)
  returning name

-- :name find! :? :1
select name, display_name, created_at from users where name = :name

-- :name update! :<! :1
update users
  set display_name = :display-name
  where name = :name
  returning name, display_name, created_at

-- :name destroy! :<! :1
delete from users
  where name = :name
  returning true as result
