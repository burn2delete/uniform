(ns gravity.pass-execution.canonical
  "Type-sensitive canonical encoding and content identity."
  (:require [gravity.digest :as digest]
            [gravity.pass-execution.diagnostics :as diagnostics]))

(declare canonical-node)

(defn integral-tag
  [value]
  (cond
    (instance? Byte value) :byte
    (instance? Short value) :short
    (instance? Integer value) :int
    (instance? Long value) :long
    (instance? clojure.lang.BigInt value) :bigint
    (instance? java.math.BigInteger value) :biginteger
    :else nil))

(defn canonical-sort
  [values]
  ;; Serialize once per entry; sorting retains the existing stable order for
  ;; equal printed keys. Decorations live only for this invocation.
  (->> values
       (mapv (fn [value] [(pr-str value) value]))
       (sort-by first)
       (mapv second)))

(defn canonical-node
  [value]
  (when (and (instance? clojure.lang.IMeta value) (seq (meta value)))
    (diagnostics/fail! "C16-KEY" "semantic values may not carry host metadata"
                       {:value-class (.getName (class value))}))
  (cond
    (nil? value) [:nil]
    (true? value) [:boolean true]
    (false? value) [:boolean false]
    (string? value) [:string value]
    (char? value) [:character (int value)]
    (keyword? value) [:keyword (namespace value) (name value)]
    (symbol? value) [:symbol (namespace value) (name value)]
    (integral-tag value) [:integer (integral-tag value) (str value)]
    (ratio? value) [:ratio (str (numerator value)) (str (denominator value))]
    (map? value) [:map (canonical-sort
                        (mapv (fn [[key item]]
                                [(canonical-node key)
                                 (canonical-node item)])
                              value))]
    (set? value) [:set (canonical-sort
                        (mapv canonical-node value))]
    (vector? value) [:vector (mapv canonical-node value)]
    (seq? value) [:list (mapv canonical-node value)]
    :else (diagnostics/fail! "C16-KEY"
                             "unsupported value in semantic identity"
                             {:value-class (.getName (class value))})))

(defn canonical-bytes
  [value]
  (let [text (pr-str (canonical-node value))
        bytes (.getBytes text java.nio.charset.StandardCharsets/UTF_8)]
    bytes))

(defn content-id
  [domain value]
  (str "sha256:"
       (digest/sha256-bytes-hex
        (canonical-bytes {:domain domain :schema-version 1 :value value}))))
