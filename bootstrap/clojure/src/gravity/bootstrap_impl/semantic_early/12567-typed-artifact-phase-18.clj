; Semantic decomposition of HEAD reader line 12567.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-typed-artifact-phase-18
 [artifact state]
 (let
  [{:keys [checked dispatch-records]} state]
  (assoc
   artifact
   :facet-composition-records
   (distinct-records (:facet-composition-records checked))
   :schema-type-links
   (:schema-type-links checked)
   :dispatch-mode-records
   dispatch-records
   :handler-capability-and-profile-report
   (distinct-records (:handler-capability-and-profile-report checked))
   :ownership-resource-type-facts
   (:linear-resource-table checked)
   :initialization-facts
   (distinct-records (:initialization-facts checked))
   :safe15-conformance-records
   (distinct-records (:safe15-conformance-records checked))
   :safe15-invalidation-records
   (distinct-records (:safe15-invalidation-records checked))
   :safe-memory-operation-records
   (distinct-records (:safe-memory-operation-records checked))
   :safe11-generated-taint-propagation
   (distinct-records (:safe11-generated-taint-propagation checked))
   :allocator-runtime-manifests
   (distinct-records (:allocator-runtime-manifests checked))
   :alternative-memory-release-evidence
   (distinct-records (:alternative-memory-release-evidence checked))
   :facet-compatibility-records
   (distinct-records (:facet-compatibility-records checked))
   :safety-check-failure-records
   (distinct-records (:safety-check-failure-records checked))
   :safe15-proof-records
   (distinct-records (:safe15-proof-records checked)))))
