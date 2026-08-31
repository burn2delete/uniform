

(defn stage1-reader-execute-compiled-program
  [reader-source-path definitions source-path source-text compiled-program]
  (loop [env {:source-path source-path
              :source-text source-text}
         instructions (:instructions compiled-program)]
    (if (empty? instructions)
      (let [records (:forms env)]
        (when-not (vector? records)
          (stage1-reader-compiled-pipeline-fail!
           "STAGE1COMP004" source-path env
           {:missing-fields [:forms]}))
        records)
      (let [instruction (first instructions)]
        (case (:op instruction)
          :stage1-create-character-stream
          (let [source-runtime (stage1-reader-compiled-ref
                                reader-source-path definitions env
                                (:runtime instruction))
                character-stream
                (stage1-reader-character-stream-from-runtime
                 source-path source-text source-runtime)]
            (when *stage1-reader-pipeline-trace*
              (swap! *stage1-reader-pipeline-trace*
                     (fn [trace]
                       (-> (or trace {})
                           (update :gravity-runtimes (fnil conj [])
                                   :stage1-reader-source-runtime)
                           (assoc :compiled-program compiled-program)
                           (assoc :source-runtime source-runtime)
                           (assoc :character-stream character-stream)
                           (assoc :source-runtime-created
                                  {:character-count
                                   (:character-count character-stream)
                                   :source-runtime-engine
                                   (:source-runtime-engine
                                    character-stream)})))))
            (recur (assoc env (:label instruction) character-stream)
                   (rest instructions)))

          :stage1-execute-token-automaton
          (let [classifier (stage1-reader-compiled-ref
                            reader-source-path definitions env
                            (:classifier instruction))
                realizer (stage1-reader-compiled-ref
                          reader-source-path definitions env
                          (:realizer instruction))
                automaton (stage1-reader-compiled-ref
                           reader-source-path definitions env
                           (:automaton instruction))
                executor (stage1-reader-compiled-ref
                          reader-source-path definitions env
                          (:executor instruction))
                character-stream (stage1-reader-compiled-ref
                                  reader-source-path definitions env
                                  (:character-stream instruction))
                token-stream
                (stage1-reader-token-stream-from-executor
                 source-path source-text classifier realizer automaton
                 executor character-stream)]
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
                                   (:character-count character-stream)
                                   :token-count (:token-count token-stream)
                                   :operation-count
                                   (count (:executed-operations
                                           token-stream))})))))
            (recur (assoc env (:label instruction) token-stream)
                   (rest instructions)))

          :stage1-execute-form-builder
          (let [classifier (stage1-reader-compiled-ref
                            reader-source-path definitions env
                            (:classifier instruction))
                realizer (stage1-reader-compiled-ref
                          reader-source-path definitions env
                          (:realizer instruction))
                automaton (stage1-reader-compiled-ref
                           reader-source-path definitions env
                           (:automaton instruction))
                form-builder (stage1-reader-compiled-ref
                              reader-source-path definitions env
                              (:form-builder instruction))
                executor (stage1-reader-compiled-ref
                          reader-source-path definitions env
                          (:executor instruction))
                token-stream (stage1-reader-compiled-ref
                              reader-source-path definitions env
                              (:token-stream instruction))
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
            (recur (assoc env (:label instruction) records)
                   (rest instructions)))

          (stage1-reader-compiled-pipeline-fail!
           "STAGE1COMP004" reader-source-path instruction
           {:missing-fields [:op]}))))))