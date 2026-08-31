(ns gravity.c2-lexical-validation
  "Hosted Stage0 C2 UTF-8 span, form-graph, and lexical-product validation.

  This facade retains the C2 public API and operation interposition. Its
  semantic bodies live in focused namespaces below this boundary."
  (:require [gravity.c2-lexical-validation.operations :as operations]
            [gravity.c2-lexical-validation.primitives :as primitives]
            [gravity.c2-lexical-validation.product :as product]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(def ^:private operation-keys operations/operation-keys)
(def ^:private validate-operations! operations/validate-operations!)

(def ^:private namespace-contract
  {:namespace 'gravity.c2-lexical-validation
   :contract-boundary :hosted-c2-lexical-product-validation
   :public-api
   {'with-operations {:arglists '([operations thunk])}
    'c2-utf8-slice {:arglists '([source-bytes byte-start byte-end])}
    'c2-span-encloses? {:arglists '([parent child])}
    'c2-spans-source-ordered? {:arglists '([spans])}
    'c2-form-graph-metrics {:arglists '([form-tree])}
    'c2-lexical-product-validation
    {:arglists '([source-text token-stream form-tree root-form-ids])}}
   :operation-interposition
   {:accepted-keys operation-keys
    :partial-overrides? true
    :unknown-keys-rejected? true}
   :artifact-inputs [:hosted-c2-token-stream :hosted-c2-form-tree]
   :artifact-outputs [:hosted-c2-lexical-product-validation]
   :ownership
   {:owns [:hosted-c2-utf8-slice-validation
           :hosted-c2-span-validation
           :hosted-c2-form-graph-validation
           :hosted-c2-lexical-product-validation]
    :does-not-own [:canonical-c2-reader-authority
                   :sh03-reader-product-authentication
                   :source-reading
                   :tokenization
                   :form-construction
                   :complete-lexical-conformance-authority
                   :diagnostic-policy
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
   :proof-authority? false
   :self-hosted? false
   :release-authority? false})

(defn with-operations [operations thunk]
  (validate-operations! operations)
  (when-not (fn? thunk)
    (throw (ex-info "C2 lexical-validation thunk must be a function"
                    {:thunk thunk})))
  (binding [*operations* operations] (thunk)))

(defn- current-operation [key]
  (when-not (contains? *active-operation-keys* key) (get *operations* key)))

(defmacro ^:private definterposable [name key arguments & body]
  `(defn ~name ~arguments
     (if-let [operation# (current-operation ~key)]
       (binding [*active-operation-keys* (conj *active-operation-keys* ~key)]
         (operation# ~@arguments))
       (do ~@body))))

(definterposable c2-utf8-slice :c2-utf8-slice
  [source-bytes byte-start byte-end]
  (primitives/utf8-slice source-bytes byte-start byte-end))

(definterposable c2-span-encloses? :c2-span-encloses?
  [parent child]
  (primitives/span-encloses? parent child))

(definterposable c2-spans-source-ordered? :c2-spans-source-ordered?
  [spans]
  (primitives/spans-source-ordered? spans))

(definterposable c2-form-graph-metrics :c2-form-graph-metrics
  [form-tree]
  (primitives/form-graph-metrics form-tree))

(definterposable c2-lexical-product-validation :c2-lexical-product-validation
  [source-text token-stream form-tree root-form-ids]
  (product/lexical-product-validation
   {:utf8-slice c2-utf8-slice
    :span-encloses? c2-span-encloses?
    :spans-source-ordered? c2-spans-source-ordered?
    :form-graph-metrics c2-form-graph-metrics}
   source-text token-stream form-tree root-form-ids))
