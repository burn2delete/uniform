(defn- __gravity_bootstrap_runtime_source_rule_runtime_artifact_contract_bindings_08 [state]
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
          runtime-artifact-plan]} state
        _runtime-artifact-plan-bounds (p15-s23-reference-runtime-bounded-value!
                                        runtime-artifact-source
                                        target
                                        :runtime-artifact-plan
                                        runtime-artifact-plan)
        runtime-artifact-function (get-in
                                    runtime-artifact-plan
                                    [:functions
                                     p15-s23-stage2-runtime-artifact-function])
        runtime-artifact-concat-function (get-in
                                           runtime-artifact-plan
                                           [:functions
                                            p15-s23-stage2-runtime-artifact-concat-function])
        runtime-artifact-println-function (get-in
                                            runtime-artifact-plan
                                            [:functions
                                             p15-s23-stage2-runtime-artifact-println-function])
        runtime-artifact-println-two-function (get-in
                                                runtime-artifact-plan
                                                [:functions
                                                 p15-s23-stage2-runtime-artifact-println-two-function])
        runtime-artifact-closed-plan-function (get-in
                                                runtime-artifact-plan
                                                [:functions
                                                 p15-s23-stage2-runtime-artifact-closed-plan-function])
        runtime-artifact-closed-functions (select-keys
                                            (:functions runtime-artifact-plan)
                                            (conj
                                              p15-s23-stage2-runtime-artifact-closed-plan-helper-functions
                                              p15-s23-stage2-runtime-artifact-closed-plan-function))
        runtime-artifact-closed-function-hashes (into
                                                  (sorted-map)
                                                  (map
                                                    (fn
                                                      [[name definition]]
                                                      [name
                                                       (p15-s23-stage2-runtime-artifact-function-semantic-hash
                                                         definition)]))
                                                  runtime-artifact-closed-functions)
        runtime-artifact-functions (:functions runtime-artifact-plan)
        runtime-artifact-function-hashes (into
                                           (sorted-map)
                                           (map
                                             (fn [[name definition]] [name
                                                                      (p15-s23-stage2-runtime-artifact-function-semantic-hash
                                                                        definition)]))
                                           runtime-artifact-functions)
        runtime-contract-validation-record (p15-s23-reference-runtime-contract-validation!
                                             runtime-artifact-source
                                             target
                                             (:definitions runtime-contract-bundle)
                                             runtime-artifact-authoritative-module
                                             runtime-artifact-plan)
        runtime-contract-derived-facts-hash (p15-s23-reference-runtime-hash
                                              (:derived-contract-facts
                                                runtime-contract-validation-record))
        runtime-artifact-hash-input (c-backend-stage2-runtime-artifact-hash-input
                                      runtime-artifact-plan
                                      runtime-artifact-authoritative-module
                                      (:definitions runtime-contract-bundle)
                                      runtime-artifact-function-hashes
                                      (:derived-contract-facts
                                        runtime-contract-validation-record))
        runtime-artifact-hash (p15-s23-reference-runtime-hash
                                runtime-artifact-hash-input)]
    (assoc
      state
      '_runtime-artifact-plan-bounds
      _runtime-artifact-plan-bounds
      'runtime-artifact-function
      runtime-artifact-function
      'runtime-artifact-concat-function
      runtime-artifact-concat-function
      'runtime-artifact-println-function
      runtime-artifact-println-function
      'runtime-artifact-println-two-function
      runtime-artifact-println-two-function
      'runtime-artifact-closed-plan-function
      runtime-artifact-closed-plan-function
      'runtime-artifact-closed-functions
      runtime-artifact-closed-functions
      'runtime-artifact-closed-function-hashes
      runtime-artifact-closed-function-hashes
      'runtime-artifact-functions
      runtime-artifact-functions
      'runtime-artifact-function-hashes
      runtime-artifact-function-hashes
      'runtime-contract-validation-record
      runtime-contract-validation-record
      'runtime-contract-derived-facts-hash
      runtime-contract-derived-facts-hash
      'runtime-artifact-hash-input
      runtime-artifact-hash-input
      'runtime-artifact-hash
      runtime-artifact-hash)))
