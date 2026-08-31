(ns gravity.pass-cache.canonical-scalar
  "Bounded scalar accounting for canonical cache data."
  (:require [gravity.pass-cache.policy :refer :all])
  (:import [java.math BigDecimal BigInteger]))

(defn integral-tag
  [value]
  (cond
    (instance? Byte value) :byte
    (instance? Short value) :short
    (instance? Integer value) :int
    (instance? Long value) :long
    (instance? clojure.lang.BigInt value) :bigint
    (instance? BigInteger value) :biginteger
    :else nil))

(defn bounded-string-bytes
  [text]
  ;; Count exact UTF-8 bytes without first allocating an encoded copy.  The
  ;; former `4 * UTF-16-length` shortcut was safe but rejected large ASCII and
  ;; Base64 payloads far below the declared artifact bound.
  (loop [index 0 total 0]
    (if (= index (.length ^String text))
      total
      (let [code-point (.codePointAt ^String text index)
            width (Character/charCount code-point)
            encoded-width (cond
                            (<= code-point 0x7f) 1
                            (<= code-point 0x7ff) 2
                            (<= code-point 0xffff) 3
                            :else 4)
            next-total (+ total encoded-width)]
        (when (> next-total maximum-canonical-bytes)
          (fail! "C16-KEY" "canonical scalar exceeds its byte bound"
                 {:maximum-bytes maximum-canonical-bytes}))
        (recur (+ index width) next-total)))))

(defn byte-array?
  [value]
  (and value (= (class value) (Class/forName "[B"))))

(defn object-metadata!
  [value]
  (when (and (instance? clojure.lang.IMeta value) (seq (meta value)))
    (fail! "C16-KEY" "semantic cache values may not carry host metadata"
           {:value-class (.getName (class value))}))
  value)

(defn finite-double!
  [value]
  (when-not (Double/isFinite (double value))
    (fail! "C16-KEY" "nonfinite floating point values are forbidden"
           {:observed value}))
  value)

(defn integer-bit-length
  [value]
  (cond
    (instance? BigInteger value) (.bitLength ^BigInteger value)
    (instance? clojure.lang.BigInt value)
    (.bitLength ^BigInteger (.toBigInteger ^clojure.lang.BigInt value))
    :else (.bitLength (BigInteger/valueOf (long value)))))

(defn require-integer-bound!
  [field value]
  (when (> (integer-bit-length value) (* 8 maximum-canonical-bytes))
    (fail! "C16-KEY" "integer exceeds canonical bit bound"
           {:field field :maximum-bits (* 8 maximum-canonical-bytes)}))
  value)

(defn negative-integral?
  [value]
  (cond
    (instance? BigInteger value) (neg? (.signum ^BigInteger value))
    (instance? clojure.lang.BigInt value)
    (neg? (.signum ^BigInteger
                   (.toBigInteger ^clojure.lang.BigInt value)))
    :else (neg? (long value))))

(defn conservative-integer-printed-bytes
  [value]
  ;; ceil(bit-length * log10(2)), plus sign and surrounding string quotes.
  (let [bits (max 1 (integer-bit-length value))
        digits (quot (+ (* (long bits) 30103) 99999) 100000)]
    (+ digits (if (negative-integral? value) 1 0) 2)))

(declare ^{:private true} account-bytes!)

(defn account-integral-text!
  [state field value]
  (require-integer-bound! field value)
  (account-bytes! state (conservative-integer-printed-bytes value)))

(defn account-bigdecimal-text!
  [state ^BigDecimal value]
  ;; BigDecimal.toString may choose plain or exponent form. precision+|scale|
  ;; plus sign, decimal point, exponent marker/sign/digits, and quotes is a
  ;; conservative allocation-free upper bound.
  (let [precision (long (.precision value))
        scale (long (.scale value))
        scale-magnitude (Math/abs scale)
        exponent-magnitude (+ precision scale-magnitude 1)
        exponent-digits
        (max 1 (quot (+ (* (max 1 (.bitLength
                                   (BigInteger/valueOf exponent-magnitude)))
                             30103)
                          99999)
                       100000))
        upper-bound (+ precision scale-magnitude exponent-digits 8)]
    (account-bytes! state upper-bound)))

(defn account-node!
  [state]
  (let [{:keys [nodes]}
        (swap! state update :nodes inc)]
    (when (> nodes maximum-nodes)
      (fail! "C16-KEY" "canonical value exceeds its node bound"
             {:maximum-nodes maximum-nodes}))))

(defn account-bytes!
  [state estimate]
  (let [{:keys [bytes limit]}
        (swap! state update :bytes + estimate)]
    (when (> bytes maximum-canonical-bytes)
      (fail! "C16-KEY" "canonical value exceeds its byte bound"
             {:maximum-bytes maximum-canonical-bytes}))
    (when (> bytes limit)
      (fail! "C16-KEY" "canonical value exceeds its requested byte bound"
             {:maximum-bytes limit}))))

(defn account-text!
  [state text]
  (let [text (str text)
        _ (bounded-string-bytes text)
        printed-bytes
        (loop [index 0 total 2]
          (if (< index (.length ^String text))
            (let [code-point (.codePointAt ^String text index)
                  width (Character/charCount code-point)
                  escaped-bytes
                  (cond
                    (or (= code-point (int \")) (= code-point (int \\))) 2
                    (#{(int \newline) (int \return) (int \tab)
                       (int \backspace) (int \formfeed)} code-point) 2
                    (Character/isISOControl code-point) 6
                    (<= code-point 0x7f) 1
                    (<= code-point 0x7ff) 2
                    (<= code-point 0xffff) 3
                    :else 4)]
              (recur (+ index width) (+ total escaped-bytes)))
            total))]
    (account-bytes! state printed-bytes)))
