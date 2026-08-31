(defn- p15-s23-b3-llvm-run-process
  [candidate directory command timeout-ms source-path]
  (p15-s23-b3-llvm-require-authority!
   candidate source-path :run-bounded-process)
  (let [builder (ProcessBuilder. ^java.util.List command)
        _ (.directory builder (.toFile directory))
        environment (.environment builder)
        _ (.clear environment)
        _ (.put environment "PATH" "/usr/bin:/bin:/usr/sbin:/sbin")
        _ (.put environment "LC_ALL" "C")
        _ (.put environment "LANG" "C")
        _ (when-let [developer-dir
                     (get-in p15-s23-b3-llvm-environment-policy
                             [:fixed-values "DEVELOPER_DIR"])]
            (.put environment "DEVELOPER_DIR" developer-dir))
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
            (future (p15-s23-b3-llvm-read-bounded-stream
                     candidate (.getInputStream process) source-path))
            _ (reset! stdout-holder stdout-future)
            stderr-future
            (future (p15-s23-b3-llvm-read-bounded-stream
                     candidate (.getErrorStream process) source-path))
            _ (reset! stderr-holder stderr-future)
            finished? (.waitFor process timeout-ms
                                java.util.concurrent.TimeUnit/MILLISECONDS)]
        (let [termination
              (if finished?
                {:kill-requested? false
                 :captured-process-set-reaped? :not-applicable
                 :whole-process-tree-reaping-proved? false
                 :root-alive-after-kill? false
                 :descendants-alive-after-kill 0}
                (assoc
                 (p15-s23-b3-llvm-destroy-process-tree!
                  candidate process source-path)
                 :whole-process-tree-reaping-proved? false))
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
                        (instance? InterruptedException cause)
                        (do (.interrupt (Thread/currentThread))
                            (throw cause))
                        (instance? Exception cause) (throw cause)
                        :else (throw wrapped))))))
              stdout (deref-stream stdout-future)
              stderr (deref-stream stderr-future)]
          (when-not (:stream-read-complete? stdout)
            (future-cancel stdout-future))
          (when-not (:stream-read-complete? stderr)
            (future-cancel stderr-future))
          {:command-role :bounded-external-tool
           :finished? finished?
           :timed-out? (not finished?)
           :exit-code (when finished? (.exitValue process))
           :termination termination
           :stdout stdout
           :stderr stderr}))
      (catch InterruptedException interrupted
        (reset! primary-failure interrupted)
        (try
          (p15-s23-b3-llvm-destroy-process-tree!
           candidate process source-path)
          (catch Exception cleanup
            (.addSuppressed interrupted cleanup)))
        (when-let [stream-future @stdout-holder]
          (future-cancel stream-future))
        (when-let [stream-future @stderr-holder]
          (future-cancel stream-future))
        (.interrupt (Thread/currentThread))
        (throw interrupted))
      (catch Throwable error
        (reset! primary-failure error)
        (throw error))
      (finally
        (try
          (when (.isAlive process)
            (p15-s23-b3-llvm-destroy-process-tree!
             candidate process source-path))
          (doseq [holder [stdout-holder stderr-holder]]
            (when-let [stream-future @holder]
              (when-not (future-done? stream-future)
                (future-cancel stream-future))))
          (catch Throwable cleanup
            (if-let [error @primary-failure]
              (cond
                (instance? Error error)
                (.addSuppressed ^Throwable error cleanup)

                (instance? Error cleanup)
                (do (.addSuppressed ^Throwable cleanup error)
                    (throw cleanup))

                :else
                (.addSuppressed ^Throwable error cleanup))
              (throw cleanup))))))))

(defn p15-s23-b3-llvm-tool-execution-snapshot
  []
  @p15-s23-b3-llvm-tool-observation-state)

(defn- p15-s23-b3-llvm-normalized-tool-output
  [step text]
  (let [text (or text "")]
    (case step
      (:sdk-path :clang-path :ld-path :otool-path) "<physical-path>\n"
      :clang-version
      (str/replace text #"(?m)^InstalledDir: .+$"
                   "InstalledDir: <effective-clang-directory>")
      :file-version
      (str/replace text "/usr/share/file/magic"
                   "<file-magic-source>")
      text)))

(defn- p15-s23-b3-llvm-normalized-command-argument
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
    (and (string? argument)
         (str/starts-with? argument "type=bind,src=")
         (str/ends-with? argument ",dst=/work"))
    "<workspace-bind:/work>"
    (= "/usr/bin/file" argument) "<effective-file>"
    :else argument))
