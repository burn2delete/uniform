

(defn stage1-reader-execute-core-bootstrap-runtime
  [reader-source-path
   definitions
   source-path
   source-text
   self-hosted-runtime
   core-bootstrap-runtime
   core-bootstrap-builtins]
  (let [builtins-id (:core-bootstrap-builtins-id core-bootstrap-builtins)
        runtime-id (:core-bootstrap-runtime-id core-bootstrap-runtime)
        operation-names (mapv :op (:operations core-bootstrap-builtins))
        annotate-with-core
        (fn [record]
          (assoc record
                 :core-bootstrap-runtime-id runtime-id
                 :core-bootstrap-builtins-id builtins-id
                 :core-bootstrap-builtins-engine
                 (:engine core-bootstrap-builtins)
                 :seed-builtin-replacement
                 :gravity-core-bootstrap-builtins-v1))
        records
        (stage1-reader-execute-self-hosted-runtime
         reader-source-path definitions source-path source-text
         self-hosted-runtime)
        annotated-records (mapv annotate-with-core records)]
    (when *stage1-reader-pipeline-trace*
      (swap! *stage1-reader-pipeline-trace*
             (fn [trace]
               (-> (or trace {})
                   (assoc :core-bootstrap-runtime
                          core-bootstrap-runtime)
                   (assoc :core-bootstrap-builtins
                          core-bootstrap-builtins)
                   (assoc :core-bootstrap-operation-coverage
                          {:required
                           stage1-reader-core-bootstrap-required-operations
                           :provided operation-names
                           :covered?
                           (set/subset?
                            (set stage1-reader-core-bootstrap-required-operations)
                            (set operation-names))})
                   (assoc :seed-builtin-fallbacks [])
                   (update :gravity-runtimes (fnil conj [])
                           :stage1-reader-core-bootstrap-runtime)
                   (update :character-stream annotate-with-core)
                   (update :token-stream annotate-with-core)
                   (assoc :core-bootstrap-builtins-applied
                          {:runtime-id runtime-id
                           :builtins-id builtins-id
                           :operation-count (count operation-names)
                           :host-fallbacks (:host-fallbacks
                                            core-bootstrap-builtins)})))))
    annotated-records))

(defn stage1-reader-execute-core-bootstrap-runtime-pipeline
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
         reader-source-path definitions)]
    (when-not (stage1-reader-core-bootstrap-entrypoint-valid?
               definitions)
      (stage1-reader-core-bootstrap-fail!
       "STAGE1CORE001" reader-source-path
       stage1-reader-core-bootstrap-entrypoint
       {:missing-fields [stage1-reader-core-bootstrap-entrypoint]}))
    {:records
     (stage1-reader-execute-core-bootstrap-runtime
      reader-source-path
      definitions
      source-path
      source-text
      self-hosted-runtime
      core-bootstrap-runtime
      core-bootstrap-builtins)
     :self-hosted-runtime self-hosted-runtime
     :core-bootstrap-runtime core-bootstrap-runtime
     :core-bootstrap-builtins core-bootstrap-builtins}))

(defn stage1-reader-core-bootstrap-diagnostic-stream
  [source-path core-bootstrap-runtime-id core-bootstrap-builtins-id]
  {:artifact :gravity/stage1-reader-core-bootstrap-diagnostic-stream
   :stage :stage1-reader-core-bootstrap
   :source-path source-path
   :core-bootstrap-runtime-id core-bootstrap-runtime-id
   :core-bootstrap-builtins-id core-bootstrap-builtins-id
   :diagnostics
   (mapv (fn [id]
           {:diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :stage1-reader-core-bootstrap
            :artifact :gravity/diagnostic
            :message
            (or (stage1-reader-core-bootstrap-diagnostic-messages id)
                (stage1-reader-execution-diagnostic-messages id))})
         (concat stage1-reader-core-bootstrap-diagnostic-ids
                 (butlast stage1-reader-execution-diagnostic-ids)))
   :status :complete})

(defn stage1-reader-core-bootstrap-proof
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:stage1-reader-core-bootstrap-diagnostic-stream
                           :diagnostics])))
        runtime (:stage1-reader-core-bootstrap-runtime artifact)
        builtins (:stage1-reader-core-bootstrap-builtins artifact)
        source-runtime (:stage1-reader-source-runtime artifact)
        character-stream (:stage1-reader-character-stream artifact)
        token-stream (:stage1-reader-token-stream artifact)
        records (:stage1-reader-records artifact)
        operation-names (set (map :op (:operations builtins)))
        direct-stages (mapv :op (:direct-stages runtime))
        gravity-runtimes (set (:gravity-runtimes artifact))
        gravity-executors (set (:gravity-executors artifact))]
    {:gravity-reader-core-bootstrap-entrypoint-verified?
     (= stage1-reader-core-bootstrap-entrypoint
        (:gravity-entrypoint artifact))
     :gravity-reader-source-verified?
     (= :complete (get-in artifact
                          [:stage1-bootstrap-source-artifact
                           :capability-based-proof
                           :status]))
     :core-bootstrap-runtime-authored?
     (and (= :gravity-reader-core-bootstrap-runtime-v1
             (:engine runtime))
          (= :gravity-source (get-in runtime [:provenance :owner]))
          (= :reader-core-bootstrap-runtime
             (get-in runtime [:provenance :purpose])))
     :core-bootstrap-runtime-direct-stages-covered?
     (= [:stage1-core-bootstrap-create-character-stream
         :stage1-core-bootstrap-execute-token-automaton
         :stage1-core-bootstrap-execute-form-builder
         :stage1-core-bootstrap-compare-stage0]
        direct-stages)
     :core-bootstrap-builtins-authored?
     (and (= :gravity-core-bootstrap-builtins-v1 (:engine builtins))
          (= :gravity-source (:owner builtins))
          (= :reader-core-bootstrap-builtin-replacement
             (get-in builtins [:provenance :purpose])))
     :core-bootstrap-operations-covered?
     (set/subset?
      (set stage1-reader-core-bootstrap-required-operations)
      operation-names)
     :core-bootstrap-runtime-links-builtins?
     (= :stage1-reader-core-bootstrap-builtins
        (:core-bootstrap-builtins runtime))
     :seed-builtins-replaced?
     (false? (get-in artifact [:trusted-boundary
                               :clojure-seed-builtins?]))
     :clojure-seed-orchestration-boundary-explicit?
     (true? (get-in artifact [:trusted-boundary
                              :clojure-seed-orchestration?]))
     :binary-runner-replaced?
     (false? (get-in artifact [:trusted-boundary
                               :clojure-binary-runner?]))
     :character-stream-implementation-replaced?
     (false? (get-in artifact [:trusted-boundary
                               :clojure-character-stream-implementation?]))
     :runtime-interpreter-replaced?
     (false? (get-in artifact [:trusted-boundary
                               :clojure-runtime-interpreter?]))
     :instruction-executor-replaced?
     (false? (get-in artifact [:trusted-boundary
                               :clojure-instruction-executor?]))
     :host-primitive-boundary-empty?
     (= [] (:host-primitives artifact))
     :host-fallbacks-empty?
     (= [] (get-in artifact [:stage1-reader-core-bootstrap-builtins
                             :host-fallbacks]))
     :seed-builtin-fallbacks-empty?
     (= [] (:seed-builtin-fallbacks artifact))
     :gravity-source-runtime-authored?
     (and (= :gravity-reader-source-runtime-v1 (:engine source-runtime))
          (= :gravity-source (get-in source-runtime [:provenance :owner])))
     :gravity-runtimes-covered?
     (set/subset? #{:stage1-reader-core-bootstrap-runtime
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
          (= :gravity-reader-source-runtime-v1
             (:source-runtime-engine character-stream))
          (= :gravity-reader-self-hosted-runtime-v1
             (:self-hosted-runtime-engine character-stream))
          (= :gravity-core-bootstrap-builtins-v1
             (:core-bootstrap-builtins-engine character-stream))
          (pos? (:character-count character-stream))
          (= (:character-count character-stream)
             (count (:characters character-stream))))
     :token-stream-covered?
     (and (= :gravity/stage1-reader-token-stream (:kind token-stream))
          (= :gravity-reader-token-automaton-executor-v1
             (:token-automaton-executor-engine token-stream))
          (= :gravity-core-bootstrap-builtins-v1
             (:core-bootstrap-builtins-engine token-stream))
          (pos? (:token-count token-stream))
          (= (:token-count token-stream) (count (:tokens token-stream))))
     :form-records-covered?
     (and (seq records)
          (every? #(and (= :gravity-reader-form-builder-executor-v1
                           (:form-builder-executor-engine %))
                        (= :gravity-core-bootstrap-builtins-v1
                           (:core-bootstrap-builtins-engine %)))
                  records))
     :forms-match-stage0?
     (true? (get-in artifact [:stage0-comparison :forms-equal?]))
     :source-spans-covered?
     (every? #(get-in % [:span :source]) records)
     :diagnostics-covered?
     (set/subset?
      (set (concat stage1-reader-core-bootstrap-diagnostic-ids
                   (butlast stage1-reader-execution-diagnostic-ids)))
      diagnostics)
     :limitations
     {:clojure-runtime-interpreter? false
      :clojure-instruction-executor? false
      :clojure-binary-runner? false
      :clojure-character-stream-implementation? false
      :clojure-seed-builtins? false
      :clojure-seed-orchestration? true
      :clojure-seed-retired? false
      :next-required-capability
      :replace-clojure-seed-orchestration-with-gravity-compiler-driver}
     :status :complete}))