(defn- semantic-mid-formal-governance-definition-context
  [reader-source-path definitions]
  (let [formal-governance
        (stage1-reader-formal-release-governance-seed-retirement-literal-definition-value
         reader-source-path definitions
         'stage1-reader-formal-release-governance-seed-retirement)
        diagnostics (:diagnostics formal-governance)
        missing-diagnostics
        (remove #(contains? diagnostics %)
                [:missing-entrypoint
                 :unsupported-formal-governance-operation
                 :missing-formal-release-governance
                 :unverifiable-deployment-custody
                 :missing-self-hosting-evidence
                 :unreproducible-full-compiler-rebuild
                 :missing-stage-compiler-equivalence
                 :missing-tcb-delta
                 :illegal-governance-or-deployment-fallback
                 :invalid-formal-release-governance])
        direct-stages (:direct-stages formal-governance)
        formal-record (:formal-release-governance-record formal-governance)
        deployment-custody (:deployment-custody-record formal-governance)
        self-hosting-evidence (:self-hosting-evidence formal-governance)
        seed-retirement-evidence (:seed-retirement-evidence formal-governance)
        tcb-delta-record (:tcb-delta-record formal-governance)
        unsafe-audit-report (:unsafe-audit-report formal-governance)
        formal-provenance-record
        (:formal-release-provenance-record formal-governance)]
    {:reader-source-path reader-source-path
     :formal-governance formal-governance
     :diagnostics diagnostics
     :missing-diagnostics missing-diagnostics
     :direct-stages direct-stages
     :formal-record formal-record
     :deployment-custody deployment-custody
     :self-hosting-evidence self-hosting-evidence
     :seed-retirement-evidence seed-retirement-evidence
     :tcb-delta-record tcb-delta-record
     :unsafe-audit-report unsafe-audit-report
     :formal-provenance-record formal-provenance-record}))
