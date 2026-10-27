(ns ci.run-test
  (:require [clojure.test :refer [deftest is run-tests]]))

(binding [*command-line-args* nil] (load-file "tools/ci/run.clj"))
(require '[ci.run :as ci])

(deftest receipts-preserve-failure-and-launch-errors
  (is (= :failed (:status (ci/execute! "negative-exit" 15
                                     (ci/command "128m" "-M" "-e" "(System/exit 7)")))))
  (is (= 7 (:exit-code (read-string (slurp "target/ci/negative-exit.edn")))))
  (is (= :launch-error (:status (ci/execute! "negative-launch" 1
                                           ["/nonexistent/ci-command"])))))

(deftest timeout-is-a-failure
  (is (= :timeout (:status (ci/execute! "negative-timeout" 1
                                      (ci/command "128m" "-M" "-e" "(Thread/sleep 30000)")))))
  (is (= 124 (:exit-code (read-string (slurp "target/ci/negative-timeout.edn"))))))

(let [result (run-tests)]
  (System/exit (if (zero? (+ (:fail result) (:error result))) 0 1)))
