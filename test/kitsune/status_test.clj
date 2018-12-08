(ns kitsune.status-test
  (:require [clojure.test :refer :all]
            [mount.core :refer [start]]
            [kitsune.db.statuses :refer :all]
            [kitsune.handlers.statuses :refer :all]
            [kitsune.fixtures.user :as user-fixt]
            [kitsune.fixtures.status :as status-fixt]))

(start #'kitsune.instance/config)

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
                    (fn [_ {:keys [id]}] (if (= id 42) {:id 42 :account-id 13}))
                  kitsune.db.statuses/create-status!
                    (fn [& {:keys [in-reply-to-id in-reply-to-user-id]}]
                      (if (= in-reply-to-id 42)
                        {:object (status-fixt/dummy-object
                                   :in-reply-to-id in-reply-to-id
                                   :in-reply-to-user-id in-reply-to-user-id)
                         :activity (status-fixt/dummy-activity)}
                        {:object (status-fixt/dummy-object)
                         :activity (status-fixt/dummy-activity)}))
                  kitsune.db.user/find-by-user-id
                    (fn [& _] (user-fixt/dummy-user))]
      (let [request {:body-params {:status "foo"}}
            result (:body (create request))]
        (is (empty? (:in-reply-to-id result)))
        (is (empty? (:in-reply-to-account-id result))))
      (let [request {:body-params {:status "foo" :in-reply-to-id 13}}
                result (:body (create request))]
        (is (empty? (:in-reply-to-id result)))
        (is (empty? (:in-reply-to-account-id result))))
      (let [request {:body-params {:status "foo" :in-reply-to-id 42}}
            result (:body (create request))]
        (is (= (:in-reply-to-id result) 42))
        (is (= (:in-reply-to-account-id result) 13))))))
