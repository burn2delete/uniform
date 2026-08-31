(ns gravity.c10-safety-analysis.pipeline
  "C10 orchestration and final artifact assembly.")

(defn source-artifact [ops diagnostic-ids governing-document
                       source-path source-text]
  (let [records ((:read-source-form-records ops) source-path source-text)
        forms (mapv :form records)
        _ ((:validate-ns-syntax! ops) source-path forms)
        module ((:parse-module ops) source-path forms)
        overrides ((:source-overrides ops) module)
        _ ((:validate-overrides! ops) source-path module overrides)
        c9-artifact ((:c9-artifact ops) source-path source-text)
        inventory ((:operation-inventory ops) module c9-artifact)
        outcomes ((:outcome-records ops) module inventory)
        checks ((:runtime-check-list ops) module outcomes)
        obligations ((:proof-obligation-list ops) module outcomes)
        certificates ((:proof-certificate-references ops) module)
        unsafe ((:unsafe-island-audit-manifest ops) module outcomes)
        report ((:taint-capability-safety-report ops) module)
        generated ((:generated-code-safety-provenance ops) module)
        optimization ((:optimization-safety-preservation ops) module)
        diagnostics ((:safety-diagnostics ops) source-path)
        verifier ((:verifier-report ops) c9-artifact inventory outcomes
                  checks obligations certificates unsafe report generated
                  optimization diagnostics)
        artifact-base
        {:kind :gravity/stage0-c10-safety-analysis-artifact
         :task "P06-D089"
         :document-set ["C10"]
         :governing-document governing-document
         :pass {:name :c10-safety-analysis-pipeline
                :input :ownership-checked-core
                :output :safety-checked-core
                :requires [:typed-core-module :effect-graph
                           :capability-proof-records :ownership-graph
                           :borrow-graph :lifetime-interval-map
                           :linear-resource-flow-graph :profile :target]
                :preserves [:source-spans :generated-origin :types
                            :effects :capabilities :ownership-facts
                            :profile :target :unsafe-metadata]
                :emits [:safety-operation-inventory
                        :safety-outcome-records :runtime-check-list
                        :proof-obligation-list
                        :proof-certificate-references
                        :unsafe-island-audit-manifest
                        :taint-capability-safety-report
                        :generated-code-safety-provenance
                        :optimization-safety-preservation
                        :safety-diagnostics]
                :rejects diagnostic-ids}
         :source-overrides overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :c9-ownership-checker-artifact
         (select-keys c9-artifact [:kind :artifact-id :ownership-graph
                                   :borrow-graph :lifetime-interval-map
                                   :linear-resource-flow-graph
                                   :capability-based-proof])
         :safety-operation-inventory inventory
         :safety-outcome-records outcomes
         :runtime-check-list checks
         :proof-obligation-list obligations
         :proof-certificate-references certificates
         :unsafe-island-audit-manifest unsafe
         :taint-capability-safety-report report
         :generated-code-safety-provenance generated
         :optimization-safety-preservation optimization
         :safety-verifier-report verifier
         :safety-diagnostics diagnostics
         :c10-safety-analysis-results
         {:documents ["C10"]
          :task "P06-D089"
          :required-diagnostic-ids diagnostic-ids
          :operation-inventory-status :complete
          :outcome-status :complete
          :runtime-check-status :complete
          :proof-obligation-status :complete
          :certificate-status :complete
          :unsafe-audit-status :complete
          :taint-capability-status :complete
          :generated-provenance-status :complete
          :optimization-preservation-status :complete
          :verifier-status (:status verifier)
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ ((:validate! ops) source-path artifact-base)
        capability-proof ((:capability-proof ops) artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id ((:artifact-id ops)
                         (assoc artifact-base
                                :capability-based-proof
                                capability-proof)))))
