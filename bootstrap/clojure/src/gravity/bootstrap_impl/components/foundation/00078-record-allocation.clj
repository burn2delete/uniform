

(defn record-allocation!
  [checker ctx node operator spec effects capabilities]
  (when (seq (set/intersection #{:memory/allocate :memory/free} effects))
    (let [record {:node-id (:node-id node)
                  :operator operator
                  :effects effects
                  :capabilities capabilities
                  :profile (:profile @ctx)
                  :allocation-kind (:allocation-kind spec)
                  :regime (:memory-regime spec)
                  :visibility :effect-recorded}]
      (record-checker! checker :allocation-effect-records record)
      (when (contains? capabilities :memory/allocator)
        (record-checker! checker :allocator-runtime-manifests
                         {:node-id (:node-id node)
                          :allocator-provider (provider-name :memory/allocator)
                          :provider-version (provider-version :memory/allocator)
                          :profile (:profile @ctx)
                          :target (:target @ctx)
                          :regime (:memory-regime spec)
                          :allocation-kind (:allocation-kind spec)
                          :runtime-assumption (:runtime-assumption spec)
                          :status :declared}))
      record)))

(defn record-memory-call!
  [checker ctx node operator args effects capabilities return-type spec]
  (when-let [regime (:memory-regime spec)]
    (record-checker! checker :memory-facts
                     {:node-id (:node-id node)
                      :operator operator
                      :regime regime
                      :family (memory-regime->family regime)
                      :type return-type
                      :effects effects
                      :capabilities capabilities
                      :profile (:profile @ctx)
                      :target (:target @ctx)
                      :safety-outcome (cond
                                        (contains? #{'unsafe/raw-read 'unsafe/mmio-read} operator)
                                        :unsafe-island

                                        (= 'bounds/check operator)
                                        :runtime-checked

                                        :else
                                        :proven-safe)
                      :memory-space (:memory-space spec)}))
  (record-allocation! checker ctx node operator spec effects capabilities)
  (case operator
    buffer/new
    (record-checker! checker :ownership-borrow-facts
                     {:node-id (:node-id node)
                      :fact :owner-created
                      :owner (:node-id node)
                      :type return-type
                      :status :owned})

    ownership/move
    (let [name (symbol-arg-name args 0)]
      (when name
        (swap! ctx assoc-in [:moved-values name]
               {:moved-at (:node-id node)
                :source-span (:source-span node)}))
      (record-checker! checker :ownership-borrow-facts
                       {:node-id (:node-id node)
                        :fact :ownership-move
                        :name name
                        :source-node (:node-id (first args))
                        :status :moved}))

    borrow/read
    (record-checker! checker :ownership-borrow-facts
                     {:node-id (:node-id node)
                      :fact :borrow
                      :mode :immutable
                      :owner-node (:node-id (first args))
                      :lifetime :lexical
                      :status :valid})

    borrow/write
    (record-checker! checker :ownership-borrow-facts
                     {:node-id (:node-id node)
                      :fact :borrow
                      :mode :mutable
                      :owner-node (:node-id (first args))
                      :lifetime :lexical
                      :excludes-other-borrows true
                      :status :valid})

    region/alloc
    (record-checker! checker :lifetime-region-facts
                     {:node-id (:node-id node)
                      :region (dispatch-arg-value args 0)
                      :allocation :region/alloc
                      :escape :rejected-unless-copied-or-moved
                      :lifetime :region-bound})

    arena/alloc
    (record-checker! checker :lifetime-region-facts
                     {:node-id (:node-id node)
                      :arena (dispatch-arg-value args 0)
                      :allocation :arena/alloc
                      :release :bulk
                      :thread-behavior :provider-declared
                      :alignment :provider-declared})

    memory/init
    (record-checker! checker :initialization-facts
                     {:node-id (:node-id node)
                      :state :init
                      :from :constructor
                      :status :proven-initialized})

    unsafe/raw-read
    (record-checker! checker :unsafe-raw-memory-audit-records
                     {:node-id (:node-id node)
                      :effect :memory/raw
                      :capabilities capabilities
                      :safety-outcome :unsafe-island
                      :preconditions [:alignment-checked :bounds-wrapper]
                      :postconditions [:no-safe-alias-created]
                      :safe-boundary :memory.safe/raw-read
                      :evidence [:stage0-fixture]
                      :owner "stage0-bootstrap"
                      :review "L10-RAW-STAGE0"})

    unsafe/mmio-read
    (do
      (record-checker! checker :unsafe-raw-memory-audit-records
                       {:node-id (:node-id node)
                        :effect :memory/mmio
                        :capabilities capabilities
                        :safety-outcome :unsafe-island
                        :preconditions [:aligned-u32 :volatile-region]
                        :postconditions [:u32-value :no-safe-alias-created]
                        :safe-boundary :mmio.safe/read-u32
                        :evidence [:stage0-fixture]
                        :owner "stage0-bootstrap"
                        :review "L10-MMIO-STAGE0"})
      (record-checker! checker :mmio-capability-records
                       {:node-id (:node-id node)
                        :capability :hardware/mmio
                        :profile (:profile @ctx)
                        :memory-space :mmio
                        :volatile-ordering :preserved
                        :alignment :u32
                        :status :audited}))

    bounds/check
    (record-checker! checker :runtime-check-records
                     {:node-id (:node-id node)
                      :kind :bounds
                      :status :runtime-checked
                      :proof :absent
                      :residual-check :retained})

    nil))

(def l11-family->conformance
  {:structured-task-scope :task-scope
   :structured-task :structured-task
   :async-await :async-await
   :atomic :atomic
   :lock :lock
   :channel :channel
   :actor :actor
   :ownership-transfer :ownership-transfer
   :immutable-sharing :immutable-sharing
   :durable-workflow :workflow-replay
   :hardware-state :hardware-state
   :gpu-kernel :gpu-kernel})