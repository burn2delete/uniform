

(defn stage1-reader-self-hosted-runtime-entrypoint-valid?
  [definitions]
  (let [definition (get definitions
                        stage1-reader-self-hosted-runtime-entrypoint)]
    (and (= :defn (:kind definition))
         (= '[source-path source-text] (:params definition))
         (= '((reader/execute-self-hosted-runtime
               source-path
               source-text
               stage1-reader-self-hosted-runtime))
            (:body definition)))))

(defn stage1-reader-execute-self-hosted-runtime
  [reader-source-path definitions source-path source-text self-hosted-runtime]
  (let [source-runtime
        (stage1-reader-self-hosted-runtime-literal-definition-value
         reader-source-path definitions 'stage1-reader-source-runtime)
        classifier
        (stage1-reader-self-hosted-runtime-literal-definition-value
         reader-source-path definitions 'stage1-reader-token-classifier)
        realizer
        (stage1-reader-self-hosted-runtime-literal-definition-value
         reader-source-path definitions 'stage1-reader-token-realizer)
        automaton
        (stage1-reader-self-hosted-runtime-literal-definition-value
         reader-source-path definitions 'stage1-reader-token-automaton)
        token-executor
        (stage1-reader-self-hosted-runtime-literal-definition-value
         reader-source-path definitions
         'stage1-reader-token-automaton-executor)
        form-builder
        (stage1-reader-self-hosted-runtime-literal-definition-value
         reader-source-path definitions 'stage1-reader-form-builder)
        form-executor
        (stage1-reader-self-hosted-runtime-literal-definition-value
         reader-source-path definitions
         'stage1-reader-form-builder-executor)
        character-stream
        (assoc (stage1-reader-character-stream-from-runtime
                source-path source-text source-runtime)
               :gravity-runtime :stage1-reader-self-hosted-runtime
               :self-hosted-runtime-id
               (:self-hosted-runtime-id self-hosted-runtime)
               :self-hosted-runtime-engine (:engine self-hosted-runtime))
        token-stream (stage1-reader-token-stream-from-executor
                      source-path source-text classifier realizer automaton
                      token-executor character-stream)
        records (stage1-reader-records-from-executor
                 source-path source-text classifier realizer automaton
                 form-builder form-executor token-stream)]
    (when *stage1-reader-pipeline-trace*
      (swap! *stage1-reader-pipeline-trace*
             (fn [trace]
               (-> (or trace {})
                   (assoc :self-hosted-reader-runtime self-hosted-runtime)
                   (assoc :reader-self-hosted-direct-stages
                          (mapv :op (:direct-stages self-hosted-runtime)))
                   (update :gravity-runtimes (fnil conj [])
                           :stage1-reader-self-hosted-runtime)
                   (update :gravity-runtimes (fnil conj [])
                           :stage1-reader-source-runtime)
                   (update :gravity-executors (fnil conj [])
                           :stage1-reader-token-automaton-executor)
                   (update :gravity-executors (fnil conj [])
                           :stage1-reader-form-builder-executor)
                   (assoc :source-runtime source-runtime)
                   (assoc :character-stream character-stream)
                   (assoc :token-classifier classifier)
                   (assoc :token-realizer realizer)
                   (assoc :token-automaton automaton)
                   (assoc :token-automaton-executor token-executor)
                   (assoc :form-builder form-builder)
                   (assoc :form-builder-executor form-executor)
                   (assoc :token-stream token-stream)
                   (assoc :self-hosted-runtime-created
                          {:character-count
                           (:character-count character-stream)
                           :source-runtime-engine
                           (:source-runtime-engine character-stream)
                           :self-hosted-runtime-engine
                           (:self-hosted-runtime-engine character-stream)})
                   (assoc :token-automaton-executed
                          {:character-count
                           (:character-count character-stream)
                           :token-count (:token-count token-stream)
                           :operation-count
                           (count (:executed-operations token-stream))})
                   (assoc :forms-built
                          {:token-count (:token-count token-stream)
                           :form-count (count records)})))))
    records))

(defn stage1-reader-execute-self-hosted-runtime-pipeline
  [reader-source-path source-path source-text]
  (let [forms (stage1-reader-source-forms reader-source-path)
        definitions (stage1-reader-definition-map reader-source-path forms)
        self-hosted-runtime
        (stage1-reader-self-hosted-runtime-from-definitions
         reader-source-path definitions)]
    (when-not (stage1-reader-self-hosted-runtime-entrypoint-valid?
               definitions)
      (stage1-reader-self-hosted-runtime-fail!
       "STAGE1SELF001" reader-source-path
       stage1-reader-self-hosted-runtime-entrypoint
       {:missing-fields
        [stage1-reader-self-hosted-runtime-entrypoint]}))
    {:records
     (stage1-reader-execute-self-hosted-runtime
      reader-source-path definitions source-path source-text
      self-hosted-runtime)
     :self-hosted-runtime self-hosted-runtime}))

(defn stage1-reader-self-hosted-runtime-diagnostic-stream
  [source-path self-hosted-runtime-id]
  {:artifact :gravity/stage1-reader-self-hosted-runtime-diagnostic-stream
   :stage :stage1-reader-self-hosted-runtime
   :source-path source-path
   :self-hosted-runtime-id self-hosted-runtime-id
   :diagnostics
   (mapv (fn [id]
           {:diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :stage1-reader-self-hosted-runtime
            :artifact :gravity/diagnostic
            :message
            (or (stage1-reader-self-hosted-runtime-diagnostic-messages id)
                (stage1-reader-execution-diagnostic-messages id))})
         (concat stage1-reader-self-hosted-runtime-diagnostic-ids
                 (butlast stage1-reader-execution-diagnostic-ids)))
   :status :complete})

(defn stage1-reader-self-hosted-runtime-proof
  [artifact]
  (let [diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:stage1-reader-self-hosted-runtime-diagnostic-stream
                                       :diagnostics])))
        runtime (:stage1-reader-self-hosted-runtime artifact)
        source-runtime (:stage1-reader-source-runtime artifact)
        character-stream (:stage1-reader-character-stream artifact)
        token-stream (:stage1-reader-token-stream artifact)
        records (:stage1-reader-records artifact)
        direct-stages (mapv :op (:direct-stages runtime))
        gravity-runtimes (set (:gravity-runtimes artifact))
        gravity-executors (set (:gravity-executors artifact))]
    {:gravity-reader-self-hosted-entrypoint-verified?
     (= stage1-reader-self-hosted-runtime-entrypoint
        (:gravity-entrypoint artifact))
     :gravity-reader-source-verified?
     (= :complete (get-in artifact
                          [:stage1-bootstrap-source-artifact
                           :capability-based-proof
                           :status]))
     :gravity-self-hosted-runtime-authored?
     (and (= :gravity-reader-self-hosted-runtime-v1 (:engine runtime))
          (= :gravity-source (get-in runtime [:provenance :owner]))
          (= :reader-self-hosted-runtime
             (get-in runtime [:provenance :purpose])))
     :gravity-self-hosted-runtime-direct-stages-covered?
     (= [:stage1-self-hosted-create-character-stream
         :stage1-self-hosted-execute-token-automaton
         :stage1-self-hosted-execute-form-builder]
        direct-stages)
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
     :seed-builtin-boundary-explicit?
     (true? (get-in artifact [:trusted-boundary
                              :clojure-seed-builtins?]))
     :host-primitive-boundary-empty?
     (= [] (:host-primitives artifact))
     :gravity-source-runtime-authored?
     (and (= :gravity-reader-source-runtime-v1 (:engine source-runtime))
          (= :gravity-source (get-in source-runtime [:provenance :owner])))
     :gravity-runtimes-covered?
     (set/subset? #{:stage1-reader-self-hosted-runtime
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
     (set/subset?
      (set (concat stage1-reader-self-hosted-runtime-diagnostic-ids
                   (butlast stage1-reader-execution-diagnostic-ids)))
      diagnostics)
     :limitations
     {:clojure-runtime-interpreter? false
      :clojure-instruction-executor? false
      :clojure-binary-runner? false
      :clojure-character-stream-implementation? false
      :clojure-seed-builtins? true
      :clojure-seed-retired? false
      :next-required-capability
      :replace-clojure-seed-builtins-with-gravity-core-bootstrap}
     :status :complete}))