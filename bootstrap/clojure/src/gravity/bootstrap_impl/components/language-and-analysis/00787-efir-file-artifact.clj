

(defn efir-file-artifact
  [path]
  (efir-source-artifact path (slurp path)))

(defn eml-file-artifact
  [path]
  (eml-source-artifact path (slurp path)))