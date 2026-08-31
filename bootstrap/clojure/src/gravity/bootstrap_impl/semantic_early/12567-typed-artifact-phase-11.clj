; Semantic decomposition of HEAD reader line 12567.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-typed-artifact-phase-11
 [artifact state]
 (let
  [{:keys [checked multimethods]} state]
  (assoc
   artifact
   :interop-capability-effect-records
   (distinct-records (:interop-capability-effect-records checked))
   :safe-memory-linear-flow-graphs
   (distinct-records (:safe-memory-linear-flow-graphs checked))
   :allocation-effect-records
   (distinct-records (:allocation-effect-records checked))
   :dynamic-boundary-records
   (:dynamic-boundary-records checked)
   :safe16-expected-outcome-manifests
   (distinct-records (:safe16-expected-outcome-manifests checked))
   :safe6-release-audit-reports
   (distinct-records (:safe6-release-audit-reports checked))
   :linear-resource-table
   (distinct-records (:linear-resource-table checked))
   :multimethod-dispatch-tables
   multimethods
   :safe13-human-review-records
   (distinct-records (:safe13-human-review-records checked))
   :alternative-type-domain-facts
   (distinct-records (:alternative-type-domain-facts checked))
   :safe6-unsafe-island-records
   (distinct-records (:safe6-unsafe-island-records checked))
   :compile-time-evaluation-trace
   (distinct-records (:compile-time-evaluation-trace checked))
   :safe11-sink-authorization-records
   (distinct-records (:safe11-sink-authorization-records checked))
   :safe9-elementary-approximation-records
   (distinct-records (:safe9-elementary-approximation-records checked))
   :provider-declaration-records
   (distinct-records (:provider-declaration-records checked))
   :safe-memory-generated-linear-flow-records
   (distinct-records
    (:safe-memory-generated-linear-flow-records checked))
   :safe14-build-effect-summaries
   (distinct-records (:safe14-build-effect-summaries checked))
   :safe16-conformance-records
   (distinct-records (:safe16-conformance-records checked)))))
