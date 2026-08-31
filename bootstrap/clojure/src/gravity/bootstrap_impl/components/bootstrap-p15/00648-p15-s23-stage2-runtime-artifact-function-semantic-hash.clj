

(defn p15-s23-stage2-runtime-artifact-function-semantic-hash
  [definition]
  (str "sha256:"
       (sha256-hex
        (pr-str
         (c-backend-canonical-value
          (select-keys definition [:arity :params :instructions]))))))

(def p15-s23-reference-runtime-source-provider-selections
  [{:capability :memory/allocator
    :provider-id :gravity.reference/jvm-managed-allocator
    :version 1
    :profile :hosted
    :target :jvm
    :phase :runtime
    :mode :pinned-reference}])

(def p15-s23-checked-core-program-authority-policy-id
  :gravity.reference/checked-core-program-authority-policy)

(def p15-s23-checked-core-verification-replay-policy-id
  :gravity.reference/checked-core-verification-replay-policy)

(def p15-s23-checked-core-verification-replay-audit-policy-id
  :gravity.reference/checked-core-verification-replay-audit-policy)

(def p15-s23-checked-core-reference-policy-selector
  {:kind :gravity/p15-s23-checked-core-authority-request
   :schema-version 1
   :policy-id p15-s23-checked-core-program-authority-policy-id
   :mode :closed-plan-reference
   :reference-harness :gravity.bootstrap/reference-harness
   :single-invocation? true
   :deployment-runtime? false
   :live-external-io? false})

(def p15-s23-checked-core-reference-policy-selector-keys
  #{:kind :schema-version :policy-id :mode :reference-harness
    :single-invocation? :deployment-runtime? :live-external-io?})

(def p15-s23-checked-core-program-authority-policy-keys
  #{:artifact :schema-version :policy-id :audit-policy-id :status
    :profile :target
    :program-principal-source :source-declaration-is-grant?
    :operation-contracts :runtime-principal :handler-principal
    :allowed-runtime-provider-ids :allowed-runtime-grant-ids
    :allowed-handler-provider-ids :allowed-handler-grant-ids
    :phase :lifetime :reference-invocation :package
    :deployment :single-invocation? :reference-interpreter?
    :transcript-only? :deployment-runtime? :live-external-io? :delegation
    :authority-widening? :program-grants-in-adapter-authority?})

(def p15-s23-checked-core-program-operation-contracts
  {'str {:source-operation :str
         :effect :memory/allocate
         :capability :memory/allocator
         :provider-id :gravity.reference/jvm-managed-allocator
         :program-grant-template-id
         :gravity.reference/program-grant-managed-allocation
         :scope :pinned-runtime-plan
         :safety-outcome :runtime-checked
         :result-type :gravity/string}
   'println {:source-operation :println
             :effect :io/write
             :capability :io/stdout
             :provider-id :gravity.reference/transcript-capture
             :program-grant-template-id
             :gravity.reference/program-grant-reference-stdout
             :scope :checked-core-program-transcript
             :safety-outcome :runtime-checked
             :result-type :gravity/nil
             :delivery :in-memory-reference-transcript}})

(def p15-s23-checked-core-expected-program-authority-policy
  {:artifact :gravity/p15-s23-checked-core-program-authority-policy
   :schema-version 1
   :policy-id p15-s23-checked-core-program-authority-policy-id
   :audit-policy-id :gravity.reference/runtime-audit-policy
   :status :complete-for-authenticated-hosted-jvm-reference-interpreter-slice
   :profile :hosted
   :target :jvm
   :program-principal-source :compiled-module
   :source-declaration-is-grant? false
   :operation-contracts
   p15-s23-checked-core-program-operation-contracts
   :runtime-principal 'gravity.bootstrap.p15-s23.runtime
   :handler-principal :gravity.bootstrap/reference-harness
   :allowed-runtime-provider-ids
   #{:gravity.reference/jvm-managed-allocator
     :gravity.reference/transcript-capture}
   :allowed-runtime-grant-ids
   #{:gravity.reference/grant-managed-allocation
     :gravity.reference/grant-reference-stdout}
   :allowed-handler-provider-ids #{:gravity.reference/transcript-capture}
   :allowed-handler-grant-ids #{:gravity.reference/grant-test-fixture}
   :phase :runtime
   :lifetime :single-reference-execution
   :reference-invocation :single-reference-execution
   :package :gravity/bootstrap
   :deployment :reference-harness-only
   :single-invocation? true
   :reference-interpreter? true
   :transcript-only? true
   :deployment-runtime? false
   :live-external-io? false
   :delegation :none
   :authority-widening? false
   :program-grants-in-adapter-authority? false})

(def p15-s23-checked-core-verification-replay-policy-keys
  #{:artifact :schema-version :policy-id :audit-policy-id :status
    :profile :target :verifier-principal :runtime-principal
    :handler-principal :invocation-contract
    :provider-contracts :grant-contracts :binding-inputs
    :plan-requirement-source :host-service-boundary :deny-by-default?
    :redaction-policy :audit-record-contract
    :phase :lifetime :reference-invocation
    :package :deployment :single-replay? :authoritative-invocation?
    :excluded-from-authoritative-invocation-count? :result-producing?
    :external-io-delivery :live-external-io?
    :program-authority-consumed? :program-grants-consumed? :delegation
    :authority-widening?})