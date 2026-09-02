(ns gravity.self-hosting.sh07-parallel-shard-runner
  "Process-isolated routing for heavyweight SH-07 test shards.

  Use `--list` to print shard names, `--check` to resolve every routed test
  without running it, or pass one shard name to run that shard."
  (:require [clojure.string :as str]
            [clojure.test :as test]
            [gravity.self-hosting.unified-execution-plan :as execution-plan]))

(def ^:private shard-route-prefix
  "sh07-parallel-")

(defn- parallel-route-ids
  []
  (->> (execution-plan/route-names)
       (filter #(str/starts-with? % shard-route-prefix))
       vec))

(defn- route-id
  [shard-name]
  (when (string? shard-name)
    (str shard-route-prefix shard-name)))

(defn shard-names
  []
  (mapv #(subs % (count shard-route-prefix))
        (parallel-route-ids)))

(defn- resolve-shard
  [shard-name]
  (let [parallel-routes (set (parallel-route-ids))
        route (when (contains? parallel-routes (route-id shard-name))
                (execution-plan/route (route-id shard-name)))]
    (when-not route
      (throw
       (ex-info
        "Unknown SH-07 parallel test shard"
        {:id "SH07-PARALLEL-SHARD"
         :shard shard-name
         :available (shard-names)})))
    (let [{:keys [namespace selectors]} (:selection route)
          namespace (symbol namespace)
          test-symbols (mapv symbol selectors)]
      (require namespace)
      [{:namespace namespace
        :test-vars
        (mapv
         (fn [test-symbol]
           (let [test-var (ns-resolve namespace test-symbol)]
             (when-not (and (var? test-var) (:test (meta test-var)))
               (throw
                (ex-info
                 "SH-07 parallel shard references an absent test"
                 {:id "SH07-PARALLEL-SHARD-TEST"
                  :shard shard-name
                  :namespace namespace
                  :test test-symbol})))
             test-var))
         test-symbols)}])))

(defn check-shards
  []
  (into
   (sorted-map)
   (map
    (fn [shard-name]
      [shard-name
       (reduce + (map #(count (:test-vars %))
                      (resolve-shard shard-name)))])
    (shard-names))))

(defn run-shard
  [shard-name]
  (let [routes (resolve-shard shard-name)
        test-vars (vec (mapcat :test-vars routes))
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
