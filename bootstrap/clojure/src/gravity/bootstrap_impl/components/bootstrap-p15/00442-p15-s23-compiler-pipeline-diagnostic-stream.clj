

(defn p15-s23-compiler-pipeline-diagnostic-stream
  [source-path manifest-id]
  {:artifact :gravity/p15-s23-compiler-pipeline-manifest-diagnostic-stream
   :stage :p15-s23-compiler-pipeline-manifest
   :source-path source-path
   :manifest-id manifest-id
   :diagnostics
   (mapv (fn [id]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :p15-s23-compiler-pipeline-manifest
            :message
            (get p15-s23-compiler-pipeline-manifest-diagnostic-messages id)})
         p15-s23-compiler-pipeline-manifest-diagnostic-ids)
   :status :complete})

(defn p15-s23-compiler-pipeline-manifest-proof
  [artifact]
  (let [manifest (:compiler-pipeline-manifest artifact)
        rejected-diagnostics
        (set (mapcat #(map :diagnostic (:diagnostics %))
                     (:rejected-p15-s23-compiler-pipeline-fixtures
                      artifact)))
        diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:p15-s23-compiler-pipeline-manifest-diagnostic-stream
                           :diagnostics])))]
    {:compiler-pipeline-manifest-authored-in-gravity? true
     :status :in-progress
     :task "P15-S23"
     :compiler-source-inventory-linked?
     (= :gravity/p15-s23-compiler-source-inventory-artifact
        (get-in artifact [:compiler-source-inventory-artifact :kind]))
     :canonical-pipeline-covered?
     (= p15-s23-canonical-compiler-pipeline (:pipeline manifest))
     :pass-contracts-covered?
     (and (= p15-s23-canonical-compiler-pipeline
             (mapv :stage (:pass-contracts manifest)))
          (every? p15-s23-pass-contract-complete?
                  (:pass-contracts manifest)))
     :preservation-facts-covered?
     (set/subset?
      p15-s23-compiler-pipeline-manifest-required-preserves
      (set (:preserves manifest)))
     :does-not-claim-full-self-hosting?
     (false? (get-in manifest
                     [:self-hosting-claims
                      :full-language-compiler-self-hosted?]))
     :does-not-claim-clojure-seed-retirement?
     (false? (get-in manifest
                     [:self-hosting-claims :clojure-seed-retired?]))
     :rejected-candidates-covered?
     (set/subset? (set p15-s23-compiler-pipeline-manifest-diagnostic-ids)
                  rejected-diagnostics)
     :diagnostics-covered?
     (= (set p15-s23-compiler-pipeline-manifest-diagnostic-ids)
        diagnostics)
     :limitations
     {:full-language-compiler-self-hosted? false
      :clojure-seed-retired? false
      :emits-target-artifacts? false
      :runs-nontrivial-gravity-app? false
      :next-required-capability
      :implement_pipeline_stage_execution_from_manifest}}))

(defn p15-s23-compiler-pipeline-manifest-source-artifact
  [source-path]
  (p15-s23-context-artifact
   :compiler-pipeline-manifest source-path
   (fn [] (let [source-data (p15-s23-compiler-source-form-record source-path)
        manifest (p15-s23-compiler-def-value
                  source-path
                  (:forms source-data)
                  'p15-s23-compiler-pipeline-manifest)
        diagnostics
        (p15-s23-compiler-pipeline-manifest-diagnostics
         source-path manifest)
        _ (when (seq diagnostics)
            (p15-s23-compiler-pipeline-manifest-fail!
             (:diagnostic (first diagnostics)) source-path manifest
             {:diagnostics diagnostics}))
        compiler-source-inventory
        (p15-s23-compiler-source-inventory-source-artifact source-path)
        manifest-id
        (str "sha256:"
             (sha256-hex (pr-str {:source-path source-path
                                  :manifest manifest
                                  :inventory
                                  (:artifact-id
                                   compiler-source-inventory)})))
        rejected-records
        (p15-s23-compiler-pipeline-rejected-records source-path)
        artifact-base
        {:kind :gravity/p15-s23-compiler-pipeline-manifest-artifact
         :phase "15"
         :task "P15-S23"
         :stage :p15-s23-compiler-pipeline-manifest
         :source-path source-path
         :manifest-id manifest-id
         :compiler-source-inventory-artifact compiler-source-inventory
         :compiler-pipeline-manifest manifest
         :full-language-compiler-self-hosted?
         (get-in manifest
                 [:self-hosting-claims
                  :full-language-compiler-self-hosted?])
         :clojure-seed-retired?
         (get-in manifest
                 [:self-hosting-claims :clojure-seed-retired?])
         :accepted-p15-s23-compiler-pipeline-fixtures
         [{:fixture source-path
           :status :accepted
           :pipeline (:pipeline manifest)
           :pass-contract-count (count (:pass-contracts manifest))}]
         :rejected-p15-s23-compiler-pipeline-fixtures
         rejected-records
         :p15-s23-compiler-pipeline-manifest-diagnostic-stream
         (p15-s23-compiler-pipeline-diagnostic-stream source-path
                                                      manifest-id)
         :p15-s23-compiler-pipeline-manifest-results
         {:accepted-fixtures 1
          :rejected-fixtures (count rejected-records)
          :diagnostic-count
          (count p15-s23-compiler-pipeline-manifest-diagnostic-ids)
          :pass-contract-count (count (:pass-contracts manifest))
          :status :in-progress}
         :diagnostics []}
        proof (p15-s23-compiler-pipeline-manifest-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id
           (c4-artifact-id
            (assoc artifact-base :capability-based-proof proof)))))))

(defn p15-s23-compiler-pipeline-manifest-file-artifact
  [path]
  (when-not (.isFile (java.io.File. path))
    (p15-s23-compiler-pipeline-manifest-fail!
     "P15S23M001" path nil {:missing-fields [:compiler-source]}))
  (p15-s23-with-artifact-build-context
   #(p15-s23-compiler-pipeline-manifest-source-artifact path)))