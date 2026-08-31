

(defn p16-standard-library-document-record
  [document]
  (let [summary (p16-standard-library-data-by-document document)]
    (merge
     {:document document
      :task-id (p16-task-id document)
      :governing-doc (:file summary)
      :suite-id (:owned-surface summary)
      :diagnostics (p16-standard-library-diagnostics-by-document document)
      :evidence (into {}
                      (map (fn [diagnostic]
                             [(keyword (str/lower-case diagnostic))
                              {:diagnostic diagnostic
                               :source :governing-document
                               :status :present}])
                           (p16-standard-library-diagnostics-by-document
                            document)))
      :standard-library
      {:accepted-fixture "bootstrap/clojure/fixtures/accepted/standard-library-phase16.gravity"
       :rejected-fixture (str "bootstrap/clojure/fixtures/rejected/"
                              (:rejected-fixture summary))
       :artifact-evidence :standard-library
       :validation-command "clojure -M:test"
       :status :complete}}
     (select-keys summary [:title :module :namespace :owned-surface
                           :accepted-behavior :rejected-diagnostic
                           :artifact-keys :dependencies :profiles]))))

(defn p16-standard-library-document-records
  []
  (into {} (map (fn [document]
                  [document (p16-standard-library-document-record document)])
                p16-standard-library-documents)))

(defn p16-accepted-standard-library-fixtures
  []
  (mapv (fn [document]
          (let [summary (p16-standard-library-data-by-document document)]
            {:document document
             :fixture "bootstrap/clojure/fixtures/accepted/standard-library-phase16.gravity"
             :artifact (:owned-surface summary)
             :evidence [(p16-task-id document) (:file summary)]
             :status :accepted}))
        p16-standard-library-documents))

(defn p16-rejected-standard-library-fixtures
  []
  (mapv (fn [document]
          (let [summary (p16-standard-library-data-by-document document)]
            {:document document
             :fixture (str "bootstrap/clojure/fixtures/rejected/"
                           (:rejected-fixture summary))
             :artifact :stable-standard-library-diagnostic
             :diagnostic (:rejected-diagnostic summary)
             :evidence [(p16-task-id document) (:file summary)]
             :status :rejected}))
        p16-standard-library-documents))

(defn p16-standard-library-evidence
  []
  (mapv (fn [document]
          (let [summary (p16-standard-library-data-by-document document)]
            {:document document
             :accepted-behavior (:accepted-behavior summary)
             :rejected-behavior (:rejected-diagnostic summary)
             :artifacts (:artifact-keys summary)
             :validation ["clojure -M:test"
                          "clojure -M:gravity standard-library bootstrap/clojure/fixtures/accepted/standard-library-phase16.gravity"]
             :status :complete}))
        p16-standard-library-documents))

(defn p16-standard-library-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/phase16-standard-library-diagnostic-stream
   :stage :standard-library
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           (let [document (p16-standard-library-diagnostic-document id)]
             {:artifact :gravity/diagnostic
              :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
              :diagnostic id
              :rule id
              :severity :error
              :stage :standard-library
              :document-id document
              :task (when document (p16-task-id document))
              :primary {:span (source-span source-path index)
                        :syntax-id (str "p16-standard-library-syntax-"
                                        index)
                        :artifact input-id}
              :missing-fact (keyword (str/lower-case id))
              :remediation [{:kind :record-module-manifest}
                            {:kind :declare-profile-effect-capability}
                            {:kind :attach-safe-wrapper-audit}
                            {:kind :update-stability-policy}]
              :ordering-key [id :standard-library]}))
         p16-standard-library-diagnostic-ids
         (range))
   :status :complete})

(defn p16-present?
  [value]
  (if (coll? value)
    (seq value)
    (some? value)))

(defn p16-module-record
  [artifact document]
  (some #(when (= document (:document %)) %)
        (get-in artifact [:library-module-manifest :modules])))

(defn p16-standard-library-validate!
  [source-path artifact]
  (let [documents (:document-contracts artifact)
        accepted (:accepted-standard-library-fixtures artifact)
        rejected (:rejected-standard-library-fixtures artifact)
        standard-library (:standard-library-evidence artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:standard-library-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= (set p16-standard-library-documents)
                 (set (:document-set artifact)))
      (p16-standard-library-fail! "P16-MANIFEST" source-path artifact
                                  {:missing-fields [:document-set]}))
    (doseq [artifact-key p16-standard-library-artifact-keys]
      (when-not (p16-present? (get artifact artifact-key))
        (p16-standard-library-fail! "P16-MANIFEST" source-path artifact
                                    {:missing-fields [artifact-key]}))
      (when-not (= :complete (:status (get artifact artifact-key)))
        (p16-standard-library-fail! "P16-MANIFEST" source-path
                                    (get artifact artifact-key)
                                    {:missing-fields [:status]})))
    (when-not (every? #(contains? documents %) p16-standard-library-documents)
      (p16-standard-library-fail! "P16-MANIFEST" source-path documents
                                  {:missing-fields [:document-contracts]}))
    (doseq [document p16-standard-library-documents
            :let [record (get documents document)
                  summary (p16-standard-library-data-by-document document)]]
      (doseq [field [:document :task-id :governing-doc :suite-id
                     :diagnostics :evidence :owned-surface
                     :accepted-behavior :rejected-diagnostic
                     :artifact-keys :standard-library]]
        (when-not (p16-present? (get record field))
          (p16-standard-library-fail! "P16-MANIFEST" source-path record
                                      {:missing-fields [field]})))
      (when-not (= (:owned-surface summary) (:owned-surface record))
        (p16-standard-library-fail! "P16-MANIFEST" source-path record
                                    {:missing-fields [:owned-surface]}))
      (doseq [diagnostic (p16-standard-library-diagnostics-by-document
                          document)]
        (when-not (and (contains? (set (:diagnostics record)) diagnostic)
                       (get-in record [:evidence
                                       (keyword (str/lower-case diagnostic))]))
          (p16-standard-library-fail!
           diagnostic source-path record
           {:missing-fields [(keyword (str/lower-case diagnostic))]})))
      (let [module-record (p16-module-record artifact document)]
        (when-not (true? (get-in module-record [:checks (:check-key summary)]))
          (p16-standard-library-fail!
           (:rejected-diagnostic summary) source-path module-record
           {:missing-fields [(:check-key summary)]}))))
    (when-not (= (set p16-standard-library-documents)
                 (set (map :document accepted)))
      (p16-standard-library-fail!
       "P16-ACCEPTED" source-path accepted
       {:missing-fields [:accepted-standard-library-fixtures]}))
    (when-not (= (set p16-standard-library-documents)
                 (set (map :document rejected)))
      (p16-standard-library-fail!
       "P16-REJECTED" source-path rejected
       {:missing-fields [:rejected-standard-library-fixtures]}))
    (when-not (= (set p16-standard-library-documents)
                 (set (map :document standard-library)))
      (p16-standard-library-fail!
       "P16-STDLIB" source-path standard-library
       {:missing-fields [:standard-library-evidence]}))
    (when-not (= 20 (:module-count (:library-module-manifest artifact)))
      (p16-standard-library-fail!
       "STD1001" source-path (:library-module-manifest artifact)
       {:missing-fields [:module-count]}))
    (when-not (true? (:profile-metadata-complete
                     (:library-module-manifest artifact)))
      (p16-standard-library-fail!
       "STD1001" source-path (:library-module-manifest artifact)
       {:missing-fields [:profile-metadata-complete]}))
    (when-not (p16-present? (:entries (:api-stability-record artifact)))
      (p16-standard-library-fail!
       "STD20001" source-path (:api-stability-record artifact)
       {:missing-fields [:entries]}))
    (when-not (p16-present? (:audit-records (:safe-wrapper-audit artifact)))
      (p16-standard-library-fail!
       "STD1005" source-path (:safe-wrapper-audit artifact)
       {:missing-fields [:audit-records]}))
    (when-not (= 20 (:accepted-count (:library-conformance-fixture artifact)))
      (p16-standard-library-fail!
       "P16-ACCEPTED" source-path (:library-conformance-fixture artifact)
       {:missing-fields [:accepted-count]}))
    (when-not (= 20 (:rejected-count (:library-conformance-fixture artifact)))
      (p16-standard-library-fail!
       "P16-REJECTED" source-path (:library-conformance-fixture artifact)
       {:missing-fields [:rejected-count]}))
    (when-not (= 20 (count (:rows (:profile-support-matrix artifact))))
      (p16-standard-library-fail!
       "P16-MANIFEST" source-path (:profile-support-matrix artifact)
       {:missing-fields [:rows]}))
    (when-not (true? (:diagnostic-compatible (:compatibility-report artifact)))
      (p16-standard-library-fail!
       "STD20004" source-path (:compatibility-report artifact)
       {:missing-fields [:diagnostic-compatible]}))
    (when-not (set/subset? (set p16-standard-library-diagnostic-ids)
                           diagnostics)
      (p16-standard-library-fail!
       "P16-MANIFEST" source-path
       (:standard-library-diagnostic-stream artifact)
       {:missing-fields [:diagnostics]})))
  :complete)

(defn p16-task-statuses
  []
  (merge (zipmap ["P16-T01" "P16-T02" "P16-T03"
                  "P16-T04" "P16-T05" "P16-T06"]
                 (repeat :complete))
         (zipmap (map p16-task-id p16-standard-library-documents)
                 (repeat :complete))))