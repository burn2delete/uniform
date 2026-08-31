

(defn typed-file-artifact
  [path]
  (typed-source-artifact path (slurp path)))

(defn safety-file-artifact
  [path]
  (safety-source-artifact path (slurp path)))

(defn memory-safety-file-artifact
  [path]
  (memory-safety-source-artifact path (slurp path)))