

(defn domain-ir-validate!
  [source-path artifact]
  (domain-ir-validate-overrides! source-path artifact)
  (let [registrations (:domain-ir-registry artifact)
        domain-artifacts (:domain-ir-artifacts artifact)
        lowering (:lowering-eligibility-matrix artifact)
        fallback (:fallback-records artifact)]
    (doseq [registration registrations]
      (when-not (every? #(perf-present? (get registration %))
                        [:artifact :domain :owner-doc :schema
                         :semantic-anchor :entry-passes :exit-passes
                         :verifier :supported-profiles :target-lowerings
                         :proof-obligations :fallback])
        (domain-ir-fail! "C12-REGISTRATION" source-path artifact
                         registration
                         {:missing-fields [:domain :owner-doc :schema
                                           :semantic-anchor :entry-passes
                                           :exit-passes :verifier
                                           :supported-profiles
                                           :target-lowerings
                                           :proof-obligations :fallback]})))
    (doseq [domain-artifact domain-artifacts]
      (when-not (and (perf-present? (get-in domain-artifact
                                            [:semantic-anchor :mir-ops]))
                     (perf-present? (get-in domain-artifact
                                            [:semantic-anchor :typed-core])))
        (domain-ir-fail! "C12-ANCHOR" source-path artifact
                         domain-artifact
                         {:missing-fields [:semantic-anchor]}))
      (when-not (and (perf-present? (get-in domain-artifact
                                            [:payload :schema]))
                     (= (:schema domain-artifact)
                        (get-in domain-artifact [:payload :schema])))
        (domain-ir-fail! "C12-SCHEMA" source-path artifact
                         domain-artifact
                         {:missing-fields [:payload :schema]}))
      (when-not (every? #(perf-present? (get-in domain-artifact
                                                [:facts %]))
                        [:types :effects :ownership :capabilities
                         :safety :provenance])
        (domain-ir-fail! "C12-FACTS" source-path artifact
                         domain-artifact
                         {:missing-fields [:types :effects :ownership
                                           :capabilities :safety
                                           :provenance]}))
      (when-not (= :accepted (get-in domain-artifact [:verifier :result]))
        (domain-ir-fail! "C12-VERIFY" source-path artifact
                         domain-artifact
                         {:missing-fields [:verifier]}))
      (when-not (some #(= :accepted (:status %)) (:proofs domain-artifact))
        (domain-ir-fail! "C12-PROOF" source-path artifact
                         domain-artifact
                         {:missing-fields [:proofs]}))
      (when-not (= :accepted (get-in domain-artifact [:plugin-policy :status]))
        (domain-ir-fail! "C12-PLUGIN" source-path artifact
                         domain-artifact
                         {:missing-fields [:plugin-policy]})))
    (when (some #(= :unsupported (:status %)) lowering)
      (domain-ir-fail! "C12-LOWERING" source-path artifact
                       (first (filter #(= :unsupported (:status %)) lowering))
                       {:missing-fields [:lowering]}))
    (when-not (every? #(= :available (:status %)) fallback)
      (domain-ir-fail! "C12-FALLBACK" source-path artifact
                       (first (remove #(= :available (:status %)) fallback))
                       {:missing-fields [:fallback]}))
    (when-not (= :passed (get-in artifact [:domain-verifier-report :status]))
      (domain-ir-fail! "C12-VERIFY" source-path artifact
                       (:domain-verifier-report artifact)
                       {:missing-fields [:status]})))
  :complete)

(defn domain-ir-capability-proof
  [artifact]
  (let [domain-artifacts (:domain-ir-artifacts artifact)
        registrations (:domain-ir-registry artifact)
        lowering (:lowering-eligibility-matrix artifact)
        fallback (:fallback-records artifact)]
    {:registrations-complete?
     (every? #(every? (fn [field] (perf-present? (get % field)))
                      [:domain :owner-doc :schema :semantic-anchor
                       :entry-passes :exit-passes :verifier])
             registrations)
     :required-families-covered?
     (= (set domain-ir-required-families)
        (set (map :domain domain-artifacts)))
     :semantic-anchors-linked?
     (every? #(and (perf-present? (get-in % [:semantic-anchor :mir-ops]))
                   (perf-present? (get-in % [:semantic-anchor :typed-core])))
             domain-artifacts)
     :facts-preserved?
     (every? #(every? (fn [fact] (perf-present? (get-in % [:facts fact])))
                      [:types :effects :ownership :capabilities
                       :safety :provenance])
             domain-artifacts)
     :verifiers-accepted?
     (every? #(= :accepted (get-in % [:verifier :result])) domain-artifacts)
     :proof-evidence-present?
     (every? #(some (fn [proof] (= :accepted (:status proof)))
                    (:proofs %))
             domain-artifacts)
     :lowering-or-fallback-present?
     (and (every? #(contains? #{:eligible :fallback} (:status %)) lowering)
          (every? #(= :available (:status %)) fallback))
     :plugin-policy-enforced?
     (every? #(= :accepted (get-in % [:plugin-policy :status]))
             domain-artifacts)
     :status :complete}))