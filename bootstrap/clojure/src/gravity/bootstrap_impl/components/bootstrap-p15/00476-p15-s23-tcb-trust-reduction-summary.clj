

(defn p15-s23-tcb-trust-reduction-summary
  [baseline current classification residual]
  {:artifact :gravity/p15-s23-trust-reduction-summary
   :baseline-trusted-count (count (:trusted-components baseline))
   :current-residual-trusted-count
   (count (:trusted-components current))
   :retired-component-count (count (:retired-components classification))
   :reduced-component-count (count (:reduced-components classification))
   :evidence-control-count
   (count (:evidence-controlled-components current))
   :whole-language-tcb-reduced? false
   :reader-subset-prior-reductions-recorded? true
   :clojure-seed-retired? false
   :residual-trust-boundary-count
   (count (:residual-boundaries residual))
   :count-reconciliation
   {:baseline-count-matches?
    (= (:trusted-component-count baseline)
       (count (:trusted-components baseline)))
    :current-count-matches?
    (= (:trusted-component-count current)
       (count (:trusted-components current)))
    :evidence-control-count-matches?
    (= (:evidence-control-count current)
       (count (:evidence-controlled-components current)))
    :retired-count-matches?
    (= 0 (count (:retired-components classification)))}
   :status :complete})

(defn p15-s23-tcb-auditor-query-record
  [baseline current classification residual links]
  {:artifact :gravity/p15-s23-tcb-auditor-query-record
   :queries
   [{:query :which-components-remain-trusted
     :answer (mapv :component (:trusted-components current))
     :status :answered}
    {:query :which-baseline-components-were-retired
     :answer (:retired-components classification)
     :status :answered}
    {:query :is-clojure-seed-still-in-tcb
     :answer (:clojure-seed-still-trusted? residual)
     :status :answered}
    {:query :which-evidence-links-support-delta
     :answer (mapv :link (:links links))
     :status :answered}]
   :baseline-components-accounted?
   (empty? (:unclassified-baseline-components classification))
   :required-links-covered? (:required-links-covered? links)
   :no-unaccounted-trusted-components?
   (and (empty? (:unclassified-baseline-components classification))
        (set/subset? p15-s23-tcb-required-residual-boundaries
                     (set (map :component
                               (:trusted-components current)))))
   :status :complete})

(defn p15-s23-tcb-delta-record
  [source-path baseline current classification residual links summary auditor]
  (let [record-base
        {:artifact :gravity/trusted-computing-base-delta-record
         :source-path source-path
         :bootstrap-stage :p15-s23
         :baseline-tcb-inventory-id (:inventory-id baseline)
         :current-tcb-inventory-id (:inventory-id current)
         :delta-classification-id (c4-artifact-id classification)
         :residual-trust-boundary-id (c4-artifact-id residual)
         :evidence-link-table-id (c4-artifact-id links)
         :trust-reduction-summary-id (c4-artifact-id summary)
         :auditor-query-record-id (c4-artifact-id auditor)
         :baseline-trusted-count (:baseline-trusted-count summary)
         :current-residual-trusted-count
         (:current-residual-trusted-count summary)
         :retired-component-count (:retired-component-count summary)
         :reduced-component-count (:reduced-component-count summary)
         :whole-language-tcb-reduced?
         (:whole-language-tcb-reduced? summary)
         :clojure-seed-retired?
         (:clojure-seed-retired? summary)
         :residual-trust-boundaries (:residual-boundaries residual)
         :status :complete}
        record-id (c4-artifact-id record-base)]
    (assoc record-base :tcb-delta-record-id record-id)))

(defn p15-s23-tcb-proof-diagnostics
  [source-path candidate]
  (let [proof-contract (:proof-contract candidate)
        baseline (:baseline-tcb-inventory candidate)
        current (:current-tcb-inventory candidate)
        classification (:tcb-delta-classification candidate)
        residual (:residual-trust-boundary-record candidate)
        links (:tcb-evidence-link-table candidate)
        summary (:trust-reduction-summary candidate)
        auditor (:tcb-auditor-query-record candidate)
        claims (:self-hosting-claims proof-contract)
        preserves (set (:preserves proof-contract))
        missing-preserves
        (set/difference p15-s23-tcb-required-preserves preserves)]
    (vec
     (concat
      (when-not (= :gravity/trusted-computing-base-delta-record
                   (:artifact proof-contract))
        [(p15-s23-tcb-diagnostic-record
          source-path "P15S23T001" proof-contract
          {:missing-fields [:artifact]})])
      (when (or (seq missing-preserves)
                (not= :complete (:status baseline))
                (not= :complete (:status current))
                (empty? (:trusted-components baseline))
                (empty? (:trusted-components current)))
        [(p15-s23-tcb-diagnostic-record
          source-path "P15S23T002"
          {:baseline baseline :current current}
          {:missing-preserves (vec (sort missing-preserves))
           :required-inventories [:baseline-tcb-inventory
                                  :current-tcb-inventory]})])
      (when-not
       (and (= :complete (:status classification))
            (true? (:classification-complete? classification))
            (empty? (:unclassified-baseline-components classification)))
        [(p15-s23-tcb-diagnostic-record
          source-path "P15S23T003" classification
          {:required [:classified-baseline-components
                      :empty-unclassified-baseline-components]})])
      (when-not
       (and (= :complete (:status residual))
            (true? (:clojure-seed-still-trusted? residual))
            (empty? (:missing-required-residual-boundaries residual)))
        [(p15-s23-tcb-diagnostic-record
          source-path "P15S23T004" residual
          {:required-residual-boundaries
           (vec (sort p15-s23-tcb-required-residual-boundaries))})])
      (when-not
       (and (= :complete (:status links))
            (true? (:required-links-covered? links))
            (= p15-s23-tcb-required-evidence-links
               (set (map :link (:links links)))))
        [(p15-s23-tcb-diagnostic-record
          source-path "P15S23T005" links
          {:required-links
           (vec (sort p15-s23-tcb-required-evidence-links))})])
      (when-not
       (and (= :complete (:status summary))
            (= (:baseline-trusted-count summary)
               (count (:trusted-components baseline)))
            (= (:current-residual-trusted-count summary)
               (count (:trusted-components current)))
            (= (:evidence-control-count summary)
               (count (:evidence-controlled-components current)))
            (false? (:whole-language-tcb-reduced? summary))
            (false? (:clojure-seed-retired? summary))
            (true? (:no-unaccounted-trusted-components? auditor)))
        [(p15-s23-tcb-diagnostic-record
          source-path "P15S23T006" summary
          {:required [:matching-counts
                      :explicit-no-whole-language-tcb-reduction
                      :no-unaccounted-trusted-components]})])
      (when (or (true? (:full-language-compiler-self-hosted? claims))
                (true? (:clojure-seed-retired? claims)))
        [(p15-s23-tcb-diagnostic-record
          source-path "P15S23T007" claims
          {:full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? claims)
           :clojure-seed-retired?
           (:clojure-seed-retired? claims)})])))))

(defn p15-s23-tcb-diagnostic-stream
  [source-path proof-id]
  {:artifact :gravity/p15-s23-tcb-delta-diagnostic-stream
   :stage :p15-s23-tcb-delta-record
   :source-path source-path
   :proof-id proof-id
   :diagnostics
   (mapv (fn [id]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :p15-s23-tcb-delta-record
            :message (get p15-s23-tcb-diagnostic-messages id)})
         p15-s23-tcb-diagnostic-ids)
   :status :complete})

(defn p15-s23-tcb-rejected-candidates
  [accepted-candidate]
  [{:fixture :internal-p15-s23-tcb-missing-record
    :candidate (assoc accepted-candidate :proof-contract {})
    :expected-diagnostic "P15S23T001"}
   {:fixture :internal-p15-s23-tcb-inventory-gap
    :candidate (assoc-in accepted-candidate
                         [:baseline-tcb-inventory :trusted-components]
                         [])
    :expected-diagnostic "P15S23T002"}
   {:fixture :internal-p15-s23-tcb-classification-gap
    :candidate (-> accepted-candidate
                   (assoc-in [:tcb-delta-classification
                              :classification-complete?]
                             false)
                   (assoc-in [:tcb-delta-classification
                              :unclassified-baseline-components]
                             [:clojure-stage0-bootstrap]))
    :expected-diagnostic "P15S23T003"}
   {:fixture :internal-p15-s23-tcb-residual-gap
    :candidate (-> accepted-candidate
                   (assoc-in [:residual-trust-boundary-record
                              :clojure-seed-still-trusted?]
                             false)
                   (assoc-in [:residual-trust-boundary-record
                              :missing-required-residual-boundaries]
                             [:clojure-stage0-bootstrap]))
    :expected-diagnostic "P15S23T004"}
   {:fixture :internal-p15-s23-tcb-evidence-link-gap
    :candidate (assoc-in accepted-candidate
                         [:tcb-evidence-link-table
                          :required-links-covered?]
                         false)
    :expected-diagnostic "P15S23T005"}
   {:fixture :internal-p15-s23-tcb-delta-mismatch
    :candidate (assoc-in accepted-candidate
                         [:trust-reduction-summary
                          :baseline-trusted-count]
                         99)
    :expected-diagnostic "P15S23T006"}
   {:fixture :internal-p15-s23-tcb-overclaim
    :candidate (-> accepted-candidate
                   (assoc-in [:proof-contract :self-hosting-claims
                              :full-language-compiler-self-hosted?]
                             true)
                   (assoc-in [:proof-contract :self-hosting-claims
                              :clojure-seed-retired?]
                             true))
    :expected-diagnostic "P15S23T007"}])

(defn p15-s23-tcb-rejected-records
  [source-path accepted-candidate]
  (mapv (fn [{:keys [fixture candidate expected-diagnostic]}]
          {:fixture fixture
           :status :rejected
           :expected-diagnostic expected-diagnostic
           :diagnostics
           (p15-s23-tcb-proof-diagnostics source-path candidate)})
        (p15-s23-tcb-rejected-candidates accepted-candidate)))