(ns gravity.self-hosting.prepend-premise-test
  "Complete normalized prepend/insertion descriptors and their actual consumers."
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh08-function-call-type-test]
            [gravity.self-hosting.sh09-body-effect-test]))

(defn- helper [name] (ns-resolve 'gravity.self-hosting.sh08-function-call-type-test name))
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
   {:engine :prepend-premise-test} @emitter-plan name (vec args)))
(defn- literal [kind members] {:kind kind :shape :literal :element-types members})
(defn- homogeneous [kind element] {:kind kind :shape :homogeneous :element-type element})
(def ^:private cases
  (concat
   [[(literal :gravity.type/list []) :gravity.type/nil]
    [(literal :gravity.type/list [:gravity.type/bool :gravity.type/string]) :gravity.type/keyword]
    [:gravity.type/list :gravity.type/string]
    [(homogeneous :gravity.type/list :gravity.type/string) :gravity.type/string]
    [(homogeneous :gravity.type/list :gravity.type/string) :gravity.type/never]]
   (for [element [:gravity.type/string :gravity.type/keyword :gravity.type/symbol]
         [receiver value] [[(literal :gravity.type/set []) element]
                           [(literal :gravity.type/set [element]) element]
                           [(literal :gravity.type/set [element]) :gravity.type/never]]]
     [receiver value])
   (for [element [:gravity.type/string :gravity.type/symbol]]
     [(homogeneous :gravity.type/set element) element])))
(def ^:private obsolete [:operand-type-work :result-type-work :type-construction-work])

(defn- case-data [receiver value]
  (let [{:keys [core node call edge nodes bindings types forms module] :as input}
        ((helper 'list-prepend-input) receiver value)
        derived ((helper 'list-prepend-derived) input)
        descriptor (:descriptor derived)
        args [core node call edge nodes bindings types module]
        consumers [(body 'body-pl-finish
                         (apply body 'body-pl-source (conj args (body 'body-pl-type-work [receiver value] 65536))))
                   (emit 'p15-s23-pl-finish
                         (apply emit 'p15-s23-pl-source (conj args (emit 'p15-s23-pl-type-work [receiver value] 65536))))]
        context {:core core :nodes nodes :forms forms :bindings bindings
                 :types (assoc types :operator descriptor :call (:type derived))
                 :calls {:call call} :edges {:call edge} :call-order {:call 0}
                 :call-facts {:call (c7 'sh08-pl-call-fact descriptor module)}
                 :ledger [(c7 'sh08-pl-constraint descriptor)]
                 :locals {:arg0 'items :arg1 'value} :parameter-types types}]
    {:input input :derived derived :descriptor descriptor :consumers consumers :context context}))

(deftest complete-descriptors-and-typed-consumers
  (doseq [[receiver value] cases]
    (testing (str receiver " " value)
      (let [{:keys [derived descriptor consumers context]} (case-data receiver value)]
        (is (= :accepted (:status derived)))
        (is (every? #(= descriptor %) consumers))
        (is (every? #(not-any? (partial contains? %) obsolete) (cons descriptor consumers)))
        (is (= :complete (:status (emit 'p15-s23-typed-compile-node context :call 65536))))
        (is (= (:type derived) (:return descriptor) (:result-type descriptor)))
        (if (= value :gravity.type/never)
          (do (is (= :not-invoked (:application descriptor)))
              (is (= [] (:direct-effects descriptor)))
              (is (nil? (:result-extent descriptor))))
          (is (= [:memory/allocate] (:direct-effects descriptor))))))))

(deftest coherent-old-costs-and-forgeries-reject
  (doseq [[receiver value] cases]
    (let [{:keys [input descriptor context]} (case-data receiver value)
          compile #(emit 'p15-s23-typed-compile-node % :call 65536)
          coherent (fn [changed]
                     (-> context
                         (assoc-in [:types :operator] changed)
                         (assoc :call-facts {:call (c7 'sh08-pl-call-fact changed (:module input))}
                                :ledger [(c7 'sh08-pl-constraint changed)])))]
      (is (= :complete (:status (compile context))))
      (doseq [changed (concat (map #(assoc descriptor % 0) obsolete)
                             [(merge descriptor (zipmap obsolete [0 0 0]))
                              (assoc-in descriptor [:source :syntax-id] :forged)
                              (assoc descriptor :result-type :gravity.type/unknown)
                              (assoc descriptor :direct-effects [:io/write])])]
        (is (= :rejected (:status (compile (coherent changed))))))
      (doseq [change [#(assoc % :ledger [])
                      #(update % :ledger into (:ledger %))
                      #(assoc % :call-facts {})
                      #(assoc-in % [:bindings :ordering-binding :binding-class] :lexical)
                      #(assoc-in % [:forms :arg0 :parent-form-id] :stale)
                      #(assoc-in % [:types :arg0] :gravity.type/unknown)]]
        (is (= :rejected (:status (compile (change context)))))))))

(deftest late-invalid-and-opaque-types-reject-without-callbacks
  (let [callbacks (atom 0)
        opaque (proxy [Object] []
                 (hashCode [] (swap! callbacks inc) (throw (AssertionError. "opaque hash")))
                 (equals [_] (swap! callbacks inc) (throw (AssertionError. "opaque equality")))
                 (toString [] (swap! callbacks inc) (throw (AssertionError. "opaque formatting"))))]
    (doseq [[kind width index] [[:gravity.type/list 1023 1022] [:gravity.type/set 1024 1023]]
            invalid [:gravity.type/unknown opaque]]
      (let [receiver (literal kind (assoc (vec (repeat width :gravity.type/string)) index invalid))]
        (doseq [[invoke prefix] [[c7 "sh08-"] [body "body-"] [emit "p15-s23-"]]]
          (is (= :rejected (:status (invoke (symbol (str prefix "pl-type-work"))
                                          [receiver :gravity.type/string] 65536)))))))
    (doseq [receiver [:gravity.type/set
                      (homogeneous :gravity.type/set :gravity.type/keyword)
                      (literal :gravity.type/vector [])]]
      (is (= :rejected (:status ((helper 'list-prepend-derived)
                                 ((helper 'list-prepend-input) receiver :gravity.type/string))))))
    (is (zero? @callbacks))))
