

(def capability-supply-chain-diagnostic-ids
  ["SAFE10-MISSING" "SAFE10-DENIED" "SAFE10-SCOPE"
   "SAFE10-PROVIDER" "SAFE10-AMBIENT" "SAFE10-PHASE"
   "SAFE10-SECRET-LEAK" "SAFE10-ATTENUATION"
   "SAFE10-REVOCATION" "SAFE10-RUNTIME"
   "SAFE14-MANIFEST" "SAFE14-BUILD-EFFECT"
   "SAFE14-RUNTIME-CAPABILITY" "SAFE14-LOCKFILE"
   "SAFE14-UNSAFE-SUMMARY" "SAFE14-NATIVE-DEP"
   "SAFE14-GENERATED" "SAFE14-SIGNATURE"
   "SAFE14-AUTHORITY-DIFF" "SAFE14-POSTINSTALL"])

(defn capability-supply-chain-source-artifact
  [source-path source-text]
  (let [typed-artifact (typed-source-artifact source-path source-text)
        safe10 (:safe10-conformance-fixture typed-artifact)
        safe14 (:safe14-conformance-fixture typed-artifact)
        conformance (:capability-supply-chain-conformance-fixture typed-artifact)]
    {:kind :gravity/stage0-capability-supply-chain-safety-artifact
     :pass {:name :capability-and-supply-chain-safety
            :input :typed-effected-core
            :output :capability-supply-chain-safety-report
            :requires [:reader :namespace-analyzer :macro-expansion
                       :core-lowering :type-effect-capability-check
                       :safety-outcome-classifier
                       :unsafe-island-extraction-and-audit
                       :ffi-concurrency-numeric-taint-safety]
            :preserves [:source-spans :generated-origin :profile :target
                        :types :effects :capabilities :safety-outcomes
                        :provider-selection-records
                        :unsafe-island-audit-records]
            :emits [:capability-requirement-records
                    :grant-intersection-records
                    :provider-selection-records
                    :scope-check-records
                    :attenuation-records
                    :revocation-records
                    :secret-redaction-records
                    :runtime-capability-check-records
                    :capability-usage-summary
                    :package-safety-manifest
                    :lockfile-records
                    :build-effect-summary
                    :runtime-capability-summary
                    :unsafe-island-summary
                    :native-dependency-records
                    :generated-artifact-provenance
                    :signature-attestation-records
                    :transitive-authority-diff
                    :supply-chain-conformance-report
                    :safety-certificate-inputs]
            :rejects capability-supply-chain-diagnostic-ids}
     :documents ["SAFE10" "SAFE14"]
     :module (:module typed-artifact)
     :typed-core-artifact-hash (str "sha256:" (sha256-hex (pr-str typed-artifact)))
     :capability-requirement-records (:safe10-capability-requirement-records typed-artifact)
     :grant-intersection-records (:safe10-grant-intersection-records typed-artifact)
     :capability-provider-selection-records (:safe10-provider-selection-records typed-artifact)
     :scope-check-records (:safe10-scope-check-records typed-artifact)
     :attenuation-records (:safe10-attenuation-records typed-artifact)
     :revocation-records (:safe10-revocation-records typed-artifact)
     :secret-redaction-records (:safe10-secret-redaction-records typed-artifact)
     :runtime-capability-check-records (:safe10-runtime-check-records typed-artifact)
     :capability-usage-summaries (:safe10-usage-summaries typed-artifact)
     :package-safety-manifests (:safe14-package-safety-manifests typed-artifact)
     :lockfile-records (:safe14-lockfile-records typed-artifact)
     :build-effect-summaries (:safe14-build-effect-summaries typed-artifact)
     :runtime-capability-summaries (:safe14-runtime-capability-summaries typed-artifact)
     :unsafe-island-summaries (:safe14-unsafe-summaries typed-artifact)
     :native-dependency-records (:safe14-native-dependency-records typed-artifact)
     :generated-artifact-provenance (:safe14-generated-artifact-provenance typed-artifact)
     :signature-attestation-records (:safe14-signature-attestation-records typed-artifact)
     :transitive-authority-diffs (:safe14-authority-diff-records typed-artifact)
     :package-build-policy-report {:build-effects (count (:safe14-build-effect-summaries typed-artifact))
                                   :lockfiles (count (:safe14-lockfile-records typed-artifact))
                                   :generated-artifacts (count (:safe14-generated-artifact-provenance typed-artifact))
                                   :signature-attestations (count (:safe14-signature-attestation-records typed-artifact))
                                   :authority-diffs (count (:safe14-authority-diff-records typed-artifact))
                                   :status (:status safe14)}
     :capability-policy-report {:requirements (count (:safe10-capability-requirement-records typed-artifact))
                                :grant-intersections (count (:safe10-grant-intersection-records typed-artifact))
                                :scope-checks (count (:safe10-scope-check-records typed-artifact))
                                :runtime-checks (count (:safe10-runtime-check-records typed-artifact))
                                :covered-capability-families (:covered-capability-families safe10)
                                :status (:status safe10)}
     :profile-safety-capability-report {:profile (get-in typed-artifact [:module :profile])
                                        :safety-mode (get-in typed-artifact [:module :safety])
                                        :effects (get-in typed-artifact [:namespace-effect-summary :inferred])
                                        :capabilities (set (map :capability (:provider-selection-records typed-artifact)))
                                        :profile-specific? true}
     :safety-certificate-inputs {:documents ["SAFE10" "SAFE14"]
                                 :conformance-status (:status conformance)
                                 :document-statuses (:document-statuses conformance)
                                 :required-families (:required-families conformance)
                                 :covered-families (:covered-families conformance)}
     :safe10-conformance-fixture safe10
	     :safe14-conformance-fixture safe14
	     :capability-supply-chain-conformance-fixture conformance
	     :diagnostics []}))