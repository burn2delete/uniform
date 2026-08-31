

(defn zero-cost-source-artifact
  [source-path source-text]
  (let [performance-artifact (performance-source-artifact source-path
                                                          source-text)
        manifest (:profile-manifest performance-artifact)
        performance (get-in manifest [:metadata :performance] {})
        performance-claim (perf1-normalize-claim (:claim performance))
        suite (:zero-cost performance)
        claims (mapv perf2-normalize-claim (:claims suite))]
    (when (empty? claims)
      (perf2-fail! "PERF2-CLAIM" source-path manifest performance-claim
                   {:claim-id (:suite-id suite)
                    :profile (:profile manifest)
                    :target (:target performance-claim)
                    :abstraction nil
                    :equivalent-form nil
                    :expected-erased-costs #{}
                    :erased-costs #{}
                    :residual-costs #{}
                    :artifacts #{}}
                   {:missing-fields [:claims]
                    :remediation "Provide at least one zero-cost abstraction erasure claim."}))
    (doseq [claim claims]
      (perf2-validate-claim! source-path manifest performance-claim claim))
    (let [capability-proof (zero-cost-capability-proof manifest
                                                       performance-claim
                                                       claims)
          conformance {:document "PERF2"
                       :task "P04-T02"
                       :required-diagnostic-ids perf2-diagnostic-ids
                       :abstraction-families-covered
                       (set (map :abstraction claims))
                       :before-after-ir-status :complete
                       :residual-cost-reporting-status :complete
                       :allocation-boxing-audit-status :complete
                       :dispatch-specialization-status :complete
                       :runtime-check-proof-status :complete
                       :status :complete}]
      {:kind :gravity/stage0-zero-cost-abstraction-artifact
       :document "PERF2"
       :pass {:name :zero-cost-abstraction-validation
              :input :optimization-manifest
              :output :abstraction-erasure-report
              :requires [:performance-claim-validation
                         :typed-core-facts
                         :effect-capability-facts
                         :dispatch-artifacts
                         :compile-time-provenance
                         :safe15-proof-records
                         :before-after-mir]
              :preserves [:source-spans :profile :target :effects
                          :capabilities :safety-mode :profile-legality
                          :proof-index :dispatch-diagnostics]
              :emits [:abstraction-erasure-report
                      :before-after-ir-records
                      :residual-cost-list
                      :allocation-boxing-audit
                      :dispatch-specialization-report
                      :runtime-check-erasure-report
                      :zero-cost-conformance-results]
              :rejects perf2-diagnostic-ids}
       :performance-artifact-hash (str "sha256:"
                                       (sha256-hex
                                        (pr-str performance-artifact)))
       :performance-contract-manifest
       (:performance-contract-manifest performance-artifact)
       :abstraction-erasure-report
       {:suite-id (:suite-id suite)
        :claim-count (count claims)
        :claims (mapv #(select-keys %
                                    [:claim-id :abstraction :equivalent-form
                                     :expected-erased-costs :erased-costs
                                     :residual-costs :semantic-proof
                                     :safety-proof :proof-ids :status])
                      claims)
        :status :complete}
       :before-after-ir-records
       (mapv #(select-keys % [:claim-id :source-operation
                              :before-ir :after-ir])
             claims)
       :residual-cost-list
       (mapv #(select-keys % [:claim-id :residual-costs])
             claims)
       :allocation-boxing-audit
       (mapv #(select-keys % [:claim-id :allocation-audit
                              :boxing-audit])
             claims)
       :dispatch-specialization-report
       (mapv #(select-keys % [:claim-id
                              :dispatch-specialization-report])
             claims)
       :runtime-check-erasure-report
       (mapv #(select-keys % [:claim-id :runtime-check-report])
             claims)
       :capability-based-proof capability-proof
       :zero-cost-conformance-results conformance
       :diagnostics []})))

(def perf3-required-record-fields
  [:record-id :optimization :source-function :profile :target :behavior-facts
   :key :guard :effects :capabilities :semantic-proof :safety-proof
   :artifacts :source-map :cache-key-inputs :invalidation-inputs
   :variant-id :variant-selection])

(def perf3-required-nonempty-record-fields
  #{:record-id :optimization :source-function :profile :target :behavior-facts
    :key :semantic-proof :safety-proof :artifacts
    :cache-key-inputs :invalidation-inputs :variant-id})

(def perf3-required-artifacts
  #{:specialized-mir :guard-table :source-map})

(defn perf3-normalize-record
  [record]
  (assoc record
         :record-id (or (:record-id record) (:id record))
         :source-function (or (:source-function record) (:source record))
         :variant-id (or (:variant-id record)
                         (get-in record [:variant-selection :variant-id]))))

(defn perf3-missing-record-fields
  [record]
  (vec (remove #(if (contains? perf3-required-nonempty-record-fields %)
                  (perf-present? (get record %))
                  (contains? record %))
               perf3-required-record-fields)))

(defn perf3-fail!
  [id source-path manifest performance-claim record extra]
  (fail! id
         (case id
           "PERF3-KEY" "specialization key omits behavior-affecting facts"
           "PERF3-GUARD" "specialized variant lacks a valid guard predicate"
           "PERF3-EFFECT" "partial evaluation uses undeclared build effects"
           "PERF3-HERMETIC" "partial evaluation is not hermetic or replayable"
           "PERF3-SOURCE-MAP" "specialized artifact lacks source map"
           "PERF3-CACHE" "specialization cache omits invalidation inputs"
           "PERF3-PROFILE" "specialized variant is illegal in the active profile"
           "PERF3-PROOF" "specialization erased checks without specialized proof"
           "PERF3-VARIANT" "variant selection is ambiguous or unsafe"
           "specialization record is invalid")
         (merge {:source-span {:source source-path}
                 :profile (:profile manifest)
                 :target (:target manifest)
                 :target-request (:target performance-claim)
                 :source-function (:source-function record)
                 :specialization-key (:key record)
                 :guard (:guard record)
                 :build-effects (get-in record [:partial-evaluation
                                                :build-effects])
                 :variant-id (:variant-id record)
                 :proof-id (:proof-id record)
                 :diagnostic-family :specialization-validation}
                extra)))

(defn perf3-missing-key-inputs
  [record]
  (let [key-map (:key record)]
    (vec (remove #(contains? key-map %)
                 (:behavior-facts record)))))

(defn perf3-required-cache-inputs
  [record]
  (set/union (set (:invalidation-inputs record))
             (set (:behavior-facts record))
             #{:source :macro-expansion :profile :target}))