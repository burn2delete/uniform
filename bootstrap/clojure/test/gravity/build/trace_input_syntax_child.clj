(ns gravity.build.trace-input-syntax-child
  "Child-local binding for the single trace input-syntax control. No launcher,
  guard, parent report hook, lifecycle admission or genuine-execution authority."
  (:require [clojure.test :as test]
            [gravity.build.trace-input-syntax-receipt :as receipt]
            [gravity.development-test-runner :as development])
  (:import [java.nio.file SecureDirectoryStream]))

(def ^:private selection
  {:namespaces [(namespace receipt/selector)]
   :test-vars [(str receipt/selector)] :fail-fast? false})

(def ^:private arguments
  ["--namespace" (namespace receipt/selector) "--exact" (name receipt/selector)])

(defn- machine-checks [results run failures]
  (let [result (first results) counts (:summary result)
        complete? (and (map? counts) (= #{:test :pass :fail :error} (set (keys counts)))
                       (every? #(and (integer? %) (<= 0 %))
                              (map #(get counts %) [:test :pass :fail :error]))
                       (= 1 (:test counts)))
        passed? (and complete? (zero? (:fail counts)) (zero? (:error counts)))
        expected-exit (if passed? 0 1)]
    {:one-machine-result (= 1 (count results))
     :closed-machine-shape (and (map? result)
                                 (= #{:schema :authority :authoritative? :selection :summary
                                      :outcome :exit-code} (set (keys result))))
     :machine-schema (= development/machine-result-schema (:schema result))
     :machine-authority (and (= :non-authoritative (:authority result))
                              (false? (:authoritative? result)))
     :exact-machine-selection (= selection (:selection result))
     :complete-machine-counters (boolean complete?)
     :machine-outcome (= (if passed? :passed :failed) (:outcome result))
     :machine-exit (= expected-exit (:exit-code result))
     :original-run-return (and (:returned? run) (= (:return run) (:exit-code result)))
     :no-failure-reports (empty? failures)}))

(defn- finish! [observations directory admit-bytes! provenance run results failures]
  (let [reported-error (some #(when (and (= :error (:type %))
                                        (instance? Throwable (:actual %))) (:actual %)) failures)
        completion {:returned? (:returned? run) :run-return (:return run)
                    :run-error (:error run) :error (or (:error run) reported-error)
                    :counts (when (= 1 (count results)) (:summary (first results)))
                    :machine-results results :failure-reports failures}
        collected (receipt/collect observations completion provenance)
        checks (merge (:checks collected) (machine-checks results run failures))
        collected (assoc collected :checks checks
                         :validation (if (every? true? (vals checks))
                                       :consistent-supplied-observation :nonpassing))]
    ;; Return raw local evidence even when serialization/capture/CREATE_NEW
    ;; fails. A caller must never infer success from the original CLI integer.
    (try
      {:collected collected :publication (receipt/publish! directory collected admit-bytes!)}
      (catch Throwable error
        {:collected collected :publication-error error}))))

(defn invoke!
  "Call original development/run-cli! with the one fixed selector, inside an
  ALREADY owner-established child. Caller supplies the actual observations
  IDeref, private secure directory, existing byte-admission callback and exact
  provenance. Type checks do not authenticate any of these or admit execution.

  Bind the existing machine sink and clojure.test's dynamic report port only
  for run-cli!. Delegate each actual call to the prior sink/reporter, retaining
  raw result and failure/error report references. The stock sink is nil. The
  report port is necessary because test-var catches test-body Throwables before
  the machine sink sees only counters. No reporter or runner Var root is changed.

  Collection/publication happen after run-cli! returns or throws, after the
  bindings restore. Counts include fixtures/finally only if actually emitted;
  missing/multiple/malformed sink results remain nonpassing. No synthetic test
  result, restoration fact or cleanup fact is supplied.

  Normal return is an envelope containing the unchanged :run-return, raw
  :collected evidence and publication/error. :binding-status is consistency
  only, never genuine PASS or parent terminal/release. The owning child transport
  must carry receipt/nonpassing facts; :run-return alone never admits output.
  Original runner throws are rethrown identically; evidence failures are
  suppressed on that original.
  No retry, output deletion, alternate path, process/lease work or new limits."
  [observations directory admit-bytes! provenance]
  (when-not (and (instance? clojure.lang.IDeref observations)
                 (instance? SecureDirectoryStream directory) (ifn? admit-bytes!))
    (throw (ex-info "Trace child binding requires owner-provided observation and output ports"
                    {:id "TRACE-CHILD-PORTS"})))
  (let [results (atom []) failures (atom [])
        prior-sink development/*machine-result-sink* prior-report test/report
        run (try
              {:returned? true
               :return (binding [development/*machine-result-sink*
                                 (fn [result]
                                   (swap! results conj result)
                                   (when prior-sink (prior-sink result)))
                                 test/report
                                 (fn [report]
                                   (when (#{:fail :error} (:type report))
                                     (swap! failures conj report))
                                   (prior-report report))]
                         (development/run-cli! arguments))}
              (catch Throwable error {:returned? false :error error}))
        finished (try
                   (finish! observations directory admit-bytes! provenance run @results @failures)
                   (catch Throwable error {:collection-error error}))
        evidence-error (or (:collection-error finished) (:publication-error finished))]
    (if-let [original (:error run)]
      (do
        (when (and evidence-error (not (identical? original evidence-error)))
          (.addSuppressed ^Throwable original ^Throwable evidence-error))
        (throw original))
      (merge {:run-return (:return run)
              :binding-status (if (and (not evidence-error)
                                       (= :consistent-supplied-observation
                                          (get-in finished [:collected :validation])))
                                :consistent-supplied-observation :nonpassing)
              :method-status :constructed-unadmitted
              :genuine-execution-established? false :cache-admission? false}
             finished))))
