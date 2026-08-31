

(defn validate-stage0-compiled-performance-regression!
  [module performance-report]
  (when-not (true? (:semantic-gates-passed performance-report))
    (compiled-conformance-fail!
     "TEST12003" module performance-report
     {:missing-fields [:semantic-gates-passed]})))