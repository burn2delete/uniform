; Semantic decomposition of HEAD reader line 24702.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-hosted-lowering-wasm-backend
 [source-path state]
 (let
  [{:keys [wasm-module wasm-manifest]} state]
  (assoc
   {}
   :wasm-backend
   {:component-contract-manifest
    {:packages ["gravity:stage0/hosted"],
     :interfaces ["gravity-host"],
     :worlds
     [{:name "gravity-stage0",
       :imports [:clock/now],
       :exports [:gravity/entry],
       :capabilities #{:time/read},
       :replay-policy :recorded}],
     :status :complete},
    :host-boundary-schema-manifest
    {:schemas [:Instant :I64],
     :taint-policy :validated,
     :status :complete},
    :import-capability-manifest
    {:imports
     [{:symbol :clock/now,
       :effect :time/read,
       :capability :time/read,
       :schema :Instant,
       :provider :host/clock,
       :replay :recorded}],
     :status :declared},
    :target-feature-record
    {:kind :component,
     :memory :wasm32,
     :component-model :wasi-0.3,
     :async-abi #{:future :async-func :stream},
     :features #{:bulk-memory},
     :status :pinned},
    :canonical-abi-manifest
    {:records [:entry-params],
     :resources [:host-clock],
     :streams [],
     :futures [],
     :status :complete},
    :replay-nondeterminism-record
    {:events [:clock/now], :policy :record, :status :complete},
    :status :complete,
    :export-schema-manifest
    {:exports
     [{:symbol :gravity/entry, :schema :I64, :ownership :copy}],
     :status :complete},
    :artifact :gravity/wasm-backend-manifest,
    :wasm-modules
    [{:path "gravity_stage0.wat",
      :content wasm-module,
      :hash (:content-hash wasm-manifest)}],
    :backend :gravity.backend/wasm,
    :async-abi-manifest
    {:async-funcs [:clock/now],
     :completion :recorded,
     :cancellation :declared,
     :backpressure :not-applicable,
     :status :complete}})))
