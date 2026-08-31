

(defn minimal-native-runtime-manifest
  [input-id]
  {:artifact :gravity/minimal-native-runtime
   :input-artifact input-id
   :family :minimal-native
   :profile :native
   :target {:backend :llvm :platform :linux}
   :services {:linked #{:startup :panic :atomics :runtime-checks
                        :resource-cleanup :ffi-trampoline}
              :generated #{:bounds-check-helper :numeric-check-helper
                           :stack-probe :panic-site-table}
              :delegated #{:allocator/provider}
              :external #{}
              :forbidden #{:gc :reflection :dynamic-eval
                           :managed-exceptions :host-exceptions
                           :classloading}}
   :linked-support-objects
   [{:object "gravity_startup.o" :service :startup :status :declared}
    {:object "gravity_panic.o" :service :panic :status :declared}
    {:object "gravity_checks.o" :service :runtime-checks :status :declared}
    {:object "gravity_atomics.o" :service :atomics :status :declared}
    {:object "gravity_resources.o" :service :resource-cleanup
     :status :declared}]
   :startup-record
   {:artifact :gravity/minimal-native-startup-record
    :entry-symbol "gravity_main"
    :initialization-order [:capture-argv :init-panic-table
                           :init-region-provider :call-main
                           :run-cleanup-handlers]
    :cleanup-order [:linear-resources :arena-reset :debug-flush]
    :status :complete}
   :allocator-provider-record
   {:artifact :gravity/native-allocator-provider-record
    :provider :allocator/region-arena
    :profiles #{:native}
    :effects #{:memory/allocate :memory/release}
    :capabilities #{:memory/arena}
    :allocation-regime :alloc/region
    :layout {:alignment 16 :max-object-align 64}
    :failure :result
    :deallocation :arena-reset-or-explicit-release
    :no-allocation-regions #{:signal-handler :panic-handler}
    :unsafe-implementation-boundary :runtime-internal-unsafe-island
    :debug {:allocation-trace true}
    :status :complete}
   :panic-failure-policy
   {:artifact :gravity/minimal-native-panic-policy
    :panic :abort-with-report
    :trap :target-trap
    :result-boundary :explicit-result
    :unwind :disabled-unless-abi-declared
    :abort-symbol "gravity_abort"
    :source-map-preserved? true
    :status :complete}
   :atomic-synchronization-provider-record
   {:artifact :gravity/native-atomic-synchronization-provider
    :supported-orders [:relaxed :acquire :release :acq-rel :seq-cst]
    :unsupported-orders []
    :target-memory-model :llvm-native
    :safe8-preservation :complete
    :locks {:provider :native-mutex :requires-capability? false}
    :scheduler-hooks :not-linked-without-effect
    :status :complete}
   :ffi-helper-manifest
   {:artifact :gravity/native-ffi-helper-manifest
    :helpers [:call-trampoline :ownership-adapter :error-adapter
              :callback-lifetime-token]
    :safe7-boundary-metadata-preserved? true
    :source-map-preserved? true
    :unsafe-island-audit "R3-FFI-STAGE0"
    :status :complete}
   :runtime-check-helper-manifest
   {:artifact :gravity/native-runtime-check-helper-manifest
    :checks [{:check :bounds :failure :panic :source-map true}
             {:check :numeric :failure :result-or-panic :source-map true}
             {:check :initialization :failure :panic :source-map true}
             {:check :borrow-state :failure :panic :source-map true}]
    :proof-elision :requires-safe15-proof
    :status :complete}
   :debug-release-behavior-record
   {:artifact :gravity/native-debug-release-behavior
    :debug {:stack-traces true
            :allocation-traces true
            :check-reports true
            :authority-effects #{}}
    :release {:stack-traces :source-map-only
              :allocation-traces false
              :check-reports :redacted
              :debug-only-services-linked? false}
    :status :complete}
   :capability-enforcement-table
   {:artifact :gravity/minimal-native-capability-table
    :runtime-helpers-grant-authority? false
    :helper-effects [{:helper :debug-stack :effects #{}
                      :capabilities #{}}
                     {:helper :allocator :effects #{:memory/allocate}
                      :capabilities #{:memory/arena}}
                     {:helper :ffi-trampoline :effects #{:ffi/call}
                      :capabilities #{:ffi/c}}]
    :status :complete}
   :managed-service-rejection-record
   {:artifact :gravity/minimal-native-managed-service-rejection
    :rejected #{:gc :reflection :dynamic-eval :managed-exceptions
                :host-exceptions :classloading}
    :status :complete}
   :status :complete})

(defn memory-runtime-manifest
  [input-id]
  {:artifact :gravity/memory-runtime-manifest
   :input-artifact input-id
   :family :memory
   :provider-families
   {:no-allocation {:profiles #{:firmware :hardware :formal}
                    :runtime-checks #{}
                    :status :declared}
    :stack {:profiles #{:native :firmware :kernel}
            :runtime-checks #{:bounds :initialization}
            :status :declared}
    :ownership {:profiles #{:native :kernel}
                :runtime-checks #{:use-after-move}
                :status :declared}
    :region {:profiles #{:native :firmware :kernel}
             :runtime-checks #{:region-escape :lifetime}
             :status :declared}
    :arena {:profiles #{:native :firmware}
            :runtime-checks #{:arena-reset :provider-match}
            :status :declared}
    :gc {:profiles #{:hosted}
         :runtime-checks #{:null :bounds}
         :status :profile-restricted}
    :reference-counting {:profiles #{:native :hosted}
                         :runtime-checks #{:release-state}
                         :status :declared}
    :raw {:profiles #{:kernel :native}
          :safe-default? false
          :requires #{:unsafe-island :safe-wrapper}
          :status :audited-only}
    :foreign {:profiles #{:native :hosted}
              :runtime-checks #{:lifetime :ownership-transfer}
              :status :declared}
    :pinned {:profiles #{:native :hosted :gpu}
             :runtime-checks #{:pinned-lifetime}
             :status :declared}
    :device {:profiles #{:gpu}
             :runtime-checks #{:transfer-state :synchronization
                               :address-space}
             :status :declared}}
   :provider-selection-record
   {:artifact :gravity/memory-provider-selection-record
    :selected [:region :arena :ownership :stack]
    :active-profile :native
    :rejected [{:provider :gc :reason :native-minimal-runtime}
               {:provider :raw :reason :safe-default-forbidden}]
    :status :complete}
   :allocation-deallocation-contract
   {:artifact :gravity/allocation-deallocation-contract
    :provider :memory/region-arena
    :allocation-effects #{:memory/allocate}
    :release-effects #{:memory/release}
    :alignment 16
    :failure :result
    :deallocation :explicit-release-or-region-exit
    :allocator-identity-check :runtime-checked
    :status :complete}
   :region-arena-manifest
   {:artifact :gravity/region-arena-manifest
    :regions [{:id :request-region :lifetime :lexical
               :escape :rejected}
              {:id :scratch-arena :lifetime :bounded-call
               :escape :rejected}]
    :arena-reset :requires-no-live-borrows
    :status :complete}
   :ownership-borrow-runtime-check-map
   {:artifact :gravity/ownership-borrow-runtime-check-map
    :checks [{:check :borrow-state :when :debug-or-dynamic-boundary}
             {:check :lifetime-generation :when :runtime-handle}
             {:check :allocator-identity :when :release}]
    :compiler-facts-preserved? true
    :status :complete}
   :linear-resource-ledger
   {:artifact :gravity/linear-resource-ledger
    :resources [{:resource-id :native-file-handle
                 :provider :resource/native-file
                 :terminal-states #{:closed}
                 :normal-path :closed
                 :error-path :closed
                 :panic-path :closed
                 :cancellation-path :not-applicable}]
    :duplicated-handles []
    :unconsumed-handles []
    :status :complete}
   :raw-memory-unsafe-audit-records
   [{:artifact :gravity/raw-memory-unsafe-audit
     :operation :runtime-internal-arena-pointer
     :unsafe-island "R5-RAW-STAGE0"
     :safe-wrapper :arena/alloc
     :invariants [:aligned :provider-owned :lifetime-bounded]
     :capabilities #{:memory/raw}
     :status :audited}]
   :device-memory-provider-manifest
   {:artifact :gravity/device-memory-provider-manifest
    :address-spaces #{:global :shared :local :constant}
    :transfer-records [:host-to-device :device-to-host]
    :synchronization [:barrier :stream-event]
    :lifetime :kernel-or-explicit-buffer
    :status :complete}
   :debug-allocation-trace-schema
   {:artifact :gravity/debug-allocation-trace-schema
    :fields [:source-span :allocation-site :provider :lifetime
             :resource-id :release-site]
    :hidden-authority-effects #{}
    :source-map-preserved? true
    :status :complete}
   :runtime-check-proof-agreement
   {:artifact :gravity/runtime-check-proof-agreement
    :retained-checks #{:bounds :initialization :borrow-state
                       :allocator-identity :region-escape}
    :elided-checks [{:check :static-stack-bound
                     :proof :safe15-static-stack-proof}]
    :unproved-elisions []
    :status :complete}
   :status :complete})