; Semantic decomposition of HEAD reader line 12567.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-typed-artifact-phase-16
 [artifact state]
 (let
  [{:keys [core-artifact checked build-log handled-table]} state]
  (assoc
   artifact
   :core-artifact-hash
   (str "sha256:" (sha256-hex (pr-str core-artifact)))
   :interop-parity-test-reports
   (distinct-records (:interop-parity-test-reports checked))
   :alternative-macro-generated-validation-records
   (distinct-records
    (:alternative-macro-generated-validation-records checked))
   :compile-time-conformance-fixture
   (compile-time-conformance-fixture checked build-log)
   :alternative-type-profile-soundness-evidence
   (distinct-records
    (:alternative-type-profile-soundness-evidence checked))
   :capability-revocation-records
   (distinct-records (:capability-revocation-records checked))
   :safe7-type-mapping-records
   (distinct-records (:safe7-type-mapping-records checked))
   :safe12-generated-origin-chains
   (distinct-records (:safe12-generated-origin-chains checked))
   :race-analysis-reports
   (distinct-records (:race-analysis-reports checked))
   :boundary-safety-conformance-fixture
   (boundary-safety-conformance-fixture checked)
   :provider-selection-records
   (distinct-records (:provider-selection-records checked))
   :handled-effect-table
   handled-table
   :safe14-runtime-capability-summaries
   (distinct-records (:safe14-runtime-capability-summaries checked))
   :unreachable-branch-diagnostics
   (distinct-records (:unreachable-branch-diagnostics checked))
   :safe16-diagnostic-match-records
   (distinct-records (:safe16-diagnostic-match-records checked))
   :safe14-lockfile-records
   (distinct-records (:safe14-lockfile-records checked))
   :safe15-unsafe-wrapper-audit-views
   (distinct-records (:safe15-unsafe-wrapper-audit-views checked))
   :facet-domain-ir-records
   (distinct-records (:facet-domain-ir-records checked)))))
