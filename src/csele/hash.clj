(ns csele.hash)

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
