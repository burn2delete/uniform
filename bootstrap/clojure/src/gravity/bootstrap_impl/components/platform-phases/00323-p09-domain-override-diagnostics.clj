

(def p09-domain-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             p09-domain-diagnostic-ids)))

(defn p09-document-number
  [document]
  (Integer/parseInt (subs document 3)))

(defn p09-task-id
  [document]
  (str "P09-D" (+ 123 (p09-document-number document))))

(defn p09-domain-source-overrides
  [module]
  (get-in module [:metadata :domain :coverage] {}))

(defn p09-domain-diagnostic-document
  [diagnostic-id]
  (some (fn [document]
          (when (some #(= diagnostic-id (first %))
                      (vals (p09-domain-contracts document)))
            document))
        p09-domain-documents))

(defn p09-domain-fail!
  [id source-path subject extra]
  (let [document (or (:document-id subject)
                     (p09-domain-diagnostic-document id))]
    (fail! id
           "P09 domain coverage validation failed"
           (merge {:source-span (or (:source-span subject)
                                    (source-span source-path 0))
                   :diagnostic-family :phase09-domain-coverage
                   :stage :domain-coverage
                   :document-id document
                   :task (when document (p09-task-id document))
                   :domain (:domain subject)
                   :profile (or (:profile subject) :hosted)
                   :target (or (:target subject) :jvm)
                   :artifact-id (:artifact-id subject)
                   :claim-id (:claim-id subject)
                   :missing-fact (:missing-fact subject)
                   :fallback-status :rejected
                   :remediation "Phase 09 requires a slice-scoped domain manifest with profiles, backends, runtime services, effects, capabilities, artifacts, accepted and rejected fixtures, replacement claim boundaries, conformance evidence, and stable DOM diagnostics."}
                  extra))))

(defn p09-domain-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get p09-domain-override-diagnostics fail-kind)]
      (p09-domain-fail!
       id source-path
       {:artifact-id (str "p09-domain-" (name fail-kind))
        :claim-id (when (= id "P09-CLAIM") "phase09-broad-replacement")
        :document-id (p09-domain-diagnostic-document id)
        :missing-fact fail-kind}
       {:missing-fields [fail-kind]}))))

(defn p09-contract-diagnostics
  [document]
  (mapv (comp first val)
        (sort-by (comp name key) (p09-domain-contracts document))))

(defn p09-contract-evidence
  [document]
  (into {}
        (map (fn [[fact [diagnostic missing-fact]]]
               [fact {:diagnostic diagnostic
                      :missing-fact missing-fact
                      :source :governing-document
                      :status :present}])
             (sort-by (comp name key) (p09-domain-contracts document)))))

(defn p09-domain-record
  [document]
  (let [summary (p09-domain-summaries document)
        diagnostic (p09-domain-rejected-diagnostics document)]
    (merge
     {:document document
      :task-id (p09-task-id document)
      :governing-doc (p09-domain-governing-documents document)
      :diagnostics (p09-contract-diagnostics document)
      :evidence (p09-contract-evidence document)
      :replacement-scope
      {:claim-status :slice-supported
       :scope (str "Phase 09 " document " implementable vertical slice")
       :provider-boundaries #{:host-runtime :backend-provider
                              :external-toolchain :platform-provider}
       :claim-limits #{:no-platform-wide-replacement
                       :no-provider-replacement
                       :requires-fixture-evidence}}
      :conformance
      {:accepted-fixture "bootstrap/clojure/fixtures/accepted/domain-coverage.gravity"
       :rejected-fixture (str "bootstrap/clojure/fixtures/rejected/domain-"
                              (str/lower-case document) "-"
                              (str/lower-case diagnostic)
                              ".gravity")
       :artifact-evidence :domain-coverage-artifact
       :validation-command "clojure -M:test"
       :status :complete}}
     summary)))

(defn p09-domain-records
  []
  (into {} (map (fn [document] [document (p09-domain-record document)])
                p09-domain-documents)))

(defn p09-domain-slice-manifest
  [input-id]
  {:artifact :gravity/domain-slice-manifest
   :input-artifact input-id
   :documents p09-domain-documents
   :packet [:incumbent-comparison :profiles :backends :runtime-services
            :effects :capabilities :examples :artifacts :proof-gaps
            :replacement-scope :conformance-evidence]
   :incumbent-comparison :recorded-per-domain
   :profile-matrix :profile-target-backend-runtime-separated
   :artifact-packet :manifest-fixtures-diagnostics-conformance
   :proof-gaps :recorded-as-later-phase-dependencies
   :replacement-claim-policy :slice-scoped-only
   :status :complete})

(defn p09-accepted-domain-fixtures
  []
  (mapv (fn [document]
          {:document document
           :fixture "bootstrap/clojure/fixtures/accepted/domain-coverage.gravity"
           :artifact :domain-slice-manifest
           :evidence [(p09-task-id document)
                      (p09-domain-governing-documents document)]
           :status :accepted})
        p09-domain-documents))

(defn p09-rejected-domain-fixtures
  []
  (mapv (fn [document]
          (let [diagnostic (p09-domain-rejected-diagnostics document)]
            {:document document
             :fixture (str "bootstrap/clojure/fixtures/rejected/domain-"
                           (str/lower-case document) "-"
                           (str/lower-case diagnostic)
                           ".gravity")
             :artifact :stable-domain-diagnostic
             :diagnostic diagnostic
             :evidence [(p09-task-id document)
                        (p09-domain-governing-documents document)]
             :status :rejected}))
        p09-domain-documents))

(defn p09-replacement-claim-records
  []
  (mapv (fn [document]
          {:document document
           :claim-id (str "phase09-" (str/lower-case document) "-slice-claim")
           :claim-status :slice-supported
           :evidence-refs [(p09-task-id document)
                           "bootstrap/clojure/fixtures/accepted/domain-coverage.gravity"
                           (str "diagnostic:" (p09-domain-rejected-diagnostics document))]
           :excluded-provider-boundaries #{:host-platform :cloud-provider
                                           :browser-engine :mobile-os
                                           :chain-consensus :proof-provider}
           :status :complete})
        p09-domain-documents))

(defn p09-domain-conformance-evidence
  []
  (mapv (fn [document]
          {:document document
           :accepted-behavior :slice-manifest-accepted
           :rejected-behavior (p09-domain-rejected-diagnostics document)
           :artifacts [:domain-slice-manifest :domain-contract
                       :replacement-claim-record :diagnostic-stream]
           :validation ["clojure -M:test"
                        "clojure -M:gravity domain-coverage bootstrap/clojure/fixtures/accepted/domain-coverage.gravity"]
           :status :complete})
        p09-domain-documents))

(defn p09-domain-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/phase09-domain-diagnostic-stream
   :stage :domain-coverage
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           (let [document (p09-domain-diagnostic-document id)]
             {:artifact :gravity/diagnostic
              :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
              :diagnostic id
              :rule id
              :severity :error
              :stage :domain-coverage
              :document-id document
              :task (when document (p09-task-id document))
              :primary {:span (source-span source-path index)
                        :syntax-id (str "p09-domain-syntax-" index)
                        :artifact input-id}
              :missing-fact (or (some (fn [[_ [diagnostic missing-fact]]]
                                         (when (= id diagnostic) missing-fact))
                                       (get p09-domain-contracts document))
                                :domain_coverage_manifest)
              :remediation [{:kind :complete-domain-packet}
                            {:kind :attach-accepted-and-rejected-fixtures}
                            {:kind :scope-replacement-claim}
                            {:kind :record-conformance-evidence}]
              :ordering-key [id :domain-coverage]}))
         p09-domain-diagnostic-ids
         (range))
   :status :complete})

(defn p09-present?
  [value]
  (if (coll? value)
    (seq value)
    (some? value)))