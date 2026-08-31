(ns gravity.c2-source-identity
  "Hosted Stage0 C2 source identity and reader-record projections.

  This facade preserves C2's public API and recursive bootstrap interposition.
  Focused semantic namespaces hold path, identity, and reader-record bodies."
  (:require [clojure.string :as str]
            [gravity.c2-source-identity.identity]
            [gravity.c2-source-identity.operations]
            [gravity.c2-source-identity.paths]
            [gravity.c2-source-identity.records]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(def ^:private ^:dynamic *bypass-next-operation-keys* #{})
(def ^:private function-operation-keys
  gravity.c2-source-identity.operations/function-operation-keys)
(def ^:private operation-keys gravity.c2-source-identity.operations/operation-keys)
(def ^:private validate-operations!
  gravity.c2-source-identity.operations/validate-operations!)

(def ^:private namespace-contract
  {:namespace 'gravity.c2-source-identity
   :contract-boundary :hosted-c2-source-identity-and-reader-record-projection
   :public-api
   {'with-operations {:arglists '([operations thunk])}
    'call-entrypoint-body {:arglists '([operation-key operation args])}
    'reader-normalize-relative-path {:arglists '([path])}
    'reader-platform-neutral-absolute-path? {:arglists '([path])}
    'reader-valid-project-relative-path? {:arglists '([path])}
    'reader-explicit-project-context {:arglists '([project-context])}
    'reader-valid-options? {:arglists '([reader-options])}
    'reader-validate-options! {:arglists '([reader-options])}
    'reader-project-root-record {:arglists '([project-context])}
    'reader-source-identity-inputs
    {:arglists '([source-text reader-options project-context])}
    'c2-source-unit-record
    {:arglists '([source-path source-text reader-options project-context])}
    'c2-token-record {:arglists '([token source-unit])}
    'c2-form-record {:arglists '([record source-unit])}
    'c2-literal-records {:arglists '([form-tree])}
    'c2-trivia-records {:arglists '([token-stream])}}
   :operation-interposition
   {:accepted-keys operation-keys
    :partial-overrides? true
    :unknown-keys-rejected? true
    :function-values-must-satisfy :fn?
    :entrypoint-requirements
    {'reader-source-identity-inputs #{:sha256-hex}
     'c2-source-unit-record
     #{:sha256-hex :reader-canonical-hash
       :gravity-source-extension :gravity-source-kind}}}
   :artifact-inputs [:explicit-project-context :source-text :reader-options
                     :source-path-provenance :hosted-c2-token-records
                     :hosted-c2-form-records]
   :artifact-outputs [:hosted-c2-source-unit :hosted-c2-token-records
                      :hosted-c2-form-records :hosted-c2-literal-records
                      :hosted-c2-trivia-records]
   :ownership
   {:owns [:hosted-c2-project-relative-identity-normalization
           :hosted-c2-reader-options-validation
           :hosted-c2-source-unit-record-projection
           :hosted-c2-token-record-projection
           :hosted-c2-form-record-projection
           :hosted-c2-literal-record-projection
           :hosted-c2-trivia-record-projection]
    :does-not-own [:filesystem-project-root-discovery
                   :project-root-authority :source-reading :source-byte-decoding
                   :source-extension-policy :source-authentication
                   :reader-tokenization :reader-form-construction
                   :canonical-c2-reader-authority
                   :canonical-c2-reader-product-authority
                   :sh03-reader-product-authentication :diagnostic-policy
                   :diagnostic-policy-authority :cache-reuse-authority
                   :proof-authority :attestation-authority :self-hosted-authority
                   :release-authority]}
   :dependency-direction
   {:requires ['clojure.core 'clojure.string]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :canonical-c2-authority? false
   :project-root-authority? false
   :source-reading? false
   :source-authentication? false
   :proof-authority? false
   :self-hosted? false
   :release-authority? false})

(defn with-operations
  [operations thunk]
  (validate-operations! operations)
  (when-not (fn? thunk)
    (throw (ex-info "C2 source-identity thunk must be a function" {:thunk thunk})))
  (binding [*operations* operations] (thunk)))

(defn call-entrypoint-body
  "Enter one extracted function body with one-shot bootstrap interposition."
  [operation-key operation args]
  (when-not (contains? operation-keys operation-key)
    (throw (ex-info "C2 source-identity entrypoint key is unknown"
                    {:operation operation-key})))
  (when-not (fn? operation)
    (throw (ex-info "C2 source-identity entrypoint must be a function"
                    {:operation operation-key :value operation})))
  (when-not (sequential? args)
    (throw (ex-info "C2 source-identity entrypoint args must be sequential"
                    {:operation operation-key :args args})))
  (binding [*active-operation-keys* (disj *active-operation-keys* operation-key)
            *bypass-next-operation-keys* (conj *bypass-next-operation-keys* operation-key)]
    (apply operation args)))

(defn- current-operation [key]
  (when-not (contains? *active-operation-keys* key) (get *operations* key)))

(defn- invoke [key & args]
  (if-let [operation (current-operation key)]
    (apply operation args)
    (throw (ex-info (str "C2 source-identity requires operation " key)
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

(definterposable reader-normalize-relative-path :reader-normalize-relative-path [path]
  (gravity.c2-source-identity.paths/normalize-relative-path path))

(definterposable reader-platform-neutral-absolute-path?
  :reader-platform-neutral-absolute-path? [path]
  (gravity.c2-source-identity.paths/platform-neutral-absolute-path? path))

(definterposable reader-valid-project-relative-path?
  :reader-valid-project-relative-path? [path]
  (gravity.c2-source-identity.paths/valid-project-relative-path?
   reader-normalize-relative-path reader-platform-neutral-absolute-path? path))

(definterposable reader-explicit-project-context :reader-explicit-project-context [project-context]
  (gravity.c2-source-identity.identity/explicit-project-context
   reader-normalize-relative-path reader-valid-project-relative-path? project-context))

(definterposable reader-valid-options? :reader-valid-options? [reader-options]
  (gravity.c2-source-identity.identity/valid-options? reader-options))

(definterposable reader-validate-options! :reader-validate-options! [reader-options]
  (gravity.c2-source-identity.identity/validate-options!
   reader-valid-options? reader-options))

(definterposable reader-project-root-record :reader-project-root-record [project-context]
  (gravity.c2-source-identity.identity/project-root-record
   reader-explicit-project-context project-context))

(definterposable reader-source-identity-inputs
  :reader-source-identity-inputs [source-text reader-options project-context]
  (gravity.c2-source-identity.identity/source-identity-inputs
   reader-explicit-project-context reader-validate-options! #(invoke :sha256-hex %)
   source-text reader-options project-context))

(definterposable c2-source-unit-record
  :c2-source-unit-record [source-path source-text reader-options project-context]
  (gravity.c2-source-identity.records/source-unit-record
   {:explicit-project-context reader-explicit-project-context
    :reader-source-identity-inputs reader-source-identity-inputs
    :reader-project-root-record reader-project-root-record
    :reader-canonical-hash #(invoke :reader-canonical-hash %)
    :gravity-source-extension #(invoke :gravity-source-extension %)
    :gravity-source-kind #(invoke :gravity-source-kind %)}
   source-path source-text reader-options project-context))

(definterposable c2-token-record :c2-token-record [token source-unit]
  (gravity.c2-source-identity.records/token-record token source-unit))

(definterposable c2-form-record :c2-form-record [record source-unit]
  (gravity.c2-source-identity.records/form-record record source-unit))

(definterposable c2-literal-records :c2-literal-records [form-tree]
  (gravity.c2-source-identity.records/literal-records form-tree))

(definterposable c2-trivia-records :c2-trivia-records [token-stream]
  (gravity.c2-source-identity.records/trivia-records token-stream))
