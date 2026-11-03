(ns ci.resolution-interval-test
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.test :refer [deftest is run-tests]]))

(binding [*command-line-args* nil]
  (load-file "bootstrap/clojure/test/gravity/development_test_runner.clj"))
(require '[gravity.development-test-runner :as runner])

(def digest-ns "gravity.self-hosting.sh06-unmetered-resolution-test")
(def digest-var (str digest-ns "/digest-count-quota-is-gone-but-complete-ordered-requests-remain-required"))
(def artifact-var (str digest-ns "/actual-current-source-artifact-retains-authentication-and-roundtrip"))

(defn- rows [text]
  (mapv edn/read-string (remove str/blank? (str/split-lines (str text)))))

(defn- execute [state test-var intervals helper]
  (let [output (java.io.StringWriter.) events (atom [])
        context {:state state :namespace digest-ns :test-var test-var}
        invoke (fn [interval body] (helper interval body))]
    (binding [*err* output]
      (with-bindings {#'runner/*progress-state* state
                     #'runner/*progress-test-context* context}
        (doseq [interval intervals]
          (invoke interval #(do (swap! events conj interval) :value)))))
    {:events @events :rows (rows output)}))

(deftest digest-intervals-are-fixed-non-authoritative-observations
  (let [state (#'runner/progress-state)
        intervals [:positive-resolve :count-and-hash-shape :ordinal-negative
                   :count-negative :shape-negative]
        result (execute state digest-var intervals runner/observe-resolution-digest-interval!)]
    (is (= intervals (:events result)))
    (is (= 10 (count (:rows result))))
    (is (= #{:resolution-digest-positive-resolve
             :resolution-digest-count-and-hash-shape
             :resolution-digest-ordinal-negative
             :resolution-digest-count-negative
             :resolution-digest-shape-negative}
           (set (map :phase (:rows result)))))
    (is (every? #(= :non-authoritative (:authority %)) (:rows result)))
    (is (every? #(not-any? (set (keys %)) [:argv :environment :message :stack :result :summary])
                (:rows result)))))

(deftest artifact-intervals-preserve-order-and-disabled-path
  (let [state (#'runner/progress-state)
        intervals [:construction :verification :serialization]
        enabled (execute state artifact-var intervals runner/observe-resolution-current-artifact-interval!)
        disabled (execute nil artifact-var intervals runner/observe-resolution-current-artifact-interval!)]
    (is (= intervals (:events enabled)))
    (is (= 6 (count (:rows enabled))))
    (is (= #{:resolution-current-artifact-construction
             :resolution-current-artifact-verification
             :resolution-current-artifact-serialization}
           (set (map :phase (:rows enabled)))))
    (is (= intervals (:events disabled)))
    (is (empty? (:rows disabled)))))

(deftest diagnostic-failure-retains-body-and-is-reported-afterwards
  (let [state (#'runner/progress-state)
        original (java.io.IOException. "diagnostic")
        output (proxy [java.io.Writer] []
                 (write ([_] (throw original)) ([_ _ _] (throw original)))
                 (flush [] (throw original)) (close []))
        events (atom [])
        result (binding [*err* output]
                 (with-bindings {#'runner/*progress-state* state
                                #'runner/*progress-test-context*
                                {:state state :namespace digest-ns :test-var digest-var}}
                   (try
                     (runner/observe-resolution-digest-interval!
                      :positive-resolve #(swap! events conj :body))
                     (#'runner/require-progress-success!)
                     nil
                     (catch Throwable error error))))]
    (is (identical? original result))
    (is (= [:body] @events))
    (is (= 1 (.size ^java.util.ArrayList (:errors state))))))

(deftest scope-and-unknown-intervals-do-not-emit
  (let [state (#'runner/progress-state)
        other (#'runner/progress-state)
        output (java.io.StringWriter.)
        calls (atom 0)]
    (binding [*err* output]
      (with-bindings {#'runner/*progress-state* state
                     #'runner/*progress-test-context*
                     {:state state :namespace "other" :test-var digest-var}}
        (runner/observe-resolution-digest-interval! :positive-resolve #(swap! calls inc))
        (with-bindings {#'runner/*progress-state* other}
          (runner/observe-resolution-digest-interval! :positive-resolve #(swap! calls inc)))
        (runner/observe-resolution-digest-interval! :unknown #(swap! calls inc))))
    (is (= 3 @calls))
    (is (empty? (rows output)))))

(let [result (run-tests)]
  (System/exit (if (zero? (+ (:fail result) (:error result))) 0 1)))
