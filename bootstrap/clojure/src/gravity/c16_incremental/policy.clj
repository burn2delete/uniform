(ns gravity.c16-incremental.policy)

(def function-operation-keys
  #{:fail! :source-span :sha256-hex :c4-artifact-id :perf-present?
    :read-source-form-records :validate-ns-syntax! :parse-module
    :compiler-c15-diagnostics-source-artifact
    :c16-incremental-source-overrides :c16-incremental-fail!
    :c16-incremental-validate-source-overrides! :c16-stage-cache-key
    :c16-incremental-diagnostic-stream :c16-incremental-validate!
    :c16-incremental-capability-proof
    :compiler-c16-incremental-source-artifact
    :compiler-c16-incremental-file-artifact})

(def scalar-operation-keys
  #{:compiler-verification-diagnostic-messages
    :compiler-verification-override-diagnostics
    :c16-incremental-governing-document
    :c16-incremental-diagnostic-ids
    :c16-cache-key-required-fields
    :c16-invalidation-causes})

(def operation-keys
  (into function-operation-keys scalar-operation-keys))

(defn unsupported [key]
  (fn [& _]
    (throw (ex-info (str "C16 leaf requires injected operation " key)
                    {:operation key}))))

(defn- string-vector? [value]
  (and (vector? value) (seq value) (every? string? value)))

(defn- keyword-vector? [value]
  (and (vector? value) (seq value) (every? keyword? value)))

(defn- string-map? [value]
  (and (map? value)
       (every? (fn [[key entry]] (and (string? key) (string? entry))) value)))

(defn- override-map? [value]
  (and (map? value)
       (every? (fn [[key entry]]
                 (and (keyword? key) (vector? entry) (= 2 (count entry))
                      (string? (first entry)) (keyword? (second entry))))
               value)))

(defn validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "C16 operation map must be a map" {:value operations})))
  (let [unknown (seq (remove operation-keys (keys operations)))
        invalid (seq (for [[key value]
                           (select-keys operations function-operation-keys)
                           :when (not (fn? value))]
                       key))]
    (when unknown
      (throw (ex-info "C16 operation map contains unknown keys"
                      {:unknown-keys (vec unknown)})))
    (when invalid
      (throw (ex-info "C16 function operations must be functions"
                      {:non-function-keys (vec invalid)}))))
  (doseq [[key predicate]
          [[:compiler-verification-diagnostic-messages string-map?]
           [:compiler-verification-override-diagnostics override-map?]
           [:c16-incremental-governing-document #(and (string? %) (seq %))]
           [:c16-incremental-diagnostic-ids string-vector?]
           [:c16-cache-key-required-fields keyword-vector?]
           [:c16-invalidation-causes keyword-vector?]]
          :when (and (contains? operations key)
                     (not (predicate (get operations key))))]
    (throw (ex-info "C16 scalar operation has invalid shape" {:key key})))
  operations)

(defn engine-contract [public-api]
  {:contract-boundary :hosted-stage0-c16-incremental-evidence
   :dependency-direction
   {:requires ['clojure.set 'clojure.string
               'gravity.compiler-verification-shared 'gravity.digest]
    :forbids ['gravity.bootstrap 'gravity.diagnostics 'gravity.c2-pass-cache]}
   :owns [:hosted-stage0-c16-cache-schema
          :hosted-stage0-c16-invalidation-evidence]
   :does-not-own [:canonical-c16-authority :source-authentication
                  :content-addressed-pass-cache :cache-storage :cache-lookup
                  :cache-publication :actual-artifact-reuse
                  :proof-freshness-authority :release-reproducibility-proof
                  :equivalence :self-hosting :release :seed-retirement]
   :compatibility-only? true
   :cache-implementation? false
   :incremental-model-complete? false
   :canonical-c16-authority? false
   :operation-interposition
   {:accepted-keys operation-keys
    :unknown-keys-rejected? true
    :partial-overrides? true
    :single-binding-per-top-level-call? true}
   :public-api public-api})
