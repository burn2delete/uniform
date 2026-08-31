

(defn stage1-reader-binary-pipeline-proof
  [artifact]
  (let [diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:stage1-reader-binary-pipeline-diagnostic-stream
                                       :diagnostics])))
        emitted-binary (:stage1-reader-emitted-binary artifact)
        compiled-program (:stage1-reader-compiled-program artifact)
        source-runtime (:stage1-reader-source-runtime artifact)
        character-stream (:stage1-reader-character-stream artifact)
        token-stream (:stage1-reader-token-stream artifact)
        records (:stage1-reader-records artifact)
        direct-stages (mapv :op (:direct-stages emitted-binary))
        gravity-runtimes (set (:gravity-runtimes artifact))
        gravity-executors (set (:gravity-executors artifact))]
    {:gravity-reader-binary-pipeline-entrypoint-verified?
     (= stage1-reader-binary-pipeline-entrypoint (:gravity-entrypoint artifact))
     :gravity-reader-source-verified?
     (= :complete (get-in artifact
                          [:stage1-bootstrap-source-artifact
                           :capability-based-proof
                           :status]))
     :gravity-emitted-binary-authored?
     (and (= :gravity-reader-binary-v1 (:engine emitted-binary))
          (= :gravity-source (get-in emitted-binary [:provenance :owner]))
          (= :reader-emitted-binary
             (get-in emitted-binary [:provenance :purpose])))
     :gravity-emitted-binary-direct-stages-covered?
     (= [:stage1-binary-create-character-stream
         :stage1-binary-execute-token-automaton
         :stage1-binary-execute-form-builder]
        direct-stages)
     :gravity-compiled-program-linked?
     (and (= :gravity-reader-compiled-program-v1 (:engine compiled-program))
          (= :stage1-reader-compiled-program
             (:emitted-from emitted-binary))
          (= (:compiled-program-id compiled-program)
             (:linked-compiled-program-id emitted-binary)))
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
     :instruction-executor-replaced?
     (false? (get-in artifact [:trusted-boundary
                               :clojure-instruction-executor?]))
     :binary-runner-boundary-explicit?
     (true? (get-in artifact [:trusted-boundary
                              :clojure-binary-runner?]))
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
     (set/subset? (set (concat stage1-reader-binary-pipeline-diagnostic-ids
                                (butlast stage1-reader-execution-diagnostic-ids)))
                  diagnostics)
     :limitations
     {:clojure-runtime-interpreter? false
      :clojure-instruction-executor? false
      :clojure-binary-runner? true
      :clojure-character-stream-implementation? true
      :clojure-seed-builtins? true
      :clojure-seed-retired? false
      :next-required-capability
      :replace-clojure-binary-runner-and-character-stream-with-self-hosted-reader-runtime}
     :status :complete}))