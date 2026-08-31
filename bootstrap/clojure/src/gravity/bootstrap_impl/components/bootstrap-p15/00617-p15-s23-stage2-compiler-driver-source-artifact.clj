

(defn p15-s23-stage2-compiler-driver-source-artifact*
  [source-path]
  (let [source-data (p15-s23-compiler-source-form-record source-path)
        driver
        (p15-s23-compiler-def-value
         source-path (:forms source-data)
         'p15-s23-stage2-compiler-driver)
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
        rule-record
        (p15-s23-stage2-compiler-driver-rule-record driver)
        accepted-record
        (p15-s23-stage2-compiler-driver-accepted-record
         driver front-end emitter runtime)
        rejected-fixture-records
        (p15-s23-stage2-compiler-driver-rejected-records
         driver front-end emitter runtime)
        rejected-record
        (p15-s23-stage2-compiler-driver-rejected-record
         rejected-fixture-records)
        front-end-executor-artifact
        (p15-s23-stage2-front-end-executor-source-artifact source-path)
	        front-end-artifact
	        (p15-s23-stage2-source-front-end-source-artifact source-path)
	        runtime-kernel-artifact
	        (p15-s23-stage2-runtime-kernel-source-artifact source-path)
	        runtime-artifact
	        (p15-s23-stage2-runtime-executor-source-artifact source-path)
        plan-emitter-artifact
        (p15-s23-stage2-plan-emitter-source-artifact source-path)
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
	        (p15-s23-stage2-compiler-driver-evidence-link-record
	         front-end-executor-artifact front-end-artifact
	         runtime-kernel-artifact runtime-artifact plan-emitter-artifact
	         nucleus-artifact pipeline-artifact accepted-artifact
	         rejected-artifact)
        boundary-record
        (p15-s23-stage2-compiler-driver-boundary-record driver)
        candidate {:driver-contract driver
                   :rule-record rule-record
                   :accepted-record accepted-record
                   :rejected-record rejected-record
                   :evidence-link-record evidence-link-record
                   :boundary-record boundary-record}
        diagnostics
        (p15-s23-stage2-compiler-driver-diagnostics source-path
                                                   candidate)
        _ (when (seq diagnostics)
            (p15-s23-stage2-compiler-driver-fail!
             (:diagnostic (first diagnostics)) source-path candidate
             {:diagnostics diagnostics}))
        proof-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:source-path source-path
                       :driver driver
                       :stage2-plan-id
                       (:stage2-plan-id accepted-record)
                       :runtime-output
                       (:stage2-driver-output accepted-record)
                       :rejected-diagnostics
                       (:observed-diagnostics rejected-record)})))
        rejected-proof-records
        (p15-s23-stage2-compiler-driver-rejected-proof-records
         source-path candidate)
        artifact-base
        {:kind :gravity/p15-s23-stage2-compiler-driver-artifact
         :phase "15"
         :task "P15-S23"
         :stage :p15-s23-stage2-compiler-driver
         :source-path source-path
         :proof-id proof-id
         :driver-contract driver
         :rule-record rule-record
         :accepted-record accepted-record
         :rejected-record rejected-record
         :evidence-link-record evidence-link-record
         :boundary-record boundary-record
         :linked-artifacts
         {:stage2-front-end-executor
          (select-keys front-end-executor-artifact
                       [:kind :artifact-id :proof-id])
	          :stage2-source-front-end
	          (select-keys front-end-artifact [:kind :artifact-id :proof-id])
	          :stage2-runtime-kernel
	          (select-keys runtime-kernel-artifact
	                       [:kind :artifact-id :proof-id])
	          :stage2-runtime-executor
          (select-keys runtime-artifact [:kind :artifact-id :proof-id])
          :stage2-plan-emitter
          (select-keys plan-emitter-artifact
                       [:kind :artifact-id :proof-id])
          :stage2-compiler-nucleus
          (select-keys nucleus-artifact [:kind :artifact-id :proof-id])
          :compiler-pipeline-manifest
          (select-keys pipeline-artifact [:kind :artifact-id :proof-id])
          :accepted-app-execution-proof
          (select-keys accepted-artifact [:kind :artifact-id :proof-id])
          :rejected-app-diagnostic-proof
          (select-keys rejected-artifact [:kind :artifact-id :proof-id])}
         :full-language-compiler-self-hosted?
         (get-in driver
                 [:self-hosting-claims
                  :full-language-compiler-self-hosted?])
         :clojure-seed-retired?
         (get-in driver [:self-hosting-claims
                         :clojure-seed-retired?])
         :accepted-p15-s23-stage2-compiler-driver-fixtures
         [{:fixture p15-s23-accepted-app-source-path
           :status :accepted
           :stage2-plan-id (:stage2-plan-id accepted-record)
           :stdout (:stage2-driver-output accepted-record)}]
         :verified-p15-s23-stage2-compiler-driver-rejected-fixtures
         rejected-fixture-records
         :rejected-p15-s23-stage2-compiler-driver-fixtures
         rejected-proof-records
         :p15-s23-stage2-compiler-driver-diagnostic-stream
         (p15-s23-stage2-compiler-driver-diagnostic-stream
          source-path proof-id)
         :p15-s23-stage2-compiler-driver-results
         {:accepted-fixtures 1
          :rejected-fixtures (count rejected-fixture-records)
          :internal-rejected-fixtures (count rejected-proof-records)
          :diagnostic-count
          (count p15-s23-stage2-compiler-driver-diagnostic-ids)
          :stage2-plan-id (:stage2-plan-id accepted-record)
          :stage0-plan-id (:stage0-plan-id accepted-record)
          :accepted-output (:stage2-driver-output accepted-record)
          :rejected-diagnostics (:observed-diagnostics rejected-record)
          :status :in-progress}
         :diagnostics []}
        proof (p15-s23-stage2-compiler-driver-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id
           (c4-artifact-id
            (assoc artifact-base :capability-based-proof proof)))))

(defn p15-s23-stage2-compiler-driver-source-artifact
  [source-path]
  (p15-s23-cached-source-artifact
   :p15-s23-stage2-compiler-driver
   source-path
   #(p15-s23-stage2-compiler-driver-source-artifact* source-path)))

(defn p15-s23-stage2-compiler-driver-file-artifact
  [path]
  (when-not (.isFile (java.io.File. path))
    (p15-s23-stage2-compiler-driver-fail!
     "P15S23Y001" path nil {:missing-fields [:compiler-source]}))
  (p15-s23-stage2-compiler-driver-source-artifact path))

(defn p15-s23-stage2-compiler-driver-evidence
  []
  (let [source-path "bootstrap/gravity/p15_s23/compiler.gravity"]
    (when (.isFile (java.io.File. source-path))
      (try
        (let [artifact
              (p15-s23-stage2-compiler-driver-file-artifact
               source-path)
              proof (:capability-based-proof artifact)]
          {:status :verified
           :artifact (:kind artifact)
           :artifact-id (:artifact-id artifact)
           :proof-id (:proof-id artifact)
           :source-path source-path
           :stage2-plan-id
           (get-in artifact [:accepted-record :stage2-plan-id])
           :stage2-compiler-driver-executed?
           (:stage2-compiler-driver-executed? proof)
           :stage0-compiler-driver-replaced?
           (:stage0-compiler-driver-replaced? proof)
           :stage0-rule-runner-replaced?
           (:stage0-rule-runner-replaced? proof)
           :stage0-reader-replaced?
           (:stage0-reader-replaced? proof)
           :stage0-macro-expander-replaced?
           (:stage0-macro-expander-replaced? proof)
           :stage2-front-end-executor-used?
           (:stage2-front-end-executor-used? proof)
           :stage2-front-end-host-replaced?
           (:stage2-front-end-host-replaced? proof)
	           :stage2-source-front-end-used?
	           (:stage2-source-front-end-used? proof)
	           :stage2-runtime-kernel-used?
	           (:stage2-runtime-kernel-used? proof)
	           :stage2-runtime-host-replaced?
	           (:stage2-runtime-host-replaced? proof)
	           :stage2-runtime-primitives-replaced?
	           (:stage2-runtime-primitives-replaced? proof)
	           :gravity-runtime-primitives-used?
	           (:gravity-runtime-primitives-used? proof)
	           :accepted-output-equivalent?
           (:accepted-output-equivalent? proof)
           :rejected-diagnostics-equivalent?
           (:rejected-diagnostics-equivalent? proof)
           :residual-clojure-driver-host-recorded?
           (:residual-clojure-driver-host-recorded? proof)
	           :does-not-use-clojure-stage2-front-end-host?
	           (:does-not-use-clojure-stage2-front-end-host? proof)
	           :does-not-use-clojure-stage0-runtime-host?
	           (:does-not-use-clojure-stage0-runtime-host? proof)
	           :does-not-use-clojure-runtime-primitives?
	           (:does-not-use-clojure-runtime-primitives? proof)
	           :does-not-use-clojure-stage0-reader?
           (:does-not-use-clojure-stage0-reader? proof)
           :does-not-use-clojure-stage0-macro-expander?
           (:does-not-use-clojure-stage0-macro-expander? proof)
           :full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? artifact)
           :clojure-seed-retired? (:clojure-seed-retired? artifact)})
	      (catch Exception _
	          nil)))))

(def p15-s23-stage2-whole-language-compiler-required-preserves
  #{:source-spans :source-unit-identity :syntax-identity :diagnostic-codes
    :artifact-provenance :pipeline-stage-contracts
    :runtime-capability-manifest :accepted-app-output
    :rejected-app-diagnostic-trace :compiler-lineage
    :tcb-component-inventory :unsafe-island-index})