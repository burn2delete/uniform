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

(import '(java.util.concurrent Executors ExecutorService TimeUnit))

(def ^:private default-normal-parallelism 2)
(def ^:private default-memory-parallelism 1)
(def ^:private default-command ["clojure" "-M:test"])

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
    (when-not (and (integer? number) (pos? number))
      (throw
       (ex-info
        (str option " must be a positive integer")
        {:id "SH01-PARALLEL-OPTION"
         :option option
         :value value})))
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
             :memory-parallelism memory))))

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

(defn- result-failure?
  [result]
  (or (contains? #{:failed :error :timeout} (:status result))
      (and (number? (:exit-code result))
           (not (zero? (:exit-code result))))
      (pos? (long (or (:fail result) 0)))
      (pos? (long (or (:error result) 0)))
      (false? (:ok? result))))

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
        (normalize-result
         job
         {:status :error
          :exit-code 1
          :stderr (or (.getMessage throwable) (str throwable))
          :exception-class (class throwable)}
          (elapsed-ms started))))))

(defn- await-future
  [future]
  (try
    @future
    (catch Throwable throwable
      {:status :error
       :exit-code 1
       :stderr (or (.getMessage throwable) (str throwable))
       :exception-class (class throwable)})))

(declare run-namespace-process)

(defn- submit-jobs
  [executor worker jobs]
  (mapv
   (fn [job]
     [job (.submit ^ExecutorService executor
                    ^java.util.concurrent.Callable
                    (fn [] (invoke-worker worker job)))])
   jobs))

(defn- collect-submitted
  [results submitted]
  (reduce
   (fn [results [job future]]
     (let [result (normalize-result job (await-future future) 0)]
       (assoc results (namespace-key (:namespace job)) result)))
   results
   submitted))

(defn execute-plan
  "Executes a plan according to `schedule-plan` and returns a report.

  Options include `:normal-parallelism`, `:memory-parallelism`, `:timeout-ms`,
  `:working-directory`, `:command`, `:process-launcher`, and an injectable
  one-argument `:worker`.  The default worker launches one fresh Clojure
  process per namespace.  All jobs run to completion so failures are reported
  together; `:exit-code` is one if any job failed."
  ([plan]
   (execute-plan plan {}))
  ([plan options]
   (let [options (execution-options options)
         schedule (schedule-plan plan options)
         worker (or (:worker options)
                    #(run-namespace-process % options))
         normal-executor
         (Executors/newFixedThreadPool
          (int (:normal-parallelism schedule)))
         memory-executor
         (Executors/newFixedThreadPool
          (int (:memory-parallelism schedule)))]
     (try
       (let [normal-submitted
             (submit-jobs normal-executor worker
                          (get-in schedule [:parallel-phase :normal]))
             memory-submitted
             (submit-jobs memory-executor worker
                          (get-in schedule [:parallel-phase :memory-heavy]))
             ;; Submitting every job before waiting lets each bounded executor
             ;; refill immediately as a slot becomes free. Both resource pools
             ;; are fully drained before any exclusive work begins.
             parallel-results
             (-> {}
                 (collect-submitted normal-submitted)
                 (collect-submitted memory-submitted))
             results-by-key
             (reduce
              (fn [results job]
                (assoc results
                       (namespace-key (:namespace job))
                       (invoke-worker worker job)))
              parallel-results
              (get-in schedule [:exclusive-phase :exclusive]))
             results
             (mapv
              #(get results-by-key (namespace-key (:namespace %)))
              (:jobs schedule))
             failures (vec (filter result-failure? results))]
         {:schema :gravity/sh01-parallel-test-report-v1
          :plan-schema (:plan-schema schedule)
          :plan-authority (:plan-authority schedule)
          :normal-parallelism (:normal-parallelism schedule)
          :memory-parallelism (:memory-parallelism schedule)
          :jobs (count results)
          :results results
          :reports results
          :failures failures
          :status (if (seq failures) :failed :passed)
          :ok? (empty? failures)
          :exit-code (if (seq failures) 1 0)})
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

(defn- stream-capture
  [stream]
  (let [result (promise)
        thread
        (Thread.
         (fn []
           (deliver
            result
            (try
              (with-open [reader (io/reader stream)]
                (slurp reader))
              (catch Throwable throwable
                (str throwable))))))]
    ;; Stream drainage must not keep a completed CLI JVM alive. Clojure futures
    ;; use a shared cached executor whose idle lifetime can add a minute to a
    ;; short validation run, so use one bounded daemon thread per stream.
    (.setDaemon thread true)
    (.setName thread "sh01-process-stream-capture")
    (.start thread)
    result))

(defn- process-result
  [process timeout-ms]
  (let [stdout-capture (stream-capture (.getInputStream ^Process process))
        stderr-capture (stream-capture (.getErrorStream ^Process process))
        completed?
        (if timeout-ms
          (.waitFor ^Process process (long timeout-ms) TimeUnit/MILLISECONDS)
          (do (.waitFor ^Process process) true))
        timed-out? (not completed?)]
    (when timed-out?
      (.destroyForcibly ^Process process)
      (.waitFor ^Process process))
    {:status (if timed-out?
               :timeout
               (if (zero? (.exitValue ^Process process)) :passed :failed))
     :exit-code (if timed-out? 124 (long (.exitValue ^Process process)))
     :stdout @stdout-capture
     :stderr @stderr-capture}))

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
           (process-result launched (:timeout-ms options))
           (if (map? launched)
             launched
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
                 :supported ["--slice SH-NN" "--changed"]})))
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
          (if (= :slice mode)
            (throw
             (ex-info "Only one planner selection mode may be supplied"
                      {:id "SH01-PARALLEL-USAGE" :arguments (vec arguments)}))
            (recur (subvec remaining 1)
                   (assoc request :changed-selection? true)
                   options
                   (if (= :iteration mode) :iteration :changed)
                   dry-run?))

          (= "--slice" argument)
          (let [slice (get remaining 1)]
            (when (or (nil? slice) (str/starts-with? slice "--"))
              (throw
               (ex-info "--slice requires SH-NN"
                        {:id "SH01-PARALLEL-USAGE"})))
            (when (#{:changed :iteration} mode)
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
            (when (= :slice mode)
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

          (or (= "--expand-dependants" argument)
              (= "--expand-dependents" argument))
          (recur (subvec remaining 1)
                 (assoc request :expand-dependants? true)
                 options mode dry-run?)

          (= "--no-expand-dependants" argument)
          (recur (subvec remaining 1)
                 (assoc request :expand-dependants? false)
                 options mode dry-run?)

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
        (assoc parsed :plan (planner/build-plan request))))))

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
             "sh01-parallel-test-runner --slice SH-NN|--changed [--iteration-slice SH-NN] [--dry-run] [--normal-parallelism N] [--memory-parallelism 1]"}]
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
