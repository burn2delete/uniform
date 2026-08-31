(defn- __gravity_bootstrap_runtime_source_rule_final_runtime_rule_record [state]
  (let [{:syms
         [source-path
          target
          compiler-source
          pinned-source
          source-data
          forms
          runtime
          kernel
          runtime-rule-record
          kernel-rule-record
          linked-kernel?
          runtime-artifact-source
          runtime-artifact-file
          _missing-runtime-artifact
          _runtime-artifact-source-byte-count
          runtime-artifact-bytes
          runtime-artifact-source-content-hash
          _runtime-artifact-source-hash
          runtime-artifact-text
          runtime-artifact-source-data
          runtime-artifact-authoritative-module
          runtime-contract-bundle
          runtime-artifact-plan
          _runtime-artifact-plan-bounds
          runtime-artifact-function
          runtime-artifact-concat-function
          runtime-artifact-println-function
          runtime-artifact-println-two-function
          runtime-artifact-closed-plan-function
          runtime-artifact-closed-functions
          runtime-artifact-closed-function-hashes
          runtime-artifact-functions
          runtime-artifact-function-hashes
          runtime-contract-validation-record
          runtime-contract-derived-facts-hash
          runtime-artifact-hash-input
          runtime-artifact-hash
          runtime-artifact-valid?
          _
          source-content-hash
          runtime-rule-hash
          kernel-rule-hash]} state]
    {:runtime-artifact-source-path runtime-artifact-source,
     :runtime-artifact-capabilities
     (get-in runtime-artifact-plan [:module :capabilities]),
     :runtime-engine (:engine runtime),
     :runtime-artifact-function-hashes runtime-artifact-function-hashes,
     :runtime-artifact-generic-bridge-residual? true,
     :runtime-contract-definition-hash (:definition-hash runtime-contract-bundle),
     :runtime-artifact-providers (:providers runtime-artifact-authoritative-module),
     :runtime-contract-validation-record runtime-contract-validation-record,
     :stage2-compiler-source-content-hash source-content-hash,
     :runtime-kernel-rule-hash kernel-rule-hash,
     :runtime-artifact-closed-plan-function
     p15-s23-stage2-runtime-artifact-closed-plan-function,
     :kernel-rule-record kernel-rule-record,
     :runtime-artifact-generic-emitter-effect-summary-credited? false,
     :runtime-source-path runtime-artifact-source,
     :runtime-rule-hash runtime-rule-hash,
     :runtime-source-content-hash runtime-artifact-source-content-hash,
     :stage2-compiler-source-path compiler-source,
     :runtime-artifact-authoritative-module runtime-artifact-authoritative-module,
     :runtime-artifact-closed-plan-helper-functions
     p15-s23-stage2-runtime-artifact-closed-plan-helper-functions,
     :runtime-contract-definitions (:definitions runtime-contract-bundle),
     :runtime-artifact-functions runtime-artifact-functions,
     :runtime-artifact-effects (get-in runtime-artifact-plan [:module :effects]),
     :runtime-rule-source
     {:kind :gravity-source,
      :sha256 source-content-hash,
      :stage2-compiler-source {:sha256 source-content-hash},
      :runtime-source {:sha256 runtime-artifact-source-content-hash},
      :runtime-rule-hash runtime-rule-hash,
      :runtime-kernel-rule-hash kernel-rule-hash,
      :runtime-artifact-source
      {:generic-emitter-effect-summary-credited? false,
       :println-function p15-s23-stage2-runtime-artifact-println-function,
       :concat-function p15-s23-stage2-runtime-artifact-concat-function,
       :closed-plan-function p15-s23-stage2-runtime-artifact-closed-plan-function,
       :closed-plan-helper-functions
       p15-s23-stage2-runtime-artifact-closed-plan-helper-functions,
       :closed-function-hashes runtime-artifact-closed-function-hashes,
       :function p15-s23-stage2-runtime-artifact-function,
       :closed-plan-function-hash
       (get
         runtime-artifact-closed-function-hashes
         p15-s23-stage2-runtime-artifact-closed-plan-function),
       :contract-definition-hash (:definition-hash runtime-contract-bundle),
       :sha256 runtime-artifact-source-content-hash,
       :println-two-function p15-s23-stage2-runtime-artifact-println-two-function,
       :derived-contract-facts-hash runtime-contract-derived-facts-hash,
       :function-hashes runtime-artifact-function-hashes,
       :println-over-two-boundary
       p15-s23-stage2-runtime-artifact-println-over-two-boundary,
       :contract-validation
       (dissoc runtime-contract-validation-record :derived-contract-facts),
       :artifact-hash runtime-artifact-hash,
       :generic-bridge-residual? true}},
     :runtime-artifact-hash-input runtime-artifact-hash-input,
     :runtime-artifact-closed-plan-function-hash
     (get
       runtime-artifact-closed-function-hashes
       p15-s23-stage2-runtime-artifact-closed-plan-function),
     :runtime runtime,
     :runtime-artifact-println-two-function
     p15-s23-stage2-runtime-artifact-println-two-function,
     :runtime-artifact-closed-function-hashes runtime-artifact-closed-function-hashes,
     :runtime-artifact-concat-function p15-s23-stage2-runtime-artifact-concat-function,
     :kernel kernel,
     :runtime-contract-derived-facts-hash runtime-contract-derived-facts-hash,
     :runtime-rule-record runtime-rule-record,
     :runtime-artifact-hash runtime-artifact-hash,
     :runtime-artifact-source-content-hash runtime-artifact-source-content-hash,
     :runtime-artifact-println-over-two-boundary
     p15-s23-stage2-runtime-artifact-println-over-two-boundary,
     :runtime-artifact-function p15-s23-stage2-runtime-artifact-function,
     :runtime-artifact-println-function
     p15-s23-stage2-runtime-artifact-println-function,
     :runtime-kernel-engine (:engine kernel),
     :runtime-artifact-plan runtime-artifact-plan}))
