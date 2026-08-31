

(defn p15-s23-reference-runtime-adapter-fail!
  ([diagnostic runtime-rule action-record decision-record missing-fact]
   (p15-s23-reference-runtime-adapter-fail!
    diagnostic runtime-rule action-record decision-record missing-fact {}))
  ([diagnostic runtime-rule action-record decision-record missing-fact extra]
   (let [candidate-source (when (map? runtime-rule)
                            (:runtime-artifact-source-path runtime-rule))
         safe-source
         (if (and (string? candidate-source)
                  (<= (count candidate-source) 4096))
           candidate-source
           "p15-s23-reference-runtime-adapter")
         candidate-hash (when (map? runtime-rule)
                          (:runtime-artifact-hash runtime-rule))
         safe-hash
         (when (and (string? candidate-hash)
                    (re-matches #"sha256:[0-9a-f]{64}" candidate-hash))
           candidate-hash)]
    (fail!
     diagnostic
     "Pinned reference runtime adapter rejected execution"
     (merge
      {:source-span {:source safe-source}
      :stage :p15-s23-reference-runtime-adapter
      :diagnostic-family :p15-s23-reference-runtime-adapter
      :missing-fact missing-fact
      :runtime-artifact-hash safe-hash
      :action-record action-record
      :decision-record decision-record
      :result-committed? false
      :output-committed? false
      :remediation :restore_pinned_runtime_provider_grant_and_contract}
      extra)))))

(def p15-s23-reference-runtime-authority-keys
  #{:mode :source-principal :handler-principal :providers :grants
    :failure-injection :deployment-stdout?})

(defn p15-s23-reference-runtime-decision-record
  [runtime-rule attributes]
  (let [record
        (merge
         {:artifact :gravity/runtime-capability-decision-record
          :runtime-artifact-hash (:runtime-artifact-hash runtime-rule)
          :runtime-contract-definition-hash
          (:runtime-contract-definition-hash runtime-rule)
          :runtime-contract-derived-facts-hash
          (:runtime-contract-derived-facts-hash runtime-rule)
          :runtime-function
          p15-s23-stage2-runtime-artifact-closed-plan-function
          :phase :runtime
          :lifetime :single-reference-execution
          :policy-id :gravity.reference/runtime-audit-policy
          :audit-policy-id :gravity.reference/runtime-audit-policy
          :reference-invocation :single-reference-execution
          :package :gravity/bootstrap
          :deployment :reference-harness-only
          :source-declaration-is-grant? false
          :redaction :none}
         attributes)]
    (assoc record :decision-id
           (p15-s23-reference-runtime-hash record))))

(defn p15-s23-reference-runtime-action-record
  [runtime-rule attributes]
  (let [base
        (merge
         {:artifact :gravity/runtime-action-record
          :runtime-artifact-hash (:runtime-artifact-hash runtime-rule)
          :runtime-contract-definition-hash
          (:runtime-contract-definition-hash runtime-rule)
          :runtime-contract-derived-facts-hash
          (:runtime-contract-derived-facts-hash runtime-rule)
          :runtime-function
          p15-s23-stage2-runtime-artifact-closed-plan-function
          :phase :runtime
          :lifetime :single-reference-execution
          :policy-id :gravity.reference/runtime-audit-policy
          :audit-policy-id :gravity.reference/runtime-audit-policy
          :reference-invocation :single-reference-execution
          :package :gravity/bootstrap
          :deployment :reference-harness-only
          :profile :hosted
          :target :jvm
          :source-declaration-is-grant? false
          :redaction :none}
         attributes)
        record
        (merge {:diagnostic nil
                :source-span
                {:source-id (or (:source-id base)
                                (:runtime-artifact-source-content-hash
                                 runtime-rule))}
                :generated-origin-chain
                [:source-unit :stage2-plan-emitter
                 :p15-s23-reference-runtime-adapter]
                :artifact-id (or (:plan-id base)
                                 (:runtime-artifact-hash runtime-rule))
                :remediation :none}
               base)]
    (assoc record :record-id
           (p15-s23-reference-runtime-hash record))))

(defn p15-s23-reference-runtime-adapter-noncapability-reject!
  ([diagnostic runtime-rule function plan-id source-id missing-fact
    remediation]
   (p15-s23-reference-runtime-adapter-noncapability-reject!
    diagnostic runtime-rule function plan-id source-id missing-fact
    remediation {}))
  ([diagnostic runtime-rule function plan-id source-id missing-fact
    remediation extra]
   (let [redaction (or (:redaction extra) :none)
         failure-cause (:failure-cause extra)
         action-base
         (cond->
          {:artifact :gravity/runtime-preflight-failure-record
           :diagnostic diagnostic
           :source-span
           {:source-id (or source-id
                           p15-s23-stage2-runtime-artifact-expected-source-content-hash)}
           :generated-origin-chain
           [:source-unit :p15-s23-reference-runtime-adapter]
           :artifact-id (or plan-id
                            p15-s23-stage2-runtime-artifact-expected-artifact-hash)
           :runtime-function function
           :operation :runtime-contract-preflight
           :missing-fact missing-fact
           :action-started? false
           :action-status :rejected-before-start
           :result-committed? false
           :output-committed? false
           :redaction redaction
           :remediation remediation}
           failure-cause (assoc :failure-cause failure-cause))
         action-record
         (assoc action-base :record-id
                (p15-s23-reference-runtime-hash action-base))
         decision-base
         (cond->
          {:artifact :gravity/runtime-preflight-decision-record
           :diagnostic diagnostic
           :runtime-artifact-hash
           p15-s23-stage2-runtime-artifact-expected-artifact-hash
           :plan-id plan-id
           :source-id source-id
           :runtime-function function
           :missing-fact missing-fact
           :result :reject
           :redaction redaction}
           failure-cause (assoc :failure-cause failure-cause))
         decision-record
         (assoc decision-base :decision-id
                (p15-s23-reference-runtime-hash decision-base))]
     (p15-s23-reference-runtime-adapter-fail!
      diagnostic runtime-rule action-record decision-record missing-fact
      extra))))