

(def p15-s23-whole-language-self-hosting-required-evidence
  [{:key :whole-language-compiler-artifact
    :diagnostic "P15S23001"
    :governing-documents ["BOOT3" "C1"]
    :description :gravity_compiler_compiles_claimed_language_subset}
   {:key :compiler-pipeline-manifest
    :diagnostic "P15S23002"
    :governing-documents ["C1" "BOOT7"]
    :description :pipeline_manifest_preserves_stage_contracts}
   {:key :source-unit-and-syntax-serialization-proof
    :diagnostic "P15S23003"
    :governing-documents ["C2" "C3"]
    :description :source_and_syntax_identity_survive_self_hosting}
   {:key :core-lowering-and-diagnostic-preservation-report
    :diagnostic "P15S23004"
    :governing-documents ["C6" "C15"]
    :description :core_lowering_and_diagnostics_match_seed_stage}
   {:key :runtime-manifest-and-capability-enforcement-report
    :diagnostic "P15S23005"
    :governing-documents ["R1" "R11"]
    :description :runtime_services_and_capabilities_are_explicit}
   {:key :accepted-app-execution-proof
    :diagnostic "P15S23006"
    :governing-documents ["BOOT7" "TEST13" "R1"]
    :description
    :nontrivial_gravity_app_runs_through_current_compiled_candidate}
   {:key :rejected-app-diagnostic-proof
    :diagnostic "P15S23007"
    :governing-documents ["BOOT7" "TEST13" "C15"]
    :description :invalid_gravity_app_fails_closed_with_stable_diagnostics}
   {:key :reproducible-rebuild-log
    :diagnostic "P15S23008"
    :governing-documents ["BOOT6" "PKG7" "TEST13"]
    :description :self_hosted_rebuild_is_reproducible}
   {:key :stage-comparison-report
    :diagnostic "P15S23009"
    :governing-documents ["BOOT6" "BOOT7" "TEST13"]
    :description :self_hosted_and_seed_stage_outputs_are_equivalent}
   {:key :conformance-report
    :diagnostic "P15S23010"
    :governing-documents ["BOOT1" "BOOT7" "TEST13"]
    :description :self_hosted_stage_passes_conformance}
   {:key :provenance-attestation
    :diagnostic "P15S23011"
    :governing-documents ["BOOT8" "PKG7" "GOV10"]
    :description :compiler_lineage_and_release_inputs_are_attested}
   {:key :tcb-delta-record
    :diagnostic "P15S23012"
    :governing-documents ["BOOT1" "BOOT6" "TEST13"]
    :description :trusted_computing_base_delta_is_recorded}
   {:key :unsafe-audit-report
    :diagnostic "P15S23013"
    :governing-documents ["BOOT1" "BOOT7" "TEST13"]
    :description :unsafe_islands_are_audited_or_absent}
   {:key :governance-and-package-release-record
    :diagnostic "P15S23015"
    :governing-documents ["GOV6" "GOV10" "BOOT8"]
    :description :release_governance_and_package_policy_are_satisfied}])


(def stage1-reader-pipeline-rejected-fixture-records
  (vec
   (concat
    stage1-reader-execution-rejected-fixture-records
    [{:fixture stage1-reader-source-path
      :diagnostic "STAGE1PIPE001"
      :rejected-behavior :missing-gravity-reader-pipeline-entrypoint}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1PIPE002"
      :rejected-behavior :unsupported-gravity-reader-pipeline-form}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1PIPE003"
      :rejected-behavior :unsupported-reader-pipeline-host-primitive}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1PIPE004"
      :rejected-behavior :invalid-reader-token-stream}
     {:fixture "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
      :diagnostic "STAGE1PIPE005"
      :rejected-behavior :stage0-reader-divergence}])))

(def stage1-reader-character-pipeline-rejected-fixture-records
  (vec
   (concat
    stage1-reader-execution-rejected-fixture-records
    [{:fixture stage1-reader-source-path
      :diagnostic "STAGE1CHAR001"
      :rejected-behavior :missing-gravity-reader-character-pipeline-entrypoint}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1CHAR002"
      :rejected-behavior :unsupported-gravity-reader-character-pipeline-form}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1CHAR003"
      :rejected-behavior :unsupported-reader-character-pipeline-host-primitive}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1CHAR004"
      :rejected-behavior :invalid-reader-character-stream}
     {:fixture "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
      :diagnostic "STAGE1CHAR005"
      :rejected-behavior :stage0-reader-divergence}])))

(def stage1-reader-token-classifier-pipeline-rejected-fixture-records
  (vec
   (concat
    stage1-reader-execution-rejected-fixture-records
    [{:fixture stage1-reader-source-path
      :diagnostic "STAGE1CLASS001"
      :rejected-behavior :missing-gravity-reader-token-classifier-pipeline-entrypoint}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1CLASS002"
      :rejected-behavior :unsupported-gravity-reader-token-classifier-pipeline-form}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1CLASS003"
      :rejected-behavior :unsupported-reader-token-classifier-pipeline-host-primitive}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1CLASS004"
      :rejected-behavior :invalid-reader-token-classifier}
     {:fixture "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
      :diagnostic "STAGE1CLASS005"
      :rejected-behavior :stage0-reader-divergence}])))

(def stage1-reader-token-realizer-pipeline-rejected-fixture-records
  (vec
   (concat
    stage1-reader-execution-rejected-fixture-records
    [{:fixture stage1-reader-source-path
      :diagnostic "STAGE1REAL001"
      :rejected-behavior :missing-gravity-reader-token-realizer-pipeline-entrypoint}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1REAL002"
      :rejected-behavior :unsupported-gravity-reader-token-realizer-pipeline-form}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1REAL003"
      :rejected-behavior :unsupported-reader-token-realizer-pipeline-host-primitive}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1REAL004"
      :rejected-behavior :invalid-reader-token-realizer}
     {:fixture "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
      :diagnostic "STAGE1REAL005"
      :rejected-behavior :stage0-reader-divergence}])))

(def stage1-reader-token-automaton-pipeline-rejected-fixture-records
  (vec
   (concat
    stage1-reader-execution-rejected-fixture-records
    [{:fixture stage1-reader-source-path
      :diagnostic "STAGE1AUTO001"
      :rejected-behavior :missing-gravity-reader-token-automaton-pipeline-entrypoint}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1AUTO002"
      :rejected-behavior :unsupported-gravity-reader-token-automaton-pipeline-form}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1AUTO003"
      :rejected-behavior :unsupported-reader-token-automaton-pipeline-host-primitive}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1AUTO004"
      :rejected-behavior :invalid-reader-token-automaton}
     {:fixture "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
      :diagnostic "STAGE1AUTO005"
      :rejected-behavior :stage0-reader-divergence}])))

(def stage1-reader-form-builder-pipeline-rejected-fixture-records
  (vec
   (concat
    stage1-reader-execution-rejected-fixture-records
    [{:fixture stage1-reader-source-path
      :diagnostic "STAGE1FORM001"
      :rejected-behavior :missing-gravity-reader-form-builder-pipeline-entrypoint}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1FORM002"
      :rejected-behavior :unsupported-gravity-reader-form-builder-pipeline-form}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1FORM003"
      :rejected-behavior :unsupported-reader-form-builder-pipeline-host-primitive}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1FORM004"
      :rejected-behavior :invalid-reader-form-builder}
     {:fixture "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
      :diagnostic "STAGE1FORM005"
      :rejected-behavior :stage0-reader-divergence}])))

(def stage1-reader-executor-pipeline-rejected-fixture-records
  (vec
   (concat
    stage1-reader-execution-rejected-fixture-records
    [{:fixture stage1-reader-source-path
      :diagnostic "STAGE1EXEC001"
      :rejected-behavior :missing-gravity-reader-executor-pipeline-entrypoint}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1EXEC002"
      :rejected-behavior :unsupported-gravity-reader-executor-pipeline-form}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1EXEC003"
      :rejected-behavior :unsupported-reader-executor-pipeline-host-primitive}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1EXEC004"
      :rejected-behavior :invalid-reader-executor}
     {:fixture "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
      :diagnostic "STAGE1EXEC005"
      :rejected-behavior :stage0-reader-divergence}])))

(def stage1-reader-runtime-pipeline-rejected-fixture-records
  (vec
   (concat
    stage1-reader-execution-rejected-fixture-records
    [{:fixture stage1-reader-source-path
      :diagnostic "STAGE1RUN001"
      :rejected-behavior :missing-gravity-reader-runtime-pipeline-entrypoint}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1RUN002"
      :rejected-behavior :unsupported-gravity-reader-runtime-pipeline-form}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1RUN003"
      :rejected-behavior :unsupported-reader-runtime-host-primitive}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1RUN004"
      :rejected-behavior :invalid-reader-runtime-record}
     {:fixture "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
      :diagnostic "STAGE1RUN005"
      :rejected-behavior :stage0-reader-divergence}])))