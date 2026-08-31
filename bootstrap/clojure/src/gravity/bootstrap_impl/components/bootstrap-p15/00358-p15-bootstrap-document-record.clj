

(defn p15-bootstrap-document-record
  [document]
  (let [summary (p15-bootstrap-document-summaries document)]
    (merge
     {:document document
      :task-id (p15-task-id document)
      :governing-doc (p15-bootstrap-phase-governing-documents document)
      :suite-id (get-in summary [:owned-surface])
      :diagnostics (p15-bootstrap-diagnostics-by-document document)
      :evidence (into {}
                      (map (fn [diagnostic]
                             [(keyword (str/lower-case diagnostic))
                              {:diagnostic diagnostic
                               :source :governing-document
                               :status :present}])
                           (p15-bootstrap-diagnostics-by-document document)))
      :bootstrap
      {:accepted-fixture "bootstrap/clojure/fixtures/accepted/bootstrap-self-hosting.gravity"
       :rejected-fixture (str "bootstrap/clojure/fixtures/rejected/"
                              (p15-bootstrap-rejected-fixture-names document))
       :artifact-evidence :bootstrap-self-hosting
       :validation-command "clojure -M:test"
       :status :complete}}
     summary)))

(defn p15-bootstrap-document-records
  []
  (into {} (map (fn [document]
                  [document (p15-bootstrap-document-record document)])
                p15-bootstrap-documents)))

(defn p15-accepted-bootstrap-fixtures
  []
  (mapv (fn [document]
          {:document document
           :fixture "bootstrap/clojure/fixtures/accepted/bootstrap-self-hosting.gravity"
           :artifact (get-in (p15-bootstrap-document-summaries document)
                             [:owned-surface])
           :evidence [(p15-task-id document)
                      (p15-bootstrap-phase-governing-documents document)]
           :status :accepted})
        p15-bootstrap-documents))

(defn p15-rejected-bootstrap-fixtures
  []
  (mapv (fn [document]
          {:document document
           :fixture (str "bootstrap/clojure/fixtures/rejected/"
                         (p15-bootstrap-rejected-fixture-names document))
           :artifact :stable-bootstrap-diagnostic
           :diagnostic (p15-bootstrap-rejected-diagnostics document)
           :evidence [(p15-task-id document)
                      (p15-bootstrap-phase-governing-documents document)]
           :status :rejected})
        p15-bootstrap-documents))

(defn p15-bootstrap-evidence
  []
  (mapv (fn [document]
          {:document document
           :accepted-behavior (get-in (p15-bootstrap-document-summaries
                                       document)
                                      [:accepted-behavior])
           :rejected-behavior (p15-bootstrap-rejected-diagnostics document)
           :artifacts (get-in (p15-bootstrap-document-summaries document)
                              [:artifact-keys])
           :validation ["clojure -M:test"
                        "clojure -M:gravity bootstrap-self-hosting bootstrap/clojure/fixtures/accepted/bootstrap-self-hosting.gravity"]
           :status :complete})
        p15-bootstrap-documents))

(defn p15-bootstrap-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/phase15-bootstrap-diagnostic-stream
   :stage :bootstrap-self-hosting
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           (let [document (p15-bootstrap-diagnostic-document id)]
             {:artifact :gravity/diagnostic
              :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
              :diagnostic id
              :rule id
              :severity :error
              :stage :bootstrap-self-hosting
              :document-id document
              :task (when document (p15-task-id document))
              :primary {:span (source-span source-path index)
                        :syntax-id (str "p15-bootstrap-syntax-" index)
                        :artifact input-id}
              :missing-fact (keyword (str/lower-case id))
              :remediation [{:kind :record-bootstrap-stage-evidence}
                            {:kind :declare-seed-and-self-hosted-scope}
                            {:kind :compare-stage-artifacts}
                            {:kind :attach-bootstrap-provenance}]
              :ordering-key [id :bootstrap-self-hosting]}))
         p15-bootstrap-diagnostic-ids
         (range))
   :status :complete})

(defn p15-present?
  [value]
  (if (coll? value)
    (seq value)
    (some? value)))

(defn p15-bootstrap-validate!
  [source-path artifact]
  (let [documents (:document-contracts artifact)
        accepted (:accepted-bootstrap-fixtures artifact)
        rejected (:rejected-bootstrap-fixtures artifact)
        bootstrap (:bootstrap-evidence artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:bootstrap-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= (set p15-bootstrap-documents)
                 (set (:document-set artifact)))
      (p15-bootstrap-fail! "P15-MANIFEST" source-path artifact
                           {:missing-fields [:document-set]}))
    (doseq [artifact-key p15-bootstrap-artifact-keys]
      (when-not (p15-present? (get artifact artifact-key))
        (p15-bootstrap-fail! "P15-MANIFEST" source-path artifact
                             {:missing-fields [artifact-key]}))
      (when-not (= :complete (:status (get artifact artifact-key)))
        (p15-bootstrap-fail! "P15-MANIFEST" source-path
                             (get artifact artifact-key)
                             {:missing-fields [:status]})))
    (when-not (every? #(contains? documents %) p15-bootstrap-documents)
      (p15-bootstrap-fail! "P15-MANIFEST" source-path documents
                           {:missing-fields [:document-contracts]}))
    (doseq [document p15-bootstrap-documents
            :let [record (get documents document)
                  summary (p15-bootstrap-document-summaries document)]]
      (doseq [field [:document :task-id :governing-doc :suite-id
                     :diagnostics :evidence :owned-surface
                     :accepted-behavior :rejected-behavior :artifact-keys
                     :bootstrap]]
        (when-not (p15-present? (get record field))
          (p15-bootstrap-fail! "P15-MANIFEST" source-path record
                               {:missing-fields [field]})))
      (when-not (= (:owned-surface summary) (:owned-surface record))
        (p15-bootstrap-fail! "P15-MANIFEST" source-path record
                             {:missing-fields [:owned-surface]}))
      (doseq [diagnostic (p15-bootstrap-diagnostics-by-document document)]
        (when-not (and (contains? (set (:diagnostics record)) diagnostic)
                       (get-in record [:evidence
                                       (keyword (str/lower-case diagnostic))]))
          (p15-bootstrap-fail! diagnostic source-path record
                               {:missing-fields [(keyword (str/lower-case
                                                          diagnostic))]}))))
    (when-not (= (set p15-bootstrap-documents) (set (map :document accepted)))
      (p15-bootstrap-fail! "P15-ACCEPTED" source-path accepted
                           {:missing-fields [:accepted-bootstrap-fixtures]}))
    (when-not (= (set p15-bootstrap-documents) (set (map :document rejected)))
      (p15-bootstrap-fail! "P15-REJECTED" source-path rejected
                           {:missing-fields [:rejected-bootstrap-fixtures]}))
    (when-not (= (set p15-bootstrap-documents) (set (map :document bootstrap)))
      (p15-bootstrap-fail! "P15-BOOTSTRAP" source-path bootstrap
                           {:missing-fields [:bootstrap-evidence]}))
    (when-not (p15-present? (:stage-manifests
                            (:bootstrap-stage-matrix artifact)))
      (p15-bootstrap-fail! "BOOT1001" source-path
                           (:bootstrap-stage-matrix artifact)
                           {:missing-fields [:stage-manifests]}))
    (when-not (true? (:unsupported-profiles-rejected
                     (:seed-compiler-manifest artifact)))
      (p15-bootstrap-fail! "BOOT2002" source-path
                           (:seed-compiler-manifest artifact)
                           {:missing-fields [:unsupported-profiles-rejected]}))
    (when-not (true? (:ambient-authority-denied
                     (:self-hosted-component-manifest artifact)))
      (p15-bootstrap-fail! "BOOT3002" source-path
                           (:self-hosted-component-manifest artifact)
                           {:missing-fields [:ambient-authority-denied]}))
    (when-not (p15-present? (get-in artifact
                                    [:compiler-coding-standard-report
                                     :pass-preservation-report
                                     :preserved]))
      (p15-bootstrap-fail! "BOOT4003" source-path
                           (:compiler-coding-standard-report artifact)
                           {:missing-fields [:preserved]}))
    (when-not (p15-present? (:conformance-link-table
                            (:stage-compatibility-matrix artifact)))
      (p15-bootstrap-fail! "BOOT5003" source-path
                           (:stage-compatibility-matrix artifact)
                           {:missing-fields [:conformance-link-table]}))
    (when-not (p15-present? (:environment-manifest
                            (:trusting-trust-report artifact)))
      (p15-bootstrap-fail! "BOOT6001" source-path
                           (:trusting-trust-report artifact)
                           {:missing-fields [:environment-manifest]}))
    (when-not (and (:compiler-a (:equivalence-report artifact))
                   (:compiler-b (:equivalence-report artifact)))
      (p15-bootstrap-fail! "BOOT7001" source-path
                           (:equivalence-report artifact)
                           {:missing-fields [:compiler-a :compiler-b]}))
    (when-not (p15-present? (:compiler-lineage-graph
                            (:bootstrap-provenance-record artifact)))
      (p15-bootstrap-fail! "BOOT8002" source-path
                           (:bootstrap-provenance-record artifact)
                           {:missing-fields [:compiler-lineage-graph]}))
    (when-not (set/subset? (set p15-bootstrap-diagnostic-ids) diagnostics)
      (p15-bootstrap-fail! "P15-MANIFEST" source-path
                           (:bootstrap-diagnostic-stream artifact)
                           {:missing-fields [:diagnostics]})))
  :complete)

(defn p15-task-statuses
  []
  (merge (zipmap ["P15-T01" "P15-T02" "P15-T03"
                  "P15-T04" "P15-T05" "P15-T06"]
                 (repeat :complete))
         (zipmap (map p15-task-id p15-bootstrap-documents)
                 (repeat :complete))))