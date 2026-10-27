(ns sh-crosswalk
  "Source-bound SH routing projection. Task status is read only from component ledgers."
  (:require [clojure.edn :as edn] [clojure.string :as str] [clojure.pprint :as pp] [clojure.java.io :as io]))
(binding [*command-line-args* nil] (load-file "tools/sh_inventory.clj") (load-file "tools/acceptance_dependencies.clj"))
(def baseline "254a5b50ea5e2825c424248c9216802cc866c0fd")
(def original-baseline "15a5499e97798621d270c36dd2546ce98ddd62a9")
(def output-path "specs/sh-family-crosswalk/crosswalk.edn")
(def rollup-path "specs/sh-family-crosswalk/rollup.md")
(def sh07-path "docs/self-hosting-sh07-completion-plan.md")
(def original-sh07-sha256 "dc43b208677945318effa93734449716785b27b5737256279b1c75a9195ae17e")
(def audit-path "docs/self-hosting-sh00-sh06-evidence-audit.md")
(def p15-whole-gate "docs/artifacts/phase-15/bootstrap/p15-s23-whole-language-self-hosting-gate.edn")
(def p15-final-proof "docs/artifacts/phase-15/bootstrap/p15-s23-final-seed-retirement-proof.edn")
(def release-flags [:status :task :full-language-compiler-self-hosted? :clojure-seed-retired? :clojure-seed-boundary?])
(def legacy-accounting-facet-ids #{"SH-02.5" "SH-03.4" "SH-04.5" "SH-05.5" "SH-16.6"})

;; Integers below select pinned literal IDs, never live document/display order.
;; The mapping keys are local routing shorthand, not tasks or completion units.
(def stable-task-routing-keys
  {"auth"
 ["auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection"
  "auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime"
  "auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks"
  "auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity"
  "auth/remaining-tasks-and-evidence/publish-conformance-and-remaining-trust-assumptions"],
 "backends"
 ["backend-interface"
  "backend-c"
  "backend-llvm"
  "backend-wasm"
  "backend-jvm"
  "backend-js"
  "backend-mlir"
  "backend-gpu"
  "backend-hdl"
  "backend-workflow"
  "backend-ai"
  "backend-query"
  "backend-mobile"
  "backend-emission"
  "backend-conformance"],
 "build"
 ["build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution"
  "build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects"
  "build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation"
  "build/remaining-tasks-and-evidence/produce-and-verify-packageprovenancesigning-records"
  "build/remaining-tasks-and-evidence/close-reproducibility-and-target-matrix"],
 "cli"
 ["cli/remaining-tasks-and-evidence/reconcile-supported-commandoptions-with-normative-families"
  "cli/remaining-tasks-and-evidence/connect-checkruncompilebuildtestpackageverify-to-real-services"
  "cli/remaining-tasks-and-evidence/connect-replformatlintdocinspectprofileauditairegistryexplain-services"
  "cli/remaining-tasks-and-evidence/normalize-json-text-exit-codes-and-redaction"
  "cli/remaining-tasks-and-evidence/package-and-verify-user-entrypoints"],
 "compiler-api"
 ["pipeline" "plugins" "pass-evidence" "development-boundaries"],
 "core"
 ["core/current-evidence-and-remaining-work/core1-lower-core-forms"
  "core/current-evidence-and-remaining-work/core2-preserve-binding-structure"
  "core/current-evidence-and-remaining-work/core3-desugar-without-semantic-loss"
  "core/current-evidence-and-remaining-work/core4-verify-and-serialize"
  "core/current-evidence-and-remaining-work/core5-integrate-semantic-analyses"],
 "diag"
 ["diag/remaining-tasks-and-evidence/reconcile-stage-carriers-with-canonical-record-schema"
  "diag/remaining-tasks-and-evidence/preserve-primaryrelatedgenerated-origins-and-stable-order"
  "diag/remaining-tasks-and-evidence/close-textjsonredaction-and-explain-contracts"
  "diag/remaining-tasks-and-evidence/integrate-runtimebuildeditor-consumers-without-rule-drift"],
 "domains" ["math" "schema" "query" "workflow" "ai" "gpu" "ui"],
 "effects"
 ["effects/current-evidence-and-remaining-work/effects1-infer-direct-effects"
  "effects/current-evidence-and-remaining-work/effects2-propagate-latent-effects"
  "effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering"
  "effects/current-evidence-and-remaining-work/effects4-record-replay-and-invalidation"
  "effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers"],
 "emit"
 ["emit/current-evidence-and-remaining-work/emit1-reconcile-selected-target-contracts"
  "emit/current-evidence-and-remaining-work/emit2-emit-real-outputs"
  "emit/current-evidence-and-remaining-work/emit3-bind-provenance-to-bytes"
  "emit/current-evidence-and-remaining-work/emit4-establish-conformance"
  "emit/current-evidence-and-remaining-work/emit5-integrate-artifact-consumers"],
 "expand"
 ["expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros"
  "expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety"
  "expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects"
  "expand/current-evidence-and-remaining-work/expand4-validate-reuse-and-termination"
  "expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs"],
 "legality"
 ["legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks"
  "legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis"
  "legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries"
  "legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution"
  "legality/remaining-tasks-and-evidence/close-conformance-for-the-declared-profile-surface"],
 "lower"
 ["lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs"
  "lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout"
  "lower/current-evidence-and-remaining-work/lower3-select-explicit-providers"
  "lower/current-evidence-and-remaining-work/lower4-preserve-proofs-and-origins"
  "lower/current-evidence-and-remaining-work/lower5-integrate-target-emission"],
 "mir"
 ["mir/current-evidence-and-remaining-work/mir1-construct-semantic-mir"
  "mir/current-evidence-and-remaining-work/mir2-attach-fact-tables"
  "mir/current-evidence-and-remaining-work/mir3-verify-graph-and-legality"
  "mir/current-evidence-and-remaining-work/mir4-serialize-stable-modules"
  "mir/current-evidence-and-remaining-work/mir5-gate-downstream-consumption"],
 "opt"
 ["opt/current-evidence-and-remaining-work/opt1-execute-pass-contracts"
  "opt/current-evidence-and-remaining-work/opt2-implement-structural-families"
  "opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors"
  "opt/current-evidence-and-remaining-work/opt4-verify-and-regenerate"
  "opt/current-evidence-and-remaining-work/opt5-integrate-reproducible-pipeline"],
 "platform" ["hardware" "firmware" "kernel" "devices"],
 "reader"
 ["reader/finite-remaining-work/contract-coverage-reconciliation"
  "reader/finite-remaining-work/decode-and-source-identity-closure"
  "reader/finite-remaining-work/lexical-products-and-origins-closure"
  "reader/finite-remaining-work/extension-policy-closure"
  "reader/finite-remaining-work/authentication-and-quota-removal-closure"
  "reader/finite-remaining-work/fresh-syntax-handoff"
  "reader/finite-remaining-work/component-release-evidence"],
 "release"
 ["release/remaining-tasks-and-evidence/bind-each-claimed-component-to-exact-implementation-and-conformance"
  "release/remaining-tasks-and-evidence/complete-stage-compatibilityequivalence-across-the-claimed-compiler"
  "release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution"
  "release/remaining-tasks-and-evidence/build-reproducible-signed-distributions-with-provenancesbom"
  "release/remaining-tasks-and-evidence/obtain-independent-acceptance-for-the-exact-release-candidate"],
 "resolve"
 ["resolve/current-evidence-and-remaining-work/resolve1-resolve-lexical-positions"
  "resolve/current-evidence-and-remaining-work/resolve2-validate-module-boundaries"
  "resolve/current-evidence-and-remaining-work/resolve3-check-graph-legality"
  "resolve/current-evidence-and-remaining-work/resolve4-publish-stable-indexes"
  "resolve/current-evidence-and-remaining-work/resolve5-integrate-binding-consumers"],
 "runtime"
 ["runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection"
  "runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup"
  "runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries"
  "runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution"
  "runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration"],
 "stdlib"
 ["stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations"
  "stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis"
  "stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis"
  "stdlib/remaining-tasks-and-evidence/complete-schemaqueryworkflowaimetacryptoui-adapters"
  "stdlib/remaining-tasks-and-evidence/integrate-platform-modules-and-close-release-manifests"],
 "syntax"
 ["syntax/current-evidence-and-remaining-work/syntax1-construct-canonical-syntax"
  "syntax/current-evidence-and-remaining-work/syntax2-preserve-transformations"
  "syntax/current-evidence-and-remaining-work/syntax3-invalidate-facts"
  "syntax/current-evidence-and-remaining-work/syntax4-serialize-and-verify"
  "syntax/current-evidence-and-remaining-work/syntax5-integrate-consumers"],
 "tools"
 ["tools/finite-service-outcomes-and-remaining-work/format-formatter-maintainer"
  "tools/finite-service-outcomes-and-remaining-work/lint-analysis-tool-maintainer"
  "tools/finite-service-outcomes-and-remaining-work/language-server-editor-maintainer"
  "tools/finite-service-outcomes-and-remaining-work/debug-debugger-maintainer"
  "tools/finite-service-outcomes-and-remaining-work/repl-interactive-runtime-tooling-maintainer"
  "tools/finite-service-outcomes-and-remaining-work/ir-explorer-compiler-tool-maintainer"
  "tools/finite-service-outcomes-and-remaining-work/profile-performance-tool-maintainer"
  "tools/finite-service-outcomes-and-remaining-work/docs-documentation-tool-maintainer"
  "tools/finite-service-outcomes-and-remaining-work/dev-server-development-session-maintainer"
  "tools/finite-service-outcomes-and-remaining-work/registry-view-package-tool-maintainer"
  "tools/finite-service-outcomes-and-remaining-work/safety-explorer-safety-tool-maintainer"
  "tools/finite-service-outcomes-and-remaining-work/ai-development-ai-tool-maintainer"],
 "types"
 ["types/current-evidence-and-remaining-work/types1-solve-core-constraints"
  "types/current-evidence-and-remaining-work/types2-check-declared-abstractions"
  "types/current-evidence-and-remaining-work/types3-make-dynamic-boundaries-explicit"
  "types/current-evidence-and-remaining-work/types4-preserve-and-invalidate-facts"
  "types/current-evidence-and-remaining-work/types5-integrate-typed-core"]}
)

(def routes
  [[[["mir" 1] ["lower" 1]] [["backends" "backend-wasm"] ["emit" 2]] [["backends" "backend-conformance"] ["emit" 4]] [["lower" 1] ["backends" "backend-interface"]] [["compiler-api" "pass-evidence"]] [["release" 5]]]
   [[["compiler-api" "development-boundaries"]] [["compiler-api" "development-boundaries"]] [["compiler-api" "development-boundaries"]] [["compiler-api" "development-boundaries"]] [["compiler-api" "development-boundaries"]] [["compiler-api" "development-boundaries"]]]
   [[["auth" 4] ["compiler-api" "pipeline"]] [["auth" 4]] [["auth" 4]] [["auth" 4]] [["auth" 4]] [["auth" 4] ["compiler-api" "pass-evidence"]]]
   [[["reader" 2] ["reader" 3]] [["reader" 3]] [["reader" 4] ["reader" 3]] [["reader" 2] ["reader" 3]] [["reader" 6]] [["reader" 1] ["reader" 7]]]
   [[["syntax" 1]] [["syntax" 2]] [["syntax" 2] ["syntax" 4]] [["syntax" 4]] [["syntax" 4] ["auth" 4]] [["syntax" 5]]]
   [[["expand" 1]] [["expand" 2]] [["expand" 1] ["expand" 5]] [["expand" 3] ["auth" 2]] [["expand" 4]] [["expand" 5] ["compiler-api" "pass-evidence"]]]
   [[["resolve" 2] ["resolve" 3]] [["resolve" 1]] [["resolve" 1]] [["resolve" 2] ["resolve" 3]] [["resolve" 4] ["resolve" 5]] [["resolve" 5]]]
   [[["core" 1] ["core" 2]] [["core" 1] ["core" 3]] [["core" 1] ["core" 2]] [["core" 1] ["core" 3]] [["core" 1] ["core" 2]] [["core" 4] ["core" 5] ["compiler-api" "pass-evidence"]]]
   [[["types" 1]] [["types" 1] ["types" 2]] [["types" 1] ["types" 2]] [["types" 1] ["types" 2]] [["types" 2] ["types" 3] ["types" 4]] [["types" 5]]]
   [[["effects" 1] ["effects" 2]] [["effects" 2] ["effects" 3]] [["auth" 1] ["auth" 2]] [["legality" 1] ["effects" 3]] [["effects" 4] ["auth" 3]] [["effects" 5] ["legality" 5]]]
   [[["legality" 2]] [["legality" 2]] [["legality" 2]] [["legality" 2]] [["legality" 2] ["legality" 4]] [["legality" 4] ["mir" 2]]]
   [[["legality" 3]] [["legality" 3]] [["legality" 3]] [["legality" 4]] [["legality" 3] ["auth" 2]] [["legality" 4] ["mir" 2]]]
   [[["mir" 1]] [["mir" 2]] [["mir" 3]] [["mir" 3] ["mir" 5]] [["mir" 4]] [["mir" 5]]]
   [[["mir" 1] ["runtime" 2]] [["mir" 1] ["runtime" 2]] [["mir" 1] ["mir" 3] ["emit" 4] ["runtime" 2]] [["mir" 1] ["runtime" 2]] [["mir" 5] ["emit" 4] ["runtime" 2]] [["mir" 5] ["emit" 4] ["runtime" 2]]]
   [[["mir" 1] ["stdlib" 2]] [["mir" 1] ["stdlib" 2]] [["lower" 2] ["mir" 1]] [["runtime" 3] ["legality" 2]] [["runtime" 3] ["lower" 2]] [["lower" 2] ["emit" 4] ["runtime" 3]]]
   [[["diag" 1]] [["diag" 2]] [["diag" 1] ["diag" 3]] [["diag" 4] ["cli" 4]] [["diag" 3]] [["diag" 1] ["diag" 2] ["release" 2]]]
   [[["opt" 1]] [["opt" 1] ["opt" 2]] [["opt" 4]] [["opt" 3]] [["opt" 4] ["compiler-api" "pass-evidence"]] [["opt" 5] ["lower" 1]]]
   [[["lower" 1]] [["lower" 1] ["emit" 1]] [["lower" 2] ["lower" 3]] [["lower" 4]] [["lower" 4] ["emit" 3]] [["lower" 5] ["backends" "backend-interface"]]]
   [[["emit" 2] ["backends" "backend-llvm"]] [["backends" "backend-llvm"] ["runtime" 2] ["stdlib" 2]] [["emit" 2] ["backends" "backend-llvm"]] [["emit" 4] ["backends" "backend-conformance"]] [["emit" 4] ["lower" 1]] [["emit" 3] ["compiler-api" "pass-evidence"]]]
   [[["runtime" 2]] [["runtime" 3]] [["runtime" 2] ["auth" 3] ["stdlib" 3]] [["runtime" 2] ["runtime" 5]] [["runtime" 1] ["auth" 3]] [["runtime" 5] ["auth" 5]]]
   [[["emit" 2]] [["emit" 3]] [["emit" 3] ["build" 4]] [["emit" 3] ["emit" 5]] [["emit" 5] ["build" 4]] [["build" 4] ["release" 4]]]
   [[["compiler-api" "pipeline"] ["release" 1]] [["legality" 1] ["legality" 2] ["legality" 3] ["types" 5] ["effects" 5]] [["auth" 2] ["effects" 3]] [["expand" 5] ["compiler-api" "pipeline"]] [["auth" 2] ["auth" 3]] [["compiler-api" "pass-evidence"] ["legality" 5]]]
   [[["stdlib" 1]] [["stdlib" 2]] [["stdlib" 2]] [["stdlib" 2] ["stdlib" 4]] [["stdlib" 3] ["auth" 3]] [["stdlib" 1] ["stdlib" 2] ["stdlib" 3]]]
   [[["build" 1]] [["build" 1] ["build" 2]] [["build" 2]] [["build" 2] ["auth" 3]] [["build" 3] ["build" 4]] [["build" 2] ["release" 3]]]
   [[["cli" 1] ["cli" 5]] [["cli" 2] ["compiler-api" "pipeline"]] [["cli" 2]] [["cli" 4]] [["cli" 5] ["reader" 2]] [["cli" 2] ["cli" 5] ["release" 3]]]
   [[["release" 1]] [["release" 1] ["compiler-api" "pipeline"]] [["release" 1] ["build" 2]] [["build" 2] ["release" 3]] [["release" 1] ["auth" 4]] [["release" 3] ["compiler-api" "pass-evidence"]]]
   [[["release" 3] ["build" 5]] [["release" 3] ["build" 2]] [["release" 3]] [["release" 3] ["cli" 2]] [["release" 3] ["auth" 3]] [["release" 3] ["emit" 3]]]
   [[["release" 2] ["build" 5]] [["release" 2] ["emit" 3]] [["release" 2] ["diag" 1]] [["release" 2] ["runtime" 2]] [["release" 2] ["compiler-api" "pass-evidence"]] [["release" 4] ["build" 5]]]
   [[["release" 4] ["build" 5]] [["release" 4] ["auth" 4]] [["release" 4] ["build" 4]] [["release" 4] ["auth" 2]] [["release" 5] ["compiler-api" "pass-evidence"]] [["release" 3] ["release" 5]]]
   [[["release" 3] ["cli" 5]] [["release" 3] ["build" 2]] [["release" 1] ["cli" 2]] [["release" 4]] [["release" 5] ["cli" 5]] [["release" 5]]]])

(def historical-commits
  ["dbd7764ca648d808e6fe6c4af971303152bdd7ea" "cbc55cd1477deca08a9c042c91db98005833021a"
   "914215e3b99822e4c4ef48e004c26bdf3e075216" "eeb9253dff4c7e7d4fb0152b7fa1a720b56e052d"
   "64f7b968b577013e47ae7d02bad3e39748b4250d" "bd8384b5344d9d99626e698976ee6e8a190859e5"
   "8f4b91d3efc4c4f42442002001707b89ca388025"])

(defn span-matches [path pattern]
  (let [text (slurp path) m (re-matcher pattern text)]
    (loop [rows []] (if (.find m)
                     (recur (conj rows {:groups (mapv #(.group m %) (range 1 (inc (.groupCount m))))
                                        :source (sh-inventory/source-span path text (.start m) (.end m))})) rows))))
(defn task-records [component]
  (let [path (str "specs/" component "-workflow/tasks.md") text (slurp path)
        marker (str "<!-- " component "-implementation-tasks:")
        start (+ (.indexOf text (str marker "start -->")) (count (str marker "start -->")))
        end (.indexOf text (str marker "end -->"))
        _ (acceptance-dependencies/implementation-ids text component)
        m (re-matcher #"(?m)^- \[([ xX])\] `([^`]+)`[^\n]*" text)
        positions (loop [v []] (if (.find m) (recur (if (< start (.start m) end)
                   (conj v {:start (.start m) :id (.group m 2) :checked (not= " " (.group m 1))}) v)) v))]
    (mapv (fn [row next-row]
            (let [stop (or (:start next-row) end) raw (str/trimr (subs text (:start row) stop))]
              {:id (:id row) :component component :path path :checked (:checked row)
               :authority :component-implementation-ledger
               :source-commit baseline :file-sha256 (sh-inventory/sha256 text)
               :source (sh-inventory/source-span path text (:start row) (+ (:start row) (count raw)))
               :owner-source (str "docs/component-roadmaps/" component ".md")}))
          positions (concat (rest positions) [nil]))))
(defn task-ref [tasks [component selector]]
  (let [id (if (integer? selector) (get-in stable-task-routing-keys [component (dec selector)]) selector)
        row (first (filter #(= id (:id %)) (tasks component)))]
    (when-not row (throw (ex-info "Unknown task route" {:component component :selector selector})))
    (select-keys row [:id :component :path])))
(defn review-fingerprint [result]
  (sh-inventory/sha256
    (pr-str [(dissoc result :requirements :source-digests)
             (mapv #(dissoc % :mapping-review :coverage) (:requirements result))
             (dissoc (:source-digests result) "docs/acceptance-dependencies.md")
             (sh-inventory/sha256 (slurp "tools/acceptance_dependencies.clj"))])))
(def mapping-review
  {:status :accepted-mapping-only
   :reviewer "01a17a6d-8d52-7002-999b-89d8cdd34008"
   :reviewed-commit "c6c80530dd2b36a9b66ffd1a2a9042f0f43b4f36"
   :reviewed-tree "1229798edab46b3774dbab18c1c555c043eafa55"
   :receipt-sha256 "39e91ae6270c92ad789e15b635e7dbaf8890726ea2dd7c432ec019077375037c"
   :review-text-sha256 "1269e58c6d1dc859f3d064bb8c3342a1d82061a26acc8252af7d32cc6e046942"
   :input-fingerprint "cdca8f11a6ad11d3917646a932235a302f3356860ab5c559e2e4dd12166c4c5e"
   :qualification "Independent acceptance of all180 mappings/18sequence rows and graph only. No implementation acceptance, global requirement denominator or rate."})
(defn source-view []
  (let [inventory (edn/read-string (slurp "specs/sh-family-crosswalk/original-inventory.edn"))
        _ (when-not (= original-sh07-sha256 (sh-inventory/sha256 (slurp sh07-path)))
            (throw (ex-info "SH07 source changed; preserve lineage before amending sequence" {})))
        _ (when-not (= (:requirements inventory) (sh-inventory/inventory (slurp sh-inventory/plan-path)))
            (throw (ex-info "Original source changed; retain lineage before rebinding" {})))
        components (mapv second (re-seq #"(?m)^\| [^|\n]+ / `([a-z-]+)` \|" (slurp "docs/component-roadmaps/README.md")))
        tasks (into (sorted-map) (map (fn [c] [c (task-records c)]) components))
        _ (doseq [[c rows] tasks]
            (when-not (= (set (map :id rows)) (set (stable-task-routing-keys c)))
              (throw (ex-info "Task identity population changed; preserve source/ownership routing before amendment" {:component c}))))
        test-files (group-by #(.getName %) (filter #(.isFile %) (file-seq (io/file "bootstrap/clojure/test"))))
        locator (memoize (fn [path] {:path path :sha256 (sh-inventory/sha256 (slurp path))
                                    :source-commit baseline :kind :source-inspection-not-execution
                                    :current-applicability :unassessed}))
        sh-graph (acceptance-dependencies/milestone-graph (slurp sh-inventory/plan-path))
        historical (into {} (map (fn [r] [(first (:groups r)) r])
                          (span-matches audit-path #"(?m)^\| (SH-\d{2}\.[1-6]) \| [^\n]+$")))
        sections (into {} (map (fn [r] [(first (:groups r)) (:source r)])
                             (span-matches sh-inventory/plan-path #"(?ms)^## (SH-\d{2}) - [^\n]+\n.*?(?=^## |\z)")))
        crows (span-matches sh07-path #"(?m)^\| (SH07-C\d{2}) \| ([^|]+) \| ([^|]+) \| ([^|]+) \| ([^\n]+)\|$")
        sources (distinct (concat [sh-inventory/plan-path sh07-path audit-path p15-whole-gate p15-final-proof
                                    "docs/phase-15-bootstrap-and-self-hosting/IMPLEMENTATION-ROADMAP.md"
                                    "docs/artifacts/phase-18/self-hosted/p18-t03-self-hosted-release-artifact-proof.edn"
                                    "docs/artifacts/phase-06/reports/current-reader-fresh-syntax-handoff.md"
                                    "docs/self-hosting-slice-backlog.md" "docs/self-hosting-current-migration-cohort.md"
                                    "docs/self-hosting-slice-ownership.edn" "docs/document-inventory.json"
                                    "docs/acceptance-dependencies.md" "docs/component-roadmaps/README.md"
                                    "docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md"
                                    "docs/phase-06-compiler-architecture/080-c1-compiler-architecture-overview.md"
                                    "docs/phase-06-compiler-architecture/084-c5-name-resolution-and-namespace-analyzer-design.md"
                                    "docs/phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md"
                                    "docs/phase-18-binary-distribution-and-seedless-release/README.md"
                                    "docs/phase-18-binary-distribution-and-seedless-release/IMPLEMENTATION-ROADMAP.md"]
                                  (mapcat (fn [[c rows]] (conj (mapv :path rows) (str "docs/component-roadmaps/" c ".md"))) tasks)))
        requirements
        (mapv (fn [{:keys [id milestone source]}]
                (let [n (Long/parseLong (subs milestone 3)) r (dec (Long/parseLong (subs id 6)))
                      hs (get historical id)
                      refs (mapv #(task-ref tasks %) (get-in routes [n r]))
                      evidence-targets
                      (distinct (for [t refs row (tasks (:component t)) :when (= (:id t) (:id row))
                                      [_ target] (re-seq #"\]\(([^)#]+)(?:#[^)]*)?\)" (get-in row [:source :raw]))
                                      :when (not (re-find #"^[a-z]+:" target))
                                      :let [p (str (.normalize (.resolve (.getParent (.toPath (io/file (:path row)))) target)))]
                                      :when (.isFile (io/file p))] p))
                      checks (distinct (mapcat #(map str (get test-files %))
                                         (re-seq #"[a-z0-9_]+_test\.clj" (:raw (sections milestone)))))]
                  {:id id :milestone milestone :acceptance-source source
                   :context-source (sections milestone)
                   :task-references refs
                   :evidence-source-locators (mapv locator (sort (distinct (concat evidence-targets checks))))
                   :evidence-qualification "Owning test/source/doc locations are exact inspected bytes at the task baseline, not executed or accepted results; missing full closure and current applicability remain explicit."
                   :coverage :mapped-owner-review-pending
                   :relation :acceptance-facet-of-existing-outcomes
                   :completion-prerequisites (vec (sort (sh-graph milestone)))
                   :shared-completion-rule (when (<= 3 n) :sh03-and-later-uniform-completion)
                   :source-policy-qualification (when (legacy-accounting-facet-ids id) :legacy-project-accounting-wording)
                   :historical-completion (if (< n 7)
                     {:state :retained-accepted-original-scope :source-commit (historical-commits n)
                      :evidence-source (:source hs)
                      :original-audit-classification (some-> (re-find #"\| ([^|]+) \|$" (get-in hs [:source :raw])) second str/trim)
                      :current-applicability :unassessed}
                     {:state :no-whole-milestone-completion-claim})
                   :current-evidence (cond
                     (#{"SH-05.1" "SH-05.6" "SH-06.6"} id) :partial-current-cohort-open
                     (< n 7) :historical-supported-slice-current-closure-unassessed
                     (or (= n 7) (<= 8 n 13) (<= 15 n 17) (= n 20)) :partial-work-not-accepted-complete
                     :else :executable-coverage-unassessed)
                   :accepted-current-requirement :unassessed
                   :mapping-review :pending-independent-owner-review
                   :qualification (cond
                     (= n 1) "Owner-reviewed task-authority gap filled by one development-boundaries record with six acceptance facets; historical E01 completion retained, current acceptance unassessed."
                     (= n 7) "All required C01-C18 joins remain open. Six-owner pilot does not define the complete population."
                     (= n 16) "Only selected required pass set. Residual-cost wording is historical project accounting where superseded; semantic proof/check preservation survives."
                     (= n 18) "Selected Linux LLVM release target; Darwin harness evidence cannot substitute."
                     (<= 21 n 29) "Real execution/rebuild/release evidence required; supplied-record validation cannot close this facet."
                     :else "Retain exact source scope, rejects, host boundaries and independently accepted evidence; task mapping grants no completion.")}))
              (:requirements inventory))]
    (when-not (and (= 30 (count routes)) (every? #(= 6 (count %)) routes) (= 18 (count crows)))
      (throw (ex-info "Incomplete original routing population" {})))
    {:schema-version 1 :kind :sh-family-derived-crosswalk :authority :read-only-acceptance-rollup
     :applying-task-source-commit baseline :original-inventory-commit original-baseline
     :task-authority-lineage {:path "specs/sh-family-crosswalk/task-authority-lineage.edn"
                              :sha256 (sh-inventory/sha256 (slurp "specs/sh-family-crosswalk/task-authority-lineage.edn"))}
     :shared-completion-rule
     (let [text (slurp sh-inventory/plan-path)
           start (str/index-of text "## Three boundaries and one shared completion rule")
           end (str/index-of text "### How to use the check references")]
       {:id :sh03-and-later-uniform-completion :applicability :sh03-through-sh29
        :source (sh-inventory/source-span sh-inventory/plan-path text start end)
        :qualification "Implementation, integration and authoritative completion remain separate. Actual owning execution, rejection/preservation/composition, exact provenance/support/TCB and independently accepted stable revision are required only for the applicable claim. SH00-SH02 retain accepted baseline/process scope; no unconditional replay of unchanged evidence."})
     :component-owner-evidence
     (into (sorted-map)
       (for [c components :let [path (str "docs/component-roadmaps/" c ".md") text (slurp path)
                                end (or (str/index-of text "\n## ") (count text))]]
         [c {:source (sh-inventory/source-span path text 0 end)
             :assignment :existing-reservations-preserved-not-inferred
             :qualification "Use the named component owner role and existing actor reservations; a source-unassigned person remains unassigned, not permission for a new writer."}]))
     :global-requirement-owner "01a179ad-2198-7392-966a-1e3c31209027"
     :global-requirement-handoff {:sha256 "25be0fe3d53bf133b72f405d50d039ba3ab9fb186499036587ab72a860174954"
                                :authority :join-proposals-only :accepted-denominator false}
     :source-digests (into (sorted-map) (map (fn [p] [p (sh-inventory/sha256 (slurp p))]) sources))
     :stable-task-routing-keys stable-task-routing-keys
     :retained-native-history {:path "specs/sh-family-crosswalk/retained-native-evidence.edn"
                              :sha256 (sh-inventory/sha256 (slurp "specs/sh-family-crosswalk/retained-native-evidence.edn"))
                              :relation :bounded-history-not-whole-sh-acceptance
                              :scope "Three actual native ABI producers and selected PR368 SH22 consumer progress; original source/provider qualifications and14source facets retained; no generic authentication/C12/selfhost/release acceptance."}
     :accepted-current-scoped-evidence
     [{:task-id "reader/finite-remaining-work/fresh-syntax-handoff"
       :implementation-commit "bcf9c72ed9445e0b8bf259dc5d025c3192ddacc6"
       :receipt (locator "docs/artifacts/phase-06/reports/current-reader-fresh-syntax-handoff.md")
       :relation :retained-explicit-accepted-task-record
       :qualification "Selected-current development-unadmitted fresh Reader/Syntax handoff only; original reported mixed-suite failure retained. No full SH03, canonical Syntax, C5/C14 or release closure."}]
     :source-conflicts
     [{:id :p15-whole-gate-vs-final-seed-retirement
       :whole-gate-source (locator p15-whole-gate)
       :whole-gate-recorded-flags (select-keys (edn/read-string (slurp p15-whole-gate)) release-flags)
       :final-seed-retirement-source (locator p15-final-proof)
       :final-seed-retirement-recorded-flags (select-keys (edn/read-string (slurp p15-final-proof)) release-flags)
       :disposition :conflicting-whole-gate-record-no-current-closure
       :qualification "Distinct retained artifacts, not duplicate proof identities. Final proof and P15/P18 roadmap retain incomplete seed retirement; old whole-gate complete flags cannot substitute. Preserve14finite stage0 and P18T00/01/02/04 historical completions; no artifact regeneration or compiler replay."}
      {:id :core-representation-vs-resolved-semantic-lowering
       :sources (mapv locator ["docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md"
                               "docs/phase-06-compiler-architecture/080-c1-compiler-architecture-overview.md"
                               "docs/phase-06-compiler-architecture/084-c5-name-resolution-and-namespace-analyzer-design.md"
                               "docs/phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md"])
       :disposition :operational-c6-requires-c5-bindings-core-interface-can-precede-resolution
       :qualification "D1 places Core Gravity AST before Name Resolution; C1 places resolve-names before lower-to-core. C5 resolves syntax after expansion and before type/effects; C6 explicitly consumes resolved syntax and C5 bindings. Use that actual C6 consumer edge without requiring completed semantic Core for Resolve. Whether D1 means the identical AST construction event remains an explicit architecture-owner qualification; no source rewrite or invented fused pass."}
      {:id :legacy-project-accounting-wording
       :criterion-ids (vec (sort legacy-accounting-facet-ids))
       :current-policy-source
       (let [path "docs/self-hosting-current-migration-cohort.md" text (slurp path)
             start (str/index-of text "## Current requirements: no project quotas or cost accounting")
             end (str/index-of text "Assessment revision:")]
         (sh-inventory/source-span path text start end))
       :disposition :preserve-mixed-source-with-existing-supersession-qualification
       :global-equivalence-owner "01a179ad-2198-7392-966a-1e3c31209027"
       :qualification "Original raw wording and historical checks remain provenance. Existing current direction retires project work/capacity-only refusals; it does not retire malformed/cyclic/tampered/stale rejection, intrinsic type/Unicode/index/arity/target constraints, semantic resource ownership, actual broker/heap/watchdog/capture/platform controls or external provider spending limits. OOM/timeout remains a resource failure. This SH view makes no new global facet/equivalence adjudication."}]
     :task-authorities tasks :requirements requirements
     :sh07-sequence (mapv (fn [{:keys [groups source]}]
                            {:id (nth groups 0) :requirement-scope (str/trim (nth groups 1))
                             :accountability (str/trim (nth groups 2))
                             :dependency-declaration (str/trim (nth groups 3)) :source source
                             :state :open-source-recorded
                             :relation (if (#{"SH07-C17" "SH07-C18"} (nth groups 0))
                                         :scoped-migration-or-milestone-decision-gate
                                         :implementation-or-composition-provenance-step)
                             :current-ownership-source {:path sh07-path :anchor "outcome-ownership-and-evidence-decisions"}
                             :task-references (case (nth groups 0)
                               "SH07-C01" [(task-ref tasks ["core" 1]) (task-ref tasks ["compiler-api" "pass-evidence"])]
                               "SH07-C03" [(task-ref tasks ["core" 2]) (task-ref tasks ["resolve" 5])]
                               "SH07-C05" [(task-ref tasks ["core" 1]) (task-ref tasks ["core" 2])]
                               "SH07-C06" [(task-ref tasks ["core" 1]) (task-ref tasks ["core" 2])]
                               "SH07-C11" [(task-ref tasks ["core" 4])]
                               "SH07-C12" [(task-ref tasks ["core" 4]) (task-ref tasks ["compiler-api" "pipeline"])]
                               "SH07-C13" [(task-ref tasks ["core" 5]) (task-ref tasks ["compiler-api" "pipeline"])]
                               "SH07-C14" [(task-ref tasks ["core" 5]) (task-ref tasks ["types" 5])]
                               "SH07-C15" [(task-ref tasks ["core" 4]) (task-ref tasks ["diag" 1])]
                               "SH07-C16" [(task-ref tasks ["expand" 5]) (task-ref tasks ["resolve" 5])]
                               "SH07-C17" [(task-ref tasks ["compiler-api" "pass-evidence"])]
                               "SH07-C18" []
                               [(task-ref tasks ["core" 1])])}) crows)
     :scope-boundaries {:deployment "SH29 first declared seedless release; wider platform, targets, deployment services and physical product retain their existing task authorities."
                        :platform-tasks (mapv #(select-keys % [:id :path]) (tasks "platform"))
                        :completion-units "Neither180 source criteria nor18 SH07 provenance IDs nor graph facets are implementation task denominators."
                        :status "Read only component ledgers; preserve scoped historical/current receipts."
                        :missing-coverage "Task entailment and owner review pending; do not infer complete coverage from a nonempty route."
                        :denial "Restricted C12 work remains with its existing owner; no execution or transfer authorized by this mapping."}}))

(defn adopt-review [result review]
  (when-not (and (= :accepted-mapping-only (:status review))
                 (= (:input-fingerprint review) (review-fingerprint result)))
    (throw (ex-info "Source/routing inputs differ from accepted review; preserve the old receipt and obtain an explicit successor disposition" {})))
  (-> result
      (assoc :mapping-review review)
      (update :requirements (fn [rows] (mapv #(assoc % :coverage :owner-and-independent-reviewed
                                                     :mapping-review :independently-reviewed) rows)))
      (assoc-in [:scope-boundaries :missing-coverage]
                "Exact source/routing mapping reviewed; changed inputs require successor review. Implementation coverage and current whole-criterion acceptance remain separate and unassessed.")))
(defn build [] (adopt-review (source-view) mapping-review))

(defn rollup [result]
  (str "# SH acceptance rollup\n\nGenerated by tools/sh_crosswalk.clj; read-only. Edit component task authorities,\nnot this view. Source criteria and sequence IDs are provenance, not additive\nimplementation tasks. Mapping/graph reviewed at c6c80530; implementation and\ncurrent whole-criterion acceptance remain separate and unassessed.\n\n"
       "Original source: " original-baseline ". Task authority amendment: " baseline ".\n\n"
       "Every requirement retains its complete acceptance/context text, source bytes,\nlines and SHA256 in original-inventory.edn and crosswalk.edn. Current acceptance\nis unassessed unless an exact scoped task receipt explicitly establishes it.\n\n"
       "SH03-SH29 additionally retain the original [shared completion rule](../../docs/self-hosting-milestone-completion-plan.md#three-boundaries-and-one-shared-completion-rule): actual owning execution, rejection/preservation/composition and applicable independently accepted migration evidence. SH00-SH02 keep their historical baseline/process scope.\n\n"
       (apply str
         (for [rows (partition-by :milestone (:requirements result))
               :let [milestone (:milestone (first rows))]]
           (str "## " milestone "\n\nCompletion-credit prerequisites: "
                (if (seq (:completion-prerequisites (first rows)))
                  (str/join ", " (:completion-prerequisites (first rows))) "none")
                ". These do not prohibit interface-based development.\n\n"
                "| Criterion / exact source | Existing canonical task IDs | Evidence classification |\n"
                "| --- | --- | --- |\n"
                (apply str
                  (for [r rows]
                    (str "| [" (:id r) "](https://github.com/burn2delete/uniform/blob/" original-baseline "/"
                         sh-inventory/plan-path "#L" (get-in r [:acceptance-source :start-line]) ") | "
                         (str/join "; " (for [t (:task-references r)]
                                          (str "[" (:component t) "](../" (:component t)
                                               "-workflow/tasks.md#implementation-tasks): `" (:id t) "`")))
                         " | " (name (:current-evidence r))
                         (when (= :retained-accepted-original-scope (get-in r [:historical-completion :state]))
                           (str "; historical original-scope completion `" (get-in r [:historical-completion :source-commit]) "` retained"))
                         ". Current full criterion unassessed. |\n"))) "\n")))
       "## SH07 implementation sequence\n\nThe following 18 rows expand the same six SH07 criteria. They do not add 18\nimplementation completion units. Current outcomes remain open at original scope.\n\n"
       "| Provenance step | Source dependency declaration | Canonical task IDs |\n| --- | --- | --- |\n"
       (apply str (for [r (:sh07-sequence result)]
                    (str "| [" (:id r) "](https://github.com/burn2delete/uniform/blob/" original-baseline "/" sh07-path
                         "#L" (get-in r [:source :start-line]) ") | " (:dependency-declaration r) " | "
                         (if (seq (:task-references r))
                           (str/join "; " (map #(str "`" (:id %) "`") (:task-references r)))
                           "Coordinator milestone decision; no separate implementation task") " |\n")))
       "\n## Wider platform and deployment scope\n\nSH29 closes only the declared first seedless release. The existing hardware,\nfirmware, kernel and devices records plus conditional platform-product decisions\nremain in the Platform task authority. Additional backend, runtime and deployment\nservices retain their own source-bound outcomes. They are not silently completed\nor made universal prerequisites for the selected compiler release.\n"))

(defn validate-pinned-sources! [result read-version]
  (let [locators (for [node (tree-seq coll? seq result) :when (map? node)
                       :let [m (into {} node)]
                       :when (and (:path m) (:source-commit m) (:sha256 m))]
                   [(:source-commit m) (:path m) (:sha256 m)])
        task-files (for [[_ rows] (:task-authorities result) row rows]
                     [(:source-commit row) (:path row) (:file-sha256 row)])]
    (doseq [[commit path expected] (distinct (concat locators task-files))]
      (when-not (= expected (sh-inventory/sha256 (read-version commit path)))
        (throw (ex-info "Source differs from its applying Git pin; preserve lineage before regeneration"
                        {:path path :source-commit commit}))))))

(defn git-source [commit path]
  (let [process (.start (doto (ProcessBuilder. ^java.util.List ["git" "show" (str commit ":" path)])
                         (.redirectErrorStream true)))
        text (slurp (.getInputStream process))]
    (when-not (zero? (.waitFor process))
      (throw (ex-info "Pinned local Git source unavailable; no projection written" {:path path :source-commit commit})))
    text))

(defn run [args]
  (let [result (build)]
    (case (vec args)
      ["--write"] (do (validate-pinned-sources! result git-source)
                       (spit output-path (with-out-str (pp/pprint result))) (spit rollup-path (rollup result)))
      ["--check"] (when-not (and (= result (edn/read-string (slurp output-path))) (= (rollup result) (slurp rollup-path)))
                    (throw (ex-info "Stale SH projection" {})))
      (throw (ex-info "Usage: clojure -M tools/sh_crosswalk.clj --write|--check" {})))
    (println "SH projection:180 criteria,18 sequence rows; mapping reviewed, implementation acceptance unchanged.")))
(when (seq *command-line-args*) (run *command-line-args*))
