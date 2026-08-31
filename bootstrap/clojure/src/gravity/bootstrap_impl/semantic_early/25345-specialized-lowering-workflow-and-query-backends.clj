; Semantic decomposition of HEAD reader line 25345.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-specialized-lowering-workflow-and-query-backends
 [source-path state]
 (let
  [{:keys [workflow-manifest workflow-graph query-manifest sql-module]}
   state]
  (assoc
   {}
   :workflow-backend
   {:human-review-policy-graph
    {:gates [{:step :approve-output, :capability :ai/human-review}],
     :status :complete},
    :retry-timeout-compensation-table
    {:steps
     {:call-model
      {:retry :exponential,
       :timeout-ms 30000,
       :compensation :record-failure}},
     :status :complete},
    :workflow-graph-artifact
    {:content workflow-graph,
     :hash (:content-hash workflow-manifest),
     :status :complete},
    :audit-provenance-record
    {:source-map :preserved,
     :policy-decisions :recorded,
     :status :complete},
    :step-schema-bundle
    {:schemas [:workflow-input :step-output :durable-state],
     :migration-policy :versioned,
     :status :complete},
    :idempotency-key-map
    {:steps {:call-model :workflow-input-hash}, :status :complete},
    :event-log-schema
    {:events [:started :step-completed :human-reviewed],
     :status :complete},
    :external-capability-manifest
    {:capabilities #{:network/request :ai/human-review :ai/model-call},
     :providers [:stage0-model-provider],
     :status :complete},
    :status :complete,
    :artifact :gravity/workflow-backend-manifest,
    :replay-policy
    {:nondeterminism [:clock :model-call],
     :side-effects :event-log-guarded,
     :status :complete},
    :backend :gravity.backend/workflow-graph}
   :query-backend
   {:capability-taint-report
    {:capabilities #{:db/read}, :taint :validated, :status :complete},
    :sql-statement-artifacts
    [{:id :select-gravity-value,
      :sql sql-module,
      :hash (:content-hash query-manifest)}],
    :relational-ir-handoff-record
    {:domain-anchor :relational-query, :status :complete},
    :query-plan-metadata
    {:plan :index-neutral-select,
     :provider-specific-movement [],
     :status :complete},
    :transaction-isolation-manifest
    {:effect :db/read,
     :isolation :read-committed,
     :capability :db/read,
     :status :complete},
    :status :complete,
    :artifact :gravity/query-backend-manifest,
    :typed-result-adapter
    {:columns
     [{:name :gravity_value,
       :gravity-type :I64,
       :nullability :nonnull}],
     :status :complete},
    :target {:dialect :postgresql, :version "stage0-declared"},
    :prepared-binding-manifest
    {:parameters
     [{:position 1,
       :gravity-type :I64,
       :database-type :bigint,
       :taint :validated}],
     :status :parameterized},
    :backend :gravity.backend/query-relational,
    :migration-artifact
    {:previous-schema :stage0-v1,
     :next-schema :stage0-v1,
     :data-loss :none,
     :status :complete}})))
