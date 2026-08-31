

(defn bind-macro-arguments
  [macro args call-span]
  (macro-expansion/bind-macro-arguments macro args call-span
                                        (macro-expansion-ops)))