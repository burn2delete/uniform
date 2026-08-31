

(defn stage1-reader-binary-pipeline-source-artifact
  [source-path source-text]
  (let [stage1-bootstrap-artifact
        (stage1-bootstrap-source-artifact stage1-reader-bootstrap-source-root)
        trace (atom {})
        binary-result
        (binding [*stage1-reader-pipeline-trace* trace]
          (stage1-reader-execute-binary-pipeline
           stage1-reader-source-path source-path source-text))
        stage1-records (:records binary-result)
        trace-value @trace
        emitted-binary (:emitted-binary binary-result)
        compiled-program (:compiled-program binary-result)
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
        binary-pipeline-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:reader-source stage1-reader-source-path
                       :entrypoint stage1-reader-binary-pipeline-entrypoint
                       :emitted-binary emitted-binary
                       :compiled-program-id
                       (:compiled-program-id compiled-program)})))]
    (let [artifact-base
          {:kind :gravity/stage1-reader-binary-pipeline-artifact
         :phase "15"
         :task "P15-S13"
         :stage :stage1-reader-binary-pipeline
         :source-path source-path
         :reader-source-path stage1-reader-source-path
         :gravity-entrypoint stage1-reader-binary-pipeline-entrypoint
         :binary-pipeline-id binary-pipeline-id
         :reader-binary-id (:emitted-binary-id emitted-binary)
         :host-primitives host-primitives
         :gravity-runtimes gravity-runtimes
         :gravity-executors gravity-executors
         :trusted-boundary
         {:clojure-runtime-interpreter? false
          :clojure-instruction-executor? false
          :clojure-binary-runner? true
          :clojure-character-stream-implementation? true}
         :stage1-bootstrap-source-artifact stage1-bootstrap-artifact
         :stage1-reader-compiled-program compiled-program
         :stage1-reader-emitted-binary emitted-binary
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
         :stage1-reader-binary-pipeline-trace
         (dissoc trace-value :character-stream :token-stream
                 :token-classifier :token-realizer :token-automaton
                 :token-automaton-executor :form-builder
                 :form-builder-executor :source-runtime
                 :emitted-reader-binary)
         :stage0-comparison comparison
         :accepted-stage1-reader-binary-pipeline-fixtures
         [{:fixture "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
           :status :accepted
           :comparison comparison
           :character-count (:character-count character-stream)
           :token-count (:token-count token-stream)
           :form-count (count stage1-records)}]
         :rejected-stage1-reader-binary-pipeline-fixtures
         stage1-reader-binary-pipeline-rejected-fixture-records
         :stage1-reader-binary-pipeline-diagnostic-stream
         (stage1-reader-binary-pipeline-diagnostic-stream
          source-path binary-pipeline-id)
         :stage1-reader-binary-pipeline-results
         {:accepted-fixtures 1
          :rejected-fixtures
          (count stage1-reader-binary-pipeline-rejected-fixture-records)
          :diagnostic-count
          (+ (count stage1-reader-binary-pipeline-diagnostic-ids)
             (dec (count stage1-reader-execution-diagnostic-ids)))
	         :character-count (:character-count character-stream)
	          :token-count (:token-count token-stream)
	         :form-count (count stage1-records)
	          :status :complete}
	         :diagnostics []}]
	      (when-not (:forms-equal? comparison)
	        (stage1-reader-binary-pipeline-fail!
	         "STAGE1BIN005" source-path comparison
         {:missing-fields [:stage0-form-parity]}))
      (when (seq host-primitives)
        (stage1-reader-binary-pipeline-fail!
         "STAGE1BIN003" source-path trace-value
         {:host-primitives host-primitives}))
      (when-not (and (false? (get-in artifact-base
                                     [:trusted-boundary
                                      :clojure-runtime-interpreter?]))
                     (false? (get-in artifact-base
                                     [:trusted-boundary
                                      :clojure-instruction-executor?]))
                     (true? (get-in artifact-base
                                    [:trusted-boundary
                                     :clojure-binary-runner?])))
	        (stage1-reader-binary-pipeline-fail!
	         "STAGE1BIN004" source-path artifact-base
	         {:missing-fields [:trusted-boundary]}))
	      (let [capability-proof
	            (stage1-reader-binary-pipeline-proof artifact-base)]
	        (assoc artifact-base
	               :capability-based-proof capability-proof
	               :artifact-id (c4-artifact-id (assoc artifact-base
	                                                   :capability-based-proof
	                                                   capability-proof)))))))

(defn stage1-reader-binary-pipeline-file-artifact
  [path]
  (stage1-reader-binary-pipeline-source-artifact path (slurp path)))

(defn stage1-reader-self-hosted-runtime-literal-definition-value
  [reader-source-path definitions symbol-name]
  (let [definition (get definitions symbol-name)]
    (when-not (= :def (:kind definition))
      (stage1-reader-self-hosted-runtime-fail!
       "STAGE1SELF004" reader-source-path symbol-name
       {:missing-fields [symbol-name]}))
    (:value-form definition)))

(defn stage1-reader-self-hosted-runtime-from-definitions
  [reader-source-path definitions]
  (let [runtime (stage1-reader-self-hosted-runtime-literal-definition-value
                 reader-source-path definitions
                 'stage1-reader-self-hosted-runtime)
        diagnostics (:diagnostics runtime)
        missing-diagnostics (remove #(contains? diagnostics %)
                                    [:missing-entrypoint :unsupported-form
                                     :unsupported-host-primitive
                                     :invalid-runtime :stage0-divergence])
        required-stages [:stage1-self-hosted-create-character-stream
                         :stage1-self-hosted-execute-token-automaton
                         :stage1-self-hosted-execute-form-builder]
        required-replacements #{:clojure-binary-runner
                                :clojure-character-stream-implementation}
        required-runtimes #{:stage1-reader-self-hosted-runtime
                            :stage1-reader-source-runtime}
        required-executors #{:stage1-reader-token-automaton-executor
                             :stage1-reader-form-builder-executor}
        direct-stages (:direct-stages runtime)]
    (when-not (map? runtime)
      (stage1-reader-self-hosted-runtime-fail!
       "STAGE1SELF004" reader-source-path runtime
       {:missing-fields [:stage1-reader-self-hosted-runtime]}))
    (when-not (= :gravity-reader-self-hosted-runtime-v1 (:engine runtime))
      (stage1-reader-self-hosted-runtime-fail!
       "STAGE1SELF004" reader-source-path runtime
       {:missing-fields [:engine]}))
    (doseq [field [:entrypoint :replaces :input :output :source-runtime
                   :direct-stages :uses-runtimes :uses-executors
                   :preserves :diagnostics :provenance]]
      (when-not (contains? runtime field)
        (stage1-reader-self-hosted-runtime-fail!
         "STAGE1SELF004" reader-source-path runtime
         {:missing-fields [field]})))
    (when-not (= :stage1-read-source-self-hosted-runtime
                 (:entrypoint runtime))
      (stage1-reader-self-hosted-runtime-fail!
       "STAGE1SELF004" reader-source-path runtime
       {:missing-fields [:entrypoint]}))
    (when-not (set/subset? required-replacements
                           (set (:replaces runtime)))
      (stage1-reader-self-hosted-runtime-fail!
       "STAGE1SELF004" reader-source-path runtime
       {:missing-fields [:replaces]}))
    (when-not (= [:source-path :source-text] (:input runtime))
      (stage1-reader-self-hosted-runtime-fail!
       "STAGE1SELF004" reader-source-path runtime
       {:missing-fields [:input]}))
    (when-not (= :gravity/stage1-reader-form-records (:output runtime))
      (stage1-reader-self-hosted-runtime-fail!
       "STAGE1SELF004" reader-source-path runtime
       {:missing-fields [:output]}))
    (when-not (= :stage1-reader-source-runtime (:source-runtime runtime))
      (stage1-reader-self-hosted-runtime-fail!
       "STAGE1SELF004" reader-source-path runtime
       {:missing-fields [:source-runtime]}))
    (when-not (and (vector? direct-stages)
                   (= required-stages (mapv :op direct-stages)))
      (stage1-reader-self-hosted-runtime-fail!
       "STAGE1SELF004" reader-source-path runtime
       {:missing-fields [:direct-stages]}))
    (when-not (set/subset? required-runtimes
                           (set (:uses-runtimes runtime)))
      (stage1-reader-self-hosted-runtime-fail!
       "STAGE1SELF004" reader-source-path runtime
       {:missing-fields [:uses-runtimes]}))
    (when-not (set/subset? required-executors
                           (set (:uses-executors runtime)))
      (stage1-reader-self-hosted-runtime-fail!
       "STAGE1SELF004" reader-source-path runtime
       {:missing-fields [:uses-executors]}))
    (when-not (= :gravity-source (get-in runtime [:provenance :owner]))
      (stage1-reader-self-hosted-runtime-fail!
       "STAGE1SELF004" reader-source-path runtime
       {:missing-fields [:provenance :owner]}))
    (when-not (= :reader-self-hosted-runtime
                 (get-in runtime [:provenance :purpose]))
      (stage1-reader-self-hosted-runtime-fail!
       "STAGE1SELF004" reader-source-path runtime
       {:missing-fields [:provenance :purpose]}))
    (when-not (= :replace-clojure-binary-runner-and-character-stream
                 (get-in runtime [:provenance :retirement-objective]))
      (stage1-reader-self-hosted-runtime-fail!
       "STAGE1SELF004" reader-source-path runtime
       {:missing-fields [:provenance :retirement-objective]}))
    (when (seq missing-diagnostics)
      (stage1-reader-self-hosted-runtime-fail!
       "STAGE1SELF004" reader-source-path diagnostics
       {:missing-fields (vec missing-diagnostics)}))
    (assoc runtime
           :self-hosted-runtime-id
           (str "sha256:" (sha256-hex (pr-str runtime))))))