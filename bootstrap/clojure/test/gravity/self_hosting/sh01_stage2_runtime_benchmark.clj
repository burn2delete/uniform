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

(defn- measure-nanoseconds
  [iterations operation]
  (let [started (System/nanoTime)]
    (run-iterations iterations operation)
    (- (System/nanoTime) started)))

(defn- median
  [values]
  (nth (vec (sort values)) (quot (count values) 2)))

(defn benchmark-workload
  [{:keys [warmup-iterations measurement-iterations rounds]} operation]
  (run-iterations warmup-iterations operation)
  (let [samples-ns
        (mapv (fn [_]
                (measure-nanoseconds measurement-iterations operation))
              (range rounds))
        median-ns (median samples-ns)]
    {:samples-ns samples-ns
     :median-ns median-ns
     :median-ms (/ (double median-ns) 1000000.0)
     :operations-per-second
     (if (zero? median-ns)
       nil
       (/ (* (double measurement-iterations) 1000000000.0)
          median-ns))}))

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
