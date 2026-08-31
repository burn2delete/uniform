

(defn approximation-file-artifact
  [path]
  (approximation-source-artifact path (slurp path)))