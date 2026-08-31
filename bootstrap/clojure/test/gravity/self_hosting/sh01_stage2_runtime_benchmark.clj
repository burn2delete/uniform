(ns gravity.self-hosting.sh01-stage2-runtime-benchmark
  "Repeatable, non-authoritative microbenchmarks for stage2 runtime hot paths."
  (:require [gravity.bootstrap :as bootstrap]))

(def ^:private default-options
  {:warmup-iterations 100000
   :measurement-iterations 1000000
   :rounds 5})

(def ^:private runtime {:engine :stage2-runtime-benchmark})
(def ^:private source-path "stage2-runtime-benchmark.gravity")
(def ^:private simple-plan {:source {:path source-path}})
(def ^:private compiler-plan
  {:compiler-artifact-plan? true
   :kind :gravity/stage2-compiler-artifact-plan
   :module {:profile :meta}
   :compiler {:stage :p15-s23-stage2-expression-lowering}
   :source {:path source-path}})
(def ^:private argument-instructions
  [{:op :literal :value 1} {:op :literal :value 2}])
(def ^:private body-instructions
  [{:op :literal :value 1}
   {:op :literal :value 2}
   {:op :literal :value 3}
   {:op :literal :value 4}])
(def ^:private local-environment {'value 1})
(def ^:private local-instruction {:op :local :name 'value})
(def ^:private collection-environment {'values (vec (range 16))})
(def ^:private count-instruction
  {:op :builtin-call
   :function 'count
   :args [{:op :local :name 'values}]})
(def ^:private first-instruction
  {:op :builtin-call
   :function 'first
   :args [{:op :local :name 'values}]})
(def ^:private rest-instruction
  {:op :builtin-call
   :function 'rest
   :args [{:op :local :name 'values}]})
(def ^:private predicate-environment {'record {:value 1}})
(def ^:private map-predicate-instruction
  {:op :builtin-call
   :function 'map?
   :args [{:op :local :name 'record}]})
(def ^:private lookup-environment {'record {:value 1}})
(def ^:private get-instruction
  {:op :builtin-call
   :function 'get
   :args [{:op :local :name 'record}
          {:op :literal :value :value}]})
(def ^:private equality-environment
  {'left [1 {:value :same}]
   'right [1 {:value :same}]})
(def ^:private equality-instruction
  {:op :builtin-call
   :function '=
   :args [{:op :local :name 'left}
          {:op :local :name 'right}]})
(def ^:private binary-add-instruction
  {:op :builtin-call
   :function '+
   :args [{:op :literal :value 1}
          {:op :literal :value 2}]})
(def ^:private ternary-add-instruction
  {:op :builtin-call
   :function '+
   :args [{:op :literal :value 1}
          {:op :literal :value 2}
          {:op :literal :value 3}]})
(def ^:private four-argument-add-instruction
  {:op :builtin-call
   :function '+
   :args [{:op :literal :value 1}
          {:op :literal :value 2}
          {:op :literal :value 3}
          {:op :literal :value 4}]})
(def ^:private assoc-environment {'record {:existing 1}})
(def ^:private assoc-instruction
  {:op :builtin-call
   :function 'assoc
   :args [{:op :local :name 'record}
          {:op :literal :value :value}
          {:op :literal :value 2}]})
(def ^:private map-literal-instruction
  {:op :map-literal
   :entries
   (mapv (fn [index]
           {:key {:op :literal :value (keyword (str "key-" index))}
            :value {:op :literal :value index}})
         (range 32))})
(def ^:private function-plan
  {:source {:path source-path}
   :functions
   {'identity-second
    {:params ['first-value 'second-value]
     :instructions [{:op :local :name 'second-value}]}}})
(def ^:private ordinary-map {:value 1})

(defn workloads
  []
  (sorted-map
   :builtin-first
   #(bootstrap/p15-s23-stage2-runtime-invoke-builtin
     simple-plan 'first [[1 2]])
   :builtin-greater-than
   #(bootstrap/p15-s23-stage2-runtime-invoke-builtin
     simple-plan '> [2 1])
   :builtin-plus
   #(bootstrap/p15-s23-stage2-runtime-invoke-builtin
     simple-plan '+ [1 2])
   :execute-instructions-four
   #(bootstrap/p15-s23-stage2-runtime-execute-instructions
     runtime simple-plan {} body-instructions)
   :execute-values-two
   #(bootstrap/p15-s23-stage2-runtime-execute-values
     runtime simple-plan {} argument-instructions :benchmark)
   :interpreted-count
   #(bootstrap/p15-s23-stage2-runtime-execute-instruction
     runtime simple-plan collection-environment count-instruction)
   :interpreted-binary-add
   #(bootstrap/p15-s23-stage2-runtime-execute-instruction
     runtime simple-plan {} binary-add-instruction)
   :interpreted-ternary-add
   #(bootstrap/p15-s23-stage2-runtime-execute-instruction
     runtime simple-plan {} ternary-add-instruction)
   :interpreted-four-argument-add
   #(bootstrap/p15-s23-stage2-runtime-execute-instruction
     runtime simple-plan {} four-argument-add-instruction)
   :interpreted-assoc-three
   #(bootstrap/p15-s23-stage2-runtime-execute-instruction
     runtime simple-plan assoc-environment assoc-instruction)
   :interpreted-equality
   #(bootstrap/p15-s23-stage2-runtime-execute-instruction
     runtime simple-plan equality-environment equality-instruction)
   :interpreted-first
   #(bootstrap/p15-s23-stage2-runtime-execute-instruction
     runtime simple-plan collection-environment first-instruction)
   :interpreted-get
   #(bootstrap/p15-s23-stage2-runtime-execute-instruction
     runtime simple-plan lookup-environment get-instruction)
   :interpreted-map-predicate
   #(bootstrap/p15-s23-stage2-runtime-execute-instruction
     runtime compiler-plan predicate-environment map-predicate-instruction)
   :interpreted-map-literal
   #(bootstrap/p15-s23-stage2-runtime-execute-instruction
     runtime simple-plan {} map-literal-instruction)
   :interpreted-rest
   #(bootstrap/p15-s23-stage2-runtime-execute-instruction
     runtime simple-plan collection-environment rest-instruction)
   :legacy-carrier-count
   #(bootstrap/p15-s23-stage2-runtime-invoke-builtin
     simple-plan 'count
     (bootstrap/p15-s23-stage2-runtime-execute-values
      runtime simple-plan collection-environment
      (:args count-instruction) :recur-inside-builtin-argument))
   :legacy-carrier-assoc
   #(bootstrap/p15-s23-stage2-runtime-invoke-builtin
     simple-plan 'assoc
     (bootstrap/p15-s23-stage2-runtime-execute-values
      runtime simple-plan assoc-environment
      (:args assoc-instruction) :recur-inside-builtin-argument))
   :legacy-carrier-equality
   #(bootstrap/p15-s23-stage2-runtime-invoke-builtin
     simple-plan '=
     (bootstrap/p15-s23-stage2-runtime-execute-values
      runtime simple-plan equality-environment
      (:args equality-instruction) :recur-inside-builtin-argument))
   :legacy-carrier-first
   #(bootstrap/p15-s23-stage2-runtime-invoke-builtin
     simple-plan 'first
     (bootstrap/p15-s23-stage2-runtime-execute-values
      runtime simple-plan collection-environment
      (:args first-instruction) :recur-inside-builtin-argument))
   :legacy-carrier-map-predicate
   #(bootstrap/p15-s23-stage2-runtime-invoke-builtin
     compiler-plan 'map?
     (bootstrap/p15-s23-stage2-runtime-execute-values
      runtime compiler-plan predicate-environment
      (:args map-predicate-instruction) :recur-inside-builtin-argument))
   :legacy-carrier-rest
   #(bootstrap/p15-s23-stage2-runtime-invoke-builtin
     simple-plan 'rest
     (bootstrap/p15-s23-stage2-runtime-execute-values
      runtime simple-plan collection-environment
      (:args rest-instruction) :recur-inside-builtin-argument))
   :function-bind-two
   #(bootstrap/p15-s23-stage2-runtime-execute-function
     runtime function-plan 'identity-second [1 2])
   :local-instruction
   #(bootstrap/p15-s23-stage2-runtime-execute-instruction
     runtime simple-plan local-environment local-instruction)
   :ordinary-map-recur-check
   #(bootstrap/p15-s23-stage2-runtime-recur-signal? ordinary-map)))

(defn- run-iterations
  [iterations operation]
  (dotimes [_ iterations]
    (operation)))

(def ^:private thread-allocated-bytes-reader
  (delay
    (try
      (let [bean (java.lang.management.ManagementFactory/getThreadMXBean)
            bean-class (Class/forName "com.sun.management.ThreadMXBean")
            no-parameters (make-array Class 0)
            supported-method
            (.getMethod bean-class "isThreadAllocatedMemorySupported"
                        no-parameters)
            enabled-method
            (.getMethod bean-class "isThreadAllocatedMemoryEnabled"
                        no-parameters)
            bytes-method
            (.getMethod bean-class "getThreadAllocatedBytes"
                        (into-array Class [Long/TYPE]))]
        (when (and (.isInstance bean-class bean)
                   (true? (.invoke supported-method bean (object-array 0)))
                   (true? (.invoke enabled-method bean (object-array 0))))
          (fn []
            (long
             (.invoke bytes-method bean
                      (object-array [(.getId (Thread/currentThread))]))))))
      (catch Exception _
        nil))))

(defn- thread-allocated-bytes
  []
  (when-let [reader @thread-allocated-bytes-reader]
    (try
      (reader)
      (catch Exception _
        nil))))

(defn- measure-round
  [iterations operation]
  (let [allocated-before (thread-allocated-bytes)
        started (System/nanoTime)]
    (run-iterations iterations operation)
    (let [elapsed (- (System/nanoTime) started)
          allocated-after (thread-allocated-bytes)]
      {:nanoseconds elapsed
       :allocated-bytes
       (when (and allocated-before allocated-after)
         (- allocated-after allocated-before))})))

(defn- median
  [values]
  (nth (vec (sort values)) (quot (count values) 2)))

(defn benchmark-workload
  [{:keys [warmup-iterations measurement-iterations rounds]} operation]
  (run-iterations warmup-iterations operation)
  (let [samples (mapv (fn [_] (measure-round measurement-iterations
                                             operation))
                      (range rounds))
        samples-ns (mapv :nanoseconds samples)
        allocation-samples (mapv :allocated-bytes samples)
        allocation-available? (every? some? allocation-samples)
        median-ns (median samples-ns)]
    (cond->
     {:samples-ns samples-ns
      :median-ns median-ns
      :median-ms (/ (double median-ns) 1000000.0)
      :operations-per-second
      (if (zero? median-ns)
        nil
        (/ (* (double measurement-iterations) 1000000000.0)
           median-ns))
      :allocation-telemetry-available? allocation-available?}
      allocation-available?
      (assoc
       :samples-allocated-bytes allocation-samples
       :median-allocated-bytes (median allocation-samples)
       :median-allocated-bytes-per-operation
       (/ (double (median allocation-samples))
          (double measurement-iterations))))))

(defn run-benchmark
  [options]
  (let [options (merge default-options options)
        available-workloads (workloads)
        requested-workload (:workload options)
        selected-workloads
        (if requested-workload
          (if-let [operation (get available-workloads requested-workload)]
            (sorted-map requested-workload operation)
            (throw
             (ex-info "Unknown stage2 benchmark workload"
                      {:id "SH01-STAGE2-BENCHMARK-WORKLOAD"
                       :workload requested-workload
                       :available (vec (keys available-workloads))})))
          available-workloads)]
    (when-not (every? (fn [key]
                        (let [value (get options key)]
                          (and (integer? value) (pos? value))))
                      [:warmup-iterations
                       :measurement-iterations
                       :rounds])
      (throw
       (ex-info "Stage2 benchmark counts must be positive integers"
                {:id "SH01-STAGE2-BENCHMARK-COUNT"
                 :options options})))
    {:artifact :gravity/sh01-stage2-runtime-benchmark
     :authority :non-authoritative
     :authoritative? false
     :purpose :performance-regression-feedback
     :java-runtime-version (System/getProperty "java.runtime.version")
     :clojure-version (clojure-version)
     :options options
     :results
     (into
      (sorted-map)
      (map (fn [[name operation]]
             [name (benchmark-workload options operation)]))
      selected-workloads)}))

(defn- parse-positive-long
  [option value]
  (try
    (let [parsed (Long/parseLong value)]
      (when-not (pos? parsed)
        (throw (NumberFormatException.)))
      parsed)
    (catch NumberFormatException _
      (throw
       (ex-info "Stage2 benchmark option requires a positive integer"
                {:id "SH01-STAGE2-BENCHMARK-ARGUMENT"
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
           (ex-info "Stage2 benchmark option requires a value"
                    {:id "SH01-STAGE2-BENCHMARK-USAGE"
                     :option option})))
        (let [key
              (case option
                "--warmup" :warmup-iterations
                "--iterations" :measurement-iterations
                "--rounds" :rounds
                "--workload" :workload
                (throw
                 (ex-info "Unsupported stage2 benchmark option"
                          {:id "SH01-STAGE2-BENCHMARK-USAGE"
                           :option option
                           :supported
                           ["--warmup" "--iterations" "--rounds"
                            "--workload"]})))]
          (recur (vec tail)
                 (assoc options key
                        (if (= key :workload)
                          (keyword value)
                          (parse-positive-long option value)))))))))

(defn -main
  [& arguments]
  (println (pr-str (run-benchmark (parse-arguments arguments)))))
