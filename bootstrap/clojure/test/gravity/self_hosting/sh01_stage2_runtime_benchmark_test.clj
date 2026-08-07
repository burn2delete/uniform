(ns gravity.self-hosting.sh01-stage2-runtime-benchmark-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting.sh01-stage2-runtime-benchmark :as benchmark]))

(deftest benchmark-report-is-complete-and-non-authoritative
  (let [workload-names (set (keys (benchmark/workloads)))
        result
        (benchmark/run-benchmark
         {:warmup-iterations 10
          :measurement-iterations 100
          :rounds 1})]
    (is (= :gravity/sh01-stage2-runtime-benchmark (:artifact result)))
    (is (= :non-authoritative (:authority result)))
    (is (false? (:authoritative? result)))
    (is (= :performance-regression-feedback (:purpose result)))
    (is (= workload-names (set (keys (:results result)))))
    (is (= 10 (count workload-names)))
    (doseq [[name measurement] (:results result)]
      (is (= 1 (count (:samples-ns measurement))) name)
      (is (pos? (:median-ns measurement)) name)
      (is (pos? (:median-ms measurement)) name)
      (is (pos? (:operations-per-second measurement)) name))))

(deftest benchmark-cli-options-are-explicit-and-fail-closed
  (is (= {:warmup-iterations 10
          :measurement-iterations 20
          :rounds 3
          :workload :interpreted-count}
         (benchmark/parse-arguments
          ["--warmup" "10"
           "--iterations" "20"
           "--rounds" "3"
           "--workload" "interpreted-count"])))
  (is (= #{:interpreted-count}
         (set
          (keys
           (:results
            (benchmark/run-benchmark
             {:warmup-iterations 10
              :measurement-iterations 20
              :rounds 1
              :workload :interpreted-count}))))))
  (testing "invalid values and unknown options are rejected"
    (is (= "SH01-STAGE2-BENCHMARK-ARGUMENT"
           (:id
            (ex-data
             (try
               (benchmark/parse-arguments ["--rounds" "0"])
               (catch clojure.lang.ExceptionInfo error error))))))
    (is (= "SH01-STAGE2-BENCHMARK-USAGE"
           (:id
            (ex-data
             (try
               (benchmark/parse-arguments ["--unknown" "1"])
               (catch clojure.lang.ExceptionInfo error error))))))
    (is (= "SH01-STAGE2-BENCHMARK-WORKLOAD"
           (:id
            (ex-data
             (try
               (benchmark/run-benchmark
                {:warmup-iterations 1
                 :measurement-iterations 1
                 :rounds 1
                 :workload :absent})
               (catch clojure.lang.ExceptionInfo error error))))))))
