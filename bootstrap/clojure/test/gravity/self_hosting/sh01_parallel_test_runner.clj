(ns gravity.self-hosting.sh01-parallel-test-runner
  "Process-isolated execution for SH-01 impact plans.

  The planner remains the source of selection truth.  This namespace only
  schedules and executes the planner's already selected namespaces.  Regular
  and memory-heavy work have independent bounded pools; exclusive work starts
  only after every regular job has drained and is run one job at a time.

  The process boundary deliberately uses ProcessBuilder argument vectors.  A
  worker function and a process launcher can be injected by tests, so scheduler
  behaviour can be checked without starting a JVM for every fixture."
  (:gen-class)
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [gravity.self-hosting.sh01-development-loop-wiring :as wiring]
            [gravity.self-hosting.sh01-host-resource-broker :as broker]
            [gravity.self-hosting.sh01-impact-test-planner :as planner]
            [gravity.self-hosting-test-runner :as child-runner]))

(import '(java.io ByteArrayOutputStream InputStream)
        '(java.nio ByteBuffer CharBuffer)
        '(java.nio.charset CodingErrorAction StandardCharsets)
        '(java.util.concurrent ExecutorCompletionService Executors
          ExecutorService LinkedBlockingQueue TimeUnit))

(def ^:private default-normal-parallelism 2)
(def ^:private default-normal-batch-size 8)
(def ^:private maximum-normal-batch-size 8)
(def ^:private default-memory-parallelism 1)
(def ^:private default-command ["clojure" "-M:test"])
(def ^:private default-capture-limit-bytes (* 1024 1024))
(def ^:private default-capture-limit-chars (* 1024 1024))
(def ^:private default-capture-wait-ms 5000)
(def ^:private batch-report-limit-bytes (* 256 1024))
(def ^:private process-cleanup-grace-ms 100)
(def ^:private process-cleanup-timeout-ms 1500)
(def ^:private process-post-cleanup-wait-ms 100)
(def ^:private capture-post-close-wait-ms 100)

(defn- fatal-throwable?
  "Errors which must never be converted into a worker result.

  A scheduler can report ordinary test failures, but swallowing VM/linkage
  failures (or ThreadDeath) would leave the process in an unknown state and
  make the report falsely authoritative."
  [throwable]
  (or (instance? ThreadDeath throwable)
      (instance? VirtualMachineError throwable)
      (instance? LinkageError throwable)))

(defn- fatal-cause
  [throwable]
  (loop [candidate throwable]
    (cond
      (nil? candidate) nil
      (fatal-throwable? candidate) candidate
      :else (recur (.getCause ^Throwable candidate)))))

(defn- rethrow-fatal!
  [throwable]
  (when-let [fatal (fatal-cause throwable)]
    (throw fatal))
  throwable)

(defn- restore-interrupt!
  [throwable]
  (loop [candidate throwable]
    (when candidate
      (if (instance? InterruptedException candidate)
        (.interrupt (Thread/currentThread))
        (recur (.getCause ^Throwable candidate))))))

(defn- interrupted-throwable?
  [throwable]
  (loop [candidate throwable]
    (cond
      (nil? candidate) false
      (instance? InterruptedException candidate) true
      :else (recur (.getCause ^Throwable candidate)))))

(defn- throwable-message
  [throwable]
  (or (.getMessage ^Throwable throwable)
      (str throwable)))

(def ^:private authority-paths
  "Known plan metadata locations.  Missing metadata is intentionally treated
  as non-authoritative; selection and scheduling must not manufacture proof
  authority merely because a plan has the expected schema."
  [[:authoritative?]
   [:authoritative]
   [:plan-authoritative?]
   [:plan-authoritative]
   [:metadata :authoritative?]
   [:metadata :authoritative]
   [:metadata :authority]
   [:metadata :authority-status]
   [:metadata :plan-authoritative?]
   [:metadata :plan-authoritative]
   [:plan-metadata :authoritative?]
   [:plan-metadata :authoritative]
   [:plan-metadata :authority]
   [:provenance :authoritative?]
   [:provenance :authoritative]
   [:provenance :authority]
   [:authority :authoritative?]
   [:authority :status]
   [:authority]])

(defn- repository-root
  []
  (let [resource
        (or (io/resource "gravity/self_hosting/sh01_parallel_test_runner.clj")
            (io/resource "gravity/self_hosting/sh01_impact_test_planner.clj"))]
    (when-not resource
      (throw
       (ex-info
        "SH-01 parallel runner source is not on the classpath"
        {:id "SH01-PARALLEL-SOURCE"})))
    (loop [path (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? path)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH01-PARALLEL-ROOT"}))

        (and (.isFile (.toFile (.resolve path "deps.edn")))
             (.isFile
              (.toFile
               (.resolve path "docs/self-hosting-slice-ownership.edn"))))
        (.toFile path)

        :else
        (recur (.getParent path))))))

(def ^:private root (delay (repository-root)))

(defn- namespace-name
  [namespace]
  (str namespace))

(defn- namespace-key
  [namespace]
  (namespace-name namespace))

(defn- authority-value
  [value]
  (cond
    (true? value) true
    (false? value) false
    (keyword? value)
    (case value
      :authoritative true
      :non-authoritative false
      :not-authoritative false
      nil)
    (string? value)
    (case (str/lower-case value)
      "authoritative" true
      "non-authoritative" false
      "not-authoritative" false
      "true" true
      "false" false
      nil)
    :else nil))

(defn plan-authority
  "Returns explicit authority metadata for a plan.

  A plan with no recognized authority marker is reported as
  `:non-authoritative`.  This is intentionally independent of the planner's
  schema: planning is selection data, while authority is an evidence claim."
  [plan]
  (let [entries
        (->> authority-paths
             (keep
              (fn [path]
                (let [parent (get-in plan (butlast path) ::missing)
                      present? (and (map? parent)
                                    (contains? parent (last path)))
                      raw (when present? (get-in plan path))]
                  ;; A map at :authority is a container for a nested status,
                  ;; not itself an authority marker.
                  (when (and present? (not (map? raw)))
                    {:source path
                     :value raw
                     :authoritative? (authority-value raw)}))))
             vec)
        decisions (set (keep :authoritative? entries))
        conflict? (< 1 (count decisions))
        invalid? (boolean (some #(nil? (:authoritative? %)) entries))
        authoritative?
        (and (seq entries)
             (not conflict?)
             (not invalid?)
             (= #{true} decisions))
        single? (= 1 (count entries))]
    {:status (if authoritative? :authoritative :non-authoritative)
     :authoritative? (boolean authoritative?)
     :metadata-present? (boolean (seq entries))
     :source (cond
               (empty? entries) :missing-metadata
               single? (:source (first entries))
               :else (mapv :source entries))
     :value (cond
              (empty? entries) nil
              single? (:value (first entries))
              :else (mapv #(select-keys % [:source :value]) entries))
     :conflict? conflict?
     :invalid? invalid?
     :reason (cond
               conflict? :conflicting-authority-markers
               invalid? :invalid-authority-marker
               (empty? entries) :missing-authority-metadata
               authoritative? :explicit-authority
               :else :explicit-non-authority)}))

(defn- resource-class
  [job]
  (let [value (:resource-class job)]
    (if (nil? value) :normal value)))

(defn- require-supported-namespace!
  [namespace]
  ;; Policy comes from the owner, never from caller-supplied shard markers.
  (when-let [blocked (planner/unsupported-execution namespace)]
    (throw
     (ex-info "Selected source tests require their established guarded owner wrapper"
              blocked))))

(defn- canonical-jobs
  "Canonicalizes either the planner's `:namespaces` plus `:shards` or a plan
  containing shards alone.  Namespace identity is unique and reporting order
  is lexical, making completion timing irrelevant to the output order."
  [plan]
  (when-not (map? plan)
    (throw
     (ex-info "SH-01 parallel execution requires a plan map"
              {:id "SH01-PARALLEL-PLAN" :plan plan})))
  (let [raw-shards (vec (or (:shards plan) []))
        malformed-shards
        (->> raw-shards
             (filter #(or (not (map? %))
                          (nil? (:namespace %))))
             vec)
        raw-namespaces
        (vec
         (if (contains? plan :namespaces)
           (or (:namespaces plan) [])
           (keep :namespace raw-shards)))
        malformed-namespaces (vec (filter nil? raw-namespaces))
        duplicate-namespace-keys
        (fn [values]
          (->> values
               (map namespace-key)
               frequencies
               (keep (fn [[key count]] (when (< 1 count) key)))
               sort
               vec))
        shard-duplicates
        (duplicate-namespace-keys (keep :namespace raw-shards))
        namespace-duplicates (duplicate-namespace-keys raw-namespaces)
        _
        (when (seq malformed-shards)
          (throw
           (ex-info "SH-01 plan shard is missing a namespace"
                    {:id "SH01-PARALLEL-NAMESPACE"
                     :shards malformed-shards})))
        _
        (when (seq malformed-namespaces)
          (throw
           (ex-info "SH-01 plan contains an empty namespace"
                    {:id "SH01-PARALLEL-NAMESPACE"
                     :namespaces raw-namespaces})))
        _
        (when (or (seq shard-duplicates) (seq namespace-duplicates))
          (throw
           (ex-info "SH-01 plan contains duplicate namespaces"
                    {:id "SH01-PARALLEL-NAMESPACE-DUPLICATE"
                     :shard-duplicates shard-duplicates
                     :namespace-duplicates namespace-duplicates})))
        shards
        (reduce
         (fn [acc shard]
           (assoc acc (namespace-key (:namespace shard)) shard))
         {}
         raw-shards)
        namespaced
        (reduce
         (fn [acc namespace]
           (let [key (namespace-key namespace)]
             (assoc acc key (merge (get shards key) {:namespace namespace}))))
         shards
         raw-namespaces)
        jobs
        (->> namespaced
             vals
             (map (fn [job]
                    ;; Recompute the owning policy rather than trusting optional
                    ;; shard fields that a direct caller could omit or relabel.
                    (require-supported-namespace! (:namespace job))
                    (when-not
                     (contains? #{:normal :memory-heavy :exclusive}
                                (resource-class job))
                      (throw
                       (ex-info
                        "SH-01 plan shard has an unknown resource class"
                        {:id "SH01-PARALLEL-RESOURCE"
                         :namespace (:namespace job)
                         :resource-class (:resource-class job)})))
                    (assoc job :resource-class (resource-class job))))
             (sort-by #(namespace-name (:namespace %)))
             vec)]
    (when (empty? jobs)
      (throw
       (ex-info "SH-01 parallel execution refuses an empty plan"
                {:id "SH01-PARALLEL-EMPTY-PLAN"
                 :plan-schema (:schema plan)})))
    jobs))

(defn- positive-integer
  [value option]
  (let [number
        (cond
          (integer? value) value
          (string? value) (try (parse-long value)
                               (catch NumberFormatException _ nil))
          :else nil)]
    (when-not (and (integer? number)
                   (pos? number)
                   (<= number (long Integer/MAX_VALUE)))
      (throw
       (ex-info
        (str option " must be a positive integer no greater than Integer/MAX_VALUE")
        {:id "SH01-PARALLEL-OPTION"
         :option option
         :value value
         :maximum Integer/MAX_VALUE})))
    (long number)))

(defn- execution-options
  [options]
  (let [options (or options {})
        normal
        (or (:normal-parallelism options)
            (:normal-jobs options)
            (:parallelism options)
            default-normal-parallelism)
        normal-batch-size
        (or (:normal-batch-size options) default-normal-batch-size)
        memory
        (or (:memory-parallelism options)
            (:memory-heavy-parallelism options)
            (:memory-jobs options)
            default-memory-parallelism)]
    (let [normal (positive-integer normal :normal-parallelism)
          normal-batch-size
          (positive-integer normal-batch-size :normal-batch-size)
          memory (positive-integer memory :memory-parallelism)]
      (when (> normal-batch-size maximum-normal-batch-size)
        (throw
         (ex-info "Normal SH-01 batch size exceeds the reviewed bound"
                  {:id "SH01-PARALLEL-BATCH-LIMIT"
                   :normal-batch-size normal-batch-size
                   :maximum maximum-normal-batch-size})))
      (when-not (= 1 memory)
        (throw
         (ex-info
          "Memory-heavy SH-01 tests require concurrency exactly one"
          {:id "SH01-PARALLEL-MEMORY-LIMIT"
           :memory-parallelism memory
           :required 1})))
      (assoc options
             :normal-parallelism normal
             :normal-batch-size normal-batch-size
             :memory-parallelism memory
             ;; Both spellings are accepted by the programmatic API.  The
             ;; question-mark spelling is used by the CLI, matching the
             ;; existing SH-07 runner, while :fail-fast keeps embedding code
             ;; concise.
             :fail-fast? (boolean (or (:fail-fast? options)
                                      (:fail-fast options)))
             :fail-fast (boolean (or (:fail-fast? options)
                                     (:fail-fast options)))))))

(defn- compatibility-bootstrap-affinity
  [job]
  ;; Both suites require the real Bootstrap facade, restore their interposed
  ;; Vars, and keep separate namespace results. This shares initialization,
  ;; never an upstream artifact or verifier report. A narrowed closure cannot
  ;; be carried across component ownership by this affinity.
  (when (and (contains? #{"gravity.bootstrap-compatibility.c11-test"
                         "gravity.self-hosting.sh01-compiler-pass-manifest-compatibility-test"}
                       (namespace-name (:namespace job)))
             (not (contains? job :cache-closure))
             (not (:cache-closure-authorized? job))
             (or (nil? (:test-policy job))
                 (and (map? (:test-policy job))
                      (= #{:deterministic? :performance? :proof? :freshness-required?}
                         (set (keys (:test-policy job))))
                      (every? boolean? (vals (:test-policy job))))))
    [::compatibility-bootstrap (:test-policy job)]))

(defn- normal-batches
  "Packs normal jobs into bounded, deterministic cache-affine batches.

  A reviewed JVM group takes precedence over slice affinity. Jobs are ordered
  by that affinity and namespace before bounded partitioning; no memory-heavy
  or exclusive job can enter this path."
  [jobs batch-size]
  (->> jobs
       (group-by #(or (compatibility-bootstrap-affinity %)
                      [::existing-affinity (or (:batch-key %) (:slice %))]))
       (sort-by (comp str key))
       (mapcat (fn [[_ affinity-jobs]]
                 (partition-all
                  batch-size
                  (sort-by #(namespace-name (:namespace %)) affinity-jobs))))
       (mapv
        (fn [batch]
          (let [batch (vec batch)
                shared-bootstrap? (and (> (count batch) 1)
                                       (compatibility-bootstrap-affinity (first batch)))
                component-id (when (or (not shared-bootstrap?)
                                       (apply = (map :component-id batch)))
                               (:component-id (first batch)))
                batch-key (if shared-bootstrap?
                            "bootstrap-facade/compatibility"
                            (:batch-key (first batch)))
                test-policies (mapv :test-policy batch)
                common-test-policy
                (when (and (every? map? test-policies)
                           (apply = test-policies))
                  (first test-policies))]
            (cond->
             {:namespace (:namespace (first batch))
              :slice (:slice (first batch))
              :resource-class :normal
              :batch-jobs batch
              :batch-namespaces (mapv :namespace batch)}
              component-id
              (assoc :component-id component-id)
              batch-key
              (assoc :batch-key batch-key)
              common-test-policy
              (assoc :test-policy common-test-policy)))))))

(defn schedule-plan
  "Returns the deterministic execution schedule for an impact plan.

  The parallel phase submits all normal and memory-heavy jobs to independent
  bounded executors.  Executor queues refill immediately as slots become free;
  there is no fixed-wave barrier.  After both queues drain, the exclusive phase
  runs its jobs sequentially with capacity one."
  ([plan]
   (schedule-plan plan {}))
  ([plan options]
   (let [{:keys [normal-parallelism normal-batch-size memory-parallelism]
          :as options}
         (execution-options options)
         jobs (canonical-jobs plan)
         grouped (group-by resource-class jobs)
         normal (vec (get grouped :normal []))
         normal-batches (normal-batches normal normal-batch-size)
         memory (vec (get grouped :memory-heavy []))
         exclusive (vec (get grouped :exclusive []))
         parallel-phase
         {:phase :parallel
          :capacities {:normal normal-parallelism
                       :memory-heavy memory-parallelism}
          :normal normal
          :normal-batches normal-batches
          :memory-heavy memory
          :exclusive []
          :jobs (vec (concat normal memory))}
         exclusive-phase
         {:phase :exclusive
          :capacity 1
          :normal []
          :memory-heavy []
          :exclusive exclusive
          :jobs exclusive}
         phases
         (cond-> []
           (seq (:jobs parallel-phase)) (conj parallel-phase)
           (seq (:jobs exclusive-phase)) (conj exclusive-phase))]
     {:schema :gravity/sh01-parallel-test-schedule-v1
      :plan-schema (:schema plan)
      :plan-authority (plan-authority plan)
      :normal-parallelism normal-parallelism
      :normal-batch-size normal-batch-size
      :memory-parallelism memory-parallelism
      :fail-fast? (:fail-fast? options)
      :fail-fast (:fail-fast? options)
      :planned-jvms (+ (count normal-batches)
                       (count memory)
                       (count exclusive))
      :jobs jobs
      :parallel-phase parallel-phase
      :exclusive-phase exclusive-phase
      :phases phases})))

(def schedule schedule-plan)

(defn dry-run
  "Builds a schedule without requiring or running any test namespace."
  ([plan]
   (dry-run plan {}))
  ([plan options]
   (assoc (schedule-plan plan options) :dry-run? true)))

(defn- elapsed-ms
  [started]
  (long (/ (- (System/nanoTime) started) 1000000.0)))

(defn- cleanup-failure?
  [result]
  (or (true? (:cleanup-failed? result))
      (false? (:cleanup-complete? result))
      (= :failed (:cleanup-status result))
      (false? (get-in result [:cleanup :complete?]))))

(defn- capture-failure?
  "Whether stream capture failed independently of the child exit status.

  `:closed` is the expected result after a timeout closes descriptors held by
  an inherited child.  It is therefore not a failure unless the capture
  thread also recorded an error."
  [result]
  (or (true? (:capture-failed? result))
      (contains? #{:error :capture-timeout :interrupted}
                 (:stdout-capture-status result))
      (contains? #{:error :capture-timeout :interrupted}
                 (:stderr-capture-status result))
      (some? (:stdout-capture-error result))
      (some? (:stderr-capture-error result))
      (true? (:stdout-capture-forced-close? result))
      (true? (:stderr-capture-forced-close? result))))

(defn- result-failure?
  [result]
  (or (contains? #{:failed :error :timeout} (:status result))
      (and (number? (:exit-code result))
           (not (zero? (:exit-code result))))
      (and (number? (:fail result))
           (pos? (long (:fail result))))
      (and (number? (:error result))
           (pos? (long (:error result))))
      (false? (:ok? result))
      (cleanup-failure? result)
      (capture-failure? result)))

(defn- stop-after-result?
  "Returns whether a completion must stop queued submission.

  Ordinary failures stop only an explicitly requested fail-fast run.  A
  ProcessHandle cleanup failure is different: the runner cannot prove that a
  timed-out child tree is gone, so it always stops queued and exclusive work,
  even when complete collection was requested."
  [result fail-fast?]
  (or (and fail-fast? (result-failure? result))
      (cleanup-failure? result)
      ;; A reader failure means the child result is incomplete even when the
      ;; process itself exited zero.  Stop all queued/exclusive work so the
      ;; report cannot be mistaken for a complete collection.
      (capture-failure? result)))

(defn- normalize-result
  [job result elapsed]
  (let [result (if (map? result) result {:value result})
        nested (if (map? (:result result)) (:result result) {})
        result (merge nested result)
        exit-code
        (if (contains? result :exit-code)
          (long (or (:exit-code result) 1))
          (if (result-failure? (assoc result :exit-code 0)) 1 0))
        status
        (or (:status result)
            (if (zero? exit-code) :passed :failed))]
    (merge
     {:namespace (:namespace job)
      :slice (:slice job)
      :resource-class (:resource-class job)
      :status status
      :exit-code exit-code
      :stdout ""
      :stderr ""
      :elapsed-ms elapsed}
     result
     ;; The job identity is owned by the plan, not by an injectable worker.
     {:namespace (:namespace job)
      :slice (:slice job)
      :resource-class (:resource-class job)
      :elapsed-ms (long (or (:elapsed-ms result) elapsed))})))

(defn- invoke-worker
  [worker job]
  (let [started (System/nanoTime)]
    (try
      (normalize-result job (worker job) (elapsed-ms started))
      (catch Throwable throwable
        (rethrow-fatal! throwable)
        (restore-interrupt! throwable)
        (let [error-data (ex-data throwable)
              broker-receipts
              (or (:broker-receipts error-data)
                  (when-let [receipt (:receipt error-data)] [receipt]))]
          (normalize-result
           job
           (cond->
            {:status :error
             :exit-code 1
             :stderr (throwable-message throwable)
             :child-jvm-launched?
             (boolean (:child-jvm-launched? error-data))
             :diagnostic-id (:id error-data)
             :interrupted? (interrupted-throwable? throwable)
             :interrupt-restored? (.isInterrupted (Thread/currentThread))
             :exception-class (class throwable)}
             (seq broker-receipts)
             (assoc :broker-receipts (vec broker-receipts)))
           (elapsed-ms started)))))))

(defn- await-future
  [future]
  (try
    @future
    (catch Throwable throwable
      (rethrow-fatal! throwable)
      (restore-interrupt! throwable)
      {:status :error
       :exit-code 1
       :stderr (throwable-message throwable)
       :interrupted? (interrupted-throwable? throwable)
       :exception-class (class throwable)})))

(declare run-namespace-process run-normal-batch-process
         process-result bound-launch-result)

(defn- expand-batch-result
  [batch-job result]
  (let [jobs (:batch-jobs batch-job)
        members (into {} (map (juxt :namespace identity)
                              (:namespace-results result)))
        common (dissoc result :namespace :slice :resource-class :result
                       :batch-job
                       :namespace-results :namespaces :skipped-namespaces
                       :summary :test :pass :fail :error :status :exit-code
                       :stdout :stderr :elapsed-ms)]
    (mapv
     (fn [job]
       (if-let [member (get members (:namespace job))]
         (let [stdout (:stdout member)
               stderr (:stderr member)]
           (normalize-result
            job
            (merge common member
                   {:stdout (if (map? stdout) (:text stdout) stdout)
                    :stderr (if (map? stderr) (:text stderr) stderr)
                    :stdout-truncated? (boolean (and (map? stdout)
                                                     (:truncated? stdout)))
                    :stderr-truncated? (boolean (and (map? stderr)
                                                     (:truncated? stderr)))
                    :batch-namespaces (:batch-namespaces batch-job)})
            (:elapsed-ms member)))
         (when (empty? members)
           (normalize-result
            job
            (assoc result :batch-namespaces (:batch-namespaces batch-job))
            (:elapsed-ms result)))))
     jobs)))

(defn- expanded-results
  [results]
  (->> results
       vals
       (mapcat (fn [result]
                 (if-let [batch-job (:batch-job result)]
                   (expand-batch-result batch-job result)
                   [result])))
       (remove nil?)
       (into {} (map (juxt (comp namespace-key :namespace) identity)))))

(defn- lane-inflight-count
  [state lane]
  (count
   (filter #(= lane (:lane (val %)))
           (:inflight state))))

(defn- submit-one!
  "Submits exactly one job to a lane.

  Callers must enforce the lane capacity.  Keeping this operation separate
  from queue filling makes the bounded in-flight invariant auditable and
  prevents an ExecutorService's unbounded work queue from becoming an
  accidental second scheduler."
  [state {:keys [lane service]} worker job]
  (let [future
        (.submit ^ExecutorCompletionService service
                 ^java.util.concurrent.Callable
                 (fn [] (invoke-worker worker job)))]
    (swap! state
           (fn [state]
             (-> state
                 (update-in [:pending-index lane] inc)
                 (assoc-in [:inflight future]
                           {:lane lane
                            :job job})))))
  state)

(defn- fill-lane!
  "Fills a lane only up to its executor capacity.

  This is deliberately called only after completion events have been
  observed.  In fail-fast mode a stop marker prevents any further submission
  in either independent pool."
  [state lane-config worker fail-fast?]
  (loop []
    (let [{:keys [stop? pending pending-index] :as snapshot} @state
          lane (:lane lane-config)
          jobs (get pending lane)
          next-index (long (get pending-index lane 0))
          next-job (when (< next-index (count jobs))
                     (nth jobs next-index))
          active (lane-inflight-count snapshot lane)]
      (when (and next-job
                 (< active (:capacity lane-config))
                 (not (and fail-fast? stop?)))
        (submit-one! state lane-config worker next-job)
        (recur)))))

(defn- completed-events
  "Takes one completion and drains already-ready completions.

  Draining the ready queue before refilling a lane means a failure that has
  already completed in an independent pool wins over a successful completion
  that happens to be observed first; no queued job is launched after the
  failure is known."
  [completion-queue]
  (try
    (loop [events [(.take ^java.util.concurrent.BlockingQueue completion-queue)]]
      (if-let [event (.poll ^java.util.concurrent.BlockingQueue completion-queue)]
        (recur (conj events event))
        events))
    (catch Throwable throwable
      (rethrow-fatal! throwable)
      (restore-interrupt! throwable)
      (throw throwable))))

(defn- record-completions!
  [state futures fail-fast? completion-observer]
  (reduce
   (fn [state future]
     (if-let [{:keys [job lane]} (get-in @state [:inflight future])]
       (let [result (normalize-result job (await-future future) 0)]
         (swap! state
                (fn [state]
                  (cond-> (-> state
                               (update :inflight dissoc future)
                               (assoc-in [:results (namespace-key
                                                   (:namespace job))]
                                         result))
                    (stop-after-result? result fail-fast?)
                    (assoc :stop? true
                           :failure result))))
         ;; Tests can inject a completion-observed barrier.  This callback is
         ;; invoked only after the result is in the scheduler state and before
         ;; any refill, making fail-fast ordering an explicit observation
         ;; rather than a timing race.
         (when completion-observer
           (completion-observer job result))
         state)
       state))
   state
   futures))

(defn- run-parallel-lanes!
  "Runs normal and memory-heavy lanes with bounded, refill-on-completion
  submission.  The returned state contains only jobs that actually started;
  jobs left in the plan are reported as skipped by execute-plan."
  [worker lane-configs jobs-by-lane fail-fast? completion-observer]
  (let [completion-queue (LinkedBlockingQueue.)
        state (atom {:pending jobs-by-lane
                     :pending-index (zipmap (keys jobs-by-lane)
                                            (repeat 0))
                     :inflight {}
                     :results {}
                     :stop? false
                     :failure nil})
        lane-configs
        (mapv
         (fn [{:keys [executor] :as lane-config}]
           (assoc lane-config
                  :service
                  (ExecutorCompletionService. ^ExecutorService executor
                                               completion-queue)))
         lane-configs)]
    (doseq [lane-config lane-configs]
      (fill-lane! state lane-config worker fail-fast?))
    (loop []
      (if (empty? (:inflight @state))
        @state
        (do
          (record-completions!
           state
           (completed-events completion-queue)
           fail-fast?
           completion-observer)
          (when-not (:stop? @state)
            (doseq [lane-config lane-configs]
              (fill-lane! state lane-config worker fail-fast?)))
          (recur))))))

(defn- prepare-development-jobs
  [jobs probe-outcomes]
  (reduce
   (fn [{:keys [pending hits]} [job {:keys [hit-result]}]]
     (let [key (namespace-key (:namespace job))]
       (if hit-result
         {:pending pending
          :hits (assoc hits key
                       (cond-> (normalize-result job hit-result 0)
                         (:batch-jobs job) (assoc :batch-job job)))}
         {:pending (conj pending job) :hits hits})))
   {:pending [] :hits {}}
   (map vector jobs probe-outcomes)))

(defn- cache-report-count
  [receipts predicate]
  (count (filter predicate receipts)))

(defn execute-plan
  "Executes a plan according to `schedule-plan` and returns a report.

  Options include `:normal-parallelism`, `:normal-batch-size`,
  `:memory-parallelism`, `:timeout-ms`,
  `:working-directory`, `:command`, `:process-launcher`, and an injectable
  one-argument `:worker`. `:development-loop?` enables parent-side immutable
  cache probes, cross-process singleflight on misses, and host-wide broker
  admission at the child-process boundary. The default worker launches bounded batches of
  normal namespaces in warm Clojure processes. Memory-heavy and exclusive
  jobs still launch one fresh process per namespace. Supplying `:worker`
  preserves the one-job injectable unit-test API; `:normal-batch-worker` can
  explicitly test batching. `:fail-fast?` (or `:fail-fast`) keeps only one
  capacity-sized submission set in flight and stops refilling both independent
  pools after the first failed, timed-out, or errored result.  Already-running
  jobs drain safely; skipped jobs are reported in deterministic plan order.
  Without fail-fast every selected job still runs to completion."
  ([plan]
   (execute-plan plan {}))
  ([plan options]
   (let [options (execution-options options)
         working-directory (or (:working-directory options) @root)
         development-context
         (when (:development-loop? options)
           (if-let [supplied-context (:development-loop-context options)]
             (wiring/validate-context! supplied-context
                                       working-directory options)
             (wiring/prepare-context working-directory options)))
         options
         (cond-> options
           development-context
           (assoc :timeout-ms (:timeout-ms development-context)
                  :process-launch-observer
                  #(wiring/record-launch! development-context)))
         schedule (schedule-plan plan options)
         development-options
         (let [authority (:plan-authority schedule)]
           (assoc options :authoritative?
                  (boolean (or (:authoritative? options)
                               (:authoritative? authority)
                               (:conflict? authority)
                               (:invalid? authority)))))
         options
         (cond-> (dissoc options :child-development-cache)
           (and development-context
                (not-any? development-options
                          [:authoritative? :fresh? :freshness-required?
                           :performance? :proof? :nondeterministic?
                           :command :clojure-command :process-launcher
                           :worker :normal-batch-worker])
                (not-any?
                 (fn [job]
                   (or (false? (get-in job [:test-policy :deterministic?]))
                       (some #(or (get job %) (get-in job [:test-policy %]))
                             [:fresh? :freshness-required? :performance?
                              :proof? :nondeterministic?])))
                 (:shards plan)))
           (assoc :child-development-cache
                  {:cache-directory (str (:cache-directory development-context))
                   :timeout-ms (:timeout-ms development-context)
                   :repository-identity
                   (get-in development-context [:snapshot :repository-identity])}))
         batch? (or (nil? (:worker options))
                    (some? (:normal-batch-worker options)))
         normal-jobs
         (vec (if batch?
                (get-in schedule [:parallel-phase :normal-batches])
                (get-in schedule [:parallel-phase :normal])))
         memory-jobs (vec (get-in schedule [:parallel-phase :memory-heavy]))
         exclusive-jobs (vec (get-in schedule [:exclusive-phase :exclusive]))
         attach-jobs
         (fn [jobs]
           (if development-context
             (mapv #(wiring/attach-request development-context plan
                                            development-options %)
                   jobs)
             jobs))
         attached-normal (attach-jobs normal-jobs)
         attached-memory (attach-jobs memory-jobs)
         attached-exclusive (attach-jobs exclusive-jobs)
         attached-jobs
         (vec (concat attached-normal attached-memory attached-exclusive))
         probe-outcomes
         (if development-context
           (wiring/probe-batch development-context attached-jobs)
           (mapv (fn [job] {:job job :hit-result nil}) attached-jobs))
         [normal-probes remaining-probes]
         (split-at (count attached-normal) probe-outcomes)
         [memory-probes exclusive-probes]
         (split-at (count attached-memory) remaining-probes)
         prepared-normal
         (prepare-development-jobs attached-normal normal-probes)
         prepared-memory
         (prepare-development-jobs attached-memory memory-probes)
         prepared-exclusive
         (prepare-development-jobs attached-exclusive exclusive-probes)
         cached-units
         (merge (:hits prepared-normal)
                (:hits prepared-memory)
                (:hits prepared-exclusive))
         post-cache-planned-jvms
         (+ (count (:pending prepared-normal))
            (count (:pending prepared-memory))
            (count (:pending prepared-exclusive)))
         namespace-worker (or (:worker options)
                              #(run-namespace-process % options))
         batch-worker (or (:normal-batch-worker options)
                          #(run-normal-batch-process % options))
         worker (fn [job]
                  (let [operation
                        #(if (and (= :normal (:resource-class job))
                                  (:batch-jobs job))
                           (batch-worker (:batch-jobs job))
                           (namespace-worker job))
                        result
                        (if development-context
                          (wiring/run-unit! development-context job operation)
                          (operation))]
                    (cond-> result
                      (:batch-jobs job) (assoc :batch-job job))))
         fail-fast? (:fail-fast? options)
         normal-executor
         (Executors/newFixedThreadPool
          (int (:normal-parallelism schedule)))
         memory-executor
         (Executors/newFixedThreadPool
          (int (:memory-parallelism schedule)))]
     (try
       (let [parallel-lanes
             [{:lane :normal
               :capacity (:normal-parallelism schedule)
               :executor normal-executor}
              {:lane :memory-heavy
               :capacity (:memory-parallelism schedule)
               :executor memory-executor}]
             parallel-state
             (run-parallel-lanes!
              worker
              parallel-lanes
              {:normal (vec (:pending prepared-normal))
               :memory-heavy
               (vec (:pending prepared-memory))}
              fail-fast?
              (:completion-observer options))
             ;; Exclusive work is intentionally started only after both
             ;; independent pools have drained.  It is sequential even when
             ;; fail-fast is disabled, and the same stop marker prevents any
             ;; exclusive job from starting after a parallel failure.
             results-by-key
             (atom (merge (expanded-results cached-units)
                          (expanded-results (:results parallel-state))))
             stop? (atom (:stop? parallel-state))
             failure (atom (:failure parallel-state))
             exclusive-jobs
             (:pending prepared-exclusive)]
         (when-not @stop?
           (doseq [job exclusive-jobs]
             (when-not @stop?
               (let [result (invoke-worker worker job)
                     key (namespace-key (:namespace job))]
                 (swap! results-by-key assoc key result)
                 (when (stop-after-result? result fail-fast?)
                   (reset! stop? true)
                   (reset! failure result))))))
         (let [results
               (->> (:jobs schedule)
                    (keep #(get @results-by-key
                                (namespace-key (:namespace %))))
                    vec)
               skipped-jobs
               (->> (:jobs schedule)
                    (remove #(contains? @results-by-key
                                        (namespace-key (:namespace %))))
                    vec)
               skipped-namespaces (mapv :namespace skipped-jobs)
               failures (vec (filter result-failure? results))
               cleanup-failures
               (vec (filter cleanup-failure? results))
               capture-failures
               (vec (filter capture-failure? results))
               incomplete? (seq skipped-jobs)
               development-facts
               (when development-context
                 (wiring/report-facts development-context))
               cache-receipts (:cache-receipts development-facts)
               broker-receipts
               (->> results
                    (mapcat #(or (:broker-receipts %) []))
                    distinct
                    (sort-by pr-str)
                    vec)
               producer-broker-receipts
               (->> results
                    (mapcat #(or (:producer-broker-receipts %) []))
                    distinct
                    (sort-by pr-str)
                    vec)]
           {:schema :gravity/sh01-parallel-test-report-v1
            :plan-schema (:plan-schema schedule)
            :plan-authority (:plan-authority schedule)
            ;; Execution results are evidence from a bounded development
            ;; runner, never an authority promotion.  Preserve the planner's
            ;; metadata separately above.
            :authority :non-authoritative
            :authoritative? false
            :normal-parallelism (:normal-parallelism schedule)
            :normal-batch-size (:normal-batch-size schedule)
            :normal-batching? (boolean batch?)
            :memory-parallelism (:memory-parallelism schedule)
            :fail-fast? (boolean fail-fast?)
            :fail-fast (boolean fail-fast?)
            :fail-fast-triggered? (boolean (and fail-fast?
                                                (seq skipped-jobs)))
            :fail-fast-failure (or (when fail-fast? (first failures))
                                   @failure
                                   (:failure parallel-state))
            :cleanup-failed? (boolean (seq cleanup-failures))
            :cleanup-failures cleanup-failures
            :capture-failed? (boolean (seq capture-failures))
            :capture-failures capture-failures
            :jobs (count results)
            :planned-jobs (count (:jobs schedule))
            :planned-jvms (:planned-jvms schedule)
            :post-cache-planned-jvms
            (if development-context
              post-cache-planned-jvms
              (:planned-jvms schedule))
            :launched-jvms
            (if development-context
              (:launched-jvms development-facts)
              nil)
            :revision (:revision development-facts)
            :cache-hit-jvms
            (if development-context
              (cache-report-count
               cache-receipts
               #(= :hit (get-in % [:receipt :decision])))
              0)
            :cache-producer-jvms
            (if development-context
              (cache-report-count
               cache-receipts
               #(and (= :execution (:phase %))
                     (true? (get-in % [:receipt :producer-executed?]))
                     (true? (get-in % [:receipt :cacheable?]))))
              0)
            :snapshot-telemetry
            (when development-context
              (:snapshot-telemetry development-facts))
            :cache-receipts (vec (or cache-receipts []))
            :broker-receipts broker-receipts
            :producer-broker-receipts producer-broker-receipts
            :results results
            :reports results
            :failures failures
            :skipped-jobs skipped-jobs
            :skipped-namespaces skipped-namespaces
            ;; Keep a compact alias useful to callers that only need names.
            :skipped skipped-namespaces
            :complete? (not incomplete?)
            :status (if (seq failures) :failed :passed)
            :ok? (and (empty? failures) (not incomplete?))
            :exit-code (if (or (seq failures) incomplete?) 1 0)}))
       (finally
         (.shutdown ^ExecutorService normal-executor)
         (.shutdown ^ExecutorService memory-executor)
         (.awaitTermination ^ExecutorService normal-executor 1 TimeUnit/MINUTES)
         (.awaitTermination ^ExecutorService memory-executor 1 TimeUnit/MINUTES))))))

(def run-plan execute-plan)

(defn- command-prefix
  [options]
  (let [command (or (:command options) (:clojure-command options))]
    (cond
      (nil? command) default-command
      (vector? command) (mapv str command)
      (sequential? command) (mapv str command)
      :else [(str command) "-M:test"])))

(defn- forward-child-cache-option? [options]
  (when-let [cache-options (:child-development-cache options)]
    ;; Keep malformed options on the child's original refusal path. Only a
    ;; valid optional optimization may be omitted to run the tests fresh.
    (or (not (child-runner/valid-development-cache-options? cache-options))
        (wiring/project-startup-resources-absent?
         (or (:working-directory options) @root)))))

(defn namespace-command
  "Returns the exact ProcessBuilder argument vector for one namespace.
  Opt-in :exact-test-vars and :child-fail-fast? forward development-test-runner
  selection flags after the namespace, without changing the command prefix."
  ([namespace]
   (namespace-command namespace {}))
  ([namespace options]
   (when (and (contains? options :exact-test-vars)
              (not (and (vector? (:exact-test-vars options))
                         (seq (:exact-test-vars options))
                         (every? #(and (string? %) (not (str/blank? %)))
                                 (:exact-test-vars options)))))
     (throw (ex-info "Exact child selection requires a nonempty vector of names"
                     {:id "SH01-PARALLEL-CHILD-SELECTION"})))
   (when (and (contains? options :child-fail-fast?)
              (not (instance? Boolean (:child-fail-fast? options))))
     (throw (ex-info "Child fail-fast must be an explicit boolean"
                     {:id "SH01-PARALLEL-CHILD-SELECTION"})))
   (cond-> (into (vec (command-prefix options))
                 ["--namespace" (namespace-name namespace)])
     (:exact-test-vars options)
     (into (mapcat (fn [name] ["--exact" name]) (:exact-test-vars options)))
     (:child-fail-fast? options) (conj "--fail-fast")
     (:development-result-delivery options)
     (into ["--machine-result" (pr-str (:development-result-delivery options))])
     (forward-child-cache-option? options)
     (into ["--development-cache" (pr-str (:child-development-cache options))]))))

(defn normal-batch-command
  "Returns the exact argument vector for one bounded warm-JVM normal batch."
  ([jobs report-file]
   (normal-batch-command jobs report-file {}))
  ([jobs report-file options]
   (cond->
    (into (vec (command-prefix options))
          (mapcat (fn [job]
                    ["--namespace" (namespace-name (:namespace job))])
                  jobs))
     (:fail-fast? options) (conj "--fail-fast")
     (forward-child-cache-option? options)
     (into ["--development-cache" (pr-str (:child-development-cache options))])
     true (into ["--report-file" (str report-file)]))))

(defn- reported-batch-command
  [command]
  ;; The final report-file path is a parent-owned random temporary identity.
  ;; It must reach ProcessBuilder but must not enter a reusable result.
  (vec (drop-last 2 command)))

(declare retain-owned-process!)

(defn start-process
  "Starts a process from an argument vector and working directory.

  This function is public specifically so tests and embedders can inject a
  launcher without replacing ProcessBuilder or invoking a shell."
  [command working-directory]
  (let [builder (-> (ProcessBuilder. ^java.util.List (vec (map str command)))
                    (.directory (io/file working-directory))
                    (.redirectErrorStream false))
        custody (broker/retain-process-start-custody!)]
    (try
      (let [process (.start builder)]
        (when custody
          (broker/record-started-process! custody process)
          (broker/record-started-ownership! custody (retain-owned-process! process)))
        process)
      (catch Throwable original
        (try
          (broker/record-process-start-error! custody original)
          (catch Throwable custody-error
            (when-not (identical? original custody-error)
              (.addSuppressed original custody-error))))
        (when custody (restore-interrupt! original))
        (throw original)))))

(defn- report-counter?
  [value]
  (and (integer? value) (<= 0 (long value))))

(defn- valid-report-output?
  [output]
  (and (map? output)
       (string? (:text output))
       (every? report-counter?
               [(:bytes output) (:observed-bytes output)
                (:limit-bytes output)])
       (<= (long (:bytes output)) (long (:observed-bytes output)))
       (<= (long (:bytes output)) (long (:limit-bytes output)))
       (boolean? (:truncated? output))
       (= (alength (.getBytes ^String (:text output)
                              StandardCharsets/UTF_8))
          (long (:bytes output)))))

(defn- valid-report-member?
  [member]
  (and (map? member)
       (true? (:attempted? member))
       (contains? #{:passed :failed :error} (:status member))
       (every? report-counter?
               [(:exit-code member) (:elapsed-ms member)
                (:test member) (:pass member) (:fail member) (:error member)])
       (= (:exit-code member) (if (= :passed (:status member)) 0 1))
       (valid-report-output? (:stdout member))
       (valid-report-output? (:stderr member))))

(defn- valid-batch-report?
  [report jobs fail-fast?]
  (let [expected (vec (sort (map :namespace jobs)))
        attempted (mapv :namespace (:namespace-results report))
        skipped (vec (:skipped-namespaces report))
        summary (:summary report)]
    (and (map? report)
         (= :gravity/self-hosting-test-report-v2 (:schema report))
         (= :non-authoritative (:authority report))
         (false? (:authoritative? report))
         (= expected (:namespaces report))
         (seq attempted)
         (= expected (vec (concat attempted skipped)))
         (= attempted (vec (take (count attempted) expected)))
         (every? valid-report-member? (:namespace-results report))
         (boolean? (:fail-fast? report))
         (= (boolean fail-fast?) (:fail-fast? report))
         (or (:fail-fast? report) (empty? skipped))
         (every? report-counter?
                 [(:test report) (:pass report) (:fail report) (:error report)
                  (:test summary) (:pass summary)
                  (:fail summary) (:error summary)])
         (= (select-keys report [:test :pass :fail :error]) summary)
         (contains? #{:passed :failed} (:status report))
         (= (:status report)
            (if (and (empty? skipped)
                     (every? #(= :passed (:status %))
                             (:namespace-results report)))
              :passed
              :failed))
         (= (:exit-code report) (if (= :passed (:status report)) 0 1)))))

(defn- read-batch-report
  [report-file jobs fail-fast?]
  (when-not (java.nio.file.Files/isRegularFile
             report-file
             (make-array java.nio.file.LinkOption 0))
    (throw (ex-info "Warm-JVM batch did not publish result.edn"
                    {:id "SH01-PARALLEL-BATCH-REPORT-MISSING"})))
  (let [size (java.nio.file.Files/size report-file)]
    (when (> size batch-report-limit-bytes)
      (throw (ex-info "Warm-JVM batch report exceeds its bounded size"
                      {:id "SH01-PARALLEL-BATCH-REPORT-LIMIT"
                       :bytes size
                       :limit batch-report-limit-bytes})))
    (let [report
          (edn/read-string
           (String. (java.nio.file.Files/readAllBytes report-file)
                    StandardCharsets/UTF_8))]
      (when-not (valid-batch-report? report jobs fail-fast?)
        (throw (ex-info "Warm-JVM batch report failed validation"
                        {:id "SH01-PARALLEL-BATCH-REPORT-SCHEMA"})))
      report)))

(defn run-normal-batch-process
  "Runs a bounded normal batch in one warm Clojure process.

  The child publishes a separate bounded EDN receipt so verbose test output
  cannot hide per-namespace results. Memory-heavy and exclusive jobs never
  call this function."
  ([jobs]
   (run-normal-batch-process jobs {}))
  ([jobs options]
   (let [jobs (vec jobs)
         _ (doseq [job jobs] (require-supported-namespace! (:namespace job)))
         _ (when (or (empty? jobs)
                     (some #(not= :normal (:resource-class %)) jobs))
             (throw (ex-info "Warm-JVM batching accepts normal jobs only"
                             {:id "SH01-PARALLEL-BATCH-RESOURCE"})))
         working-directory (or (:working-directory options) @root)
         batch-temporary-root
         (if-let [temporary-directory (:batch-temporary-directory options)]
           (.toPath (io/file temporary-directory))
           (.resolve (.toPath (io/file working-directory))
                     "target/development-loop-batches"))
         _ (java.nio.file.Files/createDirectories
            batch-temporary-root
            (make-array java.nio.file.attribute.FileAttribute 0))
         batch-directory
         (java.nio.file.Files/createTempDirectory
          batch-temporary-root
          ".gravity-sh01-normal-batch-"
          (make-array java.nio.file.attribute.FileAttribute 0))
         report-file (.resolve batch-directory "result.edn")
         command (normal-batch-command jobs report-file options)
         launcher (or (:process-launcher options) start-process)
         cleanup-failed? (atom false)
         cleanup-errors (atom [])
         result
         (try
           (let [launched (launcher command working-directory)
                 _ (when-let [observer (:process-launch-observer options)]
                     (observer))
                 process-result
                 (if (instance? Process launched)
                   (process-result launched options)
                   (if (map? launched)
                     (bound-launch-result launched options)
                     {:status :failed
                      :exit-code 1
                      :stderr (str "Process launcher returned unsupported value: "
                                   (pr-str (type launched))) }))]
             (try
               (let [report (read-batch-report report-file jobs
                                               (:fail-fast? options))]
                 (when-not (= (zero? (long (or (:exit-code process-result) 1)))
                              (= :passed (:status report)))
                   (throw
                    (ex-info "Warm-JVM process exit disagrees with its report"
                             {:id "SH01-PARALLEL-BATCH-EXIT-MISMATCH"
                              :process-exit-code (:exit-code process-result)
                              :report-exit-code (:exit-code report)})))
                 (assoc process-result
                        :child-jvm-launched? true
                        :result report
                        :batch-report-schema (:schema report)
                        :command (reported-batch-command command)))
               (catch Throwable throwable
                 (rethrow-fatal! throwable)
                 (cond-> (assoc process-result
                                :child-jvm-launched? true
                                :batch-report-error
                                (throwable-message throwable)
                                :command (reported-batch-command command))
                   (= :passed (:status process-result))
                   (assoc :status :error :exit-code 1)))))
           (finally
             (doseq [[kind path] [[:report report-file]
                                  [:directory batch-directory]]]
               (try
                 (let [deleted?
                       (java.nio.file.Files/deleteIfExists path)]
                   (when (and (= :directory kind) (not deleted?))
                     (reset! cleanup-failed? true)
                     (swap! cleanup-errors conj
                            {:kind kind :error "temporary path was absent"})))
                 (catch Throwable throwable
                   (rethrow-fatal! throwable)
                   (restore-interrupt! throwable)
                   (reset! cleanup-failed? true)
                   (swap! cleanup-errors conj
                          {:kind kind :error (throwable-message throwable)}))))))]
     (cond-> result
       @cleanup-failed?
       (assoc :status :error
              :exit-code 1
              :cleanup-failed? true
              :cleanup-complete? false
              :batch-cleanup-errors @cleanup-errors)))))

(defn- capture-limit
  [value option]
  (let [number
        (cond
          (integer? value) value
          (string? value) (try (parse-long value)
                               (catch NumberFormatException _ nil))
          :else nil)]
    (when-not (and (integer? number)
                   (<= 0 number (long Integer/MAX_VALUE)))
      (throw
       (ex-info
        (str option " must be a non-negative integer no greater than Integer/MAX_VALUE")
        {:id "SH01-PARALLEL-CAPTURE-LIMIT"
         :option option
         :value value
         :maximum Integer/MAX_VALUE})))
    (long number)))

(defn- capture-limits
  [options stream]
  (let [options (or options {})
        stream-name (name stream)
        stream-key (fn [suffix]
                     (keyword (str stream-name "-" suffix)))
        stream-alias (fn [prefix suffix]
                       (keyword (str stream-name "-" prefix "-" suffix)))
        generic-bytes
        (or (:output-limit-bytes options)
            (:capture-limit-bytes options)
            (:output-cap-bytes options)
            (:capture-cap-bytes options)
            (:max-output-bytes options)
            (:max-capture-bytes options)
            (:output-cap options)
            (:capture-cap options)
            (:output-limit options)
            (:capture-limit options))
        generic-chars
        (or (:output-limit-chars options)
            (:capture-limit-chars options)
            (:output-cap-chars options)
            (:capture-cap-chars options)
            (:max-output-chars options)
            (:max-capture-chars options)
            (:output-cap options)
            (:capture-cap options)
            (:output-limit options)
            (:capture-limit options))
        bytes
        (capture-limit
         (or (get options (stream-key "limit-bytes"))
             (get options (stream-key "limit"))
             (get options (stream-alias "max" "bytes"))
             generic-bytes
             default-capture-limit-bytes)
         (stream-key "limit-bytes"))
        chars
        (capture-limit
         (or (get options (stream-key "limit-chars"))
             (get options (stream-key "chars"))
             (get options (stream-alias "max" "chars"))
             generic-chars
             default-capture-limit-chars)
         (stream-key "limit-chars"))]
    {:byte-limit bytes
     :char-limit chars}))

(defn- strict-utf8-text
  [^bytes bytes length]
  (try
    (let [decoder (doto (.newDecoder StandardCharsets/UTF_8)
                    (.onMalformedInput CodingErrorAction/REPORT)
                    (.onUnmappableCharacter CodingErrorAction/REPORT))]
      (str (.decode decoder (ByteBuffer/wrap bytes 0 (int length)))))
    (catch java.nio.charset.CharacterCodingException _ nil)))

(defn- truncate-unicode-text
  [text char-limit]
  (if (<= (count text) char-limit)
    text
    (let [limit (int char-limit)
          ;; A character cap is expressed in JVM chars for compatibility with
          ;; the existing EDN reports, but never return a lone high surrogate.
          safe-limit
          (if (and (pos? limit)
                   (Character/isHighSurrogate (.charAt text (dec limit))))
            (dec limit)
            limit)]
      (subs text 0 safe-limit))))

(defn- bounded-utf8-text
  "Decodes only a valid UTF-8 prefix of retained bytes.

  A byte cap can land in the middle of a multi-byte sequence.  Strict decode
  plus a short suffix search drops that incomplete sequence instead of asking
  String's replacement decoder to emit U+FFFD.  Invalid bytes in an injected
  stream fail closed to an empty retained prefix; the reader still drains all
  input before returning."
  [^bytes bytes length char-limit]
  (loop [candidate (int length)
         attempts 0]
    (if-let [decoded (strict-utf8-text bytes candidate)]
      {:text (truncate-unicode-text decoded char-limit)
       :bytes candidate}
      (if (and (pos? candidate) (< attempts 4))
        (recur (dec candidate) (inc attempts))
        {:text ""
         :bytes 0}))))

(defn- decode-observed-chunk
  "Feeds one byte chunk through a stateful UTF-8 decoder.

  `String.` on each pipe read is unsafe: a multi-byte sequence can straddle
  the 8192-byte read boundary and produce a replacement character.  Keep the
  decoder strict and carry only its incomplete suffix into the next call."
  [decoder ^bytes carry ^bytes source read end-of-input?]
  (let [carry-length (alength carry)
        combined (byte-array (+ carry-length read))]
    (when (pos? carry-length)
      (System/arraycopy carry 0 combined 0 carry-length))
    (when (pos? read)
      (System/arraycopy source 0 combined carry-length read))
    (let [input (ByteBuffer/wrap combined)
          output (CharBuffer/allocate (max 1 (+ 1 (alength combined))))]
      (loop [observed-chars 0]
        (let [coder-result (.decode decoder input output end-of-input?)]
          (.flip output)
          (let [produced (.remaining output)
                observed-chars (+ observed-chars produced)]
            (.clear output)
            (cond
              (.isError coder-result)
              (.throwException coder-result)

              (.isOverflow coder-result)
              (recur observed-chars)

              :else
              (let [remaining (.remaining input)
                    next-carry (byte-array remaining)]
                (when (pos? remaining)
                  (.get input next-carry))
                (if end-of-input?
                  (if (pos? remaining)
                    (throw (java.nio.charset.MalformedInputException.
                            (int remaining)))
                    ;; Flush is required by CharsetDecoder's contract even
                    ;; though UTF-8 has no state after a complete codepoint.
                    (loop [flushed observed-chars]
                      (let [flush-result (.flush decoder output)]
                        (.flip output)
                        (let [produced (.remaining output)
                              flushed (+ flushed produced)]
                          (.clear output)
                          (cond
                            (.isError flush-result)
                            (.throwException flush-result)

                            (.isOverflow flush-result)
                            (recur flushed)

                            :else
                            {:chars flushed
                             :carry (byte-array 0)})))))
                  {:chars observed-chars
                   :carry next-carry})))))))))

(defn- read-stream-capture
  [^InputStream input byte-limit char-limit]
  (with-open [input input
              output (ByteArrayOutputStream.)]
    (let [buffer (byte-array 8192)
          decoder (doto (.newDecoder StandardCharsets/UTF_8)
                    (.onMalformedInput CodingErrorAction/REPORT)
                    (.onUnmappableCharacter CodingErrorAction/REPORT))]
      (loop [observed-bytes 0
             observed-chars 0
             retained-bytes 0
             carry (byte-array 0)]
        (let [read (.read input buffer)]
          (if (neg? read)
            (let [final-decoded (decode-observed-chunk decoder carry
                                                       buffer 0 true)
                  observed-chars (+ observed-chars (:chars final-decoded))
                  retained-bytes-array (.toByteArray output)
                  bounded (bounded-utf8-text retained-bytes-array
                                              retained-bytes
                                              char-limit)
                  text (:text bounded)
                  safe-bytes (:bytes bounded)
                  truncated? (or (> observed-bytes byte-limit)
                                 (> observed-chars char-limit)
                                 (< safe-bytes retained-bytes))]
              {:status (if truncated? :truncated :complete)
               :text text
               :captured-bytes safe-bytes
               :captured-chars (count text)
               :observed-bytes observed-bytes
               :observed-chars observed-chars
               :byte-limit byte-limit
               :char-limit char-limit
               :truncated? truncated?})
            (let [decoded (decode-observed-chunk decoder carry buffer read false)
                  next-observed-bytes (+ observed-bytes read)
                  next-observed-chars (+ observed-chars (:chars decoded))
                  remaining (- byte-limit retained-bytes)
                  retained (min (max 0 remaining) read)]
              (when (pos? retained)
                (.write output buffer 0 (int retained)))
              (recur next-observed-bytes
                     next-observed-chars
                     (+ retained-bytes retained)
                     (:carry decoded)))))))))

(defn- capture-error-result
  [throwable closing? byte-limit char-limit]
  (if-let [fatal (fatal-cause throwable)]
    ;; A capture daemon cannot propagate an Error through the Promise
    ;; boundary by throwing before `deliver`: the caller would wait the full
    ;; capture timeout and misreport it as a pipe timeout.  Deliver the
    ;; original object as a sentinel; await-capture rethrows that identical
    ;; object on the scheduler thread.
    {:status :fatal
     :text ""
     :captured-bytes 0
     :captured-chars 0
     :observed-bytes 0
     :observed-chars 0
     :byte-limit byte-limit
     :char-limit char-limit
     :truncated? false
     :fatal-throwable fatal
     :error (throwable-message fatal)}
    (do
      (restore-interrupt! throwable)
      {:status (if @closing? :closed :error)
       :text ""
       :captured-bytes 0
       :captured-chars 0
       :observed-bytes 0
       :observed-chars 0
       :byte-limit byte-limit
       :char-limit char-limit
       :truncated? false
       :error (when-not @closing? (throwable-message throwable))
       :drain-error (when-not @closing?
                      (throwable-message throwable))
       :interrupted? (interrupted-throwable? throwable)})))

(defn- stream-capture
  "Drains one process stream while retaining only bounded output.

  The reader always consumes through EOF, even after either cap is reached, so
  a verbose child cannot block on a full pipe.  Byte accounting is performed
  on the wire; character accounting is performed on UTF-8 chunks and the
  final retained text is clipped to the requested character cap."
  [stream {:keys [byte-limit char-limit]}]
  (let [result (promise)
        closing? (atom false)
        thread
        (Thread.
         (fn []
           (deliver
            result
            (try
              (read-stream-capture stream byte-limit char-limit)
              (catch Throwable throwable
                (capture-error-result throwable closing?
                                      byte-limit char-limit))))))]
    ;; Stream drainage must not keep a completed CLI JVM alive. Clojure futures
    ;; use a shared cached executor whose idle lifetime can add a minute to a
    ;; short validation run, so use one bounded daemon thread per stream.
    (.setDaemon thread true)
    (.setName thread "sh01-process-stream-capture")
    (.start thread)
    {:promise result
     :stream stream
     :closing? closing?
     :limits {:byte-limit byte-limit :char-limit char-limit}}))

(defn- close-stream!
  [stream]
  (try
    (.close ^java.io.Closeable stream)
    true
    (catch Throwable throwable
      (rethrow-fatal! throwable)
      (restore-interrupt! throwable)
      false)))

(defn- capture-timeout-result
  [{:keys [byte-limit char-limit]}]
  {:status :capture-timeout
   :text ""
   :captured-bytes 0
   :captured-chars 0
   :observed-bytes 0
   :observed-chars 0
   :byte-limit byte-limit
   :char-limit char-limit
   :truncated? true
   :forced-close? true
   :error "stream capture did not drain before the bounded wait"})

(defn- await-capture
  [{:keys [promise stream limits closing?]} wait-ms]
  (let [timeout-marker (Object.)
        captured (deref promise (long wait-ms) timeout-marker)]
    (if (identical? captured timeout-marker)
      (do
        ;; A timed-out process may have descendants holding the inherited pipe
        ;; open. Closing our descriptors wakes the capture daemon and prevents
        ;; a report from waiting forever for EOF.
        (reset! closing? true)
        (close-stream! stream)
        (let [captured (or (deref promise capture-post-close-wait-ms nil)
                           (capture-timeout-result limits))
              captured (assoc captured :forced-close? true)]
          (if-let [fatal (:fatal-throwable captured)]
            (throw fatal)
            captured)))
      (if-let [fatal (:fatal-throwable captured)]
        (throw fatal)
        captured))))

(defn- handle-descendants
  [^java.lang.ProcessHandle root]
  (try
      (with-open [descendants (.descendants root)]
        {:handles (vec (iterator-seq (.iterator descendants)))
         :error nil})
      (catch Throwable throwable
        (restore-interrupt! throwable)
        ;; Enumeration failure is itself non-authoritative.  The caller still
        ;; terminates the root, but must not claim the descendant tree was
        ;; cleaned when it could not observe that tree.
        {:handles []
         :error throwable})))

(defrecord RetainedProcess
    [process handles discovery-error cleanup-error wait-throwable terminal owner-thread-id])

(defn- retain-process-error! [cell error]
  (restore-interrupt! error)
  (let [previous @cell
        fatal (fatal-cause error)
        primary (cond (nil? previous) (or fatal error)
                      (and fatal (not (fatal-cause previous))) fatal
                      :else previous)
        secondary (if (identical? primary previous) fatal previous)]
    (when (and secondary (not (identical? primary secondary))
               (< (count (.getSuppressed ^Throwable primary)) 8)
               (not-any? #(identical? secondary %) (.getSuppressed ^Throwable primary)))
      (.addSuppressed ^Throwable primary secondary))
    (reset! cell primary)))

(defn- require-retained-process! [owned]
  (when-not (and (instance? RetainedProcess owned)
                 (= (:owner-thread-id owned) (.getId (Thread/currentThread))))
    (throw (ex-info "Owned process handle belongs to its retaining thread"
                    {:id "SH01-PROCESS-OWNERSHIP"})))
  owned)

(defn retain-owned-process!
  "Retain the actual Process and observed handles, never look up a PID.

  Call immediately after launch on the thread that will wait/finalize. Java
  cannot prove that observation covers every descendant, even at this point."
  [process]
  (when-not (instance? Process process)
    (throw (ex-info "Retaining ownership requires an actual Process"
                    {:id "SH01-PROCESS-OWNERSHIP"})))
  (or (broker/retained-start-token process)
      (let [owned (->RetainedProcess process (atom []) (atom nil) (atom nil) (atom nil)
                                    (atom nil) (.getId (Thread/currentThread)))]
        (try
          (swap! (:handles owned) conj (.toHandle ^Process process))
          (catch Throwable error
            (retain-process-error! (:discovery-error owned) error)))
        owned)))

(defn retained-process-handles
  "Return retained raw handles, not evidence or PID-reconstructed ownership."
  [owned]
  @(:handles (require-retained-process! owned)))

(defn- observe-owned-process! [owned]
  (require-retained-process! owned)
  ;; Revisit retained descendants as well as the root: a known child can fork
  ;; after root exit. Absence from a later enumeration never drops a handle.
  (doseq [handle @(:handles owned)]
    (let [{:keys [handles error]} (handle-descendants handle)]
      (when error (retain-process-error! (:discovery-error owned) error))
      (swap! (:handles owned) #(vec (distinct (concat % handles))))))
  owned)

(defn- handle-pid
  [^java.lang.ProcessHandle handle]
  (long (.pid handle)))

(defn- alive-handle?
  [^java.lang.ProcessHandle handle owned]
  (try
    (.isAlive handle)
    (catch Throwable throwable
      (retain-process-error! (:cleanup-error owned) throwable)
      ;; If a handle can no longer be queried, retain it as a cleanup failure
      ;; rather than promoting an unverified timeout to a clean result.
      true)))

(defn- request-handle-termination!
  [^java.lang.ProcessHandle handle forcibly? owned]
  (try
    (if forcibly?
      (.destroyForcibly handle)
      (.destroy handle))
    (catch Throwable throwable
      (retain-process-error! (:cleanup-error owned) throwable)
      false)))

(defn- reap-process-handle!
  [^java.lang.ProcessHandle handle deadline owned]
  (try
    ;; ProcessHandle::onExit is the JVM-level wait/reap boundary for a
    ;; descendant that is not represented by a java.lang.Process object.
    (when (< (System/nanoTime) deadline)
      (.get (.onExit handle)
            (long (max 1 (min 100 (quot (- deadline (System/nanoTime)) 1000000))))
            TimeUnit/MILLISECONDS)
      true)
    (catch Throwable throwable
      (retain-process-error! (:cleanup-error owned) throwable)
      false)))

(defn- retained-root-alive? [owned]
  (try
    (.isAlive ^Process (:process owned))
    (catch Throwable error
      (retain-process-error! (:cleanup-error owned) error)
      true)))

(defn- terminate-retained-process! [owned]
  (let [root (first @(:handles owned))
        started (System/nanoTime)
        grace-deadline (+ started (* process-cleanup-grace-ms 1000000))
        deadline (+ started (* process-cleanup-timeout-ms 1000000))
        requested (atom #{})
        forced (atom #{})
        live-at-entry (atom nil)]
    (loop []
      (observe-owned-process! owned)
      (let [observed @(:handles owned)
            alive (vec (filter #(alive-handle? % owned) observed))
            root-alive? (retained-root-alive? owned)
            now (System/nanoTime)]
        (compare-and-set! live-at-entry nil (count (remove #(= root %) alive)))
        (doseq [handle (reverse (remove #(= root %) alive))
                :when (not (contains? @requested handle))]
          (request-handle-termination! handle false owned)
          (swap! requested conj handle))
        ;; The actual Process is the root control handle, including when its
        ;; ProcessHandle is unavailable or cannot be queried.
        (when (and root-alive? (not (contains? @requested :root)))
          (try (.destroy ^Process (:process owned))
               (catch Throwable error (retain-process-error! (:cleanup-error owned) error)))
          (swap! requested conj :root))
        (cond
          (and (empty? alive) (not root-alive?))
          (let [reaped (mapv #(reap-process-handle! % deadline owned) observed)
                reaped? (and (every? true? reaped) (nil? @(:cleanup-error owned)))]
            {:status (if reaped? :tracked-clean :failed)
             :complete? (when-not reaped? false)
             :tracked-complete? reaped?
             :descendant-coverage :unknown
             :root-alive? false
             :descendants-alive 0
             :live-descendants-at-finalization @live-at-entry
             :forced? (boolean (seq @forced))
             :observed-pids (mapv handle-pid observed)
             :survivor-pids []
             :discovery-error (some-> @(:discovery-error owned) str)
             :cleanup-error (some-> @(:cleanup-error owned) str)})

          (>= now deadline)
          {:status :failed :complete? false :tracked-complete? false
           :descendant-coverage :unknown :root-alive? root-alive?
           :descendants-alive (count (remove #(= root %) alive))
           :live-descendants-at-finalization @live-at-entry
           :forced? (boolean (seq @forced))
           :observed-pids (mapv handle-pid observed)
           :survivor-pids (mapv handle-pid alive)
           :discovery-error (some-> @(:discovery-error owned) str)
           :cleanup-error (some-> @(:cleanup-error owned) str)}

          :else
          (do
            (when (>= now grace-deadline)
              (doseq [handle (reverse (remove #(= root %) alive))
                      :when (and (not (contains? @forced handle)) (alive-handle? handle owned))]
                (request-handle-termination! handle true owned)
                (swap! forced conj handle))
              (when (and (retained-root-alive? owned) (not (contains? @forced :root)))
                (try (.destroyForcibly ^Process (:process owned))
                     (catch Throwable error (retain-process-error! (:cleanup-error owned) error)))
                (swap! forced conj :root)))
            (java.util.concurrent.locks.LockSupport/parkNanos
             (long (* (min 25 (max 1 (quot (- deadline now) 1000000))) 1000000)))
            (recur)))))))

(defn finalize-owned-process!
  "Finalize retained handles once on the retaining thread; repeat calls reuse
  the same observation. :tracked-complete? never proves descendant coverage.
  :complete? is nil (unknown) or false, never a global success claim. A caller
  whose required lifecycle scope is these retained handles can consume the
  tracked fact; source-bound creation knowledge belongs to its exact method,
  not to an all-OS registry or an inferred meaning for the unknown field."
  [owned]
  (require-retained-process! owned)
  (let [terminal (:terminal owned)]
    (if (compare-and-set! terminal nil :finalizing)
      (try
        (let [result (terminate-retained-process! owned)]
          (when-let [error @(:discovery-error owned)] (rethrow-fatal! error))
          (when-let [error @(:cleanup-error owned)] (rethrow-fatal! error))
          (reset! terminal {:result result})
          result)
        (catch Throwable error
          (reset! terminal {:error error})
          (throw error)))
      (let [{:keys [result error]} @terminal]
        (cond error (throw error)
              result result
              :else (throw (ex-info "Reentrant process finalization refused"
                                   {:id "SH01-PROCESS-FINALIZATION"})))))))

(defn- wait-for-process
  "Waits for a process with an optional bound and never hides interruption.

  The unbounded form is used only for an ordinary process whose caller did
  not request a timeout.  Cleanup paths always use the bounded form below."
  [^Process process timeout-ms owned]
  (try
    (let [deadline (when timeout-ms (+ (System/nanoTime) (* timeout-ms 1000000)))]
      (loop []
        (observe-owned-process! owned)
        (when-let [error @(:discovery-error owned)] (rethrow-fatal! error))
        (let [remaining (when deadline (max 0 (quot (- deadline (System/nanoTime)) 1000000)))
              completed? (.waitFor process (long (min 50 (or remaining 50))) TimeUnit/MILLISECONDS)]
          (observe-owned-process! owned)
          (cond completed? {:completed? true :interrupted? false}
                (and deadline (>= (System/nanoTime) deadline))
                {:completed? false :interrupted? false}
                :else (recur)))))
    (catch Throwable throwable
      (reset! (:wait-throwable owned) throwable)
      (rethrow-fatal! throwable)
      (restore-interrupt! throwable)
      {:completed? false
       :interrupted? (interrupted-throwable? throwable)
       :error (throwable-message throwable)})))

(defn- bounded-process-wait
  [^Process process timeout-ms]
  (try
    (.waitFor process (long timeout-ms) TimeUnit/MILLISECONDS)
    (catch Throwable throwable
      (rethrow-fatal! throwable)
      (restore-interrupt! throwable)
      false)))

(defn- retained-process-result
  [process options owned]
  (let [stdout-stream (.getInputStream ^Process process)
        stderr-stream (.getErrorStream ^Process process)
        stdout-capture (stream-capture stdout-stream
                                       (capture-limits options :stdout))
        stderr-capture (stream-capture stderr-stream
                                       (capture-limits options :stderr))
        {:keys [completed? interrupted? error]}
        (wait-for-process process (:timeout-ms options) owned)
        timed-out? (and (some? (:timeout-ms options)) (not completed?)
                         (not interrupted?) (nil? error))
        containment-needed? (or timed-out? interrupted? (some? error))
        cleanup (finalize-owned-process! owned)]
    (when containment-needed?
      ;; Close after descendant termination: this releases pipe readers without
      ;; allowing an inherited child descriptor to hide a live process.
      (reset! (:closing? stdout-capture) true)
      (reset! (:closing? stderr-capture) true)
      (close-stream! stdout-stream)
      (close-stream! stderr-stream)
      ;; Never call the unbounded Process.waitFor after cleanup.  The result is
      ;; incomplete if the root remains alive after this bounded wait.
      (bounded-process-wait process process-post-cleanup-wait-ms))
    (let [capture-wait-ms (long (or (:capture-wait-ms options)
                                    default-capture-wait-ms))
          stdout (await-capture stdout-capture capture-wait-ms)
          stderr (await-capture stderr-capture capture-wait-ms)
          capture-failed? (or (capture-failure? {:stdout-capture-status
                                                  (:status stdout)
                                                  :stderr-capture-status
                                                  (:status stderr)
                                                  :stdout-capture-error
                                                  (:error stdout)
                                                  :stderr-capture-error
                                                  (:error stderr)
                                                  :stdout-capture-forced-close?
                                                  (:forced-close? stdout)
                                                  :stderr-capture-forced-close?
                                                  (:forced-close? stderr)})
                              (= :capture-timeout (:status stdout))
                              (= :capture-timeout (:status stderr)))
          root-alive? (:root-alive? cleanup)
          tracked-failure? (or (not (:tracked-complete? cleanup))
                               (and completed? (pos? (:live-descendants-at-finalization cleanup))))
          exit-code (cond
                      timed-out? 124
                      interrupted? 1
                      error 1
                      :else (try
                              (long (.exitValue ^Process process))
                              (catch Throwable throwable
                                (rethrow-fatal! throwable)
                                (restore-interrupt! throwable)
                                1)))
          status (cond
                   timed-out? :timeout
                   interrupted? :error
                   error :error
                   tracked-failure? :error
                   capture-failed? :error
                   (zero? exit-code) :passed
                   :else :failed)
          exit-code (if (and (zero? exit-code)
                             (not= :passed status))
                      1
                      exit-code)]
      {:status status
       :exit-code exit-code
       :wait-error error
       :wait-interrupted? (boolean interrupted?)
       :cleanup cleanup
       :cleanup-root-alive? (boolean root-alive?)
       :root-exited? (not root-alive?)
       :tracked-owned-processes-terminal? (boolean (:tracked-complete? cleanup))
       :descendant-coverage :unknown
       ;; Timeout containment is inherently unproven: ProcessHandle only
       ;; observes the tree present at the snapshot point.  Preserve nested
       ;; best-effort telemetry without claiming lane cleanliness.
       :timeout-containment-unproven? (boolean timed-out?)
       :cleanup-status (if timed-out?
                         :unproven-timeout
                         (if tracked-failure? :failed :unproven-descendant-coverage))
       :cleanup-complete? (if containment-needed?
                            false
                            nil)
       :cleanup-failed? (boolean (or timed-out?
                                     interrupted?
                                     tracked-failure?))
       :capture-failed? (boolean capture-failed?)
       :stdout-capture-status (:status stdout)
       :stderr-capture-status (:status stderr)
       :stdout-capture-error (:error stdout)
       :stderr-capture-error (:error stderr)
       :stdout-capture-forced-close? (boolean (:forced-close? stdout))
       :stderr-capture-forced-close? (boolean (:forced-close? stderr))
       :stdout (:text stdout)
       :stderr (:text stderr)
       :stdout-truncated? (boolean (:truncated? stdout))
       :stderr-truncated? (boolean (:truncated? stderr))
       :output-truncated? (boolean (or (:truncated? stdout)
                                       (:truncated? stderr)))
       :stdout-bytes (:captured-bytes stdout)
       :stderr-bytes (:captured-bytes stderr)
       :stdout-captured-bytes (:captured-bytes stdout)
       :stderr-captured-bytes (:captured-bytes stderr)
       :stdout-chars (:captured-chars stdout)
       :stderr-chars (:captured-chars stderr)
       :stdout-captured-chars (:captured-chars stdout)
       :stderr-captured-chars (:captured-chars stderr)
       :stdout-observed-bytes (:observed-bytes stdout)
       :stderr-observed-bytes (:observed-bytes stderr)
       :stdout-observed-chars (:observed-chars stdout)
       :stderr-observed-chars (:observed-chars stderr)
       :stdout-limit-bytes (:byte-limit stdout)
       :stderr-limit-bytes (:byte-limit stderr)
       :stdout-limit-chars (:char-limit stdout)
       :stderr-limit-chars (:char-limit stderr)})))

(defn process-result
  "Wait/capture/finalize through the normal runner. An optional retained token
  lets the parent retain raw owned handles and the original wait throwable.
  Natural command success does not prove complete descendant containment."
  ([process options]
   (process-result process options (retain-owned-process! process)))
  ([process options owned]
   (require-retained-process! owned)
   (when-not (and (identical? process (:process owned)) (nil? @(:terminal owned)))
     (throw (ex-info "Process wait requires its unused retained ownership token"
                     {:id "SH01-PROCESS-OWNERSHIP"})))
   (let [custody (broker/retain-process-custody! owned)]
     (try
       (broker/record-process-result!
        custody (retained-process-result process options owned))
       (catch Throwable original
         (let [wait-error @(:wait-throwable owned)
               primary (if (and (not (fatal-cause original))
                                (interrupted-throwable? original)
                                (instance? InterruptedException wait-error))
                         wait-error original)]
           (when-not (identical? primary original) (.addSuppressed primary original))
           (try
             (finalize-owned-process! owned)
             (catch Throwable cleanup-error
               (when-not (identical? primary cleanup-error)
                 (.addSuppressed primary cleanup-error))))
           (doseq [stream-fn [#(.getInputStream ^Process process)
                            #(.getErrorStream ^Process process)]]
             (try
               (close-stream! (stream-fn))
               (catch Throwable close-error
                 (when-not (identical? primary close-error)
                   (.addSuppressed primary close-error)))))
           (restore-interrupt! primary)
           (broker/record-process-result! custody {:error primary})
           (throw primary)))))))

(defn- clip-result-text
  [value {:keys [byte-limit char-limit]}]
  (let [text (if (nil? value) "" (str value))
        bytes (.getBytes text StandardCharsets/UTF_8)
        retained-bytes (min (long byte-limit) (alength bytes))
        bounded (bounded-utf8-text bytes retained-bytes char-limit)
        retained-text (:text bounded)
        safe-bytes (:bytes bounded)]
    {:text retained-text
     :captured-bytes safe-bytes
     :captured-chars (count retained-text)
     :observed-bytes (alength bytes)
     :observed-chars (count text)
     :truncated? (or (> (alength bytes) byte-limit)
                     (> (count text) char-limit)
                     (< safe-bytes retained-bytes))
     :byte-limit byte-limit
     :char-limit char-limit}))

(defn- bound-launch-result
  "Applies the same output contract to an injected launcher's result map.

  Production Process values are bounded while their pipes are drained by
  `process-result`; injected maps have no pipe to drain, but must not bypass
  the retention bound and reintroduce unbounded report memory."
  [result options]
  (let [stdout (clip-result-text (:stdout result)
                                 (capture-limits options :stdout))
        stderr (clip-result-text (:stderr result)
                                 (capture-limits options :stderr))
        capture-failed? (or (capture-failure? result)
                            (= :error (:status stdout))
                            (= :error (:status stderr)))
        status (cond
                 capture-failed? :error
                 (:status result) (:status result)
                 (zero? (long (or (:exit-code result) 0))) :passed
                 :else :failed)
        exit-code (if capture-failed?
                    1
                    (long (or (:exit-code result)
                              (if (= :passed status) 0 1))))]
    (merge result
           {:status status
            :exit-code exit-code
            :capture-failed? (boolean capture-failed?)
            :stdout-capture-status (or (:stdout-capture-status result)
                                       :complete)
            :stderr-capture-status (or (:stderr-capture-status result)
                                       :complete)
            :stdout-capture-error (:stdout-capture-error result)
            :stderr-capture-error (:stderr-capture-error result)
            :stdout-capture-forced-close?
            (boolean (:stdout-capture-forced-close? result))
            :stderr-capture-forced-close?
            (boolean (:stderr-capture-forced-close? result))
            :stdout (:text stdout)
            :stderr (:text stderr)
            :stdout-truncated? (:truncated? stdout)
            :stderr-truncated? (:truncated? stderr)
            :output-truncated? (or (:truncated? stdout)
                                   (:truncated? stderr))
            :stdout-bytes (:captured-bytes stdout)
            :stderr-bytes (:captured-bytes stderr)
            :stdout-captured-bytes (:captured-bytes stdout)
            :stderr-captured-bytes (:captured-bytes stderr)
            :stdout-chars (:captured-chars stdout)
            :stderr-chars (:captured-chars stderr)
            :stdout-captured-chars (:captured-chars stdout)
            :stderr-captured-chars (:captured-chars stderr)
            :stdout-observed-bytes (:observed-bytes stdout)
            :stderr-observed-bytes (:observed-bytes stderr)
            :stdout-observed-chars (:observed-chars stdout)
            :stderr-observed-chars (:observed-chars stderr)
            :stdout-limit-bytes (:byte-limit stdout)
            :stderr-limit-bytes (:byte-limit stderr)
            :stdout-limit-chars (:char-limit stdout)
            :stderr-limit-chars (:char-limit stderr)})))

(defn- validate-development-result!
  [source vars qualified namespace]
  (let [{:keys [source-commit source-tree configuration-sha256 limit-bytes]} source]
    (when-not (and (= #{:source-commit :source-tree :configuration-sha256 :limit-bytes}
                      (set (keys source)))
                   (every? #(and (string? %) (re-matches #"[0-9a-f]{40}" %)) [source-commit source-tree])
                   (string? configuration-sha256) (re-matches #"[0-9a-f]{64}" configuration-sha256)
                   (integer? limit-bytes) (pos? limit-bytes) (< limit-bytes Integer/MAX_VALUE)
                   (vector? vars) (seq vars)
                   (every? #(and (string? %) (not (str/blank? %))) vars)
                   (= (count qualified) (count (set qualified)))
                   (every? #(and (str/starts-with? % (str namespace "/"))
                                  (not (str/blank? (subs % (inc (count namespace)))))) qualified))
      (throw (ex-info "Development result requires exact selection and source/configuration binding"
                      {:id "SH01-DEVELOPMENT-RESULT-REQUEST"})))))

(defn- prepare-development-result
  [job options]
  (when-let [{:keys [limit-bytes] :as source}
             (:development-result options)]
    (let [namespace (str (:namespace job))
          vars (:exact-test-vars options)
          qualified (when (and (vector? vars) (every? string? vars))
                      (mapv #(if (str/includes? % "/") % (str namespace "/" %)) vars))]
      (validate-development-result! source vars qualified namespace)
      (let [directory (java.nio.file.Files/createTempDirectory
                       "gravity-development-result-"
                       (make-array java.nio.file.attribute.FileAttribute 0))]
        {:file (str (.resolve directory "result.edn")) :limit-bytes limit-bytes
         :request {:id (str (java.util.UUID/randomUUID))
                   :source (dissoc source :limit-bytes)
                   :selection {:namespaces [namespace] :test-vars (vec (sort qualified))
                               :fail-fast? (boolean (:child-fail-fast? options))}}}))))

(defn- open-development-result-input! [file]
  (java.nio.file.Files/newInputStream
   (.toPath (io/file file))
   (into-array java.nio.file.OpenOption
               [java.nio.file.StandardOpenOption/READ java.nio.file.LinkOption/NOFOLLOW_LINKS])))

(defn- read-development-result-bytes! [file limit exceeded!]
  (let [input (open-development-result-input! file) primary (volatile! nil)]
    (try
      (let [bytes (.readNBytes ^java.io.InputStream input (int limit))]
        (when-not (= -1 (.read ^java.io.InputStream input)) (exceeded!))
        bytes)
      (catch Throwable error (vreset! primary error) (throw error))
      (finally
        (try (.close ^java.io.InputStream input)
             (catch Throwable close-error
               ;; Close diagnostics cannot mask the original read or lose a
               ;; suppressed interruption, including cyclic Throwable causes.
               (try
                 (let [seen (java.util.IdentityHashMap.)]
                   (loop [cause close-error]
                     (when (and cause (not (.containsKey seen cause)))
                       (.put seen cause true)
                       (if (instance? InterruptedException cause)
                         (.interrupt (Thread/currentThread))
                         (recur (.getCause ^Throwable cause))))))
                 (catch Throwable _ nil))
               (if-let [original @primary]
                 (when-not (identical? original close-error)
                   (try (.addSuppressed ^Throwable original close-error) (catch Throwable _ nil)))
                 (throw close-error))))))))

(defn- read-development-result
  [delivery process-result]
  (let [fail! (fn [field & [record cause]]
                (throw (ex-info "Development completion delivery failed validation"
                                {:id "SH01-DEVELOPMENT-RESULT" :field field
                                 :delivery delivery :record record :process-result process-result}
                                cause)))]
    (try
      (let [limit (:limit-bytes delivery)
            bytes (read-development-result-bytes! (:file delivery) limit #(fail! :limit))
            decoder (doto (.newDecoder StandardCharsets/UTF_8)
                      (.onMalformedInput java.nio.charset.CodingErrorAction/REPORT)
                      (.onUnmappableCharacter java.nio.charset.CodingErrorAction/REPORT))
            text (str (.decode decoder (java.nio.ByteBuffer/wrap bytes)))
            record (with-open [reader (java.io.PushbackReader. (java.io.StringReader. text))]
                     (let [eof (Object.)
                           record (edn/read {:eof eof} reader)]
                       (when (or (identical? eof record)
                                 (not (identical? eof (edn/read {:eof eof} reader))))
                         (fail! :one-record record))
                       record))
            result (:result record)
            summary (:summary result)
            full? (= (get-in delivery [:request :selection :test-vars] [])
                     (get-in result [:selection :test-vars]))
            passed? (and (integer? (:fail summary)) (integer? (:error summary))
                         (zero? (:fail summary)) (zero? (:error summary)))
            expected-exit (if passed? 0 1)]
        (when-not (and (= #{:schema :request :result} (set (keys record)))
                       (= :gravity/development-test-result-delivery-v1 (:schema record))
                       (= (:request delivery) (:request record)))
          (fail! :request record))
        (when-not (and (= #{:schema :authority :authoritative? :selection :summary :outcome :exit-code}
                         (set (keys result)))
                       (= :gravity/development-test-result-v1 (:schema result))
                       (= :non-authoritative (:authority result)) (false? (:authoritative? result))
                       (= (:selection (:request delivery)) (:selection result)) full?
                       (= #{:test :pass :fail :error} (set (keys summary)))
                       (every? #(and (integer? %) (<= 0 %)) (vals summary))
                       (= (:test summary) (count (get-in delivery [:request :selection :test-vars])))
                       (= (if passed? :passed :failed) (:outcome result))
                       (= expected-exit (:exit-code result)))
          (fail! :completion record))
        (when-not (and (= expected-exit (:exit-code process-result))
                       (= (:outcome result) (:status process-result))
                       (true? (:root-exited? process-result))
                       (false? (:cleanup-root-alive? process-result))
                       (true? (:tracked-owned-processes-terminal? process-result))
                       (false? (:capture-failed? process-result))
                       (false? (:output-truncated? process-result))
                       (every? (fn [stream]
                                 (and (= :complete (get process-result (keyword (str stream "-capture-status"))))
                                      (nil? (get process-result (keyword (str stream "-capture-error"))))
                                      (false? (get process-result (keyword (str stream "-capture-forced-close?"))))))
                               ["stdout" "stderr"]))
          (fail! :process-capture record))
        result)
      (catch clojure.lang.ExceptionInfo error
        (if (= "SH01-DEVELOPMENT-RESULT" (:id (ex-data error))) (throw error)
            (fail! :read nil error)))
      (catch Throwable error
        (rethrow-fatal! error)
        (restore-interrupt! error)
        (fail! :read nil error)))))

(defn- publish-parent-report!
  [job options request-id observation]
  (let [{:keys [operation disposition resource-class]} observation
        {:keys [state release-attempted? receipt error parent-exit-custody]} disposition
        result (:value operation)
        error-view #(when % (Throwable->map %))
        _ (when-not (and (= (:resource-class job) resource-class)
                         (boolean? (:returned? operation))
                         (if (:returned? operation)
                           (and (nil? (:error operation)) (map? result)
                                (= :supplied-data-matched (:development-result-binding-status result))
                                (contains? #{:passed :failed} (:status result))
                                (= (if (= :passed (:status result)) 0 1) (:exit-code result))
                                (= :gravity/development-test-result-v1
                                   (get-in result [:development-result :schema]))
                                (= (select-keys (:development-result options)
                                                [:source-commit :source-tree :configuration-sha256])
                                   (get-in result [:development-result-request :source]))
                                (= (get-in result [:development-result-request :selection])
                                   (get-in result [:development-result :selection]))
                                (= (:status result) (get-in result [:development-result :outcome]))
                                (= (:exit-code result) (get-in result [:development-result :exit-code])))
                           (instance? Throwable (:error operation)))
                         (case state
                           :released (and (true? release-attempted?) (nil? error)
                                          (= :released (:outcome receipt))
                                          (= resource-class (:resource-class receipt))
                                          (= :gravity/sh01-host-resource-non-authoritative-receipt-v1 (:schema receipt))
                                          (= :not-required-for-this-released-lease parent-exit-custody))
                           :quarantined (and (false? release-attempted?) (nil? receipt)
                                             (or (nil? error) (instance? Throwable error))
                                             (= :unsupported-in-JVM-only parent-exit-custody))
                           :release-failed (and (true? release-attempted?) (nil? receipt)
                                                (instance? Throwable error)
                                                (= :unsupported-in-JVM-only parent-exit-custody))
                           :acquire-failed (and (false? release-attempted?) (nil? receipt)
                                                (instance? Throwable error)
                                                (= :not-established parent-exit-custody))
                           false))
            (throw (ex-info "Final parent report contradicts its actual observation"
                            {:id "SH01-PARENT-REPORT" :observation observation})))
        report {:schema :gravity/development-parent-report-v1
                :authority :non-authoritative :authoritative? false
                :request {:id request-id :namespace (str (:namespace job))
                          :source (select-keys (:development-result options)
                                               [:source-commit :source-tree :configuration-sha256])}
                :operation {:returned? (:returned? operation) :result result
                            :error (error-view (:error operation))}
                :resource {:state state :release-attempted? release-attempted?
                           :receipt receipt :error (error-view error)
                           :parent-exit-custody parent-exit-custody}
                :outcome (cond (or (:error operation) error) :error
                               (= :quarantined state) :unresolved
                               (and (= :passed (:status result)) (zero? (:exit-code result))) :passed
                               :else :failed)}
        report (if-let [owned (:owned-resources observation)]
                 (-> report
                     (assoc :schema :gravity/development-parent-report-v2)
                     (assoc-in [:resource :owned-resource]
                               {:state (:state owned) :release-attempted? (:release-attempted? owned)
                                :receipt (:receipt owned) :error (error-view (:error owned))}))
                 report)
        text (str (pr-str report) "\n")
        _ (when-not (= report (edn/read-string text))
            (throw (ex-info "Final parent report is not round-trip EDN"
                            {:id "SH01-PARENT-REPORT-ENCODING"})))
        bytes (.getBytes text StandardCharsets/UTF_8)
        {:keys [file limit-bytes]} (:parent-report options)]
    (when (> (alength bytes) limit-bytes)
      (throw (ex-info "Final parent report exceeds declared capture limit"
                      {:id "SH01-PARENT-REPORT-LIMIT" :bytes (alength bytes) :limit limit-bytes})))
    (java.nio.file.Files/write
     (.toPath (io/file file)) bytes
     (into-array java.nio.file.OpenOption
                 [java.nio.file.StandardOpenOption/CREATE_NEW java.nio.file.StandardOpenOption/WRITE
                  java.nio.file.LinkOption/NOFOLLOW_LINKS]))
    {:file file :bytes (alength bytes) :report report}))

(defn- report-inside-lease!
  [callback observation]
  (let [callback-error (try (callback observation) nil (catch Throwable error error))
        errors (vec (distinct (remove nil? [(:error observation) callback-error])))
        cause-with (fn [predicate error]
                     (loop [error error seen #{}]
                       (when (and error (not (contains? seen error)))
                         (if (predicate error) error
                             (recur (.getCause ^Throwable error) (conj seen error))))))
        primary (or (some #(cause-with fatal-throwable? %) errors) (first errors))]
    (when (some #(cause-with (fn [error] (instance? InterruptedException error)) %) errors)
      (.interrupt (Thread/currentThread)))
    (when primary
      (doseq [error errors :when (and (not (identical? primary error))
                                      (not (cause-with #(identical? primary %) error)))]
        (.addSuppressed ^Throwable primary error))
      (.addSuppressed ^Throwable primary
                      (ex-info "Inside-lease report observation"
                               {:id "SH01-INSIDE-LEASE-REPORT" :observation observation
                                :callback-error callback-error}))
      (throw primary))
    (:result observation)))

(defn- run-namespace-process-owned
  "Runs one namespace in a fresh process.

  `:process-launcher` receives `[command working-directory]`.  For fast unit
  tests it may return a result map directly; the production launcher returns a
  java.lang.Process and stdout/stderr are drained concurrently.

  Opt-in :resource-broker supplies existing with-lease options. This direct call
  acquires and waits on the same thread, requires an explicit job resource class
  and an actual Process, and consumes the normal process-result custody API.
  It does not admit an operation, alter inherited environment, or cover startup.
  Optional :inside-lease-report! observes return/error synchronously after child
  completion processing, before broker disposition. Its return is ignored; its
  failure propagates through normal disposition. It supplies no guards/custody."
  ([job]
   (run-namespace-process-owned job {} nil))
  ([job options]
   (run-namespace-process-owned job options nil))
  ([job options guards]
   (when (and (contains? options :inside-lease-report!)
              (not (and (ifn? (:inside-lease-report! options))
                         (map? (:resource-broker options))
                         (map? (:development-result options)))))
     (throw (ex-info "Inside-lease report requires a callback and owned completion path"
                     {:id "SH01-INSIDE-LEASE-REPORT-REQUEST"})))
   (when (or (and (contains? options :development-result) (not (map? (:development-result options))))
             (contains? options :development-result-delivery))
     (throw (ex-info "Development result delivery is parent-owned"
                     {:id "SH01-DEVELOPMENT-RESULT-REQUEST"})))
   (when (and (contains? options :parent-report)
              (not (and (= #{:file :limit-bytes} (set (keys (:parent-report options))))
                        (string? (get-in options [:parent-report :file]))
                        (.isAbsolute (io/file (get-in options [:parent-report :file])))
                        (integer? (get-in options [:parent-report :limit-bytes]))
                        (pos? (get-in options [:parent-report :limit-bytes]))
                        (map? (:development-result options))
                        (map? (:resource-broker options)))))
     (throw (ex-info "Final parent report requires owned output and normal resource/completion paths"
                     {:id "SH01-PARENT-REPORT-REQUEST"})))
   (let [resource-owned? (contains? options :resource-broker)
         _ (when (and resource-owned?
                      (not (and (map? (:resource-broker options))
                                 (contains? #{:normal :memory-heavy :exclusive}
                                            (:resource-class job)))))
             (throw (ex-info "Resource-owned child requires broker options and an explicit class"
                             {:id "SH01-PARALLEL-RESOURCE-CALLER"})))
         report-facts (when (:inside-lease-report! options) (volatile! {}))
         operation
         (fn []
           (let [started (System/nanoTime)
                 delivery (prepare-development-result job options)
                 _ (when report-facts (vswap! report-facts assoc :delivery-request (:request delivery)))
                 command (namespace-command (:namespace job)
                                            (cond-> options delivery (assoc :development-result-delivery delivery)))
                 working-directory (or (:working-directory options) @root)
                 launcher (or (:process-launcher options) start-process)
                 launched (launcher command working-directory)
                 _ (when-let [observer (:process-launch-observer options)]
                     (observer))
                 result
                 (if (instance? Process launched)
                   (process-result launched options)
                   (if (or resource-owned? delivery)
                     (throw (ex-info "Resource-owned child requires an actual Process"
                                     {:id "SH01-PARALLEL-RESOURCE-CALLER"}))
                     (if (map? launched)
                       (bound-launch-result launched options)
                       {:status :failed
                        :exit-code 1
                        :stderr (str "Process launcher returned unsupported value: "
                                     (pr-str (type launched)))})))
                 _ (when report-facts (vswap! report-facts assoc :process-result result))
                 result (if delivery
                            (assoc result :development-result (read-development-result delivery result)
                                          :development-result-request (:request delivery)
                                          :development-result-file (:file delivery)
                                          :development-result-binding-status :supplied-data-matched)
                            result)]
             (normalize-result
              job
              (assoc result :command command :child-jvm-launched? true)
              (long (/ (- (System/nanoTime) started) 1000000.0)))))
         operation (if-let [callback (:inside-lease-report! options)]
                     (fn []
                       (let [outcome (try {:tag :returned :result (operation) :error nil}
                                          (catch Throwable error {:tag :threw :result nil :error error}))]
                         (try
                           (report-inside-lease! callback (merge @report-facts {:job job} outcome))
                           (finally
                             (when (.isInterrupted (Thread/currentThread))
                               (vswap! report-facts assoc :interrupted? true))))))
                     operation)
         operation (if guards
                     (fn [] ((:acquire! guards)) (operation))
                     operation)]
     (try
      (if guards
        (let [publication (atom nil) request-id (str (java.util.UUID/randomUUID))
              result (broker/with-lease
                      (:resource-broker options) (:resource-class job) operation (:finalize! guards)
                      #(reset! publication (publish-parent-report! job options request-id %)))]
          (assoc result :parent-report @publication))
       (if resource-owned?
       (if (or (:parent-report options) (:inside-lease-report! options))
         (let [publication (atom nil)
               request-id (str (java.util.UUID/randomUUID))
               result (broker/with-lease
                        (:resource-broker options) (:resource-class job) operation
                        #(when (:parent-report options)
                           (reset! publication (publish-parent-report! job options request-id %))))]
           (if (:parent-report options) (assoc result :parent-report @publication) result))
         (broker/with-lease (:resource-broker options) (:resource-class job) operation (fn [_] nil)))
       (operation)))
      (finally
        (when (and report-facts (:interrupted? @report-facts))
          (.interrupt (Thread/currentThread))))))))

(def ^:private resolution-parent-namespace 'gravity.self-hosting.resolution-parent-mutation-test)

(def ^:private resolution-parent-selector "actual-macro-trace-input-syntax-id-mutation-at-second-id")

(def ^:private resolution-canonical-lock "/private/tmp/gravity-sh07-heavy.lock")

(defn- resolution-common-broker-root
  [working-directory]
  ;; Read Git worktree metadata, never start an unowned Git helper.
  (let [cwd (.normalize (.toAbsolutePath (.toPath (io/file working-directory))))
        entry (.resolve cwd ".git")
        nofollow (into-array java.nio.file.LinkOption [java.nio.file.LinkOption/NOFOLLOW_LINKS])
        read-path (fn [path]
                    (when-not (java.nio.file.Files/isRegularFile path nofollow)
                      (throw (ex-info "Expected regular Git metadata" {:id "SH01-RESOLUTION-OWNER-REQUEST"})))
                    (str/trim (java.nio.file.Files/readString path StandardCharsets/UTF_8)))
        gitdir (if (java.nio.file.Files/isDirectory entry nofollow) entry
                   (let [text (read-path entry) match (re-matches #"gitdir: ([^\r\n]+)" text)]
                     (when-not match
                       (throw (ex-info "Invalid worktree Git metadata" {:id "SH01-RESOLUTION-OWNER-REQUEST"})))
                     (.normalize (.resolve cwd (second match)))))
        common-entry (.resolve gitdir "commondir")
        common (if (java.nio.file.Files/exists common-entry nofollow)
                 (.normalize (.resolve gitdir (read-path common-entry))) gitdir)]
    (str (.resolve common "gravity-development-loop-v1/broker"))))

(defn- acquire-resolution-lock!
  [custody]
  (let [path (.toPath (io/file resolution-canonical-lock))
        channel (java.nio.channels.FileChannel/open
                 path
                 #{java.nio.file.StandardOpenOption/CREATE java.nio.file.StandardOpenOption/WRITE
                   java.nio.file.LinkOption/NOFOLLOW_LINKS}
                 (into-array java.nio.file.attribute.FileAttribute
                             [(java.nio.file.attribute.PosixFilePermissions/asFileAttribute
                               (java.nio.file.attribute.PosixFilePermissions/fromString "rw-------"))]))]
    (swap! custody assoc :channel channel :lock-acquired? :unknown)
    (let [lock (.tryLock ^java.nio.channels.FileChannel channel)]
      (swap! custody assoc :lock lock :lock-acquired? (boolean lock))
      (when-not lock
        (throw (ex-info "Canonical heavy lock is busy" {:id "SH01-RESOLUTION-CANONICAL-BUSY"}))))))

(defn- finalize-resolution-lock!
  [custody {:keys [terminal]}]
  (let [{:keys [channel lock lock-acquired?]} @custody
        result (cond
                 (nil? channel)
                 {:state :not-acquired :release-attempted? false :error nil}

                 (or (= :unknown lock-acquired?) (not (true? (:complete? terminal))))
                 {:state :quarantined :release-attempted? false :error nil}

                 :else
                 (try
                   (when lock
                     (swap! custody assoc :release-attempted? true)
                     (.release ^java.nio.channels.FileLock lock)
                     (swap! custody assoc :lock-released? (not (.isValid ^java.nio.channels.FileLock lock)))
                     (when-not (:lock-released? @custody)
                       (throw (ex-info "Canonical lock remains held" {:id "SH01-RESOLUTION-CANONICAL-RELEASE"}))))
                   (swap! custody assoc :close-attempted? true)
                   (.close ^java.nio.channels.FileChannel channel)
                   (swap! custody assoc :channel-closed? (not (.isOpen ^java.nio.channels.FileChannel channel)))
                   (when-not (:channel-closed? @custody)
                     (throw (ex-info "Canonical channel remains open" {:id "SH01-RESOLUTION-CANONICAL-RELEASE"})))
                   {:state (if lock :released :not-acquired)
                    :release-attempted? (boolean (:release-attempted? @custody)) :error nil}
                   (catch Throwable error
                     {:state :release-failed :release-attempted? (boolean (:release-attempted? @custody))
                      :error error})))]
    (swap! custody assoc :disposition result)
    (assoc result :custody custody
                 :receipt (assoc (select-keys @custody [:lock-acquired? :release-attempted? :lock-released?
                                                       :close-attempted? :channel-closed?])
                                 :canonical-path resolution-canonical-lock))))

(defn run-resolution-parent-mutation-process
  "Dedicated cooperative owner route for the one trace input-syntax control.
  Always C-exclusive then library-owned canonical L; ordinary routing is unchanged.
  This source path supplies no genuine/runtime or held-operation admission."
  [selected-operation options]
  (let [expected {:id :RV1-trace-input-syntax-second-id
                  :selector (symbol (str resolution-parent-namespace "/" resolution-parent-selector))
                  :reuse-held-operation false :genuine-execution false}
        allowed #{:resource-broker :command :exact-test-vars :child-fail-fast? :working-directory
                  :development-result :inside-lease-report! :parent-report :timeout-ms
                  :max-capture-bytes :max-capture-chars :capture-wait-ms}
        source (:development-result options)
        cwd (:working-directory options)]
    (when-not (and (= expected selected-operation) (map? options)
                   (every? allowed (keys options))
                   (= [resolution-parent-selector] (:exact-test-vars options))
                   (false? (:child-fail-fast? options))
                   (vector? (:command options)) (seq (:command options))
                   (every? #(and (string? %) (not (str/blank? %))) (:command options))
                   (.isAbsolute (io/file (first (:command options))))
                   (string? cwd) (.isAbsolute (io/file cwd))
                   (map? (:resource-broker options)) (map? source)
                   (ifn? (:inside-lease-report! options))
                   (map? (:parent-report options))
                   (every? #(and (integer? (get options %)) (pos? (get options %))
                                  (<= (get options %) Long/MAX_VALUE))
                           [:timeout-ms :max-capture-bytes :max-capture-chars :capture-wait-ms]))
      (throw (ex-info "Invalid exact resolution owner request" {:id "SH01-RESOLUTION-OWNER-REQUEST"})))
    (validate-development-result! source (:exact-test-vars options)
                                 [(str resolution-parent-namespace "/" resolution-parent-selector)]
                                 (str resolution-parent-namespace))
    (when-not (= (resolution-common-broker-root cwd)
                 (some-> (get-in options [:resource-broker :coordination-root]) io/file .toPath .toAbsolutePath .normalize str))
      (throw (ex-info "Resolution owner requires the shared Git-common broker root"
                      {:id "SH01-RESOLUTION-OWNER-REQUEST"})))
    (let [custody (atom {:channel nil :lock nil :lock-acquired? false
                        :release-attempted? false :close-attempted? false})]
      (run-namespace-process-owned
       {:namespace resolution-parent-namespace :resource-class :exclusive} options
       {:acquire! #(acquire-resolution-lock! custody)
        :finalize! #(finalize-resolution-lock! custody %)}))))


(defn run-namespace-process
  "Run the ordinary namespace path, preserving the owner's unsupported guard."
  ([job] (run-namespace-process job {}))
  ([job options]
   (require-supported-namespace! (:namespace job))
   (run-namespace-process-owned job options)))

(defn- parse-positive-option
  [arguments index option]
  (let [value (get arguments (inc index))]
    (when (or (nil? value) (str/starts-with? value "--"))
      (throw
       (ex-info
        (str option " requires a value")
        {:id "SH01-PARALLEL-USAGE" :option option})))
    [(inc index) (positive-integer value option)]))

(defn parse-arguments
  "Parses runner controls without constructing a plan.

  `--slice SH-NN` and `--changed` are translated into the existing planner's
  build-plan request.  No planner implementation or ownership file is edited."
  [arguments]
  (loop [remaining (vec arguments)
         request {}
         options {}
         mode nil
         dry-run? false]
    (if (empty? remaining)
      (let [_
            (when-not mode
              (throw
               (ex-info
                "SH-01 parallel execution requires a selection mode"
                {:id "SH01-PARALLEL-SELECTION"
                 :supported ["--namespace NS" "--slice SH-NN" "--changed"]})))
            request
            (if (= :iteration mode)
              (do
                (when-not (:changed-selection? request)
                  (throw
                   (ex-info
                    "Iteration mode requires --changed"
                    {:id "SH01-PARALLEL-USAGE"})))
                (dissoc request :changed-selection?))
              (dissoc request :changed-selection?))]
        {:request (cond-> request
                    (and (= :changed mode)
                         (not (contains? request :expand-dependants?)))
                    (assoc :expand-dependants? true))
         :options options
         :mode mode
         :dry-run? dry-run?})
      (let [argument (first remaining)]
        (cond
          (= "--changed" argument)
          (if (#{:slice :namespace} mode)
            (throw
             (ex-info "Only one planner selection mode may be supplied"
                      {:id "SH01-PARALLEL-USAGE" :arguments (vec arguments)}))
            (recur (subvec remaining 1)
                   (assoc request :changed-selection? true)
                   options
                   (if (= :iteration mode) :iteration :changed)
                   dry-run?))

          (= "--namespace" argument)
          (let [namespace (get remaining 1)]
            (when (or (nil? namespace) (str/starts-with? namespace "--"))
              (throw
               (ex-info "--namespace requires a dedicated test namespace"
                        {:id "SH01-PARALLEL-USAGE"})))
            (when (#{:slice :changed :iteration} mode)
              (throw
               (ex-info "Only one planner selection mode may be supplied"
                        {:id "SH01-PARALLEL-USAGE"})))
            (when (contains? request :expand-dependants?)
              (throw
               (ex-info "Exact namespace mode does not accept dependant expansion"
                        {:id "SH01-PARALLEL-USAGE"})))
            (recur (subvec remaining 2)
                   (update request :direct-namespaces (fnil conj [])
                           (symbol namespace))
                   options
                   :namespace
                   dry-run?))

          (= "--slice" argument)
          (let [slice (get remaining 1)]
            (when (or (nil? slice) (str/starts-with? slice "--"))
              (throw
               (ex-info "--slice requires SH-NN"
                        {:id "SH01-PARALLEL-USAGE"})))
            (when (#{:changed :iteration :namespace} mode)
              (throw
               (ex-info "Only one planner selection mode may be supplied"
                        {:id "SH01-PARALLEL-USAGE"})))
            (recur (subvec remaining 2)
                   {:direct-slices #{slice}}
                   options
                   :slice
                   dry-run?))

          (= "--iteration-slice" argument)
          (let [slice (get remaining 1)]
            (when (or (nil? slice) (str/starts-with? slice "--"))
              (throw
               (ex-info "--iteration-slice requires SH-NN"
                        {:id "SH01-PARALLEL-USAGE"})))
            (when (#{:slice :namespace} mode)
              (throw
               (ex-info "Only one planner selection mode may be supplied"
                        {:id "SH01-PARALLEL-USAGE"})))
            (recur (subvec remaining 2)
                   (update request :iteration-slices (fnil conj #{}) slice)
                   options
                   :iteration
                   dry-run?))

          (or (= "--dry-run" argument) (= "--plan" argument))
          (recur (subvec remaining 1) request options mode true)

          (= "--fail-fast" argument)
          (recur (subvec remaining 1)
                 request
                 (assoc options :fail-fast? true :fail-fast true)
                 mode
                 dry-run?)

          (or (= "--expand-dependants" argument)
              (= "--expand-dependents" argument))
          (if (= :namespace mode)
            (throw
             (ex-info "Exact namespace mode does not accept dependant expansion"
                      {:id "SH01-PARALLEL-USAGE"}))
            (recur (subvec remaining 1)
                   (assoc request :expand-dependants? true)
                   options mode dry-run?))

          (= "--no-expand-dependants" argument)
          (if (= :namespace mode)
            (throw
             (ex-info "Exact namespace mode does not accept dependant expansion"
                      {:id "SH01-PARALLEL-USAGE"}))
            (recur (subvec remaining 1)
                   (assoc request :expand-dependants? false)
                   options mode dry-run?))

          (or (= "--normal-parallelism" argument)
              (= "--normal-jobs" argument)
              (= "--parallelism" argument))
          (let [[index value]
                (parse-positive-option
                 remaining 0 (keyword (subs argument 2)))]
            (recur (subvec remaining 2)
                   request
                   (assoc options :normal-parallelism value)
                   mode dry-run?))

          (= "--normal-batch-size" argument)
          (let [[_ value]
                (parse-positive-option remaining 0 :normal-batch-size)]
            (recur (subvec remaining 2)
                   request
                   (assoc options :normal-batch-size value)
                   mode dry-run?))

          (or (= "--memory-parallelism" argument)
              (= "--memory-heavy-parallelism" argument)
              (= "--memory-jobs" argument))
          (let [[_ value]
                (parse-positive-option
                 remaining 0 (keyword (subs argument 2)))]
            (when-not (= 1 value)
              (throw
               (ex-info
                "Memory-heavy SH-01 tests require concurrency exactly one"
                {:id "SH01-PARALLEL-MEMORY-LIMIT"
                 :memory-parallelism value
                 :required 1})))
            (recur (subvec remaining 2)
                   request
                   (assoc options :memory-parallelism value)
                   mode dry-run?))

          (or (= "--timeout-ms" argument)
              (= "--process-timeout-ms" argument))
          (let [[_ value]
                (parse-positive-option
                 remaining 0 (keyword (subs argument 2)))]
            (recur (subvec remaining 2)
                   request
                   (assoc options :timeout-ms value)
                   mode dry-run?))

          (= "--working-directory" argument)
          (let [directory (get remaining 1)]
            (when (or (nil? directory) (str/starts-with? directory "--"))
              (throw
               (ex-info "--working-directory requires a path"
                        {:id "SH01-PARALLEL-USAGE"})))
            (recur (subvec remaining 2)
                   request
                   (assoc options :working-directory directory)
                   mode dry-run?))

          (= "--command" argument)
          (let [command (get remaining 1)]
            (when (or (nil? command) (str/starts-with? command "--"))
              (throw
               (ex-info "--command requires an executable path"
                        {:id "SH01-PARALLEL-USAGE"})))
            (recur (subvec remaining 2)
                   request
                   (assoc options :clojure-command command)
                   mode dry-run?))

          (or (= "--output-limit-bytes" argument)
              (= "--capture-limit-bytes" argument))
          (let [[_ value]
                (parse-positive-option
                 remaining 0 (keyword (subs argument 2)))]
            (recur (subvec remaining 2)
                   request
                   (assoc options :output-limit-bytes value)
                   mode dry-run?))

          (or (= "--output-limit-chars" argument)
              (= "--capture-limit-chars" argument))
          (let [[_ value]
                (parse-positive-option
                 remaining 0 (keyword (subs argument 2)))]
            (recur (subvec remaining 2)
                   request
                   (assoc options :output-limit-chars value)
                   mode dry-run?))

          (= "--help" argument)
          {:help? true
           :request request
           :options options
           :mode mode
           :dry-run? dry-run?}

          :else
          (throw
           (ex-info
            "Unsupported SH-01 parallel runner argument"
            {:id "SH01-PARALLEL-USAGE"
             :argument argument
             :arguments (vec arguments)})))))))

(defn build-plan-from-arguments
  "Builds a plan through the existing SH-01 planner only."
  [arguments]
  (let [{:keys [request help?] :as parsed} (parse-arguments arguments)]
    (if help?
      parsed
      (let [request
            (cond-> request
              (#{:changed :iteration} (:mode parsed))
              (assoc :changed-paths (planner/changed-paths))
              true
              (dissoc :changed-selection?))]
        (assoc parsed :plan
               (if (= :namespace (:mode parsed))
                 (planner/build-namespace-plan (:direct-namespaces request))
                 (planner/build-plan request)))))))

(defn run-cli
  "Runs the CLI request and returns an EDN-friendly result without exiting."
  [arguments]
  (let [{:keys [help? plan options dry-run?] :as parsed}
        (build-plan-from-arguments arguments)]
    (if help?
      (let [report
            {:status :help
             :exit-code 0
             :usage
             "sh01-parallel-test-runner (--namespace NS)...|--slice SH-NN|(--changed [--iteration-slice SH-NN]...) [--dry-run] [--fail-fast] [--normal-parallelism N] [--normal-batch-size N] [--memory-parallelism 1] [--output-limit-bytes N] [--output-limit-chars N]"}]
        (println (:usage report))
        report)
      (let [schedule (dry-run plan options)]
        (if dry-run?
          (let [report
                {:schema :gravity/sh01-parallel-test-report-v1
                 :plan-schema (:schema plan)
                 :plan-authority (:plan-authority schedule)
                 :status :dry-run
                 :ok? true
                 :exit-code 0
                 :results []
                 :schedule schedule}]
            (println (str "SH-01 plan authority: "
                          (name (get-in report [:plan-authority :status]))))
            (prn report)
          (assoc parsed :schedule schedule :report report :exit-code 0))
          (let [report (execute-plan plan options)]
            (println (str "SH-01 plan authority: "
                          (name (get-in report [:plan-authority :status]))))
            (prn report)
            (assoc parsed :schedule schedule :report report
                   :exit-code (:exit-code report))))))))

(defn -main
  [& arguments]
  (let [result (run-cli arguments)]
    (when (pos? (long (or (:exit-code result) 0)))
      (System/exit (:exit-code result)))
    result))
