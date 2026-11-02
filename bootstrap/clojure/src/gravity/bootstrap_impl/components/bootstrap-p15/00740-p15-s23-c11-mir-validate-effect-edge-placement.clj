

(defn p15-s23-c11-mir-validate-effect-edge-placement!
  [source-path mir operation-products]
  (let [blocks (:blocks operation-products)
        operation-by-id (:operation-by-id operation-products)
        join (get-in mir [:control-flow-graph :join])
        position-by-id
        (into {}
              (mapcat
               (fn [[_ block]]
                 (map-indexed (fn [index operation]
                                [(:op-id operation) index])
                              (:instructions block))))
              blocks)]
    (doseq [edge (get-in mir [:effect-order-graph :event-edges])]
      (let [before-operation (get operation-by-id (:before edge))
            after-operation (get operation-by-id (:after edge))
            before-block (:block-id before-operation)
            after-block (:block-id after-operation)
            same-block? (= before-block after-block)
            same-block-order?
            (and same-block?
                 (< (get position-by-id (:before edge) Long/MAX_VALUE)
                    (get position-by-id (:after edge) Long/MIN_VALUE)))
            dominates?
            (and (not same-block?)
                 (contains? (set (:dominators (get blocks after-block)))
                            before-block))
            exact-join-pair?
            (and (= after-block (:block-id join))
                 (= (:after edge) (:value-id join))
                 (some #(= {:predecessor before-block
                            :value (:before edge)}
                           %)
                       (:incoming join)))
            branch-to-post-join?
            (and (= after-block (:block-id join))
                 (contains? (set (map :predecessor (:incoming join)))
                            before-block)
                 (< (get position-by-id (:value-id join) Long/MAX_VALUE)
                    (get position-by-id (:after edge) Long/MIN_VALUE)))]
        (p15-s23-c11-mir-require!
         (and (map? before-operation)
              (map? after-operation)
              (or same-block-order? dominates? exact-join-pair?
                  branch-to-post-join?))
         "C11-EFFECT" source-path after-operation
         :effect-edge-cfg-placement-and-order)))
    :passed))

(defn p15-s23-c11-mir-validate-data-flow!
  [source-path checked-core mir operation-products]
  (let [parameter (p15-s23-c11-mir-entry-parameter! source-path checked-core)
        nodes (:nodes operation-products)
        operations (:operations operation-products)
        operation-by-id (:operation-by-id operation-products)
        blocks (:blocks operation-products)
        join (get-in mir [:control-flow-graph :join])
        position-by-id
        (into {}
              (mapcat
               (fn [[_ block]]
                 (map-indexed (fn [index operation]
                                [(:op-id operation) index])
                              (:instructions block))))
              blocks)
        data-flow (get-in mir [:data-flow-graph :edges])
        terminator-uses (get-in mir [:data-flow-graph :terminator-uses])
        expected-data-flow
        (p15-s23-c11-mir-expected-data-flow nodes operation-by-id)
        expected-terminator-uses
        (p15-s23-c11-mir-expected-terminator-uses checked-core)
        expected-definitions
        (into
         {}
         (mapcat
          (fn [node]
            (let [node-id (:node-id node)
                  block-id (:block-id (get operation-by-id node-id))
                  check (get-in node [:safety :check])]
              (cond->
               [[node-id (or (when (= node-id (:id parameter)) (:definition parameter)) {:value-id node-id
                          :operation-id node-id
                          :block-id block-id})]]
                check
                (conj
                 [(p15-s23-c11-mir-runtime-check-token-id check)
                  {:value-id
                   (p15-s23-c11-mir-runtime-check-token-id check)
                   :operation-id
                   (p15-s23-c11-mir-runtime-check-operation-id check)
                   :block-id block-id}]))))
          nodes))
        expected-values
        (into
         {}
         (mapcat
          (fn [node]
            (let [node-id (:node-id node)
                  check (get-in node [:safety :check])]
              (cond->
               [[node-id
                 (p15-s23-c11-mir-expected-value
                  node expected-data-flow expected-terminator-uses
                  (get operation-by-id node-id) (when (= node-id (:id parameter)) parameter))]]
                check
                (conj
                 [(p15-s23-c11-mir-runtime-check-token-id check)
                  (p15-s23-c11-mir-expected-runtime-check-value
                   node expected-data-flow
                   (get operation-by-id
                        (p15-s23-c11-mir-runtime-check-operation-id
                         check)))]))))
          nodes))
        expected-source-map
        (into
         {}
         (mapcat
          (fn [node]
            (if-let [check (get-in node [:safety :check])]
              [[(:node-id node) (:source node)]
               [(p15-s23-c11-mir-runtime-check-operation-id check)
                (p15-s23-c11-mir-runtime-check-source node)]]
              [[(:node-id node) (:source node)]]))
          nodes))]
    (p15-s23-c11-mir-require!
     (and (vector? data-flow)
          (= expected-data-flow data-flow))
     "C11-DOMINANCE" source-path mir :exact-checked-core-data-flow)
    (p15-s23-c11-mir-require!
     (every? #(p15-s23-c11-mir-valid-dominance-edge?
               % blocks operation-by-id expected-definitions
               position-by-id join parameter)
             data-flow)
     "C11-DOMINANCE" source-path mir :definition-dominates-use)
    (p15-s23-c11-mir-require!
     (and (vector? terminator-uses)
          (= expected-terminator-uses terminator-uses))
     "C11-DOMINANCE" source-path mir :exact-terminator-use-sites)
    (p15-s23-c11-mir-require!
     (every? #(p15-s23-c11-mir-valid-terminator-use?
               % blocks operation-by-id position-by-id)
             terminator-uses)
     "C11-DOMINANCE" source-path mir
     :terminator-definition-dominance-and-order)
    (p15-s23-c11-mir-require!
     (= expected-definitions
        (get-in mir [:data-flow-graph :definitions]))
     "C11-DOMINANCE" source-path mir :exact-ssa-definition-table)
    (p15-s23-c11-mir-require!
     (= expected-values (get-in mir [:data-flow-graph :values]))
     "C11-TYPE" source-path mir :exact-ssa-value-table)
    (p15-s23-c11-mir-require!
     (= expected-source-map (:source-map mir))
     "C11-ORIGIN" source-path mir :exact-checked-core-source-map)
    (p15-s23-c11-mir-require!
     (every? (fn [operation]
               (let [direct-effects (:effects operation)
                     source-operation (:source-operation operation)]
                 (or (empty? direct-effects)
                     (contains? #{:str :println} source-operation))))
             operations)
     "C11-EFFECT" source-path mir :direct-effectful-operation-boundary)
    {:edge-count (count data-flow)
     :terminator-use-count (count terminator-uses)
     :definition-count (count expected-definitions)
     :value-count (count expected-values)}))

(defn p15-s23-c11-mir-validate-constructed!*
  "Independent Clojure verifier for the untrusted result returned by the
  pinned Gravity builder.  It bounds the carrier before traversal, then checks
  C6-C10 fact parity, CFG/SSA dominance, direct effects, safety, provenance,
  target independence, and the narrow B1 handoff without recursively walking
  attacker-controlled graphs."
  [source-path checked-core mir]
  (p15-s23-c11-mir-bounded-value!
   source-path :gravity-c11-builder-result mir)
  (let [envelope
        (p15-s23-c11-mir-validate-module-envelope!
         source-path checked-core mir)
        _ (p15-s23-c11-mir-validate-fact-tables!
           source-path checked-core mir)
        operation-products
        (p15-s23-c11-mir-validate-operations!
         source-path checked-core mir)
        runtime-checks
        (p15-s23-c11-mir-validate-runtime-checks!
         source-path checked-core mir operation-products)
        cfg (p15-s23-c11-mir-validate-cfg!
             source-path checked-core mir envelope operation-products)
        _ (p15-s23-c11-mir-validate-effect-edge-placement!
           source-path mir operation-products)
        data-flow
        (p15-s23-c11-mir-validate-data-flow!
         source-path checked-core mir operation-products)]
    {:status :passed
     :module-shape-valid? true
     :block-count (count (:blocks operation-products))
     :operation-count (count (:operations operation-products))
     :conditional-count (:conditional-count cfg)
     :dominance-valid? true
     :type-facts-valid? true
     :direct-effects-valid? true
     :safety-facts-valid? true
     :origin-closure-valid? true
     :runtime-checks-valid? true
     :runtime-checks runtime-checks
     :target-independent? true
     :data-flow data-flow}))

(defn p15-s23-c11-mir-validate-constructed!
  [source-path checked-core mir]
  (binding [*p15-s23-c11-mir-diagnostic-context*
            (p15-s23-c11-mir-diagnostic-context
             checked-core {} mir)]
   (try
    (p15-s23-c11-mir-validate-constructed!*
      source-path checked-core mir)
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch StackOverflowError error
      (p15-s23-c11-mir-contain-exception!
       source-path :contained-c11-constructed-verifier-host-stack error))
    (catch AssertionError error
      (p15-s23-c11-mir-contain-exception!
       source-path :contained-c11-constructed-verifier-assertion error))
    (catch LinkageError error
      (p15-s23-c11-mir-contain-exception!
       source-path :contained-c11-constructed-verifier-linkage error))
    (catch clojure.lang.ExceptionInfo exception
      (p15-s23-c11-mir-contain-exception!
       source-path :contained-c11-constructed-verifier-diagnostic exception))
    (catch Exception exception
      (p15-s23-c11-mir-contain-exception!
       source-path :contained-c11-constructed-verifier-host-failure
       exception)))))


(require '[gravity.checked-core.bool-branch-join :as bool-branch-join])

(defn p15-s23-c11-mir-v2-source-family!
  "A semantic family gate on already authenticated Core, never MIR authority."
  [source-path checked-core version]
  (when (= 2 version)
    (let [parameter (p15-s23-c11-mir-entry-parameter! source-path checked-core)]
      (p15-s23-c11-mir-require!
       (bool-branch-join/source-family? checked-core (some? parameter))
       "C11-MODULE" source-path checked-core
       :v2-bool-branch-join-source-family)))
  (when (= 3 version)
    (p15-s23-c11-mir-require-trusted-carrier! source-path :flat-bool-original-core checked-core :reject)
    (p15-s23-c11-mir-require!
     (bool-branch-join/flat-sequence-source-family? checked-core)
     "C11-MODULE" source-path checked-core :v3-flat-bool-sequence-source-family))
  nil)


;; These helpers validate/project DATA. They never authenticate a supplied
;; report. Only the lexical public constructor/verifier calls the fresh C6
;; owner; standalone final-artifact validation is relative to checked Core.
(declare p15-s23-c11-mir-expected-ingress-semantic p15-s23-c11-ingress-source-path)

(def p15-s23-c11-core-policy-keys
  #{:source-interface :source-policy-binding-id :admission-status
    :verification-scope :source-policy-receipt :compiler-program})
(def p15-s23-c11-core-policy-receipt-keys
  #{:source-interface :admission-status :verification-scope :reader-plan-binding
    :reader-source-revision :reader-result-identity :authenticated-envelope
    :envelope-verification :self-hosted? :expression-binding
    :owner-metadata-content-id :projection-contract})
(def p15-s23-c11-core-program-projection-keys
  [:source-contract-version :source-byte-count :source-content-hash
   :plan-semantic-hash :functions-semantic-hash :builder-semantic-hash :verifier-semantic-hash])

(def p15-s23-c11-core-projection-contract
  {:artifact :gravity/c6-reader-owner-metadata-projection :schema-version 1
   :encoding :owner-canonical-semantic-v1
   :sorted-metadata-policy :default-only :core-carrier-policy :reject})

(defn- p15-s23-c11-core-policy-content-id? [value]
  (and (identical? String (class value))
       (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn p15-s23-c11-mir-require-core-policy-data!
  ([checked-core context policy]
   (p15-s23-c11-mir-require-core-policy-data! checked-core context policy 2))
  ([checked-core context policy version]
  (p15-s23-c11-mir-contract version)
  (let [path (p15-s23-c11-ingress-source-path context)]
    (p15-s23-c11-mir-require-trusted-carrier! path :current-core-policy-data policy :reject)
    (let [receipt (:source-policy-receipt policy)
        compiler (:compiler-program policy)
        contract (p15-s23-c6c10-source-program-contract version)
        header {:interface :gravity/c6-c10 :version version}
        program-keys (cond-> p15-s23-c11-core-program-projection-keys
                       (= 3 version) (conj :semantic-family))
        expected-program (assoc (select-keys contract (rest program-keys))
                                :source-contract-version version)
        envelope (:authenticated-envelope receipt)
        expression (:expression-binding receipt)
        expression-contract c6-source-contract/current-expression-program]
    (p15-s23-c11-mir-require!
     (and (= (cond-> p15-s23-c11-core-policy-keys (= 3 version) (conj :semantic-family)) (set (keys policy)))
          (= (count (cond-> p15-s23-c11-core-policy-keys (= 3 version) (conj :semantic-family))) (count policy))
          (= (cond-> p15-s23-c11-core-policy-receipt-keys (= 3 version) (conj :semantic-family)) (set (keys receipt)))
          (= (count (cond-> p15-s23-c11-core-policy-receipt-keys (= 3 version) (conj :semantic-family))) (count receipt))
          (identical? Long (class (get-in policy [:source-interface :version])))
          (identical? Long (class (:source-contract-version compiler)))
          (identical? Long (class (get-in receipt [:source-interface :version])))
          (= header (:source-interface policy) (:source-interface receipt))
          (if (= 3 version)
            (= :closed-flat-bool-literal-sequence (:semantic-family policy)
               (:semantic-family receipt) (:semantic-family compiler)) true)
          (= :development-unadmitted (:admission-status policy) (:admission-status receipt)
             (get-in envelope [:source-rule :admission-status]))
          (= :selected-current-source-host-seed (:verification-scope policy) (:verification-scope receipt))
          (= :passed (:envelope-verification receipt))
          (false? (:self-hosted? receipt))
          (= p15-s23-c11-core-projection-contract (:projection-contract receipt))
          (identical? Long (class (get-in receipt [:projection-contract :schema-version])))
          (every? p15-s23-c11-core-policy-content-id?
            [(:owner-metadata-content-id receipt)
             (get-in receipt [:reader-plan-binding :instruction-summary-content-id])
             (get-in receipt [:reader-source-revision :function-shapes-content-id])
             (get-in envelope [:source-rule :function-shapes-content-id])
             (get-in envelope [:source-rule :owner-source-rule-content-id])
             (get-in envelope [:gravity-template-replay :checks-content-id])
             (get-in envelope [:gravity-template-replay :diagnostics-content-id])
             (:functions-content-id expression)
             (:declared-plan-compiler-labels-content-id expression)
             (:owner-binding-content-id expression)])
          (= expected-program compiler)
          (= :accepted (:status envelope)) (= :c2-reader (:stage envelope))
          (= #{:actual-sh03-reader-result-id :actual-sh03-semantic-product-binding :actual-c2-artifact-id
               :actual-reader-product-integrity-hash :actual-reader-source-id}
             (set (keys (:reader-result-identity receipt))))
          (every? string? (map (:reader-result-identity receipt)
                              [:actual-sh03-reader-result-id :actual-c2-artifact-id
                               :actual-reader-product-integrity-hash :actual-reader-source-id]))
          (= (get-in c6-source-contract/current-reader-program [:binding :source-content-hash])
             (get-in receipt [:reader-source-revision :source-content-hash]))
          (identical? Long (class (:source-contract-version expression)))
          (= 2 (:source-contract-version expression))
          (= (select-keys expression-contract [:source-content-hash :semantic-hash :functions-semantic-hash :artifact-hash :function-count])
             (select-keys expression [:source-content-hash :semantic-hash :functions-semantic-hash :artifact-hash :function-count]))
          (= (:source-policy-binding-id policy)
             (p15-s23-c6c10-canonical-digest
              path {:domain (if (= 3 version) :gravity/c6-c10-source-policy-v3 :gravity/c6-c10-source-policy-v2)
                    :artifact-id (:artifact-id checked-core)
                    :context context :policy receipt})))
     "C11-MODULE" path {} :current-core-source-policy-data)
    policy))))

(defn p15-s23-c11-mir-core-policy-projection
  ([checked-core context fresh-report]
   (p15-s23-c11-mir-core-policy-projection checked-core context fresh-report 2))
  ([checked-core context fresh-report version]
  ;; A projection, not a saved-report admission API. Callers establish fresh
  ;; authority before projecting; the structural helper only checks coherence.
  (p15-s23-c11-mir-require-trusted-carrier!
   (p15-s23-c11-ingress-source-path context) :fresh-core-report-projection fresh-report :reject)
  (p15-s23-c11-mir-require-core-policy-data!
   checked-core context
   (assoc (select-keys fresh-report
            (cond-> [:source-interface :source-policy-binding-id :admission-status
                     :verification-scope :source-policy-receipt]
              (= 3 version) (conj :semantic-family)))
          :compiler-program (select-keys (:source-binding fresh-report)
                              (cond-> p15-s23-c11-core-program-projection-keys
                                (= 3 version) (conj :semantic-family)))) version)))

(defn p15-s23-c11-mir-core-policy-hash
  ([policy] (p15-s23-c11-mir-core-policy-hash policy 2))
  ([policy version]
  (when policy (p15-s23-c6c10-canonical-digest
                "<c11-core-policy>"
                {:domain (if (= 3 version) :gravity/c11-checked-core-policy-v3 :gravity/c11-checked-core-policy-v2)
                 :policy policy}))))

(defn p15-s23-c11-mir-validate-ingress-data!
  [checked-core context ingress version]
  (when (contains? #{2 3} version)
    (p15-s23-c11-mir-require-trusted-carrier!
     (p15-s23-c11-ingress-source-path context) :core-ingress-policy-data ingress :reject))
  (let [base (p15-s23-c11-mir-expected-ingress-semantic checked-core context)
        policy (:source-policy ingress)]
    (if (contains? #{2 3} version)
      (do
        (p15-s23-c11-mir-require-core-policy-data! checked-core context policy version)
        (p15-s23-c11-mir-require!
         (and (= :gravity-source-pure (:checked-core-ingress-mode base))
              (identical? Long (class (:schema-version ingress)))
              (= (assoc base :schema-version version :source-policy policy) ingress))
         "C11-MODULE" (p15-s23-c11-ingress-source-path context) {} :current-core-ingress-policy-shape))
      (p15-s23-c11-mir-require!
       (= base ingress) "C11-MODULE" (p15-s23-c11-ingress-source-path context) {}
       :authenticated-checked-core-ingress-authority))
    ingress))

(defn p15-s23-c11-mir-core-policy-binding [version policy]
  (when (contains? #{2 3} version)
    (p15-s23-c11-mir-require-trusted-carrier! "<c11-core-policy>" :core-policy-hash-input policy :reject)
    (p15-s23-c11-mir-require!
     (and (map? policy) (string? (:source-policy-binding-id policy)))
     "C11-MODULE" "<c11-core-policy>" {} :current-core-policy-binding-required)
    {:source-policy-binding-id (:source-policy-binding-id policy)
     :source-policy-hash (p15-s23-c11-mir-core-policy-hash policy version)}))
