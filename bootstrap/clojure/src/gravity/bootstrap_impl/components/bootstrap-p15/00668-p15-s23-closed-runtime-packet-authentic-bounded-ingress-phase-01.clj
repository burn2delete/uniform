(defn- __gravity_bootstrap_packet_authentic_bounded_ingress_phase_01 [state]
  (let [{:syms [packet context]} state
        _ (p15-s23-reference-runtime-bounded-value!
            (when (map? context) (:source-path context))
            (when (map? context) (:requested-target context))
            :closed-runtime-packet
            packet
            p15-s23-reference-runtime-max-packet-nodes
            p15-s23-reference-runtime-max-closed-plan-carrier-depth)
        _ (p15-s23-reference-runtime-bounded-value!
            (when (map? context) (:source-path context))
            (when (map? context) (:requested-target context))
            :closed-runtime-packet-context
            context
            p15-s23-reference-runtime-max-contract-nodes
            p15-s23-reference-runtime-max-contract-depth)
        context-valid? (and
                         (map? context)
                         (=
                           #{:source-content-hash
                             :requested-target
                             :source-text
                             :source-path}
                           (set (keys context)))
                         (string? (:source-path context))
                         (string? (:source-text context))
                         (=
                           :valid
                           (:status
                             (p15-s23-reference-runtime-bounded-utf8-count
                               (:source-text context)
                               p15-s23-reference-runtime-max-context-source-bytes)))
                         (keyword? (:requested-target context))
                         (contains?
                           stage2-runtime-derived-source-targets
                           (:requested-target context))
                         (=
                           (:source-content-hash context)
                           (str "sha256:" (sha256-hex (:source-text context)))))
        packet-envelope-valid? (and
                                 (map? packet)
                                 (=
                                   #{:stage2-compiler-artifact-record
                                     :stage2-runtime-rule
                                     :stage2-compiler-driver-rule
                                     :stage2-plan-emitter-rule
                                     :closed-plan-execution-record
                                     :requested-target
                                     :closed-plan-invocation-record
                                     :stage2-compiler-driver-record
                                     :status
                                     :kind
                                     :runtime-contract-adapter-record
                                     :closed-plan-validation-record
                                     :provenance
                                     :reference-output
                                     :plan
                                     :stage2-runtime-execution-record
                                     :target-eligibility}
                                   (set (keys packet)))
                                 (=
                                   :gravity/target-neutral-stage2-runtime-packet
                                   (:kind packet))
                                 (= :complete (:status packet))
                                 (=
                                   (:requested-target context)
                                   (:requested-target packet)))
        candidate-runtime-rule-authentic? (and
                                            context-valid?
                                            packet-envelope-valid?
                                            (p15-s23-reference-runtime-rule-authentic?
                                              (:stage2-runtime-rule packet)))
        packet-plan (:plan packet)
        derived-validation (when candidate-runtime-rule-authentic?
                             (try
                               (p15-s23-closed-runtime-plan-validation!
                                 (:source-path context)
                                 (:requested-target context)
                                 packet-plan)
                               (catch Exception _ nil)))
        trusted-stage2-rule (when derived-validation
                              (try
                                (c-backend-stage2-plan-emitter-source-rule!
                                  (:source-path context)
                                  (:requested-target context))
                                (catch Exception _ nil)))
        trusted-plan (when trusted-stage2-rule
                       (try
                         (binding [*additional-bootstrap-targets* stage2-runtime-derived-source-targets]
                           (p15-s23-stage2-plan-emitter-compile-source
                             (:emitter trusted-stage2-rule)
                             (:source-path context)
                             (:source-text context)))
                         (catch Exception _ nil)))
        trusted-plan-validation (when trusted-plan
                                  (try
                                    (p15-s23-closed-runtime-plan-validation!
                                      (:source-path context)
                                      (:requested-target context)
                                      trusted-plan)
                                    (catch Exception _ nil)))
        trusted-driver-rule (when trusted-plan-validation
                              (try
                                (c-backend-stage2-compiler-driver-source-rule!
                                  (:source-path context)
                                  (:requested-target context))
                                (catch Exception _ nil)))
        trusted-runtime-rule (when trusted-driver-rule
                               (try
                                 (c-backend-stage2-runtime-source-rule!
                                   (:source-path context)
                                   (:requested-target context))
                                 (catch Exception _ nil)))]
    (assoc
      state
      '_
      _
      '_
      _
      'context-valid?
      context-valid?
      'packet-envelope-valid?
      packet-envelope-valid?
      'candidate-runtime-rule-authentic?
      candidate-runtime-rule-authentic?
      'packet-plan
      packet-plan
      'derived-validation
      derived-validation
      'trusted-stage2-rule
      trusted-stage2-rule
      'trusted-plan
      trusted-plan
      'trusted-plan-validation
      trusted-plan-validation
      'trusted-driver-rule
      trusted-driver-rule
      'trusted-runtime-rule
      trusted-runtime-rule)))
