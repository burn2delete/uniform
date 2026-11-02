(let [p15-s23-b2-c17-gate-b-authority-token (nth __gravity_bootstrap_lexical_values_131964 0)
      p15-s23-b2-c17-gate-b-tool-state (nth __gravity_bootstrap_lexical_values_131964 1)]
(defn p15-s23-c14-wasm-policy-base
  [c11-artifact checked-core c11-report c13-record c13-rule]
  (let [mir (:mir-module c11-artifact)
        c11-verifier (p15-s23-b3-llvm-c11-verifier-record c11-report)
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
         :backend-contract :gravity.backend/b4-bounded-wasm32-core-v1}]
    {:expected-c11-artifact-id (:artifact-id c11-artifact)
     :expected-c13-artifact-id (:artifact-id c13-record)
     :profile :hosted
     :profile-contract {:name :hosted :validated? true}
     :target (p15-s23-c14-wasm-target-contract)
     :source-target-selection
     (p15-s23-c14-wasm-source-target-selection)
     :abi (p15-s23-c14-wasm-abi)
     :runtime (p15-s23-c14-wasm-runtime)
     :providers [] :effects #{} :capabilities #{}
     :safety {:outcomes (count (:safety-table mir))
              :runtime-checks (count (:runtime-check-table mir))
              :unsafe-islands 0 :binding (:safety fact-bindings)}
     :proofs proofs
     :contract-bindings
     (p15-s23-c14-wasm-contract-bindings
      c11-artifact checked-core c11-report c13-record dependencies)
     :required-evidence p15-s23-c14-wasm-required-evidence
     :source-map source-map :expected-source-map-binding source-map
     :dependencies dependencies
     :target-policy (p15-s23-c14-wasm-target-policy)
     :unsupported-surface p15-s23-c14-wasm-unsupported-surface
     :proof-target-metadata
     {:entries [] :proofless-metadata-rejected? true}
     :fact-bindings fact-bindings
     :supported-operation-ids
     (get-in c13-record [:semantic-identity :operation-order])
     :c11-verifier c11-verifier
     :c11-verifier-id (p15-s23-c11-mir-digest c11-verifier)}))

(defn p15-s23-c14-wasm-expected-record
  [optimized policy]
  (gravity.lowering/wasm-record optimized policy))

(defn p15-s23-c14-wasm-policy
  [c11-artifact checked-core c11-report c13-record c13-rule]
  (let [base
        (p15-s23-c14-wasm-policy-base
         c11-artifact checked-core c11-report c13-record c13-rule)
        request
        (:request
         (p15-s23-c14-wasm-expected-record
          c13-record (assoc base :request-id nil)))]
    (assoc base :request-id
           (p15-s23-c11-mir-digest
            {:kind :gravity/c14-bounded-wasm-lowering-request
             :request (dissoc request :request-id)}))))

(defn- p15-s23-c14-wasm-build!
  [candidate source-path c11-artifact checked-core c11-report
   c13-record binding]
  (let [policy
        (p15-s23-c14-wasm-policy
         c11-artifact checked-core c11-report c13-record
         (:source-rule c13-record))
        raw
        (p15-s23-c13-c14-b1-invoke!
         candidate source-path binding p15-s23-c14-wasm-builder-function
         [c13-record policy] "C14-INPUT")
        expected (p15-s23-c14-wasm-expected-record c13-record policy)]
    (gravity.lowering/assert-wasm-reconstruction!
     #'p15-s23-c11-mir-require-strict-structure! #'p15-s23-b4-wasm-fail! source-path expected raw)
    (p15-s23-c13-c14-b1-seal-stage
     :gravity/c14-bounded-wasm-lowering-record raw
     (p15-s23-c13-c14-b1-source-rule
      :gravity.compiler/c14-target-lowering binding
      p15-s23-c14-wasm-builder-function)
     {:source source-path
      :c11-source (get-in c11-artifact
                          [:provenance :actual-paths :c11-source])
      :c13-source (get-in c13-record
                          [:actual-path-provenance :c13-source])
      :c14-source (:source-path binding)})))

(defn p15-s23-b1-wasm-backend-manifest [c14-record]
  {:artifact :gravity/backend-manifest
   :backend :gravity.backend/wasm
   :version :bounded-authenticated-wasm32-core-v1
   :accepts [:gravity/mir]
   :emits [:wasm-core-module]
   :requires [:profile-manifest :target-manifest :wasm-feature-policy
              :abi-policy :runtime-provider-selection :effect-summary
              :capability-proof-summary :safety-bundle :proof-table
              :source-map :dependency-graph]
   :supports-profiles [:hosted]
   :rejects [:unverified-ir :unsupported-op :missing-proof
             :implicit-ub :ambient-capability :profile-violation
             :unsupported-wasm-feature]
   :target-kind :core-module :memory-width :wasm32
   :features #{} :imports [] :runtime-helpers []
   :c14-request-id (get-in c14-record [:request :request-id])
   :c14-artifact-id (:artifact-id c14-record)}))
