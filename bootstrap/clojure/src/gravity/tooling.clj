(ns gravity.tooling
  "Native coverage data and static source evidence; no tooling admission."
  (:require [clojure.set :as set]))

(def ^:private namespace-contract
  '{:authenticates-inputs? false,
 :public-api
 {native-metadata-fail!
  {:arglists ([fail-provider id module subject extra]),
   :admission :none,
   :native-compatibility-only? true},
  verify-evidence
  {:arglists ([invoke original-request candidate]), :admission :none},
  coverage-record
  {:arglists
   ([document-ids-var
     diagnostic-ids-var
     present-var
     task-statuses-var
     artifact]),
   :admission :none},
  native-validate-lsp!
  {:arglists ([operations module lsp-session]),
   :admission :none,
   :native-compatibility-only? true},
  native-validate-debugger!
  {:arglists ([operations module debugger-trace]),
   :admission :none,
   :native-compatibility-only? true},
  native-validate-safety-audit!
  {:arglists ([operations module safety-audit-report]),
   :admission :none,
   :native-compatibility-only? true},
  execute-evidence
  {:arglists ([invoke original-request]), :admission :none},
  native-validate-profiler!
  {:arglists ([operations module profiler-report]),
   :admission :none,
   :native-compatibility-only? true},
  native-metadata-suite
  {:arglists ([module]),
   :admission :none,
   :native-compatibility-only? true},
  native-validate-registry-ux!
  {:arglists ([operations module registry-record]),
   :admission :none,
   :native-compatibility-only? true},
  native-validate-formatter!
  {:arglists ([operations module formatter-fixture]),
   :admission :none,
   :native-compatibility-only? true},
  native-validate-documentation!
  {:arglists ([operations module documentation-artifact]),
   :admission :none,
   :native-compatibility-only? true},
  native-validate-ir-inspector!
  {:arglists ([operations module ir-inspector-bundle]),
   :admission :none,
   :native-compatibility-only? true},
  native-validate-dev-server!
  {:arglists ([operations module dev-server-session]),
   :admission :none,
   :native-compatibility-only? true},
  check-evidence {:arglists ([invoke request]), :admission :none},
  native-metadata-suite-present?
  {:arglists ([module]),
   :admission :none,
   :native-compatibility-only? true},
  native-validate-linter!
  {:arglists ([operations module linter-report]),
   :admission :none,
   :native-compatibility-only? true},
  native-validate-ai-tooling!
  {:arglists ([operations module ai-tooling-record]),
   :admission :none,
   :native-compatibility-only? true},
  native-validate-repl!
  {:arglists ([operations module repl-session]),
   :admission :none,
   :native-compatibility-only? true},
  native-validate-tooling!
  {:arglists ([operations module]),
   :admission :none,
   :native-compatibility-only? true},
  native-validate-cli!
  {:arglists ([operations module cli-command-set]),
   :admission :none,
   :native-compatibility-only? true}},
 :dependency-direction
 {:requires [clojure.set],
  :consumed-by [gravity.bootstrap],
  :forbids
  [gravity.bootstrap gravity.cli gravity.build gravity.release]},
 :legacy-complete-and-proof-labels :supplied-compatibility-data,
 :native-metadata-boundary :stage0-tooling-compiled-gate,
 :performs-tool-services? false,
 :source-scope :static-non-authority-meta-jvm,
 :governing-contracts
 [D1
  D2
  D3
  D8
  D9
  L2
  L3
  T1
  T2
  T3
  T4
  T5
  T6
  T7
  T8
  T9
  T10
  T11
  T12
  T13
  C15
  BOOT8
  TEST2
  D6
  C1],
 :preserves
 {:native-coverage
  [:var-read-position :argument-order :legacy-record-and-task-data],
  :source-evidence
  [:original-request
   :source-provenance
   :deterministic-identity
   :negative-status
   :incomplete-status
   :blocked-verifier-gate],
  :native-metadata
  [:original-predicates
   :selection-and-dispatch-order
   :late-function-roots
   :argument-evaluation-order
   :field-precedence
   :callback-and-exception-identity]},
 :unsupported
 [:formatter
  :linter
  :repl
  :lsp
  :debugger
  :dev-server
  :registry-ui
  :profiler
  :audit-explorer
  :ai-execution
  :provider-grants
  :public-cli
  :source-core-admission
  :callback-authentication
  :native-backend-execution
  :migration
  :release
  :self-hosting
  :seed-retirement],
 :artifact-inputs
 [:caller-supplied-native-report-data
  :document-and-diagnostic-vars
  :trusted-presence-and-task-status-vars
  :static-w5-original-request
  :static-w5-candidate
  :trusted-source-invocation-operation
  :trusted-native-module-and-tooling-records
  :late-native-operation-suppliers],
 :trusted-native-operation-suppliers? true,
 :namespace gravity.tooling,
 :diagnostic-ownership
 {:native-builder :none,
  :source-rules :gravity.self-hosting.w5-tooling-conformance-verifier,
  :compiler-and-runtime-failures :propagated-unchanged,
  :bootstrap-admission-and-publication :existing-owner,
  :native-metadata-decides
  ["T1003"
   "T2002"
   "T3002"
   "T4003"
   "T5001"
   "T6004"
   "T7001"
   "T8003"
   "T9001"
   "T10002"
   "T11003"
   "T12001"
   "T13002"],
  :native-metadata-transport :existing-p13-operation},
 :artifact-outputs
 [:legacy-coverage-data
  :source-owned-static-evidence-result
  :fresh-replay-verification
  :unchanged-source-rejection
  :native-suite-and-presence
  :nil-or-native-callback-value
  :unchanged-native-tooling-refusal],
 :issues-certificates? false,
 :contract-boundary :native-coverage-data-and-static-source-evidence}
)

(defn
 coverage-record
 [document-ids-var
  diagnostic-ids-var
  present-var
  task-statuses-var
  artifact]
 (let
  [documents
   (:document-contracts artifact)
   accepted-docs
   (set (map :document (:accepted-tooling-fixtures artifact)))
   rejected-docs
   (set (map :document (:rejected-tooling-fixtures artifact)))
   conformance-docs
   (set (map :document (:tooling-conformance-evidence artifact)))
   diagnostics
   (set
    (map
     :diagnostic
     (get-in artifact [:tooling-diagnostic-stream :diagnostics])))]
  {:accepted-fixtures-covered?
   (= (set (deref document-ids-var)) accepted-docs),
   :formatter-linter-docs-covered?
   (and
    (true? (get-in artifact [:formatter-fixture :reader-round-trip]))
    (set/subset?
     #{:capabilities :types :effects :profile :artifacts}
     (set
      (get-in artifact [:linter-diagnostic-report :compiler-facts])))
    (true?
     (get-in artifact [:documentation-artifact :structured-docs]))),
   :lsp-and-debugger-covered?
   (and
    (true?
     (get-in artifact [:lsp-capability-matrix :diagnostics-match-cli]))
    (=
     :passed
     (get-in
      artifact
      [:debugger-trace :source-map-validation :status]))),
   :task-statuses ((deref task-statuses-var)),
   :document-coverage-complete?
   (= (set (deref document-ids-var)) (set (keys documents))),
   :repl-and-incremental-dev-covered?
   (and
    (= :complete (:status (:repl-session-artifact artifact)))
    (true?
     (get-in artifact [:repl-session-artifact :transcript-redacted]))
    (= :complete (:status (:dev-server-session artifact)))),
   :cli-and-project-workflow-covered?
   (and
    (= :complete (:status (:cli-command-set artifact)))
    (true? (get-in artifact [:cli-command-set :json-output]))
    (contains?
     (set
      (get-in
       artifact
       [:cli-command-set :capability-prompts :shown-denials]))
     :shell/exec)),
   :rejected-fixtures-covered?
   (= (set (deref document-ids-var)) rejected-docs),
   :status :complete,
   :ai-assisted-tooling-covered?
   (and
    (true?
     (get-in artifact [:ai-tooling-record :patch-artifact :validated]))
    (=
     :approved
     (get-in
      artifact
      [:ai-tooling-record :human-review-record :decision]))
    (false?
     (get-in
      artifact
      [:ai-tooling-record :replay-trace :hidden-tool-use-detected]))),
   :diagnostics-covered?
   (set/subset? (set (deref diagnostic-ids-var)) diagnostics),
   :conformance-evidence-covered?
   (= (set (deref document-ids-var)) conformance-docs),
   :dev-registry-and-inspectors-covered?
   (boolean
    (and
     (true?
      (get-in
       artifact
       [:registry-ux-record :update-diff :capability-diff-visible]))
     ((deref present-var)
      (get-in artifact [:ir-inspector-bundle :source-span-maps]))
     ((deref present-var)
      (get-in
       artifact
       [:profiler-report :check-elision-report :evidence]))
     ((deref present-var)
      (get-in artifact [:safety-audit-report :proof-index]))))}))

(defn check-evidence
  [invoke request]
  (invoke 'w5-tooling-conformance-verify [request]))

(defn verify-evidence
  [invoke original-request candidate]
  (invoke 'w5-tooling-conformance-verify-result [original-request candidate]))

(defn execute-evidence
  [invoke original-request]
  (let [candidate (check-evidence invoke original-request)
        verification (verify-evidence invoke original-request candidate)]
    (if (= :passed (:status verification))
      {:result candidate :verification verification}
      verification)))

(defn native-metadata-suite
  [module]
  (get-in module [:metadata :tooling :compiled-gate] {}))

(defn native-metadata-suite-present?
  [module]
  (contains? (get-in module [:metadata :tooling] {}) :compiled-gate))

(defn native-metadata-fail!
  [fail-provider id module subject extra]
  ((fail-provider)
   id
   (:source-path module)
   subject
   (merge {:stage :stage0-compiled-tooling-gate
           :diagnostic-family :phase13-compiled-tooling-experience
           :compiled-gate :tooling-developer-experience
           :remediation
           "Compiled tooling/developer-experience metadata must preserve CLI authority prompts, REPL capability grants, formatter round trips, linter autofix safety, LSP compiler diagnostics, debugger redaction, generated documentation freshness, dev-server reload decisions, registry capability diffs, IR origins, profiler check-elision evidence, safety audit unsafe-island evidence, and AI-generated source validation before instruction-plan execution."}
          extra)))

(defn native-validate-cli!
  [operations module cli-command-set]
  (when (or (not (contains? (set (get-in cli-command-set
                                          [:capability-prompts
                                           :shown-denials]))
                            :shell/exec))
            (false? (:authority-denial-visible? cli-command-set)))
    (((:fail operations))
     "T1003" module cli-command-set
     {:missing-fields [:capability-denial]})))

(defn native-validate-repl!
  [operations module repl-session]
  (when-not (seq (:capability-grants repl-session))
    (((:fail operations))
     "T2002" module repl-session
     {:missing-fields [:capability-grants]})))

(defn native-validate-formatter!
  [operations module formatter-fixture]
  (when-not (true? (:reader-round-trip formatter-fixture))
    (((:fail operations))
     "T3002" module formatter-fixture
     {:missing-fields [:reader-round-trip]})))

(defn native-validate-linter!
  [operations module linter-report]
  (when (or (false? (:autofix-safe? linter-report))
            (false? (get-in linter-report
                            [:autofix :semantics-preserved?])))
    (((:fail operations))
     "T4003" module linter-report
     {:missing-fields [:safe-autofix]})))

(defn native-validate-lsp!
  [operations module lsp-session]
  (when-not (true? (:diagnostics-match-cli lsp-session))
    (((:fail operations))
     "T5001" module lsp-session
     {:missing-fields [:diagnostics-match-cli]})))

(defn native-validate-debugger!
  [operations module debugger-trace]
  (when (or (true? (:redacted-value-access? debugger-trace))
            (some #(and (true? (:redacted %))
                        (true? (:accessed? %)))
                  (:variable-reports debugger-trace)))
    (((:fail operations))
     "T6004" module debugger-trace
     {:missing-fields [:redacted-value-policy]})))

(defn native-validate-documentation!
  [operations module documentation-artifact]
  (when (or (true? (:stale? documentation-artifact))
            (false? (:fresh? documentation-artifact)))
    (((:fail operations))
     "T7001" module documentation-artifact
     {:missing-fields [:source-freshness]})))

(defn native-validate-dev-server!
  [operations module dev-server-session]
  (when-not (some #(= :restart (:decision %))
                  (:hot-reload-decisions dev-server-session))
    (((:fail operations))
     "T8003" module dev-server-session
     {:missing-fields [:safe-hot-reload-decision]})))

(defn native-validate-registry-ux!
  [operations module registry-record]
  (when-not (true? (get-in registry-record
                           [:update-diff :capability-diff-visible]))
    (((:fail operations))
     "T9001" module registry-record
     {:missing-fields [:capability-diff-visible]})))

(defn native-validate-ir-inspector!
  [operations module ir-inspector-bundle]
  (when-not (((:present operations)) (:source-span-maps ir-inspector-bundle))
    (((:fail operations))
     "T10002" module ir-inspector-bundle
     {:missing-fields [:source-span-maps]})))

(defn native-validate-profiler!
  [operations module profiler-report]
  (when-not (((:present operations)) (get-in profiler-report
                                  [:check-elision-report :evidence]))
    (((:fail operations))
     "T11003" module profiler-report
     {:missing-fields [:check-elision-evidence]})))

(defn native-validate-safety-audit!
  [operations module safety-audit-report]
  (when (and (seq (:unsafe-islands safety-audit-report))
             (or (false? (:unsafe-island-evidence? safety-audit-report))
                 (not (((:present operations)) (:proof-index safety-audit-report)))))
    (((:fail operations))
     "T12001" module safety-audit-report
     {:missing-fields [:unsafe-island-evidence]})))

(defn native-validate-ai-tooling!
  [operations module ai-tooling-record]
  (when-not (and (true? (get-in ai-tooling-record
                                [:patch-artifact :validated]))
                 (seq (:validation-report ai-tooling-record))
                 (every? #(= :passed %)
                         (vals (:validation-report ai-tooling-record))))
    (((:fail operations))
     "T13002" module ai-tooling-record
     {:missing-fields [:checked-generated-source]})))

(defn native-validate-tooling!
  [operations module]
  (when (((:suite-present? operations)) module)
    (let [suite (((:suite operations)) module)]
      (doseq [cli-command-set (:cli-command-sets suite)]
        (((:cli operations)) module cli-command-set))
      (doseq [repl-session (:repl-sessions suite)]
        (((:repl operations)) module repl-session))
      (doseq [formatter (:formatters suite)]
        (((:formatter operations)) module formatter))
      (doseq [linter (:linters suite)]
        (((:linter operations)) module linter))
      (doseq [lsp-session (:lsp-sessions suite)]
        (((:lsp operations)) module lsp-session))
      (doseq [debugger-trace (:debugger-traces suite)]
        (((:debugger operations)) module debugger-trace))
      (doseq [documentation-artifact (:documentation-artifacts suite)]
        (((:documentation operations))
         module documentation-artifact))
      (doseq [dev-server-session (:dev-server-sessions suite)]
        (((:dev-server operations)) module dev-server-session))
      (doseq [registry-record (:registry-ux-records suite)]
        (((:registry-ux operations)) module registry-record))
      (doseq [ir-inspector-bundle (:ir-inspector-bundles suite)]
        (((:ir-inspector operations))
         module ir-inspector-bundle))
      (doseq [profiler-report (:profiler-reports suite)]
        (((:profiler operations)) module profiler-report))
      (doseq [safety-audit-report (:safety-audit-reports suite)]
        (((:safety-audit operations))
         module safety-audit-report))
      (doseq [ai-tooling-record (:ai-tooling-records suite)]
        (((:ai-tooling operations))
         module ai-tooling-record)))))

(doseq [operation [#'coverage-record #'check-evidence #'execute-evidence #'verify-evidence
                  #'native-metadata-suite #'native-metadata-suite-present? #'native-metadata-fail! #'native-validate-cli! #'native-validate-repl! #'native-validate-formatter! #'native-validate-linter! #'native-validate-lsp! #'native-validate-debugger! #'native-validate-documentation! #'native-validate-dev-server! #'native-validate-registry-ux! #'native-validate-ir-inspector! #'native-validate-profiler! #'native-validate-safety-audit! #'native-validate-ai-tooling! #'native-validate-tooling!]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
