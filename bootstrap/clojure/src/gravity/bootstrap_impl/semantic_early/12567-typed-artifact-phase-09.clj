; Semantic decomposition of HEAD reader line 12567.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-typed-artifact-phase-09
 [artifact state]
 (let
  [{:keys [checked type-coverage]} state]
  (assoc
   artifact
   :safe16-fixture-manifests
   (distinct-records (:safe16-fixture-manifests checked))
   :ownership-borrow-facts
   (distinct-records (:ownership-borrow-facts checked))
   :safe14-unsafe-summaries
   (distinct-records (:safe14-unsafe-summaries checked))
   :continuation-and-replay-safety-report
   (distinct-records (:continuation-and-replay-safety-report checked))
   :compile-time-provider-replay-records
   (distinct-records (:compile-time-provider-replay-records checked))
   :capability-report
   (:capability-records checked)
   :safe6-operation-inventories
   (distinct-records (:safe6-operation-inventories checked))
   :safe1-unsafe-island-audit-records
   (distinct-records (:safe1-unsafe-island-audit-records checked))
   :alternative-type-diagnostic-mapping-records
   (distinct-records
    (:alternative-type-diagnostic-mapping-records checked))
   :safe11-unsafe-clear-audits
   (distinct-records (:safe11-unsafe-clear-audits checked))
   :safe1-generated-code-safety-provenance
   (distinct-records (:safe1-generated-code-safety-provenance checked))
   :type-conformance-fixture
   (type-conformance-fixture type-coverage)
   :safe13-replay-records
   (distinct-records (:safe13-replay-records checked))
   :standard-library-conformance-fixture
   (standard-library-conformance-fixture checked)
   :interop-migration-shim-records
   (distinct-records (:interop-migration-shim-records checked))
   :facet-activation-records
   (distinct-records (:facet-activation-records checked))
   :safe6-safe-wrapper-records
   (distinct-records (:safe6-safe-wrapper-records checked))
   :safe12-macro-build-effect-records
   (distinct-records (:safe12-macro-build-effect-records checked)))))
