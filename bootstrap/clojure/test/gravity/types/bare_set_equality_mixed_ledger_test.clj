(ns gravity.types.bare-set-equality-mixed-ledger-test
  "Current BKA/KSE joins with an unrelated ordinary C7 call.

  This is finite current-owner evidence only.  It does not claim source
  admission, unrestricted Set equality, runtime execution, or migration
  authority."
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh08-function-call-kernel-support :as kernel-support]))

(defn- c7
  [function & arguments]
  (kernel-support/invoke-c7 function (vec arguments)))

(def ^:private lowering-plan
  (delay
    (let [path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
          emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path)))))

(defn- lower
  [function & arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :bare-set-equality-mixed-ledger}
   @lowering-plan function (vec arguments)))

(defn- mixed-input
  [argument-types derive-name constraint-name fact-name]
  (let [base (kernel-support/atomic-equality-input argument-types)
        ordinary-node {:node-id :ordinary-call :core-form :call :children [:ordinary-operator]
                       :source {:syntax-id :ordinary-call :semantic-span nil
                                :origin-chain [] :generated-origin nil}}
        ordinary-call {:core-node-id :ordinary-call :operator-node-id :ordinary-operator
                       :operator-binding-id :ordinary-binding :argument-node-ids []
                       :ordered-evaluation-node-ids [:ordinary-operator]
                       :evaluation-order :operator-then-arguments :result-policy :call-result}
        ordinary-edge {:call-core-node-id :ordinary-call :classification :local-function
                       :caller-function-syntax-id :ordinary-caller
                       :callee-function-syntax-id :ordinary-callee}
        ordinary-nodes {:ordinary-call ordinary-node}
        ordinary-types {:ordinary-call :gravity.type/bool}
        functions {:ordinary-callee {:parameters [] :fixed-arity 0 :return :gravity.type/bool}}
        constrained (c7 'sh08-ft-call-constraints
                        [ordinary-call] [ordinary-edge] ordinary-nodes ordinary-types {}
                        functions)
        ordinary-facts (c7 'sh08-ft-call-facts
                           [ordinary-call] [ordinary-edge] ordinary-nodes ordinary-types
                           functions (:module base))
        core (-> (:core base)
                 (update :calls conj ordinary-call)
                 (update :call-edges conj ordinary-edge)
                 (update :nodes conj ordinary-node))
        nodes (merge (:nodes base) ordinary-nodes)
        types (merge (:types base) ordinary-types)
        derived (c7 derive-name core (:node base) (:call base) (:edge base)
                    nodes (:bindings base) types (:forms base))
        descriptor (:descriptor derived)
        row (c7 constraint-name descriptor)
        fact (c7 fact-name descriptor (:module base))
        ordinary-row (first (:constraints constrained))
        ordinary-fact (first ordinary-facts)
        context {:core core :nodes nodes :forms (:forms base) :bindings (:bindings base)
                 :types (assoc types :operator descriptor :call (:type derived))
                 :calls {:call (:call base) :ordinary-call ordinary-call}
                 :edges {:call (:edge base) :ordinary-call ordinary-edge}
                 :call-order {:call 0 :ordinary-call 1}
                 :call-facts {:call fact :ordinary-call ordinary-fact}
                 :ledger [ordinary-row row]
                 :locals {:arg0 'left :arg1 'right}
                 :parameter-types types}]
    {:context context :descriptor descriptor :derived derived :row row :fact fact
     :ordinary-row ordinary-row :ordinary-fact ordinary-fact :module (:module base)}))

(defn- product
  [kind]
  (if (= kind :bka)
    (mixed-input [:gravity.type/set
                  {:kind :gravity.type/set :shape :literal
                   :element-types [:gravity.type/keyword :gravity.type/keyword]}]
                 'sh08-bka-derive 'sh08-bka-constraint 'sh08-bka-call-fact)
    (mixed-input [{:kind :gravity.type/set :shape :literal
                   :element-types [:gravity.type/keyword :gravity.type/keyword]}
                  {:kind :gravity.type/set :shape :literal
                   :element-types [:gravity.type/keyword]}]
                 'sh08-kse-derive 'sh08-kse-constraint 'sh08-kse-call-fact)))

(defn- compile-product
  [{:keys [context]}]
  (lower 'p15-s23-typed-compile-node context :call))

(deftest current-bare-set-equality-joins-compose-with-ordinary-ledger
  (doseq [kind [:bka :kse]]
    (let [{:keys [context derived row fact ordinary-row ordinary-fact] :as data}
          (product kind)
          expected {:op :builtin-call :function '= :args [{:op :local :name 'left}
                                                           {:op :local :name 'right}]}]
      (is (= :accepted (:status derived)) (pr-str derived))
      (is (not (contains? ordinary-row :producer-rule)))
      (is (not (contains? ordinary-fact :dispatch)))
      (doseq [rows [[ordinary-row row] [row ordinary-row]]
              facts (if (= kind :bka)
                       [[ordinary-fact fact] [fact ordinary-fact]
                        (array-map :ordinary-call ordinary-fact :call fact)
                        (array-map :call fact :ordinary-call ordinary-fact)]
                       [(array-map :ordinary-call ordinary-fact :call fact)
                        (array-map :call fact :ordinary-call ordinary-fact)])]
        (testing (str kind " row/fact order " (count rows) "/" (count facts))
          (let [candidate (assoc context :ledger rows :call-facts facts)
                compiled (compile-product (assoc data :context candidate))]
            (is (= :complete (:status compiled)) (pr-str compiled))
            (is (= expected (:value compiled)))))))))

(deftest current-bare-set-equality-joins-reject-tampering-without-callbacks
  (doseq [kind [:bka :kse]]
    (let [{:keys [context descriptor row fact ordinary-row ordinary-fact module] :as data}
          (product kind)
          mixed (assoc context :ledger [ordinary-row row]
                       :call-facts [ordinary-fact fact])
          callbacks (atom 0)
          opaque (proxy [Object] []
                   (hashCode [] (swap! callbacks inc) (throw (AssertionError. "opaque hash")))
                   (equals [_] (swap! callbacks inc) (throw (AssertionError. "opaque equality")))
                   (toString [] (swap! callbacks inc) (throw (AssertionError. "opaque spelling"))))
          constraint-name (if (= kind :bka) 'sh08-bka-constraint 'sh08-kse-constraint)
          fact-name (if (= kind :bka) 'sh08-bka-call-fact 'sh08-kse-call-fact)
          bad-rows [[nil row]
                    [ordinary-row]
                    [row row]
                    [ordinary-row (assoc-in row [:lhs :core-node-id] :orphan)]]
          bad-facts (if (= kind :bka)
                      [[nil fact]
                       [ordinary-fact (dissoc fact :dispatch)]
                       [fact fact]
                       [ordinary-fact (assoc fact :call-core-node-id :orphan)]]
                      [(array-map :call (dissoc fact :dispatch))
                       (array-map :ordinary-call ordinary-fact)
                       (array-map :ordinary-call ordinary-fact :call (assoc fact :call-core-node-id :orphan))])]
      (doseq [[label rows] (map vector [:nil :missing-target-tag :duplicate-target :orphan] bad-rows)]
        (testing (str kind " bad rows " label)
          (is (= :rejected (:status (compile-product (assoc data :context (assoc mixed :ledger rows))))))))
      (doseq [facts bad-facts]
        (is (= :rejected (:status (compile-product (assoc data :context (assoc mixed :call-facts facts)))))))
      (doseq [changed [(assoc descriptor :version :forged)
                       (assoc descriptor :operand-counts [0 0])
                       (assoc-in descriptor [:source-call :core-node-id] :forged)
                       (assoc-in descriptor [:source-edge :call-core-node-id] :forged)]]
        (let [candidate (-> mixed
                            (assoc-in [:types :operator] changed)
                            (assoc :ledger [ordinary-row (c7 constraint-name changed)]
                                   :call-facts [ordinary-fact (c7 fact-name changed module)]))]
          (is (= :rejected (:status (compile-product (assoc data :context candidate)))))))
      (doseq [value [opaque nil false 0 "forged"]]
        (is (= :rejected
               (:status (compile-product
                         (assoc data :context
                                (assoc mixed :ledger
                                       [ordinary-row (assoc row :producer-rule value)]))))))
        (is (= :rejected
               (:status (compile-product
                         (assoc data :context
                                (assoc mixed :call-facts
                                       [ordinary-fact (assoc fact :dispatch value)])))))))
      (doseq [value [:forged :wrong-family]]
        (is (= :rejected
               (:status (compile-product
                         (assoc data :context
                                (assoc mixed :ledger
                                       [(assoc ordinary-row :producer-rule value) row]))))))
        (is (= :rejected
               (:status (compile-product
                         (assoc data :context
                                (assoc mixed :call-facts
                                       (if (= kind :bka)
                                         [(assoc ordinary-fact :dispatch value) fact]
                                         (array-map :ordinary-call (assoc ordinary-fact :dispatch value)
                                                    :call fact)))))))))
      (is (zero? @callbacks)))))
