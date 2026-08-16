(ns gravity.self-hosting.sh07-cached-shard-runner
  "Runs B15 iteration shards with process-local immutable artifact caches.

  Cache results are acceleration only and are never authoritative evidence.
  Use sh07-authoritative-runner for the fresh-process gate."
  (:require [clojure.test :as test]
            [gravity.bootstrap :as bootstrap]))

(def ^:private test-namespace
  'gravity.self-hosting.sh07-module-fragment-test)

(def ^:private shards
  (sorted-map
   "contract"
   '[sh07-b13-fixtures-are-dynamically-discovered-paired-and-bounded
     sh07-b13-claim-boundary-remains-honest]
   "accepted"
   '[sh07-b13-direct-and-public-routing-use-v14
     sh07-b13-fragment-manifest-is-exact-root-aligned-and-exhaustive
     sh07-b13-fragment-coverage-is-exact-ordered-and-complete
     sh07-b13-module-assembly-manifest-is-exact-and-bound
     sh07-b13-cross-fragment-definitions-and-references-bind
     sh07-b13-b12-alias-value-and-call-survive-fragment-assembly]
   "identity"
   '[sh07-b13-identities-are-deterministic-path-neutral-and-provenanced
     sh07-b13-stale-sh06-and-alias-binding-resolution-fail-closed]
   "verification"
   '[sh07-b13-fragment-request-alterations-fail-closed
     sh07-b13-gravity-validates-fragment-plan-and-module-anchors
     sh07-b13-module-resolvers-use-module-aggregate-bounds
     sh07-b13-fragment-module-and-coverage-products-fail-replay]
   "rejected"
   '[sh07-b13-rejected-fixtures-follow-declared-oracles
     sh07-b13-rejected-corpus-covers-size-and-form-boundaries]))

(defn shard-names
  []
  (vec (keys shards)))

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
  (let [symbols (get shards shard-name)]
    (when-not symbols
      (throw
       (ex-info "Unknown cached SH-07 shard"
                {:id "SH07-CACHED-SHARD"
                 :shard shard-name
                 :available (shard-names)})))
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
