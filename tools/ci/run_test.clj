(ns ci.run-test
  (:require [clojure.test :refer [deftest is run-tests]]))

(binding [*command-line-args* nil] (load-file "tools/ci/run.clj"))
(require '[ci.run :as ci])

(defn alive? [pid]
  (let [handle (java.lang.ProcessHandle/of pid)]
    (and (.isPresent handle) (.isAlive (.get handle)))))

(deftest changed-path-base-is-consistent
  (doseq [base [nil "" "main;echo bad" (apply str (repeat 40 "0"))]]
    (is (= "HEAD^" (ci/base-ref base))))
  (let [sha (apply str (repeat 40 "a"))]
    (is (= sha (ci/base-ref sha)))))

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
