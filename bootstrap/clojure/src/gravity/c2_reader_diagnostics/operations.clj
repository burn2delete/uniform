(ns gravity.c2-reader-diagnostics.operations)

(def function-operation-keys
  #{:fail!
    :source-span
    :reader-canonical-hash
    :c2-reader-source-overrides
    :c2-reader-message
    :c2-reader-fail!
    :c2-reader-remap-exception!
    :c2-reader-validate-overrides!})

(def scalar-operation-keys
  #{:c2-reader-diagnostic-ids
    :c2-reader-governing-document
    :c2-reader-rejected-designs
    :c2-reader-override-diagnostics
    :standard-reader-options})

(def operation-keys
  (into function-operation-keys scalar-operation-keys))

(defn valid-string-vector? [value]
  (and (vector? value)
       (seq value)
       (every? #(and (string? %) (seq %)) value)))

(defn valid-rejected-designs? [value]
  (and (vector? value)
       (seq value)
       (every? (fn [entry]
                 (and (map? entry)
                      (string? (:diagnostic entry))
                      (seq (:diagnostic entry))
                      (string? (:fixture entry))
                      (seq (:fixture entry))
                      (contains? entry :rejected-design)))
               value)))

(defn valid-override-map? [value]
  (and (map? value)
       (seq value)
       (every? keyword? (keys value))
       (every? #(and (string? %) (seq %)) (vals value))))

(defn valid-standard-reader-options? [value]
  (and (map? value)
       (boolean? (:retain-comments value))
       (set? (:enabled-features value))
       (every? keyword? (:enabled-features value))
       (string? (:extension-policy value))
       (seq (:extension-policy value))))

(defn validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "C2 reader diagnostics operations must be a map"
                    {:operations operations})))
  (let [unknown (vec (remove operation-keys (keys operations)))]
    (when (seq unknown)
      (throw (ex-info "C2 reader diagnostics operations contain unknown keys"
                      {:unknown-keys unknown}))))
  (doseq [key function-operation-keys
          :when (contains? operations key)]
    (when-not (fn? (get operations key))
      (throw (ex-info "C2 reader diagnostics operation must be a function"
                      {:operation key :value (get operations key)}))))
  (doseq [[key predicate message]
          [[:c2-reader-diagnostic-ids valid-string-vector?
            "C2 diagnostic identifiers must be a nonempty string vector"]
           [:c2-reader-governing-document #(and (string? %) (seq %))
            "C2 governing document must be a nonempty string"]
           [:c2-reader-rejected-designs valid-rejected-designs?
            "C2 rejected designs must be a nonempty vector of shaped maps"]
           [:c2-reader-override-diagnostics valid-override-map?
            "C2 override diagnostics must map keywords to nonempty strings"]
           [:standard-reader-options valid-standard-reader-options?
            "standard reader options must have strict hosted shape"]]
          :when (contains? operations key)]
    (when-not (predicate (get operations key))
      (throw (ex-info message {:operation key :value (get operations key)}))))
  operations)
