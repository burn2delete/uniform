
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
