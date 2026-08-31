

	(defn p15-s23-stage2-runtime-kernel-diagnostics
	  [source-path candidate]
	  (let [kernel (:kernel-contract candidate)
	        rule-record (:rule-record candidate)
	        accepted-record (:accepted-record candidate)
	        rejected-record (:rejected-record candidate)
	        evidence-link-record (:evidence-link-record candidate)
	        boundary-record (:boundary-record candidate)
	        claims (:self-hosting-claims kernel)
	        preserves (set (:preserves kernel))
	        emits (set (:emits kernel))
	        missing-preserves
	        (set/difference
	         p15-s23-stage2-runtime-kernel-required-preserves
	         preserves)
	        missing-emits
	        (set/difference
	         p15-s23-stage2-runtime-kernel-required-emits
	         emits)]
	    (vec
	     (concat
	      (when-not (and (= :gravity/stage2-runtime-kernel
	                        (:artifact kernel))
	                     (= :p15-s23-stage2-runtime-kernel
	                        (:stage kernel))
	                     (= :gravity-source (:implemented-by kernel))
	                     (= :gravity-stage2-runtime-kernel
	                        (:executed-by kernel))
	                     (= :stage2-instruction-plan (:input kernel))
	                     (= :stage2-runtime-kernel-execution-record
	                        (:output kernel)))
	        [(p15-s23-stage2-runtime-kernel-diagnostic-record
	          source-path "P15S23K001" kernel
	          {:missing-fields [:artifact :stage :implemented-by
	                            :executed-by :input :output]})])
	      (when-not (= :complete (:status rule-record))
	        [(p15-s23-stage2-runtime-kernel-diagnostic-record
	          source-path "P15S23K002" rule-record
	          {:missing-instructions (:missing-instructions rule-record)
	           :missing-runtime-primitives
	           (:missing-runtime-primitives rule-record)})])
	      (when-not (= :complete (:status accepted-record))
	        [(p15-s23-stage2-runtime-kernel-diagnostic-record
	          source-path "P15S23K003" accepted-record
	          {:stage2-runtime-kernel-output
	           (:stage2-runtime-kernel-output accepted-record)
	           :stage0-instruction-runner-output
	           (:stage0-instruction-runner-output accepted-record)
	           :expected-stdout (:expected-stdout accepted-record)})])
	      (when-not (= :complete (:status rejected-record))
	        [(p15-s23-stage2-runtime-kernel-diagnostic-record
	          source-path "P15S23K004" rejected-record
	          {:expected-diagnostics (:expected-diagnostics rejected-record)
	           :observed-diagnostics (:observed-diagnostics rejected-record)})])
	      (when-not (= :complete (:status evidence-link-record))
	        [(p15-s23-stage2-runtime-kernel-diagnostic-record
	          source-path "P15S23K005" evidence-link-record
	          {:missing-links (:missing-links evidence-link-record)})])
	      (when (or (seq missing-preserves) (seq missing-emits))
	        [(p15-s23-stage2-runtime-kernel-diagnostic-record
	          source-path "P15S23K006" kernel
	          {:missing-preserves
	           (p15-s23-stage2-sort-values missing-preserves)
	           :missing-emits
	           (p15-s23-stage2-sort-values missing-emits)})])
	      (when-not (and (= :complete (:status boundary-record))
	                     (true? (:stage2-runtime-kernel-used?
	                             boundary-record))
	                     (true? (:stage2-runtime-host-replaced?
	                             boundary-record))
	                     (true? (:stage2-runtime-primitives-replaced?
	                             boundary-record))
	                     (false? (:clojure-stage0-runtime-host?
	                              boundary-record))
	                     (false? (:clojure-host-primitive-boundary?
	                              boundary-record))
	                     (true? (:gravity-runtime-primitives?
	                             boundary-record))
	                     (true? (:clojure-stage0-verifier?
	                             boundary-record))
	                     (true? (:clojure-stage0-compiler?
	                             boundary-record))
	                     (false? (:full-language-compiler-self-hosted?
	                              boundary-record))
	                     (false? (:clojure-seed-retired?
	                              boundary-record)))
	        [(p15-s23-stage2-runtime-kernel-diagnostic-record
	          source-path "P15S23K007" boundary-record
	          {:required-boundary [:stage2-runtime-kernel-used
	                               :stage2-runtime-host-replaced
	                               :stage2-runtime-primitives-replaced
	                               :clojure-runtime-host-false
	                               :clojure-primitives-false
	                               :gravity-runtime-primitives
	                               :clojure-verifier-recorded
	                               :clojure-compiler-recorded
	                               :self-hosting-false
	                               :clojure-seed-retired-false]})])
	      (when (or (true? (:full-language-compiler-self-hosted? claims))
	                (true? (:clojure-seed-retired? claims)))
	        [(p15-s23-stage2-runtime-kernel-diagnostic-record
	          source-path "P15S23K008" claims
	          {:full-language-compiler-self-hosted?
	           (:full-language-compiler-self-hosted? claims)
	           :clojure-seed-retired?
	           (:clojure-seed-retired? claims)})])))))

	(defn p15-s23-stage2-runtime-kernel-diagnostic-stream
	  [source-path proof-id]
	  {:artifact :gravity/p15-s23-stage2-runtime-kernel-diagnostic-stream
	   :stage :p15-s23-stage2-runtime-kernel
	   :source-path source-path
	   :proof-id proof-id
	   :diagnostics
	   (mapv (fn [id]
	           {:artifact :gravity/diagnostic
	            :diagnostic-id (str "diag-" (str/lower-case id))
	            :diagnostic id
	            :severity :error
	            :stage :p15-s23-stage2-runtime-kernel
	            :message
	            (get p15-s23-stage2-runtime-kernel-diagnostic-messages
	                 id)})
	         p15-s23-stage2-runtime-kernel-diagnostic-ids)
	   :status :complete})

	(defn p15-s23-stage2-runtime-kernel-rejected-candidates
	  [candidate]
	  [{:fixture :internal-p15-s23-stage2-runtime-kernel-missing-contract
	    :candidate (assoc candidate :kernel-contract {})
	    :expected-diagnostic "P15S23K001"}
	   {:fixture :internal-p15-s23-stage2-runtime-kernel-rule-gap
	    :candidate (assoc-in candidate [:rule-record :status] :failed)
	    :expected-diagnostic "P15S23K002"}
	   {:fixture :internal-p15-s23-stage2-runtime-kernel-output-gap
	    :candidate (-> candidate
	                   (assoc-in [:accepted-record :status] :failed)
	                   (assoc-in [:accepted-record
	                              :accepted-output-equivalent?]
	                             false))
	    :expected-diagnostic "P15S23K003"}
	   {:fixture :internal-p15-s23-stage2-runtime-kernel-rejected-gap
	    :candidate (-> candidate
	                   (assoc-in [:rejected-record :status] :failed)
	                   (assoc-in [:rejected-record :mismatch-count] 1))
	    :expected-diagnostic "P15S23K004"}
	   {:fixture :internal-p15-s23-stage2-runtime-kernel-evidence-gap
	    :candidate (-> candidate
	                   (assoc-in [:evidence-link-record :status] :failed)
	                   (assoc-in [:evidence-link-record :missing-links]
	                             [:runtime-manifest-and-capability-enforcement-report]))
	    :expected-diagnostic "P15S23K005"}
	   {:fixture :internal-p15-s23-stage2-runtime-kernel-preservation-gap
	    :candidate (assoc-in candidate [:kernel-contract :preserves] [])
	    :expected-diagnostic "P15S23K006"}
	   {:fixture :internal-p15-s23-stage2-runtime-kernel-boundary-gap
	    :candidate (-> candidate
	                   (assoc-in [:boundary-record
	                              :stage2-runtime-host-replaced?]
	                             false)
	                   (assoc-in [:boundary-record
	                              :clojure-stage0-runtime-host?]
	                             true))
	    :expected-diagnostic "P15S23K007"}
	   {:fixture :internal-p15-s23-stage2-runtime-kernel-overclaim
	    :candidate (-> candidate
	                   (assoc-in [:kernel-contract :self-hosting-claims
	                              :full-language-compiler-self-hosted?]
	                             true)
	                   (assoc-in [:kernel-contract :self-hosting-claims
	                              :clojure-seed-retired?]
	                             true))
	    :expected-diagnostic "P15S23K008"}])

	(defn p15-s23-stage2-runtime-kernel-rejected-proof-records
	  [source-path candidate]
	  (mapv (fn [{:keys [fixture candidate expected-diagnostic]}]
	          {:fixture fixture
	           :status :rejected
	           :expected-diagnostic expected-diagnostic
	           :diagnostics
	           (p15-s23-stage2-runtime-kernel-diagnostics source-path
	                                                      candidate)})
	        (p15-s23-stage2-runtime-kernel-rejected-candidates
	         candidate)))