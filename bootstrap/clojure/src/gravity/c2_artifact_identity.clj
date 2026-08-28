(ns gravity.c2-artifact-identity
  "Hosted Stage0 C2 canonical hashing and content-addressed artifact identity.

  This leaf projects already-produced hosted reader records into path-neutral
  hashes, integrity records, and artifact IDs. It does not read source,
  execute/authenticate SH03, construct canonical C2 products, or grant proof,
  self-hosting, attestation, cache-reuse, or release authority.")

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(def ^:private ^:dynamic *bypass-next-operation-keys* #{})
(def ^:private function-operation-keys
  #{:sha256-hex
    :c2-form-graph-metrics
    :c2-reader-fail!
    :source-span
    :reader-canonical-value
    :reader-canonical-hash
    :c2-semantic-form-hash-input
    :c2-path-neutral-span
    :c2-token-hash-input
    :c2-form-hash-input
    :c2-syntax-seed-hash-input
    :c2-extension-hash-input
    :c2-diagnostic-hash-input
    :c2-incremental-hashes
    :c2-reader-product-integrity-record
    :c2-reader-artifact-id})
(def ^:private scalar-operation-keys #{:max-reader-form-graph-depth})
(def ^:private operation-keys
  (into function-operation-keys scalar-operation-keys))

(def ^:private namespace-contract
  {:namespace 'gravity.c2-artifact-identity
   :contract-boundary :hosted-c2-content-addressed-artifact-identity
   :public-api
   {'with-operations {:arglists '([operations thunk])}
    'call-entrypoint-body {:arglists '([operation-key operation args])}
    'reader-canonical-value {:arglists '([value])}
    'reader-canonical-hash {:arglists '([value])}
    'c2-semantic-form-hash-input {:arglists '([form-tree])}
    'c2-path-neutral-span {:arglists '([span])}
    'c2-token-hash-input {:arglists '([token-stream])}
    'c2-form-hash-input {:arglists '([form-tree])}
    'c2-syntax-seed-hash-input {:arglists '([syntax-seeds])}
    'c2-extension-hash-input {:arglists '([extension-invocations])}
    'c2-diagnostic-hash-input {:arglists '([diagnostics])}
    'c2-incremental-hashes
    {:arglists '([source-unit token-stream form-tree syntax-seeds
                  extension-invocations diagnostics])}
    'c2-reader-product-integrity-record
    {:arglists '([source-unit top-level-form-ids incremental-hashes
                  literal-records deferred-literal-records])}
    'c2-reader-artifact-id {:arglists '([artifact])}}
   :operation-interposition
   {:accepted-keys operation-keys
    :partial-overrides? true
    :unknown-keys-rejected? true
    :function-values-must-satisfy :fn?
    :entrypoint-requirements
    {'reader-canonical-hash #{:sha256-hex}
     'c2-incremental-hashes
     #{:sha256-hex :c2-form-graph-metrics :c2-reader-fail! :source-span
       :max-reader-form-graph-depth}
     'c2-reader-product-integrity-record #{:sha256-hex}
     'c2-reader-artifact-id #{:sha256-hex}}}
   :artifact-inputs [:hosted-c2-reader-records]
   :artifact-outputs [:hosted-c2-incremental-hashes
                      :hosted-c2-reader-product-integrity
                      :hosted-c2-reader-artifact-id]
   :ownership
   {:owns [:hosted-c2-canonical-value-projection
           :hosted-c2-path-neutral-hash-inputs
           :hosted-c2-incremental-product-hashes
           :hosted-c2-reader-product-integrity-record
           :hosted-c2-artifact-id]
    :does-not-own [:canonical-c2-reader-authority
                   :sh03-reader-product-authentication
                   :source-reading
                   :canonical-source-identity
                   :cache-reuse-authority
                   :diagnostic-policy
                   :proof-authority
                   :attestation-authority
                   :self-hosted-authority
                   :release-authority]}
   :dependency-direction
   {:requires ['clojure.core]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :canonical-c2-authority? false
   :cache-reuse-authority? false
   :proof-authority? false
   :self-hosted? false
   :release-authority? false})

(defn- validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "C2 artifact-identity operations must be a map"
                    {:operations operations})))
  (let [unknown (vec (remove operation-keys (keys operations)))]
    (when (seq unknown)
      (throw (ex-info "C2 artifact-identity operations contain unknown keys"
                      {:unknown-keys unknown}))))
  (doseq [key function-operation-keys
          :when (contains? operations key)]
    (when-not (fn? (get operations key))
      (throw (ex-info "C2 artifact-identity operation must be a function"
                      {:operation key :value (get operations key)}))))
  (when (contains? operations :max-reader-form-graph-depth)
    (let [depth (:max-reader-form-graph-depth operations)]
      (when-not (and (integer? depth) (pos? depth))
        (throw (ex-info "C2 artifact-identity depth limit must be positive"
                        {:operation :max-reader-form-graph-depth
                         :value depth})))))
  operations)

(defn with-operations [operations thunk]
  (validate-operations! operations)
  (when-not (fn? thunk)
    (throw (ex-info "C2 artifact-identity thunk must be a function"
                    {:thunk thunk})))
  (binding [*operations* operations] (thunk)))

(defn call-entrypoint-body
  "Enter one extracted function body while retaining recursive bootstrap Var
  interposition. This is the narrow compatibility trampoline used by the
  bootstrap wrappers; ordinary leaf callers should use with-operations."
  [operation-key operation args]
  (when-not (contains? operation-keys operation-key)
    (throw (ex-info "C2 artifact-identity entrypoint key is unknown"
                    {:operation operation-key})))
  (when-not (fn? operation)
    (throw (ex-info "C2 artifact-identity entrypoint must be a function"
                    {:operation operation-key :value operation})))
  (when-not (sequential? args)
    (throw (ex-info "C2 artifact-identity entrypoint args must be sequential"
                    {:operation operation-key :args args})))
  (binding [*active-operation-keys*
            (disj *active-operation-keys* operation-key)
            *bypass-next-operation-keys*
            (conj *bypass-next-operation-keys* operation-key)]
    (apply operation args)))

(defn- current-operation [key]
  (when-not (contains? *active-operation-keys* key) (get *operations* key)))

(defn- operation-value [key]
  (if (contains? *operations* key)
    (get *operations* key)
    (throw (ex-info (str "C2 artifact identity requires operation " key)
                    {:operation key}))))

(defn- invoke [key & args]
  (if-let [operation (current-operation key)]
    (apply operation args)
    (throw (ex-info (str "C2 artifact identity requires operation " key)
                    {:operation key}))))

(defmacro ^:private definterposable [name key arguments & body]
  `(defn ~name ~arguments
     (if (contains? *bypass-next-operation-keys* ~key)
       (binding [*bypass-next-operation-keys*
                 (disj *bypass-next-operation-keys* ~key)]
         (do ~@body))
       (if-let [operation# (current-operation ~key)]
         (binding [*active-operation-keys*
                   (conj *active-operation-keys* ~key)]
           (operation# ~@arguments))
         (do ~@body)))))

(definterposable reader-canonical-value :reader-canonical-value
  [value]
  (cond
    (map? value)
    (let [decorated
          (mapv
           (fn [[key item]]
             (let [entry [(reader-canonical-value key)
                          (reader-canonical-value item)]]
               [(pr-str entry) entry]))
           value)]
      [:map
       (->> decorated
            (sort-by first)
            (mapv second))])

    (set? value)
    (let [decorated
          (mapv (fn [item]
                  (let [entry (reader-canonical-value item)]
                    [(pr-str entry) entry]))
                value)]
      [:set (mapv second (sort-by first decorated))])

    (vector? value)
    [:vector (mapv reader-canonical-value value)]

    (seq? value)
    [:list (mapv reader-canonical-value value)]

    :else value))

(definterposable reader-canonical-hash :reader-canonical-hash
  [value]
  (str "sha256:"
       (invoke :sha256-hex
        (binding [*print-length* nil
                  *print-level* nil
                  *print-meta* true]
          (pr-str (reader-canonical-value value))))))


(definterposable c2-semantic-form-hash-input :c2-semantic-form-hash-input
  [form-tree]
  (mapv #(select-keys % [:form-id :kind :collection-kind :children
                         :parent-form-id :abbrev :tag :value :metadata])
        form-tree))

(definterposable c2-path-neutral-span :c2-path-neutral-span
  [span]
  (if (map? span) (dissoc span :source) span))

(definterposable c2-token-hash-input :c2-token-hash-input
  [token-stream]
  (mapv #(-> %
             (dissoc :source-path)
             (update :span c2-path-neutral-span))
        token-stream))

(definterposable c2-form-hash-input :c2-form-hash-input
  [form-tree]
  (mapv (fn [form]
          (-> form
              (dissoc :source-path)
              (update :span c2-path-neutral-span)
              (update :surface-span c2-path-neutral-span)
              (update :origin #(when % (dissoc % :source-path)))
              (update :generated-origin
                      (fn [origins]
                        (mapv #(update % :from c2-path-neutral-span)
                              (or origins []))))))
        form-tree))

(definterposable c2-syntax-seed-hash-input :c2-syntax-seed-hash-input
  [syntax-seeds]
  (mapv (fn [seed]
          (cond-> (update seed :span c2-path-neutral-span)
            (contains? seed :generated-origin)
            (update :generated-origin
                    (fn [origins]
                      (mapv #(cond-> %
                               (contains? % :from)
                               (update :from c2-path-neutral-span))
                            origins)))))
        syntax-seeds))

(definterposable c2-extension-hash-input :c2-extension-hash-input
  [extension-invocations]
  (let [semantic-span
        (fn [span]
          (if (map? span)
            (dissoc span :source :file)
            span))]
    (mapv
     (fn [invocation]
       (cond-> (dissoc invocation :source-path)
         (contains? invocation :span)
         (update :span semantic-span)

         (contains? invocation :invocations)
         (update :invocations
                 (fn [records]
                   (mapv #(cond-> %
                            (contains? % :span)
                            (update :span semantic-span))
                         records)))))
     extension-invocations)))

(definterposable c2-diagnostic-hash-input :c2-diagnostic-hash-input
  [diagnostics]
  (mapv
   (fn [diagnostic]
     (cond-> diagnostic
       (contains? diagnostic :source-span)
       (update :source-span c2-path-neutral-span)

       (get-in diagnostic [:primary :span])
       (update-in [:primary :span] c2-path-neutral-span)

       (contains? diagnostic :related)
       (update :related
               (fn [related]
                 (mapv #(cond-> %
                          (contains? % :span)
                          (update :span c2-path-neutral-span))
                       related)))

       (contains? diagnostic :origin-chain)
       (update :origin-chain
               (fn [origins]
                 (mapv #(cond-> (dissoc % :path)
                          (contains? % :span)
                          (update :span c2-path-neutral-span))
                       origins)))))
   diagnostics))

(definterposable c2-incremental-hashes :c2-incremental-hashes
  [source-unit token-stream form-tree syntax-seeds extension-invocations
  diagnostics]
  (let [graph-metrics (invoke :c2-form-graph-metrics form-tree)
        max-depth (:max-form-depth graph-metrics)
        depth-limit (operation-value :max-reader-form-graph-depth)
        _ (when-not (:acyclic? graph-metrics)
            (invoke :c2-reader-fail!
             "C2-HASH" (:path source-unit)
             {:stage :read-source
              :source-id (:source-id source-unit)
              :source-span (or (:span (first form-tree))
                               (invoke :source-span (:path source-unit) 0))
              :reader-options (:reader-options source-unit)}
             {:missing-fields [:acyclic-reader-form-graph]
              :facts {:failure-kind :reader-form-cycle}}))
        _ (when (> max-depth depth-limit)
            (invoke :c2-reader-fail!
             "C2-HASH" (:path source-unit)
             {:stage :read-source
              :source-id (:source-id source-unit)
              :source-span (or (:span (first form-tree))
                               (invoke :source-span (:path source-unit) 0))
              :reader-options (:reader-options source-unit)}
             {:missing-fields [:bounded-reader-form-depth]
              :facts {:observed-form-depth max-depth
                      :maximum-form-depth depth-limit
                      :failure-kind :reader-resource-depth-limit}}))
        retain-trivia? (true? (get-in source-unit
                                      [:reader-options :retain-comments]))
        form-hash-input (if retain-trivia?
                          (c2-form-hash-input form-tree)
                          (c2-semantic-form-hash-input form-tree))
        token-hash-input (c2-token-hash-input token-stream)
        syntax-hash-input (c2-syntax-seed-hash-input syntax-seeds)
        extension-hash-input (c2-extension-hash-input extension-invocations)
        diagnostic-hash-input (c2-diagnostic-hash-input diagnostics)]
    {:artifact :gravity/reader-incremental-hashes
     :source-unit (:source-id source-unit)
     :token-stream (reader-canonical-hash token-hash-input)
     :form-tree (reader-canonical-hash form-hash-input)
     :syntax-seed-stream (reader-canonical-hash syntax-hash-input)
     :extension-invocation-set (reader-canonical-hash extension-hash-input)
     :reader-diagnostics (reader-canonical-hash diagnostic-hash-input)
     :retained-trivia-affects-form-tree? retain-trivia?
     :status :stable}))

(definterposable c2-reader-product-integrity-record :c2-reader-product-integrity-record
  [source-unit top-level-form-ids incremental-hashes literal-records
   deferred-literal-records]
  (let [literal-input
        (mapv #(update % :span c2-path-neutral-span) literal-records)
        deferred-input
        (mapv #(update % :span c2-path-neutral-span)
              deferred-literal-records)
        input
        {:source-id (:source-id source-unit)
         :source-identity-inputs (:identity-inputs source-unit)
         :source-bytes-hash (:bytes-hash source-unit)
         :reader-options (:reader-options source-unit)
         :top-level-form-ids (vec top-level-form-ids)
         :incremental-reader-hashes incremental-hashes
         :literal-records-hash (reader-canonical-hash literal-input)
         :deferred-literal-records-hash
         (reader-canonical-hash deferred-input)}
        integrity-hash (reader-canonical-hash input)]
    {:artifact :gravity/c2-reader-product-integrity
     :algorithm :sha256
     :input input
     :integrity-hash integrity-hash
     :status :verified}))

(definterposable c2-reader-artifact-id :c2-reader-artifact-id
  [artifact]
  (reader-canonical-hash
   {:kind (:kind artifact)
    :task (:task artifact)
    :document-set (:document-set artifact)
    :source-id (get-in artifact [:source-unit-record :source-id])
    :reader-product-integrity (:reader-product-integrity artifact)
    :incremental-reader-hashes (:incremental-reader-hashes artifact)
    :representation-boundary (:representation-boundary artifact)
    :source-overrides (:source-overrides artifact)
    :capability-based-proof (:capability-based-proof artifact)}))
