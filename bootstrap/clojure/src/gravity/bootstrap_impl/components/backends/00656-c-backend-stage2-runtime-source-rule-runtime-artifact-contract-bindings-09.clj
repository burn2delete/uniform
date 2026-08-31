(defn- __gravity_bootstrap_runtime_source_rule_runtime_artifact_contract_bindings_09 [state]
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
          runtime-artifact-hash]} state
        runtime-artifact-valid? (and
                                  (map? runtime-artifact-plan)
                                  (=
                                    :gravity/stage2-hosted-core-compiled-plan
                                    (:kind runtime-artifact-plan))
                                  (=
                                    p15-s23-stage2-runtime-artifact-required-effects
                                    (get-in runtime-artifact-plan [:module :effects]))
                                  (=
                                    p15-s23-stage2-runtime-artifact-required-capabilities
                                    (get-in
                                      runtime-artifact-plan
                                      [:module :capabilities]))
                                  (=
                                    p15-s23-stage2-runtime-artifact-required-effects
                                    (:effects runtime-artifact-authoritative-module))
                                  (=
                                    p15-s23-stage2-runtime-artifact-required-capabilities
                                    (:capabilities
                                      runtime-artifact-authoritative-module))
                                  (=
                                    p15-s23-reference-runtime-source-provider-selections
                                    (:providers runtime-artifact-authoritative-module))
                                  (map? runtime-artifact-function)
                                  (map? runtime-artifact-concat-function)
                                  (map? runtime-artifact-println-function)
                                  (map? runtime-artifact-println-two-function)
                                  (map? runtime-artifact-closed-plan-function)
                                  (=
                                    (conj
                                      p15-s23-stage2-runtime-artifact-closed-plan-helper-functions
                                      p15-s23-stage2-runtime-artifact-closed-plan-function)
                                    (set (keys runtime-artifact-closed-functions)))
                                  (=
                                    p15-s23-stage2-runtime-artifact-expected-source-content-hash
                                    runtime-artifact-source-content-hash)
                                  (=
                                    p15-s23-stage2-runtime-artifact-expected-closed-function-hashes
                                    runtime-artifact-closed-function-hashes)
                                  (=
                                    p15-s23-reference-runtime-function-set
                                    (set (keys runtime-artifact-functions)))
                                  (=
                                    p15-s23-reference-runtime-expected-function-hashes
                                    runtime-artifact-function-hashes)
                                  (=
                                    :complete
                                    (:status runtime-contract-validation-record))
                                  (=
                                    p15-s23-reference-runtime-expected-derived-facts-hash
                                    runtime-contract-derived-facts-hash)
                                  (=
                                    p15-s23-stage2-runtime-artifact-expected-artifact-hash
                                    runtime-artifact-hash)
                                  (seq (:instructions runtime-artifact-function))
                                  (seq
                                    (:instructions runtime-artifact-concat-function))
                                  (seq
                                    (:instructions runtime-artifact-println-function))
                                  (seq
                                    (:instructions
                                      runtime-artifact-println-two-function))
                                  (=
                                    p15-s23-stage2-runtime-artifact-required-function-shape
                                    (select-keys
                                      runtime-artifact-function
                                      [:arity :params :instructions]))
                                  (=
                                    p15-s23-stage2-runtime-artifact-required-concat-function-shape
                                    (select-keys
                                      runtime-artifact-concat-function
                                      [:arity :params :instructions]))
                                  (=
                                    p15-s23-stage2-runtime-artifact-required-println-function-shape
                                    (select-keys
                                      runtime-artifact-println-function
                                      [:arity :params :instructions]))
                                  (=
                                    p15-s23-stage2-runtime-artifact-required-println-two-function-shape
                                    (select-keys
                                      runtime-artifact-println-two-function
                                      [:arity :params :instructions])))
        _ (when-not runtime-artifact-valid?
            (p15-s23-stage2-runtime-executor-fail!
              (if runtime-artifact-text "P15S23X002" "P15S23X001")
              runtime-artifact-source
              runtime-artifact-plan
              {:requested-source source-path,
               :target target,
               :missing-fields
               (if runtime-artifact-text
                 [:runtime-artifact-functions :runtime-artifact-plan-kind]
                 [:runtime-artifact-source]),
               :missing-fact
               (if runtime-artifact-text
                 :runtime-artifact-function-set
                 :runtime-artifact-source)}))
        source-content-hash (str "sha256:" (sha256-hex (:source-text source-data)))
        _ (when-not (=
                      p15-s23-stage2-compiler-expected-source-content-hash
                      source-content-hash)
            (p15-s23-stage2-runtime-executor-fail!
              "P15S23X002"
              compiler-source
              nil
              {:requested-source source-path,
               :target target,
               :missing-fact :stage2-compiler-source-content-hash,
               :expected-source-content-hash
               p15-s23-stage2-compiler-expected-source-content-hash,
               :observed-source-content-hash source-content-hash}))
        runtime-rule-hash (str
                            "sha256:"
                            (sha256-hex (pr-str (c-backend-canonical-value runtime))))
        kernel-rule-hash (str
                           "sha256:"
                           (sha256-hex (pr-str (c-backend-canonical-value kernel))))]
    (assoc
      state
      'runtime-artifact-valid?
      runtime-artifact-valid?
      '_
      _
      'source-content-hash
      source-content-hash
      '_
      _
      'runtime-rule-hash
      runtime-rule-hash
      'kernel-rule-hash
      kernel-rule-hash)))
