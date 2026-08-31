

(defn p15-s23-stage2-plan-emitter-source-artifact
  [source-path]
  (let [source-data (p15-s23-compiler-source-form-record source-path)
        emitter
        (p15-s23-compiler-def-value source-path
                                     (:forms source-data)
                                     'p15-s23-stage2-plan-emitter)
        rule-record (p15-s23-stage2-plan-emitter-rule-record emitter)
        accepted-record (p15-s23-stage2-plan-emitter-accepted-record
                         emitter)
        rejected-fixture-records
        (p15-s23-stage2-plan-emitter-rejected-records emitter)
        rejected-record
        (p15-s23-stage2-plan-emitter-rejected-record
         rejected-fixture-records)
        nucleus-artifact
        (p15-s23-stage2-compiler-nucleus-source-artifact source-path)
        pipeline-artifact
        (p15-s23-compiler-pipeline-manifest-source-artifact
         source-path)
        accepted-artifact
        (p15-s23-accepted-app-execution-source-artifact source-path)
        rejected-artifact
        (p15-s23-rejected-app-diagnostic-source-artifact source-path)
        evidence-link-record
        (p15-s23-stage2-plan-emitter-evidence-link-record
         nucleus-artifact pipeline-artifact accepted-artifact
         rejected-artifact)
        boundary-record
        (p15-s23-stage2-plan-emitter-boundary-record emitter)
        candidate {:emitter-contract emitter
                   :rule-record rule-record
                   :accepted-record accepted-record
                   :rejected-record rejected-record
                   :evidence-link-record evidence-link-record
                   :boundary-record boundary-record}
        diagnostics
        (p15-s23-stage2-plan-emitter-diagnostics source-path
                                                 candidate)
        _ (when (seq diagnostics)
            (p15-s23-stage2-plan-emitter-fail!
             (:diagnostic (first diagnostics)) source-path candidate
             {:diagnostics diagnostics}))
        proof-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:source-path source-path
                       :emitter emitter
                       :stage2-plan-id
                       (:stage2-plan-id accepted-record)
                       :stage0-plan-id
                       (:stage0-plan-id accepted-record)
                       :rejected-diagnostics
                       (:observed-diagnostics rejected-record)})))
        rejected-proof-records
        (p15-s23-stage2-plan-emitter-rejected-proof-records
         source-path candidate)
        artifact-base
        {:kind :gravity/p15-s23-stage2-plan-emitter-artifact
         :phase "15"
         :task "P15-S23"
         :stage :p15-s23-stage2-plan-emitter
         :source-path source-path
         :proof-id proof-id
         :emitter-contract emitter
         :rule-record rule-record
         :accepted-record accepted-record
         :rejected-record rejected-record
         :evidence-link-record evidence-link-record
         :boundary-record boundary-record
         :linked-artifacts
         {:stage2-compiler-nucleus
          (select-keys nucleus-artifact [:kind :artifact-id :proof-id])
          :compiler-pipeline-manifest
          (select-keys pipeline-artifact [:kind :artifact-id :proof-id])
          :accepted-app-execution-proof
          (select-keys accepted-artifact [:kind :artifact-id :proof-id])
          :rejected-app-diagnostic-proof
          (select-keys rejected-artifact [:kind :artifact-id :proof-id])}
         :full-language-compiler-self-hosted?
         (get-in emitter
                 [:self-hosting-claims
                  :full-language-compiler-self-hosted?])
         :clojure-seed-retired?
         (get-in emitter [:self-hosting-claims
                          :clojure-seed-retired?])
         :accepted-p15-s23-stage2-plan-emitter-fixtures
         [{:fixture p15-s23-accepted-app-source-path
           :status :accepted
           :stage2-plan-id (:stage2-plan-id accepted-record)
           :stage0-plan-id (:stage0-plan-id accepted-record)
           :stdout (:stage2-output accepted-record)}]
         :verified-p15-s23-stage2-plan-emitter-rejected-fixtures
         rejected-fixture-records
         :rejected-p15-s23-stage2-plan-emitter-fixtures
         rejected-proof-records
         :p15-s23-stage2-plan-emitter-diagnostic-stream
         (p15-s23-stage2-plan-emitter-diagnostic-stream
          source-path proof-id)
         :p15-s23-stage2-plan-emitter-results
         {:accepted-fixtures 1
          :rejected-fixtures (count rejected-fixture-records)
          :internal-rejected-fixtures (count rejected-proof-records)
          :diagnostic-count
          (count p15-s23-stage2-plan-emitter-diagnostic-ids)
          :stage2-plan-id (:stage2-plan-id accepted-record)
          :stage0-plan-id (:stage0-plan-id accepted-record)
          :accepted-output (:stage2-output accepted-record)
          :rejected-diagnostics (:observed-diagnostics rejected-record)
          :status :in-progress}
         :diagnostics []}
        proof (p15-s23-stage2-plan-emitter-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id
           (c4-artifact-id
            (assoc artifact-base :capability-based-proof proof)))))

(defn p15-s23-stage2-plan-emitter-file-artifact
  [path]
  (when-not (.isFile (java.io.File. path))
    (p15-s23-stage2-plan-emitter-fail!
     "P15S23Q001" path nil {:missing-fields [:compiler-source]}))
  (p15-s23-stage2-plan-emitter-source-artifact path))

(defn p15-s23-stage2-plan-emitter-evidence
  []
  (let [source-path "bootstrap/gravity/p15_s23/compiler.gravity"]
    (when (.isFile (java.io.File. source-path))
      (try
        (let [artifact
              (p15-s23-stage2-plan-emitter-file-artifact source-path)
              proof (:capability-based-proof artifact)]
          {:status :verified
           :artifact (:kind artifact)
           :artifact-id (:artifact-id artifact)
           :proof-id (:proof-id artifact)
           :source-path source-path
           :stage2-plan-id
           (get-in artifact [:accepted-record :stage2-plan-id])
           :stage0-plan-emitter-replaced?
           (:stage0-plan-emitter-replaced? proof)
           :accepted-output-equivalent?
           (:accepted-output-equivalent? proof)
           :rejected-diagnostics-equivalent?
           (:rejected-diagnostics-equivalent? proof)
           :residual-clojure-rule-runner-recorded?
           (:residual-clojure-rule-runner-recorded? proof)
           :full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? artifact)
           :clojure-seed-retired? (:clojure-seed-retired? artifact)})
        (catch Exception _
          nil)))))

(def p15-s23-stage2-runtime-executor-required-preserves
  #{:source-spans :diagnostic-codes :instruction-semantics
    :function-bindings :effects :capabilities :profile
    :compiler-lineage :artifact-provenance})

(def p15-s23-stage2-runtime-executor-required-emits
  #{:stage2-runtime-execution-record :accepted-output-comparison
    :rejected-diagnostic-comparison :stage2-runtime-boundary-record})

(def p15-s23-stage2-runtime-executor-required-instructions
  #{:literal :quote :local :vector-literal :set-literal :map-literal
    :println :do :if :let :builtin-call :function-call})

(def p15-s23-stage2-runtime-executor-diagnostic-messages
  {"P15S23X001" "P15-S23 stage2 runtime executor contract is missing"
   "P15S23X002" "P15-S23 stage2 runtime executor rule set is incomplete"
   "P15S23X003" "P15-S23 stage2 runtime executor accepted output is not equivalent"
   "P15S23X004" "P15-S23 stage2 runtime executor rejected diagnostics are not preserved"
   "P15S23X005" "P15-S23 stage2 runtime executor evidence links are incomplete"
   "P15S23X006" "P15-S23 stage2 runtime executor preservation or emission contract is incomplete"
   "P15S23X007" "P15-S23 stage2 runtime executor residual boundary record is incomplete"
   "P15S23X008" "P15-S23 stage2 runtime executor makes an unsupported self-hosting or seed-retirement claim"})

(def p15-s23-stage2-runtime-executor-diagnostic-ids
  ["P15S23X001" "P15S23X002" "P15S23X003" "P15S23X004"
   "P15S23X005" "P15S23X006" "P15S23X007" "P15S23X008"])

(defn p15-s23-stage2-runtime-executor-fail!
  [id source-path value data]
  (fail! id
         (get p15-s23-stage2-runtime-executor-diagnostic-messages
              id
              "P15-S23 stage2 runtime executor proof failed")
         (merge {:source-span {:source source-path}
                 :stage :p15-s23-stage2-runtime-executor
                 :diagnostic-family :p15-s23-stage2-runtime-executor
                 :value value
                 :remediation "Keep the stage2 runtime executor rules authored in Gravity source, execute the stage2 plan through the declared runtime-host boundary, prove accepted output and rejected diagnostics against the current stage, and keep full self-hosting claims false."}
                data)))

(defn p15-s23-stage2-runtime-executor-diagnostic-record
  [source-path id value data]
  {:artifact :gravity/diagnostic
   :diagnostic-id (str "diag-" (str/lower-case id))
   :diagnostic id
   :severity :error
   :stage :p15-s23-stage2-runtime-executor
   :source-span {:source source-path}
   :message (get p15-s23-stage2-runtime-executor-diagnostic-messages id)
   :facts data
   :observed value
   :remediation :repair_stage2_runtime_executor})