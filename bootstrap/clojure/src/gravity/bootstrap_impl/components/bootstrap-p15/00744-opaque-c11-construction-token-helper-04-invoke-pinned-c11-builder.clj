(def ^:private __gravity_bootstrap_lexical_119416_invoke-pinned-c11-builder
  (let [opaque-c11-construction-token (nth __gravity_bootstrap_lexical_values_119416 0)
        classify-checked-core-ingress-pair __gravity_bootstrap_lexical_119416_classify-checked-core-ingress-pair]
    (fn invoke-pinned-c11-builder!
      [checked-core context binding mode token]
      (when-not (identical? opaque-c11-construction-token token)
        (p15-s23-c11-mir-fail!
         "C11-VERIFY" (:source-path context) {}
         {:missing-fact :opaque-c11-construction-token}))
      (try
        (let [result
              (p15-s23-stage2-runtime-execute-function
               {:engine :gravity-c11-pinned-builder-host-runner
                :compiler-artifact-plan? true}
               (:plan binding)
               p15-s23-c11-mir-builder-function
               [checked-core])]
          (when (= :rejected (:status result))
            (p15-s23-c11-mir-fail!
             (if (and (= #{:status :diagnostic :missing-fact
                            :conditional-count}
                          (set (keys result)))
                      (= "C11-BLOCK" (:diagnostic result))
                      (= :bounded-single-conditional-cfg
                         (:missing-fact result)))
               "C11-BLOCK"
               "C11-VERIFY")
             (:source-path context) result
             {:missing-fact (or (:missing-fact result)
                                :gravity-c11-builder-rejection)
              :conditional-count (:conditional-count result)}))
          result)
        (catch InterruptedException interrupted
          (.interrupt (Thread/currentThread))
          (throw interrupted))
        (catch StackOverflowError error
          (p15-s23-c11-mir-contain-exception!
           (:source-path context)
           :contained-gravity-c11-builder-host-stack error))
        (catch AssertionError error
          (p15-s23-c11-mir-contain-exception!
           (:source-path context)
           :contained-gravity-c11-builder-assertion error))
        (catch LinkageError error
          (p15-s23-c11-mir-contain-exception!
           (:source-path context)
           :contained-gravity-c11-builder-linkage error))
        (catch clojure.lang.ExceptionInfo ex
          (p15-s23-c11-mir-contain-exception!
           (:source-path context)
           :contained-gravity-c11-builder-diagnostic ex))
        (catch Exception error
          (p15-s23-c11-mir-contain-exception!
           (:source-path context)
           :contained-gravity-c11-builder-host-failure error))))))
