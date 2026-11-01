(ns gravity.self-hosting.sh09-unary-local-call-census-test
  "ST/LQ census compatibility with current C7 ordinary local-call products."
  (:require [clojure.test :refer [deftest is]]
            [gravity.self-hosting.sh08-function-call-kernel-support :as kernel-support]))

(defn- leaf [function & arguments]
  (apply kernel-support/leaf function arguments))
(defn- sha [n] (str "sha256:" (format "%064x" n)))

(defn- mixed-ordinary-local-call-effect-input [base derive-name constraint-name fact-name]
  ;; Normalize only the family call; the unrelated caller row/fact comes from
  ;; the current owning C7 functions, not a hand-written producer/dispatch tag.
  (let [c7 kernel-support/invoke-c7 ids (zipmap (keys (:nodes base)) (map sha (range 1600 1610)))
        convert (fn [node] (-> node (update :node-id ids) (update :children #(mapv ids %))
                              (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
        node-list (mapv convert (get-in base [:core :nodes])) node (last node-list)
        call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                 (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
        edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                 (update :ordered-evaluation-node-ids #(mapv ids %)))
        entry {:core-node-id (sha 1610) :operator-node-id (sha 1611) :operator-binding-id (sha 1612)
               :argument-node-ids [(sha 1613) (sha 1614)]
               :ordered-evaluation-node-ids [(sha 1611) (sha 1613) (sha 1614)]
               :evaluation-order :operator-then-arguments :result-policy :call-result}
        entry-edge {:ordinal 1 :call-core-node-id (sha 1610) :callee-binding-id (sha 1612)
                    :callee-function-syntax-id (sha 1615) :classification :local-function}
        entry-node {:node-id (sha 1610) :core-form :call :children (:ordered-evaluation-node-ids entry)
                    :source {:form-id (sha 1610) :syntax-id (sha 1610) :semantic-span nil :origin-chain [] :generated-origin nil}}
        nodes (into {} (map (juxt :node-id identity) (conj node-list entry-node)))
        bindings (assoc (:bindings base) (sha 1612)
                        {:binding-id (sha 1612) :name 'compare-sets :binding-class :namespace :kind :function})
        core (assoc (:core base) :nodes (vec (vals nodes)) :calls [call entry] :call-edges [edge entry-edge])
        types (assoc (into {} (map (fn [[id type]] [(ids id) type]) (:types base)))
                     (sha 1613) :gravity.type/set (sha 1614) :gravity.type/set)
        derived (c7 derive-name [core node call edge nodes bindings types (:forms base)])
        descriptor (:descriptor derived)
        types (assoc types (:operator-node-id call) descriptor (:node-id node) (:type derived) (sha 1610) :gravity.type/bool)
        functions {(sha 1615) {:function-id (sha 1615) :definition-name 'compare-sets :fixed-arity 2
                               :parameters [{:binding-id (sha 1616) :type :gravity.type/set}
                                            {:binding-id (sha 1617) :type :gravity.type/set}]
                               :return :gravity.type/bool :latent-effects [] :capabilities []}}
        ordinary (c7 'sh08-ft-call-constraints-with-context
                     [[entry] [entry-edge] nodes types {} functions
                     {:ordering-core core :ordering-bindings bindings :lookup-bindings (vec (vals bindings))}])
        ordinary-row (first (:constraints ordinary))
        ordinary-fact (first (c7 'sh08-ft-call-facts [[entry] [entry-edge] nodes types functions (:module core)]))
        row (c7 constraint-name [descriptor]) fact (c7 fact-name [descriptor (:module core)])]
    {:core core :node node :call call :edge edge :nodes nodes :bindings bindings :ids ids
     :derived derived :descriptor descriptor :ordinary ordinary :ordinary-row ordinary-row :ordinary-fact ordinary-fact
     :row row :fact fact :constraint-name constraint-name :fact-name fact-name
     :request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))}}}
     :facts {:module (:module core) :type-table types :function-type-table []
             :constraint-ledger [ordinary-row row] :call-type-facts [ordinary-fact fact]}}))

(deftest body-list-set-local-call-composition-development-kernel
  (let [base (kernel-support/list-set-input {:kind :gravity.type/list :shape :homogeneous :element-type :gravity.type/keyword})
        {:keys [core node call edge nodes bindings derived descriptor ordinary ordinary-row ordinary-fact row fact request facts]}
        (mixed-ordinary-local-call-effect-input base 'sh08-st-derive 'sh08-st-constraint 'sh08-st-call-fact)
        c7 kernel-support/invoke-c7 prepare #(leaf 'body-st-context request %1 bindings)
        prepared (prepare facts)
        checked (assoc facts :set-conversion-context (:context prepared))
        valid? #(leaf 'body-st-call-valid? core % bindings node call edge nodes)
        coherent (fn [changed]
                   (-> facts (assoc-in [:type-table (:operator-node-id call)] changed)
                       (assoc :constraint-ledger [ordinary-row (c7 'sh08-st-constraint [changed])]
                              :call-type-facts [ordinary-fact (c7 'sh08-st-call-fact [changed (:module core)])])))]
    (is (= :accepted (:status derived)))
    (is (= [] (:diagnostics ordinary)))
    (is (= 1 (count (:constraints ordinary))))
    (is (not (contains? ordinary-row :producer-rule)))
    (is (not (contains? ordinary-fact :dispatch)))
    (is (= :accepted (:status prepared)) (pr-str (select-keys prepared [:status :reason])))
    (is (true? (valid? checked)))
    (prn {:mixed-list-set-context (select-keys prepared [:status :reason])})
    (doseq [ordered [(assoc facts :constraint-ledger [row ordinary-row])
                     (assoc facts :call-type-facts [fact ordinary-fact])
                     (assoc facts :call-type-facts {(sha 1610) ordinary-fact (:node-id node) fact})]]
      (is (= :accepted (:status (prepare ordered)))))
    (doseq [bad [nil "untyped" []]]
      (is (= :rejected (:status (prepare (assoc-in facts [:constraint-ledger 0 :producer-rule] bad)))))
      (is (= :rejected (:status (prepare (assoc-in facts [:call-type-facts 0 :dispatch] bad)))))
    (doseq [changed [(assoc facts :constraint-ledger [nil row]) (assoc facts :call-type-facts [nil fact])
                     (assoc facts :constraint-ledger [ordinary-row (dissoc row :producer-rule)])
                     (assoc facts :call-type-facts [ordinary-fact (dissoc fact :dispatch)])
                     (update facts :constraint-ledger conj row) (update facts :call-type-facts conj fact)
                     (update facts :constraint-ledger conj (c7 'sh08-st-constraint [(assoc descriptor :call-core-node-id (sha 1699))]))
                     (update facts :call-type-facts conj (c7 'sh08-st-call-fact [(assoc descriptor :call-core-node-id (sha 1699)) (:module core)]))]]
      (is (= :rejected (:status (prepare changed)))))
    (let [callbacks (atom 0)
          opaque (proxy [Object] []
                   (hashCode [] (swap! callbacks inc) (throw (AssertionError. "Mixed list-set tag hash")))
                   (equals [_] (swap! callbacks inc) (throw (AssertionError. "Mixed list-set tag equality")))
                   (toString [] (swap! callbacks inc) (throw (AssertionError. "Mixed list-set tag printing"))))]
      (doseq [changed [(assoc-in facts [:constraint-ledger 0 :producer-rule] opaque)
                       (assoc-in facts [:call-type-facts 0 :dispatch] opaque)
                       (assoc-in facts [:call-type-facts 0 :call-core-node-id] opaque)
                       (assoc facts :call-type-facts (array-map opaque ordinary-fact (:node-id node) fact))]]
        (is (= :rejected (:status (prepare changed))))
        (is (zero? @callbacks))))
    (doseq [changed [(assoc descriptor :version :unknown)
                     (assoc-in descriptor [:source :syntax-id] :stale)
                     (assoc-in descriptor [:source-edge :caller-function-syntax-id] :stale)
                     (assoc-in descriptor [:source-binding :definition-syntax-id] :stale)
                     (assoc-in descriptor [:source-call :ordered-evaluation-node-ids] (vec (reverse (:children node))))]]
      (is (= :rejected (:status (prepare (coherent changed))))))
    (doseq [changed [(assoc-in request [:canonical-core-artifact :call-edges 0 :caller-function-syntax-id] :stale)
                     (assoc-in request [:canonical-core-artifact :calls 0 :ordered-evaluation-node-ids] (vec (reverse (:children node))))]]
      (is (= :rejected (:status (leaf 'body-st-context changed facts bindings)))))
    (let [stale (assoc-in checked [:set-conversion-context :forms :arg0 :parent-form-id] :stale)]
      (is (false? (valid? stale))))
    (is (false? (leaf 'body-st-call-valid? core checked
                 (assoc-in bindings [:ordering-binding :definition-syntax-id] :stale) node call edge nodes))))))

(deftest body-map-keys-local-call-composition-development-kernel
  (doseq [members [[] [:gravity.type/keyword :gravity.type/integer]]]
  (let [base (kernel-support/map-keys-input {:kind :gravity.type/map :shape :literal :member-types members})
        {:keys [core node call edge nodes bindings derived descriptor ordinary ordinary-row ordinary-fact row fact request facts]}
        (mixed-ordinary-local-call-effect-input base 'sh08-lq-derive 'sh08-lq-constraint 'sh08-lq-call-fact)
        c7 kernel-support/invoke-c7 prepare #(leaf 'body-lq-context request %1 bindings)
        prepared (prepare facts)
        checked (assoc facts :list-query-context (:context prepared))
        valid? #(leaf 'body-lq-call-valid? core % bindings node call edge nodes)
        coherent (fn [changed]
                   (-> facts (assoc-in [:type-table (:operator-node-id call)] changed)
                       (assoc :constraint-ledger [ordinary-row (c7 'sh08-lq-constraint [changed])]
                              :call-type-facts [ordinary-fact (c7 'sh08-lq-call-fact [changed (:module core)])])))]
    (is (= :accepted (:status derived)))
    (is (= [] (:diagnostics ordinary)))
    (is (= 1 (count (:constraints ordinary))))
    (is (not (contains? ordinary-row :producer-rule)))
    (is (not (contains? ordinary-fact :dispatch)))
    (is (= :accepted (:status prepared)) (pr-str (select-keys prepared [:status :reason])))
    (is (true? (valid? checked)))
    (prn {:mixed-map-keys-context (select-keys prepared [:status :reason]) :member-types members})
    (doseq [ordered [(assoc facts :constraint-ledger [row ordinary-row])
                     (assoc facts :call-type-facts [fact ordinary-fact])
                     (assoc facts :call-type-facts {(sha 1610) ordinary-fact (:node-id node) fact})]]
      (is (= :accepted (:status (prepare ordered)))))
    (doseq [bad [nil "untyped" []]]
      (is (= :rejected (:status (prepare (assoc-in facts [:constraint-ledger 0 :producer-rule] bad)))))
      (is (= :rejected (:status (prepare (assoc-in facts [:call-type-facts 0 :dispatch] bad)))))
    (doseq [changed [(assoc facts :constraint-ledger [nil row]) (assoc facts :call-type-facts [nil fact])
                     (assoc facts :constraint-ledger [ordinary-row (dissoc row :producer-rule)])
                     (assoc facts :call-type-facts [ordinary-fact (dissoc fact :dispatch)])
                     (update facts :constraint-ledger conj row) (update facts :call-type-facts conj fact)
                     (update facts :constraint-ledger conj (c7 'sh08-lq-constraint [(assoc descriptor :call-core-node-id (sha 1699))]))
                     (update facts :call-type-facts conj (c7 'sh08-lq-call-fact [(assoc descriptor :call-core-node-id (sha 1699)) (:module core)]))]]
      (is (= :rejected (:status (prepare changed)))))
    (let [callbacks (atom 0)
          opaque (proxy [Object] []
                   (hashCode [] (swap! callbacks inc) (throw (AssertionError. "Mixed map-keys tag hash")))
                   (equals [_] (swap! callbacks inc) (throw (AssertionError. "Mixed map-keys tag equality")))
                   (toString [] (swap! callbacks inc) (throw (AssertionError. "Mixed map-keys tag printing"))))]
      (doseq [changed [(assoc-in facts [:constraint-ledger 0 :producer-rule] opaque)
                       (assoc-in facts [:call-type-facts 0 :dispatch] opaque)
                       (assoc-in facts [:call-type-facts 0 :call-core-node-id] opaque)
                       (assoc facts :call-type-facts (array-map opaque ordinary-fact (:node-id node) fact))]]
        (is (= :rejected (:status (prepare changed))))
        (is (zero? @callbacks))))
    (doseq [changed [(assoc descriptor :version :unknown)
                     (assoc-in descriptor [:source :syntax-id] :stale)
                     (assoc-in descriptor [:source-edge :caller-function-syntax-id] :stale)
                     (assoc-in descriptor [:source-binding :definition-syntax-id] :stale)
                     (assoc-in descriptor [:source-call :ordered-evaluation-node-ids] (vec (reverse (:children node))))]]
      (is (= :rejected (:status (prepare (coherent changed))))))
    (doseq [changed [(assoc-in request [:canonical-core-artifact :call-edges 0 :caller-function-syntax-id] :stale)
                     (assoc-in request [:canonical-core-artifact :calls 0 :ordered-evaluation-node-ids] (vec (reverse (:children node))))]]
      (is (= :rejected (:status (leaf 'body-lq-context changed facts bindings)))))
    (let [stale (assoc-in checked [:list-query-context :forms :arg0 :parent-form-id] :stale)]
      (is (false? (valid? stale))))
    (is (false? (leaf 'body-lq-call-valid? core checked
                 (assoc-in bindings [:ordering-binding :definition-syntax-id] :stale) node call edge nodes)))))))
