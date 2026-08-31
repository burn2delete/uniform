

(defn stage1-reader-compiled-pipeline-proof
  [artifact]
  (let [diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:stage1-reader-compiled-pipeline-diagnostic-stream
                                       :diagnostics])))
        compiled-program (:stage1-reader-compiled-program artifact)
        source-runtime (:stage1-reader-source-runtime artifact)
        character-stream (:stage1-reader-character-stream artifact)
        token-stream (:stage1-reader-token-stream artifact)
        records (:stage1-reader-records artifact)
        gravity-runtimes (set (:gravity-runtimes artifact))
        gravity-executors (set (:gravity-executors artifact))]
    {:gravity-reader-compiled-pipeline-entrypoint-verified?
     (= stage1-reader-compiled-pipeline-entrypoint
        (:gravity-entrypoint artifact))
     :gravity-reader-source-verified?
     (= :complete (get-in artifact
                          [:stage1-bootstrap-source-artifact
                           :capability-based-proof
                           :status]))
     :gravity-compiled-program-authored?
     (and (= :gravity-reader-compiled-program-v1 (:engine compiled-program))
          (= :gravity-source (get-in compiled-program [:provenance :owner]))
          (= :stage1-read-source-runtime-pipeline
             (:compiled-from compiled-program)))
     :gravity-compiled-program-instructions-covered?
     (= [:stage1-create-character-stream
         :stage1-execute-token-automaton
         :stage1-execute-form-builder]
        (mapv :op (:instructions compiled-program)))
     :gravity-source-runtime-authored?
     (and (= :gravity-reader-source-runtime-v1 (:engine source-runtime))
          (= :gravity-source (get-in source-runtime [:provenance :owner])))
     :gravity-runtimes-covered?
     (set/subset? #{:stage1-reader-source-runtime} gravity-runtimes)
     :gravity-executors-covered?
     (set/subset? #{:stage1-reader-token-automaton-executor
                    :stage1-reader-form-builder-executor}
                  gravity-executors)
     :runtime-interpreter-replaced?
     (false? (get-in artifact [:trusted-boundary
                               :clojure-runtime-interpreter?]))
     :host-primitive-boundary-empty?
     (= [] (:host-primitives artifact))
     :source-characters-host-primitive-removed?
     (not-any? #{:reader/source-characters} (:host-primitives artifact))
     :run-token-automaton-host-primitive-removed?
     (not-any? #{:reader/run-token-automaton} (:host-primitives artifact))
     :build-forms-host-primitive-removed?
     (not-any? #{:reader/build-forms} (:host-primitives artifact))
     :forms-from-tokens-host-primitive-removed?
     (not-any? #{:reader/forms-from-tokens} (:host-primitives artifact))
     :realize-tokens-host-primitive-removed?
     (not-any? #{:reader/realize-tokens} (:host-primitives artifact))
     :tokens-from-classifier-host-primitive-removed?
     (not-any? #{:reader/tokens-from-classifier} (:host-primitives artifact))
     :tokens-from-characters-host-primitive-removed?
     (not-any? #{:reader/tokens-from-characters} (:host-primitives artifact))
     :scan-tokens-host-primitive-removed?
     (not-any? #{:reader/scan-tokens} (:host-primitives artifact))
     :whole-reader-host-primitive-removed?
     (not-any? #{:reader/read-with-table} (:host-primitives artifact))
     :character-stream-covered?
     (and (= :gravity/stage1-reader-character-stream
             (:kind character-stream))
          (= :gravity-reader-source-runtime-v1
             (:source-runtime-engine character-stream))
          (pos? (:character-count character-stream))
          (= (:character-count character-stream)
             (count (:characters character-stream)))
          (every? #(get-in % [:span :source])
                  (:characters character-stream)))
     :token-stream-covered?
     (and (= :gravity/stage1-reader-token-stream (:kind token-stream))
          (= :gravity-reader-token-automaton-executor-v1
             (:token-automaton-executor-engine token-stream))
          (pos? (:token-count token-stream))
          (= (:token-count token-stream) (count (:tokens token-stream))))
     :form-records-covered?
     (and (seq records)
          (every? #(= :gravity-reader-form-builder-executor-v1
                      (:form-builder-executor-engine %))
                  records))
     :forms-match-stage0?
     (true? (get-in artifact [:stage0-comparison :forms-equal?]))
     :source-spans-covered?
     (every? #(get-in % [:span :source]) records)
     :diagnostics-covered?
     (set/subset? (set (concat stage1-reader-compiled-pipeline-diagnostic-ids
                                (butlast stage1-reader-execution-diagnostic-ids)))
                  diagnostics)
     :limitations
     {:clojure-runtime-interpreter? false
      :clojure-instruction-executor? true
      :clojure-character-stream-implementation? true
      :clojure-seed-builtins? true
      :clojure-seed-retired? false
      :next-required-capability
      :replace-clojure-instruction-executor-with-gravity-emitted-reader-binary}
     :status :complete}))

(defn stage1-reader-compiled-pipeline-source-artifact
  [source-path source-text]
  (let [stage1-bootstrap-artifact
        (stage1-bootstrap-source-artifact stage1-reader-bootstrap-source-root)
        trace (atom {})
        compiled-result
        (binding [*stage1-reader-pipeline-trace* trace]
          (stage1-reader-execute-compiled-pipeline
           stage1-reader-source-path source-path source-text))
        stage1-records (:records compiled-result)
        trace-value @trace
        compiled-program (:compiled-program compiled-result)
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
        stage0-records (read-source-form-records source-path source-text)
        stage1-forms (mapv :form stage1-records)
        stage0-forms (mapv :form stage0-records)
        comparison {:forms-equal? (= stage1-forms stage0-forms)
                    :top-level-count-matches? (= (count stage1-records)
                                                 (count stage0-records))
                    :stage1-form-count (count stage1-records)
                    :stage0-form-count (count stage0-records)}
        compiled-pipeline-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:reader-source stage1-reader-source-path
                       :entrypoint
                       stage1-reader-compiled-pipeline-entrypoint
                       :compiled-program compiled-program})))
        artifact-base
        {:kind :gravity/stage1-reader-compiled-pipeline-artifact
         :phase "15"
         :task "P15-S12"
         :stage :stage1-reader-compiled-pipeline
         :source-path source-path
         :reader-source-path stage1-reader-source-path
         :gravity-entrypoint stage1-reader-compiled-pipeline-entrypoint
         :compiled-pipeline-id compiled-pipeline-id
         :host-primitives host-primitives
         :gravity-runtimes gravity-runtimes
         :gravity-executors gravity-executors
         :trusted-boundary
         {:clojure-runtime-interpreter? false
          :clojure-instruction-executor? true
          :clojure-character-stream-implementation? true}
         :stage1-bootstrap-source-artifact stage1-bootstrap-artifact
         :stage1-reader-compiled-program compiled-program
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
         :stage1-reader-compiled-pipeline-trace
         (dissoc trace-value :character-stream :token-stream
                 :token-classifier :token-realizer :token-automaton
                 :token-automaton-executor :form-builder
                 :form-builder-executor :source-runtime
                 :compiled-program)
         :stage0-comparison comparison
         :accepted-stage1-reader-compiled-pipeline-fixtures
         [{:fixture "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
           :status :accepted
           :comparison comparison
           :character-count (:character-count character-stream)
           :token-count (:token-count token-stream)
           :form-count (count stage1-records)}]
         :rejected-stage1-reader-compiled-pipeline-fixtures
         stage1-reader-compiled-pipeline-rejected-fixture-records
         :stage1-reader-compiled-pipeline-diagnostic-stream
         (stage1-reader-compiled-pipeline-diagnostic-stream
          source-path compiled-pipeline-id)
         :stage1-reader-compiled-pipeline-results
         {:accepted-fixtures 1
          :rejected-fixtures
          (count stage1-reader-compiled-pipeline-rejected-fixture-records)
          :diagnostic-count
          (+ (count stage1-reader-compiled-pipeline-diagnostic-ids)
             (dec (count stage1-reader-execution-diagnostic-ids)))
          :character-count (:character-count character-stream)
          :token-count (:token-count token-stream)
          :form-count (count stage1-records)
          :status :complete}
         :diagnostics []}
        capability-proof (stage1-reader-compiled-pipeline-proof artifact-base)]
    (when-not (:forms-equal? comparison)
      (stage1-reader-compiled-pipeline-fail!
       "STAGE1COMP005" source-path comparison
       {:missing-fields [:stage0-form-parity]}))
    (when (seq host-primitives)
      (stage1-reader-compiled-pipeline-fail!
       "STAGE1COMP003" source-path trace-value
       {:host-primitives host-primitives}))
    (when-not (false? (get-in artifact-base
                              [:trusted-boundary
                               :clojure-runtime-interpreter?]))
      (stage1-reader-compiled-pipeline-fail!
       "STAGE1COMP004" source-path artifact-base
       {:missing-fields [:trusted-boundary]}))
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn stage1-reader-compiled-pipeline-file-artifact
  [path]
  (stage1-reader-compiled-pipeline-source-artifact path (slurp path)))

(defn stage1-reader-binary-literal-definition-value
  [reader-source-path definitions symbol-name]
  (let [definition (get definitions symbol-name)]
    (when-not (= :def (:kind definition))
      (stage1-reader-binary-pipeline-fail!
       "STAGE1BIN004" reader-source-path symbol-name
       {:missing-fields [symbol-name]}))
    (:value-form definition)))