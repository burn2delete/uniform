(ns gravity.self-hosting.sh01-parallel-test-runner-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting.sh01-parallel-test-runner :as runner]))

(defn- plan
  [shards]
  {:schema :gravity/sh01-impact-test-plan-v1
   :namespaces (mapv :namespace shards)
   :shards (vec shards)})

(defn- shard
  [namespace slice resource-class]
  {:namespace (symbol namespace)
   :slice slice
   :resource-class resource-class})

(defn- exception-data
  [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo exception
      (ex-data exception))))

(deftest schedule-declares-bounded-phases-and-exclusive-drain-order
  (let [impact-plan
        (plan
         [(shard "gravity.self-hosting.normal-a-test" "SH-01" :normal)
          (shard "gravity.self-hosting.normal-b-test" "SH-02" :normal)
          (shard "gravity.self-hosting.normal-c-test" "SH-03" :normal)
          (shard "gravity.self-hosting.memory-a-test" "SH-07" :memory-heavy)
          (shard "gravity.self-hosting.memory-b-test" "SH-07" :memory-heavy)
          (shard "gravity.self-hosting.exclusive-a-test" "SH-26" :exclusive)
          (shard "gravity.self-hosting.exclusive-b-test" "SH-27" :exclusive)])
        schedule (runner/schedule-plan impact-plan
                                      {:normal-parallelism 2
                                       :memory-parallelism 1})
        phases (:phases schedule)]
    (is (= [:parallel :exclusive] (mapv :phase phases)))
    (is (= {:normal 2 :memory-heavy 1}
           (get-in schedule [:parallel-phase :capacities])))
    (is (= 1 (get-in schedule [:exclusive-phase :capacity])))
    (is (= ["gravity.self-hosting.normal-a-test"
            "gravity.self-hosting.normal-b-test"
            "gravity.self-hosting.normal-c-test"]
           (mapv #(str (:namespace %))
                 (get-in schedule [:parallel-phase :normal]))))
    (is (= ["gravity.self-hosting.memory-a-test"
            "gravity.self-hosting.memory-b-test"]
           (mapv #(str (:namespace %))
                 (get-in schedule [:parallel-phase :memory-heavy]))))
    (is (= ["gravity.self-hosting.exclusive-a-test"
            "gravity.self-hosting.exclusive-b-test"]
           (mapv #(str (:namespace %))
                 (get-in schedule [:exclusive-phase :exclusive]))))))

(deftest memory-heavy-concurrency-is-fixed-at-one
  (let [impact-plan
        (plan
         [(shard "gravity.self-hosting.memory-a-test" "SH-07" :memory-heavy)
          (shard "gravity.self-hosting.memory-b-test" "SH-07" :memory-heavy)])
        active (atom 0)
        maximum (atom 0)
        report
        (runner/execute-plan
         impact-plan
         {:worker
          (fn [_]
            (let [current (swap! active inc)]
              (swap! maximum max current)
              (Thread/sleep 10)
              (swap! active dec)
              {:exit-code 0}))})]
    (is (= 1 @maximum))
    (is (zero? (:exit-code report)))
    (is (= "SH01-PARALLEL-MEMORY-LIMIT"
           (:id
            (exception-data
             #(runner/schedule-plan impact-plan
                                    {:memory-parallelism 2})))))
    (is (= "SH01-PARALLEL-MEMORY-LIMIT"
           (:id
            (exception-data
             #(runner/parse-arguments
               ["--slice" "SH-07" "--memory-parallelism" "2"])))))))

(deftest parallel-pools-refill-without-a-fixed-wave-barrier
  (let [memory-started (promise)
        release-memory (promise)
        normal-c-started (promise)
        impact-plan
        (plan
         [(shard "gravity.self-hosting.normal-a-test" "SH-01" :normal)
          (shard "gravity.self-hosting.normal-b-test" "SH-02" :normal)
          (shard "gravity.self-hosting.normal-c-test" "SH-03" :normal)
          (shard "gravity.self-hosting.memory-a-test" "SH-07" :memory-heavy)])
        report-result (promise)
        report-thread
        (Thread.
         (fn []
           (deliver
            report-result
            (runner/execute-plan
             impact-plan
             {:normal-parallelism 2
              :worker
              (fn [job]
                (case (str (:namespace job))
                  "gravity.self-hosting.memory-a-test"
                  (do (deliver memory-started true)
                      @release-memory
                      {:exit-code 0})

                  "gravity.self-hosting.normal-c-test"
                  (do (deliver normal-c-started true) {:exit-code 0})

                  {:exit-code 0}))}))))]
    (.setDaemon report-thread true)
    (.start report-thread)
    (is (= true (deref memory-started 1000 ::timeout)))
    (is (= true (deref normal-c-started 1000 ::timeout))
        "normal queue refills while the independent memory lane is occupied")
    (deliver release-memory true)
    (is (zero? (:exit-code (deref report-result 1000 {:exit-code 1}))))))

(deftest execution-keeps-deterministic-report-order
  (let [impact-plan
        (plan
         [(shard "gravity.self-hosting.z-test" "SH-01" :normal)
          (shard "gravity.self-hosting.a-test" "SH-02" :normal)
          (shard "gravity.self-hosting.m-test" "SH-03" :normal)])
        worker
        (fn [job]
          ;; Deliberately finish in the reverse lexical order.
          (Thread/sleep (case (str (:namespace job))
                          "gravity.self-hosting.a-test" 20
                          "gravity.self-hosting.m-test" 5
                          1))
          {:exit-code 0
           :stdout (str "out:" (:namespace job))
           :stderr ""
           :elapsed-ms 7})
        report (runner/execute-plan impact-plan
                                    {:normal-parallelism 3
                                     :worker worker})]
    (is (= '[gravity.self-hosting.a-test
             gravity.self-hosting.m-test
             gravity.self-hosting.z-test]
           (mapv :namespace (:results report))))
    (is (= ["out:gravity.self-hosting.a-test"
            "out:gravity.self-hosting.m-test"
            "out:gravity.self-hosting.z-test"]
           (mapv :stdout (:results report))))
    (is (every? #(number? (:elapsed-ms %)) (:results report)))
    (is (= :passed (:status report)))
    (is (zero? (:exit-code report)))))

(deftest failures-are-collected-and-produce-nonzero-report
  (let [impact-plan
        (plan
         [(shard "gravity.self-hosting.pass-test" "SH-01" :normal)
          (shard "gravity.self-hosting.fail-test" "SH-02" :normal)])
        report
        (runner/execute-plan
         impact-plan
         {:worker
          (fn [job]
            (if (= "gravity.self-hosting.fail-test"
                   (str (:namespace job)))
              {:exit-code 9 :stdout "before-failure" :stderr "bad"}
              {:exit-code 0 :stdout "ok" :stderr ""}))})]
    (is (= :failed (:status report)))
    (is (= 1 (:exit-code report)))
    (is (= ['gravity.self-hosting.fail-test]
           (mapv :namespace (:failures report))))
    (is (= "bad" (:stderr (first (:failures report)))))))

(deftest process-launcher-is-injectable-and-receives-an-argument-vector
  (let [seen (atom nil)
        job {:namespace 'gravity.self-hosting.argument-vector-test
             :slice "SH-01"
             :resource-class :normal}
        result
        (runner/run-namespace-process
         job
         {:working-directory "/tmp"
          :process-launcher
          (fn [command working-directory]
            (reset! seen {:command command
                          :working-directory working-directory})
            {:exit-code 0 :stdout "hello" :stderr ""})})]
    (is (= ["clojure" "-M:test" "--namespace"
            "gravity.self-hosting.argument-vector-test"]
            (:command @seen)))
    (is (= "/tmp" (:working-directory @seen)))
    (is (= :passed (:status result)))
    (is (= "hello" (:stdout result)))
    (is (number? (:elapsed-ms result)))))

(deftest process-timeout-is-reported-as-a-failure
  (let [job {:namespace 'gravity.self-hosting.timeout-test
             :slice "SH-01"
             :resource-class :normal}
        result
        (runner/run-namespace-process
         job
         {:timeout-ms 10
          :working-directory "/tmp"
          :process-launcher
          (fn [_ working-directory]
            (runner/start-process ["/bin/sleep" "1"] working-directory))})]
    (is (= :timeout (:status result)))
    (is (= 124 (:exit-code result)))
    (is (string? (:stdout result)))
    (is (string? (:stderr result)))))

(deftest malformed-and-empty-plans-fail-closed
  (is (= "SH01-PARALLEL-EMPTY-PLAN"
         (:id
          (exception-data
           #(runner/schedule-plan
             {:schema :gravity/sh01-impact-test-plan-v1
              :namespaces []
              :shards []})))))
  (is (= "SH01-PARALLEL-NAMESPACE"
         (:id
          (exception-data
           #(runner/schedule-plan
             {:schema :gravity/sh01-impact-test-plan-v1
              :namespaces ['gravity.self-hosting.valid-test]
              :shards [{:slice "SH-01" :resource-class :normal}]})))))
  (is (= "SH01-PARALLEL-NAMESPACE-DUPLICATE"
         (:id
          (exception-data
           #(runner/schedule-plan
             {:schema :gravity/sh01-impact-test-plan-v1
              :namespaces ['gravity.self-hosting.duplicate-test
                           'gravity.self-hosting.duplicate-test]
              :shards []})))))
  (is (= "SH01-PARALLEL-NAMESPACE-DUPLICATE"
         (:id
          (exception-data
           #(runner/schedule-plan
             {:schema :gravity/sh01-impact-test-plan-v1
              :namespaces ['gravity.self-hosting.duplicate-test]
              :shards
              [(shard "gravity.self-hosting.duplicate-test" "SH-01" :normal)
               (shard "gravity.self-hosting.duplicate-test" "SH-01" :normal)]})))))
  (is (= "SH01-PARALLEL-SELECTION"
         (:id (exception-data #(runner/parse-arguments [])))))
  (is (true? (:help? (runner/parse-arguments ["--help"])))))

(deftest plan-authority-is-explicit-and-fail-closed
  (testing "missing metadata is non-authoritative"
    (let [authority (runner/plan-authority {:schema :gravity/sh01-impact-test-plan-v1})]
      (is (= :non-authoritative (:status authority)))
      (is (false? (:authoritative? authority)))
      (is (= :missing-metadata (:source authority)))))
  (testing "explicit metadata is preserved"
    (is (= {:status :authoritative
            :authoritative? true
            :metadata-present? true
            :source [:metadata :authoritative?]
            :value true
            :conflict? false
            :invalid? false}
           (select-keys
            (runner/plan-authority
             {:metadata {:authoritative? true}})
            [:status :authoritative? :metadata-present? :source :value
             :conflict? :invalid?]))))
  (testing "conflicting recognized markers never promote authority"
    (let [authority
          (runner/plan-authority
           {:authoritative? true
            :metadata {:authority :non-authoritative}})]
      (is (= :non-authoritative (:status authority)))
      (is (false? (:authoritative? authority)))
      (is (true? (:conflict? authority)))
      (is (= :conflicting-authority-markers (:reason authority)))))
  (testing "unknown marker values also fail closed"
    (let [authority (runner/plan-authority {:authoritative? :maybe})]
      (is (= :non-authoritative (:status authority)))
      (is (true? (:invalid? authority))))))

(deftest cli-controls-are-separated-from-planner-selection
  (is (= {:request {:direct-slices #{"SH-01"}}
          :options {:normal-parallelism 4
                    :memory-parallelism 1}
          :mode :slice
          :dry-run? true}
         (select-keys
          (runner/parse-arguments
           ["--slice" "SH-01"
            "--dry-run"
            "--normal-parallelism" "4"
            "--memory-parallelism" "1"])
          [:request :options :mode :dry-run?])))
  (is (= {:request {:expand-dependants? true}
          :mode :changed}
         (select-keys (runner/parse-arguments ["--changed"])
                      [:request :mode])))
  (is (= {:request {:iteration-slices #{"SH-07"}}
          :mode :iteration
          :dry-run? true}
         (select-keys
          (runner/parse-arguments
           ["--iteration-slice" "SH-07" "--changed" "--dry-run"])
          [:request :mode :dry-run?])))
  (is (= {:request
          {:direct-namespaces
           ['gravity.self-hosting.sh07-b48-call-arity-test]}
          :mode :namespace
          :dry-run? true}
         (select-keys
          (runner/parse-arguments
           ["--namespace" "gravity.self-hosting.sh07-b48-call-arity-test"
            "--dry-run"])
          [:request :mode :dry-run?])))
  (is (= "SH01-PARALLEL-USAGE"
         (:id
          (exception-data
           #(runner/parse-arguments ["--iteration-slice" "SH-07"]))))))

(deftest exact-namespace-cli-builds-one-job-without-slice-expansion
  (let [{:keys [mode plan]}
        (runner/build-plan-from-arguments
         ["--namespace" "gravity.self-hosting.sh07-b48-call-arity-test"
          "--dry-run"])]
    (is (= :namespace mode))
    (is (= ['gravity.self-hosting.sh07-b48-call-arity-test]
           (:namespaces plan)))
    (is (= [{:namespace 'gravity.self-hosting.sh07-b48-call-arity-test
             :slice "SH-07"
             :resource-class :memory-heavy}]
           (:shards plan)))
    (is (= :non-authoritative (:authority plan))))
  (is (= "SH01-PARALLEL-USAGE"
         (:id
          (exception-data
           #(runner/parse-arguments
             ["--namespace" "gravity.self-hosting.sh07-b48-call-arity-test"
              "--changed"])))))
  (is (= "SH01-IMPACT-NAMESPACE"
         (:id
          (exception-data
           #(runner/build-plan-from-arguments
             ["--namespace" "gravity.self-hosting.absent-test"])))))
  (doseq [arguments
          [["--namespace" "gravity.self-hosting.sh07-b48-call-arity-test"
            "--expand-dependants"]
           ["--no-expand-dependants"
            "--namespace" "gravity.self-hosting.sh07-b48-call-arity-test"]]]
    (is (= "SH01-PARALLEL-USAGE"
           (:id (exception-data #(runner/parse-arguments arguments)))))))
