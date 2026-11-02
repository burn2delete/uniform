(ns gravity.self-hosting.sh07-bounded-development-runner-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting.sh07-bounded-development-runner :as runner]))

(defn- exception-data
  [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo error
      (ex-data error))))

(defn- route-report
  [route-name status exit-code]
  (let [route (runner/route route-name)
        selected
        (mapv #(str (:namespace route) "/" %)
              (:test-symbols route))]
    {:schema :gravity/sh07-development-child-result-v1
     :authority :non-authoritative
     :authoritative? false
     :route route-name
     :selected selected
     :selection-count (count selected)
     :artifact-build-count (if (= "b8-regression" route-name) 4 0)
     :status status
     :exit-code exit-code
     :summary {:test (count selected) :pass (if (= :passed status) 1 0)
               :fail (if (= :failed status) 1 0) :error 0}
     :elapsed-ms 1}))

(defn- progress-record
  [phase]
  (pr-str {:schema :gravity/sh07-development-progress-v1
           :sequence 1
           :phase phase
           :event :child-start
           :active? true
           :elapsed-ms 1}))

(defn- successful-command
  [route-name]
  (let [report (pr-str (route-report route-name :passed 0))]
    (fn [_ {:keys [path]}]
      ["/bin/sh" "-c"
       "printf '%s\\n' \"$2\" > \"$1\"; printf 'SH07_DEV_RESULT %s\\n' \"$3\""
       "sh07-runner-test" (str path) (progress-record route-name) report])))

(deftest successful-run-emits-bounded-non-authoritative-receipt
  (let [result (runner/run-process!
                "c6-contract"
                {:timeout-ms 2000
                 :heartbeat-interval-ms 50
                 :child-command-fn (successful-command "c6-contract")})]
    (is (= :passed (:status result)) result)
    (is (= 0 (:exit-code result)) result)
    (is (false? (:authoritative? result)))
    (is (false? (:timed-out? result)))
    (is (= :passed (get-in result [:child-report :status])))
    (is (= "c6-contract" (get-in result [:child-report :route])))
    (is (= :gravity/sh07-development-progress-v1
           (get-in result [:progress :last-progress :schema])))
    (is (= "c6-contract"
           (get-in result [:progress :last-progress :phase])))
    (is (<= (get-in result [:stdout :bytes]) (* 256 1024)))
    (is (false? (get-in result [:stdout :truncated?])))))

(deftest natural-exit-drains-capture-before-closing-streams
  (let [payload (.getBytes (str "SH07_DEV_RESULT "
                               (pr-str (route-report "c6-contract" :passed 0))
                               "\n") "UTF-8")
        closed? (atom false)
        first-read? (atom true)
        input (let [delegate (java.io.ByteArrayInputStream. payload)]
                (proxy [java.io.InputStream] []
                  (read ([buffer]
                         (.read ^java.io.InputStream this buffer 0
                                (alength ^bytes buffer)))
                        ([buffer offset length]
                         (when (compare-and-set! first-read? true false)
                           (Thread/sleep 100))
                         (if @closed? -1
                             (.read delegate buffer offset length))))
                  (close [] (reset! closed? true))))
        error-input (java.io.ByteArrayInputStream. (byte-array 0))
        alive (atom false)
        handle (reify java.lang.ProcessHandle
                 (pid [_] 7002)
                 (isAlive [_] @alive)
                 (descendants [_] (java.util.stream.Stream/empty))
                 (destroy [_] false)
                 (destroyForcibly [_] false)
                 (onExit [this]
                   (java.util.concurrent.CompletableFuture/completedFuture this)))
        process (proxy [Process] []
                  (toHandle [] handle)
                  (getInputStream [] input)
                  (getErrorStream [] error-input)
                  (getOutputStream [] (java.io.ByteArrayOutputStream.))
                  (waitFor ([] 0) ([_ _] true))
                  (exitValue [] 0)
                  (destroy [] nil)
                  (destroyForcibly [] nil)
                  (isAlive [] @alive))
        start-var (ns-resolve 'gravity.process-supervision 'start-process)
        result (with-redefs-fn
                 {start-var (fn [& _] process)}
                 #(runner/run-process!
                   "c6-contract"
                   {:timeout-ms 2000
                    :child-command-fn (fn [_ _] ["/bin/true"])}))]
    (is (= :passed (:status result)) result)
    (is (= 0 (:exit-code result)) result)
    (is (= :passed (get-in result [:child-report :status])) result)
    (is (true? @closed?))))

(deftest timeout-is-failed-and-reaps-descendants
  (let [result
        (runner/run-process!
         "c6-contract"
         {:timeout-ms 150
          :heartbeat-interval-ms 50
          :child-command-fn (fn [_ _]
                              ["/bin/sh" "-c" "sleep 30"])} )]
    (is (= :timeout (:status result)) result)
    (is (= 124 (:exit-code result)) result)
    (is (true? (:timed-out? result)) result)
    (is (= "SH07-DEV-TIMEOUT" (:diagnostic-id result)) result)
    (is (nil? (:child-report result)) result)
    (is (true? (get-in result [:cleanup :cleanup-complete?])) result)
    (is (false? (get-in result [:cleanup :root-alive-after-kill?])) result)))

(deftest late-success-after-bound-remains-a-timeout
  (let [report (pr-str (route-report "c6-contract" :passed 0))
        result
        (runner/run-process!
         "c6-contract"
         {:timeout-ms 100
          :child-command-fn
          (fn [_ _]
            ["/bin/sh" "-c"
             "sleep 1; printf '%s\\n' \"$1\""
             "sh07-late-result" (str "SH07_DEV_RESULT " report)])})]
    (is (= :timeout (:status result)) result)
    (is (true? (:timed-out? result)) result)
    (is (= 124 (:exit-code result)) result)
    (is (nil? (:child-report result)) result)))

(deftest malformed-child-output-never-passes
  (let [result
        (runner/run-process!
         "c6-contract"
         {:timeout-ms 2000
          :child-command-fn
          (fn [_ _]
            ["/bin/sh" "-c"
             "printf '%s\\n' 'SH07_DEV_RESULT {:schema :forged}'"])} )]
    (is (= :failed (:status result)) result)
    (is (= 1 (:exit-code result)) result)
    (is (= "SH07-DEV-MALFORMED-OUTPUT" (:diagnostic-id result)) result)
    (is (nil? (:child-report result)) result)
    (is (false? (:timed-out? result)) result)))

(deftest producer-claims-cannot-replace-observed-execution
  (let [parse (ns-resolve 'gravity.self-hosting.sh07-bounded-development-runner
                          'parse-child-report)
        report (route-report "c6-contract" :passed 0)
        output (fn [value] {:text (str "SH07_DEV_RESULT " (pr-str value))
                            :complete? true :truncated? false})]
    (doseq [summary [{:test 0 :pass 0 :fail 0 :error 0}
                     {:test 1 :pass 0 :fail 0 :error 0}
                     {:test 1 :pass 1000000 :fail 1 :error 0}]]
      (is (nil? (:report (parse "c6-contract"
                               (output (assoc report :summary summary)) 0)))))
    (is (nil? (:report (parse "c6-contract" (output report) 1))))
    (doseq [[key value] [[:authority :authoritative] [:authoritative? true]
                         [:mergeable true] [:verification-report {:status :passed}]]]
      (is (nil? (:report (parse "c6-contract"
                               (output (assoc report key value)) 0)))))
    (doseq [capture [(assoc (output report) :truncated? true)
                     (assoc (output report) :complete? false)
                     (dissoc (output report) :complete?)]]
      (is (nil? (:report (parse "c6-contract" capture 0)))))
    (is (= report (:report (parse "c6-contract" (output report) 0))))))

(deftest saturated-output-cannot-hide-a-second-terminal-result
  (let [line (str "SH07_DEV_RESULT "
                  (pr-str (route-report "c6-contract" :passed 0)))
        result (runner/run-process!
                "c6-contract"
                {:timeout-ms 2000
                 :child-command-fn
                 (fn [_ _]
                   ["/bin/sh" "-c"
                    "printf '%s\\n' \"$1\"; head -c 262144 /dev/zero | tr '\\000' x; printf '\\n%s\\n' \"$1\""
                    "capture-saturation-test" line])})]
    (is (true? (get-in result [:stdout :truncated?])))
    (is (<= (get-in result [:stdout :bytes]) (* 256 1024)))
    (is (= :failed (:status result)))
    (is (= "SH07-DEV-MALFORMED-OUTPUT" (:diagnostic-id result)))
    (is (nil? (:child-report result)))))

(deftest failed-stream-observation-is-not-complete-output
  (let [capture (ns-resolve 'gravity.self-hosting.sh07-bounded-development-runner
                            'capture-stream-output)
        failed (capture (proxy [java.io.InputStream] []
                          (read [] (throw (java.io.IOException. "read failed")))))
        empty (capture (java.io.ByteArrayInputStream. (byte-array 0)))]
    (is (false? (:complete? failed)))
    (is (false? (:truncated? failed)))
    (is (true? (:complete? empty)))
    (is (false? (:truncated? empty)))))

(deftest post-launch-capture-setup-failure-cleans-the-owned-process
  (doseq [failure-stream [:stdout :stderr]]
    (let [alive (atom true)
          handle (reify java.lang.ProcessHandle
                   (pid [_] 7001)
                   (isAlive [_] @alive)
                   (descendants [_] (java.util.stream.Stream/empty))
                   (destroy [_] (reset! alive false) true)
                   (destroyForcibly [_] (reset! alive false) true)
                   (onExit [this]
                     (java.util.concurrent.CompletableFuture/completedFuture this)))
          empty-stream (java.io.ByteArrayInputStream. (byte-array 0))
          process (proxy [Process] []
                    (toHandle [] handle)
                    (getInputStream []
                      (if (= :stdout failure-stream)
                        (throw (java.io.IOException. "stdout capture setup failed"))
                        empty-stream))
                    (getErrorStream []
                      (if (= :stderr failure-stream)
                        (throw (java.io.IOException. "stderr capture setup failed"))
                        empty-stream))
                    (getOutputStream [] (java.io.ByteArrayOutputStream.))
                    (waitFor
                      ([] 0)
                      ([_ _] true))
                    (exitValue [] (if @alive
                                    (throw (IllegalThreadStateException.))
                                    1))
                    (destroy [] (reset! alive false))
                    (destroyForcibly [] (reset! alive false))
                    (isAlive [] @alive))
          start-var (ns-resolve 'gravity.process-supervision 'start-process)
          thrown (with-redefs-fn
                   {start-var (fn [& _] process)}
                   #(try
                      (runner/run-process!
                       "c6-contract"
                       {:timeout-ms 1000
                        :child-command-fn (fn [_ _] ["/bin/true"])})
                      nil
                      (catch Throwable error error)))]
      (is (instance? java.io.IOException thrown) thrown)
      (is (false? @alive)
          (str "capture setup failure must clean the " (name failure-stream)
               " process")))))

(deftest failed-process-observation-cannot-claim-complete-cleanup
  (let [cleanup (ns-resolve 'gravity.self-hosting.sh07-bounded-development-runner
                            'cleanup-process-tree!)
        process (proxy [Process] []
                  (toHandle [] (throw (UnsupportedOperationException. "unavailable")))
                  (isAlive [] false))
        result (cleanup process)]
    (is (true? (:sampling-failed? result)))
    (is (false? (:cleanup-complete? result)))
    (is (true? (:root-exited? result)))))

(deftest descendant-cleanup-covers-background-child
  (let [result
        (runner/run-process!
         "c6-contract"
         {:timeout-ms 1000
          :heartbeat-interval-ms 50
          :child-command-fn
          (fn [_ _]
            ["/bin/sh" "-c" "sleep 30 & wait"])})]
    (is (= :timeout (:status result)) result)
    (is (true? (get-in result [:cleanup :cleanup-complete?])) result)
    (is (pos? (get-in result [:cleanup :captured-descendant-count])) result)
    (is (zero? (get-in result [:cleanup :descendants-alive-after-kill])) result)
    (is (false? (get-in result [:cleanup :root-alive-after-kill?])) result)))

(deftest unknown-route-is-rejected-before-launch
  (let [launched? (atom false)
        data (exception-data
              #(runner/run-process!
                "not-a-reviewed-route"
                {:child-command-fn
                 (fn [_ _] (reset! launched? true) ["/bin/false"])}))]
    (is (= "SH07-DEV-ROUTE" (:id data)) data)
    (is (false? @launched?))
    (is (= ["b8-regression" "c6-contract" "c6-coverage"]
           (:available data)))))

(deftest memory-heavy-route-requires-host-admission-root
  (let [launched? (atom false)
        data (exception-data
              #(runner/run-route!
                "c6-coverage"
                {:child-command-fn
                 (fn [_ _] (reset! launched? true) ["/bin/false"])}))]
    (is (= "SH07-DEV-RESOURCE-ROOT" (:id data)) data)
    (is (false? @launched?))))

(deftest b8-regression-route-is-closed-and-counts-one-jvm-selection
  (let [route (runner/route "b8-regression")
        selected (:test-symbols route)
        parse (ns-resolve 'gravity.self-hosting.sh07-bounded-development-runner
                          'parse-args)
        mixed
        (exception-data
         #(parse ["--route" "b8-regression"
                  "--namespace" "gravity.self-hosting.sh07-try-catch-test"]))]
    (is (= :exact-test-vars (:selection-mode route)) route)
    (is (= 'gravity.self-hosting.sh07-try-catch-test
           (:namespace route)) route)
    (is (= 2 (count selected)) selected)
    (is (= 2 (count (set selected))) selected)
    (is (= "SH07-DEV-USAGE" (:id mixed)) mixed)
    (let [extra
          (exception-data
           #(runner/run-route!
             "b8-regression"
             {:test-var "gravity.self-hosting.sh07-try-catch-test/foo"}))]
      (is (= "SH07-DEV-SELECTION" (:id extra)) extra))
    (let [result
          (runner/run-process!
           "b8-regression"
           {:timeout-ms 2000
            :heartbeat-interval-ms 50
            :child-command-fn (successful-command "b8-regression")})]
      (is (= :passed (:status result)) result)
      (is (= 2 (get-in result [:child-report :selection-count])) result)
      (is (= 4 (get-in result [:child-report :artifact-build-count])) result)
      (is (= 2 (:selection-count result)) result)
      (is (= 4 (:artifact-build-count result)) result))))
