

(defn p15-s23-stage2-source-front-end-source-artifact*
  [source-path]
  (let [source-data (p15-s23-compiler-source-form-record source-path)
        front-end
        (p15-s23-compiler-def-value
         source-path (:forms source-data)
         'p15-s23-stage2-source-front-end)
        emitter
        (p15-s23-compiler-def-value
         source-path (:forms source-data)
         'p15-s23-stage2-plan-emitter)
        runtime
        (p15-s23-compiler-def-value
         source-path (:forms source-data)
         'p15-s23-stage2-runtime-executor)
        front-end-executor-artifact
        (p15-s23-stage2-front-end-executor-source-artifact source-path)
        rule-record
        (p15-s23-stage2-source-front-end-rule-record front-end)
        accepted-record
        (p15-s23-stage2-source-front-end-accepted-record
         front-end emitter runtime)
        rejected-fixture-records
        (p15-s23-stage2-source-front-end-rejected-records
         front-end emitter runtime)
        rejected-record
        (p15-s23-stage2-source-front-end-rejected-record
         rejected-fixture-records)
        nucleus-artifact
        (p15-s23-stage2-compiler-nucleus-source-artifact source-path)
	        plan-emitter-artifact
	        (p15-s23-stage2-plan-emitter-source-artifact source-path)
	        runtime-kernel-artifact
	        (p15-s23-stage2-runtime-kernel-source-artifact source-path)
	        runtime-artifact
	        (p15-s23-stage2-runtime-executor-source-artifact source-path)
        pipeline-artifact
        (p15-s23-compiler-pipeline-manifest-source-artifact
         source-path)
	        evidence-link-record
	        (p15-s23-stage2-source-front-end-evidence-link-record
	        front-end-executor-artifact nucleus-artifact
	         plan-emitter-artifact runtime-kernel-artifact
	         runtime-artifact pipeline-artifact)
        boundary-record
        (p15-s23-stage2-source-front-end-boundary-record front-end)
        candidate {:front-end-contract front-end
                   :rule-record rule-record
                   :accepted-record accepted-record
                   :rejected-record rejected-record
                   :evidence-link-record evidence-link-record
                   :boundary-record boundary-record}
        diagnostics
        (p15-s23-stage2-source-front-end-diagnostics source-path
                                                     candidate)
        _ (when (seq diagnostics)
            (p15-s23-stage2-source-front-end-fail!
             (:diagnostic (first diagnostics)) source-path candidate
             {:diagnostics diagnostics}))
        proof-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:source-path source-path
                       :front-end front-end
                       :stage2-plan-id (:stage2-plan-id accepted-record)
                       :runtime-output
                       (:stage2-front-end-output accepted-record)
                       :rejected-diagnostics
                       (:observed-diagnostics rejected-record)})))
        rejected-proof-records
        (p15-s23-stage2-source-front-end-rejected-proof-records
         source-path candidate)
        artifact-base
        {:kind :gravity/p15-s23-stage2-source-front-end-artifact
         :phase "15"
         :task "P15-S23"
         :stage :p15-s23-stage2-source-front-end
         :source-path source-path
         :proof-id proof-id
         :front-end-contract front-end
         :rule-record rule-record
         :accepted-record accepted-record
         :rejected-record rejected-record
         :evidence-link-record evidence-link-record
         :boundary-record boundary-record
         :linked-artifacts
         {:stage2-front-end-executor
          (select-keys front-end-executor-artifact
                       [:kind :artifact-id :proof-id])
          :stage2-compiler-nucleus
          (select-keys nucleus-artifact [:kind :artifact-id :proof-id])
	          :stage2-plan-emitter
	          (select-keys plan-emitter-artifact
	                       [:kind :artifact-id :proof-id])
	          :stage2-runtime-kernel
	          (select-keys runtime-kernel-artifact
	                       [:kind :artifact-id :proof-id])
	          :stage2-runtime-executor
          (select-keys runtime-artifact [:kind :artifact-id :proof-id])
          :compiler-pipeline-manifest
          (select-keys pipeline-artifact [:kind :artifact-id :proof-id])}
         :full-language-compiler-self-hosted?
         (get-in front-end
                 [:self-hosting-claims
                  :full-language-compiler-self-hosted?])
         :clojure-seed-retired?
         (get-in front-end [:self-hosting-claims
                            :clojure-seed-retired?])
         :accepted-p15-s23-stage2-source-front-end-fixtures
         [{:fixture p15-s23-accepted-app-source-path
           :status :accepted
           :stage2-plan-id (:stage2-plan-id accepted-record)
           :stdout (:stage2-front-end-output accepted-record)}]
         :verified-p15-s23-stage2-source-front-end-rejected-fixtures
         rejected-fixture-records
         :rejected-p15-s23-stage2-source-front-end-fixtures
         rejected-proof-records
         :p15-s23-stage2-source-front-end-diagnostic-stream
         (p15-s23-stage2-source-front-end-diagnostic-stream
          source-path proof-id)
         :p15-s23-stage2-source-front-end-results
         {:accepted-fixtures 1
          :rejected-fixtures (count rejected-fixture-records)
          :internal-rejected-fixtures (count rejected-proof-records)
          :diagnostic-count
          (count p15-s23-stage2-source-front-end-diagnostic-ids)
          :stage2-plan-id (:stage2-plan-id accepted-record)
          :accepted-output (:stage2-front-end-output accepted-record)
          :rejected-diagnostics (:observed-diagnostics rejected-record)
          :status :in-progress}
         :diagnostics []}
        proof (p15-s23-stage2-source-front-end-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id
           (c4-artifact-id
            (assoc artifact-base :capability-based-proof proof)))))

(defn p15-s23-stage2-source-front-end-source-artifact
  [source-path]
  (p15-s23-cached-source-artifact
   :p15-s23-stage2-source-front-end
   source-path
   #(p15-s23-stage2-source-front-end-source-artifact* source-path)))

(defn p15-s23-stage2-source-front-end-file-artifact
  [path]
  (when-not (.isFile (java.io.File. path))
    (p15-s23-stage2-source-front-end-fail!
     "P15S23F001" path nil {:missing-fields [:compiler-source]}))
  (p15-s23-stage2-source-front-end-source-artifact path))

(defn p15-s23-stage2-source-front-end-evidence
  []
  (let [source-path "bootstrap/gravity/p15_s23/compiler.gravity"]
    (when (.isFile (java.io.File. source-path))
      (try
        (let [artifact
              (p15-s23-stage2-source-front-end-file-artifact
               source-path)
              proof (:capability-based-proof artifact)]
          {:status :verified
           :artifact (:kind artifact)
           :artifact-id (:artifact-id artifact)
           :proof-id (:proof-id artifact)
           :source-path source-path
           :stage2-plan-id
           (get-in artifact [:accepted-record :stage2-plan-id])
           :stage2-source-front-end-executed?
           (:stage2-source-front-end-executed? proof)
           :stage2-front-end-executor-used?
           (:stage2-front-end-executor-used? proof)
	           :stage2-front-end-host-replaced?
	           (:stage2-front-end-host-replaced? proof)
	           :stage2-runtime-kernel-used?
	           (:stage2-runtime-kernel-used? proof)
	           :stage2-runtime-host-replaced?
	           (:stage2-runtime-host-replaced? proof)
	           :stage2-runtime-primitives-replaced?
	           (:stage2-runtime-primitives-replaced? proof)
	           :gravity-runtime-primitives-used?
	           (:gravity-runtime-primitives-used? proof)
	           :stage0-reader-replaced?
           (:stage0-reader-replaced? proof)
           :stage0-macro-expander-replaced?
           (:stage0-macro-expander-replaced? proof)
           :accepted-output-equivalent?
           (:accepted-output-equivalent? proof)
           :rejected-diagnostics-equivalent?
           (:rejected-diagnostics-equivalent? proof)
	           :does-not-use-clojure-stage2-front-end-host?
	           (:does-not-use-clojure-stage2-front-end-host? proof)
	           :does-not-use-clojure-stage0-runtime-host?
	           (:does-not-use-clojure-stage0-runtime-host? proof)
	           :does-not-use-clojure-runtime-primitives?
	           (:does-not-use-clojure-runtime-primitives? proof)
           :full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? artifact)
           :clojure-seed-retired? (:clojure-seed-retired? artifact)})
        (catch Exception _
          nil)))))

(def p15-s23-stage2-compiler-driver-required-preserves
  #{:source-spans :source-unit-identity :syntax-identity
    :diagnostic-codes :function-bindings :instruction-semantics
    :effects :capabilities :profile :compiler-lineage
    :artifact-provenance})

(def p15-s23-stage2-compiler-driver-required-emits
  #{:stage2-driver-run-record :stage2-instruction-plan
    :stage2-runtime-execution-record :accepted-output-comparison
    :rejected-diagnostic-comparison :stage2-driver-boundary-record})

(def p15-s23-stage2-compiler-driver-required-steps
  #{:read-source :macro-expand :emit-stage2-plan
    :execute-stage2-runtime :compare-reference-output
    :capture-rejected-diagnostics :emit-boundary-record})

(def p15-s23-stage2-compiler-driver-diagnostic-messages
  {"P15S23Y001" "P15-S23 stage2 compiler driver contract is missing"
   "P15S23Y002" "P15-S23 stage2 compiler driver step set is incomplete"
   "P15S23Y003" "P15-S23 stage2 compiler driver accepted execution is not equivalent"
   "P15S23Y004" "P15-S23 stage2 compiler driver rejected diagnostics are not preserved"
   "P15S23Y005" "P15-S23 stage2 compiler driver evidence links are incomplete"
   "P15S23Y006" "P15-S23 stage2 compiler driver preservation or emission contract is incomplete"
   "P15S23Y007" "P15-S23 stage2 compiler driver residual boundary record is incomplete"
   "P15S23Y008" "P15-S23 stage2 compiler driver makes an unsupported self-hosting or seed-retirement claim"})

(def p15-s23-stage2-compiler-driver-diagnostic-ids
  ["P15S23Y001" "P15S23Y002" "P15S23Y003" "P15S23Y004"
   "P15S23Y005" "P15S23Y006" "P15S23Y007" "P15S23Y008"])