(let [p15-s23-c13-c14-b1-authority-token (nth __gravity_bootstrap_lexical_values_126914 0)]
(defn p15-s23-c14-policy-base
  [c11-artifact checked-core c11-report c13-record c13-rule]
  (let [mir (:mir-module c11-artifact)
        c11-verifier (p15-s23-b3-llvm-c11-verifier-record c11-report)
        c11-verifier-id (p15-s23-c11-mir-digest c11-verifier)
        target (p15-s23-c14-target-contract)
        abi (assoc (p15-s23-b3-llvm-expected-abi-contract)
                   :return-type :i32)
        runtime (p15-s23-b3-llvm-expected-runtime-contract)
        providers (p15-s23-b3-llvm-expected-provider-contract)
        fact-bindings (get-in c13-record
                              [:semantic-identity :fact-bindings])
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
         :b3-source p15-s23-b3-llvm-expected-source-content-hash}
        contract-bindings
        (p15-s23-c14-contract-bindings
         c11-artifact checked-core c11-report c13-record dependencies)]
    {:expected-c11-artifact-id (:artifact-id c11-artifact)
     :expected-c13-artifact-id (:artifact-id c13-record)
     :profile :hosted
     :profile-contract (p15-s23-b3-llvm-expected-profile-contract)
     :target target
     :source-target-selection
     (p15-s23-b3-llvm-expected-source-target-selection)
     :abi abi :runtime runtime :providers providers
     :effects #{} :capabilities #{}
     :safety {:outcomes (count (:safety-table mir))
              :runtime-checks (count (:runtime-check-table mir))
              :unsafe-islands 0
              :binding (:safety fact-bindings)}
     :proofs proofs
     :contract-bindings contract-bindings
     :required-evidence (:required-evidence p15-s23-b3-llvm-policy)
     :source-map source-map
     :expected-source-map-binding source-map
     :dependencies dependencies
     :target-policy (p15-s23-c14-target-policy)
     :unsupported-surface p15-s23-c14-llvm-unsupported-surface
     :proof-target-metadata
     {:entries [] :proofless-metadata-rejected? true}
     :fact-bindings fact-bindings
     :supported-operation-ids
     (get-in c13-record [:semantic-identity :operation-order])
     :c11-verifier c11-verifier
     :c11-verifier-id c11-verifier-id}))

(defn p15-s23-c14-expected-record
  [optimized policy]
  (gravity.lowering/llvm-record optimized policy))

(defn p15-s23-c14-policy
  [c11-artifact checked-core c11-report c13-record c13-rule]
  (let [base (p15-s23-c14-policy-base
              c11-artifact checked-core c11-report c13-record c13-rule)
        request-base
        (:request (p15-s23-c14-expected-record
                   c13-record (assoc base :request-id nil)))
        request-id
        (p15-s23-c11-mir-digest
         {:kind :gravity/c14-bounded-llvm-lowering-request
          :request (dissoc request-base :request-id)})]
    (assoc base :request-id request-id)))

(defn- p15-s23-c14-build!
  [candidate source-path c11-artifact checked-core c11-report
   c13-record binding]
  (let [c13-rule (:source-rule c13-record)
        policy (p15-s23-c14-policy
                c11-artifact checked-core c11-report c13-record c13-rule)
        raw
        (p15-s23-c13-c14-b1-invoke!
         candidate source-path binding p15-s23-c14-builder-function
         [c13-record policy] "C14-INPUT")
        expected (p15-s23-c14-expected-record c13-record policy)]
    (gravity.lowering/assert-llvm-reconstruction!
     #'p15-s23-c11-mir-require-strict-structure! #'p15-s23-b3-llvm-fail! source-path c13-record policy expected raw)
    (p15-s23-c13-c14-b1-seal-stage
     :gravity/c14-bounded-llvm-lowering-record raw
     (p15-s23-c13-c14-b1-source-rule
      :gravity.compiler/c14-target-lowering binding
      p15-s23-c14-builder-function)
     {:source source-path
      :c11-source (get-in c11-artifact
                          [:provenance :actual-paths :c11-source])
      :c13-source (get-in c13-record
                          [:actual-path-provenance :c13-source])
      :c14-source (:source-path binding)})))

(declare p15-s23-b2-c17-expected-source-content-hash
         p15-s23-c14-c-builder-function))
