

(defn ai-agentic-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (p11-ai-source-overrides module)
        _ (p11-ai-validate-source-overrides! source-path source-overrides)
        input-id (c4-artifact-id {:source-path source-path
                                  :module (:module module)
                                  :task "P11-T01-T06"
                                  :documents p11-ai-documents})
        diagnostic-stream (p11-ai-diagnostic-stream source-path input-id)
        artifact-base
        {:kind :gravity/stage0-ai-agentic-artifact
         :phase "11"
         :task "P11-T01-T06"
         :document-set p11-ai-documents
         :governing-documents p11-ai-phase-governing-documents
         :pass {:name :phase11-ai-agentic
                :input :ordinary-gravity-source
                :output :ai-agentic-artifact
                :requires p11-ai-artifact-keys
                :preserves [:profile :target :schema :effect :capability
                            :taint :authority-partition :policy
                            :replay :evaluation :human-review
                            :source-spans :provenance]
                :emits [:document-contracts :accepted-ai-fixtures
                        :rejected-ai-fixtures :ai-conformance-evidence
                        :ai-diagnostic-stream]
                :rejects p11-ai-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :ai-program-manifest (p11-ai-program-manifest source-path input-id)
         :model-manifest (p11-model-manifest)
         :prompt-artifact (p11-prompt-artifact)
         :tool-schema (p11-tool-schema)
         :agent-manifest (p11-agent-manifest)
         :workflow-graph (p11-workflow-graph)
         :memory-policy (p11-memory-policy)
         :policy-manifest (p11-policy-manifest)
         :evaluation-report (p11-evaluation-report)
         :human-review-manifest (p11-human-review-manifest)
         :injection-defense (p11-injection-defense)
         :document-contracts (p11-ai-document-records)
         :accepted-ai-fixtures (p11-accepted-ai-fixtures)
         :rejected-ai-fixtures (p11-rejected-ai-fixtures)
         :ai-conformance-evidence (p11-ai-conformance-evidence)
         :ai-diagnostic-stream diagnostic-stream
         :ai-agentic-results
         {:documents p11-ai-documents
          :tasks (p11-task-statuses)
          :document-contracts 11
          :artifact-families (count p11-ai-artifact-keys)
          :accepted-fixtures 11
          :rejected-fixtures 11
          :conformance-records 11
          :diagnostic-count (count p11-ai-diagnostic-ids)
          :status :complete}
         :diagnostics []}
        _ (p11-ai-validate! source-path artifact-base)
        capability-proof (p11-ai-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn ai-agentic-file-artifact
  [path]
  (ai-agentic-source-artifact path (slurp path)))