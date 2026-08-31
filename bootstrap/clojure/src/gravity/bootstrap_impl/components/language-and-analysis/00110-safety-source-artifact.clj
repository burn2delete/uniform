

(defn safety-source-artifact
  [source-path source-text]
  (let [typed-artifact (typed-source-artifact source-path source-text)
        classifications (:safe1-safety-classification-records typed-artifact)
        runtime-checks (:safe1-runtime-check-records typed-artifact)
        unsafe-islands (:safe1-unsafe-island-audit-records typed-artifact)
        provenance (:safe1-generated-code-safety-provenance typed-artifact)
        optimizations (:safe1-optimization-check-erasure-justifications typed-artifact)
        dependencies (:safe1-dependency-safety-mode-records typed-artifact)
        conformance (:safe1-conformance-fixture typed-artifact)]
    {:kind :gravity/stage0-safety-artifact
     :pass {:name :safety-outcome-classifier
            :input :typed-effected-core
            :output :safety-analysis-report
            :requires [:reader :namespace-analyzer :macro-expansion
                       :core-lowering :type-effect-capability-check]
            :preserves [:source-spans :generated-origin :profile :types
                        :effects :capabilities]
            :emits [:safety-classification-records
                    :runtime-check-manifest
                    :unsafe-island-audit-records
                    :profile-safety-capability-report
                    :generated-code-safety-provenance
                    :optimization-check-erasure-justifications
                    :safety-certificate-inputs]
            :rejects ["SAFE1-NO-OUTCOME"
                      "SAFE1-PROOF-MISSING"
                      "SAFE1-CHECK-MISSING"
                      "SAFE1-CHECK-ILLEGAL"
                      "SAFE1-UNSAFE-POLICY"
                      "SAFE1-UNSAFE-METADATA"
                      "SAFE1-GENERATED-PROVENANCE"
                      "SAFE1-OPTIMIZATION-PROOF"
                      "SAFE1-DEPENDENCY-MODE"]}
     :document "SAFE1"
     :module (:module typed-artifact)
     :typed-core-artifact-hash (str "sha256:" (sha256-hex (pr-str typed-artifact)))
     :safety-classification-records classifications
     :runtime-check-manifest runtime-checks
     :unsafe-island-audit-records unsafe-islands
     :profile-safety-capability-report {:profile (get-in typed-artifact [:module :profile])
                                        :safety-mode (get-in typed-artifact [:module :safety])
                                        :effects (get-in typed-artifact [:namespace-effect-summary :inferred])
                                        :capabilities (set (map :capability (:provider-selection-records typed-artifact)))
                                        :profile-specific? true}
     :generated-code-safety-provenance provenance
     :optimization-check-erasure-justifications optimizations
     :dependency-safety-mode-records dependencies
     :safety-certificate-inputs {:classifications (count classifications)
                                 :runtime-checks (count runtime-checks)
                                 :unsafe-islands (count unsafe-islands)
                                 :proof-references (vec (keep :proof-reference classifications))
                                 :conformance-status (:status conformance)}
     :safe1-conformance-fixture conformance
     :diagnostics []}))