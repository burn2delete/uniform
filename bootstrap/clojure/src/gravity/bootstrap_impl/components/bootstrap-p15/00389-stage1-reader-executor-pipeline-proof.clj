

(defn stage1-reader-executor-pipeline-proof
  [artifact]
  (let [diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:stage1-reader-executor-pipeline-diagnostic-stream
                                       :diagnostics])))
        character-stream (:stage1-reader-character-stream artifact)
        characters (:characters character-stream)
        token-classifier (:stage1-reader-token-classifier artifact)
        token-realizer (:stage1-reader-token-realizer artifact)
        token-automaton (:stage1-reader-token-automaton artifact)
        token-automaton-executor
        (:stage1-reader-token-automaton-executor artifact)
        form-builder (:stage1-reader-form-builder artifact)
        form-builder-executor
        (:stage1-reader-form-builder-executor artifact)
        token-stream (:stage1-reader-token-stream artifact)
        tokens (:tokens token-stream)
        records (:stage1-reader-records artifact)
        gravity-executors (set (:gravity-executors artifact))]
    {:gravity-reader-executor-pipeline-entrypoint-executed?
     (= stage1-reader-executor-pipeline-entrypoint
        (:gravity-entrypoint artifact))
     :gravity-reader-source-verified?
     (= :complete (get-in artifact
                          [:stage1-bootstrap-source-artifact
                           :capability-based-proof
                           :status]))
     :gravity-token-classifier-authored?
     (= :gravity-reader-token-classifier-v1 (:engine token-classifier))
     :gravity-token-realizer-authored?
     (= :gravity-reader-token-realizer-v1 (:engine token-realizer))
     :gravity-token-automaton-authored?
     (= :gravity-reader-token-automaton-v1 (:engine token-automaton))
     :gravity-form-builder-authored?
     (= :gravity-reader-form-builder-v1 (:engine form-builder))
     :gravity-token-automaton-executor-authored?
     (and (= :gravity-reader-token-automaton-executor-v1
             (:engine token-automaton-executor))
          (= (:engine token-automaton)
             (:executes token-automaton-executor))
          (= :gravity-source
             (get-in token-automaton-executor [:provenance :owner])))
     :gravity-form-builder-executor-authored?
     (and (= :gravity-reader-form-builder-executor-v1
             (:engine form-builder-executor))
          (= (:engine form-builder)
             (:executes form-builder-executor))
          (= :gravity-source
             (get-in form-builder-executor [:provenance :owner])))
     :gravity-executors-covered?
     (set/subset? #{:stage1-reader-token-automaton-executor
                    :stage1-reader-form-builder-executor}
                  gravity-executors)
     :host-primitive-boundary-reduced?
     (= [:reader/source-characters] (:host-primitives artifact))
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
          (pos? (:character-count character-stream))
          (= (:character-count character-stream) (count characters))
          (every? #(get-in % [:span :source]) characters))
     :token-stream-covered?
     (and (= :gravity/stage1-reader-token-stream (:kind token-stream))
          (= :gravity-reader-token-automaton-v1
             (:token-automaton-engine token-stream))
          (= :gravity-reader-token-automaton-executor-v1
             (:token-automaton-executor-engine token-stream))
          (pos? (:token-count token-stream))
          (= (:token-count token-stream) (count tokens))
          (every? #(get-in % [:span :source]) tokens)
          (set/subset? #{:read-string-token :read-dispatch-token
                         :read-delimiter-token :read-atom-token}
                       (set (:executed-operations token-stream))))
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
     (set/subset? (set (concat stage1-reader-executor-pipeline-diagnostic-ids
                                (butlast stage1-reader-execution-diagnostic-ids)))
                  diagnostics)
     :limitations
     {:clojure-seed-evaluator? true
      :host-character-stream? true
      :clojure-seed-builtins? true
      :clojure-seed-retired? false
      :next-required-capability
      :replace-host-character-stream-and-seed-evaluator-with-gravity-reader-runtime}
     :status :complete}))

(defn stage1-reader-executor-pipeline-source-artifact
  [source-path source-text]
  (let [stage1-bootstrap-artifact
        (stage1-bootstrap-source-artifact stage1-reader-bootstrap-source-root)
        trace (atom {})
        stage1-records
        (binding [*stage1-reader-pipeline-trace* trace]
          (stage1-reader-execute-gravity-executor-pipeline
           stage1-reader-source-path source-path source-text))
        trace-value @trace
        character-stream (:character-stream trace-value)
        token-classifier (:token-classifier trace-value)
        token-realizer (:token-realizer trace-value)
        token-automaton (:token-automaton trace-value)
        token-automaton-executor (:token-automaton-executor trace-value)
        form-builder (:form-builder trace-value)
        form-builder-executor (:form-builder-executor trace-value)
        token-stream (:token-stream trace-value)
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
        executor-pipeline-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:reader-source stage1-reader-source-path
                       :entrypoint
                       stage1-reader-executor-pipeline-entrypoint
                       :forms (stage1-reader-source-forms
                               stage1-reader-source-path)})))
        artifact-base
        {:kind :gravity/stage1-reader-executor-pipeline-artifact
         :phase "15"
         :task "P15-S10"
         :stage :stage1-reader-executor-pipeline
         :source-path source-path
         :reader-source-path stage1-reader-source-path
         :gravity-entrypoint stage1-reader-executor-pipeline-entrypoint
         :executor-pipeline-id executor-pipeline-id
         :host-primitives host-primitives
         :gravity-executors gravity-executors
         :stage1-bootstrap-source-artifact stage1-bootstrap-artifact
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
         :stage1-reader-executor-pipeline-trace
         (dissoc trace-value :character-stream :token-stream
                 :token-classifier :token-realizer :token-automaton
                 :token-automaton-executor :form-builder
                 :form-builder-executor)
         :stage0-comparison comparison
         :accepted-stage1-reader-executor-pipeline-fixtures
         [{:fixture "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
           :status :accepted
           :comparison comparison
           :character-count (:character-count character-stream)
           :token-count (:token-count token-stream)
           :form-count (count stage1-records)}]
         :rejected-stage1-reader-executor-pipeline-fixtures
         stage1-reader-executor-pipeline-rejected-fixture-records
         :stage1-reader-executor-pipeline-diagnostic-stream
         (stage1-reader-executor-pipeline-diagnostic-stream
          source-path executor-pipeline-id)
         :stage1-reader-executor-pipeline-results
         {:accepted-fixtures 1
          :rejected-fixtures
          (count stage1-reader-executor-pipeline-rejected-fixture-records)
          :diagnostic-count
          (+ (count stage1-reader-executor-pipeline-diagnostic-ids)
             (dec (count stage1-reader-execution-diagnostic-ids)))
          :character-count (:character-count character-stream)
          :token-count (:token-count token-stream)
          :form-count (count stage1-records)
          :status :complete}
         :diagnostics []}
        capability-proof
        (stage1-reader-executor-pipeline-proof artifact-base)]
    (when-not (:forms-equal? comparison)
      (stage1-reader-executor-pipeline-fail!
       "STAGE1EXEC005" source-path comparison
       {:missing-fields [:stage0-form-parity]}))
    (when-not (= [:reader/source-characters] host-primitives)
      (stage1-reader-executor-pipeline-fail!
       "STAGE1EXEC003" source-path trace-value
       {:host-primitives host-primitives}))
    (when-not (seq gravity-executors)
      (stage1-reader-executor-pipeline-fail!
       "STAGE1EXEC004" source-path trace-value
       {:missing-fields [:gravity-executors]}))
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn stage1-reader-executor-pipeline-file-artifact
  [path]
  (stage1-reader-executor-pipeline-source-artifact path (slurp path)))