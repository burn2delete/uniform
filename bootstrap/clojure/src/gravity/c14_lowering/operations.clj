(ns gravity.c14-lowering.operations)

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(defn current-operations [] *operations*)
(defn invoke [key fallback & args]
  (if-let [operation (when-not (contains? *active-operation-keys* key)
                       (get *operations* key))]
    (binding [*active-operation-keys* (conj *active-operation-keys* key)]
      (apply operation args))
    (apply fallback args)))
(defn with-operations [operations thunk]
  (binding [*operations* (merge *operations* operations)] (thunk)))
