(ns gravity.tooling
  "Native coverage data and static source evidence; no tooling admission."
  (:require [clojure.set :as set]
            [clojure.string :as str]))

(def ^:private namespace-contract
  '{:authenticates-inputs? false,
 :native-assembly-boundary :p13-native-artifact-construction,
 :public-api
 {native-task-statuses
  {:arglists ([operations]),
   :admission :none,
   :native-compatibility-only? true,
   :operation-suppliers [:task-id :documents]},
  native-conformance-evidence
  {:arglists ([operations]),
   :admission :none,
   :native-compatibility-only? true,
   :operation-suppliers [:summaries :documents :rejected-diagnostics]},
  native-task-id
  {:arglists ([operations document]),
   :admission :none,
   :native-compatibility-only? true,
   :operation-suppliers [:document-number]},
  native-metadata-fail!
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
  native-validate-artifact!
  {:arglists ([operations source-path artifact]),
   :admission :none,
   :native-compatibility-only? true,
   :operation-suppliers
   [:present
    :artifact-keys
    :diagnostic-ids
    :diagnostics-by-document
    :summaries
    :documents
    :fail]},
  native-document-records
  {:arglists ([operations]),
   :admission :none,
   :native-compatibility-only? true,
   :operation-suppliers [:document-record :documents]},
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
  native-source-overrides
  {:arglists ([module]),
   :admission :none,
   :native-compatibility-only? true,
   :operation-suppliers []},
  native-validate-profiler!
  {:arglists ([operations module profiler-report]),
   :admission :none,
   :native-compatibility-only? true},
  native-document-record
  {:arglists ([operations document]),
   :admission :none,
   :native-compatibility-only? true,
   :operation-suppliers
   [:task-id
    :diagnostics-by-document
    :summaries
    :governing-documents
    :fixture-names]},
  native-plan-report
  {:arglists ([operations plan module]),
   :admission :none,
   :native-compatibility-only? true,
   :operation-suppliers
   [:artifact-values
    :documents
    :rejected-diagnostics
    :sha256
    :compiled-rejected-fixtures]},
  native-metadata-suite
  {:arglists ([module]),
   :admission :none,
   :native-compatibility-only? true},
  native-document-number
  {:arglists ([document]),
   :admission :none,
   :native-compatibility-only? true,
   :operation-suppliers []},
  native-artifact-values
  {:arglists ([core-artifacts extended-artifacts]),
   :admission :none,
   :native-compatibility-only? true,
   :operation-suppliers []},
  native-validate-registry-ux!
  {:arglists ([operations module registry-record]),
   :admission :none,
   :native-compatibility-only? true},
  native-validate-formatter!
  {:arglists ([operations module formatter-fixture]),
   :admission :none,
   :native-compatibility-only? true},
  native-experience-artifact
  {:arglists ([operations source-path module source-overrides]),
   :admission :none,
   :native-compatibility-only? true,
   :operation-suppliers
   [:artifact-id
    :accepted-fixtures
    :rejected-fixtures
    :task-statuses
    :artifact-keys
    :artifact-values
    :conformance-evidence
    :diagnostic-ids
    :diagnostic-stream
    :document-records
    :documents
    :governing-documents
    :coverage-record
    :validate-artifact]},
  native-validate-documentation!
  {:arglists ([operations module documentation-artifact]),
   :admission :none,
   :native-compatibility-only? true},
  native-core-records
  {:arglists ([]),
   :admission :none,
   :native-compatibility-only? true,
   :operation-suppliers []},
  native-fail!
  {:arglists ([operations id source-path subject extra]),
   :admission :none,
   :native-compatibility-only? true,
   :operation-suppliers
   [:transport-fail :task-id :diagnostic-document :source-span]},
  native-validate-source-overrides!
  {:arglists ([operations source-path overrides]),
   :admission :none,
   :native-compatibility-only? true,
   :operation-suppliers
   [:diagnostic-document :fail :override-diagnostics]},
  native-diagnostic-document
  {:arglists ([operations diagnostic-id]),
   :admission :none,
   :native-compatibility-only? true,
   :operation-suppliers [:diagnostics-by-document :documents]},
  native-present?
  {:arglists ([value]),
   :admission :none,
   :native-compatibility-only? true,
   :operation-suppliers []},
  native-validate-ir-inspector!
  {:arglists ([operations module ir-inspector-bundle]),
   :admission :none,
   :native-compatibility-only? true},
  native-validate-dev-server!
  {:arglists ([operations module dev-server-session]),
   :admission :none,
   :native-compatibility-only? true},
  check-evidence {:arglists ([invoke request]), :admission :none},
  native-extended-records
  {:arglists ([operations]),
   :admission :none,
   :native-compatibility-only? true,
   :operation-suppliers [:documents]},
  native-diagnostic-stream
  {:arglists ([operations source-path input-id]),
   :admission :none,
   :native-compatibility-only? true,
   :operation-suppliers
   [:task-id :diagnostic-document :diagnostic-ids :source-span]},
  native-compiled-artifact
  {:arglists
   ([operations
     source-path
     source-text
     module
     compiled-plan
     run-output]),
   :admission :none,
   :native-compatibility-only? true,
   :operation-suppliers
   [:present :sha256 :plan-report :compiled-rejected-fixtures]},
  native-metadata-suite-present?
  {:arglists ([module]),
   :admission :none,
   :native-compatibility-only? true},
  native-validate-linter!
  {:arglists ([operations module linter-report]),
   :admission :none,
   :native-compatibility-only? true},
  native-accepted-fixtures
  {:arglists ([operations]),
   :admission :none,
   :native-compatibility-only? true,
   :operation-suppliers
   [:task-id :summaries :documents :governing-documents]},
  native-validate-ai-tooling!
  {:arglists ([operations module ai-tooling-record]),
   :admission :none,
   :native-compatibility-only? true},
  native-rejected-fixtures
  {:arglists ([operations]),
   :admission :none,
   :native-compatibility-only? true,
   :operation-suppliers
   [:task-id
    :documents
    :governing-documents
    :rejected-diagnostics
    :fixture-names]},
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
 {:requires [clojure.set clojure.string],
  :consumed-by [gravity.bootstrap],
  :forbids
  [gravity.bootstrap gravity.cli gravity.build gravity.release]},
 :legacy-complete-and-proof-labels :supplied-compatibility-data,
 :native-metadata-boundary :stage0-tooling-compiled-gate,
 :performs-tool-services? false,
 :source-scope :static-non-authority-meta-jvm,
 :native-record-boundary :p13-native-records-and-validation,
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
   :callback-and-exception-identity],
  :native-records
  [:catalog-read-position
   :captured-factory-functions
   :call-order
   :original-predicates
   :refusal-order
   :diagnostic-precedence
   :source-and-plan-identity],
  :native-artifact-construction
  [:caller-preludes
   :construction-and-validation-order
   :late-function-and-catalog-reads
   :ignored-callback-return-values
   :exception-identity
   :source-plan-report-and-artifact-identities]},
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
  :late-native-operation-suppliers
  :trusted-native-catalog-suppliers
  :captured-native-record-factories
  :native-plan-and-module
  :caller-produced-native-module-plan-and-output
  :source-text-for-hashing
  :late-native-construction-suppliers],
 :trusted-native-operation-suppliers? true,
 :namespace gravity.tooling,
 :diagnostic-ownership
 {:bootstrap-admission-and-publication :existing-owner,
  :compiler-and-runtime-failures :propagated-unchanged,
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
  :native-record-transport
  :existing-failure-span-and-digest-operations,
  :native-records :existing-p13-rule-decisions-and-payload-assembly,
  :native-metadata-transport :existing-p13-operation,
  :source-rules :gravity.self-hosting.w5-tooling-conformance-verifier,
  :native-artifact-construction
  :original-validation-and-callback-failures,
  :native-builder :none},
 :artifact-outputs
 [:legacy-coverage-data
  :source-owned-static-evidence-result
  :fresh-replay-verification
  :unchanged-source-rejection
  :native-suite-and-presence
  :nil-or-native-callback-value
  :unchanged-native-tooling-refusal
  :native-p13-record-views
  :native-artifact-validation-outcome
  :unchanged-p13-diagnostic
  :native-plan-report-and-digest
  :native-experience-artifact
  :native-compiled-tooling-artifact],
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

(defn native-document-number
  [document]
  (Integer/parseInt (subs document 1)))

(defn native-task-id
  [operations document]
  (str "P13-D" (+ 176 (((:document-number operations)) document))))

(defn native-source-overrides
  [module]
  (get-in module [:metadata :tooling :experience] {}))

(defn native-diagnostic-document
  [operations diagnostic-id]
  (some (fn [document]
          (when (some #(= diagnostic-id %)
                      (((:diagnostics-by-document operations)) document))
            document))
        ((:documents operations))))

(defn native-fail!
  [operations id source-path subject extra]
  (let [document (or (:document-id subject)
                     (((:diagnostic-document operations)) id))]
    (((:transport-fail operations)) id
           "P13 tooling and developer-experience validation failed"
           (merge {:source-span (or (:source-span subject)
                                    (((:source-span operations)) source-path 0))
                   :diagnostic-family :phase13-tooling-experience
                   :stage :tooling-experience
                   :document-id document
                   :task (when document (((:task-id operations)) document))
                   :tool-id (or (:tool-id subject) :gravity-tooling)
                   :artifact-id (:artifact-id subject)
                   :missing-fact (:missing-fact subject)
                   :fallback-status :rejected
                   :remediation "Phase 13 requires tooling to expose compiler, package, runtime, safety, performance, and AI truth through structured artifacts; accepted and rejected fixtures, stable diagnostics, redaction, authority checks, and capability-based proof must all be present."}
                  extra))))

(defn native-validate-source-overrides!
  [operations source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (if-let [id (get ((:override-diagnostics operations)) fail-kind)]
      (((:fail operations))
       id source-path
       {:artifact-id (str "p13-tooling-" (name fail-kind))
        :document-id (((:diagnostic-document operations)) id)
        :missing-fact fail-kind}
       {:missing-fields [fail-kind]})
      (((:fail operations))
       "P13-MANIFEST" source-path
       {:artifact-id "p13-tooling-unknown-override"
        :missing-fact fail-kind}
       {:missing-fields [:known-override-diagnostic]}))))

(defn native-document-record
  [operations document]
  (let [summary (((:summaries operations)) document)]
    (merge
     {:document document
      :task-id (((:task-id operations)) document)
      :governing-doc (((:governing-documents operations)) document)
      :tool-id (get-in summary [:owned-surface])
      :diagnostics (((:diagnostics-by-document operations)) document)
      :evidence (into {}
                      (map (fn [diagnostic]
                             [(keyword (str/lower-case diagnostic))
                              {:diagnostic diagnostic
                               :source :governing-document
                               :status :present}])
                           (((:diagnostics-by-document operations)) document)))
      :conformance
      {:accepted-fixture "bootstrap/clojure/fixtures/accepted/tooling-experience.gravity"
       :rejected-fixture (str "bootstrap/clojure/fixtures/rejected/"
                              (((:fixture-names operations)) document))
       :artifact-evidence :tooling-experience
       :validation-command "clojure -M:test"
       :status :complete}}
     summary)))

(defn native-document-records
  [operations]
  (into {} (map (fn [document]
                  [document (((:document-record operations)) document)])
                ((:documents operations)))))

(defn native-accepted-fixtures
  [operations]
  (mapv (fn [document]
          {:document document
           :fixture "bootstrap/clojure/fixtures/accepted/tooling-experience.gravity"
           :artifact (get-in (((:summaries operations)) document)
                             [:owned-surface])
           :evidence [(((:task-id operations)) document)
                      (((:governing-documents operations)) document)]
           :status :accepted})
        ((:documents operations))))

(defn native-rejected-fixtures
  [operations]
  (mapv (fn [document]
          {:document document
           :fixture (str "bootstrap/clojure/fixtures/rejected/"
                         (((:fixture-names operations)) document))
           :artifact :stable-tooling-diagnostic
           :diagnostic (((:rejected-diagnostics operations)) document)
           :evidence [(((:task-id operations)) document)
                      (((:governing-documents operations)) document)]
           :status :rejected})
        ((:documents operations))))

(defn native-conformance-evidence
  [operations]
  (mapv (fn [document]
          {:document document
           :accepted-behavior (get-in (((:summaries operations)) document)
                                      [:accepted-behavior])
           :rejected-behavior (((:rejected-diagnostics operations)) document)
           :artifacts (get-in (((:summaries operations)) document)
                              [:artifact-keys])
           :validation ["clojure -M:test"
                        "clojure -M:gravity tooling-experience bootstrap/clojure/fixtures/accepted/tooling-experience.gravity"]
           :status :complete})
        ((:documents operations))))

(defn native-diagnostic-stream
  [operations source-path input-id]
  {:artifact :gravity/phase13-tooling-diagnostic-stream
   :stage :tooling-experience
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           (let [document (((:diagnostic-document operations)) id)]
             {:artifact :gravity/diagnostic
              :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
              :diagnostic id
              :rule id
              :severity :error
              :stage :tooling-experience
              :document-id document
              :task (when document (((:task-id operations)) document))
              :primary {:span (((:source-span operations)) source-path index)
                        :syntax-id (str "p13-tooling-syntax-" index)
                        :artifact input-id}
              :missing-fact (keyword (str/lower-case id))
              :remediation [{:kind :surface-compiler-truth}
                            {:kind :preserve-source-artifact-lineage}
                            {:kind :enforce-authority-and-redaction}
                            {:kind :emit-structured-tooling-evidence}]
              :ordering-key [id :tooling-experience]}))
         ((:diagnostic-ids operations))
         (range))
   :status :complete})

(defn native-present?
  [value]
  (if (coll? value)
    (seq value)
    (some? value)))

(defn native-validate-artifact!
  [operations source-path artifact]
  (let [documents (:document-contracts artifact)
        accepted (:accepted-tooling-fixtures artifact)
        rejected (:rejected-tooling-fixtures artifact)
        conformance (:tooling-conformance-evidence artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:tooling-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= (set ((:documents operations)))
                 (set (:document-set artifact)))
      (((:fail operations)) "P13-MANIFEST" source-path artifact
                         {:missing-fields [:document-set]}))
    (doseq [artifact-key ((:artifact-keys operations))]
      (when-not (((:present operations)) (get artifact artifact-key))
        (((:fail operations)) "P13-MANIFEST" source-path artifact
                           {:missing-fields [artifact-key]}))
      (when-not (= :complete (:status (get artifact artifact-key)))
        (((:fail operations)) "P13-MANIFEST" source-path
                           (get artifact artifact-key)
                           {:missing-fields [:status]})))
    (when-not (every? #(contains? documents %) ((:documents operations)))
      (((:fail operations)) "P13-MANIFEST" source-path documents
                         {:missing-fields [:document-contracts]}))
    (doseq [document ((:documents operations))
            :let [record (get documents document)
                  summary (((:summaries operations)) document)]]
      (doseq [field [:document :task-id :governing-doc :tool-id
                     :diagnostics :evidence :owned-surface
                     :accepted-behavior :rejected-behavior :artifact-keys
                     :conformance]]
        (when-not (((:present operations)) (get record field))
          (((:fail operations)) "P13-MANIFEST" source-path record
                             {:missing-fields [field]})))
      (when-not (= (:owned-surface summary) (:owned-surface record))
        (((:fail operations)) "P13-MANIFEST" source-path record
                           {:missing-fields [:owned-surface]}))
      (doseq [diagnostic (((:diagnostics-by-document operations)) document)]
        (when-not (and (contains? (set (:diagnostics record)) diagnostic)
                       (get-in record [:evidence
                                       (keyword (str/lower-case diagnostic))]))
          (((:fail operations)) diagnostic source-path record
                             {:missing-fields [(keyword (str/lower-case diagnostic))]}))))
    (when-not (= (set ((:documents operations))) (set (map :document accepted)))
      (((:fail operations)) "P13-ACCEPTED" source-path accepted
                         {:missing-fields [:accepted-tooling-fixtures]}))
    (when-not (= (set ((:documents operations))) (set (map :document rejected)))
      (((:fail operations)) "P13-REJECTED" source-path rejected
                         {:missing-fields [:rejected-tooling-fixtures]}))
    (when-not (= (set ((:documents operations))) (set (map :document conformance)))
      (((:fail operations)) "P13-CONFORMANCE" source-path conformance
                         {:missing-fields [:tooling-conformance-evidence]}))
    (when-not (set/subset?
               #{:check :build :test :run :repl :fmt :lint :doc :package
                 :registry :audit :verify :inspect-ir :profile :ai :explain}
               (set (get-in artifact [:cli-command-set :commands])))
      (((:fail operations)) "T1006" source-path (:cli-command-set artifact)
                         {:missing-fields [:commands]}))
    (when-not (true? (get-in artifact [:cli-command-set :json-output]))
      (((:fail operations)) "T1002" source-path (:cli-command-set artifact)
                         {:missing-fields [:json-output]}))
    (when-not (contains? (set (get-in artifact
                                      [:cli-command-set
                                       :capability-prompts
                                       :shown-denials]))
                         :shell/exec)
      (((:fail operations)) "T1003" source-path (:cli-command-set artifact)
                         {:missing-fields [:capability-denial]}))
    (when-not (and (:profile (:repl-session-artifact artifact))
                   (:target (:repl-session-artifact artifact)))
      (((:fail operations)) "T2001" source-path (:repl-session-artifact artifact)
                         {:missing-fields [:profile :target]}))
    (when-not (seq (get-in artifact
                           [:repl-session-artifact :capability-grants]))
      (((:fail operations)) "T2002" source-path (:repl-session-artifact artifact)
                         {:missing-fields [:capability-grants]}))
    (when-not (true? (get-in artifact
                             [:repl-session-artifact
                              :transcript-redacted]))
      (((:fail operations)) "T2004" source-path (:repl-session-artifact artifact)
                         {:missing-fields [:transcript-redacted]}))
    (when-not (true? (get-in artifact
                             [:formatter-fixture :reader-round-trip]))
      (((:fail operations)) "T3002" source-path (:formatter-fixture artifact)
                         {:missing-fields [:reader-round-trip]}))
    (when-not (and (true? (get-in artifact
                                  [:formatter-fixture :comments-preserved]))
                   (true? (get-in artifact
                                  [:formatter-fixture :metadata-preserved])))
      (((:fail operations)) "T3005" source-path (:formatter-fixture artifact)
                         {:missing-fields [:comments-preserved
                                           :metadata-preserved]}))
    (when-not (set/subset?
               #{:types :effects :capabilities :profile :artifacts}
               (set (get-in artifact
                            [:linter-diagnostic-report :compiler-facts])))
      (((:fail operations)) "T4004" source-path
                         (:linter-diagnostic-report artifact)
                         {:missing-fields [:compiler-facts]}))
    (when-not (true? (get-in artifact
                             [:lsp-capability-matrix
                              :diagnostics-match-cli]))
      (((:fail operations)) "T5001" source-path (:lsp-capability-matrix artifact)
                         {:missing-fields [:diagnostics-match-cli]}))
    (when-not (true? (get-in artifact
                             [:lsp-capability-matrix :trace-redacted]))
      (((:fail operations)) "T5005" source-path (:lsp-capability-matrix artifact)
                         {:missing-fields [:trace-redacted]}))
    (when-not (= :passed (get-in artifact
                                 [:debugger-trace
                                  :source-map-validation :status]))
      (((:fail operations)) "T6001" source-path (:debugger-trace artifact)
                         {:missing-fields [:source-map-validation]}))
    (when-not (true? (get-in artifact [:documentation-artifact :redacted]))
      (((:fail operations)) "T7005" source-path
                         (:documentation-artifact artifact)
                         {:missing-fields [:redacted]}))
    (when-not (zero? (get-in artifact
                             [:documentation-artifact
                              :example-validation-report :failed]))
      (((:fail operations)) "T7003" source-path
                         (:documentation-artifact artifact)
                         {:missing-fields [:example-validation-report]}))
    (when-not (true? (get-in artifact
                             [:dev-server-session :bug-report-redacted]))
      (((:fail operations)) "T8004" source-path (:dev-server-session artifact)
                         {:missing-fields [:bug-report-redacted]}))
    (when-not (some #(= :restart (:decision %))
                    (get-in artifact
                            [:dev-server-session :hot-reload-decisions]))
      (((:fail operations)) "T8003" source-path (:dev-server-session artifact)
                         {:missing-fields [:hot-reload-decisions]}))
    (when-not (true? (get-in artifact
                             [:registry-ux-record :update-diff
                              :capability-diff-visible]))
      (((:fail operations)) "T9001" source-path (:registry-ux-record artifact)
                         {:missing-fields [:capability-diff-visible]}))
    (when-not (((:present operations)) (get-in artifact
                                    [:ir-inspector-bundle
                                     :source-span-maps]))
      (((:fail operations)) "T10002" source-path
                         (:ir-inspector-bundle artifact)
                         {:missing-fields [:source-span-maps]}))
    (when-not (((:present operations)) (get-in artifact
                                    [:profiler-report
                                     :check-elision-report :evidence]))
      (((:fail operations)) "T11003" source-path (:profiler-report artifact)
                         {:missing-fields [:check-elision-evidence]}))
    (when-not (((:present operations)) (get-in artifact
                                    [:safety-audit-report :proof-index]))
      (((:fail operations)) "T12005" source-path (:safety-audit-report artifact)
                         {:missing-fields [:proof-index]}))
    (when-not (and (true? (get-in artifact
                                  [:ai-tooling-record
                                   :patch-artifact :validated]))
                   (= :approved
                      (get-in artifact
                              [:ai-tooling-record
                               :human-review-record :decision]))
                   (every? #(= :passed %)
                           (vals (get-in artifact
                                         [:ai-tooling-record
                                          :validation-report]))))
      (((:fail operations)) "T13002" source-path (:ai-tooling-record artifact)
                         {:missing-fields [:checked-generated-source]}))
    (when-not (set/subset? (set ((:diagnostic-ids operations))) diagnostics)
      (((:fail operations)) "P13-MANIFEST" source-path
                         (:tooling-diagnostic-stream artifact)
                         {:missing-fields [:diagnostics]})))
  :complete)

(defn native-task-statuses
  [operations]
  (merge (zipmap ["P13-T01" "P13-T02" "P13-T03"
                  "P13-T04" "P13-T05" "P13-T06"]
                 (repeat :complete))
         (zipmap (map ((:task-id operations)) ((:documents operations)))
                 (repeat :complete))))

(defn native-core-records
  []
  {:cli-command-set
   {:artifact :gravity/cli-command-set
    :artifact-id "cli-contract:gravity"
    :document "T1"
    :tool-id :gravity-cli
    :commands [:check :build :test :run :repl :fmt :lint :doc :package
               :registry :audit :verify :inspect-ir :profile :ai :explain]
    :exit-codes {:success 0 :check-failure 1 :usage-error 2
                 :dependency-resolution-failure 3
                 :build-or-verification-failure 4
                 :runtime-execution-failure 5
                 :authority-or-human-review-missing 6}
    :json-output true
    :diagnostic-routing {:schema "GravityDiagnosticStream/v1"
                         :sources [:compiler :package :runtime :tooling]}
    :artifact-outputs [:artifact-manifest :diagnostic-stream
                       :verification-report]
    :capability-prompts {:shown-grants [:http/client :db/query]
                         :shown-denials [:shell/exec :secret/read]
                         :human-review-required [:package/publish
                                                 :ai/apply-patch]}
    :secret-redaction {:mode :redact-values
                       :secret-names-visible true}
    :golden-fixtures ["cli-check-success.edn"
                      "cli-authority-denied.edn"]
    :status :complete}
   :repl-session-artifact
   {:artifact :gravity/repl-session
    :artifact-id "repl-session:support-agent"
    :document "T2"
    :tool-id :gravity-repl
    :session-id "repl:phase13-support-agent"
    :project-manifest-hash "sha256:project-001"
    :lockfile-hash "sha256:lock-001"
    :profile :hosted
    :target :jvm-21
    :namespace "support.main"
    :capability-grants [:fs/read :http/client]
    :evaluation-history [{:form "(:effects '(fs/read \"notes.txt\"))"
                          :checks [:reader :macro :type :effect
                                   :profile :safety]
                          :result [:filesystem/read]}]
    :runtime-ledgers ["runtime-ledger:repl-001"]
    :transcript-redacted true
    :command-forms [:profile :target :ns :load :eval :expand :type
                    :effects :capabilities :inspect :artifacts
                    :diagnostics :replay :transcript]
    :status :complete}
   :formatter-fixture
   {:artifact :gravity/formatter-fixture
    :artifact-id "fmt-report:phase13"
    :document "T3"
    :tool-id :gravity-fmt
    :formatter-version "gravity-fmt:0.1.0"
    :configuration-hash "sha256:fmt-config-001"
    :reader-round-trip true
    :changed-files ["src/support/main.grav"]
    :diff-output true
    :json-report {:files 1
                  :changed-ranges ["src/support/main.grav:1:1"]}
    :comments-preserved true
    :metadata-preserved true
    :generated-source-policy :deny-by-default
    :status :complete}
   :linter-diagnostic-report
   {:artifact :gravity/linter-diagnostic-report
    :artifact-id "lint-report:phase13"
    :document "T4"
    :tool-id :gravity-lint
    :rules [:profile :effects :safety :package :ai]
    :diagnostics [{:id "LINT-CAP-MIN"
                   :severity :warning
                   :source-span "src/support/main.grav:12:4"}]
    :baseline {:mode :fail-on-new :stale-entries 0}
    :rule-metadata {"LINT-CAP-MIN" {:inputs [:capability-manifest]
                                    :fixable false}}
    :compiler-facts [:types :effects :capabilities :profile :artifacts]
    :profile-target-applicability {:hosted/jvm-21 [:effects
                                                   :capabilities]}
    :json-export true
    :sarif-like-export true
    :status :complete}
   :lsp-capability-matrix
   {:artifact :gravity/lsp-capability-matrix
    :artifact-id "lsp-matrix:phase13"
    :document "T5"
    :tool-id :gravity-lsp
    :server-id "gravity-lsp:0.1.0"
    :compiler-state "compiler-state:phase13"
    :diagnostics-match-cli true
    :hover-facts [:type :effects :capabilities :profile :docs]
    :completion-constraints [:namespace :profile :visibility :target]
    :code-action-records [{:title "Add capability request"
                           :checks [:reader-round-trip :type :effect]}]
    :rename-boundaries [:namespace :macro-origin]
    :trace-redacted true
    :generated-file-policy :source-artifact-required
    :status :complete}
   :debugger-trace
   {:artifact :gravity/debugger-trace
    :artifact-id "debug-trace:phase13"
    :document "T6"
    :tool-id :gravity-debug
    :session-id "debug:phase13"
    :profile :hosted
    :target :jvm-21
    :debug-data-version "GravityDebug/v1"
    :breakpoints [{:source-span "src/support/main.grav:42:3"
                   :artifact-location "jvm:Support.main:42"}]
    :stack-frames [{:function "support.main/serve"
                    :source-span "src/support/main.grav:42:3"}]
    :variable-reports [{:name "request"
                        :type "SupportRequest"
                        :redacted false}]
    :policy-denials [{:operation :mutate-state
                      :reason :missing-debug-authority}]
    :source-map-validation {:status :passed}
    :status :complete}
   :documentation-artifact
   {:artifact :gravity/documentation-artifact
    :artifact-id "docs:support-agent"
    :document "T7"
    :tool-id :gravity-doc
    :source-hash "sha256:source-001"
    :compiler-version "gravityc-seed:0.1.0"
    :package-version "0.3.0"
    :api-signature-index ["support.main/serve"]
    :effect-capability-docs {"support.main/serve"
                             {:effects [:network/http]
                              :capabilities [:http/client]}}
    :schema-links ["schema:SupportRequest/v1"]
    :example-validation-report {:checked 3 :failed 0}
    :structured-docs true
    :redacted true
    :freshness {:source-hash "sha256:source-001"
                :artifact-hash "sha256:artifact-001"}
    :status :complete}})

(defn native-extended-records
  [operations]
  {:dev-server-session
   {:artifact :gravity/dev-server-session
    :artifact-id "dev-session:phase13"
    :document "T8"
    :tool-id :gravity-dev
    :session-id "dev:phase13"
    :project "gravity.edn"
    :profile :hosted
    :target :jvm-21
    :capability-grants [:http/client]
    :incremental-updates [{:input "src/support/main.grav"
                           :result :converged-with-cli}]
    :diagnostic-stream "diagnostic-stream:dev-001"
    :artifact-events [{:artifact-id "artifact:library-001"
                       :path "build/support-agent.jar"
                       :profile :hosted
                       :target :jvm-21
                       :build-node :emit-library}]
    :runtime-log "runtime-log:dev-001"
    :hot-reload-decisions [{:input "src/support/main.grav"
                            :decision :restart
                            :reason :checked-assumption-invalidated}]
    :bug-report-redacted true
    :status :complete}
   :registry-ux-record
   {:artifact :gravity/registry-ux-record
    :artifact-id "registry-ux:gravity-http"
    :document "T9"
    :tool-id :gravity-registry
    :package-detail-json {:profiles [:hosted :native]
                          :targets [:jvm-21 :wasm32-wasi]
                          :capabilities [:http/client]}
    :human-view {:matches-json true}
    :update-diff {:capability-delta []
                  :safety-delta []
                  :provenance-delta [:builder-attestation]
                  :capability-diff-visible true}
    :verification-report {:signature :verified
                          :sbom :verified
                          :provenance :verified}
    :search-results {:filters [:profile :target :capability :safety]
                     :respected true}
    :access-denials [{:package "acme/private"
                      :metadata-leaked false}]
    :policy-compatibility {:status :compatible}
    :status :complete}
   :ir-inspector-bundle
   {:artifact :gravity/ir-inspector-bundle
    :artifact-id "ir-inspector:phase13"
    :document "T10"
    :tool-id :gravity-inspect-ir
    :stage-id :typed-core
    :compiler-version "gravityc-seed:0.1.0"
    :project-hash "sha256:project-001"
    :profile :hosted
    :target :jvm-21
    :stage-views [:syntax-objects :typed-core :mir :artifact-manifest]
    :source-span-maps ["source-map:reader-to-mir"]
    :pass-diff-reports [{:pass :bounds-check-elision
                         :nonsemantic-renumbering-ignored true}]
    :preservation-reports [{:evidence "proof:bounds-check-elision"
                            :preserved [:types :effects :source-spans]}]
    :origin-chains [{:generated "macro-output:when"
                     :source "src/support/main.grav:17:1"}]
    :redacted true
    :status :complete}
   :profiler-report
   {:artifact :gravity/profiler-report
    :artifact-id "artifact:library-001"
    :document "T11"
    :tool-id :gravity-profile
    :report-id "perf-report:phase13"
    :profile :hosted
    :target :jvm-21
    :benchmark-id "bench/support-agent-check"
    :environment {:host "linux-x64" :runtime :managed}
    :compiler-version "gravityc-seed:0.1.0"
    :samples [{:source-span "src/support/main.grav:42:3"
               :latency-ms 12.5}]
    :comparison-policy {:latency-p95-threshold "10pct"}
    :check-elision-report {:status :accepted
                           :evidence ["proof:bounds-check-elision"]}
    :capabilities [:profiler/read-counters]
    :status :complete}
   :safety-audit-report
   {:artifact :gravity/safety-audit-report
    :artifact-id "audit-report:phase13"
    :document "T12"
    :tool-id :gravity-audit
    :report-id "safety-audit:phase13"
    :unsafe-islands []
    :capability-graph {:requested [:http/client]
                       :granted [:http/client]
                       :denied [:shell/exec]
                       :used [:http/client]}
    :taint-graph [{:source "user-ticket"
                   :sink "support.workflow/model-call"
                   :validator "schema:triage-input"}]
    :ffi-boundaries []
    :ai-safety {:prompt-authority :partitioned
                :tool-escalations 0}
    :package-safety {:sbom "sbom:support-agent-001"
                     :unsafe-summary {:count 0}}
    :proof-index [{:proof-id "proof:support-agent-capability-summary"
                   :checker "gravity.safety/check"}]
    :missing-evidence-report []
    :redacted true
    :status :complete}
   :ai-tooling-record
   {:artifact :gravity/ai-tooling-record
    :artifact-id "ai-tooling:phase13"
    :document "T13"
    :tool-id :gravity-ai-assist
    :modes [:diagnostic-explanation :patch-proposal :test-generation
            :package-update-review]
    :plan-artifact {:schema "GravityAIPlan/v1" :validated true}
    :patch-artifact {:schema "GravityPatch/v1" :validated true}
    :generated-source-provenance [{:input "prompt:patch-proposal-001"
                                   :output "patch:proposal-001"}]
    :prompt-model-ledger ["prompt:patch-proposal-001"
                          "model:gpt-tooling-001"]
    :tool-call-ledger [{:tool :search
                        :capability :repo/read}]
    :validation-report {:type :passed
                        :effect :passed
                        :safety :passed
                        :test :passed}
    :human-review-record {:review-id "review:phase13-ai"
                          :decision :approved}
    :replay-trace {:trace-id "replay:phase13-ai"
                   :hidden-tool-use-detected false}
    :status :complete}
   :tooling-ui-data-model
   {:artifact :gravity/tooling-ui-data-model
    :artifact-id "tooling-ui:phase13"
    :documents ((:documents operations))
    :views [:cli :repl :formatter :lint :lsp :debug :docs :dev
            :registry :ir :profile :safety :ai]
    :source-of-truth :compiler-package-runtime-artifacts
    :structured-automation true
    :redacted true
    :status :complete}})

(defn native-artifact-values
    [core-artifacts extended-artifacts]
    (merge (core-artifacts) (extended-artifacts)))

(defn native-plan-report
  [operations plan module]
  (let [plan-id (:plan-id plan)
        tooling-artifacts (((:artifact-values operations)))
        cli-command-set (:cli-command-set tooling-artifacts)
        repl-session (:repl-session-artifact tooling-artifacts)
        formatter (:formatter-fixture tooling-artifacts)
        linter (:linter-diagnostic-report tooling-artifacts)
        lsp (:lsp-capability-matrix tooling-artifacts)
        debugger (:debugger-trace tooling-artifacts)
        documentation (:documentation-artifact tooling-artifacts)
        dev-server (:dev-server-session tooling-artifacts)
        registry (:registry-ux-record tooling-artifacts)
        ir-inspector (:ir-inspector-bundle tooling-artifacts)
        profiler (:profiler-report tooling-artifacts)
        safety-audit (:safety-audit-report tooling-artifacts)
        ai-tooling (:ai-tooling-record tooling-artifacts)
        tooling-ui (:tooling-ui-data-model tooling-artifacts)
        conformance
        {:document-set ((:documents operations))
         :task "P13-S1"
         :required-diagnostic-ids
         (mapv ((:rejected-diagnostics operations))
               ((:documents operations)))
         :tooling-gate-status :metadata-gate-only
         :cli-status :complete
         :repl-status :complete
         :formatter-status :complete
         :linter-status :complete
         :lsp-status :complete
         :debugger-status :complete
         :documentation-status :complete
         :dev-server-status :complete
         :registry-ux-status :complete
         :ir-inspector-status :complete
         :profiler-status :complete
         :safety-audit-status :complete
         :ai-tooling-status :complete
         :tooling-ui-status :complete
         :status :complete}
        report-base
        {:kind :gravity/stage0-hosted-core-compiled-tooling-report
         :document-set ["D1" "T1-T13"]
         :compiled-plan-id plan-id
         :tooling-manifest
         {:artifact :gravity/stage0-hosted-core-compiled-tooling-manifest
          :profile (:profile module)
          :target (:target module)
          :cli-command-set (:artifact cli-command-set)
          :repl-session (:artifact repl-session)
          :formatter-fixture (:artifact formatter)
          :linter-diagnostic-report (:artifact linter)
          :lsp-capability-matrix (:artifact lsp)
          :debugger-trace (:artifact debugger)
          :documentation-artifact (:artifact documentation)
          :dev-server-session (:artifact dev-server)
          :registry-ux-record (:artifact registry)
          :ir-inspector-bundle (:artifact ir-inspector)
          :profiler-report (:artifact profiler)
          :safety-audit-report (:artifact safety-audit)
          :ai-tooling-record (:artifact ai-tooling)
          :tooling-ui-data-model (:artifact tooling-ui)
          :accepted-fixtures
          ["bootstrap/clojure/fixtures/accepted/core-app.gravity"]
          :rejected-fixtures ((:compiled-rejected-fixtures operations))
          :conformance {:status :complete}
          :status :complete}
         :cli-command-set-record cli-command-set
         :repl-session-record repl-session
         :formatter-record formatter
         :linter-record linter
         :lsp-record lsp
         :debugger-record debugger
         :documentation-record documentation
         :dev-server-record dev-server
         :registry-ux-record registry
         :ir-inspector-record ir-inspector
         :profiler-record profiler
         :safety-audit-record safety-audit
         :ai-tooling-record ai-tooling
         :tooling-ui-data-model-record tooling-ui
         :tooling-conformance-results conformance
         :diagnostics []}]
    (assoc report-base
           :report-id (str "sha256:" (((:sha256 operations)) (pr-str report-base))))))

(defn native-experience-artifact
  [operations source-path module source-overrides]
  (let [input-id (((:artifact-id operations)) {:source-path source-path
                                  :module (:module module)
                                  :task "P13-T01-T06"
                                  :documents ((:documents operations))})
        diagnostic-stream (((:diagnostic-stream operations)) source-path input-id)
        tooling-artifacts (((:artifact-values operations)))
        artifact-base
        (merge
         {:kind :gravity/stage0-tooling-experience-artifact
          :phase "13"
          :task "P13-T01-T06"
          :document-set ((:documents operations))
          :governing-documents ((:governing-documents operations))
          :pass {:name :phase13-tooling-experience
                 :input :ordinary-gravity-source
                 :output :tooling-experience
                 :requires ((:artifact-keys operations))
                 :preserves [:compiler-truth :profile :target :effect
                             :capability :runtime :safety :artifact-lineage
                             :source-spans :redaction-policy
                             :human-review-policy]
                 :emits [:document-contracts :accepted-tooling-fixtures
                         :rejected-tooling-fixtures
                         :tooling-conformance-evidence
                         :tooling-diagnostic-stream]
                 :rejects ((:diagnostic-ids operations))}
          :source-overrides source-overrides
          :module (select-keys module [:module :source-path :profile :target
                                       :effects :capabilities :safety
                                       :metadata])
          :document-contracts (((:document-records operations)))
          :accepted-tooling-fixtures (((:accepted-fixtures operations)))
          :rejected-tooling-fixtures (((:rejected-fixtures operations)))
          :tooling-conformance-evidence (((:conformance-evidence operations)))
          :tooling-diagnostic-stream diagnostic-stream
          :tooling-experience-results
          {:documents ((:documents operations))
           :tasks (((:task-statuses operations)))
           :document-contracts 13
           :artifact-families (count ((:artifact-keys operations)))
           :accepted-fixtures 13
           :rejected-fixtures 13
           :conformance-records 13
           :diagnostic-count (count ((:diagnostic-ids operations)))
           :status :complete}
          :diagnostics []}
         tooling-artifacts)
        _ (((:validate-artifact operations)) source-path artifact-base)
        capability-proof (((:coverage-record operations)) artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (((:artifact-id operations)) (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn native-compiled-artifact
  [operations source-path source-text module compiled-plan run-output]
  (let [tooling-report (((:plan-report operations)) compiled-plan
                                                            module)
        manifest (:tooling-manifest tooling-report)
        cli-command-set (:cli-command-set-record tooling-report)
        repl-session (:repl-session-record tooling-report)
        formatter (:formatter-record tooling-report)
        linter (:linter-record tooling-report)
        lsp (:lsp-record tooling-report)
        debugger (:debugger-record tooling-report)
        documentation (:documentation-record tooling-report)
        dev-server (:dev-server-record tooling-report)
        registry (:registry-ux-record tooling-report)
        ir-inspector (:ir-inspector-record tooling-report)
        profiler (:profiler-record tooling-report)
        safety-audit (:safety-audit-record tooling-report)
        ai-tooling (:ai-tooling-record tooling-report)
        conformance (:tooling-conformance-results tooling-report)
        proof {:compiled-tooling-gate-validated? true
               :tooling-manifest-recorded?
               (= :complete (:status manifest))
               :cli-and-repl-recorded?
               (boolean
                (and (contains? (set (get-in cli-command-set
                                              [:capability-prompts
                                               :shown-denials]))
                                 :shell/exec)
                     (seq (:capability-grants repl-session))))
               :formatter-linter-docs-recorded?
               (boolean
                (and (true? (:reader-round-trip formatter))
                     (set/subset?
                      #{:types :effects :capabilities :profile :artifacts}
                      (set (:compiler-facts linter)))
                     (true? (:structured-docs documentation))
                     (zero? (get-in documentation
                                    [:example-validation-report :failed]))))
               :lsp-debugger-recorded?
               (boolean
                (and (true? (:diagnostics-match-cli lsp))
                     (= :passed (get-in debugger
                                        [:source-map-validation :status]))
                     (not (some #(and (true? (:redacted %))
                                      (true? (:accessed? %)))
                                (:variable-reports debugger)))))
               :dev-registry-inspector-recorded?
               (boolean
                (and (some #(= :restart (:decision %))
                           (:hot-reload-decisions dev-server))
                     (true? (get-in registry
                                    [:update-diff
                                     :capability-diff-visible]))
                     (((:present operations)) (:source-span-maps ir-inspector))))
               :profiler-safety-ai-recorded?
               (boolean
                (and (((:present operations)) (get-in profiler
                                           [:check-elision-report
                                            :evidence]))
                     (((:present operations)) (:proof-index safety-audit))
                     (true? (get-in ai-tooling
                                    [:patch-artifact :validated]))
                     (every? #(= :passed %)
                             (vals (:validation-report ai-tooling)))))
               :compiled-plan-executed? (= "core-app\ngravity:19:2\n(:ok 19)\n"
                                          run-output)
               :rejected-diagnostics-covered?
               (= #{"T1003" "T2002" "T3002" "T4003" "T5001"
                    "T6004" "T7001" "T8003" "T9001" "T10002"
                    "T11003" "T12001" "T13002"}
                  (set (:required-diagnostic-ids conformance)))
               :limitations {:clojure-instruction-runner? true
                             :production-cli? false
                             :interactive-repl-server? false
                             :lsp-server-transport? false
                             :debugger-runtime-session? false
                             :dev-server-process? false
                             :registry-ui-service? false
                             :profiler-runtime? false
                             :ai-patch-application? false
                             :self-hosted-tooling? false
                             :next-required-capability
                             :compile-and-run-real-tooling-slices}
               :status :complete}
        artifact-base
        {:kind :gravity/stage0-hosted-core-compiled-tooling-proof
         :phase "13"
         :task "P13-S1"
         :governing-documents ["D1" "T1-T13"]
         :source {:path source-path
                  :sha256 (str "sha256:" (((:sha256 operations)) source-text))}
         :compiled-plan (select-keys compiled-plan
                                     [:kind :plan-id :entrypoint
                                      :instruction-summary :effect-summary])
         :tooling-report
         (select-keys tooling-report
                      [:kind :report-id :document-set
                       :compiled-plan-id
                       :tooling-manifest
                       :cli-command-set-record
                       :repl-session-record
                       :formatter-record
                       :linter-record
                       :lsp-record
                       :debugger-record
                       :documentation-record
                       :dev-server-record
                       :registry-ux-record
                       :ir-inspector-record
                       :profiler-record
                       :safety-audit-record
                       :ai-tooling-record
                       :tooling-ui-data-model-record
                       :tooling-conformance-results
                       :diagnostics])
         :accepted-run {:command (str "clojure -M:gravity run-compiled "
                                      source-path)
                        :stdout run-output}
         :proof-command (str "clojure -M:gravity hosted-core-compiled-tooling "
                             source-path)
         :rejected-fixtures ((:compiled-rejected-fixtures operations))
         :trusted-boundary {:compiler :clojure/jvm
                            :runtime
                            :gravity.runtime/stage0-clojure-jvm-instruction-runner
                            :instruction-plan? true
                            :clojure-instruction-runner? true
                            :production-cli? false
                            :interactive-repl-server? false
                            :lsp-server-transport? false
                            :debugger-runtime-session? false
                            :dev-server-process? false
                            :registry-ui-service? false
                            :profiler-runtime? false
                            :ai-patch-application? false
                            :self-hosted-tooling? false}
         :capability-based-proof proof}]
    (assoc artifact-base
           :artifact-id (str "sha256:" (((:sha256 operations)) (pr-str artifact-base))))))

(doseq [operation [#'coverage-record #'check-evidence #'execute-evidence #'verify-evidence
                  #'native-metadata-suite #'native-metadata-suite-present? #'native-metadata-fail! #'native-validate-cli! #'native-validate-repl! #'native-validate-formatter! #'native-validate-linter! #'native-validate-lsp! #'native-validate-debugger! #'native-validate-documentation! #'native-validate-dev-server! #'native-validate-registry-ux! #'native-validate-ir-inspector! #'native-validate-profiler! #'native-validate-safety-audit! #'native-validate-ai-tooling! #'native-validate-tooling!
                  #'native-document-number #'native-task-id #'native-source-overrides #'native-diagnostic-document #'native-fail! #'native-validate-source-overrides! #'native-document-record #'native-document-records #'native-accepted-fixtures #'native-rejected-fixtures #'native-conformance-evidence #'native-diagnostic-stream #'native-present? #'native-validate-artifact! #'native-task-statuses #'native-core-records #'native-extended-records #'native-artifact-values #'native-plan-report
                  #'native-experience-artifact #'native-compiled-artifact]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
