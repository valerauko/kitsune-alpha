(ns markdown.core
  (:require [markdown.transformers :refer [defaults]]))

(defn one-transform
  [arr trf]
  (reduce
    (fn [aggr [tag text :as match]]
      (concat aggr
              (if (= tag :text)
                (trf text)
                [match])))
    []
    arr))

(defn to-tagged-vec
  ([text]
    (to-tagged-vec text defaults))
  ([text transformers]
    (reduce
      one-transform
      [[:text text]]
      transformers)))

(defn process
  [input]
  (reduce
    (fn [{:keys [length text mentions hashtags]} [type match name host]]
      {:length (case type
                 (:raw :text) (+ length (count match))
                 length)
       :text (str text match)
       :mentions (case type
                   :mention (conj mentions {:name name :host host})
                   mentions)
       :hashtags (case type
                   :hashtag (conj hashtags name)
                   hashtags)})
    {:length 0 :mentions [] :hashtags [] :text ""}
    (to-tagged-vec input)))
