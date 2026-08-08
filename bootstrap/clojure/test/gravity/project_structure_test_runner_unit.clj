(ns gravity.project-structure-test-runner-unit
  "Small executable wrapper for the project-structure runner's own tests.

  This node is a prerequisite for the production extraction gate, so changes
  to runner semantics cannot be hidden behind the larger compatibility batch."
  (:require [clojure.test :as test]
            [gravity.project-structure-test-runner-test]))

(defn- cleanup!
  []
  (flush)
  (shutdown-agents)
  (flush))

(defn -main
  [& _args]
  (try
    (let [result (test/run-tests 'gravity.project-structure-test-runner-test)
          exit-code (if (and (zero? (:fail result))
                             (zero? (:error result)))
                      0
                      1)]
      (cleanup!)
      (System/exit exit-code))
    (catch Throwable ex
      (cleanup!)
      (throw ex))))
