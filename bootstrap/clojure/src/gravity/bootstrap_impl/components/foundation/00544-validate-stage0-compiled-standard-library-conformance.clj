

(defn validate-stage0-compiled-standard-library-conformance!
  [module standard-library-report]
  (when-not (p14-present? (:modules standard-library-report))
    (compiled-conformance-fail!
     "TEST7001" module standard-library-report
     {:missing-fields [:modules]})))

(defn validate-stage0-compiled-ai-workflow-conformance!
  [module ai-workflow-report]
  (when-not (p14-present? (:replay-traces ai-workflow-report))
    (compiled-conformance-fail!
     "TEST8003" module ai-workflow-report
     {:missing-fields [:replay-traces]})))

(defn validate-stage0-compiled-fuzz-property-suite!
  [module fuzz-property-suite]
  (when-not (:seed fuzz-property-suite)
    (compiled-conformance-fail!
     "TEST9001" module fuzz-property-suite
     {:missing-fields [:seed]})))

(defn validate-stage0-compiled-differential-report!
  [module differential-report]
  (when-not (empty? (:accepted-divergence differential-report))
    (compiled-conformance-fail!
     "TEST10002" module differential-report
     {:missing-fields [:accepted-divergence]})))

(defn validate-stage0-compiled-formal-proof-report!
  [module formal-proof-report]
  (when-not (every? true? (map :machine-checkable
                               (:claims formal-proof-report)))
    (compiled-conformance-fail!
     "TEST11003" module formal-proof-report
     {:missing-fields [:machine-checkable]})))