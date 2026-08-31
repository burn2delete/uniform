(let [static-rebuild-token (nth __gravity_bootstrap_checked_core_authority_values 0)
      p15-s23-stage2-closed-checked-core-source-artifact-internal __gravity_bootstrap_checked_core_source_helper
      p15-s23-stage2-closed-checked-core-rebuild-internal __gravity_bootstrap_checked_core_rebuild_helper]
(defn p15-s23-checked-core-verification-replay-authority-record
  [artifact context plan validation runtime-rule policy audit-policy]
  (p15-s23-checked-core-bounded-context! context)
  (doseq [[definition value maximum-nodes maximum-depth]
          [[:verification-authority-artifact artifact
            p15-s23-reference-runtime-max-packet-nodes
            p15-s23-reference-runtime-max-closed-plan-carrier-depth]
           [:verification-authority-plan plan
            p15-s23-reference-runtime-max-contract-nodes
            p15-s23-reference-runtime-max-closed-plan-carrier-depth]
           [:verification-authority-validation validation
            p15-s23-reference-runtime-max-contract-nodes
            p15-s23-reference-runtime-max-contract-depth]
           [:verification-authority-runtime-rule runtime-rule
            p15-s23-reference-runtime-max-rule-nodes
            p15-s23-reference-runtime-max-closed-plan-carrier-depth]
           [:verification-authority-policy policy
            p15-s23-reference-runtime-max-contract-nodes
            p15-s23-reference-runtime-max-contract-depth]
           [:verification-authority-audit-policy audit-policy
            p15-s23-reference-runtime-max-contract-nodes
            p15-s23-reference-runtime-max-contract-depth]]]
    (p15-s23-checked-core-bounded-ingress!
     "C8-CAPABILITY" definition value maximum-nodes maximum-depth))
  (let [authority-record (:authority-record context)
        authority-evidence
        (p15-s23-checked-core-authority-evidence authority-record)
        structural-operation-set
        (:structural-operation-set authority-evidence)
        concrete-core-operation-set
        (get-in artifact [:source-core-input :operation-set])
        writes-stdout? (contains? structural-operation-set :println)
        policy-hash (p15-s23-reference-runtime-hash policy)
        audit-policy-hash (p15-s23-reference-runtime-hash audit-policy)
        function p15-s23-stage2-runtime-artifact-closed-plan-function
        function-hash
        (get p15-s23-reference-runtime-expected-function-hashes function)
        binding
        {:checked-core-artifact-id (:artifact-id artifact)
         :mapping-id (:mapping-id artifact)
         :provenance-binding-id (:provenance-binding-id artifact)
         :source-content-hash (:source-content-hash artifact)
         :plan-id (:plan-id plan)
         :module (:module authority-evidence)
         :runtime-source-content-hash
         (:runtime-artifact-source-content-hash runtime-rule)
         :runtime-artifact-hash (:runtime-artifact-hash runtime-rule)
         :runtime-contract-definition-hash
         (:runtime-contract-definition-hash runtime-rule)
         :runtime-derived-facts-hash
         (:runtime-contract-derived-facts-hash runtime-rule)
         :runtime-function function
         :runtime-function-hash function-hash
         :verification-policy-id (:policy-id policy)
         :verification-policy-hash policy-hash
         :verification-audit-policy-hash audit-policy-hash
         :structural-operation-set structural-operation-set
         :concrete-core-operation-set concrete-core-operation-set
         :reissued-program-authority-record-id
         (:authority-record-id authority-record)
         :reissued-program-authority-evidence-id
         (:evidence-id authority-evidence)}
        instance
        (fn [kind role attributes]
          (p15-s23-checked-core-verification-replay-instance-record
           kind role binding policy audit-policy attributes))
        verifier-allocation-provider
        (instance
         :provider-selection :verifier-managed-allocation
         {:provider-id :gravity.reference/jvm-managed-allocator
          :principal :gravity.bootstrap/checked-core-verifier
          :callee-principal 'gravity.bootstrap.p15-s23.runtime
          :effect :memory/allocate
          :capability :memory/allocator
          :scope :pinned-checked-core-artifact-replay
          :service-handle
          :gravity.reference/checked-core-verification-runtime-handle
          :classification :typed-r1-delegated-adapter})
        verifier-fixture-provider
        (when writes-stdout?
          (instance
           :provider-selection :verifier-transcript-fixture
           {:provider-id :gravity.reference/transcript-capture
            :principal :gravity.bootstrap/checked-core-verifier
            :callee-principal 'gravity.bootstrap.p15-s23.runtime
            :handler-principal
            :gravity.bootstrap/verification-transcript-harness
            :effect :io/write
            :capability :test/fixture
            :scope :verification-transcript
            :service-handle
            :gravity.reference/checked-core-verification-runtime-handle
            :classification :typed-r1-delegated-adapter}))
        allocation-provider
        (instance
         :provider-selection :managed-allocation
         {:provider-id :gravity.reference/jvm-managed-allocator
          :principal 'gravity.bootstrap.p15-s23.runtime
          :effect :memory/allocate
          :capability :memory/allocator
          :scope :pinned-verification-runtime-plan
          :classification :typed-r1-delegated-adapter})
        capture-provider
        (when writes-stdout?
          (instance
           :provider-selection :transcript-capture
           {:provider-id :gravity.reference/transcript-capture
            :principal 'gravity.bootstrap.p15-s23.runtime
            :handler-principal
            :gravity.bootstrap/verification-transcript-harness
            :effect :io/write
            :capability :io/stdout
            :handler-capability :test/fixture
            :scope :verification-transcript
            :classification :typed-r1-delegated-adapter}))
        verifier-allocation-grant
        (instance
         :grant :verifier-managed-allocation
         {:grant-template-id
          :gravity.reference/verification-grant-verifier-managed-allocation
          :principal :gravity.bootstrap/checked-core-verifier
          :callee-principal 'gravity.bootstrap.p15-s23.runtime
          :provider-id :gravity.reference/jvm-managed-allocator
          :effect :memory/allocate
          :capability :memory/allocator
          :scope :pinned-checked-core-artifact-replay
          :authority-transfer? false})
        verifier-fixture-grant
        (when writes-stdout?
          (instance
           :grant :verifier-transcript-fixture
           {:grant-template-id
            :gravity.reference/verification-grant-verifier-test-fixture
            :principal :gravity.bootstrap/checked-core-verifier
            :callee-principal 'gravity.bootstrap.p15-s23.runtime
            :handler-principal
            :gravity.bootstrap/verification-transcript-harness
            :provider-id :gravity.reference/transcript-capture
            :effect :io/write
            :capability :test/fixture
            :scope :verification-transcript
            :authority-transfer? false}))
        allocation-grant
        (instance
         :grant :managed-allocation
         {:grant-template-id
          :gravity.reference/verification-grant-managed-allocation
          :principal 'gravity.bootstrap.p15-s23.runtime
          :provider-id :gravity.reference/jvm-managed-allocator
          :effect :memory/allocate
          :capability :memory/allocator
          :scope :pinned-verification-runtime-plan})
        transcript-grant
        (when writes-stdout?
          (instance
           :grant :transcript-capture
           {:grant-template-id
            :gravity.reference/verification-grant-transcript-capture
            :principal 'gravity.bootstrap.p15-s23.runtime
            :provider-id :gravity.reference/transcript-capture
            :effect :io/write
            :capability :io/stdout
            :scope :verification-transcript}))
        fixture-grant
        (when writes-stdout?
          (instance
           :grant :fixture
           {:grant-template-id
            :gravity.reference/verification-grant-test-fixture
            :principal :gravity.bootstrap/verification-transcript-harness
            :source-principal 'gravity.bootstrap.p15-s23.runtime
            :provider-id :gravity.reference/transcript-capture
            :effect :io/write
            :capability :test/fixture
            :scope :verification-transcript}))
        provider-records
        (cond-> [verifier-allocation-provider allocation-provider]
          writes-stdout?
          (conj verifier-fixture-provider capture-provider))
        grant-records
        (cond-> [verifier-allocation-grant allocation-grant]
          writes-stdout?
          (conj verifier-fixture-grant transcript-grant fixture-grant))
        base
        {:kind :gravity/p15-s23-checked-core-verification-replay-authority
         :schema-version 1
         :policy-id (:policy-id policy)
         :policy-hash policy-hash
         :audit-policy-id (:policy-id audit-policy)
         :audit-policy-hash audit-policy-hash
         :binding binding
         :verifier-principal (:verifier-principal policy)
         :runtime-principal (:runtime-principal policy)
         :handler-principal (:handler-principal policy)
         :invocation-contract (:invocation-contract policy)
         :provider-selection-records provider-records
         :grant-records grant-records
         :required-effects
         (cond-> #{:memory/allocate}
           writes-stdout? (conj :io/write))
         :required-capabilities
         (cond-> #{:memory/allocator}
           writes-stdout? (conj :io/stdout :test/fixture))
         :phase :verification
         :lifetime :single-verification-replay
         :reference-invocation :single-verification-replay
         :package :gravity/bootstrap
         :deployment :verification-harness-only
         :deny-by-default? true
         :host-service-boundary :typed-r1-delegated-adapters
         :authoritative-invocation? false
         :excluded-from-authoritative-invocation-count? true
         :program-authority-consumed? false
         :program-grants-consumed? false
         :live-external-io? false
         :delegation :none
         :authority-widening? false}]
    (when-not (= (:observed-operation-set validation)
                 (p15-s23-closed-core-observed-plan-operations plan))
      (p15-s23-closed-core-fail!
       "C8-CAPABILITY" (:source-path context) artifact
       {:missing-fact
        :verification-replay-authenticated-structural-operation-set}))
    (assoc base :authority-record-id
           (p15-s23-reference-runtime-hash base)))))
