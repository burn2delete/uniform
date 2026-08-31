

(defn p15-s23-rejected-app-diagnostic-preservation-record
  [records]
  (let [diagnostics (mapv :diagnostic records)
        expected (mapv :expected-diagnostic records)]
    {:artifact :gravity/p15-s23-rejected-app-diagnostic-preservation-record
     :diagnostics diagnostics
     :expected-diagnostics expected
     :all-fixtures-rejected? (every? #(= :rejected (:status %)) records)
     :all-diagnostics-match? (every? true? (map :matches-expected? records))
     :diagnostic-codes-stable? (= (set diagnostics) (set expected))
     :source-spans-present?
     (every? #(get-in % [:diagnostic-data :source-span]) records)
     :remediations-present?
     (every? #(get-in % [:diagnostic-data :remediation]) records)
     :status
     (if (and (seq records)
              (every? #(= :rejected (:status %)) records)
              (every? true? (map :matches-expected? records))
              (= (set diagnostics) (set expected)))
       :complete
       :failed)}))

(defn p15-s23-rejected-app-proof-diagnostics
  [source-path candidate]
  (let [proof-contract (:proof-contract candidate)
        records (:rejected-app-diagnostic-records candidate)
        preservation (:diagnostic-preservation-record candidate)
        accepted-app-artifact (:accepted-app-execution-artifact candidate)
        trusted-boundary (:trusted-boundary-record candidate)
        claims (:self-hosting-claims proof-contract)
        preserves (set (:preserves proof-contract))
        missing-preserves
        (set/difference p15-s23-rejected-app-required-preserves preserves)
        expected-fixtures (set (map :fixture p15-s23-rejected-app-fixtures))
        observed-fixtures (set (map :fixture records))]
    (vec
     (concat
      (when-not (= :gravity/rejected-app-diagnostic-proof
                   (:artifact proof-contract))
        [(p15-s23-rejected-app-diagnostic-record
          source-path "P15S23E001" proof-contract
          {:missing-fields [:artifact]})])
      (when-not (and (= expected-fixtures observed-fixtures)
                     (empty? missing-preserves))
        [(p15-s23-rejected-app-diagnostic-record
          source-path "P15S23E002" records
          {:expected-fixtures expected-fixtures
           :observed-fixtures observed-fixtures
           :missing-preserves (vec (sort missing-preserves))})])
      (when (some #(not= :rejected (:status %)) records)
        [(p15-s23-rejected-app-diagnostic-record
          source-path "P15S23E003" records
          {:accepted-fixtures
           (mapv :fixture (remove #(= :rejected (:status %)) records))})])
      (when-not (and (= :gravity/p15-s23-rejected-app-diagnostic-preservation-record
                        (:artifact preservation))
                     (= :complete (:status preservation))
                     (true? (:all-fixtures-rejected? preservation))
                     (true? (:all-diagnostics-match? preservation))
                     (true? (:diagnostic-codes-stable? preservation)))
        [(p15-s23-rejected-app-diagnostic-record
          source-path "P15S23E004" preservation
          {:expected-diagnostics
           (mapv :expected-diagnostic p15-s23-rejected-app-fixtures)})])
      (when-not
       (and (= :gravity/p15-s23-accepted-app-execution-artifact
               (:kind accepted-app-artifact))
            (= :gravity/p15-s23-runtime-manifest-capability-enforcement-artifact
               (get-in accepted-app-artifact
                       [:runtime-capability-artifact :kind]))
            (re-find #"^sha256:" (str (:artifact-id accepted-app-artifact)))
            (true? (:clojure-instruction-runner? trusted-boundary))
            (false? (:self-hosted-compiler? trusted-boundary))
            (false? (:clojure-seed-retired? trusted-boundary)))
        [(p15-s23-rejected-app-diagnostic-record
          source-path "P15S23E005" accepted-app-artifact
          {:required-links [:accepted-app-execution-artifact
                            :runtime-capability-artifact
                            :trusted-boundary-record]})])
      (when (or (true? (:full-language-compiler-self-hosted? claims))
                (true? (:clojure-seed-retired? claims)))
        [(p15-s23-rejected-app-diagnostic-record
          source-path "P15S23E006" claims
          {:full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? claims)
           :clojure-seed-retired?
           (:clojure-seed-retired? claims)})])))))

(defn p15-s23-rejected-app-diagnostic-stream
  [source-path proof-id]
  {:artifact :gravity/p15-s23-rejected-app-diagnostic-stream
   :stage :p15-s23-rejected-app-diagnostic-proof
   :source-path source-path
   :proof-id proof-id
   :diagnostics
   (mapv (fn [id]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :p15-s23-rejected-app-diagnostic-proof
            :message (get p15-s23-rejected-app-diagnostic-messages id)})
         p15-s23-rejected-app-diagnostic-ids)
   :status :complete})

(defn p15-s23-rejected-app-rejected-candidates
  [accepted-candidate]
  [{:fixture :internal-p15-s23-rejected-app-missing-proof
    :candidate (assoc accepted-candidate :proof-contract {})
    :expected-diagnostic "P15S23E001"}
   {:fixture :internal-p15-s23-rejected-app-fixture-manifest-gap
    :candidate (assoc accepted-candidate
                      :rejected-app-diagnostic-records [])
    :expected-diagnostic "P15S23E002"}
   {:fixture :internal-p15-s23-rejected-app-accepted-unexpectedly
    :candidate (assoc-in accepted-candidate
                         [:rejected-app-diagnostic-records 0 :status]
                         :accepted-unexpectedly)
    :expected-diagnostic "P15S23E003"}
   {:fixture :internal-p15-s23-rejected-app-diagnostic-mismatch
    :candidate (-> accepted-candidate
                   (assoc-in [:rejected-app-diagnostic-records
                              0 :diagnostic]
                             "WRONG")
                   (assoc-in [:rejected-app-diagnostic-records
                              0 :matches-expected?]
                             false)
                   (assoc-in [:diagnostic-preservation-record
                              :all-diagnostics-match?]
                             false)
                   (assoc-in [:diagnostic-preservation-record :status]
                             :failed))
    :expected-diagnostic "P15S23E004"}
   {:fixture :internal-p15-s23-rejected-app-artifact-link-gap
    :candidate (assoc accepted-candidate
                      :accepted-app-execution-artifact {:kind :wrong})
    :expected-diagnostic "P15S23E005"}
   {:fixture :internal-p15-s23-rejected-app-overclaim
    :candidate (-> accepted-candidate
                   (assoc-in [:proof-contract :self-hosting-claims
                              :full-language-compiler-self-hosted?]
                             true)
                   (assoc-in [:proof-contract :self-hosting-claims
                              :clojure-seed-retired?]
                             true))
    :expected-diagnostic "P15S23E006"}])

(defn p15-s23-rejected-app-rejected-records
  [source-path accepted-candidate]
  (mapv (fn [{:keys [fixture candidate expected-diagnostic]}]
          {:fixture fixture
           :status :rejected
           :expected-diagnostic expected-diagnostic
           :diagnostics
           (p15-s23-rejected-app-proof-diagnostics source-path candidate)})
        (p15-s23-rejected-app-rejected-candidates accepted-candidate)))

(defn p15-s23-rejected-app-diagnostic-proof
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:p15-s23-rejected-app-diagnostic-stream
                           :diagnostics])))
        rejected-diagnostics
        (set (mapcat #(map :diagnostic (:diagnostics %))
                     (:rejected-p15-s23-app-diagnostic-proof-fixtures
                      artifact)))
        preservation (:diagnostic-preservation-record artifact)
        boundary (:trusted-boundary-record artifact)]
    {:rejected-app-diagnostic-proof-authored-in-gravity? true
     :status :in-progress
     :task "P15-S23"
     :fixture-manifest-complete?
     (= (set (map :fixture p15-s23-rejected-app-fixtures))
        (set (map :fixture (:rejected-app-diagnostic-records artifact))))
     :fixtures-rejected?
     (true? (:all-fixtures-rejected? preservation))
     :stable-diagnostics-covered?
     (true? (:diagnostic-codes-stable? preservation))
     :diagnostics-match-expected?
     (true? (:all-diagnostics-match? preservation))
     :accepted-app-proof-linked?
     (= :gravity/p15-s23-accepted-app-execution-artifact
        (get-in artifact [:accepted-app-execution-artifact :kind]))
     :runtime-capability-proof-linked?
     (= :gravity/p15-s23-runtime-manifest-capability-enforcement-artifact
        (get-in artifact
                [:accepted-app-execution-artifact
                 :runtime-capability-artifact :kind]))
     :trusted-boundaries-explicit?
     (and (true? (:clojure-instruction-runner? boundary))
          (false? (:self-hosted-compiler? boundary))
          (false? (:clojure-seed-retired? boundary)))
     :does-not-claim-full-self-hosting?
     (false? (:full-language-compiler-self-hosted? artifact))
     :does-not-claim-clojure-seed-retirement?
     (false? (:clojure-seed-retired? artifact))
     :rejected-candidates-covered?
     (set/subset? (set p15-s23-rejected-app-diagnostic-ids)
                  rejected-diagnostics)
     :diagnostics-covered?
     (= (set p15-s23-rejected-app-diagnostic-ids) diagnostics)
     :limitations
     {:full-language-compiler-self-hosted? false
      :clojure-seed-retired? false
      :clojure-instruction-runner? true
      :full-self-hosted-toolchain? false
      :next-required-capability
      :implement_reproducible_rebuild_log}}))