(ns gravity.capability-validation.interposition)

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(def ^:private ^:dynamic *bypass-next-operation-keys* #{})

(def function-operation-keys
  #{:stable-set :stable-vec :diagnostic-record :provider-name
    :profile-capabilities :profile-effective-capabilities
    :capability-permission-table :capability-validation-facts})
(def scalar-operation-keys #{:provider-specs :capability-diagnostic-ids})
(def operation-keys (into function-operation-keys scalar-operation-keys))

(def ^:private default-scalars
  {:capability-diagnostic-ids
   ["L15-CAPABILITY-MISSING" "L15-PROVIDER-MISSING" "L15-PROFILE"
    "L15-SCOPE" "L15-PHASE" "L15-TRUST"]})

(defn current-operation [key]
  (when-not (contains? *active-operation-keys* key)
    (get *operations* key)))

(defn operation-value [key]
  (if (contains? *operations* key)
    (get *operations* key)
    (if (contains? default-scalars key)
      (get default-scalars key)
      (throw (ex-info "Capability validation requires an injected operation"
                      {:operation key})))))

(defn- default-stable-vec [values] (->> values (sort-by pr-str) vec))
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
        {:artifact :gravity/capability-diagnostic
         :diagnostic id :stage :capability-validation :facts facts
         :status :rejected})
      (throw (ex-info "Capability validation requires a function operation"
                      {:operation key})))))

(defn bypass? [key] (contains? *bypass-next-operation-keys* key))
(defn without-bypass [key thunk]
  (binding [*bypass-next-operation-keys* (disj *bypass-next-operation-keys* key)]
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

(defn- provider-specs? [value]
  (and (map? value) (seq value) (every? keyword? (keys value))
       (every? #(and (map? %) (symbol? (:provider %))
                     (set? (:profiles %)) (seq (:profiles %))
                     (every? keyword? (:profiles %)))
               (vals value))))
(defn- non-empty-string-vector? [value]
  (and (vector? value) (seq value)
       (every? #(and (string? %) (seq %)) value)))

(defn- validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "Capability validation operation map must be a map"
                    {:operations operations})))
  (let [unknown (vec (remove operation-keys (keys operations)))
        invalid (vec (for [[key value] (select-keys operations function-operation-keys)
                           :when (not (fn? value))] key))]
    (when (seq unknown)
      (throw (ex-info "Capability validation operation map has unknown keys"
                      {:unknown-keys unknown})))
    (when (seq invalid)
      (throw (ex-info "Capability validation function operation is not a function"
                      {:operation-keys invalid}))))
  (when (and (contains? operations :provider-specs)
             (not (provider-specs? (:provider-specs operations))))
    (throw (ex-info "Capability validation provider specs are malformed"
                    {:provider-specs (:provider-specs operations)})))
  (when (and (contains? operations :capability-diagnostic-ids)
             (not (non-empty-string-vector? (:capability-diagnostic-ids operations))))
    (throw (ex-info "Capability validation diagnostic IDs are malformed"
                    {:capability-diagnostic-ids (:capability-diagnostic-ids operations)})))
  operations)

(defn with-operations [operations thunk]
  (validate-operations! operations)
  (when-not (fn? thunk)
    (throw (ex-info "Capability validation thunk must be a function" {:thunk thunk})))
  (binding [*operations* (merge *operations* operations)] (thunk)))

(defn call-entrypoint-body [operation-key operation args]
  (when-not (contains? function-operation-keys operation-key)
    (throw (ex-info "Capability validation entrypoint key is unknown"
                    {:operation operation-key})))
  (when-not (fn? operation)
    (throw (ex-info "Capability validation entrypoint must be a function"
                    {:operation operation-key :value operation})))
  (when-not (sequential? args)
    (throw (ex-info "Capability validation entrypoint args must be sequential"
                    {:operation operation-key :args args})))
  (binding [*active-operation-keys* (disj *active-operation-keys* operation-key)
            *bypass-next-operation-keys* (conj *bypass-next-operation-keys* operation-key)]
    (apply operation args)))
