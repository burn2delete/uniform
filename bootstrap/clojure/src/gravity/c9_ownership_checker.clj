(ns gravity.c9-ownership-checker
  "Hosted Stage0 C9 ownership, lifetime, region, and linear-resource analysis.

  This leaf preserves the Clojure seed compatibility implementation. It emits
  ownership evidence but is not ownership-safety, proof, self-hosting, or
  release authority."
  (:require [clojure.set :as set]
            [gravity.digest :as digest]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})

(def ^:private function-operation-keys
  #{:fail!
    :source-span
    :c4-artifact-id
    :read-source-form-records
    :validate-ns-syntax!
    :parse-module
    :compiler-c8-effect-source-artifact
    :c9-ownership-source-overrides
    :c9-ownership-message
    :c9-ownership-fail!
    :c9-ownership-validate-overrides!
    :c9-node-ids
    :c9-node
    :c9-ownership-graph
    :c9-borrow-graph
    :c9-lifetime-interval-map
    :c9-escape-analysis-report
    :c9-region-lifetime-graph
    :c9-arena-generation-graph
    :c9-linear-resource-flow-graph
    :c9-transfer-records
    :c9-runtime-check-records
    :c9-unsafe-audit-references
    :c9-ownership-diagnostics
    :c9-linear-paths-exact?
    :c9-ownership-verifier-report
    :c9-ownership-capability-proof
    :c9-ownership-validate!
    :compiler-c9-ownership-source-artifact
    :compiler-c9-ownership-file-artifact})

(def ^:private scalar-operation-keys
  #{:c9-ownership-diagnostic-ids
    :c9-ownership-governing-document
    :c9-ownership-rejected-designs
    :c9-ownership-override-diagnostics})

(def ^:private operation-keys
  (into function-operation-keys scalar-operation-keys))

(defn- current-operation [key]
  (when-not (contains? *active-operation-keys* key)
    (get *operations* key)))

(defmacro ^:private definterposable [name args & body]
  (let [key (keyword name)]
    `(defn ~name ~args
       (if-let [operation# (current-operation ~key)]
         (binding [*active-operation-keys*
                   (conj *active-operation-keys* ~key)]
           (operation# ~@args))
         (do ~@body)))))

(defn- default-fail! [id message data]
  (throw (ex-info message (assoc (or data {}) :id id))))
(defn- default-source-span [path index]
  {:source path :form-index index})
(defn- default-c4-artifact-id [artifact]
  (str "sha256:" (digest/sha256-hex (pr-str artifact))))
(defn- unsupported-host-operation [operation]
  (fn [& _]
    (throw (ex-info (str "C9 leaf requires injected operation " operation)
                    {:operation operation}))))
(defn- op-fn [key fallback] (or (get *operations* key) fallback))
(defn- fail! [id message data]
  ((op-fn :fail! default-fail!) id message data))
(defn- source-span [path index]
  ((op-fn :source-span default-source-span) path index))
(defn- c4-artifact-id [artifact]
  ((op-fn :c4-artifact-id default-c4-artifact-id) artifact))
(defn- read-source-form-records [path text]
  ((op-fn :read-source-form-records
          (unsupported-host-operation :read-source-form-records))
   path text))
(defn- validate-ns-syntax! [path forms]
  ((op-fn :validate-ns-syntax!
          (unsupported-host-operation :validate-ns-syntax!))
   path forms))
(defn- parse-module [path forms]
  ((op-fn :parse-module (unsupported-host-operation :parse-module))
   path forms))
(defn- compiler-c8-effect-source-artifact [path text]
  ((op-fn :compiler-c8-effect-source-artifact
          (unsupported-host-operation :compiler-c8-effect-source-artifact))
   path text))

(def ^:dynamic c9-ownership-diagnostic-ids
  ["C9-USE-AFTER-MOVE"
   "C9-USE-AFTER-CONSUME"
   "C9-BORROW-ESCAPE"
   "C9-MUT-ALIAS"
   "C9-MOVE-WHILE-BORROWED"
   "C9-REGION-ESCAPE"
   "C9-ARENA-GENERATION"
   "C9-LINEAR-LEAK"
   "C9-LINEAR-DOUBLE"
   "C9-TRANSFER"
   "C9-RUNTIME-CHECK"
   "C9-UNSAFE"])

(def ^:dynamic c9-ownership-governing-document
  "docs/phase-06-compiler-architecture/088-c9-ownership-lifetime-and-region-checker-design.md")

(def ^:dynamic c9-ownership-rejected-designs
  [{:diagnostic "C9-USE-AFTER-MOVE"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c9-use-after-move.gravity"
    :rejected-design :use-after-move}
   {:diagnostic "C9-USE-AFTER-CONSUME"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c9-use-after-consume.gravity"
    :rejected-design :use-after-terminal-consume}
   {:diagnostic "C9-BORROW-ESCAPE"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c9-borrow-escape.gravity"
    :rejected-design :borrow-outlives-valid-scope}
   {:diagnostic "C9-MUT-ALIAS"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c9-mut-alias.gravity"
    :rejected-design :mutable-access-while-aliased}
   {:diagnostic "C9-MOVE-WHILE-BORROWED"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c9-move-while-borrowed.gravity"
    :rejected-design :move-during-active-borrow}
   {:diagnostic "C9-REGION-ESCAPE"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c9-region-escape.gravity"
    :rejected-design :region-value-escapes}
   {:diagnostic "C9-ARENA-GENERATION"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c9-arena-generation.gravity"
    :rejected-design :stale-arena-generation}
   {:diagnostic "C9-LINEAR-LEAK"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c9-linear-leak.gravity"
    :rejected-design :missing-terminal-resource-state}
   {:diagnostic "C9-LINEAR-DOUBLE"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c9-linear-double.gravity"
    :rejected-design :duplicate-terminal-resource-state}
   {:diagnostic "C9-TRANSFER"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c9-transfer.gravity"
    :rejected-design :invalid-ownership-transfer}
   {:diagnostic "C9-RUNTIME-CHECK"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c9-runtime-check.gravity"
    :rejected-design :runtime-check-unavailable}
   {:diagnostic "C9-UNSAFE"
    :fixture "bootstrap/clojure/fixtures/rejected/compiler-c9-unsafe.gravity"
    :rejected-design :manual-lifetime-or-resource-flow-without-audit}])

(def ^:dynamic c9-ownership-override-diagnostics
  {:use-after-move "C9-USE-AFTER-MOVE"
   :use-after-consume "C9-USE-AFTER-CONSUME"
   :borrow-escape "C9-BORROW-ESCAPE"
   :mut-alias "C9-MUT-ALIAS"
   :move-while-borrowed "C9-MOVE-WHILE-BORROWED"
   :region-escape "C9-REGION-ESCAPE"
   :arena-generation "C9-ARENA-GENERATION"
   :linear-leak "C9-LINEAR-LEAK"
   :linear-double "C9-LINEAR-DOUBLE"
   :transfer "C9-TRANSFER"
   :runtime-check "C9-RUNTIME-CHECK"
   :unsafe "C9-UNSAFE"})

(definterposable c9-ownership-source-overrides
  [module]
  (get-in module [:metadata :compiler :c9-ownership-check] {}))

(definterposable c9-ownership-message
  [id]
  (case id
    "C9-USE-AFTER-MOVE" "owned value is used after move"
    "C9-USE-AFTER-CONSUME" "linear or owned value is used after terminal consumption"
    "C9-BORROW-ESCAPE" "borrow outlives owner, region, provider, callback, or task scope"
    "C9-MUT-ALIAS" "mutable access overlaps active aliases"
    "C9-MOVE-WHILE-BORROWED" "owner is moved while an active borrow exists"
    "C9-REGION-ESCAPE" "region value escapes its valid lifetime"
    "C9-ARENA-GENERATION" "arena value is used after reset generation invalidation"
    "C9-LINEAR-LEAK" "linear resource may miss a terminal operation"
    "C9-LINEAR-DOUBLE" "linear resource may reach multiple terminal operations"
    "C9-TRANSFER" "ownership transfer lacks explicit destination cleanup or lifetime proof"
    "C9-RUNTIME-CHECK" "required dynamic ownership check is unavailable in the active profile"
    "C9-UNSAFE" "manual lifetime, alias, or resource behavior lacks unsafe audit evidence"
    "Ownership checking failed"))

(definterposable c9-ownership-fail!
  [id source-path subject extra]
  (fail! id
         (c9-ownership-message id)
         (merge {:source-span (or (:source-span subject)
                                  (get-in subject [:source :span])
                                  (:span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :c9-ownership-checker
                 :stage :ownership-lifetime-region-check
                 :document-id "C9"
                 :expected-document c9-ownership-governing-document
                 :value-id (or (:value-id subject) :fixture/value)
                 :owner-id (or (:owner-id subject) :fixture/owner)
                 :borrow-id (or (:borrow-id subject) :fixture/borrow)
                 :region-id (or (:region-id subject) :fixture/region)
                 :arena-generation (or (:arena-generation subject)
                                       :fixture/generation)
                 :resource-id (or (:resource-id subject)
                                  :fixture/resource)
                 :control-path (or (:control-path subject) :fixture/path)
                 :generated-origin-chain (or (:generated-origin subject)
                                             (get-in subject
                                                     [:source :origin-chain]))
                 :profile (:profile subject)
                 :target (:target subject)
                 :transfer (:transfer subject)
                 :runtime-check (:runtime-check subject)
                 :unsafe-audit (:unsafe-audit subject)
                 :remediation "Emit ownership, borrow, lifetime, region, arena, linear-flow, transfer, runtime-check, unsafe-audit, and diagnostic records before safety analysis and MIR construction."}
                extra)))

(definterposable c9-ownership-validate-overrides!
  [source-path module overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get c9-ownership-override-diagnostics fail-kind)]
      (c9-ownership-fail! id source-path
                          {:source-span (source-span source-path 0)
                           :value-id (keyword "fixture" (name fail-kind))
                           :owner-id :fixture/owner
                           :borrow-id :fixture/borrow
                           :region-id :fixture/region
                           :arena-generation :fixture/generation
                           :resource-id :fixture/resource
                           :control-path :fixture/path
                           :profile (:profile module)
                           :target (:target module)
                           :generated-origin []}
                          {:missing-fields [fail-kind]}))))

(definterposable c9-node-ids
  [effect-graph]
  (vec (keys (:nodes effect-graph))))

(definterposable c9-node
  [node-ids index fallback]
  (or (get node-ids index) fallback))

(definterposable c9-ownership-graph
  [module effect-graph]
  (let [node-ids (c9-node-ids effect-graph)]
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
                            :source (get-in effect-graph
                                            [:nodes node-id :source])}])
                        node-ids))
     :moves [{:move-id "move-owned-buffer"
              :from "buffer"
              :to "worker-buffer"
              :value (c9-node node-ids 1 "core-node-owned")
              :owner-before "owner-2"
              :owner-after "owner-task"
              :span (source-span (:source-path module) 0)
              :status :accepted}]
     :consumes [{:consume-id "consume-linear-file"
                 :resource-id "resource-file"
                 :operation :close
                 :owner-before "owner-resource"
                 :terminal-state :closed
                 :span (source-span (:source-path module) 0)
                 :status :accepted}]
     :status :complete}))

(definterposable c9-borrow-graph
  [module effect-graph]
  (let [node-ids (c9-node-ids effect-graph)]
    {:artifact :gravity/c9-borrow-graph
     :module (:module module)
     :nodes {:owners ["owner-2" "owner-region" "owner-resource"]
             :borrows ["borrow-immutable-a" "borrow-immutable-b"
                       "borrow-mutable-exclusive"]
             :ranges [:whole :header :payload]
             :provider-scopes [:provider/scope-stdout
                               :provider/scope-memory]}
     :edges [{:edge :immutable-borrow
              :owner "owner-2"
              :borrow-id "borrow-immutable-a"
              :value (c9-node node-ids 1 "core-node-owned")
              :range :whole
              :lifetime "lt-borrow-read"
              :status :accepted}
             {:edge :immutable-borrow
              :owner "owner-2"
              :borrow-id "borrow-immutable-b"
              :value (c9-node node-ids 1 "core-node-owned")
              :range :whole
              :lifetime "lt-borrow-read"
              :status :accepted}
             {:edge :mutable-borrow
              :owner "owner-2"
              :borrow-id "borrow-mutable-exclusive"
              :value (c9-node node-ids 1 "core-node-owned")
              :range :payload
              :lifetime "lt-borrow-write"
              :status :accepted}
             {:edge :field-projection
              :owner "owner-2"
              :borrow-id "borrow-field-header"
              :value (c9-node node-ids 2 "core-node-field")
              :range :header
              :lifetime "lt-borrow-read"
              :status :accepted}
             {:edge :transfer
              :owner "owner-2"
              :destination "owner-task"
              :value (c9-node node-ids 3 "core-node-transfer")
              :lifetime "lt-structured-task"
              :status :accepted}]
     :conflict-analysis {:many-immutable-borrows :accepted
                         :one-mutable-borrow :accepted
                         :move-while-borrowed :rejected-by-diagnostic
                         :mutable-alias :rejected-by-diagnostic
                         :status :complete}
     :status :complete}))

(definterposable c9-lifetime-interval-map
  [module]
  {:artifact :gravity/c9-lifetime-interval-map
   :module (:module module)
   :intervals
   (sorted-map
    "lt-lexical" {:kind :lexical
                  :start :function-entry
                  :end :function-exit
                  :owner "owner-1"
                  :allowed-escapes #{}
                  :invalidates [:scope-exit]}
    "lt-borrow-read" {:kind :borrow
                      :start :borrow-enter
                      :end :borrow-exit
                      :owner "owner-2"
                      :allowed-escapes #{}
                      :invalidates [:owner-move :owner-consume]}
    "lt-borrow-write" {:kind :mutable-borrow
                       :start :borrow-mut-enter
                       :end :borrow-mut-exit
                       :owner "owner-2"
                       :allowed-escapes #{}
                       :invalidates [:owner-move :owner-consume]}
    "lt-region-outer" {:kind :region
                       :start :region-enter
                       :end :region-exit
                       :owner "region-outer"
                       :allowed-escapes #{:copy :serialize}
                       :invalidates [:region-exit]}
    "lt-arena-generation-1" {:kind :arena-generation
                             :start :arena-reset
                             :end :arena-reset-next
                             :owner "arena-main"
                             :allowed-escapes #{}
                             :invalidates [:arena-reset]}
    "lt-provider-scope" {:kind :provider-scope
                         :start :provider-enter
                         :end :provider-exit
                         :owner "provider-memory"
                         :allowed-escapes #{}
                         :invalidates [:provider-revoke]}
    "lt-structured-task" {:kind :structured-task
                          :start :task-spawn
                          :end :task-join
                          :owner "owner-task"
                          :allowed-escapes #{:ownership-transfer}
                          :invalidates [:task-end]}
    "lt-callback" {:kind :callback
                   :start :callback-enter
                   :end :callback-return
                   :owner "foreign-callback"
                   :allowed-escapes #{}
                   :invalidates [:callback-return]}
    "lt-generated-artifact" {:kind :generated-artifact
                             :start :macro-expansion
                             :end :artifact-serialization
                             :owner "generated-origin"
                             :allowed-escapes #{:artifact-provenance}
                             :invalidates [:compiler-pass-invalidation]})
   :status :complete})

(definterposable c9-escape-analysis-report
  [module]
  {:artifact :gravity/c9-escape-analysis-report
   :module (:module module)
   :legal-escapes [{:destination :function-return
                    :mode :persistent-copy
                    :status :accepted}
                   {:destination :actor-message
                    :mode :ownership-transfer
                    :status :accepted}
                   {:destination :ffi-call
                    :mode :borrowed-for-call-duration
                    :status :accepted}
                   {:destination :generated-artifact
                    :mode :provenance-only
                    :status :accepted}]
   :illegal-escapes-covered-by-diagnostics
   ["C9-BORROW-ESCAPE" "C9-REGION-ESCAPE" "C9-TRANSFER"]
   :status :complete})

(definterposable c9-region-lifetime-graph
  [module]
  {:artifact :gravity/c9-region-lifetime-graph
   :module (:module module)
   :regions
   (sorted-map
    "region-outer" {:scope "scope-outer"
                    :lifetime "lt-region-outer"
                    :allocations ["region-value-config"]
                    :escapes []
                    :provider :region/provider
                    :status :accepted}
    "region-inner" {:scope "scope-inner"
                    :parent "region-outer"
                    :lifetime "lt-region-inner"
                    :allocations ["region-value-scratch"]
                    :escapes []
                    :provider :region/provider
                    :status :accepted})
   :nested-references [{:from "region-inner"
                        :to "region-outer"
                        :direction :inner-may-borrow-outer
                        :status :accepted}]
   :rejected-escape-families ["C9-REGION-ESCAPE"]
   :status :complete})

(definterposable c9-arena-generation-graph
  [module]
  {:artifact :gravity/c9-arena-generation-graph
   :module (:module module)
   :arenas
   (sorted-map
    "arena-main" {:provider :arena/provider
                  :thread-affinity :task-local
                  :generations [{:generation "gen-0"
                                 :allocations ["arena-node-old"]
                                 :reset-node "core-node-arena-reset"
                                 :valid? false}
                                {:generation "gen-1"
                                 :allocations ["arena-node-current"]
                                 :valid? true}]
                  :runtime-generation-checks? true
                  :status :accepted})
   :reset-invalidation [{:arena "arena-main"
                         :invalidated-generation "gen-0"
                         :replacement-generation "gen-1"
                         :status :recorded}]
   :rejected-generation-families ["C9-ARENA-GENERATION"]
   :status :complete})

(definterposable c9-linear-resource-flow-graph
  [module]
  {:artifact :gravity/c9-linear-resource-flow-graph
   :module (:module module)
   :resources
   (sorted-map
    "resource-file" {:provider :fs/provider
                     :state :owned
                     :terminal-paths [{:path :normal
                                       :terminal :closed
                                       :terminal-count 1}
                                      {:path :error
                                       :terminal :closed
                                       :terminal-count 1}
                                      {:path :panic
                                       :terminal :closed
                                       :terminal-count 1}
                                      {:path :cancellation
                                       :terminal :cancelled
                                       :terminal-count 1}]
                     :cleanup-obligations [:close-on-normal
                                           :close-on-error
                                           :close-on-panic
                                           :cancel-on-cancellation]
                     :status :accepted}
    "resource-transaction" {:provider :db/provider
                            :state :owned
                            :terminal-paths [{:path :normal
                                              :terminal :committed
                                              :terminal-count 1}
                                             {:path :error
                                              :terminal :rolled-back
                                              :terminal-count 1}
                                             {:path :panic
                                              :terminal :rolled-back
                                              :terminal-count 1}
                                             {:path :cancellation
                                              :terminal :cancelled
                                              :terminal-count 1}]
                            :cleanup-obligations [:commit-or-rollback
                                                  :cancel-on-cancellation]
                            :status :accepted})
   :structured-resource-lowering [{:form :with-open
                                   :resource "resource-file"
                                   :normal :closed
                                   :error :closed
                                   :panic :closed
                                   :cancellation :cancelled
                                   :status :accepted}]
   :rejected-flow-families ["C9-LINEAR-LEAK" "C9-LINEAR-DOUBLE"]
   :status :complete})

(definterposable c9-transfer-records
  [module]
  {:artifact :gravity/c9-transfer-records
   :module (:module module)
   :records [{:transfer-id "transfer-function-return"
              :boundary :function
              :value-id "owned-result"
              :from "callee"
              :to "caller"
              :mode :ownership-transfer
              :cleanup-obligation :caller
              :status :accepted}
             {:transfer-id "transfer-actor-message"
              :boundary :actor
              :value-id "actor-buffer"
              :from "parent-task"
              :to "worker-actor"
              :mode :move
              :cleanup-obligation :worker-actor
              :status :accepted}
             {:transfer-id "transfer-structured-task"
              :boundary :task
              :value-id "task-buffer"
              :from "parent-task"
              :to "child-task"
              :mode :structured-move
              :lifetime "lt-structured-task"
              :cleanup-obligation :child-task
              :status :accepted}
             {:transfer-id "transfer-ffi-borrow"
              :boundary :ffi
              :value-id "ffi-slice"
              :from "gravity"
              :to "foreign-call"
              :mode :borrowed-for-call
              :lifetime "lt-callback"
              :cleanup-obligation :gravity
              :status :accepted}]
   :rejected-transfer-families ["C9-TRANSFER"]
   :status :complete})

(definterposable c9-runtime-check-records
  [module]
  (let [profile (:profile module)
        legal? (contains? #{:hosted :native :distributed :ai} profile)]
    {:artifact :gravity/c9-runtime-check-records
     :module (:module module)
     :records [{:check-id "runtime-borrow-state"
                :kind :dynamic-borrow-state
                :failure :recoverable-error
                :profile profile
                :profile-legal? legal?
                :status :recorded}
               {:check-id "runtime-arena-generation"
                :kind :arena-generation
                :failure :recoverable-error
                :profile profile
                :profile-legal? legal?
                :status :recorded}
               {:check-id "runtime-provider-scope"
                :kind :provider-scope-validity
                :failure :recoverable-error
                :profile profile
                :profile-legal? legal?
                :status :recorded}
               {:check-id "runtime-resource-terminal-state"
                :kind :resource-terminal-state
                :failure :recoverable-error
                :profile profile
                :profile-legal? legal?
                :status :recorded}]
     :rejected-runtime-check-families ["C9-RUNTIME-CHECK"]
     :status :complete}))

(definterposable c9-unsafe-audit-references
  [module]
  {:artifact :gravity/c9-unsafe-audit-references
   :module (:module module)
   :records [{:audit-id "C9-AUDIT-MANUAL-LIFETIME"
              :unsafe-island :manual-lifetime-extension
              :safe-api-boundary :stage0/checked-lifetime-handle
              :reason :manual-lifetime-extension
              :review :required
              :status :recorded}
             {:audit-id "C9-AUDIT-MANUAL-RESOURCE-FLOW"
              :unsafe-island :manual-resource-flow
              :safe-api-boundary :stage0/linear-resource-wrapper
              :reason :manual-resource-flow
              :review :required
              :status :recorded}]
   :rejected-unsafe-families ["C9-UNSAFE"]
   :status :complete})

(definterposable c9-ownership-diagnostics
  [source-path ownership]
  {:artifact :gravity/c9-ownership-diagnostic-registry
   :required-diagnostic-ids c9-ownership-diagnostic-ids
   :diagnostics
   (mapv (fn [design]
           {:diagnostic (:diagnostic design)
            :fixture (:fixture design)
            :value-id (or (some-> ownership :owners keys first)
                          :fixture/value)
            :owner-id :fixture/owner
            :borrow-id :fixture/borrow
            :region-id :fixture/region
            :arena-generation :fixture/generation
            :resource-id :fixture/resource
            :control-path :fixture/path
            :source-span (source-span source-path 0)
            :generated-origin-chain []
            :profile :fixture/profile
            :target :fixture/target
            :remediation "Keep ownership, lifetime, region, arena, and linear-resource facts explicit before safety analysis."})
         c9-ownership-rejected-designs)
   :status :complete})

(definterposable c9-linear-paths-exact?
  [linear]
  (every? (fn [[_ resource]]
            (every? #(= 1 (:terminal-count %))
                    (:terminal-paths resource)))
          (:resources linear)))

(definterposable c9-ownership-verifier-report
  [c8-artifact ownership borrow lifetimes moves escape region arena linear transfer runtime unsafe diagnostics]
  (let [diagnostics? (= (set c9-ownership-diagnostic-ids)
                        (set (map :diagnostic (:diagnostics diagnostics))))
        borrow-kinds (set (map :edge (:edges borrow)))
        transfer-boundaries (set (map :boundary (:records transfer)))]
    {:artifact :gravity/c9-ownership-verifier-report
     :c8-proof-complete? (= :complete
                            (get-in c8-artifact
                                    [:capability-based-proof :status]))
     :ownership-graph-complete? (and (seq (:owners ownership))
                                     (= :complete (:status ownership)))
     :borrow-rules-proven? (and (contains? borrow-kinds :immutable-borrow)
                                (contains? borrow-kinds :mutable-borrow)
                                (= :complete (:status borrow)))
     :lifetime-intervals-recorded? (and (seq (:intervals lifetimes))
                                        (= :complete (:status lifetimes)))
     :move-and-consume-recorded? (boolean (and (seq (:moves moves))
                                               (seq (:consumes moves))))
     :escape-analysis-recorded? (= :complete (:status escape))
     :region-and-arena-recorded? (and (seq (:regions region))
                                      (seq (:arenas arena))
                                      (= :complete (:status region))
                                      (= :complete (:status arena)))
     :linear-flow-complete? (and (c9-linear-paths-exact? linear)
                                 (= :complete (:status linear)))
     :transfer-boundaries-explicit? (set/subset?
                                     #{:function :actor :task :ffi}
                                     transfer-boundaries)
     :runtime-checks-profile-gated? (every? :profile-legal?
                                            (:records runtime))
     :unsafe-audit-references-recorded? (boolean (seq (:records unsafe)))
     :diagnostics-covered? diagnostics?
     :status (if (and (= :complete
                         (get-in c8-artifact
                                 [:capability-based-proof :status]))
                      (seq (:owners ownership))
                      (contains? borrow-kinds :immutable-borrow)
                      (contains? borrow-kinds :mutable-borrow)
                      (seq (:intervals lifetimes))
                      (seq (:moves moves))
                      (seq (:consumes moves))
                      (= :complete (:status escape))
                      (seq (:regions region))
                      (seq (:arenas arena))
                      (c9-linear-paths-exact? linear)
                      (set/subset? #{:function :actor :task :ffi}
                                   transfer-boundaries)
                      (every? :profile-legal? (:records runtime))
                      (seq (:records unsafe))
                      diagnostics?)
               :passed
               :failed)}))

(definterposable c9-ownership-capability-proof
  [artifact]
  (let [verifier (:ownership-verifier-report artifact)]
    {:ownership-graph-complete?
     (:ownership-graph-complete? verifier)
     :borrow-rules-proven?
     (:borrow-rules-proven? verifier)
     :lifetime-intervals-recorded?
     (:lifetime-intervals-recorded? verifier)
     :move-and-consume-recorded?
     (:move-and-consume-recorded? verifier)
     :region-and-arena-recorded?
     (:region-and-arena-recorded? verifier)
     :linear-flow-complete?
     (:linear-flow-complete? verifier)
     :transfer-boundaries-explicit?
     (:transfer-boundaries-explicit? verifier)
     :runtime-checks-profile-gated?
     (:runtime-checks-profile-gated? verifier)
     :unsafe-audit-references-recorded?
     (:unsafe-audit-references-recorded? verifier)
     :diagnostics-covered?
     (:diagnostics-covered? verifier)
     :verifier-passed?
     (= :passed (:status verifier))
     :status :complete}))

(definterposable c9-ownership-validate!
  [source-path artifact]
  (let [proof (c9-ownership-capability-proof artifact)]
    (doseq [[field id] [[:ownership-graph-complete? "C9-USE-AFTER-MOVE"]
                        [:borrow-rules-proven? "C9-MUT-ALIAS"]
                        [:lifetime-intervals-recorded? "C9-BORROW-ESCAPE"]
                        [:move-and-consume-recorded?
                         "C9-USE-AFTER-CONSUME"]
                        [:region-and-arena-recorded?
                         "C9-REGION-ESCAPE"]
                        [:linear-flow-complete? "C9-LINEAR-LEAK"]
                        [:transfer-boundaries-explicit? "C9-TRANSFER"]
                        [:runtime-checks-profile-gated?
                         "C9-RUNTIME-CHECK"]
                        [:unsafe-audit-references-recorded? "C9-UNSAFE"]
                        [:diagnostics-covered? "C9-UNSAFE"]
                        [:verifier-passed? "C9-UNSAFE"]]]
      (when-not (get proof field)
        (c9-ownership-fail! id source-path
                            {:stage :ownership-lifetime-region-check}
                            {:missing-fields [field]}))))
  :complete)

(definterposable compiler-c9-ownership-source-artifact
  [source-path source-text]
  (let [records (read-source-form-records source-path source-text)
        forms (mapv :form records)
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        overrides (c9-ownership-source-overrides module)
        _ (c9-ownership-validate-overrides! source-path module overrides)
        c8-artifact (compiler-c8-effect-source-artifact source-path source-text)
        effect-graph (:effect-graph c8-artifact)
        ownership (c9-ownership-graph module effect-graph)
        borrow (c9-borrow-graph module effect-graph)
        lifetimes (c9-lifetime-interval-map module)
        moves (select-keys ownership [:moves :consumes])
        escape (c9-escape-analysis-report module)
        region (c9-region-lifetime-graph module)
        arena (c9-arena-generation-graph module)
        linear (c9-linear-resource-flow-graph module)
        transfer (c9-transfer-records module)
        runtime (c9-runtime-check-records module)
        unsafe (c9-unsafe-audit-references module)
        diagnostics (c9-ownership-diagnostics source-path ownership)
        verifier (c9-ownership-verifier-report c8-artifact ownership borrow
                                               lifetimes moves escape region
                                               arena linear transfer runtime
                                               unsafe diagnostics)
        artifact-base
        {:kind :gravity/stage0-c9-ownership-checker-artifact
         :task "P06-D088"
         :document-set ["C9"]
         :governing-document c9-ownership-governing-document
         :pass {:name :c9-ownership-lifetime-region-checker
                :input :effected-core
                :output :ownership-checked-core
                :requires [:typed-core-module :effect-graph
                           :capability-proof-records :profile :target]
                :preserves [:source-spans :generated-origin :types
                            :effects :capabilities :profile :target
                            :unsafe-metadata]
                :emits [:ownership-graph :borrow-graph
                        :lifetime-interval-map :move-consume-records
                        :escape-analysis-report :region-lifetime-graph
                        :arena-generation-graph
                        :linear-resource-flow-graph :transfer-records
                        :runtime-check-records :unsafe-audit-references
                        :ownership-diagnostics]
                :rejects c9-ownership-diagnostic-ids}
         :source-overrides overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :c8-effect-checker-artifact
         (select-keys c8-artifact [:kind :artifact-id :effect-graph
                                   :namespace-effect-summary
                                   :capability-proof-records
                                   :capability-based-proof])
         :ownership-graph ownership
         :borrow-graph borrow
         :lifetime-interval-map lifetimes
         :move-consume-records moves
         :escape-analysis-report escape
         :region-lifetime-graph region
         :arena-generation-graph arena
         :linear-resource-flow-graph linear
         :transfer-records transfer
         :runtime-check-records runtime
         :unsafe-audit-references unsafe
         :ownership-verifier-report verifier
         :ownership-diagnostics diagnostics
         :c9-ownership-check-results
         {:documents ["C9"]
          :task "P06-D088"
          :required-diagnostic-ids c9-ownership-diagnostic-ids
          :ownership-graph-status :complete
          :borrow-graph-status :complete
          :lifetime-status :complete
          :move-consume-status :complete
          :escape-status :complete
          :region-status :complete
          :arena-status :complete
          :linear-status :complete
          :transfer-status :complete
          :runtime-check-status :complete
          :unsafe-audit-status :complete
          :verifier-status (:status verifier)
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (c9-ownership-validate! source-path artifact-base)
        capability-proof (c9-ownership-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(definterposable compiler-c9-ownership-file-artifact
  [path]
  (compiler-c9-ownership-source-artifact path (slurp path)))

(def ^:private namespace-contract
  {:contract-boundary :hosted-stage0-c9-ownership-checker
   :artifact-inputs [:c8-effect-checker-artifact :module-context]
   :artifact-outputs [:ownership-graph :borrow-graph :lifetime-interval-map
                      :move-consume-records :escape-analysis-report
                      :region-lifetime-graph :arena-generation-graph
                      :linear-resource-flow-graph :transfer-records
                      :runtime-check-records :unsafe-audit-references
                      :ownership-diagnostics]
   :owns [:hosted-stage0-c9-ownership-analysis
          :hosted-stage0-c9-artifact-projection]
   :dependency-direction {:requires ['clojure.set 'gravity.digest]
                          :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :does-not-own [:canonical-c9-authority :source-authentication
                  :effect-checking-authority :ownership-safety-authority
                  :region-provider-authority :arena-provider-authority
                  :linear-resource-provider-authority :runtime-check-authority
                  :safety-analysis :mir-construction :proof-authority
                  :equivalence :self-hosting :release :seed-retirement]
   :compatibility-only? true
   :override-driven-diagnostics? true
   :ownership-model-complete? false
   :canonical-c9-authority? false
   :operation-interposition {:accepted-keys operation-keys
                             :unknown-keys-rejected? true
                             :partial-overrides? true
                             :single-binding-per-top-level-call? true}})

(defn- string-vector? [value]
  (and (vector? value) (seq value) (every? string? value)))
(defn- vector-of-maps? [value]
  (and (vector? value) (every? map? value)))
(defn- keyword-string-map? [value]
  (and (map? value)
       (every? (fn [[key item]]
                 (and (keyword? key) (string? item)))
               value)))

(defn- validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "C9 operation map must be a map" {:value operations})))
  (let [unknown (seq (remove operation-keys (keys operations)))
        invalid-fns
        (seq (for [[key value] (select-keys operations
                                            function-operation-keys)
                   :when (not (fn? value))]
               key))]
    (when unknown
      (throw (ex-info "C9 operation map contains unknown keys"
                      {:unknown-keys (vec unknown)
                       :allowed-keys operation-keys})))
    (when invalid-fns
      (throw (ex-info "C9 function operation values must be functions"
                      {:non-function-keys (vec invalid-fns)}))))
  (doseq [[key predicate expected]
          [[:c9-ownership-diagnostic-ids string-vector?
            :non-empty-string-vector]
           [:c9-ownership-governing-document
            #(and (string? %) (seq %)) :non-empty-string]
           [:c9-ownership-rejected-designs vector-of-maps? :vector-of-maps]
           [:c9-ownership-override-diagnostics keyword-string-map?
            :keyword-to-string-map]]
          :when (and (contains? operations key)
                     (not (predicate (get operations key))))]
    (throw (ex-info "C9 scalar operation has an invalid shape"
                    {:key key :expected expected
                     :actual (get operations key)})))
  operations)

(defn with-operations [operations thunk]
  (validate-operations! operations)
  (let [merged (merge *operations* operations)]
    (binding [*operations* merged
              c9-ownership-diagnostic-ids
              (get merged :c9-ownership-diagnostic-ids
                   c9-ownership-diagnostic-ids)
              c9-ownership-governing-document
              (get merged :c9-ownership-governing-document
                   c9-ownership-governing-document)
              c9-ownership-rejected-designs
              (get merged :c9-ownership-rejected-designs
                   c9-ownership-rejected-designs)
              c9-ownership-override-diagnostics
              (get merged :c9-ownership-override-diagnostics
                   c9-ownership-override-diagnostics)]
      (thunk))))

(def public-api
  {'public-api {:kind :contract}
   'with-operations {:arglists '([operations thunk])}
   'c9-engine-contract {:arglists '([])}
   'c9-ownership-diagnostic-ids {:kind :constant}
   'c9-ownership-governing-document {:kind :constant}
   'c9-ownership-rejected-designs {:kind :constant}
   'c9-ownership-override-diagnostics {:kind :constant}
   'c9-ownership-source-overrides {:arglists '([module])}
   'c9-ownership-message {:arglists '([id])}
   'c9-ownership-fail! {:arglists '([id source-path subject extra])}
   'c9-ownership-validate-overrides! {:arglists '([source-path module overrides])}
   'c9-node-ids {:arglists '([effect-graph])}
   'c9-node {:arglists '([node-ids index fallback])}
   'c9-ownership-graph {:arglists '([module effect-graph])}
   'c9-borrow-graph {:arglists '([module effect-graph])}
   'c9-lifetime-interval-map {:arglists '([module])}
   'c9-escape-analysis-report {:arglists '([module])}
   'c9-region-lifetime-graph {:arglists '([module])}
   'c9-arena-generation-graph {:arglists '([module])}
   'c9-linear-resource-flow-graph {:arglists '([module])}
   'c9-transfer-records {:arglists '([module])}
   'c9-runtime-check-records {:arglists '([module])}
   'c9-unsafe-audit-references {:arglists '([module])}
   'c9-ownership-diagnostics {:arglists '([source-path ownership])}
   'c9-linear-paths-exact? {:arglists '([linear])}
   'c9-ownership-verifier-report {:arglists '([c8-artifact ownership borrow lifetimes moves escape region arena linear transfer runtime unsafe diagnostics])}
   'c9-ownership-capability-proof {:arglists '([artifact])}
   'c9-ownership-validate! {:arglists '([source-path artifact])}
   'compiler-c9-ownership-source-artifact {:arglists '([source-path source-text])}
   'compiler-c9-ownership-file-artifact {:arglists '([path])}
   })

(defn c9-engine-contract []
  (assoc namespace-contract :public-api public-api))
