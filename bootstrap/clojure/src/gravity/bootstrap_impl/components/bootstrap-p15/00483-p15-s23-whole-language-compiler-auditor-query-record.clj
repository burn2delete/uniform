

(defn p15-s23-whole-language-compiler-auditor-query-record
  [manifest stage-support accepted-record rejected-record boundary-record
   lineage-record link-table]
  {:artifact :gravity/p15-s23-whole-language-compiler-auditor-query-record
   :queries
   [{:query :which_artifact_is_the_current_compiler
     :answer (:compiler-artifact-id manifest)
     :status :answered}
    {:query :which_language_subset_is_claimed
     :answer (:claimed-subset stage-support)
     :status :answered}
    {:query :did_the_accepted_application_run
     :answer (:output-matches? accepted-record)
     :status :answered}
    {:query :did_rejected_applications_fail_closed
     :answer (:diagnostic-codes-stable? rejected-record)
     :status :answered}
    {:query :which_seed_boundary_remains
     :answer (:residual-tcb-boundaries boundary-record)
     :status :answered}
    {:query :which_compiler_compiled_this_compiler
     :answer (:compiled-by lineage-record)
     :status :answered}
    {:query :which_evidence_links_support_this_artifact
     :answer (mapv :link (:links link-table))
     :status :answered}]
   :compiler-artifact-identifiable?
   (boolean (re-find #"^sha256:"
                     (str (:compiler-artifact-id manifest))))
   :accepted-application-proven? (:output-matches? accepted-record)
   :rejected-diagnostics-proven? (:diagnostic-codes-stable? rejected-record)
   :residual-boundary-explicit? (= :complete (:status boundary-record))
   :lineage-traversable? (:lineage-traversable-to-seed? lineage-record)
   :required-links-covered? (:required-links-covered? link-table)
   :status :complete})

(defn p15-s23-whole-language-compiler-manifest
  [source-path proof-contract inventory-artifact pipeline-artifact
   stage-support accepted-record rejected-record boundary-record
   lineage-record link-table]
  (let [manifest-base
        {:artifact :gravity/p15-s23-whole-language-compiler-artifact-manifest
         :source-path source-path
         :bootstrap-stage :p15-s23
         :compiler-source-language :gravity
         :compiler-source-inventory-id (:inventory-id inventory-artifact)
         :pipeline-manifest-id (:manifest-id pipeline-artifact)
         :stage-support-id (c4-artifact-id stage-support)
         :accepted-application-compile-record-id
         (c4-artifact-id accepted-record)
         :rejected-application-diagnostic-record-id
         (c4-artifact-id rejected-record)
         :residual-trusted-boundary-record-id
         (c4-artifact-id boundary-record)
         :compiler-lineage-record-id (c4-artifact-id lineage-record)
         :evidence-link-table-id (c4-artifact-id link-table)
         :claimed-subset
         (get-in proof-contract [:claimed-language-subset :scope])
         :canonical-stage-count (:canonical-stage-count stage-support)
         :accepted-app-output (:stdout accepted-record)
         :rejected-diagnostics (:diagnostics rejected-record)
         :required-links-covered?
         (:required-links-covered? link-table)
         :full-language-compiler-self-hosted? false
         :clojure-seed-retired? false
         :release-eligible? false
         :status :complete}
        compiler-artifact-id (c4-artifact-id manifest-base)]
    (assoc manifest-base :compiler-artifact-id compiler-artifact-id)))

(defn p15-s23-whole-language-compiler-proof-diagnostics
  [source-path candidate]
  (let [proof-contract (:proof-contract candidate)
        stage-support (:stage-support-matrix candidate)
        manifest (:compiler-artifact-manifest candidate)
        link-table (:compiler-evidence-link-table candidate)
        accepted-record (:accepted-application-compile-record candidate)
        rejected-record (:rejected-application-diagnostic-record candidate)
        boundary-record (:residual-trusted-boundary-record candidate)
        lineage-record (:compiler-artifact-lineage-record candidate)
        rebuild-artifact (:reproducible-rebuild-log-artifact candidate)
        stage-comparison-artifact (:stage-comparison-report-artifact candidate)
        conformance-artifact (:self-hosting-conformance-report-artifact candidate)
        provenance-artifact (:bootstrap-provenance-attestation-artifact candidate)
        claims (:self-hosting-claims proof-contract)
        preserves (set (:preserves proof-contract))
        missing-preserves
        (set/difference p15-s23-whole-language-compiler-required-preserves
                        preserves)]
    (vec
     (concat
      (when-not (= :gravity/whole-language-compiler-artifact
                   (:artifact proof-contract))
        [(p15-s23-whole-language-compiler-diagnostic-record
          source-path "P15S23W001" proof-contract
          {:missing-fields [:artifact]})])
      (when (or (seq missing-preserves)
                (not= :complete (:status link-table))
                (not (:required-links-covered? link-table))
                (not= :complete (:status manifest)))
        [(p15-s23-whole-language-compiler-diagnostic-record
          source-path "P15S23W002"
          {:manifest manifest :link-table link-table}
          {:missing-preserves (vec (sort missing-preserves))
           :required-links
           (vec (sort p15-s23-whole-language-compiler-required-links))})])
      (when-not
       (and (= :complete (:status stage-support))
            (= :complete (:status accepted-record))
            (true? (:output-matches? accepted-record))
            (true? (:compiled-plan-emitted? accepted-record))
            (true? (:compiled-plan-executed? accepted-record)))
        [(p15-s23-whole-language-compiler-diagnostic-record
          source-path "P15S23W003"
          {:stage-support stage-support
           :accepted-application-compile-record accepted-record}
          {:required [:complete-stage-support
                      :compiled-plan-emitted
                      :compiled-plan-executed
                      :accepted-output-matches-reference]})])
      (when-not
       (and (= :complete (:status lineage-record))
            (true?
             (get-in rebuild-artifact
                     [:artifact-identity-comparison
                      :all-artifact-identities-match?]))
            (true?
             (get-in stage-comparison-artifact
                     [:capability-based-proof
                      :current-candidate-equivalent-to-seed?]))
            (true?
             (get-in conformance-artifact
                     [:stage-support-conformance-record
                      :stage-support-conformant?]))
            (true?
             (get-in provenance-artifact
                     [:compiler-lineage-graph
                      :lineage-traversable-to-seed?])))
        [(p15-s23-whole-language-compiler-diagnostic-record
          source-path "P15S23W004"
          {:lineage-record lineage-record
           :rebuild-artifact (:artifact-id rebuild-artifact)
           :stage-comparison-artifact (:artifact-id stage-comparison-artifact)
           :conformance-artifact (:artifact-id conformance-artifact)
           :provenance-artifact (:artifact-id provenance-artifact)}
          {:required [:reproducible-artifact-identities
                      :stage-equivalence
                      :stage-support-conformance
                      :lineage-traversable-to-seed]})])
      (when (or (true? (:full-language-compiler-self-hosted? claims))
                (true? (:clojure-seed-retired? claims))
                (true? (:full-language-compiler-self-hosted? manifest))
                (true? (:clojure-seed-retired? manifest))
                (not= :complete (:status boundary-record))
                (false? (:clojure-stage0-still-required?
                         boundary-record)))
        [(p15-s23-whole-language-compiler-diagnostic-record
          source-path "P15S23W005"
          {:claims claims
           :manifest manifest
           :boundary-record boundary-record}
          {:full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? claims)
           :clojure-seed-retired?
           (:clojure-seed-retired? claims)
           :residual-boundary-status (:status boundary-record)})])
      (when-not
       (and (= :complete (:status rejected-record))
            (true? (:all-fixtures-rejected? rejected-record))
            (true? (:diagnostics-match-expected? rejected-record))
            (true? (:diagnostic-codes-stable? rejected-record)))
        [(p15-s23-whole-language-compiler-diagnostic-record
          source-path "P15S23W006" rejected-record
          {:required [:all-rejected-fixtures-fail-closed
                      :diagnostics-match-expected
                      :diagnostic-codes-stable]})])))))

(defn p15-s23-whole-language-compiler-diagnostic-stream
  [source-path proof-id]
  {:artifact :gravity/p15-s23-whole-language-compiler-diagnostic-stream
   :stage :p15-s23-whole-language-compiler-artifact
   :source-path source-path
   :proof-id proof-id
   :diagnostics
   (mapv (fn [id]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :p15-s23-whole-language-compiler-artifact
            :message
            (get p15-s23-whole-language-compiler-diagnostic-messages id)})
         p15-s23-whole-language-compiler-diagnostic-ids)
   :status :complete})