

(defn p15-s23-stage3-seedless-compiler-candidate-proof-diagnostics
  [source-path candidate]
  (let [proof-contract (:proof-contract candidate)
        source-record (:source-record candidate)
        candidate-record (:candidate-record candidate)
        accepted-record (:accepted-record candidate)
        rejected-record (:rejected-record candidate)
        boundary-record (:boundary-record candidate)
        evidence-link-record (:evidence-link-record candidate)]
    (vec
     (keep
      identity
      [(when-not (= :gravity/stage3-seedless-compiler-candidate
                    (:artifact proof-contract))
         (p15-s23-stage3-seedless-compiler-candidate-diagnostic-record
          source-path "P15S23AA001" proof-contract
          {:required [:gravity/stage3-seedless-compiler-candidate]}))
       (when-not (= :complete (:status source-record))
         (p15-s23-stage3-seedless-compiler-candidate-diagnostic-record
          source-path "P15S23AA002" source-record
          {:missing-components (:missing-components source-record)
           :missing-files (:missing-files source-record)}))
       (when-not (= :complete (:status candidate-record))
         (p15-s23-stage3-seedless-compiler-candidate-diagnostic-record
          source-path "P15S23AA003" candidate-record
          {:missing-preserves (:missing-preserves candidate-record)
           :missing-emits (:missing-emits candidate-record)
           :missing-requires (:missing-requires candidate-record)}))
       (when-not (= :complete (:status accepted-record))
         (p15-s23-stage3-seedless-compiler-candidate-diagnostic-record
          source-path "P15S23AA004" accepted-record
          {:expected-output p15-s23-accepted-app-expected-stdout
           :observed-output (:seedless-candidate-output
                             accepted-record)}))
       (when-not (= :complete (:status rejected-record))
         (p15-s23-stage3-seedless-compiler-candidate-diagnostic-record
          source-path "P15S23AA005" rejected-record
          {:expected-diagnostics ["L2-BUILTIN-ARITY"
                                  "L2-FUNCTION-ARITY"]
           :observed-diagnostics
           (:seedless-candidate-diagnostics rejected-record)}))
       (when-not (= :complete (:status boundary-record))
         (p15-s23-stage3-seedless-compiler-candidate-diagnostic-record
          source-path "P15S23AA006" boundary-record
          {:required [:candidate-seedless
                      :clojure-stage0-verifier-false
                      :clojure-stage0-release-compiler-false]}))
       (when-not (= :complete (:status evidence-link-record))
         (p15-s23-stage3-seedless-compiler-candidate-diagnostic-record
          source-path "P15S23AA007" evidence-link-record
          {:required-links
           p15-s23-stage3-seedless-compiler-candidate-required-links}))
       (when (or (true? (get-in proof-contract
                                [:self-hosting-claims
                                 :full-language-compiler-self-hosted?]))
                 (true? (get-in proof-contract
                                [:self-hosting-claims
                                 :clojure-seed-retired?])))
         (p15-s23-stage3-seedless-compiler-candidate-diagnostic-record
          source-path "P15S23AA008"
          (:self-hosting-claims proof-contract)
          {:reason :candidate_is_not_final_release_bundle}))]))))

(defn p15-s23-stage3-seedless-compiler-candidate-diagnostic-stream
  [source-path proof-id]
  {:artifact
   :gravity/p15-s23-stage3-seedless-compiler-candidate-diagnostic-stream
   :stage :p15-s23-stage3-seedless-compiler-candidate
   :source-path source-path
   :proof-id proof-id
   :diagnostics
   (mapv (fn [id]
           {:diagnostic id
            :severity :error
            :message
            (get p15-s23-stage3-seedless-compiler-candidate-diagnostic-messages
                 id)
            :stable? true})
         p15-s23-stage3-seedless-compiler-candidate-diagnostic-ids)
   :status :complete})

(defn p15-s23-stage3-seedless-compiler-candidate-rejected-records
  [source-path candidate]
  (let [base candidate
        candidates
        [{:fixture :internal-p15-s23-stage3-seedless-compiler-missing-contract
          :expected-diagnostic "P15S23AA001"
          :candidate (assoc base :proof-contract {})}
         {:fixture :internal-p15-s23-stage3-seedless-compiler-source-gap
          :expected-diagnostic "P15S23AA002"
          :candidate (assoc-in base [:source-record :status] :failed)}
         {:fixture :internal-p15-s23-stage3-seedless-compiler-path-gap
          :expected-diagnostic "P15S23AA003"
          :candidate (assoc-in base [:candidate-record :status] :failed)}
         {:fixture :internal-p15-s23-stage3-seedless-compiler-output-gap
          :expected-diagnostic "P15S23AA004"
          :candidate (assoc-in base [:accepted-record :status] :failed)}
         {:fixture :internal-p15-s23-stage3-seedless-compiler-rejected-gap
          :expected-diagnostic "P15S23AA005"
          :candidate (assoc-in base [:rejected-record :status] :failed)}
         {:fixture :internal-p15-s23-stage3-seedless-compiler-boundary-gap
          :expected-diagnostic "P15S23AA006"
          :candidate (assoc-in base [:boundary-record :status] :failed)}
         {:fixture :internal-p15-s23-stage3-seedless-compiler-evidence-gap
          :expected-diagnostic "P15S23AA007"
          :candidate (assoc-in base [:evidence-link-record :status] :failed)}
         {:fixture :internal-p15-s23-stage3-seedless-compiler-overclaim
          :expected-diagnostic "P15S23AA008"
          :candidate
          (-> base
              (assoc-in [:proof-contract :self-hosting-claims
                         :full-language-compiler-self-hosted?]
                        true)
              (assoc-in [:proof-contract :self-hosting-claims
                         :clojure-seed-retired?]
                        true))}]]
    (mapv (fn [{:keys [fixture expected-diagnostic candidate]}]
            {:fixture fixture
             :status :rejected
             :expected-diagnostic expected-diagnostic
             :diagnostics
             (p15-s23-stage3-seedless-compiler-candidate-proof-diagnostics
              source-path candidate)})
          candidates)))

(defn p15-s23-stage3-seedless-compiler-candidate-proof
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:p15-s23-stage3-seedless-compiler-candidate-diagnostic-stream
                           :diagnostics])))
        rejected-diagnostics
        (set (mapcat
              #(map :diagnostic (:diagnostics %))
              (:rejected-p15-s23-stage3-seedless-compiler-candidate-fixtures
               artifact)))]
    {:stage3-seedless-compiler-candidate-authored-in-gravity? true
     :status :candidate
     :task "P15-S23"
     :seedless-compiler-candidate-present?
     (= :gravity/stage3-seedless-compiler-candidate
        (get-in artifact [:proof-contract :artifact]))
     :source-subset-covered?
     (= :complete (get-in artifact [:source-record :status]))
     :compiler-path-complete?
     (= :complete (get-in artifact [:candidate-record :status]))
     :compiler-path-seedless?
     (true? (get-in artifact
                    [:candidate-record :compiler-path-seedless?]))
     :accepted-output-equivalent?
     (true? (get-in artifact
                    [:accepted-record :accepted-output-equivalent?]))
     :rejected-diagnostics-equivalent?
     (true? (get-in artifact
                    [:rejected-record :diagnostics-equivalent?]))
     :boundary-recorded?
     (= :complete (get-in artifact [:boundary-record :status]))
     :clojure-stage0-verifier-absent?
     (false? (get-in artifact
                     [:boundary-record :clojure-stage0-verifier?]))
     :clojure-stage0-release-compiler-absent?
     (false? (get-in artifact
                     [:boundary-record
                      :clojure-stage0-release-compiler?]))
     :evidence-links-covered?
     (true? (get-in artifact
                    [:evidence-link-record
                     :required-links-covered?]))
     :lineage-recorded?
     (= :complete (get-in artifact [:lineage-record :status]))
     :does-not-claim-final-full-self-hosting?
     (false? (:full-language-compiler-self-hosted? artifact))
     :does-not-claim-final-clojure-seed-retirement?
     (false? (:clojure-seed-retired? artifact))
     :rejected-candidates-covered?
     (set/subset?
      (set p15-s23-stage3-seedless-compiler-candidate-diagnostic-ids)
      rejected-diagnostics)
     :diagnostics-covered?
     (= (set p15-s23-stage3-seedless-compiler-candidate-diagnostic-ids)
        diagnostics)
     :limitations
     {:seedless-compiler-candidate? true
      :compiler-path-uses-clojure-seed? false
      :clojure-stage0-verifier? false
      :clojure-stage0-release-compiler? false
      :full-language-compiler-self-hosted? false
      :clojure-seed-retired? false
      :final-equivalence-bundle-complete? false
      :next-required-capability
      :prove_stage3_seedless_candidate_equivalence_bundle}}))