(ns kitsune.relationship-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [kitsune.spec.mastodon.relationship :as spec]
            [kitsune.handlers.relationships :refer :all]))

(mount.core/start #'kitsune.instance/config)

(deftest follow-test
  ; TODO: test error handling properly when done
  (let [dummy-subject 1
        dummy-object 2
        dummy-fn (fn [_ {:keys [follower followed]}]
                   (if (and (= follower dummy-subject)
                            (= followed dummy-object))
                     true
                     (throw (Throwable. ""))))
        dummy-query (fn [_ {id :id}]
                      {:local true
                       :account-id id})]
    (with-redefs [kitsune.db.user/find-by-user-id dummy-query
                  kitsune.db.user/find-by-id dummy-query
                  kitsune.db.relationship/follow! dummy-fn
                  kitsune.db.relationship/unfollow! dummy-fn
                  kitsune.db.relationship/follows? (constantly 1)
                  kitsune.db.relationship/requested-follow? (constantly 1)]
      (testing "Follow"
        (is (let [req {:path-params {:id 2}}]
              (-> (follow req) :body :error))
          "Fails unless authenticated")
        (is (let [req {:path-params {:id 3}
                       :auth {:user-id 1}}]
              (-> (follow req) :body :error))
          "Fails if the user-to-follow doesn't exist")
        (is (let [req {:path-params {:id 1}
                       :auth {:user-id 2}}]
              (-> (follow req) :body :error))
          "Fails if current user already follows user-to-follow")
        (is (let [req {:path-params {:id 2}
                       :auth {:user-id 1}}]
              (s/valid? ::spec/relationship (:body (follow req))))
          "Returns a Relationship if successful"))
      (testing "Unfollow"
        (is (let [req {:path-params {:id 2}}]
              (-> (unfollow req) :body :error))
          "Fails unless authenticated")
        (is (let [req {:path-params {:id 1}
                       :auth {:user-id 2}}]
              (-> (unfollow req) :body :error))
          "Fails if current user doesn't follow user-to-follow")
        (is (let [req {:path-params {:id 2}
                       :auth {:user-id 1}}]
              (s/valid? ::spec/relationship (:body (follow req))))
          "Returns a Relationship if successful")))))
