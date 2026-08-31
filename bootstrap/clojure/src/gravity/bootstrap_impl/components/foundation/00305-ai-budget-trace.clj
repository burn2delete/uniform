

(defn ai-budget-trace
  [input-id]
  {:artifact :gravity/ai-budget-trace
   :input-artifact input-id
   :limits {:tokens 1000 :tool-calls 3 :usd-micros 10000
            :retries 1 :human-review 1}
   :usage {:tokens 42 :tool-calls 1 :usd-micros 100
           :retries 0 :human-review 1}
   :violations []
   :status :complete})

(defn ai-replay-barrier-record
  [input-id]
  {:artifact :gravity/ai-replay-barrier-record
   :input-artifact input-id
   :records [{:segment-id "segment/support"
              :model-output :recorded
              :tool-output :recorded
              :memory-read :recorded
              :human-review-decision :recorded
              :policy-decision :recorded}]
   :live-calls-in-replay []
   :status :complete})

(defn repl-runtime-manifest
  [input-id]
  {:artifact :gravity/repl-runtime
   :input-artifact input-id
   :family :interactive
   :profile :meta
   :target :jvm
   :services #{:incremental-eval :macro-inspection :hot-reload
               :debugger :artifact-inspection}
   :requires #{:session-policy :capability-grants :audit-log
               :incremental-state :compiler-pipeline-checks}
   :records #{:session-transcript :typed-core-snapshot :mir-diff
              :capability-decision-log}
   :rejects #{:dynamic-eval-forbidden-profile :unhermetic-session-state
              :undeclared-interactive-io :compiler-check-bypass}
   :status :complete})

(defn repl-session-artifacts
  [source-path input-id]
  {:session-transcript
   {:artifact :gravity/session-transcript
    :input-artifact input-id
    :session-id "session/stage0"
    :forms [{:evaluated-form-id "eval-1"
             :source-span (source-span source-path 1)
             :pipeline [:reader :macro :type-check :effect-check
                        :capability-check :safety-analysis]
             :result :accepted}]
    :status :complete}
   :evaluated-form-artifact
   {:artifact :gravity/evaluated-form-artifact
    :input-artifact input-id
    :evaluated-form-id "eval-1"
    :compiler-checks-passed? true
    :effects #{:build/read-file}
    :capabilities #{:build/read-file}
    :status :complete}
   :syntax-object-snapshot
   {:artifact :gravity/syntax-object-snapshot
    :input-artifact input-id
    :forms ["eval-1"]
    :source-spans-preserved? true
    :status :complete}
   :macro-expansion-diff
   {:artifact :gravity/macro-expansion-diff
    :input-artifact input-id
    :introduced-forms []
    :hygiene-preserved? true
    :status :complete}
   :typed-core-snapshot
   {:artifact :gravity/typed-core-snapshot
    :input-artifact input-id
    :typed? true
    :effects-checked? true
    :status :complete}
   :mir-domain-ir-snapshot
   {:artifact :gravity/mir-domain-ir-snapshot
    :input-artifact input-id
    :mir-snapshot "mir/eval-1"
    :domain-ir-snapshot "domain/eval-1"
    :status :complete}
   :repl-capability-decision-log
   {:artifact :gravity/repl-capability-decision-log
    :input-artifact input-id
    :decisions [{:action-id "repl/read"
                 :effect :build/read-file
                 :capability :build/read-file
                 :decision :grant
                 :audit :recorded}
                {:action-id "repl/debug-secret"
                 :effect :debug/read-state
                 :capability :debug/read-state
                 :decision :deny
                 :audit :recorded}]
    :status :complete}
   :incremental-invalidation-record
   {:artifact :gravity/repl-incremental-invalidation-record
    :input-artifact input-id
    :invalidates [:syntax :core :mir :backend :runtime :package]
    :stale-artifacts []
    :status :complete}
   :hot-reload-record
   {:artifact :gravity/hot-reload-record
    :input-artifact input-id
    :source-change "eval-1"
    :preserved-runtime-state [:pure-cache]
    :restarted-runtime-state [:ffi-handles]
    :stale-analysis-kept? false
    :status :complete}})

(defn ffi-runtime-manifest
  [input-id]
  {:artifact :gravity/ffi-runtime
   :input-artifact input-id
   :family :ffi
   :services #{:foreign-call :layout-adapter :error-conversion
               :callback-adapter :safe-wrapper}
   :requires #{:abi :calling-convention :layout-tests :lifetime-policy
               :ownership-transfer :effect-map :capability-policy}
   :records #{:ffi-binding-manifest :symbol-resolution :unsafe-audit
              :safe-wrapper-contract}
   :rejects #{:raw-extern-from-safe-code :pointer-without-lifetime
              :foreign-effect-without-grant :unchecked-null}
   :status :complete})

(defn ffi-runtime-artifacts
  [input-id]
  {:binding-manifest
   {:artifact :gravity/ffi-binding-manifest
    :input-artifact input-id
    :bindings [{:binding-id "ffi/libc-open"
                :foreign-library "libc"
                :symbol "open"
                :abi :c
                :calling-convention :c
                :gravity-type 'Path
                :foreign-type "const char*"
                :layout :validated
                :alignment 8
                :nullability :checked
                :lifetime :borrowed-call
                :ownership :no-transfer
                :effects #{:ffi/call :filesystem/read}
                :capabilities #{:ffi/c :fs/read}
                :error-mapping :errno-to-result
                :taint :foreign-input}]
    :status :complete}
   :symbol-resolution-record
   {:artifact :gravity/symbol-resolution-record
    :input-artifact input-id
    :symbols [{:binding-id "ffi/libc-open"
               :symbol "open"
               :resolution :package-policy-approved
               :dynamic-loading :denied-by-default}]
    :status :complete}
   :abi-layout-validation-report
   {:artifact :gravity/abi-layout-validation-report
    :input-artifact input-id
    :target-abi :c
    :layout-tests [{:binding-id "ffi/libc-open"
                    :result :passed}]
    :mismatches []
    :status :complete}
   :generated-adapter-artifact
   {:artifact :gravity/generated-ffi-adapter-artifact
    :input-artifact input-id
    :adapter-id "adapter/libc-open"
    :validates [:layout :nullability :capability :error-mapping]
    :status :complete}
   :safe-wrapper-contract
   {:artifact :gravity/safe-wrapper-contract
    :input-artifact input-id
    :wrapper-id "wrapper/open"
    :preconditions [:path-valid :capability-present]
    :runtime-checks [:null-check :errno-check]
    :ensures [:result-or-error]
    :unsafe-island "R10-FFI-STAGE0"
    :status :complete}
   :foreign-handle-lifetime-table
   {:artifact :gravity/foreign-handle-lifetime-table
    :input-artifact input-id
    :handles [{:handle-id "fd/stage0"
               :provider :libc
               :raw-representation :int
               :lifetime-owner :linear-resource
               :release-function "close"
               :nullability :not-null
               :thread-affinity :none
               :aliasing :linear
               :taint :foreign-handle}]
    :missing-lifetimes []
    :status :complete}
   :callback-adapter-manifest
   {:artifact :gravity/callback-adapter-manifest
    :input-artifact input-id
    :callbacks [{:callback-id "cb/stage0"
                 :thread-affinity :scheduler-checked
                 :taint :preserved
                 :error-mapping :foreign-error-to-result
                 :capability-check :required}]
    :violations []
    :status :complete}
   :ffi-unsafe-audit-record
   {:artifact :gravity/ffi-unsafe-audit-record
    :input-artifact input-id
    :unsafe-island "R10-FFI-STAGE0"
    :safe-wrapper "wrapper/open"
    :audited? true
    :status :complete}})