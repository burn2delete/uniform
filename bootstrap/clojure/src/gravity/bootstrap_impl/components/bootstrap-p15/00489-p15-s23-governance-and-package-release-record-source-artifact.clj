

(defn p15-s23-governance-and-package-release-record-source-artifact
  [source-path]
  (p15-s23-context-artifact
   :governance-and-package-release-record source-path
   #(let [source-data (p15-s23-compiler-source-form-record source-path)
        proof-contract
        (p15-s23-compiler-def-value
         source-path
         (:forms source-data)
         'p15-s23-governance-and-package-release-record)
        whole-artifact
        (p15-s23-current-candidate-artifact-evidence
         :whole-language-compiler-artifact)
        runtime-artifact
        (p15-s23-current-candidate-artifact-evidence
         :runtime-manifest-and-capability-enforcement-report)
        rebuild-artifact
        (p15-s23-current-candidate-artifact-evidence
         :reproducible-rebuild-log)
        conformance-artifact
        (p15-s23-current-candidate-artifact-evidence
         :conformance-report)
        provenance-artifact
        (p15-s23-current-candidate-artifact-evidence
         :provenance-attestation)
        tcb-artifact
        (p15-s23-current-candidate-artifact-evidence
         :tcb-delta-record)
        unsafe-artifact
        (p15-s23-current-candidate-artifact-evidence
         :unsafe-audit-report)
        rfc-record
        (p15-s23-governance-package-rfc-record
         proof-contract whole-artifact conformance-artifact)
        package-record
        (p15-s23-package-release-record
         proof-contract whole-artifact runtime-artifact conformance-artifact
         provenance-artifact unsafe-artifact)
        reproducible-record
        (p15-s23-reproducible-release-record
         rebuild-artifact package-record provenance-artifact)
        registry-decision
        (p15-s23-registry-policy-decision
         proof-contract package-record reproducible-record unsafe-artifact)
        release-decision
        (p15-s23-release-decision-record
         proof-contract registry-decision)
        provenance-link
        (p15-s23-release-provenance-link
         whole-artifact provenance-artifact package-record
         reproducible-record release-decision)
        auditor-query
        (p15-s23-governance-package-auditor-query-record
         rfc-record package-record reproducible-record registry-decision
         provenance-link release-decision)
        summary-record
        (p15-s23-governance-and-package-release-record-summary
         rfc-record package-record reproducible-record registry-decision
         provenance-link release-decision auditor-query)
        candidate
        {:proof-contract proof-contract
         :rfc-record rfc-record
         :package-release-record package-record
         :reproducible-release-record reproducible-record
         :registry-policy-decision registry-decision
         :release-decision-record release-decision
         :release-provenance-link provenance-link
         :auditor-query-record auditor-query
         :governance-and-package-release-record summary-record}
        diagnostics
        (p15-s23-governance-package-release-proof-diagnostics
         source-path candidate)
        _ (when (seq diagnostics)
            (p15-s23-governance-package-release-fail!
             (:diagnostic (first diagnostics)) source-path candidate
             {:diagnostics diagnostics}))
        proof-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:source-path source-path
                       :proof-contract proof-contract
                       :rfc-record rfc-record
                       :package-record package-record
                       :reproducible-record reproducible-record
                       :registry-decision registry-decision
                       :release-decision release-decision
                       :provenance-link provenance-link
                       :auditor-query auditor-query
                       :summary-record summary-record})))
        rejected-records
        (p15-s23-governance-package-release-rejected-records
         source-path candidate)
        artifact-base
        {:kind
         :gravity/p15-s23-governance-and-package-release-record-artifact
         :phase "15"
         :task "P15-S23"
         :stage :p15-s23-governance-and-package-release-record
         :source-path source-path
         :proof-id proof-id
         :proof-contract proof-contract
         :whole-language-compiler-artifact
         (select-keys whole-artifact
                      [:kind :artifact-id :proof-id
                       :compiler-artifact-manifest
                       :capability-based-proof])
         :runtime-manifest-capability-artifact
         (select-keys runtime-artifact
                      [:kind :artifact-id :proof-id
                       :runtime-capability-manifest
                       :capability-based-proof])
         :reproducible-rebuild-log-artifact
         (select-keys rebuild-artifact
                      [:kind :artifact-id :proof-id
                       :artifact-identity-comparison
                       :capability-based-proof])
         :self-hosting-conformance-report-artifact
         (select-keys conformance-artifact
                      [:kind :artifact-id :proof-id
                       :stage-support-conformance-record
                       :capability-based-proof])
         :bootstrap-provenance-attestation-artifact
         (select-keys provenance-artifact
                      [:kind :artifact-id :proof-id
                       :bootstrap-provenance-record
                       :compiler-lineage-graph
                       :canonical-provenance-payload
                       :revocation-check-report
                       :capability-based-proof])
         :tcb-delta-record-artifact
         (select-keys tcb-artifact
                      [:kind :artifact-id :proof-id
                       :tcb-delta-record
                       :capability-based-proof])
         :unsafe-audit-report-artifact
         (select-keys unsafe-artifact
                      [:kind :artifact-id :proof-id
                       :unsafe-audit-report
                       :unsafe-island-index
                       :package-safety-metadata
                       :capability-based-proof])
         :rfc-record rfc-record
         :package-release-record package-record
         :reproducible-release-record reproducible-record
         :registry-policy-decision registry-decision
         :release-decision-record release-decision
         :release-provenance-link provenance-link
         :auditor-query-record auditor-query
         :governance-and-package-release-record summary-record
         :full-language-compiler-self-hosted?
         (get-in proof-contract
                 [:self-hosting-claims
                  :full-language-compiler-self-hosted?])
         :clojure-seed-retired?
         (get-in proof-contract
                 [:self-hosting-claims :clojure-seed-retired?])
         :release-eligible? (:release-eligible? release-decision)
         :registry-publication-eligible?
         (:registry-publication-eligible? registry-decision)
         :accepted-p15-s23-governance-package-release-fixtures
         [{:fixture source-path
           :status :accepted
           :governance-package-record-id
           (:governance-package-record-id summary-record)}]
         :rejected-p15-s23-governance-package-release-fixtures
         rejected-records
         :p15-s23-governance-package-release-diagnostic-stream
         (p15-s23-governance-package-release-diagnostic-stream
          source-path proof-id)
         :p15-s23-governance-package-release-results
         {:accepted-fixtures 1
          :rejected-fixtures (count rejected-records)
          :diagnostic-count
          (count p15-s23-governance-package-release-diagnostic-ids)
          :governance-package-record-id
          (:governance-package-record-id summary-record)
          :package-release-id (:package-release-id package-record)
          :registry-decision (:decision registry-decision)
          :governance-and-package-policy-satisfied?
          (:governance-and-package-policy-satisfied? release-decision)
          :release-eligible? (:release-eligible? release-decision)
          :release-blockers (:release-blockers release-decision)
          :status :in-progress}
         :diagnostics []}
        proof
        (p15-s23-governance-package-release-record-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id
           (c4-artifact-id
            (assoc artifact-base :capability-based-proof proof))))))

(defn p15-s23-governance-and-package-release-record-file-artifact
  [path]
  (when-not (.isFile (java.io.File. path))
    (p15-s23-governance-package-release-fail!
     "P15S23L001" path nil {:missing-fields [:compiler-source]}))
  (p15-s23-with-artifact-build-context
   #(p15-s23-governance-and-package-release-record-source-artifact path)))