(ns gravity.self-hosting.sh18-native-toolchain-harness-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting.sh18-native-toolchain-harness :as harness])
  (:import [java.nio.charset StandardCharsets]
           [java.nio.file Files]))

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-18")

(defn- fixture
  [kind filename]
  (str fixture-root "/" kind "/" filename))

(defn- available-toolchain
  []
  (let [toolchain (harness/discover-toolchain)]
    (when (= :available (:status toolchain))
      toolchain)))

(defn- write-source!
  [directory filename source]
  (let [path (.resolve directory filename)]
    (Files/write path
                 (.getBytes source StandardCharsets/UTF_8)
                 (make-array java.nio.file.OpenOption 0))
    path))

(defn- compile-generated-source!
  [toolchain directory filename source]
  (let [source-path (write-source! directory filename source)
        output-path (.resolve directory (str filename ".out"))
        compilation
        (harness/compile-c
         {:toolchain toolchain
          :source-path (str source-path)
          :output-path (str output-path)
          :working-directory (str directory)})]
    (is (= :accepted (:status compilation)) compilation)
    {:compilation compilation
     :output-path output-path}))

(deftest sh18-native-toolchain-probe-is-explicit-and-shell-free
  (let [toolchain (harness/discover-toolchain)]
    (is (= :gravity/sh18-native-toolchain-probe-v1
           (:schema toolchain)))
    (is (contains? #{:available :unavailable} (:status toolchain)))
    (when (= :available (:status toolchain))
      (is (string? (:compiler-path toolchain)))
      (is (string? (:version-line toolchain)))
      (is (string? (:target toolchain)))
      (is (re-matches #"sha256:[0-9a-f]{64}"
                      (:toolchain-id toolchain)))
      (is (false? (:shell-used? toolchain)))
      (is (some #{"PATH"} (:environment-keys toolchain)))
      (is (some #{"LC_ALL"} (:environment-keys toolchain))))))

(deftest sh18-native-harness-compiles-and-executes-argv-arithmetic
  (when (available-toolchain)
    (let [result
          (harness/exercise-c-fixture
           {:source-path (fixture "accepted" "argv-arithmetic-stdout.c")
            :arguments ["19" "23"]})
          compilation (:compilation result)
          execution (:execution result)]
      (is (= :executed (:status result)) result)
      (is (= :accepted (:status compilation)) compilation)
      (is (= :accepted (:status execution)) execution)
      (is (= 0 (get-in execution [:process :exit-code])))
      (is (= "sum=42\nargc=3\n"
             (get-in execution [:process :stdout])))
      (is (= "" (get-in execution [:process :stderr])))
      (is (re-matches #"sha256:[0-9a-f]{64}"
                      (:source-hash compilation)))
      (is (true? (:source-frozen? compilation)))
      (is (re-matches #"sha256:[0-9a-f]{64}"
                      (:executable-hash compilation)))
      (is (= (:executable-hash compilation)
             (:executable-hash execution)))
      (is (= {:external-toolchain-harness? true
              :gravity-derived-input? false
              :verified-mir-input? false
              :sh18-complete? false}
             (:claims result))))))

(deftest sh18-native-harness-records-compile-rejection
  (when (available-toolchain)
    (let [result
          (harness/exercise-c-fixture
           {:source-path (fixture "rejected" "compile-error.c")})]
      (is (= :compile-rejected (:status result)) result)
      (is (= :rejected (get-in result [:compilation :status])))
      (is (nil? (:execution result)))
      (is (not (zero?
                (get-in result
                        [:compilation :process :exit-code]))))
      (is (str/includes?
           (get-in result [:compilation :process :stderr])
           "SH18_EXPECTED_COMPILE_REJECTION")))))

(deftest sh18-native-harness-preserves-nonzero-exit-and-stderr
  (when (available-toolchain)
    (let [result
          (harness/exercise-c-fixture
           {:source-path (fixture "rejected" "runtime-nonzero.c")})]
      (is (= :execution-rejected (:status result)) result)
      (is (= :accepted (get-in result [:compilation :status])))
      (is (= :rejected (get-in result [:execution :status])))
      (is (= 23 (get-in result [:execution :process :exit-code])))
      (is (= "" (get-in result [:execution :process :stdout])))
      (is (= "SH18_EXPECTED_RUNTIME_REJECTION\n"
             (get-in result [:execution :process :stderr]))))))

(deftest sh18-native-harness-rejects-signed-addition-overflow
  (when (available-toolchain)
    (let [result
          (harness/exercise-c-fixture
           {:source-path (fixture "accepted" "argv-arithmetic-stdout.c")
            :arguments [(str Long/MAX_VALUE) "1"]})]
      (is (= :execution-rejected (:status result)) result)
      (is (= 67 (get-in result [:execution :process :exit-code])))
      (is (= "integer addition overflow\n"
             (get-in result [:execution :process :stderr]))))))

(deftest sh18-native-harness-times-out-and-reaps-the-process
  (when (available-toolchain)
    (let [result
          (harness/exercise-c-fixture
           {:source-path (fixture "rejected" "runtime-timeout.c")
            :timeout-ms 250})
          process (get-in result [:execution :process])]
      (is (= :execution-timed-out (:status result)) result)
      (is (= :timed-out (:status process)))
      (is (= 250 (:timeout-ms process)))
      (is (false? (get-in process [:cleanup :root-alive?])))
      (is (zero? (get-in process [:cleanup :descendants-alive]))))))

(deftest sh18-native-harness-stops-output-at-the-live-capture-bound
  (when-let [toolchain (available-toolchain)]
    (harness/with-temporary-directory
      "gravity-sh18-output-bound-"
      (fn [directory]
        (let [{:keys [output-path]}
              (compile-generated-source!
               toolchain directory "output-flood.c"
               (str "#include <stdio.h>\n"
                    "int main(void) {\n"
                    "  for (;;) {\n"
                    "    if (fputs(\"0123456789abcdef\", stdout) == EOF) {\n"
                    "      return 71;\n"
                    "    }\n"
                    "  }\n"
                    "}\n"))
              process
              (harness/run-bounded-process
               {:command [(str output-path)]
                :working-directory (str directory)
                :timeout-ms 5000
                :max-output-bytes 4096})
              finite-output
              (:output-path
               (compile-generated-source!
                toolchain directory "finite-output.c"
                (str "#include <stdio.h>\n"
                     "int main(void) {\n"
                     "  int index = 0;\n"
                     "  for (index = 0; index < 1024; index += 1) {\n"
                     "    if (fputs(\"0123456789abcdef\", stdout) == EOF) {\n"
                     "      return 71;\n"
                     "    }\n"
                     "  }\n"
                     "  return 0;\n"
                     "}\n")))
              finite-process
              (harness/run-bounded-process
               {:command [(str finite-output)]
                :working-directory (str directory)
                :timeout-ms 5000
                :max-output-bytes 4096})]
          (is (= :output-limit-exceeded (:status process)) process)
          (is (= :stdout (get-in process [:overflow :stream])))
          (is (= 4096 (:stdout-bytes process)))
          (is (<= (:stderr-bytes process) 4096))
          (is (true? (get-in process [:cleanup :complete?])))
          (is (false? (get-in process [:cleanup :root-alive?])))
          (is (zero? (get-in process [:cleanup :descendants-alive])))
          (is (= :output-limit-exceeded (:status finite-process))
              finite-process)
          (is (= 4096 (:stdout-bytes finite-process)))
          (is (true? (get-in finite-process [:cleanup :complete?]))))))))

(deftest sh18-native-harness-reaps-observed-descendants-before-return
  (when-let [toolchain (available-toolchain)]
    (when (re-find #"(?i)darwin|linux|bsd" (:target toolchain))
      (harness/with-temporary-directory
        "gravity-sh18-process-tree-"
        (fn [directory]
          (let [{:keys [output-path]}
                (compile-generated-source!
                 toolchain directory "process-tree.c"
                 (str "#define _POSIX_C_SOURCE 200809L\n"
                      "#include <stdio.h>\n"
                      "#include <sys/types.h>\n"
                      "#include <unistd.h>\n"
                      "int main(void) {\n"
                      "  pid_t child = fork();\n"
                      "  if (child < 0) {\n"
                      "    return 70;\n"
                      "  }\n"
                      "  if (child > 0) {\n"
                      "    if (fputs(\"child-ready\\n\", stdout) == EOF) {\n"
                      "      return 71;\n"
                      "    }\n"
                      "    if (fflush(stdout) != 0) {\n"
                      "      return 72;\n"
                      "    }\n"
                      "  }\n"
                      "  for (;;) {\n"
                      "    pause();\n"
                      "  }\n"
                      "}\n"))
                process
                (harness/run-bounded-process
                 {:command [(str output-path)]
                  :working-directory (str directory)
                  :readiness-stdout "child-ready\n"
                  :readiness-timeout-ms 5000
                  :timeout-ms 250})]
            (is (= :timed-out (:status process)) process)
            (is (true? (:readiness-observed? process)))
            (is (= "child-ready\n" (:stdout process)))
            (is (true? (get-in process [:cleanup :complete?])))
            (is (pos? (get-in process [:cleanup :observed-descendants])))
            (is (false? (get-in process [:cleanup :root-alive?])))
            (is (zero? (get-in process
                               [:cleanup :descendants-alive])))))))))

(deftest sh18-native-harness-rejects-output-path-escape-and-nul-arguments
  (when-let [toolchain (available-toolchain)]
    (harness/with-temporary-directory
      "gravity-sh18-containment-"
      (fn [directory]
        (let [outside
              (.resolve (.getParent directory)
                        "gravity-sh18-outside-executable")
              output-error
              (try
                (harness/compile-c
                 {:toolchain toolchain
                  :source-path
                  (fixture "accepted" "argv-arithmetic-stdout.c")
                  :output-path (str outside)
                  :working-directory (str directory)})
                nil
                (catch clojure.lang.ExceptionInfo error error))
              argument-error
              (try
                (harness/run-bounded-process
                 {:command [(:compiler-path toolchain)
                            (str "bad" (char 0) "argument")]})
                nil
                (catch clojure.lang.ExceptionInfo error error))]
          (is (= "SH18-HARNESS-OUTPUT-PATH"
                 (:id (ex-data output-error))))
          (is (= "SH18-HARNESS-INPUT"
                 (:id (ex-data argument-error)))))))))
