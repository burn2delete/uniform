(defn- __gravity_bootstrap_runtime_packet_packet_construction_phase_04 [state]
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
          closed-plan-validation
          compiler-artifact-record
          stage2-driver-run
          driver-plan
          plan-shape
          driver-plan-shape
          runtime-adapter-output
          closed-plan-execution
          runtime-contract-adapter-record
          closed-plan-execution-hash
          closed-plan-invocation
          stage2-runtime-execution]} state
        reference-output (try
                           (execute-stage0-compiled-plan plan)
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
                                :boundary :stage0-oracle-execution,
                                :maximum-depth p15-s23-closed-runtime-max-depth,
                                :maximum-nodes p15-s23-closed-runtime-max-nodes}))
                           (catch
                             Exception
                             ex
                             (stage2-runtime-derived-packet-fail!
                               "C14-UNSUPPORTED"
                               "stage2 plan lacks closed hosted runtime semantics"
                               source-path
                               requested-target
                               nil
                               {:cause-message (.getMessage ex),
                                :missing-fact :closed-stage2-runtime-semantics})))
        _ (when-not (= (:stdout stage2-runtime-execution) reference-output)
            (p15-s23-stage2-runtime-executor-fail!
              "P15S23X003"
              source-path
              stage2-runtime-execution
              {:requested-source source-path,
               :target requested-target,
               :runtime-engine (:runtime-engine stage2-runtime-rule),
               :runtime-rule-hash (:runtime-rule-hash stage2-runtime-rule),
               :stage2-runtime-output (:stdout stage2-runtime-execution),
               :stage0-reference-output reference-output,
               :missing-fact :stage2-stage0-output-equivalence}))
        _ (when-not (and
                      (=
                        (:stdout closed-plan-execution)
                        (:stdout stage2-runtime-execution))
                      (= (:stdout closed-plan-execution) reference-output))
            (p15-s23-stage2-runtime-executor-fail!
              "P15S23X003"
              source-path
              closed-plan-execution
              {:requested-source source-path,
               :target requested-target,
               :gravity-closed-plan-output (:stdout closed-plan-execution),
               :clojure-stage2-output (:stdout stage2-runtime-execution),
               :stage0-reference-output reference-output,
               :missing-fact :closed-plan-three-way-output-equivalence}))]
    (assoc state 'reference-output reference-output '_ _ '_ _)))
