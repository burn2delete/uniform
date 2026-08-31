

(defn p12-artifact-manifest
  []
  {:artifact :gravity/artifact-manifest
   :artifact-id "artifact:library-001"
   :kind :library
   :schema-version "GravityArtifact/v1"
   :package-id "acme/support-agent"
   :source-graph-hash "sha256:source-001"
   :project-manifest-hash "sha256:project-001"
   :lockfile-hash "sha256:lock-001"
   :compiler-id "gravityc-seed:0.1.0"
   :profile :hosted
   :target :jvm-21
   :content-hash "sha256:library-content-001"
   :dependency-graph-hash "sha256:deps-001"
   :capability-summary {:requests [:http/client :db/query :model/call]}
   :evidence {:types "type-report:support-agent"
              :effects "effect-report:support-agent"
              :capabilities "capability-manifest:support-agent-001"
              :safety "safety-report:phase12-support-agent"
              :tests "test-report:phase12-support-agent"
              :provenance "provenance:support-agent-001"
              :sbom "sbom:support-agent-001"
              :signature "signature:support-agent-001"}
   :canonical true
   :status :complete})

(defn p12-package-manifest
  []
  {:artifact :gravity/package-manifest
   :artifact-id "package-manifest:acme/support-agent:0.3.0"
   :package-id "acme/support-agent"
   :version "0.3.0"
   :artifact-kinds [:library :agent-manifest :workflow-graph]
   :release-policy {:sign true :sbom true :provenance true}
   :operation-compatibility [:add :remove :update :verify :publish]
   :status :complete})

(defn p12-package-operation
  []
  {:artifact :gravity/package-operation
   :artifact-id "package-operation:add-gravity-http"
   :operation :add
   :package-id "gravity/http"
   :version "2.1.4"
   :registry :gravity-public
   :effects #{:build/network :filesystem/write}
   :download-verified true
   :registry-signature-verified true
   :lockfile-diff {:changed ["gravity/http"]
                   :content-hash "sha256:http-214"
                   :capability-summary [:http/client]
                   :provenance-summary "prov:gravity-http"}
   :capability-diff {:added [] :removed []}
   :safety-diff {:unsafe-added [] :review-state :reviewed}
   :provenance-diff {:new ["prov:gravity-http"]}
   :machine-readable true
   :credential-redaction :redacted
   :status :complete})

(defn p12-resolution-report
  []
  {:artifact :gravity/dependency-resolution-report
   :artifact-id "resolution:acme/support-agent:0.3.0"
   :canonical-input-hash "sha256:resolution-input-001"
   :deterministic true
   :mode :locked-or-explain
   :selected-graph ["gravity/core@1.0.0"
                    "gravity/http@2.1.4"
                    "gravity/ai@1.2.0"]
   :target-variants {:jvm-21 ["gravity/http:jvm"]
                     :workflow-graph ["gravity/ai:workflow"]}
   :capability-compatible true
   :private-registry-grants {:acme/private :read}
   :revocation-status :checked
   :feature-selections {"gravity/http" #{:client}}
   :conflict-report :none
   :offline-resolution-proof true
   :status :complete})

(defn p12-capability-manifest
  []
  {:artifact :gravity/package-capability-manifest
   :artifact-id "capability-manifest:support-agent-001"
   :package-id "acme/support-agent"
   :effects {:requests #{:network/http :database/read :ai/model-call}
             :denies #{:shell/exec :secrets/read :filesystem/write}}
   :capabilities {:requests #{:http/client :db/query :model/call}
                  :denies #{:shell/exec :secret/read :fs/write}
                  :deployment-grants #{:http/client :db/query :model/call}}
   :derivation {:network/http :http/client
                :database/read :db/query
                :ai/model-call :model/call}
   :dependency-diff {:expanded []}
   :runtime-handles {:db/query "DatabaseReadCap"
                     :http/client "HttpClientCap"
                     :model/call "ModelCallCap"}
   :sbom-fields [:capabilities :privileged-effects]
   :audit-events [:capability/requested :capability/granted
                  :capability/used]
   :status :complete})

(defn p12-reproducible-build-recipe
  []
  {:artifact :gravity/reproducible-build-recipe
   :artifact-id "recipe:acme/support-agent:0.3.0"
   :package-id "acme/support-agent"
   :source-hash "sha256:source-001"
   :project-hash "sha256:project-001"
   :lockfile-hash "sha256:lock-001"
   :compiler-id "gravityc-seed:0.1.0"
   :toolchain-id "clojure-bootstrap:stage0"
   :environment {:time :fixed
                 :locale "C"
                 :timezone "UTC"
                 :filesystem-order :sorted
                 :network :disabled}
   :randomness {:seed "sha256:seed-001"}
   :generated-source-inputs ["sha256:generated-input-001"]
   :target-matrix [:jvm-21 :workflow-graph]
   :build-graph-hash "sha256:build-graph-001"
   :expected-artifacts ["artifact:library-001" "artifact:workflow-001"
                        "sbom:support-agent-001"]
   :output-hashes ["sha256:library-content-001"
                   "sha256:workflow-content-001"]
   :rebuild-verification :manifest-and-content-hash
   :non-reproducible-exceptions []
   :status :complete})