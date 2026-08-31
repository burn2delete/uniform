(ns gravity.c11-mir.policy)
(def
 function-operation-keys
 #{:compiler-c11-mir-source-artifact
   :c4-artifact-id
   :compiler-c10-safety-source-artifact
   :c11-mir-validate-overrides!
   :source-span
   :parse-module
   :c11-mir-operation
   :c11-data-flow-graph
   :validate-ns-syntax!
   :c11-present?
   :c11-domain-anchor-table
   :c11-family-effects
   :c11-mir-source-overrides
   :c11-mir-module-record
   :read-source-form-records
   :c11-mir-verifier-report
   :c11-mir-validate!
   :c11-mir-message
   :c11-family-opcode
   :c11-mir-diagnostics
   :c11-mir-capability-proof
   :compiler-c11-mir-file-artifact
   :c11-mir-fail!
   :fail!})
(def
 scalar-operation-keys
 #{:c11-mir-rejected-designs
   :c11-mir-override-diagnostics
   :c11-mir-required-operation-families
   :c11-mir-diagnostic-ids
   :c11-mir-governing-document})
(def
 operation-keys
 (into function-operation-keys scalar-operation-keys))
(defn
 unsupported
 [key]
 (fn
  [& _]
  (throw
   (ex-info
    (str "C11 leaf requires injected operation " key)
    {:operation key}))))
(defn- sv? [x] (and (vector? x) (seq x) (every? string? x)))
(defn- kv? [x] (and (vector? x) (seq x) (every? keyword? x)))
(defn- vm? [x] (and (vector? x) (every? map? x)))
(defn-
 ks?
 [x]
 (and (map? x) (every? (fn [[k v]] (and (keyword? k) (string? v))) x)))
(defn
 validate-operations!
 [operations]
 (when-not
  (map? operations)
  (throw
   (ex-info "C11 operation map must be a map" {:value operations})))
 (let
  [unknown
   (seq (remove operation-keys (keys operations)))
   invalid
   (seq
    (for
     [[k v]
      (select-keys operations function-operation-keys)
      :when
      (not (fn? v))]
     k))]
  (when
   unknown
   (throw
    (ex-info
     "C11 operation map contains unknown keys"
     {:unknown-keys (vec unknown), :allowed-keys operation-keys})))
  (when
   invalid
   (throw
    (ex-info
     "C11 function operation values must be functions"
     {:non-function-keys (vec invalid)}))))
 (doseq
  [[k p e]
   [[:c11-mir-diagnostic-ids sv? :non-empty-string-vector]
    [:c11-mir-governing-document
     (fn* [p1__138#] (and (string? p1__138#) (seq p1__138#)))
     :non-empty-string]
    [:c11-mir-required-operation-families
     kv?
     :non-empty-keyword-vector]
    [:c11-mir-rejected-designs vm? :vector-of-maps]
    [:c11-mir-override-diagnostics ks? :keyword-to-string-map]]
   :when
   (and (contains? operations k) (not (p (get operations k))))]
  (throw
   (ex-info
    "C11 scalar operation has an invalid shape"
    {:key k, :expected e, :actual (get operations k)})))
 operations)
(defn
 engine-contract
 [public-api]
 {:public-api public-api,
  :override-driven-diagnostics? true,
  :does-not-own
  [:canonical-c11-authority
   :source-authentication
   :type-effect-ownership-safety-authority
   :canonical-mir-verifier-authority
   :domain-ir-authority
   :optimization-authority
   :target-lowering-authority
   :backend-authority
   :proof-authority
   :equivalence
   :self-hosting
   :release
   :seed-retirement],
  :dependency-direction
  {:requires ['clojure.string 'gravity.digest],
   :forbids ['gravity.bootstrap 'gravity.diagnostics]},
  :operation-interposition
  {:accepted-keys operation-keys,
   :unknown-keys-rejected? true,
   :partial-overrides? true,
   :single-binding-per-top-level-call? true},
  :canonical-c11-authority? false,
  :artifact-inputs [:c10-safety-analysis-artifact :module-context],
  :owns
  [:hosted-stage0-c11-mir-construction
   :hosted-stage0-c11-artifact-projection],
  :mir-model-complete? false,
  :artifact-outputs
  [:mir-module
   :control-flow-graph
   :data-flow-graph
   :metadata-tables
   :source-origin-map
   :domain-anchor-table
   :mir-verifier-report
   :mir-diagnostics],
  :compatibility-only? true,
  :contract-boundary :hosted-stage0-c11-mir})
