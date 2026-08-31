

(defn p15-s23-reference-runtime-contract-validation!
  [source-path target definitions authoritative-module plan]
  (let [derived
        (p15-s23-reference-runtime-derived-contract-facts
         source-path target plan)]
    (p15-s23-reference-runtime-validate-function-graph!
     source-path target definitions derived)
    (p15-s23-reference-runtime-validate-function-effects!
     source-path target definitions authoritative-module derived)
    (p15-s23-reference-runtime-validate-effect-namespace!
     source-path target definitions authoritative-module derived)
    (p15-s23-reference-runtime-validate-scope-records!
     source-path target definitions derived)
    (p15-s23-reference-runtime-validate-cross-links!
     source-path target definitions authoritative-module derived)
    {:artifact :gravity/p15-s23-reference-runtime-contract-validation
     :status :complete
     :function-count (count p15-s23-reference-runtime-function-set)
     :contract-definition-count
     (count p15-s23-reference-runtime-contract-definition-names)
     :operation-count
     (reduce + 0 (map count (vals (:operation-paths derived))))
     :proven-allocation-count
     (reduce + 0 (vals (:proven-allocation-operation-counts derived)))
     :allocation-unproven-count
     (reduce + 0 (vals (:allocation-unproven-operation-counts derived)))
     :handler-scope (:handler-scope derived)
     :escaping-io-functions (:escaping-io-functions derived)
     :derived-contract-facts derived}))