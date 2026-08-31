

(defn stage1-reader-form-builder-pipeline-source-artifact
  [source-path source-text]
  (let [stage1-bootstrap-artifact
        (stage1-bootstrap-source-artifact stage1-reader-bootstrap-source-root)
        trace (atom {})
        stage1-records
        (binding [*stage1-reader-pipeline-trace* trace]
          (stage1-reader-execute-gravity-form-builder-pipeline
           stage1-reader-source-path source-path source-text))
        trace-value @trace
        character-stream (:character-stream trace-value)
        token-classifier (:token-classifier trace-value)
        token-realizer (:token-realizer trace-value)
        token-automaton (:token-automaton trace-value)
        form-builder (:form-builder trace-value)
        token-stream (:token-stream trace-value)
        host-primitives (vec (distinct (:host-primitives trace-value)))
        stage0-records (read-source-form-records source-path source-text)
        stage1-forms (mapv :form stage1-records)
        stage0-forms (mapv :form stage0-records)
        comparison {:forms-equal? (= stage1-forms stage0-forms)
                    :top-level-count-matches? (= (count stage1-records)
                                                 (count stage0-records))
                    :stage1-form-count (count stage1-records)
                    :stage0-form-count (count stage0-records)}
        form-builder-pipeline-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:reader-source stage1-reader-source-path
                       :entrypoint
                       stage1-reader-form-builder-pipeline-entrypoint
                       :forms (stage1-reader-source-forms
                               stage1-reader-source-path)})))
        artifact-base
        {:kind :gravity/stage1-reader-form-builder-pipeline-artifact
         :phase "15"
         :task "P15-S9"
         :stage :stage1-reader-form-builder-pipeline
         :source-path source-path
         :reader-source-path stage1-reader-source-path
         :gravity-entrypoint
         stage1-reader-form-builder-pipeline-entrypoint
         :form-builder-pipeline-id form-builder-pipeline-id
         :host-primitives host-primitives
         :stage1-bootstrap-source-artifact stage1-bootstrap-artifact
         :stage1-reader-character-stream character-stream
         :stage1-reader-token-classifier token-classifier
         :stage1-reader-token-realizer token-realizer
         :stage1-reader-token-automaton token-automaton
         :stage1-reader-form-builder form-builder
         :stage1-reader-token-stream token-stream
         :stage1-reader-records stage1-records
         :stage1-reader-form-builder-pipeline-trace
         (dissoc trace-value :character-stream :token-stream
                 :token-classifier :token-realizer :token-automaton
                 :form-builder)
         :stage0-comparison comparison
         :accepted-stage1-reader-form-builder-pipeline-fixtures
         [{:fixture "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
           :status :accepted
           :comparison comparison
           :character-count (:character-count character-stream)
           :token-count (:token-count token-stream)
           :form-count (count stage1-records)}]
         :rejected-stage1-reader-form-builder-pipeline-fixtures
         stage1-reader-form-builder-pipeline-rejected-fixture-records
         :stage1-reader-form-builder-pipeline-diagnostic-stream
         (stage1-reader-form-builder-pipeline-diagnostic-stream
          source-path form-builder-pipeline-id)
         :stage1-reader-form-builder-pipeline-results
         {:accepted-fixtures 1
          :rejected-fixtures
          (count stage1-reader-form-builder-pipeline-rejected-fixture-records)
          :diagnostic-count
          (+ (count stage1-reader-form-builder-pipeline-diagnostic-ids)
             (dec (count stage1-reader-execution-diagnostic-ids)))
          :character-count (:character-count character-stream)
          :token-count (:token-count token-stream)
          :form-count (count stage1-records)
          :status :complete}
         :diagnostics []}
        capability-proof
        (stage1-reader-form-builder-pipeline-proof artifact-base)]
    (when-not (:forms-equal? comparison)
      (stage1-reader-form-builder-pipeline-fail!
       "STAGE1FORM005" source-path comparison
       {:missing-fields [:stage0-form-parity]}))
    (when-not (seq host-primitives)
      (stage1-reader-form-builder-pipeline-fail!
       "STAGE1FORM003" source-path trace-value
       {:missing-fields [:host-primitives]}))
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn stage1-reader-form-builder-pipeline-file-artifact
  [path]
  (stage1-reader-form-builder-pipeline-source-artifact path (slurp path)))

(defn stage1-reader-execute-gravity-executor-pipeline
  [reader-source-path source-path source-text]
  (let [forms (stage1-reader-source-forms reader-source-path)
        definitions (stage1-reader-definition-map reader-source-path forms)]
    (when-not (= :defn (:kind (get definitions
                                    stage1-reader-executor-pipeline-entrypoint)))
      (stage1-reader-executor-pipeline-fail!
       "STAGE1EXEC001" reader-source-path
       stage1-reader-executor-pipeline-entrypoint
       {:missing-fields [stage1-reader-executor-pipeline-entrypoint]}))
    (stage1-reader-execute-gravity-function
     reader-source-path definitions
     stage1-reader-executor-pipeline-entrypoint
     [source-path source-text])))

(defn stage1-reader-executor-pipeline-diagnostic-stream
  [source-path executor-pipeline-id]
  {:artifact :gravity/stage1-reader-executor-pipeline-diagnostic-stream
   :stage :stage1-reader-executor-pipeline
   :source-path source-path
   :executor-pipeline-id executor-pipeline-id
   :diagnostics
   (mapv (fn [id]
           {:diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :stage1-reader-executor-pipeline
            :artifact :gravity/diagnostic
            :message
            (or (stage1-reader-executor-pipeline-diagnostic-messages id)
                (stage1-reader-execution-diagnostic-messages id))})
         (concat stage1-reader-executor-pipeline-diagnostic-ids
                 (butlast stage1-reader-execution-diagnostic-ids)))
   :status :complete})