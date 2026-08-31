

(defn stage1-reader-emitted-binary-from-definitions
  [reader-source-path definitions compiled-program]
  (let [binary (stage1-reader-binary-literal-definition-value
                reader-source-path definitions
                'stage1-reader-emitted-binary)
        diagnostics (:diagnostics binary)
        missing-diagnostics (remove #(contains? diagnostics %)
                                    [:missing-entrypoint :unsupported-form
                                     :unsupported-host-primitive
                                     :invalid-binary :stage0-divergence])
        required-stages [:stage1-binary-create-character-stream
                         :stage1-binary-execute-token-automaton
                         :stage1-binary-execute-form-builder]
        required-runtimes #{:stage1-reader-source-runtime}
        required-executors #{:stage1-reader-token-automaton-executor
                             :stage1-reader-form-builder-executor}
        direct-stages (:direct-stages binary)]
    (when-not (map? binary)
      (stage1-reader-binary-pipeline-fail!
       "STAGE1BIN004" reader-source-path binary
       {:missing-fields [:stage1-reader-emitted-binary]}))
    (when-not (= :gravity-reader-binary-v1 (:engine binary))
      (stage1-reader-binary-pipeline-fail!
       "STAGE1BIN004" reader-source-path binary
       {:missing-fields [:engine]}))
    (doseq [field [:entrypoint :emitted-from :compiled-entrypoint
                   :input :output :direct-stages :uses-runtimes
                   :uses-executors :preserves :diagnostics :provenance]]
      (when-not (contains? binary field)
        (stage1-reader-binary-pipeline-fail!
         "STAGE1BIN004" reader-source-path binary
         {:missing-fields [field]})))
    (when-not (= :stage1-read-source-binary-pipeline (:entrypoint binary))
      (stage1-reader-binary-pipeline-fail!
       "STAGE1BIN004" reader-source-path binary
       {:missing-fields [:entrypoint]}))
    (when-not (= :stage1-reader-compiled-program (:emitted-from binary))
      (stage1-reader-binary-pipeline-fail!
       "STAGE1BIN004" reader-source-path binary
       {:missing-fields [:emitted-from]}))
    (when-not (= (:entrypoint compiled-program)
                 (:compiled-entrypoint binary))
      (stage1-reader-binary-pipeline-fail!
       "STAGE1BIN004" reader-source-path binary
       {:missing-fields [:compiled-entrypoint]}))
    (when-not (= [:source-path :source-text] (:input binary))
      (stage1-reader-binary-pipeline-fail!
       "STAGE1BIN004" reader-source-path binary
       {:missing-fields [:input]}))
    (when-not (= :gravity/stage1-reader-form-records (:output binary))
      (stage1-reader-binary-pipeline-fail!
       "STAGE1BIN004" reader-source-path binary
       {:missing-fields [:output]}))
    (when-not (and (vector? direct-stages)
                   (= required-stages (mapv :op direct-stages)))
      (stage1-reader-binary-pipeline-fail!
       "STAGE1BIN004" reader-source-path binary
       {:missing-fields [:direct-stages]}))
    (when-not (set/subset? required-runtimes (set (:uses-runtimes binary)))
      (stage1-reader-binary-pipeline-fail!
       "STAGE1BIN004" reader-source-path binary
       {:missing-fields [:uses-runtimes]}))
    (when-not (set/subset? required-executors (set (:uses-executors binary)))
      (stage1-reader-binary-pipeline-fail!
       "STAGE1BIN004" reader-source-path binary
       {:missing-fields [:uses-executors]}))
    (when-not (= :gravity-source (get-in binary [:provenance :owner]))
      (stage1-reader-binary-pipeline-fail!
       "STAGE1BIN004" reader-source-path binary
       {:missing-fields [:provenance :owner]}))
    (when-not (= :replace-clojure-instruction-executor
                 (get-in binary [:provenance :retirement-objective]))
      (stage1-reader-binary-pipeline-fail!
       "STAGE1BIN004" reader-source-path binary
       {:missing-fields [:provenance :retirement-objective]}))
    (when (seq missing-diagnostics)
      (stage1-reader-binary-pipeline-fail!
       "STAGE1BIN004" reader-source-path diagnostics
       {:missing-fields (vec missing-diagnostics)}))
    (assoc binary
           :emitted-binary-id
           (str "sha256:" (sha256-hex (pr-str binary)))
           :linked-compiled-program-id
           (:compiled-program-id compiled-program))))

(defn stage1-reader-binary-entrypoint-valid?
  [definitions]
  (let [definition (get definitions stage1-reader-binary-pipeline-entrypoint)]
    (and (= :defn (:kind definition))
         (= '[source-path source-text] (:params definition))
         (= '((reader/execute-binary
               source-path
               source-text
               stage1-reader-emitted-binary))
            (:body definition)))))

(defn stage1-reader-execute-emitted-binary
  [reader-source-path definitions source-path source-text emitted-binary]
  (let [source-runtime (stage1-reader-binary-literal-definition-value
                        reader-source-path definitions
                        'stage1-reader-source-runtime)
        classifier (stage1-reader-binary-literal-definition-value
                    reader-source-path definitions
                    'stage1-reader-token-classifier)
        realizer (stage1-reader-binary-literal-definition-value
                  reader-source-path definitions
                  'stage1-reader-token-realizer)
        automaton (stage1-reader-binary-literal-definition-value
                   reader-source-path definitions
                   'stage1-reader-token-automaton)
        token-executor (stage1-reader-binary-literal-definition-value
                        reader-source-path definitions
                        'stage1-reader-token-automaton-executor)
        form-builder (stage1-reader-binary-literal-definition-value
                      reader-source-path definitions
                      'stage1-reader-form-builder)
        form-executor (stage1-reader-binary-literal-definition-value
                       reader-source-path definitions
                       'stage1-reader-form-builder-executor)
        character-stream (stage1-reader-character-stream-from-runtime
                          source-path source-text source-runtime)
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
                   (assoc :emitted-reader-binary emitted-binary)
                   (assoc :reader-binary-direct-stages
                          (mapv :op (:direct-stages emitted-binary)))
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
                   (assoc :source-runtime-created
                          {:character-count
                           (:character-count character-stream)
                           :source-runtime-engine
                           (:source-runtime-engine character-stream)})
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

(defn stage1-reader-execute-binary-pipeline
  [reader-source-path source-path source-text]
  (let [forms (stage1-reader-source-forms reader-source-path)
        definitions (stage1-reader-definition-map reader-source-path forms)
        compiled-program
        (stage1-reader-compiled-program-from-definitions reader-source-path
                                                         definitions)
        emitted-binary
        (stage1-reader-emitted-binary-from-definitions
         reader-source-path definitions compiled-program)]
    (when-not (stage1-reader-binary-entrypoint-valid? definitions)
      (stage1-reader-binary-pipeline-fail!
       "STAGE1BIN001" reader-source-path
       stage1-reader-binary-pipeline-entrypoint
       {:missing-fields [stage1-reader-binary-pipeline-entrypoint]}))
    {:records
     (stage1-reader-execute-emitted-binary
      reader-source-path definitions source-path source-text emitted-binary)
     :compiled-program compiled-program
     :emitted-binary emitted-binary}))

(defn stage1-reader-binary-pipeline-diagnostic-stream
  [source-path binary-pipeline-id]
  {:artifact :gravity/stage1-reader-binary-pipeline-diagnostic-stream
   :stage :stage1-reader-binary-pipeline
   :source-path source-path
   :binary-pipeline-id binary-pipeline-id
   :diagnostics
   (mapv (fn [id]
           {:diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :stage1-reader-binary-pipeline
            :artifact :gravity/diagnostic
            :message
            (or (stage1-reader-binary-pipeline-diagnostic-messages id)
                (stage1-reader-execution-diagnostic-messages id))})
         (concat stage1-reader-binary-pipeline-diagnostic-ids
                 (butlast stage1-reader-execution-diagnostic-ids)))
   :status :complete})