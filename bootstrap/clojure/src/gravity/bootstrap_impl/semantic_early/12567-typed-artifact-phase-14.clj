; Semantic decomposition of HEAD reader line 12567.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-typed-artifact-phase-14
 [artifact state]
 (let
  [{:keys [checked]} state]
  (assoc
   artifact
   :safe-memory-allocation-release-maps
   (distinct-records (:safe-memory-allocation-release-maps checked))
   :concurrency-ownership-transfer-records
   (distinct-records (:concurrency-ownership-transfer-records checked))
   :safe13-memory-retention-policies
   (distinct-records (:safe13-memory-retention-policies checked))
   :safe10-runtime-check-records
   (distinct-records (:safe10-runtime-check-records checked))
   :safe6-conformance-records
   (distinct-records (:safe6-conformance-records checked))
   :safe-memory-cleanup-records
   (distinct-records (:safe-memory-cleanup-records checked))
   :safe11-taint-flow-records
   (distinct-records (:safe11-taint-flow-records checked))
   :final-safety-conformance-fixture
   (final-safety-conformance-fixture checked)
   :alternative-memory-allocation-strategies
   (distinct-records
    (:alternative-memory-allocation-strategies checked))
   :safe7-conformance-records
   (distinct-records (:safe7-conformance-records checked))
   :safe-memory-structured-resource-lowerings
   (distinct-records
    (:safe-memory-structured-resource-lowerings checked))
   :error-type-declarations
   (distinct-records (:error-type-declarations checked))
   :safe-memory-escape-analysis-records
   (distinct-records (:safe-memory-escape-analysis-records checked))
   :safe15-conformance-fixture
   (safe15-conformance-fixture checked)
   :safe1-conformance-fixture
   (safe1-conformance-fixture checked)
   :ai-tool-error-records
   (distinct-records (:ai-tool-error-records checked))
   :safe-memory-backend-preservation-records
   (distinct-records
    (:safe-memory-backend-preservation-records checked))
   :safe13-conformance-records
   (distinct-records (:safe13-conformance-records checked)))))
