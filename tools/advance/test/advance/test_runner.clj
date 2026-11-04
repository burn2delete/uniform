(ns advance.test-runner
  (:require [clojure.test :as test]
            [advance.catalog-test]
            [advance.coordination-test]
            [advance.workflow-test]
            [simplify.core-test]
            [simplify.coordination-test]
            [simplify.workflow-test]))

(defn -main [& _]
  (let [{:keys [fail error]} (test/run-tests 'advance.catalog-test 'advance.coordination-test 'advance.workflow-test
                                          'simplify.core-test 'simplify.coordination-test 'simplify.workflow-test)]
    (shutdown-agents)
    (when (pos? (+ fail error)) (System/exit 1))))
