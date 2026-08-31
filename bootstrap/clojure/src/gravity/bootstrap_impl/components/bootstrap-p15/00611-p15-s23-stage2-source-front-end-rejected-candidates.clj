

(defn p15-s23-stage2-source-front-end-rejected-candidates
  [candidate]
  [{:fixture :internal-p15-s23-stage2-source-front-end-missing-contract
    :candidate (assoc candidate :front-end-contract {})
    :expected-diagnostic "P15S23F001"}
   {:fixture :internal-p15-s23-stage2-source-front-end-rule-gap
    :candidate (assoc-in candidate [:rule-record :status] :failed)
    :expected-diagnostic "P15S23F002"}
   {:fixture :internal-p15-s23-stage2-source-front-end-macro-gap
    :candidate (assoc-in candidate
                         [:front-end-contract :macro-rules :engine]
                         :missing)
    :expected-diagnostic "P15S23F003"}
   {:fixture :internal-p15-s23-stage2-source-front-end-output-gap
    :candidate (-> candidate
                   (assoc-in [:accepted-record :status] :failed)
                   (assoc-in [:accepted-record
                              :accepted-output-equivalent?]
                             false))
    :expected-diagnostic "P15S23F004"}
   {:fixture :internal-p15-s23-stage2-source-front-end-rejected-gap
    :candidate (-> candidate
                   (assoc-in [:rejected-record :status] :failed)
                   (assoc-in [:rejected-record :mismatch-count] 1))
    :expected-diagnostic "P15S23F005"}
   {:fixture :internal-p15-s23-stage2-source-front-end-evidence-gap
    :candidate (-> candidate
	                   (assoc-in [:evidence-link-record :status] :failed)
	                   (assoc-in [:evidence-link-record :missing-links]
	                             [:stage2-runtime-kernel]))
    :expected-diagnostic "P15S23F006"}
   {:fixture :internal-p15-s23-stage2-source-front-end-preservation-gap
    :candidate (assoc-in candidate
                         [:front-end-contract :preserves]
                         [])
    :expected-diagnostic "P15S23F007"}
   {:fixture :internal-p15-s23-stage2-source-front-end-boundary-gap
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
    :expected-diagnostic "P15S23F008"}
   {:fixture :internal-p15-s23-stage2-source-front-end-overclaim
    :candidate (-> candidate
                   (assoc-in [:front-end-contract :self-hosting-claims
                              :full-language-compiler-self-hosted?]
                             true)
                   (assoc-in [:front-end-contract :self-hosting-claims
                              :clojure-seed-retired?]
                             true))
    :expected-diagnostic "P15S23F009"}])

(defn p15-s23-stage2-source-front-end-rejected-proof-records
  [source-path candidate]
  (mapv (fn [{:keys [fixture candidate expected-diagnostic]}]
          {:fixture fixture
           :status :rejected
           :expected-diagnostic expected-diagnostic
           :diagnostics
           (p15-s23-stage2-source-front-end-diagnostics source-path
                                                        candidate)})
        (p15-s23-stage2-source-front-end-rejected-candidates
         candidate)))

(defn p15-s23-stage2-source-front-end-proof
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:p15-s23-stage2-source-front-end-diagnostic-stream
                           :diagnostics])))
        rejected-diagnostics
        (set (mapcat #(map :diagnostic (:diagnostics %))
                     (:rejected-p15-s23-stage2-source-front-end-fixtures
                      artifact)))
        accepted-record (:accepted-record artifact)
        rejected-record (:rejected-record artifact)
        boundary-record (:boundary-record artifact)]
    {:stage2-source-front-end-authored-in-gravity? true
     :status :in-progress
     :task "P15-S23"
     :stage2-source-front-end-present?
     (= :gravity/stage2-source-front-end
        (get-in artifact [:front-end-contract :artifact]))
     :stage2-source-front-end-executed?
     (true? (:stage2-source-front-end-executed? boundary-record))
     :stage0-reader-replaced?
     (true? (:stage0-reader-replaced? boundary-record))
     :stage0-macro-expander-replaced?
     (true? (:stage0-macro-expander-replaced? boundary-record))
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
	     :read-form-parity?
	     (true? (:read-form-parity? accepted-record))
     :macro-expanded-form-parity?
     (true? (:macro-expanded-form-parity? accepted-record))
     :accepted-output-equivalent?
     (true? (:accepted-output-equivalent? accepted-record))
     :rejected-diagnostics-equivalent?
     (= :complete (:status rejected-record))
     :does-not-use-clojure-stage0-reader?
     (false? (:clojure-stage0-reader? boundary-record))
     :does-not-use-clojure-stage0-macro-expander?
     (false? (:clojure-stage0-macro-expander? boundary-record))
	     :does-not-use-clojure-stage2-front-end-host?
	     (false? (:clojure-stage2-front-end-host? boundary-record))
	     :does-not-use-clojure-stage0-runtime-host?
	     (false? (:clojure-stage0-runtime-host? boundary-record))
	     :does-not-use-clojure-runtime-primitives?
	     (false? (:clojure-host-primitive-boundary? boundary-record))
     :does-not-claim-full-self-hosting?
     (false? (:full-language-compiler-self-hosted? artifact))
     :does-not-claim-clojure-seed-retirement?
     (false? (:clojure-seed-retired? artifact))
     :rejected-candidates-covered?
     (= (set p15-s23-stage2-source-front-end-diagnostic-ids)
        rejected-diagnostics)
     :diagnostics-covered?
     (= (set p15-s23-stage2-source-front-end-diagnostic-ids)
        diagnostics)
     :limitations
     {:full-language-compiler-self-hosted? false
      :clojure-seed-retired? false
      :stage2-source-front-end-authored-in-gravity? true
	      :stage2-source-front-end-executed? true
	      :stage2-front-end-executor-used? true
	      :stage2-front-end-host-replaced? true
	      :stage2-runtime-kernel-used? true
	      :stage2-runtime-host-replaced? true
	      :stage2-runtime-primitives-replaced? true
	      :gravity-runtime-primitives? true
	      :stage0-reader-replaced? true
      :stage0-macro-expander-replaced? true
      :clojure-stage0-reader? false
      :clojure-stage0-macro-expander? false
	      :clojure-stage0-front-end-reference? true
	      :clojure-stage2-front-end-host? false
	      :clojure-stage0-runtime-host? false
	      :clojure-host-primitive-boundary? false
	      :full-self-hosted-toolchain? false
	      :next-required-capability
	      :implement_whole_language_compiler_stage_without_clojure_seed}}))