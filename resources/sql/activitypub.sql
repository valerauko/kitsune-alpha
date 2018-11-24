-- :name create-object! :<! :1
insert into objects
  (uri, type, account_id, ap_to, cc, content, in_reply_to_id, in_reply_to_user_id)
values
  (concat(:uri, currval(pg_get_serial_sequence('objects', 'id'))),
   :type, :account-id,
   array[:v*:to]::varchar[],
   array[:v*:cc]::varchar[],
   :content,
   :in-reply-to-id,
   :in-reply-to-user-id)
returning *

-- :name create-activity! :<! :1
insert into activities
  (uri, object_id, type, account_id, ap_to, cc)
values
  (concat(:uri, currval(pg_get_serial_sequence('activities', 'id'))),
   :object-id, :type, :account-id,
   array[:v*:to]::varchar[],
   array[:v*:cc]::varchar[])
returning *

-- :name status-exists? :? :1
select id, account_id from objects where id = :id

-- :name activity-exists? :? :1
select id, account_id from activities where uri = :uri

-- :name activity-with-object :? :1
select
  activities.*, objects.uri as object_uri, objects.user_id as object_user_id,
  objects.in_reply_to_id, objects.in_reply_to_user_id, objects.summary,
  objects.content, objects.created_at as object_created_at
from activities join objects on activities.object_id = objects.id
  where activities.id = :id::int
  limit 1

-- :name user-activities :? :*
select
  activities.*, objects.uri as object_uri, objects.user_id as object_user_id,
  objects.in_reply_to_id, objects.summary, objects.content,
  objects.created_at as object_created_at
from activities join objects on activities.object_id = objects.id
  where activities.user_id = :user-id::int

-- :name delete-object! :! :n
delete from objects
  where id = :id and user_id = :user-id
