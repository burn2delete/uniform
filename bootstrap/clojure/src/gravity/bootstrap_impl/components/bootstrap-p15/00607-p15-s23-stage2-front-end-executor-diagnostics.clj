

(defn p15-s23-stage2-front-end-executor-diagnostics
  [source-path candidate]
  (let [executor (:executor-contract candidate)
        rule-record (:rule-record candidate)
        accepted-record (:accepted-record candidate)
        rejected-record (:rejected-record candidate)
        evidence-link-record (:evidence-link-record candidate)
        boundary-record (:boundary-record candidate)
        claims (:self-hosting-claims executor)
        preserves (set (:preserves executor))
        emits (set (:emits executor))
        missing-preserves
        (set/difference
         p15-s23-stage2-front-end-executor-required-preserves
         preserves)
        missing-emits
        (set/difference
         p15-s23-stage2-front-end-executor-required-emits
         emits)]
    (vec
     (concat
      (when-not (and (= :gravity/stage2-front-end-executor
                        (:artifact executor))
                     (= :p15-s23-stage2-front-end-executor
                        (:stage executor))
                     (= :gravity-source (:implemented-by executor))
                     (= :gravity-stage2-runtime-kernel
                        (:executed-by executor)))
        [(p15-s23-stage2-front-end-executor-diagnostic-record
          source-path "P15S23J001" executor
          {:missing-fields [:artifact :stage :implemented-by
                            :executed-by]})])
      (when-not (= :complete (:status rule-record))
        [(p15-s23-stage2-front-end-executor-diagnostic-record
          source-path "P15S23J002" rule-record
          {:missing-steps (:missing-steps rule-record)})])
      (when-not (= :complete (:status accepted-record))
        [(p15-s23-stage2-front-end-executor-diagnostic-record
          source-path "P15S23J003" accepted-record
          {:expected-stdout (:expected-stdout accepted-record)
           :stage2-front-end-executor-output
           (:stage2-front-end-executor-output accepted-record)})])
      (when-not (= :complete (:status rejected-record))
        [(p15-s23-stage2-front-end-executor-diagnostic-record
          source-path "P15S23J004" rejected-record
          {:expected-diagnostics (:expected-diagnostics rejected-record)
           :observed-diagnostics (:observed-diagnostics rejected-record)})])
      (when-not (= :complete (:status evidence-link-record))
        [(p15-s23-stage2-front-end-executor-diagnostic-record
          source-path "P15S23J005" evidence-link-record
          {:missing-links (:missing-links evidence-link-record)})])
      (when (or (seq missing-preserves) (seq missing-emits))
        [(p15-s23-stage2-front-end-executor-diagnostic-record
          source-path "P15S23J006" executor
          {:missing-preserves
           (p15-s23-stage2-sort-values missing-preserves)
           :missing-emits
           (p15-s23-stage2-sort-values missing-emits)})])
      (when-not (and (= :complete (:status boundary-record))
                     (true? (:stage2-front-end-executor-used?
                             boundary-record))
                     (true? (:stage2-front-end-host-replaced?
                             boundary-record))
                     (true? (:stage2-runtime-kernel-used?
                             boundary-record))
                     (true? (:stage2-runtime-host-replaced?
                             boundary-record))
                     (true? (:stage2-runtime-primitives-replaced?
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
        [(p15-s23-stage2-front-end-executor-diagnostic-record
          source-path "P15S23J007" boundary-record
          {:required-boundary [:stage2-front-end-executor-used
                               :stage2-front-end-host-replaced
                               :stage2-runtime-kernel-used
                               :stage2-runtime-host-replaced
                               :stage2-runtime-primitives-replaced
                               :clojure-stage2-front-end-host-false
                               :clojure-runtime-host-false
                               :clojure-primitives-false
                               :gravity-runtime-primitives
                               :self-hosting-false
                               :clojure-seed-retired-false]})])
      (when (or (true? (:full-language-compiler-self-hosted? claims))
                (true? (:clojure-seed-retired? claims)))
        [(p15-s23-stage2-front-end-executor-diagnostic-record
          source-path "P15S23J008" claims
          {:full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? claims)
           :clojure-seed-retired?
           (:clojure-seed-retired? claims)})])))))

(defn p15-s23-stage2-front-end-executor-diagnostic-stream
  [source-path proof-id]
  {:artifact
   :gravity/p15-s23-stage2-front-end-executor-diagnostic-stream
   :stage :p15-s23-stage2-front-end-executor
   :source-path source-path
   :proof-id proof-id
   :diagnostics
   (mapv (fn [id]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :p15-s23-stage2-front-end-executor
            :message
            (get p15-s23-stage2-front-end-executor-diagnostic-messages
                 id)})
         p15-s23-stage2-front-end-executor-diagnostic-ids)
   :status :complete})

(defn p15-s23-stage2-front-end-executor-rejected-candidates
  [candidate]
  [{:fixture :internal-p15-s23-stage2-front-end-executor-missing-contract
    :candidate (assoc candidate :executor-contract {})
    :expected-diagnostic "P15S23J001"}
   {:fixture :internal-p15-s23-stage2-front-end-executor-rule-gap
    :candidate (assoc-in candidate [:rule-record :status] :failed)
    :expected-diagnostic "P15S23J002"}
   {:fixture :internal-p15-s23-stage2-front-end-executor-output-gap
    :candidate (-> candidate
                   (assoc-in [:accepted-record :status] :failed)
                   (assoc-in [:accepted-record
                              :accepted-output-equivalent?]
                             false))
    :expected-diagnostic "P15S23J003"}
   {:fixture :internal-p15-s23-stage2-front-end-executor-rejected-gap
    :candidate (-> candidate
                   (assoc-in [:rejected-record :status] :failed)
                   (assoc-in [:rejected-record :mismatch-count] 1))
    :expected-diagnostic "P15S23J004"}
   {:fixture :internal-p15-s23-stage2-front-end-executor-evidence-gap
    :candidate (-> candidate
	                   (assoc-in [:evidence-link-record :status] :failed)
	                   (assoc-in [:evidence-link-record :missing-links]
	                             [:stage2-runtime-kernel]))
    :expected-diagnostic "P15S23J005"}
   {:fixture :internal-p15-s23-stage2-front-end-executor-preservation-gap
    :candidate (assoc-in candidate
                         [:executor-contract :preserves]
                         [])
    :expected-diagnostic "P15S23J006"}
   {:fixture :internal-p15-s23-stage2-front-end-executor-boundary-gap
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
    :expected-diagnostic "P15S23J007"}
   {:fixture :internal-p15-s23-stage2-front-end-executor-overclaim
    :candidate (-> candidate
                   (assoc-in [:executor-contract :self-hosting-claims
                              :full-language-compiler-self-hosted?]
                             true)
                   (assoc-in [:executor-contract :self-hosting-claims
                              :clojure-seed-retired?]
                             true))
    :expected-diagnostic "P15S23J008"}])

(defn p15-s23-stage2-front-end-executor-rejected-proof-records
  [source-path candidate]
  (mapv (fn [{:keys [fixture candidate expected-diagnostic]}]
          {:fixture fixture
           :status :rejected
           :expected-diagnostic expected-diagnostic
           :diagnostics
           (p15-s23-stage2-front-end-executor-diagnostics
            source-path candidate)})
        (p15-s23-stage2-front-end-executor-rejected-candidates
         candidate)))