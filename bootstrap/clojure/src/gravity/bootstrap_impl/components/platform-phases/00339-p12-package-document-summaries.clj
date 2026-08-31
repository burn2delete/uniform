

(def p12-package-document-summaries
  {"PKG1" {:title "Project File Specification"
           :owned-surface :project-manifest
           :accepted-behavior :explicit_offline_project_manifest
           :rejected-behavior "PKG1006"
           :artifact-keys [:project-manifest]
           :dependencies #{"D1" "P1" "P13" "L6" "L15" "SAFE6" "SAFE14" "C1" "S9" "PKG5" "PKG6" "PKG7"}}
   "PKG2" {:title "Build System Architecture"
           :owned-surface :build-graph
           :accepted-behavior :typed_hermetic_build_graph
           :rejected-behavior "PKG2001"
           :artifact-keys [:build-graph]
           :dependencies #{"C1" "C18" "B13" "PKG1" "PKG5" "PKG7" "PKG11" "TEST1" "TEST13"}}
   "PKG3" {:title "Artifact Model Specification"
           :owned-surface :artifact-manifest
           :accepted-behavior :manifested_content_addressed_artifact
           :rejected-behavior "PKG3005"
           :artifact-keys [:artifact-manifest]
           :dependencies #{"S9" "B13" "C15" "R12" "PKG7" "PKG10" "PKG12" "TEST1" "TEST13"}}
   "PKG4" {:title "Package Manager Specification"
           :owned-surface :package-operation
           :accepted-behavior :verified_machine_readable_package_operation
           :rejected-behavior "PKG4001"
           :artifact-keys [:package-manifest :package-operation]
           :dependencies #{"PKG1" "PKG5" "PKG6" "PKG8" "PKG9" "PKG10" "PKG12" "SAFE14"}}
   "PKG5" {:title "Dependency Resolution Specification"
           :owned-surface :resolution-report
           :accepted-behavior :deterministic_policy_checked_resolution
           :rejected-behavior "PKG5002"
           :artifact-keys [:lockfile :resolution-report]
           :dependencies #{"PKG1" "PKG4" "PKG6" "PKG8" "PKG9" "PKG10" "PKG11" "PKG12"}}
   "PKG6" {:title "Capability and Permission Manifest Specification"
           :owned-surface :capability-manifest
           :accepted-behavior :package_authority_request_manifest
           :rejected-behavior "PKG6004"
           :artifact-keys [:capability-manifest]
           :dependencies #{"L6" "L15" "SAFE10" "R11" "PKG1" "PKG5" "PKG8" "A4" "A8"}}
   "PKG7" {:title "Reproducible Build Specification"
           :owned-surface :reproducible-build-recipe
           :accepted-behavior :locked_controlled_reproducible_recipe
           :rejected-behavior "PKG7003"
           :artifact-keys [:reproducible-build-recipe]
           :dependencies #{"PKG1" "PKG2" "PKG3" "PKG5" "PKG10" "PKG12" "BOOT6"}}
   "PKG8" {:title "Package Safety and Audit Metadata Specification"
           :owned-surface :package-safety
           :accepted-behavior :schema_validated_safety_audit_metadata
           :rejected-behavior "PKG8001"
           :artifact-keys [:package-safety]
           :dependencies #{"SAFE6" "SAFE7" "SAFE11" "SAFE14" "SAFE15" "PKG6" "PKG10" "PKG12"}}
   "PKG9" {:title "Private Registry and Latent Package Space Design"
           :owned-surface :registry-record
           :accepted-behavior :grant_gated_private_and_latent_registry
           :rejected-behavior "PKG9001"
           :artifact-keys [:registry-record]
           :dependencies #{"PKG4" "PKG5" "PKG8" "PKG10" "PKG12" "A11" "GOV10"}}
   "PKG10" {:title "Supply-Chain Security and Provenance Specification"
            :owned-surface :provenance-record
            :accepted-behavior :linked_release_provenance
            :rejected-behavior "PKG10001"
            :artifact-keys [:provenance-record]
            :dependencies #{"SAFE14" "PKG3" "PKG5" "PKG7" "PKG8" "PKG12" "BOOT6" "GOV4"}}
   "PKG11" {:title "Cross-Compilation and Target Matrix Specification"
            :owned-surface :target-matrix
            :accepted-behavior :expanded_per_target_release_matrix
            :rejected-behavior "PKG11002"
            :artifact-keys [:target-matrix]
            :dependencies #{"P1" "P13" "B1" "B14" "R1" "R12" "DOM1" "DOM21" "PKG5" "PKG7" "TEST3" "TEST4" "TEST6"}}
   "PKG12" {:title "Artifact Signing Verification and SBOM Specification"
            :owned-surface :signing-sbom-verification
            :accepted-behavior :canonical_signed_verified_release_bundle
            :rejected-behavior "PKG12002"
            :artifact-keys [:signing-sbom-verification]
            :dependencies #{"S3" "S9" "PKG3" "PKG7" "PKG8" "PKG10" "L12" "GOV4" "GOV10"}}})

(defn p12-document-number
  [document]
  (Integer/parseInt (subs document 3)))

(defn p12-task-id
  [document]
  (str "P12-D" (+ 164 (p12-document-number document))))

(defn p12-package-source-overrides
  [module]
  (get-in module [:metadata :package :artifacts] {}))

(defn p12-package-diagnostic-document
  [diagnostic-id]
  (some (fn [document]
          (when (some #(= diagnostic-id (first %))
                      (vals (p12-package-contracts document)))
            document))
        p12-package-documents))

(defn p12-package-fail!
  [id source-path subject extra]
  (let [document (or (:document-id subject)
                     (p12-package-diagnostic-document id))]
    (fail! id
           "P12 package/build/artifact validation failed"
           (merge {:source-span (or (:source-span subject)
                                    (source-span source-path 0))
                   :diagnostic-family :phase12-package-artifacts
                   :stage :package-artifacts
                   :document-id document
                   :task (when document (p12-task-id document))
                   :package-id (or (:package-id subject) "acme/support-agent")
                   :target (or (:target subject) :jvm-21)
                   :artifact-id (:artifact-id subject)
                   :missing-fact (:missing-fact subject)
                   :fallback-status :rejected
                   :remediation "Phase 12 requires explicit project manifests, lockfiles, build graphs, package operations, dependency resolution, capability manifests, reproducible recipes, safety metadata, registry records, provenance, target matrices, signing, SBOM, accepted and rejected fixtures, stable diagnostics, and capability-based proof."}
                  extra))))

(defn p12-package-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (if-let [id (get p12-package-override-diagnostics fail-kind)]
      (p12-package-fail!
       id source-path
       {:artifact-id (str "p12-package-" (name fail-kind))
        :document-id (p12-package-diagnostic-document id)
        :missing-fact fail-kind}
       {:missing-fields [fail-kind]})
      (p12-package-fail!
       "P12-MANIFEST" source-path
       {:artifact-id "p12-package-unknown-override"
        :missing-fact fail-kind}
       {:missing-fields [:known-override-diagnostic]}))))

(defn p12-contract-diagnostics
  [document]
  (mapv (comp first val)
        (sort-by (comp name key) (p12-package-contracts document))))

(defn p12-contract-evidence
  [document]
  (into {}
        (map (fn [[fact [diagnostic missing-fact]]]
               [fact {:diagnostic diagnostic
                      :missing-fact missing-fact
                      :source :governing-document
                      :status :present}])
             (sort-by (comp name key) (p12-package-contracts document)))))

(defn p12-project-manifest
  []
  {:artifact :gravity/project-manifest
   :artifact-id "manifest:acme/support-agent:0.3.0"
   :package-id "acme/support-agent"
   :version "0.3.0"
   :edition "2026.1"
   :offline-parse true
   :source-roots ["src" "workflows"]
   :generated-source-roots [{:root "target/generated"
                             :generator "gravity.schema/generate"
                             :hash "sha256:generated-input-001"}]
   :profiles [:hosted :ai]
   :targets [:jvm-21 :workflow-graph]
   :entrypoints {:service "support.main/serve"
                 :workflow "support.workflow/triage"}
   :dependencies {"gravity/core" "1.0.0"
                  "gravity/ai" "1.2.0"
                  "gravity/http" "2.1.4"}
   :registries {:gravity-public {:scope "gravity/*"
                                 :signatures :required}}
   :effects {:request [:network/http :database/read :ai/model-call]
             :deny [:shell/exec :secrets/read :filesystem/write]}
   :capabilities {:request [:http/client :db/query :model/call]
                  :deny [:shell/exec :secret/read :fs/write]}
   :artifacts [:library :agent-manifest :workflow-graph
               :sbom :signature :provenance]
   :policy {:unsafe :deny
            :release {:lockfile true :sign true :sbom true}}
   :lockfile-complete true
   :canonical-hash (c4-artifact-id [:p12 :project-manifest])
   :status :complete})

(defn p12-lockfile
  []
  {:artifact :gravity/lockfile
   :artifact-id "lock:acme/support-agent:0.3.0"
   :complete true
   :offline-proof true
   :lockfile-hash "sha256:lock-001"
   :records [{:package "gravity/core" :version "1.0.0"
              :registry :gravity-public :content-hash "sha256:core-100"
              :capabilities [] :safety :reviewed
              :provenance "prov:gravity-core"}
             {:package "gravity/http" :version "2.1.4"
              :registry :gravity-public :content-hash "sha256:http-214"
              :capabilities [:http/client] :safety :reviewed
              :provenance "prov:gravity-http"}
             {:package "gravity/ai" :version "1.2.0"
              :registry :gravity-public :content-hash "sha256:ai-120"
              :capabilities [:model/call] :safety :reviewed
              :provenance "prov:gravity-ai"}]
   :status :complete})

(defn p12-build-graph
  []
  {:artifact :gravity/build-graph
   :artifact-id "build-graph:phase12-support-agent"
   :project-manifest-hash "sha256:project-001"
   :lockfile-hash "sha256:lock-001"
   :compiler-id "gravityc-seed:0.1.0"
   :policy-hash "sha256:policy-001"
   :declared-effects #{:build/sign}
   :network-policy :dependencies-only
   :generated-source-provenance [{:generator "gravity.schema/generate"
                                  :inputs ["sha256:schema-source-001"]
                                  :outputs ["sha256:generated-source-001"]}]
   :nodes [{:node-id :read-source
            :inputs ["sha256:source-001"]
            :outputs ["syntax:source-001"]
            :effects #{}
            :cache-key "sha256:cache-read-source"
            :tool-id "gravity.reader:0.1.0"}
           {:node-id :lower-workflow
            :inputs ["mir:workflow-001"]
            :outputs ["artifact:workflow-001"]
            :effects #{}
            :cache-key "sha256:cache-lower-workflow"
            :tool-id "gravity.workflow-lowerer:0.1.0"}
           {:node-id :sign-release
            :inputs ["artifact:library-001" "artifact:workflow-001" "sbom:001"]
            :outputs ["signature:support-agent-001"]
            :effects #{:build/sign}
            :cache-key "sha256:cache-sign"
            :tool-id "gravity.signer:0.1.0"}]
   :release-evidence ["test-report:phase12-support-agent"
                      "safety-report:phase12-support-agent"
                      "signature:support-agent-001"
                      "sbom:support-agent-001"]
   :status :complete})