(let [p15-s23-c13-c14-b1-authority-token (nth __gravity_bootstrap_lexical_values_126914 0)]
(defn p15-s23-c13-expected-record
  [mir evidence]
  {:artifact :gravity/c13-bounded-identity-optimized-mir
   :schema-version 1
   :status :accepted
   :input
   {:kind :gravity/mir
    :c11-artifact-id (:c11-artifact-id evidence)
    :c11-mir-id (:c11-mir-id evidence)
    :module-id (:module-id mir)
    :source-core (:source-core mir)
    :verifier-report-id (:verifier-report-id evidence)
    :verifier-status (:verifier-status evidence)
    :semantic-replay-parity (:semantic-replay-parity evidence)
    :pass-execution-record-id
    (get-in mir [:pass-execution-record :record-id])}
   :optimized-mir mir
   :pass-contract
   {:pass-id :c13-bounded-identity
    :family :identity
    :version 1
    :input-ir :gravity/mir
    :output-ir :gravity/optimized-mir
    :required-analyses [:c11-verifier-report]
    :preconditions [:mir-verifier-passed]
    :preserves [:type-facts :effect-facts :ownership-facts
                :capability-facts :safety-outcomes
                :runtime-check-table :proof-certificate-table
                :source-map :operation-order :effect-order-graph]
    :invalidates [] :regenerates []
    :proof-obligations [:semantic-identity-replay]
    :profile-constraints [(:profile evidence)]
    :target-assumptions []
    :effect-ordering-policy :unchanged
    :safety-policy :unchanged
    :domain-policy :unchanged
    :maximum-operation-count p15-s23-b3-llvm-max-bridge-operations
    :emits [:decision-record :invalidation-ledger
            :residual-check-report :verifier-replay]}
   :decision-record
   {:artifact :gravity/optimization-decision
    :pass-id :c13-bounded-identity
    :decision-id (:decision-id evidence)
    :input-mir (:c11-mir-id evidence)
    :output-mir (:c11-mir-id evidence)
    :decision :retain-input-unchanged
    :changed? false :changed-operations []
    :reason :bounded-bootstrap-identity-pass
    :preserved
    {:fact-bindings (:fact-bindings evidence)
     :operation-order (:operation-order evidence)
     :effect-order-graph (:effect-order-graph evidence)}
    :invalidated []
    :proofs-used [(:verifier-report-id evidence)]
    :residual-checks (:runtime-check-inventory evidence)
    :source-map (:source-map-binding evidence)
    :verifier-result :passed}
   :invalidation-ledger
   {:artifact :gravity/c13-invalidation-ledger
    :pass-id :c13-bounded-identity
    :decision-id (:decision-id evidence)
    :input-mir (:c11-mir-id evidence)
    :output-mir (:c11-mir-id evidence)
    :analysis-invalidated [] :facts-invalidated []
    :facts-regenerated [] :proofs-invalidated []
    :certificates-invalidated [] :runtime-checks-restored []
    :passes-to-rerun [] :caches-cleared []
    :diagnostics-affected []
    :profile (:profile evidence) :target (:target evidence)}
   :residual-check-report
   {:artifact :gravity/c13-residual-check-report
    :status :complete
    :retained-runtime-checks (:runtime-check-inventory evidence)
    :elided-runtime-checks [] :open-proof-obligations []}
   :verifier-replay
   {:artifact :gravity/c13-post-pass-verifier-replay
    :required? true
    :c11-artifact-id (:c11-artifact-id evidence)
    :verifier-report-id (:verifier-report-id evidence)
    :c11-mir-id (:c11-mir-id evidence)
    :expected-input-module-id (:module-id evidence)
    :expected-output-module-id (:module-id evidence)
    :fact-bindings (:fact-bindings evidence)
    :semantic-identity-required? true :result :passed}
   :semantic-identity
   {:c11-input-mir-id (:c11-mir-id evidence)
    :c11-output-mir-id (:c11-mir-id evidence)
    :input-module-id (:module-id evidence)
    :output-module-id (:module-id evidence)
    :fact-bindings (:fact-bindings evidence)
    :operation-order (:operation-order evidence)
    :operation-count (count (:operation-order evidence))
    :maximum-operation-count p15-s23-b3-llvm-max-bridge-operations
    :effect-order-graph (:effect-order-graph evidence)
    :unchanged? true}
   :target-instruction-selection :forbidden
   :diagnostics []
   :semantic-authority :gravity-source
   :execution-tcb :clojure-stage0-rule-runner
   :clojure-seed-boundary? true
   :self-hosted? false})

(defn p15-s23-c13-c14-b1-seal-stage
  [kind raw source-rule actual-path-provenance]
  (let [base (assoc raw
                    :source-rule source-rule
                    :actual-path-provenance actual-path-provenance)
        semantic-id
        (p15-s23-c11-mir-digest
         {:kind kind
          :record (p15-s23-c13-c14-b1-stage-semantic-input base)})
        artifact-id
        (p15-s23-c11-mir-digest
         {:kind kind :schema-version 1 :semantic-id semantic-id})]
    (assoc base
           :semantic-id semantic-id
           :artifact-id artifact-id
           :actual-path-binding-id
           (p15-s23-c13-c14-b1-actual-path-binding-id
            semantic-id actual-path-provenance))))

(defn- p15-s23-c13-build-for-target!
  [candidate source-path c11-artifact c11-report binding target]
  (let [mir (:mir-module c11-artifact)
        evidence
        (p15-s23-c13-evidence-for-target c11-artifact c11-report target)
        raw
        (p15-s23-c13-c14-b1-invoke!
         candidate source-path binding p15-s23-c13-builder-function
         [mir evidence] "C13-VERIFY")
        expected (p15-s23-c13-expected-record mir evidence)]
    (p15-s23-c11-mir-require-strict-structure!
     source-path expected raw :independent-c13-identity-reconstruction)
    (when-not (and (= expected raw)
                   (= mir (:optimized-mir raw))
                   (= (:mir-id c11-artifact)
                      (get-in raw [:input :c11-mir-id])
                      (get-in raw [:semantic-identity :c11-input-mir-id])
                      (get-in raw [:semantic-identity :c11-output-mir-id]))
                   (= (:module-id mir)
                      (get-in raw [:input :module-id])
                      (get-in raw [:semantic-identity :input-module-id])
                      (get-in raw [:semantic-identity :output-module-id])))
      (p15-s23-b3-llvm-fail!
       "C13-VERIFY" source-path raw
       {:missing-fact :exact-c11-bound-c13-identity-replay}))
    (p15-s23-c13-c14-b1-seal-stage
     :gravity/c13-bounded-identity-optimized-mir
     raw
     (p15-s23-c13-c14-b1-source-rule
      :gravity.compiler/c13-mir-optimization binding
      p15-s23-c13-builder-function)
     {:source source-path
      :c11-source (get-in c11-artifact
                          [:provenance :actual-paths :c11-source])
      :c13-source (:source-path binding)})))

(defn- p15-s23-c13-build!
  [candidate source-path c11-artifact c11-report binding]
  (p15-s23-c13-build-for-target!
   candidate source-path c11-artifact c11-report binding :llvm-x86_64-linux))

(defn p15-s23-c14-target-contract
  []
  (let [base
        (assoc (p15-s23-b3-llvm-expected-target-contract)
               :profile-eligibility [:hosted])]
    (assoc base :fingerprint
           (p15-s23-c11-mir-digest
            {:kind :gravity/c14-bounded-llvm-target-fingerprint
             :target base}))))

(defn p15-s23-c14-target-policy
  []
  {:scope :bounded-pure-scalar-forwarding-do-let-if-integer-comparisons
   :maximum-operation-count p15-s23-b3-llvm-max-bridge-operations
   :whole-c14? false :whole-b1? false :whole-b3? false
   :public? false :release? false :self-hosted? false})

(def p15-s23-c14-llvm-unsupported-surface
  [:strings :quote :str :println :runtime-checks :effects
   :program-capabilities :domain-anchors :multiple-functions
   :multiple-conditionals :non-scalar-types
   :integer-outside-signed-i64 :process-result-outside-0-to-255])

(defn p15-s23-c14-contract-bindings
  [c11-artifact checked-core c11-report c13-record dependencies]
  (let [target (p15-s23-c14-target-contract)
        abi (assoc (p15-s23-b3-llvm-expected-abi-contract)
                   :return-type :i32)
        fact-bindings
        (get-in c13-record [:semantic-identity :fact-bindings])]
    (assoc
     (p15-s23-b3-llvm-contract-bindings
      c11-artifact checked-core c11-report)
     :target (p15-s23-c13-c14-b1-content-binding target)
     :abi (p15-s23-c13-c14-b1-content-binding abi)
     :dependencies
     (p15-s23-c13-c14-b1-content-binding dependencies)
     :type (:type fact-bindings)
     :ownership (:ownership fact-bindings)
     :c13-optimization
     (p15-s23-c13-c14-b1-content-binding
      {:artifact-id (:artifact-id c13-record)
       :semantic-id (:semantic-id c13-record)
       :decision-id (get-in c13-record [:decision-record :decision-id])
       :verifier-result (get-in c13-record [:verifier-replay :result])})))))
