

(defn schema-interop-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (p10-schema-source-overrides module)
        _ (p10-schema-validate-source-overrides! source-path source-overrides)
        input-id (c4-artifact-id {:source-path source-path
                                  :module (:module module)
                                  :task "P10-T01-T06"
                                  :documents p10-schema-documents})
        source-schema (p10-source-schema-ir source-path input-id)
        diagnostic-stream (p10-schema-diagnostic-stream source-path input-id)
        artifact-base
        {:kind :gravity/stage0-schema-interop-artifact
         :phase "10"
         :task "P10-T01-T06"
         :document-set p10-schema-documents
         :governing-documents p10-schema-phase-governing-documents
         :pass {:name :phase10-schema-interop
                :input :ordinary-gravity-source
                :output :schema-interop-artifact
                :requires [:source-schema-ir :validator-artifact
                           :serialization-fixture :canonical-format
                           :graphql-generation :openapi-generation
                           :database-mapping :binary-abi-schema
                           :typed-config :artifact-schema-registry
                           :ai-structured-output-contract]
                :preserves [:schema-id :schema-version :schema-hash
                            :source-spans :compatibility-policy
                            :validation-boundary :taint :effects
                            :capabilities :profile :target :provenance]
                :emits [:document-contracts :accepted-schema-fixtures
                        :rejected-schema-fixtures :schema-conformance-evidence
                        :schema-diagnostic-stream]
                :rejects p10-schema-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :source-schema-ir source-schema
         :validator-artifact (p10-validator-artifact source-schema)
         :serialization-fixture (p10-serialization-fixture source-schema)
         :canonical-format (p10-canonical-format source-schema)
         :graphql-generation (p10-graphql-generation source-schema)
         :openapi-generation (p10-openapi-generation source-schema)
         :database-mapping (p10-database-mapping source-schema)
         :binary-abi-schema (p10-binary-abi-schema source-schema)
         :typed-config (p10-typed-config source-schema)
         :artifact-schema-registry (p10-artifact-schema-registry source-schema)
         :ai-structured-output-contract (p10-ai-structured-output-contract
                                         source-schema)
         :document-contracts (p10-document-records)
         :accepted-schema-fixtures (p10-accepted-schema-fixtures)
         :rejected-schema-fixtures (p10-rejected-schema-fixtures)
         :schema-conformance-evidence (p10-schema-conformance-evidence)
         :schema-diagnostic-stream diagnostic-stream
         :schema-interop-results
         {:documents p10-schema-documents
          :tasks (p10-task-statuses)
          :document-contracts 9
          :generated-artifact-families (count p10-generated-artifact-keys)
          :accepted-fixtures 9
          :rejected-fixtures 9
          :conformance-records 9
          :diagnostic-count (count p10-schema-diagnostic-ids)
          :status :complete}
         :diagnostics []}
        _ (p10-schema-validate! source-path artifact-base)
        capability-proof (p10-schema-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn schema-interop-file-artifact
  [path]
  (schema-interop-source-artifact path (slurp path)))