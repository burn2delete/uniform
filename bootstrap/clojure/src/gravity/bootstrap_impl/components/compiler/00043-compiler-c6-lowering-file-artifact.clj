

(defn compiler-c6-lowering-file-artifact
  [path]
  (compiler-c6-lowering-source-artifact path (slurp path)))

(def c7-type-diagnostic-ids c7/c7-type-diagnostic-ids)
(def c7-type-governing-document c7/c7-type-governing-document)
(def c7-type-rejected-designs c7/c7-type-rejected-designs)
(def c7-type-override-diagnostics c7/c7-type-override-diagnostics)

(declare c7-type-source-overrides
         c7-type-message
         c7-type-fail!
         c7-type-validate-overrides!
         c7-literal-type
         c7-node-operator
         c7-node-type
         c7-type-fact
         c7-type-environment
         c7-constraint-ledger
         c7-function-table
         c7-dynamic-boundary-records
         c7-cast-records
         c7-generic-instantiations
         c7-protocol-dispatch-table
         c7-schema-links
         c7-layout-facts
         c7-type-diagnostics
         c7-typed-core-verifier-report
         c7-type-capability-proof
         c7-type-validate!
         compiler-c7-type-source-artifact
         compiler-c7-type-file-artifact)

(defn- c7-type-ops
  []
  {:fail! fail!
   :source-span source-span
   :c4-artifact-id c4-artifact-id
   :read-source-form-records read-source-form-records
   :validate-ns-syntax! validate-ns-syntax!
   :parse-module parse-module
   :compiler-c6-lowering-source-artifact
   compiler-c6-lowering-source-artifact
   :c7-type-diagnostic-ids c7-type-diagnostic-ids
   :c7-type-governing-document c7-type-governing-document
   :c7-type-rejected-designs c7-type-rejected-designs
   :c7-type-override-diagnostics c7-type-override-diagnostics
   :c7-type-source-overrides c7-type-source-overrides
   :c7-type-message c7-type-message
   :c7-type-fail! c7-type-fail!
   :c7-type-validate-overrides! c7-type-validate-overrides!
   :c7-literal-type c7-literal-type
   :c7-node-operator c7-node-operator
   :c7-node-type c7-node-type
   :c7-type-fact c7-type-fact
   :c7-type-environment c7-type-environment
   :c7-constraint-ledger c7-constraint-ledger
   :c7-function-table c7-function-table
   :c7-dynamic-boundary-records c7-dynamic-boundary-records
   :c7-cast-records c7-cast-records
   :c7-generic-instantiations c7-generic-instantiations
   :c7-protocol-dispatch-table c7-protocol-dispatch-table
   :c7-schema-links c7-schema-links
   :c7-layout-facts c7-layout-facts
   :c7-type-diagnostics c7-type-diagnostics
   :c7-typed-core-verifier-report c7-typed-core-verifier-report
   :c7-type-capability-proof c7-type-capability-proof
   :c7-type-validate! c7-type-validate!
   :compiler-c7-type-source-artifact compiler-c7-type-source-artifact
   :compiler-c7-type-file-artifact compiler-c7-type-file-artifact})