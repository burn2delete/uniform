

(defn p12-package-validate!
  [source-path artifact]
  (let [documents (:document-contracts artifact)
        accepted (:accepted-package-fixtures artifact)
        rejected (:rejected-package-fixtures artifact)
        conformance (:package-conformance-evidence artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:package-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= (set p12-package-documents)
                 (set (:document-set artifact)))
      (p12-package-fail! "P12-MANIFEST" source-path artifact
                         {:missing-fields [:document-set]}))
    (doseq [artifact-key p12-package-artifact-keys]
      (when-not (p12-present? (get artifact artifact-key))
        (p12-package-fail! "P12-MANIFEST" source-path artifact
                           {:missing-fields [artifact-key]}))
      (when-not (= :complete (:status (get artifact artifact-key)))
        (p12-package-fail! "P12-MANIFEST" source-path
                           (get artifact artifact-key)
                           {:missing-fields [:status]})))
    (when-not (every? #(contains? documents %) p12-package-documents)
      (p12-package-fail! "P12-MANIFEST" source-path documents
                         {:missing-fields [:document-contracts]}))
    (doseq [document p12-package-documents
            :let [record (get documents document)
                  summary (p12-package-document-summaries document)
                  contract (p12-package-contracts document)]]
      (doseq [field [:document :task-id :governing-doc :package-id
                     :diagnostics :evidence :owned-surface
                     :accepted-behavior :rejected-behavior :artifact-keys
                     :conformance]]
        (when-not (p12-present? (get record field))
          (p12-package-fail! "P12-MANIFEST" source-path record
                             {:missing-fields [field]})))
      (when-not (= (:owned-surface summary) (:owned-surface record))
        (p12-package-fail! "P12-MANIFEST" source-path record
                           {:missing-fields [:owned-surface]}))
      (doseq [[fact [diagnostic _]] contract]
        (when-not (and (contains? (set (:diagnostics record)) diagnostic)
                       (get-in record [:evidence fact]))
          (p12-package-fail! diagnostic source-path record
                             {:missing-fields [fact]}))))
    (when-not (= (set p12-package-documents) (set (map :document accepted)))
      (p12-package-fail! "P12-ACCEPTED" source-path accepted
                         {:missing-fields [:accepted-package-fixtures]}))
    (when-not (= (set p12-package-documents) (set (map :document rejected)))
      (p12-package-fail! "P12-REJECTED" source-path rejected
                         {:missing-fields [:rejected-package-fixtures]}))
    (when-not (= (set p12-package-documents) (set (map :document conformance)))
      (p12-package-fail! "P12-CONFORMANCE" source-path conformance
                         {:missing-fields [:package-conformance-evidence]}))
    (when-not (true? (:lockfile-complete (:project-manifest artifact)))
      (p12-package-fail! "PKG1006" source-path (:project-manifest artifact)
                         {:missing-fields [:lockfile-complete]}))
    (when-not (every? #(set/subset? (:effects %) (get-in artifact
                                                         [:build-graph
                                                          :declared-effects]))
                      (get-in artifact [:build-graph :nodes]))
      (p12-package-fail! "PKG2001" source-path (:build-graph artifact)
                         {:missing-fields [:declared-effects]}))
    (when-not (get-in artifact [:artifact-manifest :evidence :safety])
      (p12-package-fail! "PKG3005" source-path (:artifact-manifest artifact)
                         {:missing-fields [:safety-evidence]}))
    (when-not (true? (:download-verified (:package-operation artifact)))
      (p12-package-fail! "PKG4001" source-path (:package-operation artifact)
                         {:missing-fields [:download-verified]}))
    (when-not (true? (:capability-compatible (:resolution-report artifact)))
      (p12-package-fail! "PKG5002" source-path (:resolution-report artifact)
                         {:missing-fields [:capability-compatible]}))
    (when (seq (set/intersection
                (get-in artifact [:capability-manifest :capabilities :denies])
                (get-in artifact [:capability-manifest :capabilities :requests])))
      (p12-package-fail! "PKG6004" source-path (:capability-manifest artifact)
                         {:missing-fields [:denied-authority]}))
    (when-not (= :disabled (get-in artifact
                                   [:reproducible-build-recipe
                                    :environment :network]))
      (p12-package-fail! "PKG7003" source-path
                         (:reproducible-build-recipe artifact)
                         {:missing-fields [:controlled-network]}))
    (when-not (p12-present? (get-in artifact
                                    [:package-safety
                                     :unsafe-audit-metadata]))
      (p12-package-fail! "PKG8001" source-path (:package-safety artifact)
                         {:missing-fields [:unsafe-audit-metadata]}))
    (when-not (true? (:access-grant (:registry-record artifact)))
      (p12-package-fail! "PKG9001" source-path (:registry-record artifact)
                         {:missing-fields [:access-grant]}))
    (when-not (:source-graph-hash (:provenance-record artifact))
      (p12-package-fail! "PKG10001" source-path (:provenance-record artifact)
                         {:missing-fields [:source-graph-hash]}))
    (when (true? (:implicit-host-target? (:target-matrix artifact)))
      (p12-package-fail! "PKG11002" source-path (:target-matrix artifact)
                         {:missing-fields [:explicit-target]}))
    (when-not (true? (get-in artifact
                             [:signing-sbom-verification
                              :signature :canonical-payload]))
      (p12-package-fail! "PKG12002" source-path
                         (:signing-sbom-verification artifact)
                         {:missing-fields [:canonical-payload]}))
    (when-not (set/subset? (set p12-package-diagnostic-ids) diagnostics)
      (p12-package-fail! "P12-MANIFEST" source-path
                         (:package-diagnostic-stream artifact)
                         {:missing-fields [:diagnostics]})))
  :complete)

(defn p12-task-statuses
  []
  (merge (zipmap ["P12-T01" "P12-T02" "P12-T03"
                  "P12-T04" "P12-T05" "P12-T06"]
                 (repeat :complete))
         (zipmap (map p12-task-id p12-package-documents)
                 (repeat :complete))))

(defn p12-package-proof
  [artifact]
  (let [documents (:document-contracts artifact)
        accepted-docs (set (map :document (:accepted-package-fixtures artifact)))
        rejected-docs (set (map :document (:rejected-package-fixtures artifact)))
        conformance-docs (set (map :document
                                   (:package-conformance-evidence artifact)))
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:package-diagnostic-stream
                                       :diagnostics])))]
    {:project-and-build-graph-covered?
     (and (= :complete (:status (:project-manifest artifact)))
          (= :complete (:status (:build-graph artifact))))
     :package-and-resolution-covered?
     (and (= :complete (:status (:package-operation artifact)))
          (true? (:deterministic (:resolution-report artifact)))
          (true? (:complete (:lockfile artifact))))
     :capability-and-safety-covered?
     (and (= :complete (:status (:capability-manifest artifact)))
          (= :reviewed (:review-state (:package-safety artifact))))
     :reproducibility-covered?
     (and (= :disabled (get-in artifact
                               [:reproducible-build-recipe
                                :environment :network]))
          (= :manifest-and-content-hash
             (:rebuild-verification
              (:reproducible-build-recipe artifact))))
     :registry-provenance-targets-covered?
     (and (true? (:access-grant (:registry-record artifact)))
          (= :checked (:revocation-status (:provenance-record artifact)))
          (false? (:implicit-host-target? (:target-matrix artifact))))
     :signing-sbom-verification-covered?
     (and (true? (get-in artifact
                         [:signing-sbom-verification
                          :signature :canonical-payload]))
          (= :accepted
             (:consumer-decision (:signing-sbom-verification artifact))))
     :document-coverage-complete?
     (= (set p12-package-documents) (set (keys documents)))
     :accepted-fixtures-covered?
     (= (set p12-package-documents) accepted-docs)
     :rejected-fixtures-covered?
     (= (set p12-package-documents) rejected-docs)
     :conformance-evidence-covered?
     (= (set p12-package-documents) conformance-docs)
     :diagnostics-covered?
     (set/subset? (set p12-package-diagnostic-ids) diagnostics)
     :task-statuses (p12-task-statuses)
     :status :complete}))