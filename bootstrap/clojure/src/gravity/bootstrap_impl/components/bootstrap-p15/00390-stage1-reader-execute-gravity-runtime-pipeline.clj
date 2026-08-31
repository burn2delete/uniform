

(defn stage1-reader-execute-gravity-runtime-pipeline
  [reader-source-path source-path source-text]
  (let [forms (stage1-reader-source-forms reader-source-path)
        definitions (stage1-reader-definition-map reader-source-path forms)
        evaluator-runtime
        (stage1-reader-evaluator-runtime-from-definitions reader-source-path
                                                          definitions)]
    (when-not (= :defn (:kind (get definitions
                                    stage1-reader-runtime-pipeline-entrypoint)))
      (stage1-reader-runtime-pipeline-fail!
       "STAGE1RUN001" reader-source-path
       stage1-reader-runtime-pipeline-entrypoint
       {:missing-fields [stage1-reader-runtime-pipeline-entrypoint]}))
    (when *stage1-reader-pipeline-trace*
      (swap! *stage1-reader-pipeline-trace*
             (fn [trace]
               (-> (or trace {})
                   (update :gravity-runtimes (fnil conj [])
                           :stage1-reader-evaluator-runtime)
                   (assoc :evaluator-runtime evaluator-runtime)))))
    {:records
     (stage1-reader-execute-gravity-function
      reader-source-path definitions
      stage1-reader-runtime-pipeline-entrypoint
      [source-path source-text])
     :evaluator-runtime evaluator-runtime}))

(defn stage1-reader-runtime-pipeline-diagnostic-stream
  [source-path runtime-pipeline-id]
  {:artifact :gravity/stage1-reader-runtime-pipeline-diagnostic-stream
   :stage :stage1-reader-runtime-pipeline
   :source-path source-path
   :runtime-pipeline-id runtime-pipeline-id
   :diagnostics
   (mapv (fn [id]
           {:diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :stage1-reader-runtime-pipeline
            :artifact :gravity/diagnostic
            :message
            (or (stage1-reader-runtime-pipeline-diagnostic-messages id)
                (stage1-reader-execution-diagnostic-messages id))})
         (concat stage1-reader-runtime-pipeline-diagnostic-ids
                 (butlast stage1-reader-execution-diagnostic-ids)))
   :status :complete})

(defn stage1-reader-runtime-pipeline-proof
  [artifact]
  (let [diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:stage1-reader-runtime-pipeline-diagnostic-stream
                                       :diagnostics])))
        source-runtime (:stage1-reader-source-runtime artifact)
        evaluator-runtime (:stage1-reader-evaluator-runtime artifact)
        character-stream (:stage1-reader-character-stream artifact)
        token-stream (:stage1-reader-token-stream artifact)
        records (:stage1-reader-records artifact)
        gravity-runtimes (set (:gravity-runtimes artifact))
        gravity-executors (set (:gravity-executors artifact))]
    {:gravity-reader-runtime-pipeline-entrypoint-executed?
     (= stage1-reader-runtime-pipeline-entrypoint
        (:gravity-entrypoint artifact))
     :gravity-reader-source-verified?
     (= :complete (get-in artifact
                          [:stage1-bootstrap-source-artifact
                           :capability-based-proof
                           :status]))
     :gravity-source-runtime-authored?
     (and (= :gravity-reader-source-runtime-v1 (:engine source-runtime))
          (= :gravity-source (get-in source-runtime [:provenance :owner]))
          (= :gravity/stage1-reader-character-stream
             (:emits source-runtime)))
     :gravity-evaluator-runtime-authored?
     (and (= :gravity-reader-evaluator-runtime-v1
             (:engine evaluator-runtime))
          (= :gravity-source (get-in evaluator-runtime
                                     [:provenance :owner]))
          (= :clojure-stage0
             (:remaining-trusted-implementation evaluator-runtime)))
     :gravity-runtimes-covered?
     (set/subset? #{:stage1-reader-source-runtime
                    :stage1-reader-evaluator-runtime}
                  gravity-runtimes)
     :gravity-executors-covered?
     (set/subset? #{:stage1-reader-token-automaton-executor
                    :stage1-reader-form-builder-executor}
                  gravity-executors)
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
     (set/subset? (set (concat stage1-reader-runtime-pipeline-diagnostic-ids
                                (butlast stage1-reader-execution-diagnostic-ids)))
                  diagnostics)
     :limitations
     {:clojure-runtime-interpreter? true
      :host-character-stream-primitive? false
      :clojure-character-stream-implementation? true
      :clojure-seed-builtins? true
      :clojure-seed-retired? false
      :next-required-capability
      :replace-clojure-runtime-interpreter-with-gravity-compiled-reader}
     :status :complete}))