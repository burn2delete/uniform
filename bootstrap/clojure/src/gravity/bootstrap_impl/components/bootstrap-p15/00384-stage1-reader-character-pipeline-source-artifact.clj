

(defn stage1-reader-character-pipeline-source-artifact
  [source-path source-text]
  (let [stage1-bootstrap-artifact
        (stage1-bootstrap-source-artifact stage1-reader-bootstrap-source-root)
        trace (atom {})
        stage1-records
        (binding [*stage1-reader-pipeline-trace* trace]
          (stage1-reader-execute-gravity-character-pipeline
           stage1-reader-source-path source-path source-text))
        trace-value @trace
        character-stream (:character-stream trace-value)
        token-stream (:token-stream trace-value)
        host-primitives (vec (distinct (:host-primitives trace-value)))
        stage0-records (read-source-form-records source-path source-text)
        stage1-forms (mapv :form stage1-records)
        stage0-forms (mapv :form stage0-records)
        comparison {:forms-equal? (= stage1-forms stage0-forms)
                    :top-level-count-matches? (= (count stage1-records)
                                                 (count stage0-records))
                    :stage1-form-count (count stage1-records)
                    :stage0-form-count (count stage0-records)}
        character-pipeline-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:reader-source stage1-reader-source-path
                       :entrypoint stage1-reader-character-pipeline-entrypoint
                       :forms (stage1-reader-source-forms
                               stage1-reader-source-path)})))
        artifact-base
        {:kind :gravity/stage1-reader-character-pipeline-artifact
         :phase "15"
         :task "P15-S5"
         :stage :stage1-reader-character-pipeline
         :source-path source-path
         :reader-source-path stage1-reader-source-path
         :gravity-entrypoint stage1-reader-character-pipeline-entrypoint
         :character-pipeline-id character-pipeline-id
         :host-primitives host-primitives
         :stage1-bootstrap-source-artifact stage1-bootstrap-artifact
         :stage1-reader-character-stream character-stream
         :stage1-reader-token-stream token-stream
         :stage1-reader-records stage1-records
         :stage1-reader-character-pipeline-trace
         (dissoc trace-value :character-stream :token-stream)
         :stage0-comparison comparison
         :accepted-stage1-reader-character-pipeline-fixtures
         [{:fixture "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
           :status :accepted
           :comparison comparison
           :character-count (:character-count character-stream)
           :token-count (:token-count token-stream)}]
         :rejected-stage1-reader-character-pipeline-fixtures
         stage1-reader-character-pipeline-rejected-fixture-records
         :stage1-reader-character-pipeline-diagnostic-stream
         (stage1-reader-character-pipeline-diagnostic-stream
          source-path character-pipeline-id)
         :stage1-reader-character-pipeline-results
         {:accepted-fixtures 1
          :rejected-fixtures
          (count stage1-reader-character-pipeline-rejected-fixture-records)
          :diagnostic-count
          (+ (count stage1-reader-character-pipeline-diagnostic-ids)
             (dec (count stage1-reader-execution-diagnostic-ids)))
          :character-count (:character-count character-stream)
          :token-count (:token-count token-stream)
          :form-count (count stage1-records)
          :status :complete}
         :diagnostics []}
        capability-proof
        (stage1-reader-character-pipeline-proof artifact-base)]
    (when-not (:forms-equal? comparison)
      (stage1-reader-character-pipeline-fail!
       "STAGE1CHAR005" source-path comparison
       {:missing-fields [:stage0-form-parity]}))
    (when-not (seq host-primitives)
      (stage1-reader-character-pipeline-fail!
       "STAGE1CHAR003" source-path trace-value
       {:missing-fields [:host-primitives]}))
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn stage1-reader-character-pipeline-file-artifact
  [path]
  (stage1-reader-character-pipeline-source-artifact path (slurp path)))

(defn stage1-reader-execute-gravity-token-classifier-pipeline
  [reader-source-path source-path source-text]
  (let [forms (stage1-reader-source-forms reader-source-path)
        definitions (stage1-reader-definition-map reader-source-path forms)]
    (when-not (= :defn (:kind (get definitions
                                    stage1-reader-token-classifier-pipeline-entrypoint)))
      (stage1-reader-token-classifier-pipeline-fail!
       "STAGE1CLASS001" reader-source-path
       stage1-reader-token-classifier-pipeline-entrypoint
       {:missing-fields [stage1-reader-token-classifier-pipeline-entrypoint]}))
    (stage1-reader-execute-gravity-function
     reader-source-path definitions
     stage1-reader-token-classifier-pipeline-entrypoint
     [source-path source-text])))

(defn stage1-reader-token-classifier-pipeline-diagnostic-stream
  [source-path token-classifier-pipeline-id]
  {:artifact :gravity/stage1-reader-token-classifier-pipeline-diagnostic-stream
   :stage :stage1-reader-token-classifier-pipeline
   :source-path source-path
   :token-classifier-pipeline-id token-classifier-pipeline-id
   :diagnostics
   (mapv (fn [id]
           {:diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :stage1-reader-token-classifier-pipeline
            :artifact :gravity/diagnostic
            :message
            (or (stage1-reader-token-classifier-pipeline-diagnostic-messages id)
                (stage1-reader-execution-diagnostic-messages id))})
         (concat stage1-reader-token-classifier-pipeline-diagnostic-ids
                 (butlast stage1-reader-execution-diagnostic-ids)))
   :status :complete})

(defn stage1-reader-token-classifier-pipeline-proof
  [artifact]
  (let [diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:stage1-reader-token-classifier-pipeline-diagnostic-stream
                                       :diagnostics])))
        character-stream (:stage1-reader-character-stream artifact)
        characters (:characters character-stream)
        token-classifier (:stage1-reader-token-classifier artifact)
        token-stream (:stage1-reader-token-stream artifact)
        tokens (:tokens token-stream)]
    {:gravity-reader-token-classifier-pipeline-entrypoint-executed?
     (= stage1-reader-token-classifier-pipeline-entrypoint
        (:gravity-entrypoint artifact))
     :gravity-reader-source-verified?
     (= :complete (get-in artifact
                          [:stage1-bootstrap-source-artifact
                           :capability-based-proof
                           :status]))
     :gravity-token-classifier-authored?
     (= :gravity-reader-token-classifier-v1 (:engine token-classifier))
     :host-primitive-boundary-split?
     (= [:reader/source-characters
         :reader/tokens-from-classifier
         :reader/forms-from-tokens]
        (:host-primitives artifact))
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
     :token-classifier-covered?
     (and (= :gravity-reader-token-classifier-v1 (:engine token-classifier))
          (= :stage1-reader-table (:derived-from token-classifier))
          (= :gravity-source (get-in token-classifier [:provenance :owner]))
          (contains? (:diagnostics token-classifier) :invalid-classifier))
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
     (set/subset? (set (concat stage1-reader-token-classifier-pipeline-diagnostic-ids
                                (butlast stage1-reader-execution-diagnostic-ids)))
                  diagnostics)
     :limitations
     {:clojure-seed-evaluator? true
      :host-character-stream? true
      :host-token-realizer? true
      :host-form-builder? true
      :clojure-seed-retired? false
      :next-required-capability
      :replace-token-realizer-and-form-builder-host-primitives-with-gravity-code}
     :status :complete}))