(defn- __gravity_bootstrap_packet_authentic_trusted_rebuild_phase_01 [state]
  (let [{:syms
         [packet
          context
          _
          context-valid?
          packet-envelope-valid?
          candidate-runtime-rule-authentic?
          packet-plan
          derived-validation
          trusted-stage2-rule
          trusted-plan
          trusted-plan-validation
          trusted-driver-rule
          trusted-runtime-rule
          trusted-driver-record]} state
        validation (:closed-plan-validation-record packet)
        execution (:closed-plan-execution-record packet)
        invocation (:closed-plan-invocation-record packet)
        adapter-record (:runtime-contract-adapter-record packet)
        runtime-rule (:stage2-runtime-rule packet)
        emitter-rule (:stage2-plan-emitter-rule packet)
        driver-rule (:stage2-compiler-driver-rule packet)
        driver-record (:stage2-compiler-driver-record packet)
        driver-plan (:stage2-plan driver-record)
        normalize-plan (fn [plan]
                         (-> plan
                          (dissoc :plan-id)
                          (update :source dissoc :path)
                          (update :module dissoc :source-path)))
        packet-plan-base (normalize-plan packet-plan)
        trusted-plan-base (normalize-plan trusted-plan)
        driver-plan-base (normalize-plan driver-plan)
        normalize-comparison (fn [plan-base]
                               (-> plan-base
                                (update-in [:module :exports] #(vec (or % [])))
                                (update-in [:module :providers] #(vec (or % [])))))
        normalize-emitter-rule #(dissoc % :source-path)
        normalize-driver-rule #(dissoc % :driver-source-path)
        normalize-runtime-rule #(->
                                 %
                                 (dissoc
                                   :stage2-compiler-source-path
                                   :runtime-artifact-source-path
                                   :runtime-source-path)
                                 (update
                                   :runtime-artifact-authoritative-module
                                   dissoc
                                   :source-path)
                                 (update-in
                                   [:runtime-artifact-plan :source]
                                   dissoc
                                   :path)
                                 (update-in
                                   [:runtime-artifact-plan :module]
                                   dissoc
                                   :source-path))
        raw-source-target (get-in trusted-plan [:module :target])
        source-target (if (= :js raw-source-target) :js-ts raw-source-target)
        expected-target-eligibility (cond
                                      (= source-target (:requested-target context)) (cond->
                                                                                      {:status
                                                                                       :accepted,
                                                                                       :source-declared-target
                                                                                       source-target,
                                                                                       :requested-target
                                                                                       (:requested-target
                                                                                         context),
                                                                                       :selection
                                                                                       :source-and-request-agree}
                                                                                      (not=
                                                                                        raw-source-target
                                                                                        source-target)
                                                                                      (assoc
                                                                                        :raw-source-declared-target
                                                                                        raw-source-target
                                                                                        :source-target-alias-canonicalized?
                                                                                        true))
                                      (= :jvm source-target) {:status :accepted,
                                                              :source-declared-target
                                                              :jvm,
                                                              :requested-target
                                                              (:requested-target
                                                                context),
                                                              :selection
                                                              :explicit-bootstrap-seed-target-override,
                                                              :bootstrap-seed-target?
                                                              true}
                                      :else nil)]
    (assoc
      state
      'validation
      validation
      'execution
      execution
      'invocation
      invocation
      'adapter-record
      adapter-record
      'runtime-rule
      runtime-rule
      'emitter-rule
      emitter-rule
      'driver-rule
      driver-rule
      'driver-record
      driver-record
      'driver-plan
      driver-plan
      'normalize-plan
      normalize-plan
      'packet-plan-base
      packet-plan-base
      'trusted-plan-base
      trusted-plan-base
      'driver-plan-base
      driver-plan-base
      'normalize-comparison
      normalize-comparison
      'normalize-emitter-rule
      normalize-emitter-rule
      'normalize-driver-rule
      normalize-driver-rule
      'normalize-runtime-rule
      normalize-runtime-rule
      'raw-source-target
      raw-source-target
      'source-target
      source-target
      'expected-target-eligibility
      expected-target-eligibility)))
