

(defn p15-s23-stage2-front-end-executor-proof
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:p15-s23-stage2-front-end-executor-diagnostic-stream
                           :diagnostics])))
        rejected-diagnostics
        (set (mapcat #(map :diagnostic (:diagnostics %))
                     (:rejected-p15-s23-stage2-front-end-executor-fixtures
                      artifact)))
        accepted-record (:accepted-record artifact)
        rejected-record (:rejected-record artifact)
        boundary-record (:boundary-record artifact)]
    {:stage2-front-end-executor-authored-in-gravity? true
     :status :in-progress
     :task "P15-S23"
     :stage2-front-end-executor-present?
     (= :gravity/stage2-front-end-executor
        (get-in artifact [:executor-contract :artifact]))
     :stage2-front-end-executor-used?
     (true? (:stage2-front-end-executor-used? boundary-record))
	     :stage2-front-end-host-replaced?
	     (true? (:stage2-front-end-host-replaced? boundary-record))
	     :stage2-runtime-kernel-used?
	     (true? (:stage2-runtime-kernel-used? boundary-record))
	     :stage2-runtime-host-replaced?
	     (true? (:stage2-runtime-host-replaced? boundary-record))
	     :stage2-runtime-primitives-replaced?
	     (true? (:stage2-runtime-primitives-replaced?
	             boundary-record))
	     :gravity-runtime-primitives-used?
	     (true? (:gravity-runtime-primitives? boundary-record))
     :does-not-use-clojure-stage2-front-end-host?
     (false? (:clojure-stage2-front-end-host? boundary-record))
     :read-form-parity?
     (true? (:read-form-parity? accepted-record))
     :macro-expanded-form-parity?
     (true? (:macro-expanded-form-parity? accepted-record))
	     :accepted-output-equivalent?
	     (true? (:accepted-output-equivalent? accepted-record))
		     :rejected-diagnostics-equivalent?
		     (= :complete (:status rejected-record))
		     :does-not-use-clojure-stage0-runtime-host?
		     (false? (:clojure-stage0-runtime-host? boundary-record))
	     :does-not-use-clojure-runtime-primitives?
	     (false? (:clojure-host-primitive-boundary? boundary-record))
     :does-not-claim-full-self-hosting?
     (false? (:full-language-compiler-self-hosted? artifact))
     :does-not-claim-clojure-seed-retirement?
     (false? (:clojure-seed-retired? artifact))
     :rejected-candidates-covered?
     (= (set p15-s23-stage2-front-end-executor-diagnostic-ids)
        rejected-diagnostics)
     :diagnostics-covered?
     (= (set p15-s23-stage2-front-end-executor-diagnostic-ids)
        diagnostics)
     :limitations
     {:full-language-compiler-self-hosted? false
      :clojure-seed-retired? false
	      :stage2-front-end-executor-authored-in-gravity? true
	      :stage2-front-end-executor-used? true
	      :stage2-front-end-host-replaced? true
	      :stage2-runtime-kernel-used? true
	      :stage2-runtime-host-replaced? true
	      :stage2-runtime-primitives-replaced? true
	      :gravity-runtime-primitives? true
	      :clojure-stage2-front-end-host? false
	      :clojure-stage0-runtime-host? false
	      :clojure-host-primitive-boundary? false
	      :full-self-hosted-toolchain? false
	      :next-required-capability
	      :implement_whole_language_compiler_stage_without_clojure_seed}}))

(defn p15-s23-stage2-front-end-executor-source-artifact*
  [source-path]
  (let [source-data (p15-s23-compiler-source-form-record source-path)
        executor
        (p15-s23-compiler-def-value
         source-path (:forms source-data)
         'p15-s23-stage2-front-end-executor)
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
        (p15-s23-stage2-front-end-executor-rule-record executor)
        accepted-record
        (p15-s23-stage2-front-end-executor-accepted-record
         executor front-end emitter runtime)
        rejected-fixture-records
        (p15-s23-stage2-front-end-executor-rejected-records
         executor front-end emitter runtime)
	        rejected-record
	        (p15-s23-stage2-front-end-executor-rejected-record
	         rejected-fixture-records)
	        runtime-kernel-artifact
	        (p15-s23-stage2-runtime-kernel-source-artifact source-path)
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
	        (p15-s23-stage2-front-end-executor-evidence-link-record
	         runtime-kernel-artifact nucleus-artifact plan-emitter-artifact
	         runtime-artifact pipeline-artifact)
        boundary-record
        (p15-s23-stage2-front-end-executor-boundary-record executor)
        candidate {:executor-contract executor
                   :rule-record rule-record
                   :accepted-record accepted-record
                   :rejected-record rejected-record
                   :evidence-link-record evidence-link-record
                   :boundary-record boundary-record}
        diagnostics
        (p15-s23-stage2-front-end-executor-diagnostics source-path
                                                       candidate)
        _ (when (seq diagnostics)
            (p15-s23-stage2-front-end-executor-fail!
             (:diagnostic (first diagnostics)) source-path candidate
             {:diagnostics diagnostics}))
        proof-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:source-path source-path
                       :executor executor
                       :stage2-plan-id (:stage2-plan-id accepted-record)
                       :runtime-output
                       (:stage2-front-end-executor-output
                        accepted-record)
                       :rejected-diagnostics
                       (:observed-diagnostics rejected-record)})))
        rejected-proof-records
        (p15-s23-stage2-front-end-executor-rejected-proof-records
         source-path candidate)
        artifact-base
        {:kind :gravity/p15-s23-stage2-front-end-executor-artifact
         :phase "15"
         :task "P15-S23"
         :stage :p15-s23-stage2-front-end-executor
         :source-path source-path
         :proof-id proof-id
         :executor-contract executor
         :rule-record rule-record
         :accepted-record accepted-record
         :rejected-record rejected-record
         :evidence-link-record evidence-link-record
	         :boundary-record boundary-record
	         :linked-artifacts
	         {:stage2-runtime-kernel
	          (select-keys runtime-kernel-artifact
	                       [:kind :artifact-id :proof-id])
	          :stage2-compiler-nucleus
	          (select-keys nucleus-artifact [:kind :artifact-id :proof-id])
          :stage2-plan-emitter
          (select-keys plan-emitter-artifact
                       [:kind :artifact-id :proof-id])
          :stage2-runtime-executor
          (select-keys runtime-artifact [:kind :artifact-id :proof-id])
          :compiler-pipeline-manifest
          (select-keys pipeline-artifact [:kind :artifact-id :proof-id])}
         :full-language-compiler-self-hosted?
         (get-in executor
                 [:self-hosting-claims
                  :full-language-compiler-self-hosted?])
         :clojure-seed-retired?
         (get-in executor [:self-hosting-claims
                           :clojure-seed-retired?])
         :accepted-p15-s23-stage2-front-end-executor-fixtures
         [{:fixture p15-s23-accepted-app-source-path
           :status :accepted
           :stage2-plan-id (:stage2-plan-id accepted-record)
           :stdout (:stage2-front-end-executor-output
                    accepted-record)}]
         :verified-p15-s23-stage2-front-end-executor-rejected-fixtures
         rejected-fixture-records
         :rejected-p15-s23-stage2-front-end-executor-fixtures
         rejected-proof-records
         :p15-s23-stage2-front-end-executor-diagnostic-stream
         (p15-s23-stage2-front-end-executor-diagnostic-stream
          source-path proof-id)
         :p15-s23-stage2-front-end-executor-results
         {:accepted-fixtures 1
          :rejected-fixtures (count rejected-fixture-records)
          :internal-rejected-fixtures (count rejected-proof-records)
          :diagnostic-count
          (count p15-s23-stage2-front-end-executor-diagnostic-ids)
          :stage2-plan-id (:stage2-plan-id accepted-record)
          :accepted-output
          (:stage2-front-end-executor-output accepted-record)
          :rejected-diagnostics (:observed-diagnostics rejected-record)
          :status :in-progress}
         :diagnostics []}
        proof (p15-s23-stage2-front-end-executor-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id
           (c4-artifact-id
            (assoc artifact-base :capability-based-proof proof)))))

(defn p15-s23-stage2-front-end-executor-source-artifact
  [source-path]
  (p15-s23-cached-source-artifact
   :p15-s23-stage2-front-end-executor
   source-path
   #(p15-s23-stage2-front-end-executor-source-artifact* source-path)))

(defn p15-s23-stage2-front-end-executor-file-artifact
  [path]
  (when-not (.isFile (java.io.File. path))
    (p15-s23-stage2-front-end-executor-fail!
     "P15S23J001" path nil {:missing-fields [:compiler-source]}))
  (p15-s23-stage2-front-end-executor-source-artifact path))