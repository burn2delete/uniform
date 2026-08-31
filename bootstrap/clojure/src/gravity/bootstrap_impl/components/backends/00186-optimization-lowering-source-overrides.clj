

(declare optimization-lowering-source-overrides
         optimization-lowering-fail!
         optimization-pass-contract-record
         optimization-decision-record
         optimization-lowering-validate-overrides!
         optimization-lowering-validate!
         optimization-lowering-capability-proof
         optimization-lowering-source-artifact)

(defn- optimization-lowering-ops []
  {:fail! fail!
   :source-span source-span
   :sha256-hex sha256-hex
   :perf-present? perf-present?
   :checked-core-source-artifact checked-core-source-artifact
   :domain-ir-source-artifact domain-ir-source-artifact
   :c13-optimization-diagnostic-ids c13-optimization-diagnostic-ids
   :c14-lowering-diagnostic-ids c14-lowering-diagnostic-ids
   :optimization-lowering-diagnostic-ids optimization-lowering-diagnostic-ids
   :optimization-lowering-diagnostic-messages optimization-lowering-diagnostic-messages
   :optimization-lowering-override-diagnostics optimization-lowering-override-diagnostics
   :optimization-pass-contract-seed optimization-pass-contract-seed
   :optimization-lowering-source-overrides optimization-lowering-source-overrides
   :optimization-lowering-fail! optimization-lowering-fail!
   :optimization-pass-contract-record optimization-pass-contract-record
   :optimization-decision-record optimization-decision-record
   :optimization-lowering-validate-overrides! optimization-lowering-validate-overrides!
   :optimization-lowering-validate! optimization-lowering-validate!
   :optimization-lowering-capability-proof optimization-lowering-capability-proof
   :optimization-lowering-source-artifact optimization-lowering-source-artifact})

(def ^:private ^:dynamic *optimization-lowering-leaf-call?* false)
(defn- optimization-lowering-call [operation-key operation & args]
  (if *optimization-lowering-leaf-call?*
    (apply operation args)
    (binding [*optimization-lowering-leaf-call?* true]
      (optimization-lowering/with-operations
        (assoc (optimization-lowering-ops) operation-key operation)
        #(apply operation args)))))

(defn optimization-lowering-source-overrides
  [module]
  (optimization-lowering-call :optimization-lowering-source-overrides
                              optimization-lowering/optimization-lowering-source-overrides
                              module))

(defn optimization-lowering-fail!
  [id source-path artifact subject extra]
  (optimization-lowering-call :optimization-lowering-fail!
                              optimization-lowering/optimization-lowering-fail!
                              id source-path artifact subject extra))