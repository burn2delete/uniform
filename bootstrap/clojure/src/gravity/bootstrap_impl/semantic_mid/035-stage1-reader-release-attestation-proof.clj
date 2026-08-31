(let [proof-context semantic-mid-release-attestation-proof-context
      proof-evidence semantic-mid-release-attestation-proof-evidence
      proof-boundaries semantic-mid-release-attestation-proof-boundaries]
  (defn stage1-reader-release-attestation-seed-retirement-proof
    [artifact]
    (let [context (proof-context artifact)]
      (merge (proof-evidence context)
             (proof-boundaries context)))))

(doseq [helper '[semantic-mid-release-attestation-proof-context
                 semantic-mid-release-attestation-proof-evidence
                 semantic-mid-release-attestation-proof-boundaries]]
  (ns-unmap *ns* helper))
