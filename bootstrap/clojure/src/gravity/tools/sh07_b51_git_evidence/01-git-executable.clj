
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
   "bootstrap/gravity/src/gravity/sh07_b51/producer_b.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/producer_c.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/producer_d.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/root6_b.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/root8.gravity"])

(def owned-c-paths
  (into ["bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00580-p15-s23-stage2-compiler-artifact-expected-artifact-hash.clj"
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
   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt30_independent_census.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt30_producer_census.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_child.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_identity.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_coordinator_child.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_materialize.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_tier0_materializer.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_git_evidence.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_v9_codec.clj"
   "bootstrap/clojure/test/gravity/self_hosting/fixtures/sh07-b51-git-object-v9/accepted.edn"
   "bootstrap/clojure/test/gravity/self_hosting/fixtures/sh07-b51-git-object-v9/rejected.edn"
   "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt30_independent_census_test.clj"
   "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt30_linked_carrier_host_test.clj"
   "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt30_producer_census_test.clj"
   "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt30_producer_compact_verifier_test.clj"
   "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_attempt30_root6_compact_verifier_test.clj"
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
   "bootstrap/clojure/test/gravity/self_hosting/sh07_b51_v9_source_closure_test.clj"
   "bootstrap/gravity/src/gravity/sh07_b51/producer_a.gravity"
   "bootstrap/gravity/src/gravity/sh07_b51/root6_a.gravity"] owned-c-fragment-paths))

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
    (when-not (and (= 204 (count paths)) (= 204 (count (distinct paths))))
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
