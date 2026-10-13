(ns gravity.self-hosting.sh07-parallel-shard-runner
  "Process-isolated routing for heavyweight SH-07 test shards.

  Use `--list` to print shard names, `--check` to resolve every routed test
  without running it, or pass one shard name to run that shard."
  (:require [clojure.test :as test]
            [gravity.self-hosting.unified-execution-plan :as execution-plan]))

(def ^:private legacy-mechanism :parallel-shard-runner)

(defn- parallel-projection
  []
  (execution-plan/legacy-mechanism-projection legacy-mechanism))

(defn shard-names
  []
  (:labels (parallel-projection)))

(defn check-shards
  []
  ((requiring-resolve 'gravity.self-hosting.unified-execution-cli/check-legacy-shards)
   legacy-mechanism))

(defn run-shard
  [shard-name]
  ;; Reject bad names before loading the shared CLI's execution substrates.
  (when-not (and (string? shard-name) (some #{shard-name} (shard-names)))
    (throw (ex-info "Unknown SH-07 parallel test shard"
                    {:id "SH07-PARALLEL-SHARD"
                     :shard shard-name :available (shard-names)})))
  (let [{:keys [test-vars]}
        ((requiring-resolve 'gravity.self-hosting.unified-execution-cli/resolve-legacy-shard)
         legacy-mechanism shard-name)
        counters (ref test/*initial-report-counters*)]
    (binding [test/*report-counters* counters]
      (test/test-vars test-vars))
    (assoc @counters
           :shard shard-name
           :test-vars (count test-vars))))

(defn -main
  [& arguments]
  (cond
    (= ["--list"] (vec arguments))
    (doseq [shard-name (shard-names)]
      (println shard-name))

    (= ["--check"] (vec arguments))
    (println (pr-str (check-shards)))

    (= 1 (count arguments))
    (let [result (run-shard (first arguments))]
      (println (pr-str result))
      (when-not (and (zero? (:fail result)) (zero? (:error result)))
        (System/exit 1)))

    :else
    (throw
     (ex-info
      "Expected --list, --check, or one SH-07 shard name"
      {:id "SH07-PARALLEL-SHARD-USAGE"
       :arguments (vec arguments)
       :available (shard-names)}))))
