

(defn p15-s23-checked-core-program-provider-record
  [source-binding program-principal operation-contract policy-hash]
  (let [base
        {:artifact :gravity/p15-s23-program-provider-selection
         :schema-version 1
         :principal-id program-principal
         :effect (:effect operation-contract)
         :capability (:capability operation-contract)
         :provider-id (:provider-id operation-contract)
         :profile :hosted
         :target :jvm
         :phase :runtime
         :scope (:scope operation-contract)
         :source-binding source-binding
         :lifetime :single-reference-execution
         :policy-id p15-s23-checked-core-program-authority-policy-id
         :policy-hash policy-hash
         :selection-source :pinned-runtime-contract-policy
         :provider-provenance :authenticated-runtime-contract-operation
         :source-declaration-is-grant? false
         :deployment :reference-harness-only
         :live-external-authority? false
         :status :selected-for-single-reference-execution}]
    (assoc base :provider-selection-id
           (p15-s23-closed-core-digest base))))

(defn p15-s23-checked-core-program-grant-record
  [source-binding program-principal provider-record operation-contract
   policy]
  (let [base
        {:artifact :gravity/p15-s23-program-capability-grant
         :schema-version 1
         :principal-id program-principal
         :effect (:effect provider-record)
         :capability (:capability provider-record)
         :provider-selection-id (:provider-selection-id provider-record)
         :provider-id (:provider-id provider-record)
         :scope (:scope provider-record)
         :source-binding source-binding
         :profile :hosted
         :target :jvm
         :phase :runtime
         :lifetime :single-reference-execution
         :policy-id p15-s23-checked-core-program-authority-policy-id
         :policy-hash (:policy-hash provider-record)
         :audit-policy-id (:audit-policy-id policy)
         :program-grant-template-id
         (:program-grant-template-id operation-contract)
         :reference-invocation :single-reference-execution
         :package :gravity/bootstrap
         :deployment :reference-harness-only
         :authority-source :pinned-runtime-contract-policy
         :provider-provenance (:provider-provenance provider-record)
         :source-declaration-is-grant? false
         :live-external-authority? false
         :status :granted-for-single-reference-execution}]
    (assoc base :grant-id (p15-s23-closed-core-digest base))))

(defn p15-s23-checked-core-program-authority-records
  [source-binding program-principal structural-operation-set policy]
  (let [contracts (:operation-contracts policy)
        policy-hash (p15-s23-closed-core-digest policy)
        selected-contracts
        (mapv (fn [operation]
                (or (get contracts (case operation
                                     :str 'str
                                     :println 'println))
                    (p15-s23-closed-core-fail!
                     "C8-CAPABILITY" "<checked-core-authority>"
                     {:source-operation operation}
                     {:missing-fact
                      :pinned-program-authority-operation-contract})))
              (sort-by pr-str structural-operation-set))
        providers
        (mapv #(p15-s23-checked-core-program-provider-record
                source-binding program-principal % policy-hash)
              selected-contracts)
        grants
        (mapv (fn [provider operation-contract]
                (p15-s23-checked-core-program-grant-record
                 source-binding program-principal provider
                 operation-contract policy))
              providers selected-contracts)]
    {:provider-records providers :grant-records grants}))

(defn p15-s23-checked-core-authority-record
  [source-content-hash plan module requirements runtime-rule policy]
  (let [structural-operation-set
        (p15-s23-checked-core-authority-structural-operations requirements)
        program-principal (:module module)
        runtime-principal (:runtime-principal policy)
        handler-principal (:handler-principal policy)
        _ (when-not (and (symbol? program-principal)
                         (symbol? runtime-principal)
                         (keyword? handler-principal)
                         (= 3 (count (set [program-principal
                                           runtime-principal
                                           handler-principal]))))
            (p15-s23-closed-core-fail!
             "C8-CAPABILITY" "<checked-core-authority>"
             {:program-principal program-principal
              :runtime-principal runtime-principal
              :handler-principal handler-principal}
             {:missing-fact :typed-distinct-authority-principals}))
        source-binding
        (p15-s23-checked-core-authority-source-binding
         source-content-hash (:plan-id plan) program-principal
         structural-operation-set)
        program-records
        (p15-s23-checked-core-program-authority-records
         source-binding program-principal structural-operation-set policy)
        writes-stdout? (contains? structural-operation-set :println)
        runtime-provider-ids
        (cond-> #{:gravity.reference/jvm-managed-allocator}
          writes-stdout? (conj :gravity.reference/transcript-capture))
        runtime-grant-ids
        (cond-> #{:gravity.reference/grant-managed-allocation}
          writes-stdout? (conj :gravity.reference/grant-reference-stdout))
        handler-provider-ids
        (if writes-stdout?
          #{:gravity.reference/transcript-capture}
          #{})
        handler-grant-ids
        (if writes-stdout?
          #{:gravity.reference/grant-test-fixture}
          #{})
        adapter-authority
        (p15-s23-reference-runtime-authority
         nil {:observed-operation-set
              (if writes-stdout? #{:println} #{})})
        base
        {:kind :gravity/p15-s23-checked-core-authority-binding-v1
         :schema-version 1
         :source-content-hash source-content-hash
         :plan-id (:plan-id plan)
         :module program-principal
         :profile (:profile module)
         :source-target (:target module)
         :runtime-source-content-hash
         (:runtime-source-content-hash runtime-rule)
         :runtime-contract-definition-hash
         (:runtime-contract-definition-hash runtime-rule)
         :runtime-contract-derived-facts-hash
         (:runtime-contract-derived-facts-hash runtime-rule)
         :runtime-artifact-hash (:runtime-artifact-hash runtime-rule)
         :runtime-function
         p15-s23-stage2-runtime-artifact-closed-plan-function
         :runtime-function-hash
         (get (:runtime-artifact-function-hashes runtime-rule)
              p15-s23-stage2-runtime-artifact-closed-plan-function)
         :program-authority-policy-id (:policy-id policy)
         :program-authority-policy-hash
         (p15-s23-closed-core-digest policy)
         :structural-operation-set structural-operation-set
         :required-effects (:required-effects requirements)
         :required-capabilities (:required-capabilities requirements)
         :program-principal program-principal
         :runtime-principal runtime-principal
         :handler-principal handler-principal
         :program-provider-records (:provider-records program-records)
         :program-grant-records (:grant-records program-records)
         :runtime-provider-ids runtime-provider-ids
         :runtime-grant-ids runtime-grant-ids
         :handler-provider-ids handler-provider-ids
         :handler-grant-ids handler-grant-ids
         :scope :authenticated-checked-core-reference-interpreter
         :phase :runtime
         :lifetime :single-reference-execution
         :single-invocation? true
         :reference-interpreter? true
         :deployment-runtime? false
         :live-external-io? false
         :adapter-authority adapter-authority}]
    (assoc base :authority-record-id
           (p15-s23-closed-core-digest base))))

(defn- p15-s23-checked-core-program-provider-record-valid?
  [record]
  (and
   (map? record)
   (= p15-s23-checked-core-program-provider-record-keys
      (set (keys record)))
   (= :gravity/p15-s23-program-provider-selection (:artifact record))
   (= 1 (:schema-version record))
   (= (p15-s23-closed-core-digest
       p15-s23-checked-core-expected-program-authority-policy)
      (:policy-hash record))
   (= :authenticated-runtime-contract-operation
      (:provider-provenance record))
   (false? (:source-declaration-is-grant? record))
   (false? (:live-external-authority? record))
   (= (:provider-selection-id record)
      (p15-s23-closed-core-digest
       (dissoc record :provider-selection-id)))))

(defn- p15-s23-checked-core-program-grant-record-valid?
  [record]
  (and
   (map? record)
   (= p15-s23-checked-core-program-grant-record-keys
      (set (keys record)))
   (= :gravity/p15-s23-program-capability-grant (:artifact record))
   (= 1 (:schema-version record))
   (= (p15-s23-closed-core-digest
       p15-s23-checked-core-expected-program-authority-policy)
      (:policy-hash record))
   (= :gravity.reference/runtime-audit-policy (:audit-policy-id record))
   (= :authenticated-runtime-contract-operation
      (:provider-provenance record))
   (false? (:source-declaration-is-grant? record))
   (false? (:live-external-authority? record))
   (= (:grant-id record)
      (p15-s23-closed-core-digest (dissoc record :grant-id)))))