

(defn stage0-compiled-plan-package-report
  [plan module]
  (let [plan-id (:plan-id plan)
        project-manifest (p12-project-manifest)
        lockfile (p12-lockfile)
        build-graph (p12-build-graph)
        artifact-manifest (p12-artifact-manifest)
        package-manifest (p12-package-manifest)
        package-operation (p12-package-operation)
        resolution-report (p12-resolution-report)
        capability-manifest (p12-capability-manifest)
        reproducible-build-recipe (p12-reproducible-build-recipe)
        package-safety (p12-package-safety)
        registry-record (p12-registry-record)
        provenance-record (p12-provenance-record)
        target-matrix (p12-target-matrix)
        signing-sbom-verification (p12-signing-sbom-verification)
        conformance
        {:document-set p12-package-documents
         :task "P12-S1"
         :required-diagnostic-ids
         (mapv p12-package-rejected-diagnostics
               p12-package-documents)
         :package-gate-status :metadata-gate-only
         :project-manifest-status :complete
         :lockfile-status :complete
         :build-graph-status :complete
         :artifact-manifest-status :complete
         :package-operation-status :complete
         :resolution-status :complete
         :capability-manifest-status :complete
         :reproducible-build-status :complete
         :package-safety-status :complete
         :registry-status :complete
         :provenance-status :complete
         :target-matrix-status :complete
         :signing-sbom-verification-status :complete
         :status :complete}
        report-base
        {:kind :gravity/stage0-hosted-core-compiled-package-report
         :document-set ["D1" "PKG1-PKG12"]
         :compiled-plan-id plan-id
         :package-manifest
         {:artifact :gravity/stage0-hosted-core-compiled-package-manifest
          :package-id (:package-id project-manifest)
          :version (:version project-manifest)
          :profile (:profile module)
          :target (:target module)
          :project-manifest (:artifact project-manifest)
          :lockfile (:artifact lockfile)
          :build-graph (:artifact build-graph)
          :artifact-manifest (:artifact artifact-manifest)
          :package-manifest (:artifact package-manifest)
          :package-operation (:artifact package-operation)
          :resolution-report (:artifact resolution-report)
          :capability-manifest (:artifact capability-manifest)
          :reproducible-build-recipe (:artifact reproducible-build-recipe)
          :package-safety (:artifact package-safety)
          :registry-record (:artifact registry-record)
          :provenance-record (:artifact provenance-record)
          :target-matrix (:artifact target-matrix)
          :signing-sbom-verification
          (:artifact signing-sbom-verification)
          :accepted-fixtures
          ["bootstrap/clojure/fixtures/accepted/core-app.gravity"]
          :rejected-fixtures stage0-compiled-package-rejected-fixtures
          :conformance {:status :complete}
          :status :complete}
         :project-manifest-record
         (select-keys project-manifest
                      [:artifact :artifact-id :package-id :version
                       :edition :offline-parse :profiles :targets
                       :entrypoints :dependencies :effects :capabilities
                       :artifacts :policy :lockfile-complete :status])
         :lockfile-record
         (select-keys lockfile
                      [:artifact :artifact-id :complete :offline-proof
                       :lockfile-hash :records :status])
         :build-graph-record
         (select-keys build-graph
                      [:artifact :artifact-id :project-manifest-hash
                       :lockfile-hash :compiler-id :policy-hash
                       :declared-effects :network-policy
                       :generated-source-provenance :nodes
                       :release-evidence :status])
         :artifact-manifest-record
         (select-keys artifact-manifest
                      [:artifact :artifact-id :kind :schema-version
                       :package-id :source-graph-hash
                       :project-manifest-hash :lockfile-hash
                       :compiler-id :profile :target :content-hash
                       :dependency-graph-hash :capability-summary
                       :evidence :canonical :status])
         :package-operation-record
         (select-keys package-operation
                      [:artifact :artifact-id :operation :package-id
                       :version :registry :effects :download-verified
                       :registry-signature-verified :lockfile-diff
                       :capability-diff :safety-diff :provenance-diff
                       :machine-readable :credential-redaction :status])
         :resolution-record
         (select-keys resolution-report
                      [:artifact :artifact-id :canonical-input-hash
                       :deterministic :mode :selected-graph
                       :target-variants :capability-compatible
                       :private-registry-grants :revocation-status
                       :feature-selections :conflict-report
                       :offline-resolution-proof :status])
         :capability-manifest-record
         (select-keys capability-manifest
                      [:artifact :artifact-id :package-id :effects
                       :capabilities :derivation :dependency-diff
                       :runtime-handles :sbom-fields :audit-events
                       :status])
         :reproducible-build-record
         (select-keys reproducible-build-recipe
                      [:artifact :artifact-id :package-id :source-hash
                       :project-hash :lockfile-hash :compiler-id
                       :toolchain-id :environment :randomness
                       :generated-source-inputs :target-matrix
                       :build-graph-hash :expected-artifacts
                       :output-hashes :rebuild-verification
                       :non-reproducible-exceptions :status])
         :package-safety-record
         (select-keys package-safety
                      [:artifact :artifact-id :package-id
                       :unsafe-islands :unsafe-audit-metadata
                       :safe-wrappers :ffi-boundaries
                       :privileged-effects :capabilities
                       :taint-sinks :proof-claims :review-state
                       :vulnerability-state :schema-validated :status])
         :registry-record
         (select-keys registry-record
                      [:artifact :artifact-id :registry-id
                       :access-grant :private-metadata-redacted
                       :index-signature :latent-package-states
                       :generated-package-provenance
                       :publish-review-state :mirror-attestation
                       :lockfile-registry-source :status])
         :provenance-record
         (select-keys provenance-record
                      [:artifact :artifact-id :subject :content-hash
                       :source-graph-hash :project-manifest-hash
                       :lockfile-hash :builder-id :compiler-id
                       :build-recipe-hash :dependency-graph-hash
                       :generated-source-ledger :binary-blob-ledger
                       :evidence :signature :sbom :keyless-signing
                       :revocation-status :status])
         :target-matrix-record
         (select-keys target-matrix
                      [:artifact :artifact-id :implicit-host-target?
                       :entries :fallback-targets
                       :release-support-table :status])
         :signing-sbom-verification-record
         (select-keys signing-sbom-verification
                      [:artifact :artifact-id :signature :sbom
                       :attestation :keyless :transparency-log
                       :root-metadata :verification-report
                       :consumer-decision :status])
         :package-conformance-results conformance
         :diagnostics []}]
    (assoc report-base
           :report-id (str "sha256:" (sha256-hex (pr-str report-base))))))