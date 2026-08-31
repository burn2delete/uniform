

(declare p15-s23-c11-mir-safe-diagnostic-scalar)

(defn p15-s23-c11-mir-diagnostic-context
  [checked-core context artifact]
  (let [checked-core (if (map? checked-core) checked-core {})
        context (if (map? context) context {})
        artifact (if (map? artifact) artifact {})]
    {:requested-target
     (let [candidate (or (:requested-target context)
                         (get-in checked-core
                                 [:target-request-metadata
                                  :requested-target]))]
       (if (keyword? candidate) candidate :target-neutral))
     :source-target (if (keyword? (:source-target checked-core))
                      (:source-target checked-core) :unknown)
     :profile (if (keyword? (:profile checked-core))
                (:profile checked-core) :hosted)
     :checked-core-artifact-id
     (p15-s23-c11-mir-safe-diagnostic-scalar
      (:artifact-id checked-core))
     :module-id
     (p15-s23-c11-mir-safe-diagnostic-scalar
      (or (get-in checked-core [:source-core-input :module])
          (:module-id artifact)
          :not-applicable))
     :function-id
     (p15-s23-c11-mir-safe-diagnostic-scalar
      (or (:entrypoint checked-core) :not-applicable))
     :block-id
     (p15-s23-c11-mir-safe-diagnostic-scalar
      (or (get-in artifact [:control-flow-graph :entry])
          (get-in artifact [:mir-module :control-flow-graph :entry])
          :not-applicable))
     :operation-id :not-applicable
     :source-operation :not-applicable
     :syntax-id :not-applicable
     :origin-id :not-applicable
     :mir-id
     (p15-s23-c11-mir-safe-diagnostic-scalar
      (or (:mir-id artifact) (:source-core artifact) :not-applicable))}))

(def p15-s23-c11-mir-upstream-diagnostic-rules
  (set (concat ["L2-BUILTIN-ARITY"]
               c6-lowering-diagnostic-ids
               c7-type-diagnostic-ids
               c8-effect-diagnostic-ids
               c9-ownership-diagnostic-ids
               c10-safety-diagnostic-ids
               c11-mir-diagnostic-ids)))

(def p15-s23-c11-mir-diagnostic-rule-contracts
  [{:rules #{"L2-BUILTIN-ARITY"}
    :stage :core-language-semantics
    :family :l2-core-language-semantics
    :document-id "L2"
    :expected-document (get stage1-bootstrap-governing-documents "L2")}
   {:rules (set c6-lowering-diagnostic-ids)
    :stage :core-lowering
    :family :c6-ast-core-lowering
    :document-id "C6"
    :expected-document c6-lowering-governing-document}
   {:rules (set c7-type-diagnostic-ids)
    :stage :type-check
    :family :c7-type-checker
    :document-id "C7"
    :expected-document c7-type-governing-document}
   {:rules (set c8-effect-diagnostic-ids)
    :stage :effect-check
    :family :c8-effect-checker
    :document-id "C8"
    :expected-document c8-effect-governing-document}
   {:rules (set c9-ownership-diagnostic-ids)
    :stage :ownership-lifetime-region-check
    :family :c9-ownership-checker
    :document-id "C9"
    :expected-document c9-ownership-governing-document}
   {:rules (set c10-safety-diagnostic-ids)
    :stage :safety-analysis
    :family :c10-safety-analysis
    :document-id "C10"
    :expected-document c10-safety-governing-document}
   {:rules (set c11-mir-diagnostic-ids)
    :stage :c11-authenticated-mir
    :family :c11-authenticated-mir
    :document-id "C11"
    :expected-document
    "docs/phase-06-compiler-architecture/090-c11-gravity-mir-specification.md"}])

(defn p15-s23-c11-mir-diagnostic-rule-contract
  [rule]
  (first (filter #(contains? (:rules %) rule)
                 p15-s23-c11-mir-diagnostic-rule-contracts)))

(defn p15-s23-c11-mir-safe-diagnostic-scalar
  [value]
  (cond
    (nil? value) nil
    (boolean? value) value
    (and (keyword? value) (<= (count (str value)) 256)) value
    (and (symbol? value) (<= (count (str value)) 256)) value
    (and (integer? value)
         (<= (.bitLength (.abs (biginteger value))) 256)) value
    (and (string? value)
         (<= (.length ^String value) 256)) value
    (and (vector? value)
         (<= (count value) 16)
         (every? #(or (nil? %) (boolean? %) (keyword? %) (symbol? %)
                      (integer? %) (string? %))
                 value))
    (mapv p15-s23-c11-mir-safe-diagnostic-scalar value)
    :else :redacted))

(defn p15-s23-c11-mir-safe-origin-chain
  [candidate]
  (cond
    (not (vector? candidate)) []
    (> (count candidate) 64) [:redacted-origin-chain-over-limit]
    :else (mapv p15-s23-c11-mir-safe-diagnostic-scalar candidate)))

(defn p15-s23-c11-mir-safe-diagnostic-fact-value
  [key value]
  (let [sha256?
        (fn [candidate]
          (and (string? candidate)
               (re-matches #"sha256:[0-9a-f]{64}" candidate)))
        semantic-id?
        (fn [candidate]
          (or (and (keyword? candidate)
                   (<= (count (str candidate)) 256))
              (and (symbol? candidate)
                   (<= (count (str candidate)) 256))
              (sha256? candidate)))
        bounded-count?
        (fn [candidate]
          (and (integer? candidate)
               (<= 0 candidate Long/MAX_VALUE)))
        path-vector?
        (fn [candidate]
          (and (vector? candidate)
               (<= (count candidate) 16)
               (every? #(or (semantic-id? %)
                            (and (integer? %) (<= 0 % Long/MAX_VALUE)))
                       candidate)))]
    (cond
      (contains? #{:missing-fact :requested-target :source-target
                   :source-operation :lowering-rule :expected-type
                   :actual-type :effect :capability :provider :grant
                   :specialized-safe-rule :safety-mode
                   :boundary-identity-reason :construction-mode
                   :checked-core-artifact-kind :checked-core-context-kind
                   :checked-core-ingress-mode
                   :checked-core-semantic-authority
                   :checked-core-verification-status
                   :runtime-contract-definition :bounded-reason}
                 key)
      (if (or (keyword? value) (symbol? value)) value :redacted)

      (contains? #{:module-id :function-id :block-id :operation-id
                   :op-id :value-id :checked-core-artifact-id :mir-id
                   :syntax-id :origin-id :core-node-id :c2-form-id
                   :owner-id :borrow-id :region-id :resource-id
                   :proof-id}
                 key)
      (if (semantic-id? value) value :redacted)

      (contains? #{:expected-source-content-hash
                   :observed-source-content-hash
                   :observed-plan-semantic-hash
                   :observed-functions-semantic-hash
                   :observed-builder-semantic-hash
                   :observed-verifier-semantic-hash}
                 key)
      (if (sha256? value) value :redacted)

      (= :producer-diagnostic-id key)
      (if (sha256? value) value :redacted)

      (contains? #{:conditional-count :expected-source-bytes
                   :observed-source-bytes :observed-nodes
                   :observed-depth :observed-width :maximum-width
                   :observed-total-scalar-bytes
                   :maximum-total-scalar-bytes :maximum-nodes
                   :maximum-depth :checked-core-ingress-schema-version}
                 key)
      (if (bounded-count? value) value :redacted)

      (= :control-path key)
      (if (path-vector? value) value :redacted)

      (contains? #{:runtime-check :unsafe-audit} key)
      (if (or (boolean? value) (semantic-id? value)) value :redacted)

      :else :redacted)))

(defn p15-s23-c11-mir-safe-diagnostic-facts
  [subject extra]
  (let [candidate (merge (if (map? subject) subject {})
                         (if (map? extra) extra {}))
        facts
        (into (sorted-map)
              (keep (fn [key]
                      (when (contains? candidate key)
                        [key
                         (p15-s23-c11-mir-safe-diagnostic-fact-value
                          key (get candidate key))])))
              p15-s23-c11-mir-diagnostic-fact-keys)]
    (if (seq facts)
      facts
      {:missing-fact :c11-validation-failure})))

(defn p15-s23-c11-mir-semantic-anchor
  [subject facts]
  (let [subject (if (map? subject) subject {})
        candidate
        (some #(let [value (get subject %)]
                 (when (and (string? value)
                            (re-matches #"sha256:[0-9a-f]{64}" value))
                   value))
              [:mir-id :artifact-id :source-core :op-id :operation-id])]
    (or candidate
        (p15-s23-c11-mir-digest
         {:kind :gravity/c11-diagnostic-primary
          :facts facts}))))