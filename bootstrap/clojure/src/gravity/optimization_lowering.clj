(ns gravity.optimization-lowering
  "Shared hosted Stage0 optimization/lowering compatibility engine.

  This facade preserves the fused Clojure seed helpers used by C13 and C14. It
  is not optimization, lowering, proof, backend, self-hosting, or release
  authority."
  (:require [gravity.digest :as digest]
            [gravity.optimization-lowering.artifact :as artifact]
            [gravity.optimization-lowering.diagnostics :as diagnostics]
            [gravity.optimization-lowering.policy :as policy]
            [gravity.optimization-lowering.records :as records]
            [gravity.optimization-lowering.validation :as validation]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})

(def ^:private function-operation-keys policy/function-operation-keys)
(def ^:private scalar-operation-keys policy/scalar-operation-keys)
(def ^:private operation-keys policy/operation-keys)

(defn- current-operation [key]
  (when-not (contains? *active-operation-keys* key)
    (get *operations* key)))

(defmacro ^:private definterposable [name args & body]
  (let [key (keyword name)]
    `(defn ~name ~args
       (if-let [operation# (current-operation ~key)]
         (binding [*active-operation-keys* (conj *active-operation-keys* ~key)]
           (operation# ~@args))
         (do ~@body)))))

(defn- default-fail! [id message data]
  (throw (ex-info message (assoc (or data {}) :id id))))

(defn- default-source-span [path index]
  {:source path :form-index index})

(defn- fail! [id message data]
  ((or (:fail! *operations*) default-fail!) id message data))

(defn- source-span [path index]
  ((or (:source-span *operations*) default-source-span) path index))

(defn- sha256-hex [value]
  ((or (:sha256-hex *operations*) digest/sha256-hex) value))

(defn- perf-present? [value]
  ((or (:perf-present? *operations*)
       (fn [candidate]
         (and (some? candidate)
              (not (and (coll? candidate) (empty? candidate))))))
   value))

(defn- unsupported [key]
  (fn [& _]
    (throw (ex-info
            (str "optimization/lowering leaf requires injected operation " key)
            {:operation key}))))

(defn- checked-core-source-artifact [path text]
  ((or (:checked-core-source-artifact *operations*)
       (unsupported :checked-core-source-artifact))
   path text))

(defn- domain-ir-source-artifact [path text]
  ((or (:domain-ir-source-artifact *operations*)
       (unsupported :domain-ir-source-artifact))
   path text))

(def ^:dynamic c13-optimization-diagnostic-ids
  diagnostics/c13-optimization-diagnostic-ids)

(def ^:dynamic c14-lowering-diagnostic-ids
  diagnostics/c14-lowering-diagnostic-ids)

(def ^:dynamic optimization-lowering-diagnostic-ids
  diagnostics/diagnostic-ids)

(def ^:dynamic optimization-lowering-diagnostic-messages
  diagnostics/diagnostic-messages)

(def ^:dynamic optimization-lowering-override-diagnostics
  diagnostics/override-diagnostics)

(def ^:dynamic optimization-pass-contract-seed records/pass-contract-seed)

(definterposable optimization-lowering-source-overrides
  [module]
  (artifact/source-overrides module))

(definterposable optimization-lowering-fail!
  [id source-path artifact subject extra]
  (let [[message data]
        (diagnostics/failure-data
         optimization-lowering-diagnostic-messages source-span
         id source-path artifact subject extra)]
    (fail! id message data)))

(definterposable optimization-pass-contract-record
  [record]
  (records/pass-contract-record record))

(definterposable optimization-decision-record
  [domain-ir-artifact input-id index contract]
  (records/decision-record sha256-hex domain-ir-artifact input-id index contract))

(definterposable optimization-lowering-validate-overrides!
  [source-path artifact]
  (validation/validate-overrides!
   {:fail! optimization-lowering-fail!
    :override-diagnostics optimization-lowering-override-diagnostics
    :source-span source-span}
   source-path artifact))

(definterposable optimization-lowering-validate!
  [source-path artifact]
  (validation/validate!
   {:validate-overrides! optimization-lowering-validate-overrides!
    :fail! optimization-lowering-fail!
    :perf-present? perf-present?}
   source-path artifact))

(definterposable optimization-lowering-capability-proof
  [artifact]
  (validation/capability-proof perf-present? artifact))

(definterposable optimization-lowering-source-artifact
  [source-path source-text]
  (artifact/source-artifact
   {:checked-core-source-artifact checked-core-source-artifact
    :domain-ir-source-artifact domain-ir-source-artifact
    :source-overrides optimization-lowering-source-overrides
    :pass-contract-record optimization-pass-contract-record
    :decision-record optimization-decision-record
    :sha256-hex sha256-hex
    :validate! optimization-lowering-validate!
    :capability-proof optimization-lowering-capability-proof}
   {:pass-contract-seed optimization-pass-contract-seed
    :diagnostic-ids optimization-lowering-diagnostic-ids}
   source-path source-text))

(def ^:private namespace-contract policy/namespace-contract)

(defn- string-vector? [v] (policy/string-vector? v))
(defn- string-map? [v] (policy/string-map? v))
(defn- keyword-vector-map? [v] (policy/keyword-vector-map? v))
(defn- vector-maps? [v] (policy/vector-maps? v))

(defn- validate-operations! [operations]
  (policy/validate-operations! operations))

(defn with-operations [operations thunk]
  (validate-operations! operations)
  (let [merged (merge *operations* operations)]
    (binding [*operations* merged
              c13-optimization-diagnostic-ids
              (get merged :c13-optimization-diagnostic-ids
                   c13-optimization-diagnostic-ids)
              c14-lowering-diagnostic-ids
              (get merged :c14-lowering-diagnostic-ids
                   c14-lowering-diagnostic-ids)
              optimization-lowering-diagnostic-ids
              (get merged :optimization-lowering-diagnostic-ids
                   optimization-lowering-diagnostic-ids)
              optimization-lowering-diagnostic-messages
              (get merged :optimization-lowering-diagnostic-messages
                   optimization-lowering-diagnostic-messages)
              optimization-lowering-override-diagnostics
              (get merged :optimization-lowering-override-diagnostics
                   optimization-lowering-override-diagnostics)
              optimization-pass-contract-seed
              (get merged :optimization-pass-contract-seed
                   optimization-pass-contract-seed)]
      (thunk))))

(def public-api
  {'public-api {:kind :contract}
   'with-operations {:arglists '([operations thunk])}
   'shared-engine-contract {:arglists '([])}
   'c13-optimization-diagnostic-ids {:kind :constant}
   'c14-lowering-diagnostic-ids {:kind :constant}
   'optimization-lowering-diagnostic-ids {:kind :constant}
   'optimization-lowering-diagnostic-messages {:kind :constant}
   'optimization-lowering-override-diagnostics {:kind :constant}
   'optimization-pass-contract-seed {:kind :constant}
   'optimization-lowering-source-overrides {:arglists '([module])}
   'optimization-lowering-fail!
   {:arglists '([id source-path artifact subject extra])}
   'optimization-pass-contract-record {:arglists '([record])}
   'optimization-decision-record
   {:arglists '([domain-ir-artifact input-id index contract])}
   'optimization-lowering-validate-overrides!
   {:arglists '([source-path artifact])}
   'optimization-lowering-validate! {:arglists '([source-path artifact])}
   'optimization-lowering-capability-proof {:arglists '([artifact])}
   'optimization-lowering-source-artifact
   {:arglists '([source-path source-text])}})

(defn shared-engine-contract []
  (assoc namespace-contract :public-api public-api))
