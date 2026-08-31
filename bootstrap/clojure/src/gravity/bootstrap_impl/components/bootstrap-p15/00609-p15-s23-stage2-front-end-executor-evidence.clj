

(defn p15-s23-stage2-front-end-executor-evidence
  []
  (let [source-path "bootstrap/gravity/p15_s23/compiler.gravity"]
    (when (.isFile (java.io.File. source-path))
      (try
        (let [artifact
              (p15-s23-stage2-front-end-executor-file-artifact
               source-path)
              proof (:capability-based-proof artifact)]
          {:status :verified
           :artifact (:kind artifact)
           :artifact-id (:artifact-id artifact)
           :proof-id (:proof-id artifact)
           :source-path source-path
           :stage2-plan-id
           (get-in artifact [:accepted-record :stage2-plan-id])
           :stage2-front-end-executor-used?
           (:stage2-front-end-executor-used? proof)
           :stage2-front-end-host-replaced?
           (:stage2-front-end-host-replaced? proof)
           :does-not-use-clojure-stage2-front-end-host?
           (:does-not-use-clojure-stage2-front-end-host? proof)
           :accepted-output-equivalent?
           (:accepted-output-equivalent? proof)
	           :rejected-diagnostics-equivalent?
	           (:rejected-diagnostics-equivalent? proof)
	           :stage2-runtime-kernel-used?
	           (:stage2-runtime-kernel-used? proof)
	           :stage2-runtime-host-replaced?
	           (:stage2-runtime-host-replaced? proof)
	           :stage2-runtime-primitives-replaced?
	           (:stage2-runtime-primitives-replaced? proof)
	           :gravity-runtime-primitives-used?
	           (:gravity-runtime-primitives-used? proof)
	           :does-not-use-clojure-stage0-runtime-host?
	           (:does-not-use-clojure-stage0-runtime-host? proof)
	           :does-not-use-clojure-runtime-primitives?
	           (:does-not-use-clojure-runtime-primitives? proof)
           :full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? artifact)
           :clojure-seed-retired? (:clojure-seed-retired? artifact)})
        (catch Exception _
          nil)))))

(defn p15-s23-stage2-source-front-end-rule-record
  [front-end]
  (let [steps (set (:front-end-steps front-end))
        missing-steps
        (set/difference p15-s23-stage2-source-front-end-required-steps
                        steps)
        macro-rules (:macro-rules front-end)
        reader-rules (:reader-rules front-end)
        complete?
        (and (= :gravity-stage2-source-front-end-rules-v1
                (:engine front-end))
             (= :gravity-source (:implemented-by front-end))
             (= :gravity-stage2-front-end-executor
                (:executed-by front-end))
             (= :gravity-stage2-reader-rules-v1 (:engine reader-rules))
             (= :gravity-stage2-macro-rules-v1 (:engine macro-rules))
             (contains? (set (:built-in-macros macro-rules)) 'defn)
             (empty? missing-steps))]
    {:artifact :gravity/p15-s23-stage2-source-front-end-rule-record
     :engine (:engine front-end)
     :implemented-by (:implemented-by front-end)
     :executed-by (:executed-by front-end)
     :front-end-steps (p15-s23-stage2-sort-values steps)
     :missing-steps (p15-s23-stage2-sort-values missing-steps)
     :reader-engine (:engine reader-rules)
     :macro-engine (:engine macro-rules)
     :rule-set-complete? complete?
     :status (if complete? :complete :failed)}))

(defn p15-s23-stage2-source-front-end-accepted-record
  [front-end emitter runtime]
  (let [source-path p15-s23-accepted-app-source-path
        source-text (slurp source-path)
        front-end-record
        (p15-s23-stage2-front-end-source-module-record
         front-end source-path source-text)
        module (:module front-end-record)
        stage2-plan (p15-s23-stage2-emitted-core-plan
                     emitter source-path source-text module)
        runtime-record
        (p15-s23-stage2-runtime-execute-plan runtime stage2-plan)
        reference-macro-artifact (macro-source-artifact source-path source-text)
        reference-module
        (assoc (:module reference-macro-artifact)
               :forms (:expanded-forms reference-macro-artifact))
        read-form-parity? (= (:forms front-end-record)
                             (read-forms source-path source-text))
        macro-expanded-form-parity?
        (= (:expanded-forms front-end-record)
           (:expanded-forms reference-macro-artifact))
        module-context-parity?
        (= (select-keys module
                        [:module :profile :target :effects
                         :capabilities :safety])
           (select-keys reference-module
                        [:module :profile :target :effects
                         :capabilities :safety]))
        syntax-count-parity?
        (= (count (:expanded-syntax-object-stream front-end-record))
           (count (:expanded-syntax-object-stream
                   reference-macro-artifact)))
        expected-stdout (get-in front-end
                                [:accepted-scope :expected-stdout])
        output-equivalent? (= expected-stdout (:stdout runtime-record))
        complete?
        (and read-form-parity?
             macro-expanded-form-parity?
             module-context-parity?
             syntax-count-parity?
             output-equivalent?)]
    {:artifact :gravity/p15-s23-stage2-source-front-end-accepted-record
     :fixture source-path
     :front-end-engine (:engine front-end)
     :source-id (:source-id front-end-record)
     :form-count (count (:forms front-end-record))
     :expanded-form-count (count (:expanded-forms front-end-record))
     :macro-expansion-count (count (:macro-expansion-trace
                                    front-end-record))
     :stage2-plan-id (:plan-id stage2-plan)
     :stage2-runtime-execution-record runtime-record
     :stage2-front-end-output (:stdout runtime-record)
     :expected-stdout expected-stdout
     :read-form-parity? read-form-parity?
     :macro-expanded-form-parity? macro-expanded-form-parity?
     :module-context-parity? module-context-parity?
     :syntax-count-parity? syntax-count-parity?
     :accepted-output-equivalent? output-equivalent?
     :stage2-front-end-record
     (select-keys front-end-record
                  [:artifact :source-path :source-id :status])
     :status (if complete? :complete :failed)}))

(defn p15-s23-stage2-source-front-end-rejected-records
  [front-end emitter runtime]
  (mapv
   (fn [{:keys [fixture expected-diagnostic rejected-design]}]
     (let [diagnostic
           (try
             (let [source-text (slurp fixture)
                   front-end-record
                   (p15-s23-stage2-front-end-source-module-record
                    front-end fixture source-text)
                   module (:module front-end-record)
                   stage2-plan
                   (p15-s23-stage2-emitted-core-plan
                    emitter fixture source-text module)]
               (p15-s23-stage2-runtime-execute-plan runtime stage2-plan)
               nil)
             (catch clojure.lang.ExceptionInfo ex
               (:id (ex-data ex))))]
       {:fixture fixture
        :rejected-design rejected-design
        :expected-diagnostic expected-diagnostic
        :status :rejected
        :diagnostic diagnostic
        :matches-expected? (= expected-diagnostic diagnostic)}))
   (:rejected-diagnostic-contract front-end)))

(defn p15-s23-stage2-source-front-end-rejected-record
  [records]
  (let [expected (set (map :expected-diagnostic records))
        observed (set (map :diagnostic records))
        mismatches (remove :matches-expected? records)
        matches? (and (= expected observed) (empty? mismatches))]
    {:artifact :gravity/p15-s23-stage2-source-front-end-rejected-record
     :expected-diagnostics (p15-s23-stage2-sort-values expected)
     :observed-diagnostics (p15-s23-stage2-sort-values observed)
     :fixture-count (count records)
     :mismatch-count (count mismatches)
     :records records
     :status (if matches? :complete :failed)}))

	(defn p15-s23-stage2-source-front-end-evidence-link-record
	  [front-end-executor-artifact nucleus-artifact plan-emitter-artifact
	   runtime-kernel-artifact runtime-artifact pipeline-artifact]
	  (let [links {:stage2-compiler-nucleus
               {:artifact (:kind nucleus-artifact)
                :artifact-id (:artifact-id nucleus-artifact)
                :present?
                (= :gravity/p15-s23-stage2-compiler-nucleus-artifact
                   (:kind nucleus-artifact))}
               :stage2-front-end-executor
               {:artifact (:kind front-end-executor-artifact)
                :artifact-id (:artifact-id front-end-executor-artifact)
                :present?
                (= :gravity/p15-s23-stage2-front-end-executor-artifact
                   (:kind front-end-executor-artifact))}
	               :stage2-plan-emitter
	               {:artifact (:kind plan-emitter-artifact)
                :artifact-id (:artifact-id plan-emitter-artifact)
                :present?
	                (= :gravity/p15-s23-stage2-plan-emitter-artifact
	                   (:kind plan-emitter-artifact))}
	               :stage2-runtime-kernel
	               {:artifact (:kind runtime-kernel-artifact)
	                :artifact-id (:artifact-id runtime-kernel-artifact)
	                :present?
	                (= :gravity/p15-s23-stage2-runtime-kernel-artifact
	                   (:kind runtime-kernel-artifact))}
	               :stage2-runtime-executor
               {:artifact (:kind runtime-artifact)
                :artifact-id (:artifact-id runtime-artifact)
                :present?
                (= :gravity/p15-s23-stage2-runtime-executor-artifact
                   (:kind runtime-artifact))}
               :compiler-pipeline-manifest
               {:artifact (:kind pipeline-artifact)
                :artifact-id (:artifact-id pipeline-artifact)
                :present?
                (= :gravity/p15-s23-compiler-pipeline-manifest-artifact
                   (:kind pipeline-artifact))}}
        missing (set (for [[k v] links :when (not (:present? v))] k))]
    {:artifact :gravity/p15-s23-stage2-source-front-end-evidence-link-record
     :present-links links
     :missing-links (p15-s23-stage2-sort-values missing)
     :all-required-links-present? (empty? missing)
     :status (if (empty? missing) :complete :failed)}))