

(def stage1-reader-algorithm-entrypoint
  'stage1-read-source)

(def stage1-reader-algorithm-diagnostic-messages
  {"STAGE1ALGO001" "stage1 reader algorithm entrypoint is missing"
   "STAGE1ALGO002" "stage1 reader algorithm used unsupported executable Gravity"
   "STAGE1ALGO003" "stage1 reader algorithm requested an unsupported host primitive"
   "STAGE1ALGO004" "stage1 reader algorithm output diverged from stage0 reader forms"})

(def stage1-reader-algorithm-diagnostic-ids
  ["STAGE1ALGO001" "STAGE1ALGO002" "STAGE1ALGO003" "STAGE1ALGO004"])

(def stage1-reader-algorithm-rejected-fixture-records
  (vec
   (concat
    stage1-reader-execution-rejected-fixture-records
    [{:fixture stage1-reader-source-path
      :diagnostic "STAGE1ALGO001"
      :rejected-behavior :missing-gravity-reader-entrypoint}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1ALGO002"
      :rejected-behavior :unsupported-gravity-reader-form}
     {:fixture stage1-reader-source-path
      :diagnostic "STAGE1ALGO003"
      :rejected-behavior :unsupported-host-primitive}
     {:fixture "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
      :diagnostic "STAGE1ALGO004"
      :rejected-behavior :stage0-reader-divergence}])))

(defn stage1-reader-algorithm-fail!
  [id source-path value data]
  (fail! id
         (get stage1-reader-algorithm-diagnostic-messages
              id
              "stage1 reader algorithm execution failed")
         (merge {:source-span {:source source-path}
                 :stage :stage1-reader-algorithm
                 :diagnostic-family :stage1-reader-algorithm
                 :value value
                 :remediation "Keep the reader algorithm entrypoint in executable Gravity source and preserve stage0 reader form parity until the Clojure seed is retired."}
                data)))

(defn stage1-reader-source-forms
  [reader-source-path]
  (mapv :form (read-source-form-records reader-source-path
                                         (slurp reader-source-path))))

(defn stage1-reader-definition-map
  [reader-source-path forms]
  (into {}
        (keep (fn [form]
                (when (seq? form)
                  (case (first form)
                    def
                    (do
                      (when-not (symbol? (second form))
                        (stage1-reader-algorithm-fail!
                         "STAGE1ALGO002" reader-source-path form
                         {:missing-fields [:def-name]}))
                      [(second form) {:kind :def
                                      :form form
                                      :value-form (nth form 2 nil)}])
                    defn
                    (do
                      (when-not (and (symbol? (second form))
                                     (vector? (nth form 2 nil)))
                        (stage1-reader-algorithm-fail!
                         "STAGE1ALGO002" reader-source-path form
                         {:missing-fields [:defn-name :params]}))
                      [(second form) {:kind :defn
                                      :form form
                                      :params (nth form 2)
                                      :body (drop 3 form)}])
                    nil))))
        forms))

(declare stage1-reader-eval-gravity)

(defn stage1-reader-eval-body
  [reader-source-path definitions env body]
  (reduce (fn [_ form]
            (stage1-reader-eval-gravity reader-source-path definitions env form))
          nil
          body))

(defn stage1-reader-execute-gravity-function
  [reader-source-path definitions function-symbol args]
  (let [definition (get definitions function-symbol)]
    (when-not (= :defn (:kind definition))
      (stage1-reader-algorithm-fail!
       "STAGE1ALGO001" reader-source-path function-symbol
       {:missing-fields [function-symbol]}))
    (let [params (:params definition)]
      (when-not (= (count params) (count args))
        (stage1-reader-algorithm-fail!
         "STAGE1ALGO002" reader-source-path (:form definition)
         {:params params :actual-count (count args)}))
      (cond
        (= function-symbol 'stage1-create-character-stream)
        (let [[source-path source-text source-runtime] args
              character-stream (stage1-reader-character-stream-from-runtime
                                source-path source-text source-runtime)]
          (when *stage1-reader-pipeline-trace*
            (swap! *stage1-reader-pipeline-trace*
                   (fn [trace]
                     (-> (or trace {})
                         (update :gravity-runtimes (fnil conj [])
                                 :stage1-reader-source-runtime)
                         (assoc :source-runtime source-runtime)
                         (assoc :character-stream character-stream)
                         (assoc :source-runtime-created
                                {:character-count
                                 (:character-count character-stream)
                                 :source-runtime-engine
                                 (:source-runtime-engine
                                  character-stream)})))))
          character-stream)

        (= function-symbol 'stage1-execute-token-automaton)
        (let [[source-path source-text classifier realizer automaton
               executor character-stream] args
              token-stream (stage1-reader-token-stream-from-executor
                            source-path source-text classifier realizer
                            automaton executor character-stream)]
          (when *stage1-reader-pipeline-trace*
            (swap! *stage1-reader-pipeline-trace*
                   (fn [trace]
                     (-> (or trace {})
                         (update :gravity-executors (fnil conj [])
                                 :stage1-reader-token-automaton-executor)
                         (assoc :token-classifier classifier)
                         (assoc :token-realizer realizer)
                         (assoc :token-automaton automaton)
                         (assoc :token-automaton-executor executor)
                         (assoc :token-stream token-stream)
                         (assoc :token-automaton-executed
                                {:character-count
                                 (get character-stream :character-count)
                                 :token-count (:token-count token-stream)
                                 :operation-count
                                 (count (:executed-operations token-stream))})))))
          token-stream)

        (= function-symbol 'stage1-execute-form-builder)
        (let [[source-path source-text classifier realizer automaton
               form-builder executor token-stream] args
              records (stage1-reader-records-from-executor
                       source-path source-text classifier realizer automaton
                       form-builder executor token-stream)]
          (when *stage1-reader-pipeline-trace*
            (swap! *stage1-reader-pipeline-trace*
                   (fn [trace]
                     (-> (or trace {})
                         (update :gravity-executors (fnil conj [])
                                 :stage1-reader-form-builder-executor)
                         (assoc :token-classifier classifier)
                         (assoc :token-realizer realizer)
                         (assoc :token-automaton automaton)
                         (assoc :form-builder form-builder)
                         (assoc :form-builder-executor executor)
                         (assoc :forms-built
                                {:token-count (:token-count token-stream)
                                 :form-count (count records)})))))
          records)

        :else
        (stage1-reader-eval-body reader-source-path
                                 definitions
                                 (zipmap params args)
                                 (:body definition))))))