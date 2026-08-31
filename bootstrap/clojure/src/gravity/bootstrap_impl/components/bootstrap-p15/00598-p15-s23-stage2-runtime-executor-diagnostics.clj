

(defn p15-s23-stage2-runtime-executor-diagnostics
  [source-path candidate]
  (let [runtime (:runtime-contract candidate)
        rule-record (:rule-record candidate)
        accepted-record (:accepted-record candidate)
        rejected-record (:rejected-record candidate)
        evidence-link-record (:evidence-link-record candidate)
        boundary-record (:boundary-record candidate)
        claims (:self-hosting-claims runtime)
        preserves (set (:preserves runtime))
        emits (set (:emits runtime))
        missing-preserves
        (set/difference
         p15-s23-stage2-runtime-executor-required-preserves
         preserves)
        missing-emits
        (set/difference p15-s23-stage2-runtime-executor-required-emits
                        emits)]
    (vec
     (concat
      (when-not (and (= :gravity/stage2-runtime-executor
                        (:artifact runtime))
                     (= :p15-s23-stage2-runtime-executor
                        (:stage runtime))
                     (= :gravity-source (:implemented-by runtime))
	                     (= :gravity-stage2-runtime-kernel
	                        (:executed-by runtime)))
        [(p15-s23-stage2-runtime-executor-diagnostic-record
          source-path "P15S23X001" runtime
	          {:missing-fields [:artifact :stage :implemented-by
	                            :executed-by]})])
      (when-not (= :complete (:status rule-record))
        [(p15-s23-stage2-runtime-executor-diagnostic-record
          source-path "P15S23X002" rule-record
          {:missing-instructions (:missing-instructions rule-record)
           :missing-builtin-functions
           (:missing-builtin-functions rule-record)})])
      (when-not (= :complete (:status accepted-record))
        [(p15-s23-stage2-runtime-executor-diagnostic-record
          source-path "P15S23X003" accepted-record
          {:stage2-runtime-output
           (:stage2-runtime-output accepted-record)
           :stage0-instruction-runner-output
           (:stage0-instruction-runner-output accepted-record)
           :expected-stdout (:expected-stdout accepted-record)})])
      (when-not (= :complete (:status rejected-record))
        [(p15-s23-stage2-runtime-executor-diagnostic-record
          source-path "P15S23X004" rejected-record
          {:expected-diagnostics (:expected-diagnostics rejected-record)
           :observed-diagnostics (:observed-diagnostics rejected-record)})])
      (when-not (= :complete (:status evidence-link-record))
        [(p15-s23-stage2-runtime-executor-diagnostic-record
          source-path "P15S23X005" evidence-link-record
          {:missing-links (:missing-links evidence-link-record)})])
      (when (or (seq missing-preserves) (seq missing-emits))
        [(p15-s23-stage2-runtime-executor-diagnostic-record
          source-path "P15S23X006" runtime
          {:missing-preserves (p15-s23-stage2-sort-values
                               missing-preserves)
           :missing-emits (p15-s23-stage2-sort-values
                           missing-emits)})])
      (when-not (and (= :complete (:status boundary-record))
                     (true? (:stage0-instruction-runner-replaced?
                             boundary-record))
	                     (true? (:stage2-runtime-executed?
	                             boundary-record))
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
	                     (true? (:clojure-stage0-rule-runner?
	                             boundary-record))
                     (false? (:clojure-instruction-runner?
                              boundary-record))
                     (false? (:full-language-compiler-self-hosted?
                              boundary-record))
                     (false? (:clojure-seed-retired?
                              boundary-record)))
        [(p15-s23-stage2-runtime-executor-diagnostic-record
          source-path "P15S23X007" boundary-record
	          {:required-boundary [:stage0-instruction-runner-replaced
	                               :stage2-runtime-executed
	                               :stage2-runtime-kernel-used
	                               :stage2-runtime-host-replaced
	                               :stage2-runtime-primitives-replaced
	                               :clojure-runtime-host-false
	                               :clojure-primitives-false
	                               :gravity-runtime-primitives
	                               :clojure-stage0-rule-runner
	                               :clojure-instruction-runner-false
                               :self-hosting-false
                               :clojure-seed-retired-false]})])
      (when (or (true? (:full-language-compiler-self-hosted? claims))
                (true? (:clojure-seed-retired? claims)))
        [(p15-s23-stage2-runtime-executor-diagnostic-record
          source-path "P15S23X008" claims
          {:full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? claims)
           :clojure-seed-retired?
           (:clojure-seed-retired? claims)})])))))

(defn p15-s23-stage2-runtime-executor-diagnostic-stream
  [source-path proof-id]
  {:artifact :gravity/p15-s23-stage2-runtime-executor-diagnostic-stream
   :stage :p15-s23-stage2-runtime-executor
   :source-path source-path
   :proof-id proof-id
   :diagnostics
   (mapv (fn [id]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :p15-s23-stage2-runtime-executor
            :message
            (get p15-s23-stage2-runtime-executor-diagnostic-messages id)})
         p15-s23-stage2-runtime-executor-diagnostic-ids)
   :status :complete})

(defn p15-s23-stage2-runtime-executor-rejected-candidates
  [candidate]
  [{:fixture :internal-p15-s23-stage2-runtime-executor-missing-contract
    :candidate (assoc candidate :runtime-contract {})
    :expected-diagnostic "P15S23X001"}
   {:fixture :internal-p15-s23-stage2-runtime-executor-rule-gap
    :candidate (assoc-in candidate [:rule-record :status] :failed)
    :expected-diagnostic "P15S23X002"}
   {:fixture :internal-p15-s23-stage2-runtime-executor-output-gap
    :candidate (-> candidate
                   (assoc-in [:accepted-record :status] :failed)
                   (assoc-in [:accepted-record
                              :accepted-output-equivalent?]
                             false))
    :expected-diagnostic "P15S23X003"}
   {:fixture :internal-p15-s23-stage2-runtime-executor-rejected-gap
    :candidate (-> candidate
                   (assoc-in [:rejected-record :status] :failed)
                   (assoc-in [:rejected-record :mismatch-count] 1))
    :expected-diagnostic "P15S23X004"}
	   {:fixture :internal-p15-s23-stage2-runtime-executor-evidence-gap
	    :candidate (-> candidate
	                   (assoc-in [:evidence-link-record :status] :failed)
	                   (assoc-in [:evidence-link-record :missing-links]
	                             [:stage2-runtime-kernel]))
    :expected-diagnostic "P15S23X005"}
   {:fixture :internal-p15-s23-stage2-runtime-executor-preservation-gap
    :candidate (assoc-in candidate [:runtime-contract :preserves] [])
    :expected-diagnostic "P15S23X006"}
   {:fixture :internal-p15-s23-stage2-runtime-executor-boundary-gap
    :candidate (-> candidate
	                   (assoc-in [:boundary-record
	                              :stage0-instruction-runner-replaced?]
	                             false)
	                   (assoc-in [:boundary-record
	                              :stage2-runtime-host-replaced?]
	                             false)
	                   (assoc-in [:boundary-record
	                              :clojure-stage0-runtime-host?]
	                             true))
    :expected-diagnostic "P15S23X007"}
   {:fixture :internal-p15-s23-stage2-runtime-executor-overclaim
    :candidate (-> candidate
                   (assoc-in [:runtime-contract :self-hosting-claims
                              :full-language-compiler-self-hosted?]
                             true)
                   (assoc-in [:runtime-contract :self-hosting-claims
                              :clojure-seed-retired?]
                             true))
    :expected-diagnostic "P15S23X008"}])

(defn p15-s23-stage2-runtime-executor-rejected-proof-records
  [source-path candidate]
  (mapv (fn [{:keys [fixture candidate expected-diagnostic]}]
          {:fixture fixture
           :status :rejected
           :expected-diagnostic expected-diagnostic
           :diagnostics
           (p15-s23-stage2-runtime-executor-diagnostics source-path
                                                        candidate)})
        (p15-s23-stage2-runtime-executor-rejected-candidates
         candidate)))