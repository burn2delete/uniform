

(defn p14-conformance-artifact-values
  []
  {:conformance-harness
   {:artifact :gravity/conformance-harness
    :artifact-id "conformance:harness:phase14"
    :suite-id :gravity-stage0-conformance
    :profiles [:core :hosted :ai]
    :targets [:jvm :jvm-21 :workflow-graph]
    :fixture-classes [:positive :negative :diagnostic :artifact
                      :runtime :profile :backend :library]
    :offline true
    :release-gate :fail-closed
    :status :complete}
   :fixture-manifest
   {:artifact :gravity/fixture-manifest
    :artifact-id "conformance:fixture-manifest"
    :positive-fixtures ["accepted/conformance-system.gravity"]
    :negative-fixtures (vals p14-conformance-rejected-fixture-names)
    :fixture-metadata-fields [:suite :profile :target :expected-result
                              :expected-diagnostic :capabilities]
    :hash "sha256:conformance-fixtures-001"
    :status :complete}
   :golden-diagnostics
   {:artifact :gravity/golden-diagnostics
    :artifact-id "conformance:golden-diagnostics"
    :diagnostics p14-conformance-diagnostic-ids
    :stable-codes true
    :source-spans true
    :profile-target true
    :remediation true
    :status :complete}
   :fuzz-property-suite
   {:artifact :gravity/fuzz-property-suite
    :artifact-id "conformance:fuzz-property"
    :document "TEST9"
    :seed "seed:918273"
    :generator-version "gravity-fuzz:0.1.0"
    :profile :core
    :target :jvm
    :shrinker :delta-source
    :properties [:read-print-read-equality :type-preservation
                 :effect-preservation :diagnostic-determinism]
    :failure-oracles [:known-diagnostic :no-crash :minimized-reproducer]
    :replayable true
    :status :complete}
   :differential-report
   {:artifact :gravity/differential-report
    :artifact-id "conformance:differential"
    :document "TEST10"
    :oracles [:typed-core :mir :jvm]
    :observables [:result :diagnostic-code :artifact-hash]
    :profile :hosted
    :target :jvm
    :numeric-mode :checked
    :accepted-divergence []
    :reproducers []
    :status :complete}
   :formal-proof-report
   {:artifact :gravity/formal-proof-report
    :artifact-id "conformance:formal"
    :document "TEST11"
    :claims [{:claim-id :effect-preservation
              :assumptions ["typed-core well formed"]
              :trusted-basis ["D9" "SAFE15"]
              :checker "gravity-proof-checker:0.1.0"
              :input-hash "sha256:formal-input-001"
              :machine-checkable true}]
    :stale-proof-detection true
    :counterexamples []
    :status :complete}
   :performance-regression-report
   {:artifact :gravity/performance-regression-report
    :artifact-id "conformance:performance"
    :document "TEST12"
    :benchmarks [{:benchmark-id "bench/compiler-check"
                  :profile :hosted
                  :target :jvm
                  :artifact-hash "sha256:compiler-stage0"
                  :compiler-version "gravityc-seed:0.1.0"
                  :environment "local-stage0"
                  :metric {:latency-p95-ms 25}
                  :threshold {:regression-percent 5}
                  :semantic-gates [:same-result :same-safety-evidence]}]
    :semantic-gates-passed true
    :safety-evidence ["proof:bounds-check-elision"]
    :status :complete}
   :language-conformance
   {:artifact :gravity/language-conformance-report
    :artifact-id "conformance:language"
    :document "TEST1"
    :suite-id "suite:language-conformance"
    :fixture-index ["reader" "syntax" "macro" "typed-core"
                    "effects" "capabilities"]
    :goldens {:reader "golden:reader"
              :syntax "golden:syntax"
              :typed-core "golden:typed-core"}
    :positive-fixtures 6
    :negative-fixtures 6
    :diagnostic-json true
    :feature-matrix {:core :complete :hosted :complete}
    :status :complete}
   :compiler-test-report
   {:artifact :gravity/compiler-test-report
    :artifact-id "conformance:compiler"
    :document "TEST2"
    :stage-goldens [:reader :syntax :macro :resolution :core :typed-core
                    :effects :ownership :safety :mir :domain-ir
                    :optimization :lowering]
    :preservation-reports [{:pass :inline-small-functions
                            :preserves [:types :effects :source-spans]}]
    :incremental-cache-traces ["cache-trace:changed-source"]
    :plugin-denials ["plugin-denial:ambient-authority"]
    :diagnostic-goldens true
    :status :complete}
   :runtime-conformance-report
   {:artifact :gravity/runtime-conformance-report
    :artifact-id "conformance:runtime"
    :document "TEST3"
    :runtime-families [:no-runtime :minimal-native :managed :memory
                       :concurrency :distributed :ai :repl :ffi
                       :capability-enforcement :observability]
    :profile :hosted
    :target :jvm
    :capability-decision-log [{:effect :filesystem/read
                               :grant-set #{}
                               :decision :denied}]
    :replay-trace "replay:runtime-001"
    :observability-schema "observability:events/v1"
    :status :complete}
   :profile-compliance-report
   {:artifact :gravity/profile-compliance-report
    :artifact-id "conformance:profiles"
    :document "TEST4"
    :profiles [:core :meta :hosted :native :firmware :kernel :hardware
               :distributed :ai :gpu :formal]
    :positive-fixtures 11
    :negative-fixtures 11
    :capability-legality true
    :runtime-service-legality true
    :matrix-artifact "profile-target-matrix:phase14"
    :status :complete}
   :safety-conformance-report
   {:artifact :gravity/safety-conformance-report
    :artifact-id "conformance:safety"
    :document "TEST5"
    :outcomes [:proven-safe :runtime-checked :rejected :unsafe-island]
    :positive-fixtures 16
    :negative-fixtures 16
    :unsafe-audit-records ["unsafe-audit:ffi-clock"]
    :runtime-check-report "runtime-check:bounds"
    :proof-certificate-report "proof:safety-stage0"
    :capability-denial-report "capability-denial:shell"
    :taint-flow-report "taint-flow:user-to-model"
    :status :complete}
   :backend-conformance-report
   {:artifact :gravity/backend-conformance-report
    :artifact-id "conformance:backend"
    :document "TEST6"
    :backend-matrix [:c :llvm :wasm :jvm :js-ts :mlir :gpu :hdl
                     :workflow-graph :query :mobile]
    :profile :hosted
    :target :jvm
    :runtime-family :managed
    :lowered-artifact-manifest "artifact:backend-jvm"
    :source-map-validation :passed
    :abi-layout-report "abi-layout:jvm"
    :differential-comparison :passed
    :status :complete}
   :standard-library-test-report
   {:artifact :gravity/standard-library-test-report
    :artifact-id "conformance:stdlib"
    :document "TEST7"
    :modules [:core :collections :strings :numeric :schemas
              :workflow :ai :testing]
    :profile-availability-matrix "stdlib:profile-matrix"
    :capability-denial-reports ["capability-denial:fs-read"]
    :property-test-reports ["property:collections-count"]
    :documentation-example-report {:checked 4 :failed 0}
    :stability-compatibility-report :passed
    :status :complete}
   :ai-workflow-eval-report
   {:artifact :gravity/ai-workflow-eval-report
    :artifact-id "conformance:ai-workflow"
    :document "TEST8"
    :subject "support-triage-workflow"
    :dataset "SupportTickets/v3"
    :identities {:model "model:stage0"
                 :prompt "prompt:triage"
                 :tool "tool:ticket-update"
                 :memory "memory:case-summaries"
                 :policy "policy:ai-safe"
                 :runtime "runtime:workflow"}
    :schema-validation 1.0
    :replay-traces [:happy-path :tool-denied :human-review-denied]
    :safety-probes [:prompt-injection :unauthorized-tool
                    :secret-exposure :policy-override]
    :release-gate-decision :accepted
    :status :complete}
   :self-hosting-validation-report
   {:artifact :gravity/self-hosting-validation-report
    :artifact-id "conformance:self-hosting"
    :document "TEST13"
    :stage :stage0-seed
    :trusted-inputs [:clojure-bootstrap :gravity-stage0-fixtures]
    :stage-compiler-artifact "compiler:stage0-clojure"
    :conformance-report "conformance:stage0"
    :rebuild-log "rebuild:stage0"
    :stage-comparison-report "stage-compare:seed"
    :provenance-attestation "provenance:stage0-seed"
    :tcb-delta {:current [:clojure-bootstrap]
                :objective [:gravity-self-hosted-compiler]}
    :unsafe-audit-report "unsafe-audit:compiler-seed"
    :status :complete}})

(defn p14-conformance-document-record
  [document]
  (let [summary (p14-conformance-document-summaries document)]
    (merge
     {:document document
      :task-id (p14-task-id document)
      :governing-doc (p14-conformance-phase-governing-documents document)
      :suite-id (get-in summary [:owned-surface])
      :diagnostics (p14-conformance-diagnostics-by-document document)
      :evidence (into {}
                      (map (fn [diagnostic]
                             [(keyword (str/lower-case diagnostic))
                              {:diagnostic diagnostic
                               :source :governing-document
                               :status :present}])
                           (p14-conformance-diagnostics-by-document document)))
      :conformance
      {:accepted-fixture "bootstrap/clojure/fixtures/accepted/conformance-system.gravity"
       :rejected-fixture (str "bootstrap/clojure/fixtures/rejected/"
                              (p14-conformance-rejected-fixture-names document))
       :artifact-evidence :conformance-system
       :validation-command "clojure -M:test"
       :status :complete}}
     summary)))

(defn p14-conformance-document-records
  []
  (into {} (map (fn [document]
                  [document (p14-conformance-document-record document)])
                p14-conformance-documents)))