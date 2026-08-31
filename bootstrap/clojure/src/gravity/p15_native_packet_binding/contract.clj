(ns gravity.p15-native-packet-binding.contract
  (:import [java.nio.charset StandardCharsets]
           [java.security MessageDigest]))

(def packet-limit 65536)
(def instruction-limit 128)
(def stack-limit 128)
(def value-limit 1024)
(def output-limit 8192)
(def identity-file-limit 1048576)
(def runtime-contract-relative
  "bootstrap/gravity/p15_s23/native_runtime_driver.gravity")
(def provider-relative
  "bootstrap/native/p15_native_runtime_driver.c")

(defn utf8-bytes [value]
  (.getBytes ^String value StandardCharsets/UTF_8))

(defn sha256-bytes-hex [^bytes bytes]
  (let [digest (.digest (doto (MessageDigest/getInstance "SHA-256")
                          (.update bytes)))]
    (apply str (map #(format "%02x" (bit-and (int %) 0xff)) digest))))

(defn sha256-text [text]
  (str "sha256:" (sha256-bytes-hex (utf8-bytes text))))

(defn hex-encode [^bytes bytes]
  (apply str (map #(format "%02x" (bit-and (int %) 0xff)) bytes)))

(defn scalar-kind [value]
  (cond
    (nil? value) :nil
    (boolean? value) :bool
    (string? value) :string
    (integer? value) :integer
    :else nil))
