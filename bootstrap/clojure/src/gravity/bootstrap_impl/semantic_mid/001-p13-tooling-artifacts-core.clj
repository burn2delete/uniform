(defn- semantic-mid-p13-tooling-core-artifacts
  []
  {:cli-command-set
   {:artifact :gravity/cli-command-set
    :artifact-id "cli-contract:gravity"
    :document "T1"
    :tool-id :gravity-cli
    :commands [:check :build :test :run :repl :fmt :lint :doc :package
               :registry :audit :verify :inspect-ir :profile :ai :explain]
    :exit-codes {:success 0 :check-failure 1 :usage-error 2
                 :dependency-resolution-failure 3
                 :build-or-verification-failure 4
                 :runtime-execution-failure 5
                 :authority-or-human-review-missing 6}
    :json-output true
    :diagnostic-routing {:schema "GravityDiagnosticStream/v1"
                         :sources [:compiler :package :runtime :tooling]}
    :artifact-outputs [:artifact-manifest :diagnostic-stream
                       :verification-report]
    :capability-prompts {:shown-grants [:http/client :db/query]
                         :shown-denials [:shell/exec :secret/read]
                         :human-review-required [:package/publish
                                                 :ai/apply-patch]}
    :secret-redaction {:mode :redact-values
                       :secret-names-visible true}
    :golden-fixtures ["cli-check-success.edn"
                      "cli-authority-denied.edn"]
    :status :complete}
   :repl-session-artifact
   {:artifact :gravity/repl-session
    :artifact-id "repl-session:support-agent"
    :document "T2"
    :tool-id :gravity-repl
    :session-id "repl:phase13-support-agent"
    :project-manifest-hash "sha256:project-001"
    :lockfile-hash "sha256:lock-001"
    :profile :hosted
    :target :jvm-21
    :namespace "support.main"
    :capability-grants [:fs/read :http/client]
    :evaluation-history [{:form "(:effects '(fs/read \"notes.txt\"))"
                          :checks [:reader :macro :type :effect
                                   :profile :safety]
                          :result [:filesystem/read]}]
    :runtime-ledgers ["runtime-ledger:repl-001"]
    :transcript-redacted true
    :command-forms [:profile :target :ns :load :eval :expand :type
                    :effects :capabilities :inspect :artifacts
                    :diagnostics :replay :transcript]
    :status :complete}
   :formatter-fixture
   {:artifact :gravity/formatter-fixture
    :artifact-id "fmt-report:phase13"
    :document "T3"
    :tool-id :gravity-fmt
    :formatter-version "gravity-fmt:0.1.0"
    :configuration-hash "sha256:fmt-config-001"
    :reader-round-trip true
    :changed-files ["src/support/main.grav"]
    :diff-output true
    :json-report {:files 1
                  :changed-ranges ["src/support/main.grav:1:1"]}
    :comments-preserved true
    :metadata-preserved true
    :generated-source-policy :deny-by-default
    :status :complete}
   :linter-diagnostic-report
   {:artifact :gravity/linter-diagnostic-report
    :artifact-id "lint-report:phase13"
    :document "T4"
    :tool-id :gravity-lint
    :rules [:profile :effects :safety :package :ai]
    :diagnostics [{:id "LINT-CAP-MIN"
                   :severity :warning
                   :source-span "src/support/main.grav:12:4"}]
    :baseline {:mode :fail-on-new :stale-entries 0}
    :rule-metadata {"LINT-CAP-MIN" {:inputs [:capability-manifest]
                                    :fixable false}}
    :compiler-facts [:types :effects :capabilities :profile :artifacts]
    :profile-target-applicability {:hosted/jvm-21 [:effects
                                                   :capabilities]}
    :json-export true
    :sarif-like-export true
    :status :complete}
   :lsp-capability-matrix
   {:artifact :gravity/lsp-capability-matrix
    :artifact-id "lsp-matrix:phase13"
    :document "T5"
    :tool-id :gravity-lsp
    :server-id "gravity-lsp:0.1.0"
    :compiler-state "compiler-state:phase13"
    :diagnostics-match-cli true
    :hover-facts [:type :effects :capabilities :profile :docs]
    :completion-constraints [:namespace :profile :visibility :target]
    :code-action-records [{:title "Add capability request"
                           :checks [:reader-round-trip :type :effect]}]
    :rename-boundaries [:namespace :macro-origin]
    :trace-redacted true
    :generated-file-policy :source-artifact-required
    :status :complete}
   :debugger-trace
   {:artifact :gravity/debugger-trace
    :artifact-id "debug-trace:phase13"
    :document "T6"
    :tool-id :gravity-debug
    :session-id "debug:phase13"
    :profile :hosted
    :target :jvm-21
    :debug-data-version "GravityDebug/v1"
    :breakpoints [{:source-span "src/support/main.grav:42:3"
                   :artifact-location "jvm:Support.main:42"}]
    :stack-frames [{:function "support.main/serve"
                    :source-span "src/support/main.grav:42:3"}]
    :variable-reports [{:name "request"
                        :type "SupportRequest"
                        :redacted false}]
    :policy-denials [{:operation :mutate-state
                      :reason :missing-debug-authority}]
    :source-map-validation {:status :passed}
    :status :complete}
   :documentation-artifact
   {:artifact :gravity/documentation-artifact
    :artifact-id "docs:support-agent"
    :document "T7"
    :tool-id :gravity-doc
    :source-hash "sha256:source-001"
    :compiler-version "gravityc-seed:0.1.0"
    :package-version "0.3.0"
    :api-signature-index ["support.main/serve"]
    :effect-capability-docs {"support.main/serve"
                             {:effects [:network/http]
                              :capabilities [:http/client]}}
    :schema-links ["schema:SupportRequest/v1"]
    :example-validation-report {:checked 3 :failed 0}
    :structured-docs true
    :redacted true
    :freshness {:source-hash "sha256:source-001"
                :artifact-hash "sha256:artifact-001"}
    :status :complete}})
