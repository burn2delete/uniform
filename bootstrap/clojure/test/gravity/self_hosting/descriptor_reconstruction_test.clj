(ns gravity.self-hosting.descriptor-reconstruction-test
  "Actual descriptor reconstruction and typed calls on normalized owner data."
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
(defn- finite [members] {:kind :gravity.type/set :shape :literal :element-types members})
(def ^:private cases
  [[:ane [:gravity.type/nil :gravity.type/bool]]
   [:ane [:gravity.type/keyword :gravity.type/string]]
   [:ane [{:kind :gravity.type/nil-or :value-type :gravity.type/string}
          {:kind :gravity.type/nil-or :value-type :gravity.type/keyword}]]
   [:bka [:gravity.type/set (finite [:gravity.type/keyword])]]
   [:bka [(finite [:gravity.type/keyword :gravity.type/keyword]) :gravity.type/set]]
   [:dse [:gravity.type/set :gravity.type/set]]])

(defn- case-data [family arguments]
  (let [stem (name family)
        input ((helper (if (= family :ane) 'atomic-inequality-input 'atomic-equality-input)) arguments)
        {:keys [core node call edge nodes bindings types forms module]} input
        derived (c7 (symbol (str "sh08-" stem "-derive")) core node call edge nodes bindings types forms 65536)
        descriptor (:descriptor derived)
        context {:core core :nodes nodes :forms forms :bindings bindings
                 :types (assoc types :operator descriptor :call (:type derived))
                 :calls {:call call} :edges {:call edge} :call-order {:call 0}
                 :call-facts {:call (c7 (symbol (str "sh08-" stem "-call-fact")) descriptor module)}
                 :ledger [(c7 (symbol (str "sh08-" stem "-constraint")) descriptor)]
                 :locals {:arg0 'left :arg1 'right} :parameter-types types}]
    {:input input :derived derived :descriptor descriptor :context context :stem stem}))

(defn- consumer-descriptors [family arguments input]
  (let [{:keys [core node call edge nodes bindings types module]} input
        args [core node call edge nodes bindings types module]]
    (if (= family :ane)
      [(body 'body-ane-finish
             (apply body 'body-ane-source (conj args (body 'body-ane-type-work arguments 65536))))
       (emit 'p15-s23-ane-finish
             (apply emit 'p15-s23-ane-source (conj args (emit 'p15-s23-ane-type-work arguments 65536))))]
      [(:descriptor (apply body (symbol (str "body-" (name family) "-header")) (conj args 65536)))
       (:descriptor (apply emit (symbol (str "p15-s23-" (name family) "-header")) (conj args 65536)))])))

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
    (let [{:keys [input descriptor context stem]} (case-data family arguments)
          compile #(emit 'p15-s23-typed-compile-node % :call 65536)
          coherent (fn [changed]
                     (-> context
                         (assoc-in [:types :operator] changed)
                         (assoc :call-facts {:call (c7 (symbol (str "sh08-" stem "-call-fact")) changed (:module input))}
                                :ledger [(c7 (symbol (str "sh08-" stem "-constraint")) changed)])))]
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
