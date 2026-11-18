(ns gravity.c2-artifact-identity.canonical
  (:require [gravity.digest :as digest]))

(defn reader-canonical-value [canonical-value value]
  (cond
    (map? value)
    (let [decorated (mapv (fn [[key item]]
                            (let [entry [(canonical-value key) (canonical-value item)]]
                              (clojure.lang.MapEntry/create (pr-str entry) entry)))
                          value)]
      [:map (->> decorated (sort-by key) (mapv val))])

    (set? value)
    (let [decorated (mapv (fn [item]
                            (let [entry (canonical-value item)]
                              (clojure.lang.MapEntry/create (pr-str entry) entry)))
                          value)]
      [:set (mapv val (sort-by key decorated))])

    (vector? value) [:vector (mapv canonical-value value)]
    (seq? value) [:list (mapv canonical-value value)]
    :else value))

(def ^:private default-reader-canonical-value reader-canonical-value)

(defn reader-canonical-value-default [value]
  (reader-canonical-value reader-canonical-value-default value))

(def ^:private default-reader-canonical-value-callback
  reader-canonical-value-default)

(def ^:private default-pr pr)
(def ^:private default-pr-str pr-str)
(def ^:private default-pr-on
  (var-get (ns-resolve 'clojure.core 'pr-on)))
(def ^:private default-print-method print-method)
(def ^:private default-print-method-table
  (.getMethodTable ^clojure.lang.MultiFn print-method))
(def ^:private default-print-method-preferences
  (.getPreferTable ^clojure.lang.MultiFn print-method))

(def ^:private default-digest-sha256-hex digest/sha256-hex)
(def ^:private default-digest-sha256-output-hex digest/sha256-output-hex)

(defn streaming-sha256-hex
  "Internal SHA operation identity for the C2 vector streaming path."
  [text]
  (digest/sha256-hex text))

(def ^:private streamable-vector-classes
  #{clojure.lang.PersistentVector
    clojure.lang.LazilyPersistentVector})

(def ^:private streamable-map-classes
  #{clojure.lang.PersistentArrayMap
    clojure.lang.PersistentHashMap})

(def ^:private streamable-set-classes
  #{clojure.lang.PersistentHashSet})

(def ^:private streamable-list-classes
  #{clojure.lang.PersistentList
    (class '())})

(def ^:private streamable-scalar-classes
  #{java.lang.Boolean
    java.lang.Byte
    java.lang.Character
    java.lang.Double
    java.lang.Float
    java.lang.Integer
    java.lang.Long
    java.lang.Short
    java.lang.String
    java.math.BigDecimal
    java.math.BigInteger
    clojure.lang.BigInt
    clojure.lang.Keyword
    clojure.lang.Ratio
    clojure.lang.Symbol
    java.util.UUID
    java.util.regex.Pattern})

(declare ^:private streamable-value?)

(defn- streamable-metadata? [value]
  (let [metadata (when (instance? clojure.lang.IObj value) (meta value))]
    (or (nil? metadata)
        (and (contains? streamable-map-classes (class metadata))
             (streamable-value? metadata)
             (not (contains? metadata :type))))))

(defn- streamable-value? [value]
  (let [value-class (when-not (nil? value) (class value))]
    (cond
      (nil? value) true

      (contains? streamable-scalar-classes value-class)
      (streamable-metadata? value)

      (and (vector? value)
           (contains? streamable-vector-classes value-class))
      (every? streamable-value? value)

      (and (map? value)
           (contains? streamable-map-classes value-class))
      (every? (fn [[key item]]
                (and (streamable-value? key)
                     (streamable-value? item)))
              value)

      (and (set? value)
           (contains? streamable-set-classes value-class))
      (every? streamable-value? value)

      (and (seq? value)
           (contains? streamable-list-classes value-class))
      (every? streamable-value? value)

      :else false)))

(defn- default-printing?
  []
  (and (identical? pr default-pr)
       (identical? pr-str default-pr-str)
       (identical? (var-get (ns-resolve 'clojure.core 'pr-on)) default-pr-on)
       (identical? print-method default-print-method)
       (= default-print-method-table
          (.getMethodTable ^clojure.lang.MultiFn print-method))
       (= default-print-method-preferences
          (.getPreferTable ^clojure.lang.MultiFn print-method))
       (not *print-dup*)))

(defn reader-canonical-vector-hash
  "Hash a vector without retaining its complete canonical form or printed text.

  Returns nil when the current printer or canonicalizer cannot use the default
  streaming path; callers then use reader-canonical-hash."
  [sha256-hex canonical-value value]
  (when (and (identical? sha256-hex streaming-sha256-hex)
               (identical? reader-canonical-value default-reader-canonical-value)
               (identical? canonical-value
                           default-reader-canonical-value-callback)
               (identical? digest/sha256-hex default-digest-sha256-hex)
               (identical? digest/sha256-output-hex
                           default-digest-sha256-output-hex)
               (vector? value)
               (streamable-value? value)
               (default-printing?))
    (str "sha256:"
         (digest/sha256-output-hex
          (fn [output]
            (let [writer (java.io.OutputStreamWriter.
                          ^java.io.OutputStream output
                          java.nio.charset.StandardCharsets/UTF_8)]
              (binding [*out* writer
                        *print-length* nil
                        *print-level* nil
                        *print-meta* true]
                (.write writer "[:vector [")
                (loop [index 0]
                  (when (< index (count value))
                    (when (pos? index)
                      (.write writer " "))
                    (pr (canonical-value (nth value index)))
                    (recur (inc index))))
                (.write writer "]]"))
              (.flush writer)))))))

(defn reader-canonical-hash [sha256-hex canonical-value value]
  (str "sha256:"
       (sha256-hex
        (binding [*print-length* nil *print-level* nil *print-meta* true]
          (pr-str (canonical-value value))))))
