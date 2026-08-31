(ns gravity.self-hosting.sh01-development-test-runner
  "Bounded, non-authoritative SH-01 incremental planner and runner unit gate."
  (:require [clojure.test :as test]
            [gravity.self-hosting.sh01-incremental-check-test]
            [gravity.self-hosting.sh01-impact-test-planner-test]
            [gravity.self-hosting.sh01-parallel-test-runner-test]))

(def ^:private test-namespaces
  '[gravity.self-hosting.sh01-incremental-check-test
    gravity.self-hosting.sh01-impact-test-planner-test
    gravity.self-hosting.sh01-parallel-test-runner-test])

(defn- namespace-result
  [namespace]
  (let [summary (test/run-tests namespace)
        passed? (and (zero? (:fail summary))
                     (zero? (:error summary)))]
    (array-map
     :namespace namespace
     :status (if passed? :passed :failed)
     :test (:test summary)
     :pass (:pass summary)
     :fail (:fail summary)
     :error (:error summary))))

(defn run-gate
  "Runs the SH-01 unit namespaces in a fixed order and returns a report."
  []
  (let [results (mapv namespace-result test-namespaces)
        summary
        (reduce
         (fn [totals result]
           (merge-with + totals (select-keys result [:test :pass :fail :error])))
         {:test 0 :pass 0 :fail 0 :error 0}
         results)
        passed? (and (zero? (:fail summary))
                     (zero? (:error summary)))]
    (array-map
     :schema :gravity/sh01-development-test-report-v1
     :stage :stage1
     :slice "SH-01"
     :authority :non-authoritative
     :authoritative? false
     :status (if passed? :passed :failed)
     :exit-code (if passed? 0 1)
     :test-namespaces test-namespaces
     :results results
     :summary summary)))

(defn- cleanup!
  "Flushes ordinary output and stops agent executors."
  []
  (flush)
  (.flush *err*)
  (.flush System/out)
  (.flush System/err)
  (shutdown-agents))

(defn -main
  [& arguments]
  (let [result
        (try
          (if (seq arguments)
            (do
              (binding [*out* *err*]
                (println "sh01-development-test-runner accepts no arguments"))
              {:status :usage :exit-code 2})
            (let [report (run-gate)]
              (prn report)
              report))
          (finally
            (cleanup!)))]
    (if (pos? (:exit-code result))
      (System/exit (:exit-code result))
      result)))
