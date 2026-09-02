(ns gravity.self-hosting.sh07-cached-shard-runner
  "Runs B15 iteration shards with process-local immutable artifact caches.

  Cache results are acceleration only and are never authoritative evidence.
  Use sh07-authoritative-runner for the fresh-process gate."
  (:require [clojure.string :as str]
            [clojure.test :as test]
            [gravity.bootstrap :as bootstrap]
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

(defn- sha256
  [value]
  (let [digest (java.security.MessageDigest/getInstance "SHA-256")]
    (.update digest
             (.getBytes (str value)
                        java.nio.charset.StandardCharsets/UTF_8))
    (apply str (map #(format "%02x" (bit-and % 0xff))
                    (.digest digest)))))

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

(defn- resolution-key
  [artifact]
  [(System/identityHashCode artifact)
   (:artifact-id artifact)
   (get-in artifact [:provenance :source-path])
   (get-in artifact
           [:gravity-resolution-boundary :resolved-analysis
            :semantic-projection-id])])

(defn run-shard
  [shard-name]
  (let [tests (resolve-tests shard-name)
        cache-context
        {:request-schema-version 15
         :adapter bootstrap/sh07-core-adapter-contract
         :checked-core-source-content-hash
         bootstrap/sh07-core-expected-source-content-hash
         :checked-core-plan-semantic-hash
         bootstrap/sh07-core-expected-plan-semantic-hash
         :java-runtime-version (System/getProperty "java.runtime.version")
         :clojure-version (clojure-version)}
        original-sh06 bootstrap/sh06-resolution-source-artifact
        original-from-resolution
        bootstrap/sh07-core-from-resolution-artifact
        original-verification bootstrap/sh07-core-artifact-verification
        sh06-cache (atom {})
        core-cache (atom {})
        verification-cache (atom {})
        statistics (atom {:sh06-hits 0 :sh06-misses 0
                          :core-hits 0 :core-misses 0
                          :verification-hits 0
                          :verification-misses 0})
        cached-sh06
        (fn [path source]
          (let [key [cache-context path (sha256 source)]]
            (if-let [entry (find @sh06-cache key)]
              (do (swap! statistics update :sh06-hits inc)
                  (val entry))
              (let [value (original-sh06 path source)]
                (swap! statistics update :sh06-misses inc)
                (swap! sh06-cache assoc key value)
                value))))
        cached-core
        (fn [resolution]
          (let [key [cache-context (resolution-key resolution)]]
            (if-let [entry (find @core-cache key)]
              (do (swap! statistics update :core-hits inc)
                  (val entry))
              (let [value (original-from-resolution resolution)]
                (swap! statistics update :core-misses inc)
                (swap! core-cache assoc key value)
                value))))
        cached-verification
        (fn [artifact]
          (let [key [(System/identityHashCode artifact)
                     cache-context
                     (:artifact-id artifact)
                     (hash artifact)]]
            (if-let [entry (find @verification-cache key)]
              (do (swap! statistics update :verification-hits inc)
                  (val entry))
              (let [value (original-verification artifact)]
                (swap! statistics update :verification-misses inc)
                (swap! verification-cache assoc key value)
                value))))
        counters (ref test/*initial-report-counters*)
        started (System/nanoTime)]
    (with-redefs [bootstrap/sh06-resolution-source-artifact cached-sh06
                  bootstrap/sh07-core-from-resolution-artifact cached-core
                  bootstrap/sh07-core-artifact-verification
                  cached-verification]
      (binding [test/*report-counters* counters]
        (test/test-vars tests)))
    (merge
     @counters
     {:artifact :gravity/sh07-cached-shard-result
      :shard shard-name
      :test-vars (count tests)
      :cache-authoritative? false
      :fresh-authoritative-run-required? true
      :cache-context cache-context
      :elapsed-ms
      (long (/ (- (System/nanoTime) started) 1000000))
     :cache @statistics})))

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
