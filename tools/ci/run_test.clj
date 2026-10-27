(ns ci.run-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is run-tests]]))

(binding [*command-line-args* nil] (load-file "tools/ci/run.clj"))
(require '[ci.run :as ci])

(defn alive? [pid]
  (let [handle (java.lang.ProcessHandle/of pid)]
    (and (.isPresent handle) (.isAlive (.get handle)))))

(deftest ci-sources-pass-the-actual-clojure-reader
  (doseq [file (file-seq (io/file "tools/ci"))
          :when (.endsWith (.getName file) ".clj")]
    (with-open [reader (java.io.PushbackReader. (io/reader file))]
      (is (pos? (loop [forms 0]
                  (if (= ::eof (read {:eof ::eof} reader))
                    forms
                    (recur (inc forms)))))
          (str file)))))

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
