(ns gravity.c3-syntax-diagnostics.policy
  "Operation and namespace policy for hosted C3 syntax diagnostics.")

(def function-operation-keys
  #{:fail!
    :source-span
    :c3-syntax-source-overrides
    :c3-syntax-overrides-from-forms
    :c3-syntax-message
    :c3-syntax-fail!
    :c3-syntax-validate-overrides!})

(def scalar-operation-keys
  #{:c3-syntax-diagnostic-ids
    :c3-syntax-governing-document
    :c3-syntax-rejected-designs
    :c3-syntax-override-diagnostics})

(def operation-keys (into function-operation-keys scalar-operation-keys))

(defn- valid-string-vector? [value]
  (and (vector? value)
       (seq value)
       (every? #(and (string? %) (seq %)) value)))

(defn- valid-rejected-designs? [value]
  (and (vector? value) (seq value) (every? map? value)))

(defn- valid-override-map? [value]
  (and (map? value)
       (seq value)
       (every? keyword? (keys value))
       (every? #(and (string? %) (seq %)) (vals value))))

(defn validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "C3 syntax diagnostic operations must be a map"
                    {:operations operations})))
  (let [unknown (vec (remove operation-keys (keys operations)))]
    (when (seq unknown)
      (throw (ex-info "C3 syntax diagnostic operations contain unknown keys"
                      {:unknown-keys unknown}))))
  (doseq [key function-operation-keys
          :when (contains? operations key)]
    (when-not (fn? (get operations key))
      (throw (ex-info "C3 syntax diagnostic operation must be a function"
                      {:operation key :value (get operations key)}))))
  (doseq [[key predicate message]
          [[:c3-syntax-diagnostic-ids
            valid-string-vector?
            "C3 diagnostic identifiers must be a nonempty string vector"]
           [:c3-syntax-governing-document
            #(and (string? %) (seq %))
            "C3 governing document must be a nonempty string"]
           [:c3-syntax-rejected-designs
            valid-rejected-designs?
            "C3 rejected designs must be a nonempty vector of maps"]
           [:c3-syntax-override-diagnostics
            valid-override-map?
            "C3 override diagnostics must map keywords to nonempty strings"]]
          :when (contains? operations key)]
    (when-not (predicate (get operations key))
      (throw (ex-info message {:operation key :value (get operations key)}))))
  operations)

(def namespace-contract
  {:namespace 'gravity.c3-syntax-diagnostics
   :contract-boundary :hosted-c3-syntax-diagnostic-policy
   :public-api
   {'c3-syntax-diagnostic-ids {:kind :constant}
    'c3-syntax-governing-document {:kind :constant}
    'c3-syntax-rejected-designs {:kind :constant}
    'c3-syntax-override-diagnostics {:kind :constant}
    'with-operations {:arglists '([operations thunk])}
    'c3-syntax-source-overrides {:arglists '([module])}
    'c3-syntax-overrides-from-forms {:arglists '([forms])}
    'c3-syntax-message {:arglists '([id])}
    'c3-syntax-fail! {:arglists '([id source-path subject extra])}
    'c3-syntax-validate-overrides! {:arglists '([source-path overrides])}}
   :operation-interposition
   {:accepted-keys operation-keys
    :partial-overrides? true
    :unknown-keys-rejected? true}
   :ownership
   {:owns [:hosted-c3-diagnostic-catalog
           :hosted-c3-diagnostic-payload-policy
           :hosted-c3-fixture-override-routing]
    :does-not-own [:canonical-c3-syntax-object-authority
                   :c2-reader-product-authentication
                   :sh04-boundary-authentication
                   :syntax-object-construction
                   :syntax-verification-authority
                   :proof-authority
                   :attestation-authority
                   :self-hosted-authority
                   :release-authority]}
   :dependency-direction
   {:requires ['clojure.core]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :canonical-c3-authority? false
   :proof-authority? false
   :self-hosted? false
   :release-authority? false})
