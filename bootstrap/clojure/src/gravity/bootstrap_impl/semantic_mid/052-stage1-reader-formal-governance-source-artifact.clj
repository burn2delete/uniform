(let [artifact-context semantic-mid-stage1-artifact-context
      artifact-identity semantic-mid-formal-source-artifact-identity
      artifact-evidence semantic-mid-formal-source-artifact-evidence
      validate! semantic-mid-validate-formal-source-artifact!]
  (defn stage1-reader-formal-release-governance-seed-retirement-source-artifact
    [source-path source-text]
    (let [context
          (artifact-context
           source-path source-text
           stage1-reader-execute-formal-release-governance-seed-retirement-pipeline)
          artifact-base
          (merge (artifact-identity context) (artifact-evidence context))]
      (validate! context artifact-base)
      (let [capability-proof
            (stage1-reader-formal-release-governance-seed-retirement-proof
             artifact-base)]
        (assoc artifact-base
               :capability-based-proof capability-proof
               :artifact-id
               (c4-artifact-id
                (assoc artifact-base
                       :capability-based-proof capability-proof)))))))

(doseq [helper '[semantic-mid-stage1-artifact-context
                 semantic-mid-formal-source-artifact-identity
                 semantic-mid-formal-source-artifact-evidence
                 semantic-mid-formal-source-artifact-base
                 semantic-mid-validate-formal-source-artifact!]]
  (ns-unmap *ns* helper))
