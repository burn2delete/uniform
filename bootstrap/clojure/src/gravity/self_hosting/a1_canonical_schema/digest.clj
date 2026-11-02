(ns gravity.self-hosting.a1-canonical-schema.digest
  "Canonical equality and SHA-256 traversal for A1 uniqueness checks."
  (:require [gravity.self-hosting.a1-canonical-schema.config :as config]
            [gravity.self-hosting.a1-canonical-schema.ordering :as ordering])
  (:import (java.math BigInteger)
           (java.security MessageDigest)))

(defn digest-byte! [^MessageDigest digest value]
  (.update digest (byte value)))

(defn digest-u32! [digest value]
  (doseq [shift [24 16 8 0]]
    (digest-byte! digest (bit-and 0xff (bit-shift-right value shift)))))

(defn digest-u64! [digest value]
  (let [bits (.longValue ^BigInteger (biginteger value))]
    (doseq [shift [56 48 40 32 24 16 8 0]]
      (digest-byte! digest (bit-and 0xff (unsigned-bit-shift-right bits shift))))))

(defn digest-string! [digest ^String value]
  (loop [index 0]
    (when (< index (.length value))
      (let [point (.codePointAt value index)]
        (cond
          (<= point 0x7f)
          (digest-byte! digest point)

          (<= point 0x7ff)
          (do (digest-byte! digest (+ 0xc0 (bit-shift-right point 6)))
              (digest-byte! digest (+ 0x80 (bit-and point 0x3f))))

          (<= point 0xffff)
          (do (digest-byte! digest (+ 0xe0 (bit-shift-right point 12)))
              (digest-byte! digest
                            (+ 0x80 (bit-and (bit-shift-right point 6) 0x3f)))
              (digest-byte! digest (+ 0x80 (bit-and point 0x3f))))

          :else
          (do (digest-byte! digest (+ 0xf0 (bit-shift-right point 18)))
              (digest-byte! digest
                            (+ 0x80 (bit-and (bit-shift-right point 12) 0x3f)))
              (digest-byte! digest
                            (+ 0x80 (bit-and (bit-shift-right point 6) 0x3f)))
              (digest-byte! digest (+ 0x80 (bit-and point 0x3f)))))
        (recur (+ index (Character/charCount point)))))))

(declare digest-value!)

(defn digest-value! [digest value path]
  (let [tag (cond (nil? value) 0
                  (config/exact-class? value Boolean) 1
                  (config/uint64? value) 2
                  (config/exact-class? value String) 3
                  (config/canonical-vector? value) 4
                  :else 5)]
    (digest-byte! digest tag)
    (cond
      (nil? value) nil
      (config/exact-class? value Boolean)
      (digest-byte! digest (if value 1 0))
      (config/uint64? value)
      (digest-u64! digest value)
      (config/exact-class? value String)
      (do (digest-u32! digest (config/utf8-length value))
          (digest-string! digest value))
      (config/canonical-vector? value)
      (do
        (digest-u32! digest (count value))
        (doseq [[index item] (map-indexed vector value)]
          (digest-u32! digest index)
          (digest-value! digest item (config/path-child path index))))
      (config/canonical-map? value)
      (do
        (digest-u32! digest (count value))
        (ordering/ordered-entries
          value path
          (fn [entries]
            (doseq [[key item] entries]
              (digest-value! digest key (config/path-child path key))
              (digest-value! digest item (config/path-child path key)))))))))

(defn value-digest! [value path]
  (let [digest (MessageDigest/getInstance "SHA-256")]
    (digest-value! digest value path)
    (.digest digest)))

(declare canonical-equal!)

(defn canonical-equal! [left right path]
  (cond
    (and (nil? left) (nil? right)) true
    (and (config/exact-class? left Boolean)
         (config/exact-class? right Boolean)) (= left right)
    (and (config/uint64? left) (config/uint64? right)) (= left right)
    (and (config/exact-class? left String)
         (config/exact-class? right String))
    (= left right)
    (and (config/canonical-vector? left) (config/canonical-vector? right)
         (= (count left) (count right)))
    (every? true?
            (map-indexed (fn [index item]
                           (canonical-equal! item (nth right index)
                                             (config/path-child path index)))
                         left))
    (and (config/canonical-map? left) (config/canonical-map? right)
         (= (count left) (count right)))
    (ordering/ordered-entries
      left path
      (fn [left-entries]
        (every? true?
                (map (fn [[key item]]
                       (and (contains? right key)
                            (canonical-equal! item (get right key)
                                              (config/path-child path key))))
                     left-entries))))
    :else false))
