(let [p15-s23-c13-c14-b1-authority-token (nth __gravity_bootstrap_lexical_values_126914 0)]
(defn p15-s23-c14-c-expected-record
  [optimized policy]
  (let [request
        {:artifact :gravity/c14-internal-target-lowering-request
         :schema-version 1 :status :accepted
         :input
         {:kind :gravity/mir
          :id (:semantic-id optimized)
          :artifact-id (:artifact-id optimized)
          :c11-artifact-id (:expected-c11-artifact-id policy)
          :verified? true
          :verifier-report (:c11-verifier policy)
          :verifier-report-id (:c11-verifier-id policy)
          :optimization-status :identity-pass-complete
          :optimization-report
          {:artifact-id (:artifact-id optimized)
           :semantic-id (:semantic-id optimized)
           :decision-id (get-in optimized [:decision-record :decision-id])
           :verifier-result (get-in optimized [:verifier-replay :result])}
          :domain-status :not-applicable}
         :request-id (:request-id policy)
         :profile (:profile policy)
         :profile-contract (:profile-contract policy)
         :target (:target policy)
         :source-target-selection (:source-target-selection policy)
         :abi (:abi policy) :runtime (:runtime policy)
         :providers (:providers policy)
         :effects (:effects policy) :capabilities (:capabilities policy)
         :safety (:safety policy) :proofs (:proofs policy)
         :contract-bindings (:contract-bindings policy)
         :required-evidence (:required-evidence policy)
         :source-map (:source-map policy)
         :dependency-provenance (:dependencies policy)
         :c13-optimization-status :identity-pass-complete
         :domain-ir-status :not-applicable :fusion-status :not-run
         :target-policy (:target-policy policy)
         :proof-to-target-metadata (:proof-target-metadata policy)
         :unsupported-feature-report
         {:status :bounded-surface-only
          :policy (:unsupported-surface policy)
          :diagnostic "C14-UNSUPPORTED" :fallback-status :rejected}
         :diagnostics []}
        payload
        {:artifact :gravity/c14-bounded-c-lowering-payload
         :c11-artifact-id (:expected-c11-artifact-id policy)
         :c13-artifact-id (:artifact-id optimized)
         :c13-semantic-id (:semantic-id optimized)
         :mir (:optimized-mir optimized)
         :operation-order
         (get-in optimized [:semantic-identity :operation-order])
         :fact-bindings (:fact-bindings policy)}
        eligibility
        {:artifact :gravity/c14-target-eligibility-report
         :backend :gravity.backend/c
         :profile (:profile policy) :target (:target policy)
         :accepted? true :rejections [] :fallbacks []
         :missing-features [] :required-providers (:providers policy)
         :proof-assumptions []
         :explainability-record
         {:decision :accepted
          :bounded-surface
          :pure-scalar-forwarding-do-let-if-integer-comparisons
          :dialect :c17
          :unsupported-diagnostic "C14-UNSUPPORTED"
          :no-hidden-runtime? true
          :no-hidden-effect-or-capability? true}
         :checks
         [:profile-allows-backend
          :target-supports-required-mir-families
          :runtime-services-explicit :abi-represents-exports
          :effects-have-authority-preserving-providers
          :proof-assumptions-valid-for-target]}]
    {:artifact :gravity/c14-bounded-c-lowering-record
     :schema-version 1 :status :accepted
     :request request
     :bounded-lowering-payload payload
     :eligibility eligibility
     :abi-layout
     {:artifact :gravity/c14-abi-layout-record
      :abi (:abi policy) :target (:target policy)
      :type-facts (get-in policy [:fact-bindings :type])
      :ownership-facts (get-in policy [:fact-bindings :ownership])
      :safety-facts (get-in policy [:fact-bindings :safety])
      :profile-contract (:profile-contract policy)
      :source-map (:source-map policy)}
     :runtime-provider
     {:artifact :gravity/c14-runtime-provider-record
      :runtime (:runtime policy) :providers (:providers policy)
      :effects (:effects policy) :capabilities (:capabilities policy)}
     :proof-target-metadata
     {:artifact :gravity/c14-proof-target-metadata-map
      :entries (get-in policy [:proof-target-metadata :entries])
      :proofless-metadata-rejected?
      (get-in policy [:proof-target-metadata
                      :proofless-metadata-rejected?])
      :proofs (:proofs policy)}
     :dependency-provenance (:dependencies policy)
     :diagnostics [] :semantic-authority :gravity-source
     :execution-tcb :clojure-stage0-rule-runner
     :clojure-seed-boundary? true :whole-c14? false
     :self-hosted? false}))

(defn p15-s23-c14-c-policy
  [c11-artifact checked-core c11-report c13-record c13-rule]
  (let [base
        (p15-s23-c14-c-policy-base
         c11-artifact checked-core c11-report c13-record c13-rule)
        request-base
        (:request
         (p15-s23-c14-c-expected-record
          c13-record (assoc base :request-id nil)))
        request-id
        (p15-s23-c11-mir-digest
         {:kind :gravity/c14-bounded-c-lowering-request
          :request (dissoc request-base :request-id)})]
    (assoc base :request-id request-id)))

(defn- p15-s23-c14-c-build!
  [candidate source-path c11-artifact checked-core c11-report
   c13-record binding]
  (let [c13-rule (:source-rule c13-record)
        policy
        (p15-s23-c14-c-policy
         c11-artifact checked-core c11-report c13-record c13-rule)
        raw
        (p15-s23-c13-c14-b1-invoke!
         candidate source-path binding p15-s23-c14-c-builder-function
         [c13-record policy] "C14-INPUT")
        expected (p15-s23-c14-c-expected-record c13-record policy)]
    (p15-s23-c11-mir-require-strict-structure!
     source-path expected raw :independent-c14-c-lowering-reconstruction)
    (when-not
     (and (= expected raw)
          (= (:artifact-id c13-record)
             (get-in raw [:request :input :artifact-id])
             (get-in raw [:bounded-lowering-payload :c13-artifact-id]))
          (= (:semantic-id c13-record)
             (get-in raw [:bounded-lowering-payload :c13-semantic-id]))
          (= (:artifact-id c11-artifact)
             (get-in raw [:request :input :c11-artifact-id])
             (get-in raw [:bounded-lowering-payload :c11-artifact-id]))
          (= (:optimized-mir c13-record)
             (get-in raw [:bounded-lowering-payload :mir]))
          (= (:request-id policy) (get-in raw [:request :request-id]))
          (= (:target policy)
             (get-in raw [:request :target])
             (get-in raw [:eligibility :target])))
      (p15-s23-b3-llvm-fail!
       "C14-MANIFEST" source-path raw
       {:missing-fact :exact-c13-bound-c14-c-reconstruction}))
    (p15-s23-c13-c14-b1-seal-stage
     :gravity/c14-bounded-c-lowering-record raw
     (p15-s23-c13-c14-b1-source-rule
      :gravity.compiler/c14-target-lowering binding
      p15-s23-c14-c-builder-function)
     {:source source-path
      :c11-source (get-in c11-artifact
                          [:provenance :actual-paths :c11-source])
      :c13-source (get-in c13-record
                          [:actual-path-provenance :c13-source])
      :c14-source (:source-path binding)})))

(defn p15-s23-b1-backend-manifest
  [c14-record]
  {:artifact :gravity/backend-manifest
   :backend :gravity.backend/llvm
   :version :bounded-authenticated-v1
   :accepts [:gravity/mir]
   :emits [:llvm-ir :elf-x86_64-object :elf-x86_64-executable]
   :requires [:profile-manifest :target-manifest :abi-policy
              :runtime-provider-selection :effect-summary
              :capability-proof-summary :safety-bundle :proof-table
              :source-map :dependency-graph]
   :supports-profiles [:hosted]
   :rejects [:unverified-ir :unsupported-op :missing-proof
             :implicit-ub :ambient-capability :profile-violation]
   :c14-request-id (get-in c14-record [:request :request-id])
   :c14-artifact-id (:artifact-id c14-record)}))
