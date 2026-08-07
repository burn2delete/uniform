(ns gravity.reader-namespace
  "Stage 0 namespace-form syntax and reader module-context projection.

  This bootstrap-hosted leaf validates only the L1 namespace form shape and
  projects already-read clauses into reader context. It is not the canonical
  C2 reader, the L3 namespace semantic analyzer, or self-hosted authority."
  (:require [gravity.diagnostics :as diagnostics]
            [gravity.reader-primitives :as reader-primitives]
            [gravity.source-span :as source-span]))

(def ^:private namespace-contract
  {:namespace 'gravity.reader-namespace
   :contract-boundary :stage0-reader-namespace-shape-and-context
   :public-api
   {'allowed-ns-clauses {:value :fixed-l1-namespace-clause-key-set}
    'ns-form? {:arglists '([form]) :returns :boolean}
    'fail-ns-shape!
    {:arglists '([source-path clause remediation]
                 [source-path clause remediation operations])
     :default-operations-throw :clojure.lang/exception-info
     :interposed-operations-return :operation-defined}
    'validate-ns-syntax!
    {:arglists '([source-path forms] [source-path forms operations])
     :returns :nil}
    'reader-module-context
    {:arglists '([forms] [forms operations])
     :returns :reader-module-context-or-nil}}
   :artifact-inputs [:already-read-forms :source-path]
   :artifact-outputs [:l1-namespace-shape-diagnostic :reader-module-context]
   :ownership
   {:owns [:stage0-l1-namespace-shape-validation
           :l1-allowed-namespace-clause-keys
           :stage0-reader-module-context-projection
           :namespace-clause-syntax-projection]
    :does-not-own [:source-reading
                   :canonical-c2-reader-authority
                   :l3-namespace-semantic-analysis
                   :l3-namespace-policy
                   :bootstrap-orchestration
                   :self-hosted-authority]}
   :dependency-direction
   {:requires ['gravity.diagnostics
               'gravity.reader-primitives
               'gravity.source-span]
    :forbids ['gravity.bootstrap]}
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :canonical-c2-reader? false
   :l3-semantic-analyzer? false
   :self-hosted? false})

(def allowed-ns-clauses
  #{:profile :profiles :target :targets :requires :imports :exports :effects
    :capabilities :safety :providers :doc :metadata})

(defn- validated-operations
  [operation-name expected operations]
  (when-not (map? operations)
    (throw (ex-info (str operation-name " operations must be a map")
                    {:operation operation-name
                     :expected-keys expected
                     :operations operations})))
  (when-not (= expected (set (keys operations)))
    (throw (ex-info (str operation-name " operations must have exact keys")
                    {:operation operation-name
                     :expected-keys expected
                     :actual-keys (set (keys operations))})))
  (doseq [[key operation] operations]
    (when-not (fn? operation)
      (throw (ex-info (str operation-name " operation must be a function")
                      {:operation operation-name
                       :operation-key key
                       :operation-value operation}))))
  operations)

(defn ns-form?
  [form]
  (and (seq? form) (= 'ns (first form))))

(defn fail-ns-shape!
  ([source-path clause remediation]
   (fail-ns-shape! source-path clause remediation
                   {:source-span source-span/source-span
                    :fail! diagnostics/fail!}))
  ([source-path clause remediation operations]
   (let [{span :source-span fail :fail!}
         (validated-operations 'fail-ns-shape!
                               #{:source-span :fail!}
                               operations)]
     (fail "L1-NS-SHAPE"
           "namespace clause has invalid reader syntax shape"
           {:source-span (span source-path 0)
            :clause clause
            :remediation remediation}))))

(defn validate-ns-syntax!
  ([source-path forms]
   (validate-ns-syntax! source-path forms
                        {:ns-form? ns-form?
                         :allowed-ns-clause? #(contains? allowed-ns-clauses %)
                         :fail-ns-shape! fail-ns-shape!}))
  ([source-path forms operations]
   (let [{namespace-form? :ns-form?
          allowed-clause? :allowed-ns-clause?
          fail-shape! :fail-ns-shape!}
         (validated-operations 'validate-ns-syntax!
                               #{:ns-form? :allowed-ns-clause?
                                 :fail-ns-shape!}
                               operations)]
     (when-let [form (first forms)]
       (when (namespace-form? form)
         (when-not (symbol? (second form))
           (fail-shape! source-path form "Use a symbolic namespace name."))
         (doseq [clause (drop 2 form)]
           (when-not (and (seq? clause) (keyword? (first clause)))
             (fail-shape! source-path clause
                          "Use list clauses such as (:profile :hosted)."))
           (let [key (first clause)
                 args (vec (rest clause))
                 one? (= 1 (count args))
                 value (first args)]
             (when-not (allowed-clause? key)
               (fail-shape! source-path clause
                            "Use one of the L1 allowed namespace clause keys."))
             (case key
               (:profile :target :safety)
               (when-not one?
                 (fail-shape! source-path clause
                              "Use exactly one value in this namespace clause."))
               (:profiles :targets :effects :capabilities)
               (when-not (and one? (set? value))
                 (fail-shape! source-path clause
                              "Use exactly one set value in this namespace clause."))
               (:exports :providers)
               (when-not (and one? (vector? value))
                 (fail-shape! source-path clause
                              "Use exactly one vector value in this namespace clause."))
               (:requires :imports)
               (when-not (and (seq args) (every? vector? args))
                 (fail-shape! source-path clause
                              "Use one or more dependency vector values in this namespace clause."))
               :doc
               (when-not (and one? (string? value))
                 (fail-shape! source-path clause
                              "Use exactly one string value in the doc clause."))
               :metadata
               (when-not (and one? (map? value))
                 (fail-shape! source-path clause
                              "Use exactly one map value in the metadata clause."))))))))))

(defn reader-module-context
  ([forms]
   (reader-module-context forms
                          {:ns-form? ns-form?
                           :form-kind reader-primitives/form-kind}))
  ([forms operations]
   (let [{namespace-form? :ns-form? kind-of :form-kind}
         (validated-operations 'reader-module-context
                               #{:ns-form? :form-kind}
                               operations)
         form (first forms)]
     (when (namespace-form? form)
       (let [clauses (reduce (fn [acc clause]
                               (assoc acc (first clause) (vec (rest clause))))
                             {}
                             (drop 2 form))]
         {:module (second form)
          :profile (first (get clauses :profile))
          :target (or (first (get clauses :target)) :jvm)
          :effects (or (first (get clauses :effects)) #{})
          :capabilities (or (first (get clauses :capabilities)) #{})
          :safety (first (get clauses :safety))
          :namespace-clause-syntax
          (mapv (fn [clause]
                  {:clause (first clause)
                   :raw-form-kind (kind-of clause)
                   :value-kinds (mapv kind-of (rest clause))})
                (drop 2 form))})))))
