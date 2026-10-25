(ns gravity.build.incremental.operations
  "Dynamic operation interposition for hosted incremental evidence.")

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})

(defn current-operations []
  *operations*)

(defn invoke
  [key fallback & arguments]
  (if-let [operation (when-not (contains? *active-operation-keys* key)
                       (get *operations* key))]
    (binding [*active-operation-keys* (conj *active-operation-keys* key)]
      (apply operation arguments))
    (apply fallback arguments)))

(defn with-operations
  [operations thunk]
  (binding [*operations* (merge *operations* operations)]
    (thunk)))
