(ns seedless.test-runner
  (:require [clojure.test :as test]
            [seedless.workflow-test]
            [seedless.main-invariants-test]
            [seedless.project-identity-test]
            [seedless.controller-invariants-test]))

(defn -main [& _]
  (let [result (test/run-tests 'seedless.workflow-test 'seedless.main-invariants-test
                               'seedless.project-identity-test 'seedless.controller-invariants-test)]
    (shutdown-agents)
    (when (pos? (+ (:fail result) (:error result))) (System/exit 1))))
