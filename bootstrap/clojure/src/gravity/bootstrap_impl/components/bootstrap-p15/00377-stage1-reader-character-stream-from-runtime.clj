

(defn stage1-reader-character-stream-from-runtime
  [source-path source-text source-runtime]
  (let [diagnostics (:diagnostics source-runtime)
        missing-diagnostics (remove #(contains? diagnostics %)
                                    [:invalid-runtime :source-divergence])
        required-generated #{:source-character-stream :source-span-index
                             :source-id}
        required-forbidden #{:filesystem :network :shell :environment
                             :dynamic-eval}
        required-operations #{:decode-source-text :index-source-characters
                              :attach-source-spans :compute-source-id}]
    (when-not (= :gravity-reader-source-runtime-v1 (:engine source-runtime))
      (stage1-reader-runtime-pipeline-fail!
       "STAGE1RUN004" source-path source-runtime
       {:missing-fields [:source-runtime-engine]}))
    (doseq [field [:family :input :emits :services :operations
                   :effects :capabilities :preserves :diagnostics
                   :provenance]]
      (when-not (contains? source-runtime field)
        (stage1-reader-runtime-pipeline-fail!
         "STAGE1RUN004" source-path source-runtime
         {:missing-fields [field]})))
    (when-not (= :bootstrap-reader (:family source-runtime))
      (stage1-reader-runtime-pipeline-fail!
       "STAGE1RUN004" source-path source-runtime
       {:missing-fields [:family]}))
    (when-not (= :gravity/source-text (:input source-runtime))
      (stage1-reader-runtime-pipeline-fail!
       "STAGE1RUN004" source-path source-runtime
       {:missing-fields [:input]}))
    (when-not (= :gravity/stage1-reader-character-stream
                 (:emits source-runtime))
      (stage1-reader-runtime-pipeline-fail!
       "STAGE1RUN004" source-path source-runtime
       {:missing-fields [:emits]}))
    (when-not (set/subset? required-generated
                           (set (get-in source-runtime
                                        [:services :generated])))
      (stage1-reader-runtime-pipeline-fail!
       "STAGE1RUN004" source-path source-runtime
       {:missing-fields [:services :generated]}))
    (when-not (empty? (get-in source-runtime [:services :delegated]))
      (stage1-reader-runtime-pipeline-fail!
       "STAGE1RUN004" source-path source-runtime
       {:missing-fields [:services :delegated]}))
    (when-not (set/subset? required-forbidden
                           (set (get-in source-runtime
                                        [:services :forbidden])))
      (stage1-reader-runtime-pipeline-fail!
       "STAGE1RUN004" source-path source-runtime
       {:missing-fields [:services :forbidden]}))
    (when-not (set/subset? required-operations
                           (set (:operations source-runtime)))
      (stage1-reader-runtime-pipeline-fail!
       "STAGE1RUN004" source-path source-runtime
       {:missing-fields [:operations]}))
    (when (seq missing-diagnostics)
      (stage1-reader-runtime-pipeline-fail!
       "STAGE1RUN004" source-path diagnostics
       {:missing-fields (vec missing-diagnostics)}))
    (let [source-runtime-id (str "sha256:"
                                 (sha256-hex (pr-str source-runtime)))
          character-stream (stage1-reader-character-stream source-path
                                                           source-text)]
      (assoc character-stream
             :source-runtime-id source-runtime-id
             :source-runtime-engine (:engine source-runtime)
             :gravity-runtime :stage1-reader-source-runtime))))

(defn stage1-reader-evaluator-runtime-from-definitions
  [reader-source-path definitions]
  (let [runtime-form (:value-form (get definitions
                                       'stage1-reader-evaluator-runtime))
        runtime runtime-form
        diagnostics (:diagnostics runtime)
        missing-diagnostics (remove #(contains? diagnostics %)
                                    [:missing-entrypoint :unsupported-form
                                     :unsupported-host-primitive
                                     :invalid-runtime :stage0-divergence])
        required-services #{:stage1-reader-source-runtime
                            :stage1-reader-token-automaton-executor
                            :stage1-reader-form-builder-executor}
        required-evaluator-forms #{:let :function-call :def :defn :quote}]
    (when-not (map? runtime)
      (stage1-reader-runtime-pipeline-fail!
       "STAGE1RUN004" reader-source-path runtime
       {:missing-fields [:stage1-reader-evaluator-runtime]}))
    (when-not (= :gravity-reader-evaluator-runtime-v1 (:engine runtime))
      (stage1-reader-runtime-pipeline-fail!
       "STAGE1RUN004" reader-source-path runtime
       {:missing-fields [:evaluator-runtime-engine]}))
    (doseq [field [:family :executes :evaluates :runtime-services
                   :remaining-trusted-implementation :effects
                   :capabilities :preserves :diagnostics :provenance]]
      (when-not (contains? runtime field)
        (stage1-reader-runtime-pipeline-fail!
         "STAGE1RUN004" reader-source-path runtime
         {:missing-fields [field]})))
    (when-not (= :bootstrap-reader (:family runtime))
      (stage1-reader-runtime-pipeline-fail!
       "STAGE1RUN004" reader-source-path runtime
       {:missing-fields [:family]}))
    (when-not (= :stage1-read-source-runtime-pipeline (:executes runtime))
      (stage1-reader-runtime-pipeline-fail!
       "STAGE1RUN004" reader-source-path runtime
       {:missing-fields [:executes]}))
    (when-not (set/subset? required-evaluator-forms
                           (set (:evaluates runtime)))
      (stage1-reader-runtime-pipeline-fail!
       "STAGE1RUN004" reader-source-path runtime
       {:missing-fields [:evaluates]}))
    (when-not (set/subset? required-services
                           (set (:runtime-services runtime)))
      (stage1-reader-runtime-pipeline-fail!
       "STAGE1RUN004" reader-source-path runtime
       {:missing-fields [:runtime-services]}))
    (when-not (= :clojure-stage0 (:remaining-trusted-implementation runtime))
      (stage1-reader-runtime-pipeline-fail!
       "STAGE1RUN004" reader-source-path runtime
       {:missing-fields [:remaining-trusted-implementation]}))
    (when (seq missing-diagnostics)
      (stage1-reader-runtime-pipeline-fail!
       "STAGE1RUN004" reader-source-path diagnostics
       {:missing-fields (vec missing-diagnostics)}))
    (assoc runtime
           :evaluator-runtime-id
           (str "sha256:" (sha256-hex (pr-str runtime))))))

(defn stage1-reader-literal-definition-value
  [reader-source-path definitions symbol-name]
  (let [definition (get definitions symbol-name)]
    (when-not (= :def (:kind definition))
      (stage1-reader-compiled-pipeline-fail!
       "STAGE1COMP004" reader-source-path symbol-name
       {:missing-fields [symbol-name]}))
    (:value-form definition)))

(defn stage1-reader-compiled-program-from-definitions
  [reader-source-path definitions]
  (let [program (stage1-reader-literal-definition-value
                 reader-source-path definitions
                 'stage1-reader-compiled-program)
        diagnostics (:diagnostics program)
        missing-diagnostics (remove #(contains? diagnostics %)
                                    [:missing-entrypoint :unsupported-form
                                     :unsupported-host-primitive
                                     :invalid-program :stage0-divergence])
        required-instructions [:stage1-create-character-stream
                               :stage1-execute-token-automaton
                               :stage1-execute-form-builder]
        required-runtimes #{:stage1-reader-source-runtime}
        required-executors #{:stage1-reader-token-automaton-executor
                             :stage1-reader-form-builder-executor}
        instructions (:instructions program)]
    (when-not (map? program)
      (stage1-reader-compiled-pipeline-fail!
       "STAGE1COMP004" reader-source-path program
       {:missing-fields [:stage1-reader-compiled-program]}))
    (when-not (= :gravity-reader-compiled-program-v1 (:engine program))
      (stage1-reader-compiled-pipeline-fail!
       "STAGE1COMP004" reader-source-path program
       {:missing-fields [:engine]}))
    (doseq [field [:entrypoint :compiled-from :input :output
                   :instructions :uses-runtimes :uses-executors
                   :preserves :diagnostics :provenance]]
      (when-not (contains? program field)
        (stage1-reader-compiled-pipeline-fail!
         "STAGE1COMP004" reader-source-path program
         {:missing-fields [field]})))
    (when-not (= :stage1-read-source-compiled-pipeline
                 (:entrypoint program))
      (stage1-reader-compiled-pipeline-fail!
       "STAGE1COMP004" reader-source-path program
       {:missing-fields [:entrypoint]}))
    (when-not (= :stage1-read-source-runtime-pipeline
                 (:compiled-from program))
      (stage1-reader-compiled-pipeline-fail!
       "STAGE1COMP004" reader-source-path program
       {:missing-fields [:compiled-from]}))
    (when-not (= [:source-path :source-text] (:input program))
      (stage1-reader-compiled-pipeline-fail!
       "STAGE1COMP004" reader-source-path program
       {:missing-fields [:input]}))
    (when-not (= :gravity/stage1-reader-form-records (:output program))
      (stage1-reader-compiled-pipeline-fail!
       "STAGE1COMP004" reader-source-path program
       {:missing-fields [:output]}))
    (when-not (and (vector? instructions)
                   (= required-instructions
                      (mapv :op instructions)))
      (stage1-reader-compiled-pipeline-fail!
       "STAGE1COMP004" reader-source-path program
       {:missing-fields [:instructions]}))
    (when-not (set/subset? required-runtimes
                           (set (:uses-runtimes program)))
      (stage1-reader-compiled-pipeline-fail!
       "STAGE1COMP004" reader-source-path program
       {:missing-fields [:uses-runtimes]}))
    (when-not (set/subset? required-executors
                           (set (:uses-executors program)))
      (stage1-reader-compiled-pipeline-fail!
       "STAGE1COMP004" reader-source-path program
       {:missing-fields [:uses-executors]}))
    (when (seq missing-diagnostics)
      (stage1-reader-compiled-pipeline-fail!
       "STAGE1COMP004" reader-source-path diagnostics
       {:missing-fields (vec missing-diagnostics)}))
    (assoc program
           :compiled-program-id
           (str "sha256:" (sha256-hex (pr-str program))))))

(defn stage1-reader-compiled-entrypoint-valid?
  [definitions]
  (let [definition (get definitions stage1-reader-compiled-pipeline-entrypoint)]
    (and (= :defn (:kind definition))
         (= '[source-path source-text] (:params definition))
         (= '((reader/execute-compiled-program
               source-path
               source-text
               stage1-reader-compiled-program))
            (:body definition)))))

(defn stage1-reader-compiled-ref
  [reader-source-path definitions env ref]
  (cond
    (contains? env ref) (get env ref)
    (keyword? ref)
    (stage1-reader-literal-definition-value
     reader-source-path
     definitions
     (symbol (name ref)))
    :else
    (stage1-reader-compiled-pipeline-fail!
     "STAGE1COMP004" reader-source-path ref
     {:missing-fields [:compiled-program-ref]})))