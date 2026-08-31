(let [core-artifacts semantic-mid-p13-tooling-core-artifacts
      extended-artifacts semantic-mid-p13-tooling-extended-artifacts]
  (defn p13-tooling-artifact-values
    []
    (merge (core-artifacts) (extended-artifacts))))

(doseq [helper '[semantic-mid-p13-tooling-core-artifacts
                 semantic-mid-p13-tooling-extended-artifacts]]
  (ns-unmap *ns* helper))
