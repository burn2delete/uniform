

(defn p15-s23-self-hosting-conformance-diagnostic-stream
  [source-path proof-id]
  {:artifact :gravity/p15-s23-self-hosting-conformance-diagnostic-stream
   :stage :p15-s23-self-hosting-conformance-report
   :source-path source-path
   :proof-id proof-id
   :diagnostics
   (mapv (fn [id]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :p15-s23-self-hosting-conformance-report
            :message
            (get p15-s23-self-hosting-conformance-diagnostic-messages
                 id)})
         p15-s23-self-hosting-conformance-diagnostic-ids)
   :status :complete})

(defn p15-s23-self-hosting-conformance-rejected-candidates
  [accepted-candidate]
  [{:fixture :internal-p15-s23-conformance-missing-report
    :candidate (assoc accepted-candidate :proof-contract {})
    :expected-diagnostic "P15S23H001"}
   {:fixture :internal-p15-s23-conformance-suite-gap
    :candidate (update-in accepted-candidate
                          [:proof-contract :suite-scope]
                          subvec 0 2)
    :expected-diagnostic "P15S23H002"}
   {:fixture :internal-p15-s23-conformance-phase14-gap
    :candidate (assoc-in accepted-candidate
                         [:conformance-suite-link-table
                          :phase14-conformance-linked?]
                         false)
    :expected-diagnostic "P15S23H003"}
   {:fixture :internal-p15-s23-conformance-stage-comparison-gap
    :candidate (assoc-in accepted-candidate
                         [:conformance-suite-link-table
                          :stage-comparison-linked?]
                         false)
    :expected-diagnostic "P15S23H004"}
   {:fixture :internal-p15-s23-conformance-diagnostic-regression
    :candidate (assoc-in accepted-candidate
                         [:diagnostic-conformance-record
                          :diagnostics-preserved?]
                         false)
    :expected-diagnostic "P15S23H005"}
   {:fixture :internal-p15-s23-conformance-overclaim
    :candidate (-> accepted-candidate
                   (assoc-in [:proof-contract :self-hosting-claims
                              :full-language-compiler-self-hosted?]
                             true)
                   (assoc-in [:proof-contract :self-hosting-claims
                              :clojure-seed-retired?]
                             true))
    :expected-diagnostic "P15S23H006"}])

(defn p15-s23-self-hosting-conformance-rejected-records
  [source-path accepted-candidate]
  (mapv (fn [{:keys [fixture candidate expected-diagnostic]}]
          {:fixture fixture
           :status :rejected
           :expected-diagnostic expected-diagnostic
           :diagnostics
           (p15-s23-self-hosting-conformance-proof-diagnostics
            source-path candidate)})
        (p15-s23-self-hosting-conformance-rejected-candidates
         accepted-candidate)))

(defn p15-s23-self-hosting-conformance-report-proof
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:p15-s23-self-hosting-conformance-diagnostic-stream
                           :diagnostics])))
        rejected-diagnostics
        (set (mapcat #(map :diagnostic (:diagnostics %))
                     (:rejected-p15-s23-self-hosting-conformance-fixtures
                      artifact)))]
    {:self-hosting-conformance-report-authored-in-gravity? true
     :status :in-progress
     :task "P15-S23"
     :suite-scope-complete?
     (= (set p15-s23-self-hosting-conformance-scope)
        (set (get-in artifact [:proof-contract :suite-scope])))
     :stage-support-conformant?
     (true?
      (get-in artifact
              [:stage-support-conformance-record
               :stage-support-conformant?]))
     :phase14-conformance-linked?
     (true?
      (get-in artifact
              [:conformance-suite-link-table
               :phase14-conformance-linked?]))
     :test13-self-hosting-linked?
     (true?
      (get-in artifact
              [:conformance-suite-link-table
               :test13-self-hosting-linked?]))
     :stage-comparison-linked?
     (true?
      (get-in artifact
              [:conformance-suite-link-table
               :stage-comparison-linked?]))
     :diagnostics-preserved?
     (true?
      (get-in artifact
              [:diagnostic-conformance-record
               :diagnostics-preserved?]))
     :does-not-claim-full-self-hosting?
     (false? (:full-language-compiler-self-hosted? artifact))
     :does-not-claim-clojure-seed-retirement?
     (false? (:clojure-seed-retired? artifact))
     :rejected-candidates-covered?
     (set/subset?
      (set p15-s23-self-hosting-conformance-diagnostic-ids)
      rejected-diagnostics)
     :diagnostics-covered?
     (= (set p15-s23-self-hosting-conformance-diagnostic-ids)
        diagnostics)
     :limitations
     {:full-language-compiler-self-hosted? false
      :clojure-seed-retired? false
      :current-candidate-is-clojure-seed? true
      :production-conformance-runner? false
      :self-hosted-conformance-runner? false
      :full-self-hosted-toolchain? false
      :next-required-capability
      :implement_provenance_attestation}}))