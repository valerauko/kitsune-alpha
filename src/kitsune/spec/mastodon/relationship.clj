(ns kitsune.spec.mastodon.relationship
  (:require [clojure.spec.alpha :as s]
            [kitsune.spec.common :as common]))

(s/def ::following boolean?)
(s/def ::followed-by boolean?)
(s/def ::blocking boolean?)
(s/def ::muting boolean?)
(s/def ::muting-notifications boolean?)
(s/def ::requested boolean?)
(s/def ::domain-blocking boolean?)
(s/def ::showing-reblogs boolean?)

(s/def ::relationship
  (s/keys :req-un [::common/id ::following ::followed-by
                   ::blocking ::muting ::muting-notifications
                   ::requested ::domain-blocking ::showing-reblogs]))
