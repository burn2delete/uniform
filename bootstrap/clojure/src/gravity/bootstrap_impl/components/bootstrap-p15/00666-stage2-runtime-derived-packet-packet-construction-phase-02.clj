(defn- __gravity_bootstrap_runtime_packet_packet_construction_phase_02 [state]
  (let [{:syms
         [source-path
          source-text
          requested-target
          validate-plan!
          stage2-rule
          stage2-runtime-rule
          stage2-driver-rule
          plan
          _
          raw-source-declared-target
          source-declared-target
          target-eligibility
          closed-plan-validation]} state
        compiler-artifact-record {:source-content-hash
                                  (get-in
                                    plan
                                    [:compiler
                                     :expression-lowering-source-content-hash]),
                                  :invoked?
                                  (true?
                                    (get-in
                                      plan
                                      [:compiler :expression-lowering-invoked?])),
                                  :plan-assembly-invoked?
                                  (true?
                                    (get-in plan [:compiler :plan-assembly-invoked?])),
                                  :plan-assembly-artifact-hash
                                  (get-in
                                    plan
                                    [:compiler :plan-assembly-artifact-hash]),
                                  :functions
                                  p15-s23-stage2-compiler-artifact-required-functions,
                                  :plan-assembly-generic-bridge-residual?
                                  (true?
                                    (get-in
                                      plan
                                      [:compiler
                                       :plan-assembly-generic-bridge-residual?])),
                                  :self-hosted? false,
                                  :semantic-hash
                                  (get-in
                                    plan
                                    [:compiler :expression-lowering-semantic-hash]),
                                  :plan-assembly-function
                                  (get-in plan [:compiler :plan-assembly-function]),
                                  :artifact
                                  :gravity/p15-s23-stage2-expression-lowering-binding,
                                  :plan-assembly-semantic-hash
                                  (get-in
                                    plan
                                    [:compiler :plan-assembly-semantic-hash]),
                                  :plan-assembly-source-content-hash
                                  (get-in
                                    plan
                                    [:compiler :plan-assembly-source-content-hash]),
                                  :artifact-hash
                                  (get-in
                                    plan
                                    [:compiler :expression-lowering-artifact-hash]),
                                  :clojure-seed-boundary? true,
                                  :generic-bridge-residual?
                                  (true?
                                    (get-in
                                      plan
                                      [:compiler
                                       :expression-lowering-generic-bridge-residual?]))}
        _ (when-not (and
                      (p15-s23-stage2-compiler-artifact-record-authentic?
                        compiler-artifact-record)
                      (p15-s23-stage2-compiler-artifact-record-matches-plan?
                        compiler-artifact-record
                        plan))
            (p15-s23-stage2-plan-emitter-fail!
              "P15S23Q002"
              source-path
              compiler-artifact-record
              {:target requested-target,
               :missing-fact :stage2-expression-lowering-packet-binding}))
        stage2-driver-run (try
                            (p15-s23-stage2-compiler-driver-run-source
                              (:driver stage2-driver-rule)
                              (:front-end stage2-driver-rule)
                              (:emitter stage2-rule)
                              (assoc
                                (:runtime stage2-runtime-rule)
                                :runtime-artifact-plan
                                (:runtime-artifact-plan stage2-runtime-rule)
                                :runtime-artifact-source-path
                                (:runtime-artifact-source-path stage2-runtime-rule)
                                :runtime-artifact-hash
                                (:runtime-artifact-hash stage2-runtime-rule))
                              source-path
                              source-text)
                            (catch clojure.lang.ExceptionInfo ex (throw ex))
                            (catch
                              StackOverflowError
                              ex
                              (p15-s23-stage2-runtime-executor-fail!
                                "P15S23X002"
                                source-path
                                nil
                                {:target requested-target,
                                 :cause-message (.getMessage ex),
                                 :missing-fact :closed-plan-bounds,
                                 :boundary :stage2-driver-execution,
                                 :maximum-depth p15-s23-closed-runtime-max-depth,
                                 :maximum-nodes p15-s23-closed-runtime-max-nodes}))
                            (catch
                              Exception
                              ex
                              (p15-s23-stage2-compiler-driver-fail!
                                "P15S23Y003"
                                source-path
                                nil
                                {:target requested-target,
                                 :driver-engine (:driver-engine stage2-driver-rule),
                                 :driver-rule-hash
                                 (:driver-rule-hash stage2-driver-rule),
                                 :cause-message (.getMessage ex),
                                 :missing-fact :stage2-driver-execution})))
        _ (when-not (and
                      (map? stage2-driver-run)
                      (= :complete (:status stage2-driver-run))
                      (true? (:accepted-output-equivalent? stage2-driver-run))
                      (map? (:stage2-plan stage2-driver-run))
                      (=
                        :complete
                        (get-in
                          stage2-driver-run
                          [:stage2-runtime-execution-record :status]))
                      (true? (:stage2-runtime-executed? stage2-driver-run)))
            (p15-s23-stage2-compiler-driver-fail!
              "P15S23Y003"
              source-path
              stage2-driver-run
              {:requested-source source-path,
               :target requested-target,
               :driver-engine (:driver-engine stage2-driver-rule),
               :driver-rule-hash (:driver-rule-hash stage2-driver-rule),
               :missing-fact :stage2-driver-execution-equivalence}))
        driver-plan (:stage2-plan stage2-driver-run)]
    (assoc
      state
      'compiler-artifact-record
      compiler-artifact-record
      '_
      _
      'stage2-driver-run
      stage2-driver-run
      '_
      _
      'driver-plan
      driver-plan)))
