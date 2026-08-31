

(defn p15-s23-current-candidate-artifact
  [key source-path]
  (case key
    :compiler-source-inventory
    (p15-s23-compiler-source-inventory-file-artifact source-path)
    :compiler-pipeline-manifest
    (p15-s23-compiler-pipeline-manifest-file-artifact source-path)
    :source-unit-and-syntax-serialization-proof
    (p15-s23-source-syntax-serialization-proof-file-artifact source-path)
    :core-lowering-and-diagnostic-preservation-report
    (p15-s23-core-lowering-diagnostic-preservation-file-artifact source-path)
    :runtime-manifest-and-capability-enforcement-report
    (p15-s23-runtime-manifest-capability-enforcement-file-artifact source-path)
    :accepted-app-execution-proof
    (p15-s23-accepted-app-execution-file-artifact source-path)
    :rejected-app-diagnostic-proof
    (p15-s23-rejected-app-diagnostic-file-artifact source-path)
    :reproducible-rebuild-log
    (p15-s23-reproducible-rebuild-log-file-artifact source-path)
    :stage-comparison-report
    (p15-s23-stage-comparison-report-file-artifact source-path)
    :conformance-report
    (p15-s23-self-hosting-conformance-report-file-artifact source-path)
    :provenance-attestation
    (p15-s23-provenance-attestation-file-artifact source-path)
    :tcb-delta-record
    (p15-s23-tcb-delta-record-file-artifact source-path)
    :unsafe-audit-report
    (p15-s23-unsafe-audit-report-file-artifact source-path)
    :whole-language-compiler-artifact
    (p15-s23-whole-language-compiler-artifact-file-artifact source-path)
    :governance-and-package-release-record
    (p15-s23-governance-and-package-release-record-file-artifact source-path)
    :stage2-compiler-nucleus
    (p15-s23-stage2-compiler-nucleus-file-artifact source-path)
    :stage2-plan-emitter
    (p15-s23-stage2-plan-emitter-file-artifact source-path)
    :stage2-runtime-kernel
    (p15-s23-stage2-runtime-kernel-file-artifact source-path)
    :stage2-runtime-executor
    (p15-s23-stage2-runtime-executor-file-artifact source-path)
    :stage2-front-end-executor
    (p15-s23-stage2-front-end-executor-file-artifact source-path)
    :stage2-source-front-end
    (p15-s23-stage2-source-front-end-file-artifact source-path)
    :stage2-compiler-driver
    (p15-s23-stage2-compiler-driver-file-artifact source-path)
    :stage2-whole-language-compiler
    (p15-s23-stage2-whole-language-compiler-file-artifact source-path)
    :stage3-seedless-compiler-candidate
    (p15-s23-stage3-seedless-compiler-candidate-file-artifact source-path)
    :stage3-equivalence-bundle
    (p15-s23-stage3-equivalence-bundle-file-artifact source-path)
    :stage3-self-hosted-application-execution
    (p15-s23-stage3-self-hosted-application-file-artifact source-path)))

(def p15-s23-current-candidate-artifact-write-order
  [:compiler-source-inventory
   :compiler-pipeline-manifest
   :source-unit-and-syntax-serialization-proof
   :core-lowering-and-diagnostic-preservation-report
   :runtime-manifest-and-capability-enforcement-report
   :accepted-app-execution-proof
   :rejected-app-diagnostic-proof
   :reproducible-rebuild-log
   :stage-comparison-report
   :conformance-report
   :provenance-attestation
   :tcb-delta-record
   :unsafe-audit-report
   :whole-language-compiler-artifact
   :governance-and-package-release-record
   :stage2-compiler-nucleus
   :stage2-plan-emitter
   :stage2-runtime-kernel
   :stage2-runtime-executor
   :stage2-front-end-executor
   :stage2-source-front-end
   :stage2-compiler-driver
   :stage2-whole-language-compiler
   :stage3-seedless-compiler-candidate
   :stage3-equivalence-bundle
   :stage3-self-hosted-application-execution])

(defn p15-s23-write-current-candidate-artifacts!
  ([]
   (p15-s23-write-current-candidate-artifacts!
    p15-s23-compiler-source-path))
  ([source-path]
   (p15-s23-with-artifact-build-context
    #(let [written
           (mapv
            (fn [key]
              (let [path (get p15-s23-current-candidate-artifact-files key)
                    artifact (p15-s23-current-candidate-artifact
                              key source-path)]
                (p18-t02-write-edn! path artifact)
                {:key key
                 :path path
                 :artifact (:kind artifact)
                 :artifact-id (:artifact-id artifact)
                 :status (:status artifact)}))
            p15-s23-current-candidate-artifact-write-order)
           final-artifact
           (p15-s23-final-seed-retirement-file-artifact source-path)
           final-path
           "docs/artifacts/phase-15/bootstrap/p15-s23-final-seed-retirement-proof.edn"]
       (p18-t02-write-edn! final-path final-artifact)
       (let [record
             {:kind :gravity/p15-s23-current-candidate-artifact-write
              :task "P15-S23"
              :source-path source-path
              :written-artifacts
              (conj written
                    {:key :final-seed-retirement-proof
                     :path final-path
                     :artifact (:kind final-artifact)
                     :artifact-id (:artifact-id final-artifact)
                     :status (:status final-artifact)})
              :clojure-seed-boundary? true
              :full-language-compiler-self-hosted? false
              :clojure-seed-retired? false}]
         (assoc record :artifact-id (c4-artifact-id record)))))))