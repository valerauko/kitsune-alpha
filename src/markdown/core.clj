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
    (fn [{:keys [length text links mentions hashtags]} [type match meta]]
      {:text (str text match)
       :length (case type
                 (:raw :text) (+ length (count match))
                 length)
       :links (case type
                :link (conj links meta)
                links)
       :mentions (case type
                   :mention (conj mentions meta)
                   mentions)
       :hashtags (case type
                   :hashtag (conj hashtags meta)
                   hashtags)})
    {:text "" :length 0 :links #{} :mentions #{} :hashtags #{}}
    (to-tagged-vec input)))
