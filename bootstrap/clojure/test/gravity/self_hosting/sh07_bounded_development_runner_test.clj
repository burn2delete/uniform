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
     :status status
     :exit-code exit-code
     :summary {:test 1 :pass (if (= :passed status) 1 0)
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
    (is (= ["c6-contract" "c6-coverage"]
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
