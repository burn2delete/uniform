

(defn p15-s23-accepted-output-stage-comparison
  [accepted-artifact]
  (let [comparison (:accepted-output-comparison accepted-artifact)]
    {:artifact :gravity/p15-s23-accepted-output-stage-comparison
     :accepted-app-path (:accepted-app-path accepted-artifact)
     :seed-stage :clojure-stage0-reference-run
     :candidate-stage :current-compiled-instruction-plan
     :seed-stdout (:reference-stdout comparison)
     :candidate-stdout (:accepted-stdout comparison)
     :expected-stdout (:expected-stdout comparison)
     :accepted-output-equivalent?
     (and (= :complete (:status comparison))
          (true? (:accepted-matches-reference? comparison))
          (true? (:accepted-matches-expected? comparison)))
     :compiled-plan-id
     (get-in accepted-artifact
             [:compiled-plan-execution-trace :compiled-plan-id])
     :status :complete}))

(defn p15-s23-rejected-diagnostic-stage-comparison
  [rejected-artifact]
  (let [preservation (:diagnostic-preservation-record rejected-artifact)]
    {:artifact :gravity/p15-s23-rejected-diagnostic-stage-comparison
     :seed-stage :clojure-stage0-diagnostics
     :candidate-stage :current-compiled-instruction-plan-diagnostics
     :expected-diagnostics (:expected-diagnostics preservation)
     :candidate-diagnostics (:diagnostics preservation)
     :rejected-diagnostics-equivalent?
     (and (= :complete (:status preservation))
          (true? (:all-fixtures-rejected? preservation))
          (true? (:all-diagnostics-match? preservation))
          (true? (:diagnostic-codes-stable? preservation)))
     :status :complete}))

(defn p15-s23-stage-equivalence-row
  [scope artifact-id proof-id status equivalent?]
  {:scope scope
   :artifact-id artifact-id
   :proof-id proof-id
   :status status
   :equivalent? equivalent?})

(defn p15-s23-stage-equivalence-matrix
  [pipeline-artifact accepted-artifact rejected-artifact rebuild-artifact
   accepted-comparison rejected-comparison]
  (let [rows
        [(p15-s23-stage-equivalence-row
          :compiler-pipeline-manifest
          (:artifact-id pipeline-artifact)
          (:manifest-id pipeline-artifact)
          :complete true)
         (p15-s23-stage-equivalence-row
          :accepted-app-execution-proof
          (:artifact-id accepted-artifact)
          (:proof-id accepted-artifact)
          :complete
          (true? (:accepted-output-equivalent? accepted-comparison)))
         (p15-s23-stage-equivalence-row
          :rejected-app-diagnostic-proof
          (:artifact-id rejected-artifact)
          (:proof-id rejected-artifact)
          :complete
          (true? (:rejected-diagnostics-equivalent?
                  rejected-comparison)))
         (p15-s23-stage-equivalence-row
          :reproducible-rebuild-log
          (:artifact-id rebuild-artifact)
          (:proof-id rebuild-artifact)
          (get-in rebuild-artifact
                  [:artifact-identity-comparison :status])
          (true?
           (get-in rebuild-artifact
                   [:artifact-identity-comparison
                    :all-artifact-identities-match?])))]]
    {:artifact :gravity/p15-s23-stage-equivalence-matrix
     :rows rows
     :current-candidate-equivalent-to-seed?
     (every? true? (map :equivalent? rows))
     :full-self-hosted-equivalence? false
     :status (if (every? true? (map :equivalent? rows))
               :complete
               :failed)}))

(defn p15-s23-stage-comparison-proof-diagnostics
  [source-path candidate]
  (let [proof-contract (:proof-contract candidate)
        boundary (:stage-boundary-record candidate)
        accepted (:accepted-output-stage-comparison candidate)
        rejected (:rejected-diagnostic-stage-comparison candidate)
        rebuild (:reproducible-rebuild-artifact candidate)
        matrix (:stage-equivalence-matrix candidate)
        claims (:self-hosting-claims proof-contract)
        scope (set (:comparison-scope proof-contract))
        preserves (set (:preserves proof-contract))
        missing-preserves
        (set/difference p15-s23-stage-comparison-required-preserves
                        preserves)]
    (vec
     (concat
      (when-not (= :gravity/stage-comparison-report
                   (:artifact proof-contract))
        [(p15-s23-stage-comparison-diagnostic-record
          source-path "P15S23G001" proof-contract
          {:missing-fields [:artifact]})])
      (when-not
       (and (= (set p15-s23-stage-comparison-scope) scope)
            (empty? missing-preserves)
            (= :gravity/p15-s23-stage-boundary-record
               (:artifact boundary))
            (= :complete (:status boundary))
            (= :clojure-stage0 (:seed-stage boundary))
            (true? (:clojure-seed-boundary? boundary))
            (false? (:candidate-is-self-hosted? boundary)))
        [(p15-s23-stage-comparison-diagnostic-record
          source-path "P15S23G002" candidate
          {:expected-scope p15-s23-stage-comparison-scope
           :observed-scope (vec (sort scope))
           :missing-preserves (vec (sort missing-preserves))})])
      (when-not
       (and (= :gravity/p15-s23-accepted-output-stage-comparison
               (:artifact accepted))
            (= :complete (:status accepted))
            (true? (:accepted-output-equivalent? accepted)))
        [(p15-s23-stage-comparison-diagnostic-record
          source-path "P15S23G003" accepted
          {:expected-stdout (:expected-stdout accepted)
           :seed-stdout (:seed-stdout accepted)
           :candidate-stdout (:candidate-stdout accepted)})])
      (when-not
       (and (= :gravity/p15-s23-rejected-diagnostic-stage-comparison
               (:artifact rejected))
            (= :complete (:status rejected))
            (true? (:rejected-diagnostics-equivalent? rejected)))
        [(p15-s23-stage-comparison-diagnostic-record
          source-path "P15S23G004" rejected
          {:expected-diagnostics (:expected-diagnostics rejected)
           :candidate-diagnostics (:candidate-diagnostics rejected)})])
      (when-not
       (and (= :gravity/p15-s23-reproducible-rebuild-log-artifact
               (:kind rebuild))
            (true?
             (get-in rebuild
                     [:artifact-identity-comparison
                      :all-artifact-identities-match?]))
            (= :gravity/p15-s23-stage-equivalence-matrix
               (:artifact matrix))
            (= :complete (:status matrix))
            (true? (:current-candidate-equivalent-to-seed? matrix)))
        [(p15-s23-stage-comparison-diagnostic-record
          source-path "P15S23G005" rebuild
          {:required-link :reproducible-rebuild-log
           :matrix-status (:status matrix)})])
      (when (or (true? (:full-language-compiler-self-hosted? claims))
                (true? (:clojure-seed-retired? claims)))
        [(p15-s23-stage-comparison-diagnostic-record
          source-path "P15S23G006" claims
          {:full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? claims)
           :clojure-seed-retired?
           (:clojure-seed-retired? claims)})])))))

(defn p15-s23-stage-comparison-diagnostic-stream
  [source-path proof-id]
  {:artifact :gravity/p15-s23-stage-comparison-diagnostic-stream
   :stage :p15-s23-stage-comparison-report
   :source-path source-path
   :proof-id proof-id
   :diagnostics
   (mapv (fn [id]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :p15-s23-stage-comparison-report
            :message
            (get p15-s23-stage-comparison-diagnostic-messages id)})
         p15-s23-stage-comparison-diagnostic-ids)
   :status :complete})

(defn p15-s23-stage-comparison-rejected-candidates
  [accepted-candidate]
  [{:fixture :internal-p15-s23-stage-comparison-missing-report
    :candidate (assoc accepted-candidate :proof-contract {})
    :expected-diagnostic "P15S23G001"}
   {:fixture :internal-p15-s23-stage-comparison-candidate-gap
    :candidate (update-in accepted-candidate
                          [:proof-contract :comparison-scope]
                          subvec 0 2)
    :expected-diagnostic "P15S23G002"}
   {:fixture :internal-p15-s23-stage-comparison-output-mismatch
    :candidate (assoc-in accepted-candidate
                         [:accepted-output-stage-comparison
                          :accepted-output-equivalent?]
                         false)
    :expected-diagnostic "P15S23G003"}
   {:fixture :internal-p15-s23-stage-comparison-diagnostic-mismatch
    :candidate (assoc-in accepted-candidate
                         [:rejected-diagnostic-stage-comparison
                          :rejected-diagnostics-equivalent?]
                         false)
    :expected-diagnostic "P15S23G004"}
   {:fixture :internal-p15-s23-stage-comparison-rebuild-link-gap
    :candidate (assoc-in accepted-candidate
                         [:reproducible-rebuild-artifact
                          :artifact-identity-comparison
                          :all-artifact-identities-match?]
                         false)
    :expected-diagnostic "P15S23G005"}
   {:fixture :internal-p15-s23-stage-comparison-overclaim
    :candidate (-> accepted-candidate
                   (assoc-in [:proof-contract :self-hosting-claims
                              :full-language-compiler-self-hosted?]
                             true)
                   (assoc-in [:proof-contract :self-hosting-claims
                              :clojure-seed-retired?]
                             true))
    :expected-diagnostic "P15S23G006"}])

(defn p15-s23-stage-comparison-rejected-records
  [source-path accepted-candidate]
  (mapv (fn [{:keys [fixture candidate expected-diagnostic]}]
          {:fixture fixture
           :status :rejected
           :expected-diagnostic expected-diagnostic
           :diagnostics
           (p15-s23-stage-comparison-proof-diagnostics
            source-path candidate)})
        (p15-s23-stage-comparison-rejected-candidates
         accepted-candidate)))