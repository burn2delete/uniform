

(defn p15-s23-stage2-compiler-nucleus-source-artifact
  [source-path]
  (let [source-data (p15-s23-compiler-source-form-record source-path)
        nucleus
        (p15-s23-compiler-def-value source-path
                                     (:forms source-data)
                                     'p15-s23-stage2-compiler-nucleus)
        compiled-app-artifact
        (hosted-core-compiled-app-proof-file-artifact
         p15-s23-accepted-app-source-path)
        pipeline-artifact
        (p15-s23-compiler-pipeline-manifest-source-artifact
         source-path)
        accepted-artifact
        (p15-s23-accepted-app-execution-source-artifact
         source-path)
        rejected-artifact
        (p15-s23-rejected-app-diagnostic-source-artifact
         source-path)
        accepted-plan-record
        (p15-s23-stage2-compiler-nucleus-accepted-plan-record
         nucleus compiled-app-artifact)
        rejected-diagnostic-record
        (p15-s23-stage2-compiler-nucleus-rejected-diagnostic-record
         nucleus rejected-artifact)
        evidence-link-record
        (p15-s23-stage2-compiler-nucleus-evidence-link-record
         nucleus pipeline-artifact accepted-artifact rejected-artifact)
        boundary-record
        (p15-s23-stage2-compiler-nucleus-boundary-record nucleus)
        candidate {:nucleus-contract nucleus
                   :accepted-plan-record accepted-plan-record
                   :rejected-diagnostic-record rejected-diagnostic-record
                   :evidence-link-record evidence-link-record
                   :boundary-record boundary-record}
        diagnostics
        (p15-s23-stage2-compiler-nucleus-diagnostics source-path
                                                     candidate)
        _ (when (seq diagnostics)
            (p15-s23-stage2-compiler-nucleus-fail!
             (:diagnostic (first diagnostics)) source-path candidate
             {:diagnostics diagnostics}))
        proof-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:source-path source-path
                       :nucleus nucleus
                       :compiled-plan-id
                       (:compiled-plan-id accepted-plan-record)
                       :accepted-artifact-id
                       (:artifact-id accepted-artifact)
                       :rejected-artifact-id
                       (:artifact-id rejected-artifact)})))
        rejected-records
        (p15-s23-stage2-compiler-nucleus-rejected-records
         source-path candidate)
        artifact-base
        {:kind :gravity/p15-s23-stage2-compiler-nucleus-artifact
         :phase "15"
         :task "P15-S23"
         :stage :p15-s23-stage2-compiler-nucleus
         :source-path source-path
         :proof-id proof-id
         :nucleus-contract nucleus
         :compiled-app-artifact
         (select-keys compiled-app-artifact
                      [:kind :artifact-id :source :module :compiled-plan
                       :runtime-surface :accepted-run :reference-run
                       :trusted-boundary :capability-based-proof])
         :compiler-pipeline-manifest-artifact
         (select-keys pipeline-artifact
                      [:kind :artifact-id :proof-id
                       :pipeline-manifest
                       :capability-based-proof])
         :accepted-app-execution-artifact
         (select-keys accepted-artifact
                      [:kind :artifact-id :proof-id
                       :accepted-app-path
                       :accepted-output-comparison
                       :compiled-plan-execution-trace
                       :capability-based-proof])
         :rejected-app-diagnostic-artifact
         (select-keys rejected-artifact
                      [:kind :artifact-id :proof-id
                       :verified-p15-s23-rejected-app-fixtures
                       :p15-s23-rejected-app-diagnostic-results
                       :capability-based-proof])
         :accepted-plan-record accepted-plan-record
         :rejected-diagnostic-record rejected-diagnostic-record
         :evidence-link-record evidence-link-record
         :boundary-record boundary-record
         :full-language-compiler-self-hosted?
         (get-in nucleus
                 [:self-hosting-claims
                  :full-language-compiler-self-hosted?])
         :clojure-seed-retired?
         (get-in nucleus [:self-hosting-claims
                          :clojure-seed-retired?])
         :accepted-p15-s23-stage2-compiler-nucleus-fixtures
         [{:fixture p15-s23-accepted-app-source-path
           :status :accepted
           :compiled-plan-id (:compiled-plan-id accepted-plan-record)
           :stdout (:stdout accepted-plan-record)
           :stage2-contract :p15-s23-stage2-compiler-nucleus}]
         :rejected-p15-s23-stage2-compiler-nucleus-fixtures
         rejected-records
         :p15-s23-stage2-compiler-nucleus-diagnostic-stream
         (p15-s23-stage2-compiler-nucleus-diagnostic-stream
          source-path proof-id)
         :p15-s23-stage2-compiler-nucleus-results
         {:accepted-fixtures 1
          :rejected-fixtures (count rejected-records)
          :diagnostic-count
          (count p15-s23-stage2-compiler-nucleus-diagnostic-ids)
          :compiled-plan-id (:compiled-plan-id accepted-plan-record)
          :accepted-output (:stdout accepted-plan-record)
          :rejected-diagnostics
          (:observed-diagnostics rejected-diagnostic-record)
          :status :in-progress}
         :diagnostics []}
        proof (p15-s23-stage2-compiler-nucleus-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id
           (c4-artifact-id
            (assoc artifact-base :capability-based-proof proof)))))

(defn p15-s23-stage2-compiler-nucleus-file-artifact
  [path]
  (when-not (.isFile (java.io.File. path))
    (p15-s23-stage2-compiler-nucleus-fail!
     "P15S23N001" path nil {:missing-fields [:compiler-source]}))
  (p15-s23-stage2-compiler-nucleus-source-artifact path))

(defn p15-s23-stage2-compiler-nucleus-evidence
  []
  (let [source-path "bootstrap/gravity/p15_s23/compiler.gravity"]
    (when (.isFile (java.io.File. source-path))
      (try
        (let [artifact
              (p15-s23-stage2-compiler-nucleus-file-artifact
               source-path)
              proof (:capability-based-proof artifact)]
          {:status :verified
           :artifact (:kind artifact)
           :artifact-id (:artifact-id artifact)
           :proof-id (:proof-id artifact)
           :source-path source-path
           :compiled-plan-id
           (get-in artifact [:accepted-plan-record
                             :compiled-plan-id])
           :accepted-output-equivalent?
           (:accepted-app-output-equivalent? proof)
           :rejected-diagnostics-equivalent?
           (:rejected-diagnostics-equivalent? proof)
           :residual-clojure-boundary-recorded?
           (:residual-clojure-boundary-recorded? proof)
           :full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? artifact)
           :clojure-seed-retired? (:clojure-seed-retired? artifact)})
        (catch Exception _
          nil)))))