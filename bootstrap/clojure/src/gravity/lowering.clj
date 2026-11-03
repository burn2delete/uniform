(ns gravity.lowering
  "Native C14 reconstruction and artifact pipelines; authority remains with existing owners.")

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
  wasm-record {:arglists ([optimized policy]), :admission :none},
  native-source-overrides {:arglists ([module]), :admission :none},
  native-source-overrides-artifact {:arglists ([overrides]), :admission :none},
  native-diagnostic-catalog {:arglists ([configuration source-path input-id source-span]), :admission :none},
  native-artifact-base
  {:arglists ([configuration source-path source-text module source-overrides optimization-artifact diagnostics operations]),
   :admission :none},
  native-validate! {:arglists ([configuration source-path artifact operations]), :admission :none},
  native-capability-proof {:arglists ([configuration artifact operations]), :admission :none},
  native-source-artifact {:arglists ([configuration source-path source-text operations]), :admission :none},
  native-file-artifact {:arglists ([configuration path operations]), :admission :none}},
 :supported
 [:existing-llvm-c-wasm-reconstruction-bodies
  :existing-context-joins
  :unchanged-identity-and-provenance-fields
  :late-var-diagnostic-interposition
  :existing-native-c14-source-and-file-artifact-assembly
  :existing-native-overrides-catalog-projection-and-ordered-validation
  :trusted-zero-argument-operation-suppliers],
 :dependency-direction
 {:requires [],
  :consumed-by [gravity.bootstrap gravity.c14-lowering.artifact
                gravity.c14-lowering.projection gravity.c14-lowering.validation],
  :forbids
  [gravity.bootstrap
   gravity.cli
   gravity.release
   gravity.c14-lowering]},
 :legacy-status-labels-authoritative? false,
 :preserves [:caller-supplied-identities :source-proof-fact-associations :upstream-context :diagnostic-call-order],
 :invalidates [],
 :regenerates [:native-reconstruction-records :native-c14-artifact-and-projection],
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
  SAFE15
  PERF10
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
  :explicit-strict-comparison-and-diagnostic-operations
  :trusted-native-configuration-source-module-and-c13-parent-data
  :trusted-late-operation-suppliers],
 :namespace gravity.lowering,
 :diagnostic-ownership
 {:stage
  ["C14-MANIFEST" :exact-reconstruction-or-context-join-mismatch
   :existing-native-c14-ordered-structural-refusals
   "C14-INPUT" "C14-PROFILE" "C14-TARGET" "C14-ABI" "C14-RUNTIME"
   "C14-CAPABILITY" "C14-PROVIDER" "C14-PROOF-METADATA" "C14-UNSUPPORTED"],
  :propagated ["C11-VERIFY" :strict-structure-mismatch],
  :existing-owner
  [:canonical-c11-c13-admission
   :target-legality
   :diagnostic-publication
   :source-verification
   :sealing-and-authority]},
 :artifact-outputs
 [:raw-llvm-c-wasm-c14-reconstruction-data
  :exact-reconstruction-or-refusal :native-c14-artifact-or-refusal
  :native-c14-diagnostics-and-capability-projection],
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

(defn native-artifact-base [configuration source-path source-text module source-overrides optimization-artifact diagnostics operations]
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

(doseq [[_ operation] (ns-publics *ns*)]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
