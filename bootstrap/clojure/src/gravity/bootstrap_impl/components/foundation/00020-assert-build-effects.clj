

(defn assert-build-effects!
  [module macro call-span]
  (macro-expansion/assert-build-effects! module macro call-span
                                         (macro-expansion-ops)))

(defn collect-let-bindings
  [form]
  (macro-expansion/collect-let-bindings form (macro-expansion-ops)))

(defn assert-hygiene!
  [macro args output call-span]
  (macro-expansion/assert-hygiene! macro args output call-span
                                   (macro-expansion-ops)))