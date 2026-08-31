(ns gravity.c17-plugin.policy
  "Operation-map and namespace-boundary policy for the C17 compatibility leaf.")

(def function-operation-keys
  #{:fail! :source-span :sha256-hex :c4-artifact-id
    :read-source-form-records :validate-ns-syntax! :parse-module
    :compiler-c16-incremental-source-artifact
    :c17-plugin-source-overrides :c17-plugin-fail!
    :c17-plugin-validate-source-overrides! :c17-plugin-diagnostic-stream
    :c17-plugin-validate! :c17-plugin-capability-proof
    :compiler-c17-plugin-source-artifact
    :compiler-c17-plugin-file-artifact})

(def scalar-operation-keys
  #{:compiler-verification-diagnostic-messages
    :compiler-verification-override-diagnostics
    :c17-plugin-governing-document :c17-plugin-diagnostic-ids
    :c17-plugin-manifest-required-fields
    :c17-plugin-pass-contract-required-fields
    :c17-plugin-cache-key-required-fields})

(def operation-keys
  (into function-operation-keys scalar-operation-keys))

(def namespace-contract
  {:contract-boundary :hosted-stage0-c17-plugin-evidence
   :dependency-direction
   {:requires ['clojure.set 'clojure.string
               'gravity.compiler-verification-shared 'gravity.digest]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :owns [:hosted-stage0-c17-plugin-schema
          :hosted-stage0-c17-plugin-evidence]
   :does-not-own [:canonical-c17-authority :source-authentication
                  :plugin-discovery :plugin-loading :plugin-execution
                  :sandbox-enforcement :package-trust-authority
                  :compiler-capability-grants :build-effect-authority
                  :signature-verification :proof-authority
                  :equivalence :self-hosting :release :seed-retirement]
   :compatibility-only? true
   :plugin-runtime-implementation? false
   :plugin-model-complete? false
   :canonical-c17-authority? false
   :operation-interposition
   {:accepted-keys operation-keys
    :unknown-keys-rejected? true
    :partial-overrides? true
    :single-binding-per-top-level-call? true}})

(defn unsupported [key]
  (fn [& _]
    (throw (ex-info (str "C17 leaf requires injected operation " key)
                    {:operation key}))))

(defn string-vector? [value]
  (and (vector? value) (seq value) (every? string? value)))

(defn keyword-vector? [value]
  (and (vector? value) (seq value) (every? keyword? value)))

(defn string-map? [value]
  (and (map? value)
       (every? (fn [[key entry]] (and (string? key) (string? entry))) value)))

(defn override-map? [value]
  (and (map? value)
       (every? (fn [[key entry]]
                 (and (keyword? key) (vector? entry) (= 2 (count entry))
                      (string? (first entry)) (keyword? (second entry))))
               value)))

(defn validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "C17 operation map must be a map" {:value operations})))
  (let [unknown (seq (remove operation-keys (keys operations)))
        invalid (seq (for [[key value]
                           (select-keys operations function-operation-keys)
                           :when (not (fn? value))]
                       key))]
    (when unknown
      (throw (ex-info "C17 operation map contains unknown keys"
                      {:unknown-keys (vec unknown)})))
    (when invalid
      (throw (ex-info "C17 function operations must be functions"
                      {:non-function-keys (vec invalid)}))))
  (doseq [[key predicate]
          [[:compiler-verification-diagnostic-messages string-map?]
           [:compiler-verification-override-diagnostics override-map?]
           [:c17-plugin-governing-document #(and (string? %) (seq %))]
           [:c17-plugin-diagnostic-ids string-vector?]
           [:c17-plugin-manifest-required-fields keyword-vector?]
           [:c17-plugin-pass-contract-required-fields keyword-vector?]
           [:c17-plugin-cache-key-required-fields keyword-vector?]]
          :when (and (contains? operations key)
                     (not (predicate (get operations key))))]
    (throw (ex-info "C17 scalar operation has invalid shape" {:key key})))
  operations)
