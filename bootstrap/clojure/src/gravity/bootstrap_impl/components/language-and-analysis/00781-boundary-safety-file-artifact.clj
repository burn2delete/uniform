

(defn boundary-safety-file-artifact
  [path]
  (boundary-safety-source-artifact path (slurp path)))