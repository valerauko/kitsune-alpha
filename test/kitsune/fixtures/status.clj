(ns kitsune.fixtures.status)

(defn dummy-object
  [& {:as override}]
  (merge
    {:id 1
     :uri "http://localhost:3000/objects/1"
     :local true
     :type "Note"
     :user-id 1
     :ap-to ["https://www.w3.org/ns/activitystreams#Public"]
     :cc []
     :in-reply-to-id nil
     :in-reply-to-user-id nil
     :summary nil
     :content "foo"
     :tags []
     :mentions []
     :created-at (java.util.Date.)}
    override))

(defn dummy-activity
  [& {:as override}]
  (merge
    {:id 1
     :uri "http://localhost:3000/activities/1"
     :local true
     :object-id 1
     :type "Create"
     :user-id 1
     :ap-to ["https://www.w3.org/ns/activitystreams#Public"]
     :cc []
     :created-at (java.util.Date.)}
    override))
