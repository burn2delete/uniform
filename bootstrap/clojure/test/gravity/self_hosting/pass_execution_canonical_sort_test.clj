(ns gravity.self-hosting.pass-execution-canonical-sort-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.pass-execution :as execution]
            [gravity.pass-execution.canonical :as canonical]
            [gravity.pass-execution-test :as fixture]))

(defn- legacy-sort [values] (vec (sort-by pr-str values)))
(defn- fixture-fn [name]
  (var-get (ns-resolve 'gravity.pass-execution-test name)))
(defn- fixture-request []
  ((fixture-fn 'request) ((fixture-fn 'pass-contract) :parse 10)
   [(:input-a fixture/ids)] #{}))
(defn- execute [request calls]
  (execution/execute-pass!
   request ((fixture-fn 'operations) (:output-a fixture/ids) calls)))
(defn- diagnostic [run]
  (try (run) nil (catch clojure.lang.ExceptionInfo failure (ex-data failure))))

(deftest production-receipts-preserve-exact-values-and-operation-order
  (doseq [request [(fixture-request)
                   (assoc-in (fixture-request) [:provenance :metadata]
                             {:nested #{[1 2] [2 1]} :types [1 1N 1/2 \a]})]]
    (let [legacy-calls (atom []) candidate-calls (atom [])
          prior (with-redefs [canonical/canonical-sort legacy-sort]
                  (execute request legacy-calls))
          candidate (execute request candidate-calls)]
      (is (= prior candidate))
      (is (= [:produce :validate-output :artifact-id :verifier :evidence]
             @legacy-calls @candidate-calls))
      (is (= (seq (with-redefs [canonical/canonical-sort legacy-sort]
                    (canonical/canonical-bytes prior)))
             (seq (canonical/canonical-bytes candidate)))))))

(deftest canonical-bytes-and-diagnostics-match-the-prior-production-path
  (doseq [value [nil [] #{} {} '(1 1N 1/2) [\a "quoted\"" "line\n"]
                 {:nested {:set #{:b :a} :list '(nil true false)}
                  :integrals [(byte 1) (short 1) (int 1) (long 1) 1N]}
                 (into {} (map (fn [i] [(keyword (str "k" i)) (hash-set i (- i))])
                               (range 128)))]]
    (is (= (seq (with-redefs [canonical/canonical-sort legacy-sort]
                 (canonical/canonical-bytes value)))
           (seq (canonical/canonical-bytes value)))))
  (doseq [value [(with-meta {:a 1} {:host true}) (Object.)
                 (reduce (fn [x _] [x]) nil (range 66))
                 (vec (repeat 17000 nil))]]
    (let [prior (with-redefs [canonical/canonical-sort legacy-sort]
                  (diagnostic #(canonical/canonical-bytes value)))]
      (is (= "C16-KEY" (:id prior)))
      (is (= prior (diagnostic #(canonical/canonical-bytes value)))))))

(deftest sorting-serializes-each-entry-once-and-retains-collision-order
  (let [values (vec (reverse (range 128))) calls (atom []) original pr-str]
    (is (= (vec (sort-by original values))
           (with-redefs [clojure.core/pr-str
                         (fn [value] (swap! calls conj value) (original value))]
             (canonical/canonical-sort values))))
    (is (= values @calls)))
  ;; Truncated printing creates equal keys. Both implementations use stable
  ;; sorting, so the original insertion order of these collisions survives.
  (binding [*print-length* 0]
    (let [values [[3] [1] [2]]]
      (is (= values (legacy-sort values) (canonical/canonical-sort values)))))
  (let [values [[3] [1] [2]]]
    (is (= (legacy-sort values) (canonical/canonical-sort values))))
  (is (= [] (canonical/canonical-sort []))))

(deftest actual-receipt-path-serializes-once-per-sort-entry
  (let [sort-entry canonical/canonical-sort original pr-str counts (atom [])]
    (with-redefs [canonical/canonical-sort
                  (fn [values]
                    (let [calls (atom 0)
                          result (with-redefs [clojure.core/pr-str
                                               (fn [& args]
                                                 (swap! calls inc)
                                                 (apply original args))]
                                   (sort-entry values))]
                      (swap! counts conj [(count values) @calls])
                      result))]
      (execute (fixture-request) (atom [])))
    (is (seq @counts))
    (doseq [[entries calls] @counts] (is (= entries calls)))))

(deftest mutation-concurrency-and-failure-do-not-reuse-decorations
  (let [request (fixture-request)
        changed (assoc-in request [:semantic-bindings :compiler-id]
                          (:target fixture/ids))
        first-result (execute request (atom []))
        changed-result (execute changed (atom []))]
    (is (not= (get-in first-result [:receipt :receipt-id])
              (get-in changed-result [:receipt :receipt-id])))
    (is (= first-result (execute request (atom []))))
    (let [jobs (mapv (fn [index]
                       (future (execute (if (even? index) request changed) (atom []))))
                     (range 8))]
      (doseq [[index job] (map-indexed vector jobs)]
        (is (= (if (even? index) first-result changed-result) (deref job 10000 ::timeout)))))
    (let [calls (atom []) failure
          (diagnostic
           #(execution/execute-pass!
             request (assoc ((fixture-fn 'operations) (:output-a fixture/ids) calls)
                            :validate-output! (fn [& _] (throw (ex-info "stop" {:id "STOP"}))))))]
      (is (= "STOP" (:id failure)))
      (is (= [:produce] @calls))
      (is (= first-result (execute request (atom [])))))))
