(let [static-rebuild-token (nth __gravity_bootstrap_checked_core_authority_values 0)
      p15-s23-stage2-closed-checked-core-source-artifact-internal __gravity_bootstrap_checked_core_source_helper
      p15-s23-stage2-closed-checked-core-rebuild-internal __gravity_bootstrap_checked_core_rebuild_helper]
(defn p15-s23-checked-core-verification-replay-audit-records
  ([authority capture-invoked?]
   (p15-s23-checked-core-verification-replay-audit-records
    authority capture-invoked? :post-call))
  ([authority capture-invoked? audit-phase]
  (when-not (and (contains? #{:pre-call :post-call} audit-phase)
                 (boolean? capture-invoked?))
    (p15-s23-closed-core-fail!
     "C8-CAPABILITY" "<verification-replay-audit>" {}
     {:missing-fact :exact-verification-replay-audit-phase}))
  (p15-s23-checked-core-bounded-ingress!
   "C8-CAPABILITY" :verification-replay-audit-authority authority
   p15-s23-reference-runtime-max-contract-nodes
   p15-s23-reference-runtime-max-contract-depth)
  (let [binding (:binding authority)
        policy-hash (:policy-hash authority)
        audit-policy-hash (:audit-policy-hash authority)
        providers (into {} (map (juxt :role identity))
                        (:provider-selection-records authority))
        grants (into {} (map (juxt :role identity))
                     (:grant-records authority))
        common
        {:replay-authority-record-id (:authority-record-id authority)
         :phase :verification
         :lifetime :single-verification-replay
         :policy-id (:policy-id authority)
         :verification-policy-hash policy-hash
         :audit-policy-id (:audit-policy-id authority)
         :verification-audit-policy-hash audit-policy-hash
         :plan-id (:plan-id binding)
         :source-content-hash (:source-content-hash binding)
         :checked-core-artifact-id (:checked-core-artifact-id binding)
         :runtime-artifact-hash (:runtime-artifact-hash binding)
         :runtime-contract-definition-hash
         (:runtime-contract-definition-hash binding)
         :runtime-derived-facts-hash (:runtime-derived-facts-hash binding)
         :runtime-function (:runtime-function binding)
         :runtime-function-hash (:runtime-function-hash binding)
         :profile :hosted
         :target :jvm
         :runtime-family :managed
         :service-id
         :gravity.reference/checked-core-verification-runtime-service
         :module (:module binding)
         :package :gravity/bootstrap
         :deployment :verification-harness-only
         :source-span
         {:source-content-hash (:source-content-hash binding)
          :mapping-id (:mapping-id binding)}
         :generated-origin-edge
         {:mapping-id (:mapping-id binding)
          :provenance-binding-id (:provenance-binding-id binding)}
         :delegated-handle-id
         :gravity.reference/checked-core-verification-runtime-handle
         :redaction :none
         :redaction-policy :hash-host-class-and-message
         :redaction-status :not-required
         :audit-status :recorded
         :diagnostic nil
         :missing-fact :not-applicable
         :reason :authorized-by-pinned-verification-policy}
        decision
        (fn [role action-id reason]
          (let [provider-role (if (= :fixture role)
                                :transcript-capture role)
                provider (get providers provider-role)
                grant (get grants role)
                record
                (merge
                 common
                 {:kind :gravity/p15-s23-verification-capability-decision
                  :action-id action-id
                  :principal-id (:principal grant)
                  :effect (:effect grant)
                  :capability (:capability grant)
                  :provider-id (:provider-id provider)
                  :provider-selection-record-id
                  (:provider-selection-record-id provider)
                  :grant-id (:grant-template-id grant)
                  :grant-record-id (:grant-record-id grant)
                  :scope (:scope grant)
                  :decision :grant
                  :result :grant
                  :reason reason})]
            (assoc record :decision-id
                   (p15-s23-reference-runtime-hash record))))
        action
        (fn [role action-id started? result-committed? output-committed?]
          (let [provider-role (if (= :fixture role)
                                :transcript-capture role)
                provider (get providers provider-role)
                grant (get grants role)
                record
                (merge
                 common
                 {:kind :gravity/p15-s23-verification-action-record
                  :action-id action-id
                  :principal-id (:principal grant)
                  :effect (:effect grant)
                  :capability (:capability grant)
                  :provider-id (:provider-id provider)
                  :provider-selection-record-id
                  (:provider-selection-record-id provider)
                  :grant-id (:grant-template-id grant)
                  :grant-record-id (:grant-record-id grant)
                  :scope (:scope grant)
                  :action-started? started?
                  :action-status (if started? :committed :not-invoked)
                  :result-committed? result-committed?
                  :output-committed? output-committed?
                  :diagnostic nil})]
            (assoc record :record-id
                   (p15-s23-reference-runtime-hash record))))
        writes-stdout?
        (contains? (:structural-operation-set binding) :println)]
    {:decision-records
     (cond->
      [(decision :verifier-managed-allocation
                 :gravity.reference/action-verification-runtime-invoke
                 :explicit-verifier-managed-allocation-grant)
       (decision :managed-allocation
                 :gravity.reference/action-verification-managed-allocation
                 :explicit-verification-runtime-grant)]
       writes-stdout?
       (conj
        (decision :verifier-transcript-fixture
                  :gravity.reference/action-verification-transcript-mediation
                  :explicit-verifier-fixture-grant)
        (decision :transcript-capture
                  :gravity.reference/action-verification-transcript-authority
                  :explicit-verification-runtime-grant)
        (decision :fixture
                  :gravity.reference/action-verification-transcript-capture
                  :explicit-verification-handler-grant)))
     :action-records
     (if (= :pre-call audit-phase)
       []
       (cond->
        [(action :verifier-managed-allocation
                 :gravity.reference/action-verification-runtime-invoke
                 true true false)
         (action :managed-allocation
                 :gravity.reference/action-verification-managed-allocation
                 true true false)]
         writes-stdout?
         (conj
          (action :verifier-transcript-fixture
                  :gravity.reference/action-verification-transcript-mediation
                  true true false)
          (action :transcript-capture
                  :gravity.reference/action-verification-transcript-authority
                  capture-invoked? capture-invoked? false)
          (action :fixture
                  :gravity.reference/action-verification-transcript-capture
                  capture-invoked? capture-invoked? capture-invoked?))))})))

(defn p15-s23-checked-core-verification-replay-audit-records-valid?
  ([authority audit capture-invoked?]
   (p15-s23-checked-core-verification-replay-audit-records-valid?
    authority audit capture-invoked? :post-call))
  ([authority audit capture-invoked? audit-phase]
  (try
    (when-not (boolean? capture-invoked?)
      (throw (ex-info "non-boolean capture state" {})))
    (p15-s23-checked-core-bounded-ingress!
     "C8-CAPABILITY" :verification-replay-audit-candidate audit
     p15-s23-reference-runtime-max-contract-nodes
     p15-s23-reference-runtime-max-contract-depth)
    (let [decisions (:decision-records audit)
          actions (:action-records audit)
          decision-keys
          (:decision-record-fields
           p15-s23-checked-core-expected-verification-replay-audit-policy)
          action-keys
          (:action-record-fields
           p15-s23-checked-core-expected-verification-replay-audit-policy)]
      (and
       (map? audit)
       (= #{:decision-records :action-records} (set (keys audit)))
       (= audit
          (p15-s23-checked-core-verification-replay-audit-records
           authority capture-invoked? audit-phase))
       (vector? decisions)
       (vector? actions)
       (every? #(= decision-keys (set (keys %))) decisions)
       (every? #(= action-keys (set (keys %))) actions)
       (every? #(= (:decision-id %)
                   (p15-s23-reference-runtime-hash
                    (dissoc % :decision-id)))
               decisions)
       (every? #(= (:record-id %)
                   (p15-s23-reference-runtime-hash
                    (dissoc % :record-id)))
               actions)
       (= (count decisions) (count (set (map :decision-id decisions))))
       (= (count actions) (count (set (map :record-id actions))))))
    (catch StackOverflowError _ false)
    (catch Exception _ false)))))
