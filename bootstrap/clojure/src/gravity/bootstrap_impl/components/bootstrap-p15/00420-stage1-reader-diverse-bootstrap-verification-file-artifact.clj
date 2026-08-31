

(defn stage1-reader-diverse-bootstrap-verification-file-artifact
  [path]
  (stage1-reader-diverse-bootstrap-verification-source-artifact
   path
   (slurp path)))

(def stage1-reader-release-attestation-seed-retirement-required-operations
  [:verify-release-attestation
   :verify-seed-retirement-evidence
   :verify-supply-chain-manifest
   :verify-release-custody-reproducibility
   :verify-governance-approval
   :verify-revocation-status
   :record-release-provenance])

(defn stage1-reader-release-attestation-seed-retirement-literal-definition-value
  [reader-source-path definitions symbol-name]
  (let [definition (get definitions symbol-name)]
    (when-not (= :def (:kind definition))
      (stage1-reader-release-attestation-seed-retirement-fail!
       "STAGE1REL003" reader-source-path symbol-name
       {:missing-fields [symbol-name]}))
    (:value-form definition)))