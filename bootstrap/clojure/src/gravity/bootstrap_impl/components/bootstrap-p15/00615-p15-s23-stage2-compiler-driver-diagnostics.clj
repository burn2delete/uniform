

(defn p15-s23-stage2-compiler-driver-diagnostics
  [source-path candidate]
  (let [driver (:driver-contract candidate)
        rule-record (:rule-record candidate)
        accepted-record (:accepted-record candidate)
        rejected-record (:rejected-record candidate)
        evidence-link-record (:evidence-link-record candidate)
        boundary-record (:boundary-record candidate)
        claims (:self-hosting-claims driver)
        preserves (set (:preserves driver))
        emits (set (:emits driver))
        missing-preserves
        (set/difference
         p15-s23-stage2-compiler-driver-required-preserves
         preserves)
        missing-emits
        (set/difference p15-s23-stage2-compiler-driver-required-emits
                        emits)]
    (vec
     (concat
      (when-not (and (= :gravity/stage2-compiler-driver
                        (:artifact driver))
                     (= :p15-s23-stage2-compiler-driver
                        (:stage driver))
                     (= :gravity-source (:implemented-by driver))
                     (= :clojure-stage0-driver-host
                        (:executed-by driver)))
        [(p15-s23-stage2-compiler-driver-diagnostic-record
          source-path "P15S23Y001" driver
          {:missing-fields [:artifact :stage :implemented-by
                            :executed-by]})])
      (when-not (= :complete (:status rule-record))
        [(p15-s23-stage2-compiler-driver-diagnostic-record
          source-path "P15S23Y002" rule-record
          {:missing-steps (:missing-steps rule-record)
           :missing-uses (:missing-uses rule-record)})])
      (when-not (= :complete (:status accepted-record))
        [(p15-s23-stage2-compiler-driver-diagnostic-record
          source-path "P15S23Y003" accepted-record
          {:stage2-driver-output
           (:stage2-driver-output accepted-record)
           :stage0-reference-output
           (:stage0-reference-output accepted-record)
           :expected-stdout (:expected-stdout accepted-record)})])
      (when-not (= :complete (:status rejected-record))
        [(p15-s23-stage2-compiler-driver-diagnostic-record
          source-path "P15S23Y004" rejected-record
          {:expected-diagnostics (:expected-diagnostics rejected-record)
           :observed-diagnostics (:observed-diagnostics rejected-record)})])
      (when-not (= :complete (:status evidence-link-record))
        [(p15-s23-stage2-compiler-driver-diagnostic-record
          source-path "P15S23Y005" evidence-link-record
          {:missing-links (:missing-links evidence-link-record)})])
      (when (or (seq missing-preserves) (seq missing-emits))
        [(p15-s23-stage2-compiler-driver-diagnostic-record
          source-path "P15S23Y006" driver
          {:missing-preserves (p15-s23-stage2-sort-values
                               missing-preserves)
           :missing-emits (p15-s23-stage2-sort-values
                           missing-emits)})])
      (when-not (and (= :complete (:status boundary-record))
                     (true? (:stage0-compiler-driver-replaced?
                             boundary-record))
                     (true? (:stage0-rule-runner-replaced?
                             boundary-record))
                     (true? (:stage0-reader-replaced?
                             boundary-record))
                     (true? (:stage0-macro-expander-replaced?
                             boundary-record))
	                     (true? (:stage2-compiler-driver-executed?
	                             boundary-record))
	                     (true? (:stage2-front-end-executor-used?
	                             boundary-record))
	                     (true? (:stage2-front-end-host-replaced?
	                             boundary-record))
		                     (true? (:stage2-source-front-end-used?
		                             boundary-record))
	                     (true? (:stage2-runtime-kernel-used?
	                             boundary-record))
	                     (true? (:stage2-runtime-host-replaced?
	                             boundary-record))
	                     (true? (:stage2-runtime-primitives-replaced?
	                             boundary-record))
	                     (true? (:clojure-stage0-driver-host?
	                             boundary-record))
                     (false? (:clojure-stage0-rule-runner?
                              boundary-record))
                     (false? (:clojure-stage0-reader?
                              boundary-record))
                     (false? (:clojure-stage0-macro-expander?
                              boundary-record))
		                     (false? (:clojure-stage2-front-end-host?
		                              boundary-record))
	                     (false? (:clojure-stage0-runtime-host?
	                              boundary-record))
	                     (false? (:clojure-host-primitive-boundary?
	                              boundary-record))
	                     (true? (:gravity-runtime-primitives?
	                             boundary-record))
                     (false? (:full-language-compiler-self-hosted?
                              boundary-record))
                     (false? (:clojure-seed-retired?
                              boundary-record)))
        [(p15-s23-stage2-compiler-driver-diagnostic-record
          source-path "P15S23Y007" boundary-record
          {:required-boundary [:stage0-compiler-driver-replaced
                               :stage0-rule-runner-replaced
	                               :stage0-reader-replaced
	                               :stage0-macro-expander-replaced
	                               :stage2-compiler-driver-executed
	                               :stage2-front-end-executor-used
		                               :stage2-front-end-host-replaced
		                               :stage2-source-front-end-used
	                               :stage2-runtime-kernel-used
	                               :stage2-runtime-host-replaced
	                               :stage2-runtime-primitives-replaced
	                               :clojure-stage0-driver-host
                               :clojure-stage0-rule-runner-false
                               :clojure-stage0-reader-false
                               :clojure-stage0-macro-expander-false
		                               :clojure-stage2-front-end-host-false
	                               :clojure-runtime-host-false
	                               :clojure-primitives-false
	                               :gravity-runtime-primitives
	                               :self-hosting-false
                               :clojure-seed-retired-false]})])
      (when (or (true? (:full-language-compiler-self-hosted? claims))
                (true? (:clojure-seed-retired? claims)))
        [(p15-s23-stage2-compiler-driver-diagnostic-record
          source-path "P15S23Y008" claims
          {:full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? claims)
           :clojure-seed-retired?
           (:clojure-seed-retired? claims)})])))))

(defn p15-s23-stage2-compiler-driver-diagnostic-stream
  [source-path proof-id]
  {:artifact :gravity/p15-s23-stage2-compiler-driver-diagnostic-stream
   :stage :p15-s23-stage2-compiler-driver
   :source-path source-path
   :proof-id proof-id
   :diagnostics
   (mapv (fn [id]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :p15-s23-stage2-compiler-driver
            :message
            (get p15-s23-stage2-compiler-driver-diagnostic-messages id)})
         p15-s23-stage2-compiler-driver-diagnostic-ids)
   :status :complete})

(defn p15-s23-stage2-compiler-driver-rejected-candidates
  [candidate]
  [{:fixture :internal-p15-s23-stage2-compiler-driver-missing-contract
    :candidate (assoc candidate :driver-contract {})
    :expected-diagnostic "P15S23Y001"}
   {:fixture :internal-p15-s23-stage2-compiler-driver-step-gap
    :candidate (assoc-in candidate [:rule-record :status] :failed)
    :expected-diagnostic "P15S23Y002"}
   {:fixture :internal-p15-s23-stage2-compiler-driver-output-gap
    :candidate (-> candidate
                   (assoc-in [:accepted-record :status] :failed)
                   (assoc-in [:accepted-record
                              :accepted-output-equivalent?]
                             false))
    :expected-diagnostic "P15S23Y003"}
   {:fixture :internal-p15-s23-stage2-compiler-driver-rejected-gap
    :candidate (-> candidate
                   (assoc-in [:rejected-record :status] :failed)
                   (assoc-in [:rejected-record :mismatch-count] 1))
    :expected-diagnostic "P15S23Y004"}
   {:fixture :internal-p15-s23-stage2-compiler-driver-evidence-gap
    :candidate (-> candidate
	                   (assoc-in [:evidence-link-record :status] :failed)
	                   (assoc-in [:evidence-link-record :missing-links]
	                             [:stage2-runtime-kernel]))
    :expected-diagnostic "P15S23Y005"}
   {:fixture :internal-p15-s23-stage2-compiler-driver-preservation-gap
    :candidate (assoc-in candidate [:driver-contract :preserves] [])
    :expected-diagnostic "P15S23Y006"}
   {:fixture :internal-p15-s23-stage2-compiler-driver-boundary-gap
    :candidate (-> candidate
	                   (assoc-in [:boundary-record
	                              :stage2-front-end-host-replaced?]
	                             false)
	                   (assoc-in [:boundary-record
	                              :clojure-stage2-front-end-host?]
	                             true)
	                   (assoc-in [:boundary-record
	                              :stage2-runtime-host-replaced?]
	                             false)
	                   (assoc-in [:boundary-record
	                              :clojure-stage0-runtime-host?]
	                             true))
    :expected-diagnostic "P15S23Y007"}
   {:fixture :internal-p15-s23-stage2-compiler-driver-overclaim
    :candidate (-> candidate
                   (assoc-in [:driver-contract :self-hosting-claims
                              :full-language-compiler-self-hosted?]
                             true)
                   (assoc-in [:driver-contract :self-hosting-claims
                              :clojure-seed-retired?]
                             true))
    :expected-diagnostic "P15S23Y008"}])

(defn p15-s23-stage2-compiler-driver-rejected-proof-records
  [source-path candidate]
  (mapv (fn [{:keys [fixture candidate expected-diagnostic]}]
          {:fixture fixture
           :status :rejected
           :expected-diagnostic expected-diagnostic
           :diagnostics
           (p15-s23-stage2-compiler-driver-diagnostics source-path
                                                       candidate)})
        (p15-s23-stage2-compiler-driver-rejected-candidates
         candidate)))