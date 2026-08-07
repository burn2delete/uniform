(ns gravity.self-hosting.sh01-development-test-runner-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.self-hosting.sh01-development-test-runner :as runner]))

(deftest thrown-gate-cleans-up-and-propagates
  (let [cleaned? (atom false)
        failure (ex-info "test failure" {:kind :fixture})
        run-gate-var (ns-resolve 'gravity.self-hosting.sh01-development-test-runner
                                 'run-gate)
        cleanup-var (ns-resolve 'gravity.self-hosting.sh01-development-test-runner
                                'cleanup!)]
    (with-redefs-fn
      {run-gate-var (fn [] (throw failure))
       cleanup-var (fn [] (reset! cleaned? true))}
      (fn []
        (is (identical? failure
                        (try
                          (runner/-main)
                          nil
                          (catch Throwable thrown
                            thrown))))))
    (is @cleaned?)))
