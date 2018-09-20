(ns markdown.transformers
  (:require [clojure.string :as string]))

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

(defn named-link
  [text]
  (let [matches (re-seq #"(.*?)(?:\[([^\]]+)\]\(((?:https?|ftp)://[^\pZ)\"]+)\))?(.*?(?=\[|$))" text)]
    (wrap (fn [[title url]]
            [[:link (str "<a href=\"" url "\" "
                         "class=\"status-link\" rel=\"noopener\" target=\"_blank\">")
                    url]
             ; doing this to prevent nested links
             [:text (string/replace title #"://" "&colon;//")]
             [:meta "</a>"]])
          matches)))

(defn plain-link
  [text]
  (let [matches (re-seq #"(.*?\pZ?)((?<=^|\pZ)(?:(?:https?|ftp)://)([^\pZ\"]+)(?=[\pZ\"]|$))?(.*?(?=\pZ(?:https?|ftp)|$))" text)]
    (wrap (fn [[full no-scheme]]
            [[:link (str "<a href=\"" full "\" "
                         "class=\"status-link\" rel=\"noopener\" target=\"_blank\">")
                    full]
             [:raw (if (> (count no-scheme) 20)
                     (str (subs no-scheme 0 18) "…")
                     no-scheme)]
             [:meta "</a>"]])
          matches)))

(defn mention
  [text]
  (let [matches (re-seq #"(.*?\pZ?)(?:(?<=^|\pZ)@(?:([a-z0-9][a-z0-9_.-]+)(?:@((?:[a-z0-9-_]+\.)*[a-z0-9]+))?))?(.*?(?=\pZ@|$))" text)]
    (wrap (fn [[name host]]
            (let [acct (str "@" name (if host (str "@" host)))]
              ; FIXME: this n+1s
              (if-let [user (user-lookup acct)]
                [[:mention (str "<a href=\"" (:uri user) "\" "
                                "rel=\"noopener\" target=\"_blank\" "
                                "class=\"status-link mention\">")
                           (:uri user)]
                 ; FIXME: the @ is underlined
                 [:raw (str "<span>" acct "</span")]
                 [:meta "</a>"]]
                [[:raw acct]])))
          matches)))

(defn hashtag
  [text]
  (let [matches (re-seq #"(.*?\pZ?)(?:(?<=^|\pZ)#([\pL\pN_]+))?(.*?(?=\pZ#|$))" text)]
    (wrap (fn [[tag]]
            [[:hashtag (str "<a href=\"" tag "\" "
                            "class=\"status-link\" rel=\"noopener\" target=\"_blank\" "
                            ">") tag]
             [:raw (str "#" tag)]
             [:meta (str "</a>")]])
          matches)))

(defn code-block
  [text]
  (let [matches (re-seq #"(?ms)(.*?)(?:(?:^```\w*$)(.+?)(?:^```$))?()$" text)]
    (wrap (fn [[multiline-code]]
            [[:meta "<code><pre>"]
             [:raw (string/trim multiline-code)]
             [:meta "</pre></code"]])
          matches)))

(defn paragraph
  [text]
  (let [matches (re-seq #"(?m)^()(.+)()$" text)]
    (wrap (fn [[paragraph-text]]
            [[:meta "<p>"]
             [:text (string/trim paragraph-text)]
             [:meta "</p>"]])
          matches)))

(def defaults
  "Block-level transformers should come before inline ones.
   Also ones that produce :raw before :text producers."
  [code-block
   paragraph
   code
   named-link
   plain-link
   mention
   hashtag
   bold
   italic])
