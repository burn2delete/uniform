

(defn stage1-reader-runtime-pipeline-source-artifact
  [source-path source-text]
  (let [stage1-bootstrap-artifact
        (stage1-bootstrap-source-artifact stage1-reader-bootstrap-source-root)
        trace (atom {})
        runtime-result
        (binding [*stage1-reader-pipeline-trace* trace]
          (stage1-reader-execute-gravity-runtime-pipeline
           stage1-reader-source-path source-path source-text))
        stage1-records (:records runtime-result)
        trace-value @trace
        source-runtime (:source-runtime trace-value)
        evaluator-runtime (:evaluator-runtime trace-value)
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
        stage0-records (read-source-form-records source-path source-text)
        stage1-forms (mapv :form stage1-records)
        stage0-forms (mapv :form stage0-records)
        comparison {:forms-equal? (= stage1-forms stage0-forms)
                    :top-level-count-matches? (= (count stage1-records)
                                                 (count stage0-records))
                    :stage1-form-count (count stage1-records)
                    :stage0-form-count (count stage0-records)}
        runtime-pipeline-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:reader-source stage1-reader-source-path
                       :entrypoint
                       stage1-reader-runtime-pipeline-entrypoint
                       :forms (stage1-reader-source-forms
                               stage1-reader-source-path)})))
        artifact-base
        {:kind :gravity/stage1-reader-runtime-pipeline-artifact
         :phase "15"
         :task "P15-S11"
         :stage :stage1-reader-runtime-pipeline
         :source-path source-path
         :reader-source-path stage1-reader-source-path
         :gravity-entrypoint stage1-reader-runtime-pipeline-entrypoint
         :runtime-pipeline-id runtime-pipeline-id
         :host-primitives host-primitives
         :gravity-runtimes gravity-runtimes
         :gravity-executors gravity-executors
         :stage1-bootstrap-source-artifact stage1-bootstrap-artifact
         :stage1-reader-source-runtime source-runtime
         :stage1-reader-evaluator-runtime evaluator-runtime
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
         :stage1-reader-runtime-pipeline-trace
         (dissoc trace-value :character-stream :token-stream
                 :token-classifier :token-realizer :token-automaton
                 :token-automaton-executor :form-builder
                 :form-builder-executor :source-runtime
                 :evaluator-runtime)
         :stage0-comparison comparison
         :accepted-stage1-reader-runtime-pipeline-fixtures
         [{:fixture "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
           :status :accepted
           :comparison comparison
           :character-count (:character-count character-stream)
           :token-count (:token-count token-stream)
           :form-count (count stage1-records)}]
         :rejected-stage1-reader-runtime-pipeline-fixtures
         stage1-reader-runtime-pipeline-rejected-fixture-records
         :stage1-reader-runtime-pipeline-diagnostic-stream
         (stage1-reader-runtime-pipeline-diagnostic-stream
          source-path runtime-pipeline-id)
         :stage1-reader-runtime-pipeline-results
         {:accepted-fixtures 1
          :rejected-fixtures
          (count stage1-reader-runtime-pipeline-rejected-fixture-records)
          :diagnostic-count
          (+ (count stage1-reader-runtime-pipeline-diagnostic-ids)
             (dec (count stage1-reader-execution-diagnostic-ids)))
          :character-count (:character-count character-stream)
          :token-count (:token-count token-stream)
          :form-count (count stage1-records)
          :status :complete}
         :diagnostics []}
        capability-proof (stage1-reader-runtime-pipeline-proof artifact-base)]
    (when-not (:forms-equal? comparison)
      (stage1-reader-runtime-pipeline-fail!
       "STAGE1RUN005" source-path comparison
       {:missing-fields [:stage0-form-parity]}))
    (when (seq host-primitives)
      (stage1-reader-runtime-pipeline-fail!
       "STAGE1RUN003" source-path trace-value
       {:host-primitives host-primitives}))
    (when-not (set/subset? #{:stage1-reader-source-runtime
                             :stage1-reader-evaluator-runtime}
                           (set gravity-runtimes))
      (stage1-reader-runtime-pipeline-fail!
       "STAGE1RUN004" source-path trace-value
       {:missing-fields [:gravity-runtimes]}))
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn stage1-reader-runtime-pipeline-file-artifact
  [path]
  (stage1-reader-runtime-pipeline-source-artifact path (slurp path)))

(defn stage1-reader-execute-compiled-pipeline
  [reader-source-path source-path source-text]
  (let [forms (stage1-reader-source-forms reader-source-path)
        definitions (stage1-reader-definition-map reader-source-path forms)
        compiled-program
        (stage1-reader-compiled-program-from-definitions reader-source-path
                                                         definitions)]
    (when-not (stage1-reader-compiled-entrypoint-valid? definitions)
      (stage1-reader-compiled-pipeline-fail!
       "STAGE1COMP001" reader-source-path
       stage1-reader-compiled-pipeline-entrypoint
       {:missing-fields [stage1-reader-compiled-pipeline-entrypoint]}))
    {:records
     (stage1-reader-execute-compiled-program
      reader-source-path definitions source-path source-text compiled-program)
     :compiled-program compiled-program}))

(defn stage1-reader-compiled-pipeline-diagnostic-stream
  [source-path compiled-pipeline-id]
  {:artifact :gravity/stage1-reader-compiled-pipeline-diagnostic-stream
   :stage :stage1-reader-compiled-pipeline
   :source-path source-path
   :compiled-pipeline-id compiled-pipeline-id
   :diagnostics
   (mapv (fn [id]
           {:diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :stage1-reader-compiled-pipeline
            :artifact :gravity/diagnostic
            :message
            (or (stage1-reader-compiled-pipeline-diagnostic-messages id)
                (stage1-reader-execution-diagnostic-messages id))})
         (concat stage1-reader-compiled-pipeline-diagnostic-ids
                 (butlast stage1-reader-execution-diagnostic-ids)))
   :status :complete})