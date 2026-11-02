(ns gravity.self-hosting.a1-canonical-schema
  "Bounded Clojure seed kernel for the accepted A1 canonical-schema decision.

  This compatibility facade preserves the original public API and private test
  seams while bounded semantic components own the implementation."
  (:require [gravity.self-hosting.a1-canonical-schema.budget :as budget-impl]
            [gravity.self-hosting.a1-canonical-schema.canonical :as canonical]
            [gravity.self-hosting.a1-canonical-schema.config :as config]
            [gravity.self-hosting.a1-canonical-schema.digest :as digest]
            [gravity.self-hosting.a1-canonical-schema.execution :as execution]
            [gravity.self-hosting.a1-canonical-schema.ordering :as ordering]
            [gravity.self-hosting.a1-canonical-schema.schema :as schema]
            [gravity.self-hosting.a1-canonical-schema.validation :as validation]))

;; Preserve every original private Var for leaf-level seam discovery.
(def ^:private limits config/limits)
(def ^:private terminal-work config/terminal-work)
(def ^:private terminal-bytes config/terminal-bytes)
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
(def ^{:private true :arglists '([])} budget budget-impl/budget)
(def ^{:private true :arglists '([state counter quantity path])}
  reserve! budget-impl/reserve!)
(def ^{:private true :arglists '([state counter quantity])}
  commit! budget-impl/commit!)
(def ^{:private true :arglists '([state counter quantity])}
  release-reservation! budget-impl/release-reservation!)
(def ^{:private true :arglists '([state counter quantity path])}
  charge! budget-impl/charge!)
(def ^{:private true :arglists '([state counter quantity path])}
  acquire! budget-impl/acquire!)
(def ^{:private true :arglists '([state counter quantity])}
  release! budget-impl/release!)
(def ^{:private true :arglists '([state quantity path])} work! budget-impl/work!)
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
(def ^{:private true :arglists '([state value path consume])}
  ordered-entries ordering/ordered-entries)
(def ^{:private true :arglists '([state value path counter])}
  meter-string! canonical/meter-string!)
(def ^{:private true :arglists '([path])}
  path-payload-size budget-impl/path-payload-size)
(def ^{:private true :arglists '([state diagnostic path])}
  emit-rejection! budget-impl/emit-rejection!)
(def ^{:private true :arglists '([state value path depth counter])}
  meter-value! canonical/meter-value!)
(def ^{:private true :arglists '([value])} canonical-uint config/canonical-uint)

(def ^:private construct-vector (fn [items] (vec items)))
(def ^:private construct-map
  (fn [entries]
    (reduce (fn [result [key value]] (assoc result key value))
            clojure.lang.PersistentHashMap/EMPTY entries)))
(def ^:dynamic ^:private *audit-sink* nil)

(def ^{:private true :arglists '([state value path depth])}
  copy-value! canonical/copy-value!)
(def ^{:private true :arglists '([value path])} schema-id! schema/schema-id!)
(def ^{:private true :arglists '([definition allowed required path])}
  exact-fields! schema/exact-fields!)
(def ^{:private true :arglists '([definition name path])}
  boolean-field! schema/boolean-field!)
(def ^{:private true :arglists '([definition name maximum path])}
  uint-field! schema/uint-field!)
(def ^{:private true :arglists '([state values path])}
  ensure-unique! schema/ensure-unique!)
(def ^{:private true :arglists '([state definition path])}
  refs-in-definition! schema/refs-in-definition!)
(def ^{:private true :arglists '([state id definition])}
  candidate-refs schema/candidate-refs)
(def ^:private diagnostic-rank schema/diagnostic-rank)
(def ^{:private true :arglists '([faults])} ranked-fault schema/ranked-fault)
(def ^{:private true :arglists '([state ordered])}
  registry-shape-faults schema/registry-shape-faults)
(def ^{:private true :arglists '([state registry ordered])}
  check-ordered-registry! schema/check-ordered-registry!)
(def ^{:private true :arglists '([state registry])}
  check-registry! schema/check-registry!)
(def ^{:private true :arglists '([state digest value path])}
  digest-byte! digest/digest-byte!)
(def ^{:private true :arglists '([state digest value path])}
  digest-u32! digest/digest-u32!)
(def ^{:private true :arglists '([state digest value path])}
  digest-u64! digest/digest-u64!)
(def ^{:private true :arglists '([state digest value path])}
  digest-string! digest/digest-string!)
(def ^{:private true :arglists '([state digest value path])}
  digest-value! digest/digest-value!)
(def ^{:private true :arglists '([state value path])}
  value-digest! digest/value-digest!)
(def ^{:private true :arglists '([state left right path])}
  canonical-equal! digest/canonical-equal!)
(def ^{:private true :arglists '([state registry definition value path depth])}
  validate-array! validation/validate-array!)
(def ^{:private true :arglists '([state registry definition value path depth])}
  validate-object! validation/validate-object!)
(def ^{:private true :arglists '([state registry definition value path depth])}
  validate-tagged! validation/validate-tagged!)
(def ^{:private true :arglists '([state registry schema-id value path depth])}
  validate-value! validation/validate-value!)
(def ^{:private true :arglists '([left right path])}
  checked-size canonical/checked-size)
(def ^{:private true :arglists '([state value path depth])}
  measure-value! canonical/measure-value!)
(def ^{:private true :arglists '([state value])}
  finish-copy! canonical/finish-copy!)
(def ^{:private true :arglists '([state])}
  finalize-terminal! budget-impl/finalize-terminal!)

(defn- execute [operation args]
  (execution/execute operation args *audit-sink* construct-vector construct-map))

(defn canonical-copy [& args]
  (execute :copy args))

(defn admit-schema-registry [& args]
  (execute :registry args))

(defn validate-and-copy [& args]
  (execute :validate args))
