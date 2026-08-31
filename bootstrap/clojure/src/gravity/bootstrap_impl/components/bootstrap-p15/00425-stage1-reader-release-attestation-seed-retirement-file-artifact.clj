

(defn stage1-reader-release-attestation-seed-retirement-file-artifact
  [path]
  (stage1-reader-release-attestation-seed-retirement-source-artifact
   path
   (slurp path)))

(def stage1-reader-formal-release-governance-seed-retirement-required-operations
  [:verify-formal-release-governance
   :verify-deployment-custody
   :verify-self-hosting-evidence
   :verify-reproducible-rebuild
   :verify-stage-comparison
   :record-tcb-delta
   :record-unsafe-audit
   :record-formal-release-provenance])

(def stage1-reader-formal-release-governance-seed-retirement-required-stages
  [:stage1-formal-governance-verify
   :stage1-formal-governance-deployment-custody
   :stage1-formal-governance-self-hosting-evidence
   :stage1-formal-governance-reproducible-rebuild
   :stage1-formal-governance-stage-comparison
   :stage1-formal-governance-tcb-delta
   :stage1-formal-governance-unsafe-audit
   :stage1-formal-governance-record-provenance])

(defn stage1-reader-formal-release-governance-seed-retirement-literal-definition-value
  [reader-source-path definitions symbol-name]
  (let [definition (get definitions symbol-name)]
    (when-not (= :def (:kind definition))
      (stage1-reader-formal-release-governance-seed-retirement-fail!
       "STAGE1GOV003" reader-source-path symbol-name
       {:missing-fields [symbol-name]}))
    (:value-form definition)))