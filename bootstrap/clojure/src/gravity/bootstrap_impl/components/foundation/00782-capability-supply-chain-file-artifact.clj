

(defn capability-supply-chain-file-artifact
  [path]
  (capability-supply-chain-source-artifact path (slurp path)))