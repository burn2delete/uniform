(defn- __gravity_bootstrap_runtime_source_rule_runtime_kernel_definitions_bindings_03 [state]
  (let [{:syms [source-path target compiler-source]} state
        pinned-source (p15-s23-stage2-compiler-pinned-source!
                        compiler-source
                        source-path
                        target
                        "P15S23X001"
                        p15-s23-stage2-runtime-executor-fail!)
        source-data (try
                      (p15-s23-compiler-source-form-record-from-text
                        compiler-source
                        (:source-text pinned-source))
                      (catch clojure.lang.ExceptionInfo ex (throw ex))
                      (catch
                        Exception
                        ex
                        (p15-s23-stage2-runtime-executor-fail!
                          "P15S23X001"
                          compiler-source
                          nil
                          {:requested-source source-path,
                           :target target,
                           :missing-fact :stage2-runtime-executor-source,
                           :cause-message (.getMessage ex)})))
        forms (:forms source-data)
        runtime (try
                  (p15-s23-compiler-def-value
                    compiler-source
                    forms
                    'p15-s23-stage2-runtime-executor)
                  (catch clojure.lang.ExceptionInfo ex (throw ex))
                  (catch
                    Exception
                    ex
                    (p15-s23-stage2-runtime-executor-fail!
                      "P15S23X001"
                      compiler-source
                      nil
                      {:requested-source source-path,
                       :target target,
                       :missing-fact :stage2-runtime-executor-definition,
                       :cause-message (.getMessage ex)})))
        kernel (try
                 (p15-s23-compiler-def-value
                   compiler-source
                   forms
                   'p15-s23-stage2-runtime-kernel)
                 (catch clojure.lang.ExceptionInfo ex (throw ex))
                 (catch
                   Exception
                   ex
                   (p15-s23-stage2-runtime-executor-fail!
                     "P15S23X001"
                     compiler-source
                     nil
                     {:requested-source source-path,
                      :target target,
                      :missing-fact :stage2-runtime-kernel-definition,
                      :cause-message (.getMessage ex)})))]
    (assoc
      state
      'pinned-source
      pinned-source
      'source-data
      source-data
      'forms
      forms
      'runtime
      runtime
      'kernel
      kernel)))
