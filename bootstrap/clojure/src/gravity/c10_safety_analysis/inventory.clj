(ns gravity.c10-safety-analysis.inventory
  "Safety-sensitive operation inventory projection.")

(defn operation-inventory [module c9-artifact]
  {:artifact :gravity/c10-safety-operation-inventory
   :module (:module module)
   :records [{:operation-id "op-memory-load"
              :kind :buffer-read
              :safe-family :memory
              :source-core-node "c6-core-1"
              :facts {:types :typed-core
                      :effects :effect-graph
                      :ownership :ownership-graph}
              :profile (:profile module)
              :target (:target module)}
             {:operation-id "op-bounds-index"
              :kind :indexing
              :safe-family :bounds
              :source-core-node "c6-core-2"
              :facts {:types :typed-core
                      :effects :effect-graph
                      :ownership :borrow-graph}
              :profile (:profile module)
              :target (:target module)}
             {:operation-id "op-borrow"
              :kind :borrow
              :safe-family :ownership
              :source-core-node "c6-core-3"
              :facts {:ownership :borrow-graph
                      :lifetimes :lifetime-interval-map}
              :profile (:profile module)
              :target (:target module)}
             {:operation-id "op-region"
              :kind :region-allocation
              :safe-family :region
              :facts {:regions :region-lifetime-graph}
              :profile (:profile module)
              :target (:target module)}
             {:operation-id "op-linear"
              :kind :resource-close
              :safe-family :linear-resource
              :facts {:linear :linear-resource-flow-graph}
              :profile (:profile module)
              :target (:target module)}
             {:operation-id "op-ffi"
              :kind :ffi-call
              :safe-family :ffi
              :facts {:transfer :transfer-records}
              :profile (:profile module)
              :target (:target module)}
             {:operation-id "op-concurrency"
              :kind :task-transfer
              :safe-family :concurrency
              :facts {:transfer :transfer-records
                      :lifetimes :lifetime-interval-map}
              :profile (:profile module)
              :target (:target module)}
             {:operation-id "op-numeric"
              :kind :numeric-overflow
              :safe-family :numeric
              :facts {:types :typed-core}
              :profile (:profile module)
              :target (:target module)}
             {:operation-id "op-capability"
              :kind :authority-use
              :safe-family :capability
              :facts {:capabilities :capability-proof-records}
              :profile (:profile module)
              :target (:target module)}
             {:operation-id "op-taint"
              :kind :taint-sink
              :safe-family :taint
              :facts {:taint :taint-report}
              :profile (:profile module)
              :target (:target module)}
             {:operation-id "op-generated-unsafe"
              :kind :generated-unsafe
              :safe-family :macro-safety
              :facts {:origin :generated-origin-chain}
              :profile (:profile module)
              :target (:target module)}
             {:operation-id "op-optimization-erased-check"
              :kind :check-elision
              :safe-family :optimization
              :facts {:proof :optimization-proof}
              :profile (:profile module)
              :target (:target module)}]
   :upstream {:c9-artifact-id (:artifact-id c9-artifact)
              :ownership-proof (get-in c9-artifact
                                       [:capability-based-proof :status])}
   :status :complete})
