

(defn p15-s23-stage2-source-front-end-boundary-record
  [front-end]
  (let [claims (:self-hosting-claims front-end)
        seed-boundary (:seed-boundary front-end)]
    {:artifact :gravity/p15-s23-stage2-source-front-end-boundary-record
     :implemented-by (:implemented-by front-end)
     :executed-by (:executed-by front-end)
     :compiled-by (get-in front-end [:lineage :compiled-by])
     :stage0-reader-replaced? true
     :stage0-macro-expander-replaced? true
     :stage2-source-front-end-executed? true
     :stage2-front-end-executor-used? true
	     :stage2-front-end-host-replaced?
	     (= :replaced-by-stage2-front-end-executor
	        (:stage2-front-end-host-boundary seed-boundary))
	     :stage2-runtime-kernel-used?
	     (= :p15-s23-stage2-runtime-kernel
	        (:runtime-kernel front-end))
	     :stage2-runtime-host-replaced?
	     (= :replaced-by-stage2-runtime-kernel
	        (:stage0-runtime-host-boundary seed-boundary))
	     :stage2-runtime-primitives-replaced?
	     (= :gravity-runtime-primitives
	        (:host-primitive-boundary seed-boundary))
	     :clojure-stage0-reader? false
     :clojure-stage0-macro-expander? false
     :clojure-stage0-front-end-reference?
     (= :clojure-stage0-front-end-reference
        (:stage0-front-end-reference-boundary seed-boundary))
     :clojure-stage2-front-end-host? false
     :clojure-stage0-runtime-host?
     (= :clojure-stage0-runtime-host
        (:stage0-runtime-host-boundary seed-boundary))
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
     :next-required-capability (:next-required-capability front-end)
     :status :complete}))

(defn p15-s23-stage2-source-front-end-diagnostics
  [source-path candidate]
  (let [front-end (:front-end-contract candidate)
        rule-record (:rule-record candidate)
        accepted-record (:accepted-record candidate)
        rejected-record (:rejected-record candidate)
        evidence-link-record (:evidence-link-record candidate)
        boundary-record (:boundary-record candidate)
        claims (:self-hosting-claims front-end)
        preserves (set (:preserves front-end))
        emits (set (:emits front-end))
        missing-preserves
        (set/difference
         p15-s23-stage2-source-front-end-required-preserves
         preserves)
        missing-emits
        (set/difference p15-s23-stage2-source-front-end-required-emits
                        emits)]
    (vec
     (concat
      (when-not (and (= :gravity/stage2-source-front-end
                        (:artifact front-end))
                     (= :p15-s23-stage2-source-front-end
                        (:stage front-end))
                     (= :gravity-source (:implemented-by front-end))
                     (= :gravity-stage2-front-end-executor
                        (:executed-by front-end)))
        [(p15-s23-stage2-source-front-end-diagnostic-record
          source-path "P15S23F001" front-end
          {:missing-fields [:artifact :stage :implemented-by
                            :executed-by]})])
      (when-not (= :complete (:status rule-record))
        [(p15-s23-stage2-source-front-end-diagnostic-record
          source-path "P15S23F002" rule-record
          {:missing-steps (:missing-steps rule-record)})])
      (when-not (= :gravity-stage2-macro-rules-v1
                   (get-in front-end [:macro-rules :engine]))
        [(p15-s23-stage2-source-front-end-diagnostic-record
          source-path "P15S23F003" (:macro-rules front-end)
          {:missing-fields [:macro-rules :engine]})])
      (when-not (= :complete (:status accepted-record))
        [(p15-s23-stage2-source-front-end-diagnostic-record
          source-path "P15S23F004" accepted-record
          {:expected-stdout (:expected-stdout accepted-record)
           :stage2-front-end-output
           (:stage2-front-end-output accepted-record)})])
      (when-not (= :complete (:status rejected-record))
        [(p15-s23-stage2-source-front-end-diagnostic-record
          source-path "P15S23F005" rejected-record
          {:expected-diagnostics (:expected-diagnostics rejected-record)
           :observed-diagnostics (:observed-diagnostics rejected-record)})])
      (when-not (= :complete (:status evidence-link-record))
        [(p15-s23-stage2-source-front-end-diagnostic-record
          source-path "P15S23F006" evidence-link-record
          {:missing-links (:missing-links evidence-link-record)})])
      (when (or (seq missing-preserves) (seq missing-emits))
        [(p15-s23-stage2-source-front-end-diagnostic-record
          source-path "P15S23F007" front-end
          {:missing-preserves (p15-s23-stage2-sort-values
                               missing-preserves)
           :missing-emits (p15-s23-stage2-sort-values
                           missing-emits)})])
      (when-not (and (= :complete (:status boundary-record))
                     (true? (:stage0-reader-replaced? boundary-record))
                     (true? (:stage0-macro-expander-replaced?
                             boundary-record))
                     (true? (:stage2-source-front-end-executed?
                             boundary-record))
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
	                     (false? (:clojure-stage0-reader? boundary-record))
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
        [(p15-s23-stage2-source-front-end-diagnostic-record
          source-path "P15S23F008" boundary-record
          {:required-boundary [:stage0-reader-replaced
                               :stage0-macro-expander-replaced
                               :stage2-source-front-end-executed
	                               :stage2-front-end-executor-used
	                               :stage2-front-end-host-replaced
	                               :stage2-runtime-kernel-used
	                               :stage2-runtime-host-replaced
	                               :stage2-runtime-primitives-replaced
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
        [(p15-s23-stage2-source-front-end-diagnostic-record
          source-path "P15S23F009" claims
          {:full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? claims)
           :clojure-seed-retired?
           (:clojure-seed-retired? claims)})])))))

(defn p15-s23-stage2-source-front-end-diagnostic-stream
  [source-path proof-id]
  {:artifact :gravity/p15-s23-stage2-source-front-end-diagnostic-stream
   :stage :p15-s23-stage2-source-front-end
   :source-path source-path
   :proof-id proof-id
   :diagnostics
   (mapv (fn [id]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :p15-s23-stage2-source-front-end
            :message
            (get p15-s23-stage2-source-front-end-diagnostic-messages id)})
         p15-s23-stage2-source-front-end-diagnostic-ids)
   :status :complete})