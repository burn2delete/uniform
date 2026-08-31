

(defn p15-s23-rebuild-comparison-row
  [first-row second-row]
  {:stage (:stage first-row)
   :first (select-keys first-row
                       [:kind :artifact-id :proof-id :manifest-id
                        :serialization-id])
   :second (select-keys second-row
                        [:kind :artifact-id :proof-id :manifest-id
                         :serialization-id])
   :artifact-id-match? (= (:artifact-id first-row)
                          (:artifact-id second-row))
   :proof-id-match? (= (:proof-id first-row)
                       (:proof-id second-row))
   :manifest-id-match? (= (:manifest-id first-row)
                          (:manifest-id second-row))
   :serialization-id-match? (= (:serialization-id first-row)
                               (:serialization-id second-row))
   :diagnostics-match? (= (:diagnostics first-row)
                          (:diagnostics second-row))})

(defn p15-s23-artifact-identity-comparison
  [first-rebuild second-rebuild]
  (let [rows (mapv p15-s23-rebuild-comparison-row
                   (:stages first-rebuild)
                   (:stages second-rebuild))
        all-match?
        (every? #(and (:artifact-id-match? %)
                      (:proof-id-match? %)
                      (:manifest-id-match? %)
                      (:serialization-id-match? %)
                      (:diagnostics-match? %))
                rows)]
    {:artifact :gravity/p15-s23-artifact-identity-comparison
     :rows rows
     :all-artifact-identities-match? all-match?
     :status (if all-match? :complete :failed)}))

(defn p15-s23-rebuild-environment-provenance-record
  [source-path proof-contract]
  {:artifact :gravity/p15-s23-rebuild-environment-provenance-record
   :source-path source-path
   :source-id (str "sha256:" (sha256-hex (slurp source-path)))
   :seed-boundary :clojure-stage0
   :host-runtime :clojure/jvm
   :ambient-authority-denied? true
   :commands
   (mapv (fn [stage]
           {:stage stage
            :command
            (str "clojure -M:gravity "
                 (get p15-s23-reproducible-rebuild-stage-commands
                      stage)
                 " "
                 source-path)})
         p15-s23-reproducible-rebuild-stages)
   :proof-contract-artifact (:artifact proof-contract)
   :status :complete})

(defn p15-s23-reproducible-rebuild-proof-diagnostics
  [source-path candidate]
  (let [proof-contract (:proof-contract candidate)
        first-rebuild (:first-rebuild-record candidate)
        second-rebuild (:second-rebuild-record candidate)
        comparison (:artifact-identity-comparison candidate)
        environment (:environment-provenance-record candidate)
        stages (set (map :stage (:stages first-rebuild)))
        claims (:self-hosting-claims proof-contract)
        preserves (set (:preserves proof-contract))
        missing-preserves
        (set/difference p15-s23-reproducible-rebuild-required-preserves
                        preserves)]
    (vec
     (concat
      (when-not (= :gravity/reproducible-rebuild-log
                   (:artifact proof-contract))
        [(p15-s23-reproducible-rebuild-diagnostic-record
          source-path "P15S23B001" proof-contract
          {:missing-fields [:artifact]})])
      (when-not
       (and (= (set p15-s23-reproducible-rebuild-stages) stages)
            (= stages (set (map :stage (:stages second-rebuild))))
            (empty? missing-preserves))
        [(p15-s23-reproducible-rebuild-diagnostic-record
          source-path "P15S23B002" first-rebuild
          {:expected-stages p15-s23-reproducible-rebuild-stages
           :observed-stages (vec (sort stages))
           :missing-preserves (vec (sort missing-preserves))})])
      (when-not
       (and (= :gravity/p15-s23-artifact-identity-comparison
               (:artifact comparison))
            (= :complete (:status comparison))
            (true? (:all-artifact-identities-match? comparison)))
        [(p15-s23-reproducible-rebuild-diagnostic-record
          source-path "P15S23B003" comparison
          {:mismatched-stages
           (mapv :stage (remove :artifact-id-match?
                                (:rows comparison)))})])
      (when-not
       (and (contains? stages :accepted-app-execution-proof)
            (contains? stages :rejected-app-diagnostic-proof))
        [(p15-s23-reproducible-rebuild-diagnostic-record
          source-path "P15S23B004" first-rebuild
          {:required-links [:accepted-app-execution-proof
                            :rejected-app-diagnostic-proof]})])
      (when-not
       (and (= :gravity/p15-s23-rebuild-environment-provenance-record
               (:artifact environment))
            (= :complete (:status environment))
            (= :clojure-stage0 (:seed-boundary environment))
            (true? (:ambient-authority-denied? environment))
            (= (count p15-s23-reproducible-rebuild-stages)
               (count (:commands environment))))
        [(p15-s23-reproducible-rebuild-diagnostic-record
          source-path "P15S23B005" environment
          {:required-fields [:seed-boundary :commands
                             :ambient-authority-denied?]})])
      (when (or (true? (:full-language-compiler-self-hosted? claims))
                (true? (:clojure-seed-retired? claims)))
        [(p15-s23-reproducible-rebuild-diagnostic-record
          source-path "P15S23B006" claims
          {:full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? claims)
           :clojure-seed-retired?
           (:clojure-seed-retired? claims)})])))))

(defn p15-s23-reproducible-rebuild-diagnostic-stream
  [source-path proof-id]
  {:artifact :gravity/p15-s23-reproducible-rebuild-diagnostic-stream
   :stage :p15-s23-reproducible-rebuild-log
   :source-path source-path
   :proof-id proof-id
   :diagnostics
   (mapv (fn [id]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :p15-s23-reproducible-rebuild-log
            :message (get p15-s23-reproducible-rebuild-diagnostic-messages
                          id)})
         p15-s23-reproducible-rebuild-diagnostic-ids)
   :status :complete})

(defn p15-s23-reproducible-rebuild-rejected-candidates
  [accepted-candidate]
  [{:fixture :internal-p15-s23-rebuild-missing-log
    :candidate (assoc accepted-candidate :proof-contract {})
    :expected-diagnostic "P15S23B001"}
   {:fixture :internal-p15-s23-rebuild-input-gap
    :candidate (update-in accepted-candidate
                          [:first-rebuild-record :stages]
                          subvec 0 3)
    :expected-diagnostic "P15S23B002"}
   {:fixture :internal-p15-s23-rebuild-nondeterministic-artifact
    :candidate (assoc-in accepted-candidate
                         [:artifact-identity-comparison :status]
                         :failed)
    :expected-diagnostic "P15S23B003"}
   {:fixture :internal-p15-s23-rebuild-link-gap
    :candidate (update-in accepted-candidate
                          [:first-rebuild-record :stages]
                          #(vec (remove (fn [row]
                                           (= :rejected-app-diagnostic-proof
                                              (:stage row)))
                                         %)))
    :expected-diagnostic "P15S23B004"}
   {:fixture :internal-p15-s23-rebuild-environment-gap
    :candidate (assoc-in accepted-candidate
                         [:environment-provenance-record
                          :ambient-authority-denied?]
                         false)
    :expected-diagnostic "P15S23B005"}
   {:fixture :internal-p15-s23-rebuild-overclaim
    :candidate (-> accepted-candidate
                   (assoc-in [:proof-contract :self-hosting-claims
                              :full-language-compiler-self-hosted?]
                             true)
                   (assoc-in [:proof-contract :self-hosting-claims
                              :clojure-seed-retired?]
                             true))
    :expected-diagnostic "P15S23B006"}])

(defn p15-s23-reproducible-rebuild-rejected-records
  [source-path accepted-candidate]
  (mapv (fn [{:keys [fixture candidate expected-diagnostic]}]
          {:fixture fixture
           :status :rejected
           :expected-diagnostic expected-diagnostic
           :diagnostics
           (p15-s23-reproducible-rebuild-proof-diagnostics
            source-path candidate)})
        (p15-s23-reproducible-rebuild-rejected-candidates
         accepted-candidate)))