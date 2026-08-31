

(defn stage1-reader-runtime-image-proof
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:stage1-reader-runtime-image-diagnostic-stream
                           :diagnostics])))
        runtime-image (:stage1-reader-runtime-image artifact)
        runtime-entrypoint (:stage1-reader-runtime-entrypoint artifact)
        character-stream (:stage1-reader-character-stream artifact)
        token-stream (:stage1-reader-token-stream artifact)
        records (:stage1-reader-records artifact)
        operation-names (set (:runtime-image-operations runtime-image))
        direct-stages (mapv :op (:direct-stages runtime-image))
        gravity-runtimes (set (:gravity-runtimes artifact))
        gravity-executors (set (:gravity-executors artifact))]
    {:gravity-reader-runtime-image-verified?
     (= stage1-reader-runtime-image-entrypoint
        (:gravity-entrypoint artifact))
     :gravity-reader-source-verified?
     (= :complete (get-in artifact
                          [:stage1-bootstrap-source-artifact
                           :capability-based-proof
                           :status]))
     :runtime-image-authored?
     (and (= :gravity-reader-runtime-image-v1
             (:engine runtime-image))
          (= :gravity-source
             (get-in runtime-image [:provenance :owner]))
          (= :reader-runtime-image-os-boundary-replacement
             (get-in runtime-image [:provenance :purpose])))
     :runtime-image-direct-stages-covered?
     (= [:stage1-runtime-image-load
         :stage1-runtime-image-install-entrypoint
         :stage1-runtime-image-mount-source
         :stage1-runtime-image-execute-entrypoint
         :stage1-runtime-image-route-stdout
         :stage1-runtime-image-emit-artifact
         :stage1-runtime-image-record-machine-boundary]
        direct-stages)
     :runtime-image-operations-covered?
     (set/subset?
      (set stage1-reader-runtime-image-required-operations)
      operation-names)
     :runtime-image-links-entrypoint?
     (= :stage1-reader-runtime-entrypoint
        (:runtime-entrypoint runtime-image))
     :runtime-entrypoint-authored?
     (= :gravity-reader-runtime-entrypoint-v1 (:engine runtime-entrypoint))
     :artifact-routing-covered?
     (= :gravity/stage1-reader-runtime-image-artifact
        (:artifact runtime-image)
        (:kind artifact))
     :diagnostic-stream-routing-covered?
     (= :gravity/stage1-reader-runtime-image-diagnostic-stream
        (:diagnostic-stream runtime-image)
        (get-in artifact
                [:stage1-reader-runtime-image-diagnostic-stream
                 :artifact]))
     :os-process-boundary-replaced?
     (false? (get-in artifact [:trusted-boundary
                               :os-process-boundary?]))
     :os-filesystem-read-boundary-replaced?
     (false? (get-in artifact [:trusted-boundary
                               :os-filesystem-read-boundary?]))
     :stdout-boundary-replaced?
     (false? (get-in artifact [:trusted-boundary
                               :stdout-boundary?]))
     :machine-boundaries-explicit?
     (and (true? (get-in artifact [:trusted-boundary
                                   :machine-boundary?]))
          (true? (get-in artifact [:trusted-boundary
                                   :kernel-process-scheduler-boundary?]))
          (true? (get-in artifact [:trusted-boundary
                                   :artifact-loader-boundary?])))
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
     :replaced-os-boundaries-recorded?
     (= #{:os-process-launch :os-filesystem-read :stdout-stream}
        (set (:replaced-os-boundaries artifact)))
     :gravity-runtimes-covered?
     (set/subset? #{:stage1-reader-runtime-image
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
          (= :gravity-reader-runtime-image-v1
             (:runtime-image-engine character-stream))
          (pos? (:character-count character-stream))
          (= (:character-count character-stream)
             (count (:characters character-stream))))
     :token-stream-covered?
     (and (= :gravity/stage1-reader-token-stream (:kind token-stream))
          (= :gravity-reader-runtime-image-v1
             (:runtime-image-engine token-stream))
          (pos? (:token-count token-stream))
          (= (:token-count token-stream) (count (:tokens token-stream))))
     :form-records-covered?
     (and (seq records)
          (every? #(and (= :gravity-reader-runtime-entrypoint-v1
                           (:runtime-entrypoint-engine %))
                        (= :gravity-reader-runtime-image-v1
                           (:runtime-image-engine %)))
                  records))
     :forms-match-stage0?
     (true? (get-in artifact [:stage0-comparison :forms-equal?]))
     :source-spans-covered?
     (every? #(get-in % [:span :source]) records)
     :diagnostics-covered?
     (set/subset?
      (set (concat stage1-reader-runtime-image-diagnostic-ids
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
      :machine-boundary? true
      :kernel-process-scheduler-boundary? true
      :artifact-loader-boundary? true
      :clojure-seed-retired? false
      :next-required-capability
      :replace-machine-kernel-and-artifact-loader-boundaries-with-verified-boot-chain}
     :status :complete}))