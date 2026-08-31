(let [artifact-context semantic-mid-stage1-artifact-context
      artifact-base* semantic-mid-diverse-bootstrap-artifact-base
      validate! semantic-mid-validate-diverse-bootstrap!]
  (defn stage1-reader-diverse-bootstrap-verification-source-artifact
    [source-path source-text]
    (let [context
          (artifact-context
           source-path source-text
           stage1-reader-execute-diverse-bootstrap-verification-pipeline)
          artifact-base (artifact-base* context)]
      (validate! context artifact-base)
      (let [capability-proof
            (stage1-reader-diverse-bootstrap-verification-proof artifact-base)]
        (assoc artifact-base
               :capability-based-proof capability-proof
               :artifact-id
               (c4-artifact-id
                (assoc artifact-base
                       :capability-based-proof capability-proof)))))))

(doseq [helper '[semantic-mid-diverse-bootstrap-artifact-base
                 semantic-mid-validate-diverse-bootstrap!]]
  (ns-unmap *ns* helper))
