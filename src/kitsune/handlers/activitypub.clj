(ns kitsune.handlers.activitypub
  (:require [ring.util.http-response :refer [ok not-found]]
            [kitsune.handlers.core :refer [defhandler]]))

(defhandler inbox
  [{http-sig :http-sig
    {body-params :body} :parameters
    :as req}]
  (println "signed?" http-sig)
  (clojure.pprint/pprint body-params)
  (ok))
