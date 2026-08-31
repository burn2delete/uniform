(ns gravity.c2-reader-diagnostics
  "Hosted Stage0 C2 reader diagnostic catalog, payload policy, and override routing.

  This leaf owns compatibility diagnostics only. It does not read source,
  authenticate C2 or SH03 products, or grant proof, cache-reuse, self-hosting,
  attestation, or release authority."
  (:require [gravity.c2-reader-diagnostics.catalog :as catalog]
            [gravity.c2-reader-diagnostics.operations :as operations]
            [gravity.c2-reader-diagnostics.overrides :as overrides]
            [gravity.c2-reader-diagnostics.payload :as payload]
            [gravity.c2-reader-diagnostics.remap :as remap]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(def ^:private ^:dynamic *bypass-next-operation-keys* #{})
(def ^:private function-operation-keys operations/function-operation-keys)
(def ^:private scalar-operation-keys operations/scalar-operation-keys)
(def ^:private operation-keys operations/operation-keys)
(def ^:private valid-string-vector? operations/valid-string-vector?)
(def ^:private valid-rejected-designs? operations/valid-rejected-designs?)
(def ^:private valid-override-map? operations/valid-override-map?)
(def ^:private valid-standard-reader-options? operations/valid-standard-reader-options?)
(def ^:private validate-operations! operations/validate-operations!)

(def ^:private namespace-contract
  {:namespace 'gravity.c2-reader-diagnostics
   :contract-boundary :hosted-c2-reader-diagnostic-policy
   :public-api
   {'c2-reader-diagnostic-ids {:kind :constant}
    'c2-reader-governing-document {:kind :constant}
    'c2-reader-rejected-designs {:kind :constant}
    'c2-reader-override-diagnostics {:kind :constant}
    'with-operations {:arglists '([operations thunk])}
    'call-entrypoint-body {:arglists '([operation-key operation args])}
    'c2-reader-source-overrides {:arglists '([module])}
    'c2-reader-message {:arglists '([id])}
    'c2-reader-fail! {:arglists '([id source-path subject extra])}
    'c2-reader-remap-exception! {:arglists '([source-path ex])}
    'c2-reader-validate-overrides!
    {:arglists '([source-path overrides source-unit token-stream])}}
   :operation-interposition
   {:accepted-keys operation-keys
    :partial-overrides? true
    :unknown-keys-rejected? true
    :function-values-must-satisfy :fn?
    :entrypoint-requirements
    {'c2-reader-fail! #{:fail! :source-span :reader-canonical-hash}
     'c2-reader-remap-exception!
     #{:c2-reader-fail! :standard-reader-options}
     'c2-reader-validate-overrides! #{:c2-reader-fail!}}}
   :ownership
   {:owns [:hosted-c2-reader-diagnostic-catalog
           :hosted-c2-reader-diagnostic-payload-policy
           :hosted-c2-reader-fixture-override-routing
           :hosted-c2-reader-exception-remapping]
    :does-not-own [:canonical-c2-reader-authority
                   :canonical-c2-reader-product-authority
                   :sh03-reader-product-authentication
                   :source-reading
                   :source-authentication
                   :canonical-source-identity
                   :cache-reuse-authority
                   :diagnostic-policy-authority
                   :proof-authority
                   :attestation-authority
                   :self-hosted-authority
                   :release-authority]}
   :dependency-direction
   {:requires ['clojure.core 'clojure.string]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :canonical-c2-authority? false
   :source-authentication? false
   :cache-reuse-authority? false
   :proof-authority? false
   :self-hosted? false
   :release-authority? false})

(defn with-operations [operations thunk]
  (validate-operations! operations)
  (when-not (fn? thunk)
    (throw (ex-info "C2 reader diagnostics thunk must be a function" {:thunk thunk})))
  (binding [*operations* operations] (thunk)))

(defn call-entrypoint-body
  "Enter one extracted function body while retaining recursive bootstrap Var
  interposition. This is the narrow compatibility trampoline used by
  bootstrap wrappers; ordinary leaf callers should use with-operations."
  [operation-key operation args]
  (when-not (contains? operation-keys operation-key)
    (throw (ex-info "C2 reader diagnostics entrypoint key is unknown"
                    {:operation operation-key})))
  (when-not (fn? operation)
    (throw (ex-info "C2 reader diagnostics entrypoint must be a function"
                    {:operation operation-key :value operation})))
  (when-not (sequential? args)
    (throw (ex-info "C2 reader diagnostics entrypoint args must be sequential"
                    {:operation operation-key :args args})))
  (binding [*active-operation-keys* (disj *active-operation-keys* operation-key)
            *bypass-next-operation-keys* (conj *bypass-next-operation-keys* operation-key)]
    (apply operation args)))

(defn- current-operation [key]
  (when-not (contains? *active-operation-keys* key) (get *operations* key)))

(defn- invoke [key & args]
  (if-let [operation (current-operation key)]
    (apply operation args)
    (throw (ex-info (str "C2 reader diagnostics require operation " key)
                    {:operation key}))))

(defn- operation-value [key default]
  (if (contains? *operations* key) (get *operations* key) default))

(defn- required-operation-value [key]
  (if (contains? *operations* key)
    (get *operations* key)
    (throw (ex-info (str "C2 reader diagnostics require operation " key)
                    {:operation key}))))

(defmacro ^:private definterposable [name key arguments & body]
  `(defn ~name ~arguments
     (if (contains? *bypass-next-operation-keys* ~key)
       (binding [*bypass-next-operation-keys* (disj *bypass-next-operation-keys* ~key)]
         (do ~@body))
       (if-let [operation# (current-operation ~key)]
         (binding [*active-operation-keys* (conj *active-operation-keys* ~key)]
           (operation# ~@arguments))
         (do ~@body)))))

(def c2-reader-diagnostic-ids catalog/diagnostic-ids)
(def c2-reader-governing-document catalog/governing-document)
(def c2-reader-rejected-designs catalog/rejected-designs)
(def c2-reader-override-diagnostics catalog/override-diagnostics)

(definterposable c2-reader-source-overrides :c2-reader-source-overrides [module]
  (catalog/source-overrides module))

(definterposable c2-reader-message :c2-reader-message [id]
  (catalog/message id))

(definterposable c2-reader-fail! :c2-reader-fail! [id source-path subject extra]
  (payload/fail!
   {:source-span (fn [path offset] (invoke :source-span path offset))
    :reader-canonical-hash (fn [value] (invoke :reader-canonical-hash value))
    :governing-document (operation-value :c2-reader-governing-document
                                         c2-reader-governing-document)
    :message c2-reader-message
    :terminal-fail! (fn [failure-id message data] (invoke :fail! failure-id message data))}
   id source-path subject extra))

(definterposable c2-reader-remap-exception! :c2-reader-remap-exception! [source-path ex]
  (remap/remap-exception!
   {:diagnostic-ids (operation-value :c2-reader-diagnostic-ids c2-reader-diagnostic-ids)
    :standard-reader-options (required-operation-value :standard-reader-options)
    :fail! c2-reader-fail!}
   source-path ex))

(definterposable c2-reader-validate-overrides! :c2-reader-validate-overrides!
  [source-path overrides source-unit token-stream]
  (overrides/validate-overrides!
   {:override-diagnostics (operation-value :c2-reader-override-diagnostics
                                            c2-reader-override-diagnostics)
    :fail! c2-reader-fail!}
   source-path overrides source-unit token-stream))
