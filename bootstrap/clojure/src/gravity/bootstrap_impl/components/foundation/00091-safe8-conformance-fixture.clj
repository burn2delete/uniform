

(defn safe8-conformance-fixture
  [checker-state]
  (let [covered (cond-> #{}
                  (seq (:safe8-concurrency-graphs checker-state))
                  (conj :safe8-concurrency-graph)
                  (seq (:safe8-task-capture-records checker-state))
                  (conj :safe8-task-capture)
                  (seq (:safe8-ownership-transfer-records checker-state))
                  (conj :safe8-ownership-transfer)
                  (seq (:safe8-shared-state-access-records checker-state))
                  (conj :safe8-shared-state-access)
                  (seq (:safe8-synchronization-proof-records checker-state))
                  (conj :safe8-synchronization-proof)
                  (seq (:safe8-atomic-memory-order-records checker-state))
                  (conj :safe8-atomic-order)
                  (seq (:safe8-blocking-cancellation-records checker-state))
                  (conj :safe8-blocking-cancellation)
                  (seq (:safe8-backend-preservation-records checker-state))
                  (conj :safe8-backend-preservation)
                  (seq (:safe8-race-analysis-reports checker-state))
                  (conj :safe8-race-analysis))
        missing (vec (remove covered safe8-required-families))]
    {:required-families safe8-required-families
     :covered-families (vec (sort-by name covered))
     :document :SAFE8
     :missing-families missing
     :status (if (empty? missing) :complete :incomplete)}))

(defn safe9-conformance-fixture
  [checker-state]
  (let [covered (cond-> #{}
                  (seq (:safe9-numeric-mode-records checker-state))
                  (conj :safe9-numeric-mode)
                  (seq (:safe9-runtime-check-records checker-state))
                  (conj :safe9-runtime-check)
                  (seq (:safe9-range-proof-records checker-state))
                  (conj :safe9-range-proof)
                  (seq (:safe9-floating-mode-records checker-state))
                  (conj :safe9-floating-mode)
                  (seq (:safe9-elementary-approximation-records checker-state))
                  (conj :safe9-elementary-approximation)
                  (seq (:safe9-relaxed-approval-records checker-state))
                  (conj :safe9-relaxed-approval)
                  (seq (:safe9-optimization-proof-records checker-state))
                  (conj :safe9-optimization-proof)
                  (seq (:safe9-backend-lowering-records checker-state))
                  (conj :safe9-backend-lowering))
        missing (vec (remove covered safe9-required-families))]
    {:required-families safe9-required-families
     :covered-families (vec (sort-by name covered))
     :document :SAFE9
     :missing-families missing
     :status (if (empty? missing) :complete :incomplete)}))

(defn safe11-conformance-fixture
  [checker-state]
  (let [covered (cond-> #{}
                  (seq (:safe11-taint-source-records checker-state))
                  (conj :safe11-taint-source)
                  (seq (:safe11-taint-flow-records checker-state))
                  (conj :safe11-taint-flow)
                  (seq (:safe11-validator-contracts checker-state))
                  (conj :safe11-validator-contract)
                  (seq (:safe11-residual-constraint-records checker-state))
                  (conj :safe11-residual-constraint)
                  (seq (:safe11-sink-authorization-records checker-state))
                  (conj :safe11-sink-authorization)
                  (seq (:safe11-parameterization-records checker-state))
                  (conj :safe11-parameterization)
                  (seq (:safe11-deserialization-records checker-state))
                  (conj :safe11-deserialization)
                  (seq (:safe11-secret-redaction-records checker-state))
                  (conj :safe11-secret-redaction)
                  (seq (:safe11-prompt-tool-policy-records checker-state))
                  (conj :safe11-prompt-tool-policy)
                  (seq (:safe11-generated-taint-propagation checker-state))
                  (conj :safe11-generated-taint)
                  (seq (:safe11-unsafe-clear-audits checker-state))
                  (conj :safe11-unsafe-clear-audit))
        missing (vec (remove covered safe11-required-families))]
    {:required-families safe11-required-families
     :covered-families (vec (sort-by name covered))
     :document :SAFE11
     :missing-families missing
     :status (if (empty? missing) :complete :incomplete)}))