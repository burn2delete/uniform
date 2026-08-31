

(defn p17-governance-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/phase17-governance-diagnostic-stream
   :stage :governance-evolution
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           (let [document (p17-governance-diagnostic-document id)]
             {:artifact :gravity/diagnostic
              :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
              :diagnostic id
              :rule id
              :severity :error
              :stage :governance-evolution
              :document-id document
              :task (when document (p17-task-id document))
              :primary {:span (source-span source-path index)
                        :syntax-id (str "p17-governance-syntax-" index)
                        :artifact input-id}
              :missing-fact (keyword (str/lower-case id))
              :remediation [{:kind :record-owner-and-scope}
                            {:kind :attach-review-evidence}
                            {:kind :update-migration-plan}
                            {:kind :link-provenance}]
              :ordering-key [id :governance-evolution]}))
         p17-governance-diagnostic-ids
         (range))
   :status :complete})

(defn p17-present?
  [value]
  (if (coll? value)
    (seq value)
    (some? value)))

(defn p17-governance-record-for
  [artifact document]
  (some #(when (= document (:document %)) %)
        (:governance-records artifact)))

(defn p17-governance-validate!
  [source-path artifact]
  (let [documents (:document-contracts artifact)
        accepted (:accepted-governance-fixtures artifact)
        rejected (:rejected-governance-fixtures artifact)
        governance (:governance-evidence artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:governance-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= (set p17-governance-documents)
                 (set (:document-set artifact)))
      (p17-governance-fail! "P17-MANIFEST" source-path artifact
                             {:missing-fields [:document-set]}))
    (doseq [artifact-key p17-governance-artifact-keys]
      (when-not (p17-present? (get artifact artifact-key))
        (p17-governance-fail! "P17-MANIFEST" source-path artifact
                              {:missing-fields [artifact-key]}))
      (when-not (= :complete (:status (get artifact artifact-key)))
        (p17-governance-fail! "P17-MANIFEST" source-path
                              (get artifact artifact-key)
                              {:missing-fields [:status]})))
    (when-not (every? #(contains? documents %) p17-governance-documents)
      (p17-governance-fail! "P17-MANIFEST" source-path documents
                            {:missing-fields [:document-contracts]}))
    (doseq [document p17-governance-documents
            :let [record (get documents document)
                  summary (p17-governance-data-by-document document)]]
      (doseq [field [:document :task-id :governing-doc :suite-id
                     :diagnostics :evidence :owned-surface
                     :accepted-behavior :rejected-diagnostic
                     :artifact-key :governance]]
        (when-not (p17-present? (get record field))
          (p17-governance-fail! "P17-MANIFEST" source-path record
                                {:missing-fields [field]})))
      (doseq [diagnostic (p17-governance-diagnostics-by-document document)]
        (when-not (and (contains? (set (:diagnostics record)) diagnostic)
                       (get-in record [:evidence
                                       (keyword (str/lower-case diagnostic))]))
          (p17-governance-fail!
           diagnostic source-path record
           {:missing-fields [(keyword (str/lower-case diagnostic))]})))
      (let [governance-record (p17-governance-record-for artifact document)]
        (when-not (true? (get-in governance-record
                                 [:checks (:check-key summary)]))
          (p17-governance-fail!
           (:rejected-diagnostic summary) source-path governance-record
           {:missing-fields [(:check-key summary)]}))))
    (when-not (= (set p17-governance-documents)
                 (set (map :document accepted)))
      (p17-governance-fail!
       "P17-ACCEPTED" source-path accepted
       {:missing-fields [:accepted-governance-fixtures]}))
    (when-not (= (set p17-governance-documents)
                 (set (map :document rejected)))
      (p17-governance-fail!
       "P17-REJECTED" source-path rejected
       {:missing-fields [:rejected-governance-fixtures]}))
    (when-not (= (set p17-governance-documents)
                 (set (map :document governance)))
      (p17-governance-fail!
       "P17-GOVERNANCE" source-path governance
       {:missing-fields [:governance-evidence]}))
    (when-not (= 10 (count (:governance-records artifact)))
      (p17-governance-fail!
       "P17-MANIFEST" source-path artifact
       {:missing-fields [:governance-records]}))
    (when-not (p17-present? (get-in artifact
                                    [:language-change-record :changes]))
      (p17-governance-fail!
       "GOV1001" source-path (:language-change-record artifact)
       {:missing-fields [:changes]}))
    (when-not (p17-present? (get-in artifact
                                    [:security-review-record
                                     :threat-models]))
      (p17-governance-fail!
       "GOV4001" source-path (:security-review-record artifact)
       {:missing-fields [:threat-models]}))
    (when-not (= :off-in-stable
                 (get-in artifact [:experiment-registry :default-policy]))
      (p17-governance-fail!
       "GOV7002" source-path (:experiment-registry artifact)
       {:missing-fields [:default-policy]}))
    (when-not (true? (get-in artifact
                             [:unsafe-governance-audit
                              :stale-audit-blocks-release]))
      (p17-governance-fail!
       "GOV9006" source-path (:unsafe-governance-audit artifact)
       {:missing-fields [:stale-audit-blocks-release]}))
    (when-not (set/subset? (set p17-governance-diagnostic-ids)
                           diagnostics)
      (p17-governance-fail!
       "P17-MANIFEST" source-path
       (:governance-diagnostic-stream artifact)
       {:missing-fields [:diagnostics]})))
  :complete)

(defn p17-task-statuses
  []
  (merge (zipmap ["P17-T01" "P17-T02" "P17-T03"
                  "P17-T04" "P17-T05" "P17-T06"]
                 (repeat :complete))
         (zipmap (map p17-task-id p17-governance-documents)
                 (repeat :complete))))

(defn p17-governance-proof
  [artifact]
  (let [documents (:document-contracts artifact)
        accepted-docs (set (map :document
                                (:accepted-governance-fixtures artifact)))
        rejected-docs (set (map :document
                                (:rejected-governance-fixtures artifact)))
        governance-docs (set (map :document
                                  (:governance-evidence artifact)))
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:governance-diagnostic-stream
                                       :diagnostics])))]
    {:language-evolution-covered?
     (boolean
      (p17-present? (get-in artifact [:language-change-record :changes])))
     :compatibility-covered?
     (boolean
      (p17-present? (get-in artifact [:compatibility-report :surfaces])))
     :standard-library-governance-covered?
     (boolean
      (p17-present? (get-in artifact
                            [:standard-library-governance-record
                             :module-ownership-map])))
     :security-review-covered?
     (boolean
      (p17-present? (get-in artifact
                            [:security-review-record :threat-models])))
     :target-support-covered?
     (boolean
      (p17-present? (get-in artifact [:target-support-matrix :targets])))
     :rfc-covered?
     (boolean
      (p17-present? (get-in artifact [:rfc-record :required-sections])))
     :experiment-covered?
     (= :off-in-stable (get-in artifact
                               [:experiment-registry :default-policy]))
     :deprecation-covered?
     (boolean
      (p17-present? (get-in artifact
                            [:deprecation-plan :stabilization-evidence])))
     :unsafe-governance-covered?
     (true? (get-in artifact
                    [:unsafe-governance-audit
                     :stale-audit-blocks-release]))
     :ecosystem-package-covered?
     (boolean
      (p17-present? (get-in artifact
                            [:ecosystem-package-governance-record
                             :registry-decisions])))
     :document-coverage-complete?
     (= (set p17-governance-documents) (set (keys documents)))
     :accepted-fixtures-covered?
     (= (set p17-governance-documents) accepted-docs)
     :rejected-fixtures-covered?
     (= (set p17-governance-documents) rejected-docs)
     :governance-evidence-covered?
     (= (set p17-governance-documents) governance-docs)
     :diagnostics-covered?
     (set/subset? (set p17-governance-diagnostic-ids) diagnostics)
     :task-statuses (p17-task-statuses)
     :status :complete}))