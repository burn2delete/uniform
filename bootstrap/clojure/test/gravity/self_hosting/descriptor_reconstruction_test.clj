(ns gravity.self-hosting.descriptor-reconstruction-test
  "Current checked bare-Set descriptor controls; runtime acceptance remains separate."
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh08-function-call-type-test]
            [gravity.self-hosting.sh09-body-effect-test]))

(defn- helper [name]
  (or (ns-resolve 'gravity.self-hosting.sh08-function-call-type-test name)
      (throw (ex-info "Required current C7 helper is absent" {:name name}))))
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

(defn- case-data [arguments]
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

(deftest complete-dse-descriptors-and-typed-consumers-agree
  (let [{:keys [input derived descriptor context]} (case-data [:gravity.type/set :gravity.type/set])
        {:keys [core node call edge nodes bindings types module]} input
        args [core node call edge nodes bindings types module]
        effects-descriptor (:descriptor (apply body 'effects-set-equality-header args))
        emitter-descriptor (:descriptor (apply emit 'emit-set-equality-header args))
        lowered (emit 'p15-s23-typed-compile-node context :call)]
    (is (= :accepted (:status derived)))
    (is (= descriptor effects-descriptor emitter-descriptor))
    (is (every? #(not (contains? % :operand-type-work))
                [descriptor effects-descriptor emitter-descriptor]))
    (is (= :complete (:status lowered)))
    (is (= :gravity.type/bool (:return descriptor) (:result-type descriptor)))))

(deftest coherent-stale-fields-and-source-tampering-reject
  (let [{:keys [input descriptor context]} (case-data [:gravity.type/set :gravity.type/set])
        compile #(emit 'p15-s23-typed-compile-node % :call)
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
      (is (= :rejected (:status (compile (change context))))))))
