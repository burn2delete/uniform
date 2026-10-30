(ns gravity.self-hosting.sh01-map-keys-mixed-ledger-test
  "Actual C7 producer rows consumed by the current Map keys lowering census.

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
   {:engine :map-keys-mixed-ledger-kernel} @lowering-plan function (vec arguments)))

(defn- products []
  (let [input ((helper 'map-keys-input) {:kind :gravity.type/map :shape :literal :member-types [:gravity.type/keyword :gravity.type/bool]})
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
        derived (c7 'sh08-lq-derive core (:node input) (:call input) (:edge input)
                    nodes (:bindings input) types (:forms input) 65536)
        descriptor (:descriptor derived)
        mkeys-row (c7 'sh08-lq-constraint descriptor)
        mkeys-fact (c7 'sh08-lq-call-fact descriptor module)
        context {:core core :nodes nodes :forms (:forms input) :bindings (:bindings input)
                 :types (assoc types :operator descriptor :call (:type derived))
                 :calls {:call (:call input) :ordinary-call ordinary-call}
                 :edges {:call (:edge input) :ordinary-call ordinary-edge}
                 :call-order {:call 0 :ordinary-call 1}
                 :locals {:arg0 'items} :parameter-types types
                 :ledger [mkeys-row] :call-facts {:call mkeys-fact}}]
    {:context context :descriptor descriptor :derived derived :constrained constrained
     :ordinary-row (first (:constraints constrained)) :ordinary-fact (first facts)
     :mkeys-row mkeys-row :mkeys-fact mkeys-fact}))

(defn- row-census [context ledger descriptor budget]
  (lower 'p15-s23-mkeys-row-census (:core context) ledger (:nodes context)
         (:bindings context) (:types context) (:calls context) (:edges context)
         :call descriptor budget))

(defn- fact-census [context facts descriptor budget]
  (lower 'p15-s23-mkeys-fact-census facts (get-in context [:core :module])
         {:call descriptor} :call budget))

(defn- compile-node [context budget]
  (lower 'p15-s23-typed-compile-node context :call budget))

(deftest actual-c7-ordinary-rows-compose-with-mkeys-in-both-orders
  (let [{:keys [context descriptor derived constrained ordinary-row ordinary-fact mkeys-row mkeys-fact]}
        (products)
        expected {:op :builtin-call :function 'keys
                  :args [{:op :local :name 'items}]}]
    (is (= :accepted (:status derived)))
    (is (empty? (:diagnostics constrained)))
    (is (= 1 (count (:constraints constrained))))
    (is (map? ordinary-row))
    (is (not (contains? ordinary-row :producer-rule)))
    (is (= :ordinary-call (:call-core-node-id ordinary-fact)))
    (is (not (contains? ordinary-fact :dispatch)))
    (doseq [rows [[ordinary-row mkeys-row] [mkeys-row ordinary-row]]
            facts [[ordinary-fact mkeys-fact] [mkeys-fact ordinary-fact]
                   (array-map :ordinary-call ordinary-fact :call mkeys-fact)
                   (array-map :call mkeys-fact :ordinary-call ordinary-fact)]]
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
          (is (true? (= [mkeys-row] (:items row-result))))
          (is (= :accepted (:status fact-result)))
          (is (true? (= [mkeys-fact] (:items fact-result))))
          (is (= :complete (:status compiled)))
          (is (= expected (:value compiled))))))))

(deftest mixed-ledger-remains-complete-with-ordinary-entries
  (let [{:keys [context descriptor ordinary-row ordinary-fact mkeys-row mkeys-fact]} (products)
        rows [ordinary-row mkeys-row] facts [ordinary-fact mkeys-fact]
        candidate (assoc context :ledger rows :call-facts facts)
        result (compile-node candidate 65536)]
    (is (= :complete (:status result)))
    ;; Compare every semantic field across two fixed legacy API arguments.
    ;; Only top-level work telemetry is excluded; no payment is asserted.
    (is (= {:status :accepted :items [mkeys-row] :descriptors {:call descriptor}}
           (dissoc (row-census candidate rows descriptor 65536) :work-count)
           (dissoc (row-census candidate rows descriptor 65537) :work-count)))
    (is (= {:status :accepted :items [mkeys-fact]}
           (dissoc (fact-census candidate facts descriptor 65536) :work-count)
           (dissoc (fact-census candidate facts descriptor 65537) :work-count)))
    (is (= {:status :complete
            :value {:op :builtin-call :function 'keys :args [{:op :local :name 'items}]}}
           (dissoc result :work-count)
           (dissoc (compile-node candidate 65537) :work-count)))))

(deftest malformed-and-forged-mkeys-joins-remain-rejected
  (let [{:keys [context descriptor ordinary-row ordinary-fact mkeys-row mkeys-fact]} (products)
        mixed (assoc context :ledger [ordinary-row mkeys-row]
                     :call-facts [ordinary-fact mkeys-fact])
        callbacks (atom 0)
        opaque (proxy [Object] []
                 (hashCode [] (swap! callbacks inc) (throw (AssertionError. "opaque hash")))
                 (equals [_] (swap! callbacks inc) (throw (AssertionError. "opaque equality")))
                 (toString [] (swap! callbacks inc) (throw (AssertionError. "opaque spelling"))))]
    (doseq [value [nil false 0 "ordinary" opaque]]
      (is (= :rejected (:status (compile-node (assoc mixed :ledger
                                                   [(assoc ordinary-row :producer-rule value) mkeys-row]) 65536))))
      (is (= :rejected (:status (compile-node (assoc mixed :call-facts
                                                   [(assoc ordinary-fact :dispatch value) mkeys-fact]) 65536)))))
    (doseq [candidate
            [(assoc mixed :ledger [nil mkeys-row])
             (assoc mixed :call-facts [nil mkeys-fact])
             (assoc mixed :ledger [ordinary-row (dissoc mkeys-row :producer-rule)])
             (assoc mixed :call-facts [ordinary-fact (dissoc mkeys-fact :dispatch)])
             (assoc mixed :ledger [ordinary-row (dissoc mkeys-row :producer-rule)]
                          :call-facts [ordinary-fact (dissoc mkeys-fact :dispatch)])
             (update mixed :ledger conj mkeys-row)
             (update mixed :call-facts conj mkeys-fact)
             (update mixed :ledger conj (assoc-in mkeys-row [:lhs :core-node-id] :orphan))
             (update mixed :call-facts conj (assoc mkeys-fact :call-core-node-id :orphan))
             (assoc mixed :call-facts (array-map :wrong mkeys-fact :ordinary-call ordinary-fact))
             (assoc-in mixed [:nodes :call :evaluation :order] [])
             (assoc-in mixed [:forms :operator :parent-form-id] :stale)
             (assoc-in mixed [:bindings :ordering-binding :binding-class] :namespace)
             (update-in mixed [:core :calls] into (get-in mixed [:core :calls]))
             (update-in mixed [:core :call-edges] into (get-in mixed [:core :call-edges]))
             (update mixed :ledger conj (dissoc mkeys-row :producer-rule))
             (update mixed :ledger conj (assoc-in (dissoc mkeys-row :producer-rule) [:lhs :core-node-id] :orphan))
             (update mixed :ledger conj (-> mkeys-row (dissoc :producer-rule) (update :lhs dissoc :projection)))
             (update mixed :ledger conj (-> mkeys-row (dissoc :producer-rule) (update :lhs dissoc :projection) (assoc-in [:lhs :core-node-id] :orphan)))
             (update mixed :call-facts conj (dissoc mkeys-fact :dispatch))
             (update mixed :call-facts conj (assoc (dissoc mkeys-fact :dispatch) :call-core-node-id :orphan))
             (update mixed :call-facts conj (dissoc mkeys-fact :dispatch :applied-map-keys))
             (update mixed :call-facts conj (assoc (dissoc mkeys-fact :dispatch :applied-map-keys) :call-core-node-id :orphan))]]
      (is (= :rejected (:status (compile-node candidate 65536)))))
    (doseq [changed [(assoc descriptor :version :forged)
                    (assoc descriptor :kind :gravity.type/applied-list-query)
                    (assoc-in descriptor [:source-call :core-node-id] :forged)
                    (assoc-in descriptor [:source-edge :call-core-node-id] :forged)
                    (assoc-in descriptor [:source-binding :package :version] "forged")
                    (assoc descriptor :operator-binding-id :forged)
                    (assoc descriptor :direct-effects [] :latent-effects [])
                    (assoc-in descriptor [:storage-policy :copy] :none)
                    (assoc-in descriptor [:query-policy :payload-evaluation] :execute)]]
      (let [candidate (-> mixed (assoc-in [:types :operator] changed)
                          (assoc :ledger [ordinary-row (c7 'sh08-lq-constraint changed)]
                                 :call-facts [ordinary-fact
                                              (c7 'sh08-lq-call-fact changed (get-in context [:core :module]))]))]
        (is (= :rejected (:status (compile-node candidate 65536))))))
    (doseq [candidate [(assoc mixed :ledger [ordinary-row (with-meta mkeys-row {:opaque opaque})])
                       (assoc mixed :call-facts [ordinary-fact (with-meta mkeys-fact {:opaque opaque})])
                       (assoc mixed :ledger [ordinary-row (clojure.lang.PersistentArrayMap. (object-array [opaque :value]))])
                       (assoc mixed :call-facts [ordinary-fact (clojure.lang.PersistentArrayMap. (object-array [opaque :value]))])]]
      (is (= :rejected (:status (compile-node candidate 65536)))))
    (is (zero? @callbacks))))
