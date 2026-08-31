(defn- semantic-mid-p13-tooling-extended-artifacts
  []
  {:dev-server-session
   {:artifact :gravity/dev-server-session
    :artifact-id "dev-session:phase13"
    :document "T8"
    :tool-id :gravity-dev
    :session-id "dev:phase13"
    :project "gravity.edn"
    :profile :hosted
    :target :jvm-21
    :capability-grants [:http/client]
    :incremental-updates [{:input "src/support/main.grav"
                           :result :converged-with-cli}]
    :diagnostic-stream "diagnostic-stream:dev-001"
    :artifact-events [{:artifact-id "artifact:library-001"
                       :path "build/support-agent.jar"
                       :profile :hosted
                       :target :jvm-21
                       :build-node :emit-library}]
    :runtime-log "runtime-log:dev-001"
    :hot-reload-decisions [{:input "src/support/main.grav"
                            :decision :restart
                            :reason :checked-assumption-invalidated}]
    :bug-report-redacted true
    :status :complete}
   :registry-ux-record
   {:artifact :gravity/registry-ux-record
    :artifact-id "registry-ux:gravity-http"
    :document "T9"
    :tool-id :gravity-registry
    :package-detail-json {:profiles [:hosted :native]
                          :targets [:jvm-21 :wasm32-wasi]
                          :capabilities [:http/client]}
    :human-view {:matches-json true}
    :update-diff {:capability-delta []
                  :safety-delta []
                  :provenance-delta [:builder-attestation]
                  :capability-diff-visible true}
    :verification-report {:signature :verified
                          :sbom :verified
                          :provenance :verified}
    :search-results {:filters [:profile :target :capability :safety]
                     :respected true}
    :access-denials [{:package "acme/private"
                      :metadata-leaked false}]
    :policy-compatibility {:status :compatible}
    :status :complete}
   :ir-inspector-bundle
   {:artifact :gravity/ir-inspector-bundle
    :artifact-id "ir-inspector:phase13"
    :document "T10"
    :tool-id :gravity-inspect-ir
    :stage-id :typed-core
    :compiler-version "gravityc-seed:0.1.0"
    :project-hash "sha256:project-001"
    :profile :hosted
    :target :jvm-21
    :stage-views [:syntax-objects :typed-core :mir :artifact-manifest]
    :source-span-maps ["source-map:reader-to-mir"]
    :pass-diff-reports [{:pass :bounds-check-elision
                         :nonsemantic-renumbering-ignored true}]
    :preservation-reports [{:evidence "proof:bounds-check-elision"
                            :preserved [:types :effects :source-spans]}]
    :origin-chains [{:generated "macro-output:when"
                     :source "src/support/main.grav:17:1"}]
    :redacted true
    :status :complete}
   :profiler-report
   {:artifact :gravity/profiler-report
    :artifact-id "artifact:library-001"
    :document "T11"
    :tool-id :gravity-profile
    :report-id "perf-report:phase13"
    :profile :hosted
    :target :jvm-21
    :benchmark-id "bench/support-agent-check"
    :environment {:host "linux-x64" :runtime :managed}
    :compiler-version "gravityc-seed:0.1.0"
    :samples [{:source-span "src/support/main.grav:42:3"
               :latency-ms 12.5}]
    :comparison-policy {:latency-p95-threshold "10pct"}
    :check-elision-report {:status :accepted
                           :evidence ["proof:bounds-check-elision"]}
    :capabilities [:profiler/read-counters]
    :status :complete}
   :safety-audit-report
   {:artifact :gravity/safety-audit-report
    :artifact-id "audit-report:phase13"
    :document "T12"
    :tool-id :gravity-audit
    :report-id "safety-audit:phase13"
    :unsafe-islands []
    :capability-graph {:requested [:http/client]
                       :granted [:http/client]
                       :denied [:shell/exec]
                       :used [:http/client]}
    :taint-graph [{:source "user-ticket"
                   :sink "support.workflow/model-call"
                   :validator "schema:triage-input"}]
    :ffi-boundaries []
    :ai-safety {:prompt-authority :partitioned
                :tool-escalations 0}
    :package-safety {:sbom "sbom:support-agent-001"
                     :unsafe-summary {:count 0}}
    :proof-index [{:proof-id "proof:support-agent-capability-summary"
                   :checker "gravity.safety/check"}]
    :missing-evidence-report []
    :redacted true
    :status :complete}
   :ai-tooling-record
   {:artifact :gravity/ai-tooling-record
    :artifact-id "ai-tooling:phase13"
    :document "T13"
    :tool-id :gravity-ai-assist
    :modes [:diagnostic-explanation :patch-proposal :test-generation
            :package-update-review]
    :plan-artifact {:schema "GravityAIPlan/v1" :validated true}
    :patch-artifact {:schema "GravityPatch/v1" :validated true}
    :generated-source-provenance [{:input "prompt:patch-proposal-001"
                                   :output "patch:proposal-001"}]
    :prompt-model-ledger ["prompt:patch-proposal-001"
                          "model:gpt-tooling-001"]
    :tool-call-ledger [{:tool :search
                        :capability :repo/read}]
    :validation-report {:type :passed
                        :effect :passed
                        :safety :passed
                        :test :passed}
    :human-review-record {:review-id "review:phase13-ai"
                          :decision :approved}
    :replay-trace {:trace-id "replay:phase13-ai"
                   :hidden-tool-use-detected false}
    :status :complete}
   :tooling-ui-data-model
   {:artifact :gravity/tooling-ui-data-model
    :artifact-id "tooling-ui:phase13"
    :documents p13-tooling-documents
    :views [:cli :repl :formatter :lint :lsp :debug :docs :dev
            :registry :ir :profile :safety :ai]
    :source-of-truth :compiler-package-runtime-artifacts
    :structured-automation true
    :redacted true
    :status :complete}})
