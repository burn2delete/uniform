
(def pipeline-schema-version 1)
(def pipeline-id :gravity/authenticated-compiler-artifact-pipeline-v1)

(def stages
  "The only legal stage names, in dependency order.

  `:checked-core` is the existing SH-07 checked-core boundary.  It already
  includes core lowering and its verification transaction, so exposing a
  second C6/C7-looking stage here would create a competing architecture."
  [:source :reader :syntax :macroexpansion :resolution :checked-core])

(def lanes
  "Execution policy is deliberately closed.  :fast never carries authority."
  #{:fast :fresh})

(def ^:private stage-index
  (zipmap stages (range)))

(def ^:private stage-pass-ids
  {:source "gravity/source-identity-v1"
   :reader "gravity/c2-reader-document-v1"
   :syntax "gravity/c3-syntax-object-v1"
   :macroexpansion "gravity/sh05-macro-expansion-v1"
   :resolution "gravity/sh06-name-resolution-v1"
   :checked-core "gravity/sh07-checked-core-v1"})

(def ^:private stage-fact-contracts
  "Stage-specific fact ledgers.  These are intentionally explicit: a generic
  empty invalidation list would falsely imply that no upstream facts can ever
  be invalidated."
  {:source {:preserves [:source-bytes :source-identity :path-provenance]
            :invalidates [:none-before-source-read]}
   :reader {:preserves [:source-bytes :source-spans :raw-literal-facts
                        :reader-origin :trivia :diagnostics]
            :invalidates [:reader-tokenization-input :reader-form-input]}
   :syntax {:preserves [:source-spans :generated-origin :namespace-context
                        :profile :metadata :hygiene]
            :invalidates [:reader-seed-projection :syntax-fact-input]}
   :macroexpansion {:preserves [:syntax-identity :metadata :hygiene
                                :origins :reader-lineage]
                    :invalidates [:pre-expansion-surface :macro-input]}
   :resolution {:preserves [:syntax-identity :generated-origin :metadata
                            :macro-lineage]
                :invalidates [:unresolved-reference-facts :macro-surface]}
   :checked-core {:preserves [:source-spans :generated-origin :metadata
                              :resolution-lineage :diagnostics]
                  :invalidates [:surface-form-facts :unlowered-resolution-facts]}})

(def ^:private implementation-binding
  {:compiler-id "gravity.authenticated-artifact-pipeline"
   :compiler-version 1
   :pipeline-id pipeline-id
   :schema-version pipeline-schema-version
   :host-language :clojure
   :host-authoritative? false})

(def ^:private tool-binding
  {:clojure-version (clojure-version)
   :java-version (System/getProperty "java.version")
   :runtime-name (System/getProperty "java.runtime.name")})

(def ^:private contract-binding
  {:pipeline-contract "docs/phase-06-compiler-architecture/080-c1-compiler-architecture-overview.md"
   :artifact-contract "docs/phase-12-build-package-and-artifact-system/167-pkg3-artifact-model-specification.md"
   :cache-contract "C16"
   :source-contract "C2/C3/SH-03"})

(def ^:private policy-binding
  {:reader-policy :gravity/standard-reader
   :fresh-policy :cache-free-same-jvm-reconstruction
   :fast-policy :path-scoped-content-addressed-memory})

;; The process-local fast cache is deliberately bounded.  This retention limit
;; applies to both the staged per-run map and the committed map.  The cache is
;; an operational same-JVM store, not a compiler input budget or accounting
;; boundary.
(def ^:private cache-max-entries 32)

;; A process-local store is intentional.  It cannot become a release or
;; acceptance cache accidentally and it avoids adding another persistent cache
;; format while the unified execution-plan engine is being introduced.
(defonce ^:private stage-cache (atom {}))
(defonce ^:private cache-metrics
  (atom {:evictions 0 :skipped 0}))

(defn- bounded-cache-map
  "Return a deterministic bounded map and count of entries evicted.

  Keys are content ids plus actual paths.  Sorting their printed form gives a
  stable tie-breaker without inspecting the payload."
  [entries]
  (loop [current entries evicted 0]
    (if (<= (count current) cache-max-entries)
      {:entries current :evicted evicted}
      (if-let [victim (some-> (first (sort-by (comp str key) current)) key)]
        (recur (dissoc current victim) (inc evicted))
        {:entries {} :evicted evicted}))))

(def ^:dynamic *pending-cache*
  "Per-run fast entries are committed only after the final source re-snapshot.
  A failed or mutated run therefore cannot publish a cache entry."
  nil)

(defn- cache-put!
  [key value]
  (let [target (or *pending-cache* stage-cache)
        candidate (assoc @target key value)
        bounded (bounded-cache-map candidate)]
    (swap! target (constantly (:entries bounded)))
    (when (pos? (:evicted bounded))
      (swap! cache-metrics update :evictions + (:evicted bounded)))
    (when-not (contains? (:entries bounded) key)
      (swap! cache-metrics update :skipped inc)))
  value)

(defn- commit-pending-cache!
  "Atomically merge a run's staged entries through the same bounded policy."
  [pending]
  (let [bounded-result (atom nil)]
    (swap! stage-cache
           (fn [current]
             (let [bounded (bounded-cache-map (merge current pending))]
               (reset! bounded-result bounded)
               (:entries bounded))))
    (let [bounded @bounded-result]
      (when (pos? (:evicted bounded))
        (swap! cache-metrics update :evictions + (:evicted bounded)))
      bounded)))
