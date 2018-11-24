-- :name follows? :? :1
select * from follows where follower = :subject::int and followed = :object::int limit 1

-- :name accept-follow! :<! :1
update follows set accept_uri = :accept-uri
  where uri = :uri
  returning accept_uri

-- :name follow! :<! :1
insert into follows (uri, followed, follower, accept_uri)
  values (:uri, :followed, :follower, :accept-uri)
  on conflict do nothing
  returning *

-- :name unfollow! :<! :1
delete from follows
  where follower = :follower::int and followed = :followed::int
  returning *
