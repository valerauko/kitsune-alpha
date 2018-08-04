(ns kitsune.status-test
  (:require [clojure.test :refer :all]
            [kitsune.db.statuses :refer :all]))

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
        "`unlisted` takes precedence over `private`")
      (is (let [status {:to ["hoge" "@fuga"]
                        :bcc [public-id]
                        :audience [public-id]}]
            (= (visibility status) :direct))
        "AP's `bcc` and `audience` fields are not referenced for this purpose"))))
