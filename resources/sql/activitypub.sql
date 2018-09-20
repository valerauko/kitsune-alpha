-- :name create-object! :<! :1
insert into objects
  (uri, type, user_id, ap_to, cc, content)
values
  (concat(:uri, currval(pg_get_serial_sequence('objects', 'id'))),
   :type, :user-id,
   array[:v*:to]::varchar[],
   array[:v*:cc]::varchar[],
   :content)
returning *

-- :name create-activity! :<! :1
insert into activities
  (uri, object_id, type, user_id, ap_to, cc)
values
  (concat(:uri, currval(pg_get_serial_sequence('activities', 'id'))),
   :object-id, :type, :user-id,
   array[:v*:to]::varchar[],
   array[:v*:cc]::varchar[])
returning *

-- :name activity-with-object :? :1
select
  activities.*, objects.uri as object_uri, objects.user_id as object_user_id,
  objects.in_reply_to_id, objects.summary, objects.content,
  objects.created_at as object_created_at
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
