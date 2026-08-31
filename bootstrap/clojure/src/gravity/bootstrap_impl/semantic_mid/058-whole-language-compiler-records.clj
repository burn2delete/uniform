(defn- semantic-mid-whole-language-compiler-records
  [{:keys [source-path proof-contract inventory-artifact pipeline-artifact
           source-syntax-artifact core-artifact runtime-artifact
           accepted-artifact rejected-artifact rebuild-artifact
           stage-comparison-artifact conformance-artifact
           provenance-artifact tcb-artifact unsafe-artifact
           hosted-compiler-artifact]
    :as inputs}]
  (let [stage-support
        (p15-s23-whole-language-compiler-stage-support-matrix
         source-path proof-contract inventory-artifact pipeline-artifact
         source-syntax-artifact core-artifact runtime-artifact)
        link-table
        (p15-s23-whole-language-compiler-evidence-link-table
         inventory-artifact pipeline-artifact source-syntax-artifact
         core-artifact runtime-artifact accepted-artifact rejected-artifact
         rebuild-artifact stage-comparison-artifact conformance-artifact
         provenance-artifact tcb-artifact unsafe-artifact)
        accepted-record
        (p15-s23-whole-language-compiler-accepted-record
         accepted-artifact hosted-compiler-artifact)
        rejected-record
        (p15-s23-whole-language-compiler-rejected-record rejected-artifact)
        boundary-record
        (p15-s23-whole-language-compiler-residual-boundary-record
         accepted-artifact tcb-artifact)
        lineage-record
        (p15-s23-whole-language-compiler-lineage-record
         source-path inventory-artifact provenance-artifact
         stage-comparison-artifact)
        manifest
        (p15-s23-whole-language-compiler-manifest
         source-path proof-contract inventory-artifact pipeline-artifact
         stage-support accepted-record rejected-record boundary-record
         lineage-record link-table)
        auditor-query
        (p15-s23-whole-language-compiler-auditor-query-record
         manifest stage-support accepted-record rejected-record
         boundary-record lineage-record link-table)
        candidate
        {:proof-contract proof-contract
         :stage-support-matrix stage-support
         :compiler-artifact-manifest manifest
         :compiler-evidence-link-table link-table
         :accepted-application-compile-record accepted-record
         :rejected-application-diagnostic-record rejected-record
         :residual-trusted-boundary-record boundary-record
         :compiler-artifact-lineage-record lineage-record
         :reproducible-rebuild-log-artifact rebuild-artifact
         :stage-comparison-report-artifact stage-comparison-artifact
         :self-hosting-conformance-report-artifact conformance-artifact
         :bootstrap-provenance-attestation-artifact provenance-artifact}
        diagnostics
        (p15-s23-whole-language-compiler-proof-diagnostics
         source-path candidate)
        _ (when (seq diagnostics)
            (p15-s23-whole-language-compiler-fail!
             (:diagnostic (first diagnostics)) source-path candidate
             {:diagnostics diagnostics}))
        proof-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:source-path source-path
                       :proof-contract proof-contract
                       :compiler-artifact-id (:compiler-artifact-id manifest)
                       :stage-support stage-support
                       :accepted-record accepted-record
                       :rejected-record rejected-record
                       :lineage-record lineage-record
                       :link-table link-table})))
        rejected-records
        (p15-s23-whole-language-compiler-rejected-records
         source-path candidate)]
    (assoc inputs
           :stage-support stage-support
           :link-table link-table
           :accepted-record accepted-record
           :rejected-record rejected-record
           :boundary-record boundary-record
           :lineage-record lineage-record
           :manifest manifest
           :auditor-query auditor-query
           :candidate candidate
           :proof-id proof-id
           :rejected-records rejected-records)))
