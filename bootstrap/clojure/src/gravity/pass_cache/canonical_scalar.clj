(ns gravity.pass-cache.canonical-scalar
  "Type and representation checks for canonical cache scalars."
  (:require [gravity.pass-cache.policy :refer :all])
  (:import [java.math BigInteger]))

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
