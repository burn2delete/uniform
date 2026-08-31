

(defn math-proof-file-artifact
  [path]
  (math-proof-source-artifact path (slurp path)))

(defn math-conformance-file-artifact
  [path]
  (math-conformance-source-artifact path (slurp path)))