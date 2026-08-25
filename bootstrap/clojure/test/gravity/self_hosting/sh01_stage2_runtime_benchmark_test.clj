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
    (is (= 25 (count workload-names)))
    (is (contains? workload-names :interpreted-assoc-three))
    (is (contains? workload-names :legacy-carrier-assoc))
    (is (contains? workload-names :interpreted-equality))
    (is (contains? workload-names :legacy-carrier-equality))
    (doseq [sentinel [:interpreted-get
                      :interpreted-count
                      :interpreted-map-predicate
                      :interpreted-map-literal
                      :interpreted-binary-add
                      :interpreted-ternary-add
                      :interpreted-four-argument-add]]
      (is (contains? workload-names sentinel) sentinel))
    (is (true? ((get (benchmark/workloads) :interpreted-equality))))
    (is (true? ((get (benchmark/workloads) :legacy-carrier-equality))))
    (is (= {:existing 1 :value 2}
           ((get (benchmark/workloads) :interpreted-assoc-three))))
    (is (= {:existing 1 :value 2}
           ((get (benchmark/workloads) :legacy-carrier-assoc))))
    (is (= 3 ((get (benchmark/workloads) :interpreted-binary-add))))
    (is (= 6 ((get (benchmark/workloads) :interpreted-ternary-add))))
    (is (= 10
           ((get (benchmark/workloads) :interpreted-four-argument-add))))
    (doseq [[name measurement] (:results result)]
      (is (= 1 (count (:samples-ns measurement))) name)
      (is (pos? (:median-ns measurement)) name)
      (is (pos? (:median-ms measurement)) name)
      (is (pos? (:operations-per-second measurement)) name)
      (is (boolean? (:allocation-telemetry-available? measurement)) name)
      (when (:allocation-telemetry-available? measurement)
        (is (= 1 (count (:samples-allocated-bytes measurement))) name)
        (is (<= 0 (:median-allocated-bytes measurement)) name)
        (is (<= 0.0 (:median-allocated-bytes-per-operation measurement))
            name)))))

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
