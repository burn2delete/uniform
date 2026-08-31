(defn- __gravity_bootstrap_packet_authentic_schema_hash_cross_links_01 [state]
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
          trusted-driver-record
          validation
          execution
          invocation
          adapter-record
          runtime-rule
          emitter-rule
          driver-rule
          driver-record
          driver-plan
          normalize-plan
          packet-plan-base
          trusted-plan-base
          driver-plan-base
          normalize-comparison
          normalize-emitter-rule
          normalize-driver-rule
          normalize-runtime-rule
          raw-source-target
          source-target
          expected-target-eligibility
          derived-plan-id
          derived-driver-plan-id
          runtime-artifact-plan
          derived-runtime-artifact-hash
          derived-adapter-output
          derived-execution
          derived-adapter-record
          execution-hash
          stdout-hash]} state]
    (and
      (= (:requested-target context) (:requested-target packet))
      (= (:source-path context) (get-in packet-plan [:source :path]))
      (= (:source-content-hash context) (get-in packet-plan [:source :sha256]))
      (= (:source-content-hash context) (:source-id driver-record))
      (= (:source-path context) (:fixture driver-record))
      (= derived-driver-plan-id (:plan-id driver-plan))
      (= derived-driver-plan-id (:stage2-plan-id driver-record))
      (=
        (:stage2-runtime-execution-record packet)
        (:stage2-runtime-execution-record trusted-driver-record))
      (= (:reference-output packet) (:stage0-reference-output trusted-driver-record))
      (= :complete (:status validation))
      (=
        #{:observed-operation-set
          :node-count
          :maximum-depth
          :operation-set
          :status
          :entrypoint
          :artifact
          :maximum-nodes}
        (set (keys validation)))
      (=
        #{:self-hosted?
          :status
          :entrypoint
          :artifact
          :entrypoint-result
          :stdout
          :clojure-seed-boundary?}
        (set (keys execution)))
      (=
        #{:invocation-count-scope
          :stdout-hash
          :execution-hash
          :plan-id
          :function-hash
          :function
          :self-hosted?
          :status
          :artifact
          :runtime-artifact-hash
          :invocation-count
          :verification-replays-excluded?
          :clojure-seed-boundary?}
        (set (keys invocation)))
      (=
        (c-backend-canonical-value derived-validation)
        (c-backend-canonical-value validation))
      (=
        (c-backend-canonical-value derived-execution)
        (c-backend-canonical-value execution))
      (=
        (c-backend-canonical-value derived-adapter-record)
        (c-backend-canonical-value adapter-record))
      (= :gravity/p15-s23-reference-runtime-adapter-record (:artifact adapter-record))
      (= :complete (:status adapter-record))
      (= (:runtime-artifact-hash runtime-rule) (:runtime-artifact-hash adapter-record))
      (= (:entrypoint packet-plan) (:entrypoint validation))
      (= (:entrypoint packet-plan) (:entrypoint execution))
      (= derived-plan-id (:plan-id packet-plan))
      (= :gravity/p15-s23-runtime-closed-plan-execution-record (:artifact execution))
      (= :complete (:status execution))
      (= :gravity/p15-s23-runtime-closed-plan-invocation-record (:artifact invocation))
      (= :complete (:status invocation))
      (=
        p15-s23-stage2-runtime-artifact-expected-source-content-hash
        (:runtime-artifact-source-content-hash runtime-rule))
      (=
        p15-s23-stage2-runtime-artifact-expected-artifact-hash
        (:runtime-artifact-hash runtime-rule))
      (= derived-runtime-artifact-hash (:runtime-artifact-hash runtime-rule))
      (=
        p15-s23-stage2-runtime-artifact-expected-closed-function-hashes
        (:runtime-artifact-closed-function-hashes runtime-rule))
      (=
        p15-s23-reference-runtime-expected-function-hashes
        (:runtime-artifact-function-hashes runtime-rule))
      (=
        p15-s23-reference-runtime-expected-contract-definition-hash
        (:runtime-contract-definition-hash runtime-rule))
      (=
        p15-s23-reference-runtime-source-provider-selections
        (:runtime-artifact-providers runtime-rule))
      (= :complete (get-in runtime-rule [:runtime-contract-validation-record :status]))
      (=
        (p15-s23-reference-runtime-hash
          (get-in
            runtime-rule
            [:runtime-contract-validation-record :derived-contract-facts]))
        (:runtime-contract-derived-facts-hash runtime-rule))
      (=
        p15-s23-reference-runtime-expected-derived-facts-hash
        (:runtime-contract-derived-facts-hash runtime-rule))
      (=
        (get
          p15-s23-stage2-runtime-artifact-expected-closed-function-hashes
          p15-s23-stage2-runtime-artifact-closed-plan-function)
        (:runtime-artifact-closed-plan-function-hash runtime-rule))
      (= 1 (:invocation-count invocation))
      (= :authoritative-packet-construction (:invocation-count-scope invocation))
      (true? (:verification-replays-excluded? invocation))
      (= p15-s23-stage2-runtime-artifact-closed-plan-function (:function invocation))
      (=
        (:runtime-artifact-closed-plan-function-hash runtime-rule)
        (:function-hash invocation))
      (= (:runtime-artifact-hash runtime-rule) (:runtime-artifact-hash invocation))
      (= (:plan-id packet-plan) (:plan-id invocation))
      (= execution-hash (:execution-hash invocation))
      (= stdout-hash (:stdout-hash invocation))
      (=
        (:stdout execution)
        (get-in packet [:stage2-runtime-execution-record :stdout]))
      (= (:stdout execution) (:reference-output packet))
      (true? (:clojure-seed-boundary? execution))
      (false? (:self-hosted? execution))
      (true? (:clojure-seed-boundary? invocation))
      (false? (:self-hosted? invocation)))))
