

	(defn p15-s23-stage2-runtime-kernel-rejected-plan-fixtures
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
	    [{:fixture :internal-stage2-runtime-kernel-function-arity-plan
	      :expected-diagnostic "L2-FUNCTION-ARITY"
	      :rejected-design :wrong-user-function-arity
	      :plan function-arity-plan}
	     {:fixture :internal-stage2-runtime-kernel-builtin-arity-plan
	      :expected-diagnostic "L2-BUILTIN-ARITY"
	      :rejected-design :wrong-builtin-arity
	      :plan builtin-arity-plan}]))

	(defn p15-s23-stage2-runtime-kernel-rejected-records
	  [kernel emitter]
	  (let [source-path p15-s23-accepted-app-source-path
	        source-text (slurp source-path)
	        stage2-plan
	        (p15-s23-stage2-plan-emitter-compile-source
	         emitter source-path source-text)]
	    (mapv
	     (fn [{:keys [fixture expected-diagnostic rejected-design plan]}]
	       (try
	         (let [kernel-output
	               (p15-s23-stage2-runtime-execute-plan kernel plan)]
	           {:fixture fixture
	            :rejected-design rejected-design
	            :expected-diagnostic expected-diagnostic
	            :status :accepted-unexpectedly
	            :kernel-output kernel-output
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
	     (p15-s23-stage2-runtime-kernel-rejected-plan-fixtures
	      stage2-plan))))

	(defn p15-s23-stage2-runtime-kernel-rejected-record
	  [records]
	  (let [expected (set (map :expected-diagnostic records))
	        observed (set (map :diagnostic records))
	        mismatches (remove :matches-expected? records)
	        matches? (and (= expected observed) (empty? mismatches))]
	    {:artifact :gravity/p15-s23-stage2-runtime-kernel-rejected-record
	     :expected-diagnostics (p15-s23-stage2-sort-values expected)
	     :observed-diagnostics (p15-s23-stage2-sort-values observed)
	     :fixture-count (count records)
	     :mismatch-count (count mismatches)
	     :records records
	     :status (if matches? :complete :failed)}))

	(defn p15-s23-stage2-runtime-kernel-evidence-link-record
	  [plan-emitter-artifact nucleus-artifact runtime-capability-artifact
	   accepted-artifact rejected-artifact]
	  (let [links {:stage2-plan-emitter
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
	               :runtime-manifest-and-capability-enforcement-report
	               {:artifact (:kind runtime-capability-artifact)
	                :artifact-id (:artifact-id runtime-capability-artifact)
	                :present?
	                (= :gravity/p15-s23-runtime-manifest-capability-enforcement-artifact
	                   (:kind runtime-capability-artifact))}
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
	    {:artifact :gravity/p15-s23-stage2-runtime-kernel-evidence-link-record
	     :present-links links
	     :missing-links (p15-s23-stage2-sort-values missing)
	     :all-required-links-present? (empty? missing)
	     :status (if (empty? missing) :complete :failed)}))

	(defn p15-s23-stage2-runtime-kernel-boundary-record
	  [kernel]
	  (let [claims (:self-hosting-claims kernel)
	        seed-boundary (:seed-boundary kernel)]
	    {:artifact :gravity/p15-s23-stage2-runtime-kernel-boundary-record
	     :implemented-by (:implemented-by kernel)
	     :executed-by (:executed-by kernel)
	     :verified-by (:verified-by kernel)
	     :compiled-by (:compiled-by kernel)
	     :stage2-runtime-kernel-used?
	     (= :gravity-stage2-runtime-kernel (:executed-by kernel))
	     :stage2-runtime-host-replaced?
	     (= :replaced-by-stage2-runtime-kernel
	        (:stage0-runtime-host-boundary seed-boundary))
	     :stage2-runtime-primitives-replaced?
	     (= :gravity-runtime-primitives
	        (:host-primitive-boundary seed-boundary))
	     :clojure-stage0-runtime-host?
	     (= :clojure-stage0-runtime-host (:executed-by kernel))
	     :clojure-host-primitive-boundary?
	     (= :clojure-runtime-primitives
	        (:host-primitive-boundary seed-boundary))
	     :gravity-runtime-primitives?
	     (= :gravity-runtime-primitives
	        (:host-primitive-boundary seed-boundary))
	     :clojure-stage0-verifier?
	     (= :clojure-stage0 (:verified-by kernel))
	     :clojure-stage0-compiler?
	     (= :clojure-stage0 (:compiled-by kernel))
	     :self-hosted-compiler? false
	     :full-language-compiler-self-hosted?
	     (:full-language-compiler-self-hosted? claims)
	     :clojure-seed-retired? (:clojure-seed-retired? claims)
	     :seed-boundary seed-boundary
	     :next-required-capability (:next-required-capability kernel)
	     :status :complete}))