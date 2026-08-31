

(def c12-domain-ir-governing-document c12/c12-domain-ir-governing-document)

(declare c12-domain-ir-source-overrides
         c12-domain-ir-validate-source-overrides!
         c12-domain-ir-diagnostic-catalog
         compiler-c12-domain-ir-source-artifact
         compiler-c12-domain-ir-file-artifact)

(defn- c12-domain-ir-ops []
  {:source-span source-span
   :c4-artifact-id c4-artifact-id
   :read-source-form-records read-source-form-records
   :validate-ns-syntax! validate-ns-syntax!
   :parse-module parse-module
   :compiler-c11-mir-source-artifact compiler-c11-mir-source-artifact
   :domain-ir-validate-overrides! domain-ir-validate-overrides!
   :domain-ir-registration-record domain-ir-registration-record
   :domain-ir-artifact-record domain-ir-artifact-record
   :domain-ir-validate! domain-ir-validate!
   :domain-ir-capability-proof domain-ir-capability-proof
   :c12-domain-ir-governing-document c12-domain-ir-governing-document
   :domain-ir-diagnostic-ids domain-ir-diagnostic-ids
   :domain-ir-diagnostic-messages domain-ir-diagnostic-messages
   :domain-ir-required-families domain-ir-required-families
   :domain-ir-registry-seed domain-ir-registry-seed
   :c12-domain-ir-source-overrides c12-domain-ir-source-overrides
   :c12-domain-ir-validate-source-overrides! c12-domain-ir-validate-source-overrides!
   :c12-domain-ir-diagnostic-catalog c12-domain-ir-diagnostic-catalog
   :compiler-c12-domain-ir-source-artifact compiler-c12-domain-ir-source-artifact
   :compiler-c12-domain-ir-file-artifact compiler-c12-domain-ir-file-artifact})