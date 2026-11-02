(ns gravity.self-hosting.a1-canonical-schema.validation
  "Value validation against admitted A1 schemas."
  (:require [gravity.self-hosting.a1-canonical-schema.budget :as budget]
            [gravity.self-hosting.a1-canonical-schema.config :as config]
            [gravity.self-hosting.a1-canonical-schema.ordering :as ordering]
            [gravity.self-hosting.a1-canonical-schema.schema :as schema]))

(declare validate-value!)

(defn validate-array! [state registry definition value path depth]
  (when-not (config/canonical-vector? value) (config/fail! "E-TYPE" path))
  (let [n (count value)]
    (when-not (<= (long (get definition "min-items")) n
                  (long (get definition "max-items")))
      (config/fail! "E-SCHEMA" path))
    (when (get definition "unique")
      (schema/ensure-unique! state value path))
    (doseq [[index item] (map-indexed vector value)]
      (validate-value! state registry (get definition "item") item
                       (config/path-child path index) (inc depth)))))

(defn validate-object! [state registry definition value path depth]
  (when-not (config/canonical-map? value) (config/fail! "E-TYPE" path))
  (let [required (get definition "required")
        optional (get definition "optional")]
    (when (or (not (every? #(contains? value %) (keys required)))
              (not (every? #(or (contains? required %)
                                (contains? optional %))
                           (keys value))))
      (config/fail! "E-KEYSET" path))
    (ordering/ordered-entries
      state value path
      (fn [entries]
        (doseq [[field item] entries]
          (validate-value! state registry
                           (or (get required field) (get optional field))
                           item (config/path-child path field) (inc depth)))))))

(defn validate-tagged! [state registry definition value path depth]
  (when-not (config/canonical-map? value) (config/fail! "E-TYPE" path))
  (let [tag-key (get definition "tag-key")]
    (when-not (and (= 2 (count value))
                   (contains? value tag-key) (contains? value "value"))
      (config/fail! "E-KEYSET" path))
    (let [tag (get value tag-key)]
      (when-not (config/exact-class? tag String)
        (config/fail! "E-TYPE" (config/path-child path tag-key)))
      (let [selected (get (get definition "variants") tag)]
        (when-not selected
          (config/fail! "E-SCHEMA" (config/path-child path tag-key)))
        (budget/work! state 1 path)
        (validate-value! state registry selected (get value "value")
                         (config/path-child path "value") (inc depth))))))

(defn validate-value! [state registry schema-id value path depth]
  (when (> depth (:depth config/limits)) (config/fail! "E-BOUND" path))
  (budget/acquire! state :frames 1 path)
  (try
    (budget/work! state 1 path)
    (let [definition (get registry schema-id)]
      (when-not definition (config/fail! "E-UNKNOWN-ID" path))
      (case (get definition "kind")
        "null" (when-not (nil? value) (config/fail! "E-TYPE" path))
        "boolean" (when-not (config/exact-class? value Boolean)
                    (config/fail! "E-TYPE" path))
        "uint64" (when-not (config/uint64? value) (config/fail! "E-TYPE" path))
        "string" (do
                   (when-not (config/exact-class? value String)
                     (config/fail! "E-TYPE" path))
                   (let [byte-count (config/utf8-length value)]
                     (when (> byte-count (long (get definition "max-bytes")))
                       (config/fail! "E-SCHEMA" path))
                     (when (and (get definition "ascii-only")
                                (some #(> (int %) 0x7f) value))
                       (config/fail! "E-SCHEMA" path))))
        "enum" (do
                 (when-not (config/exact-class? value String)
                   (config/fail! "E-TYPE" path))
                 (when-not (some #(= value %) (get definition "values"))
                   (config/fail! "E-SCHEMA" path)))
        "array" (validate-array! state registry definition value path depth)
        "object" (validate-object! state registry definition value path depth)
        "tagged-union" (validate-tagged! state registry definition value path depth)))
    (finally (budget/release! state :frames 1))))
