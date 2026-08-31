; Semantic decomposition of HEAD reader line 12567.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-typed-artifact-phase-08
 [artifact state]
 (let
  [{:keys [checked effect-facts]} state]
  (assoc
   artifact
   :safe13-model-call-traces
   (distinct-records (:safe13-model-call-traces checked))
   :safe8-shared-state-access-records
   (distinct-records (:safe8-shared-state-access-records checked))
   :synchronization-facts
   (distinct-records (:synchronization-facts checked))
   :unsafe-raw-memory-audit-records
   (distinct-records (:unsafe-raw-memory-audit-records checked))
   :safe6-policy-decision-records
   (distinct-records (:safe6-policy-decision-records checked))
   :alternative-macro-cache-decisions
   (distinct-records (:alternative-macro-cache-decisions checked))
   :alternative-macro-expansion-traces
   (distinct-records (:alternative-macro-expansion-traces checked))
   :safe14-conformance-fixture
   (safe14-conformance-fixture checked)
   :safe13-conformance-fixture
   (safe13-conformance-fixture checked)
   :safe16-conformance-reports
   (distinct-records (:safe16-conformance-reports checked))
   :safe11-validator-contracts
   (distinct-records (:safe11-validator-contracts checked))
   :safe-memory-arena-generation-graphs
   (distinct-records (:safe-memory-arena-generation-graphs checked))
   :effect-legality-report
   effect-facts
   :alternative-memory-safety-classifications
   (distinct-records
    (:alternative-memory-safety-classifications checked))
   :safe-memory-terminal-operation-records
   (distinct-records (:safe-memory-terminal-operation-records checked))
   :safe9-relaxed-approval-records
   (distinct-records (:safe9-relaxed-approval-records checked))
   :thrown-error-effect-records
   (distinct-records (:thrown-error-effect-records checked))
   :alternative-memory-lifetime-facts
   (distinct-records (:alternative-memory-lifetime-facts checked)))))
