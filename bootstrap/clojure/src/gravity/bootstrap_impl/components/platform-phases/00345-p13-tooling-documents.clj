

(def p13-tooling-documents
  ["T1" "T2" "T3" "T4" "T5" "T6" "T7" "T8" "T9" "T10" "T11" "T12" "T13"])

(def p13-tooling-phase-governing-documents
  {"T1" "docs/phase-13-tooling-and-developer-experience/177-t1-cli-specification.md"
   "T2" "docs/phase-13-tooling-and-developer-experience/178-t2-repl-ux-specification.md"
   "T3" "docs/phase-13-tooling-and-developer-experience/179-t3-formatter-specification.md"
   "T4" "docs/phase-13-tooling-and-developer-experience/180-t4-linter-specification.md"
   "T5" "docs/phase-13-tooling-and-developer-experience/181-t5-language-server-protocol-design.md"
   "T6" "docs/phase-13-tooling-and-developer-experience/182-t6-debugger-design.md"
   "T7" "docs/phase-13-tooling-and-developer-experience/183-t7-documentation-generator-design.md"
   "T8" "docs/phase-13-tooling-and-developer-experience/184-t8-dev-server-design.md"
   "T9" "docs/phase-13-tooling-and-developer-experience/185-t9-package-registry-ux-specification.md"
   "T10" "docs/phase-13-tooling-and-developer-experience/186-t10-compiler-explorer-and-ir-inspector-design.md"
   "T11" "docs/phase-13-tooling-and-developer-experience/187-t11-profiler-and-performance-inspector-design.md"
   "T12" "docs/phase-13-tooling-and-developer-experience/188-t12-safety-audit-explorer-design.md"
   "T13" "docs/phase-13-tooling-and-developer-experience/189-t13-ai-assisted-development-tooling-specification.md"})

(def p13-tooling-diagnostics-by-document
  {"T1" ["T1001" "T1002" "T1003" "T1004" "T1005" "T1006" "T1007" "T1008"]
   "T2" ["T2001" "T2002" "T2003" "T2004" "T2005" "T2006" "T2007"]
   "T3" ["T3001" "T3002" "T3003" "T3004" "T3005" "T3006"]
   "T4" ["T4001" "T4002" "T4003" "T4004" "T4005" "T4006"]
   "T5" ["T5001" "T5002" "T5003" "T5004" "T5005" "T5006"]
   "T6" ["T6001" "T6002" "T6003" "T6004" "T6005" "T6006" "T6007"]
   "T7" ["T7001" "T7002" "T7003" "T7004" "T7005" "T7006" "T7007"]
   "T8" ["T8001" "T8002" "T8003" "T8004" "T8005" "T8006" "T8007"]
   "T9" ["T9001" "T9002" "T9003" "T9004" "T9005" "T9006"]
   "T10" ["T10001" "T10002" "T10003" "T10004" "T10005" "T10006"]
   "T11" ["T11001" "T11002" "T11003" "T11004" "T11005" "T11006"]
   "T12" ["T12001" "T12002" "T12003" "T12004" "T12005" "T12006" "T12007"]
   "T13" ["T13001" "T13002" "T13003" "T13004" "T13005" "T13006" "T13007" "T13008"]})

(def p13-tooling-rejected-diagnostics
  {"T1" "T1003"
   "T2" "T2002"
   "T3" "T3002"
   "T4" "T4003"
   "T5" "T5001"
   "T6" "T6004"
   "T7" "T7001"
   "T8" "T8003"
   "T9" "T9001"
   "T10" "T10002"
   "T11" "T11003"
   "T12" "T12001"
   "T13" "T13002"})

(def p13-tooling-rejected-fixture-names
  {"T1" "tooling-t1-authority-denial.gravity"
   "T2" "tooling-t2-missing-capability.gravity"
   "T3" "tooling-t3-round-trip.gravity"
   "T4" "tooling-t4-unsafe-autofix.gravity"
   "T5" "tooling-t5-diagnostic-mismatch.gravity"
   "T6" "tooling-t6-redacted-access.gravity"
   "T7" "tooling-t7-stale-docs.gravity"
   "T8" "tooling-t8-hot-reload.gravity"
   "T9" "tooling-t9-hidden-capability-diff.gravity"
   "T10" "tooling-t10-lost-origin.gravity"
   "T11" "tooling-t11-check-elision.gravity"
   "T12" "tooling-t12-unsafe-island.gravity"
   "T13" "tooling-t13-generated-source.gravity"})

(def p13-tooling-diagnostic-ids
  (vec
   (distinct
    (concat (mapcat p13-tooling-diagnostics-by-document
                    p13-tooling-documents)
            ["P13-MANIFEST" "P13-ACCEPTED" "P13-REJECTED"
             "P13-CONFORMANCE"]))))

(def p13-tooling-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             p13-tooling-diagnostic-ids)))

(def p13-tooling-artifact-keys
  [:cli-command-set :repl-session-artifact :formatter-fixture
   :linter-diagnostic-report :lsp-capability-matrix :debugger-trace
   :documentation-artifact :dev-server-session :registry-ux-record
   :ir-inspector-bundle :profiler-report :safety-audit-report
   :ai-tooling-record :tooling-ui-data-model])

(def p13-tooling-document-summaries
  {"T1" {:title "CLI Specification"
         :owned-surface :cli-command-set
         :accepted-behavior :stable_capability_aware_command_surface
         :rejected-behavior "T1003"
         :artifact-keys [:cli-command-set]
         :dependencies #{"C15" "PKG1" "PKG12" "TEST1" "R12"}}
   "T2" {:title "REPL UX Specification"
         :owned-surface :repl-session-artifact
         :accepted-behavior :checked_interactive_session_artifact
         :rejected-behavior "T2002"
         :artifact-keys [:repl-session-artifact]
         :dependencies #{"L1" "L6" "L15" "R9" "C8" "A6"}}
   "T3" {:title "Formatter Specification"
         :owned-surface :formatter-fixture
         :accepted-behavior :reader_round_trip_format_report
         :rejected-behavior "T3002"
         :artifact-keys [:formatter-fixture]
         :dependencies #{"L1" "C2" "C3" "L4" "T1"}}
   "T4" {:title "Linter Specification"
         :owned-surface :linter-diagnostic-report
         :accepted-behavior :compiler_fact_backed_lint_report
         :rejected-behavior "T4003"
         :artifact-keys [:linter-diagnostic-report]
         :dependencies #{"C15" "L5" "L6" "L15" "SAFE6" "PKG12" "A11"}}
   "T5" {:title "Language Server Protocol Design"
         :owned-surface :lsp-capability-matrix
         :accepted-behavior :editor_view_over_compiler_state
         :rejected-behavior "T5001"
         :artifact-keys [:lsp-capability-matrix]
         :dependencies #{"C15" "C16" "L3" "L4" "C4" "T3" "T4" "PKG1"}}
   "T6" {:title "Debugger Design"
         :owned-surface :debugger-trace
         :accepted-behavior :source_mapped_authority_checked_debug_trace
         :rejected-behavior "T6004"
         :artifact-keys [:debugger-trace]
         :dependencies #{"C11" "C14" "R12" "B13" "A6" "A11" "SAFE6"}}
   "T7" {:title "Documentation Generator Design"
         :owned-surface :documentation-artifact
         :accepted-behavior :source_hash_checked_generated_docs
         :rejected-behavior "T7001"
         :artifact-keys [:documentation-artifact]
         :dependencies #{"L3" "L5" "L6" "L15" "S1" "PKG3" "PKG8" "T1"}}
   "T8" {:title "Dev Server Design"
         :owned-surface :dev-server-session
         :accepted-behavior :grant_bounded_incremental_dev_session
         :rejected-behavior "T8003"
         :artifact-keys [:dev-server-session]
         :dependencies #{"C16" "R9" "R12" "T1" "T2" "T5" "T6" "A6" "A11"}}
   "T9" {:title "Package Registry UX Specification"
         :owned-surface :registry-ux-record
         :accepted-behavior :structured_policy_visible_registry_view
         :rejected-behavior "T9001"
         :artifact-keys [:registry-ux-record]
         :dependencies #{"PKG4" "PKG6" "PKG8" "PKG9" "PKG10" "PKG11" "PKG12" "GOV10"}}
   "T10" {:title "Compiler Explorer and IR Inspector Design"
          :owned-surface :ir-inspector-bundle
          :accepted-behavior :source_origin_preserving_ir_bundle
          :rejected-behavior "T10002"
          :artifact-keys [:ir-inspector-bundle]
          :dependencies #{"C2" "C18" "L4" "L5" "L6" "SAFE15" "PERF10" "B13" "T1"}}
   "T11" {:title "Profiler and Performance Inspector Design"
          :owned-surface :profiler-report
          :accepted-behavior :identity_complete_proof_linked_performance_report
          :rejected-behavior "T11003"
          :artifact-keys [:profiler-report]
          :dependencies #{"PERF1" "PERF10" "C13" "R12" "B3" "B8" "TEST12" "A9"}}
   "T12" {:title "Safety Audit Explorer Design"
          :owned-surface :safety-audit-report
          :accepted-behavior :evidence_visible_safety_audit_export
          :rejected-behavior "T12001"
          :artifact-keys [:safety-audit-report]
          :dependencies #{"SAFE1" "SAFE16" "PKG8" "PKG10" "PKG12" "A8" "A11" "C15" "R12"}}
   "T13" {:title "AI-Assisted Development Tooling Specification"
          :owned-surface :ai-tooling-record
          :accepted-behavior :policy_checked_ai_patch_artifact
          :rejected-behavior "T13002"
          :artifact-keys [:ai-tooling-record]
          :dependencies #{"A1" "A11" "T1" "T4" "T10" "T12" "PKG10" "TEST13"}}})

(defn p13-document-number
  [document]
  (Integer/parseInt (subs document 1)))

(defn p13-task-id
  [document]
  (str "P13-D" (+ 176 (p13-document-number document))))

(defn p13-tooling-source-overrides
  [module]
  (get-in module [:metadata :tooling :experience] {}))

(defn p13-tooling-diagnostic-document
  [diagnostic-id]
  (some (fn [document]
          (when (some #(= diagnostic-id %)
                      (p13-tooling-diagnostics-by-document document))
            document))
        p13-tooling-documents))

(defn p13-tooling-fail!
  [id source-path subject extra]
  (let [document (or (:document-id subject)
                     (p13-tooling-diagnostic-document id))]
    (fail! id
           "P13 tooling and developer-experience validation failed"
           (merge {:source-span (or (:source-span subject)
                                    (source-span source-path 0))
                   :diagnostic-family :phase13-tooling-experience
                   :stage :tooling-experience
                   :document-id document
                   :task (when document (p13-task-id document))
                   :tool-id (or (:tool-id subject) :gravity-tooling)
                   :artifact-id (:artifact-id subject)
                   :missing-fact (:missing-fact subject)
                   :fallback-status :rejected
                   :remediation "Phase 13 requires tooling to expose compiler, package, runtime, safety, performance, and AI truth through structured artifacts; accepted and rejected fixtures, stable diagnostics, redaction, authority checks, and capability-based proof must all be present."}
                  extra))))

(defn p13-tooling-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (if-let [id (get p13-tooling-override-diagnostics fail-kind)]
      (p13-tooling-fail!
       id source-path
       {:artifact-id (str "p13-tooling-" (name fail-kind))
        :document-id (p13-tooling-diagnostic-document id)
        :missing-fact fail-kind}
       {:missing-fields [fail-kind]})
      (p13-tooling-fail!
       "P13-MANIFEST" source-path
       {:artifact-id "p13-tooling-unknown-override"
        :missing-fact fail-kind}
       {:missing-fields [:known-override-diagnostic]}))))