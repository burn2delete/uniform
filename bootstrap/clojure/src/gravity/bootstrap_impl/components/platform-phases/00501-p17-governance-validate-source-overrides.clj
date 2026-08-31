

(defn p17-governance-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (if-let [id (get p17-governance-override-diagnostics fail-kind)]
      (p17-governance-fail!
       id source-path
       {:document-id (p17-governance-diagnostic-document id)
        :artifact-id "p17-governance-negative-fixture"
        :missing-fact fail-kind}
       {:fixture :rejected-governance})
      (p17-governance-fail!
       "P17-MANIFEST" source-path
       {:artifact-id "p17-governance-unknown-override"
        :missing-fact fail-kind}
       {:missing-fields [:known-override-diagnostic]}))))

(defn p17-governance-record
  [{:keys [document record title owned-surface check-key artifact-key
           dependencies]}]
  {:document document
   :record record
   :title title
   :owned-surface owned-surface
   :owner (str (name record) "-working-group")
   :state :implemented
   :scope #{:language :compiler :runtime :standard-library :package
            :target :security :unsafe :governance}
   :affected-surfaces #{:source :semantic :effect :capability :profile
                        :diagnostic :artifact :runtime :package}
   :review-gates [:compatibility :safety :security :profile :conformance
                  :migration :provenance]
   :evidence [:accepted-fixture :rejected-fixture :diagnostic-stream
              :capability-proof]
   :artifacts [artifact-key]
   :diagnostics (p17-governance-diagnostics-by-document document)
   :decision :accepted-with-stage0-evidence
   :provenance-record (str "phase17:" document ":stage0")
   :checks (assoc {:owner true
                   :scope true
                   :affected-documents true
                   :review-gates true
                   :compatibility true
                   :safety-security true
                   :profile-target true
                   :conformance true
                   :provenance true}
                  check-key true)
   :dependencies dependencies
   :status :complete})

(defn p17-governance-records
  []
  (mapv p17-governance-record p17-governance-document-data))

(defn p17-record-by-document
  [records document]
  (some #(when (= document (:document %)) %) records))

(defn p17-artifact-values
  []
  (let [records (p17-governance-records)
        one #(p17-record-by-document records %)]
    {:language-change-record
     {:artifact :gravity/language-change-record
      :artifact-id "governance:language-change-record"
      :changes [(one "GOV1")]
      :states [:sketch :draft :rfc :prototype :review :accepted
               :implemented :stabilized :deprecated :removed :rejected]
      :release-note-traceability :required
      :status :complete}
     :compatibility-report
     {:artifact :gravity/compatibility-report
      :artifact-id "governance:compatibility-report"
      :records [(one "GOV2")]
      :surfaces [:source :semantic :effect :capability :profile
                 :diagnostic :artifact :runtime :package]
      :baseline-release "gravity-0.1-stage0"
      :migration-records [:stage0-migration-record]
      :status :complete}
     :standard-library-governance-record
     {:artifact :gravity/standard-library-governance-record
      :artifact-id "governance:standard-library"
      :records [(one "GOV3")]
      :module-ownership-map {:gravity.std "stdlib-working-group"}
      :requires-unsafe-audit true
      :requires-compatibility-report true
      :status :complete}
     :security-review-record
     {:artifact :gravity/security-review-record
      :artifact-id "governance:security-review"
      :records [(one "GOV4")]
      :threat-models [:capability-expansion :secret-leak :taint-sink
                      :unsafe-ffi :ai-tool-authority :supply-chain]
      :residual-risk-recorded true
      :status :complete}
     :target-support-matrix
     {:artifact :gravity/target-support-matrix
      :artifact-id "governance:target-support"
      :records [(one "GOV5")]
      :targets [{:target :jvm :tier :experimental
                 :profiles [:core :meta :hosted]
                 :unsupported [:kernel :firmware :hardware]}]
      :unsupported-profile-policy :reject
      :status :complete}
     :rfc-record
     {:artifact :gravity/rfc-record
      :artifact-id "governance:rfc"
      :records [(one "GOV6")]
      :required-sections [:motivation :design :affected-documents
                          :profile-impact :safety-security
                          :compatibility :implementation-plan
                          :test-plan :migration-plan :stabilization]
      :decision-history [:draft :triage :design-review :accepted]
      :status :complete}
     :experiment-registry
     {:artifact :gravity/experiment-registry
      :artifact-id "governance:experiment-registry"
      :records [(one "GOV7")]
      :default-policy :off-in-stable
      :records-opt-in-artifacts true
      :expiry-policy :stabilize-extend-or-remove
      :status :complete}
     :deprecation-plan
     {:artifact :gravity/deprecation-plan
      :artifact-id "governance:deprecation-plan"
      :records [(one "GOV8")]
      :stabilization-evidence [:conformance-history :negative-fixtures
                               :compatibility-report :documentation
                               :migration-readiness]
      :deprecation-schedule [:warn :error :remove]
      :status :complete}
     :unsafe-governance-audit
     {:artifact :gravity/unsafe-governance-audit
      :artifact-id "governance:unsafe-audit"
      :records [(one "GOV9")]
      :unsafe-island-records [:stage0-unsafe-island-record]
      :safe-wrapper-evidence [:proof :runtime-check :negative-fixture]
      :stale-audit-blocks-release true
      :status :complete}
     :ecosystem-package-governance-record
     {:artifact :gravity/ecosystem-package-governance-record
      :artifact-id "governance:ecosystem-package"
      :records [(one "GOV10")]
      :registry-decisions [:accept :reject :quarantine :yank :reserve
                           :transfer :advisory]
      :requires [:identity :owners :namespace :version :provenance
                 :signature :sbom :capability-manifest]
      :status :complete}}))

(defn p17-governance-document-record
  [document]
  (let [summary (p17-governance-data-by-document document)]
    (merge
     {:document document
      :task-id (p17-task-id document)
      :governing-doc (:file summary)
      :suite-id (:owned-surface summary)
      :diagnostics (p17-governance-diagnostics-by-document document)
      :evidence (into {}
                      (map (fn [diagnostic]
                             [(keyword (str/lower-case diagnostic))
                              {:diagnostic diagnostic
                               :source :governing-document
                               :status :present}])
                           (p17-governance-diagnostics-by-document
                            document)))
      :governance
      {:accepted-fixture "bootstrap/clojure/fixtures/accepted/governance-evolution.gravity"
       :rejected-fixture (str "bootstrap/clojure/fixtures/rejected/"
                              (:rejected-fixture summary))
       :artifact-evidence (:artifact-key summary)
       :validation-command "clojure -M:test"
       :status :complete}}
     (select-keys summary [:title :record :owned-surface
                           :accepted-behavior :rejected-diagnostic
                           :artifact-key :dependencies]))))

(defn p17-governance-document-records
  []
  (into {} (map (fn [document]
                  [document (p17-governance-document-record document)])
                p17-governance-documents)))

(defn p17-accepted-governance-fixtures
  []
  (mapv (fn [document]
          (let [summary (p17-governance-data-by-document document)]
            {:document document
             :fixture "bootstrap/clojure/fixtures/accepted/governance-evolution.gravity"
             :artifact (:owned-surface summary)
             :evidence [(p17-task-id document) (:file summary)]
             :status :accepted}))
        p17-governance-documents))

(defn p17-rejected-governance-fixtures
  []
  (mapv (fn [document]
          (let [summary (p17-governance-data-by-document document)]
            {:document document
             :fixture (str "bootstrap/clojure/fixtures/rejected/"
                           (:rejected-fixture summary))
             :artifact :stable-governance-diagnostic
             :diagnostic (:rejected-diagnostic summary)
             :evidence [(p17-task-id document) (:file summary)]
             :status :rejected}))
        p17-governance-documents))

(defn p17-governance-evidence
  []
  (mapv (fn [document]
          (let [summary (p17-governance-data-by-document document)]
            {:document document
             :accepted-behavior (:accepted-behavior summary)
             :rejected-behavior (:rejected-diagnostic summary)
             :artifact (:artifact-key summary)
             :validation ["clojure -M:test"
                          "clojure -M:gravity governance-evolution bootstrap/clojure/fixtures/accepted/governance-evolution.gravity"]
             :status :complete}))
        p17-governance-documents))