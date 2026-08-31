; Semantic decomposition of HEAD reader line 12567.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-typed-artifact-phase-03
 [artifact state]
 (let
  [{:keys [checked implementation-table]} state]
  (assoc
   artifact
   :safe7-abi-protocol-records
   (distinct-records (:safe7-abi-protocol-records checked))
   :alternative-type-conformance-fixture
   (alternative-type-conformance-fixture checked)
   :exhaustiveness-report
   (distinct-records (:exhaustiveness-report checked))
   :alternative-memory-conformance-reports
   (distinct-records (:alternative-memory-conformance-reports checked))
   :alternative-macro-conformance-fixture
   (alternative-macro-conformance-fixture checked)
   :safe-memory-runtime-check-records
   (distinct-records (:safe-memory-runtime-check-records checked))
   :implementation-table
   implementation-table
   :panic-lowering-records
   (distinct-records (:panic-lowering-records checked))
   :diagnostics
   []
   :safe8-backend-preservation-records
   (distinct-records (:safe8-backend-preservation-records checked))
   :grant-records
   (distinct-records (:grant-records checked))
   :safe9-optimization-proof-records
   (distinct-records (:safe9-optimization-proof-records checked))
   :safe-memory-borrow-graphs
   (distinct-records (:safe-memory-borrow-graphs checked))
   :safe1-dependency-safety-mode-records
   (distinct-records (:safe1-dependency-safety-mode-records checked))
   :atomic-ordering-records
   (distinct-records (:atomic-ordering-records checked))
   :facet-manifests
   (distinct-records (:facet-manifests checked))
   :alternative-type-effect-capability-records
   (distinct-records
    (:alternative-type-effect-capability-records checked))
   :alternative-macro-facet-dispatch-records
   (distinct-records
    (:alternative-macro-facet-dispatch-records checked)))))
