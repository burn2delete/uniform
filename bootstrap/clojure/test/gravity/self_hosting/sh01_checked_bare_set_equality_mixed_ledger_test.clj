(ns gravity.self-hosting.sh01-checked-bare-set-equality-mixed-ledger-test
  "Actual C7 producer rows consumed by the current DSE lowering census.

  These are normalized DATA kernel checks, not source admission, Effects/C8,
  whole-module assembly, runtime execution, or migration evidence."
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh08-function-call-type-test]))

(defn- helper [name]
  (or (ns-resolve 'gravity.self-hosting.sh08-function-call-type-test name)
      (throw (ex-info "Required current C7 helper is absent" {:name name}))))

(defn- c7 [function & arguments]
  ((helper 'invoke-c7) function (vec arguments)))

(def ^:private lowering-plan
  (delay
    (let [path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
          emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path)))))

(defn- lower [function & arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :checked-bare-set-mixed-ledger-kernel} @lowering-plan function (vec arguments)))

(defn- products []
  (let [input ((helper 'atomic-equality-input) [:gravity.type/set :gravity.type/set])
        module (:module input)
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
        constrained (c7 'sh08-ft-call-constraints [ordinary-call] [ordinary-edge]
                        ordinary-nodes ordinary-types {} functions)
        facts (c7 'sh08-ft-call-facts [ordinary-call] [ordinary-edge]
                  ordinary-nodes ordinary-types functions module)
        core (-> (:core input) (update :calls conj ordinary-call)
                 (update :call-edges conj ordinary-edge) (update :nodes conj ordinary-node))
        nodes (merge (:nodes input) ordinary-nodes)
        types (merge (:types input) ordinary-types)
        derived (c7 'sh08-dse-derive core (:node input) (:call input) (:edge input)
                    nodes (:bindings input) types (:forms input) 65536)
        descriptor (:descriptor derived)
        dse-row (c7 'sh08-dse-constraint descriptor)
        dse-fact (c7 'sh08-dse-call-fact descriptor module)
        context {:core core :nodes nodes :forms (:forms input) :bindings (:bindings input)
                 :types (assoc types :operator descriptor :call (:type derived))
                 :calls {:call (:call input) :ordinary-call ordinary-call}
                 :edges {:call (:edge input) :ordinary-call ordinary-edge}
                 :call-order {:call 0 :ordinary-call 1}
                 :locals {:arg0 'left :arg1 'right} :parameter-types types
                 :ledger [dse-row] :call-facts {:call dse-fact}}]
    {:context context :descriptor descriptor :derived derived :constrained constrained
     :ordinary-row (first (:constraints constrained)) :ordinary-fact (first facts)
     :dse-row dse-row :dse-fact dse-fact}))

(defn- row-census [context ledger descriptor budget]
  (lower 'p15-s23-dse-row-census (:core context) ledger (:nodes context)
         (:bindings context) (:types context) (:calls context) (:edges context)
         :call descriptor budget))

(defn- fact-census [context facts descriptor budget]
  (lower 'p15-s23-dse-fact-census facts (get-in context [:core :module])
         {:call descriptor} :call budget))

(defn- compile-node [context budget]
  (lower 'p15-s23-typed-compile-node context :call budget))

(deftest actual-c7-ordinary-rows-compose-with-dse-in-both-orders
  (let [{:keys [context descriptor derived constrained ordinary-row ordinary-fact dse-row dse-fact]}
        (products)
        expected {:op :builtin-call :function '=
                  :args [{:op :local :name 'left} {:op :local :name 'right}]}]
    (is (= :accepted (:status derived)))
    (is (empty? (:diagnostics constrained)))
    (is (= 1 (count (:constraints constrained))))
    (is (map? ordinary-row))
    (is (not (contains? ordinary-row :producer-rule)))
    (is (= :ordinary-call (:call-core-node-id ordinary-fact)))
    (is (not (contains? ordinary-fact :dispatch)))
    (doseq [rows [[ordinary-row dse-row] [dse-row ordinary-row]]
            facts [[ordinary-fact dse-fact] [dse-fact ordinary-fact]
                   (array-map :ordinary-call ordinary-fact :call dse-fact)
                   (array-map :call dse-fact :ordinary-call ordinary-fact)]]
      (testing (str "row/fact counts " (count rows) "/" (count facts))
        (let [candidate (assoc context :ledger rows :call-facts facts)
              row-result (row-census candidate rows descriptor 65536)
              fact-result (fact-census candidate facts descriptor 65536)
              compiled (compile-node candidate 65536)]
          (println (pr-str {:mixed-ledger-kernel
                            {:row (select-keys row-result [:status :reason :work-count])
                             :fact (select-keys fact-result [:status :reason :work-count])
                             :lowering-status (:status compiled)
                             :lowering-reason (get-in compiled [:facts :reason])}}))
          (is (= :accepted (:status row-result)))
          (is (true? (= [dse-row] (:items row-result))))
          (is (= :accepted (:status fact-result)))
          (is (true? (= [dse-fact] (:items fact-result))))
          (is (= :complete (:status compiled)))
          (is (= expected (:value compiled))))))))

(deftest mixed-ledger-preserves-exact-payment-and-local-limit
  (let [{:keys [context descriptor ordinary-row ordinary-fact dse-row dse-fact]} (products)
        rows [ordinary-row dse-row] facts [ordinary-fact dse-fact]
        row-result (row-census context rows descriptor 65536)
        fact-result (fact-census context facts descriptor 65536)
        candidate (assoc context :ledger rows :call-facts facts)
        result (compile-node candidate 65536)]
    (is (= 8 (- (:work-count row-result)
                (:work-count (row-census context [dse-row] descriptor 65536)))))
    (is (= 6 (- (:work-count fact-result)
                (:work-count (fact-census context [dse-fact] descriptor 65536)))))
    (doseq [[function entries result]
            [[row-census rows row-result] [fact-census facts fact-result]]]
      (is (= result (function context entries descriptor (:work-count result))))
      (is (= :loop-type-work-limit
             (:reason (function context entries descriptor (dec (:work-count result)))))))
    (is (= :complete (:status result)))
    (is (= result (compile-node candidate (:work-count result))))
    (is (= :typed-lowering-work-limit
           (get-in (compile-node candidate (dec (:work-count result))) [:facts :reason])))))

(deftest malformed-and-forged-dse-joins-remain-rejected
  (let [{:keys [context descriptor ordinary-row ordinary-fact dse-row dse-fact]} (products)
        mixed (assoc context :ledger [ordinary-row dse-row]
                     :call-facts [ordinary-fact dse-fact])
        callbacks (atom 0)
        opaque (proxy [Object] []
                 (hashCode [] (swap! callbacks inc) (throw (AssertionError. "opaque hash")))
                 (equals [_] (swap! callbacks inc) (throw (AssertionError. "opaque equality")))
                 (toString [] (swap! callbacks inc) (throw (AssertionError. "opaque spelling"))))]
    (doseq [value [nil false 0 "ordinary" opaque]]
      (is (= :rejected (:status (compile-node (assoc mixed :ledger
                                                   [(assoc ordinary-row :producer-rule value) dse-row]) 65536))))
      (is (= :rejected (:status (compile-node (assoc mixed :call-facts
                                                   [(assoc ordinary-fact :dispatch value) dse-fact]) 65536)))))
    (doseq [candidate
            [(assoc mixed :ledger [nil dse-row])
             (assoc mixed :call-facts [nil dse-fact])
             (assoc mixed :ledger [ordinary-row (dissoc dse-row :producer-rule)])
             (assoc mixed :call-facts [ordinary-fact (dissoc dse-fact :dispatch)])
             (assoc mixed :ledger [ordinary-row (dissoc dse-row :producer-rule)]
                          :call-facts [ordinary-fact (dissoc dse-fact :dispatch)])
             (update mixed :ledger conj dse-row)
             (update mixed :call-facts conj dse-fact)
             (update mixed :ledger conj (assoc-in dse-row [:lhs :core-node-id] :orphan))
             (update mixed :call-facts conj (assoc dse-fact :call-core-node-id :orphan))
             (assoc mixed :call-facts (array-map :wrong dse-fact :ordinary-call ordinary-fact))
             (assoc-in mixed [:nodes :call :evaluation :order] [])
             (assoc-in mixed [:forms :arg0 :parent-form-id] :stale)
             (assoc-in mixed [:bindings :ordering-binding :binding-class] :namespace)
             (update-in mixed [:core :calls] into (get-in mixed [:core :calls]))
             (update-in mixed [:core :call-edges] into (get-in mixed [:core :call-edges]))]]
      (is (= :rejected (:status (compile-node candidate 65536)))))
    (doseq [changed ((helper 'checked-dual-bare-set-forgeries) descriptor)]
      (let [candidate (-> mixed (assoc-in [:types :operator] changed)
                          (assoc :ledger [ordinary-row (c7 'sh08-dse-constraint changed)]
                                 :call-facts [ordinary-fact
                                              (c7 'sh08-dse-call-fact changed (get-in context [:core :module]))]))]
        (is (= :rejected (:status (compile-node candidate 65536))))))
    (is (zero? @callbacks))))
