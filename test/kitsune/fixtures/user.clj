(ns kitsune.fixtures.user)

(defn dummy-user
  [& {:as override}]
  (merge
    {:id 1
     :name "foo"
     :acct "foo"
     :uri "http://localhost:3000/foo"
     :email "foo@example.com"
     :local true
     :pass-hash "foo"
     :display-name "foo"
     :created-at (java.util.Date.)
     :last-login (java.util.Date.)}
    override))
