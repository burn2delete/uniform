(ns gravity.c8-effect-checker.artifact
  "C8 pipeline orchestration and artifact assembly.")

(defn source-artifact
  [ops source-path source-text]
  (let [{:keys [read-source-form-records validate-ns-syntax! parse-module
                effect-source-overrides effect-validate-overrides!
                c7-type-source-artifact effect-graph legality-records
                capability-proof-records build-effect-log replay-requirements
                ordering-constraints residual-effect-report effect-diagnostics
                effect-verifier-report effect-validate! effect-capability-proof
                artifact-id diagnostic-ids governing-document]} ops
        records (read-source-form-records source-path source-text)
        forms (mapv :form records)
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        overrides (effect-source-overrides module)
        _ (effect-validate-overrides! source-path module overrides)
        c7-artifact (c7-type-source-artifact source-path source-text)
        type-facts (:type-facts c7-artifact)
        functions (:function-type-table c7-artifact)
        effect-graph-value (effect-graph module type-facts functions)
        legality (legality-records module effect-graph-value)
        capability-records (capability-proof-records module effect-graph-value)
        build-log (build-effect-log module)
        replay (replay-requirements effect-graph-value)
        ordering (ordering-constraints effect-graph-value)
        residual (residual-effect-report effect-graph-value)
        diagnostics (effect-diagnostics source-path type-facts)
        verifier (effect-verifier-report module effect-graph-value legality
                                         capability-records build-log replay
                                         ordering residual diagnostics)
        artifact-base
        {:kind :gravity/stage0-c8-effect-checker-artifact
         :task "P06-D087"
         :document-set ["C8"]
         :governing-document governing-document
         :pass {:name :c8-effect-checker
                :input :typed-core
                :output :effected-core
                :requires [:typed-core-module :type-facts :function-types
                           :profile :capabilities :build-grants]
                :preserves [:source-spans :generated-origin :types
                            :profile :target :capabilities]
                :emits [:effect-graph :function-latent-effect-table
                        :namespace-effect-summary :module-effect-summary
                        :capability-proof-records :build-effect-log
                        :replay-effect-requirements
                        :effect-ordering-constraints
                        :residual-effect-report
                        :effect-diagnostics]
                :rejects diagnostic-ids}
         :source-overrides overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :c7-type-checker-artifact
         (select-keys c7-artifact [:kind :artifact-id :typed-core-module
                                   :type-environment :function-type-table
                                   :capability-based-proof])
         :effect-graph effect-graph-value
         :function-latent-effect-table
         {:artifact :gravity/c8-function-latent-effect-table
          :functions (get-in effect-graph-value [:functions])
          :status :complete}
         :namespace-effect-summary (:namespace effect-graph-value)
         :module-effect-summary
         {:declared (:effects module)
          :inferred (get-in effect-graph-value [:namespace :inferred])
          :status :complete}
         :effect-legality-report legality
         :capability-proof-records capability-records
         :build-effect-log build-log
         :replay-effect-requirements replay
         :effect-ordering-constraints ordering
         :residual-effect-report residual
         :effect-verifier-report verifier
         :effect-diagnostics diagnostics
         :c8-effect-check-results
         {:documents ["C8"]
          :task "P06-D087"
          :required-diagnostic-ids diagnostic-ids
          :effect-graph-status :complete
          :function-latent-status :complete
          :namespace-summary-status :complete
          :module-summary-status :complete
          :capability-proof-status :accepted
          :build-effect-status :complete
          :replay-status :complete
          :ordering-status :complete
          :residual-status :complete
          :verifier-status (:status verifier)
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (effect-validate! source-path artifact-base)
        capability-proof (effect-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (artifact-id (assoc artifact-base
                                            :capability-based-proof
                                            capability-proof)))))

(defn file-artifact [source-artifact path]
  (source-artifact path (slurp path)))
