(ns csele.keys
  (:import [java.security KeyFactory KeyPair KeyPairGenerator]
           [java.security.spec X509EncodedKeySpec PKCS8EncodedKeySpec]
           [java.io StringReader StringWriter]
           [org.bouncycastle.openssl PEMParser PEMWriter PEMKeyPair]
           [org.bouncycastle.asn1.x509 SubjectPublicKeyInfo]
           [org.bouncycastle.crypto.util PrivateKeyFactory]
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
  (let [input-key (-> input StringReader. PEMParser. .readObject)]
    ; private key: PEMKeyPair
    ; public key: SubjectPublicKeyInfo
    (let [key-factory (KeyFactory/getInstance "RSA")]
      (if (instance? PEMKeyPair input-key)
        (let [bytes (-> ^PEMKeyPair input-key .getPrivateKeyInfo .getEncoded)
              key-spec (PKCS8EncodedKeySpec. bytes)]
          (.generatePrivate key-factory key-spec))
        (let [bytes (.getEncoded ^SubjectPublicKeyInfo input-key)
              key-spec (X509EncodedKeySpec. bytes)]
          (.generatePublic key-factory key-spec))))))

(defn key-to-string
  "Turns a key (private or public) into a string."
  [input-key]
  (let [string-writer (StringWriter.)
        pem-writer (PEMWriter. string-writer)]
    (.writeObject pem-writer input-key)
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
  (let [^sun.security.rsa.RSAPublicKeyImpl input-key (string-to-key input)
        modulus (-> input-key .getModulus .toByteArray)
        exponent (-> input-key .getPublicExponent .toByteArray)
        encoder (Base64/getUrlEncoder)]
    (str "RSA."
         (.encodeToString encoder modulus)
         "."
         (.encodeToString encoder exponent))))
