(let [static-rebuild-token (nth __gravity_bootstrap_checked_core_authority_values 0)
      p15-s23-stage2-closed-checked-core-source-artifact-internal __gravity_bootstrap_checked_core_source_helper
      p15-s23-stage2-closed-checked-core-rebuild-internal __gravity_bootstrap_checked_core_rebuild_helper]
(def p15-s23-checked-core-verification-replay-binding-keys
  #{:checked-core-artifact-id :mapping-id :provenance-binding-id
    :source-content-hash :plan-id :module :runtime-source-content-hash
    :runtime-artifact-hash :runtime-contract-definition-hash
    :runtime-derived-facts-hash :runtime-function :runtime-function-hash
    :verification-policy-id :verification-policy-hash
    :verification-audit-policy-hash :structural-operation-set
    :concrete-core-operation-set :reissued-program-authority-record-id
    :reissued-program-authority-evidence-id})

(defn p15-s23-checked-core-verification-replay-authority-structurally-valid?
  [authority]
  (try
    (and
     (map? authority)
     (contains?
      p15-s23-reference-runtime-supported-collection-class-names
      (some-> authority class .getName))
     (<= (count authority) 32)
     (do
       (p15-s23-reference-runtime-bounded-value!
        "checked-core-verification-replay-authority" :jvm
        :verification-replay-authority authority
        p15-s23-reference-runtime-max-contract-nodes
        p15-s23-reference-runtime-max-contract-depth)
       true)
     (= p15-s23-checked-core-verification-replay-authority-keys
        (set (keys authority)))
     (= p15-s23-checked-core-verification-replay-policy-id
        (:policy-id authority))
     (= (p15-s23-reference-runtime-hash
         p15-s23-checked-core-expected-verification-replay-policy)
        (:policy-hash authority))
     (= p15-s23-checked-core-verification-replay-audit-policy-id
        (:audit-policy-id authority))
     (= (p15-s23-reference-runtime-hash
         p15-s23-checked-core-expected-verification-replay-audit-policy)
        (:audit-policy-hash authority))
     (= p15-s23-checked-core-verification-replay-binding-keys
        (set (keys (:binding authority))))
     (= p15-s23-stage2-runtime-artifact-expected-source-content-hash
        (get-in authority [:binding :runtime-source-content-hash]))
     (= p15-s23-stage2-runtime-artifact-expected-artifact-hash
        (get-in authority [:binding :runtime-artifact-hash]))
     (= p15-s23-reference-runtime-expected-contract-definition-hash
        (get-in authority [:binding :runtime-contract-definition-hash]))
     (= p15-s23-reference-runtime-expected-derived-facts-hash
        (get-in authority [:binding :runtime-derived-facts-hash]))
     (= p15-s23-stage2-runtime-artifact-closed-plan-function
        (get-in authority [:binding :runtime-function]))
     (= (get p15-s23-reference-runtime-expected-function-hashes
             p15-s23-stage2-runtime-artifact-closed-plan-function)
        (get-in authority [:binding :runtime-function-hash]))
     (= p15-s23-checked-core-verification-replay-policy-id
        (get-in authority [:binding :verification-policy-id]))
     (set? (get-in authority [:binding :structural-operation-set]))
     (set/subset? (get-in authority [:binding :structural-operation-set])
                  #{:str :println})
     (set? (get-in authority [:binding :concrete-core-operation-set]))
     (vector? (:provider-selection-records authority))
     (vector? (:grant-records authority))
     (= (:authority-record-id authority)
        (p15-s23-reference-runtime-hash
         (dissoc authority :authority-record-id)))
     (every?
      (fn [record]
        (= (:provider-selection-record-id record)
           (p15-s23-reference-runtime-hash
            (dissoc record :provider-selection-record-id))))
      (:provider-selection-records authority))
     (every?
      (fn [record]
        (= (:grant-record-id record)
           (p15-s23-reference-runtime-hash
            (dissoc record :grant-record-id))))
      (:grant-records authority)))
    (catch StackOverflowError _ false)
    (catch Exception _ false)))

(defn p15-s23-checked-core-verification-replay-provider-preflight!
  [authority]
  (let [writes-stdout?
        (contains? (get-in authority [:binding :structural-operation-set])
                   :println)
        expected-providers
        (cond-> #{:verifier-managed-allocation :managed-allocation}
          writes-stdout?
          (conj :verifier-transcript-fixture :transcript-capture))
        expected-grants
        (cond-> #{:verifier-managed-allocation :managed-allocation}
          writes-stdout?
          (conj :verifier-transcript-fixture
                :transcript-capture :fixture))]
    (when-not
     (and (p15-s23-checked-core-verification-replay-authority-structurally-valid?
           authority)
          (= (set (map :role (:provider-selection-records authority)))
             expected-providers)
          (= (set (map :role (:grant-records authority)))
             expected-grants))
      (throw (ex-info "verification replay provider preflight rejected"
                      {:missing-fact
                       :exact-verification-replay-provider-grant-closure}))))
  :passed))
