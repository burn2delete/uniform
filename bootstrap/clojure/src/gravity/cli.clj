(ns gravity.cli
  "Deterministic bootstrap CLI presentation and selected Core arguments.

  Core argument normalization returns a path/options value after command
  selection. This namespace does not observe host state, select or run
  commands, authenticate or render diagnostics, choose exit codes, or
  terminate the process. Those responsibilities remain at their existing
  bootstrap boundaries until their owning stages are extracted.")

(def ^:private namespace-contract
  {:namespace 'gravity.cli
   :contract-boundary :bootstrap-cli-presentation-and-core-arguments
   :governing-contracts '[D1 D2 D3 D8 D9 L1 L2 L3 C1 C6 C15 T1 BOOT1 BOOT8 TEST2]
   :public-api
   {'p18-cli-version-record
    {:arglists '([packaged-jvm-cli?])
     :returns :structured-version-record}
    'p18-cli-help-text
    {:arglists '([packaged-jvm-cli?])
     :returns :human-help-text}
    'cli-usage-text
    {:arglists '([])
     :returns :usage-text}
    'sh07-core-input
    {:arglists '([arguments])
     :command-selection :already-selected
     :returns :path-and-reader-options
     :diagnostics ["SH07-CLI-ARGS"]
     :admission :none}}
   :artifact-inputs [:packaged-jvm-cli-state :selected-sh07-command-arguments]
   :artifact-outputs [:bootstrap-version-record :bootstrap-help-text
                      :bootstrap-usage-text :sh07-core-input]
   :diagnostic-ownership {:stage ["SH07-CLI-ARGS"]
                          :source-decoding :reader :core-legality :checked-core
                          :rendering :existing-diagnostic-presentation}
   :ownership
   {:owns [:deterministic-bootstrap-version-presentation
           :deterministic-bootstrap-help-presentation
           :deterministic-bootstrap-usage-presentation
           :sh07-core-argument-normalization]
    :does-not-own
    [:host-state-observation :other-command-argument-normalization :command-dispatch
     :compiler-semantics :diagnostic-authenticity :diagnostic-rendering
     :exit-code-selection :system-exit :java-entrypoint :clojure-entrypoint
     :release-claims]}
   :dependency-direction
   {:requires ['clojure.core]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :bootstrap-hosted? true
   :production-t1-cli-conformance? false
   :full-t1-command-surface? false
   :seedless-release? false
   :self-hosted? false})

(def ^:private experimental-c-route-version-record
  {:status :implementation-present-public-exposure-disabled
   :experiment-state :proposed
   :exposure :internal-only
   :public-command-route? false
   :excluded-from-executable-command-contract-credit? true
   :governance-conforming? false
   :t1-cli-conformance? false
   :p18-t04-proof-credited? false
   :public-target-support-claim? false})

(def ^:private experimental-c-route-help
  (str
   "  :experimental-verified-mir-c-route "
   "\"implementation present; public exposure disabled pending "
   "feature-specific GOV4, GOV9, and GOV7 records; exact requests reject "
   "before source-file I/O, output-filesystem I/O, native calls, tool "
   "execution, or publication\"\n"))

(defn p18-cli-version-record
  [packaged-jvm-cli?]
  {:command "gravity"
   :phase (if packaged-jvm-cli? "P18-T02" "P18-T01")
   :bootstrap-hosted? true
   :packaged-jvm-cli? packaged-jvm-cli?
   :seedless-release? false
   :executable-command-contract? true
   :executable-command-contract-scope :established-bootstrap-subset
   :experimental-verified-mir-c-route experimental-c-route-version-record
   :delegates-to
   (if packaged-jvm-cli?
     "java -cp target/phase-18/jvm-cli/gravity-jvm-cli.jar gravity.cli.Main"
     "clojure -M:gravity")
   :replaced-by "Phase 18 self-hosted release artifact"})

(defn p18-cli-help-text
  [packaged-jvm-cli?]
  (str
   "gravity bootstrap-hosted command\n\n"
   "Usage:\n"
   "  gravity --version\n"
   "  gravity help\n"
   "  gravity check <file.qst|file.gravity>\n"
   "  gravity sh07-core <file.qst|file.gravity>\n"
   "  gravity run <file.qst|file.gravity>\n"
   "  gravity compile <file.qst|file.gravity>\n"
   "  gravity compile <file.qst|file.gravity> -o <executable>\n"
   "  gravity build --reader-inputs EDN --principal PACKAGE --profile meta --target jvm\n"
   "    --allow-read KEY --package-policy EDN --workspace-policy EDN --format json\n"
   "    (local hermetic input entry; effectful Reader bridge unavailable)\n"
   "  gravity test\n"
   "  gravity p18-t05-seedless-release-boundary\n"
   "  gravity p18-t05-write-seedless-release-artifacts\n"
   "  gravity p18-t06-final-release\n"
   "  gravity p18-t06-write-final-release-artifacts\n"
   "  gravity p18-t00-co-canonical-source-extensions\n"
   "  gravity p18-t00-write-co-canonical-source-extension-artifacts\n"
   "  gravity <artifact-command> <file.qst|file.gravity>\n\n"
   "Metadata:\n"
   "  :bootstrap-hosted? true\n"
   "  :packaged-jvm-cli? "
   (if packaged-jvm-cli? "true" "false")
   "\n"
   "  :seedless-release? false\n"
   experimental-c-route-help))

(defn cli-usage-text
  "Return the stable usage text for a command-line shape failure.

  Usage presentation is complete without compiler operations. Command
  dispatch and diagnostic rendering remain at their existing boundaries."
  []
  (str
   "usage: clojure -M:gravity <read|module|macro|core|sh07-core|typed|safety|memory-safety|unsafe-audit|boundary-safety|capability-supply-chain|safety-conformance|profile-manifest|profile-set|profile-validation|profile-distributed-ai|profile-compatibility|profile-compliance|performance|zero-cost|specialization|layout|performance-governance|realtime-governance|numeric-modes|efir|eml|approximation|math-proof|math-conformance|compiler-passes|checked-core|mir|domain-ir|optimize-lower|compiler-verify|compiler-c1-architecture|compiler-c2-reader|compiler-c3-syntax|compiler-c4-macro|compiler-c5-resolution|compiler-c6-lowering|compiler-c7-type-check|compiler-c8-effect-check|compiler-c9-ownership-check|compiler-c10-safety-analysis|compiler-c11-mir-spec|compiler-c12-domain-ir|compiler-c13-optimization|compiler-c14-lowering|compiler-c15-diagnostics|compiler-c16-incremental|compiler-c17-plugin|compiler-c18-verification|backend-interface|native-lowering|hosted-lowering|specialized-lowering|artifact-emission|backend-test-matrix|backend-b1-document|backend-b2-c-document|backend-b3-llvm-document|backend-b4-wasm-document|backend-b5-jvm-document|backend-b6-js-ts-document|backend-b7-mlir-document|backend-b8-gpu-document|backend-b9-hdl-document|backend-b10-workflow-document|backend-b11-query-document|backend-b12-mobile-document|backend-b13-artifact-document|backend-b14-conformance-document|runtime-selection|runtime-minimal-native|runtime-managed|runtime-concurrency|runtime-ai-ffi|runtime-observability|runtime-r1-document|runtime-r2-document|runtime-r3-document|runtime-r4-document|runtime-r5-document|runtime-r6-document|runtime-r7-document|runtime-r8-document|runtime-r9-document|runtime-r10-document|runtime-r11-document|runtime-r12-document|domain-coverage|schema-interop|ai-agentic|package-artifacts|tooling-experience|conformance-system|bootstrap-self-hosting|stage1-bootstrap-source|stage1-reader-execute|stage1-reader-algorithm|stage1-reader-pipeline|stage1-reader-character-pipeline|stage1-reader-token-classifier-pipeline|stage1-reader-token-realizer-pipeline|stage1-reader-token-automaton-pipeline|stage1-reader-form-builder-pipeline|stage1-reader-executor-pipeline|stage1-reader-runtime-pipeline|stage1-reader-compiled-pipeline|stage1-reader-binary-pipeline|stage1-reader-self-hosted-runtime|stage1-reader-core-bootstrap|stage1-reader-compiler-driver|stage1-reader-runtime-entrypoint|stage1-reader-runtime-image|stage1-reader-verified-boot-chain|stage1-reader-diverse-bootstrap-verification|stage1-reader-release-attestation-seed-retirement|stage1-reader-formal-release-governance-seed-retirement|p15-s23-whole-language-self-hosting-gate|p15-s23-compiler-source-inventory|p15-s23-compiler-pipeline-manifest|p15-s23-source-syntax-serialization-proof|p15-s23-core-lowering-diagnostic-preservation|p15-s23-runtime-manifest-capability-enforcement|p15-s23-accepted-app-execution|p15-s23-rejected-app-diagnostic|p15-s23-reproducible-rebuild-log|p15-s23-stage-comparison-report|p15-s23-self-hosting-conformance-report|p15-s23-provenance-attestation|p15-s23-tcb-delta-record|p15-s23-unsafe-audit-report|p15-s23-whole-language-compiler-artifact|p15-s23-governance-and-package-release-record|p15-s23-stage2-compiler-nucleus|p15-s23-stage2-plan-emitter|p15-s23-stage2-runtime-kernel|p15-s23-stage2-runtime-executor|p15-s23-stage2-front-end-executor|p15-s23-stage2-source-front-end|p15-s23-stage2-compiler-driver|p15-s23-stage2-whole-language-compiler|p15-s23-stage3-seedless-compiler-candidate|p15-s23-stage3-equivalence-bundle|p15-s23-stage3-self-hosted-application|p15-s23-final-seed-retirement-proof|p18-t00-co-canonical-source-extensions|p18-t00-write-co-canonical-source-extension-artifacts|p18-t01-thin-cli-wrapper|p18-t02-packaged-jvm-cli|p18-t02-write-packaged-jvm-cli-artifacts|p18-t04-public-test-command|p18-t04-write-public-test-command-artifacts|standard-library|governance-evolution|hosted-core-app|hosted-core-compiled-app|hosted-core-compiled-safety|hosted-core-compiled-profile|hosted-core-compiled-performance|hosted-core-compiled-math|hosted-core-compiled-compiler|hosted-core-compiled-backend|hosted-core-compiled-runtime|hosted-core-compiled-domain|hosted-core-compiled-schema|hosted-core-compiled-ai|hosted-core-compiled-package|hosted-core-compiled-tooling|hosted-core-compiled-conformance|test|check|compile|run|run-compiled> path/to/file.qst|path/to/file.gravity"))

(defn sh07-core-input
  "Normalize the already selected Core command's arguments without source IO."
  [arguments]
  (let [[_ path & flags] arguments]
    (when-not (and (string? path) (not (.startsWith ^String path "-"))
                   (or (empty? flags) (= ["--development-current-reader"] (vec flags))))
      (throw (ex-info "usage: gravity sh07-core FILE [--development-current-reader]"
                      {:id "SH07-CLI-ARGS" :stage :command-line :severity :error})))
    [path (if (empty? flags) {} {:reader :development-current})]))

(alter-meta! #'sh07-core-input assoc :gravity/namespace-contract namespace-contract)
