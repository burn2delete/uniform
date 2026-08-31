

(defn p15-s23-provenance-proof-diagnostics
  [source-path candidate]
  (let [proof-contract (:proof-contract candidate)
        provenance-record (:bootstrap-provenance-record candidate)
        lineage (:compiler-lineage-graph candidate)
        link-table (:stage-evidence-link-table candidate)
        canonical-payload (:canonical-provenance-payload candidate)
        revocation (:revocation-check-report candidate)
        auditor (:auditor-query-index candidate)
        claims (:self-hosting-claims proof-contract)
        preserves (set (:preserves proof-contract))
        missing-preserves
        (set/difference p15-s23-provenance-required-preserves
                        preserves)
        missing-fields
        (set/difference p15-s23-provenance-required-fields
                        (set (keys provenance-record)))]
    (vec
     (concat
      (when-not (= :gravity/bootstrap-provenance-attestation
                   (:artifact proof-contract))
        [(p15-s23-provenance-diagnostic-record
          source-path "P15S23P001" proof-contract
          {:missing-fields [:artifact]})])
      (when (or (seq missing-fields)
                (seq missing-preserves)
                (not= :complete (:status provenance-record)))
        [(p15-s23-provenance-diagnostic-record
          source-path "P15S23P002" provenance-record
          {:missing-fields (vec (sort missing-fields))
           :missing-preserves (vec (sort missing-preserves))})])
      (when-not
       (and (= :complete (:status lineage))
            (true? (:compiler-lineage-explicit? lineage))
            (true? (:acyclic? lineage))
            (true? (:lineage-traversable-to-seed? lineage))
            (= :clojure-stage0
               (get-in lineage
                       [:answers-which-compiler-compiled-this-compiler
                        :compiled-by])))
        [(p15-s23-provenance-diagnostic-record
          source-path "P15S23P003" lineage
          {:required [:explicit-lineage :acyclic-lineage
                      :lineage-traversable-to-seed]})])
      (when-not
       (and (= :complete (:status link-table))
            (true? (:required-links-covered? link-table))
            (= p15-s23-provenance-required-links
               (set (map :link (:links link-table)))))
        [(p15-s23-provenance-diagnostic-record
          source-path "P15S23P004" link-table
          {:required-links (vec (sort p15-s23-provenance-required-links))})])
      (when-not
       (and (= :complete (:status canonical-payload))
            (true? (:canonicalized? canonical-payload))
            (= :verified
               (get-in canonical-payload [:signature :status]))
            (= :canonical-provenance-payload
               (get-in canonical-payload [:signature :signed-over])))
        [(p15-s23-provenance-diagnostic-record
          source-path "P15S23P005" canonical-payload
          {:required [:canonicalized-payload :verified-signature]})])
      (when-not
       (and (= :complete (:status revocation))
            (true? (:revocation-clear? revocation))
            (empty? (:revoked-inputs revocation))
            (= :complete (:status auditor))
            (true? (:auditor-query-passed? auditor)))
        [(p15-s23-provenance-diagnostic-record
          source-path "P15S23P006"
          {:revocation revocation :auditor auditor}
          {:required [:no-revoked-inputs :auditor-query-index]})])
      (when (or (true? (:full-language-compiler-self-hosted? claims))
                (true? (:clojure-seed-retired? claims)))
        [(p15-s23-provenance-diagnostic-record
          source-path "P15S23P007" claims
          {:full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? claims)
           :clojure-seed-retired?
           (:clojure-seed-retired? claims)})])))))

(defn p15-s23-provenance-diagnostic-stream
  [source-path proof-id]
  {:artifact :gravity/p15-s23-provenance-attestation-diagnostic-stream
   :stage :p15-s23-provenance-attestation
   :source-path source-path
   :proof-id proof-id
   :diagnostics
   (mapv (fn [id]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :p15-s23-provenance-attestation
            :message
            (get p15-s23-provenance-diagnostic-messages id)})
         p15-s23-provenance-diagnostic-ids)
   :status :complete})

(defn p15-s23-provenance-rejected-candidates
  [accepted-candidate]
  [{:fixture :internal-p15-s23-provenance-missing-attestation
    :candidate (assoc accepted-candidate :proof-contract {})
    :expected-diagnostic "P15S23P001"}
   {:fixture :internal-p15-s23-provenance-field-gap
    :candidate (update accepted-candidate
                       :bootstrap-provenance-record
                       dissoc
                       :source-graph-hash)
    :expected-diagnostic "P15S23P002"}
   {:fixture :internal-p15-s23-provenance-lineage-gap
    :candidate (assoc-in accepted-candidate
                         [:compiler-lineage-graph
                          :lineage-traversable-to-seed?]
                         false)
    :expected-diagnostic "P15S23P003"}
   {:fixture :internal-p15-s23-provenance-evidence-link-gap
    :candidate (assoc-in accepted-candidate
                         [:stage-evidence-link-table
                          :required-links-covered?]
                         false)
    :expected-diagnostic "P15S23P004"}
   {:fixture :internal-p15-s23-provenance-canonical-gap
    :candidate (assoc-in accepted-candidate
                         [:canonical-provenance-payload
                          :canonicalized?]
                         false)
    :expected-diagnostic "P15S23P005"}
   {:fixture :internal-p15-s23-provenance-revoked-input
    :candidate (-> accepted-candidate
                   (assoc-in [:revocation-check-report
                              :revocation-clear?]
                             false)
                   (assoc-in [:revocation-check-report
                              :revoked-inputs]
                             [:compiler-pipeline-manifest]))
    :expected-diagnostic "P15S23P006"}
   {:fixture :internal-p15-s23-provenance-overclaim
    :candidate (-> accepted-candidate
                   (assoc-in [:proof-contract :self-hosting-claims
                              :full-language-compiler-self-hosted?]
                             true)
                   (assoc-in [:proof-contract :self-hosting-claims
                              :clojure-seed-retired?]
                             true))
    :expected-diagnostic "P15S23P007"}])

(defn p15-s23-provenance-rejected-records
  [source-path accepted-candidate]
  (mapv (fn [{:keys [fixture candidate expected-diagnostic]}]
          {:fixture fixture
           :status :rejected
           :expected-diagnostic expected-diagnostic
           :diagnostics
           (p15-s23-provenance-proof-diagnostics
            source-path candidate)})
        (p15-s23-provenance-rejected-candidates
         accepted-candidate)))

(defn p15-s23-provenance-attestation-proof
  [artifact]
  (let [diagnostics
        (set (map :diagnostic
                  (get-in artifact
                          [:p15-s23-provenance-attestation-diagnostic-stream
                           :diagnostics])))
        rejected-diagnostics
        (set (mapcat #(map :diagnostic (:diagnostics %))
                     (:rejected-p15-s23-provenance-attestation-fixtures
                      artifact)))]
    {:provenance-attestation-authored-in-gravity? true
     :status :in-progress
     :task "P15-S23"
     :required-fields-present?
     (set/subset?
      p15-s23-provenance-required-fields
      (set (keys (:bootstrap-provenance-record artifact))))
     :required-links-covered?
     (true?
      (get-in artifact
              [:stage-evidence-link-table
               :required-links-covered?]))
     :compiler-lineage-traversable?
     (true?
      (get-in artifact
              [:compiler-lineage-graph
               :lineage-traversable-to-seed?]))
     :answers-which-compiler-compiled-this-compiler?
     (= :clojure-stage0
        (get-in artifact
                [:compiler-lineage-graph
                 :answers-which-compiler-compiled-this-compiler
                 :compiled-by]))
     :canonical-payload-signed?
     (and (true?
           (get-in artifact
                   [:canonical-provenance-payload :canonicalized?]))
          (= :verified
             (get-in artifact
                     [:canonical-provenance-payload
                      :signature :status])))
     :revocation-clear?
     (true?
      (get-in artifact
              [:revocation-check-report
               :revocation-clear?]))
     :auditor-query-passed?
     (true?
      (get-in artifact
              [:auditor-query-index
               :auditor-query-passed?]))
     :does-not-claim-full-self-hosting?
     (false? (:full-language-compiler-self-hosted? artifact))
     :does-not-claim-clojure-seed-retirement?
     (false? (:clojure-seed-retired? artifact))
     :release-eligible?
     (true?
      (get-in artifact [:release-policy-record :release-eligible?]))
     :current-candidate-not-release-eligible?
     (false?
      (get-in artifact [:release-policy-record :release-eligible?]))
     :rejected-candidates-covered?
     (set/subset? (set p15-s23-provenance-diagnostic-ids)
                  rejected-diagnostics)
     :diagnostics-covered?
     (= (set p15-s23-provenance-diagnostic-ids) diagnostics)
     :limitations
     {:full-language-compiler-self-hosted? false
      :clojure-seed-retired? false
      :current-candidate-is-clojure-seed? true
      :external-release-signature? false
      :release-eligible? false
      :full-self-hosted-toolchain? false
      :next-required-capability
      :implement_unsafe_audit_report}}))