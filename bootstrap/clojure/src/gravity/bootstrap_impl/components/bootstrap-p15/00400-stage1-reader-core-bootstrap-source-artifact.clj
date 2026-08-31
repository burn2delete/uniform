

(defn stage1-reader-core-bootstrap-source-artifact
  [source-path source-text]
  (let [stage1-bootstrap-artifact
        (stage1-bootstrap-source-artifact stage1-reader-bootstrap-source-root)
        trace (atom {})
        runtime-result
        (binding [*stage1-reader-pipeline-trace* trace]
          (stage1-reader-execute-core-bootstrap-runtime-pipeline
           stage1-reader-source-path source-path source-text))
        stage1-records (:records runtime-result)
        trace-value @trace
        self-hosted-runtime (:self-hosted-runtime runtime-result)
        core-bootstrap-runtime (:core-bootstrap-runtime runtime-result)
        core-bootstrap-builtins (:core-bootstrap-builtins runtime-result)
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
        stage0-records (read-source-form-records source-path source-text)
        stage1-forms (mapv :form stage1-records)
        stage0-forms (mapv :form stage0-records)
        comparison {:forms-equal? (= stage1-forms stage0-forms)
                    :top-level-count-matches? (= (count stage1-records)
                                                 (count stage0-records))
                    :stage1-form-count (count stage1-records)
                    :stage0-form-count (count stage0-records)}
        core-bootstrap-artifact-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:reader-source stage1-reader-source-path
                       :entrypoint
                       stage1-reader-core-bootstrap-entrypoint
                       :core-bootstrap-runtime
                       core-bootstrap-runtime
                       :core-bootstrap-builtins
                       core-bootstrap-builtins})))]
    (let [artifact-base
          {:kind :gravity/stage1-reader-core-bootstrap-artifact
           :phase "15"
           :task "P15-S15"
           :stage :stage1-reader-core-bootstrap
           :source-path source-path
           :reader-source-path stage1-reader-source-path
           :gravity-entrypoint stage1-reader-core-bootstrap-entrypoint
           :core-bootstrap-artifact-id core-bootstrap-artifact-id
           :reader-core-bootstrap-runtime-id
           (:core-bootstrap-runtime-id core-bootstrap-runtime)
           :reader-core-bootstrap-builtins-id
           (:core-bootstrap-builtins-id core-bootstrap-builtins)
           :reader-self-hosted-runtime-id
           (:self-hosted-runtime-id self-hosted-runtime)
           :host-primitives host-primitives
           :seed-builtin-fallbacks seed-builtin-fallbacks
           :gravity-runtimes gravity-runtimes
           :gravity-executors gravity-executors
           :trusted-boundary
           {:clojure-runtime-interpreter? false
            :clojure-instruction-executor? false
            :clojure-binary-runner? false
            :clojure-character-stream-implementation? false
            :clojure-seed-builtins? false
            :clojure-seed-orchestration? true}
           :stage1-bootstrap-source-artifact stage1-bootstrap-artifact
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
           :stage1-reader-core-bootstrap-trace
           (dissoc trace-value :character-stream :token-stream
                   :token-classifier :token-realizer :token-automaton
                   :token-automaton-executor :form-builder
                   :form-builder-executor :source-runtime
                   :self-hosted-reader-runtime
                   :core-bootstrap-runtime
                   :core-bootstrap-builtins)
           :stage0-comparison comparison
           :accepted-stage1-reader-core-bootstrap-fixtures
           [{:fixture
             "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
             :status :accepted
             :comparison comparison
             :character-count (:character-count character-stream)
             :token-count (:token-count token-stream)
             :form-count (count stage1-records)
             :core-bootstrap-builtins-id
             (:core-bootstrap-builtins-id core-bootstrap-builtins)}]
           :rejected-stage1-reader-core-bootstrap-fixtures
           stage1-reader-core-bootstrap-rejected-fixture-records
           :stage1-reader-core-bootstrap-diagnostic-stream
           (stage1-reader-core-bootstrap-diagnostic-stream
            source-path
            (:core-bootstrap-runtime-id core-bootstrap-runtime)
            (:core-bootstrap-builtins-id core-bootstrap-builtins))
           :stage1-reader-core-bootstrap-results
           {:accepted-fixtures 1
            :rejected-fixtures
            (count stage1-reader-core-bootstrap-rejected-fixture-records)
            :diagnostic-count
            (+ (count stage1-reader-core-bootstrap-diagnostic-ids)
               (dec (count stage1-reader-execution-diagnostic-ids)))
            :character-count (:character-count character-stream)
            :token-count (:token-count token-stream)
            :form-count (count stage1-records)
            :status :complete}
           :diagnostics []}]
      (when-not (:forms-equal? comparison)
        (stage1-reader-core-bootstrap-fail!
         "STAGE1CORE004" source-path comparison
         {:missing-fields [:stage0-form-parity]}))
      (when (seq host-primitives)
        (stage1-reader-core-bootstrap-fail!
         "STAGE1CORE005" source-path trace-value
         {:host-primitives host-primitives}))
      (when (seq seed-builtin-fallbacks)
        (stage1-reader-core-bootstrap-fail!
         "STAGE1CORE005" source-path trace-value
         {:seed-builtin-fallbacks seed-builtin-fallbacks}))
      (when (seq (:host-fallbacks core-bootstrap-builtins))
        (stage1-reader-core-bootstrap-fail!
         "STAGE1CORE005" source-path core-bootstrap-builtins
         {:host-fallbacks (:host-fallbacks core-bootstrap-builtins)}))
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
                     (true? (get-in artifact-base
                                    [:trusted-boundary
                                     :clojure-seed-orchestration?])))
        (stage1-reader-core-bootstrap-fail!
         "STAGE1CORE006" source-path artifact-base
         {:missing-fields [:trusted-boundary]}))
      (let [capability-proof
            (stage1-reader-core-bootstrap-proof artifact-base)]
        (assoc artifact-base
               :capability-based-proof capability-proof
               :artifact-id (c4-artifact-id (assoc artifact-base
                                                   :capability-based-proof
                                                   capability-proof)))))))

(defn stage1-reader-core-bootstrap-file-artifact
  [path]
  (stage1-reader-core-bootstrap-source-artifact path (slurp path)))

(def stage1-reader-compiler-driver-required-operations
  [:load-reader-source
   :resolve-stage1-entrypoint
   :route-source-text
   :execute-core-bootstrap-runtime
   :emit-diagnostic-stream
   :emit-proof-artifact
   :compare-stage0
   :record-provenance])

(defn stage1-reader-compiler-driver-literal-definition-value
  [reader-source-path definitions symbol-name]
  (let [definition (get definitions symbol-name)]
    (when-not (= :def (:kind definition))
      (stage1-reader-compiler-driver-fail!
       "STAGE1DRV003" reader-source-path symbol-name
       {:missing-fields [symbol-name]}))
    (:value-form definition)))