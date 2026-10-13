
(def ^:private row-plan-prefix
  [{:ordinal 0 :id :agents :document-ordinals [0]
    :sections [(contract-section
                0 ["## Source Language Boundary" "## Start Here"
                   "## Source Of Truth" "## Implementation Principles"
                   "## Agent Guardrails: Preventing Over-Engineering"
                   "## Documentation Work" "## Validation"
                   "## Working Discipline"
                   "## Workstream Lifecycle And Integration"] false)]
    :disposition :applies
    :obligations [:source-language-boundary :normative-priority :ownership
                  :validation :no-overclaim :governed-lifecycle]
    :binding-specs
    [(contract-binding :agents-contract-bytes :pre-evidence
                       :document-read-receipt [:documents 0]
                       [:normative-priority])
     (contract-binding :agents-launcher-boundary :runtime :launcher []
                       [:source-language-boundary :validation])
     (contract-binding :agents-coordinator-boundary :runtime :coordinator []
                       [:validation])
     (contract-binding :agents-ownership-plan :pre-evidence :ownership-plan []
                       [:ownership])
     (contract-binding :agents-progress :pre-evidence :progress []
                       [:no-overclaim])
     (contract-binding :agents-final-lifecycle :post-p9 :phase-evidence
                       [:carriers :final-lifecycle] [:governed-lifecycle])]
    :test-ids [:sh07-b51-v9-contract-agents]
    :diagnostic :SH07-B51-CONTRACT-DISCHARGE}
   {:ordinal 1 :id :foundation :document-ordinals [1 2 3 4 5 6 7]
    :sections
    (mapv #(contract-section
            % ["## Requirements" "## Outputs and Artifacts"
               "## Conformance Criteria"] false)
          (range 1 8))
    :disposition :applies
    :obligations [:lineage :pipeline-records :fact-provenance :safe-semantics
                  :evidence-kind :replay :invalidation]
    :binding-specs
    [(contract-binding :foundation-static :pre-evidence :foundation-static []
                       [:safe-semantics :evidence-kind :replay :invalidation])
     (contract-binding :foundation-runtime :runtime :foundation-runtime []
                       [:pipeline-records :fact-provenance :replay
                        :invalidation])
     (contract-binding :foundation-lineage :post-i9 :phase-evidence
                       [:carriers :foundation-lineage] [:lineage])]
    :test-ids [:sh07-b51-v9-contract-foundation]
    :diagnostic :SH07-B51-CONTRACT-DISCHARGE}
   {:ordinal 2 :id :l1 :document-ordinals [8]
    :sections [(contract-section
                8 ["## Source Units" "## Requirements"
                   "## Outputs and Artifacts" "## Diagnostics"
                   "## Conformance Criteria"] false)]
    :disposition :preserved
    :obligations [:source-bytes :source-order :source-id
                  :syntax-origin-metadata :no-grammar-change]
    :binding-specs
    [(contract-binding :l1-contract :pre-evidence :document-read-receipt
                       [:documents 8] [:no-grammar-change])
     (contract-binding :l1-source-descriptor :runtime :source-session-m
                       [:source-descriptors 0]
                       [:source-bytes :source-order :source-id])
     (contract-binding :l1-semantic-witness :runtime
                       :semantic-witness-runtime [:witness :l1]
                       [:syntax-origin-metadata :no-grammar-change])]
    :test-ids [:sh07-b51-v9-contract-l1]
    :diagnostic :SH07-B51-GIT-INPUT-BLOB}
   {:ordinal 3 :id :l2 :document-ordinals [9]
    :sections [(contract-section
                9 ["## Core Forms" "## Evaluation Model" "## Requirements"
                   "## Outputs and Artifacts" "## Diagnostics"
                   "## Conformance Criteria"] false)]
    :disposition :applies
    :obligations [:core-semantics :evaluation-order
                  :effects-capabilities-profile :diagnostics
                  :negative-fixtures :authentic-owning-root
                  :authenticated-external-bindings
                  :unauthorized-cross-fragment-rejection]
    :binding-specs
    [(contract-binding
      :l2-semantic-witness :runtime :semantic-witness-runtime [:witness :l2]
      [:core-semantics :evaluation-order :effects-capabilities-profile
       :diagnostics :negative-fixtures :authentic-owning-root
       :authenticated-external-bindings
       :unauthorized-cross-fragment-rejection])]
    :test-ids [:sh07-b51-v9-contract-l2]
    :diagnostic :SH07-B51-EDGE-AUTHORITY}
   {:ordinal 4 :id :c2 :document-ordinals [10]
    :sections [(contract-section
                10 ["## Requirements" "## Outputs and Artifacts"
                    "## Source Unit Record" "## Incremental Hashing"
                    "## Diagnostics" "## Conformance Criteria"] false)]
    :disposition :applies
    :obligations [:immutable-source-snapshot :encoding
                  :source-unit-identities :source-map :diagnostics]
    :binding-specs
    [(contract-binding :c2-semantic-witness :runtime
                       :semantic-witness-runtime [:witness :c2]
                       [:immutable-source-snapshot :encoding
                        :source-unit-identities :source-map :diagnostics])]
    :test-ids [:sh07-b51-v9-contract-c2]
    :diagnostic :SH07-B51-GIT-SNAPSHOT}
   {:ordinal 5 :id :c6 :document-ordinals [11]
    :sections [(contract-section
                11 ["## Requirements" "## Outputs and Artifacts"
                    "## Core Node Shape" "## Desugaring Trace"
                    "## Evaluation Order" "## Core Verification"
                    "## Diagnostics" "## Conformance Criteria"] false)]
    :disposition :applies
    :obligations [:core-ast :syntax-core-map :trace :origins-facts-order
                  :verification :mutations]
    :binding-specs
    [(contract-binding :c6-semantic-witness :runtime
                       :semantic-witness-runtime [:witness :c6]
                       [:core-ast :syntax-core-map :trace
                        :origins-facts-order :verification :mutations])]
    :test-ids [:sh07-b51-v9-contract-c6]
    :diagnostic :SH07-B51-EDGE-AUTHORITY}
   {:ordinal 6 :id :c11 :document-ordinals [12]
    :sections [(contract-section
                12 ["## Requirements" "## Outputs and Artifacts"
                    "## Module Shape" "## Operation Schema"
                    "## Metadata Tables" "## MIR Verification"
                    "## Diagnostics" "## Conformance Criteria"] false)]
    :disposition :not-applicable
    :obligations [:verified-core-boundary :no-mir-artifacts
                  :no-backend-artifacts :no-stage-support-claims]
    :binding-specs
    [(contract-binding :c11-publication-boundary :pre-evidence
                       :publication-boundary []
                       [:verified-core-boundary :no-mir-artifacts
                        :no-backend-artifacts :no-stage-support-claims])
     (contract-binding :c11-proof-scope :pre-evidence :proof-scope []
                       [:verified-core-boundary])]
    :test-ids [:sh07-b51-v9-contract-c11-nonclaim]
    :diagnostic :SH07-B51-CONTRACT-DISCHARGE}
   {:ordinal 7 :id :boot1 :document-ordinals [13]
    :sections [(contract-section
                13 ["## Stage Outline" "## Requirements"
                    "## Outputs and Artifacts" "## Conformance Criteria"]
                false)]
    :disposition :applies
    :obligations [:stage :trusted-inputs :compiler-lineage :environment
                  :conformance]
    :binding-specs
    [(contract-binding :boot1-compatibility :pre-evidence
                       :boot5-compatibility [] [:stage])
     (contract-binding :boot1-launcher :runtime :launcher []
                       [:trusted-inputs :environment])
     (contract-binding :boot1-coordinator :runtime :coordinator []
                       [:trusted-inputs :environment :conformance])
     (contract-binding :boot1-lineage :post-i9 :phase-evidence
                       [:carriers :compiler-lineage] [:compiler-lineage])]
    :test-ids [:sh07-b51-v9-contract-boot1]
    :diagnostic :SH07-B51-TRUSTED-TCB}
   {:ordinal 8 :id :boot2 :document-ordinals [14]
    :sections [(contract-section
                14 ["## Implemented Subset" "## Requirements"
                    "## Outputs and Artifacts" "## Conformance Criteria"]
                false)]
    :disposition :applies
    :obligations [:implemented-excluded-documents :host-dependencies
                  :runtime-assumptions
                  :source-compiler-backend-dependency-hashes]
    :binding-specs
    [(contract-binding :boot2-compatibility :pre-evidence
                       :boot5-compatibility []
                       [:implemented-excluded-documents])
     (contract-binding :boot2-launcher-tcb :runtime :launcher
                       [:pre-manifest-tcb]
                       [:host-dependencies :runtime-assumptions])
     (contract-binding :boot2-coordinator-tcb :runtime :coordinator
                       [:pre-manifest-tcb]
                       [:host-dependencies :runtime-assumptions])
     (contract-binding :boot2-runtime :runtime :semantic-witness-runtime
                       [:witness :boot2]
                       [:source-compiler-backend-dependency-hashes])]
    :test-ids [:sh07-b51-v9-contract-boot2]
    :diagnostic :SH07-B51-TRUSTED-TCB}
   {:ordinal 9 :id :boot3 :document-ordinals [15]
    :sections [(contract-section
                15 ["## Migration Order" "## Requirements"
                    "## Outputs and Artifacts" "## Conformance Criteria"]
                false)]
    :disposition :preserved
    :obligations [:module-io :effects-facts :tests :meta-profile
                  :diagnostic-compatibility :no-stage-advance]
    :binding-specs
    [(contract-binding :boot3-semantic-witness :runtime
                       :semantic-witness-runtime [:witness :boot3]
                       [:module-io :effects-facts :tests
                        :diagnostic-compatibility])
     (contract-binding :boot3-proof-scope :pre-evidence :proof-scope []
                       [:meta-profile])
     (contract-binding :boot3-compatibility :pre-evidence
                       :boot5-compatibility [] [:no-stage-advance])]
    :test-ids [:sh07-b51-v9-contract-boot3]
    :diagnostic :SH07-B51-CONTRACT-DISCHARGE}
])
