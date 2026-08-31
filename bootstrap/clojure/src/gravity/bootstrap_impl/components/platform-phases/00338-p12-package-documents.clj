

(def p12-package-documents
  (mapv #(str "PKG" %) (range 1 13)))

(def p12-package-governing-documents
  {"PKG1" "docs/phase-12-build-package-and-artifact-system/165-pkg1-project-file-specification.md"
   "PKG2" "docs/phase-12-build-package-and-artifact-system/166-pkg2-build-system-architecture.md"
   "PKG3" "docs/phase-12-build-package-and-artifact-system/167-pkg3-artifact-model-specification.md"
   "PKG4" "docs/phase-12-build-package-and-artifact-system/168-pkg4-package-manager-specification.md"
   "PKG5" "docs/phase-12-build-package-and-artifact-system/169-pkg5-dependency-resolution-specification.md"
   "PKG6" "docs/phase-12-build-package-and-artifact-system/170-pkg6-capability-and-permission-manifest-specification.md"
   "PKG7" "docs/phase-12-build-package-and-artifact-system/171-pkg7-reproducible-build-specification.md"
   "PKG8" "docs/phase-12-build-package-and-artifact-system/172-pkg8-package-safety-and-audit-metadata-specification.md"
   "PKG9" "docs/phase-12-build-package-and-artifact-system/173-pkg9-private-registry-and-latent-package-space-design.md"
   "PKG10" "docs/phase-12-build-package-and-artifact-system/174-pkg10-supply-chain-security-and-provenance-specification.md"
   "PKG11" "docs/phase-12-build-package-and-artifact-system/175-pkg11-cross-compilation-and-target-matrix-specification.md"
   "PKG12" "docs/phase-12-build-package-and-artifact-system/176-pkg12-artifact-signing-verification-and-sbom-specification.md"})

(def p12-package-phase-governing-documents
  (vec (concat ["docs/phase-12-build-package-and-artifact-system/IMPLEMENTATION-ROADMAP.md"
                "docs/phase-12-build-package-and-artifact-system/README.md"
                "docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md"
                "docs/phase-03-profile-system/IMPLEMENTATION-ROADMAP.md"
                "docs/phase-03-profile-system/046-p1-profile-system-specification.md"
                "docs/phase-10-schema-data-and-interop/IMPLEMENTATION-ROADMAP.md"
                "docs/phase-10-schema-data-and-interop/153-s9-artifact-schema-specification.md"
                "docs/phase-02-safety/043-safe14-supply-chain-safety.md"]
               (map p12-package-governing-documents
                    p12-package-documents))))

(def p12-package-contracts
  {"PKG1" {:readable ["PKG1001" :offline_readable_project]
           :profile ["PKG1002" :known_profile_target_pair]
           :entrypoint ["PKG1003" :resolved_entrypoint]
           :dependency ["PKG1004" :declared_registry_source]
           :capability ["PKG1005" :declared_effect_capability]
           :lockfile ["PKG1006" :release_lockfile]
           :unsafe ["PKG1007" :unsafe_policy]
           :artifact ["PKG1008" :artifact_plan]}
   "PKG2" {:effect ["PKG2001" :declared_build_effect]
           :cache ["PKG2002" :cache_key_valid]
           :generated ["PKG2003" :generated_source_provenance]
           :cycle ["PKG2004" :build_graph_cycle]
           :target ["PKG2005" :target_matrix_result]
           :evidence ["PKG2006" :release_evidence]
           :network ["PKG2007" :network_policy]
           :plugin ["PKG2008" :build_plugin_authority]}
   "PKG3" {:manifest ["PKG3001" :artifact_manifest]
           :schema ["PKG3002" :schema_version]
           :identity ["PKG3003" :artifact_identity]
           :content ["PKG3004" :content_hash]
           :evidence ["PKG3005" :evidence_link]
           :kind ["PKG3006" :artifact_kind]
           :canonical ["PKG3007" :canonical_signed_data]
           :consumer ["PKG3008" :verified_consumer_use]}
   "PKG4" {:download ["PKG4001" :download_verification]
           :lockfile ["PKG4002" :lockfile_metadata]
           :capability ["PKG4003" :capability_review]
           :publish ["PKG4004" :publish_policy]
           :plugin ["PKG4005" :plugin_effect]
           :offline ["PKG4006" :offline_cache]
           :credential ["PKG4007" :credential_redaction]
           :yank ["PKG4008" :yank_metadata]}
   "PKG5" {:version ["PKG5001" :version_constraint]
           :capability ["PKG5002" :capability_compatible_dependency]
           :target ["PKG5003" :target_variant]
           :lockfile ["PKG5004" :complete_lockfile]
           :private ["PKG5005" :private_registry_grant]
           :revoked ["PKG5006" :revocation_policy]
           :determinism ["PKG5007" :deterministic_solver_input]
           :feature ["PKG5008" :feature_conflict]}
   "PKG6" {:summary ["PKG6001" :capability_summary]
           :derivation ["PKG6002" :effect_capability_derivation]
           :expansion ["PKG6003" :capability_expansion_diff]
           :denial ["PKG6004" :denied_authority]
           :ambient ["PKG6005" :ambient_authority]
           :grant ["PKG6006" :deployment_grant_scope]
           :sbom ["PKG6007" :sbom_capability_fields]
           :mismatch ["PKG6008" :source_manifest_match]}
   "PKG7" {:recipe ["PKG7001" :build_recipe]
           :lockfile ["PKG7002" :locked_dependency]
           :network ["PKG7003" :controlled_network_input]
           :random ["PKG7004" :seeded_randomness]
           :host ["PKG7005" :host_path_normalization]
           :generated ["PKG7006" :generated_source_input_hash]
           :hash ["PKG7007" :rebuild_hash_match]
           :claim ["PKG7008" :reproducible_claim]}
   "PKG8" {:unsafe ["PKG8001" :unsafe_audit_metadata]
           :wrapper ["PKG8002" :safe_wrapper_evidence]
           :ffi ["PKG8003" :ffi_assumptions]
           :effect ["PKG8004" :privileged_effect]
           :taint ["PKG8005" :taint_sink_record]
           :proof ["PKG8006" :proof_revocation]
           :diff ["PKG8007" :safety_diff_review]
           :quarantine ["PKG8008" :quarantined_dependency]}
   "PKG9" {:access ["PKG9001" :private_registry_grant]
           :leak ["PKG9002" :private_metadata_redaction]
           :signature ["PKG9003" :registry_signature]
           :latent ["PKG9004" :latent_package_grant]
           :generated ["PKG9005" :generated_package_provenance]
           :review ["PKG9006" :latent_review]
           :mirror ["PKG9007" :mirror_attestation]
           :lockfile ["PKG9008" :lockfile_registry_source]}
   "PKG10" {:provenance ["PKG10001" :release_provenance]
            :dependency ["PKG10002" :dependency_provenance]
            :builder ["PKG10003" :trusted_builder]
            :generated ["PKG10004" :generated_source_ledger]
            :blob ["PKG10005" :binary_blob_policy]
            :revoked ["PKG10006" :revoked_supply_chain_input]
            :link ["PKG10007" :provenance_artifact_link]
            :schema ["PKG10008" :provenance_schema]
            :keyless ["PKG10009" :keyless_signing_provenance]
            :transparency ["PKG10010" :transparency_log_evidence]}
   "PKG11" {:pair ["PKG11001" :profile_target_pair]
            :host ["PKG11002" :implicit_host_target]
            :variant ["PKG11003" :dependency_variant]
            :capability ["PKG11004" :per_target_capability]
            :identity ["PKG11005" :target_identity]
            :conformance ["PKG11006" :per_target_conformance]
            :fallback ["PKG11007" :fallback_policy]
            :contradiction ["PKG11008" :project_matrix_consistency]}
   "PKG12" {:signature ["PKG12001" :signature_required]
            :canonical ["PKG12002" :canonical_signature_payload]
            :content ["PKG12003" :content_hash_match]
            :dependency ["PKG12004" :sbom_transitive_dependency]
            :safety ["PKG12005" :sbom_safety_capability]
            :revoked ["PKG12006" :revoked_signing_material]
            :consumer ["PKG12007" :verified_consumer_use]
            :report ["PKG12008" :verification_report]
            :attestation ["PKG12009" :provenance_attestation]
            :binding ["PKG12010" :attestation_artifact_binding]
            :builder ["PKG12011" :trusted_builder_identity]
            :source ["PKG12012" :attestation_source_material]
            :claim ["PKG12013" :attestation_claim_evidence]
            :transparency ["PKG12014" :transparency_timestamp]
            :freshness ["PKG12015" :attestation_freshness]
            :level ["PKG12016" :attestation_policy_level]
            :keyless ["PKG12017" :keyless_identity_evidence]
            :oidc ["PKG12018" :oidc_identity_policy]
            :root ["PKG12019" :trusted_root_metadata]
            :log ["PKG12020" :transparency_log_policy]}})

(def p12-package-rejected-diagnostics
  {"PKG1" "PKG1006"
   "PKG2" "PKG2001"
   "PKG3" "PKG3005"
   "PKG4" "PKG4001"
   "PKG5" "PKG5002"
   "PKG6" "PKG6004"
   "PKG7" "PKG7003"
   "PKG8" "PKG8001"
   "PKG9" "PKG9001"
   "PKG10" "PKG10001"
   "PKG11" "PKG11002"
   "PKG12" "PKG12002"})

(def p12-package-rejected-fixture-names
  {"PKG1" "package-pkg1-release-lockfile.gravity"
   "PKG2" "package-pkg2-undeclared-effect.gravity"
   "PKG3" "package-pkg3-evidence-link.gravity"
   "PKG4" "package-pkg4-download-verification.gravity"
   "PKG5" "package-pkg5-capability-incompatible.gravity"
   "PKG6" "package-pkg6-denied-authority.gravity"
   "PKG7" "package-pkg7-uncontrolled-network.gravity"
   "PKG8" "package-pkg8-unsafe-audit.gravity"
   "PKG9" "package-pkg9-private-registry.gravity"
   "PKG10" "package-pkg10-provenance.gravity"
   "PKG11" "package-pkg11-implicit-host-target.gravity"
   "PKG12" "package-pkg12-noncanonical-signature.gravity"})

(def p12-package-diagnostic-ids
  (vec
   (distinct
    (concat
     (mapcat
      (fn [document]
        (map (comp first val)
             (sort-by (comp name key)
                      (p12-package-contracts document))))
      p12-package-documents)
     ["P12-MANIFEST" "P12-ACCEPTED" "P12-REJECTED"
      "P12-CONFORMANCE"]))))

(def p12-package-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             p12-package-diagnostic-ids)))

(def p12-package-artifact-keys
  [:project-manifest :lockfile :build-graph :artifact-manifest
   :package-manifest :package-operation :resolution-report
   :capability-manifest :reproducible-build-recipe :package-safety
   :registry-record :provenance-record :target-matrix
   :signing-sbom-verification])