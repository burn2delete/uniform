(let [artifact-context semantic-mid-stage1-artifact-context
      artifact-base* semantic-mid-verified-boot-chain-artifact-base
      validate! semantic-mid-validate-verified-boot-chain!]
  (defn stage1-reader-verified-boot-chain-source-artifact
    [source-path source-text]
    (let [context
          (artifact-context
           source-path source-text
           stage1-reader-execute-verified-boot-chain-pipeline)
          artifact-base (artifact-base* context)]
      (validate! context artifact-base)
      (let [capability-proof
            (stage1-reader-verified-boot-chain-proof artifact-base)]
        (assoc artifact-base
               :capability-based-proof capability-proof
               :artifact-id
               (c4-artifact-id
                (assoc artifact-base
                       :capability-based-proof capability-proof)))))))

(doseq [helper '[semantic-mid-verified-boot-chain-artifact-base
                 semantic-mid-validate-verified-boot-chain!]]
  (ns-unmap *ns* helper))
