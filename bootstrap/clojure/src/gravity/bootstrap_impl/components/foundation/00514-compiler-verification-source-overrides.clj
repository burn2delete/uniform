

(defn compiler-verification-source-overrides
  [module]
  (get-in module [:metadata :compiler :verification] {}))

(defn compiler-verification-fail!
  [id source-path subject extra]
  (fail! id
         (get compiler-verification-diagnostic-messages id
              "compiler diagnostics or verification validation failed")
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :compiler-verification
                 :stage (or (:stage subject) :compiler-verify)
                 :offending-diagnostic-id (:diagnostic-id subject)
                 :schema-field (:schema-field subject)
                 :artifact-id (:artifact-id subject)
                 :cache-key (:cache-key subject)
                 :plugin-id (:plugin-id subject)
                 :package-id (:package-id subject)
                 :compiler-api-version (:compiler-api-version subject)
                 :trust-level (:trust-level subject)
                 :pass-id (:pass-id subject)
                 :risk-class (:risk-class subject)
                 :required-evidence (:required-evidence subject)
                 :available-evidence (:available-evidence subject)
                 :affected-profiles (:affected-profiles subject)
                 :affected-targets (:affected-targets subject)
                 :remediation "Regenerate diagnostics, incremental records, plugin traces, and verification evidence with stable ids, provenance, redaction, policy, and release-gate proof."}
                extra)))

(defn compiler-verification-validate-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (let [[id subject-kind] (get compiler-verification-override-diagnostics
                                 fail-kind)]
      (when id
        (compiler-verification-fail!
         id source-path
         {:stage subject-kind
          :diagnostic-id (str "compiler-verification-invalid-"
                              (name fail-kind))
          :schema-field fail-kind
          :artifact-id (str "verification-artifact-" (name fail-kind))
          :cache-key (str "cache-key-" (name fail-kind))
          :plugin-id 'gravity.plugins.stage0/verifier
          :package-id 'gravity/compiler-verifier
          :compiler-api-version "stage0"
          :trust-level :sandboxed
          :pass-id subject-kind
          :risk-class :high
          :required-evidence #{:golden :translation-validation}
          :available-evidence #{}}
         {:missing-fields [fail-kind]})))))