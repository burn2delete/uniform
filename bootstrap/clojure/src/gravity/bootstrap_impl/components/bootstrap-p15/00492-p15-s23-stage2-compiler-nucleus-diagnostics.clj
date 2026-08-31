

(defn p15-s23-stage2-compiler-nucleus-diagnostics
  [source-path candidate]
  (let [nucleus (:nucleus-contract candidate)
        accepted-plan-record (:accepted-plan-record candidate)
        rejected-diagnostic-record (:rejected-diagnostic-record candidate)
        evidence-link-record (:evidence-link-record candidate)
        boundary-record (:boundary-record candidate)
        claims (:self-hosting-claims nucleus)
        preserves (set (:preserves nucleus))
        emits (set (:emits nucleus))
        stage-preserves (set (get-in nucleus [:nucleus-stage :preserves]))
        stage-emits (set (get-in nucleus [:nucleus-stage :emits]))
        missing-preserves
        (set/difference p15-s23-stage2-compiler-nucleus-required-preserves
                        (set/union preserves stage-preserves))
        missing-emits
        (set/difference p15-s23-stage2-compiler-nucleus-required-emits
                        (set/union emits stage-emits))]
    (vec
     (concat
      (when-not (and (= :gravity/stage2-compiler-nucleus
                        (:artifact nucleus))
                     (= :p15-s23-stage2-compiler-nucleus
                        (:stage nucleus))
                     (= :gravity-source (:implemented-by nucleus))
                     (= :hosted-core-compiled-plan-emission
                        (:module-responsibility nucleus)))
        [(p15-s23-stage2-compiler-nucleus-diagnostic-record
          source-path "P15S23N001" nucleus
          {:missing-fields [:artifact :stage :implemented-by
                            :module-responsibility]})])
      (when-not (= :complete (:status accepted-plan-record))
        [(p15-s23-stage2-compiler-nucleus-diagnostic-record
          source-path "P15S23N002" accepted-plan-record
          {:missing-op-families
           (:missing-op-families accepted-plan-record)
           :missing-user-functions
           (:missing-user-functions accepted-plan-record)
           :expected-stdout p15-s23-accepted-app-expected-stdout})])
      (when-not (= :complete (:status rejected-diagnostic-record))
        [(p15-s23-stage2-compiler-nucleus-diagnostic-record
          source-path "P15S23N003" rejected-diagnostic-record
          {:required-diagnostics
           (mapv :expected-diagnostic p15-s23-rejected-app-fixtures)})])
      (when-not (= :complete (:status evidence-link-record))
        [(p15-s23-stage2-compiler-nucleus-diagnostic-record
          source-path "P15S23N004" evidence-link-record
          {:missing-links (:missing-links evidence-link-record)})])
      (when (or (seq missing-preserves) (seq missing-emits))
        [(p15-s23-stage2-compiler-nucleus-diagnostic-record
          source-path "P15S23N005" nucleus
          {:missing-preserves (p15-s23-stage2-sort-values
                               missing-preserves)
           :missing-emits (p15-s23-stage2-sort-values missing-emits)})])
      (when-not (and (= :complete (:status boundary-record))
                     (true? (:clojure-stage0-verifier? boundary-record))
                     (true? (:clojure-stage0-compiler? boundary-record))
                     (true? (:clojure-instruction-runner?
                             boundary-record))
                     (false? (:full-language-compiler-self-hosted?
                              boundary-record))
                     (false? (:clojure-seed-retired?
                              boundary-record)))
        [(p15-s23-stage2-compiler-nucleus-diagnostic-record
          source-path "P15S23N006" boundary-record
          {:required-boundary [:clojure-stage0-verifier
                               :clojure-stage0-compiler
                               :clojure-instruction-runner
                               :self-hosting-false
                               :clojure-seed-retired-false]})])
      (when (or (true? (:full-language-compiler-self-hosted? claims))
                (true? (:clojure-seed-retired? claims)))
        [(p15-s23-stage2-compiler-nucleus-diagnostic-record
          source-path "P15S23N007" claims
          {:full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? claims)
           :clojure-seed-retired?
           (:clojure-seed-retired? claims)})])))))

(defn p15-s23-stage2-compiler-nucleus-diagnostic-stream
  [source-path proof-id]
  {:artifact :gravity/p15-s23-stage2-compiler-nucleus-diagnostic-stream
   :stage :p15-s23-stage2-compiler-nucleus
   :source-path source-path
   :proof-id proof-id
   :diagnostics
   (mapv (fn [id]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :p15-s23-stage2-compiler-nucleus
            :message
            (get p15-s23-stage2-compiler-nucleus-diagnostic-messages
                 id)})
         p15-s23-stage2-compiler-nucleus-diagnostic-ids)
   :status :complete})

(defn p15-s23-stage2-compiler-nucleus-rejected-candidates
  [candidate]
  [{:fixture :internal-p15-s23-stage2-nucleus-missing-contract
    :candidate (assoc candidate :nucleus-contract {})
    :expected-diagnostic "P15S23N001"}
   {:fixture :internal-p15-s23-stage2-nucleus-plan-gap
    :candidate (-> candidate
                   (assoc-in [:accepted-plan-record :status] :failed)
                   (assoc-in [:accepted-plan-record
                              :missing-op-families]
                             [:println]))
    :expected-diagnostic "P15S23N002"}
   {:fixture :internal-p15-s23-stage2-nucleus-rejected-diagnostic-gap
    :candidate (-> candidate
                   (assoc-in [:rejected-diagnostic-record :status]
                             :failed)
                   (assoc-in [:rejected-diagnostic-record
                              :mismatch-count]
                             1))
    :expected-diagnostic "P15S23N003"}
   {:fixture :internal-p15-s23-stage2-nucleus-evidence-link-gap
    :candidate (-> candidate
                   (assoc-in [:evidence-link-record :status] :failed)
                   (assoc-in [:evidence-link-record :missing-links]
                             [:accepted-app-execution-proof]))
    :expected-diagnostic "P15S23N004"}
   {:fixture :internal-p15-s23-stage2-nucleus-preservation-gap
    :candidate (-> candidate
                   (assoc-in [:nucleus-contract :preserves] [])
                   (assoc-in [:nucleus-contract :nucleus-stage
                              :preserves]
                             []))
    :expected-diagnostic "P15S23N005"}
   {:fixture :internal-p15-s23-stage2-nucleus-boundary-gap
    :candidate (-> candidate
                   (assoc-in [:boundary-record
                              :clojure-stage0-verifier?]
                             false)
                   (assoc-in [:boundary-record
                              :clojure-instruction-runner?]
                             false))
    :expected-diagnostic "P15S23N006"}
   {:fixture :internal-p15-s23-stage2-nucleus-overclaim
    :candidate (-> candidate
                   (assoc-in [:nucleus-contract :self-hosting-claims
                              :full-language-compiler-self-hosted?]
                             true)
                   (assoc-in [:nucleus-contract :self-hosting-claims
                              :clojure-seed-retired?]
                             true))
    :expected-diagnostic "P15S23N007"}])

(defn p15-s23-stage2-compiler-nucleus-rejected-records
  [source-path candidate]
  (mapv (fn [{:keys [fixture candidate expected-diagnostic]}]
          {:fixture fixture
           :status :rejected
           :expected-diagnostic expected-diagnostic
           :diagnostics
           (p15-s23-stage2-compiler-nucleus-diagnostics
            source-path candidate)})
        (p15-s23-stage2-compiler-nucleus-rejected-candidates
         candidate)))

(defn p15-s23-stage2-compiler-nucleus-proof
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:p15-s23-stage2-compiler-nucleus-diagnostic-stream
                           :diagnostics])))
        rejected-diagnostics
        (set (mapcat #(map :diagnostic (:diagnostics %))
                     (:rejected-p15-s23-stage2-compiler-nucleus-fixtures
                      artifact)))
        accepted-plan-record (:accepted-plan-record artifact)
        rejected-diagnostic-record (:rejected-diagnostic-record artifact)
        evidence-link-record (:evidence-link-record artifact)
        boundary-record (:boundary-record artifact)]
    {:stage2-compiler-nucleus-authored-in-gravity? true
     :status :in-progress
     :task "P15-S23"
     :gravity-authored-stage2-nucleus-present?
     (= :gravity/stage2-compiler-nucleus
        (get-in artifact [:nucleus-contract :artifact]))
     :compiled-plan-contract-matches-current-stage?
     (= :complete (:status accepted-plan-record))
     :accepted-app-output-equivalent?
     (true? (:output-matches? accepted-plan-record))
     :rejected-diagnostics-equivalent?
     (= :complete (:status rejected-diagnostic-record))
     :evidence-links-complete?
     (= :complete (:status evidence-link-record))
     :preservation-contract-complete?
     (and (empty? (:missing-op-families accepted-plan-record))
          (empty? (:missing-user-functions accepted-plan-record)))
     :residual-clojure-boundary-recorded?
     (and (true? (:clojure-stage0-verifier? boundary-record))
          (true? (:clojure-stage0-compiler? boundary-record))
          (true? (:clojure-instruction-runner? boundary-record))
          (false? (:full-language-compiler-self-hosted?
                   boundary-record))
          (false? (:clojure-seed-retired? boundary-record)))
     :does-not-claim-full-self-hosting?
     (false? (:full-language-compiler-self-hosted? artifact))
     :does-not-claim-clojure-seed-retirement?
     (false? (:clojure-seed-retired? artifact))
     :rejected-candidates-covered?
     (= (set p15-s23-stage2-compiler-nucleus-diagnostic-ids)
        rejected-diagnostics)
     :diagnostics-covered?
     (= (set p15-s23-stage2-compiler-nucleus-diagnostic-ids)
        diagnostics)
     :limitations
     {:full-language-compiler-self-hosted? false
      :clojure-seed-retired? false
      :stage2-compiler-nucleus-authored-in-gravity? true
      :stage2-executed-by-gravity? false
      :clojure-stage0-verifier? true
      :clojure-instruction-runner? true
      :full-self-hosted-toolchain? false
      :next-required-capability
      :replace_stage0_plan_emitter_with_stage2_gravity_execution}}))