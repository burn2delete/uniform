

(defn expansion-generated-origin
  [macro syntax input output]
  (macro-expansion/expansion-generated-origin macro syntax input output
                                              (macro-expansion-ops)))