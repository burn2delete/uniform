(ns gravity.pass-execution.canonical
  "Bounded, type-sensitive canonical encoding and content identity."
  (:require [gravity.digest :as digest]
            [gravity.pass-execution.config :as config]
            [gravity.pass-execution.diagnostics :as diagnostics]))

(declare preflight-canonical! canonical-node)

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
  (->> values (sort-by pr-str) vec))

(defn bounded-utf8-size
  [text]
  (loop [index 0 size 0]
    (if (= index (.length ^String text))
      size
      (let [code-point (.codePointAt ^String text index)
            width (Character/charCount code-point)
            bytes (cond
                    (<= code-point 0x7f) 1
                    (<= code-point 0x7ff) 2
                    (<= code-point 0xffff) 3
                    :else 4)
            next-size (+ size bytes)]
        (when (> next-size config/maximum-canonical-bytes)
          (diagnostics/fail! "C16-KEY" "canonical scalar exceeds its byte bound"
                             {:maximum-bytes config/maximum-canonical-bytes}))
        (recur (+ index width) next-size)))))

(defn escaped-string-byte-bound
  [text]
  ;; Every UTF-8 byte can expand to at most one six-byte \\uXXXX escape in the
  ;; canonical printed representation.
  (* 6 (bounded-utf8-size text)))

(defn arbitrary-integer-bit-length
  [value]
  (let [integer (cond
                  (instance? clojure.lang.BigInt value)
                  (.toBigInteger ^clojure.lang.BigInt value)
                  (instance? java.math.BigInteger value) value
                  :else nil)]
    (cond
      integer
      (let [bits (.bitLength ^java.math.BigInteger integer)]
        (if (neg? (.signum ^java.math.BigInteger integer)) (inc bits) bits))
      (integral-tag value) 64
      :else nil)))

(defn integer-decimal-byte-bound!
  [value]
  (let [bits (arbitrary-integer-bit-length value)]
    (when (> bits config/maximum-integer-bits)
      (diagnostics/fail! "C16-KEY"
                         "canonical integer exceeds its magnitude bound"
                         {:maximum-bits config/maximum-integer-bits
                          :observed-bits bits}))
    (+ 1 (quot (+ (* bits 30103) 99999) 100000))))

(defn preflight-account!
  [state byte-bound]
  (let [{:keys [nodes bytes]}
        (swap! state (fn [{:keys [nodes bytes]}]
                       {:nodes (inc nodes) :bytes (+ bytes byte-bound)}))]
    (when (> nodes config/maximum-nodes)
      (diagnostics/fail! "C16-KEY" "canonical value exceeds its node bound"
                         {:maximum-nodes config/maximum-nodes}))
    (when (> bytes config/maximum-canonical-bytes)
      (diagnostics/fail! "C16-KEY" "canonical value exceeds its byte bound"
                         {:maximum-bytes config/maximum-canonical-bytes}))))

(declare preflight-value!)

(defn preflight-container-cardinality!
  [value]
  (when (> (count value) config/maximum-nodes)
    (diagnostics/fail! "C16-KEY"
                       "canonical container exceeds its cardinality bound"
                       {:maximum-cardinality config/maximum-nodes})))

(defn preflight-value!
  [value state depth]
  (when (> depth config/maximum-depth)
    (diagnostics/fail! "C16-KEY" "canonical value exceeds its depth bound"
                       {:maximum-depth config/maximum-depth}))
  (let [scalar-byte-bound
        (cond
          (string? value) (escaped-string-byte-bound value)
          (or (keyword? value) (symbol? value))
          (+ (if-let [space (namespace value)]
               (escaped-string-byte-bound space) 0)
             (escaped-string-byte-bound (name value)))
          (integral-tag value) (integer-decimal-byte-bound! value)
          (ratio? value) (+ (integer-decimal-byte-bound! (numerator value))
                            (integer-decimal-byte-bound! (denominator value)))
          :else 0)]
    (preflight-account! state (+ 32 scalar-byte-bound)))
  (cond
    (map? value)
    (do (preflight-container-cardinality! value)
        (reduce-kv (fn [_ key item]
                     (preflight-value! key state (inc depth))
                     (preflight-value! item state (inc depth)) nil)
                   nil value))
    (or (set? value) (vector? value))
    (do (preflight-container-cardinality! value)
        (reduce (fn [_ item]
                  (preflight-value! item state (inc depth)) nil)
                nil value))
    (seq? value)
    (loop [remaining (seq value) cardinality 0]
      (when remaining
        (when (>= cardinality config/maximum-nodes)
          (diagnostics/fail! "C16-KEY"
                             "canonical sequence exceeds its cardinality bound"
                             {:maximum-cardinality config/maximum-nodes}))
        (preflight-value! (first remaining) state (inc depth))
        (recur (next remaining) (inc cardinality))))
    :else nil)
  value)

(defn preflight-canonical!
  [value]
  (preflight-value! value (atom {:nodes 0 :bytes 0}) 0)
  value)

(defn account-canonical!
  [state byte-estimate]
  (let [{:keys [nodes bytes]}
        (swap! state (fn [{:keys [nodes bytes]}]
                       {:nodes (inc nodes) :bytes (+ bytes byte-estimate)}))]
    (when (> nodes config/maximum-nodes)
      (diagnostics/fail! "C16-KEY" "canonical value exceeds its node bound"
                         {:maximum-nodes config/maximum-nodes}))
    (when (> bytes config/maximum-canonical-bytes)
      (diagnostics/fail! "C16-KEY" "canonical value exceeds its byte bound"
                         {:maximum-bytes config/maximum-canonical-bytes}))))

(defn canonical-node
  [value state depth]
  (when (> depth config/maximum-depth)
    (diagnostics/fail! "C16-KEY" "canonical value exceeds its depth bound"
                       {:maximum-depth config/maximum-depth}))
  (account-canonical!
   state
   (+ 32 (cond
           (string? value) (escaped-string-byte-bound value)
           (or (keyword? value) (symbol? value))
           (+ (if-let [space (namespace value)]
                (escaped-string-byte-bound space) 0)
              (escaped-string-byte-bound (name value)))
           (integral-tag value) (bounded-utf8-size (str value))
           (ratio? value) (+ (bounded-utf8-size (str (numerator value)))
                             (bounded-utf8-size (str (denominator value))))
           :else 0)))
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
                                [(canonical-node key state (inc depth))
                                 (canonical-node item state (inc depth))])
                              value))]
    (set? value) [:set (canonical-sort
                        (mapv #(canonical-node % state (inc depth)) value))]
    (vector? value) [:vector (mapv #(canonical-node % state (inc depth)) value)]
    (seq? value) [:list (mapv #(canonical-node % state (inc depth)) value)]
    :else (diagnostics/fail! "C16-KEY"
                             "unsupported value in semantic identity"
                             {:value-class (.getName (class value))})))

(defn canonical-bytes
  [value]
  (preflight-canonical! value)
  (let [text (pr-str (canonical-node value (atom {:nodes 0 :bytes 0}) 0))
        bytes (.getBytes text java.nio.charset.StandardCharsets/UTF_8)]
    (when (> (alength bytes) config/maximum-canonical-bytes)
      (diagnostics/fail! "C16-KEY" "canonical value exceeds its byte bound"
                         {:maximum-bytes config/maximum-canonical-bytes
                          :observed-bytes (alength bytes)}))
    bytes))

(defn content-id
  [domain value]
  (str "sha256:"
       (digest/sha256-bytes-hex
        (canonical-bytes {:domain domain :schema-version 1 :value value}))))
