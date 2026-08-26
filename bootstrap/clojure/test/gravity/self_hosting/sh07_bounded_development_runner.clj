(ns gravity.self-hosting.sh07-bounded-development-runner
  "Bounded, non-authoritative development runner for exact SH-07 selections.

  The parent process owns the timeout, progress observation, bounded output,
  and optional SH-01 host-resource lease.  A child process owns Clojure test
  loading and execution.  This runner is deliberately separate from the
  authoritative SH-07 runner: a development receipt never proves a fresh
  artifact or any integration/release property."
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.test :as test])
  (:import (java.io ByteArrayOutputStream PushbackReader StringReader)
           (java.nio.charset StandardCharsets)
           (java.nio.file Files LinkOption Path Paths StandardCopyOption
                            StandardOpenOption)
           (java.util.concurrent TimeUnit))
  (:gen-class))

(def schema :gravity/sh07-development-runner-report-v1)
(def progress-schema :gravity/sh07-development-progress-v1)

(def ^:private default-timeout-ms (* 15 60 1000))
(def ^:private maximum-timeout-ms (* 4 60 60 1000))
(def ^:private default-heartbeat-interval-ms 1000)
(def ^:private minimum-heartbeat-interval-ms 50)
(def ^:private maximum-heartbeat-interval-ms 60000)
(def ^:private process-cleanup-timeout-ms 2000)
(def ^:private process-pid-limit 256)
(def ^:private output-limit-bytes (* 256 1024))
(def ^:private progress-limit-bytes (* 32 1024))
(def ^:private progress-history-limit 32)
(def ^:private process-output-drain-timeout-ms 1000)
(def ^:private child-result-prefix "SH07_DEV_RESULT ")
(def ^:private clojure-command
  (or (System/getenv "GRAVITY_CLOJURE_COMMAND") "clojure"))
(def ^:private progress-file-environment "GRAVITY_SH07_PROGRESS_FILE")

(def ^:private c6-namespace
  'gravity.self-hosting.sh07-core-lowering-source-coverage-test)

(def ^:private routes
  "Closed route catalog.  The child may execute only these exact test vars.

  The contract route is intentionally source/static only.  The coverage route
  contains the expensive C6 test and is the only route classified
  `:memory-heavy`; callers must provide the reviewed SH-01 coordination root
  before it can launch."
  (sorted-map
   "c6-contract"
   {:namespace c6-namespace
    :test-symbols '[sh07-b18-proof-contract-registers-c6-source-exactly]
    :resource-class :normal
    :jvm-options ["-J-Xmx2g"]}
   "c6-coverage"
   {:namespace c6-namespace
    :test-symbols '[sh07-b18-c6-source-has-exact-authentic-coverage]
    :resource-class :memory-heavy
    :jvm-options ["-J-Xmx8g"]}))

(defn route-names [] (vec (keys routes)))

(defn route
  "Return a route from the closed catalog or throw a stable diagnostic."
  [route-name]
  (let [route-name (when (string? route-name) route-name)]
    (or (get routes route-name)
        (throw
         (ex-info
          "Unknown SH-07 development route"
          {:id "SH07-DEV-ROUTE"
           :route route-name
           :available (route-names)})))))

(defn- positive-bounded
  [value option maximum]
  (let [number
        (cond
          (integer? value) value
          (string? value) (try (parse-long value)
                               (catch NumberFormatException _ nil))
          :else nil)]
    (when-not (and (integer? number)
                   (pos? number)
                   (<= (long number) (long maximum)))
      (throw
       (ex-info
        (str option " must be a positive bounded integer")
        {:id "SH07-DEV-OPTION"
         :option option
         :value value
         :maximum maximum})))
    (long number)))

(defn- timeout-ms [value]
  (positive-bounded (or value default-timeout-ms)
                    :timeout-ms maximum-timeout-ms))

(defn- heartbeat-interval-ms [value]
  (let [value (positive-bounded (or value default-heartbeat-interval-ms)
                                :heartbeat-interval-ms
                                maximum-heartbeat-interval-ms)]
    (when (< value minimum-heartbeat-interval-ms)
      (throw
       (ex-info
        "SH-07 heartbeat interval is below the reviewed minimum"
        {:id "SH07-DEV-OPTION"
         :option :heartbeat-interval-ms
         :value value
         :minimum minimum-heartbeat-interval-ms})))
    value))

(defn- current-working-directory []
  (.toRealPath
   (.toAbsolutePath (Paths/get (System/getProperty "user.dir")
                               (make-array String 0)))
   (make-array LinkOption 0)))

(defn- path-contained?
  "Return true only for a normalized path below the current checkout.

  Every component is checked for symbolic links.  This is a cooperative
  development boundary, not an adversarial filesystem guarantee."
  [value]
  (try
    (let [root (current-working-directory)
          candidate (.normalize
                      (.toAbsolutePath
                       (Paths/get (str value) (make-array String 0))))]
      (and (.startsWith candidate root)
           (loop [parent root
                  names (seq (iterator-seq
                              (.iterator (.relativize root candidate))))]
             (if-let [name (first names)]
               (let [next-path (.resolve ^Path parent ^Path name)]
                 (and (not (Files/isSymbolicLink next-path))
                      (recur next-path (next names))))
               true))))
    (catch Throwable _ false)))

(defn- progress-path
  [value]
  (when (and (string? value)
             (not (str/blank? value))
             (path-contained? value))
    (.normalize
     (.toAbsolutePath (Paths/get value (make-array String 0))))))

(defn- bounded-string
  [value limit]
  (let [value (str value)]
    (subs value 0 (min limit (count value)))))

(defn- valid-progress?
  [record]
  (and (map? record)
       (= progress-schema (:schema record))
       (integer? (:sequence record))
       (not (neg? (long (:sequence record))))
       (string? (:phase record))
       (<= (count (:phase record)) 256)
       (keyword? (:event record))
       (boolean? (:active? record))
       (integer? (:elapsed-ms record))
       (not (neg? (long (:elapsed-ms record))))
       (= #{:schema :sequence :phase :event :active? :elapsed-ms}
          (set (keys record)))))

(defn- read-progress
  [^Path path]
  (when (and path
             (path-contained? (str path))
             (Files/isRegularFile path
                                  (into-array LinkOption [LinkOption/NOFOLLOW_LINKS])))
    (try
      (let [bytes (Files/readAllBytes path)]
        (when (<= (alength bytes) progress-limit-bytes)
          (let [record (edn/read-string
                        (String. bytes StandardCharsets/UTF_8))]
            (when (valid-progress? record) record))))
      (catch Throwable _ nil))))

(defn- emit-progress!
  [path state event phase active? started]
  (when path
    (try
      (let [record {:schema progress-schema
                    :sequence (swap! (:sequence state) inc)
                    :phase (bounded-string phase 256)
                    :event event
                    :active? (boolean active?)
                    :elapsed-ms (long (/ (- (System/nanoTime) started)
                                         1000000))}
            bytes (.getBytes (pr-str record) StandardCharsets/UTF_8)]
        (when (and (<= (alength bytes) progress-limit-bytes)
                   (path-contained? (str path)))
          (let [parent (.getParent path)]
            (Files/createDirectories
             parent
             (make-array java.nio.file.attribute.FileAttribute 0))
            (let [temporary
                  (Files/createTempFile
                   parent ".gravity-sh07-progress-" ".tmp"
                   (make-array java.nio.file.attribute.FileAttribute 0))]
              (try
                (Files/write temporary bytes
                             (into-array StandardOpenOption
                                         [StandardOpenOption/WRITE
                                          StandardOpenOption/TRUNCATE_EXISTING]))
                (Files/move temporary path
                             (into-array StandardCopyOption
                                         [StandardCopyOption/ATOMIC_MOVE
                                          StandardCopyOption/REPLACE_EXISTING]))
                (finally
                  (Files/deleteIfExists temporary)))))))
      (catch Throwable _ nil))))

(defn- strict-read-edn
  [text]
  (let [eof (Object.)]
    (with-open [reader (PushbackReader. (StringReader. text))]
      (let [value (edn/read {:eof eof} reader)
            trailing (edn/read {:eof eof} reader)]
        (when (and (not (identical? eof value))
                   (identical? eof trailing))
          value)))))

(defn- capture-stream-output
  [stream]
  (let [buffer (byte-array 8192)
        output (ByteArrayOutputStream.)
        observed (atom 0)]
    (letfn [(snapshot []
              (let [bytes (.toByteArray output)
                    text (String. bytes StandardCharsets/UTF_8)
                    count (long @observed)]
                {:text text
                 :bytes (alength bytes)
                 :observed-bytes count
                 :truncated? (> count output-limit-bytes)}))]
      (try
        (loop []
          (let [read (.read ^java.io.InputStream stream buffer)]
            (if (= -1 read)
              (snapshot)
              (let [current (long @observed)
                    remaining (max 0 (- output-limit-bytes current))
                    kept (int (min remaining read))]
                (swap! observed + read)
                (when (pos? kept)
                  (.write output buffer 0 kept))
                (recur)))))
        (catch Throwable _
          ;; Closing a process pipe after a timeout unblocks this reader.
          ;; Preserve the bounded prefix instead of making cleanup wait for
          ;; an orphaned descendant that inherited the pipe.
          (snapshot))))))

(defn- capture-stream
  "Capture a process stream on a daemon thread.

  A timed-out child can leave a descendant holding the inherited pipe open.
  The reader is therefore daemonized: cleanup remains bounded even if the OS
  does not immediately unblock a closed pipe."
  [stream]
  (let [result (promise)
        worker (Thread.
                (fn [] (deliver result (capture-stream-output stream))))]
    (.setName worker "gravity-sh07-output-reader")
    (.setDaemon worker true)
    (.start worker)
    {:result result :stream stream :thread worker}))

(defn- close-capture!
  [capture]
  (try (.close ^java.io.InputStream (:stream capture))
       (catch Throwable _ nil))
  (when (.isAlive ^Thread (:thread capture))
    (.interrupt ^Thread (:thread capture))))

(defn- capture-value
  [capture timeout-ms]
  (let [value (deref (:result capture) timeout-ms
                     {:text "" :bytes 0 :observed-bytes 0 :truncated? true})]
    ;; Closing after the bounded wait also handles an exited parent whose
    ;; descendant retained the pipe.  The daemon reader cannot keep the
    ;; development JVM alive while it drains or observes that orphan.
    (close-capture! capture)
    value))

(defn- process-pids
  [^Process process]
  (try
    (let [handle (.toHandle process)]
      (with-open [descendants (.descendants handle)]
        (let [observed (vec (take (inc process-pid-limit)
                                  (iterator-seq (.iterator descendants))))]
          {:root (.pid handle)
           :pids (vec (take process-pid-limit
                            (map #(.pid ^java.lang.ProcessHandle %)
                                 observed)))
           :truncated? (> (count observed) process-pid-limit)})))
    (catch Throwable _ {:root nil :pids [] :truncated? false})))

(defn- cleanup-process-tree!
  "Terminate a child and all bounded descendants, returning cleanup evidence."
  [^Process process]
  (let [{:keys [pids truncated?]} (process-pids process)
        handles
        (keep #(try
                 (some-> (java.lang.ProcessHandle/of (long %))
                         (.orElse nil))
                 (catch Throwable _ nil))
              pids)
        _ (when (.isAlive process)
            (.destroyForcibly process))
        _ (doseq [^java.lang.ProcessHandle handle handles]
            (when (.isAlive handle)
              (.destroyForcibly handle)))
        deadline (+ (System/nanoTime)
                    (* process-cleanup-timeout-ms 1000000))]
    (loop []
      (let [alive (filterv #(.isAlive ^java.lang.ProcessHandle %) handles)
            root-alive? (.isAlive process)]
        (if (or (and (empty? alive) (not root-alive?))
                (>= (System/nanoTime) deadline))
          {:cleanup-complete? (and (empty? alive) (not root-alive?)
                                   (not truncated?))
           :capture-overflow? truncated?
           :captured-descendant-count (count handles)
           :root-exited? (not root-alive?)
           :root-alive-after-kill? root-alive?
           :descendants-alive-after-kill (count alive)}
          (do
            (Thread/sleep 10)
            (recur)))))))

(defn- report-counters?
  [value]
  (and (integer? value) (not (neg? (long value)))))

(defn- route-selection
  [route-name]
  (let [{:keys [namespace test-symbols]} (route route-name)]
    (mapv #(str namespace "/" %) test-symbols)))

(defn- valid-child-report?
  [route-name report process-exit]
  (let [summary (:summary report)
        status (:status report)
        selected (route-selection route-name)
        counters-valid?
        (and (map? summary)
             (= #{:test :pass :fail :error} (set (keys summary)))
             (every? report-counters? (vals summary)))
        passed? (and counters-valid?
                     (= :passed status)
                     (zero? (long (:fail summary)))
                     (zero? (long (:error summary))))]
    (and (map? report)
         (= #{:schema :authority :authoritative? :route :selected
              :status :exit-code :summary :elapsed-ms}
            (set (keys report)))
         (= :gravity/sh07-development-child-result-v1 (:schema report))
         (= :non-authoritative (:authority report))
         (false? (:authoritative? report))
         (= route-name (:route report))
         (= selected (:selected report))
         counters-valid?
         (report-counters? (:elapsed-ms report))
         (contains? #{:passed :failed} status)
         (= (if passed? 0 1) (:exit-code report))
         (= (:exit-code report) process-exit)
         (= passed? (= :passed status)))))

(defn- parse-child-report
  [route-name output process-exit]
  (let [lines (->> (str/split-lines (:text output))
                   (filter #(str/starts-with? % child-result-prefix))
                   vec)]
    (if (not= 1 (count lines))
      {:diagnostic-id "SH07-DEV-MALFORMED-OUTPUT"
       :diagnostic {:reason (if (empty? lines) :missing-result-line
                                :multiple-result-lines)
                    :result-line-count (count lines)}}
      (let [payload (subs (first lines) (count child-result-prefix))
            report (try (strict-read-edn payload)
                        (catch Throwable _ nil))]
        (if (valid-child-report? route-name report process-exit)
          {:report report}
          {:diagnostic-id "SH07-DEV-MALFORMED-OUTPUT"
           :diagnostic {:reason :invalid-result
                        :exit-code process-exit}})))))

(defn- child-command
  [route-name progress-file]
  (let [{:keys [jvm-options]} (route route-name)]
    (vec
     (concat [clojure-command]
             jvm-options
             ["-Sdeps"
              "{:paths [\"bootstrap/clojure/src\" \"bootstrap/clojure/test\"]}"
              "-M" "-m"
              "gravity.self-hosting.sh07-bounded-development-runner"
              "--child" "--route" route-name
              "--progress-file" (str progress-file)]))))

(defn- temporary-progress-file
  []
  (let [directory
        (Files/createTempDirectory
         ^Path (current-working-directory)
         "gravity-sh07-development-"
         (make-array java.nio.file.attribute.FileAttribute 0))]
    {:directory directory
     :path (.resolve directory "progress.edn")
     :temporary? true}))

(defn- configured-progress-file
  [value]
  (if-let [path (progress-path value)]
    {:directory nil :path path :temporary? false}
    (if (and value (not (str/blank? value)))
      (throw
       (ex-info
        "SH-07 progress file must be contained by the current checkout"
        {:id "SH07-DEV-PROGRESS-PATH"
         :path value}))
      (temporary-progress-file))))

(defn- delete-tree!
  [^Path path]
  (when (and path (Files/exists path (make-array LinkOption 0)))
    (with-open [entries (Files/walk path (make-array java.nio.file.FileVisitOption 0))]
      (doseq [entry (reverse (iterator-seq (.iterator entries)))]
        (Files/deleteIfExists ^Path entry)))))

(defn- sample-state!
  [state progress-path route-name started force?]
  (let [progress (read-progress progress-path)
        now (long (/ (- (System/nanoTime) started) 1000000))
        previous @state
        changed? (not= (:last-progress previous) progress)
        due? (>= (- now (long (or (:last-heartbeat-ms previous) 0)))
                 (long (:heartbeat-interval-ms previous)))
        emit? (and progress (or changed? due? force?))]
    (when emit?
      (println (str "SH07 development heartbeat: route=" route-name
                    " phase=" (:phase progress)
                    " event=" (:event progress)
                    " elapsed-ms=" now
                    " sequence=" (:sequence progress)))
      (flush))
    (swap! state
           (fn [current]
             (cond-> (assoc current
                            :last-progress progress
                            :last-elapsed-ms now)
               emit? (assoc :last-heartbeat-ms now)
               (and changed? progress)
               (update :progress-history
                       #(vec (take-last progress-history-limit
                                       (conj (vec %) progress)))))))
    @state))

(defn run-process!
  "Run one exact route in a bounded child process.

  This function is development-only.  It returns `:timeout` or `:failed` on
  timeout/parse errors and can never reinterpret an absent or timed-out child
  result as a pass.  `:child-command-fn` is test-only injection; production
  callers use the closed command generated by `child-command`."
  [route-name options]
  (route route-name)
  (let [options (or options {})
        timeout (timeout-ms (:timeout-ms options))
        heartbeat (heartbeat-interval-ms (:heartbeat-interval-ms options))
        progress (configured-progress-file (:progress-file options))
        command-fn (or (:child-command-fn options)
                       (fn [name {:keys [path]}]
                         (child-command name path)))
        command (vec (command-fn route-name progress))
        state (atom {:heartbeat-interval-ms heartbeat
                     :progress-history []
                     :last-progress nil
                     :last-heartbeat-ms 0
                     :last-elapsed-ms 0})
        started (System/nanoTime)
        builder (doto (ProcessBuilder. ^java.util.List command)
                  (.directory (.toFile (current-working-directory))))]
    (try
      (.put (.environment builder)
            progress-file-environment (str (:path progress)))
      (let [process (.start builder)
            stdout (capture-stream (.getInputStream process))
            stderr (capture-stream (.getErrorStream process))
            deadline (+ started (* timeout 1000000))]
        (loop []
          (let [now (System/nanoTime)]
            (sample-state! state (:path progress) route-name started false)
            (cond
              ;; Check the deadline before process liveness.  A child that
              ;; exits successfully after the bound is still a timeout; its
              ;; late result must never be reinterpreted as a pass.
              (>= now deadline)
              (let [cleanup (cleanup-process-tree! process)
                    _ (close-capture! stdout)
                    _ (close-capture! stderr)
                    stdout (capture-value stdout
                                          process-output-drain-timeout-ms)
                    stderr (capture-value stderr
                                          process-output-drain-timeout-ms)
                    final-state (sample-state! state (:path progress)
                                               route-name started true)
                    elapsed (long (/ (- (System/nanoTime) started) 1000000))]
                {:schema schema
                 :authority :non-authoritative
                 :authoritative? false
                 :route route-name
                 :status :timeout
                 :exit-code 124
                 :elapsed-ms elapsed
                 :timed-out? true
                 :selection (route-selection route-name)
                 :stdout stdout
                 :stderr stderr
                 :cleanup cleanup
                 :progress (select-keys final-state
                                        [:last-progress :progress-history
                                         :last-elapsed-ms])
                 :diagnostic-id "SH07-DEV-TIMEOUT"})

              (not (.isAlive process))
            (let [exit-code (.exitValue process)
                  stdout (capture-value stdout process-output-drain-timeout-ms)
                  stderr (capture-value stderr process-output-drain-timeout-ms)
                  final-state (sample-state! state (:path progress)
                                             route-name started true)
                  parsed (parse-child-report route-name stdout exit-code)
                  elapsed (long (/ (- (System/nanoTime) started) 1000000))]
              (merge
               {:schema schema
                :authority :non-authoritative
                :authoritative? false
                :route route-name
                :status (if (:report parsed) (:status (:report parsed)) :failed)
                :exit-code (if (:report parsed) exit-code 1)
                :elapsed-ms elapsed
                :timed-out? false
                :selection (route-selection route-name)
                :stdout stdout
                :stderr stderr
                :progress (select-keys final-state
                                       [:last-progress :progress-history
                                        :last-elapsed-ms])}
               (if-let [report (:report parsed)]
                 {:child-report report}
                 {:diagnostic-id (:diagnostic-id parsed)
                  :diagnostic (:diagnostic parsed)})))

              :else
              (do
                (Thread/sleep (long (min 100
                                         (max 1
                                              (/ (- deadline (System/nanoTime))
                                                 1000000)))))
                (recur))))))
      (finally
        (when (:temporary? progress)
          (delete-tree! (:directory progress)))))))

(defn- resource-root
  [options]
  (or (:coordination-root options)
      (System/getenv "GRAVITY_SH01_COORDINATION_ROOT")))

(defn- host-resource-lease
  "Resolve the reviewed SH-01 broker lazily.

  Mainline may run this development helper before the optional broker module
  has been reconciled.  A requested heavy lease then fails closed with a
  stable diagnostic; it is never silently replaced by an uncoordinated run."
  []
  (try
    (require 'gravity.self-hosting.sh01-host-resource-broker)
    {:acquire! (ns-resolve 'gravity.self-hosting.sh01-host-resource-broker
                           'acquire!)
     :release! (ns-resolve 'gravity.self-hosting.sh01-host-resource-broker
                           'release!)}
    (catch Throwable _ nil)))

(defn run-route!
  "Run an exact route, acquiring the reviewed host lease when required."
  [route-name options]
  (let [route (route route-name)
        options (or options {})
        resource-class (:resource-class route)
        root (resource-root options)]
    (when (and (= :memory-heavy resource-class) (nil? root))
      (throw
       (ex-info
        "SH-07 memory-heavy development route requires an SH-01 coordination root"
        {:id "SH07-DEV-RESOURCE-ROOT"
         :route route-name
         :resource-class resource-class})))
    (if-not root
      (run-process! route-name options)
      (let [{:keys [acquire! release!] :as broker}
            (host-resource-lease)
            _ (when-not (and broker acquire! release!)
                (throw
                 (ex-info
                  "SH-07 development route requested host capacity but the SH-01 broker is unavailable"
                  {:id "SH07-DEV-RESOURCE-BROKER-ABSENT"
                   :route route-name
                   :resource-class resource-class})))
            lease-options {:coordination-root root
                           :timeout-ms (timeout-ms (:timeout-ms options))}
            lease (try
                    (acquire! lease-options resource-class)
                    (catch clojure.lang.ExceptionInfo error
                      (throw
                       (ex-info
                        "SH-07 development route could not acquire host capacity"
                        {:id "SH07-DEV-RESOURCE-ADMISSION"
                         :route route-name
                         :resource-class resource-class
                         :broker (ex-data error)}
                        error))))]
        (try
          (let [result (run-process! route-name options)]
            (assoc result :resource
                   {:resource-class resource-class
                    :receipt (:receipt lease)
                    :telemetry (:telemetry lease)}))
          (finally
            (release! lease)))))))

(defn- test-vars-for-route
  [route-name]
  (let [{:keys [namespace test-symbols]} (route route-name)]
    (require namespace)
    (mapv
     (fn [test-symbol]
       (let [test-var (ns-resolve namespace test-symbol)]
         (when-not (and (var? test-var) (:test (meta test-var)))
           (throw
            (ex-info
             "SH-07 development route references an absent test var"
             {:id "SH07-DEV-TEST"
              :route route-name
              :namespace namespace
              :test test-symbol})))
         test-var))
     test-symbols)))

(defn- child-progress-emitter
  [configured-path route-name heartbeat-ms]
  (when-let [path (progress-path configured-path)]
    (let [state {:sequence (atom 0)
                 :phase (atom route-name)
                 :event (atom :child-start)
                 :active? (atom true)
                 :started (System/nanoTime)}
          emit (fn []
                 (emit-progress! path state @(:event state) @(:phase state)
                                 @(:active? state) (:started state)))]
      {:path path
       :state state
       :emit! emit
       :update!
       (fn [event]
         (when-let [test-var (:var event)]
           (let [{:keys [ns name]} (meta test-var)]
             (when (and ns name)
               (reset! (:phase state) (str ns "/" name)))))
         (reset! (:event state)
                 (case (:type event)
                   :begin-test-var :test-start
                   :end-test-var :test-complete
                   :begin-test-ns :namespace-start
                   :end-test-ns :namespace-complete
                   :heartbeat))
         (reset! (:active? state)
                 (contains? #{:begin-test-var :begin-test-ns :heartbeat}
                            (:type event)))
         (emit))
       :heartbeat
       (future
         (try
           (while @(:active? state)
             (Thread/sleep (long heartbeat-ms))
             (when @(:active? state)
               (reset! (:event state) :heartbeat)
               (emit)))
           (catch InterruptedException _ nil)))})))

(defn run-child!
  "Execute one route in the child process and print one machine result line."
  [route-name progress-file]
  (let [route (route route-name)
        vars (test-vars-for-route route-name)
        heartbeat-ms (heartbeat-interval-ms
                      (some-> (System/getenv "GRAVITY_SH07_HEARTBEAT_MS")
                              not-empty))
        emitter (child-progress-emitter progress-file route-name heartbeat-ms)
        counters (ref test/*initial-report-counters*)
        started (System/nanoTime)
        report-fn (when emitter
                    (fn [event]
                      ((:update! emitter) event)
                      event))]
    (try
      (when emitter ((:emit! emitter)))
      (binding [test/*report-counters* counters]
        (let [original-report test/report]
          (with-redefs [test/report
                        (fn [event]
                          (when report-fn (report-fn event))
                          (original-report event))]
            (test/test-vars vars))))
      (let [summary @counters
            passed? (and (zero? (:fail summary))
                         (zero? (:error summary)))
            report {:schema :gravity/sh07-development-child-result-v1
                    :authority :non-authoritative
                    :authoritative? false
                    :route route-name
                    :selected (route-selection route-name)
                    :status (if passed? :passed :failed)
                    :exit-code (if passed? 0 1)
                    :summary (select-keys summary [:test :pass :fail :error])
                    :elapsed-ms (long (/ (- (System/nanoTime) started)
                                         1000000))}]
        (println (str child-result-prefix (pr-str report)))
        (flush)
        (if passed? 0 1))
      (catch Throwable throwable
        (let [report {:schema :gravity/sh07-development-child-result-v1
                      :authority :non-authoritative
                      :authoritative? false
                      :route route-name
                      :selected (route-selection route-name)
                      :status :failed
                      :exit-code 1
                      :summary {:test 0 :pass 0 :fail 0 :error 1}
                      :elapsed-ms (long (/ (- (System/nanoTime) started)
                                           1000000))}]
          (println (str child-result-prefix (pr-str report)))
          (flush)
          1))
      (finally
        (when emitter
          (reset! (:active? (:state emitter)) false)
          (future-cancel (:heartbeat emitter))
          (reset! (:event (:state emitter)) :child-complete)
          (emit-progress! (:path emitter) (:state emitter) :child-complete
                          @(:phase (:state emitter)) false (:started (:state emitter))))))))

(defn- parse-args
  [arguments]
  (loop [remaining (seq arguments)
         options {:timeout-ms nil
                  :heartbeat-interval-ms nil
                  :route nil
                  :progress-file nil
                  :coordination-root nil
                  :list? false
                  :child? false}]
    (if-not remaining
      options
      (let [argument (first remaining)
            tail (next remaining)
            value (fn [option]
                    (or (first tail)
                        (throw
                         (ex-info
                          (str option " requires a value")
                          {:id "SH07-DEV-USAGE"}))))]
        (cond
          (= argument "--list")
          (recur tail (assoc options :list? true))

          (= argument "--child")
          (recur tail (assoc options :child? true))

          (= argument "--route")
          (recur (next tail) (assoc options :route (value argument)))

          (= argument "--timeout-ms")
          (recur (next tail) (assoc options :timeout-ms (value argument)))

          (= argument "--heartbeat-interval-ms")
          (recur (next tail)
                 (assoc options :heartbeat-interval-ms (value argument)))

          (= argument "--progress-file")
          (recur (next tail) (assoc options :progress-file (value argument)))

          (= argument "--coordination-root")
          (recur (next tail) (assoc options :coordination-root (value argument)))

          :else
          (throw
           (ex-info
            "Unsupported SH-07 development runner argument"
            {:id "SH07-DEV-USAGE"
             :argument argument})))))))

(defn -main
  [& arguments]
  (try
    (let [{:keys [child? list? route progress-file] :as options}
          (parse-args arguments)]
      (cond
        child?
        (let [result (run-child! route progress-file)]
          (shutdown-agents)
          (System/exit result))

        list?
        (doseq [route-name (route-names)] (println route-name))

        :else
        (do
          (when-not route
            (throw (ex-info "--route is required"
                            {:id "SH07-DEV-USAGE"})))
          (let [result (run-route! route options)]
            (println (pr-str result))
            (flush)
            (when (not= :passed (:status result))
              (System/exit 1))
            result))))
    (catch Throwable throwable
      (binding [*out* *err*]
        (println (str (or (:id (ex-data throwable))
                          "SH07-DEV-UNEXPECTED")
                      ": " (.getMessage throwable))))
      (System/exit 1))))
