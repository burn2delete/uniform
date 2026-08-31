

(defn stage1-reader-diverse-bootstrap-verification-proof
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:stage1-reader-diverse-bootstrap-verification-diagnostic-stream
                           :diagnostics])))
        diverse-verification
        (:stage1-reader-diverse-bootstrap-verification artifact)
        boot-chain (:stage1-reader-verified-boot-chain artifact)
        character-stream (:stage1-reader-character-stream artifact)
        token-stream (:stage1-reader-token-stream artifact)
        records (:stage1-reader-records artifact)
        operation-names
        (set (:diverse-verification-operations diverse-verification))
        direct-stages (mapv :op (:direct-stages diverse-verification))
        gravity-runtimes (set (:gravity-runtimes artifact))
        gravity-executors (set (:gravity-executors artifact))
        independent-toolchains
        (:independent-toolchains diverse-verification)
        toolchain-identities (set (map :identity independent-toolchains))
        trace-comparisons
        (:bootstrap-trace-comparisons diverse-verification)
        reproducible-evidence
        (:reproducible-build-evidence diverse-verification)
        audit-record (:independent-audit-record diverse-verification)]
    {:gravity-reader-diverse-bootstrap-verification-verified?
     (= stage1-reader-diverse-bootstrap-verification-entrypoint
        (:gravity-entrypoint artifact))
     :gravity-reader-source-verified?
     (= :complete (get-in artifact
                          [:stage1-bootstrap-source-artifact
                           :capability-based-proof
                           :status]))
     :diverse-bootstrap-verification-authored?
     (and (= :gravity-reader-diverse-bootstrap-verification-v1
             (:engine diverse-verification))
          (= :gravity-source
             (get-in diverse-verification [:provenance :owner]))
          (= :reader-diverse-bootstrap-trust-anchor-replacement
             (get-in diverse-verification [:provenance :purpose])))
     :diverse-bootstrap-verification-direct-stages-covered?
     (= [:stage1-diverse-bootstrap-seed-built-rebuild
         :stage1-diverse-bootstrap-self-built-rebuild
         :stage1-diverse-bootstrap-clean-environment-rebuild
         :stage1-diverse-bootstrap-diverse-toolchain-rebuild
         :stage1-diverse-bootstrap-compare-traces
         :stage1-diverse-bootstrap-verify-provenance
         :stage1-diverse-bootstrap-record-independent-audit]
        direct-stages)
     :diverse-bootstrap-verification-operations-covered?
     (set/subset?
      (set stage1-reader-diverse-bootstrap-verification-required-operations)
      operation-names)
     :diverse-bootstrap-links-verified-boot-chain?
     (= :stage1-reader-verified-boot-chain
        (:verified-boot-chain diverse-verification))
     :verified-boot-chain-authored?
     (= :gravity-reader-verified-boot-chain-v1 (:engine boot-chain))
     :artifact-routing-covered?
     (= :gravity/stage1-reader-diverse-bootstrap-verification-artifact
        (:artifact diverse-verification)
        (:kind artifact))
     :diagnostic-stream-routing-covered?
     (= :gravity/stage1-reader-diverse-bootstrap-verification-diagnostic-stream
        (:diagnostic-stream diverse-verification)
        (get-in artifact
                [:stage1-reader-diverse-bootstrap-verification-diagnostic-stream
                 :artifact]))
     :hardware-reset-vector-boundary-replaced?
     (false? (get-in artifact [:trusted-boundary
                               :hardware-reset-vector-boundary?]))
     :firmware-root-of-trust-boundary-replaced?
     (false? (get-in artifact [:trusted-boundary
                               :firmware-root-of-trust-boundary?]))
     :external-auditor-key-boundary-replaced?
     (false? (get-in artifact [:trusted-boundary
                               :external-auditor-key-boundary?]))
     :residual-trust-boundaries-explicit?
     (and (true? (get-in artifact [:trusted-boundary
                                   :physical-device-manufacturing-boundary?]))
          (true? (get-in artifact [:trusted-boundary
                                   :supply-chain-custody-boundary?]))
          (true? (get-in artifact [:trusted-boundary
                                   :independent-diversity-review-boundary?])))
     :independent-toolchains-covered?
     (and (>= (count independent-toolchains) 2)
          (>= (count toolchain-identities) 2))
     :bootstrap-trace-comparisons-covered?
     (and (set/subset? #{:manifest-equivalence
                         :diagnostic-equivalence
                         :stage0-form-parity}
                       (set (map :mode trace-comparisons)))
          (every? #(= :accepted (:status %)) trace-comparisons))
     :reproducible-build-evidence-covered?
     (and (= true (:locked-dependencies reproducible-evidence))
          (= true (:fixed-time reproducible-evidence))
          (= "C" (:locale reproducible-evidence))
          (= :canonical (:filesystem-order reproducible-evidence))
          (= :disabled (:network reproducible-evidence)))
	     :independent-audit-record-covered?
	     (and (= :recorded (:status audit-record))
	          (= :independent-diversity-review (:review-kind audit-record))
	          (some? (:reviewer audit-record)))
     :host-primitive-boundary-empty?
     (= [] (:host-primitives artifact))
     :host-fallbacks-empty?
     (= [] (get-in artifact [:stage1-reader-core-bootstrap-builtins
                             :host-fallbacks]))
     :seed-builtin-fallbacks-empty?
     (= [] (:seed-builtin-fallbacks artifact))
     :seed-orchestration-fallbacks-empty?
     (= [] (:seed-orchestration-fallbacks artifact))
     :runner-fallbacks-empty?
     (= [] (:runner-fallbacks artifact))
     :os-boundaries-empty?
     (= [] (:os-boundaries artifact))
     :image-fallbacks-empty?
     (= [] (:image-fallbacks artifact))
     :machine-boundaries-empty?
     (= [] (:machine-boundaries artifact))
     :boot-chain-fallbacks-empty?
     (= [] (:boot-chain-fallbacks artifact))
     :trust-anchor-boundaries-empty?
     (= [] (:trust-anchor-boundaries artifact))
     :diverse-verification-fallbacks-empty?
     (= [] (:diverse-verification-fallbacks artifact))
     :replaced-trust-anchor-boundaries-recorded?
     (= #{:hardware-reset-vector
          :firmware-root-of-trust
          :external-auditor-key}
        (set (:replaced-trust-anchor-boundaries artifact)))
     :residual-trust-boundaries-recorded?
     (= #{:physical-device-manufacturing
          :supply-chain-custody
          :independent-diversity-review}
        (set (:residual-trust-boundaries artifact)))
     :gravity-runtimes-covered?
     (set/subset? #{:stage1-reader-diverse-bootstrap-verification
                    :stage1-reader-verified-boot-chain
                    :stage1-reader-runtime-image
                    :stage1-reader-runtime-entrypoint
                    :stage1-reader-compiler-driver
                    :stage1-reader-core-bootstrap-runtime
                    :stage1-reader-self-hosted-runtime
                    :stage1-reader-source-runtime}
                  gravity-runtimes)
     :gravity-executors-covered?
     (set/subset? #{:stage1-reader-token-automaton-executor
                    :stage1-reader-form-builder-executor}
                  gravity-executors)
     :character-stream-covered?
     (and (= :gravity/stage1-reader-character-stream
             (:kind character-stream))
          (= :gravity-reader-diverse-bootstrap-verification-v1
             (:diverse-bootstrap-verification-engine character-stream))
          (pos? (:character-count character-stream))
          (= (:character-count character-stream)
             (count (:characters character-stream))))
     :token-stream-covered?
     (and (= :gravity/stage1-reader-token-stream (:kind token-stream))
          (= :gravity-reader-diverse-bootstrap-verification-v1
             (:diverse-bootstrap-verification-engine token-stream))
          (pos? (:token-count token-stream))
          (= (:token-count token-stream) (count (:tokens token-stream))))
     :form-records-covered?
     (and (seq records)
          (every? #(and (= :gravity-reader-verified-boot-chain-v1
                           (:verified-boot-chain-engine %))
                        (= :gravity-reader-diverse-bootstrap-verification-v1
                           (:diverse-bootstrap-verification-engine %)))
                  records))
     :forms-match-stage0?
     (true? (get-in artifact [:stage0-comparison :forms-equal?]))
     :source-spans-covered?
     (every? #(get-in % [:span :source]) records)
     :diagnostics-covered?
     (set/subset?
      (set (concat stage1-reader-diverse-bootstrap-verification-diagnostic-ids
                   (butlast stage1-reader-execution-diagnostic-ids)))
      diagnostics)
     :limitations
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
      :independent-diversity-review-boundary? true
      :clojure-seed-retired? false
      :next-required-capability
      :replace-physical-supply-chain-and-independent-diversity-assumptions-with-release-attestation-and-seed-retirement}
     :status :complete}))