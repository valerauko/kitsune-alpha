(ns muuntaja.format.plain-text
  (:refer-clojure :exclude [format])
  (:require [muuntaja.format.core :as core]
            [clojure.string :refer [replace-first]]
            [byte-streams :as bs])
  (:import (java.io InputStreamReader PushbackReader InputStream OutputStream)))

(defn decoder [options]
  (let [options (merge {:readers *data-readers*} options)]
    (reify
      core/Decode
      (decode [_ data charset]
        (bs/to-string data (assoc options :encoding charset))))))

(defn encoder [_]
  (reify
    core/EncodeToBytes
    (encode-to-bytes [_ data charset]
      (.getBytes
        (pr-str data)
        ^String charset))
    core/EncodeToOutputStream
    (encode-to-output-stream [_ data charset]
      (fn [^OutputStream output-stream]
        (.write output-stream (.getBytes
                                (pr-str data)
                                ^String charset))))))

(defn format
  ([] (format "application/xml"))
  ([content-type]
    (core/map->Format
      {:name content-type
       :matches [(re-pattern
                   (clojure.string/replace-first content-type "/" "/(.+\\+)"))]
       :decoder [decoder]
       :encoder [encoder]})))
