(ns gravity.p15-native-launcher-test
  "Focused Darwin evidence for the identity-bound P15 native launcher.

  These tests compile the launcher and tiny C children with Apple's `cc` into
  owner-only private roots.  They exercise only the launcher command contract,
  pathname admission/mapped-vnode identity, and same-process-group cleanup.
  They deliberately do not exercise `.gravity`/`.qst` routing or claim a
  public/self-hosted release boundary.
  "
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing run-tests]])
  (:import [java.nio.charset StandardCharsets]
           [java.nio.file Files LinkOption Path Paths]
           [java.nio.file.attribute PosixFilePermissions]
           [java.util.concurrent TimeUnit]))

(def ^:private compiler "/usr/bin/cc")
(def ^:private c-flags
  ["-std=c11" "-O0" "-Wall" "-Wextra" "-Werror" "-pedantic"])
(def ^:private base-environment
  {"PATH" "/usr/bin:/bin:/usr/sbin:/sbin"
   "LANG" "C"
   "LC_ALL" "C"})
(def ^:private no-follow-options
  (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
(def ^:private launcher-source-relative
  "bootstrap/native/p15_public_native_launcher.c")
(def ^:private fixture-root-relative
  "bootstrap/clojure/fixtures/p15-native-launcher")
(def ^:private cleanup-timeout-ms 3000)
(def ^:private output-limit-bytes (* 128 1024))

(defn- repository-root
  []
  (let [resource (io/resource "gravity/p15_native_launcher_test.clj")]
    (when-not resource
      (throw (ex-info "P15 native launcher test source is not on the classpath"
                      {:id "P15NL-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "P15 native launcher repository root is unavailable"
                        {:id "P15NL-TEST-ROOT"}))

        (Files/isRegularFile
         (.resolve candidate "deps.edn")
         (make-array LinkOption 0))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (.resolve ^Path @root relative))

(defn- darwin-toolchain-available?
  []
  (and (= "Mac OS X" (System/getProperty "os.name"))
       (Files/isExecutable
        (Paths/get compiler (make-array String 0)))))

(defn- owner-only-attributes
  []
  (into-array java.nio.file.attribute.FileAttribute
              [(PosixFilePermissions/asFileAttribute
                (PosixFilePermissions/fromString "rwx------"))]))

(defn- private-root!
  []
  (Files/createTempDirectory "gravity-p15-native-launcher-"
                             (owner-only-attributes)))

(defn- delete-tree!
  "Delete one test-owned tree without following symlinks, with a hard bound.

  A failed cleanup is intentionally surfaced by the test instead of being
  allowed to turn into an unbounded recursive delete."
  [^Path directory]
  (when (Files/exists directory no-follow-options)
    (let [entries
          (with-open [stream (Files/walk directory
                                         (make-array java.nio.file.FileVisitOption 0))]
            (vec (iterator-seq (.iterator stream))))]
      (when (< 10000 (count entries))
        (throw (ex-info "P15 native launcher test cleanup exceeded its bound"
                        {:id "P15NL-TEST-CLEANUP-BOUND"
                         :entries (count entries)})))
      (doseq [entry (reverse entries)]
        (Files/deleteIfExists ^Path entry)))))

(defn- with-private-root
  [f]
  (let [directory (private-root!)]
    (try
      (f directory)
      (finally
        (delete-tree! directory)))))

(defn- read-bounded-text
  [^Path file]
  (if (Files/exists file no-follow-options)
    (let [bytes (Files/readAllBytes file)]
      (when (< output-limit-bytes (alength bytes))
        (throw (ex-info "P15 native launcher test output exceeded its bound"
                        {:id "P15NL-TEST-OUTPUT-BOUND"
                         :path (str file)
                         :maximum output-limit-bytes
                         :observed (alength bytes)})))
      (String. bytes StandardCharsets/UTF_8))
    ""))

(defn- process-descendants
  [^Process process]
  (vec (iterator-seq (.iterator (.descendants process)))))

(defn- terminate-process-tree!
  "Bounded fallback cleanup for a malformed launcher under test.

  The assertions record whether the launcher cleaned up before this fallback;
  this function only prevents a broken negative test from leaking a process."
  [^Process process]
  (let [handles (conj (set (process-descendants process)) (.toHandle process))]
    (doseq [^java.lang.ProcessHandle handle handles]
      (when (.isAlive handle)
        (.destroyForcibly handle)))
    (.waitFor process cleanup-timeout-ms TimeUnit/MILLISECONDS)
    (doseq [^java.lang.ProcessHandle handle handles]
      (when (.isAlive handle)
        (.destroyForcibly handle)))))

(defn- run-command!
  "Run an argv directly, capturing bounded output in the private root."
  [{:keys [^Path working-directory command environment timeout-ms]
    :or {environment {}
         timeout-ms 5000}}]
  (let [stdout-file (.resolve working-directory "test-stdout")
        stderr-file (.resolve working-directory "test-stderr")
        builder (doto (ProcessBuilder. ^java.util.List (vec command))
                  (.directory (.toFile working-directory))
                  (.redirectOutput (.toFile stdout-file))
                  (.redirectError (.toFile stderr-file)))
        process-environment (.environment builder)]
    (.clear process-environment)
    (doseq [[key value] (merge base-environment environment)]
      (.put process-environment key value))
    (try
      (let [process (.start builder)
            completed? (.waitFor process (long timeout-ms)
                                 TimeUnit/MILLISECONDS)
            forced-cleanup? (not completed?)]
        (when forced-cleanup?
          (terminate-process-tree! process))
        {:command (vec command)
         :exit (when completed? (.exitValue process))
         :completed? completed?
         :forced-cleanup? forced-cleanup?
         :out (read-bounded-text stdout-file)
         :err (read-bounded-text stderr-file)})
      (finally
        (Files/deleteIfExists stdout-file)
        (Files/deleteIfExists stderr-file)))))

(defn- compile-source!
  [^Path working-directory source-path output-name extra-flags]
  (let [output-path (.resolve working-directory output-name)
        result
        (run-command!
         {:working-directory working-directory
          :command (vec (concat [compiler]
                                c-flags
                                extra-flags
                                [(str source-path) "-o" (str output-path)]))
          :timeout-ms 120000})]
    (assoc result
           :source-path (str source-path)
           :output-path output-path
           :accepted? (and (:completed? result)
                           (zero? (long (or (:exit result) -1)))
                           (Files/isRegularFile output-path no-follow-options)
                           (Files/isExecutable output-path)))))

(defn- compile-fixture!
  [^Path working-directory filename output-name]
  (compile-source!
   working-directory
   (path (str fixture-root-relative "/" filename))
   output-name
   []))

(defn- compile-launcher!
  [^Path working-directory testing?]
  (compile-source!
   working-directory
   (path launcher-source-relative)
   "launcher"
   (if testing?
     ["-DGRAVITY_NATIVE_LAUNCHER_TESTING"]
     [])))

(defn- diagnostic-text
  [result]
  (str (:err result) (:out result)))

(defn- assert-diagnostic
  [result id]
  (is (= 125 (:exit result))
      (assoc result :expected-diagnostic id
             :expected-exit 125))
  (is (str/includes? (diagnostic-text result) id)
      (assoc result :expected-diagnostic id)))

(defn- run-command-with-signal!
  "Start one command, invoke `signal-f` while it is running, then capture it.

  The callback receives the live Process and may return evidence that is
  merged into the bounded result.  It is used only for the SIGTERM regression;
  ordinary command tests continue to use `run-command!`."
  [{:keys [^Path working-directory command environment timeout-ms]
    :or {environment {}
         timeout-ms 5000}}
   signal-f]
  (let [stdout-file (.resolve working-directory "signal-test-stdout")
        stderr-file (.resolve working-directory "signal-test-stderr")
        builder (doto (ProcessBuilder. ^java.util.List (vec command))
                  (.directory (.toFile working-directory))
                  (.redirectOutput (.toFile stdout-file))
                  (.redirectError (.toFile stderr-file)))
        process-environment (.environment builder)]
    (.clear process-environment)
    (doseq [[key value] (merge base-environment environment)]
      (.put process-environment key value))
    (let [process (.start builder)]
      (try
        (let [signal-evidence (or (signal-f process) {})
              completed? (.waitFor process (long timeout-ms)
                                   TimeUnit/MILLISECONDS)
              forced-cleanup? (not completed?)]
          (when forced-cleanup?
            (terminate-process-tree! process))
          (merge signal-evidence
                 {:command (vec command)
                  :exit (when completed? (.exitValue process))
                  :completed? completed?
                  :forced-cleanup? forced-cleanup?
                  :out (read-bounded-text stdout-file)
                  :err (read-bounded-text stderr-file)}))
        (catch Throwable error
          (when (.isAlive process)
            (terminate-process-tree! process))
          (throw error))
        (finally
          (Files/deleteIfExists stdout-file)
          (Files/deleteIfExists stderr-file))))))

(defn- wait-until
  [predicate timeout-ms]
  (let [deadline (+ (System/nanoTime) (* (long timeout-ms) 1000000))]
    (loop []
      (if (predicate)
        true
        (if (<= deadline (System/nanoTime))
          false
          (do
            (Thread/sleep 10)
            (recur)))))))

(defn- pid-from-file
  [^Path file]
  (Long/parseLong (str/trim (read-bounded-text file))))

(defn- pid-alive?
  [pid]
  (let [handle (java.lang.ProcessHandle/of (long pid))]
    (and (.isPresent handle)
         (.isAlive (.get handle)))))

(defn- cleanup-pid!
  [pid]
  (when (pid-alive? pid)
    (when-let [handle (.orElse (java.lang.ProcessHandle/of (long pid)) nil)]
      (.destroyForcibly ^java.lang.ProcessHandle handle))
    (wait-until #(not (pid-alive? pid)) cleanup-timeout-ms)))

(defn- launcher-command
  [launcher timeout-ms executable & arguments]
  (into [(str (:output-path launcher))
         "--timeout-ms" (str timeout-ms)
         "--"
         (str executable)]
        (map str arguments)))

(deftest p15-native-launcher-accepts-stdout-exit-and-argv
  (when (darwin-toolchain-available?)
    (with-private-root
      (fn [directory]
        (let [launcher (compile-launcher! directory false)
              child (compile-fixture! directory "argv_stdout.c" "child")]
          (is (:accepted? launcher) launcher)
          (is (:accepted? child) child)
          (when (and (:accepted? launcher) (:accepted? child))
            (let [result
                  (run-command!
                   {:working-directory directory
                    :command (launcher-command
                              launcher 2000 (:output-path child)
                              "alpha" "beta")
                    :timeout-ms 5000})]
              (is (:completed? result) result)
              (is (= 0 (:exit result)) result)
              (is (= "child-ok\narg[1]=alpha\narg[2]=beta\n"
                     (:out result)) result)
              (is (= "" (:err result)) result))))))))

(deftest p15-native-launcher-preserves-child-nonzero-exit
  (when (darwin-toolchain-available?)
    (with-private-root
      (fn [directory]
        (let [launcher (compile-launcher! directory false)
              child (compile-fixture! directory "exit_23.c" "child")]
          (is (:accepted? launcher) launcher)
          (is (:accepted? child) child)
          (when (and (:accepted? launcher) (:accepted? child))
            (let [result
                  (run-command!
                   {:working-directory directory
                    :command (launcher-command launcher 2000 (:output-path child))
                    :timeout-ms 5000})]
              (is (:completed? result) result)
              (is (= 23 (:exit result)) result)
              (is (= "child-exit-23\n" (:err result)) result))))))))

(deftest p15-native-launcher-rejects-relative-symlink-and-nonregular-targets
  (when (darwin-toolchain-available?)
    (with-private-root
      (fn [directory]
        (let [launcher (compile-launcher! directory false)
              child (compile-fixture! directory "argv_stdout.c" "child")
              link (.resolve directory "child-link")
              nonregular (.resolve directory "not-regular")]
          (is (:accepted? launcher) launcher)
          (is (:accepted? child) child)
          (when (and (:accepted? launcher) (:accepted? child))
            (Files/createSymbolicLink link (:output-path child)
                                      (make-array java.nio.file.attribute.FileAttribute 0))
            (Files/createDirectory nonregular (owner-only-attributes))
            (let [relative
                  (run-command!
                   {:working-directory directory
                    :command (launcher-command launcher 2000
                                                "relative-child")
                    :timeout-ms 5000})
                  symlink
                  (run-command!
                   {:working-directory directory
                    :command (launcher-command launcher 2000 link)
                    :timeout-ms 5000})
                  directory-result
                  (run-command!
                   {:working-directory directory
                    :command (launcher-command launcher 2000 nonregular)
                    :timeout-ms 5000})]
              (testing "relative executable paths are rejected before open"
                (is (:completed? relative) relative)
                (assert-diagnostic relative "P15NL003"))
              (testing "symlink executable paths are rejected without follow"
                (is (:completed? symlink) symlink)
                (assert-diagnostic symlink "P15NL004"))
              (testing "non-regular executable paths are rejected"
                (is (:completed? directory-result) directory-result)
                (assert-diagnostic directory-result "P15NL005")))))))))

(deftest p15-native-launcher-testing-replacement-rejects-before-child-code
  (when (darwin-toolchain-available?)
    (with-private-root
      (fn [directory]
        (let [launcher (compile-launcher! directory true)
              target (compile-fixture! directory "marker.c" "target")
              replacement (compile-fixture! directory "marker.c" "replacement")
              marker (.resolve directory "child-ran")]
          (is (:accepted? launcher) launcher)
          (is (:accepted? target) target)
          (is (:accepted? replacement) replacement)
          (when (and (:accepted? launcher)
                     (:accepted? target)
                     (:accepted? replacement))
            (let [result
                  (run-command!
                   {:working-directory directory
                    :environment
                    {"GRAVITY_NATIVE_LAUNCHER_TEST_REPLACEMENT"
                     (str (:output-path replacement))}
                    :command (launcher-command launcher 2000
                                                (:output-path target)
                                                marker)
                    :timeout-ms 5000})]
              (is (:completed? result) result)
              (is (not= 0 (:exit result)) result)
              (assert-diagnostic result "P15NL009")
              (is (not (Files/exists marker no-follow-options))
                  {:result result :marker marker}))))))))

(deftest p15-native-launcher-timeout-kills-same-process-group
  (when (darwin-toolchain-available?)
    (with-private-root
      (fn [directory]
        (let [launcher (compile-launcher! directory false)
              child (compile-fixture! directory "timeout_group.c" "child")
              ready (.resolve directory "timeout-ready")
              pid-file (.resolve directory "timeout-child.pid")]
          (is (:accepted? launcher) launcher)
          (is (:accepted? child) child)
          (when (and (:accepted? launcher) (:accepted? child))
            (let [process-result
                  (run-command!
                   {:working-directory directory
                    :command (launcher-command launcher 1000
                                                (:output-path child)
                                                ready pid-file)
                    :timeout-ms 5000})
                  ready? (wait-until #(Files/exists ready no-follow-options) 1000)
                  pid (when (and ready?
                                 (Files/exists pid-file no-follow-options))
                        (pid-from-file pid-file))
                  gone-before-fallback?
                  (if pid
                    (wait-until #(not (pid-alive? pid)) 2000)
                    false)]
              (is (:completed? process-result) process-result)
              (assert-diagnostic process-result "P15NL011")
              (is ready? {:result process-result :ready ready})
              (is (some? pid) {:result process-result :pid-file pid-file})
              (is (true? gone-before-fallback?)
                  {:result process-result :pid pid})
              (when pid
                (cleanup-pid! pid)))))))))

(deftest p15-native-launcher-sigterm-cleans-child-group-and-returns-diagnostic
  (when (darwin-toolchain-available?)
    (with-private-root
      (fn [directory]
        (let [launcher (compile-launcher! directory false)
              child (compile-fixture! directory "timeout_group.c" "child")
              ready (.resolve directory "signal-ready")
              pid-file (.resolve directory "signal-child.pid")
              signal-evidence (atom {})]
          (is (:accepted? launcher) launcher)
          (is (:accepted? child) child)
          (when (and (:accepted? launcher) (:accepted? child))
            (let [result
                  (run-command-with-signal!
                   {:working-directory directory
                    :command (launcher-command launcher 10000
                                                (:output-path child)
                                                ready pid-file)
                    :timeout-ms 5000}
                   (fn [^Process process]
                     (let [ready? (wait-until
                                   #(Files/exists ready no-follow-options)
                                   2000)
                           pid (when (and ready?
                                          (Files/exists pid-file no-follow-options))
                                 (pid-from-file pid-file))]
                       (reset! signal-evidence
                               {:ready? ready?
                                :pid pid})
                       ;; Process.destroy() is the Java POSIX SIGTERM path.
                       (.destroy process)
                       @signal-evidence)))
                  {:keys [ready? pid]} @signal-evidence
                  gone-before-fallback?
                  (if pid
                    (wait-until #(not (pid-alive? pid)) 2000)
                    false)]
              (is (:completed? result) result)
              (assert-diagnostic result "P15NL015")
              (is ready? {:result result :ready ready})
              (is (some? pid) {:result result :pid-file pid-file})
              (is (true? gone-before-fallback?)
                  {:result result :pid pid})
              (when pid
                (cleanup-pid! pid)))))))))

(deftest p15-native-launcher-rejects-leader-exit-and-reaps-same-group
  (when (darwin-toolchain-available?)
    (with-private-root
      (fn [directory]
        (let [launcher (compile-launcher! directory false)
              child (compile-fixture! directory "leader_descendant.c" "child")
              ready (.resolve directory "leader-ready")
              pid-file (.resolve directory "leader-child.pid")]
          (is (:accepted? launcher) launcher)
          (is (:accepted? child) child)
          (when (and (:accepted? launcher) (:accepted? child))
            (let [process-result
                  (run-command!
                   {:working-directory directory
                    :command (launcher-command launcher 2000
                                                (:output-path child)
                                                ready pid-file)
                    :timeout-ms 5000})
                  ready? (wait-until #(Files/exists ready no-follow-options) 1000)
                  pid (when (and ready?
                                 (Files/exists pid-file no-follow-options))
                        (pid-from-file pid-file))
                  gone-before-fallback?
                  (if pid
                    (wait-until #(not (pid-alive? pid)) 2000)
                    false)]
              (is (:completed? process-result) process-result)
              (is (not= 0 (:exit process-result)) process-result)
              (assert-diagnostic process-result "P15NL012")
              (is ready? {:result process-result :ready ready})
              (is (some? pid) {:result process-result :pid-file pid-file})
              (is (true? gone-before-fallback?)
                  {:result process-result :pid pid})
              (when pid
                (cleanup-pid! pid)))))))))

(deftest p15-native-launcher-cli-rejects-invalid-timeout-and-argv
  (when (darwin-toolchain-available?)
    (with-private-root
      (fn [directory]
        (let [launcher (compile-launcher! directory false)]
          (is (:accepted? launcher) launcher)
          (when (:accepted? launcher)
            (let [missing-separator
                  (run-command!
                   {:working-directory directory
                    :command [(str (:output-path launcher))]
                    :timeout-ms 5000})
                  invalid-timeout
                  (run-command!
                   {:working-directory directory
                    :command [(str (:output-path launcher))
                              "--timeout-ms" "0" "--" "/bin/echo"]
                    :timeout-ms 5000})]
              (assert-diagnostic missing-separator "P15NL001")
              (assert-diagnostic invalid-timeout "P15NL002"))))))))

(defn -main
  [& _]
  (let [result (run-tests 'gravity.p15-native-launcher-test)]
    (when (or (pos? (:fail result)) (pos? (:error result)))
      (System/exit 1))))
