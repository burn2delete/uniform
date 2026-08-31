

(def perf2-required-claim-fields
  [:claim-id :profile :target :abstraction :equivalent-form
   :expected-erased-costs :erased-costs :residual-costs
   :semantic-proof :safety-proof :proof-ids :artifacts
   :source-operation :before-ir :after-ir])

(def perf2-required-nonempty-claim-fields
  #{:claim-id :profile :target :abstraction :equivalent-form
    :expected-erased-costs :semantic-proof :safety-proof :proof-ids
    :artifacts :source-operation :before-ir :after-ir})

(def perf2-required-artifacts
  #{:before-mir :after-mir})

(def perf2-abstraction-families
  #{:protocol-dispatch :interface-call :generic-function :record-field-access
    :newtype-wrapper :wrapper-value :iterator-pipeline
    :collection-view :pattern-match :macro :facet-generated-form
    :higher-order-function :error-result-wrapper :resource-wrapper
    :numeric-mode-wrapper :standard-library-wrapper})

(def perf2-cost-diagnostic
  {:allocation "PERF2-ALLOCATION"
   :hidden-allocation "PERF2-ALLOCATION"
   :boxing "PERF2-BOXING"
   :unboxing "PERF2-BOXING"
   :representation-conversion "PERF2-BOXING"
   :dynamic-dispatch "PERF2-DISPATCH"
   :vtable-lookup "PERF2-DISPATCH"
   :reflection "PERF2-REFLECTION"
   :host-reflection "PERF2-REFLECTION"
   :runtime-bounds-check "PERF2-CHECK"
   :runtime-type-check "PERF2-CHECK"
   :runtime-check "PERF2-CHECK"})

(defn perf2-normalize-claim
  [claim]
  (assoc claim
         :claim-id (or (:claim-id claim) (:claim claim))
         :expected-erased-costs
         (or (:expected-erased-costs claim) (:erased-costs claim) #{})
         :residual-costs (or (:residual-costs claim) #{})))

(defn perf2-missing-claim-fields
  [claim]
  (vec (remove #(if (contains? perf2-required-nonempty-claim-fields %)
                  (perf-present? (get claim %))
                  (contains? claim %))
               perf2-required-claim-fields)))

(defn perf2-fail!
  [id source-path manifest performance-claim claim extra]
  (fail! id
         (case id
           "PERF2-CLAIM" "zero-cost abstraction claim is incomplete"
           "PERF2-RESIDUAL" "residual work contradicts zero-cost claim"
           "PERF2-ALLOCATION" "zero-cost claim hides allocation"
           "PERF2-BOXING" "zero-cost claim hides boxing or representation conversion"
           "PERF2-DISPATCH" "zero-cost claim leaves dynamic dispatch unerased"
           "PERF2-REFLECTION" "zero-cost claim hides host reflection"
           "PERF2-CHECK" "zero-cost claim leaves runtime checks unaccounted"
           "PERF2-PROFILE" "zero-cost erasure relies on profile-illegal behavior"
           "PERF2-EVIDENCE" "zero-cost claim is missing IR or proof evidence"
           "zero-cost abstraction claim is invalid")
         (merge {:source-span {:source source-path}
                 :profile (:profile manifest)
                 :target (:target manifest)
                 :target-request (:target performance-claim)
                 :claim-id (:claim-id claim)
                 :abstraction (:abstraction claim)
                 :equivalent-form (:equivalent-form claim)
                 :expected-erased-costs (:expected-erased-costs claim)
                 :residual-costs (:residual-costs claim)
                 :ir-artifacts (:artifacts claim)
                 :diagnostic-family :zero-cost-abstraction-validation}
                extra)))

(defn perf2-ir-artifact-id
  [claim key]
  (or (get-in claim [key :artifact-id])
      (get-in claim [key :id])))

(defn perf2-costs-in-ir
  [claim ir-key]
  (set (concat (get-in claim [ir-key :costs])
               (mapcat :costs (get-in claim [ir-key :operations])))))

(defn perf2-hidden-costs
  [claim]
  (set/union (set (:residual-costs claim))
             (perf2-costs-in-ir claim :after-ir)
             (set (get-in claim [:allocation-audit :hidden-allocations]))
             (set (get-in claim [:boxing-audit :hidden-boxing]))
             (set (get-in claim [:dispatch-specialization-report
                                  :residual-dispatch]))
             (set (get-in claim [:reflection-audit :hidden-reflection]))
             (set (get-in claim [:runtime-check-report
                                  :unaccounted-checks]))))

(defn perf2-missing-evidence
  [claim]
  (let [artifacts (set (:artifacts claim))]
    (cond-> #{}
      (empty? (:semantic-proof claim)) (conj :semantic-proof)
      (empty? (:safety-proof claim)) (conj :safety-proof)
      (empty? (:proof-ids claim)) (conj :proof-ids)
      (not (perf-present? (perf2-ir-artifact-id claim :before-ir)))
      (conj :before-ir)
      (not (perf-present? (perf2-ir-artifact-id claim :after-ir)))
      (conj :after-ir)
      (not (set/subset? perf2-required-artifacts artifacts))
      (conj :before-after-mir-artifacts))))

(defn perf2-validate-claim!
  [source-path manifest performance-claim claim]
  (let [missing-fields (perf2-missing-claim-fields claim)
        artifacts (set (:artifacts claim))
        expected-erased-costs (set (:expected-erased-costs claim))
        erased-costs (set (:erased-costs claim))
        residual-costs (set (:residual-costs claim))
        unerased-expected-costs (set/difference expected-erased-costs
                                                erased-costs)
        hidden-costs (set/union (perf2-hidden-costs claim)
                                unerased-expected-costs)
        profile-illegal (set (:profile-illegal-behavior claim))
        missing-evidence (perf2-missing-evidence claim)]
    (when (seq missing-fields)
      (perf2-fail! "PERF2-CLAIM" source-path manifest performance-claim claim
                   {:missing-fields missing-fields
                    :remediation "Record abstraction kind, equivalent form, erased/residual costs, before/after IR, proof ids, and proof artifacts."}))
    (when (or (not= (:profile manifest) (:profile claim))
              (not= (:profile performance-claim) (:profile claim))
              (not= (:target performance-claim) (:target claim))
              (not (contains? perf2-abstraction-families
                              (:abstraction claim)))
              (seq profile-illegal))
      (perf2-fail! "PERF2-PROFILE" source-path manifest performance-claim claim
                   {:active-profile (:profile manifest)
                    :performance-profile (:profile performance-claim)
                    :target-request (:target performance-claim)
                    :profile-illegal-behavior profile-illegal
                    :remediation "Zero-cost erasure must use a legal abstraction family under the already validated profile and target request."}))
    (when (or (seq missing-evidence)
              (not (contains? artifacts :dispatch-report))
              (nil? (:allocation-audit claim))
              (nil? (:boxing-audit claim))
              (nil? (:runtime-check-report claim)))
      (perf2-fail! "PERF2-EVIDENCE" source-path manifest performance-claim claim
                   {:missing-evidence
                    (cond-> missing-evidence
                      (not (contains? artifacts :dispatch-report))
                      (conj :dispatch-report)
                      (nil? (:allocation-audit claim))
                      (conj :allocation-audit)
                      (nil? (:boxing-audit claim))
                      (conj :boxing-audit)
                      (nil? (:runtime-check-report claim))
                      (conj :runtime-check-report))
                    :remediation "Attach before/after MIR, dispatch report, allocation and boxing audits, runtime-check report, and proof artifacts."}))
    (when-let [[cost id] (first (keep (fn [cost]
                                        (when-let [id (perf2-cost-diagnostic
                                                       cost)]
                                          [cost id]))
                                      hidden-costs))]
      (perf2-fail! id source-path manifest performance-claim claim
                   {:residual-cost cost
                    :remediation "Report the residual cost instead of accepting the zero-cost claim, or prove that the equivalent lower-level form has the same cost."}))
    (when (seq (set/union residual-costs unerased-expected-costs))
      (perf2-fail! "PERF2-RESIDUAL" source-path manifest performance-claim claim
                   {:residual-costs (set/union residual-costs
                                               unerased-expected-costs)
                    :remediation "A zero-cost claim must have an empty residual-cost set; non-erased work belongs in a performance report."}))
    :complete))

(defn zero-cost-capability-proof
  [manifest performance-claim claims]
  {:profile-legality-preserved? (every? #(= (:profile manifest)
                                            (:profile %))
                                        claims)
   :target-request-preserved? (every? #(= (:target performance-claim)
                                          (:target %))
                                      claims)
   :effect-authority-preserved?
   (every? #(= (set (get-in % [:effect-proof :source-effects]))
               (set (get-in % [:effect-proof :optimized-effects])))
           claims)
   :capability-authority-preserved?
   (every? #(= (set (get-in % [:capability-proof
                               :source-capabilities]))
               (set (get-in % [:capability-proof
                               :optimized-capabilities])))
           claims)
   :safety-evidence-preserved? (every? #(seq (:safety-proof %)) claims)
   :before-after-ir-recorded?
   (every? #(and (perf-present? (perf2-ir-artifact-id % :before-ir))
                 (perf-present? (perf2-ir-artifact-id % :after-ir)))
           claims)
   :residual-costs-empty? (every? #(empty? (:residual-costs %)) claims)
   :status :complete})