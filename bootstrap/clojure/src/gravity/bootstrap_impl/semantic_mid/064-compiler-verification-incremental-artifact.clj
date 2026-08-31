(defn- semantic-mid-compiler-verification-incremental-artifact
  [{:keys [incremental-graph cache-entry plugin-manifest input-id]}]
  {:incremental-dependency-graph incremental-graph
   :cache-key-schema
   {:artifact :gravity/cache-key-schema
    :required-fields [:stage :source :compiler :profile :target
                      :pass-contract :dependencies :build-effects
                      :capabilities :policy]
    :status :complete}
   :cache-entry-manifest cache-entry
   :invalidation-trace
   [{:invalidating-input :diagnostic-schema-change
     :affected-nodes [:diagnostics :target-artifact]
     :revalidation-stages [:compiler-verify]
     :status :recorded}]
   :artifact-reuse-report
   {:artifact :gravity/artifact-reuse-report
    :status :revalidated
    :reuse :allowed-after-validation}
   :revalidation-report
   {:artifact :gravity/revalidation-report
    :status :passed
    :checks [:cache-key :schema-version :producer-pass
             :proof-freshness :profile-target :diagnostic-schema]}
   :stale-proof-rejection-report
   {:artifact :gravity/stale-proof-rejection-report
    :status :covered
    :diagnostic "C16-PROOF"}
   :plugin-manifest plugin-manifest
   :api-compatibility-report
   {:artifact :gravity/plugin-api-compatibility
    :plugin (:plugin plugin-manifest)
    :status :compatible}
   :sandbox-grant
   {:artifact :gravity/plugin-sandbox-grant
    :plugin (:plugin plugin-manifest)
    :status :sandboxed
    :capabilities (:capabilities plugin-manifest)}
   :plugin-pass-registration-records
   [{:plugin (:plugin plugin-manifest)
     :pass :diagnostic-golden-check
     :contract :accepted
     :status :registered}]
   :plugin-execution-trace
   {:artifact :gravity/plugin-execution
    :plugin (:plugin plugin-manifest)
    :pass :diagnostic-golden-check
    :input input-id
    :output :gravity/diagnostic-stream
    :grants :gravity/plugin-sandbox-grant
    :build-effects []
    :diagnostics []
    :verifier-result :passed}
   :plugin-conformance-results
   {:artifact :gravity/plugin-conformance-results
    :status :passed
    :fixtures [:compiler-verification-fixtures]}})
