

(defn expand-template-items
  [env items]
  (macro-expansion/expand-template-items env items (macro-expansion-ops)))