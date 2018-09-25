(ns csele.keys
  (:import [java.security KeyFactory KeyPairGenerator KeyPair]
           [java.security.spec X509EncodedKeySpec]
           [java.io StringReader StringWriter]
           [org.bouncycastle.openssl PEMParser PEMWriter]
           [org.bouncycastle.asn1.x509 SubjectPublicKeyInfo]
           [java.util Base64]))

(defn raw-keys
  "Generates raw keys. It's a Java object so don't touch it unless you know
  what you're doing."
  [^Integer strength]
  (let [generator (doto (KeyPairGenerator/getInstance "RSA")
                    (.initialize strength))]
    (.generateKeyPair generator)))

(defn string-to-key
  [input]
  (let [key (-> input StringReader. PEMParser. .readObject)]
    ; private key: PEMKeyPair
    ; public key: SubjectPublicKeyInfo
    (if (instance? KeyPair key)
      (.getPrivate ^KeyPair key)
      (let [bytes (.getEncoded ^SubjectPublicKeyInfo key)
            key-spec (X509EncodedKeySpec. bytes)
            key-factory (KeyFactory/getInstance "RSA")]
        (.generatePublic key-factory key-spec)))))

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
  [^String input]
  (let [^sun.security.rsa.RSAPublicKeyImpl key (string-to-key input)
        modulus (-> key .getModulus .toByteArray)
        exponent (-> key .getPublicExponent .toByteArray)
        encoder (Base64/getUrlEncoder)]
    (str "RSA."
         (.encodeToString encoder modulus)
         "."
         (.encodeToString encoder exponent))))
