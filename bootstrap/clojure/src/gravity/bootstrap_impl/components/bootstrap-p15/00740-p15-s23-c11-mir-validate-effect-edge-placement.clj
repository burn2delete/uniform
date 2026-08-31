

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
  (let [nodes (:nodes operation-products)
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
               [[node-id {:value-id node-id
                          :operation-id node-id
                          :block-id block-id}]]
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
                  (get operation-by-id node-id))]]
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
               position-by-id join)
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