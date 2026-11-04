(ns simplify.test-runner
  (:require [clojure.test :as test]
            [simplify.core-test]
            [simplify.workflow-test]
            [simplify.coordination-test]
            [simplify.project-membership-test]))

(defn -main [& _]
  (let [{:keys [fail error]} (test/run-tests 'simplify.core-test 'simplify.workflow-test 'simplify.coordination-test
                                              'simplify.project-membership-test)]
    (shutdown-agents)
    (when (pos? (+ fail error)) (System/exit 1))))
