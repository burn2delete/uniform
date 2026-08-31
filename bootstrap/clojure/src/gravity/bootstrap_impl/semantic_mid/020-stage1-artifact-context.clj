(defn- semantic-mid-stage1-artifact-context
  [source-path source-text execute-pipeline]
  (let [stage1-bootstrap-artifact
        (stage1-bootstrap-source-artifact stage1-reader-bootstrap-source-root)
        trace (atom {})
        runtime-result
        (binding [*stage1-reader-pipeline-trace* trace]
          (execute-pipeline stage1-reader-source-path source-path source-text))
        stage1-records (:records runtime-result)
        trace-value @trace
        self-hosted-runtime (:self-hosted-runtime runtime-result)
        core-bootstrap-runtime (:core-bootstrap-runtime runtime-result)
        core-bootstrap-builtins (:core-bootstrap-builtins runtime-result)
        compiler-driver (:compiler-driver runtime-result)
        runtime-entrypoint (:runtime-entrypoint runtime-result)
        runtime-image (:runtime-image runtime-result)
        boot-chain (:verified-boot-chain runtime-result)
        diverse-verification (:diverse-bootstrap-verification runtime-result)
        release-attestation
        (:release-attestation-seed-retirement runtime-result)
        formal-governance
        (:formal-release-governance-seed-retirement runtime-result)
        source-runtime (:source-runtime trace-value)
        character-stream (:character-stream trace-value)
        token-classifier (:token-classifier trace-value)
        token-realizer (:token-realizer trace-value)
        token-automaton (:token-automaton trace-value)
        token-automaton-executor (:token-automaton-executor trace-value)
        form-builder (:form-builder trace-value)
        form-builder-executor (:form-builder-executor trace-value)
        token-stream (:token-stream trace-value)
        gravity-runtimes (vec (distinct (:gravity-runtimes trace-value)))
        gravity-executors (vec (distinct (:gravity-executors trace-value)))
        host-primitives (vec (distinct (:host-primitives trace-value)))
        seed-builtin-fallbacks
        (vec (distinct (:seed-builtin-fallbacks trace-value)))
        seed-orchestration-fallbacks
        (vec (distinct (:seed-orchestration-fallbacks trace-value)))
        runner-fallbacks (vec (distinct (:runner-fallbacks trace-value)))
        os-boundaries (vec (distinct (:os-boundaries trace-value)))
        replaced-os-boundaries
        (vec (distinct (:replaced-os-boundaries trace-value)))
        machine-boundaries
        (vec (distinct (:machine-boundaries trace-value)))
        replaced-machine-boundaries
        (vec (distinct (:replaced-machine-boundaries trace-value)))
        trust-anchor-boundaries
        (vec (distinct (:trust-anchor-boundaries trace-value)))
        replaced-trust-anchor-boundaries
        (vec (distinct (:replaced-trust-anchor-boundaries trace-value)))
        physical-release-boundaries
        (vec (distinct (:physical-release-boundaries trace-value)))
        replaced-physical-release-boundaries
        (vec (distinct (:replaced-physical-release-boundaries trace-value)))
        residual-trust-boundaries
        (vec (distinct (:residual-trust-boundaries trace-value)))
        residual-release-governance-boundaries
        (vec (distinct (:residual-release-governance-boundaries trace-value)))
        replaced-release-governance-boundaries
        (vec (distinct (:replaced-release-governance-boundaries trace-value)))
        image-fallbacks (vec (distinct (:image-fallbacks trace-value)))
        boot-chain-fallbacks
        (vec (distinct (:boot-chain-fallbacks trace-value)))
        diverse-verification-fallbacks
        (vec (distinct (:diverse-verification-fallbacks trace-value)))
        release-attestation-fallbacks
        (vec (distinct (:release-attestation-fallbacks trace-value)))
        formal-release-governance-fallbacks
        (vec (distinct (:formal-release-governance-fallbacks trace-value)))
        independent-toolchains (:independent-toolchains trace-value)
        bootstrap-trace-comparisons (:bootstrap-trace-comparisons trace-value)
        reproducible-build-evidence (:reproducible-build-evidence trace-value)
        independent-audit-record (:independent-audit-record trace-value)
        release-attestation-record (:release-attestation-record trace-value)
        release-seed-retirement-evidence (:seed-retirement-evidence trace-value)
        supply-chain-manifest (:supply-chain-manifest trace-value)
        release-custody-record (:release-custody-record trace-value)
        governance-approval-record (:governance-approval-record trace-value)
        revocation-check-report (:revocation-check-report trace-value)
        release-provenance-record (:release-provenance-record trace-value)
        formal-release-governance-record
        (:formal-release-governance-record trace-value)
        deployment-custody-record (:deployment-custody-record trace-value)
        self-hosting-evidence (:self-hosting-evidence trace-value)
        formal-seed-retirement-evidence
        (:formal-seed-retirement-evidence trace-value)
        formal-tcb-delta-record (:formal-tcb-delta-record trace-value)
        formal-unsafe-audit-report (:formal-unsafe-audit-report trace-value)
        formal-release-provenance-record
        (:formal-release-provenance-record trace-value)
        stage0-records (read-source-form-records source-path source-text)
        stage1-forms (mapv :form stage1-records)
        stage0-forms (mapv :form stage0-records)
        comparison {:forms-equal? (= stage1-forms stage0-forms)
                    :top-level-count-matches? (= (count stage1-records)
                                                 (count stage0-records))
                    :stage1-form-count (count stage1-records)
                    :stage0-form-count (count stage0-records)}]
    {:source-path source-path :source-text source-text
     :stage1-bootstrap-artifact stage1-bootstrap-artifact
     :runtime-result runtime-result :stage1-records stage1-records
     :trace-value trace-value :comparison comparison
     :self-hosted-runtime self-hosted-runtime
     :core-bootstrap-runtime core-bootstrap-runtime
     :core-bootstrap-builtins core-bootstrap-builtins
     :compiler-driver compiler-driver :runtime-entrypoint runtime-entrypoint
     :runtime-image runtime-image :boot-chain boot-chain
     :diverse-verification diverse-verification
     :release-attestation release-attestation
     :formal-governance formal-governance :source-runtime source-runtime
     :character-stream character-stream :token-classifier token-classifier
     :token-realizer token-realizer :token-automaton token-automaton
     :token-automaton-executor token-automaton-executor
     :form-builder form-builder :form-builder-executor form-builder-executor
     :token-stream token-stream :gravity-runtimes gravity-runtimes
     :gravity-executors gravity-executors :host-primitives host-primitives
     :seed-builtin-fallbacks seed-builtin-fallbacks
     :seed-orchestration-fallbacks seed-orchestration-fallbacks
     :runner-fallbacks runner-fallbacks :os-boundaries os-boundaries
     :replaced-os-boundaries replaced-os-boundaries
     :machine-boundaries machine-boundaries
     :replaced-machine-boundaries replaced-machine-boundaries
     :trust-anchor-boundaries trust-anchor-boundaries
     :replaced-trust-anchor-boundaries replaced-trust-anchor-boundaries
     :physical-release-boundaries physical-release-boundaries
     :replaced-physical-release-boundaries replaced-physical-release-boundaries
     :residual-trust-boundaries residual-trust-boundaries
     :residual-release-governance-boundaries
     residual-release-governance-boundaries
     :replaced-release-governance-boundaries
     replaced-release-governance-boundaries
     :image-fallbacks image-fallbacks :boot-chain-fallbacks boot-chain-fallbacks
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
     :formal-release-provenance-record formal-release-provenance-record}))
