

(defn stage1-reader-runtime-image-source-artifact
  [source-path source-text]
  (let [stage1-bootstrap-artifact
        (stage1-bootstrap-source-artifact stage1-reader-bootstrap-source-root)
        trace (atom {})
        runtime-result
        (binding [*stage1-reader-pipeline-trace* trace]
          (stage1-reader-execute-runtime-image-pipeline
           stage1-reader-source-path source-path source-text))
        stage1-records (:records runtime-result)
        trace-value @trace
        self-hosted-runtime (:self-hosted-runtime runtime-result)
        core-bootstrap-runtime (:core-bootstrap-runtime runtime-result)
        core-bootstrap-builtins (:core-bootstrap-builtins runtime-result)
        compiler-driver (:compiler-driver runtime-result)
        runtime-entrypoint (:runtime-entrypoint runtime-result)
        runtime-image (:runtime-image runtime-result)
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
        machine-boundaries (vec (distinct (:machine-boundaries trace-value)))
        image-fallbacks (vec (distinct (:image-fallbacks trace-value)))
        stage0-records (read-source-form-records source-path source-text)
        stage1-forms (mapv :form stage1-records)
        stage0-forms (mapv :form stage0-records)
        comparison {:forms-equal? (= stage1-forms stage0-forms)
                    :top-level-count-matches? (= (count stage1-records)
                                                 (count stage0-records))
                    :stage1-form-count (count stage1-records)
                    :stage0-form-count (count stage0-records)}
        runtime-image-artifact-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:reader-source stage1-reader-source-path
                       :entrypoint stage1-reader-runtime-image-entrypoint
                       :runtime-image runtime-image
                       :runtime-entrypoint runtime-entrypoint
                       :compiler-driver compiler-driver
                       :core-bootstrap-runtime core-bootstrap-runtime
                       :core-bootstrap-builtins
                       core-bootstrap-builtins})))]
    (let [artifact-base
          {:kind :gravity/stage1-reader-runtime-image-artifact
           :phase "15"
           :task "P15-S18"
           :stage :stage1-reader-runtime-image
           :source-path source-path
           :reader-source-path stage1-reader-source-path
           :gravity-entrypoint stage1-reader-runtime-image-entrypoint
           :runtime-image-artifact-id runtime-image-artifact-id
           :reader-runtime-image-id
           (:runtime-image-id runtime-image)
           :reader-runtime-entrypoint-id
           (:runtime-entrypoint-id runtime-entrypoint)
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
           :runner-fallbacks runner-fallbacks
           :os-boundaries os-boundaries
           :replaced-os-boundaries replaced-os-boundaries
           :machine-boundaries machine-boundaries
           :image-fallbacks image-fallbacks
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
            :machine-boundary? true
            :kernel-process-scheduler-boundary? true
            :artifact-loader-boundary? true}
           :stage1-bootstrap-source-artifact stage1-bootstrap-artifact
           :stage1-reader-runtime-image runtime-image
           :stage1-reader-runtime-entrypoint runtime-entrypoint
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
           :stage1-reader-runtime-image-trace
           (dissoc trace-value :character-stream :token-stream
                   :token-classifier :token-realizer :token-automaton
                   :token-automaton-executor :form-builder
                   :form-builder-executor :source-runtime
                   :self-hosted-reader-runtime
                   :core-bootstrap-runtime
                   :core-bootstrap-builtins
                   :compiler-driver
                   :runtime-entrypoint
                   :runtime-image)
           :stage0-comparison comparison
           :accepted-stage1-reader-runtime-image-fixtures
           [{:fixture
             "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
             :status :accepted
             :comparison comparison
             :character-count (:character-count character-stream)
             :token-count (:token-count token-stream)
             :form-count (count stage1-records)
             :runtime-image-id (:runtime-image-id runtime-image)}]
           :rejected-stage1-reader-runtime-image-fixtures
           stage1-reader-runtime-image-rejected-fixture-records
           :stage1-reader-runtime-image-diagnostic-stream
           (stage1-reader-runtime-image-diagnostic-stream
            source-path
            (:runtime-image-id runtime-image))
           :stage1-reader-runtime-image-results
           {:accepted-fixtures 1
            :rejected-fixtures
            (count stage1-reader-runtime-image-rejected-fixture-records)
            :diagnostic-count
            (+ (count stage1-reader-runtime-image-diagnostic-ids)
               (dec (count stage1-reader-execution-diagnostic-ids)))
            :character-count (:character-count character-stream)
            :token-count (:token-count token-stream)
            :form-count (count stage1-records)
            :status :complete}
           :diagnostics []}]
      (when-not (:forms-equal? comparison)
        (stage1-reader-runtime-image-fail!
         "STAGE1IMG004" source-path comparison
         {:missing-fields [:stage0-form-parity]}))
      (when-not (= (:artifact runtime-image) (:kind artifact-base))
        (stage1-reader-runtime-image-fail!
         "STAGE1IMG004" source-path artifact-base
         {:missing-fields [:artifact]}))
      (when-not (= (:diagnostic-stream runtime-image)
                   (get-in artifact-base
                           [:stage1-reader-runtime-image-diagnostic-stream
                            :artifact]))
        (stage1-reader-runtime-image-fail!
         "STAGE1IMG008" source-path artifact-base
         {:missing-fields [:diagnostic-stream]}))
      (when (seq host-primitives)
        (stage1-reader-runtime-image-fail!
         "STAGE1IMG007" source-path trace-value
         {:host-primitives host-primitives}))
      (when (seq seed-builtin-fallbacks)
        (stage1-reader-runtime-image-fail!
         "STAGE1IMG007" source-path trace-value
         {:seed-builtin-fallbacks seed-builtin-fallbacks}))
      (when (seq seed-orchestration-fallbacks)
        (stage1-reader-runtime-image-fail!
         "STAGE1IMG007" source-path trace-value
         {:seed-orchestration-fallbacks
          seed-orchestration-fallbacks}))
      (when (seq runner-fallbacks)
        (stage1-reader-runtime-image-fail!
         "STAGE1IMG007" source-path trace-value
         {:runner-fallbacks runner-fallbacks}))
      (when (seq os-boundaries)
        (stage1-reader-runtime-image-fail!
         "STAGE1IMG007" source-path trace-value
         {:os-boundaries os-boundaries}))
      (when (seq image-fallbacks)
        (stage1-reader-runtime-image-fail!
         "STAGE1IMG007" source-path trace-value
         {:image-fallbacks image-fallbacks}))
      (when-not (= #{:os-process-launch :os-filesystem-read :stdout-stream}
                   (set replaced-os-boundaries))
        (stage1-reader-runtime-image-fail!
         "STAGE1IMG004" source-path trace-value
         {:replaced-os-boundaries replaced-os-boundaries}))
      (when-not (and (false? (get-in artifact-base
                                     [:trusted-boundary
                                      :os-process-boundary?]))
                     (false? (get-in artifact-base
                                     [:trusted-boundary
                                      :os-filesystem-read-boundary?]))
                     (false? (get-in artifact-base
                                     [:trusted-boundary
                                      :stdout-boundary?]))
                     (true? (get-in artifact-base
                                    [:trusted-boundary
                                     :machine-boundary?]))
                     (true? (get-in artifact-base
                                    [:trusted-boundary
                                     :kernel-process-scheduler-boundary?]))
                     (true? (get-in artifact-base
                                    [:trusted-boundary
                                     :artifact-loader-boundary?])))
        (stage1-reader-runtime-image-fail!
         "STAGE1IMG008" source-path artifact-base
         {:missing-fields [:trusted-boundary]}))
      (let [capability-proof
            (stage1-reader-runtime-image-proof artifact-base)]
        (assoc artifact-base
               :capability-based-proof capability-proof
               :artifact-id (c4-artifact-id (assoc artifact-base
                                                   :capability-based-proof
                                                   capability-proof)))))))

(defn stage1-reader-runtime-image-file-artifact
  [path]
  (stage1-reader-runtime-image-source-artifact path (slurp path)))