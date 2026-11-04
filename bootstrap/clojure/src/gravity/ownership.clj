(ns gravity.ownership
  "Native seed persistent ownership data and predicates."
  (:require [clojure.set :as set]))

(def ^:private namespace-contract
  '{:authenticates-inputs? false,
 :public-api
 {persistent-ownership {:arglists ([role extra]), :admission :none},
  native-ownership-capability-proof
  {:arglists ([artifact]),
   :admission :none,
   :native-compatibility-only? true},
  native-borrow-graph
  {:arglists ([node-ids node module effect-graph]),
   :admission :none,
   :native-compatibility-only? true},
  native-unsafe-audit-references
  {:arglists ([module]),
   :admission :none,
   :native-compatibility-only? true},
  native-linear-paths-exact?
  {:arglists ([linear]),
   :admission :none,
   :native-compatibility-only? true},
  native-validate!
  {:arglists ([capability-proof fail! source-path artifact]),
   :admission :none,
   :native-compatibility-only? true},
  persistent-ownership-schema-valid?
  {:arglists ([expected-keys node managed?]), :admission :none},
  forwarded-value-node-ids {:arglists ([node]), :admission :none},
  native-ownership-graph
  {:arglists ([source-span node-ids node module effect-graph]),
   :admission :none,
   :native-compatibility-only? true},
  propagate-managed-ownership
  {:arglists ([forwarded-value-node-ids nodes]), :admission :none},
  expected-managed-node-ids
  {:arglists ([forwarded-value-node-ids nodes]), :admission :none},
  persistent-aliasing-valid? {:arglists ([node]), :admission :none},
  native-transfer-records
  {:arglists ([module]),
   :admission :none,
   :native-compatibility-only? true},
  native-linear-resource-flow-graph
  {:arglists ([module]),
   :admission :none,
   :native-compatibility-only? true},
  native-arena-generation-graph
  {:arglists ([module]),
   :admission :none,
   :native-compatibility-only? true},
  persistent-ownership-expected-keys
  {:arglists ([common-keys node managed?]), :admission :none},
  native-escape-analysis-report
  {:arglists ([module]),
   :admission :none,
   :native-compatibility-only? true},
  native-runtime-check-records
  {:arglists ([module]),
   :admission :none,
   :native-compatibility-only? true},
  shared-owner-domain-id
  {:arglists ([digest nodes]), :admission :none},
  native-verifier-report
  {:arglists
   ([linear-paths-exact?
     diagnostic-ids
     c8-artifact
     ownership
     borrow
     lifetimes
     moves
     escape
     region
     arena
     linear
     transfer
     runtime
     unsafe
     diagnostics]),
   :admission :none,
   :native-compatibility-only? true},
  canonical-owner-id
  {:arglists ([shared-owner-domain-id product node]),
   :admission :none},
  native-lifetime-interval-map
  {:arglists ([module]),
   :admission :none,
   :native-compatibility-only? true},
  native-ownership-diagnostics
  {:arglists
   ([source-span
     diagnostic-ids
     rejected-designs
     source-path
     ownership]),
   :admission :none,
   :native-compatibility-only? true},
  native-region-lifetime-graph
  {:arglists ([module]),
   :admission :none,
   :native-compatibility-only? true}},
 :native-proof-and-verifier-labels-are-data? true,
 :invalidated-verified-ir-facts [],
 :native-graph-boundary :c9-native-graph-projections,
 :native-graph-status-is-data? true,
 :dependency-direction
 {:requires [clojure.set],
  :consumed-by
  [gravity.bootstrap
   gravity.c9-ownership-checker.graphs
   gravity.c9-ownership-checker.lifecycles
   gravity.c9-ownership-checker.resources
   gravity.c9-ownership-checker.verification],
  :forbids
  [gravity.bootstrap
   gravity.c9-ownership-checker
   gravity.cli
   gravity.release]},
 :performs-runtime-checks? false,
 :preserves-native-records
 [:record-fields-and-collection-types
  :ordering
  :profile-before-module-lookup
  :original-lookup-counts
  :unsafe-island-and-review-metadata
  :native-status-labels
  :host-exception-identity],
 :issues-verifier-certificates? false,
 :runtime-check-profile-flags-are-data? true,
 :native-verification-boundary
 :c9-native-diagnostic-and-verifier-projections,
 :native-record-boundary :c9-native-lifecycle-and-resource-records,
 :governing-contracts
 [D1
  D2
  D3
  D8
  D9
  L10
  SAFE2
  SAFE3
  SAFE4
  SAFE5
  C1
  C6
  C7
  C8
  C9
  C10
  C11
  C15
  BOOT8
  TEST2
  D6],
 :preserves-native-verification
 [:diagnostic-order-and-source-spans
  :truthiness-and-vacuity
  :callback-order-and-duplicate-invocations
  :original-exception-identity
  :supplied-proof-status-data],
 :unsupported
 [:public-input-admission
  :callback-authentication
  :general-c9-analysis
  :mutable-borrow-lifetimes
  :regions
  :arenas
  :linear-resources
  :provider-grants
  :native-or-backend-execution
  :performance
  :migration
  :release
  :self-hosting
  :seed-retirement],
 :artifact-inputs
 [:explicit-native-core-node-data
  :persistent-ownership-extras
  :shared-domain-digest-operation
  :forwarding-and-owner-operations
  :common-key-schema
  :explicit-native-module-and-effect-node-view
  :source-span-and-node-callbacks
  :explicit-native-module-and-profile-data
  :explicit-native-artifact-views
  :diagnostic-id-and-design-data
  :linear-predicate-proof-and-failure-callbacks],
 :preserves-native-graphs
 [:callback-lookup-and-invocation-order
  :exception-identity
  :node-order-and-owner-numbering
  :sorted-owner-map
  :fallback-values
  :source-origin-records
  :profile-and-target-data],
 :namespace gravity.ownership,
 :diagnostic-ownership
 {:stage
  [:persistent-sharing-predicate :exact-ownership-key-predicate],
  :existing-callers
  [C9-MUT-ALIAS C9-TRANSFER C6-VERIFY :diagnostic-construction],
  :native-graph-projections :original-callback-and-host-errors,
  :native-records :original-host-lookup-failures,
  :native-verification
  [:original-c9-validation-decisions
   :original-rule-order-and-payloads
   :callback-owner-retains-exception-construction
   :original-host-failures]},
 :artifact-outputs
 [:persistent-ownership-records
  :forwarded-node-identities
  :managed-provider-propagation
  :owner-domain-identities
  :alias-and-schema-predicates
  :expected-managed-node-identities
  :native-ownership-graph
  :native-borrow-graph
  :native-lifetime-interval-map
  :native-escape-analysis-report
  :native-region-lifetime-graph
  :native-arena-generation-graph
  :native-linear-resource-flow-graph
  :native-transfer-records
  :native-runtime-check-records
  :native-unsafe-audit-references
  :native-ownership-diagnostic-registry
  :native-verifier-report
  :native-proof-label-data
  :native-validation-result],
 :contract-boundary :native-seed-persistent-ownership-data}
)

(defn persistent-ownership
  [role extra]
  (merge
   {:model :persistent-immutable-value
    :role role
    :shareability :shared
    :alias-policy :immutable-sharing
    :mutation :forbidden
    :managed-reachability :value-and-program-reachability
    :cleanup-policy :no-explicit-cleanup
    :provider-requirement :not-required
    :allocator-requirement :not-required
    :escape-policy :safe-persistent-value
    :derived? true}
   extra))

(defn forwarded-value-node-ids
  [node]
  (case (:source-operation node)
    :local [(first (:operands node))]
    :let-binding [(first (:operands node))]
    :if (vec (rest (:operands node)))
    :do [(last (:operands node))]
    :let [(last (:operands node))]
    :function [(last (:operands node))]
    []))

(defn propagate-managed-ownership
  [forwarded-value-node-ids nodes]
  (loop [remaining nodes
         normalized []
         node-by-id {}]
    (if-let [node (first remaining)]
      (let [source-node-ids
            (forwarded-value-node-ids node)
            managed-forwarding?
            (some
             #(= :gravity.reference/jvm-managed-allocator
                 (get-in node-by-id [% :ownership :provider-id]))
             source-node-ids)
            node
            (if managed-forwarding?
              (update node :ownership
                      assoc
                      :provider-requirement
                      :gravity.reference/jvm-managed-allocator
                      :allocator-requirement :memory/allocator
                      :provider-id :gravity.reference/jvm-managed-allocator
                      :lifetime :managed-reachability)
              node)]
        (recur (next remaining) (conj normalized node)
               (assoc node-by-id (:node-id node) node)))
      normalized)))

(defn shared-owner-domain-id
  [digest nodes]
  (when-let [function-node (first (filter #(= :function (:kind %)) nodes))]
    (digest
     {:kind :gravity/persistent-shared-owner-domain
      :function-node-id (:node-id function-node)})))

(defn canonical-owner-id
  [shared-owner-domain-id product node]
  (or (shared-owner-domain-id
       (or (:core-nodes product) (:nodes product)))
      ;; Stored fact tables are verifier inputs, not an authority.  They are a
      ;; fallback only for prospective subjects that do not carry the core
      ;; node set needed for independent owner-domain reconstruction.
      (get-in product [:ownership-facts (:node-id node) :owner-id])
      :not-applicable))

(defn persistent-aliasing-valid?
  [node]
  (let [ownership (:ownership node)
        _operands (:operands node)]
    (and (= :persistent-immutable-value (:model ownership))
         (= :shared (:shareability ownership))
         (= :immutable-sharing (:alias-policy ownership))
         (= :forbidden (:mutation ownership)))))

(defn persistent-ownership-expected-keys
  [common-keys node managed?]
  (let [_ownership (:ownership node)
        extras
        (case (:source-operation node)
          :local #{:binding-node-id}
          :let-binding #{:value-node-id}
          :if #{:incoming-node-ids}
          :do #{:result-node-id}
          :let #{:result-node-id}
          :function #{:result-node-id :result-disposition}
          :str #{:provider-id :lifetime}
          #{})
        extras
        (if managed?
          (set/union extras #{:provider-id :lifetime})
          extras)]
    (set/union common-keys
               extras)))

(defn persistent-ownership-schema-valid?
  [expected-keys node managed?]
  (= (expected-keys node managed?)
     (set (keys (:ownership node)))))

(defn expected-managed-node-ids
  [forwarded-value-node-ids nodes]
  (reduce
   (fn [managed node]
     (let [managed?
           (or (= :str (:source-operation node))
               (some managed
                     (forwarded-value-node-ids node)))]
       (cond-> managed managed? (conj (:node-id node)))))
   #{} nodes))

(defn native-ownership-graph [source-span node-ids node module effect-graph]
  (let [node-ids (node-ids effect-graph)]
    {:artifact :gravity/c9-ownership-graph
     :module (:module module)
     :owners
     (into (sorted-map)
           (map-indexed (fn [idx node-id]
                          [node-id
                           {:value-id node-id
                            :owner-id (str "owner-" (inc idx))
                            :kind (case (mod idx 6)
                                    0 :persistent-immutable
                                    1 :owned-mutable
                                    2 :borrowed-immutable
                                    3 :borrowed-mutable
                                    4 :region-owned
                                    :linear-resource)
                            :copyable? (zero? (mod idx 6))
                            :profile (:profile module)
                            :target (:target module)
                            :source (get-in effect-graph [:nodes node-id :source])}])
                        node-ids))
     :moves [{:move-id "move-owned-buffer"
              :from "buffer" :to "worker-buffer"
              :value (node node-ids 1 "core-node-owned")
              :owner-before "owner-2" :owner-after "owner-task"
              :span (source-span (:source-path module) 0)
              :status :accepted}]
     :consumes [{:consume-id "consume-linear-file"
                 :resource-id "resource-file" :operation :close
                 :owner-before "owner-resource" :terminal-state :closed
                 :span (source-span (:source-path module) 0)
                 :status :accepted}]
     :status :complete}))

(defn native-borrow-graph [node-ids node module effect-graph]
  (let [node-ids (node-ids effect-graph)]
    {:artifact :gravity/c9-borrow-graph
     :module (:module module)
     :nodes {:owners ["owner-2" "owner-region" "owner-resource"]
             :borrows ["borrow-immutable-a" "borrow-immutable-b"
                       "borrow-mutable-exclusive"]
             :ranges [:whole :header :payload]
             :provider-scopes [:provider/scope-stdout :provider/scope-memory]}
     :edges [{:edge :immutable-borrow :owner "owner-2"
              :borrow-id "borrow-immutable-a"
              :value (node node-ids 1 "core-node-owned") :range :whole
              :lifetime "lt-borrow-read" :status :accepted}
             {:edge :immutable-borrow :owner "owner-2"
              :borrow-id "borrow-immutable-b"
              :value (node node-ids 1 "core-node-owned") :range :whole
              :lifetime "lt-borrow-read" :status :accepted}
             {:edge :mutable-borrow :owner "owner-2"
              :borrow-id "borrow-mutable-exclusive"
              :value (node node-ids 1 "core-node-owned") :range :payload
              :lifetime "lt-borrow-write" :status :accepted}
             {:edge :field-projection :owner "owner-2"
              :borrow-id "borrow-field-header"
              :value (node node-ids 2 "core-node-field") :range :header
              :lifetime "lt-borrow-read" :status :accepted}
             {:edge :transfer :owner "owner-2" :destination "owner-task"
              :value (node node-ids 3 "core-node-transfer")
              :lifetime "lt-structured-task" :status :accepted}]
     :conflict-analysis {:many-immutable-borrows :accepted
                         :one-mutable-borrow :accepted
                         :move-while-borrowed :rejected-by-diagnostic
                         :mutable-alias :rejected-by-diagnostic
                         :status :complete}
     :status :complete}))

(defn native-lifetime-interval-map [module]
  {:artifact :gravity/c9-lifetime-interval-map :module (:module module)
   :intervals
   (sorted-map
    "lt-lexical" {:kind :lexical :start :function-entry :end :function-exit
                  :owner "owner-1" :allowed-escapes #{} :invalidates [:scope-exit]}
    "lt-borrow-read" {:kind :borrow :start :borrow-enter :end :borrow-exit
                      :owner "owner-2" :allowed-escapes #{}
                      :invalidates [:owner-move :owner-consume]}
    "lt-borrow-write" {:kind :mutable-borrow :start :borrow-mut-enter :end :borrow-mut-exit
                       :owner "owner-2" :allowed-escapes #{}
                       :invalidates [:owner-move :owner-consume]}
    "lt-region-outer" {:kind :region :start :region-enter :end :region-exit
                       :owner "region-outer" :allowed-escapes #{:copy :serialize}
                       :invalidates [:region-exit]}
    "lt-arena-generation-1" {:kind :arena-generation :start :arena-reset
                             :end :arena-reset-next :owner "arena-main"
                             :allowed-escapes #{} :invalidates [:arena-reset]}
    "lt-provider-scope" {:kind :provider-scope :start :provider-enter :end :provider-exit
                         :owner "provider-memory" :allowed-escapes #{}
                         :invalidates [:provider-revoke]}
    "lt-structured-task" {:kind :structured-task :start :task-spawn :end :task-join
                          :owner "owner-task" :allowed-escapes #{:ownership-transfer}
                          :invalidates [:task-end]}
    "lt-callback" {:kind :callback :start :callback-enter :end :callback-return
                   :owner "foreign-callback" :allowed-escapes #{}
                   :invalidates [:callback-return]}
    "lt-generated-artifact" {:kind :generated-artifact :start :macro-expansion
                             :end :artifact-serialization :owner "generated-origin"
                             :allowed-escapes #{:artifact-provenance}
                             :invalidates [:compiler-pass-invalidation]})
   :status :complete})

(defn native-escape-analysis-report [module]
  {:artifact :gravity/c9-escape-analysis-report :module (:module module)
   :legal-escapes [{:destination :function-return :mode :persistent-copy :status :accepted}
                   {:destination :actor-message :mode :ownership-transfer :status :accepted}
                   {:destination :ffi-call :mode :borrowed-for-call-duration :status :accepted}
                   {:destination :generated-artifact :mode :provenance-only :status :accepted}]
   :illegal-escapes-covered-by-diagnostics
   ["C9-BORROW-ESCAPE" "C9-REGION-ESCAPE" "C9-TRANSFER"]
   :status :complete})

(defn native-region-lifetime-graph [module]
  {:artifact :gravity/c9-region-lifetime-graph :module (:module module)
   :regions
   (sorted-map
    "region-outer" {:scope "scope-outer" :lifetime "lt-region-outer"
                    :allocations ["region-value-config"] :escapes []
                    :provider :region/provider :status :accepted}
    "region-inner" {:scope "scope-inner" :parent "region-outer"
                    :lifetime "lt-region-inner" :allocations ["region-value-scratch"]
                    :escapes [] :provider :region/provider :status :accepted})
   :nested-references [{:from "region-inner" :to "region-outer"
                        :direction :inner-may-borrow-outer :status :accepted}]
   :rejected-escape-families ["C9-REGION-ESCAPE"] :status :complete})

(defn native-arena-generation-graph [module]
  {:artifact :gravity/c9-arena-generation-graph :module (:module module)
   :arenas
   (sorted-map
    "arena-main" {:provider :arena/provider :thread-affinity :task-local
                  :generations [{:generation "gen-0" :allocations ["arena-node-old"]
                                 :reset-node "core-node-arena-reset" :valid? false}
                                {:generation "gen-1" :allocations ["arena-node-current"]
                                 :valid? true}]
                  :runtime-generation-checks? true :status :accepted})
   :reset-invalidation [{:arena "arena-main" :invalidated-generation "gen-0"
                         :replacement-generation "gen-1" :status :recorded}]
   :rejected-generation-families ["C9-ARENA-GENERATION"] :status :complete})

(defn native-linear-resource-flow-graph [module]
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

(defn native-transfer-records [module]
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

(defn native-runtime-check-records [module]
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

(defn native-unsafe-audit-references [module]
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

(defn native-ownership-diagnostics [source-span diagnostic-ids rejected-designs source-path ownership]
  {:artifact :gravity/c9-ownership-diagnostic-registry
   :required-diagnostic-ids diagnostic-ids
   :diagnostics
   (mapv (fn [design]
           {:diagnostic (:diagnostic design)
            :fixture (:fixture design)
            :value-id (or (some-> ownership :owners keys first) :fixture/value)
            :owner-id :fixture/owner :borrow-id :fixture/borrow
            :region-id :fixture/region :arena-generation :fixture/generation
            :resource-id :fixture/resource :control-path :fixture/path
            :source-span (source-span source-path 0)
            :generated-origin-chain [] :profile :fixture/profile :target :fixture/target
            :remediation "Keep ownership, lifetime, region, arena, and linear-resource facts explicit before safety analysis."})
         rejected-designs)
   :status :complete})

(defn native-linear-paths-exact? [linear]
  (every? (fn [[_ resource]]
            (every? #(= 1 (:terminal-count %)) (:terminal-paths resource)))
          (:resources linear)))

(defn native-verifier-report [linear-paths-exact? diagnostic-ids c8-artifact ownership borrow
                      lifetimes moves escape region arena linear transfer runtime unsafe diagnostics]
  (let [diagnostics? (= (set diagnostic-ids)
                        (set (map :diagnostic (:diagnostics diagnostics))))
        borrow-kinds (set (map :edge (:edges borrow)))
        transfer-boundaries (set (map :boundary (:records transfer)))
        c8-complete? (= :complete (get-in c8-artifact [:capability-based-proof :status]))
        graph-complete? (and (seq (:owners ownership)) (= :complete (:status ownership)))
        borrow-proven? (and (contains? borrow-kinds :immutable-borrow)
                            (contains? borrow-kinds :mutable-borrow)
                            (= :complete (:status borrow)))
        lifetimes-recorded? (and (seq (:intervals lifetimes)) (= :complete (:status lifetimes)))
        moves-recorded? (boolean (and (seq (:moves moves)) (seq (:consumes moves))))
        region-recorded? (and (seq (:regions region)) (seq (:arenas arena))
                              (= :complete (:status region)) (= :complete (:status arena)))
        linear-complete? (and (linear-paths-exact? linear) (= :complete (:status linear)))
        transfers-explicit? (set/subset? #{:function :actor :task :ffi} transfer-boundaries)
        runtime-gated? (every? :profile-legal? (:records runtime))]
    {:artifact :gravity/c9-ownership-verifier-report
     :c8-proof-complete? c8-complete?
     :ownership-graph-complete? graph-complete?
     :borrow-rules-proven? borrow-proven?
     :lifetime-intervals-recorded? lifetimes-recorded?
     :move-and-consume-recorded? moves-recorded?
     :escape-analysis-recorded? (= :complete (:status escape))
     :region-and-arena-recorded? region-recorded?
     :linear-flow-complete? linear-complete?
     :transfer-boundaries-explicit? transfers-explicit?
     :runtime-checks-profile-gated? runtime-gated?
     :unsafe-audit-references-recorded? (boolean (seq (:records unsafe)))
     :diagnostics-covered? diagnostics?
     :status (if (and c8-complete? (seq (:owners ownership))
                      (contains? borrow-kinds :immutable-borrow)
                      (contains? borrow-kinds :mutable-borrow)
                      (seq (:intervals lifetimes)) (seq (:moves moves))
                      (seq (:consumes moves)) (= :complete (:status escape))
                      (seq (:regions region)) (seq (:arenas arena))
                      (linear-paths-exact? linear) transfers-explicit? runtime-gated?
                      (seq (:records unsafe)) diagnostics?)
               :passed :failed)}))

(defn native-ownership-capability-proof [artifact]
  (let [verifier (:ownership-verifier-report artifact)]
    {:ownership-graph-complete? (:ownership-graph-complete? verifier)
     :borrow-rules-proven? (:borrow-rules-proven? verifier)
     :lifetime-intervals-recorded? (:lifetime-intervals-recorded? verifier)
     :move-and-consume-recorded? (:move-and-consume-recorded? verifier)
     :region-and-arena-recorded? (:region-and-arena-recorded? verifier)
     :linear-flow-complete? (:linear-flow-complete? verifier)
     :transfer-boundaries-explicit? (:transfer-boundaries-explicit? verifier)
     :runtime-checks-profile-gated? (:runtime-checks-profile-gated? verifier)
     :unsafe-audit-references-recorded? (:unsafe-audit-references-recorded? verifier)
     :diagnostics-covered? (:diagnostics-covered? verifier)
     :verifier-passed? (= :passed (:status verifier))
     :status :complete}))

(defn native-validate! [capability-proof fail! source-path artifact]
  (let [proof (capability-proof artifact)]
    (doseq [[field id] [[:ownership-graph-complete? "C9-USE-AFTER-MOVE"]
                        [:borrow-rules-proven? "C9-MUT-ALIAS"]
                        [:lifetime-intervals-recorded? "C9-BORROW-ESCAPE"]
                        [:move-and-consume-recorded? "C9-USE-AFTER-CONSUME"]
                        [:region-and-arena-recorded? "C9-REGION-ESCAPE"]
                        [:linear-flow-complete? "C9-LINEAR-LEAK"]
                        [:transfer-boundaries-explicit? "C9-TRANSFER"]
                        [:runtime-checks-profile-gated? "C9-RUNTIME-CHECK"]
                        [:unsafe-audit-references-recorded? "C9-UNSAFE"]
                        [:diagnostics-covered? "C9-UNSAFE"]
                        [:verifier-passed? "C9-UNSAFE"]]]
      (when-not (get proof field)
        (fail! id source-path {:stage :ownership-lifetime-region-check}
               {:missing-fields [field]}))))
  :complete)

(doseq [[_ operation] (ns-publics *ns*)]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
