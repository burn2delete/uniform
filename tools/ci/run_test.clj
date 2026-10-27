(ns ci.run-test
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.test :refer [deftest is run-tests]]))

(binding [*command-line-args* nil] (load-file "tools/ci/run.clj"))
(require '[ci.run :as ci])
(binding [*command-line-args* nil] (load-file "tools/ci/lint.clj"))
(require '[ci.lint :as lint])

(defn fixture-tool! [directory argv input allowed-exits]
  (let [log (java.io.File/createTempFile "tool-" ".log" directory)
        process (.start (doto (ProcessBuilder. ^java.util.List argv)
                          (.directory directory) (.redirectErrorStream true) (.redirectOutput log)))]
    (try
      (with-open [stdin (.getOutputStream process)]
        (.write stdin (.getBytes input java.nio.charset.StandardCharsets/UTF_8)))
      (when-not (.waitFor process 10 java.util.concurrent.TimeUnit/SECONDS)
        (throw (ex-info "Fixture tool timed out" {:argv argv})))
      (when-not (contains? allowed-exits (.exitValue process))
        (throw (ex-info "Fixture tool failed" {:argv argv :log (slurp log)})))
      (slurp log)
      (finally
        (when (.isAlive process) (.destroyForcibly process))
        (.waitFor process 5 java.util.concurrent.TimeUnit/SECONDS)))))

(defn lint-fixture! [directory before after]
  (spit (io/file directory "before.clj") before)
  (spit (io/file directory "after.clj") after)
  (let [findings (fn [source]
                   (:findings (edn/read-string
                               (fixture-tool! directory
                                              ["clj-kondo" "--repro" "--cache" "false" "--config"
                                               "{:output {:format :edn}}" "--lint" "-" "--filename" "sample.clj"]
                                              source #{0 2 3}))))
        changes (lint/hunks (fixture-tool! directory
                                           ["git" "diff" "--no-index" "--no-ext-diff" "--no-textconv"
                                            "--text" "--unified=0" "--" "before.clj" "after.clj"] "" #{0 1}))]
    (lint/classify (findings before) (findings after)
                   {"sample.clj" {:before (str/split-lines before) :after (str/split-lines after)
                                  :changes changes}})))

(deftest actual-linter-and-git-diff-preserve-introduced-diagnostics
  (let [directory (.toFile (java.nio.file.Files/createTempDirectory
                            "uniform-ci-lint-" (make-array java.nio.file.attribute.FileAttribute 0)))]
    (try
      (doseq [[before after baseline blocking]
              [["(ns sample)\n(let [unused 1] :ok)\n" "(ns sample)\n\n(let [unused 1] :ok)\n" 1 0]
               ["(ns sample)\n(identity 1)\n" "(ns sample)\n(let [unused 1] :ok)\n" 0 1]
               ["(ns sample)\n(let [unused 1] :old)\n" "(ns sample)\n(let [unused 1] :new)\n" 0 1]
               ["(ns sample)\n(let [unused 1] :ok)\n" "(ns sample)\n(let [unused 1] :ok)\n(let [unused 1] :ok)\n" 1 1]
               ["(ns sample)\n(defn- helper [] 1)\n(helper)\n" "(ns sample)\n(defn- helper [] 1)\n" 0 1]
               ["(ns sample)\n" "(ns sample)\n(missing)\n" 0 1]
               ["(ns sample)\n(missing)\n" "(ns sample)\n\n(missing)\n" 0 1]]]
        (let [result (lint-fixture! directory before after)]
          (is (= baseline (count (:baseline-warnings result))) (pr-str result))
          (is (= blocking (count (:blocking result))) (pr-str result))))
      (finally (doseq [file (reverse (file-seq directory))] (io/delete-file file))))))

(deftest baseline-matching-and-output-validation-fail-closed
  (let [finding {:filename "sample.clj" :row 1 :col 2 :end-row 1 :end-col 3
                 :level :warning :type :unused-binding :message "unused binding x"}
        sources {"sample.clj" {:changes [] :before ["same"] :after ["same"]}}
        error (assoc finding :level :error)
        duplicate (lint/classify [finding] [finding finding] sources)
        directory (.toFile (java.nio.file.Files/createTempDirectory
                            "uniform-ci-lint-output-" (make-array java.nio.file.attribute.FileAttribute 0)))
        log (io/file directory "observation.log")
        receipt (io/file directory "observation.edn")
        result {:findings [finding] :summary {:error 0 :warning 1}}]
    (try
      (is (= 1 (count (:baseline-warnings duplicate))))
      (is (= 1 (count (:blocking duplicate))))
      (is (= [finding] (:blocking (lint/classify [finding] [finding] {}))))
      (is (= [error] (:blocking (lint/classify [error] [error] sources))))
      (spit log (pr-str result))
      (spit receipt (pr-str {:exit-code 2}))
      (is (= [finding] (lint/findings! log #{"sample.clj"} identity)))
      (doseq [[bad-result bad-exit]
              [[result 0]
               [(assoc-in result [:summary :error] 1) 2]
               [(assoc-in result [:findings 0 :filename] "unexpected.clj") 2]
               [(assoc-in result [:findings 0 :level] :unknown) 0]
               [{:findings []} 0]]]
        (spit log (pr-str bad-result))
        (spit receipt (pr-str {:exit-code bad-exit}))
        (is (thrown? clojure.lang.ExceptionInfo (lint/findings! log #{"sample.clj"} identity))))
      (finally (doseq [file (reverse (file-seq directory))] (io/delete-file file))))))

(defn alive? [pid]
  (let [handle (java.lang.ProcessHandle/of pid)]
    (and (.isPresent handle) (.isAlive (.get handle)))))

(deftest ci-sources-pass-the-actual-clojure-reader
  (binding [*read-eval* false]
    (is (thrown? RuntimeException (read-string "#=(+ 1 1)")))
    (is (thrown? IllegalArgumentException (read-string "^String \".\"")))
    (doseq [file (file-seq (io/file "tools/ci"))
            :when (.endsWith (.getName file) ".clj")]
      (with-open [reader (java.io.PushbackReader. (io/reader file))]
        (let [eof (Object.)]
          (is (pos? (loop [forms 0]
                      (if (identical? eof (read {:eof eof} reader))
                        forms
                        (recur (inc forms)))))
              (str file)))))))

(deftest changed-path-base-is-consistent
  (doseq [base [nil "" "main;echo bad" (apply str (repeat 40 "0"))]]
    (is (= "HEAD^" (ci/base-ref base))))
  (let [sha (apply str (repeat 40 "a"))]
    (is (= sha (ci/base-ref sha)))))

(deftest pr-comparison-uses-the-tested-merge-parent
  (let [stale-base (apply str (repeat 40 "a"))
        current-main (apply str (repeat 40 "b"))
        pr-head (apply str (repeat 40 "c"))]
    (is (= current-main (ci/comparison-base "pull_request" stale-base pr-head
                                            [current-main pr-head])))
    (is (= stale-base (ci/comparison-base "push" stale-base nil
                                          [current-main pr-head])))
    (doseq [parents [[] [pr-head] [current-main stale-base]
                     [current-main pr-head stale-base]]]
      (is (thrown? Exception (ci/comparison-base "pull_request" stale-base pr-head parents))))
    (is (thrown? Exception (ci/comparison-base "pull_request" stale-base nil
                                               [current-main pr-head])))))

(deftest platform-leaves-partition-the-entire-strict-catalog
  (binding [*command-line-args* nil]
    (load-file "bootstrap/clojure/test/gravity/bootstrap_free_leaf_test_runner.clj"))
  (let [catalog (var-get (requiring-resolve 'gravity.bootstrap-free-leaf-test-runner/catalog))
        portable (ci/leaf-selection "portable" catalog)
        darwin (ci/leaf-selection "darwin" catalog)]
    (is (= 51 (count portable)))
    (is (= ["darwin-publication"] (mapv :id darwin)))
    (is (= (set catalog) (set (concat portable darwin))))
    (is (= (count catalog) (count (concat portable darwin))))
    (is (thrown? Exception (ci/leaf-selection "unknown" catalog)))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest success-cleans-owned-descendants
  (let [child (ci/command "128m" "-M" "-e" "(Thread/sleep 30000)")
        expression (str "(let [p (.start (ProcessBuilder. " (pr-str child) "))] "
                        "(spit \"target/ci/orphan.pid\" (.pid p)) (Thread/sleep 2000))")
        result (ci/execute! "positive-orphan-cleanup" 20
                            (ci/command "128m" "-M" "-e" expression))
        child-pid (read-string (slurp "target/ci/orphan.pid"))]
    (is (= :passed (:status result)))
    (is (some #{child-pid} (:descendant-pids result)))
    (is (not (alive? child-pid)))
    (is (not (alive? (:pid result))))))

(deftest receipts-preserve-failure-and-launch-errors
  (is (= :failed (:status (ci/execute! "negative-exit" 15
                                       (ci/command "128m" "-M" "-e" "(System/exit 7)")))))
  (is (= 7 (:exit-code (read-string (slurp "target/ci/negative-exit.edn")))))
  (is (= :launch-error (:status (ci/execute! "negative-launch" 1
                                             ["/nonexistent/ci-command"])))))

(deftest timeout-is-a-failure
  (let [result (ci/execute! "negative-timeout" 1
                            (ci/command "128m" "-M" "-e" "(Thread/sleep 30000)"))]
    (is (= :timeout (:status result)))
    (is (not (alive? (:pid result)))))
  (is (= 124 (:exit-code (read-string (slurp "target/ci/negative-timeout.edn"))))))

(let [result (run-tests)]
  (System/exit (if (zero? (+ (:fail result) (:error result))) 0 1)))
