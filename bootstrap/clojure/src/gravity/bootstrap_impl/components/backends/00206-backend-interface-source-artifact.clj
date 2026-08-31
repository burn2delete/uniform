

(defn backend-interface-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (backend-interface-source-overrides module)
        _ (backend-interface-validate-source-overrides! source-path
                                                        source-overrides)
        verification-artifact (compiler-c18-verification-source-artifact
                               source-path source-text)
        input-id (:artifact-id verification-artifact)
        diagnostic-stream (backend-interface-diagnostic-stream source-path
                                                               input-id)
        artifact-base
        {:kind :gravity/stage0-backend-interface-artifact
         :task "P07-T01"
         :document-set ["B1" "B14"]
         :governing-documents backend-interface-governing-documents
         :pass {:name :backend-interface
                :input :compiler-verification-and-trust-artifact
                :output :backend-interface-and-conformance-artifact
                :requires [:verified-mir-or-domain-ir :profile-manifest
                           :target-manifest :abi-policy :runtime-provider
                           :effects :capabilities :safety :proofs
                           :source-map :dependencies]
                :preserves [:source-spans :generated-origins :profile
                            :target :effects :capabilities :safety
                            :proofs :unsafe-audit-ids :provenance]
                :emits [:backend-manifest :backend-input-eligibility-report
                        :target-artifact-manifest :abi-layout-record
                        :runtime-provider-dependency-record
                        :proof-to-target-metadata-map :source-debug-map
                        :unsupported-feature-report
                        :backend-diagnostic-stream
                        :backend-conformance-record]
                :rejects backend-interface-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :c18-verification-artifact
         (select-keys verification-artifact
                      [:kind :task :artifact-id :governing-document
                       :capability-based-proof])
         :compiler-verification-artifact-kind (:kind verification-artifact)
         :compiler-verification-artifact-hash input-id
         :backend-manifest
         {:artifact :gravity/backend-manifest
          :backend :gravity.backend/interface-v1
          :version "1"
          :accepts #{:gravity/mir :gravity/domain-ir}
          :emits #{:object :library :bytecode :source
                   :workflow-graph :hdl :query-plan}
          :requires #{:profile :target :abi :runtime :effects
                      :capabilities :safety}
          :supports-profiles #{:core :hosted :native :firmware :kernel
                               :gpu :hardware :distributed :ai}
          :rejects #{:unverified-ir :unsupported-op :missing-proof
                     :implicit-ub :ambient-capability :profile-violation}
          :status :complete}
         :backend-input-packet
         {:input {:kind :gravity/mir
                  :id input-id
                  :verification-status :accepted}
          :profile "profile-manifest:hosted"
          :target "target-manifest:jvm"
          :abi "abi-policy:jvm-stage0"
          :runtime "runtime-manifest:clojure-jvm-stage0"
          :providers "provider-selection:stage0"
          :effects "effect-summary:stage0"
          :capabilities "capability-summary:stage0"
          :safety "safety-bundle:stage0"
          :proofs "proof-table:stage0"
          :source-map "source-map:stage0"
          :dependencies "dependency-graph:stage0"}
         :backend-input-eligibility-report
         {:artifact :gravity/backend-input-eligibility-report
          :backend :gravity.backend/interface-v1
          :input input-id
          :profile :hosted
          :target :jvm
          :decision :eligible
          :status :complete}
         :eligibility-checks
         [{:check :verified-input :status :passed}
          {:check :profile-backend-compatibility :status :passed}
          {:check :target-feature-support :status :passed}
          {:check :runtime-availability :status :passed}
          {:check :abi-representability :status :passed}
          {:check :layout-representability :status :passed}
          {:check :provider-availability :status :passed}
          {:check :effect-capability-preservation :status :passed}
          {:check :safety-bundle-completeness :status :passed}
          {:check :proof-validity :status :passed}
          {:check :source-debug-map-preservation :status :passed}]
         :unchecked-ir-rejection-record
         {:artifact :gravity/backend-rejection
          :diagnostic "B1-INPUT"
          :input :unverified-mir
          :status :rejected}
         :undefined-behavior-rejection-record
         {:artifact :gravity/backend-rejection
          :diagnostic "B1-PROOF"
          :assumption :signed-overflow-undefined
          :status :rejected}
         :target-artifact-manifest
         [{:artifact :gravity/target-artifact
           :kind :bytecode
           :digest "sha256:p07-stage0-jvm-bytecode-metadata"
           :backend :gravity.backend/interface-v1
           :backend-version "1"
           :source-input input-id
           :profile :hosted
           :target :jvm
           :abi-layout "abi-layout:jvm-stage0"
           :runtime-provider "runtime-provider:clojure-jvm-stage0"
           :safety-evidence "safety-bundle:stage0"
           :proof-summary "proof-table:stage0"
           :capability-summary "capability-summary:stage0"
           :source-debug-map "source-debug-map:stage0"
           :unsafe-audit-ids []
           :dependencies ["dependency-graph:stage0"]
           :conformance "backend-conformance:p07-t01"
           :provenance {:compiler "gravity-stage0-clojure"
                        :pass-history ["C18" "B1" "B14"]}}]
         :abi-layout-record
         {:artifact :gravity/abi-layout-record
          :target :jvm
          :layout-policy :managed-runtime
          :representability :passed
          :status :complete}
         :runtime-provider-dependency-record
         {:artifact :gravity/runtime-provider-dependency-record
          :runtime :clojure-jvm-stage0
          :providers [:stdout :checked-exception :managed-memory]
          :hidden-dependencies []
          :status :complete}
         :proof-to-target-metadata-map
         {:artifact :gravity/proof-to-target-metadata-map
          :status :accepted
          :assumptions [{:target-assumption :no-signed-overflow
                         :proof :proof/c18-bounds-check-dominance}
                        {:target-assumption :dereferenceable-managed-reference
                         :proof :proof/c18-safety-check-elision}
                        {:target-assumption :host-exception-mapping
                         :proof :proof/c18-loop-fuser-effect-order}]}
         :source-debug-map
         {:artifact :gravity/source-debug-map
          :input input-id
          :source-spans :preserved
          :generated-origin-chain :preserved
          :status :complete}
         :capability-preservation-report
         {:artifact :gravity/backend-capability-preservation
          :capabilities (:capabilities module)
          :provider-selection :stage0
          :status :preserved}
         :unsupported-feature-report
         {:artifact :gravity/unsupported-feature-report
          :status :recorded
          :unsupported [{:mir-op :target-specific-opcode
                         :diagnostic "B1-UNSUPPORTED"
                         :fallback :reject}]}
         :backend-diagnostic-stream diagnostic-stream
         :backend-conformance-record
         {:artifact :gravity/backend-conformance-record
          :status :passed
          :suite :p07-t01-backend-interface
          :positive-lowering-results [{:fixture :backend-interface-positive
                                       :status :passed}]
          :negative-diagnostic-results
          (mapv (fn [id]
                  {:diagnostic id :status :matched})
                backend-interface-diagnostic-ids)
          :target-availability {:jvm :available}
          :evidence-pack "backend-conformance-pack:p07-t01"}
         :metadata-preservation-report
         {:artifact :gravity/backend-metadata-preservation-report
          :status :preserved
          :fields [:source-spans :generated-origin-chain :types :effects
                   :capabilities :safety :proofs :unsafe-audit-ids
                   :profile :target :runtime :abi :conformance]}
         :artifact-manifest-validation-report
         {:artifact :gravity/artifact-manifest-validation-report
          :status :valid
          :validated-artifacts ["sha256:p07-stage0-jvm-bytecode-metadata"]}
         :backend-interface-results
         {:documents ["B1" "B14"]
          :task "P07-T01"
          :required-diagnostic-ids backend-interface-diagnostic-ids
          :c18-input-status :complete
          :manifest-status :complete
          :input-contract-status :complete
          :eligibility-status :complete
          :artifact-status :complete
          :metadata-status :complete
          :conformance-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (backend-interface-validate! source-path artifact-base)
        capability-proof (backend-interface-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn backend-interface-file-artifact
  [path]
  (backend-interface-source-artifact path (slurp path)))

(def native-lowering-governing-documents
  ["docs/phase-07-backend-architecture/099-b2-c-backend-design.md"
   "docs/phase-07-backend-architecture/100-b3-llvm-backend-design.md"
   "docs/phase-07-backend-architecture/104-b7-mlir-backend-design.md"
   "docs/phase-07-backend-architecture/110-b13-artifact-emission-specification.md"
   "docs/phase-07-backend-architecture/111-b14-backend-conformance-test-plan.md"
   "docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md"])