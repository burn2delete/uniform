(defn- __gravity_bootstrap_runtime_packet_packet_construction_phase_03 [state]
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
          driver-plan]} state
        plan-shape (select-keys
                     plan
                     [:kind
                      :entrypoint
                      :functions
                      :binding-table
                      :instruction-summary
                      :effect-summary])
        driver-plan-shape (select-keys
                            driver-plan
                            [:kind
                             :entrypoint
                             :functions
                             :binding-table
                             :instruction-summary
                             :effect-summary])
        _ (when-not (=
                      (c-backend-canonical-value plan-shape)
                      (c-backend-canonical-value driver-plan-shape))
            (p15-s23-stage2-compiler-driver-fail!
              "P15S23Y003"
              source-path
              {:stage2-plan plan-shape, :driver-plan driver-plan-shape}
              {:requested-source source-path,
               :target requested-target,
               :driver-engine (:driver-engine stage2-driver-rule),
               :driver-rule-hash (:driver-rule-hash stage2-driver-rule),
               :missing-fact :stage2-driver-plan-equivalence}))
        runtime-adapter-output (try
                                 (p15-s23-reference-runtime-adapter-invoke
                                   stage2-runtime-rule
                                   p15-s23-stage2-runtime-artifact-closed-plan-function
                                   [plan]
                                   (p15-s23-reference-runtime-authority
                                     plan
                                     closed-plan-validation))
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
                                      :maximum-depth p15-s23-closed-runtime-max-depth,
                                      :maximum-nodes
                                      p15-s23-closed-runtime-max-nodes}))
                                 (catch
                                   Exception
                                   ex
                                   (p15-s23-stage2-runtime-executor-fail!
                                     "P15S23X003"
                                     source-path
                                     nil
                                     {:target requested-target,
                                      :cause-message (.getMessage ex),
                                      :missing-fact :gravity-closed-plan-execution})))
        closed-plan-execution (:result runtime-adapter-output)
        runtime-contract-adapter-record (:adapter-record runtime-adapter-output)
        _ (when-not (and
                      (=
                        :gravity/p15-s23-runtime-closed-plan-execution-record
                        (:artifact closed-plan-execution))
                      (= (:entrypoint plan) (:entrypoint closed-plan-execution))
                      (string? (:stdout closed-plan-execution))
                      (= :complete (:status closed-plan-execution))
                      (true? (:clojure-seed-boundary? closed-plan-execution))
                      (false? (:self-hosted? closed-plan-execution)))
            (p15-s23-stage2-runtime-executor-fail!
              "P15S23X003"
              source-path
              closed-plan-execution
              {:target requested-target,
               :missing-fact :gravity-closed-plan-execution-record}))
        closed-plan-execution-hash (str
                                     "sha256:"
                                     (sha256-hex
                                       (pr-str
                                         (c-backend-canonical-value
                                           closed-plan-execution))))
        closed-plan-invocation {:invocation-count-scope
                                :authoritative-packet-construction,
                                :stdout-hash
                                (str
                                  "sha256:"
                                  (sha256-hex (:stdout closed-plan-execution))),
                                :execution-hash closed-plan-execution-hash,
                                :plan-id (:plan-id plan),
                                :function-hash
                                (:runtime-artifact-closed-plan-function-hash
                                  stage2-runtime-rule),
                                :function
                                p15-s23-stage2-runtime-artifact-closed-plan-function,
                                :self-hosted? false,
                                :status :complete,
                                :artifact
                                :gravity/p15-s23-runtime-closed-plan-invocation-record,
                                :runtime-artifact-hash
                                (:runtime-artifact-hash stage2-runtime-rule),
                                :invocation-count 1,
                                :verification-replays-excluded? true,
                                :clojure-seed-boundary? true}
        stage2-runtime-execution (:stage2-runtime-execution-record stage2-driver-run)]
    (assoc
      state
      'plan-shape
      plan-shape
      'driver-plan-shape
      driver-plan-shape
      '_
      _
      'runtime-adapter-output
      runtime-adapter-output
      'closed-plan-execution
      closed-plan-execution
      'runtime-contract-adapter-record
      runtime-contract-adapter-record
      '_
      _
      'closed-plan-execution-hash
      closed-plan-execution-hash
      'closed-plan-invocation
      closed-plan-invocation
      'stage2-runtime-execution
      stage2-runtime-execution)))
