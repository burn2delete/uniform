(ns gravity.c6-core-lowering.context
  "Dynamic operation context shared by the hosted C6 lowering leaves."
  (:require [gravity.digest :as digest]))

(def ^:dynamic *operations* {})

(def operation-keys
  #{:fail! :source-span :c4-artifact-id :form-effect :ns-form?
    :core-forms :lowering-gap-forms :known-source-profiles :supported-targets
    :c6-lowering-diagnostic-ids :c6-lowering-governing-document
    :c6-lowering-rejected-designs :c6-lowering-override-diagnostics
    :c6-domain-boundary-operators :c6-core-node-forms
    :c6-lowering-source-overrides :c6-lowering-message
    :c6-lowering-fail! :c6-lowering-validate-overrides!
    :c6-node-id :c6-core-node :c6-lower-children :c6-eval-order
    :c6-form->core-form :c6-lower-form :c6-core-child-nodes
    :c6-flatten-core :c6-domain-boundary-records :c6-surface-to-core-map
    :c6-desugaring-trace :c6-evaluation-order-records
    :c6-core-verifier-report :c6-rule-invalidation-record
    :c6-lowering-capability-proof :c6-lowering-validate!})

(def function-operation-keys
  (disj operation-keys :core-forms :lowering-gap-forms
        :known-source-profiles :supported-targets
        :c6-lowering-diagnostic-ids :c6-lowering-governing-document
        :c6-lowering-rejected-designs :c6-lowering-override-diagnostics
        :c6-domain-boundary-operators :c6-core-node-forms))

(defn op-fn [key fallback]
  (or (get *operations* key) fallback))

(defn op-value [key fallback]
  (or (get *operations* key) fallback))

(defn invoke-op [key fallback & args]
  (apply (op-fn key fallback) args))

(defn default-fail! [id message data]
  (throw (ex-info message (assoc (or data {}) :id id))))

(defn default-source-span [source-path form-index]
  {:source source-path :form-index form-index})

(defn default-c4-artifact-id [artifact]
  (str "sha256:" (digest/sha256-hex (pr-str artifact))))

(defn fail! [id message data]
  ((op-fn :fail! default-fail!) id message data))

(defn source-span [path index]
  ((op-fn :source-span default-source-span) path index))

(defn c4-artifact-id [artifact]
  ((op-fn :c4-artifact-id default-c4-artifact-id) artifact))

(defn form-effect [form]
  ((op-fn :form-effect (fn [_] #{})) form))

(defn ns-form? [form]
  ((op-fn :ns-form? #(and (seq? %) (= 'ns (first %)))) form))

(defn- valid-keyword-set? [value]
  (and (set? value) (seq value) (every? keyword? value)))

(defn- valid-string-vector? [value]
  (and (vector? value) (seq value) (every? string? value)))

(defn- valid-map-of-keywords-to-strings? [value]
  (and (map? value)
       (every? (fn [[k v]] (and (keyword? k) (string? v))) value)))

(defn- valid-symbol-set? [value]
  (and (set? value) (every? symbol? value)))

(defn- valid-symbol-or-keyword-set? [value]
  (and (set? value) (seq value)
       (every? #(or (symbol? %) (keyword? %)) value)))

(defn valid-operation-map! [operations]
  (when-not (map? operations)
    (throw (ex-info "C6 operation map must be a map" {:value operations})))
  (let [unknown (seq (remove operation-keys (keys operations)))
        non-functions
        (seq (for [[k v] (select-keys operations function-operation-keys)
                   :when (not (fn? v))]
               k))]
    (when unknown
      (throw (ex-info "C6 operation map contains unknown keys"
                      {:unknown-keys (vec unknown)
                       :allowed-keys operation-keys})))
    (when non-functions
      (throw (ex-info "C6 function operation values must be callable"
                      {:non-function-keys (vec non-functions)})))
    (doseq [[k valid? expected]
            [[:core-forms valid-symbol-set? :symbol-set]
             [:lowering-gap-forms valid-symbol-set? :symbol-set]
             [:known-source-profiles valid-keyword-set? :non-empty-keyword-set]
             [:supported-targets valid-keyword-set? :non-empty-keyword-set]
             [:c6-lowering-diagnostic-ids valid-string-vector?
              :non-empty-string-vector]
             [:c6-lowering-governing-document #(and (string? %) (seq %))
              :non-empty-string]
             [:c6-lowering-rejected-designs
              #(and (vector? %) (every? map? %)) :vector-of-maps]
             [:c6-lowering-override-diagnostics
              valid-map-of-keywords-to-strings? :map-of-keywords-to-strings]
             [:c6-domain-boundary-operators valid-symbol-set? :symbol-set]
             [:c6-core-node-forms valid-symbol-or-keyword-set?
              :symbol-or-keyword-set]]
            :when (and (contains? operations k)
                       (not (valid? (get operations k))))]
      (throw (ex-info "C6 scalar operation has an invalid shape"
                      {:key k :expected expected
                       :actual (get operations k)}))))
  operations)

(defn with-operations [operations thunk]
  (valid-operation-map! operations)
  (binding [*operations* (merge *operations* operations)]
    (thunk)))
