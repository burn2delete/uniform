

(defn stage1-reader-execute-compiler-driver-pipeline
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
         reader-source-path definitions)]
    (when-not (stage1-reader-compiler-driver-entrypoint-valid?
               definitions)
      (stage1-reader-compiler-driver-fail!
       "STAGE1DRV001" reader-source-path
       stage1-reader-compiler-driver-entrypoint
       {:missing-fields [stage1-reader-compiler-driver-entrypoint]}))
    {:records
     (stage1-reader-execute-compiler-driver
      reader-source-path
      definitions
      source-path
      source-text
      self-hosted-runtime
      core-bootstrap-runtime
      core-bootstrap-builtins
      compiler-driver)
     :self-hosted-runtime self-hosted-runtime
     :core-bootstrap-runtime core-bootstrap-runtime
     :core-bootstrap-builtins core-bootstrap-builtins
     :compiler-driver compiler-driver}))

(defn stage1-reader-compiler-driver-diagnostic-stream
  [source-path compiler-driver-id]
  {:artifact :gravity/stage1-reader-compiler-driver-diagnostic-stream
   :stage :stage1-reader-compiler-driver
   :source-path source-path
   :compiler-driver-id compiler-driver-id
   :diagnostics
   (mapv (fn [id]
           {:diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :stage1-reader-compiler-driver
            :artifact :gravity/diagnostic
            :message
            (or (stage1-reader-compiler-driver-diagnostic-messages id)
                (stage1-reader-execution-diagnostic-messages id))})
         (concat stage1-reader-compiler-driver-diagnostic-ids
                 (butlast stage1-reader-execution-diagnostic-ids)))
   :status :complete})

(defn stage1-reader-compiler-driver-proof
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:stage1-reader-compiler-driver-diagnostic-stream
                           :diagnostics])))
        driver (:stage1-reader-compiler-driver artifact)
        runtime (:stage1-reader-core-bootstrap-runtime artifact)
        builtins (:stage1-reader-core-bootstrap-builtins artifact)
        character-stream (:stage1-reader-character-stream artifact)
        token-stream (:stage1-reader-token-stream artifact)
        records (:stage1-reader-records artifact)
        operation-names (set (:driver-operations driver))
        direct-stages (mapv :op (:direct-stages driver))
        gravity-runtimes (set (:gravity-runtimes artifact))
        gravity-executors (set (:gravity-executors artifact))]
    {:gravity-reader-compiler-driver-entrypoint-verified?
     (= stage1-reader-compiler-driver-entrypoint
        (:gravity-entrypoint artifact))
     :gravity-reader-source-verified?
     (= :complete (get-in artifact
                          [:stage1-bootstrap-source-artifact
                           :capability-based-proof
                           :status]))
     :compiler-driver-authored?
     (and (= :gravity-reader-compiler-driver-v1 (:engine driver))
          (= :gravity-source (get-in driver [:provenance :owner]))
          (= :reader-compiler-driver-orchestration-replacement
             (get-in driver [:provenance :purpose])))
     :compiler-driver-direct-stages-covered?
     (= [:stage1-driver-load-reader-source
         :stage1-driver-resolve-entrypoint
         :stage1-driver-execute-core-bootstrap-runtime
         :stage1-driver-emit-diagnostic-stream
         :stage1-driver-emit-proof-artifact
         :stage1-driver-record-provenance]
        direct-stages)
     :compiler-driver-operations-covered?
     (set/subset?
      (set stage1-reader-compiler-driver-required-operations)
      operation-names)
     :compiler-driver-links-runtime?
     (= :stage1-reader-core-bootstrap-runtime
        (:base-runtime driver))
     :compiler-driver-links-builtins?
     (= :stage1-reader-core-bootstrap-builtins
        (:core-bootstrap-builtins driver))
     :artifact-routing-covered?
     (= :gravity/stage1-reader-compiler-driver-artifact
        (:artifact driver)
        (:kind artifact))
     :diagnostic-stream-routing-covered?
     (= :gravity/stage1-reader-compiler-driver-diagnostic-stream
        (:diagnostic-stream driver)
        (get-in artifact
                [:stage1-reader-compiler-driver-diagnostic-stream
                 :artifact]))
     :core-bootstrap-runtime-authored?
     (= :gravity-reader-core-bootstrap-runtime-v1 (:engine runtime))
     :core-bootstrap-builtins-authored?
     (= :gravity-core-bootstrap-builtins-v1 (:engine builtins))
     :seed-builtins-replaced?
     (false? (get-in artifact [:trusted-boundary
                               :clojure-seed-builtins?]))
     :seed-orchestration-replaced?
     (false? (get-in artifact [:trusted-boundary
                               :clojure-seed-orchestration?]))
     :clojure-driver-runner-boundary-explicit?
     (true? (get-in artifact [:trusted-boundary
                              :clojure-driver-runner?]))
     :host-command-boundary-explicit?
     (true? (get-in artifact [:trusted-boundary
                              :host-command-invocation?]))
     :host-file-read-boundary-explicit?
     (true? (get-in artifact [:trusted-boundary
                              :host-file-read?]))
     :host-primitive-boundary-empty?
     (= [] (:host-primitives artifact))
     :host-fallbacks-empty?
     (= [] (get-in artifact [:stage1-reader-core-bootstrap-builtins
                             :host-fallbacks]))
     :seed-builtin-fallbacks-empty?
     (= [] (:seed-builtin-fallbacks artifact))
     :seed-orchestration-fallbacks-empty?
     (= [] (:seed-orchestration-fallbacks artifact))
     :gravity-runtimes-covered?
     (set/subset? #{:stage1-reader-compiler-driver
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
          (= :gravity-reader-compiler-driver-v1
             (:compiler-driver-engine character-stream))
          (pos? (:character-count character-stream))
          (= (:character-count character-stream)
             (count (:characters character-stream))))
     :token-stream-covered?
     (and (= :gravity/stage1-reader-token-stream (:kind token-stream))
          (= :gravity-reader-compiler-driver-v1
             (:compiler-driver-engine token-stream))
          (pos? (:token-count token-stream))
          (= (:token-count token-stream) (count (:tokens token-stream))))
     :form-records-covered?
     (and (seq records)
          (every? #(and (= :gravity-core-bootstrap-builtins-v1
                           (:core-bootstrap-builtins-engine %))
                        (= :gravity-reader-compiler-driver-v1
                           (:compiler-driver-engine %)))
                  records))
     :forms-match-stage0?
     (true? (get-in artifact [:stage0-comparison :forms-equal?]))
     :source-spans-covered?
     (every? #(get-in % [:span :source]) records)
     :diagnostics-covered?
     (set/subset?
      (set (concat stage1-reader-compiler-driver-diagnostic-ids
                   (butlast stage1-reader-execution-diagnostic-ids)))
      diagnostics)
     :limitations
     {:clojure-runtime-interpreter? false
      :clojure-instruction-executor? false
      :clojure-binary-runner? false
      :clojure-character-stream-implementation? false
      :clojure-seed-builtins? false
      :clojure-seed-orchestration? false
      :clojure-driver-runner? true
      :host-command-invocation? true
      :host-file-read? true
      :clojure-seed-retired? false
      :next-required-capability
      :replace-clojure-driver-runner-and-host-io-with-gravity-runtime-entrypoint}
     :status :complete}))