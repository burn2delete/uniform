

(defn record-concurrency-call!
  [checker ctx node operator args effects capabilities spec]
  (when-let [family (:concurrency-family spec)]
    (let [record {:node-id (:node-id node)
                  :operator operator
                  :family family
                  :conformance-family (l11-family->conformance family)
                  :effects effects
                  :capabilities capabilities
                  :profile (:profile @ctx)
                  :target (:target @ctx)}]
      (record-checker! checker :concurrency-facts record)
      (when (seq effects)
        (record-checker! checker :concurrency-effect-records
                         (assoc record :effect-order :source-order)))
      (when (contains? capabilities :scheduler/task)
        (record-checker! checker :scheduler-runtime-manifests
                         {:node-id (:node-id node)
                          :operator operator
                          :scheduler-provider (provider-name :scheduler/task)
                          :provider-version (provider-version :scheduler/task)
                          :profile (:profile @ctx)
                          :target (:target @ctx)
                          :runtime-assumption :structured-scheduler
                          :status :declared}))
      (case operator
        task/scope
        (record-checker! checker :task-scope-graphs
                         {:scope-node (:node-id node)
                          :task-nodes (mapv :node-id args)
                          :scope-exit :join-or-cancel
                          :structured? true})

        task/spawn
        (record-checker! checker :task-scope-graphs
                         {:scope-node (:task-scope-id @ctx)
                          :task-node (:node-id node)
                          :task-body (mapv :node-id args)
                          :structured? (boolean (:in-task-scope? @ctx))})

        task/transfer
        (record-checker! checker :concurrency-ownership-transfer-records
                         {:node-id (:node-id node)
                          :source-node (:node-id (first args))
                          :transfer :move-into-concurrency-boundary
                          :parent-status :consumed
                          :status :explicit})

        atomic/cell
        (record-checker! checker :atomic-ordering-records
                         {:node-id (:node-id node)
                          :operation :atomic-cell
                          :ordering (:atomic-order spec)
                          :status :declared})

        atomic/load-ordered
        (do
          (record-checker! checker :atomic-ordering-records
                           {:node-id (:node-id node)
                            :operation :atomic-load
                            :ordering (:atomic-order spec)
                            :status :declared})
          (record-checker! checker :synchronization-facts
                           {:node-id (:node-id node)
                            :primitive :atomic
                            :race-safety :proven-safe
                            :ordering (:atomic-order spec)}))

        lock/with
        (record-checker! checker :synchronization-facts
                         {:node-id (:node-id node)
                          :primitive :lock
                          :race-safety :runtime-checked
                          :scope :lexical})

        channel/send
        (record-checker! checker :actor-channel-schemas
                         {:node-id (:node-id node)
                          :kind :channel
                          :message-schema :stage0-message
                          :ownership-transfer :move-or-copy
                          :status :typed})

        actor/send
        (record-checker! checker :actor-channel-schemas
                         {:node-id (:node-id node)
                          :kind :actor
                          :message-schema :stage0-message
                          :state-ownership :actor-owned
                          :status :typed})

        shared/immutable
        (record-checker! checker :race-analysis-reports
                         {:node-id (:node-id node)
                          :subject :immutable-sharing
                          :result :proven-safe
                          :reason :immutable-value})

        workflow/parallel
        (record-checker! checker :workflow-replay-records
                         {:node-id (:node-id node)
                          :workflow-step (dispatch-arg-value args 0)
                          :branches (mapv :value args)
                          :replay-id :stage0-concurrency-replay
                          :ordering :recorded
                          :status :recorded})

        hardware/state
        (record-checker! checker :synchronization-facts
                         {:node-id (:node-id node)
                          :primitive :hardware-state
                          :clock (dispatch-arg-value args 0)
                          :domain-ir-anchor :hardware-state-machine
                          :status :represented})

        gpu/kernel
        (do
          (record-checker! checker :synchronization-facts
                           {:node-id (:node-id node)
                            :primitive :gpu-barrier
                            :barrier :declared
                            :memory-space :gpu/shared
                            :race-safety :proven-safe})
          (record-checker! checker :race-analysis-reports
                           {:node-id (:node-id node)
                            :subject :gpu-kernel
                            :result :proven-safe
                            :barrier :declared}))

        nil)
      record)))

(def l12-nondeterministic-build-effects
  #{:build/time :build/random :build/network :build/model-call :build/tool-call})

(def l12-stable-constant-types
  #{"Nil" "Boolean" "Integer" "BigInt" "F64" "ExactRatio" "String"
    "Keyword" "Symbol" "Vector" "Map" "Set" "QuotedData"})

(defn l12-build-effects
  [effects]
  (set (filter build-effect? effects)))

(defn l12-digest
  [value]
  (str "sha256:" (sha256-hex (pr-str value))))

(defn l12-build-provider
  [effect]
  (symbol "gravity.build" (name effect)))

(defn l12-stable-constant-representation?
  [type-name]
  (or (contains? l12-stable-constant-types type-name)
      (and (string? type-name)
           (or (str/starts-with? type-name "Comptime[")
               (str/starts-with? type-name "ArtifactRef[")
               (str/starts-with? type-name "Schema[")
               (= type-name "GeneratedFormArtifact")))))

(defn check-compile-time-policy!
  [ctx node args effects spec]
  (let [build-effects (l12-build-effects effects)
        first-input (:value (first args))]
    (when (and (:pure-compile-time? spec) (seq build-effects))
      (typed-diagnostic! "L12-PURE-EFFECT"
                         "pure compile-time evaluation attempts a build effect"
                         node
                         "Move effectful compile-time work to an authorized build provider."
                         {:requested-effects build-effects
                          :phase :compile-time
                          :profile (:profile @ctx)
                          :target (:target @ctx)}))
    (when (and (:hermetic? @ctx)
               (:declared-input-required? spec)
               (not (contains? (:declared-inputs @ctx) first-input)))
      (typed-diagnostic! "L12-HERMETIC-INPUT"
                         "hermetic compile-time evaluation observes an undeclared input"
                         node
                         "Declare the input in namespace metadata with a content digest or remove the ambient read."
                         {:input first-input
                          :declared-inputs (:declared-inputs @ctx)
                          :phase :compile-time
                          :profile (:profile @ctx)
                          :target (:target @ctx)}))
    (when (and (:hermetic? @ctx)
               (:target-manifest-required? spec)
               (not (contains? (:target-manifests @ctx) first-input)))
      (typed-diagnostic! "L12-HERMETIC-INPUT"
                         "hermetic target probing must use a declared target manifest"
                         node
                         "Declare the target manifest used by compile-time specialization."
                         {:target-probe first-input
                          :target-manifests (:target-manifests @ctx)
                          :phase :compile-time
                          :profile (:profile @ctx)
                          :target (:target @ctx)}))
    (when (and (:requires-replay-policy? spec)
               (seq (set/intersection l12-nondeterministic-build-effects
                                      build-effects))
               (not (set/subset? (set/intersection l12-nondeterministic-build-effects
                                                   build-effects)
                                 (:replay-policy @ctx))))
      (typed-diagnostic! "L12-NONDETERMINISM"
                         "compile-time nondeterminism lacks replay policy"
                         node
                         "Record deterministic seeds, pinned provider responses, or replay records for nondeterministic build effects."
                         {:requested-effects build-effects
                          :replay-policy (:replay-policy @ctx)
                          :phase :compile-time
                          :profile (:profile @ctx)
                          :target (:target @ctx)}))
    (when (and (:requires-strict-cache-policy? spec)
               (not= :strict (:cache-policy @ctx)))
      (typed-diagnostic! "L12-CACHE-UNSAFE"
                         "compile-time cache reuse policy is not strict enough for this result"
                         node
                         "Use a strict cache policy keyed by source, grants, target manifests, replay records, and compiler version."
                         {:cache-policy (:cache-policy @ctx)
                          :phase :compile-time
                          :profile (:profile @ctx)
                          :target (:target @ctx)}))))