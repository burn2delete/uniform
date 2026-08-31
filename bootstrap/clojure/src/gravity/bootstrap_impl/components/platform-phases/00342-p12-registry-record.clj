

(defn p12-registry-record
  []
  {:artifact :gravity/registry-record
   :artifact-id "registry:gravity-public:support-agent"
   :registry-id :gravity-public
   :access-grant true
   :private-metadata-redacted true
   :index-signature {:required true :verified true}
   :latent-package-states {:generated :review-requested
                           :reviewed :published}
   :generated-package-provenance {:generator "gravity.ai/package-proposal"
                                  :input-hashes ["sha256:proposal-input"]}
   :publish-review-state :reviewed
   :mirror-attestation {:verified true}
   :lockfile-registry-source :recorded
   :status :complete})

(defn p12-provenance-record
  []
  {:artifact :gravity/provenance-record
   :artifact-id "provenance:support-agent-001"
   :subject "artifact:library-001"
   :content-hash "sha256:library-content-001"
   :source-graph-hash "sha256:source-001"
   :project-manifest-hash "sha256:project-001"
   :lockfile-hash "sha256:lock-001"
   :builder-id "gravity-builder:trusted-linux-x64"
   :compiler-id "gravityc-seed:0.1.0"
   :build-recipe-hash "sha256:recipe-001"
   :dependency-graph-hash "sha256:deps-001"
   :generated-source-ledger [{:generator "gravity.schema/generate"
                              :input "sha256:schema-source-001"
                              :output "sha256:generated-source-001"}]
   :binary-blob-ledger [{:blob "libclock.a"
                         :hash "sha256:clock-blob"
                         :license "MIT"
                         :policy :approved}]
   :evidence [:tests :safety :reproducible-build :sbom]
   :signature "signature:support-agent-001"
   :sbom "sbom:support-agent-001"
   :keyless-signing {:issuer "https://token.actions.githubusercontent.com"
                     :subject "https://github.com/acme/support/.github/workflows/release.yml@refs/heads/main"
                     :audience "sigstore"
                     :certificate "sha256:fulcio-cert"
                     :transparency-log "rekor:entry"}
   :revocation-status :checked
   :status :complete})

(defn p12-target-matrix
  []
  {:artifact :gravity/target-matrix
   :artifact-id "target-matrix:acme/support-agent:0.3.0"
   :implicit-host-target? false
   :entries [{:profile :hosted
              :backend :jvm
              :target :jvm-21
              :runtime :managed
              :support :supported
              :dependency-variants ["gravity/http:jvm"]
              :capabilities #{:http/client :db/query}
              :artifact-kinds [:library]
              :conformance "test-report:jvm-21"}
             {:profile :ai
              :backend :workflow-graph
              :target :workflow-graph
              :runtime :workflow
              :support :experimental
              :dependency-variants ["gravity/ai:workflow"]
              :capabilities #{:model/call}
              :artifact-kinds [:agent-manifest :workflow-graph]
              :conformance "test-report:workflow-graph"}]
   :fallback-targets {:policy :explicit-only}
   :release-support-table {:jvm-21 :supported
                           :workflow-graph :experimental}
   :status :complete})

(defn p12-signing-sbom-verification
  []
  {:artifact :gravity/signing-sbom-verification
   :artifact-id "verification:support-agent-001"
   :signature {:id "signature:support-agent-001"
               :canonical-payload true
               :content-hash "sha256:library-content-001"
               :manifest-hash "sha256:manifest-001"
               :policy-id :release-signing}
   :sbom {:id "sbom:support-agent-001"
          :transitive-dependencies ["gravity/core@1.0.0"
                                    "gravity/http@2.1.4"
                                    "gravity/ai@1.2.0"]
          :capabilities [:http/client :db/query :model/call]
          :unsafe-summary ["ffi.clock/read-monotonic"]
          :generated-source ["target/generated"]
          :binary-blobs ["libclock.a"]}
   :attestation {:id "attestation:support-agent-001"
                 :subject "artifact:library-001"
                 :manifest-hash "sha256:manifest-001"
                 :builder "gravity-builder:trusted-linux-x64"
                 :build-platform "linux-x64"
                 :source-material "sha256:source-001"
                 :lockfile "sha256:lock-001"
                 :compiler "gravityc-seed:0.1.0"
                 :build-recipe "sha256:recipe-001"
                 :policy-level :hermetic
                 :verification-track :release}
   :keyless {:issuer "https://token.actions.githubusercontent.com"
             :subject "https://github.com/acme/support/.github/workflows/release.yml@refs/heads/main"
             :audience "sigstore"
             :certificate "sha256:fulcio-cert"
             :validity-window :checked}
   :transparency-log {:entry "rekor:entry"
                      :inclusion-proof :verified
                      :checkpoint :verified
                      :freshness :within-policy}
   :root-metadata {:tuf-root "sha256:tuf-root" :trusted true}
   :verification-report {:schema true
                         :signature true
                         :hash true
                         :identity true
                         :provenance true
                         :attestation true
                         :transparency-log true
                         :timestamp true
                         :revocation true
                         :policy true}
   :consumer-decision :accepted
   :status :complete})

(defn p12-package-document-record
  [document]
  (let [summary (p12-package-document-summaries document)]
    (merge
     {:document document
      :task-id (p12-task-id document)
      :governing-doc (p12-package-governing-documents document)
      :package-id "acme/support-agent"
      :diagnostics (p12-contract-diagnostics document)
      :evidence (p12-contract-evidence document)
      :conformance
      {:accepted-fixture "bootstrap/clojure/fixtures/accepted/package-artifacts.gravity"
       :rejected-fixture (str "bootstrap/clojure/fixtures/rejected/"
                              (p12-package-rejected-fixture-names document))
       :artifact-evidence :package-artifacts
       :validation-command "clojure -M:test"
       :status :complete}}
     summary)))

(defn p12-package-document-records
  []
  (into {} (map (fn [document]
                  [document (p12-package-document-record document)])
                p12-package-documents)))

(defn p12-accepted-package-fixtures
  []
  (mapv (fn [document]
          {:document document
           :fixture "bootstrap/clojure/fixtures/accepted/package-artifacts.gravity"
           :artifact (get-in (p12-package-document-summaries document)
                             [:owned-surface])
           :evidence [(p12-task-id document)
                      (p12-package-governing-documents document)]
           :status :accepted})
        p12-package-documents))

(defn p12-rejected-package-fixtures
  []
  (mapv (fn [document]
          {:document document
           :fixture (str "bootstrap/clojure/fixtures/rejected/"
                         (p12-package-rejected-fixture-names document))
           :artifact :stable-package-artifact-diagnostic
           :diagnostic (p12-package-rejected-diagnostics document)
           :evidence [(p12-task-id document)
                      (p12-package-governing-documents document)]
           :status :rejected})
        p12-package-documents))

(defn p12-package-conformance-evidence
  []
  (mapv (fn [document]
          {:document document
           :accepted-behavior (get-in (p12-package-document-summaries document)
                                      [:accepted-behavior])
           :rejected-behavior (p12-package-rejected-diagnostics document)
           :artifacts (get-in (p12-package-document-summaries document)
                              [:artifact-keys])
           :validation ["clojure -M:test"
                        "clojure -M:gravity package-artifacts bootstrap/clojure/fixtures/accepted/package-artifacts.gravity"]
           :status :complete})
        p12-package-documents))

(defn p12-package-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/phase12-package-diagnostic-stream
   :stage :package-artifacts
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           (let [document (p12-package-diagnostic-document id)]
             {:artifact :gravity/diagnostic
              :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
              :diagnostic id
              :rule id
              :severity :error
              :stage :package-artifacts
              :document-id document
              :task (when document (p12-task-id document))
              :primary {:span (source-span source-path index)
                        :syntax-id (str "p12-package-syntax-" index)
                        :artifact input-id}
              :missing-fact (or (some (fn [[_ [diagnostic missing-fact]]]
                                         (when (= id diagnostic) missing-fact))
                                       (get p12-package-contracts document))
                                :package_artifact_manifest)
              :remediation [{:kind :declare-project-and-lockfile}
                            {:kind :record-build-graph-and-artifacts}
                            {:kind :enforce-capability-and-safety-policy}
                            {:kind :verify-provenance-signing-sbom}]
              :ordering-key [id :package-artifacts]}))
         p12-package-diagnostic-ids
         (range))
   :status :complete})

(defn p12-present?
  [value]
  (if (coll? value)
    (seq value)
    (some? value)))