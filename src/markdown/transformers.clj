(ns markdown.transformers
  (:require [clojure.string :refer [trim]]))

(defn wrap
  [formatter matches]
  (reduce
    (fn [aggr [_ pre & stuck]]
      (let [hit (butlast stuck)
            post (last stuck)]
        (concat
          aggr
          (if (empty? pre) [] [[:text pre]])
          (if (first hit) (formatter hit) [])
          (if (empty? post) [] [[:text post]]))))
    []
    matches))

(defn inline
  ([text delimiter element]
    (inline text delimiter element :text))
  ([text delimiter element tag]
    (let [rex (re-pattern
                (str "(.*?\\pZ?)(?:(?<=^|\\pZ)"
                     delimiter
                     "(.+?)"
                     delimiter
                     "(?=\\pZ|$))?(.*?(?=\\pZ"
                     delimiter
                     "|$))"))
          matches (re-seq rex text)]
      (wrap (fn [[hit]]
              [[:meta (str "<" element ">")]
               [tag hit]
               [:meta (str "</" element ">")]])
            matches))))

(defn italic
  [text]
  (inline text #"\*" "em"))

(defn bold
  [text]
  (inline text #"\*\*" "strong"))

(defn code
  [text]
  (inline text #"`" "code" :raw))

(defn mention
  [text]
  (let [matches (re-seq #"(.*?\pZ?)(?:(?<=^|\pZ)@(?:([a-z0-9][a-z0-9_.-]+)(?:@((?:[a-z0-9-_]+\.)*[a-z0-9]+))?))?(.*?(?=\pZ@|$))" text)]
    (wrap (fn [[name host]]
            [[:mention (str "<a href=\"" host "/" name "\">") name host]
             [:raw (str "@" name (if host (str "@" host)))]
             [:meta "</a>"]])
          matches)))

(defn hashtag
  [text]
  (let [matches (re-seq #"(.*?\pZ?)(?:(?<=^|\pZ)#([\pL\pN_]+))?(.*?(?=\pZ#|$))" text)]
    (wrap (fn [[tag]]
            [[:hashtag (str "<a href=\"" tag "\">") tag]
             [:raw (str "#" tag)]
             [:meta (str "</a>")]])
          matches)))

(defn code-block
  [text]
  (let [matches (re-seq #"(?ms)(.*?)(?:(?:^```\w*$)(.+?)(?:^```$))?()$" text)]
    (wrap (fn [[multiline-code]]
            [[:meta "<code><pre>"]
             [:raw (trim multiline-code)]
             [:meta "</pre></code"]])
          matches)))

(defn paragraph
  [text]
  (let [matches (re-seq #"(?m)^()(.+)()$" text)]
    (wrap (fn [[paragraph-text]]
            [[:meta "<p>"]
             [:text (trim paragraph-text)]
             [:meta "</p>"]])
          matches)))

(def defaults
  "Block-level transformers should come before inline ones.
   Also ones that produce :raw before :text producers."
  [code-block
   paragraph
   code
   mention
   hashtag
   bold
   italic])
