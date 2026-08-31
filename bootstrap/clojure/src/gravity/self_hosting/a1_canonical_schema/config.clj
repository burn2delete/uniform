(ns gravity.self-hosting.a1-canonical-schema.config
  "Closed-domain constants, host predicates, paths, and diagnostics for A1."
  (:import (clojure.lang BigInt PersistentArrayMap PersistentHashMap
                         PersistentVector)))

(def limits
  {:string-bytes 65536
   :items 1024
   :schemas 512
   :depth 64
   :input-bytes 786432
   :output-bytes 750000
   :frames 65
   :key-slots 1024
   :digest-slots 1024
   :work 65536})

(def terminal-work 10)
(def terminal-bytes 128)
(def uint64-max 18446744073709551615N)
(def schema-id-pattern #"[a-z][a-z0-9-]{0,63}")
(def allowed-kinds
  #{"null" "boolean" "uint64" "string" "enum" "array" "object"
    "tagged-union"})

(def namespace-contract
  {:namespace 'gravity.self-hosting.a1-canonical-schema
   :decision "docs/artifacts/phase-15/reports/a1-canonical-schema-invariant-architecture-decision.md"
   :public-api {'canonical-copy 1
                'admit-schema-registry 1
                'validate-and-copy 3}
   :seed :clojure
   :successor :gravity-uniform
   :advances-gates []
   :held ["A2" "A3" "Stage B" "Stage C" "G1" "G2" "G3" "G4" "G5" "G6"]})

(defn accepted [value]
  {"status" "accepted" "diagnostic" "OK" "value" value "path" []})

(defn rejected [diagnostic path]
  {"status" "typed-rejected" "diagnostic" diagnostic "value" nil
   "path" path})

(defn path-of [& segments]
  (reduce (fn [path segment] (cons segment path)) nil segments))

(defn path-child [path segment]
  (cons segment path))

(defn path-count [path]
  (min 64 (count path)))

(defn path-segment [path index]
  (if (vector? path)
    (nth path index)
    (nth path (- (count path) index 1))))

(defn path-vector [path]
  (mapv #(path-segment path %) (range (path-count path))))

(defn fail! [diagnostic path]
  (throw (ex-info diagnostic {:a1/failure true
                              :diagnostic diagnostic
                              :path path})))

(defn utf8-length [^String value]
  (loop [index 0 total 0]
    (if (= index (.length value))
      total
      (let [code-point (.codePointAt value index)]
        (recur (+ index (Character/charCount code-point))
               (+ total (cond
                          (<= code-point 0x7f) 1
                          (<= code-point 0x7ff) 2
                          (<= code-point 0xffff) 3
                          :else 4)))))))

(defn scalar-string? [^String value]
  (loop [index 0]
    (if (= index (.length value))
      true
      (let [unit (int (.charAt value index))]
        (cond
          (<= 0xD800 unit 0xDBFF)
          (and (< (inc index) (.length value))
               (<= 0xDC00 (int (.charAt value (inc index))) 0xDFFF)
               (recur (+ index 2)))

          (<= 0xDC00 unit 0xDFFF) false
          :else (recur (inc index)))))))

(defn exact-class? [value klass]
  (and (some? value) (= (class value) klass)))

(defn canonical-map? [value]
  (or (exact-class? value PersistentArrayMap)
      (exact-class? value PersistentHashMap)))

(defn canonical-vector? [value]
  (exact-class? value PersistentVector))

(defn no-metadata! [value path]
  (when (and (instance? clojure.lang.IMeta value) (some? (meta value)))
    (fail! "E-TYPE" path)))

(defn uint64? [value]
  (and (or (exact-class? value Long) (exact-class? value BigInt))
       (<= 0 value uint64-max)))

(defn byte-compare [^String left ^String right]
  ;; UTF-8 preserves scalar-value order, so no byte array is needed.
  (loop [left-index 0 right-index 0]
    (cond
      (= left-index (.length left)) (if (= right-index (.length right)) 0 -1)
      (= right-index (.length right)) 1
      :else
      (let [left-point (.codePointAt left left-index)
            right-point (.codePointAt right right-index)
            comparison (compare left-point right-point)]
        (if (zero? comparison)
          (recur (+ left-index (Character/charCount left-point))
                 (+ right-index (Character/charCount right-point)))
          comparison)))))

(defn segment-compare [left right]
  (cond
    (and (string? left) (string? right)) (byte-compare left right)
    (string? left) -1
    (string? right) 1
    :else (compare left right)))

(defn path-compare [left right]
  (loop [index 0]
    (cond
      (= index (path-count left)) (if (= index (path-count right)) 0 -1)
      (= index (path-count right)) 1
      :else (let [comparison (segment-compare (path-segment left index)
                                              (path-segment right index))]
              (if (zero? comparison) (recur (inc index)) comparison)))))

(defn first-path [paths]
  (reduce (fn [best path]
            (if (or (nil? best) (neg? (path-compare path best))) path best))
          nil paths))

(defn canonical-uint [value]
  (if (<= value Long/MAX_VALUE) (long value) (bigint value)))
