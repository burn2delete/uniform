

(defn macro-env-value
  [env sym]
  (macro-expansion/macro-env-value env sym (macro-expansion-ops)))