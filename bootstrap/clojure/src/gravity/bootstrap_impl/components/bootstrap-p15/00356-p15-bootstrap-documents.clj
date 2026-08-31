

(def p15-bootstrap-documents
  ["BOOT1" "BOOT2" "BOOT3" "BOOT4" "BOOT5" "BOOT6" "BOOT7" "BOOT8"])

(def p15-bootstrap-phase-governing-documents
  {"BOOT1" "docs/phase-15-bootstrap-and-self-hosting/203-boot1-bootstrap-strategy.md"
   "BOOT2" "docs/phase-15-bootstrap-and-self-hosting/204-boot2-seed-compiler-design.md"
   "BOOT3" "docs/phase-15-bootstrap-and-self-hosting/205-boot3-self-hosted-compiler-plan.md"
   "BOOT4" "docs/phase-15-bootstrap-and-self-hosting/206-boot4-compiler-in-gravity-coding-standard.md"
   "BOOT5" "docs/phase-15-bootstrap-and-self-hosting/207-boot5-stage-compatibility-matrix.md"
   "BOOT6" "docs/phase-15-bootstrap-and-self-hosting/208-boot6-trusting-trust-and-reproducible-bootstrap-plan.md"
   "BOOT7" "docs/phase-15-bootstrap-and-self-hosting/209-boot7-self-hosting-validation-and-equivalence-plan.md"
   "BOOT8" "docs/phase-15-bootstrap-and-self-hosting/210-boot8-bootstrap-artifact-provenance-specification.md"})

(def p15-bootstrap-diagnostics-by-document
  {"BOOT1" ["BOOT1001" "BOOT1002" "BOOT1003" "BOOT1004"
            "BOOT1005" "BOOT1006"]
   "BOOT2" ["BOOT2001" "BOOT2002" "BOOT2003" "BOOT2004"
            "BOOT2005" "BOOT2006"]
   "BOOT3" ["BOOT3001" "BOOT3002" "BOOT3003" "BOOT3004"
            "BOOT3005" "BOOT3006"]
   "BOOT4" ["BOOT4001" "BOOT4002" "BOOT4003" "BOOT4004"
            "BOOT4005" "BOOT4006" "BOOT4007"]
   "BOOT5" ["BOOT5001" "BOOT5002" "BOOT5003" "BOOT5004"
            "BOOT5005" "BOOT5006"]
   "BOOT6" ["BOOT6001" "BOOT6002" "BOOT6003" "BOOT6004"
            "BOOT6005" "BOOT6006"]
   "BOOT7" ["BOOT7001" "BOOT7002" "BOOT7003" "BOOT7004"
            "BOOT7005" "BOOT7006" "BOOT7007"]
   "BOOT8" ["BOOT8001" "BOOT8002" "BOOT8003" "BOOT8004"
            "BOOT8005" "BOOT8006" "BOOT8007"]})

(def p15-bootstrap-rejected-diagnostics
  {"BOOT1" "BOOT1001"
   "BOOT2" "BOOT2002"
   "BOOT3" "BOOT3002"
   "BOOT4" "BOOT4003"
   "BOOT5" "BOOT5003"
   "BOOT6" "BOOT6001"
   "BOOT7" "BOOT7001"
   "BOOT8" "BOOT8002"})

(def p15-bootstrap-rejected-fixture-names
  {"BOOT1" "bootstrap-boot1-stage-evidence.gravity"
   "BOOT2" "bootstrap-boot2-profile.gravity"
   "BOOT3" "bootstrap-boot3-ambient-authority.gravity"
   "BOOT4" "bootstrap-boot4-preserved-fact.gravity"
   "BOOT5" "bootstrap-boot5-conformance-link.gravity"
   "BOOT6" "bootstrap-boot6-environment.gravity"
   "BOOT7" "bootstrap-boot7-compiler-identity.gravity"
   "BOOT8" "bootstrap-boot8-lineage.gravity"})

(def p15-bootstrap-diagnostic-ids
  (vec
   (distinct
    (concat (mapcat p15-bootstrap-diagnostics-by-document
                    p15-bootstrap-documents)
            ["P15-MANIFEST" "P15-ACCEPTED" "P15-REJECTED"
             "P15-BOOTSTRAP"]))))

(def p15-bootstrap-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             p15-bootstrap-diagnostic-ids)))

(def p15-bootstrap-artifact-keys
  [:bootstrap-stage-matrix :seed-compiler-manifest
   :self-hosted-component-manifest :compiler-coding-standard-report
   :stage-compatibility-matrix :trusting-trust-report
   :equivalence-report :bootstrap-provenance-record])

(def p15-bootstrap-document-summaries
  {"BOOT1" {:title "Bootstrap Strategy"
            :owned-surface :bootstrap-stage-matrix
            :accepted-behavior :stage_manifest_with_trust_reduction
            :rejected-behavior "BOOT1001"
            :artifact-keys [:bootstrap-stage-matrix
                            :seed-compiler-manifest
                            :bootstrap-provenance-record]
            :dependencies #{"C1" "C18" "PKG7" "PKG10" "PKG12"
                            "TEST13" "BOOT2" "BOOT8"}}
   "BOOT2" {:title "Seed Compiler Design"
            :owned-surface :seed-compiler-manifest
            :accepted-behavior :declared_clojure_seed_subset
            :rejected-behavior "BOOT2002"
            :artifact-keys [:seed-compiler-manifest]
            :dependencies #{"L1" "L6" "C1" "C2" "C5" "C6" "C7" "C8"
                            "C11" "C15" "B5" "TEST1" "TEST2" "TEST6"
                            "BOOT5"}}
   "BOOT3" {:title "Self-Hosted Compiler Plan"
            :owned-surface :self-hosted-component-manifest
            :accepted-behavior :module_by_module_gravity_migration_plan
            :rejected-behavior "BOOT3002"
            :artifact-keys [:self-hosted-component-manifest
                            :equivalence-report]
            :dependencies #{"P3" "C1" "C18" "BOOT4" "TEST2" "TEST13"
                            "PKG7" "PKG10"}}
   "BOOT4" {:title "Compiler-in-Gravity Coding Standard"
            :owned-surface :compiler-coding-standard-report
            :accepted-behavior :deterministic_meta_profile_compiler_rules
            :rejected-behavior "BOOT4003"
            :artifact-keys [:compiler-coding-standard-report]
            :dependencies #{"P3" "C17" "C18" "SAFE6" "PKG7" "TEST2"}}
   "BOOT5" {:title "Stage Compatibility Matrix"
            :owned-surface :stage-compatibility-matrix
            :accepted-behavior :versioned_stage_support_matrix
            :rejected-behavior "BOOT5003"
            :artifact-keys [:stage-compatibility-matrix]
            :dependencies #{"BOOT1" "BOOT4" "P13" "B14" "TEST13" "GOV5"}}
   "BOOT6" {:title "Trusting Trust and Reproducible Bootstrap Plan"
            :owned-surface :trusting-trust-report
            :accepted-behavior :controlled_rebuild_and_trust_summary
            :rejected-behavior "BOOT6001"
            :artifact-keys [:trusting-trust-report]
            :dependencies #{"PKG7" "PKG10" "PKG12" "TEST13" "BOOT8"}}
   "BOOT7" {:title "Self-Hosting Validation and Equivalence Plan"
            :owned-surface :equivalence-report
            :accepted-behavior :stage_equivalence_with_reviewed_deltas
            :rejected-behavior "BOOT7001"
            :artifact-keys [:equivalence-report]
            :dependencies #{"TEST10" "TEST13" "PKG3" "PKG7" "BOOT5"}}
   "BOOT8" {:title "Bootstrap Artifact Provenance Specification"
            :owned-surface :bootstrap-provenance-record
            :accepted-behavior :acyclic_compiler_lineage_and_auditor_index
            :rejected-behavior "BOOT8002"
            :artifact-keys [:bootstrap-provenance-record]
            :dependencies #{"PKG3" "PKG10" "PKG12" "BOOT5" "BOOT6"
                            "BOOT7"}}})

(defn p15-document-number
  [document]
  (Integer/parseInt (subs document 4)))

(defn p15-task-id
  [document]
  (str "P15-D" (+ 202 (p15-document-number document))))

(defn p15-bootstrap-source-overrides
  [module]
  (get-in module [:metadata :bootstrap :self-hosting] {}))

(defn p15-bootstrap-diagnostic-document
  [diagnostic-id]
  (some (fn [document]
          (when (some #(= diagnostic-id %)
                      (p15-bootstrap-diagnostics-by-document document))
            document))
        p15-bootstrap-documents))

(defn p15-bootstrap-fail!
  [id source-path subject extra]
  (let [document (or (:document-id subject)
                     (p15-bootstrap-diagnostic-document id))]
    (fail! id
           "P15 bootstrap and self-hosting validation failed"
           (merge {:source-span (or (:source-span subject)
                                    (source-span source-path 0))
                   :diagnostic-family :phase15-bootstrap-self-hosting
                   :stage :bootstrap-self-hosting
                   :document-id document
                   :task (when document (p15-task-id document))
                   :bootstrap-stage (or (:bootstrap-stage subject) :stage0)
                   :artifact-id (:artifact-id subject)
                   :missing-fact (:missing-fact subject)
                   :fallback-status :rejected
                   :remediation "Phase 15 requires explicit stage evidence, Clojure seed scope, Gravity migration boundaries, deterministic compiler module rules, versioned compatibility rows, controlled rebuild environments, equivalence inputs, and traversable bootstrap provenance before self-hosting tasks can complete."}
                  extra))))

(defn p15-bootstrap-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (if-let [id (get p15-bootstrap-override-diagnostics fail-kind)]
      (p15-bootstrap-fail!
       id source-path
       {:artifact-id (str "p15-bootstrap-" (name fail-kind))
        :document-id (p15-bootstrap-diagnostic-document id)
        :missing-fact fail-kind}
       {:missing-fields [fail-kind]})
      (p15-bootstrap-fail!
       "P15-MANIFEST" source-path
       {:artifact-id "p15-bootstrap-unknown-override"
        :missing-fact fail-kind}
       {:missing-fields [:known-override-diagnostic]}))))