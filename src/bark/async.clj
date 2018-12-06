(ns bark.async
  (:require [manifold.deferred :as md]
            [manifold.time :as mt]
            [clojure.math.numeric-tower :as math]))

(defn retry
  [f {:keys [max-tries initial-offset stop-on? inspect-error]
      :or {max-tries 5
           initial-offset 5
           stop-on? (constantly nil)
           inspect-error (constantly nil)}}]
  (md/loop [failures 0]
    (md/catch
      (f)
      (fn [error]
        (let [failed-tries (inc failures)
              retrying? (< failed-tries max-tries)]
          (if (and retrying? (not (stop-on? error)))
            (let [retry-in (->> failed-tries
                                (+ (rand 0.3)) ; add jitter
                                (math/expt 2) ; exponential backoff
                                (* initial-offset)
                                (mt/seconds))] ; convert to ms
              (inspect-error error failed-tries retrying? retry-in)
              (mt/in retry-in #(md/recur failed-tries)))
            (throw error)))))))
