(ns gravity.self-hosting.sh07-iteration-cache-runner
  "Runs focused SH-07 namespaces with bounded process-local caches.

  This runner is iteration acceleration only. Its result is always
  non-authoritative, and a fresh authoritative run remains required."
  (:require [clojure.test :as test]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting-test-runner :as test-runner]))

(def ^:private default-maximum-entries 4)

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
        counters (atom {:sh06-hits 0 :sh06-misses 0
                        :core-hits 0 :core-misses 0
                        :verification-hits 0 :verification-misses 0})
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
        (with-redefs [bootstrap/sh06-resolution-source-artifact cached-sh06
                      bootstrap/sh07-core-from-resolution-artifact cached-core
                      bootstrap/sh07-core-artifact-verification
                      cached-verification]
          (operation))]
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
                 :maximum-entries default-maximum-entries}]
    (if (empty? remaining)
      (do
        (when (empty? (:namespaces result))
          (throw
           (ex-info "At least one --namespace is required"
                    {:id "SH07-ITERATION-CACHE-SELECTION"})))
        (when-not (= (count (:namespaces result))
                     (count (distinct (:namespaces result))))
          (throw
           (ex-info "Iteration cache namespaces must be unique"
                    {:id "SH07-ITERATION-CACHE-DUPLICATE-NAMESPACE"
                     :namespaces (:namespaces result)})))
        result)
      (let [[option value & tail] remaining]
        (when (nil? value)
          (throw
           (ex-info "Iteration cache option requires a value"
                    {:id "SH07-ITERATION-CACHE-USAGE"
                     :option option})))
        (case option
          "--namespace"
          (let [namespace (symbol value)]
            ;; Delegate ownership validation to the coordinator runner.
            (test-runner/select-tests ["--namespace" value])
            (recur (vec tail)
                   (update result :namespaces conj namespace)))

          "--max-cache-entries"
          (recur (vec tail)
                 (assoc result :maximum-entries
                        (parse-positive-integer option value)))

          (throw
           (ex-info "Unsupported iteration cache option"
                    {:id "SH07-ITERATION-CACHE-USAGE"
                     :option option
                     :supported ["--namespace" "--max-cache-entries"]})))))))

(defn run-namespaces
  [{:keys [namespaces maximum-entries]}]
  (doseq [namespace namespaces]
    (require namespace))
  (let [cached
        (with-iteration-cache
          {:maximum-entries maximum-entries}
          #(apply test/run-tests namespaces))
        test-result (:value cached)]
    (-> cached
        (dissoc :value)
        (assoc :artifact :gravity/sh07-iteration-cache-run
               :namespaces namespaces
               :test-result test-result
               :ok? (and (zero? (:fail test-result))
                         (zero? (:error test-result)))))))

(defn -main
  [& arguments]
  (let [result (run-namespaces (parse-arguments arguments))]
    (println (pr-str result))
    (when-not (:ok? result)
      (System/exit 1))))
