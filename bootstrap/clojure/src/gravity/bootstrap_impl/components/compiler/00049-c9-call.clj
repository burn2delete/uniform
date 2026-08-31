
(defn- c9-call [operation & args]
  (if *c9-leaf-call?*
    (apply operation args)
    (binding [*c9-leaf-call?* true]
      (c9/with-operations (c9-ownership-ops)
        #(apply operation args)))))

(defn c9-ownership-source-overrides
  [module]
  (c9-call c9/c9-ownership-source-overrides module))

(defn c9-ownership-message
  [id]
  (c9-call c9/c9-ownership-message id))

(defn c9-ownership-fail!
  [id source-path subject extra]
  (c9-call c9/c9-ownership-fail! id source-path subject extra))

(defn c9-ownership-validate-overrides!
  [source-path module overrides]
  (c9-call c9/c9-ownership-validate-overrides! source-path module overrides))

(defn c9-node-ids
  [effect-graph]
  (c9-call c9/c9-node-ids effect-graph))

(defn c9-node
  [node-ids index fallback]
  (c9-call c9/c9-node node-ids index fallback))

(defn c9-ownership-graph
  [module effect-graph]
  (c9-call c9/c9-ownership-graph module effect-graph))

(defn c9-borrow-graph
  [module effect-graph]
  (c9-call c9/c9-borrow-graph module effect-graph))

(defn c9-lifetime-interval-map
  [module]
  (c9-call c9/c9-lifetime-interval-map module))

(defn c9-escape-analysis-report
  [module]
  (c9-call c9/c9-escape-analysis-report module))

(defn c9-region-lifetime-graph
  [module]
  (c9-call c9/c9-region-lifetime-graph module))

(defn c9-arena-generation-graph
  [module]
  (c9-call c9/c9-arena-generation-graph module))

(defn c9-linear-resource-flow-graph
  [module]
  (c9-call c9/c9-linear-resource-flow-graph module))

(defn c9-transfer-records
  [module]
  (c9-call c9/c9-transfer-records module))

(defn c9-runtime-check-records
  [module]
  (c9-call c9/c9-runtime-check-records module))

(defn c9-unsafe-audit-references
  [module]
  (c9-call c9/c9-unsafe-audit-references module))

(defn c9-ownership-diagnostics
  [source-path ownership]
  (c9-call c9/c9-ownership-diagnostics source-path ownership))

(defn c9-linear-paths-exact?
  [linear]
  (c9-call c9/c9-linear-paths-exact? linear))

(defn c9-ownership-verifier-report
  [c8-artifact ownership borrow lifetimes moves escape region arena linear transfer runtime unsafe diagnostics]
  (c9-call c9/c9-ownership-verifier-report c8-artifact ownership borrow lifetimes moves escape region arena linear transfer runtime unsafe diagnostics))

(defn c9-ownership-capability-proof
  [artifact]
  (c9-call c9/c9-ownership-capability-proof artifact))

(defn c9-ownership-validate!
  [source-path artifact]
  (c9-call c9/c9-ownership-validate! source-path artifact))

(defn compiler-c9-ownership-source-artifact
  [source-path source-text]
  (c9-call c9/compiler-c9-ownership-source-artifact source-path source-text))

(defn compiler-c9-ownership-file-artifact
  [path]
  (c9-call c9/compiler-c9-ownership-file-artifact path))

(def c10-safety-diagnostic-ids c10/c10-safety-diagnostic-ids)
(def c10-safety-governing-document c10/c10-safety-governing-document)
(def c10-safety-rejected-designs c10/c10-safety-rejected-designs)
(def c10-safety-override-diagnostics c10/c10-safety-override-diagnostics)
(def c10-safe-outcomes c10/c10-safe-outcomes)

(declare c10-safety-source-overrides
         c10-safety-message
         c10-safety-fail!
         c10-safety-validate-overrides!
         c10-safety-operation-inventory
         c10-safety-outcome-records
         c10-runtime-check-list
         c10-proof-obligation-list
         c10-proof-certificate-references
         c10-unsafe-island-audit-manifest
         c10-taint-capability-safety-report
         c10-generated-code-safety-provenance
         c10-optimization-safety-preservation
         c10-safety-diagnostics
         c10-safety-verifier-report
         c10-safety-capability-proof
         c10-safety-validate!
         compiler-c10-safety-source-artifact
         compiler-c10-safety-file-artifact)

(defn- c10-safety-ops []
  {:fail! fail!
   :source-span source-span
   :c4-artifact-id c4-artifact-id
   :read-source-form-records read-source-form-records
   :validate-ns-syntax! validate-ns-syntax!
   :parse-module parse-module
   :compiler-c9-ownership-source-artifact compiler-c9-ownership-source-artifact
   :c10-safety-diagnostic-ids c10-safety-diagnostic-ids
   :c10-safety-governing-document c10-safety-governing-document
   :c10-safety-rejected-designs c10-safety-rejected-designs
   :c10-safety-override-diagnostics c10-safety-override-diagnostics
   :c10-safe-outcomes c10-safe-outcomes
   :c10-safety-source-overrides c10-safety-source-overrides
   :c10-safety-message c10-safety-message
   :c10-safety-fail! c10-safety-fail!
   :c10-safety-validate-overrides! c10-safety-validate-overrides!
   :c10-safety-operation-inventory c10-safety-operation-inventory
   :c10-safety-outcome-records c10-safety-outcome-records
   :c10-runtime-check-list c10-runtime-check-list
   :c10-proof-obligation-list c10-proof-obligation-list
   :c10-proof-certificate-references c10-proof-certificate-references
   :c10-unsafe-island-audit-manifest c10-unsafe-island-audit-manifest
   :c10-taint-capability-safety-report c10-taint-capability-safety-report
   :c10-generated-code-safety-provenance c10-generated-code-safety-provenance
   :c10-optimization-safety-preservation c10-optimization-safety-preservation
   :c10-safety-diagnostics c10-safety-diagnostics
   :c10-safety-verifier-report c10-safety-verifier-report
   :c10-safety-capability-proof c10-safety-capability-proof
   :c10-safety-validate! c10-safety-validate!
   :compiler-c10-safety-source-artifact compiler-c10-safety-source-artifact
   :compiler-c10-safety-file-artifact compiler-c10-safety-file-artifact})