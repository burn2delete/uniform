
(defn- c10-call [operation & args]
  (if *c10-leaf-call?*
    (apply operation args)
    (binding [*c10-leaf-call?* true]
      (c10/with-operations (c10-safety-ops)
        #(apply operation args)))))

(defn c10-safety-source-overrides
  [module]
  (c10-call c10/c10-safety-source-overrides module))

(defn c10-safety-message
  [id]
  (c10-call c10/c10-safety-message id))

(defn c10-safety-fail!
  [id source-path subject extra]
  (c10-call c10/c10-safety-fail! id source-path subject extra))

(defn c10-safety-validate-overrides!
  [source-path module overrides]
  (c10-call c10/c10-safety-validate-overrides! source-path module overrides))

(defn c10-safety-operation-inventory
  [module c9-artifact]
  (c10-call c10/c10-safety-operation-inventory module c9-artifact))

(defn c10-safety-outcome-records
  [module inventory]
  (c10-call c10/c10-safety-outcome-records module inventory))

(defn c10-runtime-check-list
  [module outcomes]
  (c10-call c10/c10-runtime-check-list module outcomes))

(defn c10-proof-obligation-list
  [module outcomes]
  (c10-call c10/c10-proof-obligation-list module outcomes))

(defn c10-proof-certificate-references
  [module]
  (c10-call c10/c10-proof-certificate-references module))

(defn c10-unsafe-island-audit-manifest
  [module outcomes]
  (c10-call c10/c10-unsafe-island-audit-manifest module outcomes))

(defn c10-taint-capability-safety-report
  [module]
  (c10-call c10/c10-taint-capability-safety-report module))

(defn c10-generated-code-safety-provenance
  [module]
  (c10-call c10/c10-generated-code-safety-provenance module))

(defn c10-optimization-safety-preservation
  [module]
  (c10-call c10/c10-optimization-safety-preservation module))

(defn c10-safety-diagnostics
  [source-path]
  (c10-call c10/c10-safety-diagnostics source-path))

(defn c10-safety-verifier-report
  [c9-artifact inventory outcomes checks obligations certificates unsafe report generated optimization diagnostics]
  (c10-call c10/c10-safety-verifier-report c9-artifact inventory outcomes checks obligations certificates unsafe report generated optimization diagnostics))

(defn c10-safety-capability-proof
  [artifact]
  (c10-call c10/c10-safety-capability-proof artifact))

(defn c10-safety-validate!
  [source-path artifact]
  (c10-call c10/c10-safety-validate! source-path artifact))

(defn compiler-c10-safety-source-artifact
  [source-path source-text]
  (c10-call c10/compiler-c10-safety-source-artifact source-path source-text))

(defn compiler-c10-safety-file-artifact
  [path]
  (c10-call c10/compiler-c10-safety-file-artifact path))

(def c11-mir-diagnostic-ids c11/c11-mir-diagnostic-ids)
(def c11-mir-governing-document c11/c11-mir-governing-document)
(def c11-mir-required-operation-families c11/c11-mir-required-operation-families)
(def c11-mir-rejected-designs c11/c11-mir-rejected-designs)
(def c11-mir-override-diagnostics c11/c11-mir-override-diagnostics)

(declare c11-mir-source-overrides
         c11-mir-message
         c11-mir-fail!
         c11-mir-validate-overrides!
         c11-family-opcode
         c11-family-effects
         c11-mir-operation
         c11-mir-module-record
         c11-data-flow-graph
         c11-domain-anchor-table
         c11-present?
         c11-mir-diagnostics
         c11-mir-verifier-report
         c11-mir-capability-proof
         c11-mir-validate!
         compiler-c11-mir-source-artifact
         compiler-c11-mir-file-artifact)

(defn- c11-mir-ops []
  {:fail! fail!
   :source-span source-span
   :c4-artifact-id c4-artifact-id
   :read-source-form-records read-source-form-records
   :validate-ns-syntax! validate-ns-syntax!
   :parse-module parse-module
   :compiler-c10-safety-source-artifact compiler-c10-safety-source-artifact
   :c11-mir-diagnostic-ids c11-mir-diagnostic-ids
   :c11-mir-governing-document c11-mir-governing-document
   :c11-mir-required-operation-families c11-mir-required-operation-families
   :c11-mir-rejected-designs c11-mir-rejected-designs
   :c11-mir-override-diagnostics c11-mir-override-diagnostics
   :c11-mir-source-overrides c11-mir-source-overrides
   :c11-mir-message c11-mir-message
   :c11-mir-fail! c11-mir-fail!
   :c11-mir-validate-overrides! c11-mir-validate-overrides!
   :c11-family-opcode c11-family-opcode
   :c11-family-effects c11-family-effects
   :c11-mir-operation c11-mir-operation
   :c11-mir-module-record c11-mir-module-record
   :c11-data-flow-graph c11-data-flow-graph
   :c11-domain-anchor-table c11-domain-anchor-table
   :c11-present? c11-present?
   :c11-mir-diagnostics c11-mir-diagnostics
   :c11-mir-verifier-report c11-mir-verifier-report
   :c11-mir-capability-proof c11-mir-capability-proof
   :c11-mir-validate! c11-mir-validate!
   :compiler-c11-mir-source-artifact compiler-c11-mir-source-artifact
   :compiler-c11-mir-file-artifact compiler-c11-mir-file-artifact})