(ns gravity.self-hosting.a1-canonical-schema
  "Clojure seed facade for the A1 canonical-schema implementation."
  (:require [gravity.self-hosting.a1-canonical-schema.config :as config]
            [gravity.self-hosting.a1-canonical-schema.digest :as digest]
            [gravity.self-hosting.a1-canonical-schema.execution :as execution]
            [gravity.self-hosting.a1-canonical-schema.ordering :as ordering]
            [gravity.self-hosting.a1-canonical-schema.schema :as schema]
            [gravity.self-hosting.a1-canonical-schema.validation :as validation]))

(def ^:private uint64-max config/uint64-max)
(def ^:private schema-id-pattern config/schema-id-pattern)
(def ^:private allowed-kinds config/allowed-kinds)
(def ^:private namespace-contract config/namespace-contract)

(def ^{:private true :arglists '([value])} accepted config/accepted)
(def ^{:private true :arglists '([diagnostic path])} rejected config/rejected)
(def ^{:private true :arglists '([& segments])} path-of config/path-of)
(def ^{:private true :arglists '([path segment])} path-child config/path-child)
(def ^{:private true :arglists '([path])} path-count config/path-count)
(def ^{:private true :arglists '([path index])} path-segment config/path-segment)
(def ^{:private true :arglists '([path])} path-vector config/path-vector)
(def ^{:private true :arglists '([diagnostic path])} fail! config/fail!)
(def ^{:private true :arglists '([value])} utf8-length config/utf8-length)
(def ^{:private true :arglists '([value])} scalar-string? config/scalar-string?)
(def ^{:private true :arglists '([value klass])} exact-class? config/exact-class?)
(def ^{:private true :arglists '([value])} canonical-map? config/canonical-map?)
(def ^{:private true :arglists '([value])} canonical-vector? config/canonical-vector?)
(def ^{:private true :arglists '([value path])} no-metadata! config/no-metadata!)
(def ^{:private true :arglists '([value])} uint64? config/uint64?)
(def ^{:private true :arglists '([left right])} byte-compare config/byte-compare)
(def ^{:private true :arglists '([left right])} segment-compare config/segment-compare)
(def ^{:private true :arglists '([left right])} path-compare config/path-compare)
(def ^{:private true :arglists '([paths])} first-path config/first-path)
(def ^{:private true :arglists '([entries source target n width])}
  merge-index-pass! ordering/merge-index-pass!)
(def ^{:private true :arglists '([entries order n])}
  apply-index-order! ordering/apply-index-order!)
(def ^{:private true :arglists '([entries n])}
  bottom-up-mergesort ordering/bottom-up-mergesort)
(def ^{:private true :arglists '([value path consume])}
  ordered-entries ordering/ordered-entries)
(def ^{:private true :arglists '([value])} canonical-uint config/canonical-uint)

(def ^:private construct-vector (fn [items] (vec items)))
(def ^:private construct-map
  (fn [entries]
    (reduce (fn [result [key value]] (assoc result key value))
            clojure.lang.PersistentHashMap/EMPTY entries)))

(def ^{:private true :arglists '([value path])} schema-id! schema/schema-id!)
(def ^{:private true :arglists '([definition allowed required path])}
  exact-fields! schema/exact-fields!)
(def ^{:private true :arglists '([definition name path])}
  boolean-field! schema/boolean-field!)
(def ^{:private true :arglists '([definition name path])}
  uint-field! schema/uint-field!)
(def ^{:private true :arglists '([values path])}
  ensure-unique! schema/ensure-unique!)
(def ^{:private true :arglists '([definition path])}
  refs-in-definition! schema/refs-in-definition!)
(def ^{:private true :arglists '([id definition])}
  candidate-refs schema/candidate-refs)
(def ^:private diagnostic-rank schema/diagnostic-rank)
(def ^{:private true :arglists '([faults])} ranked-fault schema/ranked-fault)
(def ^{:private true :arglists '([ordered])}
  registry-shape-faults schema/registry-shape-faults)
(def ^{:private true :arglists '([registry ordered])}
  check-ordered-registry! schema/check-ordered-registry!)
(def ^{:private true :arglists '([registry])}
  check-registry! schema/check-registry!)
(def ^{:private true :arglists '([digest value])}
  digest-byte! digest/digest-byte!)
(def ^{:private true :arglists '([digest value])}
  digest-u32! digest/digest-u32!)
(def ^{:private true :arglists '([digest value])}
  digest-u64! digest/digest-u64!)
(def ^{:private true :arglists '([digest value])}
  digest-string! digest/digest-string!)
(def ^{:private true :arglists '([digest value path])}
  digest-value! digest/digest-value!)
(def ^{:private true :arglists '([value path])}
  value-digest! digest/value-digest!)
(def ^{:private true :arglists '([left right path])}
  canonical-equal! digest/canonical-equal!)
(def ^{:private true :arglists '([registry definition value path])}
  validate-array! validation/validate-array!)
(def ^{:private true :arglists '([registry definition value path])}
  validate-object! validation/validate-object!)
(def ^{:private true :arglists '([registry definition value path])}
  validate-tagged! validation/validate-tagged!)
(def ^{:private true :arglists '([registry schema-id value path])}
  validate-value! validation/validate-value!)

(defn- execute [operation args]
  (execution/execute operation args construct-vector construct-map))

(defn canonical-copy [& args]
  (execute :copy args))

(defn admit-schema-registry [& args]
  (execute :registry args))

(defn validate-and-copy [& args]
  (execute :validate args))
