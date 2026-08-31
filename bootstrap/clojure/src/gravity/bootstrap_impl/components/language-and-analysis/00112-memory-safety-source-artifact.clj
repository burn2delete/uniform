

(defn memory-safety-source-artifact
  [source-path source-text]
  (let [typed-artifact (typed-source-artifact source-path source-text)
        conformance (:safe-memory-conformance-fixture typed-artifact)]
    {:kind :gravity/stage0-memory-safety-artifact
     :pass {:name :memory-ownership-region-linear-safety
            :input :typed-effected-core
            :output :memory-safety-analysis-report
            :requires [:reader :namespace-analyzer :macro-expansion
                       :core-lowering :type-effect-capability-check
                       :safety-outcome-classifier]
            :preserves [:source-spans :generated-origin :profile :types
                        :effects :capabilities :safety-outcomes]
            :emits [:memory-safety-operation-records
                    :runtime-check-manifest
                    :allocation-release-maps
                    :escape-analysis-records
                    :proof-records
                    :backend-preservation-records
                    :unsafe-memory-audit-records
                    :ownership-graph
                    :borrow-graph
                    :lifetime-interval-map
                    :ownership-transfer-records
                    :runtime-borrow-check-records
                    :region-lifetime-map
                    :arena-generation-graph
                    :reset-invalidation-records
                    :provider-records
                    :cleanup-records
                    :linear-resource-flow-graph
                    :terminal-operation-records
                    :exceptional-cleanup-records
                    :structured-resource-lowering-records
                    :generated-linear-flow-records
                    :safety-certificate-inputs]
            :rejects safe-memory-diagnostic-ids}
     :documents ["SAFE2" "SAFE3" "SAFE4" "SAFE5"]
     :module (:module typed-artifact)
     :typed-core-artifact-hash (str "sha256:" (sha256-hex (pr-str typed-artifact)))
     :memory-safety-operation-records (:safe-memory-operation-records typed-artifact)
     :runtime-check-manifest (:safe-memory-runtime-check-records typed-artifact)
     :allocation-release-maps (:safe-memory-allocation-release-maps typed-artifact)
     :escape-analysis-records (:safe-memory-escape-analysis-records typed-artifact)
     :proof-records (:safe-memory-proof-records typed-artifact)
     :backend-memory-safety-preservation-records (:safe-memory-backend-preservation-records typed-artifact)
     :unsafe-memory-audit-records (:safe-memory-unsafe-audit-records typed-artifact)
     :ownership-graph (:safe-memory-ownership-graphs typed-artifact)
     :borrow-graph (:safe-memory-borrow-graphs typed-artifact)
     :lifetime-interval-map (:safe-memory-lifetime-interval-maps typed-artifact)
     :ownership-transfer-records (:safe-memory-transfer-records typed-artifact)
     :runtime-borrow-check-records (:safe-memory-runtime-borrow-check-records typed-artifact)
     :region-lifetime-map (:safe-memory-region-lifetime-maps typed-artifact)
     :arena-generation-graph (:safe-memory-arena-generation-graphs typed-artifact)
     :reset-invalidation-records (:safe-memory-reset-invalidation-records typed-artifact)
     :provider-records (:safe-memory-provider-records typed-artifact)
     :cleanup-records (:safe-memory-cleanup-records typed-artifact)
     :linear-resource-flow-graph (:safe-memory-linear-flow-graphs typed-artifact)
     :terminal-operation-records (:safe-memory-terminal-operation-records typed-artifact)
     :exceptional-cleanup-records (:safe-memory-exceptional-cleanup-records typed-artifact)
     :structured-resource-lowering-records (:safe-memory-structured-resource-lowerings typed-artifact)
     :generated-linear-flow-records (:safe-memory-generated-linear-flow-records typed-artifact)
     :profile-safety-capability-report {:profile (get-in typed-artifact [:module :profile])
                                        :safety-mode (get-in typed-artifact [:module :safety])
                                        :effects (get-in typed-artifact [:namespace-effect-summary :inferred])
                                        :capabilities (set (map :capability (:provider-selection-records typed-artifact)))
                                        :profile-specific? true}
     :safety-certificate-inputs {:documents ["SAFE2" "SAFE3" "SAFE4" "SAFE5"]
                                 :conformance-status (:status conformance)
                                 :required-families (:required-families conformance)
                                 :covered-families (:covered-families conformance)}
     :safe-memory-conformance-fixture conformance
     :diagnostics []}))