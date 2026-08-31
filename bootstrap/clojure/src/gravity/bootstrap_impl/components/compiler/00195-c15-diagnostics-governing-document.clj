

(def c15-diagnostics-governing-document
  c15/c15-diagnostics-governing-document)
(def c15-diagnostics-diagnostic-ids c15/c15-diagnostics-diagnostic-ids)
(def c15-diagnostic-required-fields c15/c15-diagnostic-required-fields)

(declare c15-diagnostics-source-overrides
         c15-stable-diagnostic-id
         c15-diagnostics-fail!
         c15-diagnostics-validate-source-overrides!
         c15-diagnostic-record
         c15-diagnostic-catalog
         c15-diagnostics-validate!
         c15-diagnostics-capability-proof
         compiler-c15-diagnostics-source-artifact
         compiler-c15-diagnostics-file-artifact)

(defn- c15-diagnostics-ops []
  {:fail! fail!
   :source-span source-span
   :sha256-hex sha256-hex
   :c4-artifact-id c4-artifact-id
   :read-source-form-records read-source-form-records
   :validate-ns-syntax! validate-ns-syntax!
   :parse-module parse-module
   :compiler-c14-lowering-source-artifact
   compiler-c14-lowering-source-artifact
   :compiler-verification-diagnostic-messages
   compiler-verification-diagnostic-messages
   :compiler-verification-override-diagnostics
   compiler-verification-override-diagnostics
   :c15-diagnostics-governing-document c15-diagnostics-governing-document
   :c15-diagnostics-diagnostic-ids c15-diagnostics-diagnostic-ids
   :c15-diagnostic-required-fields c15-diagnostic-required-fields
   :c15-diagnostics-source-overrides c15-diagnostics-source-overrides
   :c15-stable-diagnostic-id c15-stable-diagnostic-id
   :c15-diagnostics-fail! c15-diagnostics-fail!
   :c15-diagnostics-validate-source-overrides!
   c15-diagnostics-validate-source-overrides!
   :c15-diagnostic-record c15-diagnostic-record
   :c15-diagnostic-catalog c15-diagnostic-catalog
   :c15-diagnostics-validate! c15-diagnostics-validate!
   :c15-diagnostics-capability-proof c15-diagnostics-capability-proof
   :compiler-c15-diagnostics-source-artifact
   compiler-c15-diagnostics-source-artifact
   :compiler-c15-diagnostics-file-artifact
   compiler-c15-diagnostics-file-artifact})