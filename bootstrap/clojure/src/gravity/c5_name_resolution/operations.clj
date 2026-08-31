(ns gravity.c5-name-resolution.operations
  (:require [gravity.digest :as digest]))

(def ^:dynamic *operations* {})

(def operation-keys
  #{:fail! :source-span :sha256-hex :c4-artifact-id :read-source-form-records
    :validate-ns-syntax! :parse-module :module-source-artifact
    :compiler-c4-macro-source-artifact :collect-code-symbols :ns-form?
    :known-source-profiles :supported-targets :c5-resolution-diagnostic-ids
    :c5-resolution-governing-document :c5-resolution-rejected-designs
    :c5-resolution-override-diagnostics :c5-special-form-symbols
    :c5-core-auto-imports :c5-type-auto-imports :c5-resolution-source-overrides
    :c5-resolution-message :c5-resolution-fail! :c5-resolution-validate-overrides!
    :compiler-c5-resolution-source-artifact :c5-package-record :c5-binding-id
    :c5-binding-identity :c5-definition-binding :c5-special-form-binding
    :c5-core-binding :c5-type-binding :c5-import-binding :c5-alias-table
    :c5-import-export-table :c5-definition-bindings :c5-macro-bindings
    :c5-param-symbols :c5-local-bindings-from-params :c5-let-binding-symbols
    :c5-local-scope-graph :c5-bindings-by-name :c5-resolve-qualified-symbol
    :c5-resolution-record :c5-binding-table :c5-namespace-analysis-artifact
    :c5-dependency-graph :c5-cross-profile-edge-report
    :c5-incremental-invalidation-keys :c5-resolution-diagnostics
    :c5-resolution-verification-report :c5-resolution-capability-proof
    :c5-resolution-validate!})

(def function-operation-keys
  (apply disj operation-keys
         [:known-source-profiles :supported-targets :c5-resolution-diagnostic-ids
          :c5-resolution-governing-document :c5-resolution-rejected-designs
          :c5-resolution-override-diagnostics :c5-special-form-symbols
          :c5-core-auto-imports :c5-type-auto-imports]))

(defn op-fn [key fallback] (or (get *operations* key) fallback))
(defn op-value [key fallback] (or (get *operations* key) fallback))

(defn default-fail! [id message data]
  (throw (ex-info message (assoc (or data {}) :id id))))
(defn default-source-span [source-path form-index]
  {:source source-path :form-index form-index})
(defn default-sha256-hex [value] (digest/sha256-hex value))
(defn default-c4-artifact-id [artifact]
  (str "sha256:" (digest/sha256-hex (pr-str artifact))))
(defn default-collect-code-symbols [form]
  (cond
    (symbol? form) [form]
    (seq? form) (if (= 'quote (first form))
                  (if (symbol? (first form)) [(first form)] [])
                  (mapcat default-collect-code-symbols form))
    (coll? form) (mapcat default-collect-code-symbols form)
    :else []))
(defn default-ns-form? [form] (and (seq? form) (= 'ns (first form))))
(defn unsupported-host-operation [operation]
  (fn [& _]
    (throw (ex-info (str "C5 leaf requires injected operation " operation)
                    {:operation operation}))))

(defn fail! [id message data]
  ((op-fn :fail! default-fail!) id message data))
(defn source-span [source-path form-index]
  ((op-fn :source-span default-source-span) source-path form-index))
(defn sha256-hex [value] ((op-fn :sha256-hex default-sha256-hex) value))
(defn c4-artifact-id [artifact]
  ((op-fn :c4-artifact-id default-c4-artifact-id) artifact))
(defn collect-code-symbols [form]
  ((op-fn :collect-code-symbols default-collect-code-symbols) form))
(defn ns-form? [form] ((op-fn :ns-form? default-ns-form?) form))
(defn read-source-form-records [source-path source-text]
  ((op-fn :read-source-form-records (unsupported-host-operation :read-source-form-records)) source-path source-text))
(defn validate-ns-syntax! [source-path forms]
  ((op-fn :validate-ns-syntax! (unsupported-host-operation :validate-ns-syntax!)) source-path forms))
(defn parse-module [source-path forms]
  ((op-fn :parse-module (unsupported-host-operation :parse-module)) source-path forms))
(defn module-source-artifact [source-path source-text]
  ((op-fn :module-source-artifact (unsupported-host-operation :module-source-artifact)) source-path source-text))
(defn compiler-c4-macro-source-artifact [source-path source-text]
  ((op-fn :compiler-c4-macro-source-artifact
          (unsupported-host-operation :compiler-c4-macro-source-artifact)) source-path source-text))

(defn- valid-keyword-set? [value]
  (and (set? value) (seq value) (every? keyword? value)))
(defn- valid-string-vector? [value]
  (and (vector? value) (every? string? value)))
(defn- valid-map-of-keywords-to-strings? [value]
  (and (map? value) (every? (fn [[key item]] (and (keyword? key) (string? item))) value)))
(defn- valid-symbol-set? [value]
  (and (set? value) (every? symbol? value)))

(defn validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "C5 operation map must be a map" {:value operations})))
  (let [unknown (seq (remove operation-keys (keys operations)))
        non-functions (seq (for [[key value] (select-keys operations function-operation-keys)
                                 :when (not (fn? value))] key))]
    (when unknown (throw (ex-info "C5 operation map contains unknown keys" {:unknown-keys (vec unknown) :allowed-keys operation-keys})))
    (when non-functions (throw (ex-info "C5 function operation values must be callable" {:non-function-keys (vec non-functions)})))
    (when (and (contains? operations :known-source-profiles)
               (not (valid-keyword-set? (:known-source-profiles operations))))
      (throw (ex-info "C5 known-source-profiles operation must be a non-empty keyword set"
                      {:expected :non-empty-keyword-set :actual (:known-source-profiles operations)})))
    (when (and (contains? operations :supported-targets)
               (not (valid-keyword-set? (:supported-targets operations))))
      (throw (ex-info "C5 supported-targets operation must be a non-empty keyword set"
                      {:expected :non-empty-keyword-set :actual (:supported-targets operations)})))
    (doseq [[key valid? expected]
            [[:c5-resolution-diagnostic-ids #(and (valid-string-vector? %) (seq %)) :non-empty-string-vector]
             [:c5-resolution-governing-document #(and (string? %) (seq %)) :non-empty-string]
             [:c5-resolution-rejected-designs #(and (vector? %) (every? map? %)) :vector-of-maps]
             [:c5-resolution-override-diagnostics valid-map-of-keywords-to-strings? :map-of-keywords-to-strings]
             [:c5-special-form-symbols valid-symbol-set? :symbol-set]
             [:c5-core-auto-imports valid-symbol-set? :symbol-set]
             [:c5-type-auto-imports valid-symbol-set? :symbol-set]]
            :when (and (contains? operations key) (not (valid? (get operations key))))]
      (throw (ex-info "C5 scalar operation has an invalid shape" {:key key :expected expected :actual (get operations key)}))))
  operations)

(defn with-operations [operations thunk]
  (validate-operations! operations)
  (binding [*operations* (merge *operations* operations)] (thunk)))
