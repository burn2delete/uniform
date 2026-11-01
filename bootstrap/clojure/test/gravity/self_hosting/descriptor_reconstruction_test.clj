(ns gravity.self-hosting.descriptor-reconstruction-test
  "Preserved bare-Set descriptor controls; genuine owner execution remains unrun."
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh08-function-call-type-test]
            [gravity.self-hosting.sh09-body-effect-test]))

(defn- helper [name]
  (ns-resolve 'gravity.self-hosting.sh08-function-call-type-test name))
(defn- c7 [name & args] ((helper 'invoke-c7) name (vec args)))
(defn- body [name & args]
  (apply (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'leaf) name args))
(def ^:private emitter-plan
  (delay
    (let [path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
          emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path)))))
(defn- emit [name & args]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :descriptor-reconstruction-test} @emitter-plan name (vec args)))
(def ^:private cases
  [[:dse [:gravity.type/set :gravity.type/set]]])

(defn- case-data [_family arguments]
  (let [input ((helper 'atomic-equality-input) arguments)
        {:keys [core node call edge nodes bindings types forms module]} input
        derived (c7 'types-set-equality-derive core node call edge nodes bindings types forms)
        descriptor (:descriptor derived)
        context {:core core :nodes nodes :forms forms :bindings bindings
                 :types (assoc types :operator descriptor :call (:type derived))
                 :calls {:call call} :edges {:call edge} :call-order {:call 0}
                 :call-facts {:call (c7 'types-set-equality-call-fact descriptor module)}
                 :ledger [(c7 'types-set-equality-constraint descriptor)]
                 :locals {:arg0 'left :arg1 'right} :parameter-types types}]
    {:input input :derived derived :descriptor descriptor :context context}))

(defn- consumer-descriptors [_family _arguments input]
  (let [{:keys [core node call edge nodes bindings types module]} input
        args [core node call edge nodes bindings types module]]
    [(:descriptor (apply body 'effects-set-equality-header args))
     (:descriptor (apply emit 'emit-set-equality-header args))]))

(deftest complete-descriptors-and-typed-consumers-agree
  (doseq [[family arguments] cases]
    (testing (str family " " arguments)
      (let [{:keys [input derived descriptor context]} (case-data family arguments)
            consumers (consumer-descriptors family arguments input)
            lowered (emit 'p15-s23-typed-compile-node context :call 65536)]
        (is (= :accepted (:status derived)))
        (is (every? #(= descriptor %) consumers))
        (is (every? #(not (contains? % :operand-type-work)) (cons descriptor consumers)))
        (is (= :complete (:status lowered)))
        (is (= :gravity.type/bool (:return descriptor) (:result-type descriptor)))))))

(deftest coherent-stale-fields-and-source-tampering-reject
  (doseq [[family arguments] cases]
    (let [{:keys [input descriptor context]} (case-data family arguments)
          compile #(emit 'p15-s23-typed-compile-node % :call 65536)
          coherent (fn [changed]
                     (-> context
                         (assoc-in [:types :operator] changed)
                         (assoc :call-facts {:call (c7 'types-set-equality-call-fact changed (:module input))}
                                :ledger [(c7 'types-set-equality-constraint changed)])))]
      (is (= :complete (:status (compile context))))
      (doseq [changed [(assoc descriptor :operand-type-work 0)
                       (assoc descriptor :extra true)
                       (assoc-in descriptor [:source :syntax-id] :forged)
                       (assoc descriptor :profile :hosted)]]
        (is (= :rejected (:status (compile (coherent changed))))))
      (doseq [change [#(assoc % :ledger [])
                      #(update % :ledger into (:ledger %))
                      #(assoc % :call-facts {})
                      #(assoc-in % [:nodes :call :evaluation :order] [])
                      #(assoc-in % [:bindings :ordering-binding :binding-class] :namespace)
                      #(assoc-in % [:forms :arg0 :parent-form-id] :stale)
                      #(assoc-in % [:types :arg0] :gravity.type/unknown)]]
        (is (= :rejected (:status (compile (change context)))))))))
