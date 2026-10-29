(ns gravity.self-hosting.sh08-inference-convergence-test
  "Semantic controls retained from ACC07's preserved five-control panel.
  Loading this namespace does not load Bootstrap or execute Gravity. The tests
  use the current SH08 harness and characterize internal DATA, not source
  authentication or acceptance of a replacement convergence algorithm.
  C7 and D9 require inference evidence without unsupported proof claims."
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting.sh08-inference-occurs-model-test :as occurs-model]))

(def ^:private harness 'gravity.self-hosting.sh08-function-call-type-test)

(defn- existing [name]
  (require harness)
  (or (ns-resolve harness name)
      (throw (ex-info "Existing SH08 harness helper missing" {:helper name}))))

(defn- invoke [name args]
  ((existing 'invoke-c7) name args))

(defn- reference [id binding]
  {:node-id id :core-form :reference :children []
   :attributes {:binding-id binding}})

(defn- vector-type [element]
  {:kind :gravity.type/vector :element-types [element] :shape :literal})

(defn- pass [nodes lexical bindings]
  (invoke 'sh08-ft-infer-pass-with-context
          [nodes lexical [] [] [] {} bindings {}]))

(defn- infer [nodes lexical bindings]
  ;; Existing API still requires a round argument. This is not a claimed
  ;; convergence bound or a replacement allowance; remove it with that API.
  (invoke 'sh08-ft-infer-acyclic-with-context
          [nodes (into {} (map (juxt :node-id identity)) nodes)
           lexical [] [] [] {} bindings 64 {}]))

(deftest strict-constructor-growth-is-not-a-repeated-state
  ;; Internal DATA deliberately contains lexical feedback. This does not claim
  ;; authenticated Core admits the graph. Every pass uses the real Gravity pass.
  (let [nodes [(reference :r :x)
               {:node-id :v :core-form :collection-literal :children [:r]
                :attributes {:literal-kind :vector}}]
        lexical [{:binding-id :x :initializer-node-id :v}]
        seed {:x :gravity.type/integer}
        observed (loop [left 4 bindings seed states [seed]]
                   (if (zero? left) states
                       (let [result (pass nodes lexical bindings)]
                         (is (empty? (:diagnostics result)))
                         (recur (dec left) (:binding-types result)
                                (conj states (:binding-types result))))))
        expected (take 5 (iterate #(update % :x vector-type) seed))]
    (is (= expected observed))
    (is (= 5 (count (set observed))))))

(deftest lexical-feedback-can-oscillate
  (let [nodes [(reference :rx :x) (reference :ry :y)]
        lexical [{:binding-id :x :initializer-node-id :ry}
                 {:binding-id :y :initializer-node-id :rx}]
        seed {:x :gravity.type/integer :y :gravity.type/string}
        once (pass nodes lexical seed)
        twice (pass nodes lexical (:binding-types once))]
    (is (empty? (:diagnostics once)))
    (is (empty? (:diagnostics twice)))
    (is (= {:x :gravity.type/string :y :gravity.type/integer}
           (:binding-types once)))
    (is (= seed (:binding-types twice)))
    (is (not= seed (:binding-types once)))))

(deftest acyclic-propagation-reaches-the-actual-fixed-point
  (let [{:keys [nodes node-table definitions calls edges function-types
                call-node-ids]} ((existing 'synthetic-chain-input) 5)
        result (invoke 'sh08-ft-infer-acyclic-with-context
                       [nodes node-table [] definitions calls edges
                        function-types {} 64 {}])
        replay (invoke 'sh08-ft-infer-pass-with-context
                       [nodes [] definitions calls edges (:function-types result)
                        (:binding-types result) {}])]
    (is (= :converged (:convergence-status result)))
    (is (empty? (:diagnostics result)))
    (is (every? #(= :gravity.type/integer (get (:type-table result) %))
                call-node-ids))
    (is (= (:binding-types result) (:binding-types replay)))
    (is (= (:type-table result) (:type-table replay)))
    (is (= (:function-types result)
           (invoke 'sh08-ft-update-function-returns
                   [(:function-types replay) node-table (:type-table replay)])))))

(deftest unresolved-fixed-point-does-not-invent-a-type
  (let [nodes [(reference :unresolved :absent)]
        result (infer nodes [] {})]
    (is (= :converged (:convergence-status result)))
    (is (= :gravity.type/unknown (get-in result [:type-table :unresolved])))
    (is (= :unresolved
           (:node-id (invoke 'sh08-ft-first-unknown-node
                             [nodes (:type-table result)]))))))

(deftest declared-return-conflict-survives-inference
  (let [body {:node-id :body :core-form :literal :children []
              :attributes {:literal-kind :string}}
        function {:function-id :fn :body-core-node-id :body
                  :parameter-binding-ids [] :parameters []
                  :return :gravity.type/integer}
        result (invoke 'sh08-ft-infer-acyclic-with-context
                       [[body] {:body body} [] [] [] [] {:fn function} {} 64 {}])]
    (is (= :type-conflict (:convergence-status result)))
    (is (= "C7-TYPE-MISMATCH" (get-in result [:diagnostics 0 :rule])))
    (is (= :function-result-type-mismatch
           (get-in result [:diagnostics 0 :reason])))
    (is (= [:gravity.type/integer :gravity.type/string]
           ((juxt :expected :actual) (first (:diagnostics result)))))))

(deftest unary-vector-occurs-check-matches-closed-equation-controls
  (doseq [case occurs-model/cases]
    (let [{:keys [nodes lexical definitions functions bindings context expected]}
          (occurs-model/input case)
          result (invoke 'sh08-ft-first-constructor-cycle
                         [nodes lexical (or definitions []) (or functions {}) bindings
                          (:type-table (pass nodes lexical bindings)) context])]
      (testing (name (:name case))
        (is (= expected (map? result)))
        (when expected
          (is (seq (:links result)))
          (is (some :constructor? (:links result)))
          (is (= (:binding-id result)
                 (:dependency-binding-id (last (:links result))))))))))

(deftest seeded-constructor-feedback-fails-before-round-exhaustion
  (doseq [case (filter :expected occurs-model/cases)]
    (let [{:keys [nodes lexical bindings]} (occurs-model/input case)
          result (infer nodes lexical bindings)]
      (is (= :type-conflict (:convergence-status result)))
      (is (= "C7-TYPE-MISMATCH" (get-in result [:diagnostics 0 :rule])))
      (is (= :infinite-structural-type (get-in result [:diagnostics 0 :reason])))
      (is (= :finite-normal-structural-type (get-in result [:diagnostics 0 :expected]))))))

(deftest unresolved-and-nonreturning-feedback-retain-existing-semantics
  (doseq [case (filter #(contains? #{:unresolved :never :alias-fixed-point} (:name %)) occurs-model/cases)]
    (let [{:keys [nodes lexical bindings]} (occurs-model/input case)
          result (infer nodes lexical bindings)]
      (is (= :converged (:convergence-status result)))
      (is (empty? (:diagnostics result))))))

(deftest malformed-data-does-not-establish-an-occurs-witness
  ;; No witness is not acceptance: public source/products retain their owning
  ;; resolution/verification checks. These direct helpers bypass that ingress.
  (doseq [case [(first occurs-model/cases) (first occurs-model/set-cases)
               (first occurs-model/map-cases) (first occurs-model/prefix-vector-cases)
               (first occurs-model/fixed-member-cases) (first occurs-model/multiple-dependency-cases)]]
  (let [{:keys [nodes lexical bindings]} (occurs-model/input case)
        check (fn [nodes lexical]
                (invoke 'sh08-ft-first-constructor-cycle
                        [nodes lexical [] {} bindings (:type-table (pass nodes lexical bindings)) {}]))]
    (testing "supplied visibility excludes the forward/self dependency"
      (is (nil? (check nodes (mapv #(assoc % :visible-prior-binding-ids []) lexical)))))
    (testing "parent before child leaves no observed constructor type"
      (is (nil? (check (vec (reverse nodes)) lexical))))
    (testing "duplicate node identity cannot establish an equation graph"
      (is (nil? (check (conj nodes (first nodes)) lexical))))
    (testing "cyclic syntax cannot establish a constructor path"
      (let [id (:initializer-node-id (first lexical))
            cyclic [(assoc (last nodes) :children [id])]]
        (is (nil? (check cyclic lexical))))))))

(deftest declared-return-conflict-precedes-constructor-feedback
  (doseq [case [(first occurs-model/cases) (first occurs-model/set-cases)
               (first occurs-model/map-cases) (first occurs-model/prefix-vector-cases)
               (first occurs-model/fixed-member-cases) (first occurs-model/multiple-dependency-cases)]]
  (let [{:keys [nodes lexical bindings]} (occurs-model/input case)
        body {:node-id :body :core-form :literal :children []
              :attributes {:literal-kind :string}}
        nodes (conj nodes body)
        function {:function-id :fn :body-core-node-id :body
                  :parameter-binding-ids [] :parameters [] :return :gravity.type/integer}
        result (invoke 'sh08-ft-infer-acyclic-with-context
                       [nodes (into {} (map (juxt :node-id identity)) nodes)
                        lexical [] [] [] {:fn function} bindings 64 {}])]
    (is (= :type-conflict (:convergence-status result)))
    (is (= :function-result-type-mismatch (get-in result [:diagnostics 0 :reason]))))))

(deftest unary-set-and-mixed-occurs-check-controls
  (doseq [case occurs-model/set-cases]
    (let [{:keys [nodes lexical definitions functions bindings context expected]}
          (occurs-model/input case)
          result (invoke 'sh08-ft-first-constructor-cycle
                         [nodes lexical (or definitions []) (or functions {}) bindings
                          (:type-table (pass nodes lexical bindings)) context])]
      (testing (name (:name case))
        (is (= expected (map? result)))
        (when expected
          (let [inferred (infer nodes lexical bindings)]
            (is (= :type-conflict (:convergence-status inferred)))
            (is (= :infinite-structural-type (get-in inferred [:diagnostics 0 :reason])))))
        (when (contains? #{:set-unresolved :set-never :set-open-dependency} (:name case))
          (let [inferred (infer nodes lexical bindings)]
            (is (= :converged (:convergence-status inferred)))
            (is (empty? (:diagnostics inferred)))
            (when (= :set-open-dependency (:name case))
              (is (= {:kind :gravity.type/set :element-types [:gravity.type/integer] :shape :literal}
                     (get-in inferred [:binding-types :x]))))))))))

(deftest single-keyword-map-occurs-check-controls
  (doseq [case occurs-model/map-cases]
    (let [{:keys [nodes lexical definitions functions bindings context expected]}
          (occurs-model/input case)
          result (invoke 'sh08-ft-first-constructor-cycle
                         [nodes lexical (or definitions []) (or functions {}) bindings
                          (:type-table (pass nodes lexical bindings)) context])]
      (testing (name (:name case))
        (is (= expected (map? result)))
        (when expected
          (let [inferred (infer nodes lexical bindings)]
            (is (= :type-conflict (:convergence-status inferred)))
            (is (= :infinite-structural-type (get-in inferred [:diagnostics 0 :reason])))))
        (when (contains? #{:map-acyclic :map-unresolved :map-never} (:name case))
          (let [inferred (infer nodes lexical bindings)]
            (is (= :converged (:convergence-status inferred)))
            (is (empty? (:diagnostics inferred)))
            (when (= :map-acyclic (:name case))
              (is (= {:kind :gravity.type/map
                      :member-types [:gravity.type/keyword :gravity.type/integer] :shape :literal}
                     (get-in inferred [:binding-types :x]))))))))))

(deftest map-key-and-value-require-observed-constructor-premises
  (let [{:keys [nodes lexical bindings]} (occurs-model/input (first occurs-model/map-cases))
        types (:type-table (pass nodes lexical bindings))
        key-id (:node-id (first nodes))
        value-id (:node-id (second nodes))
        map-id (:node-id (last nodes))
        check (fn [types] (invoke 'sh08-ft-first-constructor-cycle
                                  [nodes lexical [] {} bindings types {}]))]
    (doseq [changed [(dissoc types key-id) (assoc types key-id :gravity.type/string)
                     (dissoc types value-id) (assoc types value-id :gravity.type/never)
                     (assoc types map-id {:kind :gravity.type/map
                                          :member-types [:gravity.type/keyword :gravity.type/string]
                                          :shape :literal})]]
      (is (nil? (check changed))))))

(deftest keyword-prefix-vector-occurs-check-controls
  (doseq [case occurs-model/prefix-vector-cases]
    (let [{:keys [nodes lexical definitions functions bindings context expected]}
          (occurs-model/input case)
          result (invoke 'sh08-ft-first-constructor-cycle
                         [nodes lexical (or definitions []) (or functions {}) bindings
                          (:type-table (pass nodes lexical bindings)) context])]
      (testing (name (:name case))
        (is (= expected (map? result)))
        (when expected
          (let [inferred (infer nodes lexical bindings)]
            (is (= :type-conflict (:convergence-status inferred)))
            (is (= :infinite-structural-type (get-in inferred [:diagnostics 0 :reason])))))
        (when (contains? #{:prefix-vector-acyclic :prefix-vector-unresolved :prefix-vector-never} (:name case))
          (let [inferred (infer nodes lexical bindings)]
            (is (= :converged (:convergence-status inferred)))
            (is (empty? (:diagnostics inferred)))
            (when (= :prefix-vector-acyclic (:name case))
              (is (= {:kind :gravity.type/vector
                      :element-types [:gravity.type/keyword :gravity.type/integer] :shape :literal}
                     (get-in inferred [:binding-types :x]))))))))))

(deftest vector-prefix-and-value-require-observed-constructor-premises
  (let [{:keys [nodes lexical bindings]} (occurs-model/input (first occurs-model/prefix-vector-cases))
        types (:type-table (pass nodes lexical bindings))
        prefix-id (:node-id (first nodes))
        value-id (:node-id (second nodes))
        vector-id (:node-id (last nodes))
        check (fn [types] (invoke 'sh08-ft-first-constructor-cycle
                                  [nodes lexical [] {} bindings types {}]))]
    (doseq [changed [(dissoc types prefix-id) (assoc types prefix-id :gravity.type/string)
                     (dissoc types value-id) (assoc types value-id :gravity.type/never)
                     (assoc types vector-id {:kind :gravity.type/vector
                                             :element-types [:gravity.type/keyword :gravity.type/string]
                                             :shape :literal})]]
      (is (nil? (check changed))))))

(deftest fixed-member-position-occurs-check-controls
  (doseq [case occurs-model/fixed-member-cases]
    (let [{:keys [nodes lexical definitions functions bindings context expected stable?]} (occurs-model/input case)
          result (invoke 'sh08-ft-first-constructor-cycle
                         [nodes lexical (or definitions []) (or functions {}) bindings
                          (:type-table (pass nodes lexical bindings)) context])]
      (testing (name (:name case))
        (is (= expected (map? result)))
        (when expected
          (let [inferred (infer nodes lexical bindings)]
            (is (= :type-conflict (:convergence-status inferred)))
            (is (= :infinite-structural-type (get-in inferred [:diagnostics 0 :reason])))))
        (when stable?
          (let [inferred (infer nodes lexical bindings)]
            (is (= :converged (:convergence-status inferred)))
            (is (empty? (:diagnostics inferred)))))))))

(deftest fixed-literal-member-premises-require-current-complete-data
  (let [{:keys [nodes lexical bindings]} (occurs-model/input (first occurs-model/fixed-member-cases))
        types (:type-table (pass nodes lexical bindings))
        fixed (filter #(= :literal (:core-form %)) nodes)
        index (into {} (map (juxt :node-id identity)) nodes)]
    (doseq [node fixed]
      (let [id (:node-id node)]
        (is (true? (invoke 'sh08-ft-fixed-literal-member? [id index types])))
        (doseq [changed [(dissoc types id) (assoc types id :gravity.type/never)
                         (assoc types id :gravity.type/unknown)
                         (assoc types id (if (= :gravity.type/string (get types id))
                                           :gravity.type/integer :gravity.type/string))]]
          (is (false? (invoke 'sh08-ft-fixed-literal-member? [id index changed])))
          (is (nil? (invoke 'sh08-ft-first-constructor-cycle
                            [nodes lexical [] {} bindings changed {}]))))
        ;; A forged observed parent type cannot compensate for a missing node.
        (is (nil? (invoke 'sh08-ft-first-constructor-cycle
                          [(vec (remove #(= id (:node-id %)) nodes)) lexical [] {} bindings types {}])))))
    (let [character (first (filter #(= :character (get-in % [:attributes :literal-kind])) fixed))
          id (:node-id character) invalid (assoc-in character [:attributes :value] 65)]
      (is (false? (invoke 'sh08-ft-fixed-literal-member? [id (assoc index id invalid) types]))))))

(deftest multiple-constructor-dependencies-produce-a-closed-path-witness
  (doseq [case occurs-model/multiple-dependency-cases]
    (let [{:keys [nodes lexical bindings context expected stable?]} (occurs-model/input case)
          types (:type-table (pass nodes lexical bindings))
          check #(invoke 'sh08-ft-first-constructor-cycle [nodes lexical [] {} bindings types context])
          witness (check)]
      (testing (name (:name case))
        (is (= expected (map? witness)))
        (is (= witness (check)))
        (when expected
          (let [links (:links witness) inferred (infer nodes lexical bindings)]
            (is (seq links))
            (is (some :constructor? links))
            (is (= (:binding-id witness) (:binding-id (first links))))
            (is (= (:binding-id witness) (:dependency-binding-id (last links))))
            (is (every? true? (map (fn [a b] (= (:dependency-binding-id a) (:binding-id b)))
                                   links (rest links))))
            (is (= :type-conflict (:convergence-status inferred)))
            (is (= :infinite-structural-type (get-in inferred [:diagnostics 0 :reason])))))
        (when stable?
          (let [inferred (infer nodes lexical bindings)]
            (is (= :converged (:convergence-status inferred)))
            (is (empty? (:diagnostics inferred)))))))))

(deftest every-constructor-member-must-have-a-current-premise
  (let [{:keys [nodes lexical bindings]} (occurs-model/input (first occurs-model/multiple-dependency-cases))
        types (:type-table (pass nodes lexical bindings))
        index (into {} (map (juxt :node-id identity)) nodes)
        initializer (:initializer-node-id (first lexical))
        references (filter #(= :reference (:core-form %)) nodes)
        links (invoke 'sh08-ft-literal-collection-links [initializer index bindings types {}])]
    (is (= 2 (count links)))
    (is (= 2 (count (set (map :node-path links)))))
    (doseq [node references
            changed [(dissoc types (:node-id node))
                     (assoc types (:node-id node) :gravity.type/unknown)
                     (assoc types (:node-id node) :gravity.type/never)
                     (assoc types (:node-id node) :gravity.type/string)]]
      (is (nil? (invoke 'sh08-ft-first-constructor-cycle [nodes lexical [] {} bindings changed {}]))))
    ;; A well-typed opaque sibling remains unsupported even beside a real
    ;; self dependency. Recomputing the parent's type cannot authorize it.
    (let [id (:node-id (second references))
          opaque (assoc (second references) :core-form :call)
          changed (mapv #(if (= id (:node-id %)) opaque %) nodes)]
      (is (nil? (invoke 'sh08-ft-literal-collection-links
                        [initializer (into {} (map (juxt :node-id identity)) changed) bindings types {}])))))
  (let [case (second occurs-model/multiple-dependency-cases)
        {:keys [nodes lexical bindings]} (occurs-model/input case)
        ;; Even a visible cyclic member cannot excuse an invisible sibling.
        lexical (assoc-in lexical [0 :visible-prior-binding-ids] [:y])
        types (:type-table (pass nodes lexical bindings))]
    (is (nil? (invoke 'sh08-ft-first-constructor-cycle [nodes lexical [] {} bindings types {}])))))

(deftest definition-alias-constructor-cycles-retain-actual-producer-links
  (doseq [case occurs-model/definition-alias-cases]
    (let [{:keys [nodes lexical definitions functions bindings context expected stable?]}
          (occurs-model/definition-input case)
          functions (or functions {})
          table (into {} (map (juxt :node-id identity)) nodes)
          observed (invoke 'sh08-ft-infer-pass-with-context
                           [nodes lexical definitions [] [] functions bindings context])
          witness (invoke 'sh08-ft-first-constructor-cycle
                          [nodes lexical definitions functions bindings (:type-table observed) context])]
      (testing (name (:name case))
        (is (= expected (map? witness)))
        (when expected
          (is (some :definition-core-node-id (:links witness)))
          (is (some :constructor? (:links witness)))
          (is (= (:binding-id witness) (:dependency-binding-id (last (:links witness)))))
          (let [inferred (invoke 'sh08-ft-infer-acyclic-with-context
                                 [nodes table lexical definitions [] [] functions bindings 64 context])]
            (is (= :type-conflict (:convergence-status inferred)))
            (is (= :infinite-structural-type (get-in inferred [:diagnostics 0 :reason])))))
        (when stable?
          (let [inferred (invoke 'sh08-ft-infer-acyclic-with-context
                                 [nodes table lexical definitions [] [] functions bindings 64 context])]
            (is (= :converged (:convergence-status inferred)))
            (is (empty? (:diagnostics inferred)))))))))

(deftest definition-equations-require-matched-unique-producers
  (let [{:keys [nodes lexical definitions bindings]}
        (occurs-model/definition-input (first occurs-model/definition-alias-cases))
        types (:type-table (invoke 'sh08-ft-infer-pass-with-context
                                   [nodes lexical definitions [] [] {} bindings {}]))
        definition (first definitions) wrapper (:core-node-id definition)
        check (fn [ns ls ds fs]
                (invoke 'sh08-ft-first-constructor-cycle [ns ls ds fs bindings types {}]))]
    (is (nil? (check nodes lexical (conj definitions definition) {})))
    (is (nil? (check nodes [{:binding-id (:binding-id definition)
                            :initializer-node-id (:value-node-id definition)}] definitions {})))
    (is (nil? (check nodes lexical definitions {:f {:parameter-binding-ids [(:binding-id definition)]}})))
    (doseq [changed [(assoc definition :value-node-id :missing)
                     (assoc definition :core-node-id :missing)
                     (assoc definition :binding-id :different)
                     (assoc definition :binding-id nil)]]
      (is (nil? (check nodes lexical (assoc definitions 0 changed) {}))))
    (doseq [changed [(filterv #(not= wrapper (:node-id %)) nodes)
                     (mapv #(if (= wrapper (:node-id %)) (assoc % :children []) %) nodes)
                     (mapv #(if (= wrapper (:node-id %)) (assoc % :resolved-binding-ids [:different]) %) nodes)
                     (mapv #(if (= wrapper (:node-id %)) (assoc % :core-form :call) %) nodes)
                     (mapv #(if (= (:value-node-id (second definitions)) (:node-id %))
                              (assoc % :core-form :fn) %) nodes)]]
      (is (nil? (check changed lexical definitions {}))))))

(deftest declared-return-conflict-precedes-definition-alias-feedback
  (let [{:keys [nodes lexical definitions bindings]}
        (occurs-model/definition-input (first occurs-model/definition-alias-cases))
        body {:node-id :body :core-form :literal :children [] :attributes {:literal-kind :string}}
        nodes (conj nodes body)
        function {:function-id :fn :body-core-node-id :body :parameter-binding-ids []
                  :parameters [] :return :gravity.type/integer}
        inferred (invoke 'sh08-ft-infer-acyclic-with-context
                         [nodes (into {} (map (juxt :node-id identity)) nodes)
                          lexical definitions [] [] {:fn function} bindings 64 {}])]
    (is (= :type-conflict (:convergence-status inferred)))
    (is (= :function-result-type-mismatch (get-in inferred [:diagnostics 0 :reason])))))

(deftest declared-return-conflict-precedes-differing-alias-seed-oscillation
  (let [nodes [(reference :rx :x) (reference :ry :y)
               {:node-id :body :core-form :literal :children [] :attributes {:literal-kind :string}}]
        lexical [{:binding-id :x :initializer-node-id :ry}
                 {:binding-id :y :initializer-node-id :rx}]
        function {:function-id :fn :body-core-node-id :body :parameter-binding-ids []
                  :parameters [] :return :gravity.type/integer}
        inferred (invoke 'sh08-ft-infer-acyclic-with-context
                         [nodes (into {} (map (juxt :node-id identity)) nodes)
                          lexical [] [] [] {:fn function}
                          {:x :gravity.type/integer :y :gravity.type/string} 64 {}])]
    (is (= :type-conflict (:convergence-status inferred)))
    (is (= :function-result-type-mismatch (get-in inferred [:diagnostics 0 :reason])))
    (is (= [:gravity.type/integer :gravity.type/string]
           ((juxt :expected :actual) (first (:diagnostics inferred)))))))

(deftest source-alias-premise-census-includes-unknown-and-definition-producers
  (let [{:keys [nodes lexical context]}
        (occurs-model/input {:equations [[:tail [:ref :x]] [:x [:ref :y]] [:y [:ref :x]]]})
        actual (invoke 'inference-alias-components [nodes lexical [] {} context])]
    (is (= [[:x :y]] (mapv :members actual)))
    (is (= [:tail] (mapv :binding-id (:external-alias-paths (first actual))))))
  (doseq [case (filter #(contains? #{:alias-fixed-point :alias-oscillation :unresolved
                                    :constructor-leading-to-alias-cycle :duplicate-binding :opaque-call} (:name %))
                      occurs-model/cases)]
    (let [{:keys [nodes lexical definitions functions context]} (occurs-model/input case)
          actual (invoke 'inference-alias-components [nodes lexical (or definitions []) (or functions {}) context])]
      (is (= (occurs-model/alias-component-model case) (set (map (comp set :members) actual))))))
  (doseq [case (filter #(contains? #{:definition-alias-fixed-point :definition-alias-oscillation
                                    :definition-alias-unresolved :definition-alias-never} (:name %))
                      occurs-model/definition-alias-cases)]
    (let [{:keys [nodes lexical definitions context]} (occurs-model/definition-input case)
          actual (invoke 'inference-alias-components [nodes lexical definitions {} context])]
      (is (= (occurs-model/alias-component-model case) (set (map (comp set :members) actual)))))))

(deftest alias-source-premise-retains-overridden-constraints-without-normalizing
  (let [function {:function-id :f :parameters [{:binding-id :x :type :gravity.type/unknown}]
                  :signature {:parameters [{:type-id :gravity.type/map}]}}
        functions {:f function} products {:function-records [{:function-syntax-id :f :signature (:signature function)}]}
        imports {:x {:function-type :gravity.type/string :producer-sh07-artifact-id :producer}}
        recursive {:status :accepted :definition {:binding-id :x} :function-syntax-id :f}
        handlers {:seeds {:x :gravity.type/bool :tail :gravity.type/integer}
                  :groups {:handler [{:record {:binding-id :x}} {:record {:binding-id :tail}}]}}
        origins (invoke 'inference-seed-source-premises [products functions imports true false recursive {} handlers])
        component {:members [:x :y] :edges [{:binding-id :x :dependency-binding-id :y}
                                           {:binding-id :y :dependency-binding-id :x}]
                   :external-alias-paths [{:binding-id :tail :edges [{:binding-id :tail :dependency-binding-id :x}]}]}
        observation {:x :gravity.type/integer :y :gravity.type/string}
        base {:status :accepted :diagnostics [] :binding-types observation
              :identity-input {:domain :fixture} :artifact-id-request {:domain :fixture} :typed-core {}}
        retained (invoke 'retain-alias-source-premises [base [component] origins observation])
        premise (first (get-in retained [:convergence :alias-equivalence-premises]))]
    (is (= [function :gravity.type/string :gravity.type/map :gravity.type/bool :gravity.type/integer]
           (mapv :type (:source-seed-conjunction premise))))
    (is (= [:recursive-or-higher-order-premise :imported-function :parameter-declaration :handler-binding :handler-binding]
           (mapv :producer (:source-seed-conjunction premise))))
    (is (= [:component-member :component-member :component-member :component-member :incoming-alias]
           (mapv :role (:source-seed-conjunction premise))))
    (is (= :not-performed (:compatibility-decision premise) (:normalization premise)))
    (is (= observation (:binding-types retained)))
    (is (= :accepted (:status retained)))
    (is (= (:typed-core base) (:typed-core retained)))
    (is (= (get-in retained [:convergence :alias-equivalence-premises])
           (get-in retained [:identity-input :convergence :alias-equivalence-premises])
           (get-in retained [:artifact-id-request :convergence :alias-equivalence-premises])))
    (doseq [changed [(assoc-in retained [:convergence :alias-equivalence-premises] [])
                     (assoc-in retained [:convergence :alias-equivalence-premises 0 :source-seed-conjunction] [])
                     (assoc-in retained [:identity-input :convergence :alias-equivalence-premises] [:forged])]]
      (is (= retained (invoke 'retain-alias-source-premises [changed [component] origins observation]))))
    (let [rejected {:status :rejected :diagnostics [{:reason :function-result-type-mismatch}]}
          result (invoke 'retain-alias-source-premises [rejected [component] origins observation])]
      (is (= (:diagnostics rejected) (:diagnostics result)))
      (is (= :rejected (:status result))))
    (is (= base (invoke 'retain-alias-source-premises [base [] origins observation])))))

(defn- scalar-alias-declaration [binding symbol type]
  {:binding-id binding :type type :producer :parameter-declaration
   :required-relation :declared-parameter-compatibility
   :source-records [{:function-syntax-id :source-function :parameter-binding-ids [binding]
                     :signature {:parameters [{:type-symbol symbol :type-id type}]}}]})

(deftest alias-conjunction-checks-all-source-declarations-without-choosing-a-type
  (let [x (scalar-alias-declaration :x 'I8 :gravity.type/i8)
        y (scalar-alias-declaration :y 'I8 :gravity.type/i8)
        premise {:members [:x :y] :edges [{:reference-node {:node-id :rx}}]
                 :source-seed-conjunction [x y] :provisional-observations [{:binding-id :x :type :gravity.type/string}]}
        checked (invoke 'check-alias-conjunction [premise])
        conflict (assoc premise :source-seed-conjunction [x (scalar-alias-declaration :y 'String :gravity.type/string)])]
    (is (= :compatible-scalar-declarations (get-in checked [:compatibility-decision :status])))
    (is (= [x y] (mapv :constraint (get-in checked [:compatibility-decision :checks]))))
    (is (= (:provisional-observations premise) (:provisional-observations checked)))
    (is (= :unresolved (:normalization checked)))
    (is (= :not-established (:grounding checked)))
    (is (= :conflicting-scalar-declarations
           (get-in (invoke 'check-alias-conjunction [conflict]) [:compatibility-decision :status])))
    (is (= :conflicting-scalar-declarations
           (get-in (invoke 'check-alias-conjunction [(update conflict :source-seed-conjunction #(vec (reverse %)))])
                   [:compatibility-decision :status])))
    (doseq [bad [(assoc x :type :gravity.type/string)
                 (update x :source-records #(conj % (first %)))
                 (assoc x :producer :caller-asserted-authority)
                 (assoc x :required-relation :every-seed-is-equality)]]
      (is (= :invalid-source-premise
             (get-in (invoke 'check-alias-conjunction [(assoc premise :source-seed-conjunction [bad y])])
                     [:compatibility-decision :status]))))
    (let [bare (scalar-alias-declaration :x 'Map :gravity.type/map)
          checked (invoke 'check-alias-conjunction [(assoc premise :source-seed-conjunction [bare y])])]
      (is (= :unsupported (get-in checked [:compatibility-decision :status])))
      (is (= [bare y] (mapv :constraint (get-in checked [:compatibility-decision :checks]))))
      (let [checked (invoke 'check-alias-conjunction [(update conflict :source-seed-conjunction conj bare)])]
        (is (= :conflicting-scalar-declarations (get-in checked [:compatibility-decision :status])))
        (is (true? (get-in checked [:compatibility-decision :unsupported-obligations-retained?])))))
    (let [empty (invoke 'check-alias-conjunction [(assoc premise :source-seed-conjunction [])])]
      (is (= :unconstrained (get-in empty [:compatibility-decision :status])))
      (is (= :unresolved (:normalization empty))))))

(deftest alias-conjunction-diagnostics-preserve-existing-semantic-and-resource-failures
  (let [x (scalar-alias-declaration :x 'I8 :gravity.type/i8)
        y (scalar-alias-declaration :y 'String :gravity.type/string)
        premise (invoke 'check-alias-conjunction
                        [{:members [:x :y] :edges [{:reference-node {:node-id :rx}}]
                          :source-seed-conjunction [x y]}])
        base {:convergence-status :nonconverged :diagnostics [{:reason :bounded-function-type-inference-nonconvergence}]
              :binding-types {:x :gravity.type/string :y :gravity.type/i8}
              :round-count 3 :round-bound 64 :type-join-work-left 10}
        checked (invoke 'apply-alias-conjunction-diagnostic [base [premise]])]
    (is (= :type-conflict (:convergence-status checked)))
    (is (= :alias-mandatory-declaration-conflict (get-in checked [:diagnostics 0 :reason])))
    (is (= (dissoc base :diagnostics :convergence-status) (dissoc checked :diagnostics :convergence-status)))
    (doseq [existing [(assoc base :convergence-status :resource-limit)
                     (assoc base :diagnostics [{:reason :function-result-type-mismatch}])
                     (assoc base :diagnostics [{:reason :typed-handler-source-join}])]]
      (is (= existing (invoke 'apply-alias-conjunction-diagnostic [existing [premise]]))))
    (let [ungrounded (invoke 'check-alias-conjunction [(assoc premise :source-seed-conjunction [])])
          result (invoke 'apply-alias-conjunction-diagnostic [(assoc base :diagnostics []) [ungrounded]])]
      (is (= "C7-ANNOTATION" (get-in result [:diagnostics 0 :rule])))
      (is (= :alias-source-grounding-required (get-in result [:diagnostics 0 :reason]))))))

(deftest alias-conjunction-cached-authority-and-identity-copies-are-reconstructed
  (let [x (scalar-alias-declaration :x 'I8 :gravity.type/i8)
        products {:function-records (:source-records x)}
        functions {:source-function {:parameters [{:binding-id :x}]
                                     :signature (get-in x [:source-records 0 :signature])}}
        origins (invoke 'inference-seed-source-premises [products functions {} false false {} {} {}])
        component {:members [:x :y] :edges [{:binding-id :x :dependency-binding-id :y}
                                           {:binding-id :y :dependency-binding-id :x}]}
        observations {:x :gravity.type/string :y :gravity.type/i8}
        base {:convergence {:status :converged} :identity-input {:domain :fixture}
              :artifact-id-request {:domain :fixture} :binding-types observations}
        expected (invoke 'check-retained-alias-conjunctions [base [component] origins observations])]
    (doseq [forged [(assoc-in expected [:convergence :alias-equivalence-premises 0 :members] [:different])
                    (assoc-in expected [:convergence :alias-equivalence-premises 0 :source-seed-conjunction] [])
                    (assoc-in expected [:convergence :alias-equivalence-premises 0 :normalization] :accepted)
                    (assoc-in expected [:identity-input :convergence] {:authority :caller-approved})]]
      (is (= expected (invoke 'check-retained-alias-conjunctions [forged [component] origins observations]))))
    (is (= observations (:binding-types expected)))
    (is (= (:convergence expected) (get-in expected [:identity-input :convergence])
           (get-in expected [:artifact-id-request :convergence])))))

(deftest imported-and-handler-alias-seeds-require-their-actual-source-records
  (doseq [fixture [(occurs-model/alias-import-source-fixture) (occurs-model/alias-handler-source-fixture)]]
    (let [checked (invoke 'alias-seed-constraint-check [fixture])]
      (is (= (if (= :handler-binding (:producer fixture)) :checked-scalar-handler :unsupported) (:status checked)))
      (is (= fixture (:constraint checked)))
      (is (map? (:source-witness checked))))
    (doseq [changed [(assoc fixture :binding-id "missing") (assoc fixture :type :gravity.type/string)
                     (assoc fixture :source-records {}) (assoc fixture :source-records [])
                     (assoc fixture :producer :caller-authority) (assoc fixture :required-relation :trusted-seed)]]
      (is (= :rejected (:status (invoke 'alias-seed-constraint-check [changed]))))))
  (let [fixture (occurs-model/alias-import-source-fixture) id (:binding-id fixture)]
    (doseq [changed [(assoc-in fixture [:source-records id :producer-sh07-artifact-id] (apply str (repeat 64 \8)))
                     (update-in fixture [:source-records id :producer-identity :function-records] #(conj % (first %)))
                     (update-in fixture [:source-records id :producer-identity :function-type-table] #(conj % (first %)))]]
      (is (= :rejected (:status (invoke 'alias-seed-constraint-check [changed]))))))
  (let [fixture (occurs-model/alias-handler-source-fixture) owner (first (keys (:source-records fixture)))]
    (doseq [changed [(update-in fixture [:source-records owner] #(conj % (first %)))
                     (assoc-in fixture [:source-records owner 0 :record :core-node-id] "wrong")
                     (assoc-in fixture [:source-records owner 0 :handler-core-node-id] "wrong")
                     (assoc-in fixture [:source-records owner 0 :record :catch-clause-syntax-id] "wrong")]]
      (is (= :rejected (:status (invoke 'alias-seed-constraint-check [changed])))))))

(deftest invalid-seed-source-outranks-a-retained-scalar-conflict-without-erasing-it
  (let [scalar [(scalar-alias-declaration :x 'I8 :gravity.type/i8)
                (scalar-alias-declaration :y 'String :gravity.type/string)]
        invalid (assoc (occurs-model/alias-handler-source-fixture) :type :gravity.type/string)
        premise (invoke 'check-alias-conjunction
                        [{:members [:x :y] :edges [{:reference-node {:node-id :rx}}]
                          :source-seed-conjunction (conj scalar invalid)}])
        base {:convergence-status :converged :diagnostics [] :binding-types {:x :gravity.type/string}}
        checked (invoke 'apply-alias-conjunction-diagnostic [base [premise]])]
    (is (= :invalid-source-premise (get-in premise [:compatibility-decision :status])))
    (is (map? (get-in premise [:compatibility-decision :scalar-conflict])))
    (is (= 3 (count (get-in premise [:compatibility-decision :checks]))))
    (is (= "C7-VERIFY" (get-in checked [:diagnostics 0 :rule])))
    (is (= (:binding-types base) (:binding-types checked)))
    (doseq [existing [(assoc base :convergence-status :resource-limit)
                     (assoc base :diagnostics [{:reason :function-result-type-mismatch}])]]
      (is (= existing (invoke 'apply-alias-conjunction-diagnostic [existing [premise]]))))))

(deftest scalar-handler-conjunction-checks-do-not-normalize-bindings
  (let [a (occurs-model/atomic-handler-constraint :gravity.type/i8 \0)
        b (occurs-model/atomic-handler-constraint :gravity.type/string \5)
        declaration (scalar-alias-declaration :p 'I8 :gravity.type/i8)
        observations {(:binding-id a) :gravity.type/string (:binding-id b) :gravity.type/i8}
        base {:members [(:binding-id a) (:binding-id b)] :edges [{:reference-node {:node-id :rx}}]
              :provisional-observations observations}
        cases [[[a declaration] :compatible-scalar-declarations]
               [[a b] :conflicting-scalar-declarations]
               [[b a] :conflicting-scalar-declarations]
               [[declaration b] :conflicting-scalar-declarations]
               [[a (occurs-model/alias-import-source-fixture) b] :conflicting-scalar-declarations]
               [[a b (assoc a :required-relation :trusted)] :invalid-source-premise]
               [[a (occurs-model/alias-import-source-fixture)] :unsupported]]]
    (doseq [[constraints status] cases]
      (let [checked (invoke 'check-alias-conjunction [(assoc base :source-seed-conjunction constraints)])
            result (invoke 'apply-alias-conjunction-diagnostic
                           [{:convergence-status :converged :diagnostics [] :binding-types observations} [checked]])]
        (is (= status (get-in checked [:compatibility-decision :status])))
        (is (= constraints (mapv :constraint (get-in checked [:compatibility-decision :checks]))))
        (is (= observations (:provisional-observations checked) (:binding-types result)))
        (is (= :unresolved (:normalization checked)))
        (is (= :not-established (:grounding checked)))
        (is (= (case status :invalid-source-premise "C7-VERIFY" :conflicting-scalar-declarations "C7-TYPE-MISMATCH" "C7-ANNOTATION")
               (get-in result [:diagnostics 0 :rule])))))
    (let [checked (invoke 'check-alias-conjunction [(assoc base :source-seed-conjunction [a b])])]
      (doseq [original [{:convergence-status :resource-limit :diagnostics [] :binding-types observations}
                        {:convergence-status :converged :diagnostics [{:reason :function-result-type-mismatch}] :binding-types observations}]]
        (is (= original (invoke 'apply-alias-conjunction-diagnostic [original [checked]])))))
    (doseq [type [:gravity.type/i8 :gravity.type/i16 :gravity.type/i32 :gravity.type/i64
                  :gravity.type/u8 :gravity.type/u16 :gravity.type/u32 :gravity.type/u64
                  :gravity.type/f32 :gravity.type/f64 :gravity.type/bool :gravity.type/string
                  :gravity.type/symbol :gravity.type/keyword :gravity.type/ratio]]
      (is (= :checked-scalar-handler
             (:status (invoke 'alias-seed-constraint-check [(occurs-model/atomic-handler-constraint type \0)])))))
    (doseq [type [:gravity.type/unknown :gravity.type/never :gravity.type/nil :gravity.type/integer
                  :gravity.type/map :gravity.type/list {:kind :gravity.type/vector :member-types []}]]
      (is (= :unsupported
             (:status (invoke 'alias-seed-constraint-check [(occurs-model/atomic-handler-constraint type \0)])))))))
