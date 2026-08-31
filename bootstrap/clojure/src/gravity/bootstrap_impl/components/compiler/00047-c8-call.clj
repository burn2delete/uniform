
(defn- c8-call [operation & args]
  (if *c8-leaf-call?*
    (apply operation args)
    (binding [*c8-leaf-call?* true]
      (c8/with-operations (c8-effect-ops)
        #(apply operation args)))))

(defn c8-effect-source-overrides
  [module]
  (c8-call c8/c8-effect-source-overrides module))

(defn c8-effect-message
  [id]
  (c8-call c8/c8-effect-message id))

(defn c8-effect-fail!
  [id source-path subject extra]
  (c8-call c8/c8-effect-fail! id source-path subject extra))

(defn c8-effect-validate-overrides!
  [source-path module overrides]
  (c8-call c8/c8-effect-validate-overrides! source-path module overrides))

(defn c8-fact-direct-effects
  [fact]
  (c8-call c8/c8-fact-direct-effects fact))

(defn c8-effectful-facts
  [type-facts]
  (c8-call c8/c8-effectful-facts type-facts))

(defn c8-effect-graph
  [module type-facts functions]
  (c8-call c8/c8-effect-graph module type-facts functions))

(defn c8-legality-records
  [module effect-graph]
  (c8-call c8/c8-legality-records module effect-graph))

(defn c8-capability-proof-records
  [module effect-graph]
  (c8-call c8/c8-capability-proof-records module effect-graph))

(defn c8-build-effect-log
  [module]
  (c8-call c8/c8-build-effect-log module))

(defn c8-replay-requirements
  [effect-graph]
  (c8-call c8/c8-replay-requirements effect-graph))

(defn c8-ordering-constraints
  [effect-graph]
  (c8-call c8/c8-ordering-constraints effect-graph))

(defn c8-residual-effect-report
  [effect-graph]
  (c8-call c8/c8-residual-effect-report effect-graph))

(defn c8-effect-diagnostics
  [source-path type-facts]
  (c8-call c8/c8-effect-diagnostics source-path type-facts))

(defn c8-effect-verifier-report
  [module effect-graph legality capability-proof build-log replay ordering
   residual diagnostics]
  (c8-call c8/c8-effect-verifier-report module effect-graph legality
           capability-proof build-log replay ordering residual diagnostics))

(defn c8-effect-capability-proof
  [artifact]
  (c8-call c8/c8-effect-capability-proof artifact))

(defn c8-effect-validate!
  [source-path artifact]
  (c8-call c8/c8-effect-validate! source-path artifact))

(defn compiler-c8-effect-source-artifact
  [source-path source-text]
  (c8-call c8/compiler-c8-effect-source-artifact source-path source-text))

(defn compiler-c8-effect-file-artifact
  [path]
  (c8-call c8/compiler-c8-effect-file-artifact path))

(def c9-ownership-diagnostic-ids c9/c9-ownership-diagnostic-ids)
(def c9-ownership-governing-document c9/c9-ownership-governing-document)
(def c9-ownership-rejected-designs c9/c9-ownership-rejected-designs)
(def c9-ownership-override-diagnostics c9/c9-ownership-override-diagnostics)

(declare c9-ownership-source-overrides
         c9-ownership-message
         c9-ownership-fail!
         c9-ownership-validate-overrides!
         c9-node-ids
         c9-node
         c9-ownership-graph
         c9-borrow-graph
         c9-lifetime-interval-map
         c9-escape-analysis-report
         c9-region-lifetime-graph
         c9-arena-generation-graph
         c9-linear-resource-flow-graph
         c9-transfer-records
         c9-runtime-check-records
         c9-unsafe-audit-references
         c9-ownership-diagnostics
         c9-linear-paths-exact?
         c9-ownership-verifier-report
         c9-ownership-capability-proof
         c9-ownership-validate!
         compiler-c9-ownership-source-artifact
         compiler-c9-ownership-file-artifact)

(defn- c9-ownership-ops []
  {:fail! fail!
   :source-span source-span
   :c4-artifact-id c4-artifact-id
   :read-source-form-records read-source-form-records
   :validate-ns-syntax! validate-ns-syntax!
   :parse-module parse-module
   :compiler-c8-effect-source-artifact compiler-c8-effect-source-artifact
   :c9-ownership-diagnostic-ids c9-ownership-diagnostic-ids
   :c9-ownership-governing-document c9-ownership-governing-document
   :c9-ownership-rejected-designs c9-ownership-rejected-designs
   :c9-ownership-override-diagnostics c9-ownership-override-diagnostics
   :c9-ownership-source-overrides c9-ownership-source-overrides
   :c9-ownership-message c9-ownership-message
   :c9-ownership-fail! c9-ownership-fail!
   :c9-ownership-validate-overrides! c9-ownership-validate-overrides!
   :c9-node-ids c9-node-ids
   :c9-node c9-node
   :c9-ownership-graph c9-ownership-graph
   :c9-borrow-graph c9-borrow-graph
   :c9-lifetime-interval-map c9-lifetime-interval-map
   :c9-escape-analysis-report c9-escape-analysis-report
   :c9-region-lifetime-graph c9-region-lifetime-graph
   :c9-arena-generation-graph c9-arena-generation-graph
   :c9-linear-resource-flow-graph c9-linear-resource-flow-graph
   :c9-transfer-records c9-transfer-records
   :c9-runtime-check-records c9-runtime-check-records
   :c9-unsafe-audit-references c9-unsafe-audit-references
   :c9-ownership-diagnostics c9-ownership-diagnostics
   :c9-linear-paths-exact? c9-linear-paths-exact?
   :c9-ownership-verifier-report c9-ownership-verifier-report
   :c9-ownership-capability-proof c9-ownership-capability-proof
   :c9-ownership-validate! c9-ownership-validate!
   :compiler-c9-ownership-source-artifact compiler-c9-ownership-source-artifact
   :compiler-c9-ownership-file-artifact compiler-c9-ownership-file-artifact})