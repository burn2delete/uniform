

(defn p15-s23-stage3-equivalence-rebuild-record
  [candidate rebuild comparison]
  {:artifact :gravity/p15-s23-stage3-equivalence-rebuild-record
   :candidate-artifact-id (:artifact-id candidate)
   :rebuild-artifact-id (:artifact-id rebuild)
   :stage-comparison-artifact-id (:artifact-id comparison)
   :candidate-equivalent?
   (true? (get-in candidate
                  [:capability-based-proof
                   :accepted-output-equivalent?]))
   :rebuild-reproducible?
   (true? (get-in rebuild
                  [:capability-based-proof
                   :artifact-identities-reproducible?]))
   :stage-comparison-equivalent?
   (true? (get-in comparison
                  [:capability-based-proof
                   :current-candidate-equivalent-to-seed?]))
   :status
   (if (and (true? (get-in candidate
                           [:capability-based-proof
                            :accepted-output-equivalent?]))
            (true? (get-in rebuild
                           [:capability-based-proof
                            :artifact-identities-reproducible?]))
            (true? (get-in comparison
                           [:capability-based-proof
                            :current-candidate-equivalent-to-seed?])))
     :complete
     :failed)})

(defn p15-s23-stage3-equivalence-conformance-record
  [conformance provenance tcb unsafe]
  {:artifact :gravity/p15-s23-stage3-equivalence-conformance-record
   :conformance-artifact-id (:artifact-id conformance)
   :provenance-artifact-id (:artifact-id provenance)
   :tcb-artifact-id (:artifact-id tcb)
   :unsafe-artifact-id (:artifact-id unsafe)
   :conformance-report-present?
   (true? (get-in conformance
                  [:capability-based-proof
                   :stage-support-conformant?]))
   :provenance-traversable?
   (true? (get-in provenance
                  [:capability-based-proof
                   :compiler-lineage-traversable?]))
   :tcb-accounted?
   (true? (get-in tcb
                  [:capability-based-proof
                   :no-unaccounted-trusted-components?]))
   :unsafe-audit-complete?
   (true? (get-in unsafe
                  [:capability-based-proof
                   :no-gravity-unsafe-islands?]))
   :status
   (if (and (true? (get-in conformance
                           [:capability-based-proof
                            :stage-support-conformant?]))
            (true? (get-in provenance
                           [:capability-based-proof
                            :compiler-lineage-traversable?]))
            (true? (get-in tcb
                           [:capability-based-proof
                            :no-unaccounted-trusted-components?]))
	            (true? (get-in unsafe
	                           [:capability-based-proof
	                            :no-gravity-unsafe-islands?])))
     :complete
     :failed)})

(defn p15-s23-stage3-equivalence-boundary-record
  [proof-contract candidate]
  (let [boundary (:boundary proof-contract)
        claims (:self-hosting-claims proof-contract)]
    {:artifact :gravity/p15-s23-stage3-equivalence-boundary-record
     :candidate-compiler-path-uses-clojure-seed?
     (true? (:candidate-compiler-path-uses-clojure-seed? boundary))
     :equivalence-proven-against-current-stage?
     (true? (:equivalence-proven-against-current-stage? boundary))
     :final-self-hosted-application-run?
     (true? (:final-self-hosted-application-run? boundary))
     :candidate-clojure-stage0-verifier?
     (true? (get-in candidate
                    [:boundary-record :clojure-stage0-verifier?]))
     :candidate-clojure-stage0-release-compiler?
     (true? (get-in candidate
                    [:boundary-record
                     :clojure-stage0-release-compiler?]))
     :full-language-compiler-self-hosted?
     (true? (:full-language-compiler-self-hosted? claims))
     :clojure-seed-retired?
     (true? (:clojure-seed-retired? claims))
     :stage3-equivalence-bundle-complete?
     (true? (:stage3-equivalence-bundle-complete? claims))
     :status
     (if (and (false? (:candidate-compiler-path-uses-clojure-seed?
                      boundary))
              (true? (:equivalence-proven-against-current-stage?
                      boundary))
              (false? (:final-self-hosted-application-run? boundary))
              (false? (get-in candidate
                              [:boundary-record
                               :clojure-stage0-verifier?]))
              (false? (get-in candidate
                              [:boundary-record
                               :clojure-stage0-release-compiler?]))
              (false? (:full-language-compiler-self-hosted? claims))
              (false? (:clojure-seed-retired? claims))
              (true? (:stage3-equivalence-bundle-complete? claims)))
       :complete
       :failed)}))

(defn p15-s23-stage3-equivalence-bundle-proof-diagnostics
  [source-path candidate]
  (let [proof-contract (:proof-contract candidate)]
    (vec
     (keep
      identity
      [(when-not (= :gravity/stage3-equivalence-bundle
                    (:artifact proof-contract))
         (p15-s23-stage3-equivalence-bundle-diagnostic-record
          source-path "P15S23AB001" proof-contract
          {:required [:gravity/stage3-equivalence-bundle]}))
       (when-not (= :complete (get-in candidate [:bundle-record :status]))
         (p15-s23-stage3-equivalence-bundle-diagnostic-record
          source-path "P15S23AB002" (:bundle-record candidate)
          {:required [:stage3-seedless-compiler-candidate]}))
       (when-not (= :complete (get-in candidate [:accepted-record :status]))
         (p15-s23-stage3-equivalence-bundle-diagnostic-record
          source-path "P15S23AB003" (:accepted-record candidate)
          {:expected-output p15-s23-accepted-app-expected-stdout}))
       (when-not (= :complete (get-in candidate [:rejected-record :status]))
         (p15-s23-stage3-equivalence-bundle-diagnostic-record
          source-path "P15S23AB004" (:rejected-record candidate)
          {:expected-diagnostics ["L2-BUILTIN-ARITY"
                                  "L2-FUNCTION-ARITY"]}))
       (when-not (= :complete (get-in candidate [:rebuild-record :status]))
         (p15-s23-stage3-equivalence-bundle-diagnostic-record
          source-path "P15S23AB005" (:rebuild-record candidate)
          {:required [:reproducible-rebuild-log
                      :stage-comparison-report]}))
       (when-not (= :complete (get-in candidate
                                      [:conformance-record :status]))
         (p15-s23-stage3-equivalence-bundle-diagnostic-record
          source-path "P15S23AB006" (:conformance-record candidate)
          {:required [:conformance :provenance :tcb :unsafe-audit]}))
       (when-not (= :complete (get-in candidate
                                      [:evidence-link-record :status]))
         (p15-s23-stage3-equivalence-bundle-diagnostic-record
          source-path "P15S23AB007" (:evidence-link-record candidate)
          {:required-links p15-s23-stage3-equivalence-bundle-required-links}))
       (when (or (true? (get-in proof-contract
                                [:self-hosting-claims
                                 :full-language-compiler-self-hosted?]))
                 (true? (get-in proof-contract
                                [:self-hosting-claims
                                 :clojure-seed-retired?])))
         (p15-s23-stage3-equivalence-bundle-diagnostic-record
          source-path "P15S23AB008"
          (:self-hosting-claims proof-contract)
          {:reason :equivalence_bundle_is_not_final_release_bundle}))]))))

(defn p15-s23-stage3-equivalence-bundle-diagnostic-stream
  [source-path proof-id]
  {:artifact :gravity/p15-s23-stage3-equivalence-bundle-diagnostic-stream
   :stage :p15-s23-stage3-equivalence-bundle
   :source-path source-path
   :proof-id proof-id
   :diagnostics
   (mapv (fn [id]
           {:diagnostic id
            :severity :error
            :message
            (get p15-s23-stage3-equivalence-bundle-diagnostic-messages id)
            :stable? true})
         p15-s23-stage3-equivalence-bundle-diagnostic-ids)
   :status :complete})

(defn p15-s23-stage3-equivalence-bundle-rejected-records
  [source-path candidate]
  (let [base candidate
        candidates
        [{:fixture :internal-p15-s23-stage3-equivalence-missing-bundle
          :expected-diagnostic "P15S23AB001"
          :candidate (assoc base :proof-contract {})}
         {:fixture :internal-p15-s23-stage3-equivalence-candidate-gap
          :expected-diagnostic "P15S23AB002"
          :candidate (assoc-in base [:bundle-record :status] :failed)}
         {:fixture :internal-p15-s23-stage3-equivalence-output-gap
          :expected-diagnostic "P15S23AB003"
          :candidate (assoc-in base [:accepted-record :status] :failed)}
         {:fixture :internal-p15-s23-stage3-equivalence-rejected-gap
          :expected-diagnostic "P15S23AB004"
          :candidate (assoc-in base [:rejected-record :status] :failed)}
         {:fixture :internal-p15-s23-stage3-equivalence-rebuild-gap
          :expected-diagnostic "P15S23AB005"
          :candidate (assoc-in base [:rebuild-record :status] :failed)}
         {:fixture :internal-p15-s23-stage3-equivalence-conformance-gap
          :expected-diagnostic "P15S23AB006"
          :candidate
          (assoc-in base [:conformance-record :status] :failed)}
         {:fixture :internal-p15-s23-stage3-equivalence-evidence-gap
          :expected-diagnostic "P15S23AB007"
          :candidate
          (assoc-in base [:evidence-link-record :status] :failed)}
         {:fixture :internal-p15-s23-stage3-equivalence-overclaim
          :expected-diagnostic "P15S23AB008"
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
             (p15-s23-stage3-equivalence-bundle-proof-diagnostics
              source-path candidate)})
          candidates)))