

(defn zero-cost-file-artifact
  [path]
  (zero-cost-source-artifact path (slurp path)))

(defn specialization-file-artifact
  [path]
  (specialization-source-artifact path (slurp path)))

(defn layout-file-artifact
  [path]
  (layout-source-artifact path (slurp path)))