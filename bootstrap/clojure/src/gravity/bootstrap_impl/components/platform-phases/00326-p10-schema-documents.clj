

(def p10-schema-documents
  (mapv #(str "S" %) (range 1 10)))

(def p10-schema-governing-documents
  {"S1" "docs/phase-10-schema-data-and-interop/145-s1-schema-system-specification.md"
   "S2" "docs/phase-10-schema-data-and-interop/146-s2-serialization-specification.md"
   "S3" "docs/phase-10-schema-data-and-interop/147-s3-canonical-data-format-specification.md"
   "S4" "docs/phase-10-schema-data-and-interop/148-s4-graphql-generation-design.md"
   "S5" "docs/phase-10-schema-data-and-interop/149-s5-openapi-generation-design.md"
   "S6" "docs/phase-10-schema-data-and-interop/150-s6-database-mapping-and-migration-design.md"
   "S7" "docs/phase-10-schema-data-and-interop/151-s7-binary-encoding-and-abi-schema-specification.md"
   "S8" "docs/phase-10-schema-data-and-interop/152-s8-typed-configuration-and-environment-specification.md"
   "S9" "docs/phase-10-schema-data-and-interop/153-s9-artifact-schema-specification.md"})

(def p10-schema-phase-governing-documents
  (vec (concat ["docs/phase-10-schema-data-and-interop/IMPLEMENTATION-ROADMAP.md"
                "docs/phase-10-schema-data-and-interop/README.md"
                "docs/phase-01-core-language/015-l5-type-system-specification.md"
                "docs/phase-01-core-language/016-l6-effect-system-specification.md"
                "docs/phase-02-safety/040-safe11-taint-tracking-and-input-safety.md"
                "docs/phase-12-build-package-and-artifact-system/167-pkg3-artifact-model-specification.md"
                "docs/phase-11-ai-and-agentic-programming/156-a3-prompt-and-structured-output-specification.md"]
               (map p10-schema-governing-documents p10-schema-documents))))

(def p10-schema-contracts
  {"S1" {:shape ["S1-SHAPE" :schema_structure]
         :refinement ["S1-REFINEMENT" :classified_refinement]
         :boundary ["S1-BOUNDARY" :validation_boundary_metadata]
         :compatibility ["S1-COMPATIBILITY" :schema_evolution_policy]
         :projection ["S1-PROJECTION" :non_weakening_projection]
         :taint ["S1-TAINT" :external_data_taint]
         :recursion ["S1-RECURSION" :recursive_encoding_strategy]
         :manifest ["S1-MANIFEST" :schema_manifest_completeness]}
   "S2" {:format ["S2-FORMAT" :format_mapping]
         :field ["S2-FIELD" :field_policy]
         :variant ["S2-VARIANT" :variant_policy]
         :numeric ["S2-NUMERIC" :numeric_policy]
         :string ["S2-STRING" :string_policy]
         :polymorphic ["S2-POLYMORPHIC" :finite_deserialization_type_set]
         :taint ["S2-TAINT" :decoded_value_taint]
         :roundtrip ["S2-ROUNDTRIP" :round_trip_fixture]
         :manifest ["S2-MANIFEST" :serializer_manifest_completeness]}
   "S3" {:noncanonical ["S3-NONCANONICAL" :canonical_rule]
         :order ["S3-ORDER" :deterministic_map_set_order]
         :numeric ["S3-NUMERIC" :canonical_numeric_policy]
         :string ["S3-STRING" :canonical_string_policy]
         :metadata ["S3-METADATA" :metadata_policy]
         :hash ["S3-HASH" :canonical_hash_input]
         :vector ["S3-VECTOR" :reference_vector]
         :manifest ["S3-MANIFEST" :canonical_manifest_completeness]}
   "S4" {:mapping ["S4-MAPPING" :graphql_mapping]
         :nullability ["S4-NULLABILITY" :gravity_option_result_mapping]
         :resolver ["S4-RESOLVER" :resolver_effect_capability]
         :auth ["S4-AUTH" :auth_metadata]
         :diff ["S4-DIFF" :graphql_schema_diff]
         :client ["S4-CLIENT" :typed_client_schema_hash]
         :sourcemap ["S4-SOURCEMAP" :generated_sdl_source_map]
         :manifest ["S4-MANIFEST" :graphql_manifest_completeness]}
   "S5" {:route ["S5-ROUTE" :route_metadata]
         :schema ["S5-SCHEMA" :request_response_error_schema]
         :taint ["S5-TAINT" :http_input_taint]
         :capability ["S5-CAPABILITY" :route_effect_grant]
         :idempotency ["S5-IDEMPOTENCY" :mutating_route_idempotency]
         :diff ["S5-DIFF" :openapi_breaking_change]
         :client ["S5-CLIENT" :operation_schema_hash]
         :sourcemap ["S5-SOURCEMAP" :openapi_source_provenance]
         :manifest ["S5-MANIFEST" :openapi_manifest_completeness]}
   "S6" {:mapping ["S6-MAPPING" :schema_to_database_mapping]
         :dialect ["S6-DIALECT" :database_dialect_behavior]
         :migration ["S6-MIGRATION" :migration_compatibility_policy]
         :data-loss ["S6-DATA-LOSS" :destructive_migration_policy]
         :rollback ["S6-ROLLBACK" :rollback_or_forward_policy]
         :capability ["S6-CAPABILITY" :migration_capability]
         :adapter ["S6-ADAPTER" :typed_row_adapter]
         :fixture ["S6-FIXTURE" :migration_fixture_validation]
         :manifest ["S6-MANIFEST" :migration_manifest_completeness]}
   "S7" {:layout ["S7-LAYOUT" :explicit_layout]
         :endian ["S7-ENDIAN" :endian_policy]
         :pointer ["S7-POINTER" :pointer_lifetime_ownership]
         :abi ["S7-ABI" :target_calling_convention]
         :variant ["S7-VARIANT" :variant_representation]
         :fixture ["S7-FIXTURE" :byte_reference_fixture]
         :compatibility ["S7-COMPATIBILITY" :binary_compatibility_policy]
         :manifest ["S7-MANIFEST" :abi_manifest_completeness]}
   "S8" {:schema ["S8-SCHEMA" :config_schema]
         :source ["S8-SOURCE" :source_precedence]
         :capability ["S8-CAPABILITY" :config_read_grant]
         :secret ["S8-SECRET" :secret_redaction]
         :validation ["S8-VALIDATION" :config_value_validation]
         :hermeticity ["S8-HERMETICITY" :build_input_capture]
         :reload ["S8-RELOAD" :runtime_reload_policy]
         :manifest ["S8-MANIFEST" :config_manifest_completeness]}
   "S9" {:schema ["S9-SCHEMA" :artifact_schema_version]
         :required ["S9-REQUIRED" :required_artifact_field]
         :hash ["S9-HASH" :content_source_hash]
         :provenance ["S9-PROVENANCE" :artifact_provenance]
         :evidence ["S9-EVIDENCE" :release_evidence]
         :canonical ["S9-CANONICAL" :canonical_artifact_hash]
         :cycle ["S9-CYCLE" :bootstrap_cycle_provenance]
         :compatibility ["S9-COMPATIBILITY" :artifact_schema_migration_policy]}})

(def p10-schema-rejected-diagnostics
  {"S1" "S1-PROJECTION"
   "S2" "S2-TAINT"
   "S3" "S3-HASH"
   "S4" "S4-RESOLVER"
   "S5" "S5-SCHEMA"
   "S6" "S6-DATA-LOSS"
   "S7" "S7-POINTER"
   "S8" "S8-SECRET"
   "S9" "S9-EVIDENCE"})

(def p10-schema-diagnostic-ids
  (vec
   (distinct
    (concat
     (mapcat
      (fn [document]
        (map (comp first val)
             (sort-by (comp name key) (p10-schema-contracts document))))
      p10-schema-documents)
     ["P10-MANIFEST" "P10-ACCEPTED" "P10-REJECTED"
      "P10-CONFORMANCE"]))))

(def p10-schema-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             p10-schema-diagnostic-ids)))

(def p10-source-schema-contract
  {:schema-id "TicketClassification"
   :schema-version 2
   :compatibility :additive
   :fields [{:field :priority
             :type :enum
             :variants [:low :medium :high]
             :required true
             :taint-policy :retain-until-boundary-validation}
            {:field :category
             :type :string
             :required true
             :normalization :utf8-nfc
             :taint-policy :retain-until-boundary-validation}
            {:field :confidence
             :type :f64
             :required true
             :refinement {:kind :runtime
                          :predicate :between-inclusive
                          :min 0.0
                          :max 1.0}}]
   :boundaries #{:api-input :api-response :database-row :message
                 :model-output :configuration :binary-ffi
                 :artifact-manifest}
   :derivation-targets #{:validator :serialization :canonical :graphql
                         :openapi :database :binary-abi :config
                         :artifact-schema :ai-output}})

(def p10-schema-hash
  (c4-artifact-id p10-source-schema-contract))

(def p10-schema-artifact-keys
  [:source-schema-ir :validator-artifact :serialization-fixture
   :canonical-format :graphql-generation :openapi-generation
   :database-mapping :binary-abi-schema :typed-config
   :artifact-schema-registry :ai-structured-output-contract])

(def p10-generated-artifact-keys
  (vec (remove #{:source-schema-ir} p10-schema-artifact-keys)))

(def p10-document-summaries
  {"S1" {:title "Schema System Specification"
         :owned-surface :source-schema-ir
         :accepted-behavior :authoritative_source_schema_manifest
         :rejected-behavior "S1-PROJECTION"
         :artifact-keys [:source-schema-ir :validator-artifact]
         :dependencies #{"L5" "L6" "SAFE10" "SAFE11"}}
   "S2" {:title "Serialization Specification"
         :owned-surface :serialization-fixture
         :accepted-behavior :serializer_manifest_round_trip_and_taint
         :rejected-behavior "S2-TAINT"
         :artifact-keys [:serialization-fixture]
         :dependencies #{"S1" "S3" "SAFE11" "S7"}}
   "S3" {:title "Canonical Data Format Specification"
         :owned-surface :canonical-format
         :accepted-behavior :canonical_reference_vectors_and_hash_inputs
         :rejected-behavior "S3-HASH"
         :artifact-keys [:canonical-format]
         :dependencies #{"S1" "S2" "B13"}}
   "S4" {:title "GraphQL Generation Design"
         :owned-surface :graphql-generation
         :accepted-behavior :source_schema_backed_graphql_projection
         :rejected-behavior "S4-RESOLVER"
         :artifact-keys [:graphql-generation]
         :dependencies #{"S1" "S2" "S3" "R11" "SAFE10"}}
   "S5" {:title "OpenAPI Generation Design"
         :owned-surface :openapi-generation
         :accepted-behavior :typed_route_request_response_and_error_contracts
         :rejected-behavior "S5-SCHEMA"
         :artifact-keys [:openapi-generation]
         :dependencies #{"S1" "S2" "S3" "DOM8" "B11" "R11" "R12"}}
   "S6" {:title "Database Mapping and Migration Design"
         :owned-surface :database-mapping
         :accepted-behavior :schema_diff_migration_rollback_and_row_adapter
         :rejected-behavior "S6-DATA-LOSS"
         :artifact-keys [:database-mapping]
         :dependencies #{"S1" "B11" "SAFE10" "SAFE11" "P9" "R7"}}
   "S7" {:title "Binary Encoding and ABI Schema Specification"
         :owned-surface :binary-abi-schema
         :accepted-behavior :explicit_layout_and_reference_bytes
         :rejected-behavior "S7-POINTER"
         :artifact-keys [:binary-abi-schema]
         :dependencies #{"S1" "S2" "B2" "B3" "B4" "B9" "B12" "R10"
                         "SAFE2" "SAFE7" "SAFE15"}}
   "S8" {:title "Typed Configuration and Environment Specification"
         :owned-surface :typed-config
         :accepted-behavior :capability_checked_redacted_typed_config
         :rejected-behavior "S8-SECRET"
         :artifact-keys [:typed-config]
         :dependencies #{"S1" "S2" "SAFE10" "SAFE11" "R11" "R12"}}
   "S9" {:title "Artifact Schema Specification"
         :owned-surface :artifact-schema-registry
         :accepted-behavior :schema_validated_artifact_manifests
         :rejected-behavior "S9-EVIDENCE"
         :artifact-keys [:artifact-schema-registry]
         :dependencies #{"S1" "S3" "B13"}}})

(defn p10-document-number
  [document]
  (Integer/parseInt (subs document 1)))