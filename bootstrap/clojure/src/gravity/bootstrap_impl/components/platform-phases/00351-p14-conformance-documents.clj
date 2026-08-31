

(def p14-conformance-documents
  ["TEST1" "TEST2" "TEST3" "TEST4" "TEST5" "TEST6" "TEST7"
   "TEST8" "TEST9" "TEST10" "TEST11" "TEST12" "TEST13"])

(def p14-conformance-phase-governing-documents
  {"TEST1" "docs/phase-14-testing-verification-and-conformance/190-test1-language-conformance-test-plan.md"
   "TEST2" "docs/phase-14-testing-verification-and-conformance/191-test2-compiler-test-strategy.md"
   "TEST3" "docs/phase-14-testing-verification-and-conformance/192-test3-runtime-test-strategy.md"
   "TEST4" "docs/phase-14-testing-verification-and-conformance/193-test4-profile-compliance-test-plan.md"
   "TEST5" "docs/phase-14-testing-verification-and-conformance/194-test5-safety-conformance-test-plan.md"
   "TEST6" "docs/phase-14-testing-verification-and-conformance/195-test6-backend-conformance-test-plan.md"
   "TEST7" "docs/phase-14-testing-verification-and-conformance/196-test7-standard-library-test-strategy.md"
   "TEST8" "docs/phase-14-testing-verification-and-conformance/197-test8-ai-and-workflow-evaluation-strategy.md"
   "TEST9" "docs/phase-14-testing-verification-and-conformance/198-test9-fuzzing-and-property-testing-plan.md"
   "TEST10" "docs/phase-14-testing-verification-and-conformance/199-test10-differential-testing-strategy.md"
   "TEST11" "docs/phase-14-testing-verification-and-conformance/200-test11-formal-semantics-and-verification-plan.md"
   "TEST12" "docs/phase-14-testing-verification-and-conformance/201-test12-performance-regression-test-plan.md"
   "TEST13" "docs/phase-14-testing-verification-and-conformance/202-test13-self-hosting-validation-plan.md"})

(def p14-conformance-diagnostics-by-document
  {"TEST1" ["TEST1001" "TEST1002" "TEST1003" "TEST1004" "TEST1005" "TEST1006"]
   "TEST2" ["TEST2001" "TEST2002" "TEST2003" "TEST2004" "TEST2005" "TEST2006"]
   "TEST3" ["TEST3001" "TEST3002" "TEST3003" "TEST3004" "TEST3005" "TEST3006"]
   "TEST4" ["TEST4001" "TEST4002" "TEST4003" "TEST4004" "TEST4005" "TEST4006"]
   "TEST5" ["TEST5001" "TEST5002" "TEST5003" "TEST5004" "TEST5005" "TEST5006" "TEST5007"]
   "TEST6" ["TEST6001" "TEST6002" "TEST6003" "TEST6004" "TEST6005" "TEST6006" "TEST6007"]
   "TEST7" ["TEST7001" "TEST7002" "TEST7003" "TEST7004" "TEST7005" "TEST7006"]
   "TEST8" ["TEST8001" "TEST8002" "TEST8003" "TEST8004" "TEST8005" "TEST8006" "TEST8007"]
   "TEST9" ["TEST9001" "TEST9002" "TEST9003" "TEST9004" "TEST9005" "TEST9006"]
   "TEST10" ["TEST10001" "TEST10002" "TEST10003" "TEST10004" "TEST10005" "TEST10006"]
   "TEST11" ["TEST11001" "TEST11002" "TEST11003" "TEST11004" "TEST11005" "TEST11006" "TEST11007"]
   "TEST12" ["TEST12001" "TEST12002" "TEST12003" "TEST12004" "TEST12005" "TEST12006"]
   "TEST13" ["TEST13001" "TEST13002" "TEST13003" "TEST13004" "TEST13005" "TEST13006" "TEST13007"]})

(def p14-conformance-rejected-diagnostics
  {"TEST1" "TEST1001"
   "TEST2" "TEST2002"
   "TEST3" "TEST3002"
   "TEST4" "TEST4001"
   "TEST5" "TEST5002"
   "TEST6" "TEST6004"
   "TEST7" "TEST7001"
   "TEST8" "TEST8003"
   "TEST9" "TEST9001"
   "TEST10" "TEST10002"
   "TEST11" "TEST11003"
   "TEST12" "TEST12003"
   "TEST13" "TEST13002"})

(def p14-conformance-rejected-fixture-names
  {"TEST1" "conformance-test1-metadata.gravity"
   "TEST2" "conformance-test2-preserved-fact.gravity"
   "TEST3" "conformance-test3-capability.gravity"
   "TEST4" "conformance-test4-profile-target.gravity"
   "TEST5" "conformance-test5-unsafe-audit.gravity"
   "TEST6" "conformance-test6-artifact-manifest.gravity"
   "TEST7" "conformance-test7-untested-api.gravity"
   "TEST8" "conformance-test8-replay-trace.gravity"
   "TEST9" "conformance-test9-seed.gravity"
   "TEST10" "conformance-test10-divergence.gravity"
   "TEST11" "conformance-test11-proof.gravity"
   "TEST12" "conformance-test12-semantic-gate.gravity"
   "TEST13" "conformance-test13-provenance.gravity"})

(def p14-conformance-diagnostic-ids
  (vec
   (distinct
    (concat (mapcat p14-conformance-diagnostics-by-document
                    p14-conformance-documents)
            ["P14-MANIFEST" "P14-ACCEPTED" "P14-REJECTED"
             "P14-CONFORMANCE"]))))

(def p14-conformance-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             p14-conformance-diagnostic-ids)))

(def p14-conformance-artifact-keys
  [:conformance-harness :fixture-manifest :golden-diagnostics
   :fuzz-property-suite :differential-report :formal-proof-report
   :performance-regression-report :language-conformance
   :compiler-test-report :runtime-conformance-report
   :profile-compliance-report :safety-conformance-report
   :backend-conformance-report :standard-library-test-report
   :ai-workflow-eval-report :self-hosting-validation-report])

(def p14-conformance-document-summaries
  {"TEST1" {:title "Language Conformance Test Plan"
            :owned-surface :language-conformance
            :accepted-behavior :portable_language_fixture_matrix
            :rejected-behavior "TEST1001"
            :artifact-keys [:language-conformance :fixture-manifest
                            :golden-diagnostics]
            :dependencies #{"L1" "L19" "C2" "C8" "C15" "P13" "SAFE1"}}
   "TEST2" {:title "Compiler Test Strategy"
            :owned-surface :compiler-test-report
            :accepted-behavior :compiler_stage_preservation_report
            :rejected-behavior "TEST2002"
            :artifact-keys [:compiler-test-report :golden-diagnostics]
            :dependencies #{"C1" "C18" "L15" "SAFE16" "B13" "T10"}}
   "TEST3" {:title "Runtime Test Strategy"
            :owned-surface :runtime-conformance-report
            :accepted-behavior :runtime_family_conformance_report
            :rejected-behavior "TEST3002"
            :artifact-keys [:runtime-conformance-report]
            :dependencies #{"R1" "R12" "L6" "L15" "SAFE13" "A11" "B13"}}
   "TEST4" {:title "Profile Compliance Test Plan"
            :owned-surface :profile-compliance-report
            :accepted-behavior :profile_target_matrix_report
            :rejected-behavior "TEST4001"
            :artifact-keys [:profile-compliance-report]
            :dependencies #{"P1" "P13" "L6" "L15" "R12" "B14" "PKG11"}}
   "TEST5" {:title "Safety Conformance Test Plan"
            :owned-surface :safety-conformance-report
            :accepted-behavior :safe_gravity_outcome_conformance
            :rejected-behavior "TEST5002"
            :artifact-keys [:safety-conformance-report]
            :dependencies #{"SAFE1" "SAFE16" "L10" "L11" "C9" "C10" "PERF10" "A11" "PKG8"}}
   "TEST6" {:title "Backend Conformance Test Plan"
            :owned-surface :backend-conformance-report
            :accepted-behavior :backend_matrix_conformance
            :rejected-behavior "TEST6004"
            :artifact-keys [:backend-conformance-report
                            :differential-report]
            :dependencies #{"B1" "B14" "C11" "C14" "R12" "PKG11" "TEST10"}}
   "TEST7" {:title "Standard Library Test Strategy"
            :owned-surface :standard-library-test-report
            :accepted-behavior :stdlib_profile_capability_test_matrix
            :rejected-behavior "TEST7001"
            :artifact-keys [:standard-library-test-report
                            :fuzz-property-suite]
            :dependencies #{"STD1" "STD20" "P13" "SAFE16" "S9" "TEST9" "T7"}}
   "TEST8" {:title "AI and Workflow Evaluation Strategy"
            :owned-surface :ai-workflow-eval-report
            :accepted-behavior :auditable_ai_workflow_eval
            :rejected-behavior "TEST8003"
            :artifact-keys [:ai-workflow-eval-report]
            :dependencies #{"A1" "A11" "B10" "R7" "R8" "S1" "TEST9" "T13"}}
   "TEST9" {:title "Fuzzing and Property Testing Plan"
            :owned-surface :fuzz-property-suite
            :accepted-behavior :replayable_seeded_property_suite
            :rejected-behavior "TEST9001"
            :artifact-keys [:fuzz-property-suite]
            :dependencies #{"L1" "L6" "C11" "C13" "S3" "MATH11" "A11"}}
   "TEST10" {:title "Differential Testing Strategy"
             :owned-surface :differential-report
             :accepted-behavior :declared_oracle_comparison_report
             :rejected-behavior "TEST10002"
             :artifact-keys [:differential-report]
             :dependencies #{"L2" "C11" "B14" "R12" "MATH7" "MATH8" "BOOT7"}}
   "TEST11" {:title "Formal Semantics and Verification Plan"
             :owned-surface :formal-proof-report
             :accepted-behavior :machine_checkable_claim_report
             :rejected-behavior "TEST11003"
             :artifact-keys [:formal-proof-report]
             :dependencies #{"D9" "P12" "SAFE15" "C18" "MATH11" "TEST10"}}
   "TEST12" {:title "Performance Regression Test Plan"
             :owned-surface :performance-regression-report
             :accepted-behavior :semantic_gate_checked_performance_report
             :rejected-behavior "TEST12003"
             :artifact-keys [:performance-regression-report]
             :dependencies #{"PERF1" "PERF10" "T11" "PKG11" "TEST10" "A9"}}
   "TEST13" {:title "Self-Hosting Validation Plan"
             :owned-surface :self-hosting-validation-report
             :accepted-behavior :bootstrap_stage_provenance_report
             :rejected-behavior "TEST13002"
             :artifact-keys [:self-hosting-validation-report]
             :dependencies #{"BOOT1" "BOOT8" "C18" "PKG7" "PKG10" "TEST12" "GOV9"}}})

(defn p14-document-number
  [document]
  (Integer/parseInt (subs document 4)))

(defn p14-task-id
  [document]
  (str "P14-D" (+ 189 (p14-document-number document))))

(defn p14-conformance-source-overrides
  [module]
  (get-in module [:metadata :conformance :system] {}))

(defn p14-conformance-diagnostic-document
  [diagnostic-id]
  (some (fn [document]
          (when (some #(= diagnostic-id %)
                      (p14-conformance-diagnostics-by-document document))
            document))
        p14-conformance-documents))

(defn p14-conformance-fail!
  [id source-path subject extra]
  (let [document (or (:document-id subject)
                     (p14-conformance-diagnostic-document id))]
    (fail! id
           "P14 testing, verification, and conformance validation failed"
           (merge {:source-span (or (:source-span subject)
                                    (source-span source-path 0))
                   :diagnostic-family :phase14-conformance-system
                   :stage :conformance-system
                   :document-id document
                   :task (when document (p14-task-id document))
                   :suite-id (or (:suite-id subject) :gravity-conformance)
                   :artifact-id (:artifact-id subject)
                   :missing-fact (:missing-fact subject)
                   :fallback-status :rejected
                   :remediation "Phase 14 requires explicit fixture metadata, stable diagnostics, capability-gated runtime tests, preservation evidence, reproducible fuzzing, differential oracles, formal proof records, performance gates, and bootstrap provenance before conformance tasks can complete."}
                  extra))))

(defn p14-conformance-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (if-let [id (get p14-conformance-override-diagnostics fail-kind)]
      (p14-conformance-fail!
       id source-path
       {:artifact-id (str "p14-conformance-" (name fail-kind))
        :document-id (p14-conformance-diagnostic-document id)
        :missing-fact fail-kind}
       {:missing-fields [fail-kind]})
      (p14-conformance-fail!
       "P14-MANIFEST" source-path
       {:artifact-id "p14-conformance-unknown-override"
        :missing-fact fail-kind}
       {:missing-fields [:known-override-diagnostic]}))))