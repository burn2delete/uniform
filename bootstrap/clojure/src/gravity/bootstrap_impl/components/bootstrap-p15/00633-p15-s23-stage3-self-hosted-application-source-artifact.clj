

(defn p15-s23-stage3-self-hosted-application-source-artifact*
  [source-path]
  (let [source-data (p15-s23-compiler-source-form-record source-path)
        proof-contract
        (p15-s23-compiler-def-value
         source-path
         (:forms source-data)
         'p15-s23-stage3-self-hosted-application-execution)
        equivalence-artifact
        (p15-s23-current-candidate-artifact-evidence
         :stage3-equivalence-bundle)
        candidate-artifact
        (p15-s23-current-candidate-artifact-evidence
         :stage3-seedless-compiler-candidate)
        driver-artifact
        (p15-s23-current-candidate-artifact-evidence
         :stage2-compiler-driver)
        runtime-artifact
        (p15-s23-current-candidate-artifact-evidence
         :stage2-runtime-kernel)
        accepted-artifact
        (p15-s23-current-candidate-artifact-evidence
         :accepted-app-execution-proof)
        rejected-artifact
        (p15-s23-current-candidate-artifact-evidence
         :rejected-app-diagnostic-proof)
        execution-record
        (p15-s23-stage3-self-hosted-application-record
         source-path proof-contract)
        equivalence-record
        {:artifact :gravity/p15-s23-stage3-self-hosted-application-equivalence-record
         :equivalence-artifact-id (:artifact-id equivalence-artifact)
         :equivalence-bundle-complete?
         (true? (get-in equivalence-artifact
                        [:capability-based-proof
                         :stage3-equivalence-bundle-present?]))
         :accepted-output-equivalent?
         (true? (get-in equivalence-artifact
                        [:capability-based-proof
                         :accepted-output-equivalent?]))
         :rejected-diagnostics-equivalent?
         (true? (get-in equivalence-artifact
                        [:capability-based-proof
                         :rejected-diagnostics-equivalent?]))
         :status
         (if (and (true? (get-in equivalence-artifact
                                  [:capability-based-proof
                                   :stage3-equivalence-bundle-present?]))
                  (true? (get-in equivalence-artifact
                                  [:capability-based-proof
                                   :accepted-output-equivalent?]))
                  (true? (get-in equivalence-artifact
                                  [:capability-based-proof
                                   :rejected-diagnostics-equivalent?])))
           :complete
           :failed)}
        accepted-run-record
        (p15-s23-stage3-self-hosted-application-run-record
         proof-contract equivalence-artifact candidate-artifact
         driver-artifact runtime-artifact)
        rejected-record
        (p15-s23-stage3-self-hosted-application-rejected-record
         equivalence-artifact rejected-artifact)
        toolchain-record
        (p15-s23-stage3-self-hosted-application-toolchain-record
         proof-contract equivalence-artifact candidate-artifact
         driver-artifact runtime-artifact)
        runtime-record
        (p15-s23-stage3-self-hosted-application-runtime-record
         runtime-artifact accepted-artifact)
        evidence-link-record
        (p15-s23-stage3-self-hosted-application-link-record
         equivalence-artifact candidate-artifact driver-artifact
         runtime-artifact accepted-artifact rejected-artifact)
        boundary-record
        (p15-s23-stage3-self-hosted-application-boundary-record
         proof-contract accepted-run-record rejected-record
         toolchain-record runtime-record)
        candidate {:proof-contract proof-contract
                   :execution-record execution-record
                   :equivalence-record equivalence-record
                   :accepted-run-record accepted-run-record
                   :rejected-record rejected-record
                   :toolchain-record toolchain-record
                   :runtime-record runtime-record
                   :evidence-link-record evidence-link-record
                   :boundary-record boundary-record}
        diagnostics
        (p15-s23-stage3-self-hosted-application-proof-diagnostics
         source-path candidate)
        _ (when (seq diagnostics)
            (p15-s23-stage3-self-hosted-application-fail!
             (:diagnostic (first diagnostics)) source-path candidate
             {:diagnostics diagnostics}))
        proof-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:source-path source-path
                       :proof-contract proof-contract
                       :execution-record execution-record
                       :equivalence-record equivalence-record
                       :accepted-run-record accepted-run-record
                       :rejected-record rejected-record
                       :toolchain-record toolchain-record
                       :runtime-record runtime-record
                       :evidence-link-record evidence-link-record
                       :boundary-record boundary-record})))
        rejected-records
        (p15-s23-stage3-self-hosted-application-rejected-records
         source-path candidate)
        artifact-base
        {:kind
         :gravity/p15-s23-stage3-self-hosted-application-execution-artifact
         :phase "15"
         :task "P15-S23"
         :stage :p15-s23-stage3-self-hosted-application-execution
         :source-path source-path
         :proof-id proof-id
         :proof-contract proof-contract
         :stage3-equivalence-bundle-artifact
         (select-keys equivalence-artifact
                      [:kind :artifact-id :proof-id :accepted-record
                       :rejected-record :boundary-record
                       :capability-based-proof])
         :stage3-seedless-compiler-candidate-artifact
         (select-keys candidate-artifact
                      [:kind :artifact-id :proof-id :accepted-record
                       :rejected-record :boundary-record
                       :capability-based-proof])
         :stage2-compiler-driver-artifact
         (select-keys driver-artifact
                      [:kind :artifact-id :proof-id :accepted-record
                       :boundary-record :capability-based-proof])
         :stage2-runtime-kernel-artifact
         (select-keys runtime-artifact
                      [:kind :artifact-id :proof-id :accepted-record
                       :boundary-record :capability-based-proof])
         :accepted-app-execution-artifact
         (select-keys accepted-artifact
                      [:kind :artifact-id :proof-id
                       :accepted-output-comparison
                       :capability-based-proof])
         :rejected-app-diagnostic-artifact
         (select-keys rejected-artifact
                      [:kind :artifact-id :proof-id
                       :rejected-app-diagnostic-records
                       :capability-based-proof])
         :execution-record execution-record
         :equivalence-record equivalence-record
         :accepted-run-record accepted-run-record
         :rejected-record rejected-record
         :toolchain-record toolchain-record
         :runtime-record runtime-record
         :evidence-link-record evidence-link-record
         :boundary-record boundary-record
         :full-language-compiler-self-hosted?
         (get-in proof-contract
                 [:self-hosting-claims
                  :full-language-compiler-self-hosted?])
         :clojure-seed-retired?
         (get-in proof-contract
                 [:self-hosting-claims :clojure-seed-retired?])
         :accepted-p15-s23-stage3-self-hosted-application-fixtures
         [{:fixture p15-s23-accepted-app-source-path
           :status :accepted
           :stdout (:stdout accepted-run-record)
           :application-artifact-id
           (:application-artifact-id accepted-run-record)}]
         :rejected-p15-s23-stage3-self-hosted-application-fixtures
         rejected-records
         :p15-s23-stage3-self-hosted-application-diagnostic-stream
         (p15-s23-stage3-self-hosted-application-diagnostic-stream
          source-path proof-id)
         :p15-s23-stage3-self-hosted-application-results
         {:accepted-fixtures 1
          :rejected-fixtures (count rejected-records)
          :diagnostic-count
          (count p15-s23-stage3-self-hosted-application-diagnostic-ids)
          :accepted-app-output (:stdout accepted-run-record)
          :rejected-diagnostics (:stage3-diagnostics rejected-record)
          :stage3-self-hosted-application-run? true
          :full-language-compiler-self-hosted? false
          :clojure-seed-retired? false
          :status :complete}
         :diagnostics []}
        proof (p15-s23-stage3-self-hosted-application-proof
               artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id
           (c4-artifact-id
            (assoc artifact-base :capability-based-proof proof)))))

(defn p15-s23-stage3-self-hosted-application-source-artifact
  [source-path]
  (p15-s23-cached-source-artifact
   :p15-s23-stage3-self-hosted-application-execution
   source-path
   #(p15-s23-stage3-self-hosted-application-source-artifact*
     source-path)))

(defn p15-s23-stage3-self-hosted-application-file-artifact
  [path]
  (when-not (.isFile (java.io.File. path))
    (p15-s23-stage3-self-hosted-application-fail!
     "P15S23AC001" path nil {:missing-fields [:compiler-source]}))
  (p15-s23-stage3-self-hosted-application-source-artifact path))

(defn p15-s23-stage3-self-hosted-application-evidence
  []
  (let [source-path "bootstrap/gravity/p15_s23/compiler.gravity"]
    (when (.isFile (java.io.File. source-path))
      (try
        (let [artifact
              (p15-s23-stage3-self-hosted-application-file-artifact
               source-path)
              proof (:capability-based-proof artifact)]
          {:status :verified
           :artifact (:kind artifact)
           :artifact-id (:artifact-id artifact)
           :proof-id (:proof-id artifact)
           :source-path source-path
           :stage3-self-hosted-application-execution-present?
           (:stage3-self-hosted-application-execution-present? proof)
           :accepted-application-run? (:accepted-application-run? proof)
           :accepted-output-equivalent?
           (:accepted-output-equivalent? proof)
           :rejected-application-fails-closed?
           (:rejected-application-fails-closed? proof)
           :stage3-toolchain-seedless?
           (:stage3-toolchain-seedless? proof)
           :runtime-capability-recorded?
           (:runtime-capability-recorded? proof)
           :full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? artifact)
           :clojure-seed-retired?
           (:clojure-seed-retired? artifact)})
        (catch Exception _
          nil)))))