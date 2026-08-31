(ns gravity.profile-validation.interposition
  (:require [gravity.profile-validation.defaults :as defaults]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(def ^:private ^:dynamic *bypass-next-operation-keys* #{})

(def function-operation-keys
  #{:stable-set
    :stable-vec
    :diagnostic-record
    :all-registered-effects
    :effect-registry-entry
    :profile-allowed-effects
    :profile-capabilities
    :profile-contract
    :profile-policy-layer
    :profile-effective-effects
    :effect-permission-table
    :profile-validation-facts})

(def scalar-operation-keys
  #{:standard-profile-order
    :profile-diagnostic-ids
    :profile-memory-regimes
    :profile-runtime-assumptions
    :profile-unsafe-policies
    :profile-artifact-boundaries
    :effect-registry
    :provider-specs
    :core-forms
    :supported-targets})

(def operation-keys
  (into function-operation-keys scalar-operation-keys))

(defn current-operation [key]
  (when-not (contains? *active-operation-keys* key)
    (get *operations* key)))

(defn operation-value [key]
  (if (contains? *operations* key)
    (get *operations* key)
    (if (contains? defaults/scalar-operations key)
      (get defaults/scalar-operations key)
      (throw (ex-info "Profile validation requires an injected operation"
                      {:operation key})))))

(defn- default-stable-vec [values]
  (->> values (sort-by pr-str) vec))

(defn- default-stable-set [values]
  (into (sorted-set-by #(compare (pr-str %1) (pr-str %2))) values))

(defn invoke [key & args]
  (if-let [operation (current-operation key)]
    (apply operation args)
    (case key
      :stable-set (apply default-stable-set args)
      :stable-vec (apply default-stable-vec args)
      :diagnostic-record
      (let [[id facts] args]
        {:artifact :gravity/profile-diagnostic
         :diagnostic id
         :stage :profile-validation
         :facts facts
         :status :rejected})
      (throw (ex-info "Profile validation requires a function operation"
                      {:operation key})))))

(defn bypass? [key]
  (contains? *bypass-next-operation-keys* key))

(defn without-bypass [key thunk]
  (binding [*bypass-next-operation-keys*
            (disj *bypass-next-operation-keys* key)]
    (thunk)))

(defn with-active [key thunk]
  (binding [*active-operation-keys* (conj *active-operation-keys* key)]
    (thunk)))

(defmacro definterposable [name key arguments & body]
  `(defn ~name ~arguments
     (if (bypass? ~key)
       (without-bypass ~key (fn [] ~@body))
       (if-let [operation# (current-operation ~key)]
         (with-active ~key (fn [] (operation# ~@arguments)))
         (do ~@body)))))

(defn- distinct-keyword-vector? [value]
  (and (vector? value) (seq value) (every? keyword? value)
       (= (count value) (count (distinct value)))))

(defn- non-empty-string-vector? [value]
  (and (vector? value) (seq value)
       (every? #(and (string? %) (seq %)) value)))

(defn- profile-map? [value value-predicate]
  (and (map? value) (seq value) (every? keyword? (keys value))
       (every? value-predicate (vals value))))

(defn- effect-registry? [value]
  (and (map? value) (seq value)
       (every? keyword? (keys value))
       (every? (fn [entry]
                 (and (map? entry)
                      (set? (:profiles entry))
                      (every? keyword? (:profiles entry))))
               (vals value))))

(defn- provider-specs? [value]
  (and (map? value) (seq value) (every? keyword? (keys value))
       (every? (fn [spec]
                 (and (map? spec)
                      (set? (:profiles spec))
                      (every? keyword? (:profiles spec))))
               (vals value))))

(defn- validate-function-operations! [operations]
  (let [unknown (vec (remove operation-keys (keys operations)))
        invalid-functions
        (vec (for [[key value] (select-keys operations function-operation-keys)
                   :when (not (fn? value))]
               key))]
    (when (seq unknown)
      (throw (ex-info "Profile validation operation map has unknown keys"
                      {:unknown-keys unknown})))
    (when (seq invalid-functions)
      (throw (ex-info "Profile validation function operation is not a function"
                      {:operation-keys invalid-functions})))))

(defn- validate-scalar-operations! [operations]
  (doseq [[key predicate expected]
          [[:standard-profile-order distinct-keyword-vector?
            :distinct-non-empty-keyword-vector]
           [:profile-diagnostic-ids non-empty-string-vector?
            :non-empty-string-vector]
           [:profile-memory-regimes #(profile-map? % map?) :keyword-to-map]
           [:profile-runtime-assumptions #(profile-map? % map?) :keyword-to-map]
           [:profile-unsafe-policies #(profile-map? % keyword?)
            :keyword-to-keyword]
           [:profile-artifact-boundaries
            #(profile-map? % (fn [items]
                               (and (set? items) (seq items)
                                    (every? keyword? items))))
            :keyword-to-non-empty-keyword-set]
           [:effect-registry effect-registry? :effect-registry]
           [:provider-specs provider-specs? :provider-specs]
           [:core-forms #(and (set? %) (seq %) (every? symbol? %))
            :non-empty-symbol-set]
           [:supported-targets #(and (set? %) (seq %)
                                     (every? keyword? %))
            :non-empty-keyword-set]]
          :when (and (contains? operations key)
                     (not (predicate (get operations key))))]
    (throw (ex-info "Profile validation scalar operation has an invalid shape"
                    {:operation key
                     :expected expected
                     :actual (get operations key)}))))

(defn- validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "Profile validation operation map must be a map"
                    {:operations operations})))
  (validate-function-operations! operations)
  (validate-scalar-operations! operations)
  operations)

(defn with-operations [operations thunk]
  (validate-operations! operations)
  (when-not (fn? thunk)
    (throw (ex-info "Profile validation thunk must be a function"
                    {:thunk thunk})))
  (binding [*operations* (merge *operations* operations)]
    (thunk)))

(defn call-entrypoint-body [operation-key operation args]
  (when-not (contains? function-operation-keys operation-key)
    (throw (ex-info "Profile validation entrypoint key is unknown"
                    {:operation operation-key})))
  (when-not (fn? operation)
    (throw (ex-info "Profile validation entrypoint must be a function"
                    {:operation operation-key :value operation})))
  (when-not (sequential? args)
    (throw (ex-info "Profile validation entrypoint args must be sequential"
                    {:operation operation-key :args args})))
  (binding [*active-operation-keys*
            (disj *active-operation-keys* operation-key)
            *bypass-next-operation-keys*
            (conj *bypass-next-operation-keys* operation-key)]
    (apply operation args)))
