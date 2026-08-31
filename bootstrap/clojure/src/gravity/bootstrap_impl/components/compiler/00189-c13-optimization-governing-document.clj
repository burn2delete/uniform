

(def c13-optimization-governing-document c13/c13-optimization-governing-document)

(declare c13-optimization-source-overrides
         c13-optimization-validate-source-overrides!
         c13-optimization-diagnostic-catalog
         c13-optimization-validate!
         c13-optimization-capability-proof
         compiler-c13-optimization-source-artifact
         compiler-c13-optimization-file-artifact)

(defn- c13-optimization-ops []
  {:source-span source-span
   :c4-artifact-id c4-artifact-id
   :sha256-hex sha256-hex
   :read-source-form-records read-source-form-records
   :validate-ns-syntax! validate-ns-syntax!
   :parse-module parse-module
   :perf-present? perf-present?
   :compiler-c12-domain-ir-source-artifact compiler-c12-domain-ir-source-artifact
   :optimization-lowering-validate-overrides! optimization-lowering-validate-overrides!
   :optimization-pass-contract-record optimization-pass-contract-record
   :optimization-decision-record optimization-decision-record
   :optimization-lowering-fail! optimization-lowering-fail!
   :c13-optimization-governing-document c13-optimization-governing-document
   :c13-optimization-diagnostic-ids c13-optimization-diagnostic-ids
   :optimization-lowering-diagnostic-messages optimization-lowering-diagnostic-messages
   :optimization-pass-contract-seed optimization-pass-contract-seed
   :c13-optimization-source-overrides c13-optimization-source-overrides
   :c13-optimization-validate-source-overrides! c13-optimization-validate-source-overrides!
   :c13-optimization-diagnostic-catalog c13-optimization-diagnostic-catalog
   :c13-optimization-validate! c13-optimization-validate!
   :c13-optimization-capability-proof c13-optimization-capability-proof
   :compiler-c13-optimization-source-artifact compiler-c13-optimization-source-artifact
   :compiler-c13-optimization-file-artifact compiler-c13-optimization-file-artifact})