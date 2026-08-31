

(defn- c7-call
  [operation & args]
  (if *c7-leaf-call?*
    (apply operation args)
    (binding [*c7-leaf-call?* true]
      (c7/with-operations (c7-type-ops)
        #(apply operation args)))))

(defn c7-type-source-overrides
  [module]
  (c7-call c7/c7-type-source-overrides module))

(defn c7-type-message
  [id]
  (c7-call c7/c7-type-message id))

(defn c7-type-fail!
  [id source-path subject extra]
  (c7-call c7/c7-type-fail! id source-path subject extra))

(defn c7-type-validate-overrides!
  [source-path module overrides]
  (c7-call c7/c7-type-validate-overrides! source-path module overrides))

(defn c7-literal-type
  [value]
  (c7-call c7/c7-literal-type value))

(defn c7-node-operator
  [node]
  (c7-call c7/c7-node-operator node))

(defn c7-node-type
  [node]
  (c7-call c7/c7-node-type node))

(defn c7-type-fact
  [node]
  (c7-call c7/c7-type-fact node))

(defn c7-type-environment
  [type-facts]
  (c7-call c7/c7-type-environment type-facts))

(defn c7-constraint-ledger
  [type-facts]
  (c7-call c7/c7-constraint-ledger type-facts))

(defn c7-function-table
  [nodes]
  (c7-call c7/c7-function-table nodes))

(defn c7-dynamic-boundary-records
  [nodes module]
  (c7-call c7/c7-dynamic-boundary-records nodes module))

(defn c7-cast-records
  [nodes]
  (c7-call c7/c7-cast-records nodes))

(defn c7-generic-instantiations
  [nodes]
  (c7-call c7/c7-generic-instantiations nodes))

(defn c7-protocol-dispatch-table
  [nodes]
  (c7-call c7/c7-protocol-dispatch-table nodes))

(defn c7-schema-links
  [domain-boundaries]
  (c7-call c7/c7-schema-links domain-boundaries))

(defn c7-layout-facts
  [nodes]
  (c7-call c7/c7-layout-facts nodes))

(defn c7-type-diagnostics
  [source-path nodes]
  (c7-call c7/c7-type-diagnostics source-path nodes))

(defn c7-typed-core-verifier-report
  [nodes type-facts constraints functions dynamic cast generic dispatch schema layout]
  (c7-call c7/c7-typed-core-verifier-report nodes type-facts constraints functions dynamic cast generic dispatch schema layout))

(defn c7-type-capability-proof
  [artifact]
  (c7-call c7/c7-type-capability-proof artifact))

(defn c7-type-validate!
  [source-path artifact]
  (c7-call c7/c7-type-validate! source-path artifact))

(defn compiler-c7-type-source-artifact
  [source-path source-text]
  (c7-call c7/compiler-c7-type-source-artifact source-path source-text))

(defn compiler-c7-type-file-artifact
  [path]
  (c7-call c7/compiler-c7-type-file-artifact path))

(def c8-effect-diagnostic-ids c8/c8-effect-diagnostic-ids)
(def c8-effect-governing-document c8/c8-effect-governing-document)
(def c8-effect-rejected-designs c8/c8-effect-rejected-designs)
(def c8-effect-override-diagnostics c8/c8-effect-override-diagnostics)
(def c8-known-effects c8/c8-known-effects)
(def c8-effect-capability c8/c8-effect-capability)
(def c8-replay-sensitive-effects c8/c8-replay-sensitive-effects)

(declare c8-effect-source-overrides
         c8-effect-message
         c8-effect-fail!
         c8-effect-validate-overrides!
         c8-fact-direct-effects
         c8-effectful-facts
         c8-effect-graph
         c8-legality-records
         c8-capability-proof-records
         c8-build-effect-log
         c8-replay-requirements
         c8-ordering-constraints
         c8-residual-effect-report
         c8-effect-diagnostics
         c8-effect-verifier-report
         c8-effect-capability-proof
         c8-effect-validate!
         compiler-c8-effect-source-artifact
         compiler-c8-effect-file-artifact)

(defn- c8-effect-ops []
  {:fail! fail!
   :source-span source-span
   :c4-artifact-id c4-artifact-id
   :read-source-form-records read-source-form-records
   :validate-ns-syntax! validate-ns-syntax!
   :parse-module parse-module
   :compiler-c7-type-source-artifact compiler-c7-type-source-artifact
   :c8-effect-diagnostic-ids c8-effect-diagnostic-ids
   :c8-effect-governing-document c8-effect-governing-document
   :c8-effect-rejected-designs c8-effect-rejected-designs
   :c8-effect-override-diagnostics c8-effect-override-diagnostics
   :c8-known-effects c8-known-effects
   :c8-effect-capability c8-effect-capability
   :c8-replay-sensitive-effects c8-replay-sensitive-effects
   :c8-effect-source-overrides c8-effect-source-overrides
   :c8-effect-message c8-effect-message
   :c8-effect-fail! c8-effect-fail!
   :c8-effect-validate-overrides! c8-effect-validate-overrides!
   :c8-fact-direct-effects c8-fact-direct-effects
   :c8-effectful-facts c8-effectful-facts
   :c8-effect-graph c8-effect-graph
   :c8-legality-records c8-legality-records
   :c8-capability-proof-records c8-capability-proof-records
   :c8-build-effect-log c8-build-effect-log
   :c8-replay-requirements c8-replay-requirements
   :c8-ordering-constraints c8-ordering-constraints
   :c8-residual-effect-report c8-residual-effect-report
   :c8-effect-diagnostics c8-effect-diagnostics
   :c8-effect-verifier-report c8-effect-verifier-report
   :c8-effect-capability-proof c8-effect-capability-proof
   :c8-effect-validate! c8-effect-validate!
   :compiler-c8-effect-source-artifact compiler-c8-effect-source-artifact
   :compiler-c8-effect-file-artifact compiler-c8-effect-file-artifact})