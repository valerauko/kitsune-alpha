-- :name create-object! :<! :1
insert into objects
  (uri, type, user_id, ap_to, cc, content)
values
  (:uri, :type, :user-id, array[:v*:to]::varchar[], array[:v*:cc]::varchar[], :content)
returning *

-- :name create-activity! :<! :1
insert into activities
  (uri, object_id, type, user_id, ap_to, cc)
values
  (:uri, :object-id, :type, :user-id, array[:v*:to]::varchar[], array[:v*:cc]::varchar[])
returning *

-- :name delete-object! :! :n
delete from objects
  where id = :id and user_id = :user-id
