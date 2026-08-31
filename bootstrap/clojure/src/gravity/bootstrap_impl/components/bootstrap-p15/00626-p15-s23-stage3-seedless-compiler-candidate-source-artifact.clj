

(defn p15-s23-stage3-seedless-compiler-candidate-source-artifact*
  [source-path]
  (let [source-data (p15-s23-compiler-source-form-record source-path)
        proof-contract
        (p15-s23-compiler-def-value
         source-path
         (:forms source-data)
         'p15-s23-stage3-seedless-compiler-candidate)
        inventory-artifact
        (p15-s23-current-candidate-artifact-evidence
         :compiler-source-inventory)
        stage2-whole-artifact
        (p15-s23-current-candidate-artifact-evidence
         :stage2-whole-language-compiler)
        whole-compiler-artifact
        (p15-s23-current-candidate-artifact-evidence
         :whole-language-compiler-artifact)
        driver-artifact
        (p15-s23-current-candidate-artifact-evidence
         :stage2-compiler-driver)
        source-front-end-artifact
        (p15-s23-current-candidate-artifact-evidence
         :stage2-source-front-end)
        front-end-executor-artifact
        (p15-s23-current-candidate-artifact-evidence
         :stage2-front-end-executor)
        plan-emitter-artifact
        (p15-s23-current-candidate-artifact-evidence
         :stage2-plan-emitter)
        runtime-executor-artifact
        (p15-s23-current-candidate-artifact-evidence
         :stage2-runtime-executor)
        runtime-kernel-artifact
        (p15-s23-current-candidate-artifact-evidence
         :stage2-runtime-kernel)
        accepted-artifact
        (p15-s23-current-candidate-artifact-evidence
         :accepted-app-execution-proof)
        rejected-artifact
        (p15-s23-current-candidate-artifact-evidence
         :rejected-app-diagnostic-proof)
        provenance-artifact
        (p15-s23-current-candidate-artifact-evidence
         :provenance-attestation)
        source-record
        (p15-s23-stage3-seedless-compiler-candidate-source-record
         source-path proof-contract inventory-artifact)
        candidate-record
        (p15-s23-stage3-seedless-compiler-candidate-record
         source-path proof-contract source-record)
        evidence-link-record
        (p15-s23-stage3-seedless-compiler-candidate-evidence-link-record
         stage2-whole-artifact whole-compiler-artifact driver-artifact
         source-front-end-artifact front-end-executor-artifact
         plan-emitter-artifact runtime-executor-artifact
         runtime-kernel-artifact accepted-artifact rejected-artifact)
        accepted-record
        (p15-s23-stage3-seedless-compiler-candidate-accepted-record
         source-record candidate-record stage2-whole-artifact
         driver-artifact whole-compiler-artifact)
        rejected-record
        (p15-s23-stage3-seedless-compiler-candidate-rejected-record
         stage2-whole-artifact driver-artifact whole-compiler-artifact)
        boundary-record
        (p15-s23-stage3-seedless-compiler-candidate-boundary-record
         proof-contract driver-artifact stage2-whole-artifact)
        lineage-record
        (p15-s23-stage3-seedless-compiler-candidate-lineage-record
         source-path proof-contract stage2-whole-artifact
         provenance-artifact)
        candidate {:proof-contract proof-contract
                   :source-record source-record
                   :candidate-record candidate-record
                   :accepted-record accepted-record
                   :rejected-record rejected-record
                   :boundary-record boundary-record
                   :evidence-link-record evidence-link-record
                   :lineage-record lineage-record}
        diagnostics
        (p15-s23-stage3-seedless-compiler-candidate-proof-diagnostics
         source-path candidate)
        _ (when (seq diagnostics)
            (p15-s23-stage3-seedless-compiler-candidate-fail!
             (:diagnostic (first diagnostics)) source-path candidate
             {:diagnostics diagnostics}))
        proof-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:source-path source-path
                       :proof-contract proof-contract
                       :source-record source-record
                       :candidate-record candidate-record
                       :accepted-record accepted-record
                       :rejected-record rejected-record
                       :boundary-record boundary-record
                       :evidence-link-record evidence-link-record
                       :lineage-record lineage-record})))
        rejected-records
        (p15-s23-stage3-seedless-compiler-candidate-rejected-records
         source-path candidate)
        artifact-base
        {:kind
         :gravity/p15-s23-stage3-seedless-compiler-candidate-artifact
         :phase "15"
         :task "P15-S23"
         :stage :p15-s23-stage3-seedless-compiler-candidate
         :source-path source-path
         :proof-id proof-id
         :proof-contract proof-contract
         :compiler-source-inventory-artifact
         (select-keys inventory-artifact
                      [:kind :artifact-id :inventory-id
                       :source-inventory :capability-based-proof])
         :stage2-whole-language-compiler-artifact
         (select-keys stage2-whole-artifact
                      [:kind :artifact-id :proof-id :accepted-record
                       :rejected-record :boundary-record
                       :capability-based-proof])
         :whole-language-compiler-artifact
         (select-keys whole-compiler-artifact
                      [:kind :artifact-id :proof-id
                       :compiler-artifact-manifest
                       :accepted-application-compile-record
                       :rejected-application-diagnostic-record
                       :capability-based-proof])
         :stage2-compiler-driver-artifact
         (select-keys driver-artifact
                      [:kind :artifact-id :proof-id :accepted-record
                       :rejected-record :boundary-record
                       :capability-based-proof])
         :stage2-source-front-end-artifact
         (select-keys source-front-end-artifact
                      [:kind :artifact-id :proof-id
                       :rule-record :accepted-record
                       :boundary-record :capability-based-proof])
         :stage2-front-end-executor-artifact
         (select-keys front-end-executor-artifact
                      [:kind :artifact-id :proof-id
                       :rule-record :accepted-record
                       :boundary-record :capability-based-proof])
         :stage2-plan-emitter-artifact
         (select-keys plan-emitter-artifact
                      [:kind :artifact-id :proof-id
                       :rule-record :accepted-record
                       :boundary-record :capability-based-proof])
         :stage2-runtime-executor-artifact
         (select-keys runtime-executor-artifact
                      [:kind :artifact-id :proof-id
                       :rule-record :accepted-record
                       :boundary-record :capability-based-proof])
         :stage2-runtime-kernel-artifact
         (select-keys runtime-kernel-artifact
                      [:kind :artifact-id :proof-id
                       :rule-record :accepted-record
                       :boundary-record :capability-based-proof])
         :accepted-app-execution-artifact
         (select-keys accepted-artifact
                      [:kind :artifact-id :proof-id
                       :accepted-output-comparison
                       :compiled-plan-execution-trace
                       :capability-based-proof])
         :rejected-app-diagnostic-artifact
         (select-keys rejected-artifact
                      [:kind :artifact-id :proof-id
                       :rejected-app-diagnostic-records
                       :capability-based-proof])
         :source-record source-record
         :candidate-record candidate-record
         :accepted-record accepted-record
         :rejected-record rejected-record
         :boundary-record boundary-record
         :evidence-link-record evidence-link-record
         :lineage-record lineage-record
         :seedless-compiler-candidate?
         (get-in proof-contract
                 [:self-hosting-claims
                  :seedless-compiler-candidate?])
         :full-language-compiler-self-hosted?
         (get-in proof-contract
                 [:self-hosting-claims
                  :full-language-compiler-self-hosted?])
         :clojure-seed-retired?
         (get-in proof-contract
                 [:self-hosting-claims :clojure-seed-retired?])
         :accepted-p15-s23-stage3-seedless-compiler-candidate-fixtures
         [{:fixture source-path
           :status :accepted
           :source-components (:observed-components source-record)}
          {:fixture p15-s23-accepted-app-source-path
           :status :accepted
           :stdout (:seedless-candidate-output accepted-record)
           :stage2-plan-id (:stage2-plan-id accepted-record)}]
         :rejected-p15-s23-stage3-seedless-compiler-candidate-fixtures
         rejected-records
         :p15-s23-stage3-seedless-compiler-candidate-diagnostic-stream
         (p15-s23-stage3-seedless-compiler-candidate-diagnostic-stream
          source-path proof-id)
         :p15-s23-stage3-seedless-compiler-candidate-results
         {:accepted-fixtures 2
          :rejected-fixtures (count rejected-records)
          :diagnostic-count
          (count p15-s23-stage3-seedless-compiler-candidate-diagnostic-ids)
          :source-component-count
          (count (:observed-components source-record))
          :accepted-app-output
          (:seedless-candidate-output accepted-record)
          :rejected-diagnostics
          (:seedless-candidate-diagnostics rejected-record)
          :compiler-path-seedless?
          (:compiler-path-seedless? candidate-record)
          :clojure-stage0-verifier?
          (:clojure-stage0-verifier? boundary-record)
          :clojure-stage0-release-compiler?
          (:clojure-stage0-release-compiler? boundary-record)
          :full-language-compiler-self-hosted? false
          :clojure-seed-retired? false
          :status :candidate}
         :diagnostics []}
        proof (p15-s23-stage3-seedless-compiler-candidate-proof
               artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id
           (c4-artifact-id
            (assoc artifact-base :capability-based-proof proof)))))

(defn p15-s23-stage3-seedless-compiler-candidate-source-artifact
  [source-path]
  (p15-s23-cached-source-artifact
   :p15-s23-stage3-seedless-compiler-candidate
   source-path
   #(p15-s23-stage3-seedless-compiler-candidate-source-artifact*
     source-path)))

(defn p15-s23-stage3-seedless-compiler-candidate-file-artifact
  [path]
  (when-not (.isFile (java.io.File. path))
    (p15-s23-stage3-seedless-compiler-candidate-fail!
     "P15S23AA001" path nil {:missing-fields [:compiler-source]}))
  (p15-s23-stage3-seedless-compiler-candidate-source-artifact path))