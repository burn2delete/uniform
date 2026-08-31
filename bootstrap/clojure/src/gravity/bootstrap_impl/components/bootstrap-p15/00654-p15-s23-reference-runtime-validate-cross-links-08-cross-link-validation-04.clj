(defn- __gravity_bootstrap_p15-s23-reference-runtime-validate-cross-links_cross_link_validation_04 [state]
  (let [{:syms
         [source-path
          target
          definitions
          authoritative-module
          derived
          contract
          checked-core-program-policy
          checked-core-verification-replay-policy
          checked-core-verification-replay-audit-policy
          function-graph
          function-effects
          effect-graph
          allocator
          capture-provider
          capture-handler
          selections
          proofs
          services
          adapter
          failure-policy
          audit-policy
          capability-manifest
          capability-table
          observability
          grants
          deployment
          expected-links
          source-principal
          handler-principal
          handler-scope
          excluded-functions
          grant-records
          proof-records
          expected-grants
          owner-table]} state]
    (let [mappings (into
                     {}
                     (map
                       (juxt :injected-failure identity)
                       (:catchable-mappings failure-policy)))
          expected-timing {:allocation
                           {:execution :started,
                            :action-started? true,
                            :action-status :failed-before-commit,
                            :result-committed? false,
                            :output-committed? false,
                            :output :empty,
                            :diagnostic "R1-FAILURE"},
                           :capture
                           {:execution :started,
                            :action-started? true,
                            :action-status :failed-before-commit,
                            :result-committed? false,
                            :output-committed? false,
                            :output :empty,
                            :diagnostic "R1-FAILURE"},
                           :output
                           {:execution :not-started,
                            :action-started? false,
                            :action-status :rejected-before-start,
                            :result-committed? false,
                            :output-committed? false,
                            :output :empty,
                            :diagnostic "L15-PROVIDER-MISSING"}}
          action-ids (map :action-id (vals mappings))
          service-ids (map :service-id (vals mappings))]
      (p15-s23-reference-runtime-ensure!
        source-path
        target
        :runtime-contract-failure-mapping-set
        #{:output :capture :allocation}
        (set (keys mappings)))
      (doseq [[failure expected] expected-timing]
        (p15-s23-reference-runtime-ensure!
          source-path
          target
          :runtime-contract-failure-action-timing
          expected
          (select-keys (get mappings failure) (keys expected))))
      (p15-s23-reference-runtime-ensure!
        source-path
        target
        :runtime-contract-distinct-action-ids
        3
        (count (set action-ids)))
      (p15-s23-reference-runtime-ensure!
        source-path
        target
        :runtime-contract-distinct-service-ids
        3
        (count (set service-ids))))
    (p15-s23-reference-runtime-ensure!
      source-path
      target
      :runtime-contract-no-credit
      {:checked-core-verification-replay-audit-policy-id
       p15-s23-checked-core-verification-replay-audit-policy-id,
       :release-credit? false,
       :checked-core-verification-replay-policy-id
       p15-s23-checked-core-verification-replay-policy-id,
       :checked-core-authority-record-schema-version 1,
       :c11-credit? false,
       :checked-core-reference-only? true,
       :whole-language? false,
       :typed-fourth-authority-consumed? true,
       :checked-core-str-println-admission-status
       :complete-for-authenticated-hosted-jvm-reference-interpreter-slice,
       :target-lowering-credit? false,
       :checked-core-binary-integer-comparison-operations '#{= < <= > >=},
       :self-hosted? false,
       :checked-core-program-authority-policy-id
       p15-s23-checked-core-program-authority-policy-id,
       :checked-core-verification-replay-authority-schema-version 1,
       :mir-derived? false,
       :typed-fourth-authority :gravity/p15-s23-checked-core-authority-binding-v1,
       :checked-core-binary-integer-comparison-admission? true,
       :checked-core-str-println-admission? true,
       :clojure-seed-boundary? true}
      (select-keys
        contract
        [:checked-core-str-println-admission?
         :checked-core-str-println-admission-status
         :checked-core-binary-integer-comparison-admission?
         :checked-core-binary-integer-comparison-operations
         :checked-core-program-authority-policy-id
         :checked-core-verification-replay-policy-id
         :checked-core-verification-replay-audit-policy-id
         :checked-core-authority-record-schema-version
         :checked-core-verification-replay-authority-schema-version
         :checked-core-reference-only?
         :typed-fourth-authority
         :typed-fourth-authority-consumed?
         :target-lowering-credit?
         :release-credit?
         :c11-credit?
         :mir-derived?
         :whole-language?
         :clojure-seed-boundary?
         :self-hosted?]))
    (p15-s23-reference-runtime-ensure!
      source-path
      target
      :runtime-contract-deployment-residual
      {:status :unresolved,
       :provider-selection :unresolved,
       :grant :unresolved,
       :closed-plan-interpreter-excluded? true}
      (select-keys
        deployment
        [:status :provider-selection :grant :closed-plan-interpreter-excluded?]))
    (doseq [[name value] definitions]
      (when (and
              (not
                (contains?
                  '#{p15-s23-checked-core-verification-replay-audit-policy
                     p15-s23-checked-core-program-authority-policy
                     p15-s23-reference-stdout-deployment-requirement
                     p15-s23-checked-core-verification-replay-policy}
                  name))
              (contains? value :status))
        (p15-s23-reference-runtime-ensure!
          source-path
          target
          :runtime-contract-definition-status
          :complete-for-pinned-reference-runtime-contract-slice
          (:status value))))
    state))
