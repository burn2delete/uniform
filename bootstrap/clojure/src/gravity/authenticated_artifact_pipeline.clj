(ns gravity.authenticated-artifact-pipeline
  "The single hosted source-to-checked-core artifact pipeline.

  This namespace is an orchestration facade, not a replacement compiler.  Each
  stage delegates to the existing Gravity bootstrap entrypoint and wraps its
  result in a small authenticated envelope.  The envelope makes the stage
  graph, source identity, policy, provenance, cache decision, and receipt
  explicit.  Fast reuse is process-local and non-authoritative; fresh requests
  never consult the store and execute the same entrypoints again.  Each stage
  consumes the immediately preceding authenticated artifact; transitive
  lineage remains embedded in that owner artifact and is not relabeled as a
  direct edge.

  No envelope produced here grants SH, proof, integration, release,
  self-hosting, seed-retirement, safety, or performance authority."
  (:require [clojure.java.io :as io]
            [gravity.bootstrap :as bootstrap]
            [gravity.c2-pass-cache :as c2-pass-cache]
            [gravity.digest :as digest])
  (:import [java.nio ByteBuffer]
           [java.nio.charset CharacterCodingException CodingErrorAction
            StandardCharsets]
           [java.nio.file Files Path]
           [java.util Arrays]))

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
  {:pipeline-contract "docs/phase-06-compiler-architecture/060-c1-compiler-pipeline-contract.md"
   :artifact-contract "docs/phase-06-compiler-architecture/061-c1-artifact-and-pass-contract.md"
   :cache-contract "C16"
   :source-contract "C2/C3/SH-03"})

(def ^:private policy-binding
  {:reader-policy :gravity/standard-reader
   :source-bound :sh03-reader-maximum-source-bytes
   :fresh-policy :cache-free-same-jvm-reconstruction
   :fast-policy :path-scoped-content-addressed-memory})

;; The process-local fast cache is deliberately bounded.  These limits apply
;; to both the staged per-run map and the committed map.  Cost/node accounting
;; traverses payload structure iteratively with early cutoffs; it never
;; recursively encodes a compiler payload or walks beyond per-entry bounds.
(def ^:private cache-max-entries 32)
(def ^:private cache-max-node-count 262144)
(def ^:private cache-max-entry-cost-bytes (* 2 1024 1024))
(def ^:private cache-max-cost-bytes (* 4 1024 1024))

;; A process-local store is intentional.  It cannot become a release or
;; acceptance cache accidentally and it avoids adding another persistent cache
;; format while the unified execution-plan engine is being introduced.
(defonce ^:private stage-cache (atom {}))
(defonce ^:private cache-metrics
  (atom {:evictions 0 :skipped 0}))

(defn- bounded-payload-measure
  "Iteratively measure a payload without recursive encoding or unbounded work.

  Byte arrays are charged by their real length. Cycles, lazy sequences, and
  unknown host values are rejected so cache admission remains fail-closed."
  [payload]
  (let [;; LinkedList is used rather than ArrayDeque because null is a valid
        ;; scalar payload value and still needs a bounded charge.
        stack (java.util.LinkedList.)
        active (java.util.IdentityHashMap.)
        exit-marker (Object.)
        max-bytes cache-max-entry-cost-bytes
        max-nodes cache-max-node-count]
    (.addLast stack payload)
    (loop [nodes 0 bytes 0]
      (cond
        (> nodes max-nodes) {:status :over-limit :nodes nodes :bytes bytes}
        (> bytes max-bytes) {:status :over-limit :nodes nodes :bytes bytes}
        (.isEmpty stack) {:status :ok :nodes nodes :bytes bytes}
        :else
        (let [item (.removeLast stack)]
          (if (and (vector? item) (identical? exit-marker (first item)))
            (do (.remove active (second item))
                (recur nodes bytes))
            (let [value item
              next-nodes (inc nodes)
              cls (when (some? value) (.getClass value))]
          (cond
            (nil? value)
            (recur next-nodes (+ bytes 4))

            (or (string? value) (keyword? value) (symbol? value)
                (number? value) (boolean? value) (char? value))
            (recur next-nodes (+ bytes (count (str value))))

            (and (.isArray cls)
                 (= Byte/TYPE (.getComponentType cls)))
            (let [length (java.lang.reflect.Array/getLength value)]
              (recur next-nodes (+ bytes 16 length)))

            (.isArray cls)
            (let [length (java.lang.reflect.Array/getLength value)]
              (if (contains? active value)
                {:status :cycle :nodes next-nodes :bytes bytes}
                (do
                  (.put active value true)
                  (.addLast stack [exit-marker value])
                  (doseq [index (range length)]
                    (.addLast stack
                              (java.lang.reflect.Array/get value index)))
                  (recur next-nodes (+ bytes (* 16 length))))))

            (instance? clojure.lang.LazySeq value)
            {:status :unknown :reason :lazy-sequence
             :nodes next-nodes :bytes bytes}

            (map? value)
            (if (contains? active value)
              {:status :cycle :nodes next-nodes :bytes bytes}
              (do
                (.put active value true)
                (.addLast stack [exit-marker value])
                (doseq [[key item] value]
                  (.addLast stack key)
                  (.addLast stack item))
                (recur next-nodes (+ bytes (* 24 (count value))))))

            (coll? value)
            (if (contains? active value)
              {:status :cycle :nodes next-nodes :bytes bytes}
              (do
                (.put active value true)
                (.addLast stack [exit-marker value])
                (doseq [item value] (.addLast stack item))
                (recur next-nodes (+ bytes (* 16 (count value))))))

            :else
            {:status :unknown :reason :unsupported-host-value
             :class (str cls) :nodes next-nodes :bytes bytes}))))))))

(defn- cache-entry-measure
  [entry]
  (let [parents (vec (or (:input-artifact-ids entry) []))
        pass (:pass entry)
        auth (:authentication entry)
        shallow-fields [(:stage entry) (:artifact-id entry)
                        (:payload-artifact-id entry)
                        (:payload-integrity-id entry)
                        (:cache-key-id entry)]
        payload-measure (bounded-payload-measure (:payload entry))]
    {:accepted? (= :ok (:status payload-measure))
     :nodes (+ (:nodes payload-measure)
               1 (count parents) (count (or (:preserves pass) []))
               (count (or (:invalidates pass) []))
               (count (or (:parent-artifact-ids auth) [])))
     :bytes (+ (:bytes payload-measure) 256
               (reduce + 0 (map #(count (str %)) shallow-fields))
               (* 24 (count parents))
               (* 16 (+ (count (or (:preserves pass) []))
                        (count (or (:invalidates pass) [])))))}))

(defn- bounded-cache-map
  "Return a deterministic bounded map and count of entries evicted.

  Keys are content ids plus actual paths.  Sorting their printed form gives a
  stable tie-breaker without inspecting the (potentially huge) payload."
  [entries]
  (loop [current entries evicted 0]
    (let [measured (map (fn [[entry-key entry]]
                          [entry-key (cache-entry-measure entry)]) current)
          invalid (->> measured
                       (filter (comp not :accepted? second))
                       (map first)
                       sort)
          measures (map second measured)
          total-nodes (reduce + 0 (map :nodes measures))
          total-bytes (reduce + 0 (map :bytes measures))]
      (if (and (empty? invalid)
               (<= (count current) cache-max-entries)
               (<= total-nodes cache-max-node-count)
               (<= total-bytes cache-max-cost-bytes))
        {:entries current :evicted evicted
         :nodes total-nodes :bytes total-bytes}
        (if-let [victim (or (first invalid)
                            (some-> (first (sort-by (comp str key) current)) key))]
          (recur (dissoc current victim) (inc evicted))
          {:entries {} :evicted evicted
           :nodes 0 :bytes 0})))))

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

(defn clear-cache!
  "Drop fast-lane intermediate products.  Fresh execution does not need this."
  []
  (reset! stage-cache {})
  (reset! cache-metrics {:evictions 0 :skipped 0})
  nil)

(defn cache-stats
  "Return a bounded diagnostic view of the non-authoritative in-memory store."
  []
  (let [bounded (bounded-cache-map @stage-cache)
        _ (when (not= @stage-cache (:entries bounded))
            (reset! stage-cache (:entries bounded))
            (swap! cache-metrics update :evictions + (:evicted bounded)))
        entries (vals (:entries bounded))
        measures (map cache-entry-measure entries)]
    {:kind :gravity/authenticated-artifact-pipeline-cache-stats
     :eligible? true
     :authoritative? false
     :entry-count (count entries)
     :stages (frequencies (map :stage entries))
     :node-count (reduce + 0 (map :nodes measures))
     :cost-bytes (reduce + 0 (map :bytes measures))
     :limits {:max-entries cache-max-entries
              :max-node-count cache-max-node-count
              :max-entry-cost-bytes cache-max-entry-cost-bytes
              :max-cost-bytes cache-max-cost-bytes}
     :evictions (:evictions @cache-metrics)
     :skipped (:skipped @cache-metrics)
     :nonclaims [:integration :proof :release :self-hosting :seed-retirement
                 :safety :performance]}))

(defn- fail!
  ([id message data]
   (throw (ex-info message (merge {:id id
                                   :pipeline pipeline-id}
                                  (or data {})))))
  ([id message]
   (fail! id message nil)))

(defn- require-stage!
  [stage]
  (when-not (contains? stage-index stage)
    (fail! "ARTIFACT-STAGE"
           "authenticated artifact pipeline stage is unknown"
           {:stage stage :known-stages stages})))

(defn- require-lane!
  [lane]
  (when-not (contains? lanes lane)
    (fail! "ARTIFACT-LANE"
           "authenticated artifact pipeline lane is unknown"
           {:mode lane :known-modes (vec (sort lanes))})))

(defn- require-source-path!
  [source-path]
  (when-not (and (string? source-path) (seq source-path))
    (fail! "ARTIFACT-SOURCE"
           "authenticated artifact pipeline requires a source path"
           {:source-path source-path})))

(defn- bootstrap-var
  [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (fail! "ARTIFACT-ENTRYPOINT"
             "required bootstrap artifact entrypoint is absent"
             {:symbol symbol})))

(defn- bootstrap-call
  [symbol & args]
  (apply (var-get (bootstrap-var symbol)) args))

(defn- strict-source-text!
  [source-path bytes]
  (try
    (str
     (.decode
      (doto (.newDecoder StandardCharsets/UTF_8)
        (.onMalformedInput CodingErrorAction/REPORT)
        (.onUnmappableCharacter CodingErrorAction/REPORT))
      (ByteBuffer/wrap ^bytes bytes)))
    (catch CharacterCodingException error
      (fail! "ARTIFACT-UTF8"
             "source snapshot is not valid UTF-8"
             {:source-path source-path
              :cause-message (.getMessage error)}))))

(defn- source-snapshot!
  "Read through the existing no-follow, bounded, double-snapshot primitive."
  [source-path]
  (try
    (c2-pass-cache/bounded-source-snapshot! source-path)
    (catch clojure.lang.ExceptionInfo error
      ;; Keep the C2 diagnostic id and facts intact.  Add only an orchestration
      ;; breadcrumb so callers can route the failure through a plan receipt.
      (throw (ex-info (ex-message error)
                      (assoc (ex-data error)
                             :pipeline pipeline-id
                             :pipeline-stage :source)
                      error)))))

(defn- project-context
  [actual-path]
  (bootstrap-call 'reader-project-context-for-source actual-path))

(defn- source-identity
  [snapshot context]
  ;; Semantic identity deliberately excludes absolute paths.  Actual path and
  ;; project-root path remain in the separate provenance record below.  This
  ;; lets equivalent checkouts share semantic keys while retaining a
  ;; path-scoped cache entry and never returning stale provenance.
  {:project-root-id (:project-root-id context)
   :project-relative-path (:project-relative-path context)
   :byte-count (:byte-count snapshot)
   :bytes-hash (:bytes-hash snapshot)
   :encoding :utf-8
   :reader-policy :gravity/standard-reader})

(defn- canonical-id!
  [stage value]
  (try
    (c2-pass-cache/canonical-content-id value)
    (catch clojure.lang.ExceptionInfo error
      (fail! "ARTIFACT-ID"
             "authenticated artifact cannot be canonically encoded"
             {:stage stage
              :cause-data (ex-data error)}))
    (catch IllegalArgumentException error
      (fail! "ARTIFACT-ID"
             "authenticated artifact contains an unsupported value"
             {:stage stage
              :cause-message (.getMessage error)}))))

(defn- source-payload
  [source-path snapshot context]
  (let [identity (source-identity snapshot context)]
    {:artifact :gravity/authenticated-source-unit
     :schema-version 1
     :source-identity identity
     :source-identity-id (canonical-id! :source
                                         {:domain :gravity/source-identity-v1
                                          :identity identity})
   :provenance {:actual-path (:canonical-path snapshot)
                :project-root-path (:project-root-path context)
                :project-relative-path (:project-relative-path context)}
   :source-authentication {:status :accepted
                           :method :bounded-secure-double-snapshot
                           :mutation-detected? false
                           :path-rebound? false}
   :diagnostics []
   :authority {:authoritative? false
               :authority-ceiling :development-only}}))

(defn- delegated-artifact-identity
  "Recompute a bounded identity in the owning stage's domain when the
  bootstrap exposes one.  The facade envelope id is intentionally not used as
  a substitute: mutating an embedded payload while retaining that wrapper id
  must still invalidate a fast cache hit.  SH-07's core artifact publishes an
  identity preimage rather than a standalone id helper, so its verifier remains
  the authoritative replay check when that preimage is absent."
  [stage payload]
  (case stage
    :reader (bootstrap-call 'c2-reader-artifact-id payload)
    :syntax (bootstrap-call 'c3-artifact-id payload)
    :macroexpansion (bootstrap-call 'sh05-macro-artifact-id payload)
    :resolution (bootstrap-call 'sh06-resolution-artifact-id payload)
    :checked-core
    (when-let [identity-input
               (bootstrap-call 'sh07-core-artifact-identity-input payload)]
      (bootstrap-call
       'reader-canonical-hash
       {:domain :gravity/sh07-declared-digest-v1
        :purpose :sh07-core-artifact-id
        :preimage identity-input}))
    nil))

(defn- payload-shape-witness
  "Return a bounded, deterministic witness for cache mutation checks.

  Stage-owned artifact ids remain the semantic witness.  This additional
  projection records the complete top-level key set and scalar fields so a
  caller cannot mutate an envelope payload (for example by adding an
  unrecognised field) while retaining its old stage id.  Large child graphs
  are already covered by the owning stage verifier and are not copied here."
  [stage payload]
  (let [scalar? (fn [value]
                  (or (nil? value) (boolean? value) (keyword? value)
                      (symbol? value) (string? value) (number? value)
                      (char? value)))
        scalar-fields
        (into (sorted-map)
              (for [[key value] payload :when (scalar? value)]
                [key value]))
        collection-shape
        (into (sorted-map)
              (for [[key value] payload
                    :when (coll? value)]
                [key (cond
                       (map? value) {:kind :map :count (count value)}
                       (vector? value) {:kind :vector :count (count value)}
                       (set? value) {:kind :set :count (count value)}
                       (sequential? value) {:kind :seq :count (count value)}
                       :else {:kind :coll})]))]
    {:stage stage
     :keys (vec (sort (map name (keys payload))))
     :scalar-fields scalar-fields
     :collection-shape collection-shape
     :artifact-id (:artifact-id payload)}))

(defn- payload-integrity
  [stage payload]
  (if (= :source stage)
    ;; The source envelope is deliberately tiny, so hash the complete value;
    ;; this catches nested identity/authentication edits as well as shape edits.
    {:full-id (canonical-id! stage payload)}
    {:artifact-id (:artifact-id payload)
     :shape-id (canonical-id! stage (payload-shape-witness stage payload))
     ;; Reader/C3/C4/C5 expose bounded owner identity functions.  A nil value
     ;; for SH-07 is deliberate: its existing verification transaction owns
     ;; replay of the large core identity preimage.
     :owner-artifact-id (delegated-artifact-identity stage payload)}))

(defn- payload-size-witness
  "Bounded shallow size projection used only for cache budgeting."
  [stage payload]
  (let [shape (payload-shape-witness stage payload)]
    {:top-level-keys (count (:keys shape))
     :scalar-fields (count (:scalar-fields shape))
     :collections (count (:collection-shape shape))
     :collection-elements
     (reduce + 0 (map #(min 1000000 (long (:count %)))
                      (vals (:collection-shape shape))))}))

(defn- semantic-preimage
  [stage source-id parents payload-artifact-id diagnostics-id]
  {:artifact :gravity/authenticated-compiler-artifact-semantic-v1
   :schema-version pipeline-schema-version
   :stage stage
   :source-identity source-id
   :input-artifact-ids parents
   :payload-artifact-id payload-artifact-id
   :pass-id (get stage-pass-ids stage)
   :compiler implementation-binding
   :policy policy-binding
   :diagnostics-id diagnostics-id})

(defn- payload-integrity-valid?
  [stage payload expected]
  (= expected (payload-integrity stage payload)))

(defn- revalidate-payload?
  "Re-run the owning stage's bounded verifier before accepting a fast hit."
  [stage source-path payload]
  (try
    (case stage
      :source true
      :reader (do (bootstrap-call 'c2-reader-validate! source-path payload)
                  true)
      :syntax (do (bootstrap-call 'c3-syntax-validate! source-path payload)
                  true)
      :macroexpansion (= :passed
                         (:status
                          (bootstrap-call 'sh05-macro-artifact-verification
                                          payload)))
      :resolution (= :passed
                     (:status
                      (bootstrap-call 'sh06-resolution-artifact-verification
                                      payload)))
      :checked-core (= :passed
                       (:status
                        (bootstrap-call 'sh07-core-artifact-verification
                                        payload)))
      false)
    (catch clojure.lang.ExceptionInfo _ false)
    (catch IllegalArgumentException _ false)))

(defn- authenticate-reader-source!
  "Bind the C2 reader payload to the source identity selected by this run.

  C2's own `:source-id` is intentionally a different identity domain from the
  facade's source snapshot id.  We therefore compare the complete bounded
  source identity projection (bytes, project root, and project-relative path)
  and require the source-unit record/provenance witnesses to be present."
  [source-path source-id payload]
  (let [record (:source-unit-record payload)
        identity-inputs (:identity-inputs record)
        root-record (:project-root-record record)]
    (when-not (map? record)
      (fail! "ARTIFACT-SOURCE-PARENT"
             "reader artifact is missing its authenticated source-unit record"
             {:source-path source-path}))
    (when-not (and (= (:bytes-hash source-id) (:bytes-hash record))
                   (= (:bytes-hash source-id) (:bytes-hash identity-inputs))
                   (= (:project-relative-path source-id)
                      (:project-relative-path record))
                   (= (:project-relative-path source-id)
                      (:project-relative-path identity-inputs))
                   (= (:project-root-id source-id) (:project-root record))
                   (= (:project-root-id source-id)
                      (:project-root-id root-record))
                   (= source-path (:path record))
                   (string? (:source-id record)))
      (fail! "ARTIFACT-SOURCE-PARENT"
             "reader artifact source identity does not match the snapshot"
             {:source-path source-path
              :expected (select-keys source-id
                                      [:bytes-hash :project-root-id
                                       :project-relative-path])
              :observed (select-keys record
                                     [:bytes-hash :project-root
                                      :project-relative-path :path])}))
    payload))

(defn- parent-ids
  [stage previous]
  (let [ids (if (= :reader stage)
              (if-let [source-envelope (first previous)]
                [(or (get-in source-envelope [:payload :source-identity-id])
                     (fail! "ARTIFACT-PARENT"
                            "source stage did not emit an identity binding"
                            {:stage stage}))]
                [])
              (->> previous
                   (keep (fn [envelope]
                           ;; Parent edges are authenticated in the existing
                           ;; stage's identity domain, not in this wrapper's
                           ;; envelope identity domain.
                           (or (:payload-artifact-id envelope)
                               (:artifact-id envelope))))
                   vec))]
    (when (and (pos? (get stage-index stage)) (empty? ids))
      (fail! "ARTIFACT-PARENT"
             "non-source artifact is missing its authenticated parent"
             {:stage stage}))
    ids))

(defn- cache-key-preimage
  [stage lane source-id parents request]
  {:artifact :gravity/authenticated-artifact-cache-key
   :schema-version pipeline-schema-version
   :stage stage
   :lane lane
   :source source-id
   :parents parents
   :compiler implementation-binding
   ;; These are bounded, authenticated inputs to the cache identity.  They are
   ;; deliberately timestamp-free so a stable source and toolchain can reuse a
   ;; product while any compiler/tool/contract/policy change forces rebuild.
   :tool-binding tool-binding
   :contract-binding contract-binding
   :policy-binding policy-binding
   :pass-id (get stage-pass-ids stage)
   :policy {:cache-eligible? (= lane :fast)
            :fresh-required? (= lane :fresh)
            :source-authentication :bounded-secure-double-snapshot
            :mutation-check :post-execution-secure-resnapshot}
   :request-policy (select-keys request [:target-stage :mode])})

(defn- receipt
  [stage lane artifact-id parents cache-hit? cache-key-id source-id provenance]
  (let [base {:artifact :gravity/authenticated-artifact-receipt
              :schema-version 1
              :stage stage
              :mode lane
              :artifact-id artifact-id
              :parent-artifact-ids parents
              :cache {:eligible? (= lane :fast)
                      :hit? (boolean cache-hit?)
                      :authoritative? false}
              :source-content-hash (:bytes-hash source-id)
              :actual-path (:actual-path provenance)
              :cache-key-id cache-key-id
              :authority {:authoritative? false
                          :ceiling :development-only
                          :nonclaims [:integration :proof :release :self-hosting
                                      :seed-retirement :safety :performance]}}
        receipt-id (canonical-id! stage base)]
    (assoc base :receipt-id receipt-id)))

(defn- envelope
  [stage lane source-id provenance parents payload cache-hit? cache-key-id]
  (let [payload-id (:artifact-id payload)
        payload-artifact-id (or payload-id
                                (canonical-id! stage
                                               (select-keys payload
                                                            [:artifact
                                                             :schema-version
                                                             :source-identity
                                                             :source-identity-id
                                                             :source-authentication
                                                             :diagnostics
                                                             :authority])))
        ;; Every delegated compiler artifact already carries a stage-owned,
        ;; authenticated artifact id.  Re-hashing an entire checked-core tree
        ;; would exceed C16's bounded canonicalizer (and would duplicate the
        ;; owning verifier); combine that id with a bounded independent shape
        ;; witness.  The source envelope has no upstream id, so its semantic
        ;; projection is canonically encoded by `payload-integrity`.
        payload-integrity-id (payload-integrity stage payload)
        stage-facts (get stage-fact-contracts stage)
        parent-consumption (case stage
                            :source :none
                            :reader :authenticated-source-identity
                            :syntax :authenticated-parent
                            :macroexpansion :authenticated-immediate-parent
                            :resolution :authenticated-immediate-parent
                            :checked-core :authenticated-parent)
        recomputation-boundary? false
        base {:artifact :gravity/authenticated-compiler-artifact
              :schema-version pipeline-schema-version
              :pipeline pipeline-id
              :stage stage
              :status :accepted
              :mode lane
              :artifact-id nil
              :input-artifact-ids parents
              :payload-artifact-id payload-artifact-id
              ;; This full payload digest is for mutation detection only.  It
              ;; is intentionally absent from the semantic artifact identity;
              ;; provenance and lane policy are operational, not semantics.
              :payload-integrity-id payload-integrity-id
              :payload-size-witness (payload-size-witness stage payload)
              :source-identity source-id
              :provenance provenance
              :pass {:id (get stage-pass-ids stage)
                     :input (case stage
                              :source :source-bytes
                              :reader :authenticated-source-identity
                              :syntax :authenticated-parent
                              :macroexpansion :authenticated-immediate-parent
                              :resolution :authenticated-immediate-parent
                              :checked-core :authenticated-parent)
                     :output stage
                     :parent-consumption parent-consumption
                     :preserves (:preserves stage-facts)
                     :invalidates (:invalidates stage-facts)}
              :payload payload
              :diagnostics (vec (or (:diagnostics payload)
                                    (:reader-diagnostics payload)
                                    []))
              :authentication {:status :accepted
                               :source-content-hash (:bytes-hash source-id)
                               :parent-artifact-ids parents
                               :mutation-check :post-execution-secure-resnapshot
                               :path-rebinding-check :actual-path-bound
                               :parent-consumption parent-consumption}
              :recomputation-boundary? recomputation-boundary?
              :execution-boundary {:fresh-cache-bypassed? (= lane :fresh)
                                   ;; This facade is intentionally not an
                                   ;; isolated acceptance executor.  Fresh is
                                   ;; a same-JVM cache bypass only.
                                   :isolation :same-jvm
                                   :isolated? false
                                   :acceptance-authority? false}
              :cache-policy {:eligible? (= lane :fast)
                             :observed-hit? (boolean cache-hit?)
                             :authoritative? false}
              :cache-key-id cache-key-id
              :authority {:authoritative? false
                          :ceiling :development-only
                          :nonclaims [:integration :proof :release :self-hosting
                                      :seed-retirement :safety :performance]}}
        semantic-preimage
        (semantic-preimage stage source-id parents payload-artifact-id
                           (canonical-id! stage (:diagnostics base)))
        artifact-id (canonical-id! stage semantic-preimage)]
    (assoc base
           :semantic-preimage semantic-preimage
           :artifact-id artifact-id
           :receipt (receipt stage lane artifact-id parents cache-hit?
                             cache-key-id source-id provenance))))

(defn- envelope-id-preimage
  [artifact]
  (:semantic-preimage artifact))

(defn- envelope-valid?
  [expected-stage expected-lane expected-key-id expected-key envelope
   source-id provenance parents]
  ;; Validate the operational binding as well as the semantic id.  In
  ;; particular, a valid artifact from another stage/lane/pipeline must be a
  ;; cache miss, never an accepted hit.  Any verifier/identity exception is a
  ;; fail-closed miss rather than a cache-serving error.
  (try
    (let [payload (:payload envelope)
          diagnostics (vec (or (:diagnostics payload)
                               (:reader-diagnostics payload)
                               []))
          semantic (semantic-preimage
                    expected-stage source-id parents
                    (:payload-artifact-id envelope)
                    (canonical-id! expected-stage diagnostics))
          integrity (payload-integrity expected-stage payload)
          owner-id (when-not (= :source expected-stage)
                     (delegated-artifact-identity expected-stage payload))]
      (and (map? envelope)
           (= :gravity/authenticated-compiler-artifact (:artifact envelope))
           (= pipeline-schema-version (:schema-version envelope))
           (= pipeline-id (:pipeline envelope))
           (= expected-stage (:stage envelope))
           (= :accepted (:status envelope))
           (= expected-lane (:mode envelope))
           (= expected-key-id (:cache-key-id envelope))
           (= expected-key-id
              (canonical-id! expected-stage expected-key))
           (= source-id (:source-identity envelope))
           (= provenance (:provenance envelope))
           (= parents (:input-artifact-ids envelope))
           (= semantic (:semantic-preimage envelope))
           (= (:artifact-id envelope)
              (canonical-id! expected-stage
                             (envelope-id-preimage envelope)))
           (= (:payload-integrity-id envelope) integrity)
           (= (:payload-size-witness envelope)
              (payload-size-witness expected-stage payload))
           ;; All currently exposed owner identities are independent from the
           ;; facade wrapper.  SH-07 core's owner identity is verifier-bound
           ;; and may be unavailable here; its replay verifier still runs on a
           ;; cache hit.
           (or (= :source expected-stage)
               (nil? owner-id)
               (= (:artifact-id payload) owner-id))
           (= (get-in envelope [:pass :id]) (get stage-pass-ids expected-stage))
           (= (get-in envelope [:pass :preserves])
              (get-in stage-fact-contracts [expected-stage :preserves]))
           (= (get-in envelope [:pass :invalidates])
              (get-in stage-fact-contracts [expected-stage :invalidates]))
           (= (get-in envelope [:receipt :receipt-id])
              (get-in (receipt expected-stage expected-lane
                               (:artifact-id envelope) parents
                               (get-in envelope [:receipt :cache :hit?])
                               expected-key-id source-id provenance)
                       [:receipt-id]))))
    (catch clojure.lang.ExceptionInfo _ false)
    (catch IllegalArgumentException _ false)))

(defn- embedded-parent-id
  "Read the existing bootstrap artifact's authenticated parent projection."
  [stage payload]
  (case stage
    :reader nil
    :syntax (get-in payload [:c2-reader-artifact :artifact-id])
    :macroexpansion (get-in payload [:sh04-syntax-artifact :artifact-id])
    :resolution (get-in payload [:sh05-macro-artifact :artifact-id])
    :checked-core (get-in payload [:sh06-resolution-artifact :artifact-id])
    nil))

(defn- authenticate-embedded-parent!
  [stage payload parents]
  (when (and (pos? (get stage-index stage))
             (not= :reader stage))
    (let [expected (last parents)
          observed (embedded-parent-id stage payload)]
      (when-not (= expected observed)
        (fail! "ARTIFACT-PARENT"
               "bootstrap artifact does not authenticate the requested parent"
               {:stage stage
                :expected-parent-id expected
                :observed-parent-id observed})))))

(defn- build-payload
  [stage source-path source-text context previous]
  (case stage
    :source (source-payload source-path nil context)
    :reader (bootstrap-call 'compiler-c2-reader-source-artifact
                            source-path source-text context)
    :syntax (bootstrap-call
             'compiler-c3-syntax-source-artifact
             source-path source-text context (:payload (last previous))
             (var-get (bootstrap-var 'sh03-reader-internal-product-authority)))
    ;; SH-05 and SH-06 consume only their immediate authenticated parent.
    ;; Their historical source entrypoints remain compatibility wrappers, but
    ;; the unified pipeline must not rebuild an already-authenticated prefix.
    :macroexpansion (bootstrap-call 'sh05-macro-artifact-from-c3-artifact
                                    source-path (:payload (last previous)))
    :resolution (bootstrap-call 'sh06-resolution-artifact-from-sh05-artifact
                                source-path (:payload (last previous)))
    :checked-core (bootstrap-call 'sh07-core-from-resolution-artifact
                                 (:payload (last previous)))))

(defn- build-stage!
  [stage lane source-id provenance source-path source-text context previous
   request]
  (let [parents (parent-ids stage previous)
        preimage (cache-key-preimage stage lane source-id parents request)
        key-id (canonical-id! stage preimage)
        key [key-id (:actual-path provenance)]]
    (if (and (= lane :fast) (contains? @stage-cache key))
      (let [cached (get @stage-cache key)]
        (if (and (envelope-valid? stage lane key-id preimage cached source-id
                                  provenance parents)
                 (or (not= :reader stage)
                     (try
                       (authenticate-reader-source! source-path source-id
                                                    (:payload cached))
                       true
                       (catch clojure.lang.ExceptionInfo _ false)))
                 (revalidate-payload? stage source-path (:payload cached)))
          (-> cached
              (assoc :cache-hit? true
                     :cache-policy (assoc (:cache-policy cached)
                                          :observed-hit? true))
              (assoc :receipt
                     (receipt stage lane (:artifact-id cached) parents true
                              key-id source-id provenance)))
          (do
            (swap! stage-cache dissoc key)
            (let [payload (build-payload stage source-path source-text context previous)
                  _ (when (= :reader stage)
                      (authenticate-reader-source! source-path source-id payload))
                  _ (authenticate-embedded-parent! stage payload parents)
                  built (envelope stage lane source-id provenance parents payload
                                   false key-id)]
              (cache-put! key built)
              built))))
      (let [payload (build-payload stage source-path source-text context previous)
            _ (when (= :reader stage)
                (authenticate-reader-source! source-path source-id payload))
            _ (authenticate-embedded-parent! stage payload parents)
            built (envelope stage lane source-id provenance parents payload false
                             key-id)]
        (when (= lane :fast)
          (cache-put! key built))
        built))))

(defn- stable-source-after!
  [source-path before]
  (let [after (source-snapshot! source-path)]
    (when-not (and (= (:canonical-path before) (:canonical-path after))
                   (= (:byte-count before) (:byte-count after))
                   (= (:bytes-hash before) (:bytes-hash after)))
      (fail! "ARTIFACT-MUTATED"
             "source changed or was rebound while the artifact pipeline ran"
             {:source-path source-path
              :before (select-keys before [:canonical-path :byte-count :bytes-hash])
              :after (select-keys after [:canonical-path :byte-count :bytes-hash])}))
    after))

(defn- validate-request
  [request]
  (when-not (map? request)
    (fail! "ARTIFACT-REQUEST"
           "authenticated artifact pipeline request must be a map"
           {:request request}))
  (let [known #{:source-path :target-stage :mode}]
    (when-let [unknown (seq (remove known (keys request)))]
      (fail! "ARTIFACT-REQUEST"
             "authenticated artifact pipeline request contains unknown keys"
             {:unknown-keys (vec (sort unknown))
              :known-keys (vec (sort known))})))
  (let [source-path (:source-path request)
        target-stage (get request :target-stage :checked-core)
        mode (get request :mode :fast)]
    (require-source-path! source-path)
    (require-stage! target-stage)
    (require-lane! mode)
    {:source-path source-path :target-stage target-stage :mode mode}))

(defn run-pipeline
  "Build the requested earliest artifact and all of its authenticated parents.

  Accepts either a request map or a source path.  The map schema is closed:
  `{:source-path string :target-stage stage :mode :fast|:fresh}`.  The result
  keeps both envelopes and the original bootstrap payload available:
  `:artifact` is the selected envelope, `:payload` is its existing artifact,
  and `:stage-artifacts` is the ordered stage map.
  "
  ([request]
   ;; Entries built by this invocation remain staged until the final secure
   ;; source re-snapshot and bounded result identity both succeed.  A mutation
   ;; or construction failure therefore cannot publish a misleading fast hit.
   (binding [*pending-cache* (atom {})]
     (let [{:keys [source-path target-stage mode] :as request}
           (validate-request (if (string? request)
                               {:source-path request}
                               request))
         before (source-snapshot! source-path)
         actual-path (:canonical-path before)
         context (project-context actual-path)
         source-text (strict-source-text! actual-path (:bytes before))
         source-id (source-identity before context)
         provenance {:actual-path actual-path
                     :project-root-path (:project-root-path context)
                     :project-root-id (:project-root-id context)
                     :project-relative-path (:project-relative-path context)}
         target-index (get stage-index target-stage)
         artifacts
         (loop [index 0 previous []]
           (if (> index target-index)
             previous
             (let [stage (nth stages index)
                   built
                   (if (= :source stage)
                     (let [preimage (cache-key-preimage stage mode source-id [] request)
                           key-id (canonical-id! stage preimage)
                           key [key-id actual-path]
                           cached (when (and (= mode :fast)
                                             (contains? @stage-cache key))
                                    (get @stage-cache key))]
                       (if (and cached
                           (envelope-valid? stage mode key-id preimage cached
                                            source-id provenance [])
                           (revalidate-payload? stage actual-path
                                                 (:payload cached)))
                         (-> cached
                             (assoc :cache-hit? true
                                    :cache-policy
                                    (assoc (:cache-policy cached)
                                           :observed-hit? true))
                             (assoc :receipt
                                    (receipt stage mode (:artifact-id cached) []
                                             true key-id source-id provenance)))
                         (let [payload (source-payload actual-path before context)
                               built (envelope stage mode source-id provenance []
                                                payload false key-id)]
                           (when (= mode :fast)
                             (cache-put! key built))
                           built)))
                     (build-stage! stage mode source-id provenance actual-path
                                   source-text context previous request))]
               (recur (inc index) (conj previous built)))))
         _ (stable-source-after! actual-path before)
         selected (last artifacts)
         stage-map (into (array-map) (map (juxt :stage identity) artifacts))
         result-base {:kind :gravity/authenticated-artifact-pipeline-result
                      :schema-version pipeline-schema-version
                      :pipeline pipeline-id
                      :requested-stage target-stage
                      :mode mode
                      :actual-path actual-path
                      :source-content-hash (:bytes-hash source-id)
                      :stage-artifacts stage-map
                      :artifact selected
                      :payload (:payload selected)
                      :authority {:authoritative? false
                                  :ceiling :development-only
                                  :nonclaims [:integration :proof :release
                                              :self-hosting :seed-retirement
                                              :safety :performance]}}
         ;; A result is an operational envelope over potentially very large
         ;; compiler payloads.  Bind it to the ordered authenticated stage
         ;; identities instead of recursively re-encoding those payloads;
         ;; the latter exceeds C16's closed node bound for real checked core.
         result-id
         (canonical-id!
          target-stage
          {:kind (:kind result-base)
           :schema-version pipeline-schema-version
           :pipeline pipeline-id
           :requested-stage target-stage
           ;; Semantic result identity is path/lane neutral.  Absolute path,
           ;; cache hit, and fresh/fast policy remain operational fields on the
           ;; result but do not create distinct compiler artifacts.
           :source-identity source-id
           :stage-artifact-ids
           (mapv (fn [stage]
                   [stage (get-in stage-map [stage :artifact-id])])
                 (keys stage-map))
           :selected-artifact-id (:artifact-id selected)
           :authority (:authority result-base)})
         result (assoc result-base
                       :result-id result-id
                       :execution-boundary
                       {:fresh-cache-bypassed? (= mode :fresh)
                        :isolation :same-jvm
                        :isolated? false
                        :acceptance-authority? false})
         ;; Commit only after the post-execution source snapshot and bounded
         ;; semantic result id have succeeded.  Fresh requests never stage or
         ;; commit entries.
         _ (when (= mode :fast)
             (commit-pending-cache! @*pending-cache*))]
       result))))

(defn artifact
  "Return the existing bootstrap payload selected by `run-pipeline`."
  [result]
  (or (:payload result)
      (fail! "ARTIFACT-RESULT" "pipeline result has no selected payload" {})))

(defn artifact-envelope
  "Return the authenticated envelope selected by `run-pipeline`."
  [result]
  (or (:artifact result)
      (fail! "ARTIFACT-RESULT" "pipeline result has no selected envelope" {})))

(defn stage-artifact
  "Return one stage envelope from a pipeline result, if it was requested."
  [result stage]
  (require-stage! stage)
  (or (get-in result [:stage-artifacts stage])
      (fail! "ARTIFACT-STAGE"
             "requested stage was not built by this pipeline result"
             {:stage stage
              :requested-stage (:requested-stage result)})))
