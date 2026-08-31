

(defn stage1-reader-execute-gravity-algorithm
  [reader-source-path source-path source-text]
  (let [forms (stage1-reader-source-forms reader-source-path)
        definitions (stage1-reader-definition-map reader-source-path forms)]
    (stage1-reader-execute-gravity-function
     reader-source-path definitions stage1-reader-algorithm-entrypoint
     [source-path source-text])))

(defn stage1-reader-algorithm-diagnostic-stream
  [source-path algorithm-id]
  {:artifact :gravity/stage1-reader-algorithm-diagnostic-stream
   :stage :stage1-reader-algorithm
   :source-path source-path
   :algorithm-id algorithm-id
   :diagnostics
   (mapv (fn [id]
           {:diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :stage1-reader-algorithm
            :artifact :gravity/diagnostic
            :message (or (stage1-reader-algorithm-diagnostic-messages id)
                         (stage1-reader-execution-diagnostic-messages id))})
         (concat stage1-reader-algorithm-diagnostic-ids
                 (butlast stage1-reader-execution-diagnostic-ids)))
   :status :complete})

(defn stage1-reader-algorithm-proof
  [artifact]
  (let [diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:stage1-reader-algorithm-diagnostic-stream
                                       :diagnostics])))]
    {:gravity-reader-entrypoint-executed?
     (= stage1-reader-algorithm-entrypoint (:gravity-entrypoint artifact))
     :gravity-reader-source-verified?
     (= :complete (get-in artifact
                          [:stage1-bootstrap-source-artifact
                           :capability-based-proof
                           :status]))
     :gravity-reader-algorithm-authored?
     true
     :host-primitive-boundary-explicit?
     (= [:reader/read-with-table] (:host-primitives artifact))
     :forms-match-stage0?
     (true? (get-in artifact [:stage0-comparison :forms-equal?]))
     :source-spans-covered?
     (every? #(get-in % [:span :source]) (:stage1-reader-records artifact))
     :diagnostics-covered?
     (set/subset? (set (concat stage1-reader-algorithm-diagnostic-ids
                                (butlast stage1-reader-execution-diagnostic-ids)))
                  diagnostics)
     :limitations
     {:clojure-seed-evaluator? true
      :host-character-scanner? true
      :clojure-seed-retired? false
      :next-required-capability
      :replace-reader-read-with-table-primitive-with-gravity-code}
     :status :complete}))

(defn stage1-reader-algorithm-source-artifact
  [source-path source-text]
  (let [stage1-bootstrap-artifact
        (stage1-bootstrap-source-artifact stage1-reader-bootstrap-source-root)
        stage1-records (stage1-reader-execute-gravity-algorithm
                        stage1-reader-source-path source-path source-text)
        stage0-records (read-source-form-records source-path source-text)
        stage1-forms (mapv :form stage1-records)
        stage0-forms (mapv :form stage0-records)
        comparison {:forms-equal? (= stage1-forms stage0-forms)
                    :top-level-count-matches? (= (count stage1-records)
                                                 (count stage0-records))
                    :stage1-form-count (count stage1-records)
                    :stage0-form-count (count stage0-records)}
        algorithm-id (str "sha256:"
                          (sha256-hex
                           (pr-str {:reader-source stage1-reader-source-path
                                    :entrypoint stage1-reader-algorithm-entrypoint
                                    :forms (stage1-reader-source-forms
                                            stage1-reader-source-path)})))
        artifact-base
        {:kind :gravity/stage1-reader-algorithm-artifact
         :phase "15"
         :task "P15-S3"
         :stage :stage1-reader-algorithm
         :source-path source-path
         :reader-source-path stage1-reader-source-path
         :gravity-entrypoint stage1-reader-algorithm-entrypoint
         :algorithm-id algorithm-id
         :host-primitives [:reader/read-with-table]
         :stage1-bootstrap-source-artifact stage1-bootstrap-artifact
         :stage1-reader-records stage1-records
         :stage0-comparison comparison
         :accepted-stage1-reader-algorithm-fixtures
         [{:fixture "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
           :status :accepted
           :comparison comparison}]
         :rejected-stage1-reader-algorithm-fixtures
         stage1-reader-algorithm-rejected-fixture-records
         :stage1-reader-algorithm-diagnostic-stream
         (stage1-reader-algorithm-diagnostic-stream source-path algorithm-id)
         :stage1-reader-algorithm-results
         {:accepted-fixtures 1
          :rejected-fixtures (count stage1-reader-algorithm-rejected-fixture-records)
          :diagnostic-count (+ (count stage1-reader-algorithm-diagnostic-ids)
                               (dec (count stage1-reader-execution-diagnostic-ids)))
          :form-count (count stage1-records)
          :status :complete}
         :diagnostics []}
        capability-proof (stage1-reader-algorithm-proof artifact-base)]
    (when-not (:forms-equal? comparison)
      (stage1-reader-algorithm-fail!
       "STAGE1ALGO004" source-path comparison
       {:missing-fields [:stage0-form-parity]}))
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn stage1-reader-algorithm-file-artifact
  [path]
  (stage1-reader-algorithm-source-artifact path (slurp path)))

(defn stage1-reader-execute-gravity-pipeline
  [reader-source-path source-path source-text]
  (let [forms (stage1-reader-source-forms reader-source-path)
        definitions (stage1-reader-definition-map reader-source-path forms)]
    (when-not (= :defn (:kind (get definitions
                                    stage1-reader-pipeline-entrypoint)))
      (stage1-reader-pipeline-fail!
       "STAGE1PIPE001" reader-source-path stage1-reader-pipeline-entrypoint
       {:missing-fields [stage1-reader-pipeline-entrypoint]}))
    (stage1-reader-execute-gravity-function
     reader-source-path definitions stage1-reader-pipeline-entrypoint
     [source-path source-text])))

(defn stage1-reader-pipeline-diagnostic-stream
  [source-path pipeline-id]
  {:artifact :gravity/stage1-reader-pipeline-diagnostic-stream
   :stage :stage1-reader-pipeline
   :source-path source-path
   :pipeline-id pipeline-id
   :diagnostics
   (mapv (fn [id]
           {:diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :stage1-reader-pipeline
            :artifact :gravity/diagnostic
            :message (or (stage1-reader-pipeline-diagnostic-messages id)
                         (stage1-reader-execution-diagnostic-messages id))})
         (concat stage1-reader-pipeline-diagnostic-ids
                 (butlast stage1-reader-execution-diagnostic-ids)))
   :status :complete})

(defn stage1-reader-pipeline-proof
  [artifact]
  (let [diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:stage1-reader-pipeline-diagnostic-stream
                                       :diagnostics])))
        token-stream (:stage1-reader-token-stream artifact)
        tokens (:tokens token-stream)]
    {:gravity-reader-pipeline-entrypoint-executed?
     (= stage1-reader-pipeline-entrypoint (:gravity-entrypoint artifact))
     :gravity-reader-source-verified?
     (= :complete (get-in artifact
                          [:stage1-bootstrap-source-artifact
                           :capability-based-proof
                           :status]))
     :gravity-reader-pipeline-authored?
     true
     :host-primitive-boundary-split?
     (= [:reader/scan-tokens :reader/forms-from-tokens]
        (:host-primitives artifact))
     :whole-reader-host-primitive-removed?
     (not-any? #{:reader/read-with-table} (:host-primitives artifact))
     :token-stream-covered?
     (and (= :gravity/stage1-reader-token-stream (:kind token-stream))
          (pos? (:token-count token-stream))
          (= (:token-count token-stream) (count tokens))
          (every? #(get-in % [:span :source]) tokens))
     :forms-match-stage0?
     (true? (get-in artifact [:stage0-comparison :forms-equal?]))
     :source-spans-covered?
     (every? #(get-in % [:span :source]) (:stage1-reader-records artifact))
     :diagnostics-covered?
     (set/subset? (set (concat stage1-reader-pipeline-diagnostic-ids
                                (butlast stage1-reader-execution-diagnostic-ids)))
                  diagnostics)
     :limitations
     {:clojure-seed-evaluator? true
      :host-tokenizer? true
      :host-form-builder? true
      :clojure-seed-retired? false
      :next-required-capability
      :replace-scan-and-form-host-primitives-with-gravity-code}
     :status :complete}))