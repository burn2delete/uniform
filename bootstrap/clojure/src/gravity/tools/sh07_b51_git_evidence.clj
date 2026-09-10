(ns gravity.tools.sh07-b51-git-evidence
  "Bounded Git-object and filesystem publication seam for SH-07 B5.1 V9.

  This launcher authenticates storage, transport, and publication mechanics.
  It deliberately returns `:authority :none`: the full V9 runtime contract
  discharge and receipt-normalization implementation must join this seam before
  an E9 candidate can claim evidence authority."
  (:require [clojure.string :as str]
            [gravity.tools.sh07-b51-v9-codec :as codec])
  (:import [java.io Closeable]
           [java.nio ByteBuffer]
           [java.nio.charset CodingErrorAction StandardCharsets]
           [java.nio.file DirectoryStream Files LinkOption OpenOption Path Paths
            StandardOpenOption]
           [java.nio.file.attribute BasicFileAttributes]
           [java.time Instant]
           [java.util Arrays Optional]
           [java.util.concurrent Callable ExecutionException
            ExecutorCompletionService Executors Future TimeUnit
            TimeoutException]))

(def git-executable "/usr/bin/git")

(def evidence-relative-root
  "docs/artifacts/phase-15/evidence/sh07-b51-git-object-v9")

(def eligibility-receipt-path
  "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/evidence-eligibility-receipt.edn")

(def payload-specs
  [[0 "checker-a-identity.edn" :checker-identity]
   [1 "checker-b-identity.edn" :checker-identity]
   [2 "checker-a-output.edn" :checker-output]
   [3 "checker-b-output.edn" :checker-output]
   [4 "certificate-pair.edn" :certificate-pair]
   [5 "linked-carrier-v4-candidate.edn" :linked-carrier]])

(def payload-names (mapv second payload-specs))
(def identity-receipt-name "identity-receipt.edn")
(def evidence-names (conj payload-names identity-receipt-name))
(def evidence-paths
  (mapv #(str evidence-relative-root "/" %) evidence-names))

(def closed-environment
  {"LC_ALL" "C"
   "LANG" "C"
   "TZ" "UTC"
   "GIT_CONFIG_NOSYSTEM" "1"
   "GIT_CONFIG_GLOBAL" "/dev/null"
   "GIT_OPTIONAL_LOCKS" "0"
   "GIT_NO_REPLACE_OBJECTS" "1"})

(def transport-limits
  (assoc codec/transport-limits :deadline-ms 3600000))

(def ^:private transport-api codec/transport-api)

(def nonclaims
  [:formal-proof :sh07-complete :self-hosting :release :seed-retirement
   :hostile-host])

(def pre-baseline-commit
  "09c795f0c0854d2d8ab542c92b2116515e096406")

(def boot5-path
  "docs/phase-15-bootstrap-and-self-hosting/207-boot5-stage-compatibility-matrix.md")

(def document-paths
  [[0 "AGENTS.md"]
   [1 "docs/phase-00-foundation-and-thesis/001-d0-gravity-vision-and-design-thesis.md"]
   [2 "docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md"]
   [3 "docs/phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md"]
   [4 "docs/phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md"]
   [5 "docs/phase-00-foundation-and-thesis/007-d6-performance-philosophy-and-charter.md"]
   [6 "docs/phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md"]
   [7 "docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md"]
   [8 "docs/phase-01-core-language/011-l1-surface-syntax-specification.md"]
   [9 "docs/phase-01-core-language/012-l2-core-language-semantics.md"]
   [10 "docs/phase-06-compiler-architecture/081-c2-reader-implementation-design.md"]
   [11 "docs/phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md"]
   [12 "docs/phase-06-compiler-architecture/090-c11-gravity-mir-specification.md"]
   [13 "docs/phase-15-bootstrap-and-self-hosting/203-boot1-bootstrap-strategy.md"]
   [14 "docs/phase-15-bootstrap-and-self-hosting/204-boot2-seed-compiler-design.md"]
   [15 "docs/phase-15-bootstrap-and-self-hosting/205-boot3-self-hosted-compiler-plan.md"]
   [16 "docs/phase-15-bootstrap-and-self-hosting/206-boot4-compiler-in-gravity-coding-standard.md"]
   [17 "docs/phase-15-bootstrap-and-self-hosting/207-boot5-stage-compatibility-matrix.md"]
   [18 "docs/phase-15-bootstrap-and-self-hosting/208-boot6-trusting-trust-and-reproducible-bootstrap-plan.md"]
   [19 "docs/phase-15-bootstrap-and-self-hosting/209-boot7-self-hosting-validation-and-equivalence-plan.md"]
   [20 "docs/phase-15-bootstrap-and-self-hosting/210-boot8-bootstrap-artifact-provenance-specification.md"]
   [21 "docs/phase-14-testing-verification-and-conformance/199-test10-differential-testing-strategy.md"]
   [22 "docs/phase-14-testing-verification-and-conformance/200-test11-formal-semantics-and-verification-plan.md"]
   [23 "docs/phase-14-testing-verification-and-conformance/202-test13-self-hosting-validation-plan.md"]
   [24 "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn"]
   [25 "docs/self-hosting-slice-backlog.md"]
   [26 "docs/self-hosting-slice-ownership.edn"]
   [27 "contracts/verification-catalog.edn"]
   [28 "bootstrap/gravity/src/gravity/checked_core.gravity"]
   [29 "docs/workstream-governance.md"]
   [30 "contracts/workstream-governance.json"]
   [31 "docs/workstream-ledger-v2.md"]
   [32 "contracts/workstream-ledger.json"]])

(def source-specs
  [[0 :l1-contract nil
    "docs/phase-01-core-language/011-l1-surface-syntax-specification.md"]
   [1 :l2-contract nil
    "docs/phase-01-core-language/012-l2-core-language-semantics.md"]
   [2 :checked-core nil
    "bootstrap/gravity/src/gravity/checked_core.gravity"]
   [3 :module 0
    "bootstrap/gravity/src/gravity/sh07_b51/producer_a.gravity"]
   [4 :module 1
    "bootstrap/gravity/src/gravity/sh07_b51/producer_b.gravity"]
   [5 :module 2
    "bootstrap/gravity/src/gravity/sh07_b51/producer_c.gravity"]
   [6 :module 3
    "bootstrap/gravity/src/gravity/sh07_b51/producer_d.gravity"]
   [7 :module 4
    "bootstrap/gravity/src/gravity/sh07_b51/root6_a.gravity"]
   [8 :module 5
    "bootstrap/gravity/src/gravity/sh07_b51/root6_b.gravity"]
   [9 :module 6
    "bootstrap/gravity/src/gravity/sh07_b51/root8.gravity"]
   [10 :compiler nil "bootstrap/gravity/p15_s23/compiler.gravity"]
   [11 :emitter nil "bootstrap/gravity/p15_s23/emitter.gravity"]])

(def source-only-paths
  ["bootstrap/gravity/p15_s23/compiler.gravity"
   "bootstrap/gravity/p15_s23/emitter.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/producer_a.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/producer_b.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/producer_c.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/producer_d.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/root6_a.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/root6_b.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/root8.gravity"])

(def owned-c-paths
  ["bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00580-p15-s23-stage2-compiler-artifact-expected-artifact-hash.clj"
   "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00581-p15-s23-stage2-compiler-artifact-binding.clj"
   "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00582-p15-s23-stage2-construct-emitted-core-plan.clj"
   "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00943-p15-s23-sh07-host-builtins-and-oracle.clj"
   "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker.clj"
   "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00947-p15-s23-sh07-b51-linked-carrier-v4.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_child.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_identity.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_coordinator_child.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_materialize.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_tier0_materializer.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_git_evidence.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_v9_codec.clj"
   "bootstrap/clojure/test/gravity/self_hosting/fixtures/sh07-b51-git-object-v9/accepted.edn"
   "bootstrap/clojure/test/gravity/self_hosting/fixtures/sh07-b51-git-object-v9/rejected.edn"
   "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt32_checker_child_test.clj"
   "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt32_runtime_admission_test.clj"
   "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt32_tier0_materializer_test.clj"
   "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_git_evidence_test.clj"
   "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_plan_linker_governed_read_test.clj"
   "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_codec_tcb_test.clj"
   "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_codec_test.clj"
   "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_contract_discharge_test.clj"
   "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_eligibility_object_test.clj"
   "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_existing_object_replay_test.clj"
   "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_process_isolation_test.clj"
   "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_source_closure_test.clj"])

(def a9-pre-paths
  ["docs/artifacts/phase-15/reports/sh07-b51-git-object-evidence-publication-architecture-v9.md"
   "docs/artifacts/phase-15/reports/sh07-b51-git-object-evidence-publication-provisional-evidence-v9.md"
   "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/a9-lifecycle-receipt.edn"])

(defn- unsigned-utf8-compare
  [left right]
  (Arrays/compareUnsigned (.getBytes ^String left StandardCharsets/UTF_8)
                          (.getBytes ^String right StandardCharsets/UTF_8)))

(def pre-c-paths
  (let [paths (vec (sort unsigned-utf8-compare
                         (concat (map second document-paths)
                                 source-only-paths
                                 owned-c-paths
                                 a9-pre-paths)))]
    (when-not (and (= 72 (count paths)) (= 72 (count (distinct paths))))
      (throw (ex-info "invalid literal SH-07 PRE path census"
                      {:count (count paths)
                       :distinct-count (count (distinct paths))})))
    paths))

(defn- contract-section [document-ordinal headings whole-document]
  {:document-ordinal document-ordinal
   :headings headings
   :whole-document whole-document})

(defn- contract-binding [id phase carrier path obligations]
  {:id id :phase phase :carrier carrier :path path
   :obligations obligations})

(def row-plan
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
   {:ordinal 10 :id :boot4 :document-ordinals [16]
    :sections [(contract-section
                16 ["## Module Rules" "## Requirements"
                    "## Outputs and Artifacts" "## Conformance Criteria"]
                false)]
    :disposition :applies
    :obligations [:closed-module-manifest :effects-capabilities
                  :deterministic-output :fact-provenance
                  :positive-negative-preservation-tests]
    :binding-specs
    [(contract-binding :boot4-source-closure :runtime :coordinator
                       [:source-closure] [:closed-module-manifest])
     (contract-binding :boot4-host-surface :runtime :coordinator
                       [:host-surface] [:effects-capabilities])
     (contract-binding :boot4-semantic-witness :runtime
                       :semantic-witness-runtime [:witness :boot4]
                       [:effects-capabilities :deterministic-output
                        :fact-provenance
                        :positive-negative-preservation-tests])]
    :test-ids [:sh07-b51-v9-contract-boot4]
    :diagnostic :SH07-B51-FORBIDDEN-VAR}
   {:ordinal 11 :id :boot5 :document-ordinals [17]
    :sections [(contract-section
                17 ["## Matrix Columns" "## Requirements"
                    "## Outputs and Artifacts" "## Conformance Criteria"]
                false)]
    :disposition :preserved
    :obligations [:compatibility-row :compatibility-digest :explicit-gaps
                  :no-stage-advance]
    :binding-specs
    [(contract-binding :boot5-compatibility :pre-evidence
                       :boot5-compatibility []
                       [:compatibility-row :compatibility-digest
                        :explicit-gaps :no-stage-advance])
     (contract-binding :boot5-runtime :runtime :self-hosting-runtime []
                       [:no-stage-advance])]
    :test-ids [:sh07-b51-v9-contract-boot5]
    :diagnostic :SH07-B51-CONTRACT-DISCHARGE}
   {:ordinal 12 :id :boot6-8 :document-ordinals [18 19 20]
    :sections
    (mapv #(contract-section
            % ["## Requirements" "## Outputs and Artifacts"
               "## Rejection Rules" "## Conformance Criteria"] false)
          (range 18 21))
    :disposition :applies
    :obligations [:fresh-processes :stage-comparison :tcb-delta :provenance
                  :cleanup :replay]
    :binding-specs
    [(contract-binding :boot6-8-launcher :runtime :launcher []
                       [:fresh-processes :provenance :cleanup])
     (contract-binding :boot6-8-coordinator :runtime :coordinator []
                       [:fresh-processes :provenance])
     (contract-binding :boot6-8-runtime :runtime :bootstrap-runtime []
                       [:stage-comparison :tcb-delta :provenance :cleanup
                        :replay])
     (contract-binding :boot6-8-i9 :post-i9 :phase-evidence
                       [:carriers :provenance] [:provenance])
     (contract-binding :boot6-8-g9 :post-g9 :phase-evidence
                       [:carriers :provenance] [:provenance])
     (contract-binding :boot6-8-p9 :post-p9 :phase-evidence
                       [:carriers :provenance] [:provenance])]
    :test-ids [:sh07-b51-v9-contract-boot6-8]
    :diagnostic :SH07-B51-CHECKER-PROCESS}
   {:ordinal 13 :id :test10 :document-ordinals [21]
    :sections [(contract-section
                21 ["## Oracles" "## Requirements"
                    "## Outputs and Artifacts" "## Rejection Rules"
                    "## Conformance Criteria"] false)]
    :disposition :applies
    :obligations [:oracle-identities :applicability :execution-context
                  :observables :diagnostics :permitted-divergence
                  :reproducer]
    :binding-specs
    [(contract-binding :test10-runtime :runtime :differential-runtime []
                       [:oracle-identities :applicability :execution-context
                        :observables :diagnostics :permitted-divergence
                        :reproducer])
     (contract-binding :test10-main-i9 :post-i9 :phase-evidence
                       [:carriers :test10-result]
                       [:oracle-identities :applicability])]
    :test-ids [:sh07-b51-v9-contract-test10]
    :diagnostic :SH07-B51-NORMALIZE-DIVERGENCE}
   {:ordinal 14 :id :test11 :document-ordinals [22]
    :sections [(contract-section
                22 ["## Requirements" "## Outputs and Artifacts"
                    "## Rejection Rules" "## Conformance Criteria"] false)]
    :disposition :applies
    :obligations [:assumptions :trusted-basis :checker-input-claim-ids
                  :invalidation :counterexamples :validation-not-proof]
    :binding-specs
    [(contract-binding :test11-proof-scope :pre-evidence :proof-scope []
                       [:validation-not-proof])
     (contract-binding :test11-runtime :runtime :verification-runtime []
                       [:assumptions :trusted-basis
                        :checker-input-claim-ids :invalidation
                        :counterexamples])]
    :test-ids [:sh07-b51-v9-contract-test11]
    :diagnostic :SH07-B51-PROOF-SCOPE}
   {:ordinal 15 :id :test13 :document-ordinals [23]
    :sections [(contract-section
                23 ["## Stages" "## Requirements"
                    "## Outputs and Artifacts" "## Rejection Rules"
                    "## Conformance Criteria"] false)]
    :disposition :preserved
    :obligations [:stage-trusted-inputs :generated-artifacts
                  :source-compiler-lock-target-output-ids :expected-deltas
                  :tcb-delta :no-stage-advance]
    :binding-specs
    [(contract-binding :test13-runtime :runtime :self-hosting-runtime []
                       [:stage-trusted-inputs :generated-artifacts
                        :source-compiler-lock-target-output-ids
                        :expected-deltas :tcb-delta :no-stage-advance])
     (contract-binding :test13-evidence :post-i9 :phase-evidence
                       [:carriers :test13-result] [:generated-artifacts])]
    :test-ids [:sh07-b51-v9-contract-test13]
    :diagnostic :SH07-B51-PROOF-SCOPE}
   {:ordinal 16 :id :sh07-proof-contract :document-ordinals [24]
    :sections [(contract-section 24 [] true)]
    :disposition :applies
    :obligations [:active-scope :fresh-process :nonauthoritative-cache
                  :source-rechecks :catalog-ids :bounds :cleanup]
    :binding-specs
    [(contract-binding :proof-contract-document :pre-evidence
                       :document-read-receipt [:documents 24]
                       [:active-scope :nonauthoritative-cache :bounds])
     (contract-binding :proof-contract-projection :pre-evidence :proof-scope
                       [:proof-contract] [:active-scope])
     (contract-binding :proof-contract-runtime :runtime
                       :verification-runtime []
                       [:fresh-process :source-rechecks :catalog-ids
                        :cleanup])]
    :test-ids [:sh07-b51-v9-proof-contract-scope]
    :diagnostic :SH07-B51-PROOF-SCOPE}
   {:ordinal 17 :id :backlog-ownership :document-ordinals [25 26]
    :sections
    [(contract-section
      25 ["## Countable Backlog" "## Dependency Waves and Parallel Ownership"
          "## Progress Accounting" "## Outputs and Artifacts"
          "## Conformance Criteria"] false)
     (contract-section 26 [] true)]
    :disposition :applies
    :obligations [:partial-progress :allocations :reservations :handoffs
                  :open-exit-gates]
    :binding-specs
    [(contract-binding :backlog-document :pre-evidence
                       :document-read-receipt [:documents 25]
                       [:partial-progress])
     (contract-binding :ownership-plan :pre-evidence :ownership-plan []
                       [:allocations :open-exit-gates])
     (contract-binding :backlog-progress :pre-evidence :progress []
                       [:partial-progress])
     (contract-binding :ownership-i9 :post-i9 :phase-evidence
                       [:carriers :ownership-result]
                       [:reservations :handoffs])
     (contract-binding :ownership-g9 :post-g9 :phase-evidence
                       [:carriers :ownership-result]
                       [:reservations :handoffs])
     (contract-binding :ownership-p9 :post-p9 :phase-evidence
                       [:carriers :ownership-result]
                       [:reservations :handoffs])]
    :test-ids [:sh07-b51-v9-backlog-ownership]
    :diagnostic :SH07-B51-OWNER}
   {:ordinal 18 :id :catalog-checked-core-governance
    :document-ordinals [27 28 29 30 31 32]
    :sections
    [(contract-section 27 [] true)
     (contract-section 28 [] true)
     (contract-section 29 ["## Lifecycle" "## Admission"] false)
     (contract-section 30 [] true)
     (contract-section 31 [] true)
     (contract-section 32 [] true)]
    :disposition :applies
    :obligations [:catalog-schema-authority :checked-core-revision
                  :lifecycle-transitions :review-admission-receipts
                  :final-cleanup]
    :binding-specs
    [(contract-binding :catalog-document :pre-evidence
                       :document-read-receipt [:documents 27]
                       [:catalog-schema-authority])
     (contract-binding :catalog-projection :pre-evidence
                       :catalog-projection [] [:catalog-schema-authority])
     (contract-binding :governance-rules :pre-evidence :governance-rules []
                       [:lifecycle-transitions])
     (contract-binding :checked-core-source :runtime :source-session-m
                       [:source-descriptors 2] [:checked-core-revision])
     (contract-binding :catalog-launcher-cleanup :runtime :launcher
                       [:cleanup] [:final-cleanup])
     (contract-binding :governance-i9 :post-i9 :phase-evidence
                       [:carriers :governance-result]
                       [:review-admission-receipts])
     (contract-binding :governance-g9 :post-g9 :phase-evidence
                       [:carriers :governance-result]
                       [:review-admission-receipts])
     (contract-binding :governance-p9 :post-p9 :phase-evidence
                       [:carriers :governance-results]
                       [:review-admission-receipts :final-cleanup])]
    :test-ids [:sh07-b51-v9-catalog-governance]
    :diagnostic :SH07-B51-VERIFY-CATALOG}])

(def row-plan-id
  (codec/H [:gravity/sh07-b51-contract-row-plan-id-v1 row-plan]))

(def phases
  [:pre-evidence :runtime :post-i9 :post-g9 :post-p9])

(def planned-binding-count
  (reduce + (map #(count (:binding-specs %)) row-plan)))

(defn- context-program
  [row phase obligation]
  [:and
   [:equal [:lookup :context [:row-ordinal]] [:literal (:ordinal row)]]
   [:equal [:lookup :context [:row-id]] [:literal (:id row)]]
   [:equal [:lookup :context [:phase]] [:literal phase]]
   [:equal [:lookup :context [:obligation]] [:literal obligation]]])

(defn- subject-program
  [row phase binding subject-index]
  [:and
   [:exact-keys [:lookup :subjects [subject-index]]
    [:literal [:row-ordinal :id :phase :carrier :path :obligations
               :value :value-id]]]
   [:equal [:lookup :subjects [subject-index :row-ordinal]]
    [:literal (:ordinal row)]]
   [:equal [:lookup :subjects [subject-index :id]]
    [:literal (:id binding)]]
   [:equal [:lookup :subjects [subject-index :phase]] [:literal phase]]
   [:equal [:lookup :subjects [subject-index :carrier]]
    [:literal (:carrier binding)]]
   [:equal [:lookup :subjects [subject-index :path]]
    [:literal (:path binding)]]
   [:equal [:lookup :subjects [subject-index :obligations]]
    [:literal (:obligations binding)]]
   [:equal [:lookup :subjects [subject-index :value]]
    [:lookup :carriers
     (into [(:carrier binding)] (:path binding))]]
   [:h-equal [:literal :gravity/sh07-b51-contract-binding-v2]
    [:lookup :subjects [subject-index]]
    [:lookup :subjects [subject-index :value-id]]
    [:literal [[:value-id]]]]])

(defn- predicate-spec-vector
  []
  (loop [rows row-plan
         ordinal 0
         result []]
    (if-let [row (first rows)]
      (let [[next-ordinal next-result]
            (reduce
             (fn [[position specs] phase]
               (reduce
                (fn [[inner-position inner-specs] obligation]
                  (let [bindings
                        (->> (:binding-specs row)
                             (filter #(and (= phase (:phase %))
                                           (some #{obligation}
                                                 (:obligations %))))
                             vec)]
                    (if (empty? bindings)
                      [inner-position inner-specs]
                      (let [program
                            (into [:and (context-program row phase obligation)]
                                  (map-indexed
                                   #(subject-program row phase %2 %1)
                                   bindings))
                            without-id
                            {:ordinal inner-position
                             :row-ordinal (:ordinal row)
                             :row-id (:id row)
                             :phase phase
                             :obligation obligation
                             :program program
                             :required-subject-paths (mapv :path bindings)}
                            predicate-id
                            (codec/H
                             [:gravity/sh07-b51-contract-predicate-id-v1
                              {:row-plan-id row-plan-id
                               :row-ordinal (:ordinal row)
                               :phase phase
                               :obligation obligation
                               :program program}])]
                        [(inc inner-position)
                         (conj inner-specs
                               (assoc without-id
                                      :predicate-id predicate-id))]))) )
                [position specs] (:obligations row)))
             [ordinal result] phases)]
        (recur (next rows) next-ordinal next-result))
      result)))

(def predicate-specs
  (predicate-spec-vector))

(def predicate-spec-id
  (codec/H [:gravity/sh07-b51-contract-predicate-spec-id-v1
            predicate-specs]))

(when-not (= 62 planned-binding-count)
  (throw (ex-info "invalid literal SH-07 binding census"
                  {:expected 62 :actual planned-binding-count})))

(declare exact-keys! fail! sha256-id?)

(defn- strict-path-value!
  [root path id]
  (when-not (and (vector? path)
                 (every? #(or (keyword? %)
                              (and (integer? %) (not (neg? %))))
                         path))
    (fail! id :predicate-path {:path path}))
  (reduce
   (fn [value segment]
     (let [next-value
           (cond
             (keyword? segment)
             (do
               (when-not (and (map? value) (contains? value segment))
                 (fail! id :predicate-lookup
                        {:path path :segment segment}))
               (get value segment))

             (integer? segment)
             (do
               (when-not (and (vector? value) (< segment (count value)))
                 (fail! id :predicate-lookup
                        {:path path :segment segment}))
               (nth value segment)))]
       (when (nil? next-value)
         (fail! id :predicate-nil {:path path :segment segment}))
       next-value))
   root path))

(defn- dissoc-required-path!
  [value path id]
  (when-not (seq path)
    (fail! id :predicate-omit-path {:path path}))
  (let [parent-path (pop path)
        field (peek path)
        parent (strict-path-value! value parent-path id)]
    (when-not (and (keyword? field) (map? parent)
                   (contains? parent field))
      (fail! id :predicate-omit-path {:path path}))
    (if (empty? parent-path)
      (dissoc value field)
      (letfn [(replace-parent [node remaining]
                (let [segment (first remaining)]
                  (if (= 1 (count remaining))
                    (cond
                      (keyword? segment)
                      (assoc node segment (dissoc (get node segment) field))

                      (integer? segment)
                      (assoc node segment (dissoc (nth node segment) field)))
                    (cond
                      (keyword? segment)
                      (assoc node segment
                             (replace-parent (get node segment)
                                             (next remaining)))

                      (integer? segment)
                      (assoc node segment
                             (replace-parent (nth node segment)
                                             (next remaining)))))))]
        (replace-parent value parent-path)))))

(defn- omit-required-paths!
  [value paths id]
  (when-not (and (map? value) (vector? paths)
                 (= (count paths) (count (distinct paths))))
    (fail! id :predicate-omit-paths {:paths paths}))
  ;; Paths are defined against the original map. Removing an ancestor of a
  ;; second path would otherwise make behavior order-dependent, so overlap is
  ;; forbidden by the same closed-path rule.
  (doseq [left paths right paths
          :when (and (not= left right)
                     (<= (count left) (count right))
                     (= left (subvec right 0 (count left))))]
    (fail! id :predicate-overlapping-omit-paths
           {:left left :right right}))
  (doseq [path paths]
    (strict-path-value! value path id))
  (reduce #(dissoc-required-path! %1 %2 id) value paths))

(defn- predicate-literal!
  [node id]
  (when-not (= 2 (count node))
    (fail! id :predicate-literal-node {:node node}))
  (let [value (second node)]
    ;; W is the common strict-plain-EDN admission surface and rejects host
    ;; objects, unknown tags, comparator collisions, or unbounded values.
    (codec/W value)
    value))

(declare evaluate-predicate-node!)

(defn- predicate-term!
  [node env item-bound? depth budget id]
  (when-not (and (vector? node) (seq node))
    (fail! id :predicate-term-node {:node node}))
  (swap! budget inc)
  (when (or (> @budget 100000) (> depth 256))
    (fail! id :predicate-fuel {:depth depth :nodes @budget}))
  (case (first node)
    :lookup
    (do
      (when-not (= 3 (count node))
        (fail! id :predicate-lookup-node {:node node}))
      (let [[_ source path] node
            root (case source
                   :context (:context env)
                   :subjects (:subjects env)
                   :carriers (:carriers env)
                   :item (if item-bound? (:item env)
                             (fail! id :predicate-item-unbound))
                   (fail! id :predicate-source {:source source}))]
        (strict-path-value! root path id)))

    :literal (predicate-literal! node id)

    :count
    (do
      (when-not (= 2 (count node))
        (fail! id :predicate-count-node {:node node}))
      (let [value (predicate-term! (second node) env item-bound?
                                   (inc depth) budget id)]
        (when-not (or (vector? value) (map? value))
          (fail! id :predicate-count-type
                 {:actual (some-> value class .getName)}))
        (count value)))

    (fail! id :predicate-term-operator {:operator (first node)})))

(defn- digest-term!
  [node env item-bound? depth budget id]
  (let [value (predicate-term! node env item-bound? depth budget id)]
    (when-not (sha256-id? value)
      (fail! id :predicate-digest {:value value}))
    value))

(defn- evaluate-predicate-node!
  [node env item-bound? depth budget id]
  (when-not (and (vector? node) (seq node))
    (fail! id :predicate-bool-node {:node node}))
  (swap! budget inc)
  (when (or (> @budget 100000) (> depth 256))
    (fail! id :predicate-fuel {:depth depth :nodes @budget}))
  (case (first node)
    :and
    (do
      (when (< (count node) 3)
        (fail! id :predicate-and-node {:node node}))
      (loop [children (next node)]
        (if-let [child (first children)]
          (if (true? (evaluate-predicate-node!
                      child env item-bound? (inc depth) budget id))
            (recur (next children))
            false)
          true)))

    :equal
    (do
      (when-not (= 3 (count node))
        (fail! id :predicate-equal-node {:node node}))
      (= (predicate-term! (second node) env item-bound?
                          (inc depth) budget id)
         (predicate-term! (nth node 2) env item-bound?
                          (inc depth) budget id)))

    :exact-keys
    (do
      (when-not (= 3 (count node))
        (fail! id :predicate-exact-keys-node {:node node}))
      (let [value (predicate-term! (second node) env item-bound?
                                   (inc depth) budget id)
            keys-value (predicate-term! (nth node 2) env item-bound?
                                        (inc depth) budget id)]
        (when-not (and (map? value) (vector? keys-value)
                       (every? keyword? keys-value)
                       (= (count keys-value) (count (distinct keys-value))))
          (fail! id :predicate-exact-keys-type))
        (= (set keys-value) (set (keys value)))))

    :h-equal
    (do
      (when-not (= 5 (count node))
        (fail! id :predicate-h-equal-node {:node node}))
      (let [domain (predicate-term! (second node) env item-bound?
                                    (inc depth) budget id)
            value (predicate-term! (nth node 2) env item-bound?
                                   (inc depth) budget id)
            expected (digest-term! (nth node 3) env item-bound?
                                   (inc depth) budget id)
            omit-paths (predicate-term! (nth node 4) env item-bound?
                                        (inc depth) budget id)]
        (when-not (keyword? domain)
          (fail! id :predicate-h-domain {:domain domain}))
        (= expected
           (codec/H [domain (omit-required-paths! value omit-paths id)]))))

    :r-equal
    (do
      (when-not (= 3 (count node))
        (fail! id :predicate-r-equal-node {:node node}))
      (let [value (predicate-term! (second node) env item-bound?
                                   (inc depth) budget id)
            expected (digest-term! (nth node 2) env item-bound?
                                   (inc depth) budget id)
            bytes (cond
                    (instance? (Class/forName "[B") value) value
                    (and (vector? value)
                         (every? #(and (integer? %) (<= 0 % 255)) value))
                    (byte-array (map unchecked-byte value))
                    :else (fail! id :predicate-byte-vector))]
        (= expected (codec/R bytes))))

    :every
    (do
      (when-not (= 3 (count node))
        (fail! id :predicate-every-node {:node node}))
      (when item-bound?
        (fail! id :predicate-item-shadowing))
      (let [items (predicate-term! (second node) env item-bound?
                                   (inc depth) budget id)]
        (when-not (and (vector? items) (seq items))
          (fail! id :predicate-every-type))
        (every? true?
                (map #(evaluate-predicate-node!
                       (nth node 2) (assoc env :item %) true
                       (inc depth) budget id)
                     items))))

    :not
    (do
      (when-not (= 2 (count node))
        (fail! id :predicate-not-node {:node node}))
      (not (evaluate-predicate-node!
            (second node) env item-bound? (inc depth) budget id)))

    :lte
    (do
      (when-not (= 3 (count node))
        (fail! id :predicate-lte-node {:node node}))
      (let [left (predicate-term! (second node) env item-bound?
                                  (inc depth) budget id)
            right (predicate-term! (nth node 2) env item-bound?
                                   (inc depth) budget id)]
        (when-not (and (integer? left) (not (neg? left))
                       (integer? right) (not (neg? right)))
          (fail! id :predicate-lte-type))
        (<= left right)))

    (fail! id :predicate-bool-operator {:operator (first node)})))

(defn evaluate-contract-predicate!
  "Evaluate one architecture-owned PREDICATE-SPECS program. Caller-selected
  programs are rejected; this is a closed AST interpreter, never host eval."
  [spec env]
  (exact-keys! spec
               #{:ordinal :row-ordinal :row-id :phase :obligation :program
                 :predicate-id :required-subject-paths}
               "SH07-B51-CONTRACT-DISCHARGE" :predicate-spec-keys)
  (exact-keys! env #{:context :subjects :carriers}
               "SH07-B51-CONTRACT-DISCHARGE" :predicate-env-keys)
  (exact-keys! (:context env)
               #{:row-ordinal :row-id :phase :obligation}
               "SH07-B51-CONTRACT-DISCHARGE" :predicate-context-keys)
  (when-not (and (integer? (:ordinal spec))
                 (< -1 (:ordinal spec) (count predicate-specs))
                 (= spec (nth predicate-specs (:ordinal spec)))
                 (vector? (:subjects env))
                 (map? (:carriers env)))
    (fail! "SH07-B51-CONTRACT-DISCHARGE" :predicate-spec-or-env))
  (boolean
   (evaluate-predicate-node!
   (:program spec) env false 0 (atom 0)
    "SH07-B51-CONTRACT-DISCHARGE")))

(defn resolve-contract-phase!
  "Resolve one phase from a closed carrier pool using the literal ROW-PLAN.
  This is the data-only L evaluator substrate; semantic carrier admission is a
  separate prerequisite and this function grants no evidence authority."
  [phase carriers]
  (when-not (some #{phase} phases)
    (fail! "SH07-B51-CONTRACT-DISCHARGE" :phase {:phase phase}))
  (when-not (map? carriers)
    (fail! "SH07-B51-CONTRACT-DISCHARGE" :carrier-pool))
  (let [rows
        (->> row-plan
             (keep
              (fn [row]
                (let [bindings (filterv #(= phase (:phase %))
                                        (:binding-specs row))]
                  (when (seq bindings)
                    (let [resolved
                          (mapv
                           (fn [binding]
                             (let [value
                                   (strict-path-value!
                                    carriers
                                    (into [(:carrier binding)]
                                          (:path binding))
                                    "SH07-B51-CONTRACT-DISCHARGE")
                                   without-id
                                   (assoc binding
                                          :row-ordinal (:ordinal row)
                                          :value value)]
                               (assoc without-id :value-id
                                      (codec/H
                                       [:gravity/sh07-b51-contract-binding-v2
                                        without-id]))))
                           bindings)
                          resolved-obligations
                          (filterv
                           (fn [obligation]
                             (some #(some #{obligation} (:obligations %))
                                   bindings))
                           (:obligations row))]
                      {:row-ordinal (:ordinal row)
                       :row-plan-id row-plan-id
                       :phase phase
                       :resolved-obligations resolved-obligations
                       :bindings resolved
                       :status :bindings-resolved
                       :diagnostic (:diagnostic row)})))))
             vec)
        specs-by-triple
        (into {}
              (map (fn [spec]
                     [[(:row-ordinal spec) (:phase spec) (:obligation spec)]
                      spec])
                   predicate-specs))]
    (doseq [resolved-row rows
            obligation (:resolved-obligations resolved-row)]
      (let [subjects
            (filterv #(some #{obligation} (:obligations %))
                     (:bindings resolved-row))
            spec (get specs-by-triple
                      [(:row-ordinal resolved-row) phase obligation])
            context
            {:row-ordinal (:row-ordinal resolved-row)
             :row-id (:id (nth row-plan (:row-ordinal resolved-row)))
             :phase phase
             :obligation obligation}]
        (when-not (and spec
                       (= (:required-subject-paths spec)
                          (mapv :path subjects))
                       (evaluate-contract-predicate!
                        spec {:context context
                              :subjects subjects
                              :carriers carriers}))
          (fail! "SH07-B51-CONTRACT-DISCHARGE"
                 :predicate-false
                 {:row-ordinal (:row-ordinal resolved-row)
                  :phase phase :obligation obligation}))))
    {:phase phase
     :row-plan-id row-plan-id
     :resolved-rows rows
     :resolved-root
     (codec/H
      [(get {:pre-evidence :gravity/sh07-b51-pre-resolved-row-root-v1
             :runtime :gravity/sh07-b51-runtime-resolved-row-root-v1
             :post-i9 :gravity/sh07-b51-post-i9-resolved-row-root-v1
             :post-g9 :gravity/sh07-b51-post-g9-resolved-row-root-v1
             :post-p9 :gravity/sh07-b51-post-p9-resolved-row-root-v1}
            phase)
       rows])
     :authority :none}))

(def ^:private empty-raw-sha256
  "sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")

(def ^:private no-link-options
  (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))

(def ^:private no-open-options (make-array OpenOption 0))

(defn- fail!
  ([id reason] (fail! id reason {}))
  ([id reason details]
   (throw (ex-info "SH-07 B5.1 Git evidence rejected"
                   (merge {:id id :reason reason} details)))))

(defn- exact-keys!
  [value expected id reason]
  (when-not (and (map? value) (= expected (set (keys value))))
    (fail! id reason
           {:expected-keys expected
            :actual-keys (when (map? value) (set (keys value)))}))
  value)

(defn- git-id?
  [value]
  (and (string? value) (boolean (re-matches #"[0-9a-f]{40}" value))))

(defn- sha256-id?
  [value]
  (and (string? value)
       (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn- safe-relative-path?
  [value]
  (and (string? value)
       (not (str/blank? value))
       (not (str/starts-with? value "/"))
       (not (str/includes? value "\\"))
       (not-any? #{"" "." ".."} (str/split value #"/" -1))))

(defn- require-git-id!
  [value field id]
  (when-not (git-id? value)
    (fail! id :git-id {:field field :value value}))
  value)

(defn- canonical-directory!
  [value field id]
  (when-not (instance? Path value)
    (fail! id :path-type {:field field :actual (some-> value class .getName)}))
  (let [^Path path (.normalize (.toAbsolutePath ^Path value))]
    (when-not (and (Files/isDirectory path no-link-options)
                   (not (Files/isSymbolicLink path)))
      (fail! id :directory {:field field :path (str path)}))
    path))

(defn- file-key
  [^Path path id]
  (let [attributes
        (Files/readAttributes path BasicFileAttributes no-link-options)
        key (.fileKey ^BasicFileAttributes attributes)]
    (when (nil? key)
      (fail! id :missing-file-key {:path (str path)}))
    (str key)))

(defn- decode-utf8!
  [bytes id reason]
  (let [decoder (doto (.newDecoder StandardCharsets/UTF_8)
                  (.onMalformedInput CodingErrorAction/REPORT)
                  (.onUnmappableCharacter CodingErrorAction/REPORT))]
    (try
      (.toString (.decode decoder (ByteBuffer/wrap ^bytes bytes)))
      (catch java.nio.charset.CharacterCodingException _
        (fail! id reason)))))

(defn- remaining-millis
  [deadline-nanos]
  (max 0 (long (Math/ceil
                (/ (double (- deadline-nanos (System/nanoTime)))
                   1000000.0)))))

(defn- close-quietly!
  [value]
  (when (instance? Closeable value)
    (try (.close ^Closeable value) (catch Throwable _ nil))))

(defn- terminate-process!
  [^Process process]
  (let [destroy-called (boolean (and (.isAlive process)
                                     (do (.destroy process) true)))
        destroy-forcibly-called
        (boolean
         (and (.isAlive process)
              (not (.waitFor process 250 TimeUnit/MILLISECONDS))
              (do (.destroyForcibly process) true)))]
    (when (.isAlive process)
      (.waitFor process 1000 TimeUnit/MILLISECONDS))
    {:destroy-called destroy-called
     :destroy-forcibly-called destroy-forcibly-called}))

(defn- completed-value!
  [^Future future]
  (try
    (.get future)
    (catch ExecutionException error
      (throw (or (.getCause error) error)))))

(defn- run-bounded-process!
  [{:keys [argv directory allowed-exit-codes stdout-role deadline-ms id]}]
  (when-not (and (vector? argv) (seq argv)
                 (every? #(and (string? %) (not (str/blank? %))) argv)
                 (str/starts-with? (first argv) "/"))
    (fail! id :argv {:argv argv}))
  (let [root (canonical-directory! directory :working-directory id)
        builder (doto (ProcessBuilder. ^java.util.List argv)
                  (.directory (.toFile root)))
        _ (doto (.environment builder)
            (.clear)
            (.putAll closed-environment))
        process (.start builder)
        stdout (.getInputStream process)
        stderr (.getErrorStream process)
        stdin (.getOutputStream process)
        executor (Executors/newFixedThreadPool 3)
        completions (ExecutorCompletionService. executor)
        stdout-future
        (.submit completions
                 ^Callable
                 (reify Callable
                   (call [_]
                     [:stdout ((:read-bounded-bytes transport-api)
                               stdout-role stdout)])))
        stderr-future
        (.submit completions
                 ^Callable
                 (reify Callable
                   (call [_]
                     [:stderr ((:read-bounded-bytes transport-api)
                               :stderr stderr)])))
        _ (.close stdin)
        wait-future
        (.submit completions
                 ^Callable
                 (reify Callable
                   (call [_]
                     [:exit (.waitFor process)])))
        futures [stdout-future stderr-future wait-future]
        deadline (+ (System/nanoTime) (* 1000000 (long deadline-ms)))]
    (try
      (let [parts
            (loop [remaining 3
                   result {}]
              (if (zero? remaining)
                result
                (let [millis (remaining-millis deadline)
                      completed (when (pos? millis)
                                  (.poll completions millis
                                         TimeUnit/MILLISECONDS))]
                  (when (nil? completed)
                    (throw (TimeoutException. "bounded process deadline")))
                  (let [[kind value] (completed-value! completed)]
                    (recur (dec remaining) (assoc result kind value))))))
            exit (:exit parts)
            stdout-bytes (:stdout parts)
            stderr-bytes (:stderr parts)]
        (when-not (contains? allowed-exit-codes exit)
          (fail! id :process-exit
                 {:argv argv :exit-code exit
                  :stderr-byte-count (alength ^bytes stderr-bytes)
                  :stderr-raw-sha256 (codec/R stderr-bytes)}))
        {:argv argv
         :exit-code exit
         :stdout-bytes stdout-bytes
         :stderr-bytes stderr-bytes
         :transport
         {:stdin-byte-count 0
          :stdin-raw-sha256 empty-raw-sha256
          :stdin-closed-before-wait true
          :stdout-cap (get transport-limits
                           (if (= :stderr stdout-role)
                             :stderr-max-bytes
                             :response-max-bytes))
          :stderr-cap (:stderr-max-bytes transport-limits)
          :deadline-ms deadline-ms
          :stdout-drain-started-before-wait true
          :stderr-drain-started-before-wait true
          :stdout-drain-complete true
          :stderr-drain-complete true
          :timed-out false
          :destroy-called false
          :destroy-forcibly-called false
          :tasks-joined true}})
      (catch Throwable error
        (doseq [future futures] (.cancel ^Future future true))
        (let [termination (terminate-process! process)]
          (fail! id
                 (if (instance? TimeoutException error)
                   :process-deadline
                   :process-transport)
                 {:argv argv
                  :cause (.getName (class error))
                  :destroy-called (:destroy-called termination)
                  :destroy-forcibly-called
                  (:destroy-forcibly-called termination)})))
      (finally
        (close-quietly! stdin)
        (close-quietly! stdout)
        (close-quietly! stderr)
        (.shutdownNow executor)
        (.awaitTermination executor 1000 TimeUnit/MILLISECONDS)))))

(defn- git-argv
  [^Path repository arguments]
  (into [git-executable "-C" (str repository)] arguments))

(defn- git-operation!
  [repository operation arguments allowed-exit-codes parser]
  (let [result
        (run-bounded-process!
         {:argv (git-argv repository arguments)
          :directory repository
          :allowed-exit-codes allowed-exit-codes
          :stdout-role :response
          :deadline-ms (:deadline-ms transport-limits)
          :id "SH07-B51-GIT-SNAPSHOT"})
        stdout (:stdout-bytes result)
        stderr (:stderr-bytes result)]
    (when-not (zero? (alength ^bytes stderr))
      (fail! "SH07-B51-GIT-SNAPSHOT" :git-stderr
             {:operation operation
              :stderr-byte-count (alength ^bytes stderr)
              :stderr-raw-sha256 (codec/R stderr)}))
    {:operation operation
     :argv (:argv result)
     :stdin-byte-count 0
     :stdin-raw-sha256 empty-raw-sha256
     :exit-code (:exit-code result)
     :stdout-byte-count (alength ^bytes stdout)
     :stdout-raw-sha256 (codec/R stdout)
     :stderr-byte-count 0
     :stderr-raw-sha256 empty-raw-sha256
     :parsed-result (parser stdout (:exit-code result))}))

(defn- parse-single-line!
  [bytes reason]
  (let [text (decode-utf8! bytes "SH07-B51-GIT-SNAPSHOT" reason)]
    (when-not (and (str/ends-with? text "\n")
                   (= 1 (count (filter #(= \newline %) text))))
      (fail! "SH07-B51-GIT-SNAPSHOT" reason))
    (subs text 0 (dec (count text)))))

(defn- parse-show!
  [bytes _]
  (let [text (decode-utf8! bytes "SH07-B51-GIT-SNAPSHOT" :show-utf8)
        match (re-matches
               #"([0-9a-f]{40})\n([0-9a-f]{40})\n((?:[0-9a-f]{40}(?: [0-9a-f]{40})*)?)\n"
               text)]
    (when-not match
      (fail! "SH07-B51-GIT-SNAPSHOT" :show-grammar))
    {:commit (nth match 1)
     :tree (nth match 2)
     :parents (let [parents (nth match 3)]
                (if (str/blank? parents) [] (str/split parents #" ")))}))

(defn- nul-records!
  [bytes reason]
  (let [text (decode-utf8! bytes "SH07-B51-GIT-SNAPSHOT" reason)]
    (cond
      (empty? text) []
      (not (str/ends-with? text "\u0000"))
      (fail! "SH07-B51-GIT-SNAPSHOT" reason {:missing-final-nul true})
      :else (vec (butlast (str/split text #"\u0000" -1))))))

(defn- parse-name-status!
  [parent commit bytes _]
  (let [tokens (nul-records! bytes :diff-name-status-grammar)]
    (when-not (even? (count tokens))
      (fail! "SH07-B51-GIT-SNAPSHOT" :diff-name-status-grammar))
    {:parent parent
     :commit commit
     :entries
     (mapv
      (fn [[status path]]
        (when-not (and (= "A" status) (safe-relative-path? path))
          (fail! "SH07-B51-GIT-SNAPSHOT" :diff-name-status-entry
                 {:status status :path path}))
        {:status :added :path path})
      (partition 2 tokens))}))

(defn- parse-ls-tree!
  [commit requested-paths bytes _]
  (when-not (and (vector? requested-paths)
                 (= (count requested-paths) (count (distinct requested-paths)))
                 (every? safe-relative-path? requested-paths))
    (fail! "SH07-B51-GIT-SNAPSHOT" :requested-paths))
  (let [records (nul-records! bytes :ls-tree-grammar)
        rows
        (mapv
         (fn [record]
           (let [match (re-matches
                        #"([0-7]{6}) ([a-z]+) ([0-9a-f]{40})\t(.+)"
                        record)]
             (when-not match
               (fail! "SH07-B51-GIT-SNAPSHOT" :ls-tree-record
                      {:record record}))
             (let [path (nth match 4)]
               (when-not (some #{path} requested-paths)
                 (fail! "SH07-B51-GIT-SNAPSHOT" :unrequested-tree-path
                        {:path path}))
               {:path path :mode (nth match 1) :type (nth match 2)
                :oid (nth match 3)})))
         records)
        by-path (into {} (map (juxt :path identity) rows))]
    (when-not (= (count rows) (count by-path))
      (fail! "SH07-B51-GIT-SNAPSHOT" :duplicate-tree-path))
    {:commit commit
     :requested-paths requested-paths
     :rows
     (mapv
      (fn [path]
        (if-let [row (get by-path path)]
          (assoc row :present true)
          {:path path :present false :mode nil :type nil :oid nil}))
      requested-paths)}))

(defn- show-operation!
  [repository commit]
  (git-operation!
   repository :show-tree
   ["show" "-s" "--format=%H%n%T%n%P" commit]
   #{0} parse-show!))

(defn- diff-name-status-operation!
  [repository parent commit]
  (git-operation!
   repository :diff-tree-name-status-z
   ["diff-tree" "--no-commit-id" "--name-status" "-r" "-z"
    parent commit]
   #{0} (partial parse-name-status! parent commit)))

(defn- ls-tree-operation!
  [repository commit paths]
  (git-operation!
   repository :ls-tree-z-requested
   (into ["ls-tree" "-z" commit "--"] paths)
   #{0} (partial parse-ls-tree! commit paths)))

(defn- cat-file-operation!
  [repository oid include-value?]
  (git-operation!
   repository :cat-file-blob ["cat-file" "blob" oid] #{0}
   (fn [bytes _]
     (cond-> {:oid oid
              :byte-count (alength ^bytes bytes)
              :raw-sha256 (codec/R bytes)}
       include-value? (assoc :value
                             ((:strict-read-wire transport-api)
                              :response bytes))))))

(defn- cat-file-bytes-operation!
  [repository oid]
  (let [result
        (run-bounded-process!
         {:argv (git-argv repository ["cat-file" "blob" oid])
          :directory repository
          :allowed-exit-codes #{0}
          :stdout-role :response
          :deadline-ms (:deadline-ms transport-limits)
          :id "SH07-B51-GIT-SNAPSHOT"})
        bytes (:stdout-bytes result)
        stderr (:stderr-bytes result)]
    (when-not (zero? (alength ^bytes stderr))
      (fail! "SH07-B51-GIT-SNAPSHOT" :git-stderr
             {:operation :cat-file-blob
              :stderr-byte-count (alength ^bytes stderr)
              :stderr-raw-sha256 (codec/R stderr)}))
    {:bytes bytes
     :observation
     {:operation :cat-file-blob
      :argv (:argv result)
      :stdin-byte-count 0
      :stdin-raw-sha256 empty-raw-sha256
      :exit-code 0
      :stdout-byte-count (alength ^bytes bytes)
      :stdout-raw-sha256 (codec/R bytes)
      :stderr-byte-count 0
      :stderr-raw-sha256 empty-raw-sha256
      :parsed-result {:oid oid
                      :byte-count (alength ^bytes bytes)
                      :raw-sha256 (codec/R bytes)}}}))

(def ^:private document-binding-ids
  (mapv
   (fn [[document-ordinal _]]
     (->> row-plan
          (filter #(some #{document-ordinal} (:document-ordinals %)))
          (mapcat :binding-specs)
          (mapv :id)))
   document-paths))

(defn- require-present-blob-row!
  [row path id]
  (when-not (= {:path path :present true :mode "100644" :type "blob"}
               (select-keys row [:path :present :mode :type]))
    (fail! id :pre-c-path
           {:path path :row row}))
  (require-git-id! (:oid row) :blob-oid id)
  row)

(defn- blob-descriptor
  [ordinal path row bytes]
  {:ordinal ordinal
   :path path
   :mode "100644"
   :blob-oid (:oid row)
   :byte-count (alength ^bytes bytes)
   :raw-sha256 (codec/R bytes)})

(defn- build-document-read-receipt
  [content rows-by-path bytes-by-path]
  (let [documents
        (mapv (fn [[ordinal path]]
                (blob-descriptor ordinal path (get rows-by-path path)
                                 (get bytes-by-path path)))
              document-paths)
        events
        (mapv (fn [{:keys [ordinal path raw-sha256]} binding-ids]
                {:ordinal ordinal
                 :document-ordinal ordinal
                 :path path
                 :raw-sha256 raw-sha256
                 :binding-ids binding-ids})
              documents document-binding-ids)]
    {:schema :gravity/sh07-b51-contract-read-receipt-v1
     :content content
     :documents documents
     :events events
     :event-root
     (codec/H [:gravity/sh07-b51-contract-document-event-root-v1 events])
     :counters {:acquire-count 33 :decode-count 33
                :event-count 33 :finalize-count 1}
     :finalized true}))

(defn- build-source-descriptors
  [rows-by-path bytes-by-path]
  (mapv
   (fn [[ordinal kind module-ordinal path]]
     (let [row (get rows-by-path path)
           bytes (get bytes-by-path path)]
       {:ordinal ordinal :kind kind :module-ordinal module-ordinal
        :path path :mode "100644" :blob-oid (:oid row)
        :byte-count (alength ^bytes bytes) :raw-sha256 (codec/R bytes)}))
   source-specs))

(defn- build-owned-c-descriptors
  [rows-by-path bytes-by-path]
  (mapv (fn [ordinal path]
          (blob-descriptor ordinal path (get rows-by-path path)
                           (get bytes-by-path path)))
        (range) owned-c-paths))

(def ^:private compatibility-row
  {:stage :seed-hosted-validation
   :compiler-owner :clojure-seed
   :implemented-documents [:L1 :L2 :C2 :C6]
   :language-subset :sh07-b51-verified-core
   :profiles [:meta]
   :backends []
   :runtime-services [:jvm-process-isolation :git-object-source]
   :package-build-features []
   :standard-library-subset []
   :conformance-suites [:sh07-b51-v9-contract-discharge]
   :known-gaps [:self-hosted-compiler :mir :backend :release]
   :release-eligible false})

(defn- boot5-object-descriptor
  [commit tree row bytes]
  {:commit commit :tree tree :path boot5-path :mode "100644" :type "blob"
   :blob-oid (:oid row) :byte-count (alength ^bytes bytes)
   :raw-sha256 (codec/R bytes)})

(defn- build-boot5-compatibility
  [content baseline-show candidate-row candidate-bytes baseline-row
   baseline-bytes]
  (let [candidate (boot5-object-descriptor
                   (:commit content) (:tree content) candidate-row
                   candidate-bytes)
        baseline (boot5-object-descriptor
                  pre-baseline-commit (:tree baseline-show) baseline-row
                  baseline-bytes)
        same-blob (select-keys candidate
                               [:path :mode :type :blob-oid :byte-count
                                :raw-sha256])]
    (when-not (= same-blob
                 (select-keys baseline
                              [:path :mode :type :blob-oid :byte-count
                               :raw-sha256]))
      (fail! "SH07-B51-CONTRACT-DISCHARGE" :boot5-baseline
             {:baseline baseline :candidate candidate}))
    {:baseline-document baseline
     :candidate-document candidate
     :document-equal true
     :compatibility-row compatibility-row
     :compatibility-digest
     (codec/H [:gravity/sh07-b51-boot5-compatibility-row-v1
               compatibility-row])
     :explicit-gaps (:known-gaps compatibility-row)
     :stage-support-claims []
     :stage-advanced false}))

(defn reconstruct-pre-object-snapshot!
  "Reconstruct the literal C-object PRE carrier foundation with exactly 77
  bounded Git operations. The result deliberately has no evidence authority;
  complete PRE discharge additionally requires the architecture-defined A9
  lifecycle and non-cyclic masked postimage carriers."
  [{:keys [repository-root content]}]
  (exact-keys! content #{:commit :tree} "SH07-B51-GIT-SNAPSHOT" :content)
  (require-git-id! (:commit content) :commit "SH07-B51-GIT-SNAPSHOT")
  (require-git-id! (:tree content) :tree "SH07-B51-GIT-SNAPSHOT")
  (let [repository (canonical-directory!
                    repository-root :repository-root
                    "SH07-B51-GIT-SNAPSHOT")
        show-c (show-operation! repository (:commit content))
        tree-c (ls-tree-operation! repository (:commit content) pre-c-paths)
        _ (when-not (= content
                       (select-keys (:parsed-result show-c) [:commit :tree]))
            (fail! "SH07-B51-GIT-SNAPSHOT" :content-coordinate
                   {:expected content :actual (:parsed-result show-c)}))
        rows (get-in tree-c [:parsed-result :rows])
        _ (doseq [[path row] (map vector pre-c-paths rows)]
            (require-present-blob-row! row path "SH07-B51-GIT-SNAPSHOT"))
        rows-by-path (into {} (map (juxt :path identity) rows))
        cat-results
        (mapv #(cat-file-bytes-operation!
                repository (:oid (get rows-by-path %)))
              pre-c-paths)
        bytes-by-path
        (into {} (map vector pre-c-paths (map :bytes cat-results)))
        show-base (show-operation! repository pre-baseline-commit)
        base-tree (get-in show-base [:parsed-result :tree])
        tree-base (ls-tree-operation! repository pre-baseline-commit
                                      [boot5-path])
        baseline-row (first (get-in tree-base [:parsed-result :rows]))
        _ (require-present-blob-row! baseline-row boot5-path
                                     "SH07-B51-GIT-SNAPSHOT")
        baseline-cat (cat-file-bytes-operation! repository (:oid baseline-row))
        observations
        (mapv (fn [ordinal observation] (assoc observation :ordinal ordinal))
              (range 77)
              (concat [show-c tree-c]
                      (map :observation cat-results)
                      [show-base tree-base (:observation baseline-cat)]))
        static-paths
        (vec (concat (map second document-paths) owned-c-paths a9-pre-paths))
        decoded-static
        (into {}
              (map (fn [path]
                     [path (decode-utf8!
                            (get bytes-by-path path)
                            "SH07-B51-GIT-SNAPSHOT" :pre-static-utf8)]))
              static-paths)
        document-read-receipt
        (build-document-read-receipt content rows-by-path bytes-by-path)
        source-descriptors
        (build-source-descriptors rows-by-path bytes-by-path)
        owned-c-descriptors
        (build-owned-c-descriptors rows-by-path bytes-by-path)
        boot5-compatibility
        (build-boot5-compatibility
         content (:parsed-result show-base)
         (get rows-by-path boot5-path) (get bytes-by-path boot5-path)
         baseline-row (:bytes baseline-cat))]
    (when-not (and (= 77 (count observations))
                   (= (vec (range 77)) (mapv :ordinal observations))
                   (= 63 (count static-paths))
                   (= 63 (count (distinct static-paths)))
                   (= 33 (count (:documents document-read-receipt)))
                   (= 33 (count (:events document-read-receipt)))
                   (= 12 (count source-descriptors))
                   (= 27 (count owned-c-descriptors)))
      (fail! "SH07-B51-CONTRACT-DISCHARGE" :pre-object-census))
    {:content content
     :baseline {:commit pre-baseline-commit :tree base-tree}
     :pre-c-paths pre-c-paths
     :observations observations
     :git-operation-count 77
     :document-read-receipt document-read-receipt
     :source-descriptors source-descriptors
     :source-descriptors-root
     (codec/H [:gravity/sh07-b51-source-descriptors-root-v1
               source-descriptors])
     :owned-c-descriptors owned-c-descriptors
     :owned-c-root
     (codec/H [:gravity/sh07-b51-owned-c-descriptors-root-v1
               owned-c-descriptors])
     :boot5-compatibility boot5-compatibility
     :static-decode-count (count decoded-static)
     :decoded-static decoded-static
     :completion-gaps
     [:a9-lifecycle-object-absent-from-authoritative-main
      :masked-postimage-depends-on-forbidden-future-runtime-values
      :complete-static-carrier-schema-validation]
     :authority :none}))

(defn validate-content-object!
  "Validate exact immutable C/T with the closed show grammar."
  [{:keys [repository-root content]}]
  (exact-keys! content #{:commit :tree} "SH07-B51-GIT-SNAPSHOT" :content)
  (require-git-id! (:commit content) :commit "SH07-B51-GIT-SNAPSHOT")
  (require-git-id! (:tree content) :tree "SH07-B51-GIT-SNAPSHOT")
  (let [repository (canonical-directory!
                    repository-root :repository-root
                    "SH07-B51-GIT-SNAPSHOT")
        observation (show-operation! repository (:commit content))]
    (when-not (= content (select-keys (:parsed-result observation)
                                     [:commit :tree]))
      (fail! "SH07-B51-GIT-SNAPSHOT" :content-coordinate
             {:expected content :actual (:parsed-result observation)}))
    {:content content
     :parents (get-in observation [:parsed-result :parents])
     :observation (assoc observation :ordinal 0)
     :authority :none}))

(def ^:private eligibility-coordinate-keys
  #{:schema :commit :tree :parents :path :mode :type :blob-oid :byte-count
    :raw-sha256 :receipt-id})

(def ^:private eligibility-receipt-keys
  #{:artifact :schema-version :content :freeze :source-descriptors
    :source-descriptors-root :masked-root :pre-evidence-contract-discharge
    :review :state :self-id-omits :nonclaims :receipt-id})

(def ^:private pre-discharge-keys
  #{:artifact :schema-version :phase :row-plan-id :prior-ref :evidence
    :resolved-rows :resolved-root :authority :self-id-omits :discharge-id})

(def ^:private pre-carrier-keys
  #{:content :document-read-receipt :source-descriptors
    :source-descriptors-root :masked-root :owned-c-descriptors :owned-c-root
    :row-plan :foundation-static :boot5-compatibility :ownership-plan
    :catalog-projection :governance-rules :identity-domain-registry
    :publication-boundary :proof-scope :progress :nonclaims :a9-lifecycle})

(defn- validate-source-descriptors!
  [descriptors]
  (when-not (and (vector? descriptors) (= 12 (count descriptors)))
    (fail! "SH07-B51-RECEIPT-JOIN" :source-descriptor-count))
  (doseq [[expected descriptor] (map vector source-specs descriptors)]
    (exact-keys! descriptor
                 #{:ordinal :kind :module-ordinal :path :mode :blob-oid
                   :byte-count :raw-sha256}
                 "SH07-B51-RECEIPT-JOIN" :source-descriptor-keys)
    (let [[ordinal kind module-ordinal path] expected]
      (when-not (and (= [ordinal kind module-ordinal path "100644"]
                        ((juxt :ordinal :kind :module-ordinal :path :mode)
                         descriptor))
                     (git-id? (:blob-oid descriptor))
                     (integer? (:byte-count descriptor))
                     (not (neg? (:byte-count descriptor)))
                     (sha256-id? (:raw-sha256 descriptor)))
        (fail! "SH07-B51-RECEIPT-JOIN" :source-descriptor
               {:ordinal ordinal :descriptor descriptor}))))
  descriptors)

(defn- validate-pre-discharge-envelope!
  [content receipt]
  (exact-keys! receipt pre-discharge-keys
               "SH07-B51-RECEIPT-JOIN" :pre-discharge-keys)
  (exact-keys! (:evidence receipt)
               #{:schema :phase :row-plan-id :carriers}
               "SH07-B51-RECEIPT-JOIN" :pre-phase-evidence-keys)
  (let [carriers (get-in receipt [:evidence :carriers])]
    (exact-keys! carriers pre-carrier-keys
                 "SH07-B51-RECEIPT-JOIN" :pre-carrier-keys)
    (exact-keys! (:masked-root carriers)
                 #{:coordinates :preimage-spans :postimage-spans
                   :preimage-files :postimage-files :preimage-root
                   :postimage-root :equal}
                 "SH07-B51-RECEIPT-JOIN" :masked-root-keys)
    (let [resolved (resolve-contract-phase! :pre-evidence carriers)]
      (when-not
     (and (= :gravity/sh07-b51-pre-evidence-contract-discharge-v1
             (:artifact receipt))
          (= 1 (:schema-version receipt))
          (= :pre-evidence (:phase receipt))
          (= row-plan-id (:row-plan-id receipt))
          (nil? (:prior-ref receipt))
          (= :gravity/sh07-b51-phase-evidence-v1
             (get-in receipt [:evidence :schema]))
          (= :pre-evidence (get-in receipt [:evidence :phase]))
          (= row-plan-id (get-in receipt [:evidence :row-plan-id]))
          (= content (:content carriers))
          (= row-plan (:row-plan carriers))
          (= [[:discharge-id]] (:self-id-omits receipt))
          (= (:discharge-id receipt)
             (codec/H
              [:gravity/sh07-b51-pre-evidence-contract-discharge-v1
               (dissoc receipt :discharge-id)]))
          (= (:resolved-root resolved) (:resolved-root receipt))
          (= (:resolved-rows resolved) (:resolved-rows receipt)))
        (fail! "SH07-B51-RECEIPT-JOIN" :pre-discharge-envelope))))
  receipt)

(defn- validate-eligibility-receipt!
  [content receipt]
  (exact-keys! receipt eligibility-receipt-keys
               "SH07-B51-RECEIPT-JOIN" :eligibility-receipt-keys)
  (exact-keys! (:freeze receipt)
               #{:state :candidate-commit :candidate-tree
                 :active-record-ref :frozen-at}
               "SH07-B51-RECEIPT-JOIN" :eligibility-freeze-keys)
  (exact-keys! (get-in receipt [:freeze :active-record-ref])
               #{:container :value-path :record-id :transition}
               "SH07-B51-RECEIPT-JOIN" :active-record-ref-keys)
  (exact-keys! (get-in receipt [:freeze :active-record-ref :container])
               #{:commit :tree :parents :path :mode :type :blob-oid
                 :byte-count :raw-sha256}
               "SH07-B51-RECEIPT-JOIN" :active-record-container-keys)
  (exact-keys! (get-in receipt [:freeze :active-record-ref :transition])
               #{:workstream-id :from :to :candidate-commit :candidate-tree
                 :architecture-report-sha256 :history-ordinal}
               "SH07-B51-RECEIPT-JOIN" :active-record-transition-keys)
  (exact-keys! (:review receipt)
               #{:reviewer :candidate-commit :candidate-tree :artifact-path
                 :artifact-raw-sha256 :result :completed-at}
               "SH07-B51-RECEIPT-JOIN" :eligibility-review-keys)
  (validate-source-descriptors! (:source-descriptors receipt))
  (exact-keys! (:masked-root receipt)
               #{:coordinates :preimage-spans :postimage-spans
                 :preimage-files :postimage-files :preimage-root
                 :postimage-root :equal}
               "SH07-B51-RECEIPT-JOIN" :masked-root-keys)
  (validate-pre-discharge-envelope!
   content (:pre-evidence-contract-discharge receipt))
  (let [expected-id
        (codec/H
         [:gravity/sh07-b51-evidence-eligibility-receipt-v2
          (dissoc receipt :receipt-id)])]
    (when-not
     (and (= :gravity/sh07-b51-evidence-eligibility-receipt-v2
             (:artifact receipt))
          (= 2 (:schema-version receipt))
          (= content (:content receipt))
          (= :evidence-eligible (:state receipt))
          (= [[:receipt-id]] (:self-id-omits receipt))
          (= nonclaims (:nonclaims receipt))
          (= expected-id (:receipt-id receipt))
          (= :frozen (get-in receipt [:freeze :state]))
          (= [(:commit content) (:tree content)]
             [(get-in receipt [:freeze :candidate-commit])
              (get-in receipt [:freeze :candidate-tree])])
          (= [(:commit content) (:tree content)]
             [(get-in receipt [:review :candidate-commit])
              (get-in receipt [:review :candidate-tree])])
          (= :evidence-eligible (get-in receipt [:review :result]))
          (= "1970-01-01T00:00:00Z"
             (get-in receipt [:review :completed-at]))
          (string? (get-in receipt [:review :reviewer]))
          (not (str/blank? (get-in receipt [:review :reviewer])))
          (safe-relative-path? (get-in receipt [:review :artifact-path]))
          (sha256-id? (get-in receipt [:review :artifact-raw-sha256]))
          (= (codec/H
              [:gravity/sh07-b51-source-descriptors-root-v1
               (:source-descriptors receipt)])
             (:source-descriptors-root receipt))
          (= (:source-descriptors receipt)
             (get-in receipt
                     [:pre-evidence-contract-discharge :evidence :carriers
                      :source-descriptors]))
          (= (:source-descriptors-root receipt)
             (get-in receipt
                     [:pre-evidence-contract-discharge :evidence :carriers
                      :source-descriptors-root]))
          (= (:masked-root receipt)
             (get-in receipt
                     [:pre-evidence-contract-discharge :evidence :carriers
                      :masked-root])))
      (fail! "SH07-B51-RECEIPT-JOIN" :eligibility-receipt)))
  receipt)

(defn validate-eligibility-object!
  "Validate Q/TQ as the direct sibling-lineage child of C using exactly five
  Git object operations.  This checks the immutable coordinate and receipt
  identity; it does not pretend to perform the separately owned 77-operation
  pre-discharge reconstruction."
  [{:keys [repository-root content eligibility-coordinate]}]
  (exact-keys! content #{:commit :tree} "SH07-B51-GIT-SNAPSHOT" :content)
  (exact-keys! eligibility-coordinate eligibility-coordinate-keys
               "SH07-B51-RECEIPT-JOIN" :eligibility-coordinate-keys)
  (doseq [[field value] [[:content-commit (:commit content)]
                         [:content-tree (:tree content)]
                         [:eligibility-commit (:commit eligibility-coordinate)]
                         [:eligibility-tree (:tree eligibility-coordinate)]
                         [:eligibility-blob (:blob-oid eligibility-coordinate)]]]
    (require-git-id! value field "SH07-B51-RECEIPT-JOIN"))
  (when-not
   (and (= :gravity/sh07-b51-external-review-object-v1
           (:schema eligibility-coordinate))
        (= [(:commit content)] (:parents eligibility-coordinate))
        (= eligibility-receipt-path (:path eligibility-coordinate))
        (= "100644" (:mode eligibility-coordinate))
        (= "blob" (:type eligibility-coordinate))
        (integer? (:byte-count eligibility-coordinate))
        (not (neg? (:byte-count eligibility-coordinate)))
        (sha256-id? (:raw-sha256 eligibility-coordinate))
        (sha256-id? (:receipt-id eligibility-coordinate)))
    (fail! "SH07-B51-RECEIPT-JOIN" :eligibility-coordinate))
  (let [repository
        (canonical-directory! repository-root :repository-root
                              "SH07-B51-GIT-SNAPSHOT")
        c (:commit content)
        q (:commit eligibility-coordinate)
        path (:path eligibility-coordinate)
        show-q (show-operation! repository q)
        diff-cq (diff-name-status-operation! repository c q)
        tree-q (ls-tree-operation! repository q [path])
        blob-q (cat-file-operation! repository
                                     (:blob-oid eligibility-coordinate) true)
        tree-c (ls-tree-operation! repository c [path])
        observations
        (mapv #(assoc %1 :ordinal %2)
              [show-q diff-cq tree-q blob-q tree-c] (range 5))
        receipt (get-in blob-q [:parsed-result :value])
        q-row (first (get-in tree-q [:parsed-result :rows]))
        c-row (first (get-in tree-c [:parsed-result :rows]))]
    (validate-eligibility-receipt! content receipt)
    (when-not
     (and (= {:commit q :tree (:tree eligibility-coordinate)
              :parents [c]}
             (:parsed-result show-q))
          (= {:parent c :commit q
              :entries [{:status :added :path path}]}
             (:parsed-result diff-cq))
          (= {:path path :present true :mode "100644" :type "blob"
              :oid (:blob-oid eligibility-coordinate)}
             q-row)
          (= {:path path :present false :mode nil :type nil :oid nil}
             c-row)
          (= (:byte-count eligibility-coordinate)
             (get-in blob-q [:parsed-result :byte-count]))
          (= (:raw-sha256 eligibility-coordinate)
             (get-in blob-q [:parsed-result :raw-sha256]))
          (= (:receipt-id eligibility-coordinate) (:receipt-id receipt)))
      (fail! "SH07-B51-RECEIPT-JOIN" :eligibility-object-join))
    (let [pre (reconstruct-pre-object-snapshot!
               {:repository-root repository :content content})
          pre-carriers
          (get-in receipt
                  [:pre-evidence-contract-discharge :evidence :carriers])]
      (when-not
       (and (= (:source-descriptors pre) (:source-descriptors receipt))
            (= (:source-descriptors-root pre)
               (:source-descriptors-root receipt))
            (= (:document-read-receipt pre)
               (:document-read-receipt pre-carriers))
            (= (:source-descriptors pre)
               (:source-descriptors pre-carriers))
            (= (:source-descriptors-root pre)
               (:source-descriptors-root pre-carriers))
            (= (:owned-c-descriptors pre)
               (:owned-c-descriptors pre-carriers))
            (= (:owned-c-root pre) (:owned-c-root pre-carriers))
            (= (:boot5-compatibility pre)
               (:boot5-compatibility pre-carriers)))
        (fail! "SH07-B51-RECEIPT-JOIN" :pre-object-carrier-join))
      {:coordinate eligibility-coordinate
       :receipt receipt
       :observations observations
       :operation-count 5
       :pre-object-snapshot pre
       :pre-discharge-observations (:observations pre)
       :pre-discharge-git-operation-count 77
       :scope :immutable-q-coordinate-and-pre-foundation
       :remaining-gaps (:completion-gaps pre)
       :authority :none})))

(defn- status-clean-operation!
  [repository]
  (git-operation!
   repository :status-clean
   ["status" "--porcelain=v1" "-z" "--untracked-files=all"] #{0}
   (fn [bytes _]
     {:clean (zero? (alength ^bytes bytes))
      :untracked-count
      (if (zero? (alength ^bytes bytes)) 0
          (count (filter #(or (str/starts-with? % "??")
                              (str/ends-with? % "??"))
                         (nul-records! bytes :status-grammar))))})))

(defn- rev-parse-line!
  [repository arguments]
  (git-operation!
   repository :rev-parse arguments #{0}
   (fn [bytes _] {:value (parse-single-line! bytes :rev-parse-grammar)})))

(defn validate-worktree-coordinate!
  "Validate one clean registered worktree at exact C/T with E9 absent."
  [{:keys [worktree content]}]
  (exact-keys! content #{:commit :tree} "SH07-B51-GIT-SNAPSHOT" :content)
  (let [root (canonical-directory! worktree :worktree
                                   "SH07-B51-GIT-SNAPSHOT")
        head (rev-parse-line! root ["rev-parse" "--verify" "HEAD^{commit}"])
        common (rev-parse-line!
                root ["rev-parse" "--path-format=absolute" "--git-common-dir"])
        show (show-operation! root (:commit content))
        status (status-clean-operation! root)
        absent (ls-tree-operation! root (:commit content) evidence-paths)
        common-path (Paths/get (get-in common [:parsed-result :value])
                               (make-array String 0))
        coordinate
        {:canonical-path (str root)
         :git-common-directory
         (str (.normalize (.toAbsolutePath common-path)))
         :file-key (file-key root "SH07-B51-GIT-SNAPSHOT")
         :commit (:commit content)
         :tree (:tree content)
         :index-clean (get-in status [:parsed-result :clean])
         :untracked-count (get-in status [:parsed-result :untracked-count])
         :evidence-paths-absent
         (every? (complement :present)
                 (get-in absent [:parsed-result :rows]))}]
    (when-not
     (and (= (:commit content) (get-in head [:parsed-result :value]))
          (= content (select-keys (:parsed-result show) [:commit :tree]))
          (:index-clean coordinate)
          (zero? (:untracked-count coordinate))
          (:evidence-paths-absent coordinate))
      (fail! "SH07-B51-GIT-SNAPSHOT" :worktree-coordinate
             {:coordinate coordinate}))
    coordinate))

(defn validate-publication-worktrees!
  "Validate distinct clean C source and publication worktrees in one object
  database."
  [{:keys [source-worktree publication-worktree content]}]
  (let [source (validate-worktree-coordinate!
                {:worktree source-worktree :content content})
        publication (validate-worktree-coordinate!
                     {:worktree publication-worktree :content content})]
    (when-not
     (and (not= (:canonical-path source) (:canonical-path publication))
          (not= (:file-key source) (:file-key publication))
          (= (:git-common-directory source)
             (:git-common-directory publication)))
      (fail! "SH07-B51-GIT-SNAPSHOT" :publication-worktree-separation))
    {:source-worktree source
     :publication-worktree publication
     :authority :none}))

(def ^:private payload-descriptor-keys
  #{:ordinal :name :kind :relative-path :wire-codec :byte-count :raw-sha256
    :value-id})

(defn- list-entry-names!
  [^Path directory id]
  (with-open [^DirectoryStream stream (Files/newDirectoryStream directory)]
    (->> (iterator-seq (.iterator stream))
         (mapv #(.toString (.getFileName ^Path %)))
         sort
         vec)))

(defn- read-bounded-file!
  [^Path path]
  (when-not (and (Files/isRegularFile path no-link-options)
                 (not (Files/isSymbolicLink path)))
    (fail! "SH07-B51-GIT-INPUT-BLOB" :staged-payload-file
           {:path (str path)}))
  (with-open [input (Files/newInputStream path no-open-options)]
    ((:read-bounded-bytes transport-api) :response input)))

(defn validate-staged-payloads!
  "Strictly validate the six canonical M payloads and their descriptors."
  [{:keys [staging-root payload-descriptors]}]
  (when-not (and (vector? payload-descriptors)
                 (= 6 (count payload-descriptors)))
    (fail! "SH07-B51-GIT-INPUT-BLOB" :payload-descriptor-count))
  (let [root (canonical-directory! staging-root :staging-root
                                   "SH07-B51-GIT-INPUT-BLOB")
        names (list-entry-names! root "SH07-B51-GIT-INPUT-BLOB")]
    (when-not (= (vec (sort payload-names)) names)
      (fail! "SH07-B51-GIT-INPUT-BLOB" :staging-topology
             {:expected (vec (sort payload-names)) :actual names}))
    (mapv
     (fn [[ordinal name kind] descriptor]
       (exact-keys! descriptor payload-descriptor-keys
                    "SH07-B51-GIT-INPUT-BLOB" :payload-descriptor-keys)
       (let [path (.resolve root name)
             bytes (read-bounded-file! path)
             value ((:strict-read-wire transport-api) :response bytes)
             expected
             {:ordinal ordinal
              :name name
              :kind kind
              :relative-path (str evidence-relative-root "/" name)
              :wire-codec :gravity/sh07-b51-edn-wire-v1
              :byte-count (alength ^bytes bytes)
              :raw-sha256 (codec/R bytes)
              :value-id
              (codec/H
               [:gravity/sh07-b51-staged-payload-value-v1
                {:ordinal ordinal :name name :kind kind :value value}])}]
         (when-not (= expected descriptor)
           (fail! "SH07-B51-GIT-INPUT-BLOB" :payload-descriptor
                  {:ordinal ordinal :expected expected :actual descriptor}))
         {:ordinal ordinal :name name :kind kind :path path
          :bytes bytes :value value :descriptor descriptor
          :file-key (file-key path "SH07-B51-GIT-INPUT-BLOB")}))
     payload-specs payload-descriptors)))

(defn validate-m-response!
  "Strictly load the closed M response and join all six staged payloads.

  Coordinator/contract semantic validation is intentionally not inferred here;
  callers receive `:authority :none` until that separately owned seam exists."
  [{:keys [content staging-root response-bytes]}]
  (let [response ((:strict-read-wire transport-api)
                  :response response-bytes)]
    (exact-keys! response
                 #{:schema :status :content :payload-descriptors
                   :staging-receipt :coordinator-receipt
                   :source-read-receipt :counters :transport}
                 "SH07-B51-CHECKER-PROCESS" :m-response-keys)
    (exact-keys! (:staging-receipt response)
                 #{:staging-path :evidence-relative-root :directory-mode
                   :symlink-free :payload-names :payload-descriptors}
                 "SH07-B51-CHECKER-PROCESS" :m-staging-receipt-keys)
    (exact-keys! (:transport response)
                 #{:response-preimage-id :self-id-omits}
                 "SH07-B51-CHECKER-PROCESS" :m-transport-keys)
    (let [response-id
          (codec/H
           [:gravity/sh07-b51-response-preimage-id-v1
            (update response :transport dissoc :response-preimage-id)])
          staged (validate-staged-payloads!
                  {:staging-root staging-root
                   :payload-descriptors (:payload-descriptors response)})]
      (when-not
       (and (= :gravity/sh07-b51-m-response-v1 (:schema response))
            (= :accepted (:status response))
            (= content (:content response))
            (= (str (canonical-directory!
                     staging-root :staging-root
                     "SH07-B51-CHECKER-PROCESS"))
               (:staging-path (:staging-receipt response)))
            (= evidence-relative-root
               (:evidence-relative-root (:staging-receipt response)))
            (= "0700" (:directory-mode (:staging-receipt response)))
            (true? (:symlink-free (:staging-receipt response)))
            (= payload-names (:payload-names (:staging-receipt response)))
            (= (:payload-descriptors response)
               (:payload-descriptors (:staging-receipt response)))
            (= [[:transport :response-preimage-id]]
               (:self-id-omits (:transport response)))
            (= response-id (get-in response
                                   [:transport :response-preimage-id])))
        (fail! "SH07-B51-CHECKER-PROCESS" :m-response-join))
      {:response response :staged-payloads staged
       :response-byte-count (alength ^bytes response-bytes)
       :response-raw-sha256 (codec/R response-bytes)
       :response-preimage-id response-id
       :authority :none})))

(defn coordinator-argv
  "Construct the exact 39-token M argv.  The four-entry classpath is accepted
  only as transport substrate; this function does not claim its missing C/T
  manifest validation."
  [{:keys [classpath content staging-root eligibility-coordinate]}]
  (exact-keys! content #{:commit :tree}
               "SH07-B51-CHECKER-PROCESS" :content)
  (exact-keys! eligibility-coordinate eligibility-coordinate-keys
               "SH07-B51-CHECKER-PROCESS" :eligibility-coordinate)
  (doseq [[field value] [[:content-commit (:commit content)]
                         [:content-tree (:tree content)]
                         [:eligibility-commit (:commit eligibility-coordinate)]
                         [:eligibility-tree (:tree eligibility-coordinate)]
                         [:eligibility-blob (:blob-oid eligibility-coordinate)]]]
    (require-git-id! value field "SH07-B51-CHECKER-PROCESS"))
  (when-not (and (= [(:commit content)] (:parents eligibility-coordinate))
                 (= eligibility-receipt-path
                    (:path eligibility-coordinate))
                 (= "100644" (:mode eligibility-coordinate))
                 (= "blob" (:type eligibility-coordinate))
                 (integer? (:byte-count eligibility-coordinate))
                 (not (neg? (:byte-count eligibility-coordinate)))
                 (sha256-id? (:raw-sha256 eligibility-coordinate))
                 (sha256-id? (:receipt-id eligibility-coordinate)))
    (fail! "SH07-B51-CHECKER-PROCESS" :eligibility-coordinate))
  (let [entries (if (vector? classpath) classpath
                    (str/split (or classpath "")
                               (re-pattern
                                (java.util.regex.Pattern/quote
                                 java.io.File/pathSeparator)) -1))
        _ (when-not
           (and (= 4 (count entries)) (= 4 (count (distinct entries)))
                (every? #(and (string? %) (not (str/blank? %))
                              (.isAbsolute
                               (Paths/get % (make-array String 0))))
                        entries))
            (fail! "SH07-B51-TRUSTED-TCB" :classpath))
        classpath-string (str/join java.io.File/pathSeparator entries)
        java-path
        (str (.normalize
              (.toAbsolutePath
               (Paths/get (System/getProperty "java.home")
                          (into-array String ["bin" "java"])))))
        stage (str (canonical-directory! staging-root :staging-root
                                         "SH07-B51-CHECKER-PROCESS"))]
    [java-path "-Xms64m" "-Xmx8g" "-Dfile.encoding=UTF-8"
     "-Duser.language=en" "-Duser.country=US" "-Duser.timezone=UTC"
     "-Djava.awt.headless=true" "-Dclojure.main.report=stderr"
     "-Duser.home=/nonexistent" "-cp" classpath-string "clojure.main" "-m"
     "gravity.tools.sh07-b51-attempt32-coordinator-child"
     "--snapshot-commit" (:commit content)
     "--snapshot-tree" (:tree content)
     "--completed-at" "1970-01-01T00:00:00Z"
     "--evidence-relative-root" evidence-relative-root
     "--staging-root" stage
     "--eligibility-review-commit" (:commit eligibility-coordinate)
     "--eligibility-review-tree" (:tree eligibility-coordinate)
     "--eligibility-receipt-path" (:path eligibility-coordinate)
     "--eligibility-receipt-blob" (:blob-oid eligibility-coordinate)
     "--eligibility-receipt-byte-count" (str (:byte-count eligibility-coordinate))
     "--eligibility-receipt-raw-sha256" (:raw-sha256 eligibility-coordinate)
     "--eligibility-receipt-id" (:receipt-id eligibility-coordinate)]))

(defn launch-coordinator-transport!
  "Launch one fresh M process with bounded concurrent stdout/stderr drains.

  This is an explicitly non-authoritative integration seam until the C-derived
  classpath materializer and full launcher-receipt validator are joined."
  [{:keys [repository-root content staging-root] :as options}]
  (let [repository (canonical-directory! repository-root :repository-root
                                         "SH07-B51-CHECKER-PROCESS")
        argv (coordinator-argv options)
        process-result
        (run-bounded-process!
         {:argv argv :directory repository :allowed-exit-codes #{0}
          :stdout-role :response
          :deadline-ms (:deadline-ms transport-limits)
          :id "SH07-B51-CHECKER-PROCESS"})
        stderr (:stderr-bytes process-result)]
    (when-not (zero? (alength ^bytes stderr))
      (fail! "SH07-B51-CHECKER-PROCESS" :m-stderr
             {:stderr-byte-count (alength ^bytes stderr)
              :stderr-raw-sha256 (codec/R stderr)}))
    (merge
     (validate-m-response!
      {:content content :staging-root staging-root
       :response-bytes (:stdout-bytes process-result)})
     {:argv argv
      :argv-id (codec/H [:gravity/sh07-b51-process-argv-id-v1 argv])
      :transport (:transport process-result)
      :authority :none})))

(def ^:private identity-receipt-keys
  #{:artifact :schema-version :content :semantic-ordinals
    :coordinator-events :coordinator-event-root :child-process-receipts
    :launcher-receipt :coordinator-receipt :payload-joins
    :evidence-eligibility :pre-discharge-id :runtime-contract-discharge
    :receipt-id-normalization :self-id-omits :receipt-id :nonclaims})

(defn- validate-identity-receipt-envelope!
  [content receipt]
  (exact-keys! receipt identity-receipt-keys
               "SH07-B51-RECEIPT-JOIN" :identity-receipt-keys)
  (when-not
   (and (= :gravity/sh07-b51-checker-process-identity-receipt-v1
           (:artifact receipt))
        (= 1 (:schema-version receipt))
        (= content (:content receipt))
        (= (vec (range 11)) (:semantic-ordinals receipt))
        (= [[:receipt-id]] (:self-id-omits receipt))
        (sha256-id? (:coordinator-event-root receipt))
        (sha256-id? (:pre-discharge-id receipt))
        (sha256-id? (:receipt-id receipt))
        (= nonclaims (:nonclaims receipt)))
    (fail! "SH07-B51-RECEIPT-JOIN" :identity-receipt-envelope))
  receipt)

(defn- existing-object-descriptor
  [path row blob]
  {:path path :mode (:mode row) :type (:type row) :blob-oid (:oid row)
   :byte-count (get-in blob [:parsed-result :byte-count])
   :raw-sha256 (get-in blob [:parsed-result :raw-sha256])})

(defn- cat-file-value-and-bytes!
  [repository oid]
  (let [result
        (run-bounded-process!
         {:argv (git-argv repository ["cat-file" "blob" oid])
          :directory repository :allowed-exit-codes #{0}
          :stdout-role :response :deadline-ms (:deadline-ms transport-limits)
          :id "SH07-B51-GIT-RELOAD"})
        bytes (:stdout-bytes result)
        stderr (:stderr-bytes result)]
    (when-not (zero? (alength ^bytes stderr))
      (fail! "SH07-B51-GIT-RELOAD" :git-stderr))
    {:bytes bytes
     :observation
     {:operation :cat-file-blob :argv (:argv result)
      :stdin-byte-count 0 :stdin-raw-sha256 empty-raw-sha256
      :exit-code 0 :stdout-byte-count (alength ^bytes bytes)
      :stdout-raw-sha256 (codec/R bytes)
      :stderr-byte-count 0 :stderr-raw-sha256 empty-raw-sha256
      :parsed-result {:oid oid :byte-count (alength ^bytes bytes)
                      :raw-sha256 (codec/R bytes)
                      :value ((:strict-read-wire transport-api)
                              :response bytes)}}}))

(defn load-existing-evidence-objects!
  "Load E9 from exact E/TE Git objects without accepting or resolving a final
  filesystem path.  The result is storage evidence only."
  [{:keys [repository-root content existing-evidence]}]
  (exact-keys! content #{:commit :tree} "SH07-B51-GIT-RELOAD" :content)
  (exact-keys! existing-evidence #{:commit :tree :parents}
               "SH07-B51-GIT-RELOAD" :existing-evidence)
  (doseq [[field value] [[:content-commit (:commit content)]
                         [:content-tree (:tree content)]
                         [:evidence-commit (:commit existing-evidence)]
                         [:evidence-tree (:tree existing-evidence)]]]
    (require-git-id! value field "SH07-B51-GIT-RELOAD"))
  (when-not (= [(:commit content)] (:parents existing-evidence))
    (fail! "SH07-B51-GIT-RELOAD" :existing-parent))
  (let [repository (canonical-directory! repository-root :repository-root
                                         "SH07-B51-GIT-RELOAD")
        c-show (show-operation! repository (:commit content))
        e-show (show-operation! repository (:commit existing-evidence))
        diff (diff-name-status-operation! repository
                                          (:commit content)
                                          (:commit existing-evidence))]
    (when-not
     (and (= content (select-keys (:parsed-result c-show) [:commit :tree]))
          (= existing-evidence (:parsed-result e-show))
          (= (set evidence-paths)
             (set (map :path (get-in diff [:parsed-result :entries]))))
          (= 7 (count (get-in diff [:parsed-result :entries]))))
      (fail! "SH07-B51-GIT-RELOAD" :existing-coordinate-or-delta))
    (let [objects
          (mapv
           (fn [path]
             (let [tree (ls-tree-operation!
                         repository (:commit existing-evidence) [path])
                   row (first (get-in tree [:parsed-result :rows]))]
               (when-not (and (:present row) (= "100644" (:mode row))
                              (= "blob" (:type row)) (git-id? (:oid row)))
                 (fail! "SH07-B51-GIT-RELOAD" :existing-tree-row
                        {:path path :row row}))
               (let [{:keys [bytes observation]}
                     (cat-file-value-and-bytes! repository (:oid row))]
                 {:path path :tree-observation tree
                  :blob-observation observation
                  :descriptor (existing-object-descriptor path row observation)
                  :value (get-in observation [:parsed-result :value])
                  :bytes bytes})))
           evidence-paths)
          values (mapv :value objects)
          receipt (peek values)]
      (validate-identity-receipt-envelope! content receipt)
      {:content content
       :existing-evidence existing-evidence
       :objects objects
       :values values
       :artifact-descriptors (mapv :descriptor objects)
       :observations
       (vec (concat [(assoc c-show :ordinal 0)
                     (assoc e-show :ordinal 1)
                     (assoc diff :ordinal 2)]
                    (mapcat
                     (fn [index object]
                       [(assoc (:tree-observation object)
                               :ordinal (+ 3 (* 2 index)))
                        (assoc (:blob-observation object)
                               :ordinal (+ 4 (* 2 index)))])
                     (range) objects)))
       :final-write-count 0
       :outcome :loaded-existing-objects
       :authority :none})))

(defn verify-existing-object-replay!
  "Compare fresh A/B/M payloads to E's first six W byte vectors and compare the
  freshly constructed normalized receipt ID to E's stored receipt ID.  No
  filesystem publication path is accepted by this API, so final writes are
  structurally impossible."
  [{:keys [fresh-payload-values fresh-identity-receipt-id] :as options}]
  (when-not (and (vector? fresh-payload-values)
                 (= 6 (count fresh-payload-values))
                 (sha256-id? fresh-identity-receipt-id))
    (fail! "SH07-B51-GIT-RELOAD" :fresh-replay-input))
  (let [loaded (load-existing-evidence-objects! options)
        objects (subvec (:objects loaded) 0 6)
        fresh-bytes (mapv codec/W fresh-payload-values)
        payloads-equal
        (every? true?
                (map #(Arrays/equals ^bytes %1 ^bytes (:bytes %2))
                     fresh-bytes objects))
        receipt-id (get-in loaded [:values 6 :receipt-id])]
    (when-not (and payloads-equal (= fresh-identity-receipt-id receipt-id))
      (fail! "SH07-B51-GIT-RELOAD" :existing-semantic-replay
             {:payloads-equal payloads-equal
              :receipt-id-equal (= fresh-identity-receipt-id receipt-id)}))
    (assoc loaded
           :payloads-equal true
           :receipt-id-equal true
           :final-write-count 0
           :outcome :verified-existing
           :authority :none)))

(defn- create-new-bytes!
  [^Path path bytes]
  (Files/write path bytes
               (into-array OpenOption
                           [StandardOpenOption/CREATE_NEW
                            StandardOpenOption/WRITE]))
  path)

(defn- current-directory-state
  [^Path directory]
  (if (and (Files/isDirectory directory no-link-options)
           (not (Files/isSymbolicLink directory)))
    {:file-key (file-key directory "SH07-B51-GIT-PUBLICATION-DELTA")
     :names (list-entry-names! directory
                               "SH07-B51-GIT-PUBLICATION-DELTA")}
    {:file-key nil :names nil}))

(defn- cleanup-unpublished!
  [^Path final-root reservation-key created]
  (let [marker (.resolve final-root identity-receipt-name)
        created-names (mapv :name created)
        state (current-directory-state final-root)
        removable?
        (and (not (Files/exists marker no-link-options))
             (= reservation-key (:file-key state))
             (= (set created-names) (set (:names state)))
             (= created-names (subvec evidence-names 0 (count created-names)))
             (every?
              (fn [{:keys [name] expected-file-key :file-key}]
                (let [path (.resolve final-root name)]
                  (and (Files/isRegularFile path no-link-options)
                       (not (Files/isSymbolicLink path))
                       (= expected-file-key
                          (file-key path
                                    "SH07-B51-GIT-PUBLICATION-DELTA")))))
              created))]
    (if-not removable?
      {:outcome :quarantined :foreign-entry true}
      (do
        (doseq [{:keys [name] expected-file-key :file-key} (reverse created)]
          (let [path (.resolve final-root name)
                current (current-directory-state final-root)]
            (when-not (and (= reservation-key (:file-key current))
                           (= expected-file-key
                              (file-key path
                                        "SH07-B51-GIT-PUBLICATION-DELTA")))
              (fail! "SH07-B51-GIT-PUBLICATION-DELTA"
                     :cleanup-race {:path (str path)}))
            (Files/delete path)))
        (when-not (= reservation-key
                     (:file-key (current-directory-state final-root)))
          (fail! "SH07-B51-GIT-PUBLICATION-DELTA" :cleanup-directory-race))
        (Files/delete final-root)
        {:outcome :removed :foreign-entry false}))))

(defn publish-new-evidence!
  "Publish exactly six validated staged payloads and one L-supplied identity
  receipt.  The final directory is reserved with createDirectory, all files use
  CREATE_NEW, and identity-receipt.edn is written last as the commit marker."
  [{:keys [source-worktree publication-worktree content staging-root
           payload-descriptors identity-receipt]}]
  (let [worktrees (validate-publication-worktrees!
                   {:source-worktree source-worktree
                    :publication-worktree publication-worktree
                    :content content})
        publication-root
        (Paths/get (get-in worktrees
                           [:publication-worktree :canonical-path])
                   (make-array String 0))
        stage-root (canonical-directory! staging-root :staging-root
                                         "SH07-B51-GIT-INPUT-BLOB")
        _ (when (or (.startsWith stage-root publication-root)
                    (.startsWith publication-root stage-root))
            (fail! "SH07-B51-GIT-PUBLICATION-DELTA"
                   :staging-inside-worktree))
        staged (validate-staged-payloads!
                {:staging-root stage-root
                 :payload-descriptors payload-descriptors})
        receipt (validate-identity-receipt-envelope! content identity-receipt)
        receipt-bytes (codec/W receipt)
        _ ((:strict-read-wire transport-api) :response receipt-bytes)
        final-root (.resolve publication-root evidence-relative-root)
        final-parent (.getParent final-root)
        _ (when-not (and (Files/isDirectory final-parent no-link-options)
                         (not (Files/isSymbolicLink final-parent)))
            (fail! "SH07-B51-GIT-PUBLICATION-DELTA" :final-parent
                   {:path (str final-parent)}))
        created (atom [])
        reservation-key (atom nil)]
    (try
      ;; This atomic reservation, not the earlier absence check, owns creation.
      (Files/createDirectory
       final-root (make-array java.nio.file.attribute.FileAttribute 0))
      (reset! reservation-key
              (file-key final-root "SH07-B51-GIT-PUBLICATION-DELTA"))
      (doseq [{:keys [name bytes]} staged]
        (let [target (.resolve final-root name)]
          (create-new-bytes! target bytes)
          (swap! created conj
                 {:name name
                  :file-key
                  (file-key target "SH07-B51-GIT-PUBLICATION-DELTA")})))
      ;; The identity receipt is the sole publication commit marker.
      (let [target (.resolve final-root identity-receipt-name)]
        (create-new-bytes! target receipt-bytes)
        (swap! created conj
               {:name identity-receipt-name
                :file-key
                (file-key target "SH07-B51-GIT-PUBLICATION-DELTA")}))
      {:publication
       {:mode :new
        :logical-root evidence-relative-root
        :source-worktree (:source-worktree worktrees)
        :publication-worktree (:publication-worktree worktrees)
        :existing-evidence nil
        :staging-root (str stage-root)
        :reservation-created true
        :reservation-file-key @reservation-key
        :files-created evidence-paths
        :identity-receipt-created-last true
        :final-write-count 7
        :foreign-entry false
        :outcome :staged-new}
       :payload-values (mapv :value staged)
       :identity-receipt receipt
       :authority :none}
      (catch Throwable error
        (let [cleanup
              (if @reservation-key
                (cleanup-unpublished! final-root @reservation-key @created)
                {:outcome :not-reserved :foreign-entry false})]
          (fail! "SH07-B51-GIT-PUBLICATION-DELTA"
                 (if (= :quarantined (:outcome cleanup))
                   :publication-quarantined
                   :publication-failed)
                 {:cause (.getName (class error))
                  :cleanup cleanup
                  :created (mapv :name @created)}))))))
