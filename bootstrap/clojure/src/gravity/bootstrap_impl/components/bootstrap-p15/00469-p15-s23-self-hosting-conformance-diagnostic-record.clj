

(defn p15-s23-self-hosting-conformance-diagnostic-record
  [source-path id value data]
  {:artifact :gravity/diagnostic
   :diagnostic-id (str "diag-" (str/lower-case id))
   :diagnostic id
   :severity :error
   :stage :p15-s23-self-hosting-conformance-report
   :source-span {:source source-path}
   :message (get p15-s23-self-hosting-conformance-diagnostic-messages id)
   :facts data
   :observed value
   :remediation :repair_self_hosting_conformance_report})

(defn p15-s23-stage-support-conformance-record
  [source-path stage-artifact phase14-artifact]
  (let [report (:conformance-report phase14-artifact)
        conformance (:conformance-results report)
        self-hosting (:self-hosting-validation-record report)
        stage-matrix (:stage-equivalence-matrix stage-artifact)
        diagnostic-record
        (get-in stage-artifact
                [:rejected-app-diagnostic-artifact
                 :diagnostic-preservation-record])
        conformant?
        (and (= :complete (get-in report
                                  [:conformance-manifest :status]))
             (= :complete (:status self-hosting))
             (= :complete (:status stage-matrix))
             (true? (:current-candidate-equivalent-to-seed?
                     stage-matrix))
             (= :metadata-gate-only
                (:conformance-gate-status conformance))
             (true? (:all-diagnostics-match? diagnostic-record)))]
    {:artifact :gravity/p15-s23-stage-support-conformance-record
     :source-path source-path
     :stage :p15-s23-current-clojure-seed-candidate
     :declared-support-level :current-compiled-core-app-slice
     :supported-profiles [:hosted]
     :supported-backends [:jvm-instruction-plan]
     :supported-runtimes [:managed-clojure-jvm-instruction-runner]
     :supported-test-suites (:document-set report)
     :conformance-gate-status (:conformance-gate-status conformance)
     :required-diagnostic-count
     (count (:required-diagnostic-ids conformance))
     :stage-comparison-row-count (count (:rows stage-matrix))
     :phase14-conformance-status (get-in report
                                         [:conformance-manifest :status])
     :test13-self-hosting-status (:status self-hosting)
     :stage-support-conformant? (boolean conformant?)
     :release-eligible? false
     :status (if conformant? :complete :failed)}))

(defn p15-s23-conformance-suite-link-table
  [stage-artifact phase14-artifact]
  (let [report (:conformance-report phase14-artifact)
        self-hosting (:self-hosting-validation-record report)
        phase14-linked?
        (and (= :gravity/stage0-hosted-core-compiled-conformance-proof
                (:kind phase14-artifact))
             (= :complete
                (get-in phase14-artifact
                        [:capability-based-proof :status])))
        test13-linked?
        (and (= "TEST13" (name (:document self-hosting)))
             (= :complete (:status self-hosting)))
        stage-linked?
        (and (= :gravity/p15-s23-stage-comparison-report-artifact
                (:kind stage-artifact))
             (true?
              (get-in stage-artifact
                      [:stage-equivalence-matrix
                       :current-candidate-equivalent-to-seed?])))]
    {:artifact :gravity/p15-s23-conformance-suite-link-table
     :phase14-conformance-linked? (boolean phase14-linked?)
     :test13-self-hosting-linked? (boolean test13-linked?)
     :stage-comparison-linked? (boolean stage-linked?)
     :linked-suites
     [{:suite :phase14-hosted-core-compiled-conformance
       :artifact-id (:artifact-id phase14-artifact)
       :report-id (get-in report [:report-id])
       :status (get-in report [:conformance-manifest :status])}
      {:suite :test13-self-hosting-validation
       :artifact-id (:artifact-id self-hosting)
       :document (:document self-hosting)
       :status (:status self-hosting)}
      {:suite :p15-s23-stage-comparison
       :artifact-id (:artifact-id stage-artifact)
       :proof-id (:proof-id stage-artifact)
       :status (get-in stage-artifact
                       [:stage-equivalence-matrix :status])}]
     :status (if (and phase14-linked? test13-linked? stage-linked?)
               :complete
               :failed)}))

(defn p15-s23-diagnostic-conformance-record
  [stage-artifact phase14-artifact]
  (let [report (:conformance-report phase14-artifact)
        golden (:golden-diagnostics-record report)
        conformance (:conformance-results report)
        p15-diagnostic-preservation
        (get-in stage-artifact
                [:rejected-app-diagnostic-artifact
                 :diagnostic-preservation-record])
        required (set (:required-diagnostic-ids conformance))
        p15-diagnostics
        (get-in stage-artifact
                [:rejected-diagnostic-stage-comparison
                 :candidate-diagnostics])
        diagnostics-preserved?
        (and (true? (:stable-codes golden))
             (true? (:source-spans golden))
             (contains? required "TEST13002")
             (true? (:source-spans-present?
                     p15-diagnostic-preservation))
             (true? (:diagnostic-codes-stable?
                     p15-diagnostic-preservation))
             (true? (get-in stage-artifact
                             [:rejected-diagnostic-stage-comparison
                              :rejected-diagnostics-equivalent?])))]
    {:artifact :gravity/p15-s23-diagnostic-conformance-record
     :phase14-required-diagnostics (vec (sort required))
     :phase14-golden-diagnostic-count
     (count (:diagnostics golden))
     :phase14-stable-codes? (:stable-codes golden)
     :phase14-source-spans? (:source-spans golden)
     :test13-diagnostic-covered? (contains? required "TEST13002")
     :p15-rejected-diagnostics p15-diagnostics
     :p15-diagnostic-codes-stable?
     (:diagnostic-codes-stable? p15-diagnostic-preservation)
     :p15-source-spans-present?
     (:source-spans-present? p15-diagnostic-preservation)
     :diagnostics-preserved? (boolean diagnostics-preserved?)
     :status (if diagnostics-preserved? :complete :failed)}))

(defn p15-s23-conformance-gap-record
  []
  {:artifact :gravity/p15-s23-conformance-gap-record
   :known-gaps
   [{:gap :whole-language-compiler-artifact
     :status :pending
     :diagnostic "P15S23001"}
    {:gap :production-conformance-runner
     :status :pending
     :reason :current-report-links-stage0-metadata-gate}
    {:gap :external-backend-validation
     :status :pending
     :reason :current-support-level-is-jvm-instruction-plan}
    {:gap :full-self-hosted-conformance-runner
     :status :pending
     :reason :clojure-seed-still-trusted}
    {:gap :provenance-attestation
     :status :pending
     :diagnostic "P15S23011"}]
   :full-language-compiler-self-hosted? false
   :clojure-seed-retired? false
   :status :complete})

(defn p15-s23-self-hosting-conformance-proof-diagnostics
  [source-path candidate]
  (let [proof-contract (:proof-contract candidate)
        support (:stage-support-conformance-record candidate)
        links (:conformance-suite-link-table candidate)
        diagnostic-record (:diagnostic-conformance-record candidate)
        stage-artifact (:stage-comparison-artifact candidate)
        claims (:self-hosting-claims proof-contract)
        suite-scope (set (:suite-scope proof-contract))
        preserves (set (:preserves proof-contract))
        missing-preserves
        (set/difference
         p15-s23-self-hosting-conformance-required-preserves
         preserves)]
    (vec
     (concat
      (when-not (= :gravity/self-hosting-conformance-report
                   (:artifact proof-contract))
        [(p15-s23-self-hosting-conformance-diagnostic-record
          source-path "P15S23H001" proof-contract
          {:missing-fields [:artifact]})])
      (when-not
       (and (= (set p15-s23-self-hosting-conformance-scope)
               suite-scope)
            (empty? missing-preserves)
            (= :gravity/p15-s23-stage-support-conformance-record
               (:artifact support))
            (= :complete (:status support))
            (true? (:stage-support-conformant? support)))
        [(p15-s23-self-hosting-conformance-diagnostic-record
          source-path "P15S23H002" candidate
          {:expected-scope p15-s23-self-hosting-conformance-scope
           :observed-scope (vec (sort suite-scope))
           :missing-preserves (vec (sort missing-preserves))
           :support-status (:status support)})])
      (when-not
       (and (= :complete (:status links))
            (true? (:phase14-conformance-linked? links))
            (true? (:test13-self-hosting-linked? links)))
        [(p15-s23-self-hosting-conformance-diagnostic-record
          source-path "P15S23H003" links
          {:required-links [:phase14-hosted-core-compiled-conformance
                            :test13-self-hosting-validation]})])
      (when-not
       (and (true? (:stage-comparison-linked? links))
            (= :gravity/p15-s23-stage-comparison-report-artifact
               (:kind stage-artifact))
            (true?
             (get-in stage-artifact
                     [:stage-equivalence-matrix
                      :current-candidate-equivalent-to-seed?])))
        [(p15-s23-self-hosting-conformance-diagnostic-record
          source-path "P15S23H004" stage-artifact
          {:required-link :p15-s23-stage-comparison})])
      (when-not
       (and (= :complete (:status diagnostic-record))
            (true? (:diagnostics-preserved? diagnostic-record)))
        [(p15-s23-self-hosting-conformance-diagnostic-record
          source-path "P15S23H005" diagnostic-record
          {:required [:stable-diagnostic-codes
                      :source-spans
                      :test13-diagnostic]})])
      (when (or (true? (:full-language-compiler-self-hosted? claims))
                (true? (:clojure-seed-retired? claims)))
        [(p15-s23-self-hosting-conformance-diagnostic-record
          source-path "P15S23H006" claims
          {:full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? claims)
           :clojure-seed-retired?
           (:clojure-seed-retired? claims)})])))))