

(def p15-s23-compiler-source-rejected-candidates
  [{:fixture :internal-p15-s23-compiler-missing-stage
    :candidate {}
    :expected-diagnostic "P15S23C001"}
   {:fixture :internal-p15-s23-compiler-pipeline-gap
    :candidate {:engine :gravity-p15-s23-compiler-source-inventory-v1
                :canonical-pipeline [:read-source]
                :source-modules []
                :required-evidence []
                :self-hosting-claims
                {:full-language-compiler-self-hosted? false
                 :clojure-seed-retired? false}
                :seed-boundary {:still-trusted? true}}
    :expected-diagnostic "P15S23C002"}
   {:fixture :internal-p15-s23-compiler-source-gap
    :candidate {:engine :gravity-p15-s23-compiler-source-inventory-v1
                :canonical-pipeline p15-s23-canonical-compiler-pipeline
                :source-modules []
                :required-evidence (vec (p15-s23-required-evidence-keys))
                :self-hosting-claims
                {:full-language-compiler-self-hosted? false
                 :clojure-seed-retired? false}
                :seed-boundary {:still-trusted? true}}
    :expected-diagnostic "P15S23C003"}
   {:fixture :internal-p15-s23-compiler-evidence-gap
    :candidate {:engine :gravity-p15-s23-compiler-source-inventory-v1
                :canonical-pipeline p15-s23-canonical-compiler-pipeline
                :source-modules
                [{:component :reader
                  :path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"}
                 {:component :syntax
                  :path "bootstrap/gravity/src/gravity/bootstrap/syntax.gravity"}
                 {:component :diagnostics
                  :path "bootstrap/gravity/src/gravity/bootstrap/diagnostics.gravity"}
                 {:component :source-frontend
                  :path "bootstrap/gravity/src/gravity/compiler/l1_c2_surface_syntax_reader.gravity"}
                 {:component :syntax-object-model
                  :path "bootstrap/gravity/src/gravity/compiler/c3_syntax_object_model.gravity"}
                 {:component :macro-expansion
                  :path "bootstrap/gravity/src/gravity/compiler/c4_macro_expansion_engine.gravity"}
                 {:component :name-resolution
                  :path "bootstrap/gravity/src/gravity/compiler/c5_name_resolution_namespace_analyzer.gravity"}
                 {:component :core-semantics
                  :path "bootstrap/gravity/src/gravity/compiler/l2_core_language_semantics.gravity"}
                 {:component :core-lowering
                  :path "bootstrap/gravity/src/gravity/compiler/c6_core_lowering_engine.gravity"}
                 {:component :type-checker
                  :path "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity"}
                 {:component :effect-checker
                  :path "bootstrap/gravity/src/gravity/compiler/c8_effect_checker_engine.gravity"}
                 {:component :ownership-checker
                  :path "bootstrap/gravity/src/gravity/compiler/c9_ownership_checker_engine.gravity"}
                 {:component :safety-analysis
                  :path "bootstrap/gravity/src/gravity/compiler/c10_safety_analysis_pipeline.gravity"}
                 {:component :mir-specification
                  :path "bootstrap/gravity/src/gravity/compiler/c11_mir_specification.gravity"}
                 {:component :domain-ir-architecture
                  :path "bootstrap/gravity/src/gravity/compiler/c12_domain_ir_architecture.gravity"}
                 {:component :mir-optimization
                  :path "bootstrap/gravity/src/gravity/compiler/c13_mir_optimization_passes.gravity"}
                 {:component :target-lowering
                  :path "bootstrap/gravity/src/gravity/compiler/c14_target_lowering_architecture.gravity"}
                 {:component :compiler-diagnostics
                  :path "bootstrap/gravity/src/gravity/compiler/c15_compiler_diagnostics.gravity"}
                 {:component :incremental-compilation
                  :path "bootstrap/gravity/src/gravity/compiler/c16_incremental_compilation_design.gravity"}
                 {:component :compiler-plugin-pass-api
                  :path "bootstrap/gravity/src/gravity/compiler/c17_compiler_plugin_pass_api.gravity"}
                 {:component :compiler-verification
                  :path "bootstrap/gravity/src/gravity/compiler/c18_compiler_verification_pass_correctness.gravity"}
                 {:component :backend-interface
                  :path "bootstrap/gravity/src/gravity/backend/b1_backend_interface_specification.gravity"}
                 {:component :c-backend
                  :path "bootstrap/gravity/src/gravity/backend/b2_c_backend_design.gravity"}
                 {:component :llvm-backend
                  :path "bootstrap/gravity/src/gravity/backend/b3_llvm_backend_design.gravity"}
                 {:component :wasm-backend
                  :path "bootstrap/gravity/src/gravity/backend/b4_wasm_backend_design.gravity"}
                 {:component :jvm-backend
                  :path "bootstrap/gravity/src/gravity/backend/b5_jvm_backend_design.gravity"}
                 {:component :js-ts-backend
                  :path "bootstrap/gravity/src/gravity/backend/b6_javascript_typescript_backend_design.gravity"}
                 {:component :mlir-backend
                  :path "bootstrap/gravity/src/gravity/backend/b7_mlir_backend_design.gravity"}
                 {:component :gpu-backend
                  :path "bootstrap/gravity/src/gravity/backend/b8_gpu_backend_design.gravity"}
                 {:component :hdl-backend
                  :path "bootstrap/gravity/src/gravity/backend/b9_hdl_backend_design.gravity"}
                 {:component :workflow-backend
                  :path "bootstrap/gravity/src/gravity/backend/b10_workflow_graph_backend_design.gravity"}
                 {:component :query-backend
                  :path "bootstrap/gravity/src/gravity/backend/b11_query_relational_backend_design.gravity"}
                 {:component :mobile-backend
                  :path "bootstrap/gravity/src/gravity/backend/b12_mobile_backend_design.gravity"}
                 {:component :compiler-source-inventory
                  :path p15-s23-compiler-source-path}]
                :required-evidence []
                :self-hosting-claims
                {:full-language-compiler-self-hosted? false
                 :clojure-seed-retired? false}
                :seed-boundary {:still-trusted? true}}
    :expected-diagnostic "P15S23C004"}
   {:fixture :internal-p15-s23-compiler-overclaim
    :candidate {:engine :gravity-p15-s23-compiler-source-inventory-v1
                :canonical-pipeline p15-s23-canonical-compiler-pipeline
                :source-modules
                [{:component :reader
                  :path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"}
                 {:component :syntax
                  :path "bootstrap/gravity/src/gravity/bootstrap/syntax.gravity"}
                 {:component :diagnostics
                  :path "bootstrap/gravity/src/gravity/bootstrap/diagnostics.gravity"}
                 {:component :source-frontend
                  :path "bootstrap/gravity/src/gravity/compiler/l1_c2_surface_syntax_reader.gravity"}
                 {:component :syntax-object-model
                  :path "bootstrap/gravity/src/gravity/compiler/c3_syntax_object_model.gravity"}
                 {:component :macro-expansion
                  :path "bootstrap/gravity/src/gravity/compiler/c4_macro_expansion_engine.gravity"}
                 {:component :name-resolution
                  :path "bootstrap/gravity/src/gravity/compiler/c5_name_resolution_namespace_analyzer.gravity"}
                 {:component :core-semantics
                  :path "bootstrap/gravity/src/gravity/compiler/l2_core_language_semantics.gravity"}
                 {:component :core-lowering
                  :path "bootstrap/gravity/src/gravity/compiler/c6_core_lowering_engine.gravity"}
                 {:component :type-checker
                  :path "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity"}
                 {:component :effect-checker
                  :path "bootstrap/gravity/src/gravity/compiler/c8_effect_checker_engine.gravity"}
                 {:component :ownership-checker
                  :path "bootstrap/gravity/src/gravity/compiler/c9_ownership_checker_engine.gravity"}
                 {:component :safety-analysis
                  :path "bootstrap/gravity/src/gravity/compiler/c10_safety_analysis_pipeline.gravity"}
                 {:component :mir-specification
                  :path "bootstrap/gravity/src/gravity/compiler/c11_mir_specification.gravity"}
                 {:component :domain-ir-architecture
                  :path "bootstrap/gravity/src/gravity/compiler/c12_domain_ir_architecture.gravity"}
                 {:component :mir-optimization
                  :path "bootstrap/gravity/src/gravity/compiler/c13_mir_optimization_passes.gravity"}
                 {:component :target-lowering
                  :path "bootstrap/gravity/src/gravity/compiler/c14_target_lowering_architecture.gravity"}
                 {:component :compiler-diagnostics
                  :path "bootstrap/gravity/src/gravity/compiler/c15_compiler_diagnostics.gravity"}
                 {:component :incremental-compilation
                  :path "bootstrap/gravity/src/gravity/compiler/c16_incremental_compilation_design.gravity"}
                 {:component :compiler-plugin-pass-api
                  :path "bootstrap/gravity/src/gravity/compiler/c17_compiler_plugin_pass_api.gravity"}
                 {:component :compiler-verification
                  :path "bootstrap/gravity/src/gravity/compiler/c18_compiler_verification_pass_correctness.gravity"}
                 {:component :backend-interface
                  :path "bootstrap/gravity/src/gravity/backend/b1_backend_interface_specification.gravity"}
                 {:component :c-backend
                  :path "bootstrap/gravity/src/gravity/backend/b2_c_backend_design.gravity"}
                 {:component :llvm-backend
                  :path "bootstrap/gravity/src/gravity/backend/b3_llvm_backend_design.gravity"}
                 {:component :wasm-backend
                  :path "bootstrap/gravity/src/gravity/backend/b4_wasm_backend_design.gravity"}
                 {:component :jvm-backend
                  :path "bootstrap/gravity/src/gravity/backend/b5_jvm_backend_design.gravity"}
                 {:component :js-ts-backend
                  :path "bootstrap/gravity/src/gravity/backend/b6_javascript_typescript_backend_design.gravity"}
                 {:component :mlir-backend
                  :path "bootstrap/gravity/src/gravity/backend/b7_mlir_backend_design.gravity"}
                 {:component :gpu-backend
                  :path "bootstrap/gravity/src/gravity/backend/b8_gpu_backend_design.gravity"}
                 {:component :hdl-backend
                  :path "bootstrap/gravity/src/gravity/backend/b9_hdl_backend_design.gravity"}
                 {:component :workflow-backend
                  :path "bootstrap/gravity/src/gravity/backend/b10_workflow_graph_backend_design.gravity"}
                 {:component :query-backend
                  :path "bootstrap/gravity/src/gravity/backend/b11_query_relational_backend_design.gravity"}
                 {:component :mobile-backend
                  :path "bootstrap/gravity/src/gravity/backend/b12_mobile_backend_design.gravity"}
                 {:component :compiler-source-inventory
                  :path p15-s23-compiler-source-path}]
                :required-evidence (vec (p15-s23-required-evidence-keys))
                :self-hosting-claims
                {:full-language-compiler-self-hosted? true
                 :clojure-seed-retired? true}
                :seed-boundary {:still-trusted? false}}
    :expected-diagnostic "P15S23C005"}])

(defn p15-s23-compiler-source-rejected-records
  [source-path]
  (mapv (fn [{:keys [fixture candidate expected-diagnostic]}]
          (let [diagnostics
                (p15-s23-compiler-source-inventory-diagnostics
                 source-path candidate)]
            {:fixture fixture
             :status :rejected
             :expected-diagnostic expected-diagnostic
             :diagnostics diagnostics}))
        p15-s23-compiler-source-rejected-candidates))

(defn p15-s23-compiler-source-inventory-proof
  [artifact]
  (let [compiler-stage (:compiler-stage artifact)
        diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:p15-s23-compiler-source-inventory-diagnostic-stream
                           :diagnostics])))
        rejected-diagnostics
        (set (mapcat #(map :diagnostic (:diagnostics %))
                     (:rejected-p15-s23-compiler-source-fixtures artifact)))]
    {:compiler-source-authored-in-gravity? true
     :status :in-progress
     :task "P15-S23"
     :canonical-pipeline-covered?
     (= p15-s23-canonical-compiler-pipeline
        (:canonical-pipeline compiler-stage))
     :source-inventory-covered?
     (= p15-s23-compiler-source-components
        (set (map :component (:source-inventory artifact))))
     :required-evidence-enumerated?
     (= (p15-s23-required-evidence-keys)
        (set (:required-evidence compiler-stage)))
     :does-not-claim-full-self-hosting?
     (false? (get-in compiler-stage
                     [:self-hosting-claims
                      :full-language-compiler-self-hosted?]))
     :does-not-claim-clojure-seed-retirement?
     (false? (get-in compiler-stage
                     [:self-hosting-claims :clojure-seed-retired?]))
     :seed-boundary-explicit?
     (true? (get-in compiler-stage [:seed-boundary :still-trusted?]))
     :rejected-candidates-covered?
     (set/subset? (set p15-s23-compiler-source-inventory-diagnostic-ids)
                  rejected-diagnostics)
     :diagnostics-covered?
     (= (set p15-s23-compiler-source-inventory-diagnostic-ids)
        diagnostics)
     :limitations
     {:full-language-compiler-self-hosted? false
      :clojure-seed-retired? false
      :compiles-whole-claimed-subset? false
      :nontrivial-gravity-app-through-self-hosted-toolchain? false
      :next-required-capability
      :implement_compiler_stage_from_this_source_inventory}}))