

(defn p10-artifact-schema-registry
  [source-schema]
  {:artifact :gravity/artifact-schema-registry
   :schema-id (:schema-id source-schema)
   :schema-version (:schema-version source-schema)
   :schema-hash (:schema-hash source-schema)
   :artifact-kinds [:schema :workflow-graph :agent-manifest
                    :proof-certificate :test-report :benchmark-report
                    :sbom :provenance :generated-api :migration]
   :required-fields #{:kind :schema-version :source-graph :compiler-version
                      :profile :target :backend :runtime :effects
                      :capabilities :content-hash :provenance :conformance}
   :canonical-encoding :s3-canonical-format-for-hash-and-signature-contexts
   :content-hash-schema :canonical-bytes-or-declared-target-format
   :provenance-schema [:source-graph :dependency-lock :compiler :pass-chain]
   :evidence-schema [:types :effects :capabilities :safety :proofs
                     :tests :diagnostics :conformance]
   :release-gate-schema [:provenance :sbom :signature :evidence-links]
   :compatibility-report :schema-version-changes-require-migration-policy
   :status :complete})

(defn p10-ai-structured-output-contract
  [source-schema]
  {:artifact :gravity/ai-structured-output-contract
   :schema-id (:schema-id source-schema)
   :schema-version (:schema-version source-schema)
   :schema-hash (:schema-hash source-schema)
   :prompt-artifact :ticket-classifier-prompt/v2
   :input-schema "TicketText/v1"
   :output-schema "TicketClassification/v2"
   :provider-output-trust :tainted-until-schema-validation
   :tool-use-after-validation? true
   :taint-map {:ticket-text :user-input
               :model-output :ai-output}
   :validation-policy {:partial-output :reject
                       :repair-policy :bounded-retry}
   :status :complete})

(defn p10-document-record
  [document]
  (let [summary (p10-document-summaries document)]
    (merge
     {:document document
      :task-id (p10-task-id document)
      :governing-doc (p10-schema-governing-documents document)
      :schema-id (:schema-id p10-source-schema-contract)
      :schema-version (:schema-version p10-source-schema-contract)
      :schema-hash p10-schema-hash
      :diagnostics (p10-contract-diagnostics document)
      :evidence (p10-contract-evidence document)
      :conformance
      {:accepted-fixture "bootstrap/clojure/fixtures/accepted/schema-interop.gravity"
       :rejected-fixture (str "bootstrap/clojure/fixtures/rejected/"
                              (p10-rejected-fixture-name document))
       :artifact-evidence :schema-interop-artifact
       :validation-command "clojure -M:test"
       :status :complete}}
     summary)))

(defn p10-document-records
  []
  (into {} (map (fn [document] [document (p10-document-record document)])
                p10-schema-documents)))

(defn p10-accepted-schema-fixtures
  []
  (mapv (fn [document]
          {:document document
           :fixture "bootstrap/clojure/fixtures/accepted/schema-interop.gravity"
           :artifact (get-in (p10-document-summaries document) [:owned-surface])
           :evidence [(p10-task-id document)
                      (p10-schema-governing-documents document)]
           :status :accepted})
        p10-schema-documents))

(defn p10-rejected-schema-fixtures
  []
  (mapv (fn [document]
          (let [diagnostic (p10-schema-rejected-diagnostics document)]
            {:document document
             :fixture (str "bootstrap/clojure/fixtures/rejected/"
                           (p10-rejected-fixture-name document))
             :artifact :stable-schema-interop-diagnostic
             :diagnostic diagnostic
             :evidence [(p10-task-id document)
                        (p10-schema-governing-documents document)]
             :status :rejected}))
        p10-schema-documents))

(defn p10-schema-conformance-evidence
  []
  (mapv (fn [document]
          {:document document
           :accepted-behavior (get-in (p10-document-summaries document)
                                      [:accepted-behavior])
           :rejected-behavior (p10-schema-rejected-diagnostics document)
           :artifacts (get-in (p10-document-summaries document)
                              [:artifact-keys])
           :validation ["clojure -M:test"
                        "clojure -M:gravity schema-interop bootstrap/clojure/fixtures/accepted/schema-interop.gravity"]
           :status :complete})
        p10-schema-documents))

(defn p10-schema-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/phase10-schema-interop-diagnostic-stream
   :stage :schema-interop
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           (let [document (p10-schema-diagnostic-document id)]
             {:artifact :gravity/diagnostic
              :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
              :diagnostic id
              :rule id
              :severity :error
              :stage :schema-interop
              :document-id document
              :task (when document (p10-task-id document))
              :primary {:span (source-span source-path index)
                        :syntax-id (str "p10-schema-syntax-" index)
                        :artifact input-id}
              :missing-fact (or (some (fn [[_ [diagnostic missing-fact]]]
                                         (when (= id diagnostic) missing-fact))
                                       (get p10-schema-contracts document))
                                :schema_interop_manifest)
              :remediation [{:kind :preserve-source-schema-authority}
                            {:kind :attach-schema-hash-and-source-span}
                            {:kind :preserve-taint-effects-capabilities}
                            {:kind :add-accepted-and-rejected-fixtures}]
              :ordering-key [id :schema-interop]}))
         p10-schema-diagnostic-ids
         (range))
   :status :complete})

(defn p10-present?
  [value]
  (if (coll? value)
    (seq value)
    (some? value)))