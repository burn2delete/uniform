(ns gravity.self-hosting.a1-canonical-schema.schema
  "Schema algebra, ranked diagnostics, references, and registry admission."
  (:require [gravity.self-hosting.a1-canonical-schema.budget :as budget]
            [gravity.self-hosting.a1-canonical-schema.config :as config]
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

(defn uint-field! [definition name maximum path]
  (let [value (get definition name)]
    (when-not (and (config/uint64? value) (<= value maximum))
      (config/fail! "E-SCHEMA" (config/path-child path name)))
    (long value)))

(defn ensure-unique! [state values path]
  (let [n (count values)]
    (budget/acquire! state :digest-slots n path)
    (try
      (let [digests (object-array n)]
        (loop [index 0]
          (when (< index n)
            (let [item (nth values index)
                  item-path (config/path-child path index)
                  value-digest (digest/value-digest! state item item-path)]
              (budget/work! state 1 item-path)
              (loop [prior-index 0]
                (when (< prior-index index)
                  (if (and (Arrays/equals ^bytes (aget digests prior-index)
                                          ^bytes value-digest)
                           (digest/canonical-equal!
                             state (nth values prior-index) item
                             (config/path-child path prior-index)))
                    (config/fail! "E-SCHEMA" item-path)
                    (recur (inc prior-index)))))
              (aset digests index value-digest)
              (recur (inc index))))))
      (finally (budget/release! state :digest-slots n)))))

(defn refs-in-definition! [state definition path]
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
          (uint-field! definition "max-bytes" (:string-bytes config/limits) path)
          [])
      "enum"
      (do (exact-fields! definition #{"kind" "values"} #{"kind" "values"} path)
          (let [values (get definition "values")]
            (when-not (and (config/canonical-vector? values)
                           (<= 1 (count values) 1024)
                           (every? #(config/exact-class? % String) values))
              (config/fail! "E-SCHEMA" (config/path-child path "values")))
            (ensure-unique! state values (config/path-child path "values")))
          [])
      "array"
      (do (exact-fields! definition
                         #{"kind" "item" "min-items" "max-items" "unique"}
                         #{"kind" "item" "min-items" "max-items" "unique"} path)
          (boolean-field! definition "unique" path)
          (let [minimum (uint-field! definition "min-items" 1024 path)
                maximum (uint-field! definition "max-items" 1024 path)]
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
                           (<= (+ (count required) (count optional)) 1024)
                           (not-any? #(contains? optional %) (keys required)))
              (config/fail! "E-SCHEMA" path))
            ;; Closed-value admission and ranked reference preflight already
            ;; establish field/ref types and canonical fault order.
            []))
      "tagged-union"
      (do (exact-fields! definition #{"kind" "tag-key" "variants"}
                         #{"kind" "tag-key" "variants"} path)
          (let [tag-key (get definition "tag-key")
                variants (get definition "variants")]
            (when-not (and (config/exact-class? tag-key String)
                           (not= tag-key "value")
                           (config/canonical-map? variants)
                           (<= 1 (count variants) 1024))
              (config/fail! "E-SCHEMA" path))
            [])))))

(defn candidate-refs [state id definition]
  (if-not (config/canonical-map? definition)
    []
    (case (get definition "kind")
      "array" (do (budget/work! state 1 (config/path-of id "item"))
                  [[id (config/path-of id "item") (get definition "item")]])
      "object"
      (reduce into []
              (for [name ["required" "optional"]
                    :let [fields (get definition name)]
                    :when (config/canonical-map? fields)]
                (ordering/ordered-entries
                  state fields (config/path-of id name)
                  #(do (budget/work! state (count %) (config/path-of id name))
                       (mapv (fn [[field ref]]
                               [id (config/path-of id field) ref]) %)))))
      "tagged-union"
      (if (config/canonical-map? (get definition "variants"))
        (ordering/ordered-entries
          state (get definition "variants") (config/path-of id "variants")
          #(do (budget/work! state (count %) (config/path-of id "variants"))
               (mapv (fn [[tag ref]] [id (config/path-of id tag) ref]) %)))
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

(defn registry-shape-faults [state ordered]
  (keep (fn [[id definition]]
          (try
            (when-not (config/canonical-map? definition)
              (config/fail! "E-SCHEMA" (config/path-of id)))
            (refs-in-definition! state definition (config/path-of id))
            nil
            (catch clojure.lang.ExceptionInfo failure
              (let [data (ex-data failure)]
                (when (= "E-BOUND" (:diagnostic data)) (throw failure))
                data))))
        ordered))

(defn check-ordered-registry! [state registry ordered]
  (let [refs (vec (mapcat (fn [[id definition]]
                            (candidate-refs state id definition)) ordered))]
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
    (let [shape-faults (vec (registry-shape-faults state ordered))
          high-fault (ranked-fault
                       (filter #(contains? #{"E-TYPE" "E-KEYSET"}
                                           (:diagnostic %))
                               shape-faults))
          _ (when high-fault
              (config/fail! (:diagnostic high-fault) (:path high-fault)))
          shape-fault (ranked-fault shape-faults)
          _ (when (and (empty? refs) shape-fault)
              (config/fail! (:diagnostic shape-fault) (:path shape-fault)))
          _ (swap! state assoc :registry-graph-built? true)
          graph (reduce (fn [result [id _]] (assoc result id [])) {} ordered)
          graph (reduce (fn [result [id _ ref]] (update result id conj ref))
                        graph refs)]
      (let [colors (atom {}) heights (atom {})]
        (letfn [(visit [id]
                  (case (get @colors id)
                    :gray (config/fail! "E-CYCLE" (config/path-of id))
                    :black (get @heights id)
                    (do
                      (budget/acquire! state :frames 1 (config/path-of id))
                      (try
                        (swap! colors assoc id :gray)
                        (let [children (get graph id)
                              child-height
                              (loop [index 0 maximum -1]
                                (if (= index (count children)) maximum
                                    (recur (inc index)
                                           (max maximum
                                                (visit (nth children index))))))
                              height (inc child-height)]
                          (when (> height (:depth config/limits))
                            (config/fail! "E-BOUND" (config/path-of id)))
                          (swap! heights assoc id height)
                          (swap! colors assoc id :black)
                          height)
                        (finally (budget/release! state :frames 1))))))]
          (doseq [[id _] ordered] (visit id))))
      (when shape-fault
        (config/fail! (:diagnostic shape-fault) (:path shape-fault)))
      graph)))

(defn check-registry! [state registry]
  (when (> (count registry) (:schemas config/limits))
    (config/fail! "E-BOUND" nil))
  (ordering/ordered-entries
    state registry nil
    (fn [entries]
      (budget/work! state (count registry) nil)
      (check-ordered-registry! state registry entries))))
