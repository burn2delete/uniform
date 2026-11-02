(ns process-lease-custody.check
  "Compiler-free failure controls. Never acquires a lease or starts a child."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is run-tests]]))

(load-file "bootstrap/clojure/test/gravity/self_hosting/sh01_host_resource_broker.clj")
(require '[gravity.self-hosting.sh01-host-resource-broker :as broker])

(def broker-ns 'gravity.self-hosting.sh01-host-resource-broker)
(def quarantines (var-get (ns-resolve broker-ns 'quarantined-leases)))
(def scenario (atom {}))
(def calls (atom []))
(def model-ns (create-ns 'process-lease-custody.runner-model))

;; Evaluate ONLY the actual Clojure process-result definition, with every
;; process wait/capture/finalizer dependency replaced by a local model. Never
;; require the runner, planner, Bootstrap or any Gravity implementation.
(def process-result-form
  (with-open [reader (java.io.PushbackReader.
                     (io/reader "bootstrap/clojure/test/gravity/self_hosting/sh01_parallel_test_runner.clj"))]
    (binding [*read-eval* false]
      (let [eof (Object.)]
        (loop []
          (let [form (read {:eof eof} reader)]
            (cond
              (identical? form eof) (throw (ex-info "Missing process-result" {}))
              (and (seq? form) (= 'defn (first form))
                   (= 'process-result (second form))) form
              :else (recur))))))))

(binding [*ns* model-ns]
  (clojure.core/refer 'clojure.core)
  (alias 'broker broker-ns))

(doseq [[name implementation]
        {'require-retained-process! (fn [owned] (assert (:model-token owned)))
         'retain-owned-process! (fn [process]
                                  {:model-token true :process process
                                   :terminal (atom nil) :wait-throwable (atom nil)})
         'retained-process-result (fn [_ _ _]
                                    (swap! calls conj :result)
                                    (if-let [error (:throw @scenario)]
                                      (throw error)
                                      (:result @scenario)))
         'finalize-owned-process! (fn [_]
                                    (swap! calls conj :finalize)
                                    (when-let [error (:cleanup-throw @scenario)]
                                      (throw error)))
         'fatal-cause (constantly nil)
         'interrupted-throwable? #(instance? InterruptedException %)
         'restore-interrupt! (fn [_] (swap! calls conj :restore))
         'close-stream! (fn [_] (swap! calls conj :close))}]
  (intern model-ns name implementation))

(binding [*ns* model-ns] (eval process-result-form))
(def process-result (var-get (ns-resolve model-ns 'process-result)))
(def retained-process (var-get (ns-resolve model-ns 'retain-owned-process!)))

(defn model-process []
  (proxy [Process] []
    (getInputStream [] (java.io.ByteArrayInputStream. (byte-array 0)))
    (getErrorStream [] (java.io.ByteArrayInputStream. (byte-array 0)))
    (getOutputStream [] (java.io.ByteArrayOutputStream.))
    (waitFor [] (throw (AssertionError. "No process wait allowed")))
    (exitValue [] (throw (AssertionError. "No process observation allowed")))
    (destroy [] (throw (AssertionError. "No termination allowed")))))

(def complete
  {:status :passed :exit-code 0
   :root-exited? true :cleanup-root-alive? false
   :tracked-owned-processes-terminal? true :descendant-coverage :unknown
   :cleanup-complete? nil :capture-failed? false
   :stdout-capture-status :complete :stderr-capture-status :complete
   :stdout-capture-error nil :stderr-capture-error nil
   :stdout-capture-forced-close? false :stderr-capture-forced-close? false
   :stdout "model stdout" :stderr "model stderr"})

(defn exercise [thunk]
  (reset! quarantines {})
  (reset! calls [])
  (let [released (atom false)
        lease {:released? released :coordination-root "model-root"}
        events (atom [])
        outcome
        (with-redefs [broker/acquire! (fn [options resource-class]
                                       (is (= :memory-heavy resource-class))
                                       (is (= "model-root" (:coordination-root options)))
                                       (swap! calls conj :acquire-model)
                                       lease)
                      broker/release! (fn [actual]
                                       (is (identical? lease actual))
                                       (is (false? @released))
                                       (reset! released true)
                                       (swap! calls conj :release-model)
                                       :released-model)]
          (try
            {:result (broker/with-lease {:coordination-root "model-root"
                                        :on-event #(swap! events conj %)}
                                       :memory-heavy thunk)}
            (catch Throwable error {:error error})))
        quarantine (get @quarantines released)]
    (assoc outcome :released? @released :quarantine quarantine
           :events @events :calls @calls :lease lease)))

(defn run-model [result]
  (reset! scenario {:result result})
  (let [process (model-process)
        owned (retained-process process)]
    (assoc (exercise #(process-result process {} owned)) :owned owned)))

(deftest normal-terminal-outcomes-release-without-rewriting-results
  (doseq [result [complete
                  (assoc complete :status :failed :exit-code 7)
                  (assoc complete :stdout-capture-status :truncated
                         :stdout-truncated? true :output-truncated? true)
                  (assoc complete :status :timeout :exit-code 124
                         :cleanup-complete? false :timeout-containment-unproven? true)]]
    (let [outcome (run-model result)]
      (is (identical? result (:result outcome)))
      (is (:released? outcome))
      (is (nil? (:quarantine outcome)))
      (is (empty? (:events outcome)))
      (is (= [:acquire-model :result :release-model] (:calls outcome))))))

(def incomplete-results
  (concat
   [(assoc complete :cleanup-root-alive? true)
    (assoc complete :root-exited? false)
    (assoc complete :tracked-owned-processes-terminal? false)
    (assoc complete :capture-failed? true)]
   (for [key [:root-exited? :cleanup-root-alive?
              :tracked-owned-processes-terminal? :capture-failed?
              :stdout-capture-status :stderr-capture-status
              :stdout-capture-forced-close? :stderr-capture-forced-close?]]
     (dissoc complete key))
   (for [stream ["stdout" "stderr"] status [:capture-timeout :error :closed :fatal nil]]
     (assoc complete (keyword (str stream "-capture-status")) status))
   (for [stream ["stdout" "stderr"]]
     (assoc complete (keyword (str stream "-capture-error")) "model capture error"))
   (for [stream ["stdout" "stderr"]]
     (assoc complete (keyword (str stream "-capture-forced-close?")) true))))

(deftest failure-facts-quarantine-even-if-the-status-says-passed
  (doseq [result incomplete-results]
    (let [outcome (run-model result)
          custody (first @(:processes (:quarantine outcome)))]
      (is (false? (:released? outcome)))
      (is (identical? result (:result outcome)))
      (is (identical? (:lease outcome) (get-in outcome [:quarantine :lease])))
      (is (identical? (:owned outcome) (:owned @custody)))
      (is (identical? result (:result @custody)))
      (is (= [:acquire-model :result] (:calls outcome)))
      (is (= [:quarantined] (mapv :outcome (:events outcome))))
      (is (= "SH01-BROKER-PROCESS-CUSTODY"
             (:diagnostic-id (first (:events outcome))))))))

(deftest thrown-process-result-preserves-original-error-and-custody
  (doseq [cleanup-fails? [false true]]
    (let [original (ex-info "original model error" {:source :model})
          cleanup-error (ex-info "model cleanup error" {})
          _ (reset! scenario (cond-> {:throw original}
                               cleanup-fails? (assoc :cleanup-throw cleanup-error)))
          process (model-process)
          owned (retained-process process)
          outcome (exercise #(process-result process {} owned))
          custody (first @(:processes (:quarantine outcome)))]
      (is (identical? original (:error outcome)))
      (is (false? (:released? outcome)))
      (is (identical? original (get-in @custody [:result :error])))
      (is (identical? owned (:owned @custody)))
      (is (= (if cleanup-fails? [cleanup-error] [])
             (vec (.getSuppressed original))))
      (is (= [:acquire-model :result :finalize :close :close :restore]
             (:calls outcome))))))

(deftest all-process-results-in-one-thunk-must-be-terminal
  (let [outcome
        (exercise #(do
                     (reset! scenario {:result (assoc complete :root-exited? false)})
                     (process-result (model-process) {})
                     (reset! scenario {:result complete})
                     (process-result (model-process) {})))]
    (is (false? (:released? outcome)))
    (is (= 2 (count @(:processes (:quarantine outcome)))))
    (is (identical? complete (:result outcome)))))

(deftest a-later-nonprocess-throw-does-not-hide-completed-process-facts
  (reset! scenario {:result complete})
  (let [original (ex-info "after complete result" {})
        outcome (exercise #(do (process-result (model-process) {})
                               (throw original)))]
    (is (identical? original (:error outcome)))
    (is (:released? outcome))
    (is (nil? (:quarantine outcome)))))

(deftest nonprocess-thunks-and-unleased-process-api-keep-existing-behavior
  (let [result {:arbitrary :value} outcome (exercise (constantly result))]
    (is (identical? result (:result outcome)))
    (is (:released? outcome)))
  (let [original (ex-info "nonprocess failure" {})
        outcome (exercise #(throw original))]
    (is (identical? original (:error outcome)))
    (is (:released? outcome)))
  (reset! quarantines {})
  (reset! scenario {:result complete})
  (is (identical? complete (process-result (model-process) {})))
  (is (empty? @quarantines)))

(let [summary (run-tests 'process-lease-custody.check)]
  (assert (nil? (find-ns 'gravity.bootstrap)))
  (prn {:schema :process-lease-custody/model-controls-v1
        :tests (:test summary) :assertions (:pass summary)
        :failures (:fail summary) :errors (:error summary)
        :negative-result-cases (count incomplete-results)
        :actual-children 0 :actual-leases 0 :actual-process-probes 0
        :runner-required false :bootstrap-loaded false})
  (reset! quarantines {})
  (when (pos? (+ (:fail summary) (:error summary))) (System/exit 1)))
