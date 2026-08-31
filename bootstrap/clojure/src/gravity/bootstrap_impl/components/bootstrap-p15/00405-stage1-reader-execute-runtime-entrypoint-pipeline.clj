

(defn stage1-reader-execute-runtime-entrypoint-pipeline
  [reader-source-path source-path source-text]
  (let [forms (stage1-reader-source-forms reader-source-path)
        definitions (stage1-reader-definition-map reader-source-path forms)
        self-hosted-runtime
        (stage1-reader-self-hosted-runtime-from-definitions
         reader-source-path definitions)
        core-bootstrap-builtins
        (stage1-reader-core-bootstrap-builtins-from-definitions
         reader-source-path definitions)
        core-bootstrap-runtime
        (stage1-reader-core-bootstrap-runtime-from-definitions
         reader-source-path definitions)
        compiler-driver
        (stage1-reader-compiler-driver-from-definitions
         reader-source-path definitions)
        runtime-entrypoint
        (stage1-reader-runtime-entrypoint-from-definitions
         reader-source-path definitions)]
    (when-not (stage1-reader-runtime-entrypoint-entrypoint-valid?
               definitions)
      (stage1-reader-runtime-entrypoint-fail!
       "STAGE1RTE001" reader-source-path
       stage1-reader-runtime-entrypoint-entrypoint
       {:missing-fields [stage1-reader-runtime-entrypoint-entrypoint]}))
    {:records
     (stage1-reader-execute-runtime-entrypoint
      reader-source-path
      definitions
      source-path
      source-text
      self-hosted-runtime
      core-bootstrap-runtime
      core-bootstrap-builtins
      compiler-driver
      runtime-entrypoint)
     :self-hosted-runtime self-hosted-runtime
     :core-bootstrap-runtime core-bootstrap-runtime
     :core-bootstrap-builtins core-bootstrap-builtins
     :compiler-driver compiler-driver
     :runtime-entrypoint runtime-entrypoint}))

(defn stage1-reader-runtime-entrypoint-diagnostic-stream
  [source-path runtime-entrypoint-id]
  {:artifact :gravity/stage1-reader-runtime-entrypoint-diagnostic-stream
   :stage :stage1-reader-runtime-entrypoint
   :source-path source-path
   :runtime-entrypoint-id runtime-entrypoint-id
   :diagnostics
   (mapv (fn [id]
           {:diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :stage1-reader-runtime-entrypoint
            :artifact :gravity/diagnostic
            :message
            (or (stage1-reader-runtime-entrypoint-diagnostic-messages id)
                (stage1-reader-execution-diagnostic-messages id))})
         (concat stage1-reader-runtime-entrypoint-diagnostic-ids
                 (butlast stage1-reader-execution-diagnostic-ids)))
   :status :complete})

(defn stage1-reader-runtime-entrypoint-proof
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:stage1-reader-runtime-entrypoint-diagnostic-stream
                           :diagnostics])))
        runtime-entrypoint (:stage1-reader-runtime-entrypoint artifact)
        compiler-driver (:stage1-reader-compiler-driver artifact)
        character-stream (:stage1-reader-character-stream artifact)
        token-stream (:stage1-reader-token-stream artifact)
        records (:stage1-reader-records artifact)
        operation-names (set (:entrypoint-operations runtime-entrypoint))
        direct-stages (mapv :op (:direct-stages runtime-entrypoint))
        gravity-runtimes (set (:gravity-runtimes artifact))
        gravity-executors (set (:gravity-executors artifact))]
    {:gravity-reader-runtime-entrypoint-verified?
     (= stage1-reader-runtime-entrypoint-entrypoint
        (:gravity-entrypoint artifact))
     :gravity-reader-source-verified?
     (= :complete (get-in artifact
                          [:stage1-bootstrap-source-artifact
                           :capability-based-proof
                           :status]))
     :runtime-entrypoint-authored?
     (and (= :gravity-reader-runtime-entrypoint-v1
             (:engine runtime-entrypoint))
          (= :gravity-source
             (get-in runtime-entrypoint [:provenance :owner]))
          (= :reader-runtime-entrypoint-host-runner-replacement
             (get-in runtime-entrypoint [:provenance :purpose])))
     :runtime-entrypoint-direct-stages-covered?
     (= [:stage1-runtime-entrypoint-decode-command
         :stage1-runtime-entrypoint-open-source
         :stage1-runtime-entrypoint-deliver-source
         :stage1-runtime-entrypoint-execute-driver
         :stage1-runtime-entrypoint-route-artifact
         :stage1-runtime-entrypoint-map-exit]
        direct-stages)
     :runtime-entrypoint-operations-covered?
     (set/subset?
      (set stage1-reader-runtime-entrypoint-required-operations)
      operation-names)
     :runtime-entrypoint-links-driver?
     (= :stage1-reader-compiler-driver
        (:compiler-driver runtime-entrypoint))
     :compiler-driver-authored?
     (= :gravity-reader-compiler-driver-v1 (:engine compiler-driver))
     :artifact-routing-covered?
     (= :gravity/stage1-reader-runtime-entrypoint-artifact
        (:artifact runtime-entrypoint)
        (:kind artifact))
     :diagnostic-stream-routing-covered?
     (= :gravity/stage1-reader-runtime-entrypoint-diagnostic-stream
        (:diagnostic-stream runtime-entrypoint)
        (get-in artifact
                [:stage1-reader-runtime-entrypoint-diagnostic-stream
                 :artifact]))
     :seed-builtins-replaced?
     (false? (get-in artifact [:trusted-boundary
                               :clojure-seed-builtins?]))
     :seed-orchestration-replaced?
     (false? (get-in artifact [:trusted-boundary
                               :clojure-seed-orchestration?]))
     :clojure-driver-runner-replaced?
     (false? (get-in artifact [:trusted-boundary
                               :clojure-driver-runner?]))
     :host-command-invocation-replaced?
     (false? (get-in artifact [:trusted-boundary
                               :host-command-invocation?]))
     :host-file-read-replaced?
     (false? (get-in artifact [:trusted-boundary
                               :host-file-read?]))
     :os-process-boundary-explicit?
     (true? (get-in artifact [:trusted-boundary
                              :os-process-boundary?]))
     :os-filesystem-read-boundary-explicit?
     (true? (get-in artifact [:trusted-boundary
                              :os-filesystem-read-boundary?]))
     :stdout-boundary-explicit?
     (true? (get-in artifact [:trusted-boundary
                              :stdout-boundary?]))
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
     :gravity-runtimes-covered?
     (set/subset? #{:stage1-reader-runtime-entrypoint
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
          (= :gravity-reader-runtime-entrypoint-v1
             (:runtime-entrypoint-engine character-stream))
          (pos? (:character-count character-stream))
          (= (:character-count character-stream)
             (count (:characters character-stream))))
     :token-stream-covered?
     (and (= :gravity/stage1-reader-token-stream (:kind token-stream))
          (= :gravity-reader-runtime-entrypoint-v1
             (:runtime-entrypoint-engine token-stream))
          (pos? (:token-count token-stream))
          (= (:token-count token-stream) (count (:tokens token-stream))))
     :form-records-covered?
     (and (seq records)
          (every? #(and (= :gravity-reader-compiler-driver-v1
                           (:compiler-driver-engine %))
                        (= :gravity-reader-runtime-entrypoint-v1
                           (:runtime-entrypoint-engine %)))
                  records))
     :forms-match-stage0?
     (true? (get-in artifact [:stage0-comparison :forms-equal?]))
     :source-spans-covered?
     (every? #(get-in % [:span :source]) records)
     :diagnostics-covered?
     (set/subset?
      (set (concat stage1-reader-runtime-entrypoint-diagnostic-ids
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
      :os-process-boundary? true
      :os-filesystem-read-boundary? true
      :stdout-boundary? true
      :clojure-seed-retired? false
      :next-required-capability
      :replace-os-process-filesystem-and-stdout-boundaries-with-bootstrapped-runtime-image}
     :status :complete}))