

(defn performance-governance-file-artifact
  [path]
  (performance-governance-source-artifact path (slurp path)))