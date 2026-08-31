(defn- semantic-mid-formal-source-artifact-identity
  [{:keys [source-path self-hosted-runtime core-bootstrap-runtime
           core-bootstrap-builtins compiler-driver runtime-entrypoint
           runtime-image boot-chain diverse-verification release-attestation
           formal-governance host-primitives seed-builtin-fallbacks
           seed-orchestration-fallbacks runner-fallbacks os-boundaries
           replaced-os-boundaries machine-boundaries
           replaced-machine-boundaries trust-anchor-boundaries
           replaced-trust-anchor-boundaries physical-release-boundaries
           replaced-physical-release-boundaries residual-trust-boundaries
           residual-release-governance-boundaries
           replaced-release-governance-boundaries image-fallbacks
           boot-chain-fallbacks diverse-verification-fallbacks
           release-attestation-fallbacks
           formal-release-governance-fallbacks independent-toolchains
           bootstrap-trace-comparisons reproducible-build-evidence
           independent-audit-record release-attestation-record
           release-seed-retirement-evidence supply-chain-manifest
           release-custody-record governance-approval-record
           revocation-check-report release-provenance-record
           formal-release-governance-record deployment-custody-record
           self-hosting-evidence formal-seed-retirement-evidence
           formal-tcb-delta-record formal-unsafe-audit-report
           formal-release-provenance-record gravity-runtimes
           gravity-executors]}]
  (let [artifact-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:reader-source stage1-reader-source-path
                       :entrypoint
                       stage1-reader-formal-release-governance-seed-retirement-entrypoint
                       :formal-release-governance formal-governance
                       :release-attestation release-attestation
                       :diverse-bootstrap-verification diverse-verification
                       :verified-boot-chain boot-chain
                       :runtime-image runtime-image
                       :runtime-entrypoint runtime-entrypoint
                       :compiler-driver compiler-driver
                       :core-bootstrap-runtime core-bootstrap-runtime
                       :core-bootstrap-builtins core-bootstrap-builtins})))]
    {:kind
     :gravity/stage1-reader-formal-release-governance-seed-retirement-artifact
     :phase "15"
     :task "P15-S22"
     :stage :stage1-reader-formal-release-governance-seed-retirement
     :source-path source-path
     :reader-source-path stage1-reader-source-path
     :gravity-entrypoint
     stage1-reader-formal-release-governance-seed-retirement-entrypoint
     :formal-release-governance-seed-retirement-artifact-id artifact-id
     :reader-formal-release-governance-seed-retirement-id
     (:formal-release-governance-seed-retirement-id formal-governance)
     :reader-release-attestation-seed-retirement-id
     (:release-attestation-seed-retirement-id release-attestation)
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
     :physical-release-boundaries physical-release-boundaries
     :replaced-physical-release-boundaries
     replaced-physical-release-boundaries
     :residual-trust-boundaries residual-trust-boundaries
     :residual-release-governance-boundaries
     residual-release-governance-boundaries
     :replaced-release-governance-boundaries
     replaced-release-governance-boundaries
     :image-fallbacks image-fallbacks
     :boot-chain-fallbacks boot-chain-fallbacks
     :diverse-verification-fallbacks diverse-verification-fallbacks
     :release-attestation-fallbacks release-attestation-fallbacks
     :formal-release-governance-fallbacks
     formal-release-governance-fallbacks
     :independent-toolchains independent-toolchains
     :bootstrap-trace-comparisons bootstrap-trace-comparisons
     :reproducible-build-evidence reproducible-build-evidence
     :independent-audit-record independent-audit-record
     :release-attestation-record release-attestation-record
     :release-seed-retirement-evidence release-seed-retirement-evidence
     :supply-chain-manifest supply-chain-manifest
     :release-custody-record release-custody-record
     :governance-approval-record governance-approval-record
     :revocation-check-report revocation-check-report
     :release-provenance-record release-provenance-record
     :formal-release-governance-record formal-release-governance-record
     :deployment-custody-record deployment-custody-record
     :self-hosting-evidence self-hosting-evidence
     :formal-seed-retirement-evidence formal-seed-retirement-evidence
     :formal-tcb-delta-record formal-tcb-delta-record
     :formal-unsafe-audit-report formal-unsafe-audit-report
     :formal-release-provenance-record formal-release-provenance-record
     :clojure-seed-retired?
     (true? (:clojure-seed-retired? self-hosting-evidence))
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
      :physical-device-manufacturing-boundary? false
      :supply-chain-custody-boundary? false
      :independent-diversity-review-boundary? false
      :human-release-governance-boundary? false
      :legal-custody-record-retention-boundary? false
      :deployment-environment-custody-boundary? false
      :clojure-seed-retired? false}}))
