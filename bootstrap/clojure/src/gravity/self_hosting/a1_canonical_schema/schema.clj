(ns gravity.self-hosting.a1-canonical-schema.schema
  "Schema algebra, ranked diagnostics, references, and registry admission."
  (:require [gravity.self-hosting.a1-canonical-schema.config :as config]
            [gravity.self-hosting.a1-canonical-schema.digest :as digest]
            [gravity.self-hosting.a1-canonical-schema.ordering :as ordering])
  (:import (java.util Arrays)))

(defn schema-id! [value path]
  (when-not (config/exact-class? value String) (config/fail! "E-ID-TYPE" path))
  (when-not (re-matches config/schema-id-pattern value)
    (config/fail! "E-ID-SYNTAX" path))
  value)

(defn exact-fields! [definition allowed required path]
  (let [actual (keys definition)]
    (when (or (not (every? string? actual))
              (not (every? #(contains? definition %) required))
              (not (every? allowed actual)))
      (config/fail! "E-KEYSET" path))))

(defn boolean-field! [definition name path]
  (when-not (config/exact-class? (get definition name) Boolean)
    (config/fail! "E-SCHEMA" (config/path-child path name))))

(defn uint-field! [definition name path]
  (let [value (get definition name)]
    (when-not (config/uint64? value)
      (config/fail! "E-SCHEMA" (config/path-child path name)))
    value))

(defn ensure-unique! [values path]
  (let [n (count values)
        digests (object-array n)]
    (loop [index 0]
      (when (< index n)
        (let [item (nth values index)
              item-path (config/path-child path index)
              value-digest (digest/value-digest! item item-path)]
          (loop [prior-index 0]
            (when (< prior-index index)
              (if (and (Arrays/equals ^bytes (aget digests prior-index)
                                      ^bytes value-digest)
                       (digest/canonical-equal!
                         (nth values prior-index) item
                         (config/path-child path prior-index)))
                (config/fail! "E-SCHEMA" item-path)
                (recur (inc prior-index)))))
          (aset digests index value-digest)
          (recur (inc index)))))))

(defn refs-in-definition! [definition path]
  (let [kind (get definition "kind")]
    (when-not (and (config/exact-class? kind String)
                   (contains? config/allowed-kinds kind))
      (config/fail! "E-SCHEMA" (config/path-child path "kind")))
    (case kind
      "null" (do (exact-fields! definition #{"kind"} #{"kind"} path) [])
      "boolean" (do (exact-fields! definition #{"kind"} #{"kind"} path) [])
      "uint64" (do (exact-fields! definition #{"kind"} #{"kind"} path) [])
      "string"
      (do (exact-fields! definition #{"kind" "ascii-only" "max-bytes"}
                         #{"kind" "ascii-only" "max-bytes"} path)
          (boolean-field! definition "ascii-only" path)
          (uint-field! definition "max-bytes" path)
          [])
      "enum"
      (do (exact-fields! definition #{"kind" "values"} #{"kind" "values"} path)
          (let [values (get definition "values")]
            (when-not (and (config/canonical-vector? values)
                           (seq values)
                           (every? #(config/exact-class? % String) values))
              (config/fail! "E-SCHEMA" (config/path-child path "values")))
            (ensure-unique! values (config/path-child path "values")))
          [])
      "array"
      (do (exact-fields! definition
                         #{"kind" "item" "min-items" "max-items" "unique"}
                         #{"kind" "item" "min-items" "max-items" "unique"} path)
          (boolean-field! definition "unique" path)
          (let [minimum (uint-field! definition "min-items" path)
                maximum (uint-field! definition "max-items" path)]
            (when (> minimum maximum) (config/fail! "E-SCHEMA" path)))
          [(schema-id! (get definition "item")
                       (config/path-child path "item"))])
      "object"
      (do (exact-fields! definition #{"kind" "required" "optional"}
                         #{"kind" "required" "optional"} path)
          (let [required (get definition "required")
                optional (get definition "optional")]
            (when-not (and (config/canonical-map? required)
                           (config/canonical-map? optional)
                           (not-any? #(contains? optional %) (keys required)))
              (config/fail! "E-SCHEMA" path))
            []))
      "tagged-union"
      (do (exact-fields! definition #{"kind" "tag-key" "variants"}
                         #{"kind" "tag-key" "variants"} path)
          (let [tag-key (get definition "tag-key")
                variants (get definition "variants")]
            (when-not (and (config/exact-class? tag-key String)
                           (not= tag-key "value")
                           (config/canonical-map? variants)
                           (seq variants))
              (config/fail! "E-SCHEMA" path))
            []))
      [])))

(defn candidate-refs [id definition]
  (if-not (config/canonical-map? definition)
    []
    (case (get definition "kind")
      "array" [[id (config/path-of id "item") (get definition "item")]]
      "object"
      (reduce into []
              (for [name ["required" "optional"]
                    :let [fields (get definition name)]
                    :when (config/canonical-map? fields)]
                (ordering/ordered-entries
                  fields (config/path-of id name)
                  #(mapv (fn [[field ref]]
                           [id (config/path-of id field) ref]) %))))
      "tagged-union"
      (if (config/canonical-map? (get definition "variants"))
        (ordering/ordered-entries
          (get definition "variants") (config/path-of id "variants")
          #(mapv (fn [[tag ref]] [id (config/path-of id tag) ref]) %))
        [])
      [])))

(def diagnostic-rank
  {"E-TYPE" 0 "E-KEYSET" 1 "E-CYCLE" 2 "E-SCHEMA" 3 "E-HOST" 4 "OK" 5})

(defn ranked-fault [faults]
  (reduce
    (fn [best fault]
      (if (nil? best)
        fault
        (let [rank-order (compare (get diagnostic-rank (:diagnostic fault) 99)
                                  (get diagnostic-rank (:diagnostic best) 99))]
          (if (or (neg? rank-order)
                  (and (zero? rank-order)
                       (neg? (config/path-compare (:path fault) (:path best)))))
            fault best))))
    nil faults))

(defn registry-shape-faults [ordered]
  (keep (fn [[id definition]]
          (try
            (when-not (config/canonical-map? definition)
              (config/fail! "E-SCHEMA" (config/path-of id)))
            (refs-in-definition! definition (config/path-of id))
            nil
            (catch clojure.lang.ExceptionInfo failure
              (ex-data failure))))
        ordered))

(defn check-ordered-registry! [registry ordered]
  (let [refs (vec (mapcat (fn [[id definition]]
                            (candidate-refs id definition)) ordered))]
    (doseq [[_ path ref] refs]
      (when-not (config/exact-class? ref String)
        (config/fail! "E-ID-TYPE" path)))
    (let [syntax-paths
          (concat (keep (fn [[id _]]
                          (when-not (re-matches config/schema-id-pattern id)
                            (config/path-of id)))
                        ordered)
                  (keep (fn [[_ path ref]]
                          (when-not (re-matches config/schema-id-pattern ref) path))
                        refs))]
      (when (seq syntax-paths)
        (config/fail! "E-ID-SYNTAX" (config/first-path syntax-paths))))
    (doseq [[_ path ref] refs]
      (when-not (contains? registry ref) (config/fail! "E-UNKNOWN-ID" path)))
    (let [shape-faults (vec (registry-shape-faults ordered))
          high-fault (ranked-fault
                       (filter #(contains? #{"E-TYPE" "E-KEYSET"}
                                           (:diagnostic %))
                               shape-faults))
          _ (when high-fault
              (config/fail! (:diagnostic high-fault) (:path high-fault)))
          shape-fault (ranked-fault shape-faults)
          graph (reduce (fn [result [id _]] (assoc result id [])) {} ordered)
          graph (reduce (fn [result [id _ ref]] (update result id conj ref))
                        graph refs)]
      (let [colors (atom {})]
        (letfn [(visit [id]
                  (case (get @colors id)
                    :gray (config/fail! "E-CYCLE" (config/path-of id))
                    :black true
                    (do
                      (swap! colors assoc id :gray)
                      (doseq [child (get graph id)] (visit child))
                      (swap! colors assoc id :black)
                      true)))]
          (doseq [[id _] ordered] (visit id))))
      (when shape-fault
        (config/fail! (:diagnostic shape-fault) (:path shape-fault)))
      graph)))

(defn check-registry! [registry]
  (ordering/ordered-entries
    registry nil
    #(check-ordered-registry! registry %)))
