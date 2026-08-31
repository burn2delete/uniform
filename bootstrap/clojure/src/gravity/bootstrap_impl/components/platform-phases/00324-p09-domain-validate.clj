

(defn p09-domain-validate!
  [source-path artifact]
  (let [manifest (:domain-slice-manifest artifact)
        domains (:domain-contracts artifact)
        accepted (:accepted-domain-fixtures artifact)
        rejected (:rejected-domain-fixtures artifact)
        claims (:replacement-claim-records artifact)
        conformance (:domain-conformance-evidence artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:domain-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= (set p09-domain-documents) (set (:documents manifest)))
      (p09-domain-fail! "P09-MANIFEST" source-path manifest
                        {:missing-fields [:documents]}))
    (when-not (every? #(contains? domains %) p09-domain-documents)
      (p09-domain-fail! "P09-MANIFEST" source-path domains
                        {:missing-fields [:domain-contracts]}))
    (doseq [document p09-domain-documents
            :let [record (get domains document)
                  summary (p09-domain-summaries document)
                  contract (p09-domain-contracts document)]]
      (doseq [field [:domain :profiles :backends :runtime-services
                     :schemas :capabilities :effects :artifacts :examples
                     :rejects :dependencies :replacement-scope :conformance
                     :evidence]]
        (when-not (p09-present? (get record field))
          (p09-domain-fail! "P09-MANIFEST" source-path record
                            {:missing-fields [field]})))
      (when-not (= (:domain summary) (:domain record))
        (p09-domain-fail! "P09-MANIFEST" source-path record
                          {:missing-fields [:domain]}))
      (doseq [[fact [diagnostic _]] contract]
        (when-not (and (contains? (set (:diagnostics record)) diagnostic)
                       (get-in record [:evidence fact]))
          (p09-domain-fail! diagnostic source-path record
                            {:missing-fields [fact]})))
      (when (= :full-replacement
               (get-in record [:replacement-scope :claim-status]))
        (p09-domain-fail! "P09-CLAIM" source-path record
                          {:missing-fields [:slice-supported]})))
    (when-not (= (set p09-domain-documents)
                 (set (map :document accepted)))
      (p09-domain-fail! "P09-ACCEPTED" source-path accepted
                        {:missing-fields [:accepted-domain-fixtures]}))
    (when-not (= (set p09-domain-documents)
                 (set (map :document rejected)))
      (p09-domain-fail! "P09-REJECTED" source-path rejected
                        {:missing-fields [:rejected-domain-fixtures]}))
    (when-not (= (set p09-domain-documents)
                 (set (map :document claims)))
      (p09-domain-fail! "P09-CLAIM" source-path claims
                        {:missing-fields [:replacement-claim-records]}))
    (doseq [claim claims]
      (when-not (and (= :slice-supported (:claim-status claim))
                     (seq (:evidence-refs claim))
                     (seq (:excluded-provider-boundaries claim)))
        (p09-domain-fail! "P09-CLAIM" source-path claim
                          {:missing-fields [:evidence-refs]})))
    (when-not (= (set p09-domain-documents)
                 (set (map :document conformance)))
      (p09-domain-fail! "P09-CONFORMANCE" source-path conformance
                        {:missing-fields [:domain-conformance-evidence]}))
    (when-not (every? #(and (:accepted-behavior %)
                            (:rejected-behavior %)
                            (seq (:artifacts %))
                            (seq (:validation %)))
                      conformance)
      (p09-domain-fail! "P09-CONFORMANCE" source-path conformance
                        {:missing-fields [:conformance-record]}))
    (when-not (set/subset? (set p09-domain-diagnostic-ids) diagnostics)
      (p09-domain-fail! "P09-MANIFEST" source-path
                        (:domain-diagnostic-stream artifact)
                        {:missing-fields [:diagnostics]})))
  :complete)

(defn p09-task-statuses
  []
  (merge (zipmap ["P09-T01" "P09-T02" "P09-T03"
                  "P09-T04" "P09-T05" "P09-T06"]
                 (repeat :complete))
         (zipmap (map p09-task-id p09-domain-documents)
                 (repeat :complete))))

(defn p09-domain-proof
  [artifact]
  (let [domains (:domain-contracts artifact)
        accepted-docs (set (map :document (:accepted-domain-fixtures artifact)))
        rejected-docs (set (map :document (:rejected-domain-fixtures artifact)))
        claim-statuses (set (map :claim-status (:replacement-claim-records artifact)))
        conformance-docs (set (map :document (:domain-conformance-evidence artifact)))
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:domain-diagnostic-stream
                                       :diagnostics])))]
    {:domain-slice-template-covered?
     (= :complete (get-in artifact [:domain-slice-manifest :status]))
     :systems-domains-covered?
     (every? #(contains? domains %) ["DOM1" "DOM2" "DOM3" "DOM4"
                                     "DOM5" "DOM13" "DOM15" "DOM19"])
     :application-domains-covered?
     (every? #(contains? domains %) ["DOM6" "DOM7" "DOM8" "DOM14"
                                     "DOM20" "DOM21"])
     :data-and-distributed-domains-covered?
     (every? #(contains? domains %) ["DOM9" "DOM10" "DOM11"])
     :ai-and-tooling-domains-covered?
     (every? #(contains? domains %) ["DOM17" "DOM18" "DOM21"])
     :domain-claim-governance-covered?
     (= claim-statuses #{:slice-supported})
     :document-coverage-complete?
     (= (set p09-domain-documents) (set (keys domains)))
     :accepted-fixtures-covered?
     (= (set p09-domain-documents) accepted-docs)
     :rejected-fixtures-covered?
     (= (set p09-domain-documents) rejected-docs)
     :conformance-evidence-covered?
     (= (set p09-domain-documents) conformance-docs)
     :special-domain-obligations-covered?
     (and (contains? (set (get-in domains ["DOM15" :diagnostics]))
                     "DOM15-PASSKEY")
          (contains? (set (get-in domains ["DOM15" :diagnostics]))
                     "DOM15-PRIVATE-COMPUTE")
          (contains? (set (get-in domains ["DOM16" :diagnostics]))
                     "DOM16-ERC4337")
          (contains? (set (get-in domains ["DOM16" :diagnostics]))
                     "DOM16-EIP7702")
          (contains? (set (get-in domains ["DOM16" :diagnostics]))
                     "DOM16-ERC7579")
          (contains? (set (get-in domains ["DOM19" :diagnostics]))
                     "DOM19-ZK-PRIVACY"))
     :diagnostics-covered?
     (set/subset? (set p09-domain-diagnostic-ids) diagnostics)
     :task-statuses (p09-task-statuses)
     :status :complete}))