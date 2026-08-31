

(def p09-domain-documents
  (mapv #(str "DOM" %) (range 1 22)))

(def p09-domain-governing-documents
  {"DOM1" "docs/phase-09-domain-specific-computing-coverage/124-dom1-hardware-computing-domain-specification.md"
   "DOM2" "docs/phase-09-domain-specific-computing-coverage/125-dom2-firmware-and-embedded-domain-specification.md"
   "DOM3" "docs/phase-09-domain-specific-computing-coverage/126-dom3-operating-system-and-kernel-domain-specification.md"
   "DOM4" "docs/phase-09-domain-specific-computing-coverage/127-dom4-drivers-and-device-interaction-domain-specification.md"
   "DOM5" "docs/phase-09-domain-specific-computing-coverage/128-dom5-high-performance-native-computing-domain-specification.md"
   "DOM6" "docs/phase-09-domain-specific-computing-coverage/129-dom6-web-frontend-and-ui-domain-specification.md"
   "DOM7" "docs/phase-09-domain-specific-computing-coverage/130-dom7-mobile-application-domain-specification.md"
   "DOM8" "docs/phase-09-domain-specific-computing-coverage/131-dom8-backend-services-domain-specification.md"
   "DOM9" "docs/phase-09-domain-specific-computing-coverage/132-dom9-distributed-systems-domain-specification.md"
   "DOM10" "docs/phase-09-domain-specific-computing-coverage/133-dom10-database-and-storage-engine-domain-specification.md"
   "DOM11" "docs/phase-09-domain-specific-computing-coverage/134-dom11-data-query-and-analytics-domain-specification.md"
   "DOM12" "docs/phase-09-domain-specific-computing-coverage/135-dom12-scientific-and-numeric-computing-domain-specification.md"
   "DOM13" "docs/phase-09-domain-specific-computing-coverage/136-dom13-gpu-and-accelerator-computing-domain-specification.md"
   "DOM14" "docs/phase-09-domain-specific-computing-coverage/137-dom14-game-engine-and-simulation-domain-specification.md"
   "DOM15" "docs/phase-09-domain-specific-computing-coverage/138-dom15-security-and-cryptography-domain-specification.md"
   "DOM16" "docs/phase-09-domain-specific-computing-coverage/139-dom16-blockchain-and-smart-contract-domain-specification.md"
   "DOM17" "docs/phase-09-domain-specific-computing-coverage/140-dom17-compiler-and-language-tooling-domain-specification.md"
   "DOM18" "docs/phase-09-domain-specific-computing-coverage/141-dom18-ai-and-agentic-computing-domain-specification.md"
   "DOM19" "docs/phase-09-domain-specific-computing-coverage/142-dom19-formal-verification-domain-specification.md"
   "DOM20" "docs/phase-09-domain-specific-computing-coverage/143-dom20-scripting-shell-and-automation-domain-specification.md"
   "DOM21" "docs/phase-09-domain-specific-computing-coverage/144-dom21-low-code-visual-programming-and-workflow-domain-specification.md"})

(def p09-domain-phase-governing-documents
  (vec (concat ["docs/phase-09-domain-specific-computing-coverage/IMPLEMENTATION-ROADMAP.md"
                "docs/phase-09-domain-specific-computing-coverage/README.md"
                "docs/phase-01-core-language/IMPLEMENTATION-ROADMAP.md"
                "docs/phase-03-profile-system/IMPLEMENTATION-ROADMAP.md"
                "docs/phase-07-backend-architecture/IMPLEMENTATION-ROADMAP.md"
                "docs/phase-08-runtime-architecture/IMPLEMENTATION-ROADMAP.md"
                "docs/phase-10-schema-data-and-interop/IMPLEMENTATION-ROADMAP.md"
                "docs/phase-16-standard-library/IMPLEMENTATION-ROADMAP.md"]
               (map p09-domain-governing-documents p09-domain-documents))))

(def p09-domain-contracts
  {"DOM1" {:fixed-widths ["DOM1-WIDTH" :fixed_width_integer_port_interface]
           :clock-reset ["DOM1-CLOCK" :clock_and_reset_domain]
           :cdc ["DOM1-CDC" :clock_domain_crossing]
           :no-runtime ["DOM1-RUNTIME" :no_runtime_hardware_profile]
           :finite-bounds ["DOM1-UNBOUNDED" :finite_loop_resource_bound]
           :interface-manifest ["DOM1-INTERFACE" :hardware_interface_manifest]
           :timing-manifest ["DOM1-TIMING" :timing_and_synthesis_evidence]
           :conformance ["DOM1-CONFORMANCE" :hardware_conformance_fixtures]}
   "DOM2" {:startup ["DOM2-STARTUP" :startup_reset_vector]
           :memory-map ["DOM2-MEMORY" :firmware_memory_map]
           :mmio ["DOM2-MMIO" :mmio_register_schema]
           :interrupt-vector ["DOM2-INTERRUPT" :interrupt_vector]
           :runtime-boundary ["DOM2-RUNTIME" :runtime_boundary]
           :bsp ["DOM2-BSP" :board_support_package_boundary]
           :conformance ["DOM2-CONFORMANCE" :firmware_conformance_fixtures]}
   "DOM3" {:runtime-model ["DOM3-RUNTIME" :kernel_runtime_model]
           :raw-memory ["DOM3-RAW" :raw_memory_unsafe_audit]
           :allocator ["DOM3-ALLOC" :kernel_allocation_policy]
           :interrupt ["DOM3-INTERRUPT" :interrupt_handling]
           :syscall ["DOM3-SYSCALL" :syscall_schema_taint]
           :capability ["DOM3-CAPABILITY" :kernel_capability]
           :abi ["DOM3-ABI" :kernel_abi]
           :conformance ["DOM3-CONFORMANCE" :kernel_conformance_fixtures]}
   "DOM4" {:register-schema ["DOM4-REGISTER" :device_register_schema]
           :mmio-audit ["DOM4-MMIO" :mmio_unsafe_audit]
           :dma-lifetime ["DOM4-DMA" :dma_ownership_lifetime]
           :interrupt ["DOM4-INTERRUPT" :driver_interrupt]
           :cache ["DOM4-CACHE" :cache_coherency]
           :capability ["DOM4-CAPABILITY" :device_capability]
           :adapter ["DOM4-ADAPTER" :hosted_mobile_adapter]
           :conformance ["DOM4-CONFORMANCE" :driver_conformance_fixtures]}
   "DOM5" {:memory ["DOM5-MEMORY" :native_memory_ownership]
           :target ["DOM5-TARGET" :native_target_feature]
           :ub ["DOM5-UB" :undefined_behavior_rejection]
           :optimization ["DOM5-OPTIMIZATION" :proof_gated_optimization]
           :numeric ["DOM5-NUMERIC" :numeric_mode]
           :ffi ["DOM5-FFI" :safe_ffi_boundary]
           :benchmark ["DOM5-BENCHMARK" :benchmark_context]
           :conformance ["DOM5-CONFORMANCE" :native_conformance_fixtures]}
   "DOM6" {:dom-access ["DOM6-DOM" :browser_dom_capability]
           :taint ["DOM6-TAINT" :web_taint_validation]
           :schema ["DOM6-SCHEMA" :route_component_api_schema]
           :package ["DOM6-PACKAGE" :web_package_artifact]
           :numeric ["DOM6-NUMERIC" :web_numeric_boundary]
           :sourcemap ["DOM6-SOURCEMAP" :source_map]
           :conformance ["DOM6-CONFORMANCE" :web_conformance_fixtures]}
   "DOM7" {:permission ["DOM7-PERMISSION" :platform_permission]
           :lifecycle ["DOM7-LIFECYCLE" :mobile_lifecycle]
           :thread ["DOM7-THREAD" :thread_ui_boundary]
           :nullability ["DOM7-NULL" :host_nullability_translation]
           :storage ["DOM7-STORAGE" :mobile_storage_migration]
           :secret ["DOM7-SECRET" :mobile_secret_handling]
           :conformance ["DOM7-CONFORMANCE" :mobile_conformance_fixtures]}
   "DOM8" {:route ["DOM8-ROUTE" :typed_route_handler]
           :schema ["DOM8-SCHEMA" :request_response_config_schema]
           :taint ["DOM8-TAINT" :backend_taint_validation]
           :capability ["DOM8-CAPABILITY" :backend_capability]
           :job ["DOM8-JOB" :worker_retry_idempotency]
           :secret ["DOM8-SECRET" :backend_secret]
           :observability ["DOM8-OBSERVABILITY" :observability]
           :conformance ["DOM8-CONFORMANCE" :backend_conformance_fixtures]}
   "DOM9" {:schema ["DOM9-SCHEMA" :distributed_schema]
           :replay ["DOM9-REPLAY" :event_replay]
           :idempotency ["DOM9-IDEMPOTENCY" :idempotency]
           :retry ["DOM9-RETRY" :retry_policy]
           :compensation ["DOM9-COMPENSATION" :compensation]
           :capability ["DOM9-CAPABILITY" :distributed_capability]
           :migration ["DOM9-MIGRATION" :event_log_migration]
           :crdt ["DOM9-CRDT" :crdt_evidence]
           :monotonicity ["DOM9-MONOTONICITY" :calm_monotonicity]
           :coordination ["DOM9-COORDINATION" :coordination_policy]
           :conflict ["DOM9-CONFLICT" :conflict_semantics]
           :sync ["DOM9-SYNC" :local_first_sync]
           :convergence ["DOM9-CONVERGENCE" :convergence_evidence]
           :conformance ["DOM9-CONFORMANCE" :distributed_conformance_fixtures]}
   "DOM10" {:schema ["DOM10-SCHEMA" :database_schema_mapping]
            :prepared-bindings ["DOM10-QUERY" :prepared_query_binding]
            :taint ["DOM10-TAINT" :sql_taint]
            :migration ["DOM10-MIGRATION" :migration_policy]
            :transaction ["DOM10-TRANSACTION" :transaction_retry]
            :layout ["DOM10-LAYOUT" :storage_binary_layout]
            :durability ["DOM10-DURABILITY" :durability_recovery]
            :conformance ["DOM10-CONFORMANCE" :query_recovery_conformance]}
   "DOM11" {:schema ["DOM11-SCHEMA" :dataset_schema]
            :capability ["DOM11-CAPABILITY" :data_source_capability]
            :taint ["DOM11-TAINT" :analytics_taint]
            :lineage ["DOM11-LINEAGE" :lineage]
            :memory ["DOM11-MEMORY" :bounded_materialization]
            :numeric ["DOM11-NUMERIC" :numeric_aggregate]
            :determinism ["DOM11-DETERMINISM" :determinism_policy]
            :conformance ["DOM11-CONFORMANCE" :analytics_conformance_fixtures]}
   "DOM12" {:domain ["DOM12-DOMAIN" :numeric_domain]
            :mode ["DOM12-MODE" :numeric_mode]
            :certificate ["DOM12-CERTIFICATE" :approximation_certificate]
            :rewrite ["DOM12-REWRITE" :symbolic_equivalence_proof]
            :fastmath ["DOM12-FASTMATH" :fast_math_policy]
            :interop ["DOM12-INTEROP" :numeric_provider_boundary]
            :benchmark ["DOM12-BENCHMARK" :accuracy_benchmark_context]
            :conformance ["DOM12-CONFORMANCE" :numeric_conformance_fixtures]}
   "DOM13" {:kernel ["DOM13-KERNEL" :gpu_kernel_legality]
            :memory ["DOM13-MEMORY" :device_memory_transfer]
            :sync ["DOM13-SYNC" :synchronization]
            :launch ["DOM13-LAUNCH" :launch_descriptor]
            :math ["DOM13-MATH" :gpu_numeric]
            :host-effect ["DOM13-HOST-EFFECT" :host_effect_rejection]
            :target ["DOM13-TARGET" :device_target_feature]
            :conformance ["DOM13-CONFORMANCE" :gpu_conformance_fixtures]}
   "DOM14" {:timestep ["DOM14-TIMESTEP" :simulation_timestep]
            :allocation ["DOM14-ALLOC" :frame_allocation]
            :determinism ["DOM14-DETERMINISM" :deterministic_replay]
            :numeric ["DOM14-NUMERIC" :game_numeric]
            :asset ["DOM14-ASSET" :asset_schema]
            :plugin ["DOM14-PLUGIN" :plugin_capability]
            :performance ["DOM14-PERFORMANCE" :frame_budget]
            :conformance ["DOM14-CONFORMANCE" :game_conformance_fixtures]}
   "DOM15" {:secret ["DOM15-SECRET" :secret_redaction]
            :random ["DOM15-RANDOM" :approved_randomness_capability]
            :provider ["DOM15-PROVIDER" :crypto_provider_manifest]
            :webauthn ["DOM15-WEBAUTHN" :webauthn_ceremony]
            :passkey ["DOM15-PASSKEY" :passkey_credential_policy]
            :private-compute ["DOM15-PRIVATE-COMPUTE" :private_computation_provider]
            :boundary ["DOM15-BOUNDARY" :plaintext_ciphertext_boundary]
            :noise ["DOM15-NOISE" :noise_depth_budget]
            :leakage ["DOM15-LEAKAGE" :privacy_leakage]
            :custody ["DOM15-CUSTODY" :key_custody]
            :constant-time ["DOM15-CONSTANT-TIME" :constant_time_analysis]
            :custom-crypto ["DOM15-CUSTOM" :custom_crypto_review]
            :taint ["DOM15-TAINT" :protocol_taint]
            :ffi ["DOM15-FFI" :crypto_ffi_wrapper_audit]
            :conformance ["DOM15-CONFORMANCE" :crypto_conformance_fixtures]}
   "DOM16" {:determinism ["DOM16-DETERMINISM" :contract_determinism]
            :schema ["DOM16-SCHEMA" :abi_state_event_transaction_schema]
            :numeric ["DOM16-NUMERIC" :checked_contract_arithmetic]
            :gas ["DOM16-GAS" :gas_resource_accounting]
            :auth ["DOM16-AUTH" :state_mutation_authorization]
            :account-validation ["DOM16-ACCOUNT-VALIDATION" :account_validation]
            :userop ["DOM16-USEROP" :user_operation_schema]
            :session-key ["DOM16-SESSION-KEY" :session_key_policy]
            :paymaster ["DOM16-PAYMASTER" :paymaster_policy]
            :delegation ["DOM16-DELEGATION" :delegation_revocation]
            :replay ["DOM16-REPLAY" :replay_domain_nonce]
            :bundler ["DOM16-BUNDLER" :bundler_simulation_assumptions]
            :wallet-binding ["DOM16-WALLET-BINDING" :wallet_binding]
            :aa-profile ["DOM16-AA-PROFILE" :account_abstraction_profile]
            :erc4337 ["DOM16-ERC4337" :erc4337_profile]
            :eip7702 ["DOM16-EIP7702" :eip7702_authorization]
            :erc7579 ["DOM16-ERC7579" :erc7579_module]
            :upgrade ["DOM16-UPGRADE" :upgrade_migration]
            :invariant ["DOM16-INVARIANT" :invariant_evidence]
            :ordering ["DOM16-ORDERING" :transaction_ordering_assumptions]
            :mev ["DOM16-MEV" :mev_exposure_mitigation]
            :conformance ["DOM16-CONFORMANCE" :chain_conformance]}
   "DOM17" {:pass-contract ["DOM17-PASS" :compiler_pass_contract]
            :metadata ["DOM17-METADATA" :metadata_preservation]
            :generated ["DOM17-GENERATED" :generated_code_validation]
            :plugin ["DOM17-PLUGIN" :plugin_capability]
            :macro ["DOM17-MACRO" :macro_hygiene]
            :diagnostic ["DOM17-DIAGNOSTIC" :diagnostic_origin_chain]
            :bootstrap ["DOM17-BOOTSTRAP" :bootstrap_provenance]
            :conformance ["DOM17-CONFORMANCE" :tooling_conformance_fixtures]}
   "DOM18" {:model ["DOM18-MODEL" :model_provider_budget]
            :prompt ["DOM18-PROMPT" :prompt_provenance]
            :tool ["DOM18-TOOL" :tool_schema_review_policy]
            :schema ["DOM18-SCHEMA" :structured_output_schema]
            :taint ["DOM18-TAINT" :ai_taint_validation]
            :secret ["DOM18-SECRET" :ai_secret_policy]
            :replay ["DOM18-REPLAY" :ai_replay]
            :generated ["DOM18-GENERATED" :generated_code_compiler_validation]
            :eval ["DOM18-EVAL" :eval_report]}
   "DOM19" {:claim ["DOM19-CLAIM" :verification_claim_manifest]
            :proof ["DOM19-PROOF" :proof_object]
            :stale ["DOM19-STALE" :stale_proof_invalidation]
            :assumption ["DOM19-ASSUMPTION" :solver_assumption_manifest]
            :counterexample ["DOM19-COUNTEREXAMPLE" :counterexample_mapping]
            :eml ["DOM19-EML" :eml_semantic_proof]
            :elision ["DOM19-ELISION" :proof_gated_elision]
            :zk-relation ["DOM19-ZK-RELATION" :zk_relation_metadata]
            :zk-input ["DOM19-ZK-INPUT" :public_private_input_split]
            :zk-setup ["DOM19-ZK-SETUP" :zk_setup_trust]
            :zk-privacy ["DOM19-ZK-PRIVACY" :zk_privacy_facet]
            :zk-cost ["DOM19-ZK-COST" :prover_verifier_cost]
            :zk-chain ["DOM19-ZK-CHAIN" :recursive_folding_chain]
            :zk-provider ["DOM19-ZK-PROVIDER" :zk_provider_record]
            :conformance ["DOM19-CONFORMANCE" :formal_conformance_fixtures]}
   "DOM20" {:args ["DOM20-ARGS" :argument_schema]
            :filesystem ["DOM20-FILESYSTEM" :filesystem_root_capability]
            :shell ["DOM20-SHELL" :command_schema]
            :taint ["DOM20-TAINT" :shell_taint]
            :destructive ["DOM20-DESTRUCTIVE" :destructive_action_review]
            :hermeticity ["DOM20-HERMETICITY" :hermeticity]
            :secret ["DOM20-SECRET" :script_secret]
            :audit ["DOM20-AUDIT" :dry_run_audit]}
   "DOM21" {:node ["DOM21-NODE" :typed_visual_node_schema]
            :edge ["DOM21-EDGE" :typed_visual_edge_schema]
            :effect ["DOM21-EFFECT" :visual_effect]
            :capability ["DOM21-CAPABILITY" :tool_model_capability]
            :human-review ["DOM21-HUMAN-REVIEW" :human_review_policy_graph]
            :replay ["DOM21-REPLAY" :workflow_replay]
            :generated ["DOM21-GENERATED" :generated_code_validation]
            :mapping ["DOM21-MAPPING" :visual_diagnostic_node_mapping]
            :migration ["DOM21-MIGRATION" :graph_migration]}})