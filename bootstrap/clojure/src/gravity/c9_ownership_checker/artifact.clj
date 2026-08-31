(ns gravity.c9-ownership-checker.artifact
  "Artifact assembly for the hosted C9 ownership-checker facade.")

(defn source-artifact [ops source-path source-text]
  (let [{:keys [read-source-form-records validate-ns-syntax! parse-module
                c9-ownership-source-overrides c9-ownership-validate-overrides!
                compiler-c8-effect-source-artifact c9-ownership-graph c9-borrow-graph
                c9-lifetime-interval-map c9-escape-analysis-report
                c9-region-lifetime-graph c9-arena-generation-graph
                c9-linear-resource-flow-graph c9-transfer-records
                c9-runtime-check-records c9-unsafe-audit-references
                c9-ownership-diagnostics c9-ownership-verifier-report
                c9-ownership-validate! c9-ownership-capability-proof c4-artifact-id
                c9-ownership-governing-document c9-ownership-diagnostic-ids]} ops
        records (read-source-form-records source-path source-text)
        forms (mapv :form records)
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        overrides (c9-ownership-source-overrides module)
        _ (c9-ownership-validate-overrides! source-path module overrides)
        c8-artifact (compiler-c8-effect-source-artifact source-path source-text)
        effect-graph (:effect-graph c8-artifact)
        ownership (c9-ownership-graph module effect-graph)
        borrow (c9-borrow-graph module effect-graph)
        lifetimes (c9-lifetime-interval-map module)
        moves (select-keys ownership [:moves :consumes])
        escape (c9-escape-analysis-report module)
        region (c9-region-lifetime-graph module)
        arena (c9-arena-generation-graph module)
        linear (c9-linear-resource-flow-graph module)
        transfer (c9-transfer-records module)
        runtime (c9-runtime-check-records module)
        unsafe (c9-unsafe-audit-references module)
        diagnostics (c9-ownership-diagnostics source-path ownership)
        verifier (c9-ownership-verifier-report c8-artifact ownership borrow lifetimes moves
                                               escape region arena linear transfer runtime unsafe diagnostics)
        artifact-base
        {:kind :gravity/stage0-c9-ownership-checker-artifact
         :task "P06-D088" :document-set ["C9"]
         :governing-document c9-ownership-governing-document
         :pass {:name :c9-ownership-lifetime-region-checker
                :input :effected-core :output :ownership-checked-core
                :requires [:typed-core-module :effect-graph :capability-proof-records :profile :target]
                :preserves [:source-spans :generated-origin :types :effects :capabilities :profile :target
                            :unsafe-metadata]
                :emits [:ownership-graph :borrow-graph :lifetime-interval-map :move-consume-records
                        :escape-analysis-report :region-lifetime-graph :arena-generation-graph
                        :linear-resource-flow-graph :transfer-records :runtime-check-records
                        :unsafe-audit-references :ownership-diagnostics]
                :rejects c9-ownership-diagnostic-ids}
         :source-overrides overrides
         :module (select-keys module [:module :source-path :profile :target :effects :capabilities :safety :metadata])
         :c8-effect-checker-artifact
         (select-keys c8-artifact [:kind :artifact-id :effect-graph :namespace-effect-summary
                                   :capability-proof-records :capability-based-proof])
         :ownership-graph ownership :borrow-graph borrow :lifetime-interval-map lifetimes
         :move-consume-records moves :escape-analysis-report escape
         :region-lifetime-graph region :arena-generation-graph arena
         :linear-resource-flow-graph linear :transfer-records transfer
         :runtime-check-records runtime :unsafe-audit-references unsafe
         :ownership-verifier-report verifier :ownership-diagnostics diagnostics
         :c9-ownership-check-results
         {:documents ["C9"] :task "P06-D088"
          :required-diagnostic-ids c9-ownership-diagnostic-ids
          :ownership-graph-status :complete :borrow-graph-status :complete
          :lifetime-status :complete :move-consume-status :complete :escape-status :complete
          :region-status :complete :arena-status :complete :linear-status :complete
          :transfer-status :complete :runtime-check-status :complete :unsafe-audit-status :complete
          :verifier-status (:status verifier) :diagnostic-status :complete :status :complete}
         :diagnostics []}
        _ (c9-ownership-validate! source-path artifact-base)
        capability-proof (c9-ownership-capability-proof artifact-base)]
    (assoc artifact-base :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base :capability-based-proof capability-proof)))))

(defn file-artifact [source-artifact path]
  (source-artifact path (slurp path)))
