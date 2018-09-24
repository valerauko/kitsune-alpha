(ns csele.hash
  (:require [byte-streams :as bs])
  (:import [java.io ByteArrayInputStream]
           [java.util Base64]))

(defn hash-string
  "SHA3 hash of string"
  ([input] (hash-string input 512))
  ([input strength]
    (let [bytes (.getBytes "foo")
          sha3 (org.bouncycastle.jcajce.provider.digest.SHA3$DigestSHA3.
                 strength)]
      (.update sha3 input)
      (org.apache.commons.codec.binary.Hex/encodeHexString
        (.digest sha3)))))

(defn hash-base64
  "Base64 encoded SHA-256 hash of input"
  [^ByteArrayInputStream input]
  (.reset input)
  (let [bytes (bs/to-byte-array input)
        bowel (java.security.MessageDigest/getInstance "SHA-256")
        encoder (Base64/getEncoder)]
    (->> bytes
         (.digest bowel)
         (.encodeToString encoder))))
