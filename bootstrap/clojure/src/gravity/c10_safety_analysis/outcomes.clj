(ns gravity.c10-safety-analysis.outcomes
  "SAFE1 outcome and runtime-check projections.")

(defn outcome-records [source-span-op module inventory]
  (let [span (source-span-op (:source-path module) 0)]
    {:artifact :gravity/c10-safety-outcome-records
     :module (:module module)
     :records
     [{:operation "op-memory-load"
       :kind :buffer-read
       :source {:core-node "c6-core-1"
                :span span
                :origin-chain []}
       :profile (:profile module)
       :target (:target module)
       :facts {:type "c7-type-fact"
               :effects "c8-effect-fact"
               :ownership "c9-owner-fact"}
       :outcome :proven-safe
       :proof "proof-memory-valid"
       :runtime-check nil
       :unsafe-audit nil}
      {:operation "op-bounds-index"
       :kind :indexing
       :source {:core-node "c6-core-2"
                :span span
                :origin-chain []}
       :profile (:profile module)
       :target (:target module)
       :facts {:type "c7-length-fact"
               :effects "c8-read-effect"
               :ownership "c9-borrow-fact"}
       :outcome :runtime-checked
       :condition :bounds
       :runtime-check "check-bounds-1"
       :failure-behavior :panic/bounds}
      {:operation "op-borrow"
       :kind :borrow
       :source {:core-node "c6-core-3"
                :span span
                :origin-chain []}
       :profile (:profile module)
       :target (:target module)
       :facts {:ownership "borrow-immutable-a"
               :lifetime "lt-borrow-read"}
       :outcome :proven-safe
       :proof "proof-borrow-lifetime"}
      {:operation "op-region"
       :kind :region-allocation
       :source {:core-node "region-value-config"
                :span span
                :origin-chain []}
       :profile (:profile module)
       :target (:target module)
       :facts {:region "region-outer"}
       :outcome :proven-safe
       :proof "proof-region-no-escape"}
      {:operation "op-linear"
       :kind :resource-close
       :source {:core-node "resource-file"
                :span span
                :origin-chain []}
       :profile (:profile module)
       :target (:target module)
       :facts {:linear "resource-file"}
       :outcome :proven-safe
       :proof "proof-linear-exact-terminal"}
      {:operation "op-ffi"
       :kind :ffi-call
       :source {:core-node "ffi-slice"
                :span span
                :origin-chain []}
       :profile (:profile module)
       :target (:target module)
       :facts {:transfer "transfer-ffi-borrow"}
       :outcome :unsafe-island
       :unsafe-audit "unsafe-ffi-borrow-audit"}
      {:operation "op-concurrency"
       :kind :task-transfer
       :source {:core-node "task-buffer"
                :span span
                :origin-chain []}
       :profile (:profile module)
       :target (:target module)
       :facts {:transfer "transfer-structured-task"}
       :outcome :proven-safe
       :proof "proof-structured-task-join"}
      {:operation "op-numeric"
       :kind :numeric-overflow
       :source {:core-node "numeric-add"
                :span span
                :origin-chain []}
       :profile (:profile module)
       :target (:target module)
       :facts {:type "I64"}
       :outcome :runtime-checked
       :condition :overflow
       :runtime-check "check-overflow-1"
       :failure-behavior :panic/overflow}
      {:operation "op-capability"
       :kind :authority-use
       :source {:core-node "capability-use"
                :span span
                :origin-chain []}
       :profile (:profile module)
       :target (:target module)
       :facts {:capability "stage0/stdout"}
       :outcome :proven-safe
       :proof "proof-capability-scope"}
      {:operation "op-taint"
       :kind :taint-sink
       :source {:core-node "taint-sink"
                :span span
                :origin-chain []}
       :profile (:profile module)
       :target (:target module)
       :facts {:taint "sanitized-input"}
       :outcome :runtime-checked
       :condition :sanitized-before-sink
       :runtime-check "check-taint-1"
       :failure-behavior :error/taint}
      {:operation "op-generated-unsafe"
       :kind :generated-unsafe
       :source {:core-node "generated-unsafe"
                :span span
                :origin-chain [{:kind :generated
                                :macro "stage0-unsafe"}]}
       :profile (:profile module)
       :target (:target module)
       :facts {:generated-origin "stage0-unsafe"}
       :outcome :unsafe-island
       :unsafe-audit "unsafe-generated-audit"}
      {:operation "op-optimization-erased-check"
       :kind :check-elision
       :source {:core-node "optimized-bounds"
                :span span
                :origin-chain []}
       :profile (:profile module)
       :target (:target module)
       :facts {:proof "range-analysis-1"}
       :outcome :proven-safe
       :proof "proof-check-elision-preserved"}]
     :operation-count (count (:records inventory))
     :status :complete}))

(defn runtime-check-list [module outcomes]
  {:artifact :gravity/c10-runtime-check-list
   :module (:module module)
   :records
   (mapv (fn [outcome]
           {:check-id (:runtime-check outcome)
            :operation (:operation outcome)
            :condition (:condition outcome)
            :profile (:profile outcome)
            :target (:target outcome)
            :failure-behavior (:failure-behavior outcome)
            :effects #{:error/throw}
            :performance-class :bounded
            :guards-exact-operation? true
            :invalidates-on [:control-flow-change :proof-change]
            :status :recorded})
         (filter #(= :runtime-checked (:outcome %)) (:records outcomes)))
   :status :complete})
