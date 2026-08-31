

(defn p10-schema-validate!
  [source-path artifact]
  (let [documents (:document-contracts artifact)
        accepted (:accepted-schema-fixtures artifact)
        rejected (:rejected-schema-fixtures artifact)
        conformance (:schema-conformance-evidence artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:schema-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= (set p10-schema-documents) (set (:document-set artifact)))
      (p10-schema-fail! "P10-MANIFEST" source-path artifact
                        {:missing-fields [:document-set]}))
    (when-not (every? #(contains? documents %) p10-schema-documents)
      (p10-schema-fail! "P10-MANIFEST" source-path documents
                        {:missing-fields [:document-contracts]}))
    (doseq [artifact-key p10-schema-artifact-keys
            :let [schema-artifact (get artifact artifact-key)]]
      (when-not (p10-present? schema-artifact)
        (p10-schema-fail! "P10-MANIFEST" source-path artifact
                          {:missing-fields [artifact-key]}))
      (when-not (= p10-schema-hash (:schema-hash schema-artifact))
        (p10-schema-fail! "S1-MANIFEST" source-path schema-artifact
                          {:missing-fields [:schema-hash]})))
    (when-not (every? #(= :complete (:status (get artifact %)))
                      p10-generated-artifact-keys)
      (p10-schema-fail! "P10-MANIFEST" source-path artifact
                        {:missing-fields [:generated-artifact-status]}))
    (doseq [document p10-schema-documents
            :let [record (get documents document)
                  summary (p10-document-summaries document)
                  contract (p10-schema-contracts document)]]
      (doseq [field [:document :task-id :governing-doc :schema-id
                     :schema-version :schema-hash :diagnostics :evidence
                     :owned-surface :accepted-behavior :rejected-behavior
                     :artifact-keys :conformance]]
        (when-not (p10-present? (get record field))
          (p10-schema-fail! "P10-MANIFEST" source-path record
                            {:missing-fields [field]})))
      (when-not (= (:owned-surface summary) (:owned-surface record))
        (p10-schema-fail! "P10-MANIFEST" source-path record
                          {:missing-fields [:owned-surface]}))
      (doseq [[fact [diagnostic _]] contract]
        (when-not (and (contains? (set (:diagnostics record)) diagnostic)
                       (get-in record [:evidence fact]))
          (p10-schema-fail! diagnostic source-path record
                            {:missing-fields [fact]}))))
    (when-not (= (set p10-schema-documents)
                 (set (map :document accepted)))
      (p10-schema-fail! "P10-ACCEPTED" source-path accepted
                        {:missing-fields [:accepted-schema-fixtures]}))
    (when-not (= (set p10-schema-documents)
                 (set (map :document rejected)))
      (p10-schema-fail! "P10-REJECTED" source-path rejected
                        {:missing-fields [:rejected-schema-fixtures]}))
    (when-not (= (set p10-schema-documents)
                 (set (map :document conformance)))
      (p10-schema-fail! "P10-CONFORMANCE" source-path conformance
                        {:missing-fields [:schema-conformance-evidence]}))
    (when-not (every? #(and (:accepted-behavior %)
                            (:rejected-behavior %)
                            (seq (:artifacts %))
                            (seq (:validation %)))
                      conformance)
      (p10-schema-fail! "P10-CONFORMANCE" source-path conformance
                        {:missing-fields [:conformance-record]}))
    (when-not (= :gravity-source-schema
                 (get-in artifact [:graphql-generation :source-authority]))
      (p10-schema-fail! "S4-MAPPING" source-path
                        (:graphql-generation artifact)
                        {:missing-fields [:source-authority]}))
    (when-not (= :gravity-route-and-schema-source
                 (get-in artifact [:openapi-generation :source-authority]))
      (p10-schema-fail! "S5-SCHEMA" source-path
                        (:openapi-generation artifact)
                        {:missing-fields [:source-authority]}))
    (when-not (= :no-data-loss
                 (get-in artifact
                         [:database-mapping :data-loss-report :policy]))
      (p10-schema-fail! "S6-DATA-LOSS" source-path
                        (:database-mapping artifact)
                        {:missing-fields [:data-loss-policy]}))
    (when-not (= :no-raw-pointers-in-stable-record
                 (get-in artifact [:binary-abi-schema :pointer-policy]))
      (p10-schema-fail! "S7-POINTER" source-path
                        (:binary-abi-schema artifact)
                        {:missing-fields [:pointer-policy]}))
    (when-not (= :redacted
                 (get-in artifact
                         [:typed-config :redaction-report :database-url]))
      (p10-schema-fail! "S8-SECRET" source-path
                        (:typed-config artifact)
                        {:missing-fields [:secret-redaction]}))
    (when-not (set/subset? #{:types :effects :capabilities :safety
                             :proofs :tests :diagnostics :conformance}
                           (set (get-in artifact
                                        [:artifact-schema-registry
                                         :evidence-schema])))
      (p10-schema-fail! "S9-EVIDENCE" source-path
                        (:artifact-schema-registry artifact)
                        {:missing-fields [:evidence-schema]}))
    (when-not (set/subset? (set p10-schema-diagnostic-ids) diagnostics)
      (p10-schema-fail! "P10-MANIFEST" source-path
                        (:schema-diagnostic-stream artifact)
                        {:missing-fields [:diagnostics]})))
  :complete)

(defn p10-task-statuses
  []
  (merge (zipmap ["P10-T01" "P10-T02" "P10-T03"
                  "P10-T04" "P10-T05" "P10-T06"]
                 (repeat :complete))
         (zipmap (map p10-task-id p10-schema-documents)
                 (repeat :complete))))

(defn p10-schema-proof
  [artifact]
  (let [documents (:document-contracts artifact)
        accepted-docs (set (map :document (:accepted-schema-fixtures artifact)))
        rejected-docs (set (map :document (:rejected-schema-fixtures artifact)))
        conformance-docs (set (map :document (:schema-conformance-evidence artifact)))
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:schema-diagnostic-stream
                                       :diagnostics])))]
    {:source-schema-authority-covered?
     (= (:schema-hash (:source-schema-ir artifact))
        p10-schema-hash)
     :validator-boundaries-covered?
     (set/subset? #{:api-input :database-row :model-output
                    :configuration :artifact-manifest}
                  (:clears-taint-for (:validator-artifact artifact)))
     :serialization-and-canonical-covered?
     (and (= :untrusted
             (:decoded-trust (:serialization-fixture artifact)))
          (true? (:schema-hash-included (:canonical-format artifact))))
     :api-projections-covered?
     (and (= :gravity-source-schema
             (get-in artifact [:graphql-generation :source-authority]))
          (= :gravity-route-and-schema-source
             (get-in artifact [:openapi-generation :source-authority])))
     :database-migration-covered?
     (= :rollback-supported
        (:rollback-or-forward-policy (:database-mapping artifact)))
     :abi-and-config-covered?
     (and (= :no-raw-pointers-in-stable-record
             (:pointer-policy (:binary-abi-schema artifact)))
          (= :redacted
             (get-in artifact [:typed-config :redaction-report
                               :database-url])))
     :artifact-and-ai-schema-covered?
     (and (contains? (set (get-in artifact
                                  [:artifact-schema-registry
                                   :evidence-schema]))
                     :conformance)
          (= :tainted-until-schema-validation
             (:provider-output-trust
              (:ai-structured-output-contract artifact))))
     :document-coverage-complete?
     (= (set p10-schema-documents) (set (keys documents)))
     :accepted-fixtures-covered?
     (= (set p10-schema-documents) accepted-docs)
     :rejected-fixtures-covered?
     (= (set p10-schema-documents) rejected-docs)
     :conformance-evidence-covered?
     (= (set p10-schema-documents) conformance-docs)
     :diagnostics-covered?
     (set/subset? (set p10-schema-diagnostic-ids) diagnostics)
     :task-statuses (p10-task-statuses)
     :status :complete}))