(ns gravity.c9-ownership-checker.resources
  "Linear resource, transfer, runtime-check, and unsafe-audit projections.")

(defn linear-resource-flow-graph [module]
  {:artifact :gravity/c9-linear-resource-flow-graph :module (:module module)
   :resources
   (sorted-map
    "resource-file" {:provider :fs/provider :state :owned
                     :terminal-paths [{:path :normal :terminal :closed :terminal-count 1}
                                      {:path :error :terminal :closed :terminal-count 1}
                                      {:path :panic :terminal :closed :terminal-count 1}
                                      {:path :cancellation :terminal :cancelled :terminal-count 1}]
                     :cleanup-obligations [:close-on-normal :close-on-error :close-on-panic
                                           :cancel-on-cancellation] :status :accepted}
    "resource-transaction" {:provider :db/provider :state :owned
                            :terminal-paths [{:path :normal :terminal :committed :terminal-count 1}
                                             {:path :error :terminal :rolled-back :terminal-count 1}
                                             {:path :panic :terminal :rolled-back :terminal-count 1}
                                             {:path :cancellation :terminal :cancelled :terminal-count 1}]
                            :cleanup-obligations [:commit-or-rollback :cancel-on-cancellation]
                            :status :accepted})
   :structured-resource-lowering [{:form :with-open :resource "resource-file"
                                   :normal :closed :error :closed :panic :closed
                                   :cancellation :cancelled :status :accepted}]
   :rejected-flow-families ["C9-LINEAR-LEAK" "C9-LINEAR-DOUBLE"] :status :complete})

(defn transfer-records [module]
  {:artifact :gravity/c9-transfer-records :module (:module module)
   :records [{:transfer-id "transfer-function-return" :boundary :function
              :value-id "owned-result" :from "callee" :to "caller"
              :mode :ownership-transfer :cleanup-obligation :caller :status :accepted}
             {:transfer-id "transfer-actor-message" :boundary :actor
              :value-id "actor-buffer" :from "parent-task" :to "worker-actor"
              :mode :move :cleanup-obligation :worker-actor :status :accepted}
             {:transfer-id "transfer-structured-task" :boundary :task
              :value-id "task-buffer" :from "parent-task" :to "child-task"
              :mode :structured-move :lifetime "lt-structured-task"
              :cleanup-obligation :child-task :status :accepted}
             {:transfer-id "transfer-ffi-borrow" :boundary :ffi
              :value-id "ffi-slice" :from "gravity" :to "foreign-call"
              :mode :borrowed-for-call :lifetime "lt-callback"
              :cleanup-obligation :gravity :status :accepted}]
   :rejected-transfer-families ["C9-TRANSFER"] :status :complete})

(defn runtime-check-records [module]
  (let [profile (:profile module)
        legal? (contains? #{:hosted :native :distributed :ai} profile)]
    {:artifact :gravity/c9-runtime-check-records :module (:module module)
     :records [{:check-id "runtime-borrow-state" :kind :dynamic-borrow-state
                :failure :recoverable-error :profile profile :profile-legal? legal? :status :recorded}
               {:check-id "runtime-arena-generation" :kind :arena-generation
                :failure :recoverable-error :profile profile :profile-legal? legal? :status :recorded}
               {:check-id "runtime-provider-scope" :kind :provider-scope-validity
                :failure :recoverable-error :profile profile :profile-legal? legal? :status :recorded}
               {:check-id "runtime-resource-terminal-state" :kind :resource-terminal-state
                :failure :recoverable-error :profile profile :profile-legal? legal? :status :recorded}]
     :rejected-runtime-check-families ["C9-RUNTIME-CHECK"] :status :complete}))

(defn unsafe-audit-references [module]
  {:artifact :gravity/c9-unsafe-audit-references :module (:module module)
   :records [{:audit-id "C9-AUDIT-MANUAL-LIFETIME"
              :unsafe-island :manual-lifetime-extension
              :safe-api-boundary :stage0/checked-lifetime-handle
              :reason :manual-lifetime-extension :review :required :status :recorded}
             {:audit-id "C9-AUDIT-MANUAL-RESOURCE-FLOW"
              :unsafe-island :manual-resource-flow
              :safe-api-boundary :stage0/linear-resource-wrapper
              :reason :manual-resource-flow :review :required :status :recorded}]
   :rejected-unsafe-families ["C9-UNSAFE"] :status :complete})
