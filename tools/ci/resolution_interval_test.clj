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

(defn- caught [body]
  (try {:value (body)}
       (catch Throwable error {:error error})))

(defn- exercise-legacy
  "Exercise the actual current runner helpers without loading any selected
  Resolution namespace.  The body and all diagnostic paths are synthetic;
  only the runner's interval/provenance behavior is under test."
  [{:keys [enabled? timing? fault-stage fault-edge diagnostic original swallow?
           selected-var selected-intervals selected-phases selected-helper]
    :or {enabled? true
         selected-var digest-var
         selected-intervals [:positive-resolve :count-and-hash-shape
                             :ordinal-negative :count-negative :shape-negative]
         selected-phases #{:resolution-digest-positive-resolve
                           :resolution-digest-count-and-hash-shape
                           :resolution-digest-ordinal-negative
                           :resolution-digest-count-negative
                           :resolution-digest-shape-negative}
         selected-helper runner/observe-resolution-digest-interval!}}]
  (let [state (#'runner/progress-state)
        events (atom []) timings (atom 0) sinks (atom 0)
        text (StringBuilder.) raw-pr pr-str
        fail-row? #(and (contains? selected-phases (:phase %))
                        (= fault-edge (:edge %)))
        accept! (fn [s]
                  (let [row (edn/read-string s)]
                    (.append text s)
                    (when (and (= fault-stage :write) (fail-row? row))
                      (throw diagnostic))))
        writer (proxy [java.io.Writer] []
                 (write
                   ([buffer]
                    (accept! (if (string? buffer) buffer (String. ^chars buffer))))
                   ([buffer offset length]
                    (accept! (if (string? buffer)
                               (subs buffer offset (+ offset length))
                               (String. ^chars buffer (int offset) (int length))))))
                 (flush []
                   (when (and (= fault-stage :flush) (seq (str text))
                              (fail-row? (edn/read-string
                                          (last (str/split-lines (str text))))))
                     (throw diagnostic)))
                 (close []))
        body (fn []
               (doseq [interval selected-intervals]
                 (let [call #(do (swap! events conj interval)
                                 (try
                                   (when original (throw original))
                                   :original-value
                                   (finally
                                     (swap! events conj :finally)
                                     (Thread/interrupted))))]
                   (if swallow?
                     (try (selected-helper interval call)
                          (catch Throwable _ nil))
                     (selected-helper interval call))))
               :original-value)
        run #(let [value (#'runner/observe-progress! :test-var digest-ns
                                                       selected-var body)]
               (#'runner/require-progress-success!)
               (when timing? (swap! timings inc))
               (swap! sinks inc)
               value)
        context {:state state :namespace digest-ns :test-var selected-var}
        result (binding [*err* writer]
                 (with-redefs [clojure.core/pr-str
                               (fn [& args]
                                 (when (and (= fault-stage :format)
                                            (fail-row? (first args)))
                                   (throw diagnostic))
                                 (apply raw-pr args))]
                   (with-bindings {#'runner/*progress-state*
                                   (when enabled? state)
                                   #'runner/*progress-test-context* context}
                     (caught #(if enabled?
                                (#'runner/run-with-progress! state run)
                                (body))))))
        interrupted? (Thread/interrupted)]
    {:result result :events @events :timings @timings :sinks @sinks
     :interrupted? interrupted?
     :rows (rows text) :state state}))

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

(deftest legacy-diagnostic-failures-stay-sticky-with-original-body-and-finally
  (doseq [stage [:format :write :flush]
          edge [:enter :return]
          timing? [false true]
          swallow? [false true]]
    (let [error (java.io.IOException.)
          r (exercise-legacy {:fault-stage stage :fault-edge edge
                              :diagnostic error :timing? timing?
                              :swallow? swallow?})]
      (is (identical? error (get-in r [:result :error])))
      (is (= 10 (count (:events r))))
      (is (zero? (:timings r)))
      (is (zero? (:sinks r)))
      (is (= 1 (.size ^java.util.ArrayList (get-in r [:state :errors])))))))

(deftest legacy-original-primary-and-causal-interrupt-survive-clearing-unwind
  (doseq [[original original-interrupt?]
          [[(Exception.) false]
           [(LinkageError.) false]
           [(InterruptedException.) true]
           [(Exception. "wrapped" (InterruptedException.)) true]
           [(proxy [Exception] []
              (getCause [] (throw (Exception. "unreadable")))) false]]
          diagnostic [(java.io.IOException.) (InterruptedException.)]]
    (let [r (exercise-legacy {:fault-stage :write :fault-edge :enter
                              :original original :diagnostic diagnostic})]
      (is (identical? original (get-in r [:result :error])))
      (is (some #(identical? diagnostic %)
                (.getSuppressed ^Throwable original)))
      (is (= (or original-interrupt?
                 (instance? InterruptedException diagnostic))
             (:interrupted? r)))
      (is (= [:positive-resolve :finally] (:events r))))))

(deftest legacy-outside-scope-and-nested-state-cannot-emit-fixed-intervals
  (let [state (#'runner/progress-state)
        other (#'runner/progress-state)
        output (java.io.StringWriter.)
        calls (atom 0)
        invoke (fn []
                 (runner/observe-resolution-digest-interval!
                  :positive-resolve #(swap! calls inc)))]
    (binding [*err* output]
      (with-bindings {#'runner/*progress-state* state}
        (invoke)
        (#'runner/observe-progress! :test-var "other" "other/test" invoke)
        (#'runner/observe-progress! :test-var digest-ns digest-var
                                     #(with-bindings {#'runner/*progress-state* other}
                                        (invoke)))
        (#'runner/observe-progress! :test-var digest-ns digest-var
                                     #(with-bindings {#'runner/*progress-state* nil}
                                        (invoke)))))
    (is (= 4 @calls))
    (is (not-any? #(contains? #{:resolution-digest-positive-resolve}
                              (:phase %))
                  (map edn/read-string (str/split-lines (str output)))))))

(deftest legacy-zero-repeated-and-unknown-intervals-preserve-local-execution
  (doseq [n [0 2]]
    (let [state (#'runner/progress-state)
          output (java.io.StringWriter.)
          calls (atom 0)
          value (Object.)]
      (binding [*err* output]
        (#'runner/run-with-progress!
         state
         (fn []
           (#'runner/observe-progress!
            :test-var digest-ns digest-var
            (fn []
              (dotimes [_ n]
                (is (identical?
                    value
                    (runner/observe-resolution-digest-interval!
                     :positive-resolve #(do (swap! calls inc) value))))))))))
      (is (= n @calls))
      (is (= (+ 2 (* n 2))
             (count (str/split-lines (str output)))))))
  (let [state (#'runner/progress-state)
        output (java.io.StringWriter.)
        calls (atom 0)
        foreign-calls (atom 0)
        hostile (proxy [Object] []
                  (hashCode [] (swap! foreign-calls inc) 1)
                  (toString [] (swap! foreign-calls inc) "unreadable"))]
    (binding [*err* output]
      (#'runner/run-with-progress!
       state
       (fn []
         (#'runner/observe-progress!
          :test-var digest-ns digest-var
          (fn []
            (doseq [interval [:not-selected hostile]]
              (runner/observe-resolution-digest-interval!
               interval #(swap! calls inc))))))))
    (is (= 2 @calls))
    (is (zero? @foreign-calls))
    (is (= 2 (count (str/split-lines (str output))))))
  (let [error (LinkageError.)
        r (exercise-legacy {:original error :diagnostic error
                            :fault-stage :write :fault-edge :enter})]
    (is (identical? error (get-in r [:result :error])))
    (is (not-any? #(identical? error %)
                  (.getSuppressed ^Throwable error)))))

(def artifact-options
  {:selected-var artifact-var
   :selected-intervals [:construction :verification :serialization]
   :selected-phases #{:resolution-current-artifact-construction
                      :resolution-current-artifact-verification
                      :resolution-current-artifact-serialization}
   :selected-helper runner/observe-resolution-current-artifact-interval!})

(deftest legacy-current-artifact-fixed-intervals-and-disabled-path
  (let [r (exercise-legacy artifact-options)]
    (is (= :original-value (get-in r [:result :value])))
    (is (= [:construction :finally :verification :finally
            :serialization :finally] (:events r)))
    (is (= 8 (count (:rows r))))
    (is (= (:selected-phases artifact-options)
           (set (map :phase (filter #(contains? (:selected-phases artifact-options)
                                                   (:phase %))
                                    (:rows r))))))
    (is (every? #(= :non-authoritative (:authority %)) (:rows r)))
    (is (every? #(= #{:schema :authority :phase :edge :namespace :test-var}
                    (set (keys %)))
                (:rows r))))
  (let [r (exercise-legacy (assoc artifact-options :enabled? false))]
    (is (= :original-value (get-in r [:result :value])))
    (is (= 6 (count (:events r))))
    (is (empty? (:rows r)))))

(deftest legacy-current-artifact-diagnostics-preserve-body-and-sticky-failure
  (doseq [stage [:format :write :flush] edge [:enter :return]]
    (let [error (java.io.IOException.)
          r (exercise-legacy (assoc artifact-options
                                     :fault-stage stage :fault-edge edge
                                     :diagnostic error :swallow? true
                                     :timing? true))]
      (is (identical? error (get-in r [:result :error])))
      (is (= 6 (count (:events r))))
      (is (zero? (:timings r)))
      (is (zero? (:sinks r)))))
  (doseq [original [(Exception.) (LinkageError.) (InterruptedException.)]]
    (let [diagnostic (InterruptedException.)
          r (exercise-legacy (assoc artifact-options
                                     :fault-stage :flush :fault-edge :enter
                                     :diagnostic diagnostic :original original))]
      (is (identical? original (get-in r [:result :error])))
      (is (some #(identical? diagnostic %)
                (.getSuppressed ^Throwable original)))
      (is (:interrupted? r))
      (is (= [:construction :finally] (:events r))))))

(deftest legacy-current-artifact-scope-and-live-callbacks-remain-closed
  (let [state (#'runner/progress-state)
        other (#'runner/progress-state)
        output (java.io.StringWriter.)
        calls (atom 0)
        invoke (fn []
                 (runner/observe-resolution-current-artifact-interval!
                  :construction (fn [] (swap! calls inc))))]
    (binding [*err* output]
      (with-bindings {#'runner/*progress-state* state}
        (invoke)
        (#'runner/observe-progress! :test-var digest-ns digest-var invoke)
        (#'runner/observe-progress!
         :test-var digest-ns artifact-var
         (fn []
           (runner/observe-resolution-digest-interval!
            :positive-resolve (fn [] (swap! calls inc)))
           (runner/observe-resolution-current-artifact-interval!
            :unknown (fn [] (swap! calls inc)))
           (with-bindings {#'runner/*progress-state* other} (invoke))
           (with-bindings {#'runner/*progress-state* nil} (invoke))))))
    (is (= 6 @calls))
    (is (not-any? #(contains? (set (:selected-phases artifact-options))
                              (:phase %))
                  (map edn/read-string (str/split-lines (str output))))))
  (let [state (#'runner/progress-state)
        output (java.io.StringWriter.)
        callback (atom (fn [] :first))
        events (atom [])
        value (Object.)]
    (binding [*err* output]
      (#'runner/run-with-progress!
       state
       (fn []
         (#'runner/observe-progress!
          :test-var digest-ns artifact-var
          (fn []
           (is (identical?
               value
               (runner/observe-resolution-current-artifact-interval!
                :construction
                (fn [] (swap! events conj (@callback))
                    (reset! callback (fn [] :second))
                    value))))
            (runner/observe-resolution-current-artifact-interval!
             :verification (fn [] (swap! events conj (@callback))))
            (runner/observe-resolution-current-artifact-interval!
             :serialization (fn [] (swap! events conj (@callback))))))))
    (is (= [:first :second :second] @events)))))

(let [result (run-tests)]
  (System/exit (if (zero? (+ (:fail result) (:error result))) 0 1)))
