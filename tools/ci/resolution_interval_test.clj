(ns ci.resolution-interval-test
  (:require [clojure.test :refer [deftest is]]
            [clojure.edn :as edn] [clojure.java.io :as io] [clojure.string :as str]))

;; Evaluate only the actual reporting functions in an own fixture namespace.
;; No Bootstrap, semantic test namespace, compiler, native hash or workload loads.
(def fixture-ns (create-ns 'ci.resolution-interval-fixture))
(binding [*ns* fixture-ns] (refer 'clojure.core) (alias 'test 'clojure.test))
(def reporting-forms
  '#{*progress-state* *progress-test-context* progress-state retain-progress-error!
     inspect-progress-error note-progress-interrupt! emit-progress! resolution-digest-test?
     observe-progress! observe-resolution-digest-interval! require-progress-success! run-with-progress!})
(binding [*ns* fixture-ns *read-eval* false]
  (with-open [r (clojure.lang.LineNumberingPushbackReader.
                 (io/reader "bootstrap/clojure/test/gravity/development_test_runner.clj"))]
    (loop [seen #{}]
      (let [form (read {:eof ::end} r)]
        (if (= ::end form)
          (assert (= reporting-forms seen))
          (if (and (seq? form) (contains? reporting-forms (second form)))
            (do (eval form) (recur (conj seen (second form))))
            (recur seen)))))))
(defn port [s] (ns-resolve fixture-ns s))
(defn function [s] @(port s))
(def target-ns "gravity.self-hosting.sh06-unmetered-resolution-test")
(def target-var (str target-ns "/digest-count-quota-is-gone-but-complete-ordered-requests-remain-required"))
(def intervals [:positive-resolve :count-and-hash-shape :ordinal-negative :count-negative :shape-negative])
(def phases #{:resolution-digest-positive-resolve :resolution-digest-count-and-hash-shape
              :resolution-digest-ordinal-negative :resolution-digest-count-negative :resolution-digest-shape-negative})
(defn caught [body] (try {:value (body)} (catch Throwable error {:error error})))
(defn exercise [{:keys [enabled? timing? fault-stage fault-edge diagnostic original swallow?]
                 :or {enabled? true}}]
  (let [state ((function 'progress-state)) events (atom []) timings (atom 0) sinks (atom 0)
        text (StringBuilder.) raw-pr pr-str
        fail-row? #(and (contains? phases (:phase %)) (= fault-edge (:edge %)))
        accept! (fn [s]
                  (let [row (edn/read-string s)]
                    (.append text s)
                    (when (and (= fault-stage :write) (fail-row? row)) (throw diagnostic))))
        writer (proxy [java.io.Writer] []
                 (write
                   ([buffer] (accept! (if (string? buffer) buffer (String. ^chars buffer))))
                   ([buffer offset length]
                    (accept! (if (string? buffer) (subs buffer offset (+ offset length))
                                 (String. ^chars buffer (int offset) (int length))))))
                 (flush []
                   (when (and (= fault-stage :flush) (seq (str text))
                              (fail-row? (edn/read-string (last (str/split-lines (str text))))))
                     (throw diagnostic)))
                 (close []))
        body (fn []
               (doseq [interval intervals]
                 (let [call #(do (swap! events conj interval)
                                 (try (when original (throw original))
                                      :original-value
                                      (finally (swap! events conj :finally) (Thread/interrupted))))]
                   (if swallow?
                     (try ((function 'observe-resolution-digest-interval!) interval call) (catch Throwable _ nil))
                     ((function 'observe-resolution-digest-interval!) interval call))))
               :original-value)
        run #(let [v ((function 'observe-progress!) :test-var target-ns target-var body)]
               ((function 'require-progress-success!))
               (when timing? (swap! timings inc)) (swap! sinks inc) v)
        result (binding [*err* writer]
                 (with-redefs [clojure.core/pr-str
                               (fn [& args]
                                 (when (and (= fault-stage :format) (fail-row? (first args))) (throw diagnostic))
                                 (apply raw-pr args))]
                   (with-bindings {(port '*progress-state*) (when enabled? state)}
                     (caught #(if enabled? ((function 'run-with-progress!) state run) (body))))))
        interrupted? (Thread/interrupted)]
    {:result result :events @events :timings @timings :sinks @sinks :interrupted? interrupted?
     :rows (mapv edn/read-string (remove empty? (str/split-lines (str text)))) :state state}))

(deftest fixed-intervals-return-value-order-and-no-diagnostic-authority
  (let [r (exercise {}) rows (:rows r)]
    (is (= :original-value (get-in r [:result :value])))
    (is (= (vec (mapcat #(vector % :finally) intervals)) (:events r)))
    (is (= 12 (count rows)))
    (is (= phases (set (map :phase (filter #(contains? phases (:phase %)) rows)))))
    (is (= 6 (count (filter #(= :enter (:edge %)) rows))))
    (is (= 6 (count (filter #(= :return (:edge %)) rows))))
    (is (every? #(= :non-authoritative (:authority %)) rows))
    (is (every? #(not-any? (set (keys %)) [:argv :environment :message :stack :result :summary]) rows)))
  (let [r (exercise {:enabled? false})]
    (is (= :original-value (get-in r [:result :value])))
    (is (= 10 (count (:events r)))) (is (empty? (:rows r)))))

(deftest diagnostic-failures-stay-sticky-with-original-body-and-finally
  (doseq [stage [:format :write :flush] edge [:enter :return] timing? [false true] swallow? [false true]]
    (let [error (java.io.IOException.)
          r (exercise {:fault-stage stage :fault-edge edge :diagnostic error :timing? timing? :swallow? swallow?})]
      (is (identical? error (get-in r [:result :error])))
      (is (= 10 (count (:events r))))
      (is (zero? (:timings r))) (is (zero? (:sinks r)))
      (is (= 1 (.size ^java.util.ArrayList (get-in r [:state :errors])))))))

(deftest original-primary-and-causal-interrupt-survive-clearing-unwind
  (doseq [[original original-interrupt?]
          [[(Exception.) false] [(LinkageError.) false] [(InterruptedException.) true]
           [(Exception. "wrapped" (InterruptedException.)) true]
           [(proxy [Exception] [] (getCause [] (throw (Exception. "unreadable")))) false]]
          diagnostic [(java.io.IOException.) (InterruptedException.)]]
    (let [r (exercise {:fault-stage :write :fault-edge :enter :original original :diagnostic diagnostic})]
      (is (identical? original (get-in r [:result :error])))
      (is (some #(identical? diagnostic %) (.getSuppressed ^Throwable original)))
      (is (= (or original-interrupt? (instance? InterruptedException diagnostic)) (:interrupted? r)))
      (is (= [:positive-resolve :finally] (:events r))))))

(deftest outside-scope-and-nested-state-cannot-emit-fixed-intervals
  (let [state ((function 'progress-state)) other ((function 'progress-state))
        output (java.io.StringWriter.) calls (atom 0)
        invoke (fn [] ((function 'observe-resolution-digest-interval!) :positive-resolve #(swap! calls inc)))]
    (binding [*err* output]
      (with-bindings {(port '*progress-state*) state}
        (invoke)
        ((function 'observe-progress!) :test-var "other" "other/test" invoke)
        ((function 'observe-progress!) :test-var target-ns target-var
                                       #(with-bindings {(port '*progress-state*) other} (invoke)))
        ((function 'observe-progress!) :test-var target-ns target-var
                                       #(with-bindings {(port '*progress-state*) nil} (invoke)))))
    (is (= 4 @calls))
    (is (not-any? #(contains? phases (:phase %)) (map edn/read-string (str/split-lines (str output)))))))

(deftest zero-repeated-and-unknown-intervals-preserve-once-local-execution
  (doseq [n [0 2]]
    (let [state ((function 'progress-state)) output (java.io.StringWriter.) calls (atom 0)
          value (Object.)]
      (binding [*err* output]
        ((function 'run-with-progress!) state
                                        (fn [] ((function 'observe-progress!) :test-var target-ns target-var
                                                                              (fn [] (dotimes [_ n]
                                                                                       (is (identical? value ((function 'observe-resolution-digest-interval!)
                                                                                                              :positive-resolve #(do (swap! calls inc) value))))))))))
      (is (= n @calls))
      (is (= (+ 2 (* n 2)) (count (str/split-lines (str output)))))))
  (let [state ((function 'progress-state)) output (java.io.StringWriter.) calls (atom 0)
        foreign-calls (atom 0) hostile (proxy [Object] []
                                         (hashCode [] (swap! foreign-calls inc) 1)
                                         (toString [] (swap! foreign-calls inc) "unreadable"))]
    (binding [*err* output]
      ((function 'run-with-progress!) state
                                      (fn [] ((function 'observe-progress!) :test-var target-ns target-var
                                                                            (fn [] (doseq [interval [:not-selected hostile]]
                                                                                     ((function 'observe-resolution-digest-interval!) interval #(swap! calls inc))))))))
    (is (= 2 @calls)) (is (zero? @foreign-calls))
    (is (= 2 (count (str/split-lines (str output))))))
  (let [error (LinkageError.) r (exercise {:original error :diagnostic error :fault-stage :write :fault-edge :enter})]
    (is (identical? error (get-in r [:result :error])))
    (is (not-any? #(identical? error %) (.getSuppressed error)))))
