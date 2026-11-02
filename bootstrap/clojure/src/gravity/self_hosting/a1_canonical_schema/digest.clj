(ns gravity.self-hosting.a1-canonical-schema.digest
  "Canonical equality and SHA-256 traversal for A1 uniqueness checks."
  (:require [gravity.self-hosting.a1-canonical-schema.budget :as budget]
            [gravity.self-hosting.a1-canonical-schema.config :as config]
            [gravity.self-hosting.a1-canonical-schema.ordering :as ordering])
  (:import (java.math BigInteger)
           (java.security MessageDigest)))

(defn digest-byte! [state ^MessageDigest digest value path]
  (budget/work! state 1 path)
  (.update digest (byte value)))

(defn digest-u32! [state digest value path]
  (doseq [shift [24 16 8 0]]
    (digest-byte! state digest
                  (bit-and 0xff (bit-shift-right value shift)) path)))

(defn digest-u64! [state digest value path]
  (let [bits (.longValue ^BigInteger (biginteger value))]
    (doseq [shift [56 48 40 32 24 16 8 0]]
      (digest-byte! state digest
                    (bit-and 0xff (unsigned-bit-shift-right bits shift)) path))))

(defn digest-string! [state digest ^String value path]
  (loop [index 0]
    (when (< index (.length value))
      (let [point (.codePointAt value index)]
        (cond
          (<= point 0x7f)
          (digest-byte! state digest point path)

          (<= point 0x7ff)
          (do (digest-byte! state digest (+ 0xc0 (bit-shift-right point 6)) path)
              (digest-byte! state digest (+ 0x80 (bit-and point 0x3f)) path))

          (<= point 0xffff)
          (do (digest-byte! state digest (+ 0xe0 (bit-shift-right point 12)) path)
              (digest-byte! state digest
                            (+ 0x80 (bit-and (bit-shift-right point 6) 0x3f)) path)
              (digest-byte! state digest (+ 0x80 (bit-and point 0x3f)) path))

          :else
          (do (digest-byte! state digest (+ 0xf0 (bit-shift-right point 18)) path)
              (digest-byte! state digest
                            (+ 0x80 (bit-and (bit-shift-right point 12) 0x3f)) path)
              (digest-byte! state digest
                            (+ 0x80 (bit-and (bit-shift-right point 6) 0x3f)) path)
              (digest-byte! state digest (+ 0x80 (bit-and point 0x3f)) path)))
        (recur (+ index (Character/charCount point)))))))

(declare digest-value!)

(defn digest-value! [state digest value path]
  (let [tag (cond (nil? value) 0
                  (config/exact-class? value Boolean) 1
                  (config/uint64? value) 2
                  (config/exact-class? value String) 3
                  (config/canonical-vector? value) 4
                  :else 5)]
    (digest-byte! state digest tag path)
    (cond
      (nil? value) nil
      (config/exact-class? value Boolean)
      (digest-byte! state digest (if value 1 0) path)
      (config/uint64? value)
      (digest-u64! state digest value path)
      (config/exact-class? value String)
      (do (digest-u32! state digest (config/utf8-length value) path)
          (digest-string! state digest value path))
      (config/canonical-vector? value)
      (do
        (digest-u32! state digest (count value) path)
        (doseq [[index item] (map-indexed vector value)]
          (digest-u32! state digest index path)
          (digest-value! state digest item (config/path-child path index))))
      (config/canonical-map? value)
      (do
        (digest-u32! state digest (count value) path)
        (ordering/ordered-entries
          state value path
          (fn [entries]
            (doseq [[key item] entries]
              (digest-value! state digest key (config/path-child path key))
              (digest-value! state digest item (config/path-child path key)))))))))

(defn value-digest! [state value path]
  (let [digest (MessageDigest/getInstance "SHA-256")]
    (digest-value! state digest value path)
    (.digest digest)))

(declare canonical-equal!)

(defn canonical-equal! [state left right path]
  (budget/work! state 1 path)
  (cond
    (and (nil? left) (nil? right)) true
    (and (config/exact-class? left Boolean)
         (config/exact-class? right Boolean)) (= left right)
    (and (config/uint64? left) (config/uint64? right)) (= left right)
    (and (config/exact-class? left String)
         (config/exact-class? right String))
    (let [compared (min (config/utf8-length left) (config/utf8-length right))]
      (budget/work! state compared path)
      (= left right))
    (and (config/canonical-vector? left) (config/canonical-vector? right)
         (= (count left) (count right)))
    (every? true?
            (map-indexed (fn [index item]
                           (canonical-equal! state item (nth right index)
                                             (config/path-child path index)))
                         left))
    (and (config/canonical-map? left) (config/canonical-map? right)
         (= (count left) (count right)))
    (ordering/ordered-entries
      state left path
      (fn [left-entries]
        (every? true?
                (map (fn [[key item]]
                       (and (contains? right key)
                            (canonical-equal! state item (get right key)
                                              (config/path-child path key))))
                     left-entries))))
    :else false))
