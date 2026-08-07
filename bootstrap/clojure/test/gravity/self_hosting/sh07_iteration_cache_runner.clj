(ns gravity.self-hosting.sh07-iteration-cache-runner
  "Runs focused SH-07 namespaces with bounded process-local caches.

  This runner is iteration acceleration only. Its result is always
  non-authoritative, and a fresh authoritative run remains required."
  (:require [clojure.test :as test]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting-test-runner :as test-runner]))

(def ^:private default-maximum-entries 4)

(def ^:private empty-cache-counters
  {:sh06-hits 0 :sh06-misses 0
   :core-hits 0 :core-misses 0
   :verification-hits 0 :verification-misses 0})

(def ^:dynamic ^:private *iteration-cache-counters* nil)

(deftype ^:private IdentityKey [value]
  Object
  (equals [_ other]
    (and (instance? IdentityKey other)
         (identical? value (.-value ^IdentityKey other))))
  (hashCode [_]
    (System/identityHashCode value)))

(defn- identity-key
  [value]
  (IdentityKey. value))

(defn- sha256
  [value]
  (let [digest (java.security.MessageDigest/getInstance "SHA-256")]
    (.update digest
             (.getBytes (str value)
                        java.nio.charset.StandardCharsets/UTF_8))
    (apply str
           (map #(format "%02x" (bit-and % 0xff))
                (.digest digest)))))

(defn- cache-context
  []
  {:request-schema-version 15
   :adapter bootstrap/sh07-core-adapter-contract
   :checked-core-source-content-hash
   bootstrap/sh07-core-expected-source-content-hash
   :checked-core-plan-semantic-hash
   bootstrap/sh07-core-expected-plan-semantic-hash
   :java-runtime-version (System/getProperty "java.runtime.version")
   :clojure-version (clojure-version)})

(defn- resolution-key
  [context artifact]
  [context
   (identity-key artifact)
   (:artifact-id artifact)
   (get-in artifact [:provenance :source-path])
   (get-in artifact
           [:gravity-resolution-boundary :resolved-analysis
            :semantic-projection-id])])

(defn- bounded-cache
  [maximum-entries counters hit-key miss-key]
  (let [state (atom {:order [] :values {}})
        monitor (Object.)]
    (fn [key operation]
      ;; Computation stays under the monitor intentionally: two test futures
      ;; must not duplicate a multi-gigabyte core build for the same key.
      (locking monitor
        (if-let [entry (find (:values @state) key)]
          (do
            (swap! counters update hit-key inc)
            (val entry))
          (let [value (operation)
                {:keys [order values]} @state
                order' (conj order key)
                values' (assoc values key value)
                overflow (- (count order') maximum-entries)
                evicted (if (pos? overflow)
                          (subvec order' 0 overflow)
                          [])
                retained-order (if (pos? overflow)
                                 (subvec order' overflow)
                                 order')]
            (swap! counters update miss-key inc)
            (reset! state
                    {:order retained-order
                     :values (apply dissoc values' evicted)})
            value))))))

(defn with-iteration-cache
  "Runs operation with bounded SH-06/SH-07 caches and returns value + metadata.

  Cache keys bind source content, the checked-core plan, adapter contract, and
  runtime. Results are deliberately non-authoritative."
  [{:keys [maximum-entries]
    :or {maximum-entries default-maximum-entries}}
   operation]
  (when-not (and (integer? maximum-entries) (pos? maximum-entries))
    (throw
     (ex-info "Iteration cache maximum must be a positive integer"
              {:id "SH07-ITERATION-CACHE-MAXIMUM"
               :maximum-entries maximum-entries})))
  (let [context (cache-context)
        original-sh06 bootstrap/sh06-resolution-source-artifact
        original-core bootstrap/sh07-core-from-resolution-artifact
        original-verification bootstrap/sh07-core-artifact-verification
        counters (atom empty-cache-counters)
        sh06-cache (bounded-cache maximum-entries counters
                                  :sh06-hits :sh06-misses)
        core-cache (bounded-cache maximum-entries counters
                                  :core-hits :core-misses)
        verification-cache
        (bounded-cache maximum-entries counters
                       :verification-hits :verification-misses)
        cached-sh06
        (fn [path source]
          (sh06-cache [context path (sha256 source)]
                      #(original-sh06 path source)))
        cached-core
        (fn [resolution]
          (core-cache (resolution-key context resolution)
                      #(original-core resolution)))
        cached-verification
        (fn [artifact]
          (verification-cache
           [context (identity-key artifact) (:artifact-id artifact)]
           #(original-verification artifact)))
        started (System/nanoTime)
        value
        (binding [*iteration-cache-counters* counters]
          (with-redefs [bootstrap/sh06-resolution-source-artifact cached-sh06
                        bootstrap/sh07-core-from-resolution-artifact cached-core
                        bootstrap/sh07-core-artifact-verification
                        cached-verification]
            (operation)))]
    {:value value
     :cache @counters
     :cache-context context
     :maximum-entries maximum-entries
     :elapsed-ms (long (/ (- (System/nanoTime) started) 1000000))
     :authority :non-authoritative
     :authoritative? false
     :cache-authoritative? false
     :fresh-authoritative-run-required? true}))

(defn- parse-positive-integer
  [option value]
  (try
    (let [parsed (Long/parseLong value)]
      (when-not (pos? parsed)
        (throw (NumberFormatException.)))
      parsed)
    (catch NumberFormatException _
      (throw
       (ex-info "Expected a positive integer"
                {:id "SH07-ITERATION-CACHE-ARGUMENT"
                 :option option :value value})))))

(defn parse-arguments
  [arguments]
  (loop [remaining (vec arguments)
         result {:namespaces []
                 :maximum-entries default-maximum-entries
                 :fail-fast? false
                 :test-var nil}]
    (if (empty? remaining)
      (do
        (when (and (empty? (:namespaces result))
                   (nil? (:test-var result)))
          (throw
           (ex-info "At least one --namespace or one --test-var is required"
                    {:id "SH07-ITERATION-CACHE-SELECTION"})))
        (when (and (seq (:namespaces result)) (:test-var result))
          (throw
           (ex-info "Namespace and test-var selections cannot be combined"
                    {:id "SH07-ITERATION-CACHE-SELECTION-CONFLICT"
                     :namespaces (:namespaces result)
                     :test-var (:test-var result)})))
        (when (and (:test-var result) (:fail-fast? result))
          (throw
           (ex-info "--fail-fast applies only to multi-namespace selection"
                    {:id "SH07-ITERATION-CACHE-FAIL-FAST-SELECTION"
                     :test-var (:test-var result)})))
        (when-not (= (count (:namespaces result))
                     (count (distinct (:namespaces result))))
          (throw
           (ex-info "Iteration cache namespaces must be unique"
                    {:id "SH07-ITERATION-CACHE-DUPLICATE-NAMESPACE"
                     :namespaces (:namespaces result)})))
        result)
      (let [option (first remaining)]
        (case option
          "--fail-fast"
          (recur (subvec remaining 1)
                 (assoc result :fail-fast? true))

          "--namespace"
          (let [value (second remaining)]
            (when (nil? value)
              (throw
               (ex-info "Iteration cache option requires a value"
                        {:id "SH07-ITERATION-CACHE-USAGE"
                         :option option})))
            (let [namespace (symbol value)]
              ;; Delegate ownership validation to the coordinator runner.
              (test-runner/select-tests ["--namespace" value])
              (recur (subvec remaining 2)
                     (update result :namespaces conj namespace))))

          "--max-cache-entries"
          (let [value (second remaining)]
            (when (nil? value)
              (throw
               (ex-info "Iteration cache option requires a value"
                        {:id "SH07-ITERATION-CACHE-USAGE"
                         :option option})))
            (recur (subvec remaining 2)
                   (assoc result :maximum-entries
                          (parse-positive-integer option value))))

          "--test-var"
          (let [value (second remaining)]
            (when (nil? value)
              (throw
               (ex-info "Iteration cache option requires a value"
                        {:id "SH07-ITERATION-CACHE-USAGE"
                         :option option})))
            (let [test-var (symbol value)
                  namespace-name (namespace test-var)]
              (when-not namespace-name
                (throw
                 (ex-info "Test-var selection must be namespace-qualified"
                          {:id "SH07-ITERATION-CACHE-TEST-VAR"
                           :test-var test-var})))
              (when (:test-var result)
                (throw
                 (ex-info "Only one --test-var selection is supported"
                          {:id "SH07-ITERATION-CACHE-DUPLICATE-TEST-VAR"
                           :test-vars [(:test-var result) test-var]})))
              ;; Ownership remains anchored in the discovered test catalog.
              (test-runner/select-tests ["--namespace" namespace-name])
              (recur (subvec remaining 2)
                     (assoc result :test-var test-var))))

          (throw
           (ex-info "Unsupported iteration cache option"
                    {:id "SH07-ITERATION-CACHE-USAGE"
                     :option option
                     :supported ["--namespace" "--test-var"
                                 "--max-cache-entries" "--fail-fast"]})))))))

(defn- cache-snapshot
  []
  (if *iteration-cache-counters*
    @*iteration-cache-counters*
    empty-cache-counters))

(defn- cache-delta
  [before after]
  (merge-with - after before))

(defn- namespace-symbol
  [namespace]
  (if (instance? clojure.lang.Namespace namespace)
    (ns-name namespace)
    namespace))

(defn- run-tests-with-telemetry
  [namespaces fail-fast?]
  (let [namespace-results (atom [])]
    (letfn [(run-namespace [namespace]
              (let [started (System/nanoTime)
                    before (cache-snapshot)]
                (try
                  (test/test-ns namespace)
                  (finally
                    (let [result
                          {:namespace (namespace-symbol namespace)
                           :elapsed-ms
                           (long (/ (- (System/nanoTime) started) 1000000))
                           :cache (cache-delta before (cache-snapshot))
                           :authority :non-authoritative}]
                      (swap! namespace-results conj result)
                      (println
                       (pr-str
                        (assoc result
                               :artifact
                               :gravity/sh07-iteration-namespace-result)))
                      (flush))))))
            (finish [summaries skipped]
              (let [test-result
                    (assoc (apply merge-with + summaries) :type :summary)]
                (test/do-report test-result)
                {:test-result test-result
                 :namespace-results @namespace-results
                 :stopped-early? (boolean (seq skipped))
                 :skipped-namespaces (vec skipped)}))]
      (loop [remaining (vec namespaces)
             summaries []]
        (let [namespace (first remaining)
              summary (run-namespace namespace)
              summaries' (conj summaries summary)
              tail (subvec remaining 1)
              failed? (or (pos? (:fail summary))
                          (pos? (:error summary)))]
          (if (or (empty? tail) (and fail-fast? failed?))
            (finish summaries' (if (and fail-fast? failed?) tail []))
            (recur tail summaries')))))))

(defn run-namespaces
  [{:keys [namespaces maximum-entries fail-fast?]}]
  (doseq [namespace namespaces]
    (require namespace))
  (let [cached
        (with-iteration-cache
          {:maximum-entries maximum-entries}
          #(run-tests-with-telemetry namespaces fail-fast?))
        {:keys [test-result namespace-results stopped-early?
                skipped-namespaces]} (:value cached)]
    (-> cached
        (dissoc :value)
        (assoc :artifact :gravity/sh07-iteration-cache-run
               :namespaces namespaces
               :fail-fast? (boolean fail-fast?)
               :stopped-early? stopped-early?
               :skipped-namespaces skipped-namespaces
               :namespace-results namespace-results
               :test-result test-result
               :ok? (and (zero? (:fail test-result))
                         (zero? (:error test-result)))))))

(defn- resolve-test-var
  [test-var]
  (let [namespace-symbol (symbol (namespace test-var))
        var-symbol (symbol (name test-var))]
    (require namespace-symbol)
    (let [namespace-object (find-ns namespace-symbol)
          resolved (get (ns-interns namespace-object) var-symbol)]
      (when-not (and resolved
                     (identical? namespace-object (:ns (meta resolved)))
                     (:test (meta resolved)))
        (throw
         (ex-info "Selected var is not a discovered test"
                  {:id "SH07-ITERATION-CACHE-TEST-VAR"
                   :test-var test-var
                   :namespace namespace-symbol})))
      resolved)))

(defn run-test-var
  [{:keys [test-var maximum-entries]}]
  (let [resolved (resolve-test-var test-var)
        namespace-symbol (symbol (namespace test-var))
        cached
        (with-iteration-cache
          {:maximum-entries maximum-entries}
          (fn []
            (let [started (System/nanoTime)
                  before (cache-snapshot)
                  test-result (test/run-test-var resolved)
                  var-result
                  {:test-var test-var
                   :namespace namespace-symbol
                   :elapsed-ms
                   (long (/ (- (System/nanoTime) started) 1000000))
                   :cache (cache-delta before (cache-snapshot))
                   :authority :non-authoritative}]
              (println
               (pr-str
                (assoc var-result
                       :artifact :gravity/sh07-iteration-test-var-result)))
              (flush)
              {:test-result test-result
               :test-var-result var-result})))
        {:keys [test-result test-var-result]} (:value cached)]
    (-> cached
        (dissoc :value)
        (assoc :artifact :gravity/sh07-iteration-cache-run
               :test-var test-var
               :namespaces [namespace-symbol]
               :test-var-result test-var-result
               :test-result test-result
               :ok? (and (zero? (:fail test-result))
                         (zero? (:error test-result)))))))

(defn run-selection
  [selection]
  (if (:test-var selection)
    (run-test-var selection)
    (run-namespaces selection)))

(defn -main
  [& arguments]
  (let [result (run-selection (parse-arguments arguments))]
    (println (pr-str result))
    (flush)
    (shutdown-agents)
    (when-not (:ok? result)
      (System/exit 1))))
