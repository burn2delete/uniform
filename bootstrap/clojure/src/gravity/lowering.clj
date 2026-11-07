(ns gravity.lowering
  "Native Stage0 instruction lowering, C14 reconstruction and backend document checks; authority remains with existing owners."
  (:require [clojure.set :as set]))

(def ^:private namespace-contract
  '{:regenerates
 [:stage0-native-instructions :stage0-native-opcode-frequency-map :native-reconstruction-records :native-c14-artifact-and-projection],
 :authenticates-inputs? false,
 :public-api
 {compile-native-expression
  {:arglists ([operations module locals form]), :admission :none,
   :required-operations [:compile-expression :compile-collection :compile-let :builtin-functions :fail!],
   :contract-boundary :stage0-native-instruction-lowering},
  compile-native-collection
  {:arglists ([expression-var module locals form]), :admission :none,
   :contract-boundary :stage0-native-instruction-lowering},
  compile-native-let
  {:arglists ([failure-var expression-var module locals bindings body]), :admission :none,
   :contract-boundary :stage0-native-instruction-lowering},
  compile-native-function
  {:arglists ([expression-var module {:keys [name params body] :as definition}]), :admission :none,
   :contract-boundary :stage0-native-instruction-lowering},
  native-instruction-summary
  {:arglists ([summary-var instruction]), :admission :none,
   :contract-boundary :stage0-native-instruction-lowering},
  assert-wasm-reconstruction!
  {:arglists
   ([require-strict-structure! fail! source-path expected raw]),
   :admission :none},
  native-artifact-base
  {:arglists
   ([configuration
     source-path
     _source-text
     module
     source-overrides
     optimization-artifact
     diagnostics
     operations]),
   :admission :none},
  wasm-record {:arglists ([optimized policy]), :admission :none},
  native-capability-proof
  {:arglists ([configuration artifact operations]), :admission :none},
  native-source-overrides {:arglists ([module]), :admission :none},
  validate-native-backends!
  {:authenticates-inputs? false,
   :required-operations
   [:fail! :manifest-required-fields :diagnostic-ids],
   :admission :none,
   :diagnostic-publication :caller-owned,
   :grants-authority? false,
   :artifact-inputs [:unverified-native-backend-document-data],
   :arglists ([operations source-path artifact]),
   :artifact-outputs [:legacy-structural-completion-or-refusal],
   :issues-certificates? false,
   :contract-boundary :native-backend-document-checks},
  native-validate!
  {:arglists ([configuration source-path artifact operations]),
   :admission :none},
  native-backend-artifact-manifest
  {:authenticates-inputs? false,
   :required-operations [:artifact-id],
   :admission :none,
   :grants-authority? false,
   :artifact-inputs
   [:unverified-backend-kind-content-input-and-evidence-data],
   :arglists ([operations backend kind content input-id evidence-id]),
   :artifact-outputs [:legacy-native-backend-artifact-manifest],
   :issues-certificates? false,
   :contract-boundary :native-backend-document-manifest},
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
  native-backends-coverage-report
  {:authenticates-inputs? false,
   :required-operations [:manifest-required-fields :diagnostic-ids],
   :admission :none,
   :grants-authority? false,
   :artifact-inputs [:unverified-native-backend-document-data],
   :arglists ([operations artifact]),
   :artifact-outputs [:legacy-native-backend-coverage-record],
   :issues-certificates? false,
   :contract-boundary :native-backend-document-coverage},
  native-source-overrides-artifact
  {:arglists ([overrides]), :admission :none},
  c-record {:arglists ([optimized policy]), :admission :none},
  native-diagnostic-catalog
  {:arglists ([configuration source-path input-id source-span]),
   :admission :none},
  llvm-record {:arglists ([optimized policy]), :admission :none},
  native-source-artifact
  {:arglists ([configuration source-path source-text operations]),
   :admission :none},
  native-file-artifact
  {:arglists ([configuration path operations]), :admission :none},
  assert-c-reconstruction!
  {:arglists
   ([require-strict-structure!
     fail!
     source-path
     c11-artifact
     c13-record
     policy
     expected
     raw]),
   :admission :none}},
 :supported
 [:existing-stage0-native-expression-collection-let-function-and-summary-bodies
  :late-native-recursion-helper-catalog-and-failure-vars
  :existing-llvm-c-wasm-reconstruction-bodies
  :existing-context-joins
  :unchanged-identity-and-provenance-fields
  :late-var-diagnostic-interposition
  :existing-native-c14-source-and-file-artifact-assembly
  :existing-native-overrides-catalog-projection-and-ordered-validation
  :trusted-zero-argument-operation-suppliers
  :native-backend-manifest-construction
  :native-backend-ordered-document-checks
  :native-backend-coverage-projection],
 :dependency-direction
 {:requires [clojure.set],
  :consumed-by
  [gravity.bootstrap
   gravity.c14-lowering.artifact
   gravity.c14-lowering.projection
   gravity.c14-lowering.validation],
  :must-not-require
  [gravity.bootstrap
   gravity.cli
   gravity.release
   gravity.c14-lowering]},
 :invalidates [],
 :legacy-status-labels-authoritative? false,
 :governing-contracts
 [L2 C6 BOOT1 D1
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
  SAFE15
  PERF10
  BOOT8
  TEST2],
 :preserves
 [:caller-supplied-identities
  :source-proof-fact-associations
  :upstream-context
  :diagnostic-call-order],
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
  :seed-retirement
  :native-hash-content-authentication
  :native-prefix-check-as-integrity
  :native-proof-or-conformance-labels-as-authority],
 :artifact-inputs
 [:trusted-stage0-native-module-local-scope-forms-and-function-data
  :trusted-stage0-instruction-and-explicit-live-operation-vars
  :caller-owned-optimized-mir-and-policy-data
  :expected-and-actual-c14-record-data
  :c11-c13-context-data
  :explicit-strict-comparison-and-diagnostic-operations
  :trusted-native-configuration-source-module-and-c13-parent-data
  :trusted-late-operation-suppliers
  :unverified-native-backend-document-data
  :native-manifest-constructor-arguments
  :late-native-backend-operations],
 :namespace gravity.lowering,
 :diagnostic-ownership
 {:stage
  ["L2-UNKNOWN-SYMBOL" "L2-UNKNOWN-CORE-FORM" "L2-LET-BINDING" "P4-HOST-REFLECTION"
   "C14-MANIFEST"
   :exact-reconstruction-or-context-join-mismatch
   :existing-native-c14-ordered-structural-refusals
   "C14-INPUT"
   "C14-PROFILE"
   "C14-TARGET"
   "C14-ABI"
   "C14-RUNTIME"
   "C14-CAPABILITY"
   "C14-PROVIDER"
   "C14-PROOF-METADATA"
   "C14-UNSUPPORTED"
   :native-backend-ordered-structural-decisions
   "B13-EVIDENCE"
   "B14-COVERAGE"
   "B2-MANIFEST"
   "B2-UB"
   "B3-MANIFEST"
   "B3-METADATA"
   "B7-MANIFEST"
   "B7-VERIFY"
   "B13-SCHEMA"
   "B13-HASH"
   "B14-METADATA"
   "B14-EVIDENCE"
   "B14-NEGATIVE"],
  :propagated ["C11-VERIFY" :strict-structure-mismatch],
  :existing-owner
  [:stage0-source-function-header-and-whole-plan-admission
   :stage0-native-diagnostic-construction-and-publication
   :stage0-plan-identity-and-execution
   :canonical-c11-c13-admission
   :target-legality
   :diagnostic-publication
   :source-verification
   :sealing-and-authority
   :native-backend-failure-payload-assembly
   :native-backend-selected-failure-operation]},
 :artifact-outputs
 [:stage0-native-expression-instructions
  :stage0-native-function-binding-and-instructions
  :stage0-native-opcode-frequency-map
  :raw-llvm-c-wasm-c14-reconstruction-data
  :exact-reconstruction-or-refusal
  :native-c14-artifact-or-refusal
  :native-c14-diagnostics-and-capability-projection
  :legacy-native-backend-artifact-manifest
  :legacy-structural-completion-or-refusal
  :legacy-native-backend-coverage-record],
 :consumer-component-paths
 ["bootstrap/clojure/src/gravity/bootstrap_impl/components/foundation/00554-invoke-stage0-builtin.clj"
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/foundation/00555-compile-stage0-expr.clj"
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00209-native-lowering-source-overrides.clj"
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00210-native-lowering-source-artifact.clj"
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00211-native-lowering-file-artifact.clj"],
 :issues-certificates? false,
 :contract-boundary :native-lowering-reconstruction-document-data-and-stage0-instructions}
)

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

(defn native-source-overrides [module]
  (or (get-in module [:metadata :compiler :c14-lowering])
      (get-in module [:metadata :compiler :optimization-lowering]) {}))
(defn native-source-overrides-artifact [overrides]
  {:source-overrides overrides
   :lowering-request {:profile :hosted :target {:backend :jvm}}
   :input "sha256:stage0-c14-source-override"})
(defn native-diagnostic-catalog [configuration source-path input-id source-span]
  (let [span (source-span source-path 0)]
    {:artifact :gravity/c14-lowering-diagnostic-catalog :status :complete
     :diagnostics
     (mapv (fn [id]
             {:diagnostic id :input-artifact-id input-id :mir-operation "c14-diagnostic-op"
              :domain-anchor "c14-diagnostic-anchor" :source-span span :origin-chain []
              :profile :hosted :target :jvm :backend :jvm :missing-feature :catalog-entry
              :proof-expected :proof/c14-diagnostic-catalog :provider-expected :jvm/provider
              :fallback-status :available
              :remediation (get (:optimization-lowering-diagnostic-messages configuration) id)})
           (:c14-lowering-diagnostic-ids configuration))}))
(defn native-validate! [configuration source-path artifact operations]
  (((:invoke operations)) :optimization-lowering-validate-overrides!
                     ((:validate-overrides operations)) source-path artifact)
  (let [request (:lowering-request artifact)
        providers (:provider-selection-records artifact)
        metadata (get-in artifact [:proof-to-target-metadata-map :entries])
        unsupported (:unsupported-feature-report artifact)
        diagnostics (get-in artifact [:lowering-diagnostic-stream :diagnostics])]
    (when-not (= :optimized-mir (get-in request [:input :kind]))
      (((:fail! operations)) "C14-INPUT" source-path artifact request {:missing-fields [:input]}))
    (when-not (= :eligible (get-in artifact [:target-eligibility-report :status]))
      (((:fail! operations)) "C14-PROFILE" source-path artifact (:target-eligibility-report artifact)
             {:missing-fields [:target-eligibility]}))
    (when-not (((:perf-present? operations)) (get-in request [:target :features]))
      (((:fail! operations)) "C14-TARGET" source-path artifact request {:missing-fields [:target :features]}))
    (doseq [[id path expected missing-field]
            [["C14-ABI" [:abi-manifest :status] :complete :abi-manifest]
             ["C14-RUNTIME" [:runtime-provider-manifest :status] :complete :runtime-provider]
             ["C14-CAPABILITY" [:capability-preservation-report :status] :preserved
              :capability-preservation]]]
      (when-not (= expected (get-in artifact path))
        (((:fail! operations)) id source-path artifact (get-in artifact [(first path)])
               {:missing-fields [missing-field]})))
    (when-not (every? #(= :selected (:status %)) providers)
      (((:fail! operations)) "C14-PROVIDER" source-path artifact (first providers) {:missing-fields [:provider-selection]}))
    (when-not (every? #(((:perf-present? operations)) (:proof %)) metadata)
      (((:fail! operations)) "C14-PROOF-METADATA" source-path artifact (:proof-to-target-metadata-map artifact)
             {:missing-fields [:proof]}))
    (when-not (every? #(= :available (:fallback-status %)) unsupported)
      (((:fail! operations)) "C14-UNSUPPORTED" source-path artifact (first unsupported) {:missing-fields [:unsupported-feature]}))
    (when-not (= :gravity/target-artifact-manifest (get-in artifact [:target-artifact-manifest :artifact]))
      (((:fail! operations)) "C14-MANIFEST" source-path artifact (:target-artifact-manifest artifact)
             {:missing-fields [:target-artifact-manifest]}))
    (when-not (= (set (:c14-lowering-diagnostic-ids configuration))
                 (set (map :diagnostic diagnostics)))
      (((:fail! operations)) "C14-MANIFEST" source-path artifact (:lowering-diagnostic-stream artifact)
             {:missing-fields [:lowering-diagnostics]})))
  :complete)
(defn native-capability-proof [configuration artifact operations]
  {:c13-optimized-mir-input-verified?
   (= :complete (get-in artifact [:c13-optimization-artifact :capability-based-proof :status]))
   :lowering-request-verified? (= :optimized-mir (get-in artifact [:lowering-request :input :kind]))
   :target-eligible? (= :eligible (get-in artifact [:target-eligibility-report :status]))
   :abi-manifest-complete? (= :complete (get-in artifact [:abi-manifest :status]))
   :runtime-provider-recorded? (= :complete (get-in artifact [:runtime-provider-manifest :status]))
   :providers-selected? (every? #(= :selected (:status %)) (:provider-selection-records artifact))
   :proof-metadata-linked? (every? #(((:perf-present? operations)) (:proof %)) (get-in artifact [:proof-to-target-metadata-map :entries]))
   :source-proof-safety-metadata-preserved?
   (and (= :complete (get-in artifact [:source-generated-origin-map :status]))
        (((:perf-present? operations)) (get-in artifact [:target-artifact-manifest :proof-map]))
        (((:perf-present? operations)) (get-in artifact [:target-artifact-manifest :safety])))
   :capabilities-preserved? (= :preserved (get-in artifact [:capability-preservation-report :status]))
   :unsupported-fallbacks-recorded? (every? #(= :available (:fallback-status %)) (:unsupported-feature-report artifact))
   :manifest-complete? (= :gravity/target-artifact-manifest (get-in artifact [:target-artifact-manifest :artifact]))
   :diagnostics-covered? (= (set (:c14-lowering-diagnostic-ids configuration))
                            (set (map :diagnostic (get-in artifact [:lowering-diagnostic-stream :diagnostics]))))
   :status :complete})

(defn native-artifact-base [configuration source-path _source-text module source-overrides optimization-artifact diagnostics operations]
  (let [optimized-mir (:optimized-mir-artifact optimization-artifact)
        input-id (:artifact-id optimization-artifact)
        target {:backend :jvm :triple "jvm-17" :features #{:objects :exceptions :threads}}
        lowering-request {:artifact :gravity/lowering-request
                          :input {:kind :optimized-mir :id input-id :optimized-mir (:output optimized-mir)}
                          :profile :hosted :target target :abi :jvm-hosted-stage0 :runtime :hosted-jvm
                          :providers {:allocator :jvm/gc :panic :jvm/exception :io :jvm/stdout}
                          :required-evidence {:safety :mir/safety-table :proofs :proof/c14-stage0
                                              :capabilities :mir/capability-proof-table}}
        proof-map {:artifact :gravity/proof-target-metadata-map :target :jvm
                   :entries [{:target-metadata :bounds-check-elided
                              :operation "mir-op-optimized-bounds-check-elide" :proof :proof/c13-bounds-check-elision}
                             {:target-metadata :noalias
                              :operation "mir-op-optimized-target-layout-prepare" :proof :proof/c13-layout-ownership}
                             {:target-metadata :nonnull
                              :operation "mir-op-optimized-dead-code-eliminate" :proof :proof/c13-safety-preserved}]}
        target-artifacts [{:kind :jvm-bytecode-plan
                           :hash (str "sha256:" (((:sha256-hex operations)) (pr-str (:output optimized-mir))))}]]
    {:kind :gravity/stage0-c14-target-lowering-artifact
     :task "P06-D093" :document-set ["C14"]
     :governing-document (:c14-lowering-governing-document configuration)
     :pass {:name :c14-target-lowering :input :optimized-mir :output :target-artifact-manifest
            :requires [:c13-optimized-mir :profile :target :abi :runtime :providers :effects
                       :capabilities :safety :proofs]
            :preserves [:source-spans :origin-chain :profile :target :effects :capabilities
                        :safety :proofs :dependencies]
            :emits [:lowering-request :target-eligibility-report :abi-manifest
                    :runtime-provider-manifest :provider-selection-records :layout-decision-record
                    :proof-to-target-metadata-map :source-generated-origin-map
                    :unsupported-feature-report :target-artifact-manifest :lowering-diagnostic-stream]
            :rejects (:c14-lowering-diagnostic-ids configuration)}
     :source-overrides source-overrides
     :module (select-keys module [:module :source-path :profile :target :effects :capabilities :safety :metadata])
     :c13-optimization-artifact
     (select-keys optimization-artifact [:kind :task :artifact-id :governing-document
                                         :optimized-mir-artifact :capability-based-proof])
     :optimization-artifact-kind (:kind optimization-artifact)
     :optimization-artifact-hash input-id
     :lowering-request lowering-request
     :target-eligibility-report {:artifact :gravity/target-eligibility-report :status :eligible
                                 :profile :hosted :target target :backend :jvm
                                 :reason :profile-target-provider-compatible}
     :abi-manifest {:artifact :gravity/abi-manifest :status :complete :calling-convention :jvm-static
                    :exported-symbols ["compiler_c14_lowering_main"] :data-layout :jvm-object
                    :alignment :jvm-default :enum-representation :jvm-tagged-object
                    :closure-representation :jvm-function-object :panic-strategy :exception
                    :resource-handle-representation :jvm-object-ref :ffi-boundary :jvm-interop
                    :gc-policy :jvm-gc :debug-unwind :jvm-stacktrace}
     :runtime-provider-manifest {:artifact :gravity/runtime-provider-manifest :status :complete
                                 :runtime :hosted-jvm :providers (:providers lowering-request)}
     :provider-selection-records
     [{:provider :jvm/gc :capability :memory/allocator :effect :memory/allocate :status :selected}
      {:provider :jvm/stdout :capability :io/stdout :effect :io/write :status :selected}
      {:provider :jvm/exception :capability :panic/raise :effect :error/throw :status :selected}]
     :layout-decision-record {:artifact :gravity/layout-decision-record :status :complete
                              :alignment :jvm-default :proof :proof/c13-layout-ownership
                              :ownership-facts :mir/ownership-table :safety-facts :mir/safety-table}
     :proof-to-target-metadata-map proof-map
     :source-generated-origin-map {:artifact :gravity/source-generated-origin-map :status :complete
                                   :source-map (get-in optimization-artifact [:optimized-mir-artifact :source-origin-map])
                                   :generated-origin-map []}
     :capability-preservation-report {:artifact :gravity/capability-preservation-report :status :preserved
                                      :denied-additions [] :preserved-capabilities (:capabilities module)}
     :unsupported-feature-report
     [{:mir-op "c11-mir-op-gpu-kernel" :required-feature :gpu-kernel :backend :jvm :profile :hosted
       :source-span (((:source-span operations)) source-path 0) :available-alternatives [:mir-scalar-kernel]
       :fallback :mir-scalar-kernel :fallback-status :available :diagnostic-id "C14-UNSUPPORTED"}]
     :target-artifact-manifest
     {:artifact :gravity/target-artifact-manifest :input (:output optimized-mir) :backend :jvm
      :profile :hosted :target (str "sha256:" (((:sha256-hex operations)) (pr-str target))) :artifacts target-artifacts
      :source-map :gravity/source-generated-origin-map :proof-map :gravity/proof-target-metadata-map
      :effects :mir/effect-table :capabilities :mir/capability-proof-table :safety :mir/safety-table
      :runtime :gravity/runtime-provider-manifest :dependencies input-id :diagnostics []}
     :lowering-diagnostic-stream diagnostics
     :c14-lowering-results {:documents ["C14"] :task "P06-D093"
                            :required-diagnostic-ids (:c14-lowering-diagnostic-ids configuration)
                            :c13-input-status :complete :lowering-request-status :complete
                            :target-eligibility-status :complete :abi-status :complete
                            :runtime-provider-status :complete :proof-metadata-status :complete
                            :manifest-status :complete :diagnostic-status :complete :status :complete}
     :diagnostics []}))

(defn native-source-artifact [configuration source-path source-text operations]
  (let [records (((:invoke operations)) :read-source-form-records (((:unsupported operations)) :read-source-form-records)
                                   source-path source-text)
        forms (mapv :form records)
        _ (((:invoke operations)) :validate-ns-syntax! (((:unsupported operations)) :validate-ns-syntax!) source-path forms)
        module (((:invoke operations)) :parse-module (((:unsupported operations)) :parse-module) source-path forms)
        source-overrides (((:invoke operations)) :c14-lowering-source-overrides ((:source-overrides operations)) module)
        _ (((:invoke operations)) :c14-lowering-validate-source-overrides!
                             (fn [path overrides]
                               (((:invoke operations)) :optimization-lowering-validate-overrides!
                                                  ((:validate-overrides operations))
                                                  path (((:source-overrides-artifact operations)) overrides)))
                             source-path source-overrides)
        optimization-artifact (((:invoke operations)) :compiler-c13-optimization-source-artifact
                                                 (((:unsupported operations)) :compiler-c13-optimization-source-artifact)
                                                 source-path source-text)
        input-id (:artifact-id optimization-artifact)
        diagnostics (((:diagnostic-catalog operations)) configuration source-path input-id)
        artifact-base (((:artifact-base operations)) configuration source-path source-text module source-overrides
                                               optimization-artifact diagnostics)
        _ (((:invoke operations)) :c14-lowering-validate!
                             (fn [path artifact] (((:validate! operations)) configuration path artifact))
                             source-path artifact-base)
        capability-proof (((:invoke operations)) :c14-lowering-capability-proof
                                            (fn [artifact] (((:capability-proof operations)) configuration artifact))
                                            artifact-base)]
    (assoc artifact-base :capability-based-proof capability-proof
           :artifact-id (((:artifact-id operations)) (assoc artifact-base :capability-based-proof capability-proof)))))
(defn native-file-artifact [configuration path operations]
  (((:invoke operations)) :compiler-c14-lowering-source-artifact
                     (fn [source-path source-text] (((:source-artifact operations)) configuration source-path source-text))
                     path (((:slurp operations)) path)))

(defn- native-backend-port [operations key]
  (let [operation (get operations key)]
    (if (var? operation) @operation operation)))

(defn native-backend-artifact-manifest
  [operations backend kind content input-id evidence-id]
  {:schema-version 1
   :kind kind
   :backend backend
   :profile :native
   :target :x86_64-stage0
   :content-hash ((native-backend-port operations :artifact-id) content)
   :inputs {:source input-id
            :mir input-id
            :backend-interface input-id}
   :evidence {:safety "safety-bundle:stage0"
              :proofs evidence-id
              :capabilities "capability-summary:stage0"
              :effects "effect-summary:stage0"
              :conformance "backend-conformance-pack:p07-t02"}
   :provenance {:compiler "gravity-stage0-clojure"
                :passes ["C14" "B1" "B2/B3/B7" "B13" "B14"]
                :dependencies "dependency-graph:stage0"}
   :reproducibility {:timestamp-policy :none
                     :nondeterminism []
                     :status :recorded}})

(defn validate-native-backends!
  [operations source-path artifact]
  (let [diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:native-diagnostic-stream
                                       :diagnostics])))
        manifests (:artifact-manifests artifact)]
    (when-not (= :complete (get-in artifact
                                   [:backend-interface-artifact
                                    :capability-based-proof :status]))
      ((native-backend-port operations :fail!) "B13-EVIDENCE" source-path
                             (:backend-interface-artifact artifact)
                             {:missing-fields [:backend-interface-proof]}))
    (when-not (= #{:gravity.backend/c :gravity.backend/llvm
                   :gravity.backend/mlir}
                 (set (map :backend
                           (:target-lowering-manifest artifact))))
      ((native-backend-port operations :fail!) "B14-COVERAGE" source-path
                             (:target-lowering-manifest artifact)
                             {:missing-fields [:native-backends]}))
    (when-not (= :complete (get-in artifact [:c-backend :status]))
      ((native-backend-port operations :fail!) "B2-MANIFEST" source-path
                             (:c-backend artifact)
                             {:missing-fields [:c-backend]}))
    (when-not (= :rejected (get-in artifact
                                   [:c-backend :ub-rejection :status]))
      ((native-backend-port operations :fail!) "B2-UB" source-path
                             (get-in artifact [:c-backend :ub-rejection])
                             {:missing-fields [:ub-rejection]}))
    (when-not (= :complete (get-in artifact [:llvm-backend :status]))
      ((native-backend-port operations :fail!) "B3-MANIFEST" source-path
                             (:llvm-backend artifact)
                             {:missing-fields [:llvm-backend]}))
    (when-not (= :gated (get-in artifact
                                [:llvm-backend :metadata-policy
                                 :status]))
      ((native-backend-port operations :fail!) "B3-METADATA" source-path
                             (get-in artifact
                                     [:llvm-backend :metadata-policy])
                             {:missing-fields [:llvm-metadata-policy]}))
    (when-not (= :complete (get-in artifact [:mlir-backend :status]))
      ((native-backend-port operations :fail!) "B7-MANIFEST" source-path
                             (:mlir-backend artifact)
                             {:missing-fields [:mlir-backend]}))
    (when-not (= :passed (get-in artifact
                                 [:mlir-backend :verifier-report
                                  :status]))
      ((native-backend-port operations :fail!) "B7-VERIFY" source-path
                             (get-in artifact
                                     [:mlir-backend :verifier-report])
                             {:missing-fields [:mlir-verifier]}))
    (when-not (every? #(set/subset? (set (native-backend-port operations :manifest-required-fields))
                                    (set (keys %)))
                      manifests)
      ((native-backend-port operations :fail!) "B13-SCHEMA" source-path
                             (first manifests)
                             {:missing-fields [:artifact-manifest-schema]}))
    (when-not (every? #(re-find #"^sha256:" (:content-hash %)) manifests)
      ((native-backend-port operations :fail!) "B13-HASH" source-path
                             (first manifests)
                             {:missing-fields [:content-hash]}))
    (when-not (= :preserved (get-in artifact
                                    [:metadata-preservation-report
                                     :status]))
      ((native-backend-port operations :fail!) "B14-METADATA" source-path
                             (:metadata-preservation-report artifact)
                             {:missing-fields [:metadata-preservation]}))
    (when-not (= :passed (get-in artifact
                                 [:backend-conformance-record :status]))
      ((native-backend-port operations :fail!) "B14-EVIDENCE" source-path
                             (:backend-conformance-record artifact)
                             {:missing-fields [:conformance-evidence]}))
    (when-not (= (set (native-backend-port operations :diagnostic-ids)) diagnostics)
      ((native-backend-port operations :fail!) "B14-NEGATIVE" source-path
                             (:native-diagnostic-stream artifact)
                             {:missing-fields [:native-diagnostics]})))
  :complete)

(defn native-backends-coverage-report
  [operations artifact]
  (let [diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:native-diagnostic-stream
                                       :diagnostics])))
        manifests (:artifact-manifests artifact)]
    {:backend-interface-input-verified?
     (= :complete (get-in artifact
                          [:backend-interface-artifact
                           :capability-based-proof :status]))
     :c-lowering-complete?
     (= :complete (get-in artifact [:c-backend :status]))
     :c-undefined-behavior-rejected?
     (= :rejected (get-in artifact [:c-backend :ub-rejection :status]))
     :llvm-lowering-complete?
     (= :complete (get-in artifact [:llvm-backend :status]))
     :llvm-proof-metadata-gated?
     (= :gated (get-in artifact
                       [:llvm-backend :metadata-policy :status]))
     :mlir-lowering-complete?
     (= :complete (get-in artifact [:mlir-backend :status]))
     :mlir-conversion-verified?
     (= :passed (get-in artifact
                        [:mlir-backend :conversion-legality-report
                         :status]))
     :artifact-emission-complete?
     (and (= 3 (count manifests))
          (every? #(set/subset?
                    (set (native-backend-port operations :manifest-required-fields))
                    (set (keys %)))
                  manifests)
          (every? #(re-find #"^sha256:" (:content-hash %)) manifests))
     :source-proof-capability-metadata-preserved?
     (= :preserved (get-in artifact
                           [:metadata-preservation-report :status]))
     :conformance-record-passed?
     (= :passed (get-in artifact
                        [:backend-conformance-record :status]))
     :diagnostics-covered?
     (= (set (native-backend-port operations :diagnostic-ids)) diagnostics)
     :status :complete}))
(defn compile-native-collection
  [expression-var module locals form]
  (cond
    (vector? form)
    {:op :vector-literal
     :items (mapv #((deref expression-var) module locals %) form)}

    (map? form)
    {:op :map-literal
     :entries (mapv (fn [[k v]]
                      {:key ((deref expression-var) module locals k)
                       :value ((deref expression-var) module locals v)})
                    (sort-by (fn [[k v]] (pr-str [k v])) form))}

    (set? form)
    {:op :set-literal
     :items (mapv #((deref expression-var) module locals %)
                  (sort-by pr-str form))}

    :else
    {:op :literal :value form}))

(defn compile-native-let
  [failure-var expression-var module locals bindings body]
  (when-not (and (vector? bindings) (even? (count bindings)))
    ((deref failure-var) "L2-LET-BINDING"
           "let requires an even binding vector"
           {:source-span {:source (:source-path module)}
            :bindings bindings
            :remediation "Use pairs of local names and expressions in let."}))
  (loop [scope locals
         compiled []
         pairs (partition 2 bindings)]
    (if-let [[name expr] (first pairs)]
      (do
        (when-not (symbol? name)
          ((deref failure-var) "L2-LET-BINDING"
                 "let binding name must be a symbol"
                 {:source-span {:source (:source-path module)}
                  :binding name
                  :remediation "Bind symbols in stage0 let forms."}))
        (recur (conj scope name)
               (conj compiled {:name name
                               :expr ((deref expression-var) module scope expr)})
               (rest pairs)))
      {:op :let
       :bindings compiled
       :body (mapv #((deref expression-var) module scope %) body)})))(defn compile-native-expression
  [operations module locals form]
  (cond
    (symbol? form)
    (if (contains? locals form)
      {:op :local :name form}
      ((deref (:fail! operations)) "L2-UNKNOWN-SYMBOL"
             "stage0 cannot resolve symbol"
             {:source-span {:source (:source-path module)}
              :symbol form
              :remediation "Define the symbol or stay within the stage0 subset."}))

    (seq? form)
    (case (first form)
      println {:op :println
               :effect :io/write
               :capability :io/stdout
               :args (mapv #((deref (:compile-expression operations)) module locals %) (rest form))}
      do {:op :do
          :body (mapv #((deref (:compile-expression operations)) module locals %) (rest form))}
      if (let [[_ test then else] form]
           {:op :if
            :test ((deref (:compile-expression operations)) module locals test)
            :then ((deref (:compile-expression operations)) module locals then)
            :else ((deref (:compile-expression operations)) module locals else)})
      let (let [[_ bindings & body] form]
            ((deref (:compile-let operations)) module locals bindings body))
      quote {:op :quote :value (second form)}
      host-reflect ((deref (:fail! operations)) "P4-HOST-REFLECTION"
                          "host reflection is not implemented in stage0"
                          {:source-span {:source (:source-path module)}
                           :remediation "Remove host reflection from the stage0 executable subset."})
      (let [callee (first form)
            args (mapv #((deref (:compile-expression operations)) module locals %) (rest form))]
        (cond
          (contains? (deref (:builtin-functions operations)) callee)
          {:op :builtin-call :function callee :args args}

          (contains? (:function-table module) callee)
          {:op :function-call :function callee :args args}

          :else
          ((deref (:fail! operations)) "L2-UNKNOWN-CORE-FORM"
                 "stage0 cannot execute this form"
                 {:source-span {:source (:source-path module)}
                  :operator callee
                  :remediation "Use defn, println, do, if, let, quote, supported core builtins, or local function calls in the stage0 hosted subset."}))))

    (or (vector? form) (map? form) (set? form))
    ((deref (:compile-collection operations)) module locals form)

    :else
    {:op :literal :value form}))

(defn compile-native-function
  [expression-var module {:keys [name params body] :as definition}]
  (assoc definition
         :binding {:name name
                   :kind :function
                   :namespace (:module module)
                   :profile (:profile module)
                   :target (:target module)
                   :visibility (if (seq (:exports module))
                                 (if (contains? (set (:exports module)) name)
                                   :public
                                   :private)
                                 :stage0-local)
                   :effects (:effects module)
                   :capabilities (:capabilities module)}
         :instructions (mapv #((deref expression-var) module (set params) %) body)))

(defn native-instruction-summary
  [summary-var instruction]
  (let [children
        (case (:op instruction)
          :println (:args instruction)
          :do (:body instruction)
          :if [(:test instruction) (:then instruction) (:else instruction)]
          :let (concat (map :expr (:bindings instruction)) (:body instruction))
          :loop (concat (map :expr (:bindings instruction))
                        (:body instruction))
          :recur (:args instruction)
          :throw [(:value instruction)]
          :builtin-call (:args instruction)
          :function-call (:args instruction)
          :compiler-module-call (:args instruction)
          :vector-literal (:items instruction)
          :set-literal (:items instruction)
          :map-literal (mapcat (fn [{:keys [key value]}] [key value])
                                (:entries instruction))
          [])]
    ;; A parent and child may share an opcode (for example nested builtin
    ;; calls).  Add the parent fact instead of letting `merge` overwrite it;
    ;; this makes the stage0 comparison describe every concrete instruction.
    (merge-with +
                {(:op instruction) 1}
                (apply merge-with +
                       (map (deref summary-var)
                            (remove nil? children))))))


(doseq [[_ operation] (ns-publics *ns*)]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
