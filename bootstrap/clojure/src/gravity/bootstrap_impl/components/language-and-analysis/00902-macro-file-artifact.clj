

(defn macro-file-artifact
  [path]
  (macro-source-artifact path (slurp path)))