(let [proof-context semantic-mid-formal-governance-proof-context
      proof-evidence semantic-mid-formal-governance-proof-evidence
      proof-boundaries semantic-mid-formal-governance-proof-boundaries]
  (defn stage1-reader-formal-release-governance-seed-retirement-proof
    [artifact]
    (let [context (proof-context artifact)]
      (merge (proof-evidence context)
             (proof-boundaries context)))))

(doseq [helper '[semantic-mid-formal-governance-proof-context
                 semantic-mid-formal-governance-proof-evidence
                 semantic-mid-formal-governance-proof-boundaries]]
  (ns-unmap *ns* helper))
