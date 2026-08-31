(defn- semantic-mid-diverse-bootstrap-artifact-base
  [{:keys [source-path stage1-bootstrap-artifact stage1-records trace-value
           comparison self-hosted-runtime core-bootstrap-runtime
           core-bootstrap-builtins compiler-driver runtime-entrypoint
           runtime-image boot-chain diverse-verification source-runtime
           character-stream token-classifier token-realizer token-automaton
           token-automaton-executor form-builder form-builder-executor
           token-stream gravity-runtimes gravity-executors host-primitives
           seed-builtin-fallbacks seed-orchestration-fallbacks
           runner-fallbacks os-boundaries replaced-os-boundaries
           machine-boundaries replaced-machine-boundaries
           trust-anchor-boundaries replaced-trust-anchor-boundaries
           residual-trust-boundaries image-fallbacks boot-chain-fallbacks
           diverse-verification-fallbacks independent-toolchains
           bootstrap-trace-comparisons reproducible-build-evidence
           independent-audit-record]}]
  (let [artifact-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:reader-source stage1-reader-source-path
                       :entrypoint
                       stage1-reader-diverse-bootstrap-verification-entrypoint
                       :diverse-bootstrap-verification diverse-verification
                       :verified-boot-chain boot-chain
                       :runtime-image runtime-image
                       :runtime-entrypoint runtime-entrypoint
                       :compiler-driver compiler-driver
                       :core-bootstrap-runtime core-bootstrap-runtime
                       :core-bootstrap-builtins core-bootstrap-builtins})))]
    {:kind :gravity/stage1-reader-diverse-bootstrap-verification-artifact
     :phase "15"
     :task "P15-S20"
     :stage :stage1-reader-diverse-bootstrap-verification
     :source-path source-path
     :reader-source-path stage1-reader-source-path
     :gravity-entrypoint
     stage1-reader-diverse-bootstrap-verification-entrypoint
     :diverse-bootstrap-verification-artifact-id artifact-id
     :reader-diverse-bootstrap-verification-id
     (:diverse-bootstrap-verification-id diverse-verification)
     :reader-verified-boot-chain-id (:verified-boot-chain-id boot-chain)
     :reader-runtime-image-id (:runtime-image-id runtime-image)
     :reader-runtime-entrypoint-id (:runtime-entrypoint-id runtime-entrypoint)
     :reader-compiler-driver-id (:compiler-driver-id compiler-driver)
     :reader-core-bootstrap-runtime-id
     (:core-bootstrap-runtime-id core-bootstrap-runtime)
     :reader-core-bootstrap-builtins-id
     (:core-bootstrap-builtins-id core-bootstrap-builtins)
     :reader-self-hosted-runtime-id
     (:self-hosted-runtime-id self-hosted-runtime)
     :host-primitives host-primitives
     :seed-builtin-fallbacks seed-builtin-fallbacks
     :seed-orchestration-fallbacks seed-orchestration-fallbacks
     :runner-fallbacks runner-fallbacks
     :os-boundaries os-boundaries
     :replaced-os-boundaries replaced-os-boundaries
     :machine-boundaries machine-boundaries
     :replaced-machine-boundaries replaced-machine-boundaries
     :trust-anchor-boundaries trust-anchor-boundaries
     :replaced-trust-anchor-boundaries replaced-trust-anchor-boundaries
     :residual-trust-boundaries residual-trust-boundaries
     :image-fallbacks image-fallbacks
     :boot-chain-fallbacks boot-chain-fallbacks
     :diverse-verification-fallbacks diverse-verification-fallbacks
     :independent-toolchains independent-toolchains
     :bootstrap-trace-comparisons bootstrap-trace-comparisons
     :reproducible-build-evidence reproducible-build-evidence
     :independent-audit-record independent-audit-record
     :gravity-runtimes gravity-runtimes
     :gravity-executors gravity-executors
     :trusted-boundary
     {:clojure-runtime-interpreter? false
      :clojure-instruction-executor? false
      :clojure-binary-runner? false
      :clojure-character-stream-implementation? false
      :clojure-seed-builtins? false
      :clojure-seed-orchestration? false
      :clojure-driver-runner? false
      :host-command-invocation? false
      :host-file-read? false
      :os-process-boundary? false
      :os-filesystem-read-boundary? false
      :stdout-boundary? false
      :machine-boundary? false
      :kernel-process-scheduler-boundary? false
      :artifact-loader-boundary? false
      :hardware-reset-vector-boundary? false
      :firmware-root-of-trust-boundary? false
      :external-auditor-key-boundary? false
      :physical-device-manufacturing-boundary? true
      :supply-chain-custody-boundary? true
      :independent-diversity-review-boundary? true}
     :stage1-bootstrap-source-artifact stage1-bootstrap-artifact
     :stage1-reader-diverse-bootstrap-verification diverse-verification
     :stage1-reader-verified-boot-chain boot-chain
     :stage1-reader-runtime-image runtime-image
     :stage1-reader-runtime-entrypoint runtime-entrypoint
     :stage1-reader-compiler-driver compiler-driver
     :stage1-reader-core-bootstrap-runtime core-bootstrap-runtime
     :stage1-reader-core-bootstrap-builtins core-bootstrap-builtins
     :stage1-reader-self-hosted-runtime self-hosted-runtime
     :stage1-reader-source-runtime source-runtime
     :stage1-reader-character-stream character-stream
     :stage1-reader-token-classifier token-classifier
     :stage1-reader-token-realizer token-realizer
     :stage1-reader-token-automaton token-automaton
     :stage1-reader-token-automaton-executor token-automaton-executor
     :stage1-reader-form-builder form-builder
     :stage1-reader-form-builder-executor form-builder-executor
     :stage1-reader-token-stream token-stream
     :stage1-reader-records stage1-records
     :stage1-reader-diverse-bootstrap-verification-trace
     (dissoc trace-value :character-stream :token-stream
             :token-classifier :token-realizer :token-automaton
             :token-automaton-executor :form-builder
             :form-builder-executor :source-runtime
             :self-hosted-reader-runtime :core-bootstrap-runtime
             :core-bootstrap-builtins :compiler-driver
             :runtime-entrypoint :runtime-image :verified-boot-chain
             :diverse-bootstrap-verification)
     :stage0-comparison comparison
     :accepted-stage1-reader-diverse-bootstrap-verification-fixtures
     [{:fixture
       "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
       :status :accepted
       :comparison comparison
       :character-count (:character-count character-stream)
       :token-count (:token-count token-stream)
       :form-count (count stage1-records)
       :diverse-bootstrap-verification-id
       (:diverse-bootstrap-verification-id diverse-verification)}]
     :rejected-stage1-reader-diverse-bootstrap-verification-fixtures
     stage1-reader-diverse-bootstrap-verification-rejected-fixture-records
     :stage1-reader-diverse-bootstrap-verification-diagnostic-stream
     (stage1-reader-diverse-bootstrap-verification-diagnostic-stream
      source-path (:diverse-bootstrap-verification-id diverse-verification))
     :stage1-reader-diverse-bootstrap-verification-results
     {:accepted-fixtures 1
      :rejected-fixtures
      (count stage1-reader-diverse-bootstrap-verification-rejected-fixture-records)
      :diagnostic-count
      (+ (count stage1-reader-diverse-bootstrap-verification-diagnostic-ids)
         (dec (count stage1-reader-execution-diagnostic-ids)))
      :character-count (:character-count character-stream)
      :token-count (:token-count token-stream)
      :form-count (count stage1-records)
      :status :complete}
     :diagnostics []}))
