(def ^:private __gravity_bootstrap_lexical_119416_invoke-checked-core-verifier
  (let [opaque-c11-upstream-diagnostic-owner (nth __gravity_bootstrap_lexical_values_119416 1)
        invoke-checked-core-verifier! __gravity_bootstrap_lexical_119416_invoke-checked-core-verifier]
    (fn invoke-checked-core-verifier!
      [checked-core context ingress-mode boundary-prefix]
      (let [boundary
            (fn [suffix]
              (keyword (str (name boundary-prefix) "-" suffix)))]
        (binding [*p15-s23-c11-upstream-diagnostic-owner*
                  opaque-c11-upstream-diagnostic-owner]
          (try
            (case ingress-mode
              :gravity-source-pure
              (p15-s23-stage2-gravity-checked-core-verification-report
               checked-core context)

              :effectful-reference
              (p15-s23-stage2-closed-checked-core-verification-report
               checked-core context))
            (catch InterruptedException interrupted
              (.interrupt (Thread/currentThread))
              (throw interrupted))
            (catch StackOverflowError error
              (p15-s23-c11-mir-contain-checked-core-exception!
               (:source-path context) (boundary "host-stack") error))
            (catch AssertionError error
              (p15-s23-c11-mir-contain-checked-core-exception!
               (:source-path context) (boundary "assertion") error))
            (catch LinkageError error
              (p15-s23-c11-mir-contain-checked-core-exception!
               (:source-path context) (boundary "linkage") error))
            (catch clojure.lang.ExceptionInfo exception
              (p15-s23-c11-mir-contain-checked-core-exception!
               (:source-path context) (boundary "diagnostic") exception))
            (catch Exception exception
              (p15-s23-c11-mir-contain-checked-core-exception!
               (:source-path context)
               (boundary "host-failure") exception))))))))
