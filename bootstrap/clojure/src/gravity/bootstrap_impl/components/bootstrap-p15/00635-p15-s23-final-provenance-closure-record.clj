

(defn p15-s23-final-provenance-closure-record
  [proof-contract evidence boundary-record stage3-record release-decision]
  (let [provenance (:bootstrap-provenance-attestation evidence)
        seedless (:stage3-seedless-compiler-candidate evidence)
        equivalence (:stage3-equivalence-bundle evidence)
        app (:stage3-self-hosted-application-execution evidence)
        complete?
        (and (p15-s23-evidence-present? provenance)
             (= :complete (:status boundary-record))
             (= :complete (:status stage3-record))
             (= :complete (:status release-decision))
             (true? (:revocation-clear? provenance))
             (true? (:auditor-query-passed? provenance)))]
    {:artifact :gravity/p15-s23-final-provenance-closure-record
     :prior-provenance-record-id (:provenance-record-id provenance)
     :canonical-payload-id (:canonical-payload-id provenance)
     :stage3-seedless-candidate-artifact-id (:artifact-id seedless)
     :stage3-equivalence-bundle-artifact-id (:artifact-id equivalence)
     :stage3-application-artifact-id (:artifact-id app)
     :release-compiler :gravity-stage3-release-compiler
     :verification-host
     (get-in proof-contract [:final-boundary :verification-host])
     :verification-host-in-release-boundary?
     (get-in proof-contract
             [:final-boundary :verification-host-in-release-boundary?])
     :revocation-clear? (true? (:revocation-clear? provenance))
     :auditor-query-passed?
     (true? (:auditor-query-passed? provenance))
     :status (if complete? :complete :failed)}))

(defn p15-s23-final-gate-completion-record
  [proof-contract link-record boundary-record stage3-record release-decision
   tcb-retirement provenance-closure]
  (let [claims (:self-hosting-claims proof-contract)
        complete?
        (and (= :complete (:status link-record))
             (= :complete (:status boundary-record))
             (= :complete (:status stage3-record))
             (= :complete (:status release-decision))
             (= :complete (:status tcb-retirement))
             (= :complete (:status provenance-closure))
             (true? (:full-language-compiler-self-hosted? claims))
             (true? (:clojure-seed-retired? claims))
             (false? (:clojure-seed-boundary? claims)))]
    {:artifact :gravity/p15-s23-final-gate-completion-record
     :full-language-compiler-self-hosted?
     (true? (:full-language-compiler-self-hosted? claims))
     :clojure-seed-retired? (true? (:clojure-seed-retired? claims))
     :clojure-seed-boundary? (:clojure-seed-boundary? claims)
     :next-required-capability
     (:next-required-capability proof-contract)
     :status (if complete? :complete :failed)}))

(defn p15-s23-final-seed-retirement-proof-diagnostics
  [source-path candidate]
  (let [proof-contract (:proof-contract candidate)
        link-record (:evidence-link-record candidate)
        boundary-record (:boundary-record candidate)
        stage3-record (:stage3-execution-record candidate)
        release-decision (:release-decision-record candidate)
        tcb-retirement (:tcb-retirement-record candidate)
        provenance-closure (:provenance-closure-record candidate)
        gate-completion (:gate-completion-record candidate)]
    (vec
     (concat
	      (when-not (and (= :gravity/final-seed-retirement-proof
	                        (:artifact proof-contract))
	                     (= :p15-s23-final-seed-retirement-proof
	                        (:stage proof-contract))
	                     (contains? #{:complete :incomplete}
	                                (:status proof-contract)))
        [(p15-s23-final-seed-retirement-diagnostic-record
          source-path "P15S23AD001" proof-contract
          {:missing-fields [:artifact :stage :status]})])
      (when-not (= :complete (:status link-record))
        [(p15-s23-final-seed-retirement-diagnostic-record
          source-path "P15S23AD002" link-record
          {:missing-links (:missing-links link-record)})])
      (when-not (= :complete (:status boundary-record))
        [(p15-s23-final-seed-retirement-diagnostic-record
          source-path "P15S23AD003" boundary-record
          {:required-boundary
           [:compiler-path-uses-clojure-seed-false
            :runtime-path-uses-clojure-seed-false
            :release-compiler-uses-clojure-seed-false
            :stage3-seedless-candidate
            :stage3-toolchain-seedless]})])
      (when-not (= :complete (:status stage3-record))
        [(p15-s23-final-seed-retirement-diagnostic-record
          source-path "P15S23AD004" stage3-record
          {:required-stage3 [:equivalence-bundle
                             :accepted-output-equivalence
                             :rejected-diagnostics
                             :rebuild-equivalence
                             :conformance-evidence
                             :self-hosted-application-run]})])
      (when-not (= :complete (:status release-decision))
        [(p15-s23-final-seed-retirement-diagnostic-record
          source-path "P15S23AD005" release-decision
          {:remaining-release-blockers
           (:remaining-release-blockers release-decision)})])
      (when-not (= :complete (:status tcb-retirement))
        [(p15-s23-final-seed-retirement-diagnostic-record
          source-path "P15S23AD006" tcb-retirement
          {:compiler-seed-residual-count
           (:compiler-seed-residual-count tcb-retirement)})])
      (when-not (= :complete (:status provenance-closure))
        [(p15-s23-final-seed-retirement-diagnostic-record
          source-path "P15S23AD007" provenance-closure
          {:verification-host-in-release-boundary?
           (:verification-host-in-release-boundary?
            provenance-closure)})])
      (when-not (= :complete (:status gate-completion))
        [(p15-s23-final-seed-retirement-diagnostic-record
          source-path "P15S23AD008"
          (select-keys proof-contract
                       [:self-hosting-claims :next-required-capability])
          {:gate-completion-status (:status gate-completion)})])))))

(defn p15-s23-final-seed-retirement-diagnostic-stream
  [source-path proof-id]
  {:artifact :gravity/p15-s23-final-seed-retirement-diagnostic-stream
   :stage :p15-s23-final-seed-retirement-proof
   :source-path source-path
   :proof-id proof-id
   :diagnostics
   (mapv (fn [id]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :p15-s23-final-seed-retirement-proof
            :message
            (get p15-s23-final-seed-retirement-diagnostic-messages
                 id)
            :stable? true})
         p15-s23-final-seed-retirement-diagnostic-ids)
   :status :complete})

(defn p15-s23-final-seed-retirement-rejected-records
  [source-path candidate]
  (let [base candidate
        records
        [{:fixture :internal-p15-s23-final-seed-retirement-missing-contract
          :expected-diagnostic "P15S23AD001"
          :candidate (assoc base :proof-contract {})}
         {:fixture :internal-p15-s23-final-seed-retirement-evidence-gap
          :expected-diagnostic "P15S23AD002"
          :candidate (assoc-in base
                               [:evidence-link-record :status]
                               :failed)}
         {:fixture :internal-p15-s23-final-seed-retirement-boundary-gap
          :expected-diagnostic "P15S23AD003"
          :candidate
          (-> base
              (assoc-in [:boundary-record :status] :failed)
              (assoc-in [:boundary-record
                         :compiler-path-uses-clojure-seed?]
                        true))}
         {:fixture :internal-p15-s23-final-seed-retirement-stage3-gap
          :expected-diagnostic "P15S23AD004"
          :candidate (assoc-in base
                               [:stage3-execution-record :status]
                               :failed)}
         {:fixture :internal-p15-s23-final-seed-retirement-release-gap
          :expected-diagnostic "P15S23AD005"
          :candidate (assoc-in base
                               [:release-decision-record :status]
                               :failed)}
         {:fixture :internal-p15-s23-final-seed-retirement-tcb-gap
          :expected-diagnostic "P15S23AD006"
          :candidate (assoc-in base
                               [:tcb-retirement-record :status]
                               :failed)}
         {:fixture :internal-p15-s23-final-seed-retirement-provenance-gap
          :expected-diagnostic "P15S23AD007"
          :candidate (assoc-in base
                               [:provenance-closure-record :status]
                               :failed)}
         {:fixture :internal-p15-s23-final-seed-retirement-unsupported-claim
          :expected-diagnostic "P15S23AD008"
          :candidate
          (-> base
              (assoc-in [:proof-contract :self-hosting-claims
                         :clojure-seed-boundary?]
                        true)
              (assoc-in [:gate-completion-record :status]
                        :failed))}]]
    (mapv
     (fn [{:keys [fixture expected-diagnostic candidate]}]
       {:fixture fixture
        :status :rejected
        :expected-diagnostic expected-diagnostic
        :diagnostics
        (p15-s23-final-seed-retirement-proof-diagnostics
         source-path candidate)})
     records)))