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

(defn- bootstrap-compatibility-jobs []
  (let [policy {:deterministic? true :performance? false
                :proof? false :freshness-required? false}]
    [(assoc (shard "gravity.bootstrap-compatibility.c11-test" "SH-12" :normal)
            :component-id "c11-mir" :batch-key "component/c11-mir/compatibility"
            :test-policy policy)
     (assoc (shard "gravity.self-hosting.sh01-compiler-pass-manifest-compatibility-test"
                   "SH-01" :normal)
            :component-id "compiler-pass-manifest"
            :batch-key "component/compiler-pass-manifest/compatibility"
            :test-policy policy)]))

(deftest compatibility-facades-share-only-bootstrap-initialization
  (let [jobs (bootstrap-compatibility-jobs)
        schedule (runner/schedule-plan (plan jobs))
        batches (get-in schedule [:parallel-phase :normal-batches])
        launches (atom [])
        report (runner/execute-plan
                 (plan jobs)
                 {:normal-batch-worker
                  (fn [batch]
                    (swap! launches conj batch)
                    (batch-result batch (repeat (count batch) :passed) [] false))})]
    (is (= 1 (:planned-jvms schedule)))
    (is (= 1 (count batches) (count @launches)))
    (is (= jobs (:batch-jobs (first batches)) (first @launches)))
    (is (= (mapv :namespace jobs) (:batch-namespaces (first batches))))
    (is (not (contains? (first batches) :component-id)))
    (is (= (:test-policy (first jobs)) (:test-policy (first batches))))
    (is (= :passed (:status report)))
    (is (= (set (map :namespace jobs)) (set (map :namespace (:results report)))))
    (doseq [job jobs]
      (is (= 1 (:planned-jvms (runner/schedule-plan (plan [job]))))))))

(deftest compatibility-sharing-preserves-policy-resource-and-closure-boundaries
  (let [[first-job second-job :as jobs] (bootstrap-compatibility-jobs)]
    (doseq [changed [(assoc second-job :cache-closure {})
                     (assoc second-job :cache-closure-authorized? true)
                     (assoc-in second-job [:test-policy :freshness-required?] true)
                     (assoc second-job :test-policy {:unknown true})
                     (assoc second-job :test-policy :unknown)
                     (assoc second-job :resource-class :memory-heavy)
                     (assoc second-job :resource-class :exclusive)]]
      (is (= 2 (:planned-jvms (runner/schedule-plan (plan [first-job changed]))))))
    (is (= 2 (:planned-jvms (runner/schedule-plan (plan jobs) {:normal-batch-size 1}))))
    (let [foreign (assoc (shard "gravity.self-hosting.unrelated-test" "SH-01" :normal)
                         :batch-key "bootstrap-facade/compatibility")
          batches (get-in (runner/schedule-plan (plan (conj jobs foreign)))
                          [:parallel-phase :normal-batches])]
      (is (= 2 (count batches)))
      (is (= [(:namespace foreign)] (:batch-namespaces (second batches)))))))

(deftest shared-compatibility-failure-retains-the-unattempted-namespace
  (let [jobs (bootstrap-compatibility-jobs)
        report (runner/execute-plan
                 (plan jobs)
                 {:fail-fast true
                  :normal-batch-worker
                  (fn [batch]
                    (batch-result batch [:failed] [(:namespace (second batch))] true))})]
    (is (= :failed (:status report)))
    (is (= 1 (:exit-code report)))
    (is (= [(:namespace (first jobs))] (mapv :namespace (:results report))))
    (is (= [(:namespace (second jobs))] (:skipped-namespaces report)))
    (is (true? (:fail-fast-triggered? report)))))

(deftest shared-compatibility-error-does-not-hide-the-other-result
  (let [jobs (bootstrap-compatibility-jobs)
        calls (atom 0)
        report
        (runner/execute-plan
          (plan jobs)
          {:normal-batch-worker
           (fn [batch]
             (swap! calls inc)
             (-> (batch-result batch [:failed :passed] [] false)
                 (assoc :fail 0 :error 1
                        :summary {:test 2 :pass 1 :fail 0 :error 1})
                 (assoc-in [:namespace-results 0 :status] :error)
                 (assoc-in [:namespace-results 0 :fail] 0)
                 (assoc-in [:namespace-results 0 :error] 1)
                 (assoc-in [:namespace-results 0 :summary]
                           {:test 1 :pass 0 :fail 0 :error 1})))})]
    (is (= 1 @calls))
    (is (= :failed (:status report)))
    (is (= 1 (:exit-code report)))
    (is (= (mapv :namespace jobs) (mapv :namespace (:results report))))
    (is (= [:error :passed] (mapv :status (:results report))))
    (is (= [1 0] (mapv :error (:results report))))
    (is (empty? (:skipped-namespaces report)))))

(deftest shared-compatibility-affinity-preserves-job-identity-and-freshness
  (let [jobs (bootstrap-compatibility-jobs)
        fresh (mapv #(-> % (update :namespace str)
                         (assoc-in [:test-policy :freshness-required?] true)) jobs)
        batches (get-in (runner/schedule-plan (plan fresh))
                        [:parallel-phase :normal-batches])]
    (is (= 1 (count batches)))
    (is (= (mapv :namespace fresh) (:batch-namespaces (first batches))))
    (is (true? (get-in batches [0 :test-policy :freshness-required?])))
    (doseq [unknown [{:unknown true} :unknown]]
      (let [bad (mapv #(assoc % :test-policy unknown) jobs)]
        (is (= 2 (:planned-jvms (runner/schedule-plan (plan bad)))))))))

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

(defn- injected-owned-handle [id alive descendants events terminate]
  (reify java.lang.ProcessHandle
    (pid [_] (long id))
    (isAlive [_] @alive)
    (descendants [_]
      (java.util.Arrays/stream (into-array java.lang.ProcessHandle (descendants))))
    (destroy [_] (swap! events conj [id :destroy]) (boolean (terminate false)))
    (destroyForcibly [_] (swap! events conj [id :force]) (boolean (terminate true)))
    (onExit [this] (java.util.concurrent.CompletableFuture/completedFuture this))))

(defn- injected-owned-process [handle alive wait-operation]
  (proxy [Process] []
    (toHandle []
      (if (instance? Throwable handle) (throw handle) handle))
    (getInputStream [] (java.io.ByteArrayInputStream. (byte-array 0)))
    (getErrorStream [] (java.io.ByteArrayInputStream. (byte-array 0)))
    (getOutputStream [] (java.io.ByteArrayOutputStream.))
    (waitFor
      ([] (wait-operation) 0)
      ([_ _] (wait-operation)))
    (exitValue [] (if @alive (throw (IllegalThreadStateException.)) 0))
    (destroy []
      (if (instance? Throwable handle) (reset! alive false)
          (.destroy ^java.lang.ProcessHandle handle)))
    (destroyForcibly []
      (if (instance? Throwable handle) (reset! alive false)
          (.destroyForcibly ^java.lang.ProcessHandle handle))
      this)
    (isAlive [] @alive)))

(deftest retained-handles-survive-natural-exit-and-observe-late-grandchildren
  (let [events (atom []) root-alive (atom true) child-alive (atom true) late-alive (atom true)
        late (injected-owned-handle 3 late-alive (constantly []) events
                                    (fn [_] (reset! late-alive false) true))
        child (injected-owned-handle 2 child-alive #(if @root-alive [] [late]) events
                                     (fn [_] (reset! child-alive false) true))
        root (injected-owned-handle 1 root-alive #(if @root-alive [child] []) events
                                    (fn [_] (reset! root-alive false) true))
        process (injected-owned-process root root-alive #(do (reset! root-alive false) true))
        owned (runner/retain-owned-process! process)
        result (runner/process-result process {:timeout-ms 100} owned)
        terminal (runner/finalize-owned-process! owned)]
    (is (= [root child late] (runner/retained-process-handles owned)))
    (is (identical? child (second (runner/retained-process-handles owned))))
    (is (= [[3 :destroy] [2 :destroy]] @events))
    (is (:root-exited? result))
    (is (:tracked-owned-processes-terminal? result))
    (is (= :unknown (:descendant-coverage result)))
    (is (nil? (:cleanup-complete? result)))
    (is (:cleanup-failed? result) "Live owned descendants after natural root exit refuse a clean command")
    (is (= :error (:status result)))
    (is (identical? terminal (runner/finalize-owned-process! owned)))
    (is (= 2 (count @events)) "Finalization does not repeat termination")
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest missing-and-unobserved-descendants-never-become-complete
  (doseq [missing? [false true]]
    (let [alive (atom false) events (atom []) unobserved-alive (atom true)
          unobserved (injected-owned-handle 99 unobserved-alive (constantly []) events
                                            (fn [_] (reset! unobserved-alive false) true))
          missing (UnsupportedOperationException. "No owned handle observation")
          root (if missing? missing
                   (injected-owned-handle 1 alive (constantly []) events (constantly true)))
          process (injected-owned-process root alive (constantly true))
          owned (runner/retain-owned-process! process)
          result (runner/process-result process {} owned)]
      (is (:root-exited? result))
      (is (.isAlive ^java.lang.ProcessHandle unobserved)
          "The interface cannot account for a handle absent from every observation")
      (is (= :unknown (:descendant-coverage result)))
      (is (not (true? (:cleanup-complete? result))))
      (is (not (true? (get-in result [:cleanup :complete?])))))))

(deftest retained-finalizer-refuses-foreign-token-and-thread
  (let [alive (atom false) events (atom [])
        root (injected-owned-handle 1 alive (constantly []) events (constantly true))
        process (injected-owned-process root alive (constantly true))
        other (injected-owned-process root alive (constantly true))
        owned (runner/retain-owned-process! process)]
    (is (= "SH01-PROCESS-OWNERSHIP"
           (:id (exception-data #(runner/process-result other {} owned)))))
    (is (= "SH01-PROCESS-OWNERSHIP"
           (deref (future (:id (exception-data #(runner/finalize-owned-process! owned))))
                  1000 :timed-out)))
    (is (empty? @events))
    (runner/finalize-owned-process! owned)
    (is (= "SH01-PROCESS-OWNERSHIP"
           (:id (exception-data #(runner/process-result process {} owned)))))))

(deftest retained-finalizer-refuses-a-surviving-owned-process
  (let [events (atom []) alive (atom true)
        root (injected-owned-handle 1 alive (constantly []) events (constantly false))
        process (injected-owned-process root alive (constantly false))
        owned (runner/retain-owned-process! process)
        result (with-redefs-fn
                 {(ns-resolve 'gravity.self-hosting.sh01-parallel-test-runner 'process-cleanup-grace-ms) 0
                  (ns-resolve 'gravity.self-hosting.sh01-parallel-test-runner 'process-cleanup-timeout-ms) 10}
                 #(runner/finalize-owned-process! owned))]
    (is (false? (:tracked-complete? result)))
    (is (false? (:complete? result)))
    (is (:root-alive? result))
    (is (= [1] (:survivor-pids result)))
    (is (= [[1 :destroy] [1 :force]] @events))
    (is (identical? result (runner/finalize-owned-process! owned)))))

(deftest retained-wait-errors-preserve-original-objects-and-interruption
  (doseq [failure [(IllegalStateException. "injected wait failure")
                   (InterruptedException. "injected wait interruption")]]
    (let [events (atom []) alive (atom true)
          root (injected-owned-handle 1 alive (constantly []) events
                                      (fn [_] (reset! alive false) true))
          process (injected-owned-process root alive #(throw failure))
          owned (runner/retain-owned-process! process)]
      (try
        (let [outcome (try
                        (let [result (runner/process-result process {:timeout-ms 100} owned)]
                          {:result result :interrupted? (.isInterrupted (Thread/currentThread))})
                        (catch Throwable error
                          {:error error :interrupted? (.isInterrupted (Thread/currentThread))}))
              result (:result outcome)]
          (is (identical? failure @(:wait-throwable owned)))
          (if (instance? InterruptedException failure)
            (is (identical? failure (:error outcome)))
            (do (is (= :error (:status result)))
                (is (false? (:cleanup-complete? result)))))
          (is (= [[1 :destroy]] @events))
          (when (instance? InterruptedException failure)
            (is (:interrupted? outcome))))
        (finally (Thread/interrupted))))))

(deftest retained-discovery-fatal-is-rethrown-after-owned-cleanup
  (let [fatal (OutOfMemoryError. "injected owned discovery failure")
        events (atom []) alive (atom true)
        root (injected-owned-handle 1 alive #(throw fatal) events
                                    (fn [_] (reset! alive false) true))
        process (injected-owned-process root alive (constantly false))
        owned (runner/retain-owned-process! process)
        thrown (try (runner/process-result process {:timeout-ms 100} owned) nil
                    (catch Throwable error error))]
    (is (identical? fatal thrown))
    (is (false? @alive))
    (is (= [[1 :destroy]] @events))
    (is (identical? fatal (try (runner/finalize-owned-process! owned) nil
                              (catch Throwable error error))))
    (is (= 1 (count @events)))))

(deftest retained-discovery-promotes-later-fatal-over-an-earlier-ordinary-error
  (let [ordinary (UnsupportedOperationException. "first discovery unavailable")
        fatal (OutOfMemoryError. "later owned discovery fatal")
        calls (atom 0) events (atom []) root-alive (atom true) child-alive (atom true)
        child (injected-owned-handle 2 child-alive #(throw fatal) events
                                     (fn [_] (reset! child-alive false) true))
        root (injected-owned-handle 1 root-alive
                                    #(if (= 1 (swap! calls inc)) (throw ordinary) [child])
                                    events (fn [_] (reset! root-alive false) true))
        process (injected-owned-process root root-alive (constantly false))
        owned (runner/retain-owned-process! process)
        thrown (try (runner/process-result process {:timeout-ms 100} owned) nil
                    (catch Throwable error error))]
    (is (identical? fatal thrown))
    (is (identical? fatal @(:discovery-error owned)))
    (is (some #(identical? ordinary %) (.getSuppressed fatal)))
    (is (false? @root-alive))
    (is (false? @child-alive))
    (is (= [[2 :destroy] [1 :destroy]] @events))))

(deftest retained-cleanup-attempts-other-handles-before-rethrowing-fatal
  (doseq [stage [:terminate :alive :reap]]
    (let [fatal (OutOfMemoryError. (str "injected cleanup " stage))
          active? (not= :reap stage)
          root-alive (atom active?) good-alive (atom active?) bad-alive (atom active?)
          events (atom []) reaped (atom [])
          bad (reify java.lang.ProcessHandle
                (pid [_] (long 3))
                (isAlive [_] (if (= :alive stage) (throw fatal) @bad-alive))
                (descendants [_] (java.util.stream.Stream/empty))
                (destroy [_]
                  (swap! events conj [3 :destroy])
                  (if (= :terminate stage) (throw fatal) (do (reset! bad-alive false) true)))
                (destroyForcibly [_] (swap! events conj [3 :force]) (reset! bad-alive false) true)
                (onExit [this] (if (= :reap stage) (throw fatal)
                                  (java.util.concurrent.CompletableFuture/completedFuture this))))
          good (injected-owned-handle 2 good-alive (constantly []) events
                                      (fn [_] (reset! good-alive false) true))
          root (injected-owned-handle 1 root-alive (constantly [good bad]) events
                                      (fn [_] (reset! root-alive false) true))
          process (injected-owned-process root root-alive (constantly false))
          owned (runner/retain-owned-process! process)
          reap-var (ns-resolve 'gravity.self-hosting.sh01-parallel-test-runner 'reap-process-handle!)
          original-reap @reap-var
          thrown (with-redefs-fn
                   {(ns-resolve 'gravity.self-hosting.sh01-parallel-test-runner 'process-cleanup-grace-ms) 0
                    (ns-resolve 'gravity.self-hosting.sh01-parallel-test-runner 'process-cleanup-timeout-ms) 50
                    reap-var (fn [handle deadline tracker]
                               (swap! reaped conj (.pid ^java.lang.ProcessHandle handle))
                               (original-reap handle deadline tracker))}
                   #(try (runner/finalize-owned-process! owned) nil (catch Throwable error error)))]
      (is (identical? fatal thrown) (str stage))
      (is (false? @root-alive) (str stage))
      (is (false? @good-alive) (str stage))
      (is (false? @bad-alive) (str stage))
      (when (= :reap stage) (is (= [1 2 3] @reaped)))
      (let [before @events]
        (is (identical? fatal (try (runner/finalize-owned-process! owned) nil
                                  (catch Throwable error error))))
        (is (= before @events))))))

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
    (is (or (:capture-failed? report) (:cleanup-failed? report))
        "Either unobserved pipe capture or a retained live descendant must refuse success")
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

(deftest runtime-module-read-owner-wrapper-is-required-before-scheduling
  (doseq [namespace ['gravity.self-hosting.runtime-module-reads-test
                    'gravity.self-hosting.module-census-test]]
    (let [starts (atom 0)]
      (doseq [impact-plan [{:namespaces [namespace]}
                          (plan [{:namespace namespace :slice "SH-19"
                                  :resource-class :normal}])
                          ;; Neither a stronger resource label nor a forged
                          ;; support marker supplies the missing guarded route.
                          (plan [{:namespace namespace :slice "SH-19"
                                  :resource-class :exclusive :execution :supported}])]]
        (let [data (exception-data
                    #(runner/execute-plan
                      impact-plan {:worker (fn [_] (swap! starts inc) {:exit-code 0})
                                   :process-launcher (fn [& _] (swap! starts inc))}))]
          (is (= "SH01-IMPACT-EXTERNAL-WRAPPER" (:id data)))
          (is (= :unsupported (:execution data)))
          (is (= namespace (:namespace data)))
          (is (= [:common-host-broker :host-exclusive :canonical-heavy-lock]
                 (:required-guards data)))))
      (is (zero? @starts)))))

(deftest direct-process-entrypoints-require-the-runtime-owner-wrapper
  (let [starts (atom 0)
        options {:working-directory "/tmp"
                 :process-launcher (fn [& _] (swap! starts inc) {:exit-code 0})}]
    (doseq [namespace ['gravity.self-hosting.runtime-module-reads-test
                      "gravity.self-hosting.runtime-module-reads-test"
                      'gravity.self-hosting.module-census-test
                      "gravity.self-hosting.module-census-test"]
            marker [nil :supported]]
      (let [job (cond-> {:namespace namespace :slice "SH-19" :resource-class :normal}
                  marker (assoc :execution marker))]
        (doseq [operation [#(runner/run-namespace-process job options)
                           #(runner/run-namespace-process (assoc job :resource-class :exclusive) options)
                           #(runner/run-normal-batch-process
                             [{:namespace 'gravity.self-hosting.ordinary-test
                               :slice "SH-01" :resource-class :normal} job] options)]]
          (let [data (exception-data operation)]
            (is (= "SH01-IMPACT-EXTERNAL-WRAPPER" (:id data)))
            (is (= :unsupported (:execution data)))
            (is (= [:common-host-broker :host-exclusive :canonical-heavy-lock]
                   (:required-guards data)))))))
    (is (zero? @starts))))

(deftest parent-mutation-generic-entrypoints-never-launch
  (let [starts (atom 0)
        options {:working-directory "/tmp"
                 :worker (fn [_] (swap! starts inc) {:exit-code 0})
                 :process-launcher (fn [& _] (swap! starts inc) {:exit-code 0})}]
    (doseq [namespace ['gravity.self-hosting.resolution-parent-mutation-test
                      "gravity.self-hosting.resolution-parent-mutation-test"]
            marker [nil :supported]]
      (let [job (cond-> {:namespace namespace :slice "SH-19" :owner :sh-runtime
                        :resource-class :normal}
                  marker (assoc :execution marker))]
        (doseq [operation [#(runner/run-namespace-process job options)
                           #(runner/run-normal-batch-process
                              [{:namespace 'gravity.self-hosting.ordinary-test
                                :slice "SH-01" :resource-class :normal} job] options)
                           #(runner/execute-plan {:namespaces [namespace] :shards [job]} options)]]
          (let [data (exception-data operation)]
            (is (= "SH01-IMPACT-EXTERNAL-WRAPPER" (:id data)))
            (is (= :unsupported (:execution data)))
            (is (= "SH-06" (:slice data)))
            (is (= :sh-resolution (:owner data)))))))
    (is (zero? @starts))))

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
