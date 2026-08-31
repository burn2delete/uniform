(let [definition-context semantic-mid-formal-governance-definition-context
      validate-structure! semantic-mid-validate-formal-governance-structure!
      validate-evidence! semantic-mid-validate-formal-governance-evidence!
      validate-links! semantic-mid-validate-formal-governance-links!]
  (defn stage1-reader-formal-release-governance-seed-retirement-from-definitions
    [reader-source-path definitions]
    (let [context (definition-context reader-source-path definitions)
          formal-governance (:formal-governance context)]
      (validate-structure! context)
      (validate-evidence! context)
      (validate-links! context)
      (assoc formal-governance
             :formal-release-governance-seed-retirement-id
             (str "sha256:" (sha256-hex (pr-str formal-governance)))))))

(doseq [helper '[semantic-mid-formal-governance-definition-context
                 semantic-mid-validate-formal-governance-structure!
                 semantic-mid-validate-formal-governance-evidence!
                 semantic-mid-validate-formal-governance-links!]]
  (ns-unmap *ns* helper))
