(ns gravity.self-hosting.sh01-stage2-plan-emitter-benchmark
  "Bounded, non-authoritative benchmark for one fresh stage2 plan emission."
  (:require [clojure.string :as str]
            [gravity.bootstrap :as bootstrap]))

(def ^:private accepted-source-path
  "bootstrap/clojure/fixtures/accepted/core-app.gravity")

(def ^:private default-options
  {:iterations 1})

(def ^:private maximum-iterations 3)

(def ^:private observed-source-kinds
  [:authenticated-envelope :syntax :plan-emitter :other])

(def ^:private observed-phases
  [:macro-parse-expand :function-table :function-lowering
   :instruction-summary :canonicalization :hashing])

(def ^:dynamic ^:private *source-kind* nil)
(def ^:dynamic ^:private *phase* nil)

;; `with-redefs` changes root Vars.  This private lock keeps two explicit
;; profiler invocations from overlapping those temporary replacements in one
;; JVM; normal verification never enters this benchmark namespace.
(def ^:private benchmark-lock (Object.))

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

(defn- empty-phase-observations
  []
  (into (sorted-map)
        (map (fn [source-kind]
               [source-kind
                (into (sorted-map)
                      (map (fn [phase]
                             [phase {:call-count 0
                                     :elapsed-ns 0
                                     :allocation-telemetry-available? false}])
                           observed-phases))]))
        observed-source-kinds))

(defn- record-phase-observation!
  [observations source-kind phase elapsed allocated-before allocated-after]
  (when (and observations
             (contains? (set observed-source-kinds) source-kind)
             (contains? (set observed-phases) phase))
    (let [allocation-available? (and (some? allocated-before)
                                     (some? allocated-after))]
      (swap! observations update-in [source-kind phase]
             (fn [current]
               (cond-> (-> current
                           (update :call-count inc)
                           (update :elapsed-ns + elapsed)
                           (update :allocation-telemetry-available?
                                   #(or % allocation-available?)))
                 allocation-available?
                 (update :allocated-bytes (fnil + 0)
                         (- allocated-after allocated-before))))))))

(defn- observe-phase
  [observations phase operation]
  (if (or (nil? *source-kind*) *phase*)
    (operation)
    (let [allocated-before (thread-allocated-bytes)
          started (System/nanoTime)]
      (binding [*phase* phase]
        (try
          (operation)
          (finally
            (let [elapsed (- (System/nanoTime) started)
                  allocated-after (thread-allocated-bytes)]
              (record-phase-observation! observations *source-kind* phase elapsed
                                         allocated-before allocated-after))))))))

(defn- phase-counts
  [observations]
  (into (sorted-map)
        (map (fn [source-kind]
               [source-kind
                (into (sorted-map)
                      (map (fn [phase]
                             [phase (get-in @observations
                                             [source-kind phase :call-count])])
                           observed-phases))]))
        observed-source-kinds))

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
  (locking benchmark-lock
   (let [source-text (slurp accepted-source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          accepted-source-path :jvm))
        original bootstrap/p15-s23-stage2-compiler-artifact-plan
        compiler-plan-calls (atom {})
        phase-observations (atom (empty-phase-observations))
        allocated-before (thread-allocated-bytes)
        started (System/nanoTime)
        plan
        (with-redefs
         [bootstrap/p15-s23-stage2-compiler-artifact-plan
          (fn [active-emitter source-path active-source-text]
            (let [source-kind (compiler-source-kind source-path)]
              (swap! compiler-plan-calls update source-kind (fnil inc 0))
              (binding [*source-kind* source-kind]
                (original active-emitter source-path active-source-text))))
          bootstrap/p15-s23-stage2-compiler-artifact-binding!
          (let [original bootstrap/p15-s23-stage2-compiler-artifact-binding!]
            (fn [& arguments]
              ;; The binding owns the semantic and artifact hashes after its
              ;; plan build.  Keep those observations with the emitter source,
              ;; not with the requested application that triggered the lookup.
              (binding [*source-kind* :plan-emitter]
                (apply original arguments))))
          bootstrap/macro-source-artifact
          (let [original bootstrap/macro-source-artifact]
            (fn [& arguments]
              (observe-phase phase-observations :macro-parse-expand
                             #(apply original arguments))))
          bootstrap/stage0-function-table
          (let [original bootstrap/stage0-function-table]
            (fn [& arguments]
              (observe-phase phase-observations :function-table
                             #(apply original arguments))))
          bootstrap/p15-s23-stage2-seed-compile-function
          (let [original bootstrap/p15-s23-stage2-seed-compile-function]
            (fn [& arguments]
              (observe-phase phase-observations :function-lowering
                             #(apply original arguments))))
          bootstrap/stage0-instruction-summary
          (let [original bootstrap/stage0-instruction-summary]
            (fn [& arguments]
              (observe-phase phase-observations :instruction-summary
                             #(apply original arguments))))
          bootstrap/c-backend-canonical-value
          (let [original bootstrap/c-backend-canonical-value]
            (fn [& arguments]
              (observe-phase phase-observations :canonicalization
                             #(apply original arguments))))
          bootstrap/sha256-hex
          (let [original bootstrap/sha256-hex]
            (fn [& arguments]
              (observe-phase phase-observations :hashing
                             #(apply original arguments))))]
          (binding [*source-kind* (compiler-source-kind accepted-source-path)]
            (bootstrap/p15-s23-stage2-plan-emitter-compile-source
             emitter accepted-source-path source-text)))
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
      :phase-call-counts (phase-counts phase-observations)
      :phase-observations @phase-observations
      :semantic-receipt (plan-semantic-receipt plan execution)
      :allocation-telemetry-available?
      (and (some? allocated-before) (some? allocated-after))}
      (and (some? allocated-before) (some? allocated-after))
      (assoc :allocated-bytes (- allocated-after allocated-before))))))

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
       :deterministic-accounting
       [:compiler-artifact-plan-calls :phase-call-counts :semantic-receipt]
       :host-variable-observations
       [:elapsed-ns :elapsed-ms :allocated-bytes
        :allocation-telemetry-available? :java-runtime-version :clojure-version]
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
