

	(defn p15-s23-stage2-runtime-kernel-evidence
	  []
	  (let [source-path "bootstrap/gravity/p15_s23/compiler.gravity"]
	    (when (.isFile (java.io.File. source-path))
	      (try
	        (let [artifact
	              (p15-s23-stage2-runtime-kernel-file-artifact
	               source-path)
	              proof (:capability-based-proof artifact)]
	          {:status :verified
	           :artifact (:kind artifact)
	           :artifact-id (:artifact-id artifact)
	           :proof-id (:proof-id artifact)
	           :source-path source-path
	           :stage2-plan-id
	           (get-in artifact [:accepted-record :stage2-plan-id])
	           :stage2-runtime-kernel-executed?
	           (:stage2-runtime-kernel-executed? proof)
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
	           :does-not-use-clojure-stage0-runtime-host?
	           (:does-not-use-clojure-stage0-runtime-host? proof)
	           :does-not-use-clojure-runtime-primitives?
	           (:does-not-use-clojure-runtime-primitives? proof)
	           :residual-clojure-verifier-recorded?
	           (:residual-clojure-verifier-recorded? proof)
	           :residual-clojure-compiler-recorded?
	           (:residual-clojure-compiler-recorded? proof)
	           :full-language-compiler-self-hosted?
	           (:full-language-compiler-self-hosted? artifact)
	           :clojure-seed-retired? (:clojure-seed-retired? artifact)})
	        (catch Exception _
	          nil)))))

	(defn p15-s23-stage2-runtime-executor-accepted-record
	  [runtime emitter]
  (let [source-path p15-s23-accepted-app-source-path
        source-text (slurp source-path)
        stage2-plan
        (p15-s23-stage2-plan-emitter-compile-source
         emitter source-path source-text)
        runtime-output
        (p15-s23-stage2-runtime-execute-plan runtime stage2-plan)
        stage0-output (execute-stage0-compiled-plan stage2-plan)
        expected-stdout (get-in runtime
                                [:accepted-scope :expected-stdout])
        accepted-output-equivalent?
        (and (= (:stdout runtime-output) stage0-output)
             (= (:stdout runtime-output) expected-stdout))
        entrypoint-result-equivalent?
        (nil? (:entrypoint-result runtime-output))
        summary-equivalent?
        (= (:instruction-summary runtime-output)
           (:instruction-summary stage2-plan))
        effect-summary-equivalent?
        (= (:effect-summary runtime-output)
           (:effect-summary stage2-plan))
        equivalent?
        (and accepted-output-equivalent?
             entrypoint-result-equivalent?
             summary-equivalent?
             effect-summary-equivalent?)]
    {:artifact :gravity/p15-s23-stage2-runtime-executor-accepted-record
     :fixture source-path
     :stage2-plan-id (:plan-id stage2-plan)
     :stage2-plan-kind (:kind stage2-plan)
     :entrypoint (:entrypoint stage2-plan)
     :runtime-execution-record runtime-output
     :stage2-runtime-executed? true
     :stage0-instruction-runner-output stage0-output
     :stage2-runtime-output (:stdout runtime-output)
     :expected-stdout expected-stdout
     :entrypoint-result-equivalent? entrypoint-result-equivalent?
     :instruction-summary-equivalent? summary-equivalent?
     :effect-summary-equivalent? effect-summary-equivalent?
     :accepted-output-equivalent? accepted-output-equivalent?
     :status (if equivalent? :complete :failed)}))

(defn p15-s23-stage2-runtime-executor-rejected-plan-fixtures
  [stage2-plan]
  (let [function-arity-plan
        (assoc-in stage2-plan
                  [:functions 'main :instructions]
                  [{:op :function-call
                    :function 'total
                    :args [{:op :literal :value 5}]}])
        builtin-arity-plan
        (assoc-in stage2-plan
                  [:functions 'main :instructions]
                  [{:op :builtin-call
                    :function 'assoc
                    :args [{:op :map-literal :entries []}
                           {:op :quote :value :missing-value}]}])]
    [{:fixture :internal-stage2-runtime-function-arity-plan
      :expected-diagnostic "L2-FUNCTION-ARITY"
      :rejected-design :wrong-user-function-arity
      :plan function-arity-plan}
     {:fixture :internal-stage2-runtime-builtin-arity-plan
      :expected-diagnostic "L2-BUILTIN-ARITY"
      :rejected-design :wrong-builtin-arity
      :plan builtin-arity-plan}]))

(defn p15-s23-stage2-runtime-executor-rejected-records
  [runtime emitter]
  (let [source-path p15-s23-accepted-app-source-path
        source-text (slurp source-path)
        stage2-plan
        (p15-s23-stage2-plan-emitter-compile-source
         emitter source-path source-text)]
    (mapv
     (fn [{:keys [fixture expected-diagnostic rejected-design plan]}]
       (try
         (let [runtime-output
               (p15-s23-stage2-runtime-execute-plan runtime plan)]
           {:fixture fixture
            :rejected-design rejected-design
            :expected-diagnostic expected-diagnostic
            :status :accepted-unexpectedly
            :runtime-output runtime-output
            :plan-id (:plan-id plan)
            :matches-expected? false})
         (catch clojure.lang.ExceptionInfo ex
           (let [data (ex-data ex)
                 diagnostic (:id data)]
             {:fixture fixture
              :rejected-design rejected-design
              :expected-diagnostic expected-diagnostic
              :status :rejected
              :diagnostic diagnostic
              :message (:message data)
              :diagnostic-data data
              :plan-id
              (str "sha256:" (sha256-hex (pr-str plan)))
              :matches-expected?
              (= expected-diagnostic diagnostic)}))))
     (p15-s23-stage2-runtime-executor-rejected-plan-fixtures
      stage2-plan))))

(defn p15-s23-stage2-runtime-executor-rejected-record
  [records]
  (let [expected (set (map :expected-diagnostic records))
        observed (set (map :diagnostic records))
        mismatches (remove :matches-expected? records)
        matches? (and (= expected observed) (empty? mismatches))]
    {:artifact :gravity/p15-s23-stage2-runtime-executor-rejected-record
     :expected-diagnostics (p15-s23-stage2-sort-values expected)
     :observed-diagnostics (p15-s23-stage2-sort-values observed)
     :fixture-count (count records)
     :mismatch-count (count mismatches)
     :records records
     :status (if matches? :complete :failed)}))

	(defn p15-s23-stage2-runtime-executor-evidence-link-record
	  [runtime-kernel-artifact plan-emitter-artifact nucleus-artifact
	   accepted-artifact rejected-artifact]
	  (let [links {:stage2-runtime-kernel
	               {:artifact (:kind runtime-kernel-artifact)
	                :artifact-id (:artifact-id runtime-kernel-artifact)
	                :present?
	                (= :gravity/p15-s23-stage2-runtime-kernel-artifact
	                   (:kind runtime-kernel-artifact))}
	               :stage2-plan-emitter
	               {:artifact (:kind plan-emitter-artifact)
	                :artifact-id (:artifact-id plan-emitter-artifact)
                :present?
                (= :gravity/p15-s23-stage2-plan-emitter-artifact
                   (:kind plan-emitter-artifact))}
               :stage2-compiler-nucleus
               {:artifact (:kind nucleus-artifact)
                :artifact-id (:artifact-id nucleus-artifact)
                :present?
                (= :gravity/p15-s23-stage2-compiler-nucleus-artifact
                   (:kind nucleus-artifact))}
               :accepted-app-execution-proof
               {:artifact (:kind accepted-artifact)
                :artifact-id (:artifact-id accepted-artifact)
                :present?
                (= :gravity/p15-s23-accepted-app-execution-artifact
                   (:kind accepted-artifact))}
               :rejected-app-diagnostic-proof
               {:artifact (:kind rejected-artifact)
                :artifact-id (:artifact-id rejected-artifact)
                :present?
                (= :gravity/p15-s23-rejected-app-diagnostic-artifact
                   (:kind rejected-artifact))}}
        missing (set (for [[k v] links :when (not (:present? v))] k))]
    {:artifact :gravity/p15-s23-stage2-runtime-executor-evidence-link-record
     :present-links links
     :missing-links (p15-s23-stage2-sort-values missing)
     :all-required-links-present? (empty? missing)
     :status (if (empty? missing) :complete :failed)}))

(defn p15-s23-stage2-runtime-executor-boundary-record
  [runtime]
  (let [claims (:self-hosting-claims runtime)
        seed-boundary (:seed-boundary runtime)]
    {:artifact :gravity/p15-s23-stage2-runtime-executor-boundary-record
     :implemented-by (:implemented-by runtime)
     :executed-by (:executed-by runtime)
     :compiled-by (get-in runtime [:lineage :compiled-by])
     :stage0-instruction-runner-replaced? true
     :stage2-runtime-executed? true
	     :stage2-runtime-authored-in-gravity?
	     (= :gravity-source (:implemented-by runtime))
	     :stage2-runtime-kernel-used?
	     (= :gravity-stage2-runtime-kernel (:executed-by runtime))
	     :stage2-runtime-host-replaced?
	     (= :replaced-by-stage2-runtime-kernel
	        (:stage0-runtime-host-boundary seed-boundary))
	     :stage2-runtime-primitives-replaced?
	     (= :gravity-runtime-primitives
	        (:host-primitive-boundary seed-boundary))
	     :clojure-instruction-runner? false
	     :clojure-stage0-runtime-host?
	     (= :clojure-stage0-runtime-host (:executed-by runtime))
     :clojure-stage0-rule-runner?
     (= :clojure-stage0-rule-runner
        (:stage0-rule-runner-boundary seed-boundary))
	     :clojure-host-primitive-boundary?
	     (= :clojure-runtime-primitives
	        (:host-primitive-boundary seed-boundary))
	     :gravity-runtime-primitives?
	     (= :gravity-runtime-primitives
	        (:host-primitive-boundary seed-boundary))
     :self-hosted-compiler? false
     :full-language-compiler-self-hosted?
     (:full-language-compiler-self-hosted? claims)
     :clojure-seed-retired? (:clojure-seed-retired? claims)
     :seed-boundary seed-boundary
     :next-required-capability (:next-required-capability runtime)
     :status :complete}))