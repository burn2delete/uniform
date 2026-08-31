

(defn b4-document-wasm-manifest
  [input-id]
  (let [module-hash (c4-artifact-id b4-document-wat)
        wit-hash (c4-artifact-id b4-document-wit)]
    {:artifact :gravity/wasm-backend-manifest
     :backend :gravity.backend/wasm
     :target {:kind :component
              :memory :wasm32
              :component-model :wasi-0.3
              :async-abi #{:async-func :stream :future}
              :features #{:simd :bulk-memory}
              :status :pinned}
     :emits #{:wasm-module :component :component-contracts
              :canonical-abi :bindings :source-map}
     :requires #{:linear-memory-plan :import-capabilities
                 :export-schemas :host-boundary-schemas
                 :component-composition :host-provider-manifest}
     :rejects #{:ambient-host-import :pointer-escape
                :unrecorded-nondeterminism :unsupported-wasm-feature
                :invalid-component-contract}
     :target-feature-record
     {:wasm-version :wasm-2.0
      :embedding-model :component
      :memory-width :wasm32
      :memory-count 1
      :initial-memory-pages 1
      :maximum-memory-pages 2
      :memory-growth :bounded
      :table-representation :funcref
      :reference-types :disabled
      :exceptions :disabled
      :gc :disabled
      :simd :enabled
      :relaxed-simd :disabled
      :atomics :disabled-with-rejection
      :shared-memory :disabled
      :component-model-abi-version :wasi-0.3
      :canonical-abi-adapter :required
      :resource-handles :linear
      :async-func :enabled
      :streams :enabled
      :futures :enabled
      :completion :recorded
      :cancellation :declared
      :backpressure :bounded
      :import-namespace :gravity-host
      :determinism-mode :replay-required
      :status :pinned}
     :linear-memory-and-table-plan
     {:allocation-provider :gravity.wasm/realloc
      :stack-segment :manifested
      :heap-segment :manifested
      :static-data-segments []
      :exported-memory :not-exported-as-authority
      :string-representation :canonical-abi-string
      :byte-buffer-representation :owned-byte-range
      :struct-layout :manifested
      :resource-table :component-resource-handles
      :copy-borrow-transfer [:copy :borrow :transfer]
      :bounds-and-lifetime-proofs [:safe2-bounds :safe2-lifetime]
      :memory-growth-invalidation :recorded
      :pointer-escape-policy :handle-or-copy-only
      :status :complete}
     :wasm-modules [{:path "gravity_stage0_b4.wat"
                     :content b4-document-wat
                     :hash module-hash
                     :structural-validation :passed}]
     :component-model-artifact
     {:path "gravity-stage0.wit"
      :content b4-document-wit
      :hash wit-hash
      :status :complete}
     :component-contract-manifest
     {:packages [{:name "gravity:stage0"
                  :interfaces ["host"]
                  :worlds ["gravity-stage0"]}]
      :interfaces [{:id :gravity-host
                    :items [{:id :gravity-host/clock-now
                             :direction :import
                             :effect :time/read
                             :capability :time/read
                             :source-anchor :host-call}
                            {:id :gravity-host/stream-events
                             :direction :import
                             :effect :io/read
                             :capability :stream/read
                             :source-anchor :stream-call}]}]
      :worlds [{:id :gravity-stage0-world
                :required-imports [:gravity-host/clock-now
                                  :gravity-host/stream-events]
                :exports [:gravity/entry]
                :capability-grants #{:time/read :stream/read}
                :host-boundary-schemas [:schema/clock-now
                                        :schema/stream-events
                                        :schema/gravity-entry]
                :replay-policy :recorded
                :resource-rules :linear
                :async-policy :typed-wasi-0.3
                :version "stage0"}]
      :resources [{:id :resource/stream-events
                   :owner :guest
                   :borrow-scope :call
                   :drop :declared
                   :leak-diagnostic "B4-WASI-ASYNC"}]
      :composition-plan :explicit
      :status :complete}
     :canonical-abi-manifest
     {:records [{:id :canonical-abi/entry-v1
                 :forms [:record :string :buffer :resource
                         :stream :future]
                 :copy-borrow-transfer [:copy :borrow :transfer :drop]
                 :allocation :gravity.wasm/realloc
                 :trap-error-cancellation :mapped
                 :handle-ownership :declared
                 :schema-version 1
                 :hash (c4-artifact-id "canonical-abi/entry-v1")
                 :conformance-fixture :backend-b4-canonical-abi}]
      :status :complete}
     :import-capability-manifest
     {:imports [{:id :gravity.host/clock-now
                 :namespace "gravity:host/clock"
                 :symbol "now"
                 :interface :gravity-host
                 :world :gravity-stage0-world
                 :item :gravity-host/clock-now
                 :effect :time/read
                 :capability :time/read
                 :schema :schema/clock-now
                 :determinism :nondeterministic
                 :replay :recorded
                 :provider :host/clock
                 :trap-error-behavior :mapped
                 :async :future
                 :ownership :guest-owned-handle
                 :taint-policy :validated
                 :source-anchor :host-call}]
      :status :declared}
     :export-schema-manifest
     {:exports [{:id :gravity/entry
                 :schema :schema/gravity-entry
                 :ownership :copy
                 :lifetime :call
                 :stable? true}]
      :status :complete}
     :host-boundary-schema-manifest
     {:schemas [{:id :schema/clock-now
                 :wit-item :gravity-host/clock-now
                 :canonical-abi :canonical-abi/entry-v1
                 :validation :required
                 :taint-policy :validated
                 :version "stage0"
                 :fallback :reject}
                {:id :schema/gravity-entry
                 :wit-item :gravity/entry
                 :canonical-abi :canonical-abi/entry-v1
                 :validation :required
                 :taint-policy :not-applicable
                 :version "stage0"
                 :fallback :reject}]
      :status :complete}
     :wasi-component-async-abi-manifest
     {:async-funcs [{:id :wasi-async/clock-now
                     :effect :time/read
                     :scheduling :host-declared
                     :cancellation :declared
                     :replay :recorded}]
      :futures [{:id :wasi-async/clock-future
                 :type :u64
                 :owner :guest
                 :await-cancel-drop :declared
                 :result-schema :schema/clock-now
                 :replay :recorded}]
      :streams [{:id :wasi-async/event-stream
                 :item-schema :schema/event
                 :producer :host
                 :consumer :guest
                 :backpressure :bounded
                 :close-cancellation :declared
                 :partial-consumption :recorded}]
      :status :complete}
     :component-composition-plan
     {:components [:gravity-stage0]
      :adapters [:canonical-abi-adapter]
      :authority-amplification :rejected
      :effect-capability-preservation :complete
      :replay-merge-policy :explicit
      :status :complete}
     :host-binding-stubs
     [{:target :browser
       :providers [:clock]
       :capabilities #{:time/read}
       :status :complete}
      {:target :wasi
       :providers [:clock]
       :capabilities #{:time/read}
       :status :complete}
      {:target :custom
       :providers [:clock :stream]
       :capabilities #{:time/read :stream/read}
       :status :complete}]
     :runtime-helper-manifest
     {:helpers [:allocation :bounds-check :numeric-check
                :resource-table :string-buffer-conversion
                :async-host-call :future :stream :completion-queue
                :cancellation :backpressure :trap :math :debug-hook]
      :status :complete}
     :source-map-and-generated-origin-map
     {:source input-id
      :generated-origin-chain [:mir :c14-target-lowering
                               :b1-interface :b4-wasm-backend]
      :status :preserved}
     :replay-and-nondeterminism-record
     {:events [:clock-now :stream-item :host-wakeup
               :cancellation :external-error]
      :policy :record
      :status :complete}
     :simd-feature-record
     {:features #{:simd}
      :lane-bounds-alignment-proof :perf8-stage0
      :fallback :scalar
      :status :complete}
     :atomic-feature-record
     {:shared-memory :disabled
      :accepted []
      :rejected [{:operation :atomic-compare-exchange
                  :diagnostic "B4-ATOMIC"}]
      :status :complete}
     :input-artifact input-id
     :status :complete}))