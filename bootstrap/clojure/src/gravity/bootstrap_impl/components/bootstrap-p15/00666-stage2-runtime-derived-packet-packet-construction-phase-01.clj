(defn- __gravity_bootstrap_runtime_packet_packet_construction_phase_01 [state]
  (let [{:syms [source-path source-text requested-target validate-plan!]} state
        stage2-rule (c-backend-stage2-plan-emitter-source-rule!
                      source-path
                      requested-target)
        stage2-runtime-rule (c-backend-stage2-runtime-source-rule!
                              source-path
                              requested-target)
        stage2-driver-rule (c-backend-stage2-compiler-driver-source-rule!
                             source-path
                             requested-target)
        plan (try
               (p15-s23-stage2-plan-emitter-compile-source
                 (:emitter stage2-rule)
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
                    :boundary :source-to-stage2-plan,
                    :maximum-depth p15-s23-closed-runtime-max-depth,
                    :maximum-nodes p15-s23-closed-runtime-max-nodes}))
               (catch
                 Exception
                 ex
                 (stage2-runtime-derived-packet-fail!
                   "C14-UNSUPPORTED"
                   "stage2 plan emitter could not compile the source"
                   source-path
                   requested-target
                   nil
                   {:cause-message (.getMessage ex),
                    :missing-fact :stage2-plan-emission})))
        _ (when validate-plan! (validate-plan! plan))
        _ (when-not (and
                      (= :gravity/stage2-hosted-core-compiled-plan (:kind plan))
                      (=
                        :p15-s23-stage2-plan-emitter
                        (get-in plan [:compiler :rule-source]))
                      (map? (:functions plan))
                      (symbol? (:entrypoint plan))
                      (contains? (:functions plan) (:entrypoint plan))
                      (vector?
                        (get-in plan [:functions (:entrypoint plan) :instructions])))
            (stage2-runtime-derived-packet-fail!
              "C14-INPUT"
              "stage2 plan integrity validation failed"
              source-path
              requested-target
              plan
              {:observed-plan-kind (:kind plan),
               :observed-rule-source (get-in plan [:compiler :rule-source]),
               :missing-fact :stage2-plan-integrity}))
        raw-source-declared-target (get-in plan [:module :target])
        source-declared-target (if (= :js raw-source-declared-target)
                                 :js-ts
                                 raw-source-declared-target)
        target-eligibility (cond
                             (= source-declared-target requested-target) (cond->
                                                                           {:status
                                                                            :accepted,
                                                                            :source-declared-target
                                                                            source-declared-target,
                                                                            :requested-target
                                                                            requested-target,
                                                                            :selection
                                                                            :source-and-request-agree}
                                                                           (not=
                                                                             raw-source-declared-target
                                                                             source-declared-target)
                                                                           (assoc
                                                                             :raw-source-declared-target
                                                                             raw-source-declared-target
                                                                             :source-target-alias-canonicalized?
                                                                             true))
                             (= :jvm source-declared-target) {:status :accepted,
                                                              :source-declared-target
                                                              :jvm,
                                                              :requested-target
                                                              requested-target,
                                                              :selection
                                                              :explicit-bootstrap-seed-target-override,
                                                              :bootstrap-seed-target?
                                                              true}
                             :else (stage2-runtime-derived-packet-fail!
                                     "C14-PROFILE"
                                     "source target constraint is incompatible with requested target"
                                     source-path
                                     requested-target
                                     plan
                                     {:source-declared-target source-declared-target,
                                      :requested-target requested-target,
                                      :missing-fact :target-constraint-compatibility}))
        closed-plan-validation (p15-s23-closed-runtime-plan-validation!
                                 source-path
                                 requested-target
                                 plan)]
    (assoc
      state
      'stage2-rule
      stage2-rule
      'stage2-runtime-rule
      stage2-runtime-rule
      'stage2-driver-rule
      stage2-driver-rule
      'plan
      plan
      '_
      _
      '_
      _
      'raw-source-declared-target
      raw-source-declared-target
      'source-declared-target
      source-declared-target
      'target-eligibility
      target-eligibility
      'closed-plan-validation
      closed-plan-validation)))
