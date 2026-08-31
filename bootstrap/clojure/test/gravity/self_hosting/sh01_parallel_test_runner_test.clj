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

(defn- batch-result
  [jobs statuses skipped fail-fast?]
  (let [namespaces (vec (sort (map :namespace jobs)))
        attempted-count (- (count namespaces) (count skipped))
        attempted (subvec namespaces 0 attempted-count)
        results
        (mapv (fn [namespace status]
                {:namespace namespace
                 :status status
                 :exit-code (if (= :passed status) 0 1)
                 :attempted? true
                 :elapsed-ms 1
                 :test 1 :pass (if (= :passed status) 1 0)
                 :fail (if (= :failed status) 1 0) :error 0
                 :summary {:test 1
                           :pass (if (= :passed status) 1 0)
                           :fail (if (= :failed status) 1 0)
                           :error 0}
                 :stdout {:text "" :bytes 0 :observed-bytes 0
                          :limit-bytes 8192 :truncated? false}
                 :stderr {:text "" :bytes 0 :observed-bytes 0
                          :limit-bytes 8192 :truncated? false}})
              attempted statuses)
        passed? (and (empty? skipped)
                     (every? #(= :passed (:status %)) results))]
    {:schema :gravity/self-hosting-test-report-v2
     :authority :non-authoritative
     :authoritative? false
     :status (if passed? :passed :failed)
     :exit-code (if passed? 0 1)
     :namespaces namespaces
     :namespace-results results
     :skipped-namespaces (vec skipped)
     :fail-fast? fail-fast?
     :summary {:test (count results)
               :pass (count (filter #(= :passed (:status %)) results))
               :fail (count (filter #(= :failed (:status %)) results))
               :error 0}}))

(deftest normal-jobs-share-bounded-workers-without-mixing-resource-classes
  (let [normal-a (shard "gravity.self-hosting.normal-a-test" "SH-01" :normal)
        normal-b (shard "gravity.self-hosting.normal-b-test" "SH-01" :normal)
        normal-c (shard "gravity.self-hosting.normal-c-test" "SH-01" :normal)
        memory (shard "gravity.self-hosting.memory-test" "SH-07" :memory-heavy)
        exclusive (shard "gravity.self-hosting.exclusive-test" "SH-26" :exclusive)
        batches (atom [])
        isolated (atom [])
        report
        (runner/execute-plan
         (plan [normal-c exclusive normal-a memory normal-b])
         {:normal-parallelism 1
          :normal-batch-size 2
          :normal-batch-worker
          (fn [jobs]
            (swap! batches conj (mapv :namespace jobs))
            (batch-result jobs (repeat (count jobs) :passed) [] false))
          :worker (fn [job]
                    (swap! isolated conj [(:namespace job)
                                          (:resource-class job)])
                    {:exit-code 0})})]
    (is (= '[[gravity.self-hosting.normal-a-test
              gravity.self-hosting.normal-b-test]
             [gravity.self-hosting.normal-c-test]]
           @batches))
    (is (= '[[gravity.self-hosting.memory-test :memory-heavy]
             [gravity.self-hosting.exclusive-test :exclusive]]
           @isolated))
    (is (true? (:normal-batching? report)))
    (is (= 5 (:jobs report)))
    (is (= '[gravity.self-hosting.exclusive-test
             gravity.self-hosting.memory-test
             gravity.self-hosting.normal-a-test
             gravity.self-hosting.normal-b-test
             gravity.self-hosting.normal-c-test]
           (mapv :namespace (:results report))))))

(deftest reviewed-jvm-group-controls-component-test-isolation
  (let [component-id "c11-mir"
        first-job (assoc (shard "gravity.c11-mir-test" nil :normal)
                         :component-id component-id
                         :batch-key "component/c11-mir/leaf")
        second-job
        (assoc (shard "gravity.c11-mir-extra-test"
                      nil :normal)
               :component-id component-id
               :batch-key "component/c11-mir/leaf")
        compatibility-job
        (assoc (shard "gravity.bootstrap-compatibility.c11-test"
                      nil :normal)
               :component-id component-id
               :batch-key "component/c11-mir/compatibility")
        schedule
        (runner/schedule-plan
         (plan [first-job second-job compatibility-job]))]
    (is (= 3 (count (:jobs schedule))))
    (is (= 2 (:planned-jvms schedule)))
    (is (= 2 (count (get-in schedule
                            [:parallel-phase :normal-batches]))))
    (is (= component-id
           (get-in schedule
                   [:parallel-phase :normal-batches 0 :component-id])))
    (is (= ['gravity.bootstrap-compatibility.c11-test]
           (get-in schedule
                   [:parallel-phase :normal-batches 0 :batch-namespaces])))
    (is (= ['gravity.c11-mir-extra-test
            'gravity.c11-mir-test]
           (get-in schedule
                   [:parallel-phase :normal-batches 1 :batch-namespaces])))))

(deftest warm-batch-fail-fast-preserves-attempted-prefix-and-skipped-tail
  (let [first-job (shard "gravity.self-hosting.normal-a-test" "SH-01" :normal)
        second-job (shard "gravity.self-hosting.normal-b-test" "SH-01" :normal)
        third-job (shard "gravity.self-hosting.normal-c-test" "SH-01" :normal)
        exclusive (shard "gravity.self-hosting.exclusive-test" "SH-26" :exclusive)
        report
        (runner/execute-plan
         (plan [first-job second-job third-job exclusive])
         {:normal-parallelism 1
          :normal-batch-size 2
          :fail-fast true
          :normal-batch-worker
          (fn [jobs]
            (batch-result jobs [:failed]
                          [(:namespace (second jobs))] true))
          :worker (fn [_] {:exit-code 0})})]
    (is (= ['gravity.self-hosting.normal-a-test]
           (mapv :namespace (:results report))))
    (is (= '[gravity.self-hosting.exclusive-test
             gravity.self-hosting.normal-b-test
             gravity.self-hosting.normal-c-test]
           (:skipped-namespaces report)))
    (is (= :failed (:status report)))
    (is (true? (:fail-fast-triggered? report)))
    (is (= 1 (:exit-code report)))))

(deftest warm-batch-command-and-resource-boundary-are-explicit
  (let [jobs [(shard "gravity.self-hosting.normal-a-test" "SH-01" :normal)
              (shard "gravity.self-hosting.normal-b-test" "SH-01" :normal)]]
    (is (= ["clojure" "-M:test"
            "--namespace" "gravity.self-hosting.normal-a-test"
            "--namespace" "gravity.self-hosting.normal-b-test"
            "--fail-fast" "--report-file" "/tmp/result.edn"]
           (runner/normal-batch-command jobs "/tmp/result.edn"
                                        {:fail-fast? true})))
    (is (= "SH01-PARALLEL-BATCH-RESOURCE"
           (:id
            (exception-data
             #(runner/run-normal-batch-process
               [(shard "gravity.self-hosting.heavy-test"
                       "SH-07" :memory-heavy)]))))))
  (is (= "SH01-PARALLEL-BATCH-LIMIT"
         (:id
          (exception-data
           #(runner/schedule-plan
             (plan [(shard "gravity.self-hosting.normal-test"
                           "SH-01" :normal)])
             {:normal-batch-size 9})))))
  (let [result
        (runner/run-normal-batch-process
         [(shard "gravity.self-hosting.normal-test" "SH-01" :normal)]
         {:working-directory "/tmp"
          :process-launcher (fn [_ _] {:status :passed :exit-code 0})})]
    (is (= :error (:status result)))
    (is (= 1 (:exit-code result)))
    (is (clojure.string/includes? (:batch-report-error result)
                                  "did not publish result.edn"))))

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
        release-memory (java.util.concurrent.CountDownLatch. 1)
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
                      (.await release-memory 200
                              java.util.concurrent.TimeUnit/MILLISECONDS)
                      {:exit-code 0})

                  "gravity.self-hosting.normal-c-test"
                  (do (deliver normal-c-started true) {:exit-code 0})

                  {:exit-code 0}))}))))]
    (.setDaemon report-thread true)
    (.start report-thread)
    (is (= true (deref memory-started 1000 ::timeout)))
    (is (= true (deref normal-c-started 1000 ::timeout))
        "normal queue refills while the independent memory lane is occupied")
    (.countDown release-memory)
    (is (zero? (:exit-code (deref report-result 1000 {:exit-code 1}))))))

(deftest execution-keeps-deterministic-report-order
  (let [impact-plan
        (plan
         [(shard "gravity.self-hosting.z-test" "SH-01" :normal)
          (shard "gravity.self-hosting.a-test" "SH-02" :normal)
          (shard "gravity.self-hosting.m-test" "SH-03" :normal)])
        worker
        (fn [job]
          ;; Completion timing is irrelevant to the report order.
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

(deftest fail-fast-refills-only-bounded-pools-and-reports-deterministic-skips
  (dotimes [_ 5]
    (let [impact-plan
          (plan
           [(shard "gravity.self-hosting.normal-a-test" "SH-01" :normal)
            (shard "gravity.self-hosting.normal-b-test" "SH-02" :normal)
            (shard "gravity.self-hosting.normal-c-test" "SH-03" :normal)
            (shard "gravity.self-hosting.memory-a-test" "SH-07" :memory-heavy)
            (shard "gravity.self-hosting.memory-b-test" "SH-07" :memory-heavy)
            (shard "gravity.self-hosting.exclusive-a-test" "SH-26" :exclusive)])
          started (atom [])
          normal-started (java.util.concurrent.CountDownLatch. 2)
          release-normal (java.util.concurrent.CountDownLatch. 1)
          allow-memory-return (java.util.concurrent.CountDownLatch. 1)
          memory-returned (java.util.concurrent.CountDownLatch. 1)
          memory-observed (java.util.concurrent.CountDownLatch. 1)
          report-result (promise)
          report-thread
          (Thread.
           (fn []
             (deliver
              report-result
              (runner/execute-plan
               impact-plan
               {:normal-parallelism 2
                :fail-fast true
                :completion-observer
                (fn [job _]
                  (when (= "gravity.self-hosting.memory-a-test"
                           (str (:namespace job)))
                    (.countDown memory-observed)))
                :worker
                (fn [job]
                  (swap! started conj (:namespace job))
                  (case (str (:namespace job))
                    "gravity.self-hosting.memory-a-test"
                    (do
                      (.await normal-started 200
                              java.util.concurrent.TimeUnit/MILLISECONDS)
                      (.await allow-memory-return 200
                              java.util.concurrent.TimeUnit/MILLISECONDS)
                      (try
                        {:status :failed :exit-code 9}
                        (finally (.countDown memory-returned))))

                    (do
                      (.countDown normal-started)
                      (.await release-normal 200
                              java.util.concurrent.TimeUnit/MILLISECONDS)
                      {:exit-code 0})))}))))]
      (.setDaemon report-thread true)
      (.start report-thread)
      (is (.await normal-started 200
                  java.util.concurrent.TimeUnit/MILLISECONDS))
      ;; Keep both normal jobs running while the memory lane reports failure.
      ;; The memory Future is allowed to return and its completion is observed
      ;; before releasing the normal barrier, so queued normal/memory/exclusive
      ;; work has no scheduler race to refill after failure.
      (.countDown allow-memory-return)
      (is (.await memory-returned 200
                  java.util.concurrent.TimeUnit/MILLISECONDS))
      (is (.await memory-observed 200
                  java.util.concurrent.TimeUnit/MILLISECONDS)
          "failure must be observed by the scheduler before peers are released")
      (.countDown release-normal)
      (let [report (deref report-result 1000 {:exit-code 1})]
        (is (= #{'gravity.self-hosting.normal-a-test
                 'gravity.self-hosting.normal-b-test
                 'gravity.self-hosting.memory-a-test}
               (set @started)))
        (is (= '[gravity.self-hosting.exclusive-a-test
                 gravity.self-hosting.memory-b-test
                 gravity.self-hosting.normal-c-test]
               (mapv :namespace (:skipped-jobs report))))
        (is (= (mapv :namespace (:skipped-jobs report))
               (:skipped-namespaces report)))
        (is (= :non-authoritative (:authority report)))
        (is (false? (:ok? report)))
        (is (true? (:fail-fast? report)))))))

(deftest default-execution-collects-all-jobs
  (let [impact-plan
        (plan
         [(shard "gravity.self-hosting.first-test" "SH-01" :normal)
          (shard "gravity.self-hosting.second-test" "SH-02" :normal)
          (shard "gravity.self-hosting.third-test" "SH-03" :normal)])
        started (atom [])
        report
        (runner/execute-plan
         impact-plan
         {:normal-parallelism 1
          :worker (fn [job]
                    (swap! started conj (:namespace job))
                    (if (= "gravity.self-hosting.first-test"
                           (str (:namespace job)))
                      {:exit-code 5}
                      {:exit-code 0}))})]
    (is (= '[gravity.self-hosting.first-test
             gravity.self-hosting.second-test
             gravity.self-hosting.third-test]
           @started))
    (is (= [] (:skipped-namespaces report)))
    (is (true? (:complete? report)))
    (is (= 3 (:jobs report)))))

(deftest cleanup-failure-stops-default-queued-and-exclusive-work
  (let [impact-plan
        (plan
         [(shard "gravity.self-hosting.cleanup-a-test" "SH-01" :normal)
          (shard "gravity.self-hosting.cleanup-b-test" "SH-02" :normal)
          (shard "gravity.self-hosting.cleanup-c-test" "SH-03" :normal)
          (shard "gravity.self-hosting.cleanup-exclusive-test"
                 "SH-26" :exclusive)])
        started (atom [])
        normal-started (java.util.concurrent.CountDownLatch. 2)
        release-normal (java.util.concurrent.CountDownLatch. 1)
        report-result (promise)
        report-thread
        (Thread.
         (fn []
           (deliver
            report-result
            (runner/execute-plan
             impact-plan
             ;; No --fail-fast: an unproven ProcessHandle cleanup is still a
             ;; hard safety stop, while an ordinary failure would be collected.
             {:normal-parallelism 2
              :completion-observer
              (fn [job _]
                (when (= "gravity.self-hosting.cleanup-a-test"
                         (str (:namespace job)))
                  (.countDown release-normal)))
              :worker
              (fn [job]
                (swap! started conj (:namespace job))
                (case (str (:namespace job))
                  "gravity.self-hosting.cleanup-a-test"
                  (do (.countDown normal-started)
                      {:status :timeout
                       :exit-code 124
                       :timeout-containment-unproven? true
                       :cleanup-failed? true
                       :cleanup-complete? false
                       :cleanup {:status :failed
                                 :complete? false
                                 :survivor-pids [4242]}})

                  "gravity.self-hosting.cleanup-b-test"
                  (do (.countDown normal-started)
                      (.await release-normal 200
                              java.util.concurrent.TimeUnit/MILLISECONDS)
                      {:exit-code 0})

                  {:exit-code 0}))}))))]
    (.setDaemon report-thread true)
    (.start report-thread)
    (is (.await normal-started 200
                java.util.concurrent.TimeUnit/MILLISECONDS))
    ;; The completion observer releases the peer only after the scheduler has
    ;; recorded the timeout and stopped refilling either lane.
    (let [report (deref report-result 1000 {:exit-code 1})]
      (is (= #{'gravity.self-hosting.cleanup-a-test
               'gravity.self-hosting.cleanup-b-test}
             (set @started)))
      (is (= '[gravity.self-hosting.cleanup-c-test
               gravity.self-hosting.cleanup-exclusive-test]
             (:skipped-namespaces report)))
      (is (true? (:cleanup-failed? report)))
      (is (false? (:complete? report)))
      (is (false? (:ok? report)))
      (is (= 1 (count (:cleanup-failures report))))
      (is (true? (:timeout-containment-unproven?
                  (first (:cleanup-failures report))))))))

(deftest parallelism-above-java-int-limit-is-rejected
  (is (= "SH01-PARALLEL-OPTION"
         (:id
          (exception-data
           #(runner/schedule-plan
             (plan [(shard "gravity.self-hosting.limit-test"
                            "SH-01" :normal)])
             {:normal-parallelism (inc Integer/MAX_VALUE)}))))))
  (is (= "SH01-PARALLEL-OPTION"
         (:id
          (exception-data
           #(runner/parse-arguments
             ["--slice" "SH-01"
              "--normal-parallelism" (str (inc Integer/MAX_VALUE))])))))

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

(deftest process-output-capture-is-bounded-but-fully-drained
  (let [job {:namespace 'gravity.self-hosting.output-limit-test
             :slice "SH-01"
             :resource-class :normal}
        result
        (runner/run-namespace-process
         job
         {:output-limit-bytes 64
          :output-limit-chars 64
          :working-directory "/tmp"
          :process-launcher
          (fn [_ working-directory]
            (runner/start-process
             ["/bin/sh" "-c"
              "yes x | head -c 100000; yes y | head -c 100000 >&2"]
             working-directory))})]
    (is (= :passed (:status result)))
    (is (true? (:stdout-truncated? result)))
    (is (true? (:stderr-truncated? result)))
    (is (<= (:stdout-bytes result) 64))
    (is (<= (:stderr-bytes result) 64))
    (is (> (:stdout-observed-bytes result) 64))
    (is (> (:stderr-observed-bytes result) 64))
    (is (<= (count (:stdout result)) 64))
    (is (<= (count (:stderr result)) 64))))

(deftest process-output-capture-drops-incomplete-utf8-tail
  (let [job {:namespace 'gravity.self-hosting.utf8-boundary-test
             :slice "SH-01"
             :resource-class :normal}
        result
        (runner/run-namespace-process
         job
         {:output-limit-bytes 2
          :output-limit-chars 64
          :working-directory "/tmp"
          :process-launcher
          (fn [_ working-directory]
            ;; U+2603 is three UTF-8 bytes.  The two-byte cap lands inside it.
            (runner/start-process
             ["/bin/sh" "-c" "printf '\\342\\230\\203x'"]
             working-directory))})]
    (is (= :passed (:status result)))
    (is (true? (:stdout-truncated? result)))
    (is (= "" (:stdout result)))
    (is (not (clojure.string/includes? (:stdout result) "\uFFFD")))
    (is (= 0 (:stdout-bytes result)))
    (is (= 4 (:stdout-observed-bytes result)))))

(deftest process-output-capture-keeps-utf8-boundary-across-read-chunks
  (let [job {:namespace 'gravity.self-hosting.utf8-chunk-boundary-test
             :slice "SH-01"
             :resource-class :normal}
        result
        (runner/run-namespace-process
         job
         {:output-limit-bytes 8193
          :output-limit-chars 9000
          :working-directory "/tmp"
          :process-launcher
          (fn [_ working-directory]
            ;; 8191 ASCII bytes force the three-byte snowman to straddle the
            ;; capture reader's 8192-byte chunk.  The byte cap then cuts the
            ;; snowman after two bytes; no replacement character is allowed.
            (runner/start-process
             ["/bin/sh" "-c"
              "dd if=/dev/zero bs=8191 count=1 2>/dev/null | tr '\\000' a; printf '\\342\\230\\203x'"]
             working-directory))})]
    (is (= :passed (:status result)))
    (is (true? (:stdout-truncated? result)))
    (is (= 8195 (:stdout-observed-bytes result)))
    (is (= 8193 (:stdout-observed-chars result)))
    (is (= 8191 (:stdout-bytes result)))
    (is (= 8191 (count (:stdout result))))
    (is (not (clojure.string/includes? (:stdout result) "\uFFFD")))
    (is (= (apply str (repeat 8191 "a")) (:stdout result)))))

(deftest process-output-capture-propagates-reader-failure
  (let [job {:namespace 'gravity.self-hosting.capture-error-test
             :slice "SH-01"
             :resource-class :normal}
        failing-input
        (proxy [java.io.InputStream] []
          (read [buffer]
            (throw (java.io.IOException. "injected capture failure")))
          (close [] nil))
        process
        (proxy [Process] []
          (getInputStream [] failing-input)
          (getErrorStream [] (java.io.ByteArrayInputStream. (byte-array 0)))
          (getOutputStream [] (java.io.ByteArrayOutputStream.))
          (waitFor [] 0)
          (exitValue [] 0)
          (destroy [] nil)
          (destroyForcibly [] nil)
          (isAlive [] false))
        result
        (runner/run-namespace-process
         job
         {:working-directory "/tmp"
          :process-launcher (fn [_ _] process)})]
    (is (= :error (:status result)))
    (is (= 1 (:exit-code result)))
    (is (true? (:capture-failed? result)))
    (is (= :error (:stdout-capture-status result)))
    (is (clojure.string/includes? (:stdout-capture-error result)
                                  "injected capture failure"))))

(deftest fatal-capture-errors-cross-promise-boundary-unchanged
  (let [job {:namespace 'gravity.self-hosting.capture-fatal-test
             :slice "SH-01"
             :resource-class :normal}
        fatal (OutOfMemoryError. "injected capture VM failure")
        failing-input
        (proxy [java.io.InputStream] []
          (read [buffer]
            (throw fatal))
          (close [] nil))
        process
        (proxy [Process] []
          (getInputStream [] failing-input)
          (getErrorStream [] (java.io.ByteArrayInputStream. (byte-array 0)))
          (getOutputStream [] (java.io.ByteArrayOutputStream.))
          (waitFor [] 0)
          (exitValue [] 0)
          (destroy [] nil)
          (destroyForcibly [] nil)
          (isAlive [] false))
        started (System/nanoTime)]
    (try
      (runner/run-namespace-process
       job
       {:working-directory "/tmp"
        :capture-wait-ms 100
        :process-launcher (fn [_ _] process)})
      (is false "fatal capture error was swallowed")
      (catch Throwable thrown
        (is (identical? fatal thrown)
            "capture must rethrow the original fatal object")
        (is (< (long (/ (- (System/nanoTime) started) 1000000.0))
               1000)
            "fatal capture must not wait for the five-second capture timeout")))))

(deftest normal-parent-exit-with-inherited-pipe-holder-fails-closed
  (let [pid-file
        (java.nio.file.Files/createTempFile
         "sh01-normal-pipe-holder-"
         ".pid"
         (make-array java.nio.file.attribute.FileAttribute 0))
        pid-path (str pid-file)
        impact-plan
        (plan
         [(shard "gravity.self-hosting.a-pipe-holder-test" "SH-01" :normal)
          (shard "gravity.self-hosting.b-pipe-holder-queued-test" "SH-02" :normal)
          (shard "gravity.self-hosting.c-pipe-holder-exclusive-test"
                 "SH-26" :exclusive)])
        started (atom [])
        report
        (try
          (runner/execute-plan
           impact-plan
           {:normal-parallelism 1
            :worker
            (fn [job]
              (swap! started conj (:namespace job))
              (if (= 'gravity.self-hosting.a-pipe-holder-test
                     (:namespace job))
                (runner/run-namespace-process
                 job
                 {:working-directory "/tmp"
                  :capture-wait-ms 100
                  :process-launcher
                  (fn [_ working-directory]
                    ;; The shell exits zero, but its background child keeps
                    ;; the inherited pipe open after the parent is reaped.
                    (runner/start-process
                     ["/bin/sh" "-c"
                      (str "sleep 5 & child=$!; echo $child > " pid-path
                           "; exit 0")]
                     working-directory))})
                {:exit-code 0}))})
          (finally
            ;; Best-effort test-fixture cleanup only; the runner must not claim
            ;; to have killed this late-fork child in its normal-parent report.
            (when (java.nio.file.Files/exists pid-file
                                                (make-array java.nio.file.LinkOption 0))
              (let [pid (Long/parseLong (clojure.string/trim (slurp pid-path)))
                    handle-option (java.lang.ProcessHandle/of pid)]
                (when (.isPresent handle-option)
                  (let [handle (.get handle-option)]
                    (when (.isAlive handle)
                      (.destroyForcibly handle))))))
            (java.nio.file.Files/deleteIfExists pid-file)))]
    (is (= ['gravity.self-hosting.a-pipe-holder-test] @started))
    (is (= ['gravity.self-hosting.b-pipe-holder-queued-test
            'gravity.self-hosting.c-pipe-holder-exclusive-test]
           (:skipped-namespaces report)))
    (is (true? (:capture-failed? report)))
    (is (= :error (:status (first (:failures report)))))
    (is (= 1 (:exit-code report)))
    (is (false? (:complete? report)))))

(deftest process-timeout-closes-pipes-held-by-a-child
  (let [pid-file
        (java.nio.file.Files/createTempFile
         "sh01-timeout-child-"
         ".pid"
         (make-array java.nio.file.attribute.FileAttribute 0))
        pid-path (str pid-file)
        job {:namespace 'gravity.self-hosting.timeout-pipe-test
             :slice "SH-01"
             :resource-class :normal}
        started (System/nanoTime)
        result
        (try
          (runner/run-namespace-process
           job
           {:timeout-ms 100
            :output-limit-bytes 64
            :working-directory "/tmp"
            :process-launcher
            (fn [_ working-directory]
              (runner/start-process
               ["/bin/sh" "-c"
                (str "sleep 5 & child=$!; echo $child > " pid-path
                     "; wait")]
               working-directory))})
          (finally
            ;; Keep the PID file until assertions have read it below.
            nil))
        elapsed-ms (long (/ (- (System/nanoTime) started) 1000000.0))
        pid-text (slurp pid-path)
        pid (Long/parseLong (clojure.string/trim pid-text))
        handle-option (java.lang.ProcessHandle/of pid)]
    (try
      (is (= :timeout (:status result)))
      (is (= 124 (:exit-code result)))
      ;; A ProcessHandle snapshot cannot prove that a timed-out child did not
      ;; fork after observation.  Timeout containment is therefore always
      ;; reported as unproven even when the observed handles are gone.
      (is (= :unproven-timeout (:cleanup-status result)) result)
      (is (false? (:cleanup-complete? result)) result)
      (is (true? (:cleanup-failed? result)) result)
      (is (true? (:timeout-containment-unproven? result)) result)
      (is (seq (get-in result [:cleanup :observed-pids])) result)
      (is (not (contains? (set (get-in result [:cleanup :survivor-pids]))
                          pid)))
      ;; A reaped PID yields an empty Optional; if the PID is still reusable
      ;; in the JVM view, it must be explicitly non-live.
      (is (or (not (.isPresent handle-option))
              (false? (.isAlive (.get handle-option)))))
      ;; ProcessHandle cleanup and stream closure are both bounded; a sleeping
      ;; child must not hold the runner lane open after timeout.
      (is (< elapsed-ms 3000))
      (finally
        (java.nio.file.Files/deleteIfExists pid-file)))))

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

(deftest worker-rethrows-fatal-throwables-and-restores-interrupts
  (let [job {:namespace 'gravity.self-hosting.throwable-test
             :slice "SH-01"
             :resource-class :normal}]
    (doseq [fatal [(ThreadDeath.)
                   (OutOfMemoryError. "injected VM failure")
                   (LinkageError. "injected linkage failure")]]
      (try
        (#'runner/invoke-worker (fn [_] (throw fatal)) job)
        (is false (str "fatal throwable was swallowed: " (class fatal)))
        (catch Throwable thrown
          (is (identical? fatal thrown)
              (str "fatal throwable changed: " (class fatal))))))
    (try
      (let [result
            (#'runner/invoke-worker
             (fn [_] (throw (InterruptedException. "injected interrupt")))
             job)]
        (is (= :error (:status result)))
        (is (true? (:interrupted? result)))
        (is (true? (:interrupt-restored? result))
            "worker interruption must restore the current thread flag"))
      (finally
        ;; Do not leak the test interrupt flag into later tests.
        (Thread/interrupted)))))

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
  (is (= {:fail-fast? true
          :fail-fast true}
         (select-keys (:options (runner/parse-arguments
                                 ["--slice" "SH-01" "--fail-fast"]))
                      [:fail-fast? :fail-fast])))
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
