(ns gravity.self-hosting.sh01-stage2-plan-emitter-benchmark
  "Bounded, non-authoritative benchmark for one fresh stage2 plan emission."
  (:require [clojure.string :as str]
            [gravity.bootstrap :as bootstrap]))

(def ^:private accepted-source-path
  "bootstrap/clojure/fixtures/accepted/core-app.gravity")

(def ^:private default-options
  {:iterations 1})

(def ^:private maximum-iterations 3)

(def ^:private thread-allocated-bytes-reader
  (delay
    (try
      (let [bean (java.lang.management.ManagementFactory/getThreadMXBean)]
        (when (and (instance? com.sun.management.ThreadMXBean bean)
                   (.isThreadAllocatedMemorySupported
                    ^com.sun.management.ThreadMXBean bean)
                   (.isThreadAllocatedMemoryEnabled
                    ^com.sun.management.ThreadMXBean bean))
          (fn []
            (.getThreadAllocatedBytes
             ^com.sun.management.ThreadMXBean bean
             (.getId (Thread/currentThread))))))
      (catch Exception _
        nil))))

(defn- thread-allocated-bytes
  []
  (when-let [reader @thread-allocated-bytes-reader]
    (reader)))

(defn- compiler-source-kind
  [source-path]
  (let [path (str/replace source-path "\\" "/")]
    (cond
      (str/ends-with?
       path "/gravity/compiler/authenticated_envelope.gravity")
      :authenticated-envelope

      (str/ends-with? path "/gravity/bootstrap/syntax.gravity")
      :syntax

      (str/ends-with? path "/gravity/p15_s23/emitter.gravity")
      :plan-emitter

      :else :other)))

(defn- plan-semantic-receipt
  [plan execution]
  (let [path-neutral-plan
        (-> plan
            (update :source dissoc :path)
            (update :module dissoc :source-path))]
    {:plan-id (:plan-id plan)
     :plan-value-digest
     (str "sha256:"
          (bootstrap/sha256-hex
           (pr-str (bootstrap/c-backend-canonical-value path-neutral-plan))))
     :instruction-summary (:instruction-summary plan)
     :diagnostics (:diagnostics plan)
     :function-order (vec (keys (:functions plan)))
     :execution execution}))

(defn- compile-sample
  []
  (let [source-text (slurp accepted-source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          accepted-source-path :jvm))
        original bootstrap/p15-s23-stage2-compiler-artifact-plan
        compiler-plan-calls (atom {})
        allocated-before (thread-allocated-bytes)
        started (System/nanoTime)
        plan
        (with-redefs
         [bootstrap/p15-s23-stage2-compiler-artifact-plan
          (fn [active-emitter source-path active-source-text]
            (swap! compiler-plan-calls
                   update (compiler-source-kind source-path) (fnil inc 0))
            (original active-emitter source-path active-source-text))]
          (bootstrap/p15-s23-stage2-plan-emitter-compile-source
           emitter accepted-source-path source-text))
        elapsed (- (System/nanoTime) started)
        allocated-after (thread-allocated-bytes)
        execution (bootstrap/execute-stage0-compiled-plan plan)]
    (cond->
     {:elapsed-ns elapsed
      :elapsed-ms (/ (double elapsed) 1000000.0)
      :compiler-artifact-plan-call-count
      (reduce + 0 (vals @compiler-plan-calls))
      :compiler-artifact-plan-calls
      (into (sorted-map) @compiler-plan-calls)
      :semantic-receipt (plan-semantic-receipt plan execution)
      :allocation-telemetry-available?
      (and (some? allocated-before) (some? allocated-after))}
      (and (some? allocated-before) (some? allocated-after))
      (assoc :allocated-bytes (- allocated-after allocated-before)))))

(defn run-benchmark
  [options]
  (let [{:keys [iterations] :as options}
        (merge default-options options)]
    (when-not (and (integer? iterations)
                   (<= 1 iterations maximum-iterations))
      (throw
       (ex-info "Stage2 plan emitter benchmark iterations are out of bounds"
                {:id "SH01-STAGE2-PLAN-EMITTER-BENCHMARK-COUNT"
                 :iterations iterations
                 :maximum-iterations maximum-iterations})))
    (let [samples (mapv (fn [_] (compile-sample)) (range iterations))
          receipts (mapv :semantic-receipt samples)]
      (when-not (apply = receipts)
        (throw
         (ex-info "Stage2 plan emitter benchmark changed semantic output"
                  {:id "SH01-STAGE2-PLAN-EMITTER-BENCHMARK-EQUIVALENCE"
                   :receipts receipts})))
      {:artifact :gravity/sh01-stage2-plan-emitter-benchmark
       :authority :non-authoritative
       :authoritative? false
       :purpose :bounded-performance-regression-feedback
       :fresh-plan-emission-per-iteration? true
       :persistent-cache-authority? false
       :java-runtime-version (System/getProperty "java.runtime.version")
       :clojure-version (clojure-version)
       :options options
       :semantic-receipt (first receipts)
       :samples (mapv #(dissoc % :semantic-receipt) samples)})))

(defn- parse-positive-long
  [option value]
  (try
    (let [parsed (Long/parseLong value)]
      (when-not (pos? parsed)
        (throw (NumberFormatException.)))
      parsed)
    (catch NumberFormatException _
      (throw
       (ex-info "Stage2 plan emitter benchmark option requires a positive integer"
                {:id "SH01-STAGE2-PLAN-EMITTER-BENCHMARK-ARGUMENT"
                 :option option
                 :value value})))))

(defn parse-arguments
  [arguments]
  (loop [remaining (vec arguments)
         options {}]
    (if (empty? remaining)
      options
      (let [[option value & tail] remaining]
        (when (nil? value)
          (throw
           (ex-info "Stage2 plan emitter benchmark option requires a value"
                    {:id "SH01-STAGE2-PLAN-EMITTER-BENCHMARK-USAGE"
                     :option option})))
        (case option
          "--iterations"
          (recur (vec tail)
                 (assoc options :iterations
                        (parse-positive-long option value)))
          (throw
           (ex-info "Unsupported stage2 plan emitter benchmark option"
                    {:id "SH01-STAGE2-PLAN-EMITTER-BENCHMARK-USAGE"
                     :option option
                     :supported ["--iterations"]})))))))

(defn -main
  [& arguments]
  (println (pr-str (run-benchmark (parse-arguments arguments)))))
