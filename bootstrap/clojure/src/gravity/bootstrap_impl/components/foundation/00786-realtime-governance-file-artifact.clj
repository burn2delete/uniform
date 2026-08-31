

(defn realtime-governance-file-artifact
  [path]
  (realtime-governance-source-artifact path (slurp path)))

(defn numeric-mode-file-artifact
  [path]
  (numeric-mode-source-artifact path (slurp path)))