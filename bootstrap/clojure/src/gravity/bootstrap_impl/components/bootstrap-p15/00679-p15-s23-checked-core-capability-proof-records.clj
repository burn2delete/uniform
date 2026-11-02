

(defn p15-s23-checked-core-capability-proof-records
  [nodes authority-evidence]
  (if-not (p15-s23-checked-core-authority-evidence-valid?
           authority-evidence)
    []
    (mapv
     (fn [[node capability]]
       (let [provider (get-in authority-evidence
                              [:provider-bindings capability])
             grant (get-in authority-evidence [:grant-bindings capability])
             effect (:effect provider)
             base
             {:artifact :gravity/p15-s23-checked-core-capability-proof
              :core-node-id (:node-id node)
              :effect effect
              :capability capability
              :program-principal (:program-principal authority-evidence)
              :provider-selection-id (:provider-selection-id provider)
              :provider-id (:provider-id provider)
              :grant-id (:grant-id grant)
              :authority-record-id (:authority-record-id authority-evidence)
              :authority-evidence-id (:evidence-id authority-evidence)
              :program-authority-policy-id
              (:program-authority-policy-id authority-evidence)
              :program-authority-policy-hash
              (:program-authority-policy-hash authority-evidence)
              :provider-policy-id (:policy-id provider)
              :provider-policy-hash (:policy-hash provider)
              :provider-provenance (:provider-provenance provider)
              :grant-policy-id (:policy-id grant)
              :grant-policy-hash (:policy-hash grant)
              :audit-policy-id (:audit-policy-id grant)
              :program-grant-template-id
              (:program-grant-template-id grant)
              :runtime-contract-definition-hash
              (:runtime-contract-definition-hash authority-evidence)
              :runtime-contract-derived-facts-hash
              (:runtime-contract-derived-facts-hash authority-evidence)
              :runtime-artifact-hash
              (:runtime-artifact-hash authority-evidence)
              :scope (:scope provider)
              :phase :runtime
              :lifetime :single-reference-execution
              :status :proved-for-authenticated-reference-interpreter}]
         (assoc base :proof-id (p15-s23-closed-core-digest base))))
     (sort-by
      (fn [[node capability]] [(:path node) (pr-str capability)])
      (for [node nodes
            capability (:capabilities node)]
        [node capability])))))

(def p15-s23-checked-core-capability-proof-record-keys
  #{:artifact :proof-id :core-node-id :effect :capability
    :program-principal :provider-selection-id :provider-id :grant-id
    :authority-record-id :authority-evidence-id
    :program-authority-policy-id :program-authority-policy-hash
    :provider-policy-id :provider-policy-hash :provider-provenance
    :grant-policy-id :grant-policy-hash :audit-policy-id
    :program-grant-template-id :runtime-contract-definition-hash
    :runtime-contract-derived-facts-hash :runtime-artifact-hash
    :scope :phase :lifetime :status})

(defn p15-s23-checked-core-capability-proof-record-valid?
  [record authority-evidence]
  (let [capability (:capability record)
        provider (get-in authority-evidence [:provider-bindings capability])
        grant (get-in authority-evidence [:grant-bindings capability])]
    (and
     (map? record)
     (= p15-s23-checked-core-capability-proof-record-keys
        (set (keys record)))
     (map? provider)
     (map? grant)
     (= :gravity/p15-s23-checked-core-capability-proof (:artifact record))
     (= :proved-for-authenticated-reference-interpreter (:status record))
     (= (:program-principal authority-evidence)
        (:program-principal record))
     (= (:authority-record-id authority-evidence)
        (:authority-record-id record))
     (= (:evidence-id authority-evidence)
        (:authority-evidence-id record))
     (= (:program-authority-policy-id authority-evidence)
        (:program-authority-policy-id record)
        (:policy-id provider)
        (:policy-id grant)
        (:provider-policy-id record)
        (:grant-policy-id record))
     (= (:program-authority-policy-hash authority-evidence)
        (:program-authority-policy-hash record)
        (:policy-hash provider)
        (:policy-hash grant)
        (:provider-policy-hash record)
        (:grant-policy-hash record))
     (= (:effect provider) (:effect grant) (:effect record))
     (= (:capability provider) (:capability grant) capability)
     (= (:scope provider) (:scope grant) (:scope record))
     (= (:provider-selection-id provider)
        (:provider-selection-id grant)
        (:provider-selection-id record))
     (= (:provider-id provider) (:provider-id grant)
        (:provider-id record))
     (= (:grant-id grant) (:grant-id record))
     (= (:provider-provenance provider)
        (:provider-provenance grant)
        (:provider-provenance record))
     (= (:audit-policy-id grant) (:audit-policy-id record)
        :gravity.reference/runtime-audit-policy)
     (= (:program-grant-template-id grant)
        (:program-grant-template-id record))
     (= (:runtime-contract-definition-hash authority-evidence)
        (:runtime-contract-definition-hash record))
     (= (:runtime-contract-derived-facts-hash authority-evidence)
        (:runtime-contract-derived-facts-hash record))
     (= (:runtime-artifact-hash authority-evidence)
        (:runtime-artifact-hash record))
     (= :runtime (:phase record))
     (= :single-reference-execution (:lifetime record))
     (= (:proof-id record)
        (p15-s23-closed-core-digest (dissoc record :proof-id))))))

(declare p15-s23-closed-core-intrinsic-effects
         p15-s23-closed-core-intrinsic-capabilities)

(defn p15-s23-checked-core-bind-runtime-check-authority
  [nodes authority-evidence]
  (let [proofs
        (into {}
              (map (fn [proof]
                     [[(:core-node-id proof) (:capability proof)] proof]))
              (p15-s23-checked-core-capability-proof-records
               nodes authority-evidence))]
    (mapv
     (fn [node]
       (if (= :runtime-checked (get-in node [:safety :outcome]))
         (let [capability
               (first
                (p15-s23-closed-core-intrinsic-capabilities
                 (:source-operation node)))
               provider (get-in authority-evidence
                                [:provider-bindings capability])
               grant (get-in authority-evidence [:grant-bindings capability])
               proof (get proofs [(:node-id node) capability])
               check (get-in node [:safety :check])
               check-base
               (merge
                (dissoc check :artifact :check-id :status)
                {:program-provider-selection-id
                 (:provider-selection-id provider)
                 :program-provider-id (:provider-id provider)
                 :program-grant-id (:grant-id grant)
                 :authority-record-id
                 (:authority-record-id authority-evidence)
                 :authority-evidence-id (:evidence-id authority-evidence)
                 :capability-proof-id (:proof-id proof)
                 :runtime-provider-id
                 (case capability
                   :memory/allocator
                   :gravity.reference/jvm-managed-allocator
                   :io/stdout :gravity.reference/transcript-capture)
                 :runtime-grant-id
                 (case capability
                   :memory/allocator
                   :gravity.reference/grant-managed-allocation
                   :io/stdout :gravity.reference/grant-reference-stdout)
                 :runtime-handler-provider-id
                 (if (= :io/stdout capability)
                   :gravity.reference/transcript-capture
                   :not-applicable)
                 :runtime-handler-grant-id
                 (if (= :io/stdout capability)
                   :gravity.reference/grant-test-fixture
                   :not-applicable)
                 :structural-invocation-state
                 :pre-execution-authority-bound})
               bound-check
               (assoc check-base
                      :artifact :gravity/runtime-check
                      :check-id (p15-s23-closed-core-digest check-base)
                      :status :required)]
           (-> node
               (assoc-in [:safety :check] bound-check)
               (assoc-in [:attributes :runtime-check-id]
                         (:check-id bound-check))))
         node))
     nodes)))

(defn p15-s23-checked-core-authority-safe-source-path?
  [source-path]
  (and (string? source-path)
       (<= (count source-path) 4096)
       (= :valid
          (:status
           (p15-s23-closed-core-bounded-utf8-count source-path 16384)))))

(defn p15-s23-checked-core-authority-small-map?
  [value maximum-count]
  (and (map? value)
       (contains? p15-s23-reference-runtime-supported-collection-class-names
                  (some-> value class .getName))
       (<= (count value) maximum-count)))