(ns kitsune.status-test
  (:require [clojure.test :refer :all]
            [kitsune.db.statuses :refer :all]
            [kitsune.handlers.statuses :refer :all]
            [kitsune.fixtures.user :as user-fixt]
            [kitsune.fixtures.status :as status-fixt]))

(deftest visibility-test
  (testing "Visibility"
    (testing "Basics"
      (is (let [status {:to ["hoge" public-id "@fuga"]}]
            (= (visibility status) :public))
        "`public` if the AP Public is in its `to`")
      (is (let [status {:to ["hoge" "@fuga"] :cc [public-id]}]
            (= (visibility status) :unlisted))
        "`unlisted` if the AP Public is in its `cc`")
      (is (let [status {:to ["hoge" "#followers" "@fuga"]
                        :actor {:followers "#followers"}}]
            (= (visibility status) :private))
        "`private` if the Actor's Followers URI is in `to`")
      (is (let [status {:to ["hoge" "@fuga"]}]
            (= (visibility status) :direct))
        "`direct` if it's none of the others"))
    (testing "Edge cases"
      (is (let [status {:to [public-id] :cc [public-id]}]
            (= (visibility status) :public))
        "`public` takes precedence over `unlisted`")
      (is (let [status {:to [public-id "#followers"]
                        :actor {:followers "#followers"}}]
            (= (visibility status) :public))
        "`public` takes precedence over `private`")
      (is (let [status {:to ["#followers"]
                        :cc [public-id]}]
            (= (visibility status) :unlisted))
        "`unlisted` takes precedence over `private`"))))

(deftest replies-test
  (testing "Replies"
    (with-redefs [kitsune.db.statuses/status-exists?
                    (fn [_ {:keys [id]}] (if (= id 42) {:id 42 :user-id 13}))
                  kitsune.db.statuses/create-status!
                    (fn [& {:keys [in-reply-to-id in-reply-to-user-id]}]
                      (if (= in-reply-to-id 42)
                        {:object (status-fixt/dummy-object
                                   :in-reply-to-id in-reply-to-id
                                   :in-reply-to-user-id in-reply-to-user-id)
                         :activity (status-fixt/dummy-activity)}
                        {:object (status-fixt/dummy-object)
                         :activity (status-fixt/dummy-activity)}))
                  kitsune.db.user/find-by-id
                    (fn [& _] (user-fixt/dummy-user))]
      (is (let [request {:body-params {:status "foo"}}
                result (:body (create request))]
            (and (empty? (:in-reply-to-id result))
                 (empty? (:in-reply-to-account-id result))))
        "Empty if no in-reply-to-id param is given")
      (is (let [request {:body-params {:status "foo" :in-reply-to-id 13}}
                result (:body (create request))]
            (and (empty? (:in-reply-to-id result))
                 (empty? (:in-reply-to-account-id result))))
        "Empty if the status of the given ID doesn't exist")
      (is (let [request {:body-params {:status "foo" :in-reply-to-id 42}}
                result (:body (create request))]
            ;(println request, result)
            (and (= (:in-reply-to-id result) 42)
                 (= (:in-reply-to-account-id result) 13)))
        "Has the given ID and its owner's ID"))))
