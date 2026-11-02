(ns gravity.lowering
  "Native C14 reconstruction data and exact joins; authority remains with existing owners.")

(def ^:private namespace-contract
  (quote
{:authenticates-inputs? false,
 :public-api
 {assert-c-reconstruction!
  {:arglists
   ([require-strict-structure!
     fail!
     source-path
     c11-artifact
     c13-record
     policy
     expected
     raw]),
   :admission :none},
  assert-llvm-reconstruction!
  {:arglists
   ([require-strict-structure!
     fail!
     source-path
     c13-record
     policy
     expected
     raw]),
   :admission :none},
  assert-wasm-reconstruction!
  {:arglists
   ([require-strict-structure! fail! source-path expected raw]),
   :admission :none},
  c-record {:arglists ([optimized policy]), :admission :none},
  llvm-record {:arglists ([optimized policy]), :admission :none},
  wasm-record {:arglists ([optimized policy]), :admission :none}},
 :supported
 [:existing-llvm-c-wasm-reconstruction-bodies
  :existing-context-joins
  :unchanged-identity-and-provenance-fields
  :late-var-diagnostic-interposition],
 :dependency-direction
 {:requires [],
  :consumed-by [gravity.bootstrap],
  :forbids
  [gravity.bootstrap
   gravity.cli
   gravity.release
   gravity.c14-lowering]},
 :legacy-status-labels-authoritative? false,
 :preserves [:caller-supplied-identities :source-proof-fact-associations :upstream-context :diagnostic-call-order],
 :invalidates [],
 :regenerates [:native-reconstruction-records],
 :governing-contracts
 [D1
  D2
  D3
  D6
  D8
  D9
  C1
  C11
  C12
  C13
  C14
  C15
  B1
  B2
  B3
  B4
  B13
  B14
  BOOT8
  TEST2],
 :unsupported
 [:untrusted-native-ingress
  :callback-authentication
  :canonical-c11-c13-admission
  :domain-ir-admission
  :profile-target-abi-runtime-provider-proof-legality
  :general-lowering
  :public-lower-or-verify
  :canonical-c14-completion
  :source-identity-binding
  :backend-emission-or-execution
  :migration
  :release
  :self-hosting
  :seed-retirement],
 :artifact-inputs
 [:caller-owned-optimized-mir-and-policy-data
  :expected-and-actual-c14-record-data
  :c11-c13-context-data
  :explicit-strict-comparison-and-diagnostic-operations],
 :namespace gravity.lowering,
 :diagnostic-ownership
 {:stage
  ["C14-MANIFEST" :exact-reconstruction-or-context-join-mismatch],
  :propagated ["C11-VERIFY" :strict-structure-mismatch],
  :existing-owner
  [:canonical-c11-c13-admission
   :target-legality
   :diagnostic-publication
   :source-verification
   :sealing-and-authority]},
 :artifact-outputs
 [:raw-llvm-c-wasm-c14-reconstruction-data
  :exact-reconstruction-or-refusal],
 :issues-certificates? false,
 :contract-boundary :native-c14-reconstruction-data}
))

(defn llvm-record
  [optimized policy]
  (let [request
        {:artifact :gravity/c14-internal-target-lowering-request
         :schema-version 1 :status :accepted
         :input
         {:kind :gravity/mir
          :id (:semantic-id optimized)
          :artifact-id (:artifact-id optimized)
          :verified? true
          :verifier-report (:c11-verifier policy)
          :verifier-report-id (:c11-verifier-id policy)
          :optimization-status (if (= :gravity/c13-bounded-equality-fold-optimized-mir (:artifact optimized))
                                 :constant-fold-pass-complete :identity-pass-complete)
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
         :c13-optimization-status (if (= :gravity/c13-bounded-equality-fold-optimized-mir (:artifact optimized))
                                    :constant-fold-pass-complete :identity-pass-complete)
         :domain-ir-status :not-applicable :fusion-status :not-run
         :target-policy (:target-policy policy)
         :proof-to-target-metadata (:proof-target-metadata policy)
         :unsupported-feature-report
         {:status :bounded-surface-only
          :policy (:unsupported-surface policy)
          :diagnostic "C14-UNSUPPORTED" :fallback-status :rejected}
         :diagnostics []}
        eligibility
        {:artifact :gravity/c14-target-eligibility-report
         :backend :gravity.backend/llvm
         :profile (:profile policy) :target (:target policy)
         :accepted? true :rejections [] :fallbacks []
         :missing-features [] :required-providers (:providers policy)
         :proof-assumptions []
         :explainability-record
         {:decision :accepted
          :bounded-surface :pure-scalar-forwarding-do-let-if-integer-comparisons
          :unsupported-diagnostic "C14-UNSUPPORTED"
          :no-hidden-runtime? true
          :no-hidden-effect-or-capability? true}
         :checks
         [:profile-allows-backend
          :target-supports-required-mir-families
          :runtime-services-explicit :abi-represents-exports
          :effects-have-authority-preserving-providers
          :proof-assumptions-valid-for-target]}]
    {:artifact :gravity/c14-bounded-llvm-lowering-record
     :schema-version 1 :status :accepted
     :request request :eligibility eligibility
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
     :clojure-seed-boundary? true :self-hosted? false}))


(defn assert-llvm-reconstruction!
  [require-strict-structure! fail! source-path c13-record policy expected raw]
    (require-strict-structure!
     source-path expected raw :independent-c14-lowering-reconstruction)
    (when-not (and (= expected raw)
                   (= (:artifact-id c13-record)
                      (get-in raw [:request :input :artifact-id]))
                   (= (:request-id policy)
                      (get-in raw [:request :request-id]))
                   (= (:target policy)
                      (get-in raw [:request :target])
                      (get-in raw [:eligibility :target]))
                   (= (:providers policy)
                      (get-in raw [:request :providers])
                      (get-in raw [:eligibility :required-providers])))
      (fail!
       "C14-MANIFEST" source-path raw
       {:missing-fact :exact-c13-bound-c14-reconstruction}))
)

(defn c-record
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


(defn assert-c-reconstruction!
  [require-strict-structure! fail! source-path c11-artifact c13-record policy expected raw]
    (require-strict-structure!
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
      (fail!
       "C14-MANIFEST" source-path raw
       {:missing-fact :exact-c13-bound-c14-c-reconstruction}))
)

(defn wasm-record [optimized policy]
  (let [request
        {:artifact :gravity/c14-internal-target-lowering-request
         :schema-version 1 :status :accepted
         :input
         {:kind :gravity/mir :id (:semantic-id optimized)
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
        {:artifact :gravity/c14-bounded-wasm-lowering-payload
         :c11-artifact-id (:expected-c11-artifact-id policy)
         :c13-artifact-id (:artifact-id optimized)
         :c13-semantic-id (:semantic-id optimized)
         :mir (:optimized-mir optimized)
         :operation-order
         (get-in optimized [:semantic-identity :operation-order])
         :fact-bindings (:fact-bindings policy)}]
    {:artifact :gravity/c14-bounded-wasm-lowering-record
     :schema-version 1 :status :accepted
     :request request :bounded-lowering-payload payload
     :eligibility
     {:artifact :gravity/c14-target-eligibility-report
      :backend :gravity.backend/wasm
      :profile (:profile policy) :target (:target policy)
      :accepted? true :rejections [] :fallbacks []
      :missing-features [] :required-providers (:providers policy)
      :proof-assumptions []
      :explainability-record
      {:decision :accepted
       :bounded-surface
       :pure-scalar-forwarding-do-let-if-integer-comparisons
       :unsupported-diagnostic "C14-UNSUPPORTED"
       :no-hidden-runtime? true
       :no-hidden-effect-or-capability? true}
      :checks
      [:profile-allows-backend
       :target-supports-required-mir-families
       :runtime-services-explicit :abi-represents-exports
       :effects-have-authority-preserving-providers
       :proof-assumptions-valid-for-target]}
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
     :clojure-seed-boundary? true :self-hosted? false}))


(defn assert-wasm-reconstruction!
  [require-strict-structure! fail! source-path expected raw]
    (require-strict-structure!
     source-path expected raw :independent-c14-wasm-reconstruction)
    (when-not (= expected raw)
      (fail!
       "C14-MANIFEST" source-path raw
       {:missing-fact :exact-c13-bound-c14-wasm-reconstruction}))
)

(doseq [[_ operation] (ns-publics *ns*)]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
