(ns gravity.c8-effect-checker.policy
  "Operation-map policy for the hosted C8 interposition facade.")

(def function-operation-keys
  #{:fail! :source-span :c4-artifact-id :read-source-form-records
    :validate-ns-syntax! :parse-module
    :compiler-c7-type-source-artifact
    :c8-effect-source-overrides :c8-effect-message :c8-effect-fail!
    :c8-effect-validate-overrides! :c8-fact-direct-effects
    :c8-effectful-facts :c8-effect-graph :c8-legality-records
    :c8-capability-proof-records :c8-build-effect-log
    :c8-replay-requirements :c8-ordering-constraints
    :c8-residual-effect-report :c8-effect-diagnostics
    :c8-effect-verifier-report :c8-effect-capability-proof
    :c8-effect-validate! :compiler-c8-effect-source-artifact
    :compiler-c8-effect-file-artifact})

(def scalar-operation-keys
  #{:c8-effect-diagnostic-ids :c8-effect-governing-document
    :c8-effect-rejected-designs :c8-effect-override-diagnostics
    :c8-known-effects :c8-effect-capability
    :c8-replay-sensitive-effects})

(def operation-keys
  (into function-operation-keys scalar-operation-keys))

(defn unsupported-host-operation [operation]
  (fn [& _]
    (throw (ex-info (str "C8 leaf requires injected operation " operation)
                    {:operation operation}))))

(defn- keyword-set? [value]
  (and (set? value) (seq value) (every? keyword? value)))
(defn- string-vector? [value]
  (and (vector? value) (seq value) (every? string? value)))
(defn- vector-of-maps? [value]
  (and (vector? value) (every? map? value)))
(defn- keyword-string-map? [value]
  (and (map? value)
       (every? (fn [[key item]]
                 (and (keyword? key) (string? item)))
               value)))
(defn- keyword-keyword-map? [value]
  (and (map? value)
       (every? (fn [[key item]]
                 (and (keyword? key) (keyword? item)))
               value)))

(defn validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "C8 operation map must be a map" {:value operations})))
  (let [unknown (seq (remove operation-keys (keys operations)))
        invalid-fns
        (seq (for [[key value] (select-keys operations
                                            function-operation-keys)
                   :when (not (fn? value))]
               key))]
    (when unknown
      (throw (ex-info "C8 operation map contains unknown keys"
                      {:unknown-keys (vec unknown)
                       :allowed-keys operation-keys})))
    (when invalid-fns
      (throw (ex-info "C8 function operation values must be functions"
                      {:non-function-keys (vec invalid-fns)}))))
  (doseq [[key predicate expected]
          [[:c8-effect-diagnostic-ids string-vector?
            :non-empty-string-vector]
           [:c8-effect-governing-document
            #(and (string? %) (seq %)) :non-empty-string]
           [:c8-effect-rejected-designs vector-of-maps? :vector-of-maps]
           [:c8-effect-override-diagnostics keyword-string-map?
            :keyword-to-string-map]
           [:c8-known-effects keyword-set? :non-empty-keyword-set]
           [:c8-effect-capability keyword-keyword-map?
            :keyword-to-keyword-map]
           [:c8-replay-sensitive-effects keyword-set?
            :non-empty-keyword-set]]
          :when (and (contains? operations key)
                     (not (predicate (get operations key))))]
    (throw (ex-info "C8 scalar operation has an invalid shape"
                    {:key key :expected expected
                     :actual (get operations key)})))
  operations)
