

(defn expand-macro-form
  [module macro args call-span]
  (macro-expansion/expand-macro-form module macro args call-span
                                     (macro-expansion-ops)))