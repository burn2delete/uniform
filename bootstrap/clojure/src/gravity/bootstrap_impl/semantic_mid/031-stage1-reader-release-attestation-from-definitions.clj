(let [definition-context semantic-mid-release-attestation-definition-context
      validate-structure! semantic-mid-validate-release-attestation-structure!
      validate-evidence! semantic-mid-validate-release-attestation-evidence!
      validate-links! semantic-mid-validate-release-attestation-links!]
  (defn stage1-reader-release-attestation-seed-retirement-from-definitions
    [reader-source-path definitions]
    (let [context (definition-context reader-source-path definitions)
          release-attestation (:release-attestation context)]
      (validate-structure! context)
      (validate-evidence! context)
      (validate-links! context)
      (assoc release-attestation
             :release-attestation-seed-retirement-id
             (str "sha256:"
                  (sha256-hex (pr-str release-attestation)))))))

(doseq [helper '[semantic-mid-release-attestation-definition-context
                 semantic-mid-validate-release-attestation-structure!
                 semantic-mid-validate-release-attestation-evidence!
                 semantic-mid-validate-release-attestation-links!]]
  (ns-unmap *ns* helper))
