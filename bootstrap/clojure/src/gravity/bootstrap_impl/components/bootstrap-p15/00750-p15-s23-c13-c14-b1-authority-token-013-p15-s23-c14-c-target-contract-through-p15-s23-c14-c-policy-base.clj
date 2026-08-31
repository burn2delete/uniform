(let [p15-s23-c13-c14-b1-authority-token (nth __gravity_bootstrap_lexical_values_126914 0)]
(defn p15-s23-c14-c-target-contract
  []
  (let [base
        {:request :c
         :triple (:target-triple p15-s23-b3-llvm-policy)
         :data-layout (:data-layout p15-s23-b3-llvm-policy)
         :cpu (:cpu p15-s23-b3-llvm-policy)
         :features (:features p15-s23-b3-llvm-policy)
         :object-format :mach-o
         :architecture :arm64
         :relocation-model :pic
         :code-model :small
         :optimization-level :O0
         :minimum-os-version "14.0"
         :sanitizers []
         :instrumentation []
         :backend :gravity.backend/c
         :tier :experimental
         :exposure :internal
         :source-declaration-target :jvm
         :requested-lowering-target :c
         :selection :explicit-bootstrap-seed-target-override
         :reason :checked-core-seed-contract
         :direct-source-declared-c? false
         :profile-eligibility [:hosted]
         :dialect :c17}]
    (assoc base :fingerprint
           (p15-s23-c11-mir-digest
            {:kind :gravity/c14-bounded-c17-target-fingerprint
             :target base}))))

(defn p15-s23-c14-c-target-policy
  []
  {:scope :bounded-pure-scalar-forwarding-do-let-if-integer-comparisons
   :maximum-operation-count 128
   :dialect :c17
   :whole-c14? false :whole-b1? false :whole-b2? false
   :public? false :release? false :self-hosted? false})

(def p15-s23-c14-c-required-evidence
  [:authenticated-c11-replay
   :independent-lowering-reconstruction
   :hosted-c17-source-and-header
   :mach-o-arm64-object
   :differential-process-result
   :content-hash-and-provenance])

(def p15-s23-c14-c-unsupported-surface
  [:strings :quote :str :println :runtime-checks :effects
   :program-capabilities :domain-anchors :multiple-functions
   :multiple-conditionals :non-scalar-types
   :integer-outside-signed-i64 :process-result-outside-0-to-255
   :pointers :ffi :mmio :volatile :atomics :inline-assembly])

(defn p15-s23-c14-c-contract-bindings
  [c11-artifact checked-core c11-report c13-record dependencies]
  (let [mir (:mir-module c11-artifact)
        target (p15-s23-c14-c-target-contract)
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
       :verifier-result (get-in c13-record [:verifier-replay :result])}))))

(defn p15-s23-c14-c-policy-base
  [c11-artifact checked-core c11-report c13-record c13-rule]
  (let [mir (:mir-module c11-artifact)
        c11-verifier (p15-s23-b3-llvm-c11-verifier-record c11-report)
        c11-verifier-id (p15-s23-c11-mir-digest c11-verifier)
        target (p15-s23-c14-c-target-contract)
        abi (assoc (p15-s23-b3-llvm-expected-abi-contract)
                   :return-type :i32)
        runtime (p15-s23-b3-llvm-expected-runtime-contract)
        providers (p15-s23-b3-llvm-expected-provider-contract)
        fact-bindings
        (get-in c13-record [:semantic-identity :fact-bindings])
        source-map {:id (p15-s23-c11-mir-digest (:source-map mir))
                    :preserved? true}
        proofs
        (:proofs
         (p15-s23-b3-llvm-expected-b1-context-evidence
          c11-artifact checked-core c11-report))
        dependencies
        {:source-core (:artifact-id checked-core)
         :c11-source-rule (:source-rule c11-artifact)
         :c11-pass (get-in mir [:pass-execution-record :record-id])
         :c13-artifact (:artifact-id c13-record)
         :c13-source-rule c13-rule
         :b2-source p15-s23-b2-c17-expected-source-content-hash}
        contract-bindings
        (p15-s23-c14-c-contract-bindings
         c11-artifact checked-core c11-report c13-record dependencies)]
    {:expected-c11-artifact-id (:artifact-id c11-artifact)
     :expected-c13-artifact-id (:artifact-id c13-record)
     :profile :hosted
     :profile-contract (p15-s23-b3-llvm-expected-profile-contract)
     :target target
     :source-target-selection
     {:source-declaration-target :jvm
      :requested-lowering-target :c
      :selection :explicit-bootstrap-seed-target-override
      :reason :checked-core-seed-contract
      :direct-source-declared-c? false}
     :abi abi :runtime runtime :providers providers
     :effects #{} :capabilities #{}
     :safety {:outcomes (count (:safety-table mir))
              :runtime-checks (count (:runtime-check-table mir))
              :unsafe-islands 0
              :binding (:safety fact-bindings)}
     :proofs proofs
     :contract-bindings contract-bindings
     :required-evidence p15-s23-c14-c-required-evidence
     :source-map source-map
     :expected-source-map-binding source-map
     :dependencies dependencies
     :target-policy (p15-s23-c14-c-target-policy)
     :unsupported-surface p15-s23-c14-c-unsupported-surface
     :proof-target-metadata
     {:entries [] :proofless-metadata-rejected? true}
     :fact-bindings fact-bindings
     :supported-operation-ids
     (get-in c13-record [:semantic-identity :operation-order])
     :c11-verifier c11-verifier
     :c11-verifier-id c11-verifier-id})))
