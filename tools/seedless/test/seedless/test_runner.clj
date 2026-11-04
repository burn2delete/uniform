(ns seedless.test-runner
  (:require [clojure.test :as test]
            [seedless.workflow-test]))

(defn -main [& _]
  (let [result (test/run-tests 'seedless.workflow-test)]
    (shutdown-agents)
    (when (pos? (+ (:fail result) (:error result))) (System/exit 1))))
