

(defn unsafe-audit-file-artifact
  [path]
  (unsafe-audit-source-artifact path (slurp path)))