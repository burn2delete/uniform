(ns gravity.self-hosting.sh08-function-call-kernels-test
  "Cheap normalized SH-08 function/call kernels.

  These checks deliberately construct normalized Gravity data and invoke the
  current C7 leaf.  They do not load the authenticated source-composition
  namespace; source, provenance, and authentication composition tests remain
  in sh08-function-call-type-test."
  (:require [clojure.test :refer [deftest is]]
            [gravity.self-hosting.sh08-function-call-kernel-support :as support]))

(def ^:private invoke-c7 support/invoke-c7)
(def ^:private nonnegative-integer-quotient-input
  support/nonnegative-integer-quotient-input)
(def ^:private nonnegative-integer-quotient-transfer-input
  support/nonnegative-integer-quotient-transfer-input)

(defn run-sh08-nonnegative-integer-quotient-literal-development-kernel
  []
  (doseq [values [[0 10] [127 10] [10 3] [(bigint "123456789012345678901234567890") 10]]]
    (let [{:keys [core nodes forms bindings products binding-types]} (nonnegative-integer-quotient-input values)
          context {:ordering-core core :lookup-nodes nodes :lookup-forms (vec (vals forms))
                   :lookup-bindings (vec (vals bindings)) :shape-call-products products}
          inferred (invoke-c7 'sh08-ft-infer-acyclic-with-context
                     [(:nodes core) nodes [] [] (:calls core) (:call-edges core) {} binding-types context])
          descriptor (get-in inferred [:type-table :operator])]
      (is (= :converged (:convergence-status inferred)))
      (is (= [] (:diagnostics inferred)))
      (is (= :gravity.type/integer (get-in inferred [:type-table :call])))
      (is (= :gravity.type/applied-nonnegative-integer-quotient (:kind descriptor)))
      (is (= :proof-required (:numeric-mode descriptor)))
      (is (= [:memory/allocate] (:direct-effects descriptor)))
      (is (= :may-allocate-integer-result (get-in descriptor [:storage-policy :allocation])))
      (is (= :nonnegative-euclidean-quotient (get-in descriptor [:quotient-policy :operation]))))))

(defn run-sh08-nonnegative-integer-quotient-transfer-and-id-development-kernel
  []
  (let [derive (fn [input] (invoke-c7 'sh08-nq-derive [(:core input) (:node input) (:call input) (:edge input)
                                                          (:nodes input) (:bindings input) (:types input) (:forms input) ]))
        base (nonnegative-integer-quotient-input [127 10])]
    (doseq [position [0 1]]
      (let [input (nonnegative-integer-quotient-transfer-input [127 10] position)
            result (derive input) descriptor (:descriptor result)]
        (is (= :accepted (:status result)))
        (is (= :gravity.type/never (:type result)))
        (is (= :not-invoked (:application descriptor)))
        (is (= [] (:direct-effects descriptor)))

        (is (= :accepted (:status (invoke-c7 'sh08-nq-inferred-call
                                    [(:core input) (:node input) (:call input) (:edge input) (:nodes input) (:bindings input)
                                     (assoc (:types input) :operator descriptor) ]))))))
    (is (= :rejected (:status (derive (nonnegative-integer-quotient-transfer-input [127 0] 0)))))
    (is (= :rejected (:status (derive (nonnegative-integer-quotient-transfer-input [-1 10] 1)))))
    (doseq [values [[] [1] [1 2 3]]]
      (is (= :nonnegative-integer-quotient-arity-mismatch (:reason (derive (nonnegative-integer-quotient-input values))))))
    (let [zero (derive (nonnegative-integer-quotient-input [127 0]))]
      (is (= "STD5002" (:rule zero)))
      (is (= :nonnegative-integer-quotient-zero-divisor (:reason zero)))
      (is (= {:operation 'quot :numeric-mode :proof-required :dividend :source-proven-nonnegative-Integer
              :divisor :source-proven-positive-Integer-literal} (:expected zero))))
    (let [callbacks (atom 0) opaque (proxy [Object] []
                                   (hashCode [] (swap! callbacks inc) (throw (AssertionError. "quotient metadata hash")))
                                   (equals [_] (swap! callbacks inc) (throw (AssertionError. "quotient metadata equality")))
                                   (toString [] (swap! callbacks inc) (throw (AssertionError. "quotient metadata printing"))))]
      (doseq [path [[:call :core-node-id] [:call :operator-node-id] [:call :operator-binding-id]
                    [:call :argument-node-ids 0] [:node :node-id] [:node :children 0]
                    [:node :source :form-id] [:nodes :operator :source :syntax-id]
                    [:nodes :operator :attributes :symbol] [:nodes :arg0 :attributes :value]
                    [:forms :arg1 :value] [:edge :ordinal]]]
        (is (= :rejected (:status (derive (assoc-in base path opaque)))))
        (is (zero? @callbacks))))))

(deftest sh08-nonnegative-integer-quotient-literal-development-kernel
  (run-sh08-nonnegative-integer-quotient-literal-development-kernel))

(deftest sh08-nonnegative-integer-quotient-transfer-and-id-development-kernel
  (run-sh08-nonnegative-integer-quotient-transfer-and-id-development-kernel))
