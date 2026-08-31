

(defn stage1-reader-compiler-driver-source-artifact
  [source-path source-text]
  (let [stage1-bootstrap-artifact
        (stage1-bootstrap-source-artifact stage1-reader-bootstrap-source-root)
        trace (atom {})
        runtime-result
        (binding [*stage1-reader-pipeline-trace* trace]
          (stage1-reader-execute-compiler-driver-pipeline
           stage1-reader-source-path source-path source-text))
        stage1-records (:records runtime-result)
        trace-value @trace
        self-hosted-runtime (:self-hosted-runtime runtime-result)
        core-bootstrap-runtime (:core-bootstrap-runtime runtime-result)
        core-bootstrap-builtins (:core-bootstrap-builtins runtime-result)
        compiler-driver (:compiler-driver runtime-result)
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
        host-command-boundaries
        (vec (distinct (:host-command-boundaries trace-value)))
        stage0-records (read-source-form-records source-path source-text)
        stage1-forms (mapv :form stage1-records)
        stage0-forms (mapv :form stage0-records)
        comparison {:forms-equal? (= stage1-forms stage0-forms)
                    :top-level-count-matches? (= (count stage1-records)
                                                 (count stage0-records))
                    :stage1-form-count (count stage1-records)
                    :stage0-form-count (count stage0-records)}
        compiler-driver-artifact-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:reader-source stage1-reader-source-path
                       :entrypoint
                       stage1-reader-compiler-driver-entrypoint
                       :compiler-driver compiler-driver
                       :core-bootstrap-runtime
                       core-bootstrap-runtime
                       :core-bootstrap-builtins
                       core-bootstrap-builtins})))]
    (let [artifact-base
          {:kind :gravity/stage1-reader-compiler-driver-artifact
           :phase "15"
           :task "P15-S16"
           :stage :stage1-reader-compiler-driver
           :source-path source-path
           :reader-source-path stage1-reader-source-path
           :gravity-entrypoint stage1-reader-compiler-driver-entrypoint
           :compiler-driver-artifact-id compiler-driver-artifact-id
           :reader-compiler-driver-id
           (:compiler-driver-id compiler-driver)
           :reader-core-bootstrap-runtime-id
           (:core-bootstrap-runtime-id core-bootstrap-runtime)
           :reader-core-bootstrap-builtins-id
           (:core-bootstrap-builtins-id core-bootstrap-builtins)
           :reader-self-hosted-runtime-id
           (:self-hosted-runtime-id self-hosted-runtime)
           :host-primitives host-primitives
           :seed-builtin-fallbacks seed-builtin-fallbacks
           :seed-orchestration-fallbacks
           seed-orchestration-fallbacks
           :host-command-boundaries host-command-boundaries
           :gravity-runtimes gravity-runtimes
           :gravity-executors gravity-executors
           :trusted-boundary
           {:clojure-runtime-interpreter? false
            :clojure-instruction-executor? false
            :clojure-binary-runner? false
            :clojure-character-stream-implementation? false
            :clojure-seed-builtins? false
            :clojure-seed-orchestration? false
            :clojure-driver-runner? true
            :host-command-invocation? true
            :host-file-read? true}
           :stage1-bootstrap-source-artifact stage1-bootstrap-artifact
           :stage1-reader-compiler-driver compiler-driver
           :stage1-reader-core-bootstrap-runtime
           core-bootstrap-runtime
           :stage1-reader-core-bootstrap-builtins
           core-bootstrap-builtins
           :stage1-reader-self-hosted-runtime self-hosted-runtime
           :stage1-reader-source-runtime source-runtime
           :stage1-reader-character-stream character-stream
           :stage1-reader-token-classifier token-classifier
           :stage1-reader-token-realizer token-realizer
           :stage1-reader-token-automaton token-automaton
           :stage1-reader-token-automaton-executor
           token-automaton-executor
           :stage1-reader-form-builder form-builder
           :stage1-reader-form-builder-executor form-builder-executor
           :stage1-reader-token-stream token-stream
           :stage1-reader-records stage1-records
           :stage1-reader-compiler-driver-trace
           (dissoc trace-value :character-stream :token-stream
                   :token-classifier :token-realizer :token-automaton
                   :token-automaton-executor :form-builder
                   :form-builder-executor :source-runtime
                   :self-hosted-reader-runtime
                   :core-bootstrap-runtime
                   :core-bootstrap-builtins
                   :compiler-driver)
           :stage0-comparison comparison
           :accepted-stage1-reader-compiler-driver-fixtures
           [{:fixture
             "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
             :status :accepted
             :comparison comparison
             :character-count (:character-count character-stream)
             :token-count (:token-count token-stream)
             :form-count (count stage1-records)
             :compiler-driver-id (:compiler-driver-id compiler-driver)}]
           :rejected-stage1-reader-compiler-driver-fixtures
           stage1-reader-compiler-driver-rejected-fixture-records
           :stage1-reader-compiler-driver-diagnostic-stream
           (stage1-reader-compiler-driver-diagnostic-stream
            source-path
            (:compiler-driver-id compiler-driver))
           :stage1-reader-compiler-driver-results
           {:accepted-fixtures 1
            :rejected-fixtures
            (count stage1-reader-compiler-driver-rejected-fixture-records)
            :diagnostic-count
            (+ (count stage1-reader-compiler-driver-diagnostic-ids)
               (dec (count stage1-reader-execution-diagnostic-ids)))
            :character-count (:character-count character-stream)
            :token-count (:token-count token-stream)
            :form-count (count stage1-records)
            :status :complete}
           :diagnostics []}]
      (when-not (:forms-equal? comparison)
        (stage1-reader-compiler-driver-fail!
         "STAGE1DRV004" source-path comparison
         {:missing-fields [:stage0-form-parity]}))
      (when-not (= (:artifact compiler-driver) (:kind artifact-base))
        (stage1-reader-compiler-driver-fail!
         "STAGE1DRV004" source-path artifact-base
         {:missing-fields [:artifact]}))
      (when-not (= (:diagnostic-stream compiler-driver)
                   (get-in artifact-base
                           [:stage1-reader-compiler-driver-diagnostic-stream
                            :artifact]))
        (stage1-reader-compiler-driver-fail!
         "STAGE1DRV005" source-path artifact-base
         {:missing-fields [:diagnostic-stream]}))
      (when (seq host-primitives)
        (stage1-reader-compiler-driver-fail!
         "STAGE1DRV006" source-path trace-value
         {:host-primitives host-primitives}))
      (when (seq seed-builtin-fallbacks)
        (stage1-reader-compiler-driver-fail!
         "STAGE1DRV006" source-path trace-value
         {:seed-builtin-fallbacks seed-builtin-fallbacks}))
      (when (seq seed-orchestration-fallbacks)
        (stage1-reader-compiler-driver-fail!
         "STAGE1DRV006" source-path trace-value
         {:seed-orchestration-fallbacks
          seed-orchestration-fallbacks}))
      (when (seq (:seed-orchestration-fallbacks compiler-driver))
        (stage1-reader-compiler-driver-fail!
         "STAGE1DRV006" source-path compiler-driver
         {:seed-orchestration-fallbacks
          (:seed-orchestration-fallbacks compiler-driver)}))
      (when-not (and (false? (get-in artifact-base
                                     [:trusted-boundary
                                      :clojure-runtime-interpreter?]))
                     (false? (get-in artifact-base
                                     [:trusted-boundary
                                      :clojure-instruction-executor?]))
                     (false? (get-in artifact-base
                                     [:trusted-boundary
                                      :clojure-binary-runner?]))
                     (false? (get-in artifact-base
                                     [:trusted-boundary
                                      :clojure-character-stream-implementation?]))
                     (false? (get-in artifact-base
                                     [:trusted-boundary
                                      :clojure-seed-builtins?]))
                     (false? (get-in artifact-base
                                     [:trusted-boundary
                                      :clojure-seed-orchestration?]))
                     (true? (get-in artifact-base
                                    [:trusted-boundary
                                     :clojure-driver-runner?]))
                     (true? (get-in artifact-base
                                    [:trusted-boundary
                                     :host-command-invocation?]))
                     (true? (get-in artifact-base
                                    [:trusted-boundary
                                     :host-file-read?])))
        (stage1-reader-compiler-driver-fail!
         "STAGE1DRV007" source-path artifact-base
         {:missing-fields [:trusted-boundary]}))
      (let [capability-proof
            (stage1-reader-compiler-driver-proof artifact-base)]
        (assoc artifact-base
               :capability-based-proof capability-proof
               :artifact-id (c4-artifact-id (assoc artifact-base
                                                   :capability-based-proof
                                                   capability-proof)))))))

(defn stage1-reader-compiler-driver-file-artifact
  [path]
  (stage1-reader-compiler-driver-source-artifact path (slurp path)))

(def stage1-reader-runtime-entrypoint-required-operations
  [:decode-command
   :open-source-file
   :deliver-source-bytes
   :execute-compiler-driver
   :route-artifact-output
   :map-process-exit
   :record-os-boundary])

(defn stage1-reader-runtime-entrypoint-literal-definition-value
  [reader-source-path definitions symbol-name]
  (let [definition (get definitions symbol-name)]
    (when-not (= :def (:kind definition))
      (stage1-reader-runtime-entrypoint-fail!
       "STAGE1RTE003" reader-source-path symbol-name
       {:missing-fields [symbol-name]}))
    (:value-form definition)))