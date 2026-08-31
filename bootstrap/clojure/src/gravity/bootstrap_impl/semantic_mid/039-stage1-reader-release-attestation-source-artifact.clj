(let [artifact-context semantic-mid-stage1-artifact-context
      artifact-identity semantic-mid-release-source-artifact-identity
      artifact-evidence semantic-mid-release-source-artifact-evidence
      validate! semantic-mid-validate-release-source-artifact!]
  (defn stage1-reader-release-attestation-seed-retirement-source-artifact
    [source-path source-text]
    (let [context
          (artifact-context
           source-path source-text
           stage1-reader-execute-release-attestation-seed-retirement-pipeline)
          artifact-base
          (merge (artifact-identity context) (artifact-evidence context))]
      (validate! context artifact-base)
      (let [capability-proof
            (stage1-reader-release-attestation-seed-retirement-proof
             artifact-base)]
        (assoc artifact-base
               :capability-based-proof capability-proof
               :artifact-id
               (c4-artifact-id
                (assoc artifact-base
                       :capability-based-proof capability-proof)))))))

(doseq [helper '[semantic-mid-release-source-artifact-identity
                 semantic-mid-release-source-artifact-evidence
                 semantic-mid-release-source-artifact-base
                 semantic-mid-validate-release-source-artifact!]]
  (ns-unmap *ns* helper))
