(ns csele.signatures
  (:require [csele.keys :as keys])
  (:import [java.security Security Signature]
           [java.util Base64]))

(Security/addProvider (org.bouncycastle.jce.provider.BouncyCastleProvider.))

(def algo "SHA256withRSA")

(defn verify
  "Expects a base64 encoded signature"
  [^String signature ^String actual-data public-key]
  (let [sig (doto (Signature/getInstance algo)
                  (.initVerify ^org.bouncycastle.jce.provider.JCERSAPublicKey
                    (keys/string-to-key public-key))
                  (.update (.getBytes actual-data)))]
    (.verify sig
      (.decode (Base64/getDecoder) signature))))

(defn sign
  "Produces a base64 encoded signature"
  [^String data private-key]
  (let [sig (doto (Signature/getInstance algo)
                  (.initSign (keys/string-to-key private-key))
                  (.update (.getBytes data)))]
    (.encodeToString (Base64/getEncoder)
      (.sign sig))))
