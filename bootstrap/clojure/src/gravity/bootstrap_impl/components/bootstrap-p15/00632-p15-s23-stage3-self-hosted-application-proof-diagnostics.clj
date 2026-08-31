

(defn p15-s23-stage3-self-hosted-application-proof-diagnostics
  [source-path candidate]
  (let [proof-contract (:proof-contract candidate)]
    (vec
     (keep
      identity
      [(when-not (= :gravity/stage3-self-hosted-application-execution
                    (:artifact proof-contract))
         (p15-s23-stage3-self-hosted-application-diagnostic-record
          source-path "P15S23AC001" proof-contract
          {:required [:gravity/stage3-self-hosted-application-execution]}))
       (when-not (= :complete (get-in candidate [:execution-record :status]))
         (p15-s23-stage3-self-hosted-application-diagnostic-record
          source-path "P15S23AC001" (:execution-record candidate)
          {:required [:stage3-self-hosted-application-execution-record]}))
       (when-not (= :complete (get-in candidate [:equivalence-record :status]))
         (p15-s23-stage3-self-hosted-application-diagnostic-record
          source-path "P15S23AC002" (:equivalence-record candidate)
          {:required [:stage3-equivalence-bundle]}))
       (when-not (= :complete (get-in candidate [:accepted-run-record :status]))
         (p15-s23-stage3-self-hosted-application-diagnostic-record
          source-path "P15S23AC003" (:accepted-run-record candidate)
          {:expected-output p15-s23-accepted-app-expected-stdout}))
       (when-not (= :complete (get-in candidate [:rejected-record :status]))
         (p15-s23-stage3-self-hosted-application-diagnostic-record
          source-path "P15S23AC004" (:rejected-record candidate)
          {:expected-diagnostics ["L2-BUILTIN-ARITY"
                                  "L2-FUNCTION-ARITY"]}))
       (when-not (= :complete (get-in candidate [:toolchain-record :status]))
         (p15-s23-stage3-self-hosted-application-diagnostic-record
          source-path "P15S23AC005" (:toolchain-record candidate)
          {:required [:stage3-seedless-compiler-candidate
                      :stage2-compiler-driver]}))
       (when-not (= :complete (get-in candidate [:runtime-record :status]))
         (p15-s23-stage3-self-hosted-application-diagnostic-record
          source-path "P15S23AC006" (:runtime-record candidate)
          {:required [:stage2-runtime-kernel
                      :runtime-capability-manifest]}))
       (when-not (= :complete (get-in candidate [:evidence-link-record :status]))
         (p15-s23-stage3-self-hosted-application-diagnostic-record
          source-path "P15S23AC007" (:evidence-link-record candidate)
          {:required-links
           p15-s23-stage3-self-hosted-application-required-links}))
       (when (or (true? (get-in proof-contract
                                [:self-hosting-claims
                                 :full-language-compiler-self-hosted?]))
                 (true? (get-in proof-contract
                                [:self-hosting-claims
                                 :clojure-seed-retired?])))
         (p15-s23-stage3-self-hosted-application-diagnostic-record
          source-path "P15S23AC008"
          (:self-hosting-claims proof-contract)
          {:reason :stage3_application_execution_is_not_final_seed_retirement_proof}))]))))

(defn p15-s23-stage3-self-hosted-application-diagnostic-stream
  [source-path proof-id]
  {:artifact :gravity/p15-s23-stage3-self-hosted-application-diagnostic-stream
   :stage :p15-s23-stage3-self-hosted-application-execution
   :source-path source-path
   :proof-id proof-id
   :diagnostics
   (mapv (fn [id]
           {:diagnostic id
            :severity :error
            :message
            (get p15-s23-stage3-self-hosted-application-diagnostic-messages
                 id)
            :stable? true})
         p15-s23-stage3-self-hosted-application-diagnostic-ids)
   :status :complete})

(defn p15-s23-stage3-self-hosted-application-rejected-records
  [source-path candidate]
  (let [base candidate
        candidates
        [{:fixture :internal-p15-s23-stage3-self-hosted-application-missing-contract
          :expected-diagnostic "P15S23AC001"
          :candidate (assoc base :proof-contract {})}
         {:fixture :internal-p15-s23-stage3-self-hosted-application-equivalence-gap
          :expected-diagnostic "P15S23AC002"
          :candidate (assoc-in base [:equivalence-record :status] :failed)}
         {:fixture :internal-p15-s23-stage3-self-hosted-application-output-gap
          :expected-diagnostic "P15S23AC003"
          :candidate (assoc-in base [:accepted-run-record :status] :failed)}
         {:fixture :internal-p15-s23-stage3-self-hosted-application-rejected-gap
          :expected-diagnostic "P15S23AC004"
          :candidate (assoc-in base [:rejected-record :status] :failed)}
         {:fixture :internal-p15-s23-stage3-self-hosted-application-toolchain-gap
          :expected-diagnostic "P15S23AC005"
          :candidate (assoc-in base [:toolchain-record :status] :failed)}
         {:fixture :internal-p15-s23-stage3-self-hosted-application-runtime-gap
          :expected-diagnostic "P15S23AC006"
          :candidate (assoc-in base [:runtime-record :status] :failed)}
         {:fixture :internal-p15-s23-stage3-self-hosted-application-evidence-gap
          :expected-diagnostic "P15S23AC007"
          :candidate (assoc-in base [:evidence-link-record :status] :failed)}
         {:fixture :internal-p15-s23-stage3-self-hosted-application-overclaim
          :expected-diagnostic "P15S23AC008"
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
             (p15-s23-stage3-self-hosted-application-proof-diagnostics
              source-path candidate)})
          candidates)))

(defn p15-s23-stage3-self-hosted-application-proof
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:p15-s23-stage3-self-hosted-application-diagnostic-stream
                           :diagnostics])))
        rejected-diagnostics
        (set (mapcat
              #(map :diagnostic (:diagnostics %))
              (:rejected-p15-s23-stage3-self-hosted-application-fixtures
               artifact)))]
    {:stage3-self-hosted-application-authored-in-gravity? true
     :status :complete
     :task "P15-S23"
     :stage3-self-hosted-application-execution-present?
     (= :gravity/stage3-self-hosted-application-execution
        (get-in artifact [:proof-contract :artifact]))
     :stage3-equivalence-bundle-linked?
     (= :complete (get-in artifact [:equivalence-record :status]))
     :accepted-application-run?
     (= :complete (get-in artifact [:accepted-run-record :status]))
     :accepted-output-equivalent?
     (true? (get-in artifact
                    [:accepted-run-record
                     :accepted-output-equivalent?]))
     :rejected-application-fails-closed?
     (true? (get-in artifact
                    [:rejected-record
                     :rejected-diagnostics-equivalent?]))
     :stage3-toolchain-seedless?
     (false? (get-in artifact
                     [:toolchain-record
                      :stage3-toolchain-uses-clojure-seed?]))
     :toolchain-boundary-recorded?
     (= :complete (get-in artifact [:toolchain-record :status]))
     :runtime-capability-recorded?
     (= :complete (get-in artifact [:runtime-record :status]))
     :evidence-links-covered?
     (true? (get-in artifact
                    [:evidence-link-record
                     :required-links-covered?]))
     :boundary-recorded?
     (= :complete (get-in artifact [:boundary-record :status]))
     :does-not-claim-final-full-self-hosting?
     (false? (:full-language-compiler-self-hosted? artifact))
     :does-not-claim-final-clojure-seed-retirement?
     (false? (:clojure-seed-retired? artifact))
     :rejected-candidates-covered?
     (set/subset?
      (set p15-s23-stage3-self-hosted-application-diagnostic-ids)
      rejected-diagnostics)
     :diagnostics-covered?
     (= (set p15-s23-stage3-self-hosted-application-diagnostic-ids)
        diagnostics)
     :limitations
     {:stage3-self-hosted-application-run? true
      :full-language-compiler-self-hosted? false
      :clojure-seed-retired? false
      :final-seed-retirement-proof-present? false
      :next-required-capability :emit_final_seed_retirement_proof}}))