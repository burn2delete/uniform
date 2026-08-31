(defn- __gravity_bootstrap_runtime_source_rule_runtime_artifact_contract_bindings_07 [state]
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
          linked-kernel?]} state
        runtime-artifact-source (c-backend-stage2-runtime-artifact-source-path
                                  compiler-source)
        runtime-artifact-file (java.io.File. runtime-artifact-source)
        _missing-runtime-artifact (when-not (.isFile runtime-artifact-file)
                                    (p15-s23-stage2-runtime-executor-fail!
                                      "P15S23X001"
                                      runtime-artifact-source
                                      nil
                                      {:requested-source source-path,
                                       :target target,
                                       :missing-fields [:runtime-artifact-source],
                                       :missing-fact :runtime-artifact-source,
                                       :result-committed? false,
                                       :output-committed? false}))
        _runtime-artifact-source-byte-count (when-not (=
                                                        p15-s23-stage2-runtime-artifact-expected-source-byte-count
                                                        (.length
                                                          runtime-artifact-file))
                                              (p15-s23-reference-runtime-fail!
                                                runtime-artifact-source
                                                target
                                                :runtime-artifact-source-byte-count
                                                nil
                                                {:requested-source source-path,
                                                 :expected-byte-count
                                                 p15-s23-stage2-runtime-artifact-expected-source-byte-count,
                                                 :observed-byte-count
                                                 (.length runtime-artifact-file)}))
        runtime-artifact-bytes (try
                                 (java.nio.file.Files/readAllBytes
                                   (.toPath runtime-artifact-file))
                                 (catch
                                   Exception
                                   _
                                   (p15-s23-reference-runtime-fail!
                                     runtime-artifact-source
                                     target
                                     :runtime-artifact-source-bytes
                                     nil
                                     {:requested-source source-path})))
        runtime-artifact-source-content-hash (str
                                               "sha256:"
                                               (sha256-bytes-hex
                                                 runtime-artifact-bytes))
        _runtime-artifact-source-hash (when-not (=
                                                  p15-s23-stage2-runtime-artifact-expected-source-content-hash
                                                  runtime-artifact-source-content-hash)
                                        (p15-s23-reference-runtime-fail!
                                          runtime-artifact-source
                                          target
                                          :runtime-artifact-source-content-hash
                                          nil
                                          {:requested-source source-path,
                                           :expected-source-content-hash
                                           p15-s23-stage2-runtime-artifact-expected-source-content-hash,
                                           :observed-source-content-hash
                                           runtime-artifact-source-content-hash}))
        runtime-artifact-text (String.
                                runtime-artifact-bytes
                                java.nio.charset.StandardCharsets/UTF_8)
        runtime-artifact-source-data (try
                                       (p15-s23-compiler-source-form-record-from-text
                                         runtime-artifact-source
                                         runtime-artifact-text)
                                       (catch clojure.lang.ExceptionInfo ex (throw ex))
                                       (catch
                                         StackOverflowError
                                         ex
                                         (p15-s23-reference-runtime-fail!
                                           runtime-artifact-source
                                           target
                                           :runtime-artifact-source-form-bounds
                                           nil
                                           {:requested-source source-path,
                                            :cause-message (.getMessage ex),
                                            :maximum-depth
                                            p15-s23-reference-runtime-max-contract-depth,
                                            :maximum-nodes
                                            p15-s23-reference-runtime-max-contract-nodes}))
                                       (catch
                                         Exception
                                         ex
                                         (p15-s23-reference-runtime-fail!
                                           runtime-artifact-source
                                           target
                                           :runtime-artifact-source-forms
                                           nil
                                           {:requested-source source-path,
                                            :cause-message (.getMessage ex)})))
        runtime-artifact-authoritative-module (:module runtime-artifact-source-data)
        runtime-contract-bundle (p15-s23-reference-runtime-contract-definitions
                                  runtime-artifact-source
                                  target
                                  (:forms runtime-artifact-source-data))
        runtime-artifact-plan (try
                                (let [plan-emitter-rule (c-backend-stage2-plan-emitter-source-rule!
                                                          source-path
                                                          target)]
                                  (p15-s23-stage2-plan-emitter-compile-source
                                    (:emitter plan-emitter-rule)
                                    runtime-artifact-source
                                    runtime-artifact-text))
                                (catch clojure.lang.ExceptionInfo ex (throw ex))
                                (catch
                                  StackOverflowError
                                  ex
                                  (p15-s23-reference-runtime-fail!
                                    runtime-artifact-source
                                    target
                                    :runtime-artifact-plan-bounds
                                    nil
                                    {:requested-source source-path,
                                     :cause-message (.getMessage ex),
                                     :maximum-depth
                                     p15-s23-reference-runtime-max-instruction-depth,
                                     :maximum-nodes
                                     p15-s23-reference-runtime-max-contract-nodes}))
                                (catch
                                  Exception
                                  ex
                                  (p15-s23-reference-runtime-fail!
                                    runtime-artifact-source
                                    target
                                    :runtime-artifact-plan
                                    nil
                                    {:requested-source source-path,
                                     :cause-message (.getMessage ex)})))]
    (assoc
      state
      'runtime-artifact-source
      runtime-artifact-source
      'runtime-artifact-file
      runtime-artifact-file
      '_missing-runtime-artifact
      _missing-runtime-artifact
      '_runtime-artifact-source-byte-count
      _runtime-artifact-source-byte-count
      'runtime-artifact-bytes
      runtime-artifact-bytes
      'runtime-artifact-source-content-hash
      runtime-artifact-source-content-hash
      '_runtime-artifact-source-hash
      _runtime-artifact-source-hash
      'runtime-artifact-text
      runtime-artifact-text
      'runtime-artifact-source-data
      runtime-artifact-source-data
      'runtime-artifact-authoritative-module
      runtime-artifact-authoritative-module
      'runtime-contract-bundle
      runtime-contract-bundle
      'runtime-artifact-plan
      runtime-artifact-plan)))
