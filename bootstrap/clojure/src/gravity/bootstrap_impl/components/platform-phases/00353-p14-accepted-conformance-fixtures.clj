

(defn p14-accepted-conformance-fixtures
  []
  (mapv (fn [document]
          {:document document
           :fixture "bootstrap/clojure/fixtures/accepted/conformance-system.gravity"
           :artifact (get-in (p14-conformance-document-summaries document)
                             [:owned-surface])
           :evidence [(p14-task-id document)
                      (p14-conformance-phase-governing-documents document)]
           :status :accepted})
        p14-conformance-documents))

(defn p14-rejected-conformance-fixtures
  []
  (mapv (fn [document]
          {:document document
           :fixture (str "bootstrap/clojure/fixtures/rejected/"
                         (p14-conformance-rejected-fixture-names document))
           :artifact :stable-conformance-diagnostic
           :diagnostic (p14-conformance-rejected-diagnostics document)
           :evidence [(p14-task-id document)
                      (p14-conformance-phase-governing-documents document)]
           :status :rejected})
        p14-conformance-documents))

(defn p14-conformance-evidence
  []
  (mapv (fn [document]
          {:document document
           :accepted-behavior (get-in (p14-conformance-document-summaries
                                       document)
                                      [:accepted-behavior])
           :rejected-behavior (p14-conformance-rejected-diagnostics document)
           :artifacts (get-in (p14-conformance-document-summaries document)
                              [:artifact-keys])
           :validation ["clojure -M:test"
                        "clojure -M:gravity conformance-system bootstrap/clojure/fixtures/accepted/conformance-system.gravity"]
           :status :complete})
        p14-conformance-documents))

(defn p14-conformance-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/phase14-conformance-diagnostic-stream
   :stage :conformance-system
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           (let [document (p14-conformance-diagnostic-document id)]
             {:artifact :gravity/diagnostic
              :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
              :diagnostic id
              :rule id
              :severity :error
              :stage :conformance-system
              :document-id document
              :task (when document (p14-task-id document))
              :primary {:span (source-span source-path index)
                        :syntax-id (str "p14-conformance-syntax-" index)
                        :artifact input-id}
              :missing-fact (keyword (str/lower-case id))
              :remediation [{:kind :declare-fixture-metadata}
                            {:kind :preserve-compiler-and-runtime-facts}
                            {:kind :record-replayable-evidence}
                            {:kind :fail-release-gate-closed}]
              :ordering-key [id :conformance-system]}))
         p14-conformance-diagnostic-ids
         (range))
   :status :complete})

(defn p14-present?
  [value]
  (if (coll? value)
    (seq value)
    (some? value)))

(defn p14-conformance-validate!
  [source-path artifact]
  (let [documents (:document-contracts artifact)
        accepted (:accepted-conformance-fixtures artifact)
        rejected (:rejected-conformance-fixtures artifact)
        conformance (:conformance-evidence artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:conformance-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= (set p14-conformance-documents)
                 (set (:document-set artifact)))
      (p14-conformance-fail! "P14-MANIFEST" source-path artifact
                             {:missing-fields [:document-set]}))
    (doseq [artifact-key p14-conformance-artifact-keys]
      (when-not (p14-present? (get artifact artifact-key))
        (p14-conformance-fail! "P14-MANIFEST" source-path artifact
                               {:missing-fields [artifact-key]}))
      (when-not (= :complete (:status (get artifact artifact-key)))
        (p14-conformance-fail! "P14-MANIFEST" source-path
                               (get artifact artifact-key)
                               {:missing-fields [:status]})))
    (when-not (every? #(contains? documents %) p14-conformance-documents)
      (p14-conformance-fail! "P14-MANIFEST" source-path documents
                             {:missing-fields [:document-contracts]}))
    (doseq [document p14-conformance-documents
            :let [record (get documents document)
                  summary (p14-conformance-document-summaries document)]]
      (doseq [field [:document :task-id :governing-doc :suite-id
                     :diagnostics :evidence :owned-surface
                     :accepted-behavior :rejected-behavior :artifact-keys
                     :conformance]]
        (when-not (p14-present? (get record field))
          (p14-conformance-fail! "P14-MANIFEST" source-path record
                                 {:missing-fields [field]})))
      (when-not (= (:owned-surface summary) (:owned-surface record))
        (p14-conformance-fail! "P14-MANIFEST" source-path record
                               {:missing-fields [:owned-surface]}))
      (doseq [diagnostic (p14-conformance-diagnostics-by-document document)]
        (when-not (and (contains? (set (:diagnostics record)) diagnostic)
                       (get-in record [:evidence
                                       (keyword (str/lower-case diagnostic))]))
          (p14-conformance-fail! diagnostic source-path record
                                 {:missing-fields [(keyword (str/lower-case
                                                            diagnostic))]}))))
    (when-not (= (set p14-conformance-documents) (set (map :document accepted)))
      (p14-conformance-fail! "P14-ACCEPTED" source-path accepted
                             {:missing-fields [:accepted-conformance-fixtures]}))
    (when-not (= (set p14-conformance-documents) (set (map :document rejected)))
      (p14-conformance-fail! "P14-REJECTED" source-path rejected
                             {:missing-fields [:rejected-conformance-fixtures]}))
    (when-not (= (set p14-conformance-documents) (set (map :document conformance)))
      (p14-conformance-fail! "P14-CONFORMANCE" source-path conformance
                             {:missing-fields [:conformance-evidence]}))
    (when-not (true? (:offline (:conformance-harness artifact)))
      (p14-conformance-fail! "TEST1005" source-path
                             (:conformance-harness artifact)
                             {:missing-fields [:offline]}))
    (when-not (p14-present? (get-in artifact
                                    [:fixture-manifest
                                     :negative-fixtures]))
      (p14-conformance-fail! "TEST1001" source-path
                             (:fixture-manifest artifact)
                             {:missing-fields [:negative-fixtures]}))
    (when-not (true? (:stable-codes (:golden-diagnostics artifact)))
      (p14-conformance-fail! "TEST1003" source-path
                             (:golden-diagnostics artifact)
                             {:missing-fields [:stable-codes]}))
    (when-not (p14-present? (get-in artifact
                                    [:compiler-test-report
                                     :preservation-reports]))
      (p14-conformance-fail! "TEST2002" source-path
                             (:compiler-test-report artifact)
                             {:missing-fields [:preservation-reports]}))
    (when-not (p14-present? (get-in artifact
                                    [:runtime-conformance-report
                                     :capability-decision-log]))
      (p14-conformance-fail! "TEST3002" source-path
                             (:runtime-conformance-report artifact)
                             {:missing-fields [:capability-decision-log]}))
    (when-not (p14-present? (:profiles (:profile-compliance-report artifact)))
      (p14-conformance-fail! "TEST4001" source-path
                             (:profile-compliance-report artifact)
                             {:missing-fields [:profiles]}))
    (when-not (p14-present? (get-in artifact
                                    [:safety-conformance-report
                                     :unsafe-audit-records]))
      (p14-conformance-fail! "TEST5002" source-path
                             (:safety-conformance-report artifact)
                             {:missing-fields [:unsafe-audit-records]}))
    (when-not (:lowered-artifact-manifest (:backend-conformance-report artifact))
      (p14-conformance-fail! "TEST6004" source-path
                             (:backend-conformance-report artifact)
                             {:missing-fields [:lowered-artifact-manifest]}))
    (when-not (p14-present? (:modules (:standard-library-test-report artifact)))
      (p14-conformance-fail! "TEST7001" source-path
                             (:standard-library-test-report artifact)
                             {:missing-fields [:modules]}))
    (when-not (p14-present? (:replay-traces (:ai-workflow-eval-report artifact)))
      (p14-conformance-fail! "TEST8003" source-path
                             (:ai-workflow-eval-report artifact)
                             {:missing-fields [:replay-traces]}))
    (when-not (:seed (:fuzz-property-suite artifact))
      (p14-conformance-fail! "TEST9001" source-path
                             (:fuzz-property-suite artifact)
                             {:missing-fields [:seed]}))
    (when-not (empty? (:accepted-divergence (:differential-report artifact)))
      (p14-conformance-fail! "TEST10002" source-path
                             (:differential-report artifact)
                             {:missing-fields [:accepted-divergence]}))
    (when-not (every? true? (map :machine-checkable
                                 (:claims (:formal-proof-report artifact))))
      (p14-conformance-fail! "TEST11003" source-path
                             (:formal-proof-report artifact)
                             {:missing-fields [:machine-checkable]}))
    (when-not (true? (:semantic-gates-passed
                     (:performance-regression-report artifact)))
      (p14-conformance-fail! "TEST12003" source-path
                             (:performance-regression-report artifact)
                             {:missing-fields [:semantic-gates]}))
    (when-not (:provenance-attestation
               (:self-hosting-validation-report artifact))
      (p14-conformance-fail! "TEST13002" source-path
                             (:self-hosting-validation-report artifact)
                             {:missing-fields [:provenance-attestation]}))
    (when-not (set/subset? (set p14-conformance-diagnostic-ids) diagnostics)
      (p14-conformance-fail! "P14-MANIFEST" source-path
                             (:conformance-diagnostic-stream artifact)
                             {:missing-fields [:diagnostics]})))
  :complete)

(defn p14-task-statuses
  []
  (merge (zipmap ["P14-T01" "P14-T02" "P14-T03"
                  "P14-T04" "P14-T05" "P14-T06"]
                 (repeat :complete))
         (zipmap (map p14-task-id p14-conformance-documents)
                 (repeat :complete))))