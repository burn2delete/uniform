(ns gravity.self-hosting.compatibility-premise-test
  "Actual owner joins on normalized declaration fixtures; no public admission authority."
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh08-function-call-type-test]
            [gravity.self-hosting.sh09-body-effect-test]
            [gravity.self-hosting.sh01-stage2-runtime-iteration-test]))

(defn- typed-call-context [base c7 input actual]
  (let [declaration (:declaration input) type (get-in input [:callee :parameters 0 :type])
        signature (assoc (get-in base [:records 'identity :signature]) :parameters [declaration])
        record (assoc (get-in base [:records 'identity]) :signature signature)
        function (assoc (get-in base [:functions 'identity]) :signature signature :parameters [{:binding-id :parameter :type type}])
        raw-type (if (= :homogeneous (:shape type)) '(List String) (:type-symbol declaration))
        raw-fn (list 'fn (vector 'problem :- raw-type) :- 'String "done") raw-def (list 'def 'identity raw-fn)
        type-id (:type-form-id declaration) marker-id (:marker-form-id declaration)
        context (-> base
                    (assoc-in [:core :function-records] [record]) (assoc-in [:records 'identity] record)
                    (assoc-in [:functions 'identity] function) (assoc-in [:types :operator] function)
                    (assoc-in [:types :function] function) (assoc-in [:types :definition] function)
                    (assoc-in [:types :argument] actual) (assoc-in [:parameter-types :caller-parameter] actual)
                    (assoc-in [:nodes :function :attributes :signature] signature)
                    (assoc-in [:nodes :reference :core-form] :literal)
                    (assoc-in [:nodes :reference :attributes] {:literal-kind :string :value "done"})
                    (assoc-in [:raw-roots :definition-form] raw-def) (assoc-in [:forms :definition-form :value] raw-def)
                    (assoc-in [:forms :function-form :value] raw-fn) (assoc-in [:forms :body-form :value] "done")
                    (assoc-in [:forms :parameter-form :value] (vector 'problem :- raw-type))
                    (assoc-in [:forms :parameter-form :child-form-ids] [:parameter-name marker-id type-id])
                    (assoc-in [:forms marker-id] {:form-id marker-id :syntax-id (:marker-syntax-id declaration) :value :- :parent-form-id :parameter-form})
                    (assoc-in [:forms type-id] {:form-id type-id :syntax-id (:type-syntax-id declaration) :kind :symbol :value raw-type
                                              :parent-form-id :parameter-form :child-form-ids []})
                    (update :bindings merge (select-keys (:bindings input) [(:type-binding-id declaration) (:element-binding-id declaration)])))
        context (if (= :homogeneous (:shape type))
                  (let [ctor (:constructor-form-id declaration) member (:element-form-id declaration)]
                    (-> context (assoc-in [:forms type-id :kind] :list) (assoc-in [:forms type-id :child-form-ids] [ctor member])
                        (assoc-in [:forms ctor] {:form-id ctor :syntax-id (:constructor-syntax-id declaration) :kind :symbol :value 'List :parent-form-id type-id})
                        (assoc-in [:forms member] {:form-id member :syntax-id (:element-syntax-id declaration) :kind :symbol :value 'String :parent-form-id type-id}))) context)
        call (get-in context [:calls :call]) edge (get-in context [:edges :call])
        checked (c7 'sh08-lp-compatibility [(:core context) call edge function 0 actual (:nodes context) (:bindings context) 65536])
        pair (cond-> {:parameter :parameter :argument actual :chosen type} (= :accepted (:status checked)) (assoc :compatibility (:premise checked)))
        row (c7 'sh08-lp-call-row [(assoc (first (:ledger base)) :rhs [pair])])
        fact (-> (get-in context [:call-facts :call]) (assoc :argument-types [actual]) (assoc-in [:fact-id-request :argument-types] [actual]))
        context (assoc context :ledger [row] :call-facts {:call fact})]
    context))

(defn- environment []
  (let [up 'gravity.self-hosting.sh08-function-call-type-test
        stage 'gravity.self-hosting.sh01-stage2-runtime-iteration-test
        runtime (var-get (ns-resolve stage 'runtime))
        path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path))
        emit (fn [function & args] (bootstrap/p15-s23-stage2-runtime-execute-function runtime plan function (vec args)))
        {:keys [module input observation]} ((ns-resolve stage 'typed-lowering-kernel-data))]
    {:helper #(ns-resolve up %) :c7 (ns-resolve up 'invoke-c7)
     :body (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'leaf) :emit emit
     :base ((ns-resolve stage 'typed-direct-call-kernel-context) (:value (emit 'p15-s23-typed-context module input observation)))}))

(defn- positional [members] {:kind :gravity.type/list :shape :literal :element-types members})
(defn- finite-map [members] {:kind :gravity.type/map :shape :literal :member-types members})
(def ^:private homogeneous {:kind :gravity.type/list :shape :homogeneous :element-type :gravity.type/string})
(defn- parameter-input [helper family actual]
  ((helper (case family :map 'bare-map-parameter-input :bare 'bare-list-parameter-input :hom 'homogeneous-list-parameter-input)) actual))
(defn- facts-and-context [body input rows]
  (let [facts {:constraint-ledger (vec rows) :type-table (:types input) :function-type-table [(:callee input)]}
        context (body 'body-lp-context {:canonical-core-artifact (:core input)} facts (:bindings input) 65536)]
    {:facts facts :context context :checked (assoc facts :list-parameter-context (:context context))}))
(defn- local-valid? [body input checked actual]
  (body 'body-local-arguments-valid? (:core input) checked (:bindings input) (:edge input) (:call input)
        {:argument-types [actual]} (:callee input)))

(deftest complete-parameter-premises-and-typed-calls
  (let [{:keys [helper c7 body emit base]} (environment)]
    (doseq [[family actual adapted?]
            [[:map (finite-map []) true]
             [:map (finite-map [:gravity.type/keyword :gravity.type/bool :gravity.type/string :gravity.type/nil]) true]
             [:map (finite-map [:gravity.type/symbol :gravity.type/string]) true]
             [:bare (positional []) true] [:bare (positional [:gravity.type/string :gravity.type/bool]) true]
             [:hom (positional []) true] [:hom (positional [:gravity.type/string :gravity.type/string]) true]
             [:hom (positional (vec (repeat 1024 :gravity.type/string))) true]
             [:map :gravity.type/map false] [:bare :gravity.type/list false] [:hom homogeneous false]]]
      (testing (str family " adaptation=" adapted?)
        (let [{:keys [core call edge callee nodes bindings] :as input} (parameter-input helper family actual)
              a (c7 'sh08-lp-compatibility [core call edge callee 0 actual nodes bindings 65536])
              b (body 'body-lp-compatibility core call edge callee 0 actual nodes bindings 65536)
              c (emit 'p15-s23-lp-compatibility core call edge callee 0 actual nodes bindings 65536)
              produced ((helper 'bare-list-parameter-constraints) input) row (first (:constraints produced))
              {:keys [context checked facts]} (facts-and-context body input (:constraints produced))
              typed (typed-call-context base c7 input actual)
              compile #(emit 'p15-s23-typed-compile-node % :call 65536)
              lowered (compile typed)]
          (is (= (repeat 3 (if adapted? :accepted :not-applicable)) (map :status [a b c])))
          (is (empty? (:diagnostics produced)))
          (is (= :accepted (:status context)))
          (is (true? (local-valid? body input checked actual)))
          (is (= :complete (:status lowered)))
          (is (= {:op :function-call :function 'identity :args [{:op :local :name 'problem}]} (:value lowered)))
          (when adapted?
            (is (= (:premise a) (:premise b) (:premise c)))
            (is (every? #(not (contains? (:premise %) :operand-type-work)) [a b c]))
            (when (= family :map)
              (is (= (:premise a) (:premise (c7 'sh08-mp-compatibility [core call edge callee 0 actual nodes bindings 65536])))))
            (when (= family :hom)
              (is (= (count (:element-types actual)) (get-in a [:premise :checked-element-count]))))
            (doseq [[key value] [[:operand-type-work 0]
                                [:operand-type-work (:work-count (body 'body-lc-type-work [actual] 65536))]
                                [::unknown-field true]]]
              (let [changed-row (assoc-in row [:rhs 0 :compatibility key] value)
                    rejected (facts-and-context body input [changed-row])
                    changed-typed (assoc-in typed [:ledger 0 :rhs 0 :compatibility key] value)]
                (is (= :rejected (get-in rejected [:context :status])))
                (is (false? (local-valid? body input (:checked rejected) actual)))
                (is (= :rejected (:status (compile changed-typed))))))
            (when (= family :hom)
              (doseq [[key value] [[:checked-element-count (inc (count (:element-types actual))) ]
                                  [:checked-element-type :gravity.type/keyword]]]
                (let [changed-row (assoc-in row [:rhs 0 :compatibility key] value)
                      rejected (facts-and-context body input [changed-row])]
                  (is (= :rejected (get-in rejected [:context :status])))
                  (is (false? (local-valid? body input (:checked rejected) actual)))
                  (is (= :rejected (:status (compile (assoc-in typed [:ledger 0 :rhs 0 :compatibility key] value)))))))))
          (doseq [changed [(assoc-in typed [:forms (get-in input [:declaration :marker-form-id]) :value] :forged)
                           (assoc-in typed [:edges :call :callee-function-syntax-id] :orphan)
                           (update typed :ledger into (:ledger typed))
                           (assoc-in typed [:calls :call :argument-node-ids] [])]]
            (is (= :rejected (:status (compile changed))))))))
    (let [input (parameter-input helper :hom :gravity.type/never)
          {:keys [core call edge callee nodes bindings]} input
          produced ((helper 'bare-list-parameter-constraints) input)
          {:keys [context checked]} (facts-and-context body input (:constraints produced))]
      (is (= :not-applicable (:status (c7 'sh08-lp-compatibility [core call edge callee 0 :gravity.type/never nodes bindings 65536]))))
      (is (= :accepted (:status context)))
      (is (true? (local-valid? body input checked :gravity.type/never))))))

(deftest parameter-invalid-members-and-declaration-controls
  (let [{:keys [helper c7 body emit base]} (environment)
        callbacks (atom [])
        opaque (proxy [Object] []
                 (hashCode [] (swap! callbacks conj :hash) (throw (AssertionError. "opaque hash")))
                 (equals [_] (swap! callbacks conj :equals) (throw (AssertionError. "opaque equality")))
                 (toString [] (swap! callbacks conj :format) (throw (AssertionError. "opaque formatting"))))]
    (doseq [[family actual] [[:map (finite-map [:gravity.type/keyword])]
                             [:map (finite-map [:gravity.type/keyword :gravity.type/unknown])]
                             [:bare (positional [:gravity.type/string :gravity.type/unknown])]
                             [:hom (positional [:gravity.type/string :gravity.type/string :gravity.type/keyword])]
                             [:hom (positional (assoc (vec (repeat 1024 :gravity.type/string)) 1023 :gravity.type/keyword))]
                             [:hom (positional '())]
                             [:hom (positional [:gravity.type/never])]
                             [:hom (positional [:gravity.type/string opaque])]]]
      (let [{:keys [core call edge callee nodes bindings] :as input} (parameter-input helper family actual)
            a (c7 'sh08-lp-compatibility [core call edge callee 0 actual nodes bindings 65536])
            b (body 'body-lp-compatibility core call edge callee 0 actual nodes bindings 65536)
            c (emit 'p15-s23-lp-compatibility core call edge callee 0 actual nodes bindings 65536)
            produced ((helper 'bare-list-parameter-constraints) input)
            {:keys [checked]} (facts-and-context body input (:constraints produced))
            lowered (emit 'p15-s23-typed-compile-node (typed-call-context base c7 input actual) :call 65536)]
        (is (= [:rejected :rejected :rejected] (mapv :status [a b c])))
        (is (seq (:diagnostics produced)))
        (is (false? (local-valid? body input checked actual)))
        (is (= :rejected (:status lowered)))
        (is (empty? @callbacks))))
    (doseq [family [:map :bare :hom]]
      (let [actual (case family :map (finite-map []) (positional []))
            {:keys [core call edge callee nodes bindings declaration] :as input} (parameter-input helper family actual)
            bid (if (= family :hom) (:element-binding-id declaration) (:type-binding-id declaration))]
        (doseq [changed-bindings [(assoc-in bindings [bid :kind] :var) (assoc-in bindings [bid :namespace] 'other)]]
          (is (= :rejected (:status (c7 'sh08-lp-compatibility [core call edge callee 0 actual nodes changed-bindings 65536]))))
          (is (= :rejected (:status (body 'body-lp-compatibility core call edge callee 0 actual nodes changed-bindings 65536))))
          (is (= :rejected (:status (emit 'p15-s23-lp-compatibility core call edge callee 0 actual nodes changed-bindings 65536))))
          (is (= :rejected (:status (emit 'p15-s23-typed-compile-node
                                         (assoc-in (typed-call-context base c7 input actual) [:bindings bid]
                                                   (get changed-bindings bid)) :call 65536)))))
        (is (= :not-applicable (:status (c7 'sh08-lp-compatibility [core call edge callee 1 actual nodes bindings 65536]))))))
    (is (empty? @callbacks))))
