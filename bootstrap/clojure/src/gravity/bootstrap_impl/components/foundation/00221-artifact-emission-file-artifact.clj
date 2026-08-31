

(defn artifact-emission-file-artifact
  [path]
  (artifact-emission-source-artifact path (slurp path)))