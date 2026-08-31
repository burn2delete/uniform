

(defn p15-s23-core-diagnostic-c15-artifact
  [source-path c6-artifact]
  (let [diagnostics
        (mapv #(p15-s23-core-diagnostic-preserved-diagnostic
                source-path c6-artifact %1 %2)
              p15-s23-core-diagnostic-ids
              (range))
        preservation-report
        {:artifact :gravity/p15-s23-diagnostic-preservation-report
         :source-path source-path
         :core-lowering-artifact-id (:artifact-id c6-artifact)
         :diagnostic-rules (mapv :rule diagnostics)
         :stable-ids?
         (every? #(= (:diagnostic-id %)
                     (p15-s23-core-diagnostic-stable-id
                      (dissoc % :diagnostic-id)))
                 diagnostics)
         :source-spans-preserved?
         (every? #(p15-s23-source-syntax-span-resolves?
                   (get-in % [:primary :span]))
                 diagnostics)
         :syntax-identities-preserved?
         (every? #(get-in % [:primary :syntax-id]) diagnostics)
         :origin-chains-preserved?
         (every? #(seq (:origin-chain %)) diagnostics)
         :remediation-preserved?
         (every? #(seq (:remediation %)) diagnostics)
         :status :complete}
        artifact-base
        {:kind :gravity/stage0-c15-compiler-diagnostics-artifact
         :task "P15-S23"
         :document-set ["C15"]
         :governing-document c15-diagnostics-governing-document
         :pass {:name :p15-s23-diagnostic-preservation-proof
                :input :p15-s23-core-lowering-evidence
                :output :p15-s23-diagnostic-preservation-evidence
                :requires [:core-ast-module :surface-to-core-map
                           :diagnostic-schema]
                :preserves [:diagnostic-codes :source-spans
                            :syntax-identity :origin-chain
                            :artifact-provenance :remediation]
                :emits [:diagnostic-preservation-report
                        :diagnostic-stream :golden-diagnostic-fixtures]
                :rejects p15-s23-core-diagnostic-ids}
         :c6-core-lowering-artifact
         (select-keys c6-artifact
                      [:kind :artifact-id :core-ast-module
                       :surface-to-core-map :core-verifier-report
                       :capability-based-proof])
         :diagnostic-schema
         {:artifact :gravity/diagnostic-schema
          :status :complete
          :required-fields c15-diagnostic-required-fields
          :stable-id-input [:rule :primary-artifact :stage :facts]}
         :diagnostic-stream
         {:artifact :gravity/diagnostic-stream
          :stage :p15-s23-core-lowering-diagnostic-preservation-report
          :input-artifact (:artifact-id c6-artifact)
          :diagnostics diagnostics
          :summary (frequencies (map :severity diagnostics))
          :deterministic-ordering-key :ordering-key
          :redaction-policy :public-safe
          :status :complete}
         :diagnostic-preservation-report preservation-report
         :golden-diagnostic-fixtures
         (mapv (fn [id]
                 {:fixture (str "p15-s23-core-diagnostic-" id)
                  :rule id
                  :asserts [:rule :severity :primary :related :stage
                            :profile :target :facts :remediation
                            :ordering]
                  :status :matched})
               p15-s23-core-diagnostic-ids)
         :p15-s23-diagnostic-preservation-results
         {:diagnostic-count (count diagnostics)
          :status :complete}
         :diagnostics []}
        proof {:c6-core-lowering-input-linked? true
               :diagnostic-schema-complete? true
               :diagnostic-stream-deterministic?
               (= diagnostics (vec (sort-by :ordering-key diagnostics)))
               :stable-diagnostic-ids?
               (:stable-ids? preservation-report)
               :source-spans-preserved?
               (:source-spans-preserved? preservation-report)
               :syntax-identities-preserved?
               (:syntax-identities-preserved? preservation-report)
               :origin-chains-preserved?
               (:origin-chains-preserved? preservation-report)
               :remediation-preserved?
               (:remediation-preserved? preservation-report)
               :golden-fixtures-matched?
               (every? #(= :matched (:status %))
                       (:golden-diagnostic-fixtures artifact-base))
               :diagnostics-covered?
               (= (set p15-s23-core-diagnostic-ids)
                  (set (map :rule diagnostics)))
               :status :complete}
        artifact (assoc artifact-base :capability-based-proof proof)]
    (assoc artifact :artifact-id (c4-artifact-id artifact))))

(defn p15-s23-core-diagnostic-proof-diagnostics
  [source-path candidate]
  (let [proof-contract (:proof-contract candidate)
        c6-artifact (:c6-core-lowering-artifact candidate)
        c15-artifact (:c15-diagnostics-artifact candidate)
        source-syntax (:source-syntax-artifact candidate)
        pipeline (:compiler-pipeline-manifest-artifact candidate)
        preservation (:diagnostic-preservation-report candidate)
        claims (:self-hosting-claims proof-contract)
        preserves (set (:preserves proof-contract))
        missing-preserves
        (set/difference p15-s23-core-diagnostic-required-preserves
                        preserves)]
    (vec
     (concat
      (when-not (= :gravity/core-lowering-and-diagnostic-preservation-report
                   (:artifact proof-contract))
        [(p15-s23-core-diagnostic-record
          source-path "P15S23D001" proof-contract
          {:missing-fields [:artifact]})])
      (when-not (and (= :gravity/stage0-c6-core-lowering-artifact
                        (:kind c6-artifact))
                     (= :passed
                        (get-in c6-artifact
                                [:core-verifier-report :status]))
                     (seq (:core-node-table c6-artifact))
                     (true?
                      (get-in c6-artifact
                              [:capability-based-proof
                               :source-spans-preserved?]))
                     (true?
                      (get-in c6-artifact
                              [:capability-based-proof
                               :origin-chains-preserved?]))
                     (empty? missing-preserves))
        [(p15-s23-core-diagnostic-record
          source-path "P15S23D002" c6-artifact
          {:missing-preserves (vec (sort missing-preserves))
           :core-verifier-status
           (get-in c6-artifact [:core-verifier-report :status])})])
      (when-not (and (= :gravity/stage0-c15-compiler-diagnostics-artifact
                        (:kind c15-artifact))
                     (= :complete (:status preservation))
                     (true? (:stable-ids? preservation))
                     (true? (:source-spans-preserved? preservation))
                     (true? (:syntax-identities-preserved? preservation))
                     (true? (:origin-chains-preserved? preservation))
                     (true? (:remediation-preserved? preservation))
                     (= (set p15-s23-core-diagnostic-ids)
                        (set (:diagnostic-rules preservation))))
        [(p15-s23-core-diagnostic-record
          source-path "P15S23D003" preservation
          {:expected-diagnostics p15-s23-core-diagnostic-ids})])
      (when-not (and (= :gravity/p15-s23-source-syntax-serialization-proof-artifact
                        (:kind source-syntax))
                     (= :gravity/p15-s23-compiler-pipeline-manifest-artifact
                        (:kind pipeline))
                     (= (:artifact-id c6-artifact)
                        (get-in c15-artifact
                                [:c6-core-lowering-artifact :artifact-id])))
        [(p15-s23-core-diagnostic-record
          source-path "P15S23D004"
          {:source-syntax source-syntax
           :pipeline pipeline
           :c6 (:artifact-id c6-artifact)
           :c15-input
           (get-in c15-artifact
                   [:c6-core-lowering-artifact :artifact-id])}
          {:missing-fields [:artifact-links]})])
      (when (or (true? (:full-language-compiler-self-hosted? claims))
                (true? (:clojure-seed-retired? claims)))
        [(p15-s23-core-diagnostic-record
          source-path "P15S23D005" claims
          {:full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? claims)
           :clojure-seed-retired? (:clojure-seed-retired? claims)})])))))