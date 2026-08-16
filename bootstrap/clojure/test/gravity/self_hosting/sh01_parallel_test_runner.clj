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
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [gravity.self-hosting.sh01-impact-test-planner :as planner]))

(import '(java.io ByteArrayOutputStream InputStream)
        '(java.nio ByteBuffer CharBuffer)
        '(java.nio.charset CodingErrorAction StandardCharsets)
        '(java.util.concurrent ExecutorCompletionService Executors
          ExecutorService LinkedBlockingQueue TimeUnit))

(def ^:private default-normal-parallelism 2)
(def ^:private default-memory-parallelism 1)
(def ^:private default-command ["clojure" "-M:test"])
(def ^:private default-capture-limit-bytes (* 1024 1024))
(def ^:private default-capture-limit-chars (* 1024 1024))
(def ^:private default-capture-wait-ms 5000)
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
        memory
        (or (:memory-parallelism options)
            (:memory-heavy-parallelism options)
            (:memory-jobs options)
            default-memory-parallelism)]
    (let [normal (positive-integer normal :normal-parallelism)
          memory (positive-integer memory :memory-parallelism)]
      (when-not (= 1 memory)
        (throw
         (ex-info
          "Memory-heavy SH-01 tests require concurrency exactly one"
          {:id "SH01-PARALLEL-MEMORY-LIMIT"
           :memory-parallelism memory
           :required 1})))
      (assoc options
             :normal-parallelism normal
             :memory-parallelism memory
             ;; Both spellings are accepted by the programmatic API.  The
             ;; question-mark spelling is used by the CLI, matching the
             ;; existing SH-07 runner, while :fail-fast keeps embedding code
             ;; concise.
             :fail-fast? (boolean (or (:fail-fast? options)
                                      (:fail-fast options)))
             :fail-fast (boolean (or (:fail-fast? options)
                                     (:fail-fast options)))))))

(defn schedule-plan
  "Returns the deterministic execution schedule for an impact plan.

  The parallel phase submits all normal and memory-heavy jobs to independent
  bounded executors.  Executor queues refill immediately as slots become free;
  there is no fixed-wave barrier.  After both queues drain, the exclusive phase
  runs its jobs sequentially with capacity one."
  ([plan]
   (schedule-plan plan {}))
  ([plan options]
   (let [{:keys [normal-parallelism memory-parallelism] :as options}
         (execution-options options)
         jobs (canonical-jobs plan)
         grouped (group-by resource-class jobs)
         normal (vec (get grouped :normal []))
         memory (vec (get grouped :memory-heavy []))
         exclusive (vec (get grouped :exclusive []))
         parallel-phase
         {:phase :parallel
          :capacities {:normal normal-parallelism
                       :memory-heavy memory-parallelism}
          :normal normal
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
      :memory-parallelism memory-parallelism
      :fail-fast? (:fail-fast? options)
      :fail-fast (:fail-fast? options)
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
        (normalize-result
         job
         {:status :error
          :exit-code 1
          :stderr (throwable-message throwable)
          :interrupted? (interrupted-throwable? throwable)
          :interrupt-restored? (.isInterrupted (Thread/currentThread))
          :exception-class (class throwable)}
          (elapsed-ms started))))))

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

(declare run-namespace-process)

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

(defn execute-plan
  "Executes a plan according to `schedule-plan` and returns a report.

  Options include `:normal-parallelism`, `:memory-parallelism`, `:timeout-ms`,
  `:working-directory`, `:command`, `:process-launcher`, and an injectable
  one-argument `:worker`.  The default worker launches one fresh Clojure
  process per namespace.  `:fail-fast?` (or `:fail-fast`) keeps only one
  capacity-sized submission set in flight and stops refilling both independent
  pools after the first failed, timed-out, or errored result.  Already-running
  jobs drain safely; skipped jobs are reported in deterministic plan order.
  Without fail-fast every selected job still runs to completion."
  ([plan]
   (execute-plan plan {}))
  ([plan options]
   (let [options (execution-options options)
         schedule (schedule-plan plan options)
         worker (or (:worker options)
                    #(run-namespace-process % options))
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
              {:normal (vec (get-in schedule [:parallel-phase :normal]))
               :memory-heavy
               (vec (get-in schedule [:parallel-phase :memory-heavy]))}
              fail-fast?
              (:completion-observer options))
             ;; Exclusive work is intentionally started only after both
             ;; independent pools have drained.  It is sequential even when
             ;; fail-fast is disabled, and the same stop marker prevents any
             ;; exclusive job from starting after a parallel failure.
             results-by-key
             (atom (:results parallel-state))
             stop? (atom (:stop? parallel-state))
             failure (atom (:failure parallel-state))
             exclusive-jobs
             (get-in schedule [:exclusive-phase :exclusive])]
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
               incomplete? (seq skipped-jobs)]
           {:schema :gravity/sh01-parallel-test-report-v1
            :plan-schema (:plan-schema schedule)
            :plan-authority (:plan-authority schedule)
            ;; Execution results are evidence from a bounded development
            ;; runner, never an authority promotion.  Preserve the planner's
            ;; metadata separately above.
            :authority :non-authoritative
            :authoritative? false
            :normal-parallelism (:normal-parallelism schedule)
            :memory-parallelism (:memory-parallelism schedule)
            :fail-fast? (boolean fail-fast?)
            :fail-fast (boolean fail-fast?)
            :fail-fast-triggered? (boolean (and fail-fast?
                                                (seq skipped-jobs)))
            :fail-fast-failure (or @failure (:failure parallel-state))
            :cleanup-failed? (boolean (seq cleanup-failures))
            :cleanup-failures cleanup-failures
            :capture-failed? (boolean (seq capture-failures))
            :capture-failures capture-failures
            :jobs (count results)
            :planned-jobs (count (:jobs schedule))
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

(defn namespace-command
  "Returns the exact ProcessBuilder argument vector for one namespace."
  ([namespace]
   (namespace-command namespace {}))
  ([namespace options]
   (into (vec (command-prefix options))
         ["--namespace" (namespace-name namespace)])))

(defn start-process
  "Starts a process from an argument vector and working directory.

  This function is public specifically so tests and embedders can inject a
  launcher without replacing ProcessBuilder or invoking a shell."
  [command working-directory]
  (-> (ProcessBuilder. ^java.util.List (vec (map str command)))
      (.directory (io/file working-directory))
      (.redirectErrorStream false)
      (.start)))

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

(defn- process-descendants
  [^Process process]
  (let [root (.toHandle process)]
    (try
      (with-open [descendants (.descendants root)]
        {:handles (vec (iterator-seq (.iterator descendants)))
         :error nil})
      (catch Throwable throwable
        (rethrow-fatal! throwable)
        (restore-interrupt! throwable)
        ;; Enumeration failure is itself non-authoritative.  The caller still
        ;; terminates the root, but must not claim the descendant tree was
        ;; cleaned when it could not observe that tree.
        {:handles []
         :error (str throwable)}))))

(defn- handle-pid
  [^java.lang.ProcessHandle handle]
  (long (.pid handle)))

(defn- alive-handle?
  [^java.lang.ProcessHandle handle]
  (try
    (.isAlive handle)
    (catch Throwable throwable
      (rethrow-fatal! throwable)
      (restore-interrupt! throwable)
      ;; If a handle can no longer be queried, retain it as a cleanup failure
      ;; rather than promoting an unverified timeout to a clean result.
      true)))

(defn- request-handle-termination!
  [^java.lang.ProcessHandle handle forcibly?]
  (try
    (if forcibly?
      (.destroyForcibly handle)
      (.destroy handle))
    (catch Throwable throwable
      (rethrow-fatal! throwable)
      (restore-interrupt! throwable)
      false)))

(defn- reap-process-handle!
  [^java.lang.ProcessHandle handle]
  (try
    ;; ProcessHandle::onExit is the JVM-level wait/reap boundary for a
    ;; descendant that is not represented by a java.lang.Process object.
    (.get (.onExit handle) 100 TimeUnit/MILLISECONDS)
    true
    (catch Throwable throwable
      (rethrow-fatal! throwable)
      (restore-interrupt! throwable)
      (not (alive-handle? handle)))))

(defn- terminate-process-tree!
  "Terminates and reaps the observed ProcessHandle descendant tree.

  This is intentionally a bounded JVM ProcessHandle operation, not a host
  `ps` census or a container/supervisor implementation.  The observed handle
  set is retained across root termination so reparenting cannot turn a known
  live descendant into an unreported success.  Unknown cross-session escapes
  remain outside this Stage-0 claim."
  [^Process process]
  (let [root (.toHandle process)
        descendant-observation (process-descendants process)
        descendants (:handles descendant-observation)
        discovery-error (:error descendant-observation)
        observed (vec (distinct (cons root descendants)))
        observed-descendants (vec (remove #(= root %) observed))
        started (System/nanoTime)
        grace-deadline (+ started (* process-cleanup-grace-ms 1000000))
        deadline (+ started (* process-cleanup-timeout-ms 1000000))]
    ;; Kill deepest observed descendants first, then the root.  The root is
    ;; still handled explicitly because descendants can outlive its shell.
    (doseq [handle (reverse observed-descendants)]
      (when (alive-handle? handle)
        (request-handle-termination! handle false)))
    (when (alive-handle? root)
      (request-handle-termination! root false))
    (loop [forced? false]
      (let [alive (vec (filter alive-handle? observed))
            now (System/nanoTime)]
        (cond
          (empty? alive)
          (do
            (doseq [handle observed]
              (reap-process-handle! handle))
            {:status (if discovery-error :failed :clean)
             :complete? (not discovery-error)
             :forced? forced?
             :root-alive? false
             :descendants-alive 0
             :observed-pids (mapv handle-pid observed)
             :survivor-pids []
             :discovery-error discovery-error})

          (and (not forced?) (>= now grace-deadline))
          (do
            (doseq [handle (reverse alive)]
              (request-handle-termination! handle true))
            (recur true))

          (>= now deadline)
          (let [survivors (vec (filter alive-handle? observed))]
            {:status :failed
             :complete? false
             :forced? forced?
             :root-alive? (alive-handle? root)
             :descendants-alive
             (count (remove #(= root %) survivors))
             :observed-pids (mapv handle-pid observed)
             :survivor-pids (mapv handle-pid survivors)
             :discovery-error discovery-error})

          :else
          (do
            ;; A bounded park avoids an uninterruptible sleep while retaining
            ;; a deterministic cleanup deadline.  Interrupted cleanup is
            ;; reported as incomplete by the next alive-handle observation.
            (java.util.concurrent.locks.LockSupport/parkNanos
             (long (* (min 25
                           (max 1
                                (quot (- deadline now)
                                      1000000)))
                       1000000)))
            (recur forced?)))))))

(defn- wait-for-process
  "Waits for a process with an optional bound and never hides interruption.

  The unbounded form is used only for an ordinary process whose caller did
  not request a timeout.  Cleanup paths always use the bounded form below."
  [^Process process timeout-ms]
  (try
    (if (some? timeout-ms)
      {:completed? (.waitFor process (long timeout-ms) TimeUnit/MILLISECONDS)
       :interrupted? false}
      (do
        (.waitFor process)
        {:completed? true :interrupted? false}))
    (catch Throwable throwable
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

(defn- process-result
  [process options]
  (let [stdout-stream (.getInputStream ^Process process)
        stderr-stream (.getErrorStream ^Process process)
        stdout-capture (stream-capture stdout-stream
                                       (capture-limits options :stdout))
        stderr-capture (stream-capture stderr-stream
                                       (capture-limits options :stderr))
        {:keys [completed? interrupted? error] :as wait-result}
        (wait-for-process process (:timeout-ms options))
        timed-out? (and (some? (:timeout-ms options)) (not completed?))
        containment-needed? (or timed-out? interrupted?)
        cleanup (when containment-needed?
                  ;; Capture and retain the ProcessHandle tree before the root
                  ;; exits.  Cleanup remains best effort; every timeout is
                  ;; still marked unproven because a descendant may fork after
                  ;; this snapshot.
                  (terminate-process-tree! process))]
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
          root-alive? (when containment-needed?
                        (try (.isAlive ^Process process)
                             (catch Throwable throwable
                               (rethrow-fatal! throwable)
                               (restore-interrupt! throwable)
                               true)))
          exit-code (cond
                      timed-out? 124
                      interrupted? 1
                      :else (try
                              (long (.exitValue ^Process process))
                              (catch Throwable throwable
                                (rethrow-fatal! throwable)
                                (restore-interrupt! throwable)
                                1)))
          status (cond
                   timed-out? :timeout
                   interrupted? :error
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
       ;; Timeout containment is inherently unproven: ProcessHandle only
       ;; observes the tree present at the snapshot point.  Preserve nested
       ;; best-effort telemetry without claiming lane cleanliness.
       :timeout-containment-unproven? (boolean timed-out?)
       :cleanup-status (if timed-out?
                         :unproven-timeout
                         (:status cleanup))
       :cleanup-complete? (if containment-needed?
                            false
                            true)
       :cleanup-failed? (boolean (or timed-out?
                                     interrupted?
                                     (and cleanup
                                          (not (:complete? cleanup)))))
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
                 :else :failed)]
    (merge result
           {:status status
            :exit-code (if (and capture-failed?
                               (zero? (long (or (:exit-code result) 0))))
                         1
                         (:exit-code result))
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

(defn run-namespace-process
  "Runs one namespace in a fresh process.

  `:process-launcher` receives `[command working-directory]`.  For fast unit
  tests it may return a result map directly; the production launcher returns a
  java.lang.Process and stdout/stderr are drained concurrently."
  ([job]
   (run-namespace-process job {}))
  ([job options]
   (let [started (System/nanoTime)
         command (namespace-command (:namespace job) options)
         working-directory (or (:working-directory options) @root)
         launcher (or (:process-launcher options) start-process)
         launched (launcher command working-directory)
         result
         (if (instance? Process launched)
           (process-result launched options)
           (if (map? launched)
             (bound-launch-result launched options)
             {:status :failed
              :exit-code 1
              :stderr (str "Process launcher returned unsupported value: "
                           (pr-str (type launched)))}))]
     (normalize-result
      job
      (assoc result :command command)
      (long (/ (- (System/nanoTime) started) 1000000.0))))))

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
             "sh01-parallel-test-runner (--namespace NS)...|--slice SH-NN|(--changed [--iteration-slice SH-NN]...) [--dry-run] [--fail-fast] [--normal-parallelism N] [--memory-parallelism 1] [--output-limit-bytes N] [--output-limit-chars N]"}]
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
