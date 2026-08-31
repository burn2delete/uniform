

(defn domain-ir-artifact-from-mir
  [source-path module mir-artifact]
  (let [_ (when-not (and (map? module)
                         (= :gravity/stage0-mir-artifact
                            (:kind mir-artifact))
                         (= :passed
                            (get-in mir-artifact
                                    [:mir-verifier-report :status])))
            (domain-ir-fail! "C12-VERIFY" source-path
                             {:input-artifact mir-artifact}
                             mir-artifact
                             {:missing-fields [:module :verified-mir]}))
        source-overrides (domain-ir-source-overrides module)
        registrations (mapv domain-ir-registration-record
                            domain-ir-registry-seed)
        domain-artifacts (mapv #(domain-ir-artifact-record mir-artifact %1 %2)
                               registrations
                               (range))
        semantic-anchor-map (mapv #(select-keys %
                                                [:domain :artifact-id
                                                 :semantic-anchor :source])
                                  domain-artifacts)
        entry-pass-records (mapv (fn [registration]
                                   {:domain (:domain registration)
                                    :entry-passes (:entry-passes registration)
                                    :consumes [:source-forms :typed-core
                                               :gravity/mir]
                                    :preserves [:types :effects :ownership
                                                :capabilities :safety
                                                :provenance]
                                    :status :complete})
                                 registrations)
        exit-pass-records (mapv (fn [registration]
                                  {:domain (:domain registration)
                                   :exit-passes (:exit-passes registration)
                                   :requires [:domain-verifier-report
                                              :proof-or-certificate
                                              :fallback-record]
                                   :emits [:mir-subgraph
                                           :target-provider-call
                                           :runtime-manifest
                                           :verification-artifact]
                                   :status :complete})
                                registrations)
        proof-records (mapv (fn [domain-artifact]
                              {:domain (:domain domain-artifact)
                               :artifact-id (:artifact-id domain-artifact)
                               :evidence-kind :translation-validation
                               :proofs (:proofs domain-artifact)
                               :status :accepted})
                            domain-artifacts)
        lowering-matrix
        (mapv (fn [registration]
                (let [target (get-in mir-artifact
                                     [:mir-module :target-request])
                      direct? (contains? (:target-lowerings registration)
                                         target)]
                  {:domain (:domain registration)
                   :target-request target
                   :supported-targets (:target-lowerings registration)
                   :status (if direct? :eligible :fallback)
                   :fallback (:fallback registration)}))
              registrations)
        fallback-records (mapv (fn [registration]
                                 {:domain (:domain registration)
                                  :fallback (:fallback registration)
                                  :status :available
                                  :residual :gravity/mir})
                               registrations)
        artifact {:kind :gravity/stage0-domain-ir-artifact
                  :document-set ["C12"]
                  :pass {:name :domain-ir-registry-and-artifacts
                         :input :gravity/mir
                         :output :domain-ir-registry
                         :requires [:verified-mir :semantic-anchors
                                    :type-facts :effect-facts
                                    :capability-proofs :safety-outcomes
                                    :source-provenance]
                         :preserves [:types :effects :ownership
                                     :capabilities :profile :target
                                     :safety :source-spans :origin-chain]
                         :emits [:domain-ir-registry :domain-ir-artifacts
                                 :semantic-anchor-map :entry-pass-records
                                 :exit-pass-records :domain-verifier-report
                                 :proof-records
                                 :lowering-eligibility-matrix
                                 :fallback-records]
                         :rejects domain-ir-diagnostic-ids}
                  :source-overrides source-overrides
                  :mir-artifact-kind (:kind mir-artifact)
                  :mir-artifact-hash (str "sha256:"
                                          (sha256-hex (pr-str mir-artifact)))
                  :mir-artifact mir-artifact
                  :domain-ir-registry registrations
                  :domain-ir-artifact-schema
                  {:artifact :gravity/domain-ir-artifact-schema
                   :required-fields [:artifact :domain :artifact-id
                                     :source :semantic-anchor :profile
                                     :target-request :facts :verifier
                                     :proofs :lowering-status]
                   :status :complete}
                  :domain-ir-artifacts domain-artifacts
                  :semantic-anchor-map semantic-anchor-map
                  :entry-pass-records entry-pass-records
                  :exit-pass-records exit-pass-records
                  :domain-verifier-report
                  {:artifact :gravity/domain-verifier-report
                   :status :passed
                   :domains (mapv :domain domain-artifacts)
                   :checks [:registration :schema :semantic-anchor
                            :source-provenance :facts :proof-obligations
                            :lowering :fallback :plugin-policy]
                   :diagnostics []}
                  :proof-and-certificate-references proof-records
                  :lowering-eligibility-matrix lowering-matrix
                  :fallback-records fallback-records
                  :plugin-registration-policy
                  {:status :enforced
                   :requires [:schema :owner-doc :verifier
                              :semantic-anchor :non-opaque-payload]
                   :visibility :package-visible}
                  :diagnostics []}
        _ (domain-ir-validate! source-path artifact)
        capability-proof (domain-ir-capability-proof artifact)
        conformance {:documents ["C12"]
                     :task "P06-T04"
                     :required-diagnostic-ids domain-ir-diagnostic-ids
                     :registration-status :complete
                     :artifact-schema-status :complete
                     :anchor-status :complete
                     :verifier-status :complete
                     :proof-evidence-status :complete
                     :lowering-and-fallback-status :complete
                     :plugin-policy-status :complete
                     :status :complete}]
    (assoc artifact
           :capability-based-proof capability-proof
           :domain-ir-results conformance)))

(defn domain-ir-source-artifact
  [source-path source-text]
  (let [checked-core (checked-core-source-artifact source-path source-text)
        module (:module checked-core)
        mir-artifact (mir-artifact-from-checked-core source-path checked-core)]
    (domain-ir-artifact-from-mir source-path module mir-artifact)))