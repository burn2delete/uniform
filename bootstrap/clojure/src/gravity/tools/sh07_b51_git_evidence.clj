(ns gravity.tools.sh07-b51-git-evidence
  "Bounded Git-object and filesystem publication seam for SH-07 B5.1 V9.

  This launcher authenticates storage, transport, and publication mechanics.
  It deliberately returns `:authority :none`: the full V9 runtime contract
  discharge and receipt-normalization implementation must join this seam before
  an E9 candidate can claim evidence authority."
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [gravity.tools.sh07-b51-v9-codec :as codec])
  (:import [java.io Closeable PushbackReader StringReader]
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
   [11 :emitter nil "bootstrap/gravity/p15_s23/emitter.gravity"]
   [12 :sh06-resolution nil
    "bootstrap/gravity/src/gravity/resolution.gravity"]])

(def resolution-source-descriptor
  {:ordinal 12
   :kind :sh06-resolution
   :module-ordinal nil
   :path "bootstrap/gravity/src/gravity/resolution.gravity"
   :mode "100644"
   :blob-oid "16ef2b638298a8e934fcd0b39131072305fbcfd5"
   :byte-count 77209
   :raw-sha256
   "sha256:001ef59741f17b98b37ee5bdb21e698cb1e6e56ce76c5f5fdd5f1fc9a4caeb56"})

(def source-session-counters-e2
  {:acquire-count 13 :decode-count 13 :event-count 40 :finalize-count 1})

(def source-aggregate-counters-e2
  {:acquire-count 39 :decode-count 39 :event-count 120 :finalize-count 3})

(def source-only-paths
  ["bootstrap/gravity/p15_s23/compiler.gravity"
   "bootstrap/gravity/p15_s23/emitter.gravity"
   "bootstrap/gravity/src/gravity/resolution.gravity"
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
   "bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00873-sh06-resolution-dependency-edges.clj"
   "bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00879-sh06-resolution-artifact-verification-bounded.clj"
   "bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00880-sh06-resolution-artifact-verification.clj"
   "bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00881-sh06-resolution-finalize-candidate.clj"
   "bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00890-sh07-b13-module-assembly-manifest.clj"
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
   "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_sh06_external_binding_seam_test.clj"
   "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_source_closure_test.clj"])

(def architecture-report-path
  "docs/artifacts/phase-15/reports/sh07-b51-git-object-evidence-publication-architecture-v9.md")

(def provisional-report-path
  "docs/artifacts/phase-15/reports/sh07-b51-git-object-evidence-publication-provisional-evidence-v9.md")

(def a9-terminal-path
  "contracts/workstream-records/terminal/sh07-b51-git-object-evidence-publication-architecture-v9-461e2300fb893cbc2b60fb23e51b7242608a1aefd0bb15c8af4d72219a2b3ef1.json")

(defn- governance-path-spec [workstream-id]
  {:kind :content-addressed-terminal-record
   :directory "contracts/workstream-records/terminal"
   :workstream-id workstream-id
   :record-digest-domain :sha256
   :filename-rule :workstream-id-hyphen-record-sha256-dot-json})

(def a9-paths-e1
  [architecture-report-path
   provisional-report-path
   (governance-path-spec
    "sh07-b51-git-object-evidence-publication-architecture-v9")])

(def a9-only-paths-e1
  [architecture-report-path provisional-report-path a9-terminal-path])

;; Compatibility name for callers which only need the concrete PRE paths.  It
;; intentionally no longer denotes the superseded A9 lifecycle receipt.
(def a9-pre-paths a9-only-paths-e1)

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
    (when-not (and (= 79 (count paths)) (= 79 (count (distinct paths))))
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

;; This parser is deliberately local to the authenticated Git-object seam.
;; Adding a tooling JSON namespace to the checker classpath would enlarge the
;; source-authority closure.  Duplicate keys, malformed grammar, and trailing
;; input all reject before any governance field is consulted.
(defn- json-skip-whitespace! [^PushbackReader reader]
  (loop [value (.read reader)]
    (if (and (not= -1 value)
             (contains? #{\space \tab \newline \return} (char value)))
      (recur (.read reader))
      value)))

(defn- json-unread! [^PushbackReader reader value]
  (when-not (= -1 value) (.unread reader value)))

(defn- json-read-string! [^PushbackReader reader]
  (let [output (StringBuilder.)]
    (loop []
      (let [value (.read reader)]
        (cond
          (= -1 value) (fail! "SH07-B51-GIT-SNAPSHOT" :json-string)
          (= \" (char value)) (str output)
          (= \\ (char value))
          (let [escaped (.read reader)]
            (when (= -1 escaped)
              (fail! "SH07-B51-GIT-SNAPSHOT" :json-escape))
            (case (char escaped)
              \" (.append output \" )
              \\ (.append output \\)
              \/ (.append output \/)
              \b (.append output \backspace)
              \f (.append output \formfeed)
              \n (.append output \newline)
              \r (.append output \return)
              \t (.append output \tab)
              \u (let [values (repeatedly 4 #(.read reader))]
                   (when (some #(= -1 %) values)
                     (fail! "SH07-B51-GIT-SNAPSHOT" :json-unicode))
                   (let [digits (apply str (map char values))]
                     (when-not (re-matches #"[0-9A-Fa-f]{4}" digits)
                       (fail! "SH07-B51-GIT-SNAPSHOT" :json-unicode))
                     (.append output (char (Integer/parseInt digits 16)))))
              (fail! "SH07-B51-GIT-SNAPSHOT" :json-escape))
            (recur))
          (< value 0x20)
          (fail! "SH07-B51-GIT-SNAPSHOT" :json-control)
          :else (do (.append output (char value)) (recur)))))))

(declare json-read-value!)

(defn- json-read-literal!
  [^PushbackReader reader suffix value]
  (doseq [expected suffix]
    (let [actual (.read reader)]
      (when (or (= -1 actual) (not= expected (char actual)))
        (fail! "SH07-B51-GIT-SNAPSHOT" :json-literal))))
  value)

(defn- json-read-number! [^PushbackReader reader first-character]
  (let [token
        (loop [output (StringBuilder. (str first-character))]
          (let [value (.read reader)]
            (if (and (not= -1 value)
                     (re-matches #"[0-9eE+\-.]" (str (char value))))
              (recur (.append output (char value)))
              (do (json-unread! reader value) (str output)))))]
    (when-not
     (re-matches #"-?(?:0|[1-9][0-9]*)(?:\.[0-9]+)?(?:[eE][+-]?[0-9]+)?"
                 token)
      (fail! "SH07-B51-GIT-SNAPSHOT" :json-number))
    (try
      (if (re-find #"[.eE]" token) (bigdec token) (bigint token))
      (catch NumberFormatException _
        (fail! "SH07-B51-GIT-SNAPSHOT" :json-number)))))

(defn- json-read-array! [^PushbackReader reader]
  (let [first-value (json-skip-whitespace! reader)]
    (if (= (int \]) first-value)
      []
      (do
        (json-unread! reader first-value)
        (loop [values []]
          (let [value (json-read-value! reader)
                delimiter (json-skip-whitespace! reader)]
            (cond
              (= (int \,) delimiter) (recur (conj values value))
              (= (int \]) delimiter) (conj values value)
              :else (fail! "SH07-B51-GIT-SNAPSHOT" :json-array))))))))

(defn- json-read-object! [^PushbackReader reader]
  (let [first-key (json-skip-whitespace! reader)]
    (if (= (int \}) first-key)
      {}
      (do
        (json-unread! reader first-key)
        (loop [result {}]
          (when-not (= (int \") (json-skip-whitespace! reader))
            (fail! "SH07-B51-GIT-SNAPSHOT" :json-object-key))
          (let [key (json-read-string! reader)
                colon (json-skip-whitespace! reader)]
            (when (contains? result key)
              (fail! "SH07-B51-GIT-SNAPSHOT" :json-duplicate-key
                     {:key key}))
            (when-not (= (int \:) colon)
              (fail! "SH07-B51-GIT-SNAPSHOT" :json-object-colon))
            (let [updated (assoc result key (json-read-value! reader))
                  delimiter (json-skip-whitespace! reader)]
              (cond
                (= (int \,) delimiter) (recur updated)
                (= (int \}) delimiter) updated
                :else (fail! "SH07-B51-GIT-SNAPSHOT" :json-object)))))))))

(defn- json-read-value! [^PushbackReader reader]
  (let [value (json-skip-whitespace! reader)]
    (when (= -1 value) (fail! "SH07-B51-GIT-SNAPSHOT" :json-value))
    (let [character (char value)]
      (case character
        \{ (json-read-object! reader)
        \[ (json-read-array! reader)
        \" (json-read-string! reader)
        \t (json-read-literal! reader "rue" true)
        \f (json-read-literal! reader "alse" false)
        \n (json-read-literal! reader "ull" nil)
        (if (or (= character \-) (Character/isDigit character))
          (json-read-number! reader character)
          (fail! "SH07-B51-GIT-SNAPSHOT" :json-value))))))

(defn- read-strict-json! [text]
  (with-open [reader (PushbackReader. (StringReader. text))]
    (let [value (json-read-value! reader)]
      (when-not (= -1 (json-skip-whitespace! reader))
        (fail! "SH07-B51-GIT-SNAPSHOT" :json-trailing))
      value)))

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

(def ^:private git-object-proof-id "SH07-B51-GIT-OBJECT-PROOF")

(def ^:private content-object-proof-keys #{:oid :raw-bytes})

(def ^:private source-object-proof-keys
  #{:schema :path :root-tree :tree-chain :blob-bytes})

(def ^:private tree-chain-row-keys
  #{:tree-oid :tree-bytes :component :selected-mode :selected-oid})

(defn- unsigned-byte-vector?
  [value]
  (and (vector? value)
       (<= (count value) (:response-max-bytes transport-limits))
       (every? #(and (integer? %) (<= 0 % 255)) value)))

(defn- bytes->unsigned-vector
  [bytes]
  (mapv #(bit-and (int %) 0xff) bytes))

(defn- unsigned-vector->bytes!
  [value reason]
  (when-not (unsigned-byte-vector? value)
    (fail! git-object-proof-id reason))
  (byte-array (map unchecked-byte value)))

(defn- bytes->lowercase-hex
  [bytes]
  (apply str (map #(format "%02x" (bit-and (int %) 0xff)) bytes)))

(defn- git-object-id
  [kind bytes]
  (when-not (contains? #{"blob" "commit" "tree"} kind)
    (fail! git-object-proof-id :object-kind {:kind kind}))
  (let [digest (java.security.MessageDigest/getInstance "SHA-1")
        header (.getBytes (str kind " " (alength ^bytes bytes) "\u0000")
                          StandardCharsets/US_ASCII)]
    (.update digest header)
    (.update digest ^bytes bytes)
    (bytes->lowercase-hex (.digest digest))))

(defn- byte-line-end
  [bytes start]
  (loop [index start]
    (cond
      (= index (alength ^bytes bytes)) nil
      (= 10 (bit-and (int (aget ^bytes bytes index)) 0xff)) index
      :else (recur (inc index)))))

(defn- bytes-prefix-at?
  [bytes index prefix]
  (let [prefix-bytes (.getBytes ^String prefix StandardCharsets/US_ASCII)]
    (and (<= (+ index (alength prefix-bytes)) (alength ^bytes bytes))
         (loop [offset 0]
           (or (= offset (alength prefix-bytes))
               (and (= (aget ^bytes bytes (+ index offset))
                       (aget prefix-bytes offset))
                    (recur (inc offset))))))))

(defn- parse-commit-tree!
  [bytes]
  (let [first-end (byte-line-end bytes 0)]
    (when-not first-end
      (fail! git-object-proof-id :commit-tree-header))
    (let [first-line (String. ^bytes bytes 0 first-end
                              StandardCharsets/US_ASCII)
          match (re-matches #"tree ([0-9a-f]{40})" first-line)
          tree-header-count
          (loop [index 0 count 0]
            (if (>= index (alength ^bytes bytes))
              count
              (let [line-end (or (byte-line-end bytes index)
                                 (alength ^bytes bytes))
                    next-count (if (bytes-prefix-at? bytes index "tree ")
                                 (inc count) count)]
                (recur (if (= line-end (alength ^bytes bytes))
                         line-end (inc line-end))
                       next-count))))]
      (when-not (and match (= 1 tree-header-count))
        (fail! git-object-proof-id :commit-tree-header
               {:tree-header-count tree-header-count}))
      (second match))))

(defn- index-of-byte
  [bytes start target]
  (loop [index start]
    (cond
      (= index (alength ^bytes bytes)) nil
      (= target (bit-and (int (aget ^bytes bytes index)) 0xff)) index
      :else (recur (inc index)))))

(defn- slice-bytes
  [bytes start end]
  (java.util.Arrays/copyOfRange ^bytes bytes (int start) (int end)))

(defn- git-tree-name-compare
  [left right]
  (let [left-name ^bytes (:name-bytes left)
        right-name ^bytes (:name-bytes right)
        left-count (alength left-name)
        right-count (alength right-name)]
    (loop [index 0]
      (let [left-value (if (< index left-count)
                         (bit-and (int (aget left-name index)) 0xff)
                         (if (= "40000" (:mode left)) 47 0))
            right-value (if (< index right-count)
                          (bit-and (int (aget right-name index)) 0xff)
                          (if (= "40000" (:mode right)) 47 0))]
        (cond
          (not= left-value right-value) (compare left-value right-value)
          (and (>= index left-count) (>= index right-count)) 0
          :else (recur (inc index)))))))

(defn- parse-git-tree-bytes!
  [tree-bytes]
  (let [bytes (unsigned-vector->bytes! tree-bytes :tree-byte-vector)
        byte-count (alength ^bytes bytes)
        entries
        (loop [index 0 rows []]
          (if (= index byte-count)
            rows
            (let [space (index-of-byte bytes index 32)
                  _ (when-not (and space (< index space)
                                   (<= (- space index) 6))
                      (fail! git-object-proof-id :tree-mode-grammar))
                  mode-bytes (slice-bytes bytes index space)
                  mode (String. mode-bytes StandardCharsets/US_ASCII)
                  _ (when-not (contains? #{"40000" "100644" "100755"}
                                         mode)
                      (fail! git-object-proof-id
                             (if (contains? #{"120000" "160000"} mode)
                               :forbidden-tree-mode :tree-mode-grammar)
                             {:mode mode}))
                  nul (index-of-byte bytes (inc space) 0)
                  _ (when-not (and nul (< (inc space) nul)
                                   (<= (+ nul 21) byte-count))
                      (fail! git-object-proof-id :tree-entry-grammar))
                  name-bytes (slice-bytes bytes (inc space) nul)
                  name (decode-utf8! name-bytes git-object-proof-id
                                     :tree-name-utf8)
                  _ (when-not (and (not (contains? #{"." ".."} name))
                                   (not (str/includes? name "/")))
                      (fail! git-object-proof-id :tree-name {:name name}))
                  oid-bytes (slice-bytes bytes (inc nul) (+ nul 21))]
              (recur (+ nul 21)
                     (conj rows {:mode mode :name name
                                 :name-bytes name-bytes
                                 :oid (bytes->lowercase-hex oid-bytes)})))))]
    (when-not (and (= (count entries) (count (distinct (map :name entries))))
                   (every? neg?
                           (map git-tree-name-compare entries (rest entries))))
      (fail! git-object-proof-id :tree-entry-order))
    entries))

(defn validate-content-object-proof!
  "Purely validate a complete raw Git commit payload against immutable C/T."
  [content proof]
  (exact-keys! content #{:commit :tree} git-object-proof-id :content)
  (exact-keys! proof content-object-proof-keys
               git-object-proof-id :content-object-proof-keys)
  (require-git-id! (:commit content) :commit git-object-proof-id)
  (require-git-id! (:tree content) :tree git-object-proof-id)
  (require-git-id! (:oid proof) :oid git-object-proof-id)
  (let [bytes (unsigned-vector->bytes! (:raw-bytes proof)
                                       :content-object-byte-vector)
        recomputed (git-object-id "commit" bytes)
        parsed-tree (parse-commit-tree! bytes)]
    (when-not (and (= (:commit content) (:oid proof) recomputed)
                   (= (:tree content) parsed-tree))
      (fail! git-object-proof-id :content-object-coordinate
             {:expected content :proof-oid (:oid proof)
              :recomputed-oid recomputed :parsed-tree parsed-tree})))
  proof)

(defn validate-source-object-proof!
  "Purely validate a complete Git tree-inclusion chain and leaf blob bytes."
  [content descriptor proof]
  (exact-keys! content #{:commit :tree} git-object-proof-id :content)
  (exact-keys! proof source-object-proof-keys
               git-object-proof-id :source-object-proof-keys)
  (when-not (and (map? descriptor)
                 (safe-relative-path? (:path descriptor))
                 (= "100644" (:mode descriptor))
                 (git-id? (:blob-oid descriptor))
                 (integer? (:byte-count descriptor))
                 (not (neg? (:byte-count descriptor)))
                 (sha256-id? (:raw-sha256 descriptor)))
    (fail! git-object-proof-id :source-descriptor))
  (let [components (str/split (:path descriptor) #"/" -1)
        chain (:tree-chain proof)
        blob-bytes (unsigned-vector->bytes! (:blob-bytes proof)
                                            :blob-byte-vector)]
    (when-not (and (= :gravity/sh07-b51-git-tree-inclusion-proof-v1
                      (:schema proof))
                   (= (:path descriptor) (:path proof))
                   (= (:tree content) (:root-tree proof))
                   (git-id? (:root-tree proof))
                   (vector? chain)
                   (= (count components) (count chain)))
      (fail! git-object-proof-id :source-object-coordinate))
    (doseq [[ordinal component row] (map vector (range) components chain)]
      (exact-keys! row tree-chain-row-keys
                   git-object-proof-id :tree-chain-row-keys)
      (require-git-id! (:tree-oid row) :tree-oid git-object-proof-id)
      (require-git-id! (:selected-oid row) :selected-oid git-object-proof-id)
      (let [tree-bytes (unsigned-vector->bytes! (:tree-bytes row)
                                                :tree-byte-vector)
            entries (parse-git-tree-bytes! (:tree-bytes row))
            selected (filter #(= component (:name %)) entries)
            final? (= ordinal (dec (count components)))
            next-row (nth chain (inc ordinal) nil)]
        (when-not (and (= component (:component row))
                       (= (if (zero? ordinal) (:tree content)
                              (:selected-oid (nth chain (dec ordinal))))
                          (:tree-oid row))
                       (= (:tree-oid row) (git-object-id "tree" tree-bytes))
                       (= 1 (count selected))
                       (= [(:mode (first selected)) (:oid (first selected))]
                          [(:selected-mode row) (:selected-oid row)])
                       (if final?
                         (and (= "100644" (:selected-mode row))
                              (= (:blob-oid descriptor) (:selected-oid row)))
                         (and (= "40000" (:selected-mode row))
                              (= (:selected-oid row) (:tree-oid next-row)))))
          (fail! git-object-proof-id :tree-chain-coordinate
                 {:ordinal ordinal :component component}))))
    (when-not (and (= (:blob-oid descriptor)
                      (git-object-id "blob" blob-bytes))
                   (= (:byte-count descriptor) (alength ^bytes blob-bytes))
                   (= (:raw-sha256 descriptor) (codec/R blob-bytes)))
      (fail! git-object-proof-id :blob-coordinate)))
  proof)

(defn validate-governed-source-entry!
  "Purely validate one exact five-key E2 source-session entry."
  [content entry]
  (exact-keys! entry
               #{:descriptor :bytes :text :content-object-proof
                 :source-object-proof}
               git-object-proof-id :source-entry-keys)
  (let [descriptor (:descriptor entry)
        bytes (unsigned-vector->bytes! (:bytes entry) :source-entry-bytes)]
    (validate-content-object-proof! content (:content-object-proof entry))
    (validate-source-object-proof! content descriptor
                                   (:source-object-proof entry))
    (when-not (and (= (:bytes entry)
                      (get-in entry [:source-object-proof :blob-bytes]))
                   (= (:byte-count descriptor) (alength ^bytes bytes))
                   (= (:raw-sha256 descriptor) (codec/R bytes))
                   (= (:text entry)
                      (decode-utf8! bytes git-object-proof-id
                                    :source-entry-utf8)))
      (fail! git-object-proof-id :source-entry-coordinate)))
  entry)

(defn build-source-session-aggregate!
  "Join the independently observed A/B/M E2 session counters exactly."
  [participant-counters]
  (when-not (and (map? participant-counters)
                 (= #{:a :b :m} (set (keys participant-counters)))
                 (every? #(= source-session-counters-e2
                             (get participant-counters %))
                         [:a :b :m]))
    (fail! git-object-proof-id :source-session-counters
           {:participant-counters participant-counters}))
  (let [sessions
        (mapv (fn [participant]
                (assoc (get participant-counters participant)
                       :participant participant))
              [:a :b :m])
        aggregate
        (merge {:sessions sessions}
               (into {}
                     (map (fn [key]
                            [key (reduce + (map key sessions))])
                          [:acquire-count :decode-count
                           :event-count :finalize-count])))]
    (when-not (= source-aggregate-counters-e2
                 (dissoc aggregate :sessions))
      (fail! git-object-proof-id :source-session-aggregate))
    aggregate))

(defn- cat-file-object-bytes!
  [repository kind oid]
  (when-not (contains? #{"blob" "commit" "tree"} kind)
    (fail! git-object-proof-id :object-kind {:kind kind}))
  (require-git-id! oid :oid git-object-proof-id)
  (let [result
        (run-bounded-process!
         {:argv (git-argv repository ["cat-file" kind oid])
          :directory repository
          :allowed-exit-codes #{0}
          :stdout-role :response
          :deadline-ms (:deadline-ms transport-limits)
          :id git-object-proof-id})]
    (when-not (zero? (alength ^bytes (:stderr-bytes result)))
      (fail! git-object-proof-id :git-stderr
             {:kind kind :oid oid
              :stderr-raw-sha256 (codec/R (:stderr-bytes result))}))
    (:stdout-bytes result)))

(defn build-content-object-proof!
  "Acquire a complete raw commit payload and return its validated C proof."
  [{:keys [repository-root content]}]
  (let [repository (canonical-directory! repository-root :repository-root
                                         git-object-proof-id)
        raw (cat-file-object-bytes! repository "commit" (:commit content))
        proof {:oid (:commit content) :raw-bytes (bytes->unsigned-vector raw)}]
    (validate-content-object-proof! content proof)))

(defn build-source-object-proof!
  "Acquire and validate the complete tree chain and blob for DESCRIPTOR."
  [{:keys [repository-root content descriptor]}]
  (let [repository (canonical-directory! repository-root :repository-root
                                         git-object-proof-id)
        components (str/split (:path descriptor) #"/" -1)
        chain
        (loop [tree-oid (:tree content) remaining components rows []]
          (let [tree-raw (cat-file-object-bytes! repository "tree" tree-oid)
                tree-vector (bytes->unsigned-vector tree-raw)
                component (first remaining)
                matches (filter #(= component (:name %))
                                (parse-git-tree-bytes! tree-vector))]
            (when-not (= 1 (count matches))
              (fail! git-object-proof-id :tree-component
                     {:component component :tree-oid tree-oid}))
            (let [selected (first matches)
                  row {:tree-oid tree-oid :tree-bytes tree-vector
                       :component component :selected-mode (:mode selected)
                       :selected-oid (:oid selected)}]
              (if (next remaining)
                (do
                  (when-not (= "40000" (:mode selected))
                    (fail! git-object-proof-id :tree-component-mode
                           {:component component :mode (:mode selected)}))
                  (recur (:oid selected) (next remaining) (conj rows row)))
                (conj rows row)))))
        blob-raw (cat-file-object-bytes! repository "blob"
                                         (:selected-oid (peek chain)))
        proof {:schema :gravity/sh07-b51-git-tree-inclusion-proof-v1
               :path (:path descriptor)
               :root-tree (:tree content)
               :tree-chain chain
               :blob-bytes (bytes->unsigned-vector blob-raw)}]
    (validate-source-object-proof! content descriptor proof)))

(defn build-governed-source-entry!
  "Acquire one complete E2 source entry from immutable Git objects only."
  [{:keys [repository-root content descriptor]}]
  (let [content-proof
        (build-content-object-proof!
         {:repository-root repository-root :content content})
        source-proof
        (build-source-object-proof!
         {:repository-root repository-root :content content
          :descriptor descriptor})
        bytes (:blob-bytes source-proof)
        raw (unsigned-vector->bytes! bytes :source-entry-bytes)
        entry {:descriptor descriptor
               :bytes bytes
               :text (decode-utf8! raw git-object-proof-id
                                   :source-entry-utf8)
               :content-object-proof content-proof
               :source-object-proof source-proof}]
    (validate-governed-source-entry! content entry)))

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

(def ^:private a9-fixed-descriptor-fields
  {architecture-report-path
   {:blob-oid "1eef3f25b18b941deed09bdfb178573f08f7928e"
    :byte-count 393221
    :raw-sha256
    "sha256:34ee9b302f78d88743c3c8ba91cd9bd47c31bc1b4d814afc9e0a892f70e3b0dd"}
   provisional-report-path
   {:blob-oid "fc36dc37d4e45adc7255eb531ae8d312d9eb51da"
    :byte-count 4977
    :raw-sha256
    "sha256:7401846ab6892cc32d4ba4f0d8d0c87be531575d75abc88852f6516cd769ae4d"}
   a9-terminal-path
   {:blob-oid "ddae151f339068955ff9f7df75c975e697962bd6"
    :byte-count 13404
    :raw-sha256
    "sha256:461e2300fb893cbc2b60fb23e51b7242608a1aefd0bb15c8af4d72219a2b3ef1"}})

(def ^:private a9-workstream-id
  "sh07-b51-git-object-evidence-publication-architecture-v9")

(def ^:private a9-invariant-family
  "architecture/self-hosting-sh07-b51-pin-free-publication-v1")

(def ^:private a9-dependencies
  ["sh07-b51-vector-destructuring-carrier-collection-bound-architecture-v18-attempt-32"
   "sh07-b51-first-eval-nested-validation-architecture-v3"
   "sh07-b51-binding-replay-index-architecture-v1"
   "sh07-b51-binding-replay-index-implementation-review-erratum-v1"
   "c2-pass-cache-capacity-withholding-v1"])

(def ^:private a9-record-keys
  #{"architecture_decision" "author" "base_commit" "candidate_commit"
    "clean_worktree" "dependencies" "disposition" "evidence"
    "governing_contracts" "history" "id" "invariant_family"
    "no_overclaim_authority" "owned_paths" "owner"
    "residual_host_boundaries" "reviews" "state" "title"})

(def ^:private governance-transitions
  {"draft" #{"frozen" "held" "rejected" "superseded" "abandoned"}
   "frozen" #{"draft" "review-pending" "held" "rejected"
              "superseded" "abandoned"}
   "review-pending" #{"draft" "accepted" "held" "rejected"
                      "superseded" "abandoned"}
   "accepted" #{"integration-eligible" "held" "rejected"
                 "superseded" "abandoned"}
   "integration-eligible" #{"integrated" "held" "rejected"
                            "superseded" "abandoned"}
   "held" #{"draft" "frozen" "rejected" "superseded" "abandoned"}
   "integrated" #{} "rejected" #{} "superseded" #{} "abandoned" #{}})

(defn- exact-json-keys? [value expected]
  (and (map? value) (= expected (set (keys value)))))

(defn- a9-record-shape-valid? [record]
  (and
   (exact-json-keys? record a9-record-keys)
   (every? string?
           (map #(get record %)
                ["architecture_decision" "author" "base_commit"
                 "candidate_commit" "disposition" "id" "invariant_family"
                 "owner" "state" "title"]))
   (boolean? (get record "clean_worktree"))
   (every? #(and (vector? (get record %))
                 (every? string? (get record %)))
           ["dependencies" "governing_contracts" "owned_paths"
            "residual_host_boundaries"])
   (let [evidence (get record "evidence")]
     (and (exact-json-keys?
           evidence
           #{"accepted_fixtures" "rejected_fixtures" "stable_diagnostics"
             "validation_commands"})
          (every? #(and (vector? (get evidence %))
                        (every? string? (get evidence %)))
                  ["accepted_fixtures" "rejected_fixtures"
                   "stable_diagnostics"])
          (vector? (get evidence "validation_commands"))
          (every?
           #(and (exact-json-keys? % #{"command" "exit_code" "result"})
                 (string? (get % "command"))
                 (integer? (get % "exit_code"))
                 (string? (get % "result")))
           (get evidence "validation_commands"))))
   (let [authority (get record "no_overclaim_authority")]
     (and (exact-json-keys?
           authority
           #{"integration_only" "release" "seed_retirement" "self_hosting"})
          (every? boolean? (vals authority))))
   (and (vector? (get record "reviews"))
        (every?
         #(and (exact-json-keys? % #{"kind" "result" "reviewer"})
               (every? string? (vals %)))
         (get record "reviews")))
   (and (vector? (get record "history"))
        (seq (get record "history"))
        (every?
         #(and (exact-json-keys? % #{"actor" "at" "reason" "state"})
               (every? string? (vals %)))
         (get record "history")))))

(defn- history-transitions-valid? [history]
  (and (= "draft" (get (first history) "state"))
       (every?
        (fn [[left right]]
          (contains? (get governance-transitions (get left "state") #{})
                     (get right "state")))
        (partition 2 1 history))))

(defn- a9-artifact-descriptors
  [rows-by-path bytes-by-path]
  (mapv
   (fn [path]
     (let [row (get rows-by-path path)
           bytes (get bytes-by-path path)
           descriptor
           {:path path :mode "100644" :type "blob" :blob-oid (:oid row)
            :byte-count (alength ^bytes bytes) :raw-sha256 (codec/R bytes)}]
       (when-not (= (get a9-fixed-descriptor-fields path)
                    (select-keys descriptor
                                 [:blob-oid :byte-count :raw-sha256]))
         (fail! "SH07-B51-GIT-SNAPSHOT" :a9-fixed-descriptor
                {:path path :descriptor descriptor}))
       descriptor))
   a9-only-paths-e1))

(defn- terminal-manifest-entry!
  [ledger record]
  (let [matches (filterv #(= a9-workstream-id (get % "id"))
                         (get ledger "records"))]
    (when-not (= 1 (count matches))
      (fail! "SH07-B51-GIT-SNAPSHOT" :a9-terminal-manifest-census))
    (first matches)))

(defn- build-a9-stage-result-e1
  [content rows-by-path bytes-by-path decoded-static]
  (let [descriptors (a9-artifact-descriptors rows-by-path bytes-by-path)
        terminal-descriptor (nth descriptors 2)
        record (read-strict-json! (get decoded-static a9-terminal-path))
        ledger (read-strict-json!
                (get decoded-static "contracts/workstream-ledger.json"))
        manifest (terminal-manifest-entry! ledger record)
        owned [architecture-report-path provisional-report-path]
        history (get record "history")
        expected-manifest
        {"dependencies" a9-dependencies
         "id" a9-workstream-id
         "invariant_family" a9-invariant-family
         "ordinal" 161
         "path" a9-terminal-path
         "sha256" (subs (:raw-sha256 terminal-descriptor) 7)
         "state" "integrated"
         "terminal" true}
        preceding-checks
        {:canonical-json true
         :record-shape-valid (a9-record-shape-valid? record)
         :filename-sha256-match
         (= a9-terminal-path
            (str "contracts/workstream-records/terminal/" a9-workstream-id
                 "-" (subs (:raw-sha256 terminal-descriptor) 7) ".json"))
         :manifest-entry-match (= expected-manifest manifest)
         :workstream-id-match (= a9-workstream-id (get record "id"))
         :invariant-family-match
         (= a9-invariant-family (get record "invariant_family"))
         :architecture-decision-path-match
         (= architecture-report-path (get record "architecture_decision"))
         :owned-paths-match (= owned (get record "owned_paths"))
         :accepted-fixtures-equal-owned-paths
         (= (get record "owned_paths")
            (get-in record ["evidence" "accepted_fixtures"]))
         :base-commit-id-syntax-valid (git-id? (get record "base_commit"))
         :candidate-commit-id-syntax-valid
         (git-id? (get record "candidate_commit"))
         :candidate-differs-from-base
         (not= (get record "candidate_commit") (get record "base_commit"))
         :clean-worktree-recorded (true? (get record "clean_worktree"))
         :independent-accepted-review-present
         (boolean
          (some #(and (= "independent" (get % "kind"))
                      (= "accepted" (get % "result"))
                      (not= (get record "author") (get % "reviewer")))
                (get record "reviews")))
         :history-transitions-valid
         (and (a9-record-shape-valid? record)
              (history-transitions-valid? history))
         :history-final-state-match
         (= (get record "state") (get (last history) "state"))
         :record-state-integrated (= "integrated" (get record "state"))
         :authority-ceiling-match
         (= {"integration_only" true "release" false
             "seed_retirement" false "self_hosting" false}
            (get record "no_overclaim_authority"))}
        checks (assoc preceding-checks :all-checks-pass
                      (every? true? (vals preceding-checks)))]
    (when-not (:all-checks-pass checks)
      (fail! "SH07-B51-GIT-SNAPSHOT" :a9-stage-result-checks
             {:failed-checks
              (->> preceding-checks
                   (keep (fn [[key value]] (when-not (true? value) key)))
                   vec)}))
    {:schema :gravity/sh07-b51-a9-stage-result-e1
     :stage :a9
     :content content
     :a9-artifact-descriptors descriptors
     :terminal-record
     {:coordinate (assoc terminal-descriptor
                         :commit (:commit content) :tree (:tree content))
      :manifest-entry manifest
      :record record}
     :checks checks
     :result :terminal-record-integrated
     :historical-claims
     {:candidate-bound-review false
      :candidate-tree false
      :integration-commit false
      :integration-tree false
      :candidate-integration-ancestry false
      :report-preservation-across-integration false
      :supporting-report-preservation-across-integration false
      :ownership-reservation false
      :ownership-handoff false
      :reservation-release false
      :worktree-removal false
      :branch-removal false
      :finally-entered false}
     :nonclaims nonclaims}))

(def ^:private pre-mask-specs
  [{:path
    "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker.clj"
    :load-ordinal 808 :form-ordinal 13
    :owner 'gravity.bootstrap/p15-s23-sh07-b51-expected-m3-id
    :rhs-kind :digest-pin}
   {:path
    "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker.clj"
    :load-ordinal 808 :form-ordinal 14
    :owner 'gravity.bootstrap/p15-s23-sh07-b51-expected-certificate-a-id
    :rhs-kind :digest-pin}
   {:path
    "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker.clj"
    :load-ordinal 808 :form-ordinal 15
    :owner 'gravity.bootstrap/p15-s23-sh07-b51-expected-certificate-b-id
    :rhs-kind :digest-pin}
   {:path
    "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker.clj"
    :load-ordinal 808 :form-ordinal 16
    :owner 'gravity.bootstrap/p15-s23-sh07-b51-expected-semantic-root
    :rhs-kind :digest-pin}
   {:path
    "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00947-p15-s23-sh07-b51-linked-carrier-v4.clj"
    :load-ordinal 812 :form-ordinal 59
    :owner
    'gravity.bootstrap/p15-s23-sh07-b51-authoritative-linked-carrier-value-v4
    :rhs-kind :quoted-carrier-v4}])

(def ^:private provisional-rhs-bytes
  (.getBytes ":gravity/provisional-no-authority" StandardCharsets/UTF_8))

(def ^:private masked-rhs-bytes
  (.getBytes ":gravity/sh07-b51-masked-rhs-v1" StandardCharsets/UTF_8))

;; The PRE and runtime masks are intentionally separate schema families.  PRE
;; is the four-field C/T-only projection authenticated by Q; runtime carries
;; the complete dual-image proof after the four digest pins and quoted carrier
;; have been obtained from fresh execution.  Keep these key sets public so
;; later phase validators can reuse the closed schemas without reconstructing
;; them from prose.
(def pre-masked-root-keys
  #{:coordinates :preimage-spans :preimage-files :preimage-root})

(def runtime-masked-root-keys
  #{:coordinates :preimage-spans :postimage-spans :preimage-files
    :postimage-files :preimage-root :postimage-root :equal})

(def ^:private mask-coordinate-keys
  #{:path :load-ordinal :form-ordinal :owner :rhs-kind
    :left-delimiter :right-delimiter :preceding-byte :following-byte
    :masked-rhs-byte-count})

(def ^:private mask-span-keys
  #{:coordinate-ordinal :form-start-byte :rhs-start-byte :rhs-end-byte
    :form-end-byte :rhs-byte-count :rhs-raw-sha256})

(def ^:private mask-file-keys
  #{:path :masked-byte-count :masked-raw-sha256})

(def runtime-pin-map-keys
  #{:expected-m3-id :expected-certificate-a-id
    :expected-certificate-b-id :expected-semantic-root})

(def ^:private runtime-pin-order
  [:expected-m3-id :expected-certificate-a-id
   :expected-certificate-b-id :expected-semantic-root])

(def ^:private masked-source-root-domain
  :gravity/sh07-b51-masked-source-root-v1)

(defn- byte-match-at? [^bytes haystack ^bytes needle offset]
  (and (<= (+ offset (alength needle)) (alength haystack))
       (loop [index 0]
         (or (= index (alength needle))
             (and (= (aget haystack (+ offset index)) (aget needle index))
                  (recur (inc index)))))))

(defn- byte-match-offsets [^bytes haystack ^bytes needle]
  (loop [offset 0 result []]
    (if (> (+ offset (alength needle)) (alength haystack))
      result
      (recur (inc offset)
             (if (byte-match-at? haystack needle offset)
               (conj result offset)
               result)))))

(defn- source-form-at! [text form-ordinal]
  (with-open [reader (clojure.lang.LineNumberingPushbackReader.
                      (StringReader. text))]
    (binding [*read-eval* false *data-readers* {}
              *default-data-reader-fn*
              (fn [tag _]
                (fail! "SH07-B51-GIT-SNAPSHOT" :source-reader-tag
                       {:tag tag}))]
      (loop [ordinal 0]
        (let [form (read {:eof ::eof :read-cond :allow} reader)]
          (cond
            (= ::eof form)
            (fail! "SH07-B51-GIT-SNAPSHOT" :masked-form-ordinal
                   {:form-ordinal form-ordinal})
            (= ordinal form-ordinal) form
            :else (recur (inc ordinal))))))))

(defn- replace-byte-span
  [^bytes source start end ^bytes replacement]
  (let [result (byte-array (+ start (alength replacement)
                              (- (alength source) end)))]
    (System/arraycopy source 0 result 0 start)
    (System/arraycopy replacement 0 result start (alength replacement))
    (System/arraycopy source end result (+ start (alength replacement))
                      (- (alength source) end))
    result))

(defn- expected-mask-coordinates []
  (mapv
   (fn [{:keys [path load-ordinal form-ordinal owner rhs-kind]}]
     {:path path
      :load-ordinal load-ordinal
      :form-ordinal form-ordinal
      :owner owner
      :rhs-kind rhs-kind
      :left-delimiter (str "(def ^:private " (name owner) "\n  ")
      :right-delimiter ")"
      :preceding-byte 10
      :following-byte 10
      :masked-rhs-byte-count (alength masked-rhs-bytes)})
   pre-mask-specs))

(defn- mask-form-bytes
  [left rhs]
  (let [left-bytes (.getBytes ^String left StandardCharsets/UTF_8)
        result (byte-array (+ 1 (alength left-bytes) (alength ^bytes rhs) 2))
        rhs-start (inc (alength left-bytes))]
    (aset-byte result 0 (byte 10))
    (System/arraycopy left-bytes 0 result 1 (alength left-bytes))
    (System/arraycopy rhs 0 result rhs-start (alength ^bytes rhs))
    (aset-byte result (+ rhs-start (alength ^bytes rhs))
               (byte (int \))))
    (aset-byte result (dec (alength result)) (byte 10))
    result))

(defn- mask-span-for-rhs!
  [^bytes source coordinate-ordinal coordinate rhs]
  (let [full (mask-form-bytes (:left-delimiter coordinate) rhs)
        offsets (byte-match-offsets source full)]
    (when-not (= 1 (count offsets))
      (fail! "SH07-B51-MASKED-ROOT" :masked-occurrence-census
             {:coordinate-ordinal coordinate-ordinal
              :count (count offsets)}))
    (let [form-start (inc (first offsets))
          left-byte-count
          (alength (.getBytes ^String (:left-delimiter coordinate)
                              StandardCharsets/UTF_8))
          rhs-start (+ form-start left-byte-count)
          rhs-end (+ rhs-start (alength ^bytes rhs))
          form-end (inc rhs-end)]
      {:coordinate-ordinal coordinate-ordinal
       :form-start-byte form-start
       :rhs-start-byte rhs-start
       :rhs-end-byte rhs-end
       :form-end-byte form-end
       :rhs-byte-count (alength ^bytes rhs)
       :rhs-raw-sha256 (codec/R rhs)})))

(defn- validate-mask-span!
  [span coordinate-ordinal expected-rhs id reason]
  (exact-keys! span mask-span-keys id reason)
  (let [{:keys [form-start-byte rhs-start-byte rhs-end-byte form-end-byte
                rhs-byte-count rhs-raw-sha256]} span]
    (when-not
     (and (= coordinate-ordinal (:coordinate-ordinal span))
          (every? #(and (integer? %) (not (neg? %)))
                  [form-start-byte rhs-start-byte rhs-end-byte form-end-byte
                   rhs-byte-count])
          (<= form-start-byte rhs-start-byte)
          (< rhs-start-byte rhs-end-byte)
          (<= rhs-end-byte form-end-byte)
          (= (alength ^bytes expected-rhs) rhs-byte-count)
          (= (codec/R expected-rhs) rhs-raw-sha256))
      (fail! id reason
             {:coordinate-ordinal coordinate-ordinal :span span}))
    span))

(defn- validate-mask-coordinates!
  [coordinates id reason]
  (when-not (and (vector? coordinates) (= 5 (count coordinates)))
    (fail! id reason {:coordinates coordinates}))
  (doseq [[ordinal coordinate] (map-indexed vector coordinates)]
    (exact-keys! coordinate mask-coordinate-keys id reason)
    (when-not (= coordinate (nth (expected-mask-coordinates) ordinal))
      (fail! id reason {:coordinate-ordinal ordinal
                        :coordinate coordinate})))
  coordinates)

(defn- validate-mask-files!
  [files coordinates id reason]
  (when-not (and (vector? files) (= 2 (count files)))
    (fail! id reason {:files files}))
  (let [expected-paths (vec (distinct (map :path coordinates)))]
    (doseq [[ordinal [expected-path descriptor]]
            (map-indexed vector (map vector expected-paths files))]
      (exact-keys! descriptor mask-file-keys id reason)
      (when-not
       (and (= expected-path (:path descriptor))
            (integer? (:masked-byte-count descriptor))
            (not (neg? (:masked-byte-count descriptor)))
            (sha256-id? (:masked-raw-sha256 descriptor)))
        (fail! id reason {:file-ordinal ordinal :descriptor descriptor}))))
  files)

(defn- masked-source-root
  [coordinates files]
  (codec/H [masked-source-root-domain
            {:coordinates coordinates :files files}]))

(defn validate-pre-masked-root!
  "Validate the exact acyclic four-field PRE-MASKED-ROOT schema.

  This validator checks the closed coordinate/span/file projections and
  recomputes PREIMAGE-ROOT.  It does not admit runtime/postimage fields."
  [root]
  (exact-keys! root pre-masked-root-keys
               "SH07-B51-MASKED-ROOT" :pre-masked-root-keys)
  (validate-mask-coordinates! (:coordinates root)
                              "SH07-B51-MASKED-ROOT" :pre-masked-root-coordinates)
  (let [coordinates (:coordinates root)
        pre-spans (:preimage-spans root)
        pre-files (:preimage-files root)]
    (when-not (and (vector? pre-spans) (= 5 (count pre-spans)))
      (fail! "SH07-B51-MASKED-ROOT" :pre-masked-root-spans
             {:spans pre-spans}))
    (doseq [[ordinal span] (map-indexed vector pre-spans)]
      (validate-mask-span! span ordinal provisional-rhs-bytes
                           "SH07-B51-MASKED-ROOT" :pre-masked-root-span))
    (validate-mask-files! pre-files coordinates
                          "SH07-B51-MASKED-ROOT" :pre-masked-root-files)
    (when-not (and (sha256-id? (:preimage-root root))
                   (= (:preimage-root root)
                      (masked-source-root coordinates pre-files)))
      (fail! "SH07-B51-MASKED-ROOT" :pre-masked-root-hash
             {:root (:preimage-root root)})))
  root)

(defn validate-runtime-masked-root!
  "Validate the complete eight-field RUNTIME-MASKED-ROOT.

  With PRE supplied, the preimage projection is also compared byte-for-byte
  with Q's authenticated PRE-MASKED-ROOT.  Runtime roots must carry a true
  independently recomputed pre/post equality bit; a PRE-only or legacy
  `:masked-root` map therefore cannot pass this validator."
  ([root]
   (validate-runtime-masked-root! root nil))
  ([root pre-masked-root]
   (exact-keys! root runtime-masked-root-keys
                "SH07-B51-MASKED-ROOT" :runtime-masked-root-keys)
   (validate-mask-coordinates! (:coordinates root)
                               "SH07-B51-MASKED-ROOT"
                               :runtime-masked-root-coordinates)
   (let [projection (select-keys root pre-masked-root-keys)
         coordinates (:coordinates root)
         post-spans (:postimage-spans root)
         post-files (:postimage-files root)]
     (validate-pre-masked-root! projection)
     (when (some? pre-masked-root)
       (validate-pre-masked-root! pre-masked-root)
       (when-not (Arrays/equals ^bytes (codec/W projection)
                                ^bytes (codec/W pre-masked-root))
         (fail! "SH07-B51-MASKED-ROOT" :pre-projection-mismatch)))
     (when-not (and (vector? post-spans) (= 5 (count post-spans)))
       (fail! "SH07-B51-MASKED-ROOT" :runtime-masked-root-spans
              {:spans post-spans}))
     (doseq [[ordinal span] (map-indexed vector post-spans)]
       ;; Post RHS bytes are authenticated by the runtime caller.  The root
       ;; validator checks their observational shape and raw-byte identity;
       ;; the constructor below additionally checks the exact four pins and
       ;; quote form before producing these spans.
       (exact-keys! span mask-span-keys
                    "SH07-B51-MASKED-ROOT" :runtime-masked-root-span)
       (when-not
        (and (= ordinal (:coordinate-ordinal span))
             (every? #(and (integer? %) (not (neg? %)))
                     (map span [:form-start-byte :rhs-start-byte
                                :rhs-end-byte :form-end-byte :rhs-byte-count]))
             (<= (:form-start-byte span) (:rhs-start-byte span))
             (< (:rhs-start-byte span) (:rhs-end-byte span))
             (<= (:rhs-end-byte span) (:form-end-byte span))
             (sha256-id? (:rhs-raw-sha256 span)))
         (fail! "SH07-B51-MASKED-ROOT" :runtime-masked-root-span
                {:coordinate-ordinal ordinal :span span})))
     (validate-mask-files! post-files coordinates
                           "SH07-B51-MASKED-ROOT"
                           :runtime-masked-root-files)
     (when-not (and (sha256-id? (:postimage-root root))
                    (= (:postimage-root root)
                       (masked-source-root coordinates post-files)))
       (fail! "SH07-B51-MASKED-ROOT" :runtime-masked-root-hash
              {:root (:postimage-root root)}))
     (when-not (and (= true (:equal root))
                    (= (:preimage-root root) (:postimage-root root)))
       (fail! "SH07-B51-MASKED-ROOT" :runtime-masked-root-equality
              {:preimage-root (:preimage-root root)
               :postimage-root (:postimage-root root)
               :equal (:equal root)})))
   root))

(defn- validate-runtime-pin-map!
  [pin-map]
  (exact-keys! pin-map runtime-pin-map-keys
               "SH07-B51-MASKED-ROOT" :runtime-pin-map-keys)
  (when-not (every? sha256-id? (map pin-map runtime-pin-order))
    (fail! "SH07-B51-MASKED-ROOT" :runtime-pin-map-values
           {:pin-map pin-map}))
  pin-map)

(defn- carrier-value
  [carrier]
  (if (and (seq? carrier) (= 2 (count carrier))
           (= 'quote (first carrier)))
    (second carrier)
    carrier))

(defn- quoted-carrier-bytes!
  [carrier]
  (let [value (carrier-value carrier)]
    (when-not (map? value)
      (fail! "SH07-B51-MASKED-ROOT" :quoted-carrier-shape
             {:carrier carrier}))
    (let [wire (codec/W (list 'quote value))
          length (alength ^bytes wire)]
      (when-not (and (pos? length) (= 10 (aget wire (dec length))))
        (fail! "SH07-B51-MASKED-ROOT" :quoted-carrier-wire))
      (Arrays/copyOf ^bytes wire (dec length)))))

(defn- validate-mask-source-input!
  [bytes-by-path decoded-static]
  (when-not (and (map? bytes-by-path) (map? decoded-static))
    (fail! "SH07-B51-MASKED-ROOT" :source-input-shape))
  (doseq [{:keys [path]} pre-mask-specs]
    (let [bytes (get bytes-by-path path)
          text (get decoded-static path)]
      (when-not (bytes? bytes)
        (fail! "SH07-B51-MASKED-ROOT" :source-bytes
               {:path path}))
      (when-not (string? text)
        (fail! "SH07-B51-MASKED-ROOT" :decoded-source
               {:path path}))
      (when-not (= text (decode-utf8! bytes "SH07-B51-MASKED-ROOT"
                                       :source-utf8))
        (fail! "SH07-B51-MASKED-ROOT" :decoded-source-mismatch
               {:path path}))))
  [bytes-by-path decoded-static])

(defn- replace-runtime-rhs!
  [bytes-by-path coordinates pre-spans rhs-bytes]
  (let [ordinals
        (sort-by
         (fn [ordinal]
           [(:path (nth coordinates ordinal))
            (:rhs-start-byte (nth pre-spans ordinal))])
         #(compare %2 %1)
         (range 5))]
    (reduce
     (fn [sources ordinal]
       (let [coordinate (nth coordinates ordinal)
             span (nth pre-spans ordinal)
             path (:path coordinate)
             source (get sources path)]
         (when-not (byte-match-at? source provisional-rhs-bytes
                                    (:rhs-start-byte span))
           (fail! "SH07-B51-MASKED-ROOT" :runtime-preimage-rhs
                  {:coordinate-ordinal ordinal :path path}))
         (assoc sources path
                (replace-byte-span source (:rhs-start-byte span)
                                   (:rhs-end-byte span)
                                   (nth rhs-bytes ordinal)))))
     bytes-by-path ordinals)))

(defn- build-normalized-mask-files
  [bytes-by-path coordinates spans]
  (mapv
   (fn [path]
     (let [path-spans
           (sort-by :rhs-start-byte >
                    (keep-indexed
                     (fn [ordinal span]
                       (when (= path (:path (nth coordinates ordinal))) span))
                     spans))
           masked
           (reduce
            (fn [bytes {:keys [rhs-start-byte rhs-end-byte]}]
              (replace-byte-span bytes rhs-start-byte rhs-end-byte
                                 masked-rhs-bytes))
            (get bytes-by-path path) path-spans)]
       {:path path :masked-byte-count (alength ^bytes masked)
        :masked-raw-sha256 (codec/R masked)}))
   (vec (distinct (map :path coordinates)))))

(declare build-pre-masked-root)

(defn build-runtime-masked-root
  "Construct the exact runtime dual-image mask from authenticated C/T bytes.

  Positional arguments are `bytes-by-path`, `decoded-static`, Q's
  `pre-masked-root`, the exact four-entry `pin-map`, and the raw or already
  quoted carrier value.  A one-map arity accepts the same values under those
  five closed keys.  The constructor independently rebuilds PRE, applies all
  five post RHS replacements in descending byte order, computes post spans and
  normalized files, and only returns when the pre/post roots and Q projection
  agree byte-for-byte."
  ([bytes-by-path decoded-static pre-masked-root pin-map quoted-carrier]
   (validate-mask-source-input! bytes-by-path decoded-static)
   (validate-pre-masked-root! pre-masked-root)
   (validate-runtime-pin-map! pin-map)
   (let [pre-recomputed (build-pre-masked-root bytes-by-path decoded-static)]
     (when-not (Arrays/equals ^bytes (codec/W pre-recomputed)
                              ^bytes (codec/W pre-masked-root))
       (fail! "SH07-B51-MASKED-ROOT" :pre-masked-root-mismatch))
     (let [coordinates (:coordinates pre-masked-root)
           pre-spans (:preimage-spans pre-masked-root)
           rhs-bytes
           (conj
            (mapv (fn [pin-key]
                    (.getBytes (pr-str (get pin-map pin-key))
                               StandardCharsets/UTF_8))
                  runtime-pin-order)
            (quoted-carrier-bytes! quoted-carrier))
           post-bytes
           (replace-runtime-rhs! bytes-by-path coordinates pre-spans rhs-bytes)
           post-spans
           (mapv
            (fn [ordinal]
              (let [coordinate (nth coordinates ordinal)
                    source (get post-bytes (:path coordinate))]
                (mask-span-for-rhs! source ordinal coordinate
                                    (nth rhs-bytes ordinal))))
            (range 5))
           post-files
           (build-normalized-mask-files post-bytes coordinates post-spans)
           post-root (masked-source-root coordinates post-files)
           runtime-root
           {:coordinates coordinates
            :preimage-spans (:preimage-spans pre-masked-root)
            :postimage-spans post-spans
            :preimage-files (:preimage-files pre-masked-root)
            :postimage-files post-files
            :preimage-root (:preimage-root pre-masked-root)
            :postimage-root post-root
            :equal (= (:preimage-root pre-masked-root) post-root)}]
       (validate-runtime-masked-root! runtime-root pre-masked-root)))
   )
  ([input]
   (exact-keys! input
                #{:bytes-by-path :decoded-static :pre-masked-root :pin-map
                  :quoted-carrier}
                "SH07-B51-MASKED-ROOT" :runtime-mask-input-keys)
   (build-runtime-masked-root
    (:bytes-by-path input) (:decoded-static input)
    (:pre-masked-root input) (:pin-map input) (:quoted-carrier input))))

(defn build-runtime-masked-root!
  "Compatibility bang-form for callers that treat the constructor as an
  admission operation.  It has the same exact arities and return value."
  [& arguments]
  (apply build-runtime-masked-root arguments))

(defn- build-pre-masked-root
  [bytes-by-path decoded-static]
  (let [coordinates-and-spans
        (mapv
         (fn [coordinate-ordinal spec]
           (let [{:keys [path form-ordinal owner]} spec
                 text (get decoded-static path)
                 form (source-form-at! text form-ordinal)
                 owner-name (name owner)
                 _ (when-not
                    (and (seq? form) (= 'def (first form))
                         (= owner-name (name (second form)))
                         (= :gravity/provisional-no-authority (nth form 2 nil))
                         (= 3 (count form)))
                     (fail! "SH07-B51-GIT-SNAPSHOT" :masked-form
                            {:coordinate-ordinal coordinate-ordinal
                             :form-ordinal form-ordinal :form form}))
                 left (str "(def ^:private " owner-name "\n  ")
                 left-bytes (.getBytes left StandardCharsets/UTF_8)
                 full (byte-array (+ 1 (alength left-bytes)
                                     (alength provisional-rhs-bytes) 2))
                 _ (aset-byte full 0 (byte 10))
                 _ (System/arraycopy left-bytes 0 full 1
                                     (alength left-bytes))
                 rhs-in-full (+ 1 (alength left-bytes))
                 _ (System/arraycopy provisional-rhs-bytes 0 full rhs-in-full
                                     (alength provisional-rhs-bytes))
                 _ (aset-byte full (+ rhs-in-full
                                      (alength provisional-rhs-bytes))
                              (byte (int \))))
                 _ (aset-byte full (dec (alength full)) (byte 10))
                 source (get bytes-by-path path)
                 offsets (byte-match-offsets source full)
                 _ (when-not (= 1 (count offsets))
                     (fail! "SH07-B51-GIT-SNAPSHOT" :masked-occurrence-census
                            {:coordinate-ordinal coordinate-ordinal
                             :count (count offsets)}))
                 form-start (inc (first offsets))
                 rhs-start (+ form-start (alength left-bytes))
                 rhs-end (+ rhs-start (alength provisional-rhs-bytes))
                 form-end (inc rhs-end)
                 coordinate
                 (assoc spec
                        :left-delimiter left :right-delimiter ")"
                        :preceding-byte 10 :following-byte 10
                        :masked-rhs-byte-count (alength masked-rhs-bytes))
                 span
                 {:coordinate-ordinal coordinate-ordinal
                  :form-start-byte form-start
                  :rhs-start-byte rhs-start :rhs-end-byte rhs-end
                  :form-end-byte form-end
                  :rhs-byte-count (alength provisional-rhs-bytes)
                  :rhs-raw-sha256 (codec/R provisional-rhs-bytes)}]
             {:coordinate coordinate :span span}))
         (range) pre-mask-specs)
        coordinates (mapv :coordinate coordinates-and-spans)
        spans (mapv :span coordinates-and-spans)
        files
        (mapv
         (fn [path]
           (let [path-spans
                 (sort-by :rhs-start-byte >
                          (keep-indexed
                           (fn [ordinal span]
                             (when (= path (:path (nth coordinates ordinal)))
                               span))
                           spans))
                 masked
                 (reduce
                  (fn [bytes {:keys [rhs-start-byte rhs-end-byte]}]
                    (replace-byte-span bytes rhs-start-byte rhs-end-byte
                                       masked-rhs-bytes))
                  (get bytes-by-path path) path-spans)]
             {:path path :masked-byte-count (alength ^bytes masked)
              :masked-raw-sha256 (codec/R masked)}))
         (vec (distinct (map :path coordinates))))
        root (codec/H [:gravity/sh07-b51-masked-source-root-v1
                       {:coordinates coordinates :files files}])]
    {:coordinates coordinates :preimage-spans spans
     :preimage-files files :preimage-root root}))

(def ^:private i9-lifecycle-paths
  (vec
   (concat
    owned-c-paths
    [eligibility-receipt-path
     "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/i9-integration-receipt.edn"
     "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/i9-integration-discharge.edn"
     (governance-path-spec
      "sh07-b51-git-object-evidence-publication-implementation-v9")])))

(def ^:private g9-lifecycle-paths
  ["docs/artifacts/phase-15/evidence/sh07-b51-git-object-v9/g9-publication-tuple.edn"
   "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/g9-lifecycle-receipt.edn"
   "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/g9-formal-review.md"
   "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/g9-publication-discharge.edn"
   (governance-path-spec "sh07-b51-git-object-evidence-publication-g9-v9")])

(def ^:private p9-lifecycle-paths
  ["bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker.clj"
   "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00947-p15-s23-sh07-b51-linked-carrier-v4.clj"
   "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/p9-pin-publication-receipt.edn"
   "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/p9-formal-review.md"
   "docs/artifacts/phase-15/reviews/sh07-b51-git-object-v9/p9-final-discharge.edn"
   (governance-path-spec "sh07-b51-git-object-evidence-publication-p9-v9")])

(def ^:private read-only-paths
  ["bootstrap/clojure/src/gravity/bootstrap_impl/load_order.edn"
   "bootstrap/clojure/test/gravity/self_hosting/sh07_authoritative_runner.clj"
   "bootstrap/clojure/test/gravity/self_hosting_test_runner.clj"
   "bootstrap/gravity/src/gravity/checked_core.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/producer_a.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/producer_b.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/producer_c.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/producer_d.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/root6_a.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/root6_b.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/root8.gravity"
   "contracts/verification-catalog.edn"
   "contracts/workstream-governance.json"
   "contracts/workstream-ledger.json"
   "docs/self-hosting-slice-backlog.md"
   "docs/self-hosting-slice-ownership.edn"
   "docs/workstream-governance.md"
   "docs/workstream-ledger-v2.md"])

(defn- allocation [ordinal stage paths]
  {:ordinal ordinal :stage stage :owner :master-coordinator :paths paths})

(defn- build-v9-ownership-plan-e2 []
  (let [base
        {:schema :gravity/sh07-b51-v9-ownership-plan-e2-v1
         :allocations
         [(allocation 0 :a9 a9-paths-e1)
          (allocation 1 :i9 i9-lifecycle-paths)
          (allocation 2 :e9 evidence-paths)
          (allocation 3 :g9 g9-lifecycle-paths)
          (allocation 4 :p9 p9-lifecycle-paths)]
         :semantic-review-owner :sh-core
         :reservation-required-stages [:i9 :e9 :g9 :p9]
         :handoff-required-stages [:i9 :g9 :p9]
         :read-only-prerequisite-stages [:a9]
         :read-only-paths read-only-paths
         :open-exit-gates
         [:core :evaluation :arity :mutation :recursion :exception :pattern]
         :aggregate-sh07-state :partial
         :self-id-omits [[:plan-id]]}]
    (assoc base :plan-id
           (codec/H [:gravity/sh07-b51-v9-ownership-plan-e2-v1 base]))))

(def governance-rules-e1
  {:permitted-transitions
   [:draft :frozen :review-pending :accepted :integration-eligible :integrated]
   :admission-requirements
   [:exact-base :exact-candidate :clean-named-branch :owned-paths
    :governing-contracts :validation :independent-acceptance
    :residual-boundaries :nonclaims]
   :external-discharge-order
   [:pre-evidence :runtime :post-i9 :post-g9 :post-p9]
   :final-cleanup-required-stages [:i9 :g9 :p9]
   :aggregate-sh07-state :partial})

(defn- fenced-edn-after! [text marker]
  (let [marker-start (str/index-of text marker)
        fence-start (when marker-start
                      (str/index-of text "```clojure\n" marker-start))
        value-start (when fence-start (+ fence-start 11))
        value-end (when value-start (str/index-of text "\n```" value-start))]
    (when-not (and marker-start fence-start value-end)
      (fail! "SH07-B51-CONTRACT-DISCHARGE" :registry-literal
             {:marker marker}))
    (binding [*read-eval* false]
      (edn/read-string (subs text value-start value-end)))))

(defn- build-registries-e2 [architecture-report]
  (let [old-tags (fenced-edn-after! architecture-report
                                      "`V9-TAG-REGISTRY` is")
        old-domains (fenced-edn-after! architecture-report
                                         "`H-DOMAIN-REGISTRY` has")
        member-match
        (re-find #"(?s)Member kinds are exactly\s+`(\[.*?\])`"
                 architecture-report)
        member-kinds
        (when member-match
          (binding [*read-eval* false] (edn/read-string (second member-match))))
        sorted-keywords
        (fn [values]
          (vec (sort #(unsigned-utf8-compare (str %1) (str %2)) values)))
        tags
        (sorted-keywords
         (conj (remove #{:gravity/sh07-b51-a9-lifecycle-receipt-v1
                         :gravity/sh07-b51-v9-ownership-plan-v1
                         :gravity/sh07-b51-v9-ownership-plan-e1}
                       old-tags)
               :gravity/sh07-b51-a9-stage-result-e1
               :gravity/sh07-b51-v9-ownership-plan-e2-v1))
        domains
        (sorted-keywords
         (conj (remove #{:gravity/sh07-b51-a9-lifecycle-receipt-v1
                         :gravity/sh07-b51-v9-ownership-plan-v1
                         :gravity/sh07-b51-v9-ownership-plan-e1}
                       old-domains)
               :gravity/sh07-b51-v9-ownership-plan-e2-v1))]
    (when-not
     (and (vector? member-kinds)
          (= (count tags) (count (distinct tags)))
          (= (count domains) (count (distinct domains)))
          (every? (set tags) domains)
          (contains? (set tags) :gravity/sh07-b51-a9-stage-result-e1)
          (contains? (set tags)
                     :gravity/sh07-b51-v9-ownership-plan-e2-v1)
          (contains? (set domains)
                     :gravity/sh07-b51-v9-ownership-plan-e2-v1)
          (not (contains? (set tags)
                          :gravity/sh07-b51-v9-ownership-plan-e1))
          (not (contains? (set domains)
                          :gravity/sh07-b51-v9-ownership-plan-e1))
          (not (contains? (set domains)
                          :gravity/sh07-b51-a9-stage-result-e1)))
      (fail! "SH07-B51-CONTRACT-DISCHARGE" :registry-delta))
    {:tag-registry tags
     :identity-domain-registry
     {:domains domains :member-kinds member-kinds}}))

(defn- read-static-edn! [text reason]
  (with-open [reader (PushbackReader. (StringReader. text))]
    (binding [*read-eval* false *data-readers* {}
              *default-data-reader-fn*
              (fn [tag _]
                (fail! "SH07-B51-CONTRACT-DISCHARGE" reason {:tag tag}))]
      (let [value (read {:eof ::eof :read-cond :allow} reader)
            trailing (read {:eof ::eof :read-cond :allow} reader)]
        (when (or (= ::eof value) (not= ::eof trailing))
          (fail! "SH07-B51-CONTRACT-DISCHARGE" reason))
        value))))

(def ^:private catalog-route-ids
  ["sh07-authoritative" "sh07-b10-regression" "sh07-b11-contract"
   "sh07-b13-accepted" "sh07-b8-regression" "sh07-cached-accepted"
   "sh07-cached-contract" "sh07-cached-identity" "sh07-cached-rejected"
   "sh07-cached-verification" "sh07-conformance-parity"
   "sh07-conformance-table" "sh07-parallel-accepted"
   "sh07-parallel-b12-contract-accepted"
   "sh07-parallel-b12-identity-verification"
   "sh07-parallel-b12-rejected-regression"
   "sh07-parallel-identity-lineage" "sh07-parallel-regression-a"
   "sh07-parallel-regression-b" "sh07-parallel-rejected"
   "sh07-parallel-verification"])

(defn- build-foundation-static [content documents]
  (let [seven-descriptors (subvec documents 1 8)]
    {:content content
     :evidence-kind :validation
     :canonical-pipeline-contract-id
     (codec/H [:gravity/sh07-b51-foundation-contract-root-v1
               seven-descriptors])
     :safe-semantics
     {:allowed-outcomes
      [:proven-safe :runtime-checked :rejected :unsafe-island]
      :claims []}
     :replay-key-fields
     [:content-commit :content-tree :source-root :checker-root
      :architecture-report-sha256]
     :invalidation-key-fields
     [:content-commit :content-tree :source-root :checker-root
      :architecture-report-sha256 :governing-document-root]}))

(defn- build-catalog-projection
  [document-read-receipt decoded-static]
  (let [catalog
        (read-static-edn!
         (get decoded-static "contracts/verification-catalog.edn")
         :verification-catalog-edn)
        actual-route-ids
        (->> (:routes catalog)
             (keep (fn [[id route]]
                     (when (= "sh07-checked-core" (:module-id route)) id)))
             (sort unsigned-utf8-compare)
             vec)]
    (when-not (= catalog-route-ids actual-route-ids)
      (fail! "SH07-B51-VERIFY-CATALOG" :applicable-route-census
             {:expected catalog-route-ids :actual actual-route-ids}))
    {:schema (:schema catalog)
     :version (:version catalog)
     :authority (:authority catalog)
     :authoritative? (:authoritative? catalog)
     :source-descriptor
     (nth (:documents document-read-receipt) 27)
     :module (get-in catalog [:modules "sh07-checked-core"])
     :applicable-routes
     (mapv (fn [id] [id (get-in catalog [:routes id])]) catalog-route-ids)}))

(defn- build-proof-scope [catalog-projection decoded-static]
  (let [contract
        (read-static-edn!
         (get decoded-static
              "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn")
         :proof-contract-edn)
        boundary (:boundary contract)]
    {:evidence-kind :validation
     :formal-proof false
     :candidate-scope :sh07-b51-git-object-publication
     :lowering-boundary :verified-core
     :mir-artifacts []
     :backend-artifacts []
     :stage-support-claims []
     :proof-contract
     {:artifact (:artifact contract)
      :schema-version (:schema-version contract)
      :status (:status contract)
      :coverage-milestone (:coverage-milestone contract)
      :task (:task boundary)
      :input-task (:input-task boundary)
      :scope (:scope boundary)
      :application :preserved-out-of-scope}
     :verification-catalog catalog-projection
     :adopted-independent-controls
     [:fresh-authoritative-process :immutable-source-snapshot
      :immutable-receipts :bounded-execution :finally-cleanup]
     :b51-proof-contract-revision-required true}))

(defn- build-publication-boundary [content]
  {:content content
   :scope :verified-core
   :created-paths evidence-paths
   :created-kinds
   [:checker-identity :checker-identity :checker-output :checker-output
    :certificate-pair :linked-carrier :identity-receipt]
   :mir-artifacts [] :backend-artifacts [] :stage-support-claims []})

(def ^:private progress
  {:slice :SH-07 :status :partial
   :completed-stage-count 4 :total-stage-count 8})

(defn- pre-evidence-authority []
  {:phase :pre-evidence
   :attained [:a9-terminal-record-integrated]
   :deferred [:c-content-frozen :q-evidence-eligible
              :fresh-runtime-complete :e9-evidence-immutable
              :i9-integrated :g9-integrated :p9-integrated
              :final-cleanup-complete]
   :grants [:contract-discharge-only]
   :aggregate-sh07 progress
   :nonclaims nonclaims})

(defn reconstruct-pre-object-snapshot!
  "Reconstruct the literal E2 C-object PRE carriers with exactly 84 bounded
  Git operations.  Terminal A9 and the acyclic pre-mask are authenticated
  solely from the requested C/T objects.  The result grants no authority."
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
              (range 84)
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
         baseline-row (:bytes baseline-cat))
        a9-stage-result
        (build-a9-stage-result-e1
         content rows-by-path bytes-by-path decoded-static)
        pre-masked-root
        (build-pre-masked-root bytes-by-path decoded-static)
        ownership-plan (build-v9-ownership-plan-e2)
        registries
        (build-registries-e2 (get decoded-static architecture-report-path))
        foundation-static
        (build-foundation-static content (:documents document-read-receipt))
        catalog-projection
        (build-catalog-projection document-read-receipt decoded-static)
        proof-scope (build-proof-scope catalog-projection decoded-static)
        publication-boundary (build-publication-boundary content)]
    (when-not (and (= 84 (count observations))
                   (= (vec (range 84)) (mapv :ordinal observations))
                   (= 69 (count static-paths))
                   (= 69 (count (distinct static-paths)))
                   (= 33 (count (:documents document-read-receipt)))
                   (= 33 (count (:events document-read-receipt)))
                   (= 13 (count source-descriptors))
                   (= resolution-source-descriptor
                      (nth source-descriptors 12))
                   (= 2 (count (:preimage-files pre-masked-root)))
                   (= 33 (count owned-c-descriptors))
                   (= 3 (count (:a9-artifact-descriptors a9-stage-result))))
      (fail! "SH07-B51-CONTRACT-DISCHARGE" :pre-object-census))
    {:content content
     :baseline {:commit pre-baseline-commit :tree base-tree}
     :pre-c-paths pre-c-paths
     :observations observations
     :git-operation-count 84
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
     :pre-masked-root pre-masked-root
     :a9-stage-result a9-stage-result
     :ownership-plan ownership-plan
     :governance-rules governance-rules-e1
     :v9-tag-registry (:tag-registry registries)
     :identity-domain-registry (:identity-domain-registry registries)
     :foundation-static foundation-static
     :catalog-projection catalog-projection
     :proof-scope proof-scope
     :publication-boundary publication-boundary
     :progress progress
     :static-decode-count (count decoded-static)
     :decoded-static decoded-static
     :completion-gaps []
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

(def eligibility-coordinate-keys
  #{:schema :commit :tree :parents :path :mode :type :blob-oid :byte-count
    :raw-sha256 :receipt-id})

(def evidence-eligibility-keys
  #{:coordinate :receipt-id :freeze-state :freeze-record-id
    :source-descriptors-root :pre-masked-root :pre-discharge-id
    :participant-check-roots})

(def runtime-carrier-keys
  #{:source-session-a :source-session-b :source-session-m
    :launcher :coordinator :checker-a :checker-b :runtime-masked-root
    :foundation-runtime :bootstrap-runtime :differential-runtime
    :verification-runtime :self-hosting-runtime :semantic-witness-runtime
    :contract-test-results :proof-scope :progress :nonclaims})

(def runtime-phase-carrier-keys
  (conj runtime-carrier-keys :content :evidence-eligibility))

(def phase-evidence-keys #{:schema :phase :row-plan-id :carriers})

(def contract-discharge-keys
  #{:artifact :schema-version :phase :row-plan-id :prior-ref :evidence
    :resolved-rows :resolved-root :authority :self-id-omits :discharge-id})

(def runtime-prior-ref-keys
  #{:phase :container :value-path :discharge-id :lifecycle-ref
    :forbidden-path-observations})

(def ^:private eligibility-check-keys
  #{:coordinate :observations :pre-discharge-observations :decode-count
    :receipt-id-check-count :pre-discharge-reconstruction-count
    :pre-discharge-git-operation-count
    :pre-discharge-document-descriptor-count
    :pre-discharge-source-descriptor-count
    :pre-discharge-pre-masked-file-count
    :pre-discharge-owned-c-artifact-count
    :pre-discharge-a9-artifact-count
    :pre-discharge-baseline-boot5-count
    :pre-discharge-static-decode-count :content-match
    :source-descriptors-match :source-descriptors-root-match
    :pre-masked-root-match :owned-c-root-match :pre-discharge-match})

(def eligibility-participant-order [:l :m :a :b])

(def ^:private eligibility-receipt-keys
  #{:artifact :schema-version :content :freeze :source-descriptors
    :source-descriptors-root :pre-masked-root :pre-evidence-contract-discharge
    :review :state :self-id-omits :nonclaims :receipt-id})

(def ^:private pre-discharge-keys
  #{:artifact :schema-version :phase :row-plan-id :prior-ref :evidence
    :resolved-rows :resolved-root :authority :self-id-omits :discharge-id})

(def ^:private pre-carrier-keys
  #{:content :document-read-receipt :source-descriptors
    :source-descriptors-root :pre-masked-root :owned-c-descriptors :owned-c-root
    :row-plan :foundation-static :boot5-compatibility :ownership-plan
    :catalog-projection :governance-rules :identity-domain-registry
    :publication-boundary :proof-scope :progress :nonclaims :a9-stage-result})

(defn- validate-source-descriptors!
  [descriptors]
  (when-not (and (vector? descriptors) (= 13 (count descriptors)))
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
    (exact-keys! (:pre-masked-root carriers)
                 #{:coordinates :preimage-spans :preimage-files
                   :preimage-root}
                 "SH07-B51-RECEIPT-JOIN" :pre-masked-root-keys)
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
  (exact-keys! (:pre-masked-root receipt)
               #{:coordinates :preimage-spans :preimage-files
                 :preimage-root}
               "SH07-B51-RECEIPT-JOIN" :pre-masked-root-keys)
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
          (= (:pre-masked-root receipt)
             (get-in receipt
                     [:pre-evidence-contract-discharge :evidence :carriers
                      :pre-masked-root])))
      (fail! "SH07-B51-RECEIPT-JOIN" :eligibility-receipt)))
  receipt)

(defn validate-eligibility-coordinate!
  "Validate the closed Q external-review coordinate without reading Git.

  The two-argument form additionally binds the coordinate's parent to the
  supplied immutable C/T content.  Git-object replay remains the authority
  for the five observations in `validate-eligibility-object!`."
  ([coordinate]
   (validate-eligibility-coordinate! nil coordinate))
  ([content coordinate]
   (exact-keys! coordinate eligibility-coordinate-keys
                "SH07-B51-RECEIPT-JOIN" :eligibility-coordinate-keys)
   (doseq [[field value] [[:commit (:commit coordinate)]
                          [:tree (:tree coordinate)]
                          [:blob-oid (:blob-oid coordinate)]]]
     (require-git-id! value field "SH07-B51-RECEIPT-JOIN"))
   (when-not
    (and (= :gravity/sh07-b51-external-review-object-v1
            (:schema coordinate))
         (vector? (:parents coordinate))
         (= 1 (count (:parents coordinate)))
         (git-id? (first (:parents coordinate)))
         (or (nil? content)
             (= [(:commit content)] (:parents coordinate)))
         (safe-relative-path? (:path coordinate))
         (= eligibility-receipt-path (:path coordinate))
         (= "100644" (:mode coordinate))
         (= "blob" (:type coordinate))
         (integer? (:byte-count coordinate))
         (not (neg? (:byte-count coordinate)))
         (sha256-id? (:raw-sha256 coordinate))
         (sha256-id? (:receipt-id coordinate)))
     (fail! "SH07-B51-RECEIPT-JOIN" :eligibility-coordinate
            {:coordinate coordinate}))
   (when (and content (not= (:commit content) (:commit coordinate)))
     (fail! "SH07-B51-RECEIPT-JOIN" :eligibility-content-parent
            {:content content :coordinate coordinate}))
   coordinate))

(def ^:private eligibility-check-count-values
  {:decode-count 1
   :receipt-id-check-count 1
   :pre-discharge-reconstruction-count 1
   :pre-discharge-git-operation-count 84
   :pre-discharge-document-descriptor-count 33
   :pre-discharge-source-descriptor-count 13
   :pre-discharge-pre-masked-file-count 2
   :pre-discharge-owned-c-artifact-count 33
   :pre-discharge-a9-artifact-count 3
   :pre-discharge-baseline-boot5-count 1
   :pre-discharge-static-decode-count 69})

(def ^:private eligibility-check-match-keys
  [:content-match :source-descriptors-match
   :source-descriptors-root-match :pre-masked-root-match
   :owned-c-root-match :pre-discharge-match])

(def ^:private git-observation-keys
  #{:ordinal :operation :argv :stdin-byte-count :stdin-raw-sha256
    :exit-code :stdout-byte-count :stdout-raw-sha256 :stderr-byte-count
    :stderr-raw-sha256 :parsed-result})

(defn- validate-git-observation!
  [observation ordinal operation reason]
  (exact-keys! observation git-observation-keys
               "SH07-B51-RECEIPT-JOIN" reason)
  (let [argv (:argv observation)
        parsed (:parsed-result observation)]
    (when-not
     (and (= ordinal (:ordinal observation))
          (= operation (:operation observation))
          (vector? argv)
          (<= 3 (count argv))
          (= git-executable (first argv))
          (= "-C" (second argv))
          (string? (nth argv 2 nil))
          (not (str/blank? (nth argv 2 nil)))
          (= 0 (:stdin-byte-count observation))
          (= empty-raw-sha256 (:stdin-raw-sha256 observation))
          (= 0 (:exit-code observation))
          (integer? (:stdout-byte-count observation))
          (not (neg? (:stdout-byte-count observation)))
          (sha256-id? (:stdout-raw-sha256 observation))
          (= 0 (:stderr-byte-count observation))
          (= empty-raw-sha256 (:stderr-raw-sha256 observation))
          (map? parsed))
      (fail! "SH07-B51-RECEIPT-JOIN" reason
             {:ordinal ordinal :operation operation}))
    (case operation
      :show-tree
      (when-not
       (and (= 7 (count argv))
            (= ["show" "-s" "--format=%H%n%T%n%P"]
               (subvec argv 3 6))
            (git-id? (nth argv 6))
            (= #{:commit :tree :parents} (set (keys parsed)))
            (git-id? (:commit parsed))
            (git-id? (:tree parsed))
            (vector? (:parents parsed))
            (every? git-id? (:parents parsed)))
        (fail! "SH07-B51-RECEIPT-JOIN" reason
               {:ordinal ordinal :operation operation}))

      :diff-tree-name-status-z
      (when-not
       (and (= 10 (count argv))
            (= ["diff-tree" "--no-commit-id" "--name-status" "-r" "-z"]
               (subvec argv 3 8))
            (git-id? (nth argv 8))
            (git-id? (nth argv 9))
            (= #{:parent :commit :entries} (set (keys parsed)))
            (= (nth argv 8) (:parent parsed))
            (= (nth argv 9) (:commit parsed))
            (vector? (:entries parsed))
            (every? #(and (= #{:status :path} (set (keys %)))
                          (= :added (:status %))
                          (safe-relative-path? (:path %)))
                    (:entries parsed)))
        (fail! "SH07-B51-RECEIPT-JOIN" reason
               {:ordinal ordinal :operation operation}))

      :ls-tree-z-requested
      (when-not
       (and (<= 8 (count argv))
            (= ["ls-tree" "-z"] (subvec argv 3 5))
            (git-id? (nth argv 5))
            (= "--" (nth argv 6))
            (= (vec (drop 7 argv)) (:requested-paths parsed))
            (= #{:commit :requested-paths :rows} (set (keys parsed)))
            (= (nth argv 5) (:commit parsed))
            (vector? (:requested-paths parsed))
            (= (count (:requested-paths parsed))
               (count (distinct (:requested-paths parsed))))
            (every? safe-relative-path? (:requested-paths parsed))
            (vector? (:rows parsed))
            (= (count (:requested-paths parsed)) (count (:rows parsed)))
            (every?
             #(and (= #{:path :present :mode :type :oid} (set (keys %)))
                   (safe-relative-path? (:path %))
                   (boolean? (:present %))
                   (if (:present %)
                     (and (= "100644" (:mode %))
                          (= "blob" (:type %))
                          (git-id? (:oid %)))
                     (and (nil? (:mode %)) (nil? (:type %))
                          (nil? (:oid %)))))
             (:rows parsed)))
        (fail! "SH07-B51-RECEIPT-JOIN" reason
               {:ordinal ordinal :operation operation}))

      :cat-file-blob
      (when-not
       (and (= 6 (count argv))
            (= ["cat-file" "blob"] (subvec argv 3 5))
            (git-id? (nth argv 5))
            (or (= #{:oid :byte-count :raw-sha256} (set (keys parsed)))
                (= #{:oid :byte-count :raw-sha256 :value}
                   (set (keys parsed))))
            (= (nth argv 5) (:oid parsed))
            (integer? (:byte-count parsed))
            (not (neg? (:byte-count parsed)))
            (sha256-id? (:raw-sha256 parsed)))
        (fail! "SH07-B51-RECEIPT-JOIN" reason
               {:ordinal ordinal :operation operation}))

      (fail! "SH07-B51-RECEIPT-JOIN" reason
             {:ordinal ordinal :operation operation})))
  observation)

(defn- validate-git-observation-sequence!
  [observations operations reason]
  (when-not (and (vector? observations)
                 (= (count operations) (count observations)))
    (fail! "SH07-B51-RECEIPT-JOIN" reason
           {:expected-count (count operations)
            :actual-count (when (vector? observations)
                            (count observations))}))
  (mapv (fn [ordinal [observation operation]]
          (validate-git-observation!
           observation ordinal operation reason))
        (range)
        (map vector observations operations)))

(def ^:private eligibility-object-operation-sequence
  [:show-tree :diff-tree-name-status-z :ls-tree-z-requested
   :cat-file-blob :ls-tree-z-requested])

(def ^:private pre-discharge-operation-sequence
  (vec (concat [:show-tree :ls-tree-z-requested]
               (repeat (count pre-c-paths) :cat-file-blob)
               [:show-tree :ls-tree-z-requested :cat-file-blob])))

(declare reject-runtime-back-edges!)

(defn- validate-participant-checks!
  [coordinate participant-results]
  (when-not (and (vector? participant-results)
                 (= eligibility-participant-order
                    (mapv :participant participant-results)))
    (fail! "SH07-B51-RECEIPT-JOIN" :participant-order
           {:participants (when (vector? participant-results)
                            (mapv :participant participant-results))}))
  (doseq [[ordinal result] (map-indexed vector participant-results)]
    (exact-keys! result #{:participant :eligibility-check
                           :eligibility-check-root}
                 "SH07-B51-RECEIPT-JOIN" :participant-result-keys)
    (let [check (:eligibility-check result)]
      (exact-keys! check eligibility-check-keys
                   "SH07-B51-RECEIPT-JOIN" :eligibility-check-keys)
      (validate-git-observation-sequence!
       (:observations check) eligibility-object-operation-sequence
       :eligibility-observation-sequence)
      (validate-git-observation-sequence!
       (:pre-discharge-observations check)
       pre-discharge-operation-sequence
       :pre-discharge-observation-sequence)
      (when-not
       (and (= coordinate (:coordinate check))
            (= eligibility-check-count-values
               (select-keys check (keys eligibility-check-count-values)))
            (every? true? (map check eligibility-check-match-keys))
            (sha256-id? (:eligibility-check-root result))
            (= (:eligibility-check-root result)
               (codec/H [:gravity/sh07-b51-eligibility-check-root-v1
                         check])))
        (fail! "SH07-B51-RECEIPT-JOIN" :eligibility-check
               {:participant (:participant result)
                :ordinal ordinal}))))
  participant-results)

(defn validate-evidence-eligibility!
  "Validate the normalized E1 EVIDENCE-ELIGIBILITY carrier.

  The optional context map is used by the builder and strict identity-receipt
  path to prove every value still equals the independently decoded Q receipt
  and the ordered L/M/A/B checks.  No runtime or later lifecycle value is
  accepted in this carrier."
  ([eligibility]
   (validate-evidence-eligibility! eligibility nil))
  ([eligibility {:keys [content receipt participant-results]}]
   (exact-keys! eligibility evidence-eligibility-keys
                "SH07-B51-RECEIPT-JOIN" :evidence-eligibility-keys)
   (let [coordinate (:coordinate eligibility)
         pre-root (:pre-masked-root eligibility)]
     (validate-eligibility-coordinate! content coordinate)
     (validate-pre-masked-root! pre-root)
     (when-not (and (sha256-id? (:receipt-id eligibility))
                    (= (:receipt-id coordinate) (:receipt-id eligibility))
                    (= :frozen (:freeze-state eligibility))
                    (sha256-id? (:freeze-record-id eligibility))
                    (sha256-id? (:source-descriptors-root eligibility))
                    (sha256-id? (:pre-discharge-id eligibility))
                    (vector? (:participant-check-roots eligibility))
                    (= 4 (count (:participant-check-roots eligibility)))
                    (every? sha256-id? (:participant-check-roots eligibility)))
       (fail! "SH07-B51-RECEIPT-JOIN" :evidence-eligibility-shape))
     (when receipt
       (validate-eligibility-receipt! (or content (:content receipt)) receipt)
       (let [pre-discharge (:pre-evidence-contract-discharge receipt)
             expected-record-id
             (get-in receipt [:freeze :active-record-ref :record-id])]
         (when-not
          (and (= (:receipt-id receipt) (:receipt-id eligibility))
               (= :frozen (get-in receipt [:freeze :state]))
               (= expected-record-id (:freeze-record-id eligibility))
               (= (:source-descriptors-root receipt)
                  (:source-descriptors-root eligibility))
               (= (:pre-masked-root receipt) pre-root)
               (= (:discharge-id pre-discharge)
                  (:pre-discharge-id eligibility))
               (= (:source-descriptors-root receipt)
                  (codec/H [:gravity/sh07-b51-source-descriptors-root-v1
                            (:source-descriptors receipt)])))
           ;; The coordinate/receipt relationship is checked below as a
           ;; complete map comparison; this branch is intentionally explicit
           ;; so a missing nested Q field cannot be treated as a nil default.
           (fail! "SH07-B51-RECEIPT-JOIN" :evidence-eligibility-q-join
                  {:eligibility eligibility}))
         (when (and content (not= content (:content receipt)))
           (fail! "SH07-B51-RECEIPT-JOIN" :evidence-eligibility-content))))
     (when participant-results
       (validate-participant-checks! coordinate participant-results)
       (when-not (= (mapv :eligibility-check-root participant-results)
                    (:participant-check-roots eligibility))
         (fail! "SH07-B51-RECEIPT-JOIN"
                :evidence-eligibility-participant-roots)))
     (reject-runtime-back-edges! eligibility [])
     eligibility)))

(defn build-evidence-eligibility
  "Build E1 EVIDENCE-ELIGIBILITY only from a complete Q receipt and the
  independently completed L/M/A/B eligibility checks."
  ([coordinate receipt participant-results]
   (validate-eligibility-coordinate! (:content receipt) coordinate)
   (validate-eligibility-receipt! (:content receipt) receipt)
   (validate-participant-checks! coordinate participant-results)
   (let [pre-discharge (:pre-evidence-contract-discharge receipt)
         eligibility
         {:coordinate coordinate
          :receipt-id (:receipt-id receipt)
          :freeze-state (get-in receipt [:freeze :state])
          :freeze-record-id
          (get-in receipt [:freeze :active-record-ref :record-id])
          :source-descriptors-root (:source-descriptors-root receipt)
          :pre-masked-root (:pre-masked-root receipt)
          :pre-discharge-id (:discharge-id pre-discharge)
          :participant-check-roots
          (mapv :eligibility-check-root participant-results)}]
     (validate-evidence-eligibility!
      eligibility {:content (:content receipt)
                   :receipt receipt
                   :participant-results participant-results})))
  ([input]
   (exact-keys! input #{:coordinate :receipt :participant-results}
                "SH07-B51-RECEIPT-JOIN" :evidence-eligibility-input-keys)
   (build-evidence-eligibility (:coordinate input) (:receipt input)
                               (:participant-results input))))

(defn build-evidence-eligibility!
  [& arguments]
  (apply build-evidence-eligibility arguments))

(def ^:private runtime-back-edge-keys
  #{:identity-receipt :runtime-contract-discharge :enclosing-discharge
    :enclosing-identity-receipt :enclosing-receipt :enclosing-receipt-id
    :identity-receipt-id :e9 :e9-identity-receipt :e9-identity-receipt-id
    :g9 :g9-lifecycle :g9-lifecycle-id :p9 :p9-publication :p9-final-discharge
    :p9-final-discharge-id :evidence-receipt :evidence-receipt-id
    :evidence-commit :evidence-tree :discharge :discharge-id})

(defn- reject-runtime-back-edges!
  ([value] (reject-runtime-back-edges! value []))
  ([value path]
   (cond
     (map? value)
     (doseq [[key child] value]
       (when (contains? runtime-back-edge-keys key)
         (fail! "SH07-B51-RECEIPT-JOIN" :runtime-identity-cycle
                {:path (conj path key)}))
       (reject-runtime-back-edges! child (conj path key)))

     (vector? value)
     (doseq [[ordinal child] (map-indexed vector value)]
       (reject-runtime-back-edges! child (conj path ordinal)))

     (list? value)
     (doseq [[ordinal child] (map-indexed vector value)]
       (reject-runtime-back-edges! child (conj path ordinal)))

     (set? value)
     (doseq [child value]
       (reject-runtime-back-edges! child (conj path :member)))

     :else nil)
   value))

(defn- validate-runtime-content!
  [content]
  (exact-keys! content #{:commit :tree}
               "SH07-B51-CONTRACT-DISCHARGE" :runtime-content-keys)
  (require-git-id! (:commit content) :commit
                   "SH07-B51-CONTRACT-DISCHARGE")
  (require-git-id! (:tree content) :tree
                   "SH07-B51-CONTRACT-DISCHARGE")
  content)

(defn- validate-runtime-carrier-pool!
  [carriers]
  (exact-keys! carriers runtime-carrier-keys
               "SH07-B51-CONTRACT-DISCHARGE" :runtime-carrier-keys)
  (doseq [[key value] carriers]
    (when (nil? value)
      (fail! "SH07-B51-CONTRACT-DISCHARGE" :runtime-carrier-nil
             {:carrier key})))
  (validate-runtime-masked-root! (:runtime-masked-root carriers))
  (reject-runtime-back-edges! carriers [])
  carriers)

(defn- validate-runtime-phase-carriers!
  [carriers]
  (exact-keys! carriers runtime-phase-carrier-keys
               "SH07-B51-CONTRACT-DISCHARGE"
               :runtime-phase-carrier-keys)
  (validate-runtime-content! (:content carriers))
  (validate-evidence-eligibility! (:evidence-eligibility carriers))
  (validate-runtime-carrier-pool!
   (dissoc carriers :content :evidence-eligibility))
  (reject-runtime-back-edges! carriers [])
  carriers)

(defn validate-phase-evidence!
  "Validate the closed runtime PHASE-EVIDENCE wrapper.

  This runtime seam deliberately rejects PRE/POST carrier pools; later G9/P9
  validators must admit their own exact phase-specific maps."
  [phase-evidence]
  (exact-keys! phase-evidence phase-evidence-keys
               "SH07-B51-CONTRACT-DISCHARGE" :phase-evidence-keys)
  (when-not (and (= :gravity/sh07-b51-phase-evidence-v1
                    (:schema phase-evidence))
                 (= :runtime (:phase phase-evidence))
                 (= row-plan-id (:row-plan-id phase-evidence)))
    (fail! "SH07-B51-CONTRACT-DISCHARGE" :runtime-phase-evidence-header))
  (validate-runtime-phase-carriers! (:carriers phase-evidence))
  phase-evidence)

(defn build-runtime-phase-evidence
  "Build the runtime PHASE-EVIDENCE wrapper from a strict 19-key runtime
  carrier pool plus its immutable C/T and EVIDENCE-ELIGIBILITY joins."
  ([content evidence-eligibility runtime-carriers]
   (validate-runtime-content! content)
   (validate-evidence-eligibility! evidence-eligibility)
   (validate-runtime-carrier-pool! runtime-carriers)
   (validate-phase-evidence!
    {:schema :gravity/sh07-b51-phase-evidence-v1
     :phase :runtime
     :row-plan-id row-plan-id
     :carriers (merge {:content content
                       :evidence-eligibility evidence-eligibility}
                      runtime-carriers)}))
  ([input]
   (exact-keys! input #{:content :evidence-eligibility :runtime-carriers}
                "SH07-B51-CONTRACT-DISCHARGE"
                :runtime-phase-evidence-input-keys)
   (build-runtime-phase-evidence (:content input)
                                 (:evidence-eligibility input)
                                 (:runtime-carriers input))))

(defn build-runtime-phase-evidence!
  [& arguments]
  (apply build-runtime-phase-evidence arguments))

(def runtime-authority-attained
  [:a9-terminal-record-integrated :c-content-frozen
   :q-evidence-eligible :fresh-runtime-complete])

(def runtime-authority-deferred
  [:e9-evidence-immutable :i9-integrated :g9-integrated :p9-integrated
   :final-cleanup-complete])

(defn runtime-authority []
  {:phase :runtime
   :attained runtime-authority-attained
   :deferred runtime-authority-deferred
   :grants [:contract-discharge-only]
   :aggregate-sh07 progress
   :nonclaims nonclaims})

(def ^:private identity-receipt-normalization
  {:algorithm :gravity/sh07-b51-explicit-semantic-projection-v1
   :receipt-only :gravity/receipt-only
   :child-indexes [0 1]
   :classpath-indexes [0 1 2 3]
   :coordinator-classpath-phases
   [:m-entry :pre-a :post-a :pre-b :post-b :pre-source-session
    :post-source-session :m-exit]
   :recompute-order
   [:payload-value-ids :classpath-roots :argv-ids :request-ids
    :tcb-manifest-ids :response-values-and-transport :process-handle-ids
    :source-session-ids :coordinator-event-ids :coordinator-event-root
    :coordinator-receipt :m-response :launcher-validation-input
    :contract-validation :launcher-receipt-id
    :contract-document-event-root :contract-row-plan-id
    :pre-resolved-row-root :pre-discharge-id :runtime-resolved-row-root
    :runtime-discharge-id :receipt-id]})

(def ^:private prior-ref-container-keys
  #{:commit :tree :parents :path :mode :type :blob-oid :byte-count
    :raw-sha256})

(def ^:private runtime-i9-suffix
  (subvec (vec i9-lifecycle-paths) 33))

(def ^:private runtime-forbidden-path-refs
  [[:e9 0] [:e9 1] [:e9 2] [:e9 3] [:e9 4] [:e9 5] [:e9 6]
   [:i9-suffix 1] [:i9-suffix 2] [:i9-suffix 3]
   [:g9 0] [:g9 1] [:g9 2] [:g9 3] [:g9 4]
   [:p9 0] [:p9 1] [:p9 2] [:p9 3] [:p9 4] [:p9 5]])

(def ^:private runtime-lifecycle-ref-keys
  #{:stage :path-ref :container :value-path :required-result :receipt-id})

(def ^:private runtime-forbidden-observation-keys
  #{:ordinal :path-ref :resolved-path :argv :exit-code
    :stdout-byte-count :stdout-raw-sha256 :parsed-result})

(defn- validate-reference-container!
  [container reason]
  (exact-keys! container prior-ref-container-keys
               "SH07-B51-CONTRACT-DISCHARGE" reason)
  (when-not
   (and (git-id? (:commit container))
        (git-id? (:tree container))
        (vector? (:parents container))
        (every? git-id? (:parents container))
        (safe-relative-path? (:path container))
        (= "100644" (:mode container))
        (= "blob" (:type container))
        (git-id? (:blob-oid container))
        (integer? (:byte-count container))
        (not (neg? (:byte-count container)))
        (sha256-id? (:raw-sha256 container)))
    (fail! "SH07-B51-CONTRACT-DISCHARGE" reason
           {:container container}))
  container)

(defn- runtime-forbidden-path
  [path-ref]
  (let [[domain ordinal] path-ref]
    (case domain
      :e9 (nth evidence-paths ordinal nil)
      :i9-suffix (nth runtime-i9-suffix ordinal nil)
      :g9 (nth g9-lifecycle-paths ordinal nil)
      :p9 (nth p9-lifecycle-paths ordinal nil)
      nil)))

(defn- valid-governance-resolved-path?
  [spec resolved-path]
  (and (string? resolved-path)
       (safe-relative-path? resolved-path)
       (str/starts-with?
        resolved-path (str (:directory spec) "/" (:workstream-id spec) "-"))
       (str/ends-with? resolved-path ".json")))

(defn- validate-runtime-lifecycle-ref!
  [lifecycle-ref eligibility]
  (exact-keys! lifecycle-ref runtime-lifecycle-ref-keys
               "SH07-B51-CONTRACT-DISCHARGE" :runtime-lifecycle-ref-keys)
  (validate-reference-container!
   (:container lifecycle-ref) :runtime-lifecycle-ref-container)
  (let [expected-container
        (select-keys (:coordinate eligibility) prior-ref-container-keys)
        expected-path (runtime-forbidden-path [:i9-suffix 0])]
    (when-not
     (and (= :pre-evidence (:stage lifecycle-ref))
          (= [:i9-suffix 0] (:path-ref lifecycle-ref))
          (= [] (:value-path lifecycle-ref))
          (= :evidence-eligible (:required-result lifecycle-ref))
          (sha256-id? (:receipt-id lifecycle-ref))
          (= expected-container (:container lifecycle-ref))
          (= expected-path (:path (:container lifecycle-ref)))
          (= (:freeze-record-id eligibility) (:receipt-id lifecycle-ref)))
      (fail! "SH07-B51-CONTRACT-DISCHARGE" :runtime-lifecycle-ref
             {:lifecycle-ref lifecycle-ref}))
    lifecycle-ref))

(defn- validate-runtime-forbidden-observations!
  [observations prior-container]
  (when-not (and (vector? observations)
                 (= (count runtime-forbidden-path-refs)
                    (count observations)))
    (fail! "SH07-B51-CONTRACT-DISCHARGE"
           :runtime-forbidden-path-observation-count))
  (let [repository
        (atom nil)]
    (doseq [[ordinal [observation path-ref]]
            (map-indexed vector
                         (map vector observations
                              runtime-forbidden-path-refs))]
      (exact-keys! observation runtime-forbidden-observation-keys
                   "SH07-B51-CONTRACT-DISCHARGE"
                   :runtime-forbidden-path-observation-keys)
      (let [expected-path (runtime-forbidden-path path-ref)
            resolved-path (:resolved-path observation)
            argv (:argv observation)]
        (when-not
         (and (= ordinal (:ordinal observation))
              (= path-ref (:path-ref observation))
              (or (= expected-path resolved-path)
                  (and (map? expected-path)
                       (valid-governance-resolved-path?
                        expected-path resolved-path)))
              (vector? argv)
              (= 8 (count argv))
              (= git-executable (nth argv 0))
              (= "-C" (nth argv 1))
              (string? (nth argv 2))
              (not (str/blank? (nth argv 2)))
              (= "ls-tree" (nth argv 3))
              (= "-z" (nth argv 4))
              (= (:commit prior-container) (nth argv 5))
              (= "--" (nth argv 6))
              (= resolved-path (nth argv 7))
              (= 0 (:exit-code observation))
              (= 0 (:stdout-byte-count observation))
              (= empty-raw-sha256 (:stdout-raw-sha256 observation))
              (= [] (:parsed-result observation)))
          (fail! "SH07-B51-CONTRACT-DISCHARGE"
                 :runtime-forbidden-path-observation
                 {:ordinal ordinal :path-ref path-ref}))
        (if (nil? @repository)
          (reset! repository (nth argv 2))
          (when-not (= @repository (nth argv 2))
            (fail! "SH07-B51-CONTRACT-DISCHARGE"
                   :runtime-forbidden-path-repository
                   {:ordinal ordinal}))))))
  observations)

(defn validate-runtime-prior-ref!
  "Validate the exact Q PRE-DISCHARGE prior reference used by runtime."
  ([prior-ref]
   (validate-runtime-prior-ref! prior-ref nil nil))
  ([prior-ref eligibility]
   (validate-runtime-prior-ref!
    prior-ref eligibility
    (when eligibility (:pre-discharge-id eligibility))))
  ([prior-ref eligibility pre-discharge-id]
   (exact-keys! prior-ref runtime-prior-ref-keys
                "SH07-B51-CONTRACT-DISCHARGE" :runtime-prior-ref-keys)
   (validate-reference-container!
    (:container prior-ref) :runtime-prior-ref-container-keys)
   (let [container (:container prior-ref)]
     (when-not
      (and (= :runtime (:phase prior-ref))
           (= [:pre-evidence-contract-discharge]
              (:value-path prior-ref))
           (git-id? (:commit container))
           (git-id? (:tree container))
           (vector? (:parents container))
           (every? git-id? (:parents container))
           (safe-relative-path? (:path container))
           (= "100644" (:mode container))
           (= "blob" (:type container))
           (git-id? (:blob-oid container))
           (integer? (:byte-count container))
           (not (neg? (:byte-count container)))
           (sha256-id? (:raw-sha256 container))
           (sha256-id? (:discharge-id prior-ref))
           (map? (:lifecycle-ref prior-ref))
           (vector? (:forbidden-path-observations prior-ref)))
       (fail! "SH07-B51-CONTRACT-DISCHARGE" :runtime-prior-ref))
     (when eligibility
       (validate-evidence-eligibility! eligibility)
       (when-not (= (select-keys (:coordinate eligibility)
                                  prior-ref-container-keys)
                    container)
         (fail! "SH07-B51-CONTRACT-DISCHARGE"
                :runtime-prior-ref-container-eligibility))
       (validate-runtime-lifecycle-ref!
        (:lifecycle-ref prior-ref) eligibility)
       (when-not (= (:discharge-id prior-ref)
                    (:pre-discharge-id eligibility))
         (fail! "SH07-B51-CONTRACT-DISCHARGE"
                :runtime-prior-ref-eligibility-id)))
     (when pre-discharge-id
       (when-not (= (:discharge-id prior-ref) pre-discharge-id)
         (fail! "SH07-B51-CONTRACT-DISCHARGE"
                :runtime-prior-ref-discharge-id)))
     (validate-runtime-forbidden-observations!
      (:forbidden-path-observations prior-ref) container)
     (reject-runtime-back-edges! (:lifecycle-ref prior-ref) [:lifecycle-ref])
     prior-ref)))

(defn validate-runtime-contract-discharge!
  "Validate the complete runtime discharge, including its Q prior-ref,
  resolved dense row vector/root, authority prefix, and self-ID equation."
  ([discharge]
   (validate-runtime-contract-discharge! discharge nil))
  ([discharge {:keys [content evidence-eligibility runtime-carriers prior-ref]}]
   (exact-keys! discharge contract-discharge-keys
                "SH07-B51-CONTRACT-DISCHARGE" :runtime-discharge-keys)
   (when-not (and (= :gravity/sh07-b51-runtime-contract-discharge-v1
                     (:artifact discharge))
                  (= 1 (:schema-version discharge))
                  (= :runtime (:phase discharge))
                  (= row-plan-id (:row-plan-id discharge))
                  (= [[:discharge-id]] (:self-id-omits discharge))
                  (sha256-id? (:resolved-root discharge))
                  (sha256-id? (:discharge-id discharge)))
     (fail! "SH07-B51-CONTRACT-DISCHARGE" :runtime-discharge-header))
   (validate-runtime-prior-ref! (:prior-ref discharge)
                                (:evidence-eligibility
                                 (get-in discharge [:evidence :carriers]))
                                (get-in discharge
                                        [:evidence :carriers
                                         :evidence-eligibility
                                         :pre-discharge-id]))
   (validate-phase-evidence! (:evidence discharge))
   (let [carriers (get-in discharge [:evidence :carriers])
         resolved (resolve-contract-phase! :runtime carriers)
         expected-without-id (dissoc discharge :discharge-id)
         expected-id
         (codec/H [:gravity/sh07-b51-runtime-contract-discharge-v1
                   expected-without-id])]
     (when-not
      (and (= (:resolved-rows resolved) (:resolved-rows discharge))
           (= (:resolved-root resolved) (:resolved-root discharge))
           (= (runtime-authority) (:authority discharge))
           (= expected-id (:discharge-id discharge)))
       (fail! "SH07-B51-CONTRACT-DISCHARGE" :runtime-discharge-identity))
     (when content
       (validate-runtime-content! content)
       (when-not (= content (:content carriers))
         (fail! "SH07-B51-CONTRACT-DISCHARGE" :runtime-content-join)))
     (when evidence-eligibility
       (validate-evidence-eligibility! evidence-eligibility)
       (when-not (= evidence-eligibility
                    (:evidence-eligibility carriers))
         (fail! "SH07-B51-CONTRACT-DISCHARGE"
                :runtime-eligibility-join)))
     (when runtime-carriers
       (validate-runtime-carrier-pool! runtime-carriers)
       (when-not (= runtime-carriers
                    (dissoc carriers :content :evidence-eligibility))
         (fail! "SH07-B51-CONTRACT-DISCHARGE"
                :runtime-carrier-join)))
     (when prior-ref
       (validate-runtime-prior-ref! prior-ref evidence-eligibility)
       (when-not (= prior-ref (:prior-ref discharge))
         (fail! "SH07-B51-CONTRACT-DISCHARGE" :runtime-prior-ref-join)))
     discharge)))

(defn build-runtime-contract-discharge
  "Construct RUNTIME-CONTRACT-DISCHARGE only after the complete Q-derived
  eligibility and closed runtime carrier pool are available.  The caller must
  supply the complete PRIOR-REF, including its immutable lifecycle and
  forbidden-path observations; this function never fabricates those facts."
  ([content evidence-eligibility runtime-carriers prior-ref]
   (validate-runtime-content! content)
   (validate-evidence-eligibility! evidence-eligibility)
   (validate-runtime-carrier-pool! runtime-carriers)
   (validate-runtime-prior-ref! prior-ref evidence-eligibility)
   (let [evidence (build-runtime-phase-evidence
                   content evidence-eligibility runtime-carriers)
         resolved (resolve-contract-phase! :runtime (:carriers evidence))
         without-id
         {:artifact :gravity/sh07-b51-runtime-contract-discharge-v1
          :schema-version 1
          :phase :runtime
          :row-plan-id row-plan-id
          :prior-ref prior-ref
          :evidence evidence
          :resolved-rows (:resolved-rows resolved)
          :resolved-root (:resolved-root resolved)
          :authority (runtime-authority)
          :self-id-omits [[:discharge-id]]}
         discharge
         (assoc without-id :discharge-id
                (codec/H [:gravity/sh07-b51-runtime-contract-discharge-v1
                          without-id]))]
     (validate-runtime-contract-discharge!
      discharge {:content content
                 :evidence-eligibility evidence-eligibility
                 :runtime-carriers runtime-carriers
                 :prior-ref prior-ref})))
  ([input]
   (exact-keys! input #{:content :evidence-eligibility :runtime-carriers
                         :prior-ref}
                "SH07-B51-CONTRACT-DISCHARGE"
                :runtime-discharge-input-keys)
   (build-runtime-contract-discharge (:content input)
                                     (:evidence-eligibility input)
                                     (:runtime-carriers input)
                                     (:prior-ref input))))

(defn build-runtime-contract-discharge!
  [& arguments]
  (apply build-runtime-contract-discharge arguments))

(defn validate-eligibility-object!
  "Validate Q/TQ as the direct sibling-lineage child of C using exactly five
  Git object operations, then independently reconstruct and byte-compare the
  E2 PRE discharge with exactly 84 further Git object operations."
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
          received-discharge (:pre-evidence-contract-discharge receipt)
          pre-carriers
          (get-in receipt
                  [:pre-evidence-contract-discharge :evidence :carriers])
          expected-carriers
          (assoc pre-carriers
                 :content content
                 :document-read-receipt (:document-read-receipt pre)
                 :source-descriptors (:source-descriptors pre)
                 :source-descriptors-root (:source-descriptors-root pre)
                 :pre-masked-root (:pre-masked-root pre)
                 :owned-c-descriptors (:owned-c-descriptors pre)
                 :owned-c-root (:owned-c-root pre)
                 :row-plan row-plan
                 :foundation-static (:foundation-static pre)
                 :boot5-compatibility (:boot5-compatibility pre)
                 :ownership-plan (:ownership-plan pre)
                 :catalog-projection (:catalog-projection pre)
                 :governance-rules (:governance-rules pre)
                 :identity-domain-registry (:identity-domain-registry pre)
                 :publication-boundary (:publication-boundary pre)
                 :proof-scope (:proof-scope pre)
                 :progress (:progress pre)
                 :nonclaims nonclaims
                 :a9-stage-result (:a9-stage-result pre))
          expected-evidence
          (assoc (:evidence received-discharge) :carriers expected-carriers)
          expected-resolved (resolve-contract-phase! :pre-evidence
                                                      expected-carriers)
          expected-without-id
          (-> received-discharge
              (assoc :evidence expected-evidence
                     :resolved-rows (:resolved-rows expected-resolved)
                     :resolved-root (:resolved-root expected-resolved)
                     :authority (pre-evidence-authority))
              (dissoc :discharge-id))
          expected-discharge
          (assoc expected-without-id :discharge-id
                 (codec/H
                  [:gravity/sh07-b51-pre-evidence-contract-discharge-v1
                   expected-without-id]))
          content-match
          (and (= content (:content receipt))
               (= content (:content pre-carriers))
               (= content (get-in pre-carriers [:a9-stage-result :content])))
          source-match (= (:source-descriptors pre)
                          (:source-descriptors receipt)
                          (:source-descriptors pre-carriers))
          source-root-match (= (:source-descriptors-root pre)
                               (:source-descriptors-root receipt)
                               (:source-descriptors-root pre-carriers))
          pre-mask-match (= (:pre-masked-root pre)
                            (:pre-masked-root receipt)
                            (:pre-masked-root pre-carriers))
          owned-root-match (= (:owned-c-root pre) (:owned-c-root pre-carriers))
          pre-discharge-match
          (Arrays/equals ^bytes (codec/W expected-discharge)
                         ^bytes (codec/W received-discharge))
          eligibility-check
          {:coordinate eligibility-coordinate
           :observations observations
           :pre-discharge-observations (:observations pre)
           :decode-count 1
           :receipt-id-check-count 1
           :pre-discharge-reconstruction-count 1
           :pre-discharge-git-operation-count 84
           :pre-discharge-document-descriptor-count 33
           :pre-discharge-source-descriptor-count 13
           :pre-discharge-pre-masked-file-count 2
           :pre-discharge-owned-c-artifact-count 33
           :pre-discharge-a9-artifact-count 3
           :pre-discharge-baseline-boot5-count 1
           :pre-discharge-static-decode-count 69
           :content-match content-match
           :source-descriptors-match source-match
           :source-descriptors-root-match source-root-match
           :pre-masked-root-match pre-mask-match
           :owned-c-root-match owned-root-match
           :pre-discharge-match pre-discharge-match}
          check-root
          (codec/H [:gravity/sh07-b51-eligibility-check-root-v1
                    eligibility-check])]
      (when-not
       (and content-match source-match source-root-match pre-mask-match
            owned-root-match pre-discharge-match
            (= (:document-read-receipt pre)
               (:document-read-receipt pre-carriers))
            (= (:owned-c-descriptors pre)
               (:owned-c-descriptors pre-carriers))
            (= (:boot5-compatibility pre)
               (:boot5-compatibility pre-carriers))
            (= (pre-evidence-authority) (:authority received-discharge)))
        (fail! "SH07-B51-RECEIPT-JOIN" :pre-object-carrier-join))
      {:coordinate eligibility-coordinate
       :receipt receipt
       :observations observations
       :operation-count 5
       :pre-object-snapshot pre
       :pre-discharge-observations (:observations pre)
       :pre-discharge-git-operation-count 84
       :scope :immutable-q-coordinate-and-pre-discharge
       :eligibility-check eligibility-check
       :eligibility-check-root check-root
       :remaining-gaps []
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
  (when-not (and (= :gravity/sh07-b51-external-review-object-v1
                    (:schema eligibility-coordinate))
                 (= [(:commit content)] (:parents eligibility-coordinate))
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

(declare validate-runtime-identity-receipt!)

(defn- validate-identity-receipt-envelope!
  ([content receipt]
   (validate-identity-receipt-envelope! content receipt false))
  ([content receipt strict?]
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
   (when strict?
     (validate-runtime-identity-receipt! content receipt))
   receipt))

(defn validate-runtime-identity-receipt!
  "Validate an identity receipt only after its complete runtime joins exist.

  Existing-object replay intentionally uses the two-argument envelope helper
  above because replay fixtures predate the runtime discharge.  This public
  path is the authority-bearing validation: it requires complete E1
  EVIDENCE-ELIGIBILITY and RUNTIME-CONTRACT-DISCHARGE values, recomputes both
  nested identities, and rejects later-phase or enclosing receipt cycles.

  The context must provide the independently decoded Q eligibility receipt and
  the ordered L/M/A/B participant checks.  This path never invents missing Q
  or participant values."
  ([content receipt]
   (fail! "SH07-B51-RECEIPT-JOIN"
          :runtime-identity-context-required))
  ([content receipt context]
   (validate-identity-receipt-envelope! content receipt)
   (let [allowed-context-keys #{:eligibility-receipt :participant-results}]
     (exact-keys! context allowed-context-keys
                  "SH07-B51-RECEIPT-JOIN"
                  :runtime-identity-context-keys)
     (let [eligibility (:evidence-eligibility receipt)
           discharge (:runtime-contract-discharge receipt)
           q-receipt (:eligibility-receipt context)
           participant-results (:participant-results context)]
       (let [events (:coordinator-events receipt)
             children (:child-process-receipts receipt)
             payload-joins (:payload-joins receipt)]
         (when-not (and (vector? events) (= 11 (count events))
                        (every? #(and (map? %) (seq %)) events)
                        (vector? children) (= 2 (count children))
                        (every? #(and (map? %) (seq %)) children)
                        (map? (:launcher-receipt receipt))
                        (seq (:launcher-receipt receipt))
                        (map? (:coordinator-receipt receipt))
                        (seq (:coordinator-receipt receipt)))
           (fail! "SH07-B51-RECEIPT-JOIN"
                  :runtime-identity-process-joins))
         (exact-keys! payload-joins
                      #{:checker-identity-raw-sha256s
                        :checker-output-raw-sha256s
                        :certificate-pair-raw-sha256
                        :linked-carrier-raw-sha256}
                      "SH07-B51-RECEIPT-JOIN"
                      :runtime-identity-payload-joins)
         (when-not
          (and (vector? (:checker-identity-raw-sha256s payload-joins))
               (= 2 (count (:checker-identity-raw-sha256s payload-joins)))
               (every? sha256-id? (:checker-identity-raw-sha256s payload-joins))
               (vector? (:checker-output-raw-sha256s payload-joins))
               (= 2 (count (:checker-output-raw-sha256s payload-joins)))
               (every? sha256-id? (:checker-output-raw-sha256s payload-joins))
               (sha256-id? (:certificate-pair-raw-sha256 payload-joins))
               (sha256-id? (:linked-carrier-raw-sha256 payload-joins))
               (= (:coordinator-event-root receipt)
                  (codec/H [:gravity/sh07-b51-coordinator-event-root-v1
                            events])))
           (fail! "SH07-B51-RECEIPT-JOIN"
                  :runtime-identity-process-joins))
         (reject-runtime-back-edges! events [:coordinator-events])
         (reject-runtime-back-edges! children [:child-process-receipts])
         (reject-runtime-back-edges! (:launcher-receipt receipt)
                                     [:launcher-receipt])
         (reject-runtime-back-edges! (:coordinator-receipt receipt)
                                     [:coordinator-receipt])
         (reject-runtime-back-edges! payload-joins [:payload-joins]))
       (when-not (and (map? eligibility) (map? discharge))
         (fail! "SH07-B51-RECEIPT-JOIN"
                :runtime-identity-missing-joins))
       (when-not (and (map? q-receipt) (vector? participant-results))
         (fail! "SH07-B51-RECEIPT-JOIN"
                :runtime-identity-missing-q-joins))
       (validate-evidence-eligibility!
        eligibility {:content content
                     :receipt q-receipt
                     :participant-results participant-results})
       (validate-runtime-contract-discharge! discharge)
       (let [carriers (get-in discharge [:evidence :carriers])
             expected-id
             (codec/H
              [:gravity/sh07-b51-checker-process-identity-receipt-id-v1
               (dissoc receipt :receipt-id)])]
         (when-not
          (and (= (:pre-discharge-id eligibility)
                  (:pre-discharge-id receipt))
               (= (:pre-discharge-id eligibility)
                  (get-in discharge [:prior-ref :discharge-id]))
               (= eligibility (:evidence-eligibility carriers))
               (= content (:content carriers))
               (= identity-receipt-normalization
                  (:receipt-id-normalization receipt))
               (= expected-id (:receipt-id receipt)))
           (fail! "SH07-B51-RECEIPT-JOIN"
                  :runtime-identity-join))
         ;; The enclosing discharge and its prior-ref are required above, but
         ;; only the non-enclosing carrier values are traversed here.  This
         ;; keeps their required Q prior-ref/discharge IDs legal while making
         ;; E/B/G/P and enclosing identity/discharge back-edges impossible.
         (reject-runtime-back-edges! eligibility [:evidence-eligibility])
         (reject-runtime-back-edges!
          carriers [:runtime-contract-discharge :evidence :carriers])
         receipt)))))

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
