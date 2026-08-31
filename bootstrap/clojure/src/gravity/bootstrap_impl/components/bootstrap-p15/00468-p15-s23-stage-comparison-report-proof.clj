

(defn p15-s23-stage-comparison-report-proof
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:p15-s23-stage-comparison-diagnostic-stream
                           :diagnostics])))
        rejected-diagnostics
        (set (mapcat #(map :diagnostic (:diagnostics %))
                     (:rejected-p15-s23-stage-comparison-fixtures
                      artifact)))
        matrix (:stage-equivalence-matrix artifact)
        boundary (:stage-boundary-record artifact)]
    {:stage-comparison-report-authored-in-gravity? true
     :status :in-progress
     :task "P15-S23"
     :comparison-scope-complete?
     (= (set p15-s23-stage-comparison-scope)
        (set (map :scope (:rows matrix))))
     :current-candidate-equivalent-to-seed?
     (true? (:current-candidate-equivalent-to-seed? matrix))
     :accepted-output-equivalent?
     (true? (get-in artifact
                    [:accepted-output-stage-comparison
                     :accepted-output-equivalent?]))
     :rejected-diagnostics-equivalent?
     (true? (get-in artifact
                    [:rejected-diagnostic-stage-comparison
                     :rejected-diagnostics-equivalent?]))
     :rebuild-log-linked?
     (= :gravity/p15-s23-reproducible-rebuild-log-artifact
        (get-in artifact [:reproducible-rebuild-artifact :kind]))
     :stage-boundary-explicit?
     (and (= :clojure-stage0 (:seed-stage boundary))
          (true? (:clojure-seed-boundary? boundary))
          (false? (:candidate-is-self-hosted? boundary))
          (false? (:full-self-hosted-equivalence? boundary)))
     :does-not-claim-full-self-hosting?
     (false? (:full-language-compiler-self-hosted? artifact))
     :does-not-claim-clojure-seed-retirement?
     (false? (:clojure-seed-retired? artifact))
     :rejected-candidates-covered?
     (set/subset? (set p15-s23-stage-comparison-diagnostic-ids)
                  rejected-diagnostics)
     :diagnostics-covered?
     (= (set p15-s23-stage-comparison-diagnostic-ids) diagnostics)
     :limitations
     {:full-language-compiler-self-hosted? false
      :clojure-seed-retired? false
      :current-candidate-is-clojure-seed? true
      :full-self-hosted-equivalence? false
      :full-self-hosted-toolchain? false
      :next-required-capability
      :implement_conformance_report}}))

(defn p15-s23-stage-comparison-report-source-artifact
  [source-path]
  (p15-s23-context-artifact
   :stage-comparison-report source-path
   (fn [] (let [source-data (p15-s23-compiler-source-form-record source-path)
        proof-contract
        (p15-s23-compiler-def-value
         source-path
         (:forms source-data)
         'p15-s23-stage-comparison-report)
        pipeline-artifact
        (p15-s23-compiler-pipeline-manifest-source-artifact source-path)
        accepted-artifact
        (p15-s23-accepted-app-execution-source-artifact source-path)
        rejected-artifact
        (p15-s23-rejected-app-diagnostic-source-artifact source-path)
        rebuild-artifact
        (p15-s23-reproducible-rebuild-log-source-artifact source-path)
        boundary
        (p15-s23-stage-boundary-record source-path)
        accepted-comparison
        (p15-s23-accepted-output-stage-comparison accepted-artifact)
        rejected-comparison
        (p15-s23-rejected-diagnostic-stage-comparison rejected-artifact)
        matrix
        (p15-s23-stage-equivalence-matrix
         pipeline-artifact accepted-artifact rejected-artifact
         rebuild-artifact accepted-comparison rejected-comparison)
        candidate {:proof-contract proof-contract
                   :stage-boundary-record boundary
                   :accepted-output-stage-comparison accepted-comparison
                   :rejected-diagnostic-stage-comparison rejected-comparison
                   :reproducible-rebuild-artifact rebuild-artifact
                   :stage-equivalence-matrix matrix}
        diagnostics
        (p15-s23-stage-comparison-proof-diagnostics source-path
                                                    candidate)
        _ (when (seq diagnostics)
            (p15-s23-stage-comparison-fail!
             (:diagnostic (first diagnostics)) source-path candidate
             {:diagnostics diagnostics}))
        proof-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:source-path source-path
                       :proof-contract proof-contract
                       :pipeline-artifact (:artifact-id pipeline-artifact)
                       :accepted-artifact (:artifact-id accepted-artifact)
                       :rejected-artifact (:artifact-id rejected-artifact)
                       :rebuild-artifact (:artifact-id rebuild-artifact)
                       :accepted-stdout
                       (:candidate-stdout accepted-comparison)
                       :rejected-diagnostics
                       (:candidate-diagnostics rejected-comparison)
                       :matrix (:rows matrix)})))
        rejected-records
        (p15-s23-stage-comparison-rejected-records
         source-path candidate)
        artifact-base
        {:kind :gravity/p15-s23-stage-comparison-report-artifact
         :phase "15"
         :task "P15-S23"
         :stage :p15-s23-stage-comparison-report
         :source-path source-path
         :proof-id proof-id
         :proof-contract proof-contract
         :compiler-pipeline-manifest-artifact
         (select-keys pipeline-artifact
                      [:kind :artifact-id :manifest-id
                       :capability-based-proof])
         :accepted-app-execution-artifact
         (select-keys accepted-artifact
                      [:kind :artifact-id :proof-id
                       :accepted-output-comparison
                       :compiled-plan-execution-trace
                       :trusted-boundary-record
                       :capability-based-proof])
         :rejected-app-diagnostic-artifact
         (select-keys rejected-artifact
                      [:kind :artifact-id :proof-id
                       :diagnostic-preservation-record
                       :rejected-app-diagnostic-records
                       :trusted-boundary-record
                       :capability-based-proof])
         :reproducible-rebuild-artifact
         (select-keys rebuild-artifact
                      [:kind :artifact-id :proof-id
                       :artifact-identity-comparison
                       :environment-provenance-record
                       :capability-based-proof])
         :stage-boundary-record boundary
         :accepted-output-stage-comparison accepted-comparison
         :rejected-diagnostic-stage-comparison rejected-comparison
         :stage-equivalence-matrix matrix
         :full-language-compiler-self-hosted?
         (get-in proof-contract
                 [:self-hosting-claims
                  :full-language-compiler-self-hosted?])
         :clojure-seed-retired?
         (get-in proof-contract
                 [:self-hosting-claims :clojure-seed-retired?])
         :accepted-p15-s23-stage-comparison-fixtures
         [{:fixture source-path
           :status :accepted
           :stage-comparison-row-count (count (:rows matrix))
           :current-candidate-equivalent-to-seed?
           (:current-candidate-equivalent-to-seed? matrix)
           :full-self-hosted-equivalence? false}]
         :rejected-p15-s23-stage-comparison-fixtures
         rejected-records
         :p15-s23-stage-comparison-diagnostic-stream
         (p15-s23-stage-comparison-diagnostic-stream source-path
                                                     proof-id)
         :p15-s23-stage-comparison-results
         {:accepted-fixtures 1
          :rejected-fixtures (count rejected-records)
          :diagnostic-count
          (count p15-s23-stage-comparison-diagnostic-ids)
          :stage-comparison-row-count (count (:rows matrix))
          :current-candidate-equivalent-to-seed?
          (:current-candidate-equivalent-to-seed? matrix)
          :full-self-hosted-equivalence? false
          :status :in-progress}
         :diagnostics []}
        proof (p15-s23-stage-comparison-report-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id
           (c4-artifact-id
            (assoc artifact-base :capability-based-proof proof)))))))

(defn p15-s23-stage-comparison-report-file-artifact
  [path]
  (when-not (.isFile (java.io.File. path))
    (p15-s23-stage-comparison-fail!
     "P15S23G001" path nil {:missing-fields [:compiler-source]}))
  (p15-s23-with-artifact-build-context
   #(p15-s23-stage-comparison-report-source-artifact path)))

(def p15-s23-self-hosting-conformance-scope
  [:phase14-hosted-core-compiled-conformance
   :test13-self-hosting-validation
   :p15-s23-stage-comparison])

(def p15-s23-self-hosting-conformance-required-preserves
  #{:artifact-provenance :conformance-suite-status
    :diagnostic-codes :source-spans
    :stage-support-matrix :stage-equivalence-record})

(def p15-s23-self-hosting-conformance-diagnostic-messages
  {"P15S23H001" "P15-S23 self-hosting conformance report is missing"
   "P15S23H002" "P15-S23 conformance suite scope or stage support record is incomplete"
   "P15S23H003" "P15-S23 linked Phase 14 conformance evidence is incomplete"
   "P15S23H004" "P15-S23 stage comparison evidence is missing or not equivalent"
   "P15S23H005" "P15-S23 diagnostic conformance regressed"
   "P15S23H006" "P15-S23 conformance report makes an unsupported self-hosting or seed-retirement claim"})

(def p15-s23-self-hosting-conformance-diagnostic-ids
  ["P15S23H001" "P15S23H002" "P15S23H003"
   "P15S23H004" "P15S23H005" "P15S23H006"])

(defn p15-s23-self-hosting-conformance-fail!
  [id source-path value data]
  (fail! id
         (get p15-s23-self-hosting-conformance-diagnostic-messages
              id
              "P15-S23 self-hosting conformance proof failed")
         (merge {:source-span {:source source-path}
                 :stage :p15-s23-self-hosting-conformance-report
                 :diagnostic-family
                 :p15-s23-self-hosting-conformance-report
                 :value value
                 :remediation "Link the current P15-S23 stage comparison with the Phase 14 conformance metadata and TEST13 self-hosting validation record, preserve stable diagnostics and source spans, and keep full self-hosting claims false until the full evidence bundle exists."}
                data)))