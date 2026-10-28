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
  (doseq [case [(first occurs-model/cases) (first occurs-model/set-cases)]]
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
  (doseq [case [(first occurs-model/cases) (first occurs-model/set-cases)]]
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
