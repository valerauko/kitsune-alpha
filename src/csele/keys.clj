(ns csele.keys
  (:import [java.security KeyPairGenerator KeyPair]
           [java.io StringReader StringWriter]
           [org.bouncycastle.openssl PEMParser PEMWriter]
           [java.util Base64]))

(defn raw-keys
  "Generates raw keys. It's a Java object so don't touch it unless you know
  what you're doing."
  [strength]
  (let [generator (doto (KeyPairGenerator/getInstance "RSA")
                    (.initialize strength))]
    (.generateKeyPair generator)))

(defn string-to-key
  [input]
  (let [key (-> input StringReader. PEMParser. .readObject)]
    ; a private key is read as a keypair
    (if (instance? KeyPair key)
      (.getPrivate ^KeyPair key)
      key)))

(defn key-to-string
  "Turns a key (private or public) into a string."
  [key]
  (let [string-writer (StringWriter.)
        pem-writer (PEMWriter. string-writer)]
    (.writeObject pem-writer key)
    (.flush pem-writer)
    (.toString string-writer)))

(defn generate-keypair
  "Generates a private-public keypair and returns it in a hashmap of strings.
  You can set the strength (length) of the key generated. Defaults to 1024."
  ([] (generate-keypair 1024))
  ([strength]
    (let [^KeyPair keys (raw-keys strength)]
      { :public (-> keys .getPublic key-to-string)
        :private (-> keys .getPrivate key-to-string)})))

(defn salmon-public-key
  [input]
  (let [key (-> input string-to-key .getPublicKey)
        ; can't get a proper RSAPublicKey object out of this trash
        modulus (-> key (.getObjectAt 0) .getValue .toByteArray)
        exponent (-> key (.getObjectAt 1) .getValue .toByteArray)
        encoder (Base64/getUrlEncoder)]
    (str "RSA."
         (.encodeToString encoder modulus)
         "."
         (.encodeToString encoder exponent))))
