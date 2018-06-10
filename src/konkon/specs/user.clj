(ns konkon.specs.user)

(def username-rex #"?i[a-z0-9-_]{2,15}")

(def mail-rex #"?i.+@.+")

(def user
  {:name #(re-matches % username-rex)
   :email #(re-matches % mail-rex)})
