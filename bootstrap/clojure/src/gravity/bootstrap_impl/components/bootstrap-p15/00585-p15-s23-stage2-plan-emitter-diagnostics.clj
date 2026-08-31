

(defn p15-s23-stage2-plan-emitter-diagnostics
  [source-path candidate]
  (let [emitter (:emitter-contract candidate)
        rule-record (:rule-record candidate)
        accepted-record (:accepted-record candidate)
        rejected-record (:rejected-record candidate)
        evidence-link-record (:evidence-link-record candidate)
        boundary-record (:boundary-record candidate)
        claims (:self-hosting-claims emitter)
        preserves (set (:preserves emitter))
        emits (set (:emits emitter))
        missing-preserves
        (set/difference p15-s23-stage2-plan-emitter-required-preserves
                        preserves)
        missing-emits
        (set/difference p15-s23-stage2-plan-emitter-required-emits emits)]
    (vec
     (concat
      (when-not (and (= :gravity/stage2-plan-emitter
                        (:artifact emitter))
                     (= :p15-s23-stage2-plan-emitter
                        (:stage emitter))
                     (= :gravity-source (:implemented-by emitter))
                     (= :clojure-stage0-rule-runner
                        (:executed-by emitter)))
        [(p15-s23-stage2-plan-emitter-diagnostic-record
          source-path "P15S23Q001" emitter
          {:missing-fields [:artifact :stage :implemented-by
                            :executed-by]})])
      (when-not (= :complete (:status rule-record))
        [(p15-s23-stage2-plan-emitter-diagnostic-record
          source-path "P15S23Q002" rule-record
          {:missing-special-forms (:missing-special-forms rule-record)
           :missing-builtin-functions
           (:missing-builtin-functions rule-record)})])
      (when-not (= :complete (:status accepted-record))
        [(p15-s23-stage2-plan-emitter-diagnostic-record
          source-path "P15S23Q003" accepted-record
          {:stage2-output (:stage2-output accepted-record)
           :stage0-output (:stage0-output accepted-record)
           :expected-stdout (:expected-stdout accepted-record)})])
      (when-not (= :complete (:status rejected-record))
        [(p15-s23-stage2-plan-emitter-diagnostic-record
          source-path "P15S23Q004" rejected-record
          {:expected-diagnostics (:expected-diagnostics rejected-record)
           :observed-diagnostics (:observed-diagnostics rejected-record)})])
      (when-not (= :complete (:status evidence-link-record))
        [(p15-s23-stage2-plan-emitter-diagnostic-record
          source-path "P15S23Q005" evidence-link-record
          {:missing-links (:missing-links evidence-link-record)})])
      (when (or (seq missing-preserves) (seq missing-emits))
        [(p15-s23-stage2-plan-emitter-diagnostic-record
          source-path "P15S23Q006" emitter
          {:missing-preserves (p15-s23-stage2-sort-values
                               missing-preserves)
           :missing-emits (p15-s23-stage2-sort-values missing-emits)})])
      (when-not (and (= :complete (:status boundary-record))
                     (true? (:stage0-plan-emitter-replaced?
                             boundary-record))
                     (true? (:stage2-plan-emitted-by-gravity-rules?
                             boundary-record))
                     (true? (:clojure-stage0-rule-runner?
                             boundary-record))
                     (true? (:clojure-instruction-runner?
                             boundary-record))
                     (false? (:full-language-compiler-self-hosted?
                              boundary-record))
                     (false? (:clojure-seed-retired?
                              boundary-record)))
        [(p15-s23-stage2-plan-emitter-diagnostic-record
          source-path "P15S23Q007" boundary-record
          {:required-boundary [:stage0-plan-emitter-replaced
                               :gravity-rule-authored-plan-emission
                               :clojure-stage0-rule-runner
                               :clojure-instruction-runner
                               :self-hosting-false
                               :clojure-seed-retired-false]})])
      (when (or (true? (:full-language-compiler-self-hosted? claims))
                (true? (:clojure-seed-retired? claims)))
        [(p15-s23-stage2-plan-emitter-diagnostic-record
          source-path "P15S23Q008" claims
          {:full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? claims)
           :clojure-seed-retired?
           (:clojure-seed-retired? claims)})])))))

(defn p15-s23-stage2-plan-emitter-diagnostic-stream
  [source-path proof-id]
  {:artifact :gravity/p15-s23-stage2-plan-emitter-diagnostic-stream
   :stage :p15-s23-stage2-plan-emitter
   :source-path source-path
   :proof-id proof-id
   :diagnostics
   (mapv (fn [id]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :p15-s23-stage2-plan-emitter
            :message
            (get p15-s23-stage2-plan-emitter-diagnostic-messages id)})
         p15-s23-stage2-plan-emitter-diagnostic-ids)
   :status :complete})

(defn p15-s23-stage2-plan-emitter-rejected-candidates
  [candidate]
  [{:fixture :internal-p15-s23-stage2-plan-emitter-missing-contract
    :candidate (assoc candidate :emitter-contract {})
    :expected-diagnostic "P15S23Q001"}
   {:fixture :internal-p15-s23-stage2-plan-emitter-rule-gap
    :candidate (assoc-in candidate [:rule-record :status] :failed)
    :expected-diagnostic "P15S23Q002"}
   {:fixture :internal-p15-s23-stage2-plan-emitter-output-gap
    :candidate (-> candidate
                   (assoc-in [:accepted-record :status] :failed)
                   (assoc-in [:accepted-record
                              :accepted-output-equivalent?]
                             false))
    :expected-diagnostic "P15S23Q003"}
   {:fixture :internal-p15-s23-stage2-plan-emitter-rejected-gap
    :candidate (-> candidate
                   (assoc-in [:rejected-record :status] :failed)
                   (assoc-in [:rejected-record :mismatch-count] 1))
    :expected-diagnostic "P15S23Q004"}
   {:fixture :internal-p15-s23-stage2-plan-emitter-evidence-gap
    :candidate (-> candidate
                   (assoc-in [:evidence-link-record :status] :failed)
                   (assoc-in [:evidence-link-record :missing-links]
                             [:stage2-compiler-nucleus]))
    :expected-diagnostic "P15S23Q005"}
   {:fixture :internal-p15-s23-stage2-plan-emitter-preservation-gap
    :candidate (assoc-in candidate [:emitter-contract :preserves] [])
    :expected-diagnostic "P15S23Q006"}
   {:fixture :internal-p15-s23-stage2-plan-emitter-boundary-gap
    :candidate (-> candidate
                   (assoc-in [:boundary-record
                              :stage0-plan-emitter-replaced?]
                             false)
                   (assoc-in [:boundary-record
                              :clojure-stage0-rule-runner?]
                             false))
    :expected-diagnostic "P15S23Q007"}
   {:fixture :internal-p15-s23-stage2-plan-emitter-overclaim
    :candidate (-> candidate
                   (assoc-in [:emitter-contract :self-hosting-claims
                              :full-language-compiler-self-hosted?]
                             true)
                   (assoc-in [:emitter-contract :self-hosting-claims
                              :clojure-seed-retired?]
                             true))
    :expected-diagnostic "P15S23Q008"}])

(defn p15-s23-stage2-plan-emitter-rejected-proof-records
  [source-path candidate]
  (mapv (fn [{:keys [fixture candidate expected-diagnostic]}]
          {:fixture fixture
           :status :rejected
           :expected-diagnostic expected-diagnostic
           :diagnostics
           (p15-s23-stage2-plan-emitter-diagnostics source-path
                                                    candidate)})
        (p15-s23-stage2-plan-emitter-rejected-candidates candidate)))

(defn p15-s23-stage2-plan-emitter-proof
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:p15-s23-stage2-plan-emitter-diagnostic-stream
                           :diagnostics])))
        rejected-diagnostics
        (set (mapcat #(map :diagnostic (:diagnostics %))
                     (:rejected-p15-s23-stage2-plan-emitter-fixtures
                      artifact)))
        accepted-record (:accepted-record artifact)
        rejected-record (:rejected-record artifact)
        boundary-record (:boundary-record artifact)]
    {:stage2-plan-emitter-authored-in-gravity? true
     :status :in-progress
     :task "P15-S23"
     :stage2-plan-emitter-present?
     (= :gravity/stage2-plan-emitter
        (get-in artifact [:emitter-contract :artifact]))
     :stage2-plan-emitted?
     (= :gravity/stage2-hosted-core-compiled-plan
        (:stage2-plan-kind accepted-record))
     :stage0-plan-emitter-replaced?
     (true? (:stage0-plan-emitter-replaced? boundary-record))
     :function-instructions-equivalent?
     (true? (:function-instructions-equivalent? accepted-record))
     :accepted-output-equivalent?
     (true? (:accepted-output-equivalent? accepted-record))
     :rejected-diagnostics-equivalent?
     (= :complete (:status rejected-record))
     :residual-clojure-rule-runner-recorded?
     (true? (:clojure-stage0-rule-runner? boundary-record))
     :residual-clojure-instruction-runner-recorded?
     (true? (:clojure-instruction-runner? boundary-record))
     :does-not-claim-full-self-hosting?
     (false? (:full-language-compiler-self-hosted? artifact))
     :does-not-claim-clojure-seed-retirement?
     (false? (:clojure-seed-retired? artifact))
     :rejected-candidates-covered?
     (= (set p15-s23-stage2-plan-emitter-diagnostic-ids)
        rejected-diagnostics)
     :diagnostics-covered?
     (= (set p15-s23-stage2-plan-emitter-diagnostic-ids)
        diagnostics)
     :limitations
     {:full-language-compiler-self-hosted? false
      :clojure-seed-retired? false
      :stage2-plan-emitter-authored-in-gravity? true
      :stage2-plan-emitter-executed? true
      :stage2-runtime-executed? false
      :clojure-stage0-rule-runner? true
      :clojure-instruction-runner? true
      :full-self-hosted-toolchain? false
      :next-required-capability
      :replace_stage0_instruction_runner_with_stage2_runtime_execution}}))