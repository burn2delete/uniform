(ns gravity.types.pr598-mixed-collection-joins-test
  "Current DATA-level List/Set and Map/keys mixed-ledger controls from PR598.

  These controls intentionally stop at the current C7 lowering census.  They
  do not claim source admission, Effects/C8 composition, runtime execution,
  public verifier authority, or migration evidence."
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
   {:engine :pr598-mixed-collection-joins} @lowering-plan function (vec arguments)))

(defn- products [kind]
  (let [input (if (= kind :list-set)
                ((helper 'list-set-input)
                 {:kind :gravity.type/list :shape :homogeneous :element-type :gravity.type/keyword})
                ((helper 'map-keys-input)
                 {:kind :gravity.type/map :shape :literal
                  :member-types [:gravity.type/keyword :gravity.type/bool]}))
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
        derive-name (if (= kind :list-set) 'sh08-st-derive 'sh08-lq-derive)
        derived (c7 derive-name core (:node input) (:call input) (:edge input)
                    nodes (:bindings input) types (:forms input))
        descriptor (:descriptor derived)
        row-name (if (= kind :list-set) 'sh08-st-constraint 'sh08-lq-constraint)
        fact-name (if (= kind :list-set) 'sh08-st-call-fact 'sh08-lq-call-fact)
        row (c7 row-name descriptor)
        fact (c7 fact-name descriptor module)
        context {:core core :nodes nodes :forms (:forms input) :bindings (:bindings input)
                 :types (assoc types :operator descriptor :call (:type derived))
                 :calls {:call (:call input) :ordinary-call ordinary-call}
                 :edges {:call (:edge input) :ordinary-call ordinary-edge}
                 :call-order {:call 0 :ordinary-call 1}
                 :locals {:arg0 'items} :parameter-types types
                 :ledger [row] :call-facts {:call fact}}]
    {:kind kind :context context :descriptor descriptor :derived derived :constrained constrained
     :ordinary-row (first (:constraints constrained)) :ordinary-fact (first facts)
     :row row :fact fact :module module}))

(defn- row-census [{:keys [kind context]} rows descriptor]
  (lower (if (= kind :list-set) 'p15-s23-lset-row-census 'p15-s23-mkeys-row-census)
         (:core context) rows (:nodes context) (:bindings context) (:types context)
         (:calls context) (:edges context) :call descriptor))

(defn- fact-census [{:keys [kind context]} facts descriptor]
  (lower (if (= kind :list-set) 'p15-s23-lset-fact-census 'p15-s23-mkeys-fact-census)
         facts (get-in context [:core :module]) {:call descriptor} :call))

(defn- compile-node [{:keys [context]}]
  (lower 'p15-s23-typed-compile-node context :call))

(deftest current-list-set-and-map-keys-rows-compose-with-ordinary-ledger
  (doseq [{:keys [kind] :as data} [(products :list-set) (products :map-keys)]]
    (let [{:keys [context descriptor derived constrained ordinary-row ordinary-fact row fact]} data
          expected (if (= kind :list-set)
                     {:op :builtin-call :function 'set :args [{:op :local :name 'items}]}
                     {:op :builtin-call :function 'keys :args [{:op :local :name 'items}]})]
      (is (= :accepted (:status derived)) (pr-str derived))
      (is (empty? (:diagnostics constrained)))
      (is (= 1 (count (:constraints constrained))))
      (is (map? ordinary-row))
      (is (not (contains? ordinary-row :producer-rule)))
      (is (= :ordinary-call (:call-core-node-id ordinary-fact)))
      (is (not (contains? ordinary-fact :dispatch)))
      (doseq [rows [[ordinary-row row] [row ordinary-row]]
              facts [[ordinary-fact fact] [fact ordinary-fact]
                     (array-map :ordinary-call ordinary-fact :call fact)
                     (array-map :call fact :ordinary-call ordinary-fact)]]
        (testing (str kind " row/fact counts " (count rows) "/" (count facts))
          (let [candidate (assoc context :ledger rows :call-facts facts)
                row-result (row-census (assoc data :context candidate) rows descriptor)
                fact-result (fact-census (assoc data :context candidate) facts descriptor)
                compiled (compile-node (assoc data :context candidate))]
            (is (= :accepted (:status row-result)) (pr-str row-result))
            (is (= [row] (:items row-result)))
            (is (= :accepted (:status fact-result)) (pr-str fact-result))
            (is (= [fact] (:items fact-result)))
            (is (= :complete (:status compiled)) (pr-str compiled))
            (is (= expected (:value compiled)))))))))

(deftest current-list-set-and-map-keys-complete-products-are-order-invariant
  ;; Preserve the semantic part of the retired mixed-ledger comparisons using
  ;; the current no-budget owner APIs.  The two ledger orders exercise the
  ;; same mixed source context without reintroducing accounting arguments.
  (doseq [{:keys [kind] :as data} [(products :list-set) (products :map-keys)]]
    (let [{:keys [context descriptor ordinary-row ordinary-fact row fact]} data
          expected-value (if (= kind :list-set)
                           {:op :builtin-call :function 'set
                            :args [{:op :local :name 'items}]}
                           {:op :builtin-call :function 'keys
                            :args [{:op :local :name 'items}]})
          row-orders [[ordinary-row row] [row ordinary-row]]
          fact-orders [[ordinary-fact fact] [fact ordinary-fact]]
          contexts (mapv #(assoc context :ledger %1 :call-facts %2)
                         row-orders fact-orders)
          row-results (mapv #(row-census (assoc data :context %1)
                                         (:ledger %1) descriptor)
                            contexts)
          fact-results (mapv #(fact-census (assoc data :context %1)
                                          (:call-facts %1) descriptor)
                             contexts)
          compile-results (mapv #(compile-node (assoc data :context %1)) contexts)]
      (testing (str kind " complete row products")
        (is (= {:status :accepted :items [row] :descriptors {:call descriptor}}
               (first row-results) (second row-results))))
      (testing (str kind " complete fact products")
        (is (= {:status :accepted :items [fact]}
               (first fact-results) (second fact-results))))
      (testing (str kind " complete compiler products")
        (is (= {:status :complete :value expected-value}
               (first compile-results) (second compile-results)))))))

(deftest current-list-set-and-map-keys-join-tampering-rejects-without-callbacks
  (doseq [{:keys [kind] :as data} [(products :list-set) (products :map-keys)]]
    (let [{:keys [context descriptor ordinary-row ordinary-fact row fact]} data
          mixed (assoc context :ledger [ordinary-row row]
                       :call-facts [ordinary-fact fact])
          callbacks (atom 0)
          opaque (proxy [Object] []
                   (hashCode [] (swap! callbacks inc) (throw (AssertionError. "opaque hash")))
                   (equals [_] (swap! callbacks inc) (throw (AssertionError. "opaque equality")))
                   (toString [] (swap! callbacks inc) (throw (AssertionError. "opaque spelling"))))
          bad-rows [(assoc mixed :ledger [nil row])
                    (assoc mixed :ledger [(with-meta ordinary-row {:tampered true}) row])
                    (assoc mixed :ledger
                           [(clojure.lang.PersistentArrayMap.
                             (object-array [opaque :value])) row])
                    (assoc mixed :ledger [ordinary-row (dissoc row :producer-rule)])
                    (assoc mixed :ledger [ordinary-row (dissoc row :producer-rule)]
                           :call-facts [ordinary-fact (dissoc fact :dispatch)])
                    (assoc mixed :ledger [ordinary-row (assoc-in row [:lhs :core-node-id] opaque)])
                    (update mixed :ledger conj row)
                    (update mixed :ledger conj (assoc-in row [:lhs :core-node-id] :orphan))
                    (update mixed :ledger conj (assoc (dissoc row :producer-rule)
                                                      :lhs (assoc (:lhs row) :core-node-id :orphan)))
                    (update mixed :ledger conj (assoc (dissoc row :producer-rule)
                                                      :lhs (dissoc (:lhs row) :projection)))
                    (update mixed :ledger conj (assoc (dissoc row :producer-rule)
                                                      :lhs (assoc (dissoc (:lhs row) :projection)
                                                                  :core-node-id :orphan)))
                    (update-in mixed [:core :calls] into (get-in mixed [:core :calls]))
                    (update-in mixed [:core :call-edges] into (get-in mixed [:core :call-edges]))
                    (assoc-in mixed [:nodes :call :evaluation :order] [])
                    (assoc-in mixed [:forms :operator :parent-form-id] :stale)
                    (assoc-in mixed [:bindings :ordering-binding :binding-class] :namespace)]
          bad-facts [(assoc mixed :call-facts [nil fact])
                     (assoc mixed :call-facts [(with-meta ordinary-fact {:tampered true}) fact])
                     (assoc mixed :call-facts
                            [(clojure.lang.PersistentArrayMap.
                              (object-array [opaque :value])) fact])
                     (assoc mixed :call-facts [ordinary-fact (dissoc fact :dispatch)])
                    (assoc mixed :call-facts [ordinary-fact (assoc fact :call-core-node-id opaque)])
                    (assoc mixed :call-facts [ordinary-fact
                                              (assoc (dissoc fact :dispatch)
                                                     :call-core-node-id :orphan)])
                    (assoc mixed :call-facts [ordinary-fact
                                              (dissoc fact :dispatch :applied-list-set-conversion
                                                      :applied-map-keys)])
                    (assoc mixed :call-facts [ordinary-fact
                                              (assoc (dissoc fact :dispatch
                                                             :applied-list-set-conversion
                                                             :applied-map-keys)
                                                     :call-core-node-id :orphan)])
                     (update mixed :call-facts conj fact)
                     (update mixed :call-facts conj (assoc fact :call-core-node-id :orphan))
                     (assoc mixed :call-facts (array-map :wrong fact :ordinary-call ordinary-fact))]
          constraint-name (if (= kind :list-set) 'sh08-st-constraint 'sh08-lq-constraint)
          fact-name (if (= kind :list-set) 'sh08-st-call-fact 'sh08-lq-call-fact)]
      (doseq [value [nil false 0 "ordinary" opaque :forged]]
        (is (= :rejected (:status (compile-node (assoc data :context
                                                       (assoc mixed :ledger
                                                              [(assoc ordinary-row :producer-rule value) row]))))))
        (is (= :rejected (:status (compile-node (assoc data :context
                                                       (assoc mixed :call-facts
                                                              [(assoc ordinary-fact :dispatch value) fact])))))))
      (doseq [candidate (concat bad-rows bad-facts)]
        (is (= :rejected (:status (compile-node (assoc data :context candidate))))
            (str kind " malformed join")))
      ;; A specialized census must ignore a valid record owned by another
      ;; family, while still rejecting non-keyword tag values above.
      (let [foreign-producer (if (= kind :list-set)
                               :sh08-map-keys-v1
                               :sh08-list-set-conversion-v1)
            foreign-dispatch (if (= kind :list-set)
                               :direct-map-keys
                               :direct-list-set-conversion)
            foreign (assoc mixed
                           :ledger [(assoc ordinary-row :producer-rule foreign-producer) row]
                           :call-facts [(assoc ordinary-fact :dispatch foreign-dispatch) fact])
            result (compile-node (assoc data :context foreign))]
        (is (= :complete (:status result)) (str kind " foreign-family entries")))
      (doseq [changed [(assoc descriptor :version :forged)
                       (assoc descriptor :kind :gravity.type/applied-list-query)
                       (assoc descriptor :operator-binding-id :forged)
                       (assoc-in descriptor [:source-call :core-node-id] :forged)
                       (assoc-in descriptor [:source-edge :call-core-node-id] :forged)
                       (assoc-in descriptor [:source-binding :package :version] "forged")
                       (assoc descriptor :direct-effects [] :latent-effects [])
                       (assoc-in descriptor [:storage-policy :copy] :none)
                       (assoc-in descriptor [:construction-policy :payload-evaluation] :execute)]]
        (let [candidate (-> mixed
                            (assoc-in [:types :operator] changed)
                            (assoc :ledger [ordinary-row (c7 constraint-name changed)]
                                   :call-facts [ordinary-fact (c7 fact-name changed (:module data))]))]
          (is (= :rejected (:status (compile-node (assoc data :context candidate)))))))
      (is (zero? @callbacks)))))
