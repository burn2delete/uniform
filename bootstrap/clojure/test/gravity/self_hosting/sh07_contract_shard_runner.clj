(ns gravity.self-hosting.sh07-contract-shard-runner
  "Runs the lightweight SH-07-B11 contract shard.

  This shard performs no artifact construction and is safe to run alongside
  the heavyweight accepted, rejected, identity, and replay-validation lanes.

  Run with:

    clojure -Sdeps '{:paths [\"bootstrap/clojure/src\"
                            \"bootstrap/clojure/test\"]}'
      -M -m gravity.self-hosting.sh07-contract-shard-runner"
  (:require [clojure.test :as test]
            [gravity.self-hosting.sh07-vector-pattern-test]))

(def ^:private test-namespace
  'gravity.self-hosting.sh07-vector-pattern-test)

(def ^:private test-symbols
  '[sh07-b11-fixtures-are-paired-byte-identical-and-bounded-in-scope
    sh07-b11-gravity-contract-names-l7])

(defn- resolve-test
  [test-symbol]
  (let [test-var (ns-resolve test-namespace test-symbol)]
    (when-not (and (var? test-var) (:test (meta test-var)))
      (throw
       (ex-info
        "SH-07 contract shard references an absent test"
        {:id "SH07-CONTRACT-SHARD-TEST"
         :namespace test-namespace
         :test test-symbol})))
    test-var))

(defn run-contract-shard
  []
  (let [test-vars (mapv resolve-test test-symbols)
        counters (ref test/*initial-report-counters*)]
    (binding [test/*report-counters* counters]
      (test/test-vars test-vars))
    (assoc @counters
           :shard :sh07-contract
           :test-vars test-symbols)))

(defn -main
  [& arguments]
  (when (seq arguments)
    (throw
     (ex-info
      "SH-07 contract shard accepts no arguments"
      {:id "SH07-CONTRACT-SHARD-USAGE"
       :arguments (vec arguments)})))
  (let [result (run-contract-shard)]
    (println (pr-str result))
    (when-not (and (zero? (:fail result)) (zero? (:error result)))
      (System/exit 1))))
