-- :name follows? :? :1
select * from follows where follower = :subject::int and followed = :object::int limit 1

-- :name follow! :<! :1
insert into follows (follower, followed)
  values (:subject::int, :object::int)
  returning *

-- :name unfollow! :<! :1
delete from follows
  where follower = :subject::int and followed = :object::int
  returning true as result
