(ns runtime-residual-interfaces
  "Prepare eight source-bound criterion amendments without changing shared task truth."
  (:require [clojure.edn :as edn] [clojure.string :as str] [clojure.pprint :as pp]))
(binding [*command-line-args* nil] (load-file "tools/runtime_requirement_family.clj"))
(require '[runtime-requirement-family :as family] '[runtime-task-projection :as tasks])
(def output "specs/requirement-reconciliation/runtime-residual-interfaces.edn")
(def partition-ref "bd37ab50c50d27094dce664b34831fcc43c3e9c3")
(def partition-path "specs/requirement-reconciliation/historical-gap-partition.edn")
(def partition-hash "f370fbae2cd2247f3347a61754789aeb232d2d0fdc5e1e233e206debc727e02a")
(def predecessor "43598c5b87feb4d88928549bb20fc4089768718b")
(def criteria
  [{:id "runtime/debug-service-selection" :task-index 1
    :criterion "Select debug services explicitly for each native/managed runtime and release policy. Show a debug artifact and release artifact whose service/helper manifests expose exactly the selected stack/allocation/check/trace hooks; debug-only services are absent in release unless policy explicitly allows and marks them. Preserve source/check/allocation/provider/lifetime/release identity and proof-elision metadata. Stack diagnostics are capability-neutral unless an external lookup is enabled: reject undeclared filesystem, environment, network or symbol-server access, unmarked debug services and lost source/provenance; required safety checks cannot disappear with debug instrumentation."
    :supplement "Provider debug manifests retain profile support, allocation effects, layout/alignment, failure/lifetime/deallocation strategy and unsafe boundaries; traces retain allocation site, resource ID and release site, including selected borrow-debug checks. Enforce sink grants and redact secrets, credentials, prompts, model outputs, user/tainted inputs, raw memory, environment values, package tokens, database rows and deployment identifiers; local logging permission cannot authorize network telemetry."
    :outputs ["debug/release artifact and service-manifest difference" "source-bound check/allocation traces" "debug-policy and undeclared-effect diagnostics"]
    :dependencies ["Lower artifact/source maps" "Auth effect grants" "Legality selected services" "Runtime observability"]}
   {:id "runtime/family-composition" :task-index 0
    :criterion "For each claimed profile/target pair, resolve the selected combination of R1 runtime families under profile compatibility and package policy, emitting the family-selection record and linked/generated/delegated/external/forbidden service table. Exercise an allowed combination and reject incompatible combinations, undeclared providers and hidden universal-runtime dependencies. Every selected service and typed host adapter retains its effects, capability hook or applicable static proof, startup/failure model and provenance; listing families without checking the combination is not acceptance."
    :outputs ["allowed/rejected composition matrix" "family-selection and service-table artifacts" "incompatible-profile/package and hidden-service diagnostics"]
    :dependencies ["Legality profile compatibility" "Build package policy" "Lower target/provider requirements" "Auth authority facts"]}
   {:id "runtime/foreign-symbol-resolution" :task-index 2
    :criterion "Resolve a declared foreign symbol and dynamically load an admitted library/module only under package/deployment policy, the named provider/library/version identity and required effects/grants. Emit the binding, symbol-resolution and generated-adapter records tied to target ABI, calling convention, layout/alignment and source/unsafe audit. Positive fixtures resolve the admitted identity through its safe wrapper/provider; negative fixtures reject wrong/missing symbols, mismatched ABI/layout/provider identity and ungranted or policy-denied dynamic loading. Resolution/loading does not bypass nullability, lifetime/ownership, callback affinity/taint or foreign error conversion contracts. Preserve the delivered split between resolution and loading as two criteria within this same task, without accepting an alias or retiring their history."
    :outputs ["declared-symbol resolution and admitted-loading records" "target ABI/layout and generated-adapter evidence" "symbol/provider/ABI and loading-policy diagnostics"]
    :dependencies ["Build package/deployment/library identity" "Lower ABI/layout" "Auth loading/foreign-call grants" "Ownership accepted wrappers and callback contracts"]}
   {:id "runtime/hot-reload-invalidation" :task-index 3
    :criterion "For a changed interactive definition, record changed source, affected namespace/dependency edges and invalidation of stale syntax, analysis, typed core, MIR/domain IR, backend, runtime and package artifacts. Emit a hot-reload/session record distinguishing preserved runtime state from state that must restart, with evaluated-form/source/generated-origin identity. Show an unaffected artifact retained and affected artifacts rebuilt or rejected before reuse; reject a stale analysis/MIR/backend/runtime/package artifact, untracked build-affecting state and debug inspection outside grants/secret policy. Build-affecting session inputs are captured or explicitly nonhermetic; interactive manifests do not imply release runtime support."
    :supplement "Build-affecting sessions emit transcripts, evaluated-form artifacts, syntax/core/MIR snapshots, dependency invalidation records and source maps; debug hooks cannot expose raw memory, host process state or model/tool outputs without grants."
    :outputs ["change-to-invalidation and hot-reload records" "preserved/restarted runtime-state evidence" "stale-artifact, untracked-state and debug-denial diagnostics"]
    :dependencies ["Build dependency/invalidation consumer" "Compiler phase snapshots" "Lower artifact/source maps" "Auth debug/session grants"]}
   {:id "runtime/managed-collection-semantics" :task-index 1
    :criterion "For each claimed managed host/version and collection implementation, execute Gravity-observable value/collection fixtures for host representation, nullability, equality, hashing, serialization, mutability, identity and taint. Emit the managed-value/collection manifest and source-bound comparison results for persistent values adapted from host collections or implemented by Gravity. Reject observable host equality/hash/serialization/mutation behavior that diverges from the declared Gravity contract, unchecked nulls and a collection implementation missing from the host manifest; host convenience cannot define portable core meaning."
    :outputs ["host/version collection/value representation manifest" "Gravity-observable equality/hash/serialization/mutability comparison fixtures" "divergence/null/manifest diagnostics"]
    :dependencies ["Core value semantics" "Types nullability" "Stdlib persistent collections" "Lower managed-host adapter"]}
   {:id "runtime/managed-dynamic-state" :task-index 1
    :criterion "Manifest dynamic variables, thread-local values, async context, namespace loading, REPL state and hot reload for the selected host/version/module system. Show declared binding/context behavior through thread and async boundaries and namespace/session changes, retaining source maps, taint, error translation and resource policy. Reject unmanifested state, ambient host reflection/eval/dynamic import/package globals without effects/grants and hosted dynamic state leaking into a lower profile without a legal facade/artifact. Session state influencing build artifacts is captured or nonhermetic, not an implicit capability."
    :outputs ["dynamic-variable/thread/async and namespace/session records" "binding/context propagation fixtures" "unmanifested-state, authority and profile-leak diagnostics"]
    :dependencies ["Core dynamic binding semantics" "Concurrency context provider" "Legality hosted/profile boundary" "Build session capture" "Auth effects/grants"]}
   {:id "runtime/managed-null-conversion" :task-index 1
    :criterion "At each claimed host adapter, manifest host/Gravity representation and nullability and convert host null or undefined through the selected Option, Result, checked-wrapper or opaque-foreign-value contract. Execute null, undefined and non-null fixtures for that declared boundary, retaining taint, errors and source/generated-origin diagnostics. Reject unchecked host null/undefined entering a safe Gravity value, a conversion inconsistent with its Gravity type or a missing adapter/null policy; an opaque foreign value does not authorize unchecked access or leakage into lower profiles. Alternative conversion policies are conditional choices, not a requirement to apply every conversion to one value."
    :supplement "Exception/null boundary fixtures also reject untranslated host exceptions or rejected promises and preserve the declared Gravity error/panic/effect channel."
    :outputs ["host adapter and exception/null translation map" "null/undefined/non-null conversion fixtures" "unchecked-null, type/policy and profile-leak diagnostics"]
    :dependencies ["Types Option/Result/nullability" "Lower host representation" "Legality profile boundary" "Auth foreign effects" "Runtime typed adapter"]}
   {:id "runtime/manifest-consumption" :task-index 0
    :criterion "Have backend, package, observability, conformance and self-hosting consumers read the emitted runtime manifest and cross-check it against the actual selected service/helper/provider graph. Emit consumer agreement evidence for family/profile/target, service classification, linked support objects, generated helpers, delegated typed adapters, external providers, forbidden services, capability enforcement hooks or static proofs, startup/initialization/cleanup/failure policy and runtime diagnostic schema/provenance. Reject an absent/unreadable manifest, omitted or contradictory service/provider, hidden forbidden dependency, missing enforcement hook/proof or lost artifact/source-origin edge. A manifest link or producer-only record cannot establish consumer integration."
    :supplement "Native backend integration includes C and LLVM artifacts for the claimed target matrix; fixture inspection alone does not claim those targets implemented."
    :outputs ["five-consumer manifest-to-service agreement report" "runtime architecture/native helper and capability/startup/failure records" "missing/contradictory manifest/service/provenance diagnostics"]
    :dependencies ["Lower backend artifacts" "Build package consumer" "Runtime observability" "Testing conformance consumer" "Self-hosting evidence consumer" "Auth enforcement facts"]}])
(defn apply-edits [text edits]
  (let [rows (tasks/records text)]
    (reduce (fn [text e]
              (family/require! (= (:source-sha256 e) (family/sha256 text)) "Stale/duplicate task file predecessor")
              (let [row (first (filter #(= (:id e) (:id %)) rows))]
                (family/require! (and row (= :canonical-tasks (:collection e)) (= [:acceptance] (:field-path e))
                                      (:before-present? e) (= (:before e) (:acceptance row))) "Wrong task/criterion predecessor")
                (let [old (str "Closing evidence: " (:before e)) new (str "Closing evidence: " (:after e))]
                  (family/require! (= 1 (count (re-seq (re-pattern (java.util.regex.Pattern/quote old)) text))) "Nonunique criterion")
                  (str/replace text old new)))) text edits)))
(defn build [coverage-path]
  (family/require! (= "a7e82612a715713799be4db46cc3e911aff0cb5a2525d4555c8f8e6ded87101f" (family/file-sha256 coverage-path)) "Coverage receipt drift")
  (let [partition-text (family/git-source partition-ref partition-path)
        _ (family/require! (= partition-hash (family/sha256 partition-text)) "Canonical partition drift")
        partition (edn/read-string partition-text) coverage (family/read-edn coverage-path)
        _ (family/require! (= {:historical 577 :criterion-witnesses 13 :retired-history 3 :unverified-residual 561} (:counts partition)) "Historical population drift")
        ids (:ids (first (filter #(= "phase-08" (:family %)) (:partitions partition))))
        assigned (filterv #(some #{(:id %)} ids) (:residual-records partition))
        p (family/read-edn family/proposal-path)
        _ (family/require! (= "a6b2ee0fd88a46d20da8d70afa182d54c4ad6b69b86dbb7959adc1edb4214a3f" (family/file-sha256 family/proposal-path)) "Family proposal drift")
        facets (into {} (map (juxt :id identity) (:source-facets p)))
        _ (doseq [d (:source-documents p)] (family/require! (= (:current-sha256 d) (family/file-sha256 (:path d))) "Assigned source drift"))
        historical (into {} (map (juxt :id identity) (:historical-gap-traceability coverage)))
        base (family/git-source predecessor tasks/task-path) rows (tasks/records base)
        _ (family/require! (= (set ids) (set (map :id criteria))) "Missing/foreign/duplicate assignment")
        mappings (mapv (fn [c]
                         (let [source (first (filter #(= (:id c) (:id %)) assigned)) before (historical (:id c))
                               fs (mapv facets (:source-facet-ids source))]
                           (family/require! (and (every? some? fs) (= (:source-facet-ids source) (:source-facet-ids before))
                                                (= ["phase-08"] (:affected-source-families source))) "Ambiguous/changed source assignment")
                           {:id (:id c) :before before :partition-record source :source-facets fs
                            :disposition :proposed-finite-actionable-task-criterion :criterion-id (str (:id c) "/acceptance")
                            :task-reference {:path tasks/task-path :marker "runtime-implementation-tasks" :id (:id (nth rows (:task-index c)))}
                            :criterion (str (:criterion c) (when (:supplement c) (str " " (:supplement c)))) :acceptance-and-rejection-outputs (:outputs c) :dependencies (:dependencies c)
                            :status "Open" :implementation-acceptance :unassessed :adoption :pending-independent-source-review})) criteria)
        edits (loop [indexes (sort (set (map :task-index criteria))) text base out []]
                (if-let [index (first indexes)]
                  (let [row (nth rows index) cs (filter #(= index (:task-index %)) criteria)
                        suffix (apply str (map #(str " Residual criterion " (:id %) ": " (:criterion %) (when (:supplement %) (str " " (:supplement %)))) cs))
                        e {:collection :canonical-tasks :id (:id row) :field-path [:acceptance] :before-present? true
                           :before (:acceptance row) :after (str (:acceptance row) suffix)
                           :source-sha256 (family/sha256 text)}]
                    (recur (next indexes) (apply-edits text [e]) (conj out e))) out))
        after (apply-edits base edits)]
    {:kind :runtime-residual-criterion-patch :authority :proposal-only :family "phase-08"
     :partition-input {:ref partition-ref :path partition-path :sha256 partition-hash}
     :coverage-input-sha256 (family/file-sha256 coverage-path)
     :task-predecessor {:ref predecessor :path tasks/task-path :sha256 (family/sha256 base)}
     :task-successor-sha256 (family/sha256 after) :task-edits edits :mappings mappings
     :source-documents (:source-documents p) :family-proposal-sha256 (family/file-sha256 family/proposal-path)
     :historical-conservation {:total 577 :criterion-witnesses 13 :retirements 3 :residual 561
                               :original-ids-and-dispositions-sha256 (family/sha256 (pr-str (mapv #(select-keys % [:id :coverage-disposition]) (:historical-gap-traceability coverage))))
                               :history-mutations []}
     :counts {:assigned 8 :prepared-actionable-criterion-mappings 8 :independently-adopted-mappings 0 :new-tasks 0 :unresolved-interface-design-ids []}
     :limits [:implementation-open :independent-adoption-pending :no-shared-checkout-write :no-alias-retirement :no-global-rate]
     :composition "Apply sequential exact task-file and criterion predecessors; refuse stale, duplicate or changed source context. Preserve all later L15/P4/SAFE clauses already present in the predecessor. Central owner regenerates the roadmap and SH pins and reconciles later task changes. These eight full criterion proposals replace no historical receipt and confer no execution acceptance."}))
(defn validate-patch [p expected]
  (family/require! (= p expected) "Forged/stale criterion coverage or source context")
  true)
(defn run [args]
  (family/require! (and (= 2 (count args)) (#{"--write" "--check"} (first args))) "Usage: --write|--check COVERAGE_RECEIPT")
  (let [p (build (second args))]
    (if (= "--write" (first args))
      (spit output (with-out-str (pp/pprint p)))
      (validate-patch (family/read-edn output) p))
    (prn (:counts p))))
(when (seq *command-line-args*) (run *command-line-args*))
