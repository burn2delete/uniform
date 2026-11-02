(ns gravity.self-hosting.execution-lanes
  "Concrete adapters for the two declarative verification lanes.

  The adapters deliberately sit below the unified execution plan.  They do
  not select routes, grant authority, or interpret a proof as a pass.  Fast
  work is admitted through the reviewed SH-01 broker and a bounded child
  process.  Fresh work is copied into an isolated temporary checkout and is
  delegated to the existing cache-free SH-07 authoritative runner.

  The process, broker, export, and receipt seams are dynamic so focused tests
  can exercise timeout, cleanup, mutation, and malformed-output paths without
  starting the broad SH-07 suite."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh01-host-resource-broker :as broker])
  (:import (java.io BufferedWriter ByteArrayOutputStream
                    InputStreamReader OutputStreamWriter PushbackReader StringReader)
           (java.math BigInteger)
           (java.lang Process ProcessHandle)
           (java.nio.charset StandardCharsets)
           (java.nio.file Files LinkOption Path Paths StandardOpenOption)
           (java.nio.file.attribute BasicFileAttributes)
           (java.nio.file.attribute FileAttribute)
           (java.util Comparator UUID)
           (java.util.concurrent ArrayBlockingQueue BlockingQueue TimeUnit)
           (java.util.concurrent.atomic AtomicLong)))

(def ^:private maximum-command-arguments 128)
(def ^:private maximum-output-bytes (* 256 1024))
(def ^:private process-cleanup-timeout-ms 2000)
(def ^:private output-drain-timeout-ms 1000)
(def ^:private copy-entry-limit 8192)
(def ^:private copy-total-bytes (* 64 1024 1024))

(defn- file-digest
  "Hash regular-file bytes without following links."
  [^Path path]
  (let [digest (java.security.MessageDigest/getInstance "SHA-256")
        buffer (byte-array 16384)]
    (with-open [input (Files/newByteChannel path
                                           (into-array java.nio.file.OpenOption
                                                       [StandardOpenOption/READ
                                                        LinkOption/NOFOLLOW_LINKS]))]
      (loop [total 0]
        (let [bb (java.nio.ByteBuffer/wrap buffer)
              n (.read input bb)]
          (when (pos? n)
            (when-not (<= n (- copy-total-bytes total))
              (throw (ex-info "File exceeds the authenticated closure byte bound"
                              {:id "UEP-FRESH-EXPORT-BOUND"
                               :path (str path)
                               :bytes-so-far total
                               :next-bytes n})))
            (.update digest buffer 0 n)
            (recur (+ total n))))))
    (format "%064x" (BigInteger. 1 (.digest digest)))))

(def ^:private warm-session-schema :gravity/exact-tree-warm-session-v1)
(def ^:private warm-worker-namespace
  "gravity.self-hosting.sh01-exact-tree-warm-worker")
(def ^:private warm-worker-max-requests 256)
(def ^:private warm-session-queue-capacity 4)
(def ^:private warm-maximum-request-line-length 65536)
(def ^:private warm-maximum-response-line-length
  ;; Two independently capped output fields are EDN strings. Reserve the
  ;; maximum six-character escape expansion per decoded character plus a
  ;; bounded envelope so a valid noisy worker response remains admissible.
  (+ (* 12 maximum-output-bytes) 65536))
(def ^:private warm-session-startup-timeout-ms 30000)
(def ^:private warm-session-max-count 4)
(def ^:private warm-ready-keys
  #{:schema :candidate-commit :candidate-tree :authority})
(def ^:private warm-response-keys
  #{:schema :authority :authoritative? :request-id :candidate-commit
    :candidate-tree :outcome :exit-code :elapsed-ns :request-count
    :stdout :stderr :output-truncated? :runner-result})
(def ^:private warm-cached-request-schema
  :gravity/exact-tree-warm-worker-cached-request-v1)
(def ^:private warm-cached-response-schema
  :gravity/exact-tree-warm-worker-cached-response-v1)
(def ^:private warm-cached-result-contract-id
  :gravity/sh07-cached-shard-result-v1)
(def ^:private warm-artifact-request-schema
  :gravity/exact-tree-warm-worker-artifact-request-v1)
(def ^:private warm-artifact-response-schema
  :gravity/exact-tree-warm-worker-artifact-response-v1)
(def ^:private warm-artifact-projection-schema
  :gravity/authenticated-artifact-fast-projection-v1)
(def ^:private warm-artifact-response-keys
  #{:schema :authority :authoritative? :request-id :candidate-commit
    :candidate-tree :lane :route-id :plan-id :catalog-id :closure-identity
    :binding-id :row-id :stage :outcome :exit-code :elapsed-ns
    :request-count :projection :stdout :stderr :output-truncated?})
(def ^:private warm-artifact-projection-semantic-keys
  [:schema :binding-id :pipeline :result-id :requested-stage
   :selected-artifact-id :stage-artifact-ids :source-content-hash
   :source-provenance-id :source-provenance :execution-boundary :authority :mutation-detected?
   :provenance-checked? :source-authenticated? :closure-verified?
   :fresh-replayable? :diagnostics])
(def ^:private artifact-row-id-max-length 256)
(def ^:private artifact-stages
  #{:source :reader :syntax :macroexpansion :resolution :checked-core})
(def ^:private warm-artifact-authority-keys
  #{:authoritative? :ceiling :development-only? :nonclaims})
(def ^:private warm-artifact-boundary-keys
  #{:mode :isolation :isolated? :acceptance-authority?
    :fresh-authoritative-run-required?})
(def ^:private warm-artifact-provenance-keys
  #{:actual-path :project-root-path :project-relative-path
    :byte-count :content-hash})
(def ^:private warm-artifact-diagnostic-keys #{:id :message})
(def artifact-reference-schema
  :gravity/authenticated-artifact-result-reference-v1)
(def ^:private artifact-reference-keys
  #{:schema :reference-id :operation :base-plan-id :binding-id :row-id :stage
    :projection-id :result-id :selected-artifact-id
    :source-content-hash :source-provenance-id :source-relative-path
    :source-provenance :cache-decision :execution-boundary :authority
    :status})
(def ^:private artifact-reference-id-keys
  [:schema :operation :base-plan-id :binding-id :row-id :stage
   :projection-id :result-id :selected-artifact-id
   :source-content-hash :source-provenance-id :source-relative-path
   :source-provenance :cache-decision :execution-boundary :authority
   :status])
(def ^:private artifact-reference-id-pattern #"sha256:[0-9a-f]{64}")
(def ^:private artifact-reference-max-text-length 4096)
(def ^:private warm-cached-response-keys
  #{:schema :authority :authoritative? :request-id :candidate-commit
    :candidate-tree :lane :route-id :plan-id :catalog-id :closure-identity
    :result-contract-id :shard :outcome :exit-code :elapsed-ns
    :request-count :runner-result :stdout :stderr :output-truncated?})
(def ^:private warm-cached-result-keys
  #{:test :pass :fail :error :artifact :shard :test-vars
    :cache-authoritative? :fresh-authoritative-run-required?
    :cache-context :elapsed-ms :cache})
(def ^:private warm-cached-context-keys
  #{:request-schema-version :adapter :checked-core-source-content-hash
    :checked-core-plan-semantic-hash :java-runtime-version :clojure-version})
(def ^:private warm-cached-counter-keys
  #{:sh06-hits :sh06-misses :core-hits :core-misses
    :verification-hits :verification-misses})
(def ^:private warm-cached-observation-schema
  :gravity/unified-cache-observation-v1)
(def ^:private warm-cached-result-counter-keys
  #{:test :pass :fail :error})
(def ^:private warm-cached-request-keys
  #{:schema :request-id :operation :candidate-commit :candidate-tree
    :lane :route-id :plan-id :catalog-id :closure-identity
    :result-contract-id :shard})
(def ^:private warm-cached-max-count 1000000)
(def ^:private warm-cached-max-elapsed-ms (* 24 60 60 1000))
(def ^:private warm-cached-max-elapsed-ns
  (* warm-cached-max-elapsed-ms 1000000))
(def ^:private warm-outcomes
  #{:passed :failed :tree-drift :catalog-drift :shutdown :rejected})
(def ^:private process-result-keys
  #{:exit-code :stdout :stderr :stdout-truncated? :stderr-truncated?
    :timed-out? :cleanup})

(def ^:dynamic *coordination-root*
  "Explicit SH-01 root for the fast lane; nil means admission is blocked."
  (System/getenv "GRAVITY_SH01_COORDINATION_ROOT"))

(def ^:dynamic *broker-acquire!*
  "Injected broker acquire seam."
  broker/acquire!)

(def ^:dynamic *broker-release!*
  "Injected broker release seam."
  broker/release!)

(def ^:dynamic *process-runner*
  "Injected process-supervisor seam.

  It receives a map containing :command, :directory, :environment,
  :timeout-ms, and :max-output-bytes and returns a map with :exit-code,
  :stdout, :stderr, :timed-out?, and :cleanup."
  nil)

(def ^:dynamic *warm-session-start!*
  "Focused-test seam for starting a duplex warm worker.

  The function receives a closed map containing the canonical repository root,
  exact commit/tree, static worker argv and environment. It must return a map
  with `:read-line!`, `:write-line!`, and `:close!` functions plus an optional
  `:process-alive?` predicate. The seam is intentionally private to the
  development lane and never used by fresh acceptance."
  nil)

(def ^:dynamic *fresh-exporter*
  "Injected fresh-export seam.  Returns {:directory Path :cleanup fn}."
  nil)

(def ^:dynamic *fresh-receipt-writer*
  "Injected write-once receipt seam.  It is called only after a valid fresh
  child result and must return a diagnostic-safe map."
  nil)

(def ^:dynamic *closure-snapshot*
  "Optional focused-test seam for the reviewed pre/post closure snapshot."
  nil)

(def ^:dynamic *repository-identity*
  "Optional focused-test seam for an exact clean repository identity.

  Production leaves this nil so `git` observes the canonical root directly;
  tests may provide a fixed identity while constructing private duplex
  transports in an intentionally dirty candidate worktree."
  nil)

(defonce ^:private warm-sessions (atom {}))
(defn- fail!
  [id message data]
  (throw (ex-info message (assoc data :id id))))

(defn- ensure!
  [value id message data]
  (when-not value (fail! id message data))
  value)

(defn- repository-root
  []
  (loop [path (.toPath (io/file (System/getProperty "user.dir")))]
    (if (Files/isRegularFile (.resolve path "deps.edn")
                             (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
      (.toRealPath path (make-array LinkOption 0))
      (if-let [parent (.getParent path)]
        (recur parent)
        (fail! "UEP-LANE-REPOSITORY" "Could not locate repository root" {})))))

(defn- bounded-int
  [value id path maximum]
  (ensure! (and (integer? value) (pos? (long value))
                (<= (long value) (long maximum)))
           id "Execution bound is outside its reviewed range"
           {:path path :value value :maximum maximum})
  (long value))

(defn- plan-selection
  [plan-value]
  (let [selection (:selection plan-value)]
    (ensure! (map? selection) "UEP-LANE-SELECTION"
             "Execution plan selection is absent" {})
    (let [namespace (:namespace selection)
          selectors (:selectors selection)]
      (ensure! (and (string? namespace) (not (str/blank? namespace)))
               "UEP-LANE-SELECTION" "Execution plan namespace is invalid"
               {:namespace namespace})
      (ensure! (and (vector? selectors) (seq selectors)
                    (every? #(and (string? %) (not (str/blank? %))) selectors)
                    (= (count selectors) (count (distinct selectors))))
               "UEP-LANE-SELECTION" "Execution plan selectors are invalid"
               {:selectors selectors})
      {:namespace namespace :selectors selectors})))

(defn- validate-plan-boundary!
  [plan-value]
  (if-let [validator (try (requiring-resolve
                           'gravity.self-hosting.unified-execution-plan/validate-plan)
                          (catch Throwable _ nil))]
    ((var-get validator) plan-value)
    (fail! "UEP-LANE-PLAN" "Unified execution-plan validator is unavailable" {})))

(defn- control-cause
  [throwable predicate]
  (loop [cause throwable]
    (cond
      (nil? cause) nil
      (predicate cause) cause
      :else (recur (.getCause ^Throwable cause)))))

(defn- rethrow-lane-control!
  [throwable]
  (when-let [interrupted (control-cause throwable #(instance? InterruptedException %))]
    (.interrupt (Thread/currentThread))
    (throw interrupted))
  (when-let [fatal (control-cause throwable #(or (instance? ThreadDeath %)
                                                (instance? VirtualMachineError %)
                                                (instance? LinkageError %)))]
    (throw fatal)))

(defn- canonical-plan-for
  "Resolve the reviewed route/lane plan without making generic warm startup
  depend on the UEP namespace.  A cached request is admitted only when this
  freshly constructed plan is exactly equal to the caller's plan."
  [plan-value]
  (when (and (= :fast (:lane plan-value))
             (= :eligible (get-in plan-value [:cache-policy :mode]))
             (= :cached-shard
                (get-in plan-value [:executor :request :operation])))
    (try
      (let [plan-fn (requiring-resolve
                     'gravity.self-hosting.unified-execution-plan/plan)
            validator (requiring-resolve
                       'gravity.self-hosting.unified-execution-plan/validate-plan)
            canonical ((var-get plan-fn)
                       {:route-id (:route-id plan-value) :lane :fast})]
        ((var-get validator) canonical)
        canonical)
      (catch InterruptedException error
        (.interrupt (Thread/currentThread))
        (throw error))
      (catch VirtualMachineError error (throw error))
      (catch ThreadDeath error (throw error))
      (catch LinkageError error (throw error))
      (catch java.lang.Exception error
        (rethrow-lane-control! error)
        (fail! "UEP-WARM-CACHED-PLAN"
               "Canonical cached plan reconstruction failed"
               {:cause-class (.getName (class error))})))))

(defn- cached-plan-admitted?
  "Return true only for the exact eligible canonical cached plan.

  The executor operation is the sole dispatch discriminator.  Route names,
  shard prefixes, and independent shard inventories never select this path;
  a cache-disabled or otherwise tightened plan therefore remains generic
  exact execution even when its route declares a cached executor request."
  [plan-value]
  (when-let [canonical (canonical-plan-for plan-value)]
    (= canonical plan-value)))

(defn- explicit-resource-context
  "Read the legacy dynamic context only to keep old callers fail-closed.

  A valid context is not an authority grant; production callers should use
  the broker path below.  Invalid or arbitrary values return a stable blocked
  result instead of becoming an implicit scheduling bypass."
  []
  (when-let [resolve-var (try (requiring-resolve
                               'gravity.self-hosting.unified-execution-plan/*resource-lease*)
                              (catch Throwable _ nil))]
    (var-get resolve-var)))

(defn- valid-explicit-context?
  [value]
  (and (map? value)
       (= true (:bounded-executor? value))
       (= true (:timeout-enforced? value))
       (integer? (:deadline-ns value))
       (> (long (:deadline-ns value)) (System/nanoTime))))

(defn- valid-broker-lease?
  [lease resource-class root]
  (try
    (identical? lease (broker/validate-live-lease! lease root resource-class))
    (catch Throwable _ false)))

(defn- blocked
  [id message]
  {:status :blocked
   :diagnostics [{:id id :message message}]
   :evidence []
   :mutation-detected? false
   :fresh-replayable? false
   :provenance-checked? false
   :source-authenticated? false
   :closure-verified? false
   :cache-decision :disabled})

(defn- failed
  [id message]
  {:status :failed
   :diagnostics [{:id id :message message}]
   :evidence []
   :mutation-detected? false
   :fresh-replayable? false
   :provenance-checked? false
   :source-authenticated? false
   :closure-verified? false
   :cache-decision :forbidden})

(defn- stream-result
  [stream limit]
  (with-open [stream stream]
    (let [buffer (byte-array 16384)
          output (ByteArrayOutputStream.)]
      (loop [total 0 truncated? false]
        (let [read (.read stream buffer)]
          (if (= -1 read)
            {:text (String. (.toByteArray output) StandardCharsets/UTF_8)
             :truncated? truncated?}
            (let [remaining (max 0 (- limit total))
                  write-count (int (min remaining read))]
              (when (pos? write-count)
                (.write output buffer 0 write-count))
              (recur (+ total read)
                     (or truncated? (< write-count read))))))))))

(defn- current-descendants
  [^ProcessHandle root]
  (try
    (with-open [stream (.descendants root)]
      (vec (iterator-seq (.iterator stream))))
    (catch Throwable _ [])))

(defn- await-exit
  [^ProcessHandle handle timeout-ms]
  (try
    (.get (.onExit handle) (long timeout-ms) TimeUnit/MILLISECONDS)
    true
    (catch Throwable _ false)))

(defn- stop-process-tree!
  [^Process process observed]
  (let [root (when process (.toHandle process))
        ;; Refresh descendants at teardown; the monitor may have missed a
        ;; short-lived child between polls.
        observed (into observed (when root (current-descendants root)))
        handles (->> (concat observed (when root [root]))
                     (remove nil?)
                     (reduce (fn [result ^ProcessHandle handle]
                               (assoc result (.pid handle) handle)) {})
                     vals
                     vec)]
    (doseq [^ProcessHandle handle handles :when (.isAlive handle)]
      (.destroy handle))
    (doseq [^ProcessHandle handle handles :when (.isAlive handle)]
      (await-exit handle 250))
    (doseq [^ProcessHandle handle handles :when (.isAlive handle)]
      (.destroyForcibly handle))
    (doseq [^ProcessHandle handle handles :when (.isAlive handle)]
      (await-exit handle process-cleanup-timeout-ms))
    {:observed-count (count handles)
     :survivors (vec (for [^ProcessHandle handle handles
                           :when (.isAlive handle)]
                       (.pid handle)))
     :complete? (not-any? #(.isAlive ^ProcessHandle %) handles)}))

(defn- close-quietly!
  [value]
  (try (when value (.close value)) (catch Throwable _ nil)))

(defn- run-process-default
  [{:keys [command directory environment timeout-ms max-output-bytes]}]
  (ensure! (and (vector? command) (seq command)
                (every? string? command)
                (<= (count command) maximum-command-arguments))
           "UEP-PROCESS-COMMAND" "Process command is not a bounded argv vector"
           {:command command})
  (let [builder (doto (ProcessBuilder. ^java.util.List command)
                  (.directory (.toFile ^Path directory))
                  (.redirectErrorStream false))]
    ;; A caller supplies the complete environment.  Keeping this explicit
    ;; prevents ambient cache/resume flags from silently crossing the lane.
    (let [env (.environment builder)]
      (.clear env)
      (doseq [[name value] environment]
        (when (and (string? name) (string? value))
          (.put env name value))))
    (let [process (.start builder)
          observed (atom #{(.toHandle process)})
          monitor (future
                    (try
                      (while (.isAlive process)
                        (swap! observed into (current-descendants (.toHandle process)))
                        (Thread/sleep 5))
                      (catch InterruptedException _ nil)
                      (catch Throwable _ nil)))
          stdout-reader (future (stream-result (.getInputStream process)
                                               (long max-output-bytes)))
          stderr-reader (future (stream-result (.getErrorStream process)
                                               (long max-output-bytes)))
          finished? (atom false)
          timed-out? (atom false)]
      (try
        (reset! finished? (.waitFor process (long timeout-ms) TimeUnit/MILLISECONDS))
        (reset! timed-out? (not @finished?))
        (when @timed-out?
          (close-quietly! (.getOutputStream process)))
        (let [cleanup (stop-process-tree! process @observed)
              stdout (deref stdout-reader output-drain-timeout-ms
                            {:text "" :truncated? true})
              stderr (deref stderr-reader output-drain-timeout-ms
                            {:text "" :truncated? true})]
          {:exit-code (if @timed-out? 124 (.exitValue process))
           :stdout (:text stdout)
           :stderr (:text stderr)
           :stdout-truncated? (:truncated? stdout)
           :stderr-truncated? (:truncated? stderr)
           :timed-out? @timed-out?
           :cleanup cleanup})
        (catch InterruptedException error
          ;; Always terminate descendants, then restore interruption status so
          ;; callers cannot accidentally continue as if the run completed.
          (stop-process-tree! process @observed)
          (Thread/currentThread)
          (.interrupt (Thread/currentThread))
          (throw error))
        (finally
          (future-cancel monitor)
          (future-cancel stdout-reader)
          (future-cancel stderr-reader)
          (close-quietly! (.getInputStream process))
          (close-quietly! (.getErrorStream process))
          (close-quietly! (.getOutputStream process)))))))

(defn- parse-one-edn
  [text]
  (try
    (with-open [reader (PushbackReader. (StringReader. (str text)))]
      (let [eof (Object.)
            value (edn/read {:eof eof} reader)
            trailing (edn/read {:eof eof} reader)]
        (ensure! (and (not (identical? eof value))
                      (identical? eof trailing))
                 "UEP-LANE-MALFORMED-OUTPUT"
                 "Fresh child output must contain exactly one EDN value" {})
        value))
    (catch clojure.lang.ExceptionInfo error
      (throw error))
    (catch Throwable error
      (fail! "UEP-LANE-MALFORMED-OUTPUT"
             "Fresh child output is not valid EDN"
             {:class (.getName (class error))}))))

(defn- result-evidence
  [plan-value]
  (vec (distinct (concat (:required-evidence plan-value)
                         [:fresh-replayable :cache-disabled :provenance]))))

(defn- fresh-command
  [{:keys [namespace selectors]}]
  ;; For the authoritative route selectors name the reviewed module catalog
  ;; entry (for example c6-core), while the namespace names the existing
  ;; proof runner.  No route-id dispatch is involved.
  (ensure! (= 1 (count selectors)) "UEP-FRESH-SELECTION"
           "Fresh authoritative execution requires exactly one module selector"
           {:selectors selectors})
  ["clojure" "-Srepro" "-Sforce" "-Sdeps"
   "{:paths [\"bootstrap/clojure/src\" \"bootstrap/clojure/test\"]}"
   "-M" "-m" namespace "--fresh" (first selectors)])

(defn- environment-for
  [lane plan-value export-directory]
  ;; PATH and the JVM home are launch inputs, not verification inputs; retain
  ;; only the host values needed to resolve the Clojure executable.  Cache and
  ;; resume controls are explicit and lane-owned below.
  (cond-> {"GRAVITY_UNIFIED_LANE" (name lane)
           "GRAVITY_UNIFIED_CACHE" (if (= :fresh lane) "forbidden" "eligible")
           "GRAVITY_UNIFIED_NO_RESUME" "1"
   "GRAVITY_UNIFIED_EXPORT_ROOT" (str export-directory)
   "GRAVITY_UNIFIED_PLAN_ID" (str (:plan-id plan-value))
   "GRAVITY_UNIFIED_SELECTION" (pr-str (:selection plan-value))}
    (System/getenv "PATH") (assoc "PATH" (System/getenv "PATH"))
    (System/getenv "HOME") (assoc "HOME" (System/getenv "HOME"))
    (System/getenv "JAVA_HOME") (assoc "JAVA_HOME" (System/getenv "JAVA_HOME"))))

(defn- invoke-process
  [request]
  (if *process-runner*
    (*process-runner* request)
    (run-process-default request)))

(def ^:private warm-oid-pattern #"[0-9a-f]{40,64}")

(defn- sha256-id?
  [value]
  (and (string? value)
       (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn- bounded-command-text
  "Run one fixed, bounded argv command and return UTF-8 stdout.

  This helper is used only to observe the repository identity before creating
  a warm session.  It never executes a test or accepts shell interpolation."
  [^Path directory args timeout-ms]
  (ensure! (and (vector? args) (seq args)
                (every? #(and (string? %) (<= 1 (count %) 1024)) args)
                (<= (count args) maximum-command-arguments))
           "UEP-WARM-GIT-COMMAND"
           "Warm-session identity command is not a bounded argv vector"
           {:args args})
  (let [process (doto (ProcessBuilder. ^java.util.List args)
                  (.directory (.toFile directory))
                  (.redirectErrorStream true))
        environment (.environment process)]
    (.clear environment)
    (when-let [path (System/getenv "PATH")]
      (.put environment "PATH" path))
    (let [child (.start process)
          observed (atom #{(.toHandle child)})
          monitor (future
                    (try
                      (while (.isAlive child)
                        (swap! observed into
                               (current-descendants (.toHandle child)))
                        (Thread/sleep 5))
                      (catch InterruptedException _ nil)
                      (catch Throwable _ nil)))
          output (future (stream-result (.getInputStream child)
                                        (long maximum-output-bytes)))]
      (try
        (if (.waitFor child (long timeout-ms) TimeUnit/MILLISECONDS)
          (let [value (deref output output-drain-timeout-ms
                             {:text "" :truncated? true})]
            (ensure! (and (zero? (.exitValue child))
                          (false? (:truncated? value)))
                     "UEP-WARM-GIT-IDENTITY"
                     "Repository identity observation failed"
                     {:args args :exit-code (.exitValue child)})
            (:text value))
          (do
            (stop-process-tree! child @observed)
            (fail! "UEP-WARM-GIT-TIMEOUT"
                   "Repository identity observation exceeded its bound"
                   {:args args :timeout-ms timeout-ms})))
        (finally
          (future-cancel monitor)
          (future-cancel output)
          (close-quietly! (.getInputStream child)))))))

(defn- repository-identity
  "Observe the exact clean HEAD identities required by SH-01 warm workers."
  [^Path root]
  (let [commit (str/trim
                (bounded-command-text root ["git" "rev-parse" "--verify" "HEAD"]
                                       10000))
        tree (str/trim
              (bounded-command-text root ["git" "rev-parse" "--verify" "HEAD^{tree}"]
                                     10000))
        status (str/trim
                (bounded-command-text root
                                       ["git" "status" "--porcelain=v1"
                                        "--untracked-files=all" "--no-renames"]
                                       10000))]
    (ensure! (and (re-matches warm-oid-pattern commit)
                  (re-matches warm-oid-pattern tree)
                  (str/blank? status))
             "UEP-WARM-REPOSITORY-DIRTY"
             "Fast warm execution requires an exact clean repository identity"
             {:candidate-commit commit :candidate-tree tree
              :status-present? (not (str/blank? status))})
    {:candidate-commit commit :candidate-tree tree :clean? true}))

(defn- validate-repository-identity!
  [identity]
  (ensure! (and (map? identity)
                (= #{:candidate-commit :candidate-tree :clean?}
                   (set (keys identity)))
                (re-matches warm-oid-pattern (str (:candidate-commit identity)))
                (re-matches warm-oid-pattern (str (:candidate-tree identity)))
                (true? (:clean? identity)))
           "UEP-WARM-REPOSITORY-IDENTITY"
           "Warm session repository identity is not exact and clean"
           {:identity identity})
  identity)

(defn- exact-repository-identity
  [^Path root]
  (validate-repository-identity!
   (if *repository-identity*
     (*repository-identity* root)
     (repository-identity root))))

(defn- warm-runtime-identity
  []
  {:clojure-version (str (clojure-version))
   :java-version (str (System/getProperty "java.version" ""))
   :java-vendor (str (System/getProperty "java.vendor" ""))
   :vm-name (str (System/getProperty "java.vm.name" ""))
   :os-name (str (System/getProperty "os.name" ""))
   :os-arch (str (System/getProperty "os.arch" ""))})

(defn- warm-worker-command
  [{:keys [candidate-commit candidate-tree]}]
  ;; This argv is static for one identity.  Selectors are sent as data over
  ;; the request channel and never interpolated into a child expression.
  ["clojure" "-Srepro" "-Sforce" "-Sdeps"
   "{:paths [\"bootstrap/clojure/src\" \"bootstrap/clojure/test\"]}"
   "-M" "-m" warm-worker-namespace
   "--expected-commit" candidate-commit
   "--expected-tree" candidate-tree
   "--max-requests" (str warm-worker-max-requests)])

(defn- warm-tool-identity
  []
  {:worker-namespace warm-worker-namespace
   :worker-request-schema "gravity/exact-tree-warm-worker-request-v1"
   :worker-response-schema "gravity/exact-tree-warm-worker-response-v2"
   :worker-ready-schema "gravity/exact-tree-warm-worker-ready-v1"
   :max-requests warm-worker-max-requests
   :argv-shape [:clojure :srepro :sforce :sdeps :paths :worker-namespace
                :expected-commit :expected-tree :max-requests]})

(defn- warm-session-key
  [^Path root coordination-root repository plan-value resource-class timeout-ms]
  (let [identity
        {:schema warm-session-schema
         :repository-root (str root)
         :coordination-root (str coordination-root)
         :candidate-commit (:candidate-commit repository)
         :candidate-tree (:candidate-tree repository)
         :route-id (:route-id plan-value)
         :plan-id (:plan-id plan-value)
         :catalog-id (:catalog-id plan-value)
         :closure-identity (:closure-identity plan-value)
         :executor-request (get-in plan-value [:executor :request])
         :executor-operation (get-in plan-value [:executor :request :operation])
         :result-contract-id
         (get-in plan-value [:executor :request :result-contract-id])
         :resource-class resource-class
         :request-timeout-ms timeout-ms
         :runtime-identity (warm-runtime-identity)
         :tool-identity (warm-tool-identity)}]
    {:identity identity
     :identity-id (bootstrap/reader-canonical-hash identity)}))

(defn- warm-environment
  [plan-value]
  ;; Environment is fixed when a session starts.  Runtime cache controls are
  ;; explicit and no ambient resume state crosses the warm-worker boundary.
  (cond-> {"GRAVITY_UNIFIED_LANE" "fast"
           "GRAVITY_UNIFIED_CACHE"
           (name (get-in plan-value [:cache-policy :mode]))
           "GRAVITY_UNIFIED_NO_RESUME" "1"
           "GRAVITY_UNIFIED_CLOSURE_ID" (str (:closure-identity plan-value))}
    (System/getenv "PATH") (assoc "PATH" (System/getenv "PATH"))
    (System/getenv "HOME") (assoc "HOME" (System/getenv "HOME"))
    (System/getenv "JAVA_HOME") (assoc "JAVA_HOME" (System/getenv "JAVA_HOME"))))

(defn- read-bounded-line!
  [^InputStreamReader reader maximum-length]
  (let [builder (StringBuilder.)]
    (loop []
      (let [value (.read reader)]
        (cond
          (= -1 value) (when (pos? (.length builder)) (.toString builder))
          (= (int \newline) value) (.toString builder)
          (= (int \return) value) (recur)
          :else
          (do
            (ensure! (< (.length builder) maximum-length)
                     "UEP-WARM-RESPONSE-BOUNDS"
                     "Warm response line exceeds its bound"
                     {:maximum-length maximum-length})
            (.append builder (char value))
            (recur)))))))

(defn- process-transport
  "Create the default duplex transport for one static warm worker argv."
  [{:keys [command directory environment]}]
  (ensure! (and (vector? command) (seq command)
                (every? string? command))
           "UEP-WARM-COMMAND" "Warm worker argv is malformed" {:command command})
  (let [builder (doto (ProcessBuilder. ^java.util.List command)
                  (.directory (.toFile ^Path directory))
                  (.redirectErrorStream false))
        env (.environment builder)]
    (.clear env)
    (doseq [[name value] environment]
      (when (and (string? name) (string? value))
        (.put env name value)))
    (let [process (.start builder)
          observed (atom #{(.toHandle process)})
          monitor (future
                    (try
                      (while (.isAlive process)
                        (swap! observed into
                               (current-descendants (.toHandle process)))
                        (Thread/sleep 5))
                      (catch InterruptedException _ nil)
                      (catch Throwable _ nil)))
          reader (InputStreamReader. (.getInputStream process)
                                     StandardCharsets/UTF_8)
          writer (BufferedWriter.
                  (OutputStreamWriter. (.getOutputStream process)
                                       StandardCharsets/UTF_8))
          stderr (future (stream-result (.getErrorStream process)
                                        (long maximum-output-bytes)))
          closed? (atom false)]
      {:read-line! (fn []
                     (read-bounded-line! reader
                                         warm-maximum-response-line-length))
       :write-line! (fn [line]
                      (ensure! (and (string? line)
                                    (<= (count line)
                                        warm-maximum-request-line-length))
                               "UEP-WARM-REQUEST-BOUNDS"
                               "Warm request line exceeds its bound" {})
                      (.write writer line)
                      (.newLine writer)
                      (.flush writer))
       :process-alive? (fn [] (.isAlive process))
       :close! (fn []
                 (when (compare-and-set! closed? false true)
                   (close-quietly! writer)
                   (close-quietly! reader)
                   (close-quietly! (.getErrorStream process))
                   (stop-process-tree! process @observed)
                   (future-cancel monitor)
                   (future-cancel stderr)))
       :stderr stderr
       :process process
       :observed observed})))

(defn- transport-shape!
  [transport]
  (ensure! (and (map? transport)
                (every? #(contains? transport %)
                        [:read-line! :write-line! :close! :process-alive?])
                (ifn? (:read-line! transport))
                (ifn? (:write-line! transport))
                (ifn? (:close! transport))
                (ifn? (:process-alive? transport)))
           "UEP-WARM-TRANSPORT"
           "Warm worker transport has an invalid closed shape"
           {})
  transport)

(defn- timed-call
  [operation timeout-ms id]
  (let [sentinel (Object.)
        task (future (operation))
        value (deref task (long timeout-ms) sentinel)]
    (if (identical? sentinel value)
      (do
        (future-cancel task)
        (fail! id "Warm worker operation exceeded its bound"
               {:timeout-ms timeout-ms}))
      value)))

(defn- validate-warm-ready!
  [ready expected]
  (ensure! (and (map? ready) (= warm-ready-keys (set (keys ready)))
                (= :gravity/exact-tree-warm-worker-ready-v1 (:schema ready))
                (= :non-authoritative (:authority ready))
                (= (:candidate-commit expected) (:candidate-commit ready))
                (= (:candidate-tree expected) (:candidate-tree ready)))
           "UEP-WARM-READY"
           "Warm worker did not emit one authenticated ready record"
           {:ready ready})
  ready)

(defn- read-warm-record!
  ([transport timeout-ms id]
   (read-warm-record! transport timeout-ms id id))
  ([transport timeout-ms id timeout-id]
   (let [line (timed-call (:read-line! transport) timeout-ms timeout-id)]
     (ensure! (and (string? line)
                   (str/starts-with? line "WARM_DEV ")
                   (> (count line) (count "WARM_DEV ")))
              id "Warm worker output was not one WARM_DEV record"
              {:line line})
     (parse-one-edn (subs line (count "WARM_DEV "))))))

(defn- validate-warm-runner-result!
  "Authenticate one non-authoritative result against the exact warm plan."
  [result selection]
  (let [actual-selection (:selection result)
        summary (:summary result)
        expected-vars (->> (:selectors selection)
                           (map #(if (str/includes? % "/") %
                                     (str (:namespace selection) "/" %)))
                           sort vec)
        expected-test-count (count expected-vars)
        valid-names? (fn [values]
                       (and (vector? values) (seq values) (<= (count values) 64)
                            (= (count values) (count (distinct values)))
                            (every? #(and (string? %) (not (str/blank? %))
                                          (<= (count %) 1024)) values)))
        valid-counts? (fn [value]
                        (and (integer? value) (not (neg? value))
                             (<= value 1048576)))]
    (ensure! (and (map? result)
                  (= #{:schema :authority :authoritative? :selection :summary
                       :outcome :exit-code} (set (keys result)))
                  (= :gravity/development-test-result-v1 (:schema result))
                  (= :non-authoritative (:authority result))
                  (false? (:authoritative? result))
                  (map? actual-selection)
                  (= #{:namespaces :test-vars :fail-fast?}
                     (set (keys actual-selection)))
                  (= [(:namespace selection)] (:namespaces actual-selection))
                  (= expected-vars (:test-vars actual-selection))
                  (true? (:fail-fast? actual-selection))
                  (valid-names? (:namespaces actual-selection))
                  (valid-names? (:test-vars actual-selection))
                  (map? summary)
                  (= #{:test :pass :fail :error} (set (keys summary)))
                  (every? #(valid-counts? (get summary %))
                          [:test :pass :fail :error])
                  (contains? #{0 1} (:exit-code result))
                  (= (:outcome result) ({0 :passed 1 :failed} (:exit-code result)))
                  (<= (:test summary) expected-test-count)
                  (or (and (= :passed (:outcome result))
                           (= (:test summary) expected-test-count)
                           (zero? (:fail summary)) (zero? (:error summary)))
                      (and (= :failed (:outcome result))
                           (or (< (:test summary) expected-test-count)
                               (pos? (+ (:fail summary) (:error summary)))))))
             "UEP-WARM-RUNNER-RESULT"
             "Warm worker result failed its exact closed-plan checks"
             {:selection selection :result result})
    result))

(defn- validate-warm-response!
  [response expected request-id expected-count selection]
  (ensure! (and (map? response)
                (= warm-response-keys (set (keys response)))
                (= :gravity/exact-tree-warm-worker-response-v2 (:schema response))
                (= :non-authoritative (:authority response))
                (false? (:authoritative? response))
                (= request-id (:request-id response))
                (= (:candidate-commit expected) (:candidate-commit response))
                (= (:candidate-tree expected) (:candidate-tree response))
                (contains? warm-outcomes (:outcome response))
                (integer? (:exit-code response))
                (integer? (:elapsed-ns response))
                (not (neg? (long (:elapsed-ns response))))
                (= expected-count (:request-count response))
                (string? (:stdout response))
                (string? (:stderr response))
                (<= (alength (.getBytes ^String (:stdout response)
                                        StandardCharsets/UTF_8))
                    maximum-output-bytes)
                (<= (alength (.getBytes ^String (:stderr response)
                                        StandardCharsets/UTF_8))
                    maximum-output-bytes)
                (boolean? (:output-truncated? response)))
           "UEP-WARM-RESPONSE"
           "Warm worker response failed its authenticated closed-shape checks"
           {:response response :request-id request-id})
  (if (= :shutdown (:outcome response))
    (ensure! (nil? (:runner-result response))
             "UEP-WARM-RESPONSE"
             "Shutdown response carried an execution result" {})
    (if (nil? (:runner-result response))
      (ensure! (and (= 2 (:exit-code response))
                    (contains? #{:failed :tree-drift :catalog-drift}
                               (:outcome response)))
               "UEP-WARM-RUNNER-RESULT"
               "Missing runner result is admissible only for an explicit failed response"
               {:response response})
      (do
        (validate-warm-runner-result! (:runner-result response) selection)
        (let [runner-exit (get-in response [:runner-result :exit-code])
              runner-outcome (get-in response [:runner-result :outcome])]
          (ensure! (if (contains? #{:tree-drift :catalog-drift} (:outcome response))
                      (= 2 (:exit-code response))
                      (and (= runner-outcome (:outcome response))
                           (= runner-exit (:exit-code response))))
                   "UEP-WARM-RUNNER-EXIT"
                   "Warm response exit code disagrees with runner result"
                   {:response response :runner-result (:runner-result response)})))))
  response)

(defn- warm-cached-context
  []
  {:request-schema-version 15
   :adapter bootstrap/sh07-core-adapter-contract
   :checked-core-source-content-hash
   bootstrap/sh07-core-expected-source-content-hash
   :checked-core-plan-semantic-hash
   bootstrap/sh07-core-expected-plan-semantic-hash
   :java-runtime-version (System/getProperty "java.runtime.version")
   :clojure-version (clojure-version)})

(defn- cached-bounded-count?
  [value]
  (and (integer? value)
       (<= 0 value warm-cached-max-count)))

(defn- cached-bounded-elapsed?
  [value]
  (and (integer? value)
       (<= 0 value warm-cached-max-elapsed-ms)))

(defn- validate-warm-cached-result!
  "Authenticate the raw cached-shard result before projecting it into a lane
  result.  This deliberately keeps the complete raw map intact for callers;
  no cache counter or context field is collapsed at this boundary."
  [result selection shard]
  (ensure! (and (map? result)
                (= warm-cached-result-keys (set (keys result))))
           "UEP-WARM-CACHED-RESULT"
           "Cached runner result failed its authenticated closed-shape checks"
           {:result result :shard shard})
  (doseq [field (sort-by name warm-cached-result-counter-keys)]
    (ensure! (cached-bounded-count? (get result field))
             "UEP-WARM-CACHED-COUNTER"
             "Cached runner result counter is outside its bound"
             {:field field :value (get result field) :shard shard}))
  (ensure! (and (pos? (long (:test result)))
                (= (:test-vars result) (count (:selectors selection)))
                (pos? (long (:test-vars result)))
                (<= (long (:test-vars result)) warm-cached-max-count))
           "UEP-WARM-CACHED-COUNTER"
           "Cached runner test-var count does not equal the requested selectors"
           {:test (:test result) :test-vars (:test-vars result)
            :expected (count (:selectors selection)) :shard shard})
  (ensure! (= :gravity/sh07-cached-shard-result (:artifact result))
           "UEP-WARM-CACHED-RESULT"
           "Cached runner artifact is unsupported"
           {:artifact (:artifact result) :shard shard})
  (ensure! (= shard (:shard result))
           "UEP-WARM-CACHED-RESULT-SHARD"
           "Cached runner result shard does not match the request"
           {:expected shard :actual (:shard result)})
  (ensure! (false? (:cache-authoritative? result))
           "UEP-WARM-CACHED-AUTHORITY"
           "Cached runner cannot provide authoritative evidence" {})
  (ensure! (true? (:fresh-authoritative-run-required? result))
           "UEP-WARM-CACHED-AUTHORITY"
           "Cached runner must require a fresh authoritative run" {})
  (let [context (:cache-context result)]
    (ensure! (and (map? context)
                  (= warm-cached-context-keys (set (keys context)))
                  (= (warm-cached-context) context))
             "UEP-WARM-CACHED-CONTEXT"
             "Cached result context does not match the authenticated runtime"
             {:context context :expected (warm-cached-context)}))
  (let [cache (:cache result)]
    (ensure! (and (map? cache)
                  (= warm-cached-counter-keys (set (keys cache)))
                  (every? #(cached-bounded-count? (get cache %))
                          warm-cached-counter-keys))
             "UEP-WARM-CACHED-CACHE"
             "Cached result counters are not a bounded closed map"
             {:cache cache}))
  (ensure! (cached-bounded-elapsed? (:elapsed-ms result))
           "UEP-WARM-CACHED-RESULT"
           "Cached runner elapsed time is outside its bound"
           {:elapsed-ms (:elapsed-ms result)})
  result)

(defn- warm-cached-observation
  "Project an authenticated raw cached result into the closed UEP result shape.

  The raw result remains in `:artifacts`; this map is intentionally a shallow
  observation carrying only the provenance and bounded counters needed by the
  coordinator.  It never conveys authority, and its counters/time are
  observational rather than receipt identity inputs."
  [plan-value result]
  {:schema warm-cached-observation-schema
   :result-contract-id
   (get-in plan-value [:executor :request :result-contract-id])
   :artifact (:artifact result)
   :shard (:shard result)
   :context (:cache-context result)
   :counters (:cache result)
   :elapsed-ms (:elapsed-ms result)
   :cache-authoritative? (:cache-authoritative? result)
   :fresh-authoritative-run-required?
   (:fresh-authoritative-run-required? result)})

(defn- validate-warm-cached-response!
  "Validate the operation-specific cached response and its raw result.

  This parent-side validator is intentionally separate from the generic
  response-v2 validator so exact/shutdown compatibility remains byte- and
  shape-stable."
  [response expected request-id expected-count selection plan-value]
  (let [executor-request (get-in plan-value [:executor :request])]
    (ensure! (and (map? response)
                  (= warm-cached-response-keys (set (keys response)))
                  (= warm-cached-response-schema (:schema response))
                  (= :non-authoritative (:authority response))
                  (false? (:authoritative? response))
                  (= request-id (:request-id response))
                  (= (:candidate-commit expected) (:candidate-commit response))
                  (= (:candidate-tree expected) (:candidate-tree response))
                  (= :fast (:lane response))
                  (= (:route-id plan-value) (:route-id response))
                  (= (:plan-id plan-value) (:plan-id response))
                  (= (:catalog-id plan-value) (:catalog-id response))
                  (= (:closure-identity plan-value)
                     (:closure-identity response))
                  (= (:result-contract-id executor-request)
                     (:result-contract-id response))
                  (= (:shard executor-request) (:shard response))
                  (= warm-cached-result-contract-id
                     (:result-contract-id response))
                  (contains? warm-outcomes (:outcome response))
                  (not= :shutdown (:outcome response))
                  (integer? (:exit-code response))
                  (contains? #{0 1 2} (:exit-code response))
                  (integer? (:elapsed-ns response))
                  (<= 0 (:elapsed-ns response) warm-cached-max-elapsed-ns)
                  (= expected-count (:request-count response))
                  (pos? (long (:request-count response)))
                  (<= (:request-count response) warm-worker-max-requests)
                  (string? (:stdout response))
                  (string? (:stderr response))
                  (<= (alength (.getBytes ^String (:stdout response)
                                          StandardCharsets/UTF_8))
                      maximum-output-bytes)
                  (<= (alength (.getBytes ^String (:stderr response)
                                          StandardCharsets/UTF_8))
                      maximum-output-bytes)
                  (false? (:output-truncated? response)))
             "UEP-WARM-CACHED-RESPONSE"
             "Cached worker response failed its authenticated closed-shape checks"
             {:response response :request-id request-id})
    (let [raw (:runner-result response)
          outcome (:outcome response)
          exit-code (:exit-code response)]
      ;; A cached response may carry a raw result for repository tree drift,
      ;; because the worker observed the drift after executing it. Catalog
      ;; identity drift and pre-execution failures must carry no raw evidence.
      (when (and (nil? raw) (= :passed outcome))
        (fail! "UEP-WARM-CACHED-RESULT"
               "A passed cached response must carry its raw result" {}))
      (when raw
        (validate-warm-cached-result! raw selection (:shard executor-request)))
      (cond
        (= :passed outcome)
        (ensure! (and raw (zero? exit-code)
                      (zero? (long (:fail raw)))
                      (zero? (long (:error raw))))
                 "UEP-WARM-CACHED-RESULT"
                 "Cached success does not match raw result counters"
                 {:response response :result raw})

        (= :failed outcome)
        (if raw
          (ensure! (and (= 1 exit-code)
                        (pos? (+ (long (:fail raw)) (long (:error raw)))))
                   "UEP-WARM-CACHED-RESULT"
                   "Cached failure does not match raw result counters"
                   {:response response :result raw})
          (ensure! (= 2 exit-code)
                   "UEP-WARM-CACHED-RESULT"
                   "A cached pre-execution failure must exit 2"
                   {:response response}))

        (contains? #{:tree-drift :catalog-drift} outcome)
        (ensure! (and (= 2 exit-code)
                      (or (= :tree-drift outcome) (nil? raw)))
                 "UEP-WARM-CACHED-RESULT"
                 "Cached drift response has an invalid raw-result shape"
                 {:response response})

        :else
        (fail! "UEP-WARM-CACHED-RESULT"
               "Cached response outcome is not admissible" {:response response})))
    response))

(defn- validate-warm-artifact-response!
  [response expected request-id expected-count plan-value artifact-request artifact-binding]
  (let [request artifact-request
        projection (:projection response)]
    (ensure! (and (map? response)
                  (= warm-artifact-response-keys (set (keys response)))
                  (= warm-artifact-response-schema (:schema response))
                  (= :non-authoritative (:authority response))
                  (false? (:authoritative? response))
                  (= request-id (:request-id response))
                  (= (:candidate-commit expected) (:candidate-commit response))
                  (= (:candidate-tree expected) (:candidate-tree response))
                  (= :fast (:lane response))
                  (= (:route-id plan-value) (:route-id response))
                  (= (:plan-id plan-value) (:plan-id response))
                  (= (:catalog-id plan-value) (:catalog-id response))
                  (= (:closure-identity plan-value) (:closure-identity response))
                  (= (:binding-id request) (:binding-id response))
                  (= (:row-id request) (:row-id response))
                  (= (:stage request) (:stage response))
                  (contains? warm-outcomes (:outcome response))
                  (not= :shutdown (:outcome response))
                  (integer? (:exit-code response))
                  (contains? #{0 1 2} (:exit-code response))
                  (integer? (:elapsed-ns response))
                  (<= 0 (:elapsed-ns response) warm-cached-max-elapsed-ns)
                  (= expected-count (:request-count response))
                  (pos? (long (:request-count response)))
                  (<= (:request-count response) warm-worker-max-requests)
                  (string? (:stdout response)) (string? (:stderr response))
                  (<= (alength (.getBytes ^String (:stdout response)
                                          StandardCharsets/UTF_8))
                      maximum-output-bytes)
                  (<= (alength (.getBytes ^String (:stderr response)
                                          StandardCharsets/UTF_8))
                      maximum-output-bytes)
                  (false? (:output-truncated? response)))
             "UEP-WARM-ARTIFACT-RESPONSE"
             "Artifact worker response failed its closed-shape checks"
             {:response response})
    (if (= :passed (:outcome response))
      (ensure! (and (= 0 (:exit-code response)) (map? projection)
                    (= warm-artifact-projection-schema (:schema projection))
                    (= #{:schema :binding-id :projection-id :pipeline :result-id :requested-stage
                         :selected-artifact-id :stage-artifact-ids :source-content-hash
                         :source-provenance-id :source-provenance :source-relative-path :cache-decision
                         :execution-boundary :authority :mutation-detected?
                         :provenance-checked? :source-authenticated? :closure-verified?
                         :fresh-replayable? :diagnostics}
                       (set (keys projection)))
                    (= :gravity/authenticated-compiler-artifact-pipeline-v1 (:pipeline projection))
                    (= (:binding-id request) (:binding-id projection))
                    (= (:fixture-sha256 artifact-binding) (:source-content-hash projection))
                    (= (:gravity-path artifact-binding) (:source-relative-path projection))
                    (= (:stage request) (:requested-stage projection))
                    (re-matches #"sha256:[0-9a-f]{64}" (:result-id projection))
                    (re-matches #"sha256:[0-9a-f]{64}" (:selected-artifact-id projection))
                    (re-matches #"sha256:[0-9a-f]{64}" (:projection-id projection))
                    (#{:hit :miss} (:cache-decision projection))
                    (re-matches #"sha256:[0-9a-f]{64}" (:source-provenance-id projection))
                    (= (:source-provenance-id projection)
                       (bootstrap/reader-canonical-hash
                        (:source-provenance projection)))
                    (= warm-artifact-authority-keys
                       (set (keys (:authority projection))))
                    (false? (get-in projection [:authority :authoritative?]))
                    (= :non-authoritative (get-in projection [:authority :ceiling]))
                    (true? (get-in projection [:authority :development-only?]))
                    (= (:nonclaims plan-value)
                       (get-in projection [:authority :nonclaims]))
                    (= warm-artifact-boundary-keys
                       (set (keys (:execution-boundary projection))))
                    (= :fast (get-in projection [:execution-boundary :mode]))
                    (= :same-jvm (get-in projection [:execution-boundary :isolation]))
                    (false? (get-in projection [:execution-boundary :isolated?]))
                    (false? (get-in projection [:execution-boundary :acceptance-authority?]))
                    (true? (get-in projection [:execution-boundary :fresh-authoritative-run-required?]))
                    (= warm-artifact-provenance-keys
                       (set (keys (:source-provenance projection))))
                    (= (:source-relative-path projection)
                       (get-in projection [:source-provenance :project-relative-path]))
                    (= (:source-content-hash projection)
                       (get-in projection [:source-provenance :content-hash]))
                    (= (:fixture-bytes artifact-binding)
                       (get-in projection [:source-provenance :byte-count]))
                    (integer? (get-in projection [:source-provenance :byte-count]))
                    (re-matches #"sha256:[0-9a-f]{64}"
                                (get-in projection [:source-provenance :content-hash]))
                    (= (:source-content-hash projection)
                       (get-in projection [:source-provenance :content-hash]))
                    (= (str (repository-root))
                       (get-in projection [:source-provenance :project-root-path]))
                    (= (str (.toRealPath
                             (.resolve (repository-root)
                                       ^String (:gravity-path artifact-binding))
                             (make-array LinkOption 0)))
                       (get-in projection [:source-provenance :actual-path]))
                    (true? (:provenance-checked? projection))
                    (true? (:source-authenticated? projection))
                    (true? (:closure-verified? projection))
                    (false? (:fresh-replayable? projection))
                    (vector? (:stage-artifact-ids projection))
                    (<= 1 (count (:stage-artifact-ids projection)) 6)
                    (vector? (:diagnostics projection))
                    (<= (count (:diagnostics projection)) 64)
                    (every? (fn [diagnostic]
                              (and (map? diagnostic)
                                   (= warm-artifact-diagnostic-keys
                                      (set (keys diagnostic)))
                                   (string? (:id diagnostic))
                                   (<= 1 (count (:id diagnostic)) 128)
                                   (string? (:message diagnostic))
                                   (<= 1 (count (:message diagnostic)) 4096)))
                            (:diagnostics projection))
                    (= (:projection-id projection)
                       (bootstrap/reader-canonical-hash
                        (select-keys projection warm-artifact-projection-semantic-keys))))
               "UEP-WARM-ARTIFACT-PROJECTION"
               "Passed artifact response lacks a projection"
               {:binding-id (:binding-id request)
                :request-id (:request-id response)})
      (ensure! (and (nil? projection) (= 2 (:exit-code response)))
               "UEP-WARM-ARTIFACT-RESULT"
               "Rejected artifact response must carry no projection and exit 2" {}))
    (when (and (= :passed (:outcome response)) (map? projection))
      (let [entries (:stage-artifact-ids projection)]
        (ensure! (and (vector? entries)
                      (every? (fn [entry]
                                (and (vector? entry) (= 3 (count entry))))
                              entries))
                 "UEP-WARM-ARTIFACT-PROJECTION"
                 "Artifact stage tuples are malformed" {})
        (let [stages (mapv first entries)
            ids (mapv second entries)
            expected (vec (take (count entries)
                                [:source :reader :syntax :macroexpansion
                                 :resolution :checked-core]))]
        (ensure! (= expected stages)
                 "UEP-WARM-ARTIFACT-PROJECTION" "Artifact stage prefix is invalid" {})
        (ensure! (= (:requested-stage projection) (last stages))
                 "UEP-WARM-ARTIFACT-PROJECTION" "Artifact stage does not terminate at request" {})
        (doseq [[index entry] (map-indexed vector entries)]
          (ensure! (and (= 3 (count entry))
                        (re-matches #"sha256:[0-9a-f]{64}" (second entry))
                        (every? #(re-matches #"sha256:[0-9a-f]{64}" %) (nth entry 2))
                        (= (vec (take index ids)) (nth entry 2)))
                   "UEP-WARM-ARTIFACT-PROJECTION"
                   "Artifact stage tuple or ancestry is invalid" {:index index})))
        (ensure! (= (:selected-artifact-id projection)
                    (second (last entries)))
                 "UEP-WARM-ARTIFACT-PROJECTION"
                 "Selected artifact does not match the requested stage" {})))
    response))

(defn- validate-warm-worker-response!
  [response expected request-id expected-count selection plan-value cached? artifact? artifact-request artifact-binding]
  (if artifact?
    (validate-warm-artifact-response! response expected request-id expected-count
                                      plan-value artifact-request artifact-binding)
    (if cached?
    (validate-warm-cached-response! response expected request-id expected-count
                                    selection plan-value)
    (validate-warm-response! response expected request-id expected-count selection))))

(defn- artifact-reference-id?
  [value]
  (and (string? value) (re-matches artifact-reference-id-pattern value)))

(defn artifact-reference-id
  "Return the canonical identity for a compact artifact result reference.

  The helper is intentionally small and public so the unified receipt engine
  can construct a closed failure reference without importing the bootstrap
  namespace itself.  Callers must still pass the exact pre-reference map;
  validation remains owned by `verify-artifact-reference`."
  [reference-without-id]
  (bootstrap/reader-canonical-hash reference-without-id))

(defn- artifact-reference
  "Project an already validated worker projection into a bounded receipt ref.

  `validate-warm-artifact-response!` has authenticated every field below.  We
  intentionally project only the compact lineage/provenance reference here;
  compiler payloads and the full worker envelope never enter the receipt."
  [plan-value artifact-request binding projection status]
  (let [bounded-text
        (fn [value path]
          (ensure! (and (string? value)
                        (not (str/blank? value))
                        (<= (count value) artifact-reference-max-text-length))
                   "UEP-ARTIFACT-REFERENCE-BOUND"
                   "Artifact receipt reference text exceeds its bound"
                   {:path path :maximum artifact-reference-max-text-length}))
        _ (ensure! (= (:binding-id binding) (:binding-id artifact-request))
                   "UEP-ARTIFACT-REFERENCE-BINDING"
                   "Artifact receipt binding is stale" {})
        _ (when (= :passed status)
            (doseq [field [:actual-path :project-root-path :project-relative-path]]
              (bounded-text (get-in projection [:source-provenance field])
                            [:source-provenance field])))
        base {:schema artifact-reference-schema
              :operation :run-fast-artifact
              :base-plan-id (:plan-id plan-value)
              :binding-id (:binding-id artifact-request)
              :row-id (:row-id artifact-request)
              :stage (:stage artifact-request)
              :projection-id (when (= :passed status)
                               (:projection-id projection))
              :result-id (when (= :passed status) (:result-id projection))
              :selected-artifact-id (when (= :passed status)
                                      (:selected-artifact-id projection))
              :source-content-hash (when (= :passed status)
                                     (:source-content-hash projection))
              :source-provenance-id (when (= :passed status)
                                      (:source-provenance-id projection))
              :source-relative-path (when (= :passed status)
                                      (:source-relative-path projection))
              :source-provenance (when (= :passed status)
                                   (:source-provenance projection))
              :cache-decision (or (:cache-decision projection)
                                  (when (= :passed status) :miss)
                                  :forbidden)
              :execution-boundary (when (= :passed status)
                                   (:execution-boundary projection))
              :authority (when (= :passed status) (:authority projection))
              :status status}]
    (ensure! (= (disj artifact-reference-keys :reference-id)
                (set (keys base)))
             "UEP-ARTIFACT-REFERENCE-BOUND"
             "Artifact receipt reference shape is not closed" {})
    (assoc base :reference-id (artifact-reference-id base))))

(defn verify-artifact-reference
  "Verify the compact reference structure and identity retained in a receipt.

  Full projection authentication occurs at the worker trust point above; this
  structural/identity verifier does not recreate the projection validator.
  Plan-bound receipt verification remains responsible for authenticated source
  binding."
  [reference]
  (let [sha? #(and (string? %) (re-matches #"sha256:[0-9a-f]{64}" %))
        bounded-text? #(and (string? %) (not (str/blank? %))
                            (<= (count %) artifact-reference-max-text-length))
        bounded-row? #(and (bounded-text? %)
                           (<= (count %) artifact-row-id-max-length))
        safe-relative? (fn [path]
                         (try
                           (let [parsed (Paths/get ^String path
                                                    (make-array String 0))]
                             (and (bounded-text? path)
                                  (not (.isAbsolute parsed))
                                  (= path (str (.normalize parsed)))
                                  (not (re-matches #"(?i)^[a-z]:.*" path))
                                  (not (str/includes? path "\\"))
                                  (not-any? #{"" "." ".."}
                                            (str/split path #"/"))))
                           (catch Throwable _ false)))
        status (:status reference)
        pass? (= :passed status)
        provenance (:source-provenance reference)
        boundary (:execution-boundary reference)
        authority (:authority reference)
        pass-fields [:projection-id :result-id :selected-artifact-id
                     :source-content-hash :source-provenance-id
                     :source-relative-path :source-provenance
                     :execution-boundary :authority]]
    (ensure! (and (map? reference)
                  (= artifact-reference-keys (set (keys reference)))
                  (= artifact-reference-schema (:schema reference))
                  (= :run-fast-artifact (:operation reference))
                  (artifact-reference-id? (:reference-id reference))
                  (sha? (:base-plan-id reference))
                  (sha? (:binding-id reference))
                  (bounded-row? (:row-id reference))
                  (keyword? (:stage reference))
                  (contains? artifact-stages (:stage reference))
                  (contains? #{:passed :failed :blocked} status)
                  (contains? #{:hit :miss :disabled :forbidden}
                             (:cache-decision reference))
                  (if pass?
                    (and (every? #(sha? (get reference %))
                                 [:projection-id :result-id :selected-artifact-id
                                  :source-content-hash :source-provenance-id])
                         (safe-relative? (:source-relative-path reference))
                         (= warm-artifact-provenance-keys (set (keys provenance)))
                         (every? bounded-text? (map provenance
                                                      [:actual-path
                                                       :project-root-path]))
                         (safe-relative? (:project-relative-path provenance))
                         (integer? (:byte-count provenance))
                         (<= 0 (:byte-count provenance) copy-total-bytes)
                         (sha? (:content-hash provenance))
                         (= (:source-relative-path reference)
                            (:project-relative-path provenance))
                         (= warm-artifact-boundary-keys (set (keys boundary)))
                         (= {:mode :fast :isolation :same-jvm
                             :isolated? false :acceptance-authority? false
                             :fresh-authoritative-run-required? true}
                            boundary)
                         (= warm-artifact-authority-keys (set (keys authority)))
                         (false? (:authoritative? authority))
                         (= :non-authoritative (:ceiling authority))
                         (true? (:development-only? authority))
                         (vector? (:nonclaims authority))
                         (<= (count (:nonclaims authority)) 64)
                         (every? keyword? (:nonclaims authority)))
                    (every? nil? (map #(get reference %) pass-fields))))
             "UEP-ARTIFACT-REFERENCE"
             "Artifact receipt reference is not a closed bounded value" {}))
  (ensure! (= (:reference-id reference)
              (artifact-reference-id
               (select-keys reference artifact-reference-id-keys)))
           "UEP-ARTIFACT-REFERENCE-ID"
           "Artifact receipt reference identity is stale or malformed" {})
  reference)

(defn- request-error
  [id message]
  (ex-info message {:id id}))

(defn- session-pending-failure!
  [session error]
  (loop []
    (when-let [task (.poll ^BlockingQueue (:queue session))]
      (deliver (:promise task) {:error error})
      (recur))))

(defn- close-warm-transport!
  [session]
  (when-let [transport @(:transport session)]
    (try ((:close! transport)) (catch Throwable _ nil))))

(defn- stop-warm-session!
  "Signal an owner thread to tear down a session.

  The owner thread, not the caller, performs broker release.  Closing the
  transport here only interrupts a blocked read so the owner can complete
  that linear release on its own thread."
  [session reason]
  (when (compare-and-set! (:stop? session) false true)
    (reset! (:stop-reason session) reason)
    (close-warm-transport! session)
    (when-let [^Thread owner @(:owner-thread session)]
      (when-not (= owner (Thread/currentThread))
        (.interrupt owner))))
  session)

(defn- warm-request-line
  [session request]
  ;; Generic requests historically omitted :schema at construction and got
  ;; it added here.  Keep that exact projection, while cached requests carry
  ;; their operation-specific schema and closed identity fields already.
  (if (= :run-exact (:operation request))
    (pr-str (assoc request
                  :schema :gravity/exact-tree-warm-worker-request-v1
                  :candidate-commit (:candidate-commit (:repository session))
                  :candidate-tree (:candidate-tree (:repository session))))
    (pr-str (assoc request
                  :candidate-commit (:candidate-commit (:repository session))
                  :candidate-tree (:candidate-tree (:repository session))))))

(defn- warm-request-result
  ([plan-value selection response stable?]
   (warm-request-result plan-value selection response stable? false))
  ([plan-value selection response stable? cached?]
   (let [runner-result (:runner-result response)
         summary (if cached?
                   (if (map? runner-result)
                     (select-keys runner-result [:test :pass :fail :error])
                     {:test 0 :pass 0 :fail 0 :error 1})
                   (or (:summary runner-result)
                       {:test 0 :pass 0 :fail 0 :error 1}))
         cached-pass? (and cached?
                           (map? runner-result)
                           (zero? (long (:fail runner-result)))
                           (zero? (long (:error runner-result))))
         passed? (if cached?
                   (and stable?
                        (= :passed (:outcome response))
                        cached-pass?
                        (zero? (long (:exit-code response)))
                        (false? (:output-truncated? response)))
                   (and stable?
                        (= :passed (:outcome response))
                        (= :passed (:outcome runner-result))
                        (zero? (long (:exit-code response)))
                        (false? (:output-truncated? response))))
         cache-counters (when (map? runner-result) (:cache runner-result))
         cache-hit? (and cached?
                         (map? cache-counters)
                         (some #(pos? (long (get cache-counters % 0)))
                               [:sh06-hits :core-hits :verification-hits]))]
     (cond->
       {:status (if passed? :passed :failed)
        ;; Cached :test-vars is authenticated to equal this selection count;
        ;; generic exact results retain their historical selector projection.
        :test-count (if cached?
                      (long (or (:test-vars runner-result)
                                (count (:selectors selection))))
                      (count (:selectors selection)))
        :pass (:pass summary)
        :fail (:fail summary)
        :error (:error summary)
        :diagnostics
        (cond-> []
          (not stable?)
          (conj {:id "UEP-SNAPSHOT-MUTATED"
                 :message "Dependency closure changed during warm execution"})
          (= :tree-drift (:outcome response))
          (conj {:id "UEP-WARM-TREE-DRIFT"
                 :message "Warm worker observed repository identity drift"})
          (= :catalog-drift (:outcome response))
          (conj {:id "UEP-WARM-CATALOG-DRIFT"
                 :message "Warm worker observed development catalog drift"})
          (and (not= :passed (:outcome response))
               (not (contains? #{:tree-drift :catalog-drift} (:outcome response))))
          (conj {:id "UEP-WARM-TEST-FAILED"
                 :message "Warm worker reported a failed or rejected exact selection"
                 :outcome (:outcome response)
                 :exit-code (:exit-code response)})
          (:output-truncated? response)
          (conj {:id "UEP-WARM-OUTPUT-BOUND"
                 :message "Warm worker output exceeded its bound"}))
        :evidence (if passed? (:required-evidence plan-value) [])
        :mutation-detected? (not stable?)
        :fresh-replayable? false
        :provenance-checked? stable?
        :source-authenticated? stable?
        :closure-verified? stable?
        ;; A cached result remains non-authoritative; only its three hit
        ;; counters influence the observed acceleration decision.
        :cache-decision (if cache-hit? :hit :miss)}
       cached? (assoc :artifacts (if (map? runner-result) [runner-result] []))
       (and cached? (map? runner-result))
       (assoc :cache-observation
              (warm-cached-observation plan-value runner-result))))))

(defn- warm-owner-loop
  [session]
  (let [lease (atom nil)
        transport (atom nil)
        start-fn (:start-fn session)
        acquire-fn (:acquire-fn session)
        release-fn (:release-fn session)]
    (try
      (let [acquired (acquire-fn {:coordination-root (:coordination-root session)
                                  :timeout-ms (:startup-timeout-ms session)}
                                 (:resource-class session))]
        (reset! lease acquired)
        (ensure! (valid-broker-lease?
                  acquired (:resource-class session) (:coordination-root session))
                 "UEP-WARM-LEASE"
                 "Warm worker received an unauthenticated broker lease" {})
        (when @(:stop? session)
          (throw (request-error "UEP-WARM-STOPPED"
                                "Warm session was stopped before startup")))
        (let [started (transport-shape!
                       (start-fn {:command (:command session)
                                  :directory (:root session)
                                  :environment (:environment session)
                                  :candidate-commit (:candidate-commit (:repository session))
                                  :candidate-tree (:candidate-tree (:repository session))
                                  :max-requests warm-worker-max-requests
                                  :session-key (:identity-id session)}))]
          (reset! transport started)
          (reset! (:transport session) started)
          (let [ready (validate-warm-ready!
                       (read-warm-record! started (:startup-timeout-ms session)
                                          "UEP-WARM-STARTUP")
                       (:repository session))]
            (deliver (:startup session) {:ready ready})
            (reset! (:state session) :ready)
            (let [request-count (atom 0)]
              (while (not @(:stop? session))
                (if-let [task (.poll ^BlockingQueue (:queue session)
                                      50 TimeUnit/MILLISECONDS)]
                  (let [next-count (inc @request-count)
                        step (try
                               (let [remaining-ns (- (:deadline-ns task)
                                                     (System/nanoTime))]
                                 (when-not (pos? remaining-ns)
                                   (throw (request-error
                                           "UEP-WARM-TIMEOUT"
                                           "Warm request expired before dispatch")))
                                 ((:write-line! started) (:line task))
                                 (let [response
                                       (validate-warm-worker-response!
                                        (read-warm-record!
                                         started
                                         (max 1 (quot (+ remaining-ns 999999)
                                                      1000000))
                                         "UEP-WARM-RESPONSE"
                                        "UEP-WARM-TIMEOUT")
                                        (:repository session)
                                        (:request-id task)
                                        next-count
                                        (:selection task)
                                        (:plan-value task)
                                        (:cached? task)
                                        (:artifact? task)
                                        (:artifact-request task)
                                        (:artifact-binding task))]
                                   {:response response
                                    :continue?
                                    (and (= :passed (:outcome response))
                                         (< next-count warm-worker-max-requests)
                                         ((:process-alive? started)))}))
                               (catch Throwable error
                                 {:error error}))]
                    (if-let [error (:error step)]
                      (do
                        (deliver (:promise task) {:error error})
                        (reset! (:stop-reason session)
                                (or (:id (ex-data error)) :worker-failure))
                        (reset! (:stop? session) true))
                      (do
                        (deliver (:promise task) {:response (:response step)})
                        (if (:continue? step)
                          (reset! request-count next-count)
                          (do
                            (when (not= :passed
                                        (:outcome (:response step)))
                              (reset! (:stop-reason session) :worker-rejected))
                            (reset! (:stop? session) true)))))
                  (Thread/yield))))))))
      (catch Throwable error
        (when-not (realized? (:startup session))
          (deliver (:startup session) {:error error}))
        (reset! (:state session) :failed)
        (reset! (:stop-reason session)
                (or (:id (ex-data error)) :startup-failure))
        (reset! (:stop? session) true)
        (session-pending-failure! session error))
      (finally
        (when-let [started @transport]
          (try ((:close! started)) (catch Throwable _ nil)))
        (when-let [acquired @lease]
          (try
            (release-fn acquired)
            (catch Throwable error
              (reset! (:release-error session) error))))
        (reset! (:transport session) nil)
        (reset! (:state session) :closed)
        (session-pending-failure!
         session
         (request-error "UEP-WARM-CLOSED" "Warm worker session closed"))
        (locking warm-sessions
          (when (identical? session (get @warm-sessions (:identity-id session)))
            (swap! warm-sessions dissoc (:identity-id session))))))))

(defn- new-warm-session
  [config]
  (let [session (merge
                 {:schema warm-session-schema
                  :queue (ArrayBlockingQueue. warm-session-queue-capacity)
                  :startup (promise)
                  :stop? (atom false)
                  :stop-reason (atom nil)
                  :state (atom :starting)
                  :transport (atom nil)
                  :owner-thread (atom nil)
                  :release-error (atom nil)
                  :request-sequence (AtomicLong. 0)
                  :last-used (AtomicLong. (System/nanoTime))}
                 config)
        owner (Thread. #(warm-owner-loop session)
                       (str "gravity-warm-session-" (:identity-id session)))]
    (.setDaemon owner true)
    (reset! (:owner-thread session) owner)
    (.start owner)
    session))

(defn- evict-conflicting-warm-sessions!
  [^Path root identity-id]
  (doseq [[other-id session] @warm-sessions
          :when (and (not= identity-id other-id)
                     (= (str root) (str (:root session))))]
    (stop-warm-session! session :repository-identity-drift)))

(defn- evict-oldest-warm-session!
  [identity-id]
  (when (>= (count @warm-sessions) warm-session-max-count)
    (when-let [[other-id session]
               (first (sort-by (fn [[_ value]]
                                 (.get ^AtomicLong (:last-used value)))
                               (remove #(= identity-id (first %)) @warm-sessions)))]
      (stop-warm-session! session :session-capacity)
      (locking warm-sessions
        (when (identical? session (get @warm-sessions other-id))
          (swap! warm-sessions dissoc other-id))))))

(defn- reusable-warm-session?
  [session]
  (and (not @(:stop? session))
       (contains? #{:starting :ready} @(:state session))
       (or (= :starting @(:state session))
           (when-let [transport @(:transport session)]
             (try
               ((:process-alive? transport))
               (catch Throwable _ false))))))

(defn- warm-session-for
  [config]
  (let [identity-id (:identity-id config)]
    (evict-conflicting-warm-sessions! (:root config) identity-id)
    (locking warm-sessions
      (if-let [existing (get @warm-sessions identity-id)]
        (if (reusable-warm-session? existing)
          existing
          (do
            (stop-warm-session! existing :worker-not-live)
            (swap! warm-sessions dissoc identity-id)
            (let [created (new-warm-session config)]
              (swap! warm-sessions assoc identity-id created)
              created)))
        (do
          (evict-oldest-warm-session! identity-id)
          (let [created (new-warm-session config)]
            (swap! warm-sessions assoc identity-id created)
            created))))))

(defn- warm-session-request!
  ([session plan-value selection timeout-ms]
   (warm-session-request! session plan-value selection timeout-ms false nil))
  ([session plan-value selection timeout-ms cached?]
   (warm-session-request! session plan-value selection timeout-ms cached? nil nil))
  ([session plan-value selection timeout-ms cached? artifact-request]
   (warm-session-request! session plan-value selection timeout-ms cached? artifact-request nil))
  ([session plan-value selection timeout-ms cached? artifact-request artifact-binding]
   (let [startup (deref (:startup session) (:startup-timeout-ms session)
                        ::warm-startup-timeout)]
    (when (= ::warm-startup-timeout startup)
      (stop-warm-session! session :startup-timeout)
      (throw (request-error "UEP-WARM-STARTUP-TIMEOUT"
                            "Warm worker startup exceeded its bound")))
    (when-let [error (:error startup)]
      (stop-warm-session! session :startup-failed)
      (throw error))
    (ensure! (= :ready @(:state session))
             "UEP-WARM-SESSION-CLOSED"
             "Warm worker session is not ready" {:state @(:state session)})
    (let [sequence (.incrementAndGet ^AtomicLong (:request-sequence session))
          request-id (str (subs (:identity-id session) 7 23) "-" sequence)
          executor-request (get-in plan-value [:executor :request])
          request
          (cond
            artifact-request
            (merge {:schema warm-artifact-request-schema
                    :request-id request-id
                    :operation :run-fast-artifact
                    :candidate-commit (:candidate-commit (:repository session))
                    :candidate-tree (:candidate-tree (:repository session))
                    :lane :fast
                    :route-id (:route-id plan-value)
                    :plan-id (:plan-id plan-value)
                    :catalog-id (:catalog-id plan-value)
                    :closure-identity (:closure-identity plan-value)}
                   artifact-request)
            cached?
            (do
              (ensure! (and (map? executor-request)
                            (= #{:operation :shard :result-contract-id}
                               (set (keys executor-request)))
                            (= :cached-shard (:operation executor-request))
                            (= warm-cached-result-contract-id
                               (:result-contract-id executor-request))
                            (string? (:shard executor-request))
                            (<= 1 (count (:shard executor-request)) 128))
                       "UEP-WARM-CACHED-REQUEST"
                       "Canonical cached executor request is not closed" {})
              {:schema warm-cached-request-schema
               :request-id request-id
               :operation :run-cached-shard
               :candidate-commit (:candidate-commit (:repository session))
               :candidate-tree (:candidate-tree (:repository session))
               :lane :fast
               :route-id (:route-id plan-value)
               :plan-id (:plan-id plan-value)
               :catalog-id (:catalog-id plan-value)
               :closure-identity (:closure-identity plan-value)
               :result-contract-id (:result-contract-id executor-request)
               :shard (:shard executor-request)})
            :else
            {:operation :run-exact
              :request-id request-id
              :args (vec (concat ["--namespace" (:namespace selection)]
                                 (mapcat #(list "--exact" %) (:selectors selection))
                                 ["--fail-fast"]))})
          _ (when cached?
              (ensure! (= warm-cached-request-keys (set (keys request)))
                       "UEP-WARM-CACHED-REQUEST"
                       "Cached worker request keys are not closed" {}))
          deadline-ns (+ (System/nanoTime)
                         (.toNanos TimeUnit/MILLISECONDS timeout-ms))
          task {:request-id request-id
                :selection selection
                :plan-value plan-value
                :cached? cached?
                :artifact? (boolean artifact-request)
                :artifact-request artifact-request
                :artifact-binding artifact-binding
                :line (warm-request-line session request)
                :promise (promise)
                :deadline-ns deadline-ns}
          offered? (.offer ^BlockingQueue (:queue session) task
                           (long timeout-ms) TimeUnit/MILLISECONDS)]
      (ensure! offered? "UEP-WARM-QUEUE-TIMEOUT"
               "Warm worker request queue is full" {})
      (.set ^AtomicLong (:last-used session) (System/nanoTime))
      (let [remaining-ns (- deadline-ns (System/nanoTime))
            remaining-ms (max 1 (quot (+ remaining-ns 999999) 1000000))
            result (deref (:promise task) (long remaining-ms)
                          ::warm-request-timeout)]
        (when (= ::warm-request-timeout result)
          (stop-warm-session! session :request-timeout)
          (throw (request-error "UEP-WARM-TIMEOUT"
                                "Warm worker request exceeded its bound")))
        (if-let [error (:error result)]
          (do
            (stop-warm-session! session :request-failed)
            (throw error))
          (:response result)))))))

(defn reset-warm-sessions!
  "Stop all development warm sessions.

  This explicit lifecycle hook is intended for bounded test/process shutdown
  and controlled development-worker eviction. Broker release still occurs on
  each session's owner thread; this function never releases a lease directly."
  []
  (let [sessions (vec (vals @warm-sessions))]
    (doseq [session sessions]
      (stop-warm-session! session :explicit-reset))
    (doseq [session sessions]
      (when-let [^Thread owner @(:owner-thread session)]
        (try
          (.join owner process-cleanup-timeout-ms)
          (catch InterruptedException error
            (.interrupt (Thread/currentThread))
            (throw error)))))
    (locking warm-sessions
      (doseq [[identity-id session] @warm-sessions
              :when (some #(identical? session %) sessions)]
        (swap! warm-sessions dissoc identity-id))))
  nil)

(defn- valid-process-result?
  [result]
  (and (map? result)
       (= process-result-keys (set (keys result)))
       (integer? (:exit-code result))
       (string? (:stdout result))
       (string? (:stderr result))
       (boolean? (:stdout-truncated? result))
       (boolean? (:stderr-truncated? result))
       (boolean? (:timed-out? result))
       (map? (:cleanup result))
       (boolean? (get-in result [:cleanup :complete?]))))

(defn- closure-snapshot
  [plan-value]
  (if *closure-snapshot*
    (*closure-snapshot* plan-value)
    (if-let [snapshot-var (try (requiring-resolve
                                'gravity.self-hosting.unified-execution-plan/closure-snapshot)
                               (catch Throwable _ nil))]
      ((var-get snapshot-var) plan-value)
      ;; An adapter cannot silently proceed without the reviewed closure guard.
      (fail! "UEP-LANE-SNAPSHOT" "Unified closure snapshot is unavailable" {}))))

(defn run-fast!
  "Run exact selectors through one persistent SH-01 warm worker session.

  A session owns one authenticated broker lease on its owner thread and is
  reused only when the canonical repository, clean HEAD identities, closure,
  resource class, runtime, and worker identities all match.  This lane is
  deliberately non-authoritative. Cached-shard routes may project an
  authenticated raw result and honest hit/miss counters, but neither the
  warm process nor that projection confers acceptance authority."
  [plan-value]
  (validate-plan-boundary! plan-value)
  (let [selection (plan-selection plan-value)
        cached? (try
                  (cached-plan-admitted? plan-value)
                  (catch Throwable error
                    (rethrow-lane-control! error)
                    (throw error)))
        policy (:resource-policy plan-value)
        resource-class (:resource-class policy)
        timeout-ms (bounded-int (:timeout-ms policy) "UEP-FAST-TIMEOUT"
                                :timeout-ms (* 4 60 60 1000))
        context (explicit-resource-context)]
    (cond
      (and context (not (valid-explicit-context? context)))
      (blocked "UEP-RESOURCE-BOUNDED-CONTEXT-REQUIRED"
               "Fast execution requires a reviewed bounded executor context")

      (str/blank? (str *coordination-root*))
      (blocked "UEP-RESOURCE-LEASE-REQUIRED"
               "Fast execution requires an explicit SH-01 coordination root")

      :else
      (try
        (let [root (repository-root)
              repository (exact-repository-identity root)
              {:keys [identity identity-id] :as session-key}
              (warm-session-key root *coordination-root* repository
                                plan-value resource-class timeout-ms)
              config {:schema warm-session-schema
                      :identity identity
                      :identity-id identity-id
                      :root root
                      :repository repository
                      :coordination-root (str *coordination-root*)
                      :resource-class resource-class
                      :command (warm-worker-command repository)
                      :environment (warm-environment plan-value)
                      :startup-timeout-ms (min warm-session-startup-timeout-ms
                                               timeout-ms)
                      ;; Capture dynamic seams at construction time.  The
                      ;; owner thread then uses exactly these functions for
                      ;; the lifetime of the session.
                      :acquire-fn *broker-acquire!*
                      :release-fn *broker-release!*
                      :start-fn (or *warm-session-start!* process-transport)}
              session (warm-session-for (merge session-key config))
              before (closure-snapshot plan-value)
              response (warm-session-request!
                        session plan-value selection timeout-ms cached?)
              after (closure-snapshot plan-value)
              stable? (= before after)]
          ;; A closure drift invalidates the process identity.  Evict the
          ;; session before returning so no later request can reuse it.
          (when-not stable?
            (stop-warm-session! session :closure-drift))
          (warm-request-result plan-value selection response stable? cached?))
        (catch InterruptedException error
          (.interrupt (Thread/currentThread))
          (throw error))
        (catch clojure.lang.ExceptionInfo error
          (rethrow-lane-control! error)
          (failed (or (:id (ex-data error)) "UEP-WARM-EXECUTION")
                  (or (ex-message error) "Fast warm execution failed")))
        (catch Throwable error
          (rethrow-lane-control! error)
                  (failed "UEP-WARM-EXECUTION"
                          (or (ex-message error) "Fast warm execution failed")))))))

(defn run-fast-artifact!
  "Run one reviewed fixture row through the fast authenticated pipeline child.

  This is a transport foundation only: it returns a non-authoritative compact
  projection and deliberately does not publish receipts or acceptance evidence."
  [plan-value row-id requested-stage]
  (ensure! (and (string? row-id) (not (str/blank? row-id))
                (<= (count row-id) artifact-row-id-max-length))
           "UEP-ARTIFACT-ROW" "Artifact fixture row id is invalid or oversized"
           {:row-id row-id :maximum artifact-row-id-max-length})
  (ensure! (and (keyword? requested-stage)
                (contains? artifact-stages requested-stage))
           "UEP-ARTIFACT-STAGE" "Artifact stage is outside the closed pipeline"
           {:stage requested-stage})
  (validate-plan-boundary! plan-value)
  (ensure! (= :fast (:lane plan-value)) "UEP-ARTIFACT-LANE"
           "Fast artifact transport requires the fast lane" {})
  (ensure! (= :warm-bounded-process (:execution-mode plan-value))
           "UEP-ARTIFACT-LANE" "Fast artifact transport requires a warm process" {})
  (ensure! (= :eligible (get-in plan-value [:cache-policy :mode]))
           "UEP-ARTIFACT-CACHE" "Fast artifact transport requires cache eligibility" {})
  (let [binding-fn (or (requiring-resolve
                        'gravity.self-hosting.unified-execution-plan/fixture-source-binding)
                       (fail! "UEP-ARTIFACT-BINDING" "Fixture binding is unavailable" {}))
        binding ((var-get binding-fn) plan-value row-id requested-stage)
        artifact-request {:binding-id (:binding-id binding)
                          :row-id row-id
                          :stage requested-stage}
        selection (plan-selection plan-value)
        policy (:resource-policy plan-value)
        timeout-ms (bounded-int (:timeout-ms policy) "UEP-ARTIFACT-TIMEOUT"
                                :timeout-ms (* 4 60 60 1000))
        context (explicit-resource-context)]
    (cond
      (and context (not (valid-explicit-context? context)))
      (blocked "UEP-RESOURCE-BOUNDED-CONTEXT-REQUIRED"
               "Fast artifact execution requires a reviewed bounded executor context")

      (str/blank? (str *coordination-root*))
      (blocked "UEP-RESOURCE-LEASE-REQUIRED"
               "Fast artifact execution requires an explicit SH-01 coordination root")

      :else
      (try
        (let [root (repository-root)
              repository (exact-repository-identity root)
              {:keys [identity identity-id] :as session-key}
              (warm-session-key root *coordination-root* repository
                                plan-value (:resource-class policy) timeout-ms)
              config {:schema warm-session-schema :identity identity
                      :identity-id identity-id :root root :repository repository
                      :coordination-root (str *coordination-root*)
                      :resource-class (:resource-class policy)
                      :command (warm-worker-command repository)
                      :environment (warm-environment plan-value)
                      :startup-timeout-ms (min warm-session-startup-timeout-ms timeout-ms)
                      :acquire-fn *broker-acquire!* :release-fn *broker-release!*
                      :start-fn (or *warm-session-start!* process-transport)}
              session (warm-session-for (merge session-key config))
              before (closure-snapshot plan-value)
              response (warm-session-request! session plan-value selection timeout-ms
                                              false artifact-request binding)
              after (closure-snapshot plan-value)
              stable? (= before after)]
          (when-not stable?
            (stop-warm-session! session :closure-drift))
          (ensure! stable? "UEP-ARTIFACT-SNAPSHOT"
                   "Execution closure changed during artifact execution" {})
          (let [status (if (and (= :passed (:outcome response))
                                (map? (:projection response))) :passed :failed)
                diagnostics (if (= :passed status)
                              (vec (get-in response [:projection :diagnostics]))
                              [{:id "UEP-ARTIFACT-FAILED"
                                :message "Fast artifact child rejected the request"}])
                result {:status status
                        :projection (:projection response)
                        :authority {:authoritative? false
                                    :ceiling :non-authoritative
                                    :development-only? true
                                    :nonclaims (:nonclaims plan-value)}
                        :execution-boundary {:mode :fast :isolation :same-jvm
                                             :isolated? false
                                             :acceptance-authority? false
                                             :fresh-authoritative-run-required? true}
                        :diagnostics diagnostics
                        :mutation-detected? (not= before after)
                        :provenance-checked? (= :passed (:outcome response))
                        :source-authenticated? (= :passed (:outcome response))
                        :closure-verified? (= before after)
                        :fresh-replayable? false
                        :cache-decision (or (get-in response [:projection :cache-decision])
                                            :miss)}]
            (assoc result :artifact-reference
                   (artifact-reference plan-value artifact-request binding
                                       (:projection result) status))))
        (catch InterruptedException error
          (.interrupt (Thread/currentThread))
          (throw error))
        (catch Throwable error
          (rethrow-lane-control! error)
          (failed (or (:id (ex-data error)) "UEP-ARTIFACT-EXECUTION")
                  (or (ex-message error) "Fast artifact execution failed")))))))

(defn- safe-relative-path
  [^Path root relative]
  (let [relative-path (Paths/get ^String relative (make-array String 0))
        candidate (.normalize (.resolve root ^Path relative-path))]
    (ensure! (and (not (.isAbsolute relative-path))
                  (.startsWith candidate root)
                  (Files/isDirectory root
                                     (into-array LinkOption
                                                 [LinkOption/NOFOLLOW_LINKS]))
                  (not (Files/isSymbolicLink root)))
             "UEP-FRESH-EXPORT-PATH"
             "Fresh closure path escapes its export root" {:path relative})
    (loop [current root names (seq (iterator-seq (.iterator relative-path)))]
      (when-let [name (first names)]
        (let [next-path (.resolve ^Path current ^Path name)]
          (when (Files/exists next-path
                              (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
            (ensure! (and (not (Files/isSymbolicLink next-path))
                          (or (nil? (next names))
                              (Files/isDirectory
                               next-path
                               (into-array LinkOption
                                           [LinkOption/NOFOLLOW_LINKS]))))
                     "UEP-FRESH-EXPORT-SYMLINK"
                     "Fresh closure path contains an unsafe component"
                     {:path relative}))
          (recur next-path (next names)))))
    candidate))

(defn- copy-one!
  [^Path source ^Path target state]
  (ensure! (not (Files/isSymbolicLink source)) "UEP-FRESH-EXPORT-SYMLINK"
           "Fresh closure cannot copy symbolic links" {:path (str source)})
  (let [attributes (Files/readAttributes source BasicFileAttributes
                                         (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
        size (long (.size ^BasicFileAttributes attributes))
        _ (ensure! (or (.isDirectory ^BasicFileAttributes attributes)
                       (.isRegularFile ^BasicFileAttributes attributes))
                   "UEP-FRESH-EXPORT-TYPE"
                   "Fresh closure contains a non-file entry"
                   {:path (str source)})
        _ (swap! state
                 (fn [{:keys [entries bytes] :as value}]
                   (let [next-entries (inc entries)]
                     (ensure! (<= next-entries copy-entry-limit)
                              "UEP-FRESH-EXPORT-BOUND"
                              "Fresh closure contains too many entries"
                              {:entries next-entries})
                     ;; Subtraction avoids a signed long overflow before the
                     ;; bound is enforced. No file is hashed or copied first.
                     (ensure! (and (not (neg? size))
                                   (<= size (- copy-total-bytes bytes)))
                              "UEP-FRESH-EXPORT-BOUND"
                              "Fresh closure exceeds its byte bound"
                              {:bytes-so-far bytes :next-file-bytes size})
                     (assoc value :entries next-entries
                                  :bytes (+ bytes size)))))
        source-digest-before (when (.isRegularFile ^BasicFileAttributes attributes)
                               (file-digest source))]
    (if (Files/isDirectory source (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
      (Files/createDirectories target (make-array FileAttribute 0))
      (do
        (Files/createDirectories (.getParent target) (make-array FileAttribute 0))
        (with-open [input (Files/newByteChannel source
                                                (into-array java.nio.file.OpenOption
                                                            [StandardOpenOption/READ
                                                             LinkOption/NOFOLLOW_LINKS]))
                    output (Files/newByteChannel target
                                                 (into-array java.nio.file.OpenOption
                                                             [StandardOpenOption/CREATE_NEW
                                                              StandardOpenOption/WRITE
                                                              LinkOption/NOFOLLOW_LINKS]))]
          (let [buffer (java.nio.ByteBuffer/allocate 8192)]
            (loop [copied 0]
              (.clear buffer)
              (let [read (.read input buffer)]
                (when (pos? read)
                  (ensure! (<= read (- size copied))
                           "UEP-FRESH-EXPORT-MUTATED"
                           "Fresh closure file grew while it was copied"
                           {:path (str source)
                            :declared-bytes size
                            :copied-bytes copied
                            :next-bytes read})
                  (.flip buffer)
                  (loop []
                    (when (.hasRemaining buffer)
                      (.write output buffer)
                      (recur)))
                  (recur (+ copied read))))))
          (let [after (Files/readAttributes source BasicFileAttributes
                                             (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))]
            (ensure! (= (.size ^BasicFileAttributes attributes)
                        (.size ^BasicFileAttributes after))
                     "UEP-FRESH-EXPORT-MUTATED"
                     "Fresh closure file changed while it was copied"
                     {:path (str source)})
            (ensure! (= source-digest-before (file-digest source))
                     "UEP-FRESH-EXPORT-MUTATED"
                     "Fresh closure file bytes changed while it was copied"
                     {:path (str source)})
            (ensure! (= source-digest-before (file-digest target))
                     "UEP-FRESH-EXPORT-DIGEST"
                     "Fresh export bytes differ from authenticated source"
                     {:path (str source)})))))))

(defn- export-tree-snapshot
  [^Path root]
  (let [state (atom {:entries 0 :bytes 0})]
    (with-open [paths (Files/walk root (make-array java.nio.file.FileVisitOption 0))]
      (reduce
       (fn [snapshot ^Path path]
       (ensure! (not (Files/isSymbolicLink path))
                "UEP-FRESH-EXPORT-SYMLINK"
                "Fresh export contains a symbolic link"
                {:path (str (.relativize root path))})
         (when-not (= root path)
           (swap! state
                  (fn [{:keys [entries] :as value}]
                    (ensure! (< entries copy-entry-limit)
                             "UEP-FRESH-EXPORT-BOUND"
                             "Fresh export contains too many entries"
                             {:entries (inc entries)})
                    (assoc value :entries (inc entries)))))
         (cond
           (Files/isDirectory path
                              (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
           snapshot

           (Files/isRegularFile path
                                (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
           (let [attributes (Files/readAttributes
                             path BasicFileAttributes
                             (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
                 size (long (.size ^BasicFileAttributes attributes))]
             (swap! state
                    (fn [{:keys [bytes] :as value}]
                      (ensure! (and (not (neg? size))
                                    (<= size (- copy-total-bytes bytes)))
                               "UEP-FRESH-EXPORT-BOUND"
                               "Fresh export exceeds its byte bound"
                               {:bytes-so-far bytes :next-file-bytes size})
                      (assoc value :bytes (+ bytes size))))
             (assoc snapshot (str (.relativize root path))
                             (file-digest path)))

           :else
           (fail! "UEP-FRESH-EXPORT-TYPE"
                  "Fresh export contains an unsupported entry type"
                  {:path (str (.relativize root path))})))
       (sorted-map)
       (iterator-seq (.iterator paths))))))

(defn- minimal-closure-paths
  [plan-value]
  (reduce
   (fn [accepted relative]
     (if (some #(or (= relative %)
                    (str/starts-with? relative (str % "/")))
               accepted)
       accepted
       (conj accepted relative)))
   []
   (->> (get-in plan-value [:dependency-closure :paths]) distinct sort)))

(defn- declared-closure-snapshot
  [^Path root plan-value]
  (let [state (atom {:entries 0 :bytes 0})
        add-file!
        (fn [snapshot ^Path path]
          (ensure! (not (Files/isSymbolicLink path))
                   "UEP-FRESH-EXPORT-SYMLINK"
                   "Declared closure contains a symbolic link"
                   {:path (str (.relativize root path))})
          (let [attributes
                (Files/readAttributes
                 path BasicFileAttributes
                 (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))]
            (cond
              (.isDirectory ^BasicFileAttributes attributes) snapshot
              (.isRegularFile ^BasicFileAttributes attributes)
              (let [size (long (.size ^BasicFileAttributes attributes))]
                (swap! state
                       (fn [{:keys [entries bytes] :as value}]
                         (ensure! (< entries copy-entry-limit)
                                  "UEP-FRESH-EXPORT-BOUND"
                                  "Declared closure contains too many files"
                                  {:entries (inc entries)})
                         (ensure! (and (not (neg? size))
                                       (<= size (- copy-total-bytes bytes)))
                                  "UEP-FRESH-EXPORT-BOUND"
                                  "Declared closure exceeds its byte bound"
                                  {:bytes-so-far bytes :next-file-bytes size})
                         (assoc value :entries (inc entries)
                                      :bytes (+ bytes size))))
                (assoc snapshot (str (.relativize root path))
                                (file-digest path)))
              :else
              (fail! "UEP-FRESH-EXPORT-TYPE"
                     "Declared closure contains an unsupported entry type"
                     {:path (str (.relativize root path))}))))]
    (reduce
     (fn [snapshot relative]
       (let [path (safe-relative-path root relative)]
         (ensure! (Files/exists path
                                (into-array LinkOption
                                            [LinkOption/NOFOLLOW_LINKS]))
                  "UEP-FRESH-EXPORT-MISSING"
                  "Declared closure path is missing"
                  {:path relative})
         (if (Files/isDirectory path
                                (into-array LinkOption
                                            [LinkOption/NOFOLLOW_LINKS]))
           (with-open [paths (Files/walk
                              path
                              (make-array java.nio.file.FileVisitOption 0))]
             (reduce add-file! snapshot
                     (iterator-seq (.iterator paths))))
           (add-file! snapshot path))))
     (sorted-map)
     (minimal-closure-paths plan-value))))

(defn- validate-isolated-export!
  [^Path export-root plan-value source-snapshot]
  (let [source-root (repository-root)
        lexical-root (.normalize (.toAbsolutePath export-root))]
    (ensure! (and (Files/isDirectory
                   lexical-root
                   (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
                  (not (Files/isSymbolicLink lexical-root)))
             "UEP-FRESH-EXPORT-ISOLATION"
             "Fresh export root is not a regular isolated directory"
             {:export-root (str lexical-root)})
    (let [export-root (.toRealPath lexical-root (make-array LinkOption 0))]
      (ensure! (and
                  (not= source-root export-root)
                  (not (.startsWith export-root source-root))
                  (not (.startsWith source-root export-root)))
             "UEP-FRESH-EXPORT-ISOLATION"
             "Fresh export root is not isolated from the invoking repository"
             {:export-root (str export-root)})
      (let [declared (declared-closure-snapshot export-root plan-value)
            complete (export-tree-snapshot export-root)]
        (ensure! (= source-snapshot declared complete)
                 "UEP-FRESH-EXPORT-CLOSURE"
                 "Fresh export is not the exact authenticated dependency closure"
                 {:expected-files (count source-snapshot)
                  :declared-files (count declared)
                  :export-files (count complete)}))
      export-root)))

(defn- export-closure-default
  [plan-value]
  (let [source-root (repository-root)
        export-root (Files/createTempDirectory "gravity-uep-fresh-"
                                               (make-array FileAttribute 0))
        state (atom {:entries 0 :bytes 0})]
    (try
      ;; Deterministically drop children covered by an explicitly listed
      ;; directory; retaining both would attempt CREATE_NEW twice.
      (let [paths (minimal-closure-paths plan-value)]
       (doseq [relative paths]
        (let [source (safe-relative-path source-root relative)
              target (safe-relative-path export-root relative)]
          (ensure! (Files/exists source
                                  (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
                   "UEP-FRESH-EXPORT-MISSING" "Fresh closure path is missing"
                   {:path relative})
          (if (Files/isDirectory source
                                 (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
            (with-open [paths (Files/walk source
                                          (make-array java.nio.file.FileVisitOption 0))]
              (doseq [entry (iterator-seq (.iterator paths))]
                (let [entry-relative (str (.relativize source-root entry))]
                  (copy-one! entry
                             (safe-relative-path export-root entry-relative)
                             state))))
            (copy-one! source target state)))))
      {:directory export-root
       :cleanup #(with-open [paths (Files/walk export-root
                                               (make-array java.nio.file.FileVisitOption 0))]
                    (.forEach (.sorted paths (Comparator/reverseOrder))
                              (reify java.util.function.Consumer
                                (accept [_ item]
                                  (Files/deleteIfExists ^Path item))))) }
      (catch Throwable error
        (try
          (with-open [paths (Files/walk export-root
                                        (make-array java.nio.file.FileVisitOption 0))]
            (.forEach (.sorted paths (Comparator/reverseOrder))
                      (reify java.util.function.Consumer
                        (accept [_ item]
                          (Files/deleteIfExists ^Path item)))))
          (catch Throwable _ nil))
        (throw error)))))

(defn- write-once-default
  [^Path _export-root plan-value result]
  ;; Durable receipts live in the invoking repository, never in the temporary
  ;; export that is deleted after the child exits.
  (let [root (repository-root)
        directory
        (reduce
         (fn [^Path parent component]
           (let [path (.resolve parent ^String component)]
             (if (Files/exists path
                               (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
               (ensure! (and (Files/isDirectory
                              path
                              (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
                             (not (Files/isSymbolicLink path)))
                        "UEP-FRESH-RECEIPT-PATH"
                        "Fresh receipt directory contains an unsafe component"
                        {:component component})
               (Files/createDirectory path (make-array FileAttribute 0)))
             path))
         root ["target" "validation" "integration-fresh-verification"
               "unified-execution"])
        artifact-reference (first (:artifacts result))
        receipt-base
        {:schema :gravity/unified-fresh-child-receipt-v1
         :plan-id (:plan-id plan-value)
         :selection (:selection plan-value)
         :closure-identity (:closure-identity plan-value)
         :candidate-commit (:candidate-commit artifact-reference)
         :candidate-tree (:candidate-tree artifact-reference)
         :export-closure-id (:export-closure-id artifact-reference)
         :status (:status result)
         :cache :forbidden
         :no-resume? true
         :authority-ceiling (:authority-ceiling plan-value)
         :authoritative-child artifact-reference
         :nonclaims (:nonclaims plan-value)}
        receipt (assoc receipt-base :receipt-id
                       (bootstrap/reader-canonical-hash receipt-base))
        bytes (.getBytes (str (pr-str receipt) "\n")
                         StandardCharsets/UTF_8)]
    (ensure! (and (= :passed (:status result))
                  (= :gravity/sh07-authoritative-child-reference
                     (:artifact artifact-reference))
                  (string? (:child-receipt-id artifact-reference))
                  (re-matches warm-oid-pattern
                              (str (:candidate-commit artifact-reference)))
                  (re-matches warm-oid-pattern
                              (str (:candidate-tree artifact-reference)))
                  (sha256-id? (:export-closure-id artifact-reference))
                  (seq (:module-source-bindings artifact-reference))
                  (<= (alength bytes) (* 64 1024)))
             "UEP-FRESH-RECEIPT-CONTENT"
             "Fresh receipt is not bound to a validated authoritative child"
             {})
    (loop [attempt 0]
      (let [path (.resolve directory
                           (str (:plan-id plan-value) "-"
                                (:receipt-id receipt) "-"
                                (UUID/randomUUID) ".edn"))
            written
            (try
              (with-open [channel (Files/newByteChannel
                                   path
                                   (into-array java.nio.file.OpenOption
                                               [StandardOpenOption/CREATE_NEW
                                                StandardOpenOption/WRITE
                                                LinkOption/NOFOLLOW_LINKS]))]
                (let [buffer (java.nio.ByteBuffer/wrap bytes)]
                  (while (.hasRemaining buffer) (.write channel buffer)))
                (when (instance? java.nio.channels.FileChannel channel)
                  (.force ^java.nio.channels.FileChannel channel true)))
              {:path (str path)
               :receipt-id (:receipt-id receipt)
               :write-once? true
               :no-resume? true}
              (catch java.nio.file.FileAlreadyExistsException error
                (if (< attempt 7) ::collision (throw error))))]
        (if (= ::collision written)
          (recur (inc attempt))
          written)))))

(defn- fresh-result
  [plan-value requested process-result before after export-root repository
   export-closure-id]
  (let [stable? (= before after)
        process-valid?
        (and (= (conj process-result-keys :export-stable?)
                (set (keys process-result)))
             (boolean? (:export-stable? process-result))
             (valid-process-result? (dissoc process-result :export-stable?)))
        valid-cleanup? (true? (get-in process-result [:cleanup :complete?]))
        output (str (:stdout process-result))
        parsed (try {:child (parse-one-edn output)}
                    (catch Throwable error {:parse-error error}))
        child (:child parsed)
        canonical-validator
        (try
          (requiring-resolve
           'gravity.self-hosting.sh07-authoritative-runner/validate-authoritative-run-result!)
          (catch Throwable error error))
        validation-error
        (cond
          (:parse-error parsed) (:parse-error parsed)
          (not (map? child))
          (ex-info "Fresh authoritative child output is not a receipt map"
                   {:id "UEP-LANE-MALFORMED-OUTPUT"})
          (not (var? canonical-validator))
          (ex-info "Canonical authoritative validator is unavailable"
                   {:id "UEP-FRESH-VALIDATOR"})
          :else
          (try
            ((var-get canonical-validator) requested child (str export-root))
            nil
            (catch Throwable error error)))
        child-valid? (and (map? child) (nil? validation-error))
        passed? (and process-valid?
                     stable?
                     (true? (:export-stable? process-result))
                     valid-cleanup?
                     (false? (:timed-out? process-result))
                     (zero? (long (or (:exit-code process-result) 1)))
                     child-valid?
                     (false? (:stdout-truncated? process-result))
                     (false? (:stderr-truncated? process-result)))]
    {:status (if passed? :passed :failed)
     :test-count (if passed? 1 0)
     :pass (if passed? 1 0)
     :fail 0
     :error (if passed? 0 1)
     :diagnostics
     (cond-> []
       (not process-valid?)
       (conj {:id "UEP-FRESH-PROCESS-RESULT"
              :message "Fresh child returned a malformed process envelope"})
       (not stable?) (conj {:id "UEP-SNAPSHOT-MUTATED"
                            :message "Source closure changed during fresh execution"})
       (not (true? (:export-stable? process-result)))
       (conj {:id "UEP-FRESH-EXPORT-MUTATED"
              :message "Isolated export changed during fresh execution"})
       (:timed-out? process-result) (conj {:id "UEP-FRESH-TIMEOUT"
                                           :message "Fresh authoritative child exceeded its timeout"})
       (or (:stdout-truncated? process-result)
           (:stderr-truncated? process-result))
       (conj {:id "UEP-FRESH-OUTPUT-BOUND"
              :message "Fresh authoritative child output exceeded its bound"})
       validation-error
       (conj {:id (or (:id (ex-data validation-error))
                      "UEP-LANE-MALFORMED-OUTPUT")
              :message (or (ex-message validation-error)
                           "Fresh authoritative child output was malformed")
              :data (dissoc (or (ex-data validation-error) {}) :id)})
       (and (map? child) (not= :passed (:status child)))
       (conj {:id "UEP-FRESH-CHILD-REJECTED"
              :message "Fresh authoritative child reported rejection"
              :child-status (:status child)
              :failed-checks
              (mapv #(select-keys % [:module :failed-checks])
                    (:modules child))})
       (not valid-cleanup?) (conj {:id "UEP-FRESH-PROCESS-CLEANUP"
                                   :message "Fresh authoritative process tree did not terminate"}))
     :artifacts
     (if child-valid?
       [{:artifact :gravity/sh07-authoritative-child-reference
         :child-receipt-id (bootstrap/reader-canonical-hash child)
         :candidate-commit (:candidate-commit repository)
         :candidate-tree (:candidate-tree repository)
         :export-closure-id export-closure-id
         :module-source-bindings
         (mapv #(select-keys % [:module :source-path :source-byte-count
                                :source-bytes-sha256 :source-revision-id
                                :artifact-id])
               (:modules child))}]
       [])
     :evidence (if passed? (result-evidence plan-value) [])
     :mutation-detected? (not stable?)
     :fresh-replayable? passed?
     :provenance-checked? (and stable? child-valid?)
     :source-authenticated? (and stable? child-valid?)
     :closure-verified? (and stable? (true? (:export-stable? process-result)))
     :cache-decision :forbidden}))

(defn run-fresh!
  "Run the authoritative proof runner from a fresh temporary export."
  [plan-value]
  (validate-plan-boundary! plan-value)
  (let [selection (plan-selection plan-value)
        policy (:resource-policy plan-value)
        timeout-ms (bounded-int (:timeout-ms policy) "UEP-FRESH-TIMEOUT" :timeout-ms (* 4 60 60 1000))
        output-bytes (bounded-int (:max-output-bytes policy) "UEP-FRESH-OUTPUT" :max-output-bytes maximum-output-bytes)
        root *coordination-root*]
    (if (str/blank? (str root))
      (assoc (blocked "UEP-RESOURCE-LEASE-REQUIRED"
                      "Fresh execution requires an explicit SH-01 coordination root")
             :cache-decision :forbidden)
      (let [admission (try {:lease (*broker-acquire!* {:coordination-root root :timeout-ms timeout-ms} (:resource-class policy))}
                           (catch Throwable error {:error error}))]
        (if-let [error (:error admission)]
          (failed "UEP-RESOURCE-ADMISSION" (or (ex-message error) "Fresh resource admission failed"))
          (let [lease (:lease admission)]
            (if-not (valid-broker-lease? lease (:resource-class policy) root)
              (failed "UEP-RESOURCE-LEASE-INVALID" "Fresh broker returned an unauthenticated lease")
              (let [result
                    (try
                      (let [source-root (repository-root)
                            repository-before
                            (exact-repository-identity source-root)
                            before (closure-snapshot plan-value)
                            source-export-snapshot
                            (declared-closure-snapshot source-root plan-value)
                            export-closure-id
                            (bootstrap/reader-canonical-hash
                             source-export-snapshot)
                            export (if *fresh-exporter* (*fresh-exporter* plan-value) (export-closure-default plan-value))]
                        (try
                          (ensure! (and (map? export)
                                        (= #{:directory :cleanup} (set (keys export)))
                                        (instance? Path (:directory export))
                                        (ifn? (:cleanup export)))
                                   "UEP-FRESH-EXPORT-SHAPE"
                                   "Fresh exporter returned a malformed handle"
                                   {})
                          (let [directory
                                (validate-isolated-export!
                                 ^Path (:directory export) plan-value
                                 source-export-snapshot)
                                export-before (export-tree-snapshot directory)
                                process-result (invoke-process {:command (fresh-command selection)
                                                                :directory directory
                                                                :environment (environment-for :fresh plan-value directory)
                                                                :timeout-ms timeout-ms
                                                                :max-output-bytes output-bytes})
                                export-after (export-tree-snapshot directory)
                                export-stable?
                                (= source-export-snapshot export-before export-after)
                                process-result (assoc process-result
                                                      :export-stable?
                                                      export-stable?)
                                after (closure-snapshot plan-value)
                                repository-after
                                (exact-repository-identity source-root)
                                _ (ensure! (= repository-before repository-after)
                                           "UEP-FRESH-REPOSITORY-MUTATED"
                                           "Canonical repository identity changed during fresh execution"
                                           {:before repository-before
                                            :after repository-after})
                                result (fresh-result plan-value (first (:selectors selection))
                                                     process-result before after directory
                                                     repository-before
                                                     export-closure-id)]
                            (if (= :passed (:status result))
                              (try
                                ((or *fresh-receipt-writer* write-once-default)
                                 directory plan-value result)
                                result
                                (catch Throwable _
                                  (-> result
                                      (assoc :status :failed :error 1
                                             :fresh-replayable? false)
                                      (update :diagnostics conj
                                              {:id "UEP-FRESH-RECEIPT"
                                               :message "Fresh receipt could not be written once"}))))
                              result))
                          (finally
                            (when-let [cleanup (:cleanup export)]
                              (try (cleanup)
                                   (catch Throwable error
                                     (throw (ex-info "Fresh export cleanup failed"
                                                     {:id "UEP-FRESH-CLEANUP"}
                                                     error))))))))
                      (catch Throwable error
                        (failed (or (:id (ex-data error))
                                    "UEP-FRESH-EXECUTION")
                                (or (ex-message error)
                                    "Fresh execution failed"))))]
                (let [release-error (atom nil)]
                  (try (*broker-release!* lease)
                       (catch Throwable error (reset! release-error error)))
                  (if @release-error
                    (failed "UEP-RESOURCE-RELEASE" "Fresh broker lease release failed")
                    result))))))))))

(defn run-lane!
  "Dispatch by declarative execution mode, not by route id."
  [plan-value]
  (case (:execution-mode plan-value)
    :warm-bounded-process (run-fast! plan-value)
    :fresh-isolated-process (run-fresh! plan-value)
    (failed "UEP-LANE-MODE" "Execution plan has no closed lane adapter")))
