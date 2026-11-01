(ns gravity.self-hosting.sh08-unmetered-convergence-contract-test
  "Semantic controls retained from ACC07's preserved five-control panel.
  Loading this namespace does not load Bootstrap or execute Gravity. The tests
  use the current SH08 harness and characterize internal DATA, not source
  authentication or acceptance of a replacement convergence algorithm.
  C7 and D9 require inference evidence without unsupported proof claims."
  (:require [clojure.test :refer [deftest is]]))

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
  ;; Current unmetered API has no round argument. These fixtures are internal DATA.
  (invoke 'sh08-ft-infer-acyclic-with-context
          [nodes (into {} (map (juxt :node-id identity)) nodes)
           lexical [] [] [] {} bindings {}]))

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
                        function-types {} {}])
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
                       [[body] {:body body} [] [] [] [] {:fn function} {} {}])]
    (is (= :type-conflict (:convergence-status result)))
    (is (= "C7-TYPE-MISMATCH" (get-in result [:diagnostics 0 :rule])))
    (is (= :function-result-type-mismatch
           (get-in result [:diagnostics 0 :reason])))
    (is (= [:gravity.type/integer :gravity.type/string]
           ((juxt :expected :actual) (first (:diagnostics result)))))))
