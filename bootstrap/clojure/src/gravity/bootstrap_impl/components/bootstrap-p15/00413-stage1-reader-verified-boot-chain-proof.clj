

(defn stage1-reader-verified-boot-chain-proof
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:stage1-reader-verified-boot-chain-diagnostic-stream
                           :diagnostics])))
        boot-chain (:stage1-reader-verified-boot-chain artifact)
        runtime-image (:stage1-reader-runtime-image artifact)
        character-stream (:stage1-reader-character-stream artifact)
        token-stream (:stage1-reader-token-stream artifact)
        records (:stage1-reader-records artifact)
        operation-names (set (:boot-chain-operations boot-chain))
        direct-stages (mapv :op (:direct-stages boot-chain))
        gravity-runtimes (set (:gravity-runtimes artifact))
        gravity-executors (set (:gravity-executors artifact))]
    {:gravity-reader-verified-boot-chain-verified?
     (= stage1-reader-verified-boot-chain-entrypoint
        (:gravity-entrypoint artifact))
     :gravity-reader-source-verified?
     (= :complete (get-in artifact
                          [:stage1-bootstrap-source-artifact
                           :capability-based-proof
                           :status]))
     :verified-boot-chain-authored?
     (and (= :gravity-reader-verified-boot-chain-v1
             (:engine boot-chain))
          (= :gravity-source
             (get-in boot-chain [:provenance :owner]))
          (= :reader-verified-boot-chain-machine-boundary-replacement
             (get-in boot-chain [:provenance :purpose])))
     :verified-boot-chain-direct-stages-covered?
     (= [:stage1-boot-chain-verify
         :stage1-boot-chain-load-runtime-image
         :stage1-boot-chain-activate-runtime-image
         :stage1-boot-chain-dispatch-machine-instructions
         :stage1-boot-chain-schedule-kernel-process
         :stage1-boot-chain-load-artifact
         :stage1-boot-chain-record-trust-anchor]
        direct-stages)
     :verified-boot-chain-operations-covered?
     (set/subset?
      (set stage1-reader-verified-boot-chain-required-operations)
      operation-names)
     :verified-boot-chain-links-runtime-image?
     (= :stage1-reader-runtime-image
        (:runtime-image boot-chain))
     :runtime-image-authored?
     (= :gravity-reader-runtime-image-v1 (:engine runtime-image))
     :artifact-routing-covered?
     (= :gravity/stage1-reader-verified-boot-chain-artifact
        (:artifact boot-chain)
        (:kind artifact))
     :diagnostic-stream-routing-covered?
     (= :gravity/stage1-reader-verified-boot-chain-diagnostic-stream
        (:diagnostic-stream boot-chain)
        (get-in artifact
                [:stage1-reader-verified-boot-chain-diagnostic-stream
                 :artifact]))
     :machine-boundary-replaced?
     (false? (get-in artifact [:trusted-boundary
                               :machine-boundary?]))
     :kernel-process-scheduler-boundary-replaced?
     (false? (get-in artifact [:trusted-boundary
                               :kernel-process-scheduler-boundary?]))
     :artifact-loader-boundary-replaced?
     (false? (get-in artifact [:trusted-boundary
                               :artifact-loader-boundary?]))
     :trust-anchor-boundaries-explicit?
     (and (true? (get-in artifact [:trusted-boundary
                                   :hardware-reset-vector-boundary?]))
          (true? (get-in artifact [:trusted-boundary
                                   :firmware-root-of-trust-boundary?]))
          (true? (get-in artifact [:trusted-boundary
                                   :external-auditor-key-boundary?])))
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
     :replaced-machine-boundaries-recorded?
     (= #{:machine-instruction-dispatch
          :kernel-process-scheduler
          :artifact-loader}
        (set (:replaced-machine-boundaries artifact)))
     :gravity-runtimes-covered?
     (set/subset? #{:stage1-reader-verified-boot-chain
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
          (= :gravity-reader-verified-boot-chain-v1
             (:verified-boot-chain-engine character-stream))
          (pos? (:character-count character-stream))
          (= (:character-count character-stream)
             (count (:characters character-stream))))
     :token-stream-covered?
     (and (= :gravity/stage1-reader-token-stream (:kind token-stream))
          (= :gravity-reader-verified-boot-chain-v1
             (:verified-boot-chain-engine token-stream))
          (pos? (:token-count token-stream))
          (= (:token-count token-stream) (count (:tokens token-stream))))
     :form-records-covered?
     (and (seq records)
          (every? #(and (= :gravity-reader-runtime-image-v1
                           (:runtime-image-engine %))
                        (= :gravity-reader-verified-boot-chain-v1
                           (:verified-boot-chain-engine %)))
                  records))
     :forms-match-stage0?
     (true? (get-in artifact [:stage0-comparison :forms-equal?]))
     :source-spans-covered?
     (every? #(get-in % [:span :source]) records)
     :diagnostics-covered?
     (set/subset?
      (set (concat stage1-reader-verified-boot-chain-diagnostic-ids
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
      :hardware-reset-vector-boundary? true
      :firmware-root-of-trust-boundary? true
      :external-auditor-key-boundary? true
      :clojure-seed-retired? false
      :next-required-capability
      :replace-hardware-firmware-and-external-trust-anchors-with-diverse-self-hosted-bootstrap-verification}
     :status :complete}))