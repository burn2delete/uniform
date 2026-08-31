(ns gravity.c2-artifact-identity
  "Hosted Stage0 C2 canonical hashing and content-addressed artifact identity.

  This facade retains C2's public API and recursive bootstrap interposition.
  Its semantic bodies live in focused namespaces below this boundary."
  (:require [gravity.c2-artifact-identity.artifacts :as artifacts]
            [gravity.c2-artifact-identity.canonical :as canonical]
            [gravity.c2-artifact-identity.incremental :as incremental]
            [gravity.c2-artifact-identity.operations :as operations]
            [gravity.c2-artifact-identity.projections :as projections]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(def ^:private ^:dynamic *bypass-next-operation-keys* #{})
(def ^:private function-operation-keys operations/function-operation-keys)
(def ^:private scalar-operation-keys operations/scalar-operation-keys)
(def ^:private operation-keys operations/operation-keys)
(def ^:private validate-operations! operations/validate-operations!)

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
  (binding [*active-operation-keys* (disj *active-operation-keys* operation-key)
            *bypass-next-operation-keys* (conj *bypass-next-operation-keys* operation-key)]
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
         (binding [*active-operation-keys* (conj *active-operation-keys* ~key)]
           (operation# ~@arguments))
         (do ~@body)))))

(definterposable reader-canonical-value :reader-canonical-value [value]
  (canonical/reader-canonical-value reader-canonical-value value))

(definterposable reader-canonical-hash :reader-canonical-hash [value]
  (canonical/reader-canonical-hash #(invoke :sha256-hex %) reader-canonical-value value))

(definterposable c2-semantic-form-hash-input :c2-semantic-form-hash-input [form-tree]
  (projections/semantic-form-hash-input form-tree))

(definterposable c2-path-neutral-span :c2-path-neutral-span [span]
  (projections/path-neutral-span span))

(definterposable c2-token-hash-input :c2-token-hash-input [token-stream]
  (projections/token-hash-input c2-path-neutral-span token-stream))

(definterposable c2-form-hash-input :c2-form-hash-input [form-tree]
  (projections/form-hash-input c2-path-neutral-span form-tree))

(definterposable c2-syntax-seed-hash-input :c2-syntax-seed-hash-input [syntax-seeds]
  (projections/syntax-seed-hash-input c2-path-neutral-span syntax-seeds))

(definterposable c2-extension-hash-input :c2-extension-hash-input [extension-invocations]
  (projections/extension-hash-input extension-invocations))

(definterposable c2-diagnostic-hash-input :c2-diagnostic-hash-input [diagnostics]
  (projections/diagnostic-hash-input c2-path-neutral-span diagnostics))

(definterposable c2-incremental-hashes :c2-incremental-hashes
  [source-unit token-stream form-tree syntax-seeds extension-invocations diagnostics]
  (incremental/hashes
   {:c2-form-graph-metrics #(invoke :c2-form-graph-metrics %)
    :c2-reader-fail! (fn [& args] (apply invoke :c2-reader-fail! args))
    :source-span #(invoke :source-span %1 %2)
    :max-reader-form-graph-depth (operation-value :max-reader-form-graph-depth)
    :c2-semantic-form-hash-input c2-semantic-form-hash-input
    :c2-token-hash-input c2-token-hash-input
    :c2-form-hash-input c2-form-hash-input
    :c2-syntax-seed-hash-input c2-syntax-seed-hash-input
    :c2-extension-hash-input c2-extension-hash-input
    :c2-diagnostic-hash-input c2-diagnostic-hash-input
    :reader-canonical-hash reader-canonical-hash}
   source-unit token-stream form-tree syntax-seeds extension-invocations diagnostics))

(definterposable c2-reader-product-integrity-record :c2-reader-product-integrity-record
  [source-unit top-level-form-ids incremental-hashes literal-records deferred-literal-records]
  (artifacts/reader-product-integrity-record
   {:c2-path-neutral-span c2-path-neutral-span
    :reader-canonical-hash reader-canonical-hash}
   source-unit top-level-form-ids incremental-hashes literal-records deferred-literal-records))

(definterposable c2-reader-artifact-id :c2-reader-artifact-id [artifact]
  (artifacts/reader-artifact-id reader-canonical-hash artifact))
