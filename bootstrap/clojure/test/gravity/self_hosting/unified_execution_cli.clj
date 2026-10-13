(ns gravity.self-hosting.unified-execution-cli
  "Route-agnostic process boundary for closed execution plans."
  (:refer-clojure :exclude [run!])
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [gravity.self-hosting.execution-lanes :as lanes]
            [gravity.self-hosting.unified-execution-plan :as execution-plan])
  (:import (java.nio.charset StandardCharsets)
           (java.nio.file InvalidPathException Paths)))

(def cli-error-schema :gravity/unified-execution-cli-error-v1)
(def ^:private maximum-text-length 4096)
(def ^:private maximum-arguments 32)
(def ^:private maximum-output-bytes 262144)
(def ^:private maximum-output-depth 32)
(def ^:private maximum-output-nodes 8192)
(def ^:private maximum-output-items 4096)
(def ^:private output-node-overhead 64)
(def ^:private print-circle-var
  (ns-resolve 'clojure.core '*print-circle*))
(def ^:private value-options {"--route" :route-id "--lane" :lane
                              "--coordination-root" :coordination-root
                              "--timeout-ms" :timeout-ms
                              "--max-output-bytes" :max-output-bytes
                              "--cache-mode" :cache-mode})
(def ^:private legacy-mechanism-option "--legacy-mechanism")
(def ^:private legacy-operation-options {"--list" :list "--check" :check})
(def ^:private cache-modes #{"eligible" "disabled" "forbidden"})
(def ^:dynamic *plan-fn* execution-plan/plan)
(def ^:dynamic *executor-fn*
  "Test-only closed substrate callback; execution-plan still validates results."
  nil)
(def ^:dynamic *route-fn* execution-plan/route)
(def ^:dynamic *route-names-fn* execution-plan/route-names)
(def ^:dynamic *legacy-mechanism-projection-fn*
  "Test-only seam for the reviewed legacy mechanism projection."
  execution-plan/legacy-mechanism-projection)
(defn- fail! [id message data]
  (throw (ex-info message (assoc data :id id))))
(defn- text! [value id option]
  (when-not (and (string? value) (not (str/blank? value))
                 (<= (count value) maximum-text-length)
                 (not (str/includes? value "\u0000")))
    (fail! id "CLI option value is empty or exceeds its bound"
           {:option option :value value :maximum maximum-text-length}))
  value)
(defn- integer! [value option]
  (when-not (and (string? value) (re-matches #"[0-9]+" value))
    (fail! "UEP-CLI-INTEGER" "CLI bound must be a positive decimal integer"
           {:option option :value value}))
  (try
    (let [parsed (Long/parseLong value)]
      (when-not (pos? parsed)
        (fail! "UEP-CLI-INTEGER" "CLI bound must be positive"
               {:option option :value value}))
      parsed)
    (catch NumberFormatException _
      (fail! "UEP-CLI-INTEGER" "CLI bound exceeds the integer range"
             {:option option :value value}))))
(defn- option-value [option value]
  (case option
    "--route" (text! value "UEP-CLI-ROUTE" option)
    "--lane" (case value "fast" :fast "fresh" :fresh
               (fail! "UEP-CLI-LANE" "Lane must be fast or fresh"
                      {:option option :value value}))
    "--coordination-root"
    (let [value (text! value "UEP-CLI-ROOT" option)]
      (try
        (let [path (Paths/get value (make-array String 0))]
          (when-not (.isAbsolute path)
            (fail! "UEP-CLI-ROOT" "Coordination root must be absolute"
                   {:option option :value value}))
          value)
        (catch InvalidPathException _
          (fail! "UEP-CLI-ROOT" "Coordination root is not a valid path"
                 {:option option :value value}))))
    "--timeout-ms" (integer! value option)
    "--max-output-bytes" (integer! value option)
    "--cache-mode"
    (do
      (when-not (contains? cache-modes value)
        (fail! "UEP-CLI-CACHE-MODE" "Cache mode is not recognized"
               {:option option :value value}))
      (keyword value))))

(defn- duplicate! [options key option]
  (when (contains? options key)
    (fail! "UEP-CLI-DUPLICATE" "CLI option may be supplied only once"
           {:option option}))
  options)

(defn- legacy-mechanism-value [value]
  (let [value (text! value "UEP-CLI-LEGACY-MECHANISM" legacy-mechanism-option)]
    (when-not (re-matches #"[a-z][a-z0-9-]*" value)
      (fail! "UEP-CLI-LEGACY-MECHANISM"
             "Legacy mechanism must be a closed identifier"
             {:option legacy-mechanism-option :value value}))
    (keyword value)))

(defn- parse-legacy-args
  [arguments]
  (let [options
        (loop [remaining arguments options {}]
          (if-not (seq remaining)
            options
            (let [option (first remaining) tail (next remaining)]
              (cond
                (= option legacy-mechanism-option)
                (let [value (first tail)]
                  (when (or (nil? value) (str/starts-with? value "--"))
                    (fail! "UEP-CLI-VALUE" "CLI option requires a value"
                           {:option option}))
                  (duplicate! options :mechanism option)
                  (recur (next tail)
                         (assoc options :mechanism
                                (legacy-mechanism-value value))))

                (contains? legacy-operation-options option)
                (do
                  (when (contains? options :operation)
                    (fail! "UEP-CLI-DUPLICATE"
                           "Legacy compatibility operation may be supplied only once"
                           {:option option}))
                  (recur tail (assoc options :operation
                                      (legacy-operation-options option))))

                (str/starts-with? option "--")
                (fail! "UEP-CLI-UNKNOWN" "CLI option is not recognized"
                       {:option option})

                :else
                (fail! "UEP-CLI-POSITIONAL"
                       "Positional arguments are not accepted"
                       {:argument option})))))]
    (when-not (contains? options :mechanism)
      (fail! "UEP-CLI-LEGACY-MECHANISM"
             "Legacy compatibility mode requires a mechanism"
             {:option legacy-mechanism-option}))
    (when-not (contains? options :operation)
      (fail! "UEP-CLI-LEGACY-USAGE"
             "Legacy compatibility mode requires --list or --check"
             {:mechanism (:mechanism options)}))
    (assoc options :mode :legacy)))

(defn parse-args
  "Parse only the closed CLI surface; route semantics remain in the catalog."
  [arguments]
  (let [arguments (if (seqable? arguments)
                    (vec (take (inc maximum-arguments) arguments))
                    (fail! "UEP-CLI-ARGUMENTS" "CLI arguments are not sequenceable" {}))]
    (when-not (and (<= (count arguments) maximum-arguments)
                   (every? #(and (string? %) (<= (count %) maximum-text-length)
                                 (not (str/includes? % "\u0000"))) arguments))
      (fail! "UEP-CLI-ARGUMENTS" "CLI arguments are outside their bounds"
             {:count (count arguments) :maximum maximum-arguments}))
    (if (some #{"--help" "--list-routes"} arguments)
      (do
        (when-not (= 1 (count arguments))
          (fail! "UEP-CLI-INCOMPATIBLE"
                 "--help and --list-routes are standalone options"
                 {:arguments arguments}))
        (if (= "--help" (first arguments)) {:mode :help}
            {:mode :list-routes}))
      (if (= legacy-mechanism-option (first arguments))
        (parse-legacy-args arguments)
        (let [options
            (loop [remaining arguments options {}]
              (if-not (seq remaining)
                options
                (let [option (first remaining) tail (next remaining)]
                  (cond
                    (= option "--plan-only")
                    (do (duplicate! options :plan-only option)
                        (recur tail (assoc options :plan-only true)))
                    (contains? value-options option)
                    (let [value (first tail)]
                      (when (or (nil? value) (str/starts-with? value "--"))
                        (fail! "UEP-CLI-VALUE" "CLI option requires a value"
                               {:option option}))
                      (duplicate! options (value-options option) option)
                      (recur (next tail)
                             (assoc options (value-options option)
                                    (option-value option value))))
                    (str/starts-with? option "--")
                    (fail! "UEP-CLI-UNKNOWN" "CLI option is not recognized"
                           {:option option})
                    :else (fail! "UEP-CLI-POSITIONAL"
                                  "Positional arguments are not accepted"
                                  {:argument option})))))]
        (doseq [key [:route-id :lane]]
          (when-not (contains? options key)
            (fail! "UEP-CLI-REQUIRED" "Required CLI option is missing"
                   {:option (if (= key :route-id) "--route" "--lane")})))
        (assoc options :mode :run))))))
(defn help-value []
  {:schema :gravity/unified-execution-cli-help-v1 :status :help
   :required ["--route ID" "--lane fast|fresh"]
   :optional ["--coordination-root ABSOLUTE" "--timeout-ms N"
              "--max-output-bytes N" "--cache-mode eligible|disabled|forbidden"
              "--plan-only"]
   :standalone ["--help" "--list-routes"]})
(defn list-value []
  {:schema :gravity/unified-execution-cli-routes-v1 :status :listed
   :routes (vec (*route-names-fn*))})

(defn- legacy-projection [mechanism]
  (*legacy-mechanism-projection-fn* mechanism))

(defn- legacy-shard-route
  [mechanism shard-name]
  (let [{:keys [route-ids labels]} (legacy-projection mechanism)
        route-id (when (string? shard-name)
                   (some (fn [[route-id label]]
                           (when (= shard-name label) route-id))
                         (map vector route-ids labels)))]
    (when-not route-id
      (fail! "SH07-PARALLEL-SHARD"
             "Unknown SH-07 parallel test shard"
             {:shard shard-name :available labels}))
    route-id))

(defn resolve-legacy-shard
  "Resolve the reviewed compatibility selection without executing tests."
  [mechanism shard-name]
  (if (= :iteration-cache-runner mechanism)
    ((requiring-resolve 'gravity.self-hosting.sh07-iteration-cache-runner/resolve-route)
     shard-name)
    (let [route-id (legacy-shard-route mechanism shard-name)
        route (*route-fn* route-id)
        selection (:selection route)
        namespace-value (:namespace selection)
        selectors (:selectors selection)]
    (when-not (and (= :exact-vars (:kind selection))
                   (string? namespace-value)
                   (vector? selectors)
                   (every? string? selectors))
      (fail! "UEP-CLI-LEGACY-ROUTE"
             "Legacy compatibility route selection is not exact-vars"
             {:mechanism mechanism :route-id route-id
              :selection selection}))
    (let [namespace-symbol (symbol namespace-value)
          test-symbols (mapv symbol selectors)]
      (require namespace-symbol)
      {:route-id route-id
       :shard shard-name
       :namespace namespace-symbol
       :test-vars
       (mapv
        (fn [test-symbol]
          (let [test-var (ns-resolve namespace-symbol test-symbol)]
            (when-not (and (var? test-var) (:test (meta test-var)))
              (fail! "SH07-PARALLEL-SHARD-TEST"
                     "SH-07 parallel shard references an absent test"
                     {:shard shard-name
                      :namespace namespace-symbol
                      :test test-symbol}))
            test-var))
        test-symbols)}))))

(defn- legacy-list-text [mechanism]
  (str (str/join "\n" (:labels (legacy-projection mechanism))) "\n"))

(defn check-legacy-shards
  "Return compatibility shard counts using the shared resolver."
  [mechanism]
  (let [{:keys [labels]} (legacy-projection mechanism)]
    (into (sorted-map)
          (map (fn [shard-name]
                 [shard-name
                  (count (:test-vars
                          (resolve-legacy-shard mechanism shard-name)))])
               labels))))

(defn- legacy-check-text [mechanism]
  (str (pr-str (check-legacy-shards mechanism)) "\n"))
(defn- request-for
  [{:keys [route-id lane timeout-ms max-output-bytes cache-mode]}]
  (let [requested (cond-> {}
                    (some? timeout-ms) (assoc :timeout-ms timeout-ms)
                    (some? max-output-bytes)
                    (assoc :max-output-bytes max-output-bytes))
        route-value (when (seq requested) (*route-fn* route-id))
        resource-options (when route-value
                           (merge (select-keys (:bounds route-value)
                                               [:timeout-ms :max-output-bytes])
                                  requested))]
    (cond-> {:route-id route-id :lane lane}
      (seq resource-options)
      (assoc :resource-policy
             (merge {:resource-class (:resource-class route-value)}
                    resource-options))
      cache-mode (assoc :cache-policy {:mode cache-mode}))))

(defn- output-fail! [message]
  (fail! "UEP-CLI-OUTPUT-BOUND" message {}))
(defn- integer-size [value]
  (if (or (instance? java.math.BigInteger value)
          (instance? clojure.lang.BigInt value))
    (let [bits (if (instance? java.math.BigInteger value)
                 (.bitLength ^java.math.BigInteger value)
                 (.bitLength ^java.math.BigInteger
                             (.toBigInteger ^clojure.lang.BigInt value)))]
      (+ 4 (long (Math/ceil (* (double bits) 0.302)))))
    20))
(defn- named-size [value]
  (+ 8 (* 8 (+ (count (name value)) (count (or (namespace value) ""))))))
(defn- scalar-size [value]
  (cond
    (nil? value) 3
    (boolean? value) 5
    (integer? value) (integer-size value)
    (string? value) (+ 2 (* 8 (count value)))
    (or (keyword? value) (symbol? value)) (named-size value)
    :else (output-fail! "CLI output contains a non-EDN value")))
(defn- print-bindings []
  (cond-> {#'*print-meta* false #'*print-dup* false
           #'*print-readably* true #'*print-length* nil
           #'*print-level* nil}
    print-circle-var (assoc print-circle-var false)))
(defn- bounded-print! [writer value]
  (let [nodes (atom 0) budget (atom 0)
        add! (fn [n]
               (swap! budget + n)
               (when (> @budget maximum-output-bytes)
                 (output-fail! "CLI output exceeds its aggregate byte bound")))]
    (letfn [(visit [value depth]
              (when (> depth maximum-output-depth)
                (output-fail! "CLI output nesting exceeds its bound"))
              (swap! nodes inc)
              (when (> @nodes maximum-output-nodes)
                (output-fail! "CLI output node count exceeds its bound"))
              (add! output-node-overhead)
              (cond
                (or (nil? value) (boolean? value) (integer? value)
                    (string? value) (keyword? value) (symbol? value))
                (add! (scalar-size value))
                (and (map? value) (not (record? value)))
                (do (when (> (count value) maximum-output-items)
                      (output-fail! "CLI output map exceeds its bound"))
                    (doseq [[key item] value]
                      (visit key (inc depth)) (visit item (inc depth))))
                (vector? value)
                (do (when (> (count value) maximum-output-items)
                      (output-fail! "CLI output vector exceeds its bound"))
                    (doseq [item value] (visit item (inc depth))))
                (set? value)
                (do (when (> (count value) maximum-output-items)
                      (output-fail! "CLI output set exceeds its bound"))
                    (doseq [item value] (visit item (inc depth))))
                :else (output-fail! "CLI output contains a non-EDN value")))]
      (visit value 0))
    (with-bindings (print-bindings)
        (let [text (str (pr-str value) "\n")
              bytes (.getBytes text StandardCharsets/UTF_8)]
          (when (> (alength bytes) maximum-output-bytes)
            (output-fail! "CLI output exceeds its encoded byte bound"))
          (let [eof (Object.)
                reader (java.io.PushbackReader.
                        (java.io.StringReader. text))
                parsed (try
                         (let [first-value (edn/read {:eof eof} reader)
                               trailing (edn/read {:eof eof} reader)]
                           (and (not (identical? eof first-value))
                                (identical? eof trailing)
                                (= value first-value)))
                         (catch RuntimeException _ false)
                         (finally (.close reader)))]
            (when-not parsed
              (output-fail! "CLI output is not one strict EDN value")))
          (binding [*out* writer] (print text) (flush))))))
(defn- print-one! [value] (bounded-print! *out* value))
(defn- print-legacy-text! [text]
  (let [bytes (.getBytes ^String text StandardCharsets/UTF_8)]
    (when (> (alength bytes) maximum-output-bytes)
      (output-fail! "Legacy compatibility output exceeds its byte bound"))
    (print text)
    (flush)))
(defn- safe-error-id [value]
  (cond
    (string? value) (if (<= (count value) maximum-text-length)
                      value "UEP-CLI-ERROR")
    (or (keyword? value) (symbol? value))
    (if (and (<= (count (name value)) maximum-text-length)
             (<= (count (or (namespace value) "")) maximum-text-length))
      (str value) "UEP-CLI-ERROR")
    (and (integer? value)
         (or (instance? java.lang.Byte value) (instance? java.lang.Short value)
             (instance? java.lang.Integer value) (instance? java.lang.Long value)
             (and (instance? java.math.BigInteger value)
                  (<= (.bitLength ^java.math.BigInteger value) 63))
             (and (instance? clojure.lang.BigInt value)
                  (<= (.bitLength ^java.math.BigInteger
                                  (.toBigInteger ^clojure.lang.BigInt value)) 63))))
    (str value)
    :else "UEP-CLI-ERROR"))
(defn- fatal? [error]
  (or (instance? InterruptedException error)
      (instance? VirtualMachineError error)
      (instance? ThreadDeath error)
      (instance? LinkageError error)))
(defn- print-error! [error]
  (let [original-id (safe-error-id (:id (ex-data error)))
        value {:schema cli-error-schema :status :rejected :id original-id
               :message (or (ex-message error) "CLI request rejected")
               :data (dissoc (or (ex-data error) {}) :id)}]
    (try
      (bounded-print! *err* value)
      (catch Throwable render-error
        (if (fatal? render-error)
          (throw render-error)
          (bounded-print! *err*
                          {:schema cli-error-schema :status :rejected
                           :id "UEP-CLI-OUTPUT-BOUND"
                           :message "CLI diagnostic exceeds its byte bound"
                           :original-id original-id}))))))

(defn run!
  "Run the CLI without owning process termination; returns its exit code."
  [arguments]
  (with-bindings (print-bindings)
    (try
      (let [parsed (parse-args arguments)]
        (case (:mode parsed)
          :help (do (print-one! (help-value)) 0)
          :list-routes (do (print-one! (list-value)) 0)
          :legacy (case (:operation parsed)
                    :list (do (print-legacy-text!
                               (legacy-list-text (:mechanism parsed)))
                              0)
                    :check (do (print-legacy-text!
                                (legacy-check-text (:mechanism parsed)))
                                0))
          :run (let [request (request-for parsed) built (*plan-fn* request)]
                 (execution-plan/validate-plan built)
                 (if (:plan-only parsed)
                   (do (print-one! built) 0)
                   (let [result (binding [lanes/*coordination-root*
                                          (:coordination-root parsed)]
                                  (if *executor-fn*
                                    (execution-plan/execute-plan built *executor-fn*)
                                    (execution-plan/execute-plan built)))]
                     (print-one! result)
                     (if (= :passed (:status result)) 0 1))))))
      (catch clojure.lang.ExceptionInfo error
        (print-error! error)
        2))))

(defn -main [& arguments]
  (let [status (run! arguments)]
    (when (pos? status) (System/exit status))))
