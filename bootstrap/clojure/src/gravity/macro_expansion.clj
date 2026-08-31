(ns gravity.macro-expansion
  "Hosted stage0 macro-expansion engine.\n\n  This namespace owns the Clojure seed's small macro/template engine and its\n  compatibility artifact projection.  It is deliberately not the canonical\n  C4/SH-05 authority: authenticated Gravity macro expansion, self-hosting,\n  equivalence, release, proof, and seed-retirement claims remain with the\n  canonical pipeline in `gravity.bootstrap` and the Gravity source modules.\n\n  Callers may inject the seed's diagnostic, hashing, syntax, and recursive\n  operation functions.  The injection boundary keeps this leaf acyclic while\n  preserving the bootstrap wrappers' dynamic interposition behavior."
  (:require [clojure.set :as set]
            [gravity.digest :as digest]
            [gravity.macro-expansion.artifact :as artifact]
            [gravity.macro-expansion.builtins :as builtins]
            [gravity.macro-expansion.contract :as contract]
            [gravity.macro-expansion.engine :as engine]
            [gravity.macro-expansion.operations :as operations]
            [gravity.macro-expansion.policy :as policy]
            [gravity.macro-expansion.registry :as registry]
            [gravity.macro-expansion.templates :as templates]))

(def ^:private operation-keys contract/operation-keys)
(def ^:private namespace-contract contract/namespace-contract)

(defn- default-fail! [id message data]
  (throw (ex-info message (assoc (or data {}) :id id))))
(defn- default-form-op? [op form]
  (and (seq? form) (= op (first form))))
(declare default-contains-form-op?)
(defn- default-contains-form-op? [op form]
  (cond
    (default-form-op? op form) true
    (seq? form) (some #(default-contains-form-op? op %) form)
    (coll? form) (some #(default-contains-form-op? op %) form)
    :else false))
(defn- default-collect-symbols [form]
  (cond
    (symbol? form) [form]
    (seq? form) (mapcat default-collect-symbols form)
    (coll? form) (mapcat default-collect-symbols form)
    :else []))
(defn- default-local-macro-symbol [module name]
  (symbol (str (:module module)) (str name)))
(defn- default-source-span [source-path form-index]
  {:source source-path :form-index form-index})
(defn- default-ops []
  {:fail! default-fail!
   :form-op? default-form-op?
   :contains-form-op? default-contains-form-op?
   :collect-symbols default-collect-symbols
   :local-macro-symbol default-local-macro-symbol
   :source-span default-source-span
   :sha256-hex digest/sha256-hex
   :splice-key ::splice
   :max-macro-expansion-depth 16})

;; These private vars are bootstrap concurrency and dynamic-interposition seams.
(def ^:private normalized-ops-context-token (Object.))
(def ^:private ^:dynamic *normalized-ops-context* nil)

(defn- normalize-ops-uncached
  [ops]
  (when-not (or (nil? ops) (map? ops))
    (throw (ex-info "macro expansion operations must be a map"
                    {:id "STAGE0-MACRO-EXPANSION-OPERATIONS"
                     :operations ops})))
  (let [provided (or ops {})
        unexpected (set/difference (set (keys provided)) operation-keys)
        scalar-keys #{:builtin-macros :max-macro-expansion-depth :splice-key}
        non-functions (->> (keys provided)
                           (remove scalar-keys)
                           (remove #(fn? (get provided %)))
                           set)]
    (when (or (seq unexpected)
              (seq non-functions)
              (and (contains? provided :builtin-macros)
                   (not (map? (:builtin-macros provided))))
              (and (contains? provided :max-macro-expansion-depth)
                   (not (and (integer? (:max-macro-expansion-depth provided))
                             (pos? (:max-macro-expansion-depth provided))))))
      (throw (ex-info "macro expansion operations are invalid"
                      {:id "STAGE0-MACRO-EXPANSION-OPERATIONS"
                       :accepted-operation-keys operation-keys
                       :unexpected-operation-keys unexpected
                       :non-function-operation-keys non-functions
                       :operations provided})))
    (merge (default-ops) provided)))

(defn- normalize-ops
  [ops]
  (let [context *normalized-ops-context*]
    (if (and (map? context)
             (identical? normalized-ops-context-token (:token context))
             (identical? ops (:ops context)))
      ops
      (normalize-ops-uncached ops))))

(defn with-normalized-operations
  "Run `operation` with one validated operation map for this request.\n\n  The normalized map is passed to the operation and is also bound by identity\n  for nested calls.  The binding is dynamic and request-local; it is not a\n  persistent cache or an authority-bearing artifact field."
  [operations operation]
  (let [normalized (normalize-ops operations)]
    (binding [*normalized-ops-context*
              {:token normalized-ops-context-token :ops normalized}]
      (operation normalized))))

(defn- op [ops key fallback] (get ops key fallback))
(defn- fail! [ops id message data]
  ((op ops :fail! default-fail!) id message data))

(defn local-macro-symbol
  ([module name] (local-macro-symbol module name nil))
  ([module name ops]
   ((op (normalize-ops ops) :local-macro-symbol default-local-macro-symbol)
    module name)))
(defn parse-param-list
  ([params] (parse-param-list params nil))
  ([params ops] (templates/parse-param-list params (normalize-ops ops))))
(defn bind-macro-arguments
  ([macro args call-span] (bind-macro-arguments macro args call-span nil))
  ([macro args call-span ops]
   (templates/bind-macro-arguments macro args call-span (normalize-ops ops))))
(defn expand-template-items
  ([env items] (expand-template-items env items nil))
  ([env items ops]
   (templates/expand-template-items env items (normalize-ops ops))))
(defn macro-env-value
  ([env sym] (macro-env-value env sym nil))
  ([env sym ops] (templates/macro-env-value env sym (normalize-ops ops))))
(defn expand-template
  ([env template] (expand-template env template nil))
  ([env template ops]
   (templates/expand-template env template (normalize-ops ops))))
(defn parse-syntax-template
  ([macro call-span] (parse-syntax-template macro call-span nil))
  ([macro call-span ops]
   (templates/parse-syntax-template macro call-span (normalize-ops ops))))

(defn builtin-defn-output
  ([args call-span] (builtin-defn-output args call-span nil))
  ([args call-span ops]
   (builtins/defn-output args call-span (normalize-ops ops))))
(defn builtin-when-output
  ([args call-span] (builtin-when-output args call-span nil))
  ([args call-span ops]
   (builtins/when-output args call-span (normalize-ops ops))))
(defn thread-first-step [value step] (builtins/thread-first-step value step))
(defn builtin-thread-first-output
  ([args call-span] (builtin-thread-first-output args call-span nil))
  ([args call-span ops]
   (builtins/thread-first-output args call-span (normalize-ops ops))))

(def builtin-macros
  {'defn {:name 'defn :identity 'gravity.core/defn :kind :built-in
          :version "stage0-builtin" :macro-namespace 'gravity.core
          :params {:fixed '[name params] :rest 'body} :build-effects #{}
          :required-build-capabilities #{} :hygiene-policy :hygienic
          :output-contract :gravity-syntax :expander builtin-defn-output}
   'when {:name 'when :identity 'gravity.core/when :kind :built-in
          :version "stage0-builtin" :macro-namespace 'gravity.core
          :params {:fixed '[condition] :rest 'body} :build-effects #{}
          :required-build-capabilities #{} :hygiene-policy :hygienic
          :output-contract :gravity-syntax :expander builtin-when-output}
   '-> {:name '-> :identity 'gravity.core/-> :kind :built-in
        :version "stage0-builtin" :macro-namespace 'gravity.core
        :params {:fixed '[initial] :rest 'steps} :build-effects #{}
        :required-build-capabilities #{} :hygiene-policy :hygienic
        :output-contract :gravity-syntax :expander builtin-thread-first-output}})

(defn built-in-registry
  ([] (built-in-registry nil))
  ([ops] (builtins/registry builtin-macros (normalize-ops ops))))
(defn parse-defmacro-form
  ([module syntax] (parse-defmacro-form module syntax nil))
  ([module syntax ops]
   (registry/parse-defmacro-form module syntax (normalize-ops ops))))
(defn macro-registry
  ([module syntax] (macro-registry module syntax nil))
  ([module syntax ops]
   (registry/macro-registry module syntax builtin-macros
                            (normalize-ops ops))))
(defn macro-namespace-entry [macro] (registry/namespace-entry macro))
(defn macro-build-effect-record [macro] (registry/build-effect-record macro))
(defn macro-build-grants [module] (registry/build-grants module))

(defn assert-build-effects!
  ([module macro call-span] (assert-build-effects! module macro call-span nil))
  ([module macro call-span ops]
   (policy/assert-build-effects! module macro call-span (normalize-ops ops))))
(defn collect-let-bindings
  ([form] (collect-let-bindings form nil))
  ([form ops] (policy/collect-let-bindings form (normalize-ops ops))))
(defn assert-hygiene!
  ([macro args output call-span]
   (assert-hygiene! macro args output call-span nil))
  ([macro args output call-span ops]
   (policy/assert-hygiene! macro args output call-span (normalize-ops ops))))
(defn assert-generated-profile!
  ([module macro output call-span]
   (assert-generated-profile! module macro output call-span nil))
  ([module macro output call-span ops]
   (policy/assert-generated-profile! module macro output call-span
                                     (normalize-ops ops))))
(defn assert-generated-unsafe!
  ([module macro output call-span]
   (assert-generated-unsafe! module macro output call-span nil))
  ([module macro output call-span ops]
   (policy/assert-generated-unsafe! module macro output call-span
                                    (normalize-ops ops))))
(defn expand-macro-form
  ([module macro args call-span] (expand-macro-form module macro args call-span nil))
  ([module macro args call-span ops]
   (policy/expand-macro-form module macro args call-span (normalize-ops ops))))

(defn expansion-generated-origin
  ([macro syntax input output]
   (expansion-generated-origin macro syntax input output nil))
  ([macro syntax input output ops]
   (engine/generated-origin macro syntax input output (normalize-ops ops))))
(defn macro-call [registry form] (engine/macro-call registry form))
(defn expand-child-form
  ([registry module syntax form trace depth]
   (expand-child-form registry module syntax form trace depth nil))
  ([registry module syntax form trace depth ops]
   (engine/expand-child-form registry module syntax form trace depth
                             (normalize-ops ops))))
(defn expand-form-children
  ([registry module syntax form trace depth]
   (expand-form-children registry module syntax form trace depth nil))
  ([registry module syntax form trace depth ops]
   (engine/expand-form-children registry module syntax form trace depth
                                (normalize-ops ops))))
(defn expansion-trace-record
  ([module macro syntax input output generated-origin depth]
   (expansion-trace-record module macro syntax input output generated-origin depth nil))
  ([module macro syntax input output generated-origin depth ops]
   (engine/trace-record module macro syntax input output generated-origin depth
                        (normalize-ops ops))))
(defn distinct-by-pr-str [values] (engine/distinct-by-pr-str values))
(defn expand-syntax-object
  ([registry module syntax trace depth]
   (expand-syntax-object registry module syntax trace depth nil))
  ([registry module syntax trace depth ops]
   (engine/expand-syntax-object registry module syntax trace depth
                                (normalize-ops ops))))
(defn macro-source-artifact-from-records
  ([source-path source-text records module syntax]
   (macro-source-artifact-from-records source-path source-text records module syntax nil))
  ([source-path source-text records module syntax ops]
   (artifact/from-records source-path source-text records module syntax
                          builtin-macros (normalize-ops ops))))
