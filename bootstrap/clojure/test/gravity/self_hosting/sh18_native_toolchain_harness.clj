(ns gravity.self-hosting.sh18-native-toolchain-harness
  "Leaf-only external native-toolchain harness for SH-18 preparation.

  This namespace invokes tools directly through ProcessBuilder. It does not use
  a shell, does not perform Gravity lowering, and does not claim SH-18
  completion. The eventual SH-18 integration must supply C or LLVM output
  derived from verified Gravity MIR."
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.io ByteArrayOutputStream InputStream]
           [java.nio.charset StandardCharsets]
           [java.nio.file Files LinkOption Path Paths]
           [java.security MessageDigest]
           [java.util.concurrent Callable Executors Future TimeUnit]))

(def maximum-source-bytes (* 1024 1024))
(def maximum-output-bytes (* 1024 1024))
(def default-timeout-ms 20000)
(def maximum-timeout-ms 60000)
(def cleanup-timeout-ms 3000)
(def maximum-argument-count 128)
(def maximum-argument-bytes 65536)

(def ^:private compiler-candidates ["cc" "clang" "gcc"])
(def ^:private permitted-environment-keys
  ["PATH" "TMPDIR" "SDKROOT" "MACOSX_DEPLOYMENT_TARGET"
   "DEVELOPER_DIR" "SystemRoot"])
(def ^:private strict-c-flags
  ["-std=c11" "-O0" "-Wall" "-Wextra" "-Werror" "-pedantic"])

(defn- fail!
  [id message facts]
  (throw (ex-info message (assoc facts :id id :slice "SH-18"))))

(defn- nul-free-string!
  [slot value]
  (when-not (and (string? value)
                 (not (str/includes? value (str (char 0)))))
    (fail! "SH18-HARNESS-INPUT"
           "Native harness inputs must be NUL-free strings"
           {:slot slot :value value}))
  value)

(defn- bounded-arguments!
  [arguments]
  (let [arguments (mapv #(nul-free-string! :argument %) arguments)
        argument-bytes
        (reduce
         +
         0
         (map #(alength (.getBytes % StandardCharsets/UTF_8)) arguments))]
    (when (< maximum-argument-count (count arguments))
      (fail! "SH18-HARNESS-INPUT"
             "Native harness argument count exceeds its bound"
             {:observed (count arguments)
              :maximum maximum-argument-count}))
    (when (< maximum-argument-bytes argument-bytes)
      (fail! "SH18-HARNESS-INPUT"
             "Native harness argument bytes exceed their bound"
             {:observed argument-bytes
              :maximum maximum-argument-bytes}))
    arguments))

(defn- sha256-bytes
  [bytes]
  (let [digest (.digest (MessageDigest/getInstance "SHA-256") bytes)]
    (str
     "sha256:"
     (apply str (map #(format "%02x" (bit-and 0xff %)) digest)))))

(defn file-sha256
  [path]
  (let [path (if (instance? Path path)
               path
               (.toPath (io/file path)))]
    (sha256-bytes (Files/readAllBytes path))))

(defn- read-bounded-file-bytes
  [^Path path maximum]
  (with-open [input (Files/newInputStream
                     path (make-array java.nio.file.OpenOption 0))
              output (ByteArrayOutputStream.)]
    (let [buffer (byte-array 8192)]
      (loop [observed 0]
        (let [read-count (.read input buffer)]
          (if (neg? read-count)
            (.toByteArray output)
            (let [next-observed (+ observed read-count)]
              (when (< maximum next-observed)
                (fail! "SH18-HARNESS-SOURCE"
                       "C fixture exceeds the native harness source bound"
                       {:source-path (str path)
                        :observed-at-least next-observed
                        :maximum maximum}))
              (.write output buffer 0 read-count)
              (recur next-observed))))))))

(defn- environment
  []
  (let [host (System/getenv)]
    (into
     (sorted-map "LANG" "C" "LC_ALL" "C")
     (keep
      (fn [key]
        (when-let [value (.get host key)]
          [key value])))
     permitted-environment-keys)))

(defn- path-candidates
  [program]
  (let [path (or (System/getenv "PATH") "")]
    (map
     #(Paths/get % (into-array String [program]))
     (str/split path
                (re-pattern
                 (java.util.regex.Pattern/quote
                  java.io.File/pathSeparator))))))

(defn- resolve-executable
  [program]
  (let [program (nul-free-string! :program program)
        direct (Paths/get program (make-array String 0))
        candidates
        (if (.isAbsolute direct)
          [direct]
          (path-candidates program))]
    (some
     (fn [^Path candidate]
       (when (and (Files/isRegularFile
                   candidate (make-array LinkOption 0))
                  (Files/isExecutable candidate))
         (.toRealPath candidate (make-array LinkOption 0))))
     candidates)))

(defn- bounded-positive-integer!
  [slot value maximum]
  (when-not (and (integer? value)
                 (pos? value)
                 (<= value maximum))
    (fail! "SH18-HARNESS-INPUT"
           "Native harness numeric bound is outside its permitted range"
           {:slot slot :value value :maximum maximum}))
  (long value))

(defn- captured-text
  [^ByteArrayOutputStream output]
  (String. (.toByteArray output) StandardCharsets/UTF_8))

(defn- capture-bounded-stream
  [^InputStream input stream maximum readiness-marker readiness]
  (with-open [input input
              output (ByteArrayOutputStream.)]
    (let [buffer (byte-array 8192)]
      (loop [captured 0]
        (let [observed (.read input buffer)]
          (if (neg? observed)
            {:status :complete
             :stream stream
             :captured-bytes captured
             :text (captured-text output)}
            (let [remaining (- maximum captured)
                  retained (min remaining observed)
                  next-captured (+ captured retained)]
              (when (pos? retained)
                (.write output buffer 0 retained))
              (when (and readiness-marker
                         (not (realized? readiness))
                         (str/includes?
                          (captured-text output)
                          readiness-marker))
                (deliver readiness true))
              (if (< retained observed)
                {:status :limit-exceeded
                 :stream stream
                 :captured-bytes next-captured
                 :observed-at-least (+ captured observed)
                 :maximum maximum
                 :text (captured-text output)}
                (recur next-captured)))))))))

(defn- completed-future-value
  [^Future future]
  (when (.isDone future)
    (.get future)))

(defn- observed-process-tree
  [^Process process]
  (vec (iterator-seq (.iterator (.descendants process)))))

(defn- terminate-process-tree!
  [^Process process]
  (let [deadline (+ (System/nanoTime) (* cleanup-timeout-ms 1000000))
        root (.toHandle process)
        observed (atom (set (observed-process-tree process)))]
    ;; Retain handles before terminating the root. Once the root exits, live
    ;; descendants may be reparented and disappear from ProcessHandle's
    ;; descendants view.
    (doseq [^java.lang.ProcessHandle handle (reverse (vec @observed))]
      (when (.isAlive handle)
        (.destroy handle)))
    (when (.isAlive root)
      (.destroy root))
    (loop [forced? false]
      (when (.isAlive root)
        (swap! observed into (observed-process-tree process)))
      (let [handles (conj @observed root)
            alive (vec (filter #(.isAlive ^java.lang.ProcessHandle %)
                               handles))
            now (System/nanoTime)]
        (cond
          (empty? alive)
          {:complete? true
           :root-alive? false
           :descendants-alive 0
           :observed-descendants (count @observed)
           :forced? forced?}

          (<= deadline now)
          {:complete? false
           :root-alive? (.isAlive root)
           :descendants-alive
           (count (filter #(.isAlive ^java.lang.ProcessHandle %) @observed))
           :observed-descendants (count @observed)
           :alive-pids
           (mapv #(.pid ^java.lang.ProcessHandle %) alive)
           :forced? forced?}

          :else
          (do
            (doseq [^java.lang.ProcessHandle handle alive]
              (.destroyForcibly handle))
            (Thread/sleep 10)
            (recur true)))))))

(defn- close-process-streams!
  [^Process process]
  (doseq [stream [(.getOutputStream process)
                  (.getInputStream process)
                  (.getErrorStream process)]]
    (try
      (.close stream)
      (catch Exception _))))

(defn- await-capture
  [^Future future]
  (.get future cleanup-timeout-ms TimeUnit/MILLISECONDS))

(defn run-bounded-process
  "Run one command without a shell and return bounded stdout/stderr.

  The caller supplies a complete argument vector. Environment inheritance is
  replaced by a small, explicit toolchain environment."
  [{:keys [command working-directory timeout-ms max-output-bytes
           readiness-stdout readiness-timeout-ms]
    :or {timeout-ms default-timeout-ms
         max-output-bytes maximum-output-bytes
         readiness-timeout-ms 5000}}]
  (let [command (bounded-arguments! command)
        timeout-ms
        (bounded-positive-integer!
         :timeout-ms timeout-ms maximum-timeout-ms)
        max-output-bytes
        (bounded-positive-integer!
         :max-output-bytes max-output-bytes maximum-output-bytes)
        readiness-stdout
        (when readiness-stdout
          (nul-free-string! :readiness-stdout readiness-stdout))
        _ (when (and readiness-stdout (str/blank? readiness-stdout))
            (fail! "SH18-HARNESS-INPUT"
                   "Native harness readiness marker cannot be blank"
                   {:slot :readiness-stdout}))
        readiness-timeout-ms
        (bounded-positive-integer!
         :readiness-timeout-ms
         readiness-timeout-ms
         maximum-timeout-ms)
        _ (when (empty? command)
            (fail! "SH18-HARNESS-INPUT"
                   "Native harness command cannot be empty"
                   {:slot :command}))
        executable (or (resolve-executable (first command))
                       (fail! "SH18-HARNESS-TOOL"
                              "Native harness executable is unavailable"
                              {:program (first command)}))
        directory
        (when working-directory
          (.toRealPath
           (.toPath (io/file working-directory))
           (make-array LinkOption 0)))
        started (System/nanoTime)
        readiness-deadline
        (+ started (* readiness-timeout-ms 1000000))
        readiness (promise)
        builder (ProcessBuilder.
                 ^java.util.List
                 (into [(str executable)] (rest command)))
        executor (Executors/newFixedThreadPool 2)]
    (try
      (when directory
        (.directory builder (.toFile directory)))
      (let [process-environment (.environment builder)]
        (.clear process-environment)
        (doseq [[key value] (environment)]
          (.put process-environment key value)))
      (let [process (.start builder)]
        (try
          (.close (.getOutputStream process))
          (let [stdout-future
                (.submit
                 executor
                 ^Callable
                 (reify Callable
                   (call [_]
                     (capture-bounded-stream
                      (.getInputStream process)
                      :stdout
                      max-output-bytes
                      readiness-stdout
                      readiness))))
                stderr-future
                (.submit
                 executor
                 ^Callable
                 (reify Callable
                   (call [_]
                     (capture-bounded-stream
                      (.getErrorStream process)
                      :stderr
                      max-output-bytes
                      nil
                      readiness))))
                outcome
                (loop [execution-deadline
                       (when-not readiness-stdout
                         (+ started (* timeout-ms 1000000)))]
                  (let [stdout-result
                        (completed-future-value stdout-future)
                        stderr-result
                        (completed-future-value stderr-future)
                        overflow
                        (some
                         #(when (= :limit-exceeded (:status %)) %)
                         [stdout-result stderr-result])
                        ready? (or (nil? readiness-stdout)
                                   (realized? readiness))
                        execution-deadline
                        (if (and ready? (nil? execution-deadline))
                          (+ (System/nanoTime) (* timeout-ms 1000000))
                          execution-deadline)
                        now (System/nanoTime)]
                    (cond
                      overflow
                      {:status :output-limit-exceeded
                       :overflow overflow}

                      (and (not (.isAlive process))
                           stdout-result
                           stderr-result)
                      {:status
                       (if ready?
                         :completed
                         :readiness-missing)}

                      (and (not ready?)
                           (<= readiness-deadline now))
                      {:status :readiness-timed-out}

                      (and ready?
                           (<= execution-deadline now))
                      {:status :timed-out}

                      :else
                      (do
                        (Thread/sleep 5)
                        (recur execution-deadline)))))
                cleanup
                (when (contains? #{:timed-out
                                   :readiness-timed-out
                                   :output-limit-exceeded}
                                 (:status outcome))
                  (terminate-process-tree! process))
                _ (when (and cleanup (not (:complete? cleanup)))
                    (close-process-streams! process)
                    (fail!
                     "SH18-HARNESS-CLEANUP"
                     "Native subprocess tree did not terminate within its bound"
                     {:command (vec command)
                      :outcome outcome
                      :cleanup cleanup
                      :cleanup-timeout-ms cleanup-timeout-ms}))
                stdout-result (await-capture stdout-future)
                stderr-result (await-capture stderr-future)
                duration-ms
                (long (/ (- (System/nanoTime) started) 1000000))]
            (cond-> {:status (:status outcome)
                     :exit-code
                     (when (= :completed (:status outcome))
                       (.exitValue process))
                     :stdout (:text stdout-result)
                     :stderr (:text stderr-result)
                     :stdout-bytes (:captured-bytes stdout-result)
                     :stderr-bytes (:captured-bytes stderr-result)
                     :readiness-required? (boolean readiness-stdout)
                     :readiness-observed?
                     (boolean (and readiness-stdout
                                   (realized? readiness)))
                     :duration-ms duration-ms}
              (= :timed-out (:status outcome))
              (assoc :timeout-ms timeout-ms
                     :cleanup cleanup)

              (= :readiness-timed-out (:status outcome))
              (assoc :readiness-timeout-ms readiness-timeout-ms
                     :readiness-stdout readiness-stdout
                     :cleanup cleanup)

              (= :readiness-missing (:status outcome))
              (assoc :readiness-stdout readiness-stdout)

              (= :output-limit-exceeded (:status outcome))
              (assoc :maximum-output-bytes max-output-bytes
                     :overflow (:overflow outcome)
                     :cleanup cleanup)))
          (finally
            (when (.isAlive process)
              (terminate-process-tree! process))
            (close-process-streams! process))))
      (finally
        (.shutdownNow executor)))))

(defn discover-toolchain
  "Return a concrete external C compiler identity or an explicit unavailable
  record. GRAVITY_SH18_CC may name an absolute executable or a PATH program."
  []
  (let [requested (System/getenv "GRAVITY_SH18_CC")
        candidates (if (str/blank? requested)
                     compiler-candidates
                     [requested])
        executable
        (some
         (fn [candidate]
           (when-let [path (resolve-executable candidate)]
             [candidate path]))
         candidates)]
    (if-not executable
      {:status :unavailable
       :schema :gravity/sh18-native-toolchain-probe-v1
       :requested requested
       :candidates (vec candidates)}
      (let [[selected path] executable
            version
            (run-bounded-process
             {:command [(str path) "--version"]
              :timeout-ms 5000})
            target
            (run-bounded-process
             {:command [(str path) "-dumpmachine"]
              :timeout-ms 5000})
            version-text (str (:stdout version) (:stderr version))
            family (cond
                     (re-find #"(?i)clang" version-text) :clang
                     (re-find #"(?i)gcc|free software foundation"
                              version-text) :gcc
                     :else :unknown)
            identity-input
            (str (str path) "\n" version-text "\n"
                 (:stdout target) (:stderr target))]
        (when-not (and (= :completed (:status version))
                       (zero? (:exit-code version))
                       (= :completed (:status target))
                       (zero? (:exit-code target)))
          (fail! "SH18-HARNESS-TOOL"
                 "Native compiler identity probes failed"
                 {:compiler (str path)
                  :version-probe version
                  :target-probe target}))
        {:status :available
         :schema :gravity/sh18-native-toolchain-probe-v1
         :selected selected
         :compiler-path (str path)
         :compiler-family family
         :version-line (first (str/split-lines version-text))
         :target (str/trim (:stdout target))
         :toolchain-id
         (sha256-bytes (.getBytes identity-input StandardCharsets/UTF_8))
         :environment-keys (vec (keys (environment)))
         :shell-used? false}))))

(defn- require-contained-output!
  [working-directory output-path]
  (let [as-file
        (fn [value]
          (if (instance? Path value)
            (.toFile ^Path value)
            (io/file value)))
        root (.toPath (.getCanonicalFile (as-file working-directory)))
        ;; getCanonicalFile resolves existing parent-directory aliases even
        ;; though the output itself does not exist yet. This is significant on
        ;; macOS, where /var and /private/var name the same temporary root.
        output (.toPath (.getCanonicalFile (as-file output-path)))]
    (when-not (.startsWith output root)
      (fail! "SH18-HARNESS-OUTPUT-PATH"
             "Native output path escapes its working directory"
             {:working-directory (str root)
              :output-path (str output)}))
    output))

(defn compile-c
  [{:keys [toolchain source-path output-path working-directory extra-flags]
    :or {extra-flags []}}]
  (when-not (= :available (:status toolchain))
    (fail! "SH18-HARNESS-TOOL"
           "C compilation requires an available toolchain"
           {:toolchain toolchain}))
  (when-not working-directory
    (fail! "SH18-HARNESS-INPUT"
           "C compilation requires a contained working directory"
           {:slot :working-directory}))
  (let [source (.toRealPath (.toPath (io/file source-path))
                            (make-array LinkOption 0))
        source-bytes
        (read-bounded-file-bytes source maximum-source-bytes)
        source-size (alength source-bytes)
        root (.toRealPath (.toPath (io/file working-directory))
                          (make-array LinkOption 0))
        output (require-contained-output! root output-path)
        frozen-source
        (Files/createTempFile
         root "sh18-frozen-source-" ".c"
         (make-array java.nio.file.attribute.FileAttribute 0))
        flags (bounded-arguments! extra-flags)
        command
        (into
         [(:compiler-path toolchain)]
         (concat strict-c-flags flags [(str frozen-source)
                                       "-o" (str output)]))]
    (try
      (Files/write frozen-source source-bytes
                   (make-array java.nio.file.OpenOption 0))
      (let [process
            (run-bounded-process
             {:command command
              :working-directory (str root)})]
        {:schema :gravity/sh18-native-compile-result-v1
         :status
         (if (and (= :completed (:status process))
                  (zero? (:exit-code process))
                  (Files/isRegularFile output (make-array LinkOption 0)))
           :accepted
           :rejected)
         :source-path (str source)
         :source-hash (sha256-bytes source-bytes)
         :source-bytes source-size
         :source-frozen? true
         :output-path (str output)
         :flags (vec (concat strict-c-flags flags))
         :toolchain-id (:toolchain-id toolchain)
         :target (:target toolchain)
         :process process
         :executable-hash
         (when (Files/isRegularFile output (make-array LinkOption 0))
           (file-sha256 output))})
      (finally
        (Files/deleteIfExists frozen-source)))))

(defn execute-native
  [{:keys [executable-path arguments working-directory timeout-ms]
    :or {arguments [] timeout-ms default-timeout-ms}}]
  (let [executable (.toRealPath (.toPath (io/file executable-path))
                                (make-array LinkOption 0))
        process
        (run-bounded-process
         {:command (into [(str executable)] arguments)
          :working-directory working-directory
          :timeout-ms timeout-ms})]
    {:schema :gravity/sh18-native-execution-result-v1
     :status
     (cond
       (= :timed-out (:status process)) :timed-out
       (zero? (:exit-code process)) :accepted
       :else :rejected)
     :executable-path (str executable)
     :executable-hash (file-sha256 executable)
     :arguments (vec arguments)
     :process process}))

(defn with-temporary-directory
  [prefix f]
  (let [directory (Files/createTempDirectory
                   prefix
                   (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (f directory)
      (finally
        (doseq [file (reverse (file-seq (.toFile directory)))]
          (Files/deleteIfExists (.toPath file)))))))

(defn exercise-c-fixture
  [{:keys [source-path arguments timeout-ms]
    :or {arguments [] timeout-ms default-timeout-ms}}]
  (let [toolchain (discover-toolchain)]
    (when-not (= :available (:status toolchain))
      (fail! "SH18-HARNESS-TOOL"
             "SH-18 fixture execution requires an external C compiler"
             {:toolchain toolchain}))
    (with-temporary-directory
      "gravity-sh18-native-"
      (fn [directory]
        (let [output (.resolve directory "fixture-executable")
              compilation
              (compile-c
               {:toolchain toolchain
                :source-path source-path
                :output-path (str output)
                :working-directory (str directory)})
              execution
              (when (= :accepted (:status compilation))
                (execute-native
                 {:executable-path (str output)
                  :arguments arguments
                  :working-directory (str directory)
                  :timeout-ms timeout-ms}))]
          {:schema :gravity/sh18-native-harness-result-v1
           :slice "SH-18"
           :status
           (cond
             (= :rejected (:status compilation)) :compile-rejected
             (= :timed-out (:status execution)) :execution-timed-out
             (= :accepted (:status execution)) :executed
             :else :execution-rejected)
           :toolchain toolchain
           :compilation compilation
           :execution execution
           :claims
           {:external-toolchain-harness? true
            :gravity-derived-input? false
            :verified-mir-input? false
            :sh18-complete? false}})))))

(defn -main
  [& arguments]
  (case (first arguments)
    "--probe"
    (println (pr-str (discover-toolchain)))

    "--fixture"
    (let [source-path (second arguments)]
      (when-not source-path
        (fail! "SH18-HARNESS-USAGE"
               "--fixture requires a C source path"
               {:arguments (vec arguments)}))
      (let [result
            (exercise-c-fixture
             {:source-path source-path
              :arguments (vec (drop 2 arguments))})]
        (println (pr-str result))
        (when-not (= :executed (:status result))
          (System/exit 1))))

    (fail! "SH18-HARNESS-USAGE"
           "Use --probe or --fixture <c-source> [arguments...]"
           {:arguments (vec arguments)})))
