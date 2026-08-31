

(defn stage1-reader-execution-proof
  [artifact]
  (let [diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:stage1-reader-diagnostic-stream
                                       :diagnostics])))]
    {:gravity-reader-table-used?
     (= :gravity.bootstrap.reader/stage1-reader-table
        (:reader-table-symbol artifact))
     :gravity-reader-source-verified?
     (= :complete (get-in artifact
                          [:stage1-bootstrap-source-artifact
                           :capability-based-proof
                           :status]))
     :forms-match-stage0?
     (true? (get-in artifact [:stage0-comparison :forms-equal?]))
     :top-level-count-matches?
     (true? (get-in artifact [:stage0-comparison
                              :top-level-count-matches?]))
     :source-spans-covered?
     (every? #(get-in % [:span :source]) (:stage1-reader-records artifact))
     :diagnostics-covered?
     (set/subset? (set (butlast stage1-reader-execution-diagnostic-ids))
                  diagnostics)
     :limitations
     {:clojure-host-interpreter? true
      :gravity-reader-algorithm-authored? false
      :clojure-seed-retired? false
      :next-required-capability
      :move-reader-algorithm-into-executable-gravity}
     :status :complete}))

(defn stage1-reader-execution-source-artifact
  [source-path source-text]
  (let [table (stage1-reader-table)
        table-id (str "sha256:" (sha256-hex (pr-str table)))
        stage1-bootstrap-artifact
        (stage1-bootstrap-source-artifact stage1-reader-bootstrap-source-root)
        stage1-records (stage1-reader-table-driven-records source-path
                                                           source-text
                                                           table)
        stage0-records (read-source-form-records source-path source-text)
        stage1-forms (mapv :form stage1-records)
        stage0-forms (mapv :form stage0-records)
        comparison {:forms-equal? (= stage1-forms stage0-forms)
                    :top-level-count-matches? (= (count stage1-records)
                                                 (count stage0-records))
                    :stage1-form-count (count stage1-records)
                    :stage0-form-count (count stage0-records)}
        artifact-base
        {:kind :gravity/stage1-reader-execution-artifact
         :phase "15"
         :task "P15-S2"
         :stage :stage1-reader-execution
         :source-path source-path
         :reader-source-path stage1-reader-source-path
         :reader-table-symbol :gravity.bootstrap.reader/stage1-reader-table
         :reader-table-id table-id
         :reader-table table
         :stage1-bootstrap-source-artifact stage1-bootstrap-artifact
         :stage1-reader-records stage1-records
         :stage0-comparison comparison
         :accepted-stage1-reader-fixtures
         [{:fixture "bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity"
           :status :accepted
           :comparison comparison}]
         :rejected-stage1-reader-fixtures
         stage1-reader-execution-rejected-fixture-records
         :stage1-reader-diagnostic-stream
         (stage1-reader-execution-diagnostic-stream source-path table-id)
         :stage1-reader-execution-results
         {:accepted-fixtures 1
          :rejected-fixtures
          (count stage1-reader-execution-rejected-fixture-records)
          :diagnostic-count (count stage1-reader-execution-diagnostic-ids)
          :form-count (count stage1-records)
          :status :complete}
         :diagnostics []}
        capability-proof (stage1-reader-execution-proof artifact-base)]
    (when-not (:forms-equal? comparison)
      (stage1-reader-fail! "STAGE1READER006" source-path comparison
                           {:missing-fields [:stage0-form-parity]}))
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn stage1-reader-execution-file-artifact
  [path]
  (stage1-reader-execution-source-artifact path (slurp path)))

(def stage1-reader-pipeline-entrypoint
  'stage1-read-source-pipeline)

(def stage1-reader-character-pipeline-entrypoint
  'stage1-read-source-character-pipeline)

(def stage1-reader-token-classifier-pipeline-entrypoint
  'stage1-read-source-token-classifier-pipeline)

(def stage1-reader-token-realizer-pipeline-entrypoint
  'stage1-read-source-token-realizer-pipeline)

(def stage1-reader-token-automaton-pipeline-entrypoint
  'stage1-read-source-token-automaton-pipeline)

(def stage1-reader-form-builder-pipeline-entrypoint
  'stage1-read-source-form-builder-pipeline)

(def stage1-reader-executor-pipeline-entrypoint
  'stage1-read-source-executor-pipeline)

(def stage1-reader-runtime-pipeline-entrypoint
  'stage1-read-source-runtime-pipeline)

(def stage1-reader-compiled-pipeline-entrypoint
  'stage1-read-source-compiled-pipeline)

(def stage1-reader-binary-pipeline-entrypoint
  'stage1-read-source-binary-pipeline)

(def stage1-reader-self-hosted-runtime-entrypoint
  'stage1-read-source-self-hosted-runtime)

(def stage1-reader-core-bootstrap-entrypoint
  'stage1-read-source-core-bootstrap)

(def stage1-reader-compiler-driver-entrypoint
  'stage1-read-source-compiler-driver)

(def stage1-reader-runtime-entrypoint-entrypoint
  'stage1-read-source-runtime-entrypoint)

(def stage1-reader-runtime-image-entrypoint
  'stage1-read-source-runtime-image)

(def stage1-reader-verified-boot-chain-entrypoint
  'stage1-read-source-verified-boot-chain)

(def stage1-reader-diverse-bootstrap-verification-entrypoint
  'stage1-read-source-diverse-bootstrap-verification)

(def stage1-reader-release-attestation-seed-retirement-entrypoint
  'stage1-read-source-release-attestation-seed-retirement)

(def stage1-reader-formal-release-governance-seed-retirement-entrypoint
  'stage1-read-source-formal-release-governance-seed-retirement)

(def stage1-reader-pipeline-diagnostic-messages
  {"STAGE1PIPE001" "stage1 reader pipeline entrypoint is missing"
   "STAGE1PIPE002" "stage1 reader pipeline used unsupported executable Gravity"
   "STAGE1PIPE003" "stage1 reader pipeline requested an unsupported host primitive"
   "STAGE1PIPE004" "stage1 reader pipeline token stream is invalid"
   "STAGE1PIPE005" "stage1 reader pipeline output diverged from stage0 reader forms"})

(def stage1-reader-pipeline-diagnostic-ids
  ["STAGE1PIPE001" "STAGE1PIPE002" "STAGE1PIPE003"
   "STAGE1PIPE004" "STAGE1PIPE005"])

(def stage1-reader-character-pipeline-diagnostic-messages
  {"STAGE1CHAR001" "stage1 reader character pipeline entrypoint is missing"
   "STAGE1CHAR002" "stage1 reader character pipeline used unsupported executable Gravity"
   "STAGE1CHAR003" "stage1 reader character pipeline requested an unsupported host primitive"
   "STAGE1CHAR004" "stage1 reader character pipeline stream is invalid"
   "STAGE1CHAR005" "stage1 reader character pipeline output diverged from stage0 reader forms"})

(def stage1-reader-character-pipeline-diagnostic-ids
  ["STAGE1CHAR001" "STAGE1CHAR002" "STAGE1CHAR003"
   "STAGE1CHAR004" "STAGE1CHAR005"])

(def stage1-reader-token-classifier-pipeline-diagnostic-messages
  {"STAGE1CLASS001" "stage1 reader token-classifier pipeline entrypoint is missing"
   "STAGE1CLASS002" "stage1 reader token-classifier pipeline used unsupported executable Gravity"
   "STAGE1CLASS003" "stage1 reader token-classifier pipeline requested an unsupported host primitive"
   "STAGE1CLASS004" "stage1 reader token classifier or stream is invalid"
   "STAGE1CLASS005" "stage1 reader token-classifier pipeline output diverged from stage0 reader forms"})

(def stage1-reader-token-classifier-pipeline-diagnostic-ids
  ["STAGE1CLASS001" "STAGE1CLASS002" "STAGE1CLASS003"
   "STAGE1CLASS004" "STAGE1CLASS005"])

(def stage1-reader-token-realizer-pipeline-diagnostic-messages
  {"STAGE1REAL001" "stage1 reader token-realizer pipeline entrypoint is missing"
   "STAGE1REAL002" "stage1 reader token-realizer pipeline used unsupported executable Gravity"
   "STAGE1REAL003" "stage1 reader token-realizer pipeline requested an unsupported host primitive"
   "STAGE1REAL004" "stage1 reader token realizer or stream is invalid"
   "STAGE1REAL005" "stage1 reader token-realizer pipeline output diverged from stage0 reader forms"})

(def stage1-reader-token-realizer-pipeline-diagnostic-ids
  ["STAGE1REAL001" "STAGE1REAL002" "STAGE1REAL003"
   "STAGE1REAL004" "STAGE1REAL005"])

(def stage1-reader-token-automaton-pipeline-diagnostic-messages
  {"STAGE1AUTO001" "stage1 reader token-automaton pipeline entrypoint is missing"
   "STAGE1AUTO002" "stage1 reader token-automaton pipeline used unsupported executable Gravity"
   "STAGE1AUTO003" "stage1 reader token-automaton pipeline requested an unsupported host primitive"
   "STAGE1AUTO004" "stage1 reader token automaton or stream is invalid"
   "STAGE1AUTO005" "stage1 reader token-automaton pipeline output diverged from stage0 reader forms"})

(def stage1-reader-token-automaton-pipeline-diagnostic-ids
  ["STAGE1AUTO001" "STAGE1AUTO002" "STAGE1AUTO003"
   "STAGE1AUTO004" "STAGE1AUTO005"])

(def stage1-reader-form-builder-pipeline-diagnostic-messages
  {"STAGE1FORM001" "stage1 reader form-builder pipeline entrypoint is missing"
   "STAGE1FORM002" "stage1 reader form-builder pipeline used unsupported executable Gravity"
   "STAGE1FORM003" "stage1 reader form-builder pipeline requested an unsupported host primitive"
   "STAGE1FORM004" "stage1 reader form builder or form stream is invalid"
   "STAGE1FORM005" "stage1 reader form-builder pipeline output diverged from stage0 reader forms"})