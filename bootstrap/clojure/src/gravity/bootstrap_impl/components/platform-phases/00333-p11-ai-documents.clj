

(def p11-ai-documents
  (mapv #(str "A" %) (range 1 12)))

(def p11-ai-governing-documents
  {"A1" "docs/phase-11-ai-and-agentic-programming/154-a1-ai-programming-model-specification.md"
   "A2" "docs/phase-11-ai-and-agentic-programming/155-a2-model-provider-specification.md"
   "A3" "docs/phase-11-ai-and-agentic-programming/156-a3-prompt-and-structured-output-specification.md"
   "A4" "docs/phase-11-ai-and-agentic-programming/157-a4-tool-definition-specification.md"
   "A5" "docs/phase-11-ai-and-agentic-programming/158-a5-agent-definition-specification.md"
   "A6" "docs/phase-11-ai-and-agentic-programming/159-a6-agent-workflow-specification.md"
   "A7" "docs/phase-11-ai-and-agentic-programming/160-a7-memory-and-retrieval-specification.md"
   "A8" "docs/phase-11-ai-and-agentic-programming/161-a8-ai-policy-and-safety-model.md"
   "A9" "docs/phase-11-ai-and-agentic-programming/162-a9-ai-evaluation-framework-design.md"
   "A10" "docs/phase-11-ai-and-agentic-programming/163-a10-human-in-the-loop-and-human-review-workflow-specification.md"
   "A11" "docs/phase-11-ai-and-agentic-programming/164-a11-prompt-injection-and-tool-misuse-defense-specification.md"})

(def p11-ai-phase-governing-documents
  (vec (concat ["docs/phase-11-ai-and-agentic-programming/IMPLEMENTATION-ROADMAP.md"
                "docs/phase-11-ai-and-agentic-programming/README.md"
                "docs/phase-01-core-language/016-l6-effect-system-specification.md"
                "docs/phase-02-safety/042-safe13-ai-tool-safety.md"
                "docs/phase-10-schema-data-and-interop/IMPLEMENTATION-ROADMAP.md"
                "docs/phase-08-runtime-architecture/IMPLEMENTATION-ROADMAP.md"
                "docs/phase-08-runtime-architecture/119-r8-ai-runtime-design.md"
                "docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md"]
               (map p11-ai-governing-documents p11-ai-documents))))

(def p11-ai-contracts
  {"A1" {:profile ["AI001" :legal_ai_profile]
         :model ["AI002" :model_provider_identity]
         :schema ["AI003" :input_output_schema]
         :tool ["AI004" :tool_capability_human_review]
         :authority ["AI005" :instruction_authority_separation]
         :generated ["AI006" :generated_code_pipeline]
         :replay ["AI007" :replay_mode]
         :evaluation ["AI008" :production_eval_evidence]}
   "A2" {:capability ["A2001" :provider_capability]
         :credential ["A2002" :credential_policy]
         :mode ["A2003" :supported_model_mode]
         :fallback ["A2004" :fallback_eval_gate]
         :budget ["A2005" :model_budget]
         :validation ["A2006" :provider_response_validation]
         :identity ["A2007" :provider_identity_replay]}
   "A3" {:input-schema ["A3001" :prompt_input_schema]
         :output-schema ["A3002" :trusted_output_schema]
         :authority ["A3003" :prompt_authority_partition]
         :taint ["A3004" :prompt_taint_policy]
         :validation ["A3005" :structured_output_validation]
         :partial ["A3006" :partial_output_policy]
         :compatibility ["A3007" :prompt_evolution_policy]
         :secret ["A3008" :rendered_prompt_secret_redaction]}
   "A4" {:schema ["A4001" :tool_input_output_schema]
         :effect ["A4002" :declared_tool_effect]
         :capability ["A4003" :tool_capability_handle]
         :toolset ["A4004" :agent_toolset]
         :human-review ["A4005" :write_tool_human_review]
         :retry ["A4006" :non_idempotent_retry_policy]
         :validation ["A4007" :tool_output_validation]
         :redaction ["A4008" :tool_log_redaction]}
   "A5" {:dependency ["A5001" :agent_dependency]
         :ambient ["A5002" :ambient_authority]
         :tool ["A5003" :undeclared_tool_use]
         :policy ["A5004" :agent_policy]
         :eval ["A5005" :agent_eval_gate]
         :budget ["A5006" :agent_budget]
         :output ["A5007" :agent_output_schema]
         :memory ["A5008" :memory_access_mode]}
   "A6" {:replay ["A6001" :workflow_replay_mode]
         :nondeterminism ["A6002" :recorded_nondeterminism]
         :side-effect ["A6003" :replay_side_effect_guard]
         :state ["A6004" :workflow_state_schema]
         :compensation ["A6005" :idempotency_or_compensation]
         :human-review ["A6006" :human_review_payload_hash]
         :migration ["A6007" :workflow_state_migration]
         :budget ["A6008" :retry_budget]}
   "A7" {:capability ["A7001" :memory_capability]
         :schema ["A7002" :memory_item_schema]
         :protected-data ["A7003" :protected_embedding_policy]
         :tenant ["A7004" :tenant_partition]
         :injection ["A7005" :retrieved_instruction_data]
         :replay ["A7006" :memory_replay_policy]
         :embedding ["A7007" :embedding_index_compatibility]
         :redaction ["A7008" :stale_retrieval_after_redaction]}
   "A8" {:policy ["A8001" :production_policy]
         :denial ["A8002" :deterministic_policy_denial]
         :human-review ["A8003" :policy_human_review]
         :taint ["A8004" :taint_validation_required]
         :fallback ["A8005" :fallback_denial]
         :generated ["A8006" :generated_code_validation]
         :emergency ["A8007" :emergency_override_policy]
         :logging ["A8008" :protected_data_logging]}
   "A9" {:gate ["A9001" :eval_gate]
         :subject ["A9002" :eval_subject_identity]
         :dataset ["A9003" :eval_dataset_schema]
         :threshold ["A9004" :metric_threshold]
         :probe ["A9005" :safety_probe]
         :budget ["A9006" :live_provider_eval_budget]
         :drift ["A9007" :provider_drift]
         :redaction ["A9008" :eval_output_redaction]}
   "A10" {:missing ["A10001" :human_review_record]
          :role ["A10002" :reviewer_role]
          :expired ["A10003" :human_review_expiry]
          :revoked ["A10004" :human_review_revocation]
          :payload ["A10005" :payload_hash_match]
          :denial ["A10006" :denial_branch]
          :replay ["A10007" :human_review_replay_policy]
          :emergency ["A10008" :emergency_bypass_policy]}
   "A11" {:authority ["A11001" :authority_partition]
          :tool ["A11002" :denied_tool_escalation]
          :memory ["A11003" :memory_injection]
          :secret ["A11004" :secret_exposure]
          :generated ["A11005" :generated_code_trust]
          :eval ["A11006" :injection_defense_eval]
          :tool-output ["A11007" :tainted_tool_output]
          :override ["A11008" :policy_override_text]}})

(def p11-ai-rejected-diagnostics
  {"A1" "AI004"
   "A2" "A2001"
   "A3" "A3003"
   "A4" "A4005"
   "A5" "A5005"
   "A6" "A6001"
   "A7" "A7004"
   "A8" "A8004"
   "A9" "A9001"
   "A10" "A10005"
   "A11" "A11002"})

(def p11-ai-rejected-fixture-names
  {"A1" "ai-a1-tool-authority.gravity"
   "A2" "ai-a2-provider-capability.gravity"
   "A3" "ai-a3-authority.gravity"
   "A4" "ai-a4-human-review.gravity"
   "A5" "ai-a5-eval-gate.gravity"
   "A6" "ai-a6-replay-mode.gravity"
   "A7" "ai-a7-cross-tenant.gravity"
   "A8" "ai-a8-taint-policy.gravity"
   "A9" "ai-a9-eval-gate.gravity"
   "A10" "ai-a10-payload.gravity"
   "A11" "ai-a11-tool-escalation.gravity"})

(def p11-ai-diagnostic-ids
  (vec
   (distinct
    (concat
     (mapcat
      (fn [document]
        (map (comp first val)
             (sort-by (comp name key) (p11-ai-contracts document))))
      p11-ai-documents)
     ["P11-MANIFEST" "P11-ACCEPTED" "P11-REJECTED"
      "P11-CONFORMANCE"]))))

(def p11-ai-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             p11-ai-diagnostic-ids)))

(def p11-ai-artifact-keys
  [:ai-program-manifest :model-manifest :prompt-artifact :tool-schema
   :agent-manifest :workflow-graph :memory-policy :policy-manifest
   :evaluation-report :human-review-manifest :injection-defense])

(def p11-ai-document-summaries
  {"A1" {:title "AI Programming Model Specification"
         :owned-surface :ai-program-manifest
         :accepted-behavior :typed_effectful_ai_program
         :rejected-behavior "AI004"
         :artifact-keys [:ai-program-manifest]
         :dependencies #{"L6" "SAFE10" "SAFE11" "SAFE13" "R8" "R11" "R12" "S1" "S3" "S9"}}
   "A2" {:title "Model Provider Specification"
         :owned-surface :model-manifest
         :accepted-behavior :capability_gated_redacted_provider
         :rejected-behavior "A2001"
         :artifact-keys [:model-manifest]
         :dependencies #{"L6" "L15" "SAFE10" "SAFE13" "R8" "S1" "S3" "A9"}}
   "A3" {:title "Prompt and Structured Output Specification"
         :owned-surface :prompt-artifact
         :accepted-behavior :partitioned_prompt_and_schema_validated_output
         :rejected-behavior "A3003"
         :artifact-keys [:prompt-artifact]
         :dependencies #{"L4" "L5" "L6" "SAFE11" "S1" "S3" "A2" "A11"}}
   "A4" {:title "Tool Definition Specification"
         :owned-surface :tool-schema
         :accepted-behavior :typed_capability_checked_tool_boundary
         :rejected-behavior "A4005"
         :artifact-keys [:tool-schema]
         :dependencies #{"L6" "L15" "SAFE10" "SAFE11" "R11" "S1" "A5" "A8" "A10"}}
   "A5" {:title "Agent Definition Specification"
         :owned-surface :agent-manifest
         :accepted-behavior :complete_agent_manifest_with_eval_gates
         :rejected-behavior "A5005"
         :artifact-keys [:agent-manifest]
         :dependencies #{"L6" "L15" "SAFE10" "SAFE11" "SAFE13" "R8" "A2" "A3" "A4" "A7" "A8" "A9" "A10"}}
   "A6" {:title "Agent Workflow Specification"
         :owned-surface :workflow-graph
         :accepted-behavior :replayable_agent_workflow_graph
         :rejected-behavior "A6001"
         :artifact-keys [:workflow-graph]
         :dependencies #{"L6" "L11" "P9" "P10" "B10" "R7" "R8" "S9" "A4" "A5" "A7" "A10"}}
   "A7" {:title "Memory and Retrieval Specification"
         :owned-surface :memory-policy
         :accepted-behavior :partitioned_tainted_replayable_memory
         :rejected-behavior "A7004"
         :artifact-keys [:memory-policy]
         :dependencies #{"L6" "SAFE10" "SAFE11" "R8" "S1" "S3" "A2" "A3" "A11"}}
   "A8" {:title "AI Policy and Safety Model"
         :owned-surface :policy-manifest
         :accepted-behavior :deterministic_policy_decision_model
         :rejected-behavior "A8004"
         :artifact-keys [:policy-manifest]
         :dependencies #{"SAFE10" "SAFE11" "SAFE13" "R11" "A2" "A3" "A4" "A5" "A6" "A7" "A9" "A10" "A11"}}
   "A9" {:title "AI Evaluation Framework Design"
         :owned-surface :evaluation-report
         :accepted-behavior :eval_report_and_release_gate
         :rejected-behavior "A9001"
         :artifact-keys [:evaluation-report]
         :dependencies #{"S1" "S3" "S9" "A2" "A3" "A5" "A6" "A8" "TEST8"}}
   "A10" {:title "Human-in-the-Loop and Human-Review Workflow Specification"
          :owned-surface :human-review-manifest
          :accepted-behavior :typed_human_review_effect
          :rejected-behavior "A10005"
          :artifact-keys [:human-review-manifest]
          :dependencies #{"L6" "SAFE10" "R7" "R12" "A4" "A6" "A8" "S3" "S9"}}
   "A11" {:title "Prompt Injection and Tool Misuse Defense Specification"
          :owned-surface :injection-defense
          :accepted-behavior :taint_authority_policy_defense_bundle
          :rejected-behavior "A11002"
          :artifact-keys [:injection-defense]
          :dependencies #{"SAFE11" "SAFE13" "A3" "A4" "A7" "A8" "A9" "A10" "R11"}}})

(defn p11-document-number
  [document]
  (Integer/parseInt (subs document 1)))