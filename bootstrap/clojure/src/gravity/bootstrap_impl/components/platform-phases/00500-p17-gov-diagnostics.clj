

(defn p17-gov-diagnostics
  [document]
  (let [number (Integer/parseInt (subs document 3))]
    (mapv #(format "GOV%d%03d" number %)
          (range 1 9))))

(def p17-governance-document-data
  [{:document "GOV1"
    :sequence 231
    :file "docs/phase-17-governance-and-evolution/231-gov1-language-evolution-process.md"
    :title "Language Evolution Process"
    :record :language-evolution
    :owned-surface :language-change-record
    :accepted-behavior :owned_scoped_change_record
    :rejected-diagnostic "GOV1001"
    :rejected-fixture "governance-gov1-owner.gravity"
    :check-key :change-owner-scope
    :artifact-key :language-change-record
    :dependencies #{"D0" "D1" "D3" "D6" "D8" "D9" "SAFE1"
                    "P1" "C1" "PKG1" "TEST1" "STD20"}}
   {:document "GOV2"
    :sequence 232
    :file "docs/phase-17-governance-and-evolution/232-gov2-compatibility-policy.md"
    :title "Compatibility Policy"
    :record :compatibility-policy
    :owned-surface :compatibility-report
    :accepted-behavior :baseline_scoped_compatibility_record
    :rejected-diagnostic "GOV2001"
    :rejected-fixture "governance-gov2-baseline.gravity"
    :check-key :baseline-compatibility
    :artifact-key :compatibility-report
    :dependencies #{"D6" "D8" "D9" "L1" "SAFE1" "P1" "C1"
                    "B1" "R1" "PKG1" "TEST1" "STD20" "GOV1"}}
   {:document "GOV3"
    :sequence 233
    :file "docs/phase-17-governance-and-evolution/233-gov3-standard-library-governance.md"
    :title "Standard Library Governance"
    :record :standard-library-governance
    :owned-surface :standard-library-governance-record
    :accepted-behavior :owned_standard_library_module_review
    :rejected-diagnostic "GOV3001"
    :rejected-fixture "governance-gov3-stdlib-owner.gravity"
    :check-key :stdlib-owner-stability-profile
    :artifact-key :standard-library-governance-record
    :dependencies #{"STD1" "STD20" "SAFE1" "SAFE6" "SAFE10" "P1"
                    "PKG1" "TEST7" "TEST13" "GOV1" "GOV2"}}
   {:document "GOV4"
    :sequence 234
    :file "docs/phase-17-governance-and-evolution/234-gov4-security-review-process.md"
    :title "Security Review Process"
    :record :security-review
    :owned-surface :security-review-record
    :accepted-behavior :security_review_with_threat_model_and_residual_risk
    :rejected-diagnostic "GOV4001"
    :rejected-fixture "governance-gov4-security-review.gravity"
    :check-key :security-review-record
    :artifact-key :security-review-record
    :dependencies #{"SAFE6" "SAFE7" "SAFE10" "SAFE11" "SAFE13"
                    "SAFE14" "SAFE15" "SAFE16" "A7" "A8" "A10"
                    "A11" "PKG6" "PKG8" "PKG10" "PKG12" "GOV1"}}
   {:document "GOV5"
    :sequence 235
    :file "docs/phase-17-governance-and-evolution/235-gov5-target-support-policy.md"
    :title "Target Support Policy"
    :record :target-support
    :owned-surface :target-support-matrix
    :accepted-behavior :target_tier_owner_profile_matrix
    :rejected-diagnostic "GOV5001"
    :rejected-fixture "governance-gov5-target-tier.gravity"
    :check-key :target-tier-owner-profile
    :artifact-key :target-support-matrix
    :dependencies #{"P1" "C1" "B1" "R1" "PKG3" "PKG7" "PKG10"
                    "PKG11" "PKG12" "TEST4" "TEST6" "TEST12"
                    "TEST13" "GOV1" "GOV2" "GOV4" "GOV8"}}
   {:document "GOV6"
    :sequence 236
    :file "docs/phase-17-governance-and-evolution/236-gov6-rfc-process.md"
    :title "RFC Process"
    :record :rfc-process
    :owned-surface :rfc-record
    :accepted-behavior :rfc_record_with_sections_reviews_and_traceability
    :rejected-diagnostic "GOV6001"
    :rejected-fixture "governance-gov6-rfc-owner.gravity"
    :check-key :rfc-owner-scope
    :artifact-key :rfc-record
    :dependencies #{"GOV1" "GOV2" "GOV4" "GOV7" "GOV8" "GOV9"
                    "GOV10" "D0" "D6" "D8" "D9" "TEST1"}}
   {:document "GOV7"
    :sequence 237
    :file "docs/phase-17-governance-and-evolution/237-gov7-experimental-feature-policy.md"
    :title "Experimental Feature Policy"
    :record :experiment-policy
    :owned-surface :experiment-registry
    :accepted-behavior :explicit_owned_opt_in_experiment_record
    :rejected-diagnostic "GOV7001"
    :rejected-fixture "governance-gov7-experiment-metadata.gravity"
    :check-key :experiment-metadata
    :artifact-key :experiment-registry
    :dependencies #{"GOV1" "GOV2" "GOV4" "GOV6" "GOV8" "GOV9"
                    "GOV10" "D6" "D8" "D9" "P1" "TEST1"}}
   {:document "GOV8"
    :sequence 238
    :file "docs/phase-17-governance-and-evolution/238-gov8-deprecation-and-stabilization-policy.md"
    :title "Deprecation and Stabilization Policy"
    :record :deprecation-stabilization
    :owned-surface :deprecation-plan
    :accepted-behavior :stabilization_and_deprecation_records
    :rejected-diagnostic "GOV8001"
    :rejected-fixture "governance-gov8-stabilization-evidence.gravity"
    :check-key :stabilization-evidence
    :artifact-key :deprecation-plan
    :dependencies #{"GOV1" "GOV2" "GOV4" "GOV7" "STD20" "D6"
                    "D8" "D9" "SAFE1" "P1" "TEST1" "PKG1"}}
   {:document "GOV9"
    :sequence 239
    :file "docs/phase-17-governance-and-evolution/239-gov9-unsafe-code-governance-policy.md"
    :title "Unsafe Code Governance Policy"
    :record :unsafe-governance
    :owned-surface :unsafe-governance-audit
    :accepted-behavior :unsafe_island_record_with_wrapper_evidence
    :rejected-diagnostic "GOV9001"
    :rejected-fixture "governance-gov9-unsafe-record.gravity"
    :check-key :unsafe-island-record
    :artifact-key :unsafe-governance-audit
    :dependencies #{"SAFE1" "SAFE2" "SAFE3" "SAFE4" "SAFE5" "SAFE6"
                    "SAFE7" "SAFE8" "SAFE10" "SAFE15" "SAFE16"
                    "P5" "P6" "P7" "P8" "P11" "C9" "C10" "C11"
                    "B1" "STD6" "STD7" "STD17" "STD18" "GOV3"
                    "GOV4" "GOV8" "GOV10"}}
   {:document "GOV10"
    :sequence 240
    :file "docs/phase-17-governance-and-evolution/240-gov10-ecosystem-package-governance-policy.md"
    :title "Ecosystem Package Governance Policy"
    :record :ecosystem-package-governance
    :owned-surface :ecosystem-package-governance-record
    :accepted-behavior :package_identity_provenance_capability_and_advisory_governance
    :rejected-diagnostic "GOV10001"
    :rejected-fixture "governance-gov10-package-identity.gravity"
    :check-key :package-identity
    :artifact-key :ecosystem-package-governance-record
    :dependencies #{"PKG1" "PKG12" "SAFE6" "SAFE7" "SAFE10"
                    "SAFE11" "SAFE13" "SAFE14" "SAFE15" "SAFE16"
                    "A1" "A11" "STD20" "GOV2" "GOV3" "GOV4"
                    "GOV9" "TEST1" "TEST4" "TEST7" "TEST13"}}])

(def p17-governance-documents
  (mapv :document p17-governance-document-data))

(def p17-governance-data-by-document
  (into {} (map (juxt :document identity)
                p17-governance-document-data)))

(def p17-governance-phase-governing-documents
  (into {} (map (juxt :document :file)
                p17-governance-document-data)))

(def p17-governance-diagnostics-by-document
  (into {} (map (fn [{:keys [document]}]
                  [document (p17-gov-diagnostics document)])
                p17-governance-document-data)))

(def p17-governance-rejected-diagnostics
  (into {} (map (juxt :document :rejected-diagnostic)
                p17-governance-document-data)))

(def p17-governance-rejected-fixture-names
  (into {} (map (juxt :document :rejected-fixture)
                p17-governance-document-data)))

(def p17-governance-diagnostic-ids
  (vec
   (distinct
    (concat (mapcat p17-governance-diagnostics-by-document
                    p17-governance-documents)
            ["P17-MANIFEST" "P17-ACCEPTED" "P17-REJECTED"
             "P17-GOVERNANCE"]))))

(def p17-governance-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             p17-governance-diagnostic-ids)))

(def p17-governance-artifact-keys
  [:language-change-record :compatibility-report
   :standard-library-governance-record :security-review-record
   :target-support-matrix :rfc-record :experiment-registry
   :deprecation-plan :unsafe-governance-audit
   :ecosystem-package-governance-record])

(defn p17-task-id
  [document]
  (str "P17-D" (:sequence (p17-governance-data-by-document document))))

(defn p17-governance-source-overrides
  [module]
  (get-in module [:metadata :governance :phase17] {}))

(defn p17-governance-diagnostic-document
  [diagnostic-id]
  (some (fn [document]
          (when (some #(= diagnostic-id %)
                      (p17-governance-diagnostics-by-document document))
            document))
        p17-governance-documents))

(defn p17-governance-fail!
  [id source-path subject extra]
  (let [document (or (:document-id subject)
                     (p17-governance-diagnostic-document id))]
    (fail! id
           "P17 governance/evolution validation failed"
           (merge {:source-span (or (:source-span subject)
                                    (source-span source-path 0))
                   :diagnostic-family :phase17-governance-evolution
                   :stage :governance-evolution
                   :document-id document
                   :task (when document (p17-task-id document))
                   :module (:module subject)
                   :artifact-id (:artifact-id subject)
                   :missing-fact (:missing-fact subject)
                   :fallback-status :rejected
                   :remediation "Phase 17 requires change records, compatibility reports, standard-library governance records, security reviews, target matrices, RFC records, experiment registries, deprecation plans, unsafe audits, ecosystem package governance records, and provenance before governance tasks can complete."}
                  extra))))