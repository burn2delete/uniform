(ns gravity.backend.workflow
  "Native B10 diagnostic lookup data; no workflow or input admission authority.")

(def ^:private namespace-contract
  {:namespace 'gravity.backend.workflow
   :governing-contracts '[D1 D2 D3 D8 D9 C1 C15 B1 B10 B13 B14 BOOT8 TEST2]
   :contract-boundary :native-b10-diagnostic-policy-data
   :public-api {'source-overrides {:arglists '([module]) :admission :none}
                'missing-policy {:arglists '([id]) :admission :none}
                'step-id {:arglists '([id]) :admission :none}}
   :artifact-inputs [:native-module-metadata :native-b10-diagnostic-id]
   :artifact-outputs [:borrowed-source-overrides :native-missing-policy :native-step-label]
   :diagnostic-ownership {:stage [:native-override-precedence :native-rule-context-defaults]
                          :existing-owner [:b10-refusal-decisions :failure-construction
                                           :diagnostic-stream :source-admission :publication]}
   :dependency-direction {:requires ['clojure.core] :consumed-by ['gravity.bootstrap]
                          :forbids ['gravity.bootstrap 'gravity.cli 'gravity.release]}
   :authenticates-inputs? false :issues-certificates? false
   :supported [:existing-native-projection :borrowed-identity :compatibility-var-interposition]
   :unsupported [:canonical-b10-semantics :gravity-b10-source-compilation
                 :verified-mir-or-workflow-ingress :workflow-emission :workflow-execution :workflow-verification
                 :schema-or-migration-validation :replay-or-nondeterminism-validation
                 :idempotency-retry-or-compensation-validation :capability-or-human-review-validation :taint-validation :project-accounting-authority :downstream-handoff
                 :source-authentication :public-diagnostic-redaction :migration :release
                 :self-hosting :seed-retirement]})

(defn source-overrides
  [module]
  (or (get-in module [:metadata :backend :b10-document])
      (get-in module [:metadata :backend :specialized-lowering])
      (get-in module [:metadata :backend :specialized])
      {}))

(defn missing-policy
  [id]
  (case id
    "B10-SCHEMA" :workflow-step-message-state-schema
    "B10-REPLAY" :recorded-nondeterminism-and-event-log-replay
    "B10-IDEMPOTENCY" :side-effect-idempotency-key
    "B10-RETRY" :retry-timeout-cancellation-failure-map
    "B10-COMPENSATION" :compensation-handler
    "B10-CAPABILITY" :external-authority-capability-manifest
    "B10-POLICY" :provider-budget-human-review-policy
    "B10-TAINT" :validated-model-tool-external-output
    "B10-GRAPH" :valid-workflow-graph-edge-cycle-compensation
    :complete-workflow-artifact-manifest))

(defn step-id
  [id]
  (case id
    "B10-SCHEMA" :schema-step
    "B10-REPLAY" :call-model
    "B10-IDEMPOTENCY" :write-ticket
    "B10-RETRY" :call-service
    "B10-COMPENSATION" :write-database
    "B10-CAPABILITY" :call-tool
    "B10-POLICY" :approve-output
    "B10-TAINT" :trusted-sink
    "B10-GRAPH" :join
    :workflow-manifest))

(doseq [operation [#'source-overrides #'missing-policy #'step-id]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
