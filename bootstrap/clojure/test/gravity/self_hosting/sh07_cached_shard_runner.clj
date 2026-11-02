(ns gravity.self-hosting.sh07-cached-shard-runner
  "Runs B15 iteration shards with process-local immutable artifact caches.

  Cache results are acceleration only and are never authoritative evidence.
  Use sh07-authoritative-runner for the fresh-process gate."
  (:require [clojure.string :as str]
            [clojure.test :as test]
            [gravity.self-hosting.sh07-iteration-cache-runner :as iteration-cache]
            [gravity.self-hosting.unified-execution-plan :as execution-plan]))

(def ^:private cached-route-prefix
  "sh07-cached-")

(defn- cached-route-ids
  []
  (->> (keys (execution-plan/route-catalog))
       (filter #(and (string? %)
                     (str/starts-with? % cached-route-prefix)))
       sort
       vec))

(defn shard-names
  []
  (mapv #(subs % (count cached-route-prefix))
        (cached-route-ids)))

(defn- unknown-shard!
  [shard-name]
  (throw
   (ex-info "Unknown cached SH-07 shard"
            {:id "SH07-CACHED-SHARD"
             :shard shard-name
             :available (shard-names)})))

(defn- route-for-shard
  [shard-name]
  (let [route-id (when (string? shard-name)
                   (str cached-route-prefix shard-name))]
    (if (some #{route-id} (cached-route-ids))
      (execution-plan/route route-id)
      (unknown-shard! shard-name))))

(defn- resolve-tests
  [shard-name]
  (let [route (route-for-shard shard-name)
        selection (:selection route)
        test-namespace (symbol (:namespace selection))
        symbols (mapv symbol (:selectors selection))]
    (require test-namespace)
    (mapv
     (fn [symbol]
       (let [test-var (ns-resolve test-namespace symbol)]
         (when-not (and (var? test-var) (:test (meta test-var)))
           (throw
            (ex-info "Cached SH-07 shard references an absent test"
                     {:id "SH07-CACHED-SHARD-TEST"
                      :shard shard-name
                      :test symbol})))
         test-var))
     symbols)))

(def ^:private cache-counter-keys
  [:sh06-hits :sh06-misses :core-hits :core-misses
   :verification-hits :verification-misses])

(defn- selection-test-vars
  [shard-name]
  (let [{:keys [namespace selectors]} (:selection (route-for-shard shard-name))]
    (mapv #(symbol namespace %) selectors)))

(defn run-shard
  [shard-name]
  (let [tests (resolve-tests shard-name)
        counters (ref test/*initial-report-counters*)
        cached
        (iteration-cache/with-iteration-cache
         {:selection {:mode :test-vars
                      :units (selection-test-vars shard-name)}}
         #(binding [test/*report-counters* counters]
            (test/test-vars tests)))]
    (merge
     @counters
     {:artifact :gravity/sh07-cached-shard-result
      :shard shard-name
      :test-vars (count tests)
      :cache-authoritative? false
      :fresh-authoritative-run-required? true
      :cache-context (:cache-context cached)
      :elapsed-ms (:elapsed-ms cached)
      :cache (select-keys (:cache cached) cache-counter-keys)})))

(defn check-shards
  []
  (into
   (sorted-map)
   (map (fn [name] [name (count (resolve-tests name))]))
   (shard-names)))

(defn -main
  [& arguments]
  (cond
    (= ["--list"] (vec arguments))
    (doseq [name (shard-names)] (println name))

    (= ["--check"] (vec arguments))
    (println (pr-str (check-shards)))

    (= 1 (count arguments))
    (let [result (run-shard (first arguments))]
      (println (pr-str result))
      (when-not (and (zero? (:fail result))
                     (zero? (:error result)))
        (System/exit 1)))

    :else
    (throw
     (ex-info
      "Expected --list, --check, or one cached SH-07 shard name"
      {:id "SH07-CACHED-SHARD-USAGE"
       :arguments (vec arguments)
       :available (shard-names)}))))
