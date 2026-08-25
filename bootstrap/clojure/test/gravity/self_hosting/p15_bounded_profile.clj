(ns gravity.self-hosting.p15-bounded-profile
  "Bounded, non-authoritative observability for one fixed P15 stage2 slice.

  This is deliberately outside the bootstrap interpreter and fresh-verification
  paths. It profiles the accepted hosted-core fixture only, and its artifact
  DAG counters observe one injected, request-scoped context."
  (:require [gravity.bootstrap :as bootstrap]))

(def ^:private compiler-source-path "bootstrap/gravity/p15_s23/compiler.gravity")
(def ^:private accepted-source-path "bootstrap/clojure/fixtures/accepted/core-app.gravity")
(def ^:private receipt-schema :gravity/p15-bounded-profile-receipt-v1)
(def ^:private nonclaims
  [:authoritative-verification :fresh-no-cache-verification :benchmark-baseline
   :performance-regression-or-improvement :stage-advancement :self-hosting
   :seed-retirement :allocation-bound :runtime-stack-bound])

(def ^:private thread-allocated-bytes-reader
  (delay
    (try
      (let [bean (java.lang.management.ManagementFactory/getThreadMXBean)
            bean-class (Class/forName "com.sun.management.ThreadMXBean")
            none (make-array Class 0)
            supported (.getMethod bean-class "isThreadAllocatedMemorySupported" none)
            enabled (.getMethod bean-class "isThreadAllocatedMemoryEnabled" none)
            bytes (.getMethod bean-class "getThreadAllocatedBytes"
                              (into-array Class [Long/TYPE]))]
        (when (and (.isInstance bean-class bean)
                   (true? (.invoke supported bean (object-array 0)))
                   (true? (.invoke enabled bean (object-array 0))))
          (fn [] (long (.invoke bytes bean
                              (object-array [(.getId (Thread/currentThread))]))))))
      (catch Exception _ nil))))

(defn- thread-allocated-bytes []
  (when-let [reader @thread-allocated-bytes-reader]
    (try (reader) (catch Exception _ nil))))

(defn- observe-phase [run]
  (let [allocated-before (thread-allocated-bytes)
        started (System/nanoTime)
        value (run)
        elapsed (- (System/nanoTime) started)
        allocated-after (thread-allocated-bytes)]
    {:value value
     :observation
     (cond-> {:elapsed-ns elapsed
              :allocation-telemetry-available?
              (boolean (and allocated-before allocated-after))}
       (and allocated-before allocated-after)
       (assoc :allocated-bytes (- allocated-after allocated-before)))}))

(defn- compiler-emitter []
  (let [source-data (bootstrap/p15-s23-compiler-source-form-record compiler-source-path)]
    (bootstrap/p15-s23-compiler-def-value
     compiler-source-path (:forms source-data) 'p15-s23-stage2-plan-emitter)))

(defn- emitted-plan []
  (bootstrap/p15-s23-stage2-plan-emitter-compile-source
   (compiler-emitter) accepted-source-path (slurp accepted-source-path)))

(defn- instruction-children [instruction]
  (case (:op instruction)
    (:vector-literal :set-literal) (:items instruction)
    :map-literal (mapcat (juxt :key :value) (:entries instruction))
    :println (:args instruction)
    :do (:body instruction)
    :if [(:test instruction) (:then instruction) (:else instruction)]
    (:let :loop) (concat (map :expr (:bindings instruction)) (:body instruction))
    (:recur :builtin-call :function-call) (:args instruction)
    []))

(defn- instruction-facts [instructions]
  (loop [pending (vec (map #(vector % 1) instructions))
         facts {:instruction-count 0 :function-call-count 0 :max-instruction-depth 0}]
    (if-let [[instruction depth] (peek pending)]
      (recur (into (pop pending)
                   (map #(vector % (inc depth))
                        (remove nil? (instruction-children instruction))))
             (-> facts
                 (update :instruction-count inc)
                 (update :function-call-count +
                         (if (= :function-call (:op instruction)) 1 0))
                 (update :max-instruction-depth max depth)))
      facts)))

(defn- function-call-graph [plan]
  (into (sorted-map)
        (map (fn [[name definition]]
               [name
                (loop [pending (vec (:instructions definition)) calls #{}]
                  (if-let [instruction (peek pending)]
                    (recur (into (pop pending)
                                 (remove nil? (instruction-children instruction)))
                           (cond-> calls
                             (= :function-call (:op instruction))
                             (conj (:function instruction))))
                    (vec (sort calls))))]))
        (:functions plan)))

(defn- max-frame-depth [call-graph entrypoint]
  (letfn [(depth [path function]
            (when (some #{function} path)
              (throw (ex-info "Bounded P15 profile cannot report finite frame depth for recursion"
                              {:id "P15-PROFILE-RECURSIVE-CALL-GRAPH"
                               :call-path (conj path function)})))
            (let [callees (get call-graph function)]
              (if (seq callees)
                (inc (apply max (map #(depth (conj path function) %) callees)))
                1)))]
    (depth [] entrypoint)))

(defn- stage2-static-facts [plan]
  (let [call-graph (function-call-graph plan)]
    (assoc (instruction-facts (mapcat :instructions (vals (:functions plan))))
           :function-count (count (:functions plan))
           :max-frame-depth (max-frame-depth call-graph (:entrypoint plan))
           :call-graph call-graph
           :plan-id (:plan-id plan))))

(defn- observed-proof-dag []
  (let [context (atom {:artifacts {} :in-flight {}})
        original bootstrap/p15-s23-context-artifact
        hits (atom 0)
        builds (atom 0)
        calls (atom 0)
        observed
        (binding [bootstrap/*p15-s23-artifact-build-context* context]
          (with-redefs [bootstrap/p15-s23-context-artifact
                        (fn [kind source-path build]
                          (let [key (bootstrap/p15-s23-artifact-context-key kind source-path)]
                            (swap! calls inc)
                            (if (contains? (:artifacts @context) key)
                              (swap! hits inc)
                              (swap! builds inc))
                            (original kind source-path build)))]
            (let [first-artifact
                  (bootstrap/p15-s23-compiler-source-form-record
                   compiler-source-path)
                  second-artifact
                  (bootstrap/p15-s23-compiler-source-form-record
                   compiler-source-path)]
              {:observed-node-kind :source-data
               :source-record-identical? (identical? first-artifact second-artifact)
               :source-text-identical?
               (= (:source-text first-artifact) (:source-text second-artifact))})))]
    (assoc observed
           :context-artifact-call-count @calls
           :context-artifact-hit-count @hits
           :context-artifact-build-count @builds
           :context-artifact-node-count (count (:artifacts @context)))))

(defn- deterministic-receipt [plan-facts proof-dag]
  (let [receipt
        {:schema receipt-schema
         :authority :non-authoritative
         :deterministic-accounting? true
         :scope :p15-stage2-accepted-hosted-core-fixture
         :inputs {:compiler-source compiler-source-path :fixture accepted-source-path}
         :stage2 plan-facts
         :proof-dag proof-dag
         :nonclaims nonclaims
         :residual-boundaries [:clojure-stage0-rule-runner
                               :clojure-instruction-runner
                               :jvm-thread-allocation-telemetry-optional]}]
    (assoc receipt :receipt-id
           (str "sha256:" (bootstrap/sha256-hex (pr-str receipt))))))

(defn run-profile
  "Return a fixed-scope P15 observation and deterministic accounting receipt.

  Timing and optional thread-allocation values are observations only. The
  receipt id excludes them, so it is stable for the same source/accounting.

  The optional functions are test seams. Production callers use the zero-arity
  form, which is the only form that emits the representative stage2 plan."
  ([] (run-profile {}))
  ([{:keys [emit-plan observe-proof-dag]
     :or {emit-plan emitted-plan
          observe-proof-dag observed-proof-dag}}]
  (let [{plan :value plan-observation :observation} (observe-phase emit-plan)
        plan-facts (stage2-static-facts plan)
        {proof-dag :value proof-dag-observation :observation}
        (observe-phase observe-proof-dag)
        receipt (deterministic-receipt plan-facts proof-dag)]
    (assoc receipt
           :observations
           {:phase-duration-ns {:stage2-plan-emission (:elapsed-ns plan-observation)
                                :proof-dag-repeat (:elapsed-ns proof-dag-observation)}
            :allocation {:stage2-plan-emission (dissoc plan-observation :elapsed-ns)
                         :proof-dag-repeat (dissoc proof-dag-observation :elapsed-ns)}}))))
