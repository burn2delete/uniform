(ns ci.progress-isolation-test
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.test :refer [deftest is run-tests]]))

(binding [*command-line-args* nil]
  (load-file "bootstrap/clojure/test/gravity/development_test_runner.clj"))
(require '[gravity.development-test-runner :as runner])

(deftest disabled-inner-run-does-not-use-active-outer-state
  (let [outer (#'runner/progress-state)
        output (java.io.StringWriter.)]
    (binding [*err* output]
      (with-bindings {#'runner/*progress-state* outer}
        (is (= 0 (runner/run-cli! ["--help"]))))
      (is (empty? (str output)))
      (is (.isEmpty ^java.util.ArrayList (:errors outer))))))

(deftest enabled-inner-run-owns-its-diagnostics
  (let [outer (#'runner/progress-state)
        inner (#'runner/progress-state)
        output (java.io.StringWriter.)]
    (binding [*err* output]
      (with-bindings {#'runner/*progress-state* outer}
        (#'runner/run-with-progress! inner
          (fn []
            (with-bindings {#'runner/*progress-test-context*
                            {:state inner
                             :namespace "gravity.self-hosting.sh06-unmetered-resolution-test"
                             :test-var "gravity.self-hosting.sh06-unmetered-resolution-test/digest-count-quota-is-gone-but-complete-ordered-requests-remain-required"}}
              (runner/observe-resolution-digest-interval! :positive-resolve (fn [] :value)))))))
    (is (= 2 (count (str/split-lines (str output)))))
    (is (.isEmpty ^java.util.ArrayList (:errors outer)))
    (is (.isEmpty ^java.util.ArrayList (:errors inner)))
    (is (= :return (:edge (edn/read-string (second (str/split-lines (str output)))))))))

(let [result (run-tests)]
  (System/exit (if (zero? (+ (:fail result) (:error result))) 0 1)))
