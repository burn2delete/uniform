(ns gravity.self-hosting.sh07-contract-shard-runner
  "Runs the lightweight SH-07-B11 contract shard.

  This shard performs no artifact construction and is safe to run alongside
  the heavyweight accepted, rejected, identity, and replay-validation lanes.

  Run with:

    clojure -Sdeps '{:paths [\"bootstrap/clojure/src\"
                            \"bootstrap/clojure/test\"]}'
      -M -m gravity.self-hosting.sh07-contract-shard-runner"
  (:require [clojure.string :as str]
            [clojure.test :as test]
            [gravity.self-hosting.unified-execution-plan :as execution-plan]))

(def ^:private contract-route-id
  "sh07-b11-contract")

(defn- manifest-selection
  []
  (let [route (execution-plan/route contract-route-id)
        selection (:selection route)]
    (when-not (= :exact-vars (:kind selection))
      (throw
       (ex-info
        "SH-07 contract route must select exact test vars"
        {:id "SH07-CONTRACT-SHARD-SELECTION"
         :route contract-route-id
         :kind (:kind selection)})))
    (when-not (and (string? (:namespace selection))
                   (boolean
                    (re-matches #"gravity\.self-hosting\.[A-Za-z0-9][A-Za-z0-9._-]*"
                                (:namespace selection)))
                   (vector? (:selectors selection))
                   (seq (:selectors selection))
                   (every? #(and (string? %)
                                 (not (str/blank? %))
                                 (nil? (namespace (symbol %))))
                           (:selectors selection)))
      (throw
       (ex-info
        "SH-07 contract route selection has an invalid namespace or selector"
        {:id "SH07-CONTRACT-SHARD-SELECTION"
         :route contract-route-id
         :selection selection})))
    {:namespace (symbol (:namespace selection))
     :test-symbols (mapv symbol (:selectors selection))}))

(defn- resolve-test
  [test-namespace test-symbol]
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
  (let [{:keys [namespace test-symbols]} (manifest-selection)
        _ (require namespace)
        test-vars (mapv #(resolve-test namespace %) test-symbols)
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
