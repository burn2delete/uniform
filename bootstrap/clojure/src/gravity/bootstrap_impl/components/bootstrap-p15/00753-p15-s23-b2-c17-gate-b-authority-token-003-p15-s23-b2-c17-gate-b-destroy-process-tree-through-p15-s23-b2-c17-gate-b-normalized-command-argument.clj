(let [p15-s23-b2-c17-gate-b-authority-token (nth __gravity_bootstrap_lexical_values_131964 0)
      p15-s23-b2-c17-gate-b-tool-state (nth __gravity_bootstrap_lexical_values_131964 1)]
(defn- p15-s23-b2-c17-gate-b-destroy-process-tree!
  [candidate process source-path]
  (p15-s23-b2-c17-gate-b-require-authority!
   candidate source-path :destroy-bounded-c-tool-process-set)
  (let [root (.toHandle process)
        descendants
        (with-open [stream (.descendants root)]
          (vec (iterator-seq
                (.iterator (.limit stream (long 65))))))
        overflow? (> (count descendants) 64)
        root-requested?
          (try (.destroyForcibly root) (catch Exception _ false))
        descendant-requests
        (mapv (fn [handle]
                (try (.destroyForcibly ^java.lang.ProcessHandle handle)
                     (catch Exception _ false)))
              descendants)
        deadline (+ (System/nanoTime) 2000000000)
        result
        (loop []
          (let [root-alive? (.isAlive root)
                alive-descendants
                (count (filter #(.isAlive ^java.lang.ProcessHandle %)
                               descendants))]
            (if (and (or root-alive? (pos? alive-descendants))
                     (< (System/nanoTime) deadline))
              (do (Thread/sleep 10) (recur))
              {:kill-requested? true
               :root-kill-requested? (boolean root-requested?)
               :descendant-count (count descendants)
               :descendant-kill-request-count
               (count (filter true? descendant-requests))
               :root-alive-after-kill? root-alive?
               :descendants-alive-after-kill alive-descendants
               :captured-process-set-reaped?
               (and (not root-alive?) (zero? alive-descendants))
               :whole-process-tree-reaping-proved? false})))]
    (when-not (:captured-process-set-reaped? result)
      (p15-s23-c-backend-fail!
       "B2-MANIFEST" source-path {}
       {:missing-fact :captured-c-tool-process-set-not-reaped
        :maximum-byte-count 64
        :observed-byte-count (:descendants-alive-after-kill result)}))
    (when overflow?
      (p15-s23-c-backend-fail!
       "B2-MANIFEST" source-path {}
       {:missing-fact :bounded-c-tool-descendant-count
        :maximum-byte-count 64
        :observed-byte-count (count descendants)}))
    result))

(defn- p15-s23-b2-c17-gate-b-run-process
  [candidate directory command timeout-ms source-path]
  (p15-s23-b2-c17-gate-b-require-authority!
   candidate source-path :run-bounded-c-tool-process)
  (let [builder (ProcessBuilder. ^java.util.List command)
        _ (.directory builder (.toFile directory))
        environment (.environment builder)
        _ (.clear environment)
        _ (.put environment "PATH" "/usr/bin:/bin:/usr/sbin:/sbin")
        _ (.put environment "LC_ALL" "C")
        _ (.put environment "LANG" "C")
        _ (.put environment "DEVELOPER_DIR"
                (get-in p15-s23-b2-c17-gate-b-environment-policy
                        [:fixed-values "DEVELOPER_DIR"]))
        _ (.put environment "HOME" (.toString directory))
        _ (.put environment "TMPDIR" (.toString directory))
        _ (.redirectErrorStream builder false)
        primary-failure (atom nil)
        stdout-holder (atom nil)
        stderr-holder (atom nil)
        process (.start builder)]
    (try
      (let [_ (.close (.getOutputStream process))
            stdout-future
            (future
              (p15-s23-b2-c17-gate-b-read-bounded-stream
               candidate (.getInputStream process) source-path))
            _ (reset! stdout-holder stdout-future)
            stderr-future
            (future
              (p15-s23-b2-c17-gate-b-read-bounded-stream
               candidate (.getErrorStream process) source-path))
            _ (reset! stderr-holder stderr-future)
            finished?
            (.waitFor process timeout-ms
                      java.util.concurrent.TimeUnit/MILLISECONDS)
            termination
            (if finished?
              {:kill-requested? false
               :captured-process-set-reaped? :not-applicable
               :whole-process-tree-reaping-proved? false
               :root-alive-after-kill? false
               :descendants-alive-after-kill 0}
              (p15-s23-b2-c17-gate-b-destroy-process-tree!
               candidate process source-path))
            fallback
            {:bytes (byte-array 0) :text ""
             :stream-read-complete? false
             :total-byte-count 0 :retained-byte-count 0
             :truncated? true :hash :unavailable}
            deref-stream
            (fn [stream-future]
              (try
                (deref stream-future 3000 fallback)
                (catch java.util.concurrent.ExecutionException wrapped
                  (let [cause (.getCause wrapped)]
                    (cond
                      (instance? Error cause) (throw cause)
                      (p15-s23-b2-c17-gate-b-interrupt-like? cause)
                      (do (p15-s23-b2-c17-gate-b-restore-interrupt! cause)
                          (throw cause))
                      (instance? Exception cause) (throw cause)
                      :else (throw wrapped))))))
            stdout (deref-stream stdout-future)
            stderr (deref-stream stderr-future)]
        (when-not (:stream-read-complete? stdout)
          (future-cancel stdout-future))
        (when-not (:stream-read-complete? stderr)
          (future-cancel stderr-future))
        {:command-role :bounded-c17-external-tool
         :finished? finished?
         :timed-out? (not finished?)
         :exit-code (when finished? (.exitValue process))
         :termination termination
         :stdout stdout
         :stderr stderr})
      (catch InterruptedException interrupted
        (reset! primary-failure interrupted)
        (try
          (p15-s23-b2-c17-gate-b-destroy-process-tree!
           candidate process source-path)
          (catch Throwable cleanup
            (p15-s23-b2-c17-gate-b-restore-interrupt! interrupted)
            (p15-s23-b2-c17-gate-b-restore-interrupt! cleanup)
            (if (instance? Error cleanup)
              (do (.addSuppressed ^Throwable cleanup interrupted)
                  (reset! primary-failure cleanup)
                  (throw cleanup))
              (.addSuppressed interrupted cleanup))))
        (when-let [stream-future @stdout-holder]
          (future-cancel stream-future))
        (when-let [stream-future @stderr-holder]
          (future-cancel stream-future))
        (.interrupt (Thread/currentThread))
        (throw interrupted))
      (catch Throwable error
        (reset! primary-failure error)
        (p15-s23-b2-c17-gate-b-restore-interrupt! error)
        (throw error))
      (finally
        (try
          (when (.isAlive process)
            (p15-s23-b2-c17-gate-b-destroy-process-tree!
             candidate process source-path))
          (doseq [holder [stdout-holder stderr-holder]]
            (when-let [stream-future @holder]
              (when-not (future-done? stream-future)
                (future-cancel stream-future))))
          (catch Throwable cleanup
            (if-let [error @primary-failure]
              (do
                (p15-s23-b2-c17-gate-b-restore-interrupt! error)
                (p15-s23-b2-c17-gate-b-restore-interrupt! cleanup)
                (cond
                  (instance? Error error)
                  (.addSuppressed ^Throwable error cleanup)

                  (instance? Error cleanup)
                  (do (.addSuppressed ^Throwable cleanup error)
                      (throw cleanup))

                  (p15-s23-b2-c17-gate-b-interrupt-like? error)
                  (.addSuppressed ^Throwable error cleanup)

                  (p15-s23-b2-c17-gate-b-interrupt-like? cleanup)
                  (do (.addSuppressed ^Throwable cleanup error)
                      (throw cleanup))

                  :else (.addSuppressed ^Throwable error cleanup)))
              (do
                (p15-s23-b2-c17-gate-b-restore-interrupt! cleanup)
                (throw cleanup)))))))))

(defn- p15-s23-b2-c17-gate-b-normalized-command-argument
  [argument]
  (cond
    (and (string? argument)
         (str/starts-with? argument "-fuse-ld="))
    "-fuse-ld=<effective-ld>"
    (and (string? argument) (str/includes? argument ".sdk"))
    "<sdk-root>"
    (and (string? argument) (str/ends-with? argument "/usr/bin/clang"))
    "<effective-clang>"
    (and (string? argument) (str/ends-with? argument "/usr/bin/ld"))
    "<effective-ld>"
    (and (string? argument)
         (or (str/ends-with? argument "/usr/bin/otool")
             (str/ends-with? argument "/usr/bin/llvm-otool")))
    "<effective-otool>"
    (= "/usr/bin/file" argument) "<effective-file>"
    :else argument)))
