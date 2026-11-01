(ns gravity.self-hosting.module-census-test
  "Complete development runtime manifest validation without a census allowance."
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh01-stage2-runtime-iteration-test]))

(def ^:private runtime-plan
  (delay
    (let [path "bootstrap/gravity/src/gravity/runtime.gravity"
          emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path)))))
(defn- invoke [name & arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :module-census-test} @runtime-plan name (vec arguments)))
(defn- fixture []
  ((ns-resolve 'gravity.self-hosting.sh01-stage2-runtime-iteration-test 'module-literal-runtime-input)))
(defn- with-literals [n]
  (let [{:keys [plan root]} (fixture)
        roots (mapv #(assoc (root (+ % 3) :literal (symbol (str "extra" %))) :type :gravity.type/bool) (range n))
        initializers (mapv (fn [root] {:ordinal (:ordinal root) :binding-id (:binding-id root)
                                      :instruction {:op :literal :value true}}) roots)]
    (-> plan
        (update-in [:module-initialization :roots] into roots)
        (update-in [:module-initialization :initializers] into initializers))))
(defn- census [plan] (invoke 'p15-s23-runtime-module-census plan))
(defn- execute [plan] (invoke 'p15-s23-runtime-execute-closed-plan plan))

(deftest complete-many-root-census-and-execution
  (let [plan (with-literals 4097)
        roots (get-in plan [:module-initialization :roots])
        literals (filterv #(= :literal (:kind %)) roots)
        checked (census plan)
        result (execute plan)
        expected (mapv :value (:items (:initializer (fixture))))]
    (is (= :accepted (:status checked)))
    (is (not (contains? checked :work-count)))
    (is (true? (= (into {} (map (juxt :binding-id identity) literals)) (:definitions checked))))
    (is (true? (= literals (mapv :definition (:initializers checked)))))
    (is (= :complete (:status result)))
    (is (= [expected expected] (:entrypoint-result result)))
    (is (= "" (:stdout result)))
    (is (true? (= (mapv :binding-id literals)
                  (get-in result [:module-initialization :initialized-binding-ids]))))
    (is (= 4098 (count (get-in result [:module-initialization :validated-constructor-occurrences]))))))

(defn- main-last [plan]
  (let [roots (get-in plan [:module-initialization :roots])
        reordered (vec (concat (subvec roots 0 2) (subvec roots 3) [(get roots 2)]))
        ordered (mapv #(assoc %2 :ordinal %1) (range) reordered)
        ordinals (into {} (map (juxt :binding-id :ordinal) ordered))]
    (-> plan
        (assoc-in [:module-initialization :roots] ordered)
        (assoc-in [:functions 'main :module-root] (peek ordered))
        (update-in [:module-initialization :initializers]
                   #(mapv (fn [item] (assoc item :ordinal (ordinals (:binding-id item)))) %)))))

(deftest invalid-manifests-fail-before-initialization
  (let [small (with-literals 4) wide (with-literals 4097)
        late-main (main-last small)
        last-root (dec (count (get-in small [:module-initialization :roots])))
        last-initializer (dec (count (get-in small [:module-initialization :initializers])))
        cases [[(assoc-in wide [:module-initialization :roots 4099 :definition-source] nil)
                :module-initialization-root-source]
               [(assoc-in small [:module-initialization :roots last-root :ordinal] -1)
                :module-initialization-root-source]
               [(-> small
                    (assoc-in [:module-initialization :roots last-root :binding-id] "binding-modes")
                    (assoc-in [:module-initialization :initializers last-initializer :binding-id] "binding-modes"))
                :module-initialization-root-source]
               [(assoc-in small [:module-initialization :roots last-root :name] 'modes)
                :module-initialization-root-source]
               [(assoc-in small [:module-initialization :roots last-root :definition-core-node-id] "initializer-modes")
                :module-initialization-root-source]
               [(assoc-in small [:module-initialization :roots last-root :value-core-node-id] "definition-modes")
                :module-initialization-root-source]
               [(assoc-in small [:module-initialization :roots last-root :definition-core-node-id]
                          (get-in small [:module-initialization :roots last-root :value-core-node-id]))
                :module-initialization-root-source]
               [(assoc-in small [:module-initialization :roots last-root :type] :gravity.type/keyword)
                :module-initialization-type]
               [(assoc-in small [:module-initialization :initializers last-initializer :ordinal] -1)
                :module-initialization-source-order]
               [(assoc-in small [:module-initialization :initializers last-initializer :instruction] {:op :foreign})
                :unsupported-module-initializer]
               [(update-in small [:module-initialization :initializers] pop)
                :module-initialization-source-order]
               [(assoc-in late-main [:functions 'main :params] nil)
                :module-initialization-function-source]
               [(assoc-in late-main [:functions 'main :type-input :function-core-node-id] "forged")
                :module-initialization-function-source]
               [(assoc-in small [:functions 'orphan] (get-in small [:functions 'main]))
                :module-initialization-census]
               [(assoc-in small [:module-initialization :extra] true)
                :invalid-module-initialization-manifest]]
        started (atom 0)
        scoped-var (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-function-scoped)
        original @scoped-var]
    (is (= :accepted (:status (census late-main))))
    (with-redefs-fn
      {scoped-var (fn [& arguments]
                    (when (= 'p15-s23-runtime-initialize-module (nth arguments 2))
                      (swap! started inc))
                    (apply original arguments))}
      (fn []
        ;; Positive observation proves this is the actual initialization path.
        (is (= :complete (:status (execute small))))
        (is (= 1 @started))
        (doseq [[changed reason] cases]
          (testing (str reason)
            (reset! started 0)
            (let [checked (census changed) result (execute changed)]
              (is (= :rejected (:status checked) (:status result)))
              (is (= reason (:reason checked) (get-in result [:diagnostic :reason])))
              (is (not (contains? checked :work-count)))
              (is (= "" (:stdout result)))
              (is (zero? @started)))))
        (doseq [[value element reason] [[:duplicate :gravity.type/keyword :duplicate-set-literal-keyword]
                                        ["duplicate" :gravity.type/string :duplicate-set-literal-string]]]
          (reset! started 0)
          (let [changed (-> small
                            (assoc-in [:module-initialization :roots last-root :type]
                                      {:kind :gravity.type/set :shape :literal :element-types [element element]})
                            (assoc-in [:module-initialization :initializers last-initializer :instruction]
                                      {:op :set-literal :items [{:op :literal :value value} {:op :literal :value value}]}))
                checked (census changed) result (execute changed)]
            (is (= :rejected (:status checked) (:status result)))
            (is (= :unsupported-module-initializer (:reason checked) (get-in result [:diagnostic :reason])))
            (is (= reason (:initializer-reason checked)))
            (is (not (contains? checked :work-count)))
            (is (zero? @started))))))))

(defn- initializer-plan [instruction type]
  (-> (:plan (fixture))
      (assoc-in [:module-initialization :roots 0 :type] type)
      (assoc-in [:module-initialization :initializers 0 :instruction] instruction)
      (assoc-in [:functions 'getter :instructions 0 :type] type)))

(deftest initializer-validation-has-no-work-or-depth-allowance
  (let [items (vec (repeat 33000 {:op :literal :value true}))
        instruction {:op :vector-literal :items items}
        expected (vec (repeat 33000 true))
        result (execute (initializer-plan instruction :gravity.type/vector))]
    (is (= {:status :accepted :constructors {:vector-literal 1}}
           (invoke 'runtime-validate-initializer instruction)))
    (is (= :complete (:status result)))
    (is (true? (= [expected expected] (:entrypoint-result result))))
    (is (identical? (first (:entrypoint-result result)) (second (:entrypoint-result result))))
    (is (= {"binding-modes" {:vector-literal 1}}
           (get-in result [:module-initialization :validated-constructor-occurrences])))
    (let [changed (assoc-in instruction [:items 32999] {:op :foreign})
          checked (census (initializer-plan changed :gravity.type/vector))]
      (is (= {:status :rejected :reason :unsupported-module-initializer} checked))
      (is (= :rejected (:status (execute (initializer-plan changed :gravity.type/vector)))))))
  (doseq [value [(nth (iterate vector :leaf) 130)
                 (with-meta [true {:op :throw :value {:op :literal :value :inert}}] {:tag :inert})
                 (with-meta (subvec [(Object.) :visible (Object.)] 1 2) {:view :inert})
                 (sorted-map "data-key" :value)]]
    (let [instruction {:op :quote :value value}
          type (if (map? value) :gravity.type/map :gravity.type/vector)
          result (execute (initializer-plan instruction type))]
      (is (= {:status :accepted :constructors {}}
             (invoke 'runtime-validate-initializer instruction)))
      (is (= :complete (:status result)))
      (is (identical? value (first (:entrypoint-result result))))
      (is (identical? (meta value) (meta (first (:entrypoint-result result)))))
      (is (= {"binding-modes" {}}
             (get-in result [:module-initialization :validated-constructor-occurrences]))))))

(defn- set-fixture-field! [value field-name replacement]
  (let [field (.getDeclaredField (class value) field-name)]
    (is (.trySetAccessible field))
    (.set field value replacement)
    value))

(deftest initializer-native-preflight-precedes-field-access
  (let [callbacks (atom 0)
        deny (fn [] (swap! callbacks inc) (throw (AssertionError. "foreign callback")))
        foreign-map (java.lang.reflect.Proxy/newProxyInstance
                     (.getClassLoader clojure.lang.IPersistentMap)
                     (into-array Class [clojure.lang.IPersistentMap clojure.lang.IObj])
                     (reify java.lang.reflect.InvocationHandler (invoke [_ _ _ _] (deny))))
        opaque (proxy [Object] [] (hashCode [] (deny)) (equals [_] (deny)) (toString [] (deny)))
        keyed (clojure.lang.PersistentArrayMap. (object-array [opaque :value]))
        cycle (clojure.lang.PersistentArrayMap. (object-array [:self nil]))
        metadata-cycle (with-meta [] {:self nil})
        bad-view (clojure.lang.APersistentVector$SubVector. nil [true] 0 2)
        view-cycle (clojure.lang.APersistentVector$SubVector. nil [] 0 0)]
    (set-fixture-field! cycle "array" (object-array [:self cycle]))
    (set-fixture-field! (meta metadata-cycle) "array" (object-array [:self metadata-cycle]))
    (set-fixture-field! view-cycle "v" view-cycle)
    (doseq [[instruction reason]
            [[foreign-map :untrusted-map-class-or-comparator]
             [{:op :quote :value foreign-map} :untrusted-map-class-or-comparator]
             [{:op :quote :value keyed} :untrusted-scalar-class]
             [{:op :quote :value (with-meta [:visible] {:hidden opaque})} :untrusted-scalar-class]
             [{:op :quote :value cycle} :cyclic-carrier]
             [{:op :quote :value metadata-cycle} :cyclic-carrier]
             [{:op :quote :value bad-view} :invalid-vector-view-extent]
             [{:op :quote :value view-cycle} :cyclic-vector-backing]]]
      (is (= {:status :rejected :reason :unsupported-module-initializer :initializer-reason reason}
             (invoke 'runtime-validate-initializer instruction))))
    (is (zero? @callbacks)))
  (is (= {:status :rejected :reason :unsupported-module-initializer}
         (invoke 'runtime-validate-initializer {:op :quote :value :data :supplied-preflight {:status :passed}}))))

(deftest initializer-domains-and-set-construction-remain-distinct
  (let [values (mapv #(str "member-" %) (range 1025))
        instruction {:op :set-literal :items (mapv #(hash-map :op :literal :value %) values)}
        type {:kind :gravity.type/set :shape :literal :element-types (vec (repeat 1025 :gravity.type/string))}]
    (is (= {:status :accepted :constructors {:set-literal 1}}
           (invoke 'runtime-validate-initializer instruction)))
    ;; Current collection validation and execution also have no size quota.
    (let [plan (initializer-plan instruction type)
          checked (census plan)
          result (execute plan)
          expected (set values)]
      (is (= :accepted (:status checked)))
      (is (= :complete (:status result)))
      (is (true? (= [expected expected] (:entrypoint-result result))))
      (is (= "" (:stdout result)))))
  (doseq [[value element duplicate-reason]
          [[:same :gravity.type/keyword :duplicate-set-literal-keyword]
           ["same" :gravity.type/string :duplicate-set-literal-string]]]
    (let [items (vec (repeat 1025 {:op :literal :value value}))
          invalid (assoc items 1024 {:op :literal :value true})]
      (is (= {:status :rejected :reason :unsupported-module-initializer}
             (invoke 'runtime-validate-initializer {:op :set-literal :items invalid})))
      (is (= {:status :rejected :reason :unsupported-module-initializer :initializer-reason duplicate-reason}
             (invoke 'runtime-validate-initializer {:op :set-literal :items items}))))
    (let [value (if (= element :gravity.type/string) (apply str (repeat 80000 "x"))
                    (keyword (apply str (repeat 80000 "x"))))
          instruction {:op :set-literal :items [{:op :literal :value value}]}
          evaluated (invoke 'p15-s23-runtime-evaluate-closed-instruction instruction {} "")]
      (is (= {:status :accepted :constructors {:set-literal 1}}
             (invoke 'runtime-validate-initializer instruction)))
      (is (= :normal (get evaluated 2)))
      (is (true? (= #{value} (get evaluated 0))))
      (is (= "" (get evaluated 1))))))

(deftest set-literal-construction-has-no-width-or-content-allowance
  (doseq [[family values constructor]
          [[:gravity.type/integer (vec (range 1030)) 'runtime-construct-scalar-set]
           [:gravity.type/string (mapv #(str "member-" %) (range 1030)) 'runtime-construct-scalar-set]
           [:gravity.type/keyword (mapv #(keyword (str "member-" %)) (range 1030)) 'runtime-construct-scalar-set]
           [:gravity.type/character (mapv char (range 1024 2054)) 'runtime-construct-character-set]]]
    (let [instruction {:op :set-literal :items (mapv #(hash-map :op :literal :value %) values)}
          type {:kind :gravity.type/set :shape :literal :element-types (vec (repeat (count values) family))}
          built (invoke constructor values)
          actual (invoke 'p15-s23-runtime-evaluate-closed-instruction instruction {} "")]
      (is (= :accepted (:status built)))
      (is (true? (= (set values) (:value built))))
      (is (true? (= [(set values) "" :normal] actual)))
      (is (not-any? #(contains? built %) [:work-count :work-count-kind :representation-units :value-work-model
                                        :input-element-count :comparison-attempts :insertions]))
      (if (contains? #{:gravity.type/string :gravity.type/keyword} family)
        (let [plan (initializer-plan instruction type)
              checked (census plan) result (execute plan)]
          (is (= :accepted (:status checked)))
          (is (= :complete (:status result)))
          (is (true? (= [(set values) (set values)] (:entrypoint-result result))))
          (is (identical? (first (:entrypoint-result result)) (second (:entrypoint-result result))))
          (is (= {"binding-modes" {:set-literal 1}}
                 (get-in result [:module-initialization :validated-constructor-occurrences])))
          (is (= "" (:stdout result)))
          (doseq [changed [(assoc-in plan [:module-initialization :roots 0 :type :element-types 1029] :gravity.type/unknown)
                           (update-in plan [:module-initialization :roots 0 :type :element-types] pop)
                           (assoc-in plan [:module-initialization :roots 0 :definition-source] nil)]]
            (is (= :rejected (:status (census changed))))))
        ;; Root initializer support is still only String/Keyword literal Sets.
        (is (= :unsupported-module-initializer
               (:reason (census (initializer-plan instruction type)))))))))

(deftest set-literal-native-inputs-precede-member-access
  (let [callbacks (atom 0)
        deny (fn [] (swap! callbacks inc) (throw (AssertionError. "foreign Set input callback")))
        foreign-map (java.lang.reflect.Proxy/newProxyInstance
                     (.getClassLoader clojure.lang.IPersistentMap)
                     (into-array Class [clojure.lang.IPersistentMap clojure.lang.IObj])
                     (reify java.lang.reflect.InvocationHandler (invoke [_ _ _ _] (deny))))
        foreign-vector (java.lang.reflect.Proxy/newProxyInstance
                        (.getClassLoader clojure.lang.IPersistentVector)
                        (into-array Class [clojure.lang.IPersistentVector clojure.lang.IObj])
                        (reify java.lang.reflect.InvocationHandler (invoke [_ _ _ _] (deny))))
        opaque (proxy [Object] [] (hashCode [] (deny)) (equals [_] (deny)) (toString [] (deny)))
        cycle (clojure.lang.PersistentArrayMap. (object-array [:self nil]))
        invalid-view (clojure.lang.APersistentVector$SubVector. nil [] 0 1)
        ir {:op :set-literal :items [{:op :literal :value :valid}]}
        definition {:type {:kind :gravity.type/set :shape :literal :element-types [:gravity.type/keyword]}}]
    (set-fixture-field! cycle "array" (object-array [:self cycle]))
    (doseq [constructor ['runtime-construct-scalar-set 'runtime-construct-character-set]
            input [foreign-vector [foreign-map] [opaque] [cycle] invalid-view
                   (with-meta [:valid] {:hidden opaque})]]
      (let [result (invoke constructor input)]
        (is (= :set-literal-native-data-required (:reason result)))))
    (doseq [changed [foreign-map (assoc definition :type foreign-map)
                    (assoc-in definition [:type :element-types] foreign-vector)
                    (assoc-in definition [:type :element-types] [cycle])
                    (with-meta definition {:hidden opaque})]]
      (is (false? (invoke 'p15-s23-runtime-initializer-type? changed ir))))
    (doseq [changed [(assoc ir :items foreign-vector)
                    (assoc-in ir [:items 0 :value] opaque)
                    (with-meta ir {:hidden opaque})]]
      (is (false? (invoke 'p15-s23-runtime-initializer-type? definition changed)))
      (is (= :invalid-local-runtime-instruction
             (get-in (invoke 'p15-s23-runtime-evaluate-closed-instruction changed {} "") [0 :reason]))))
    (is (zero? @callbacks))
    (doseq [constructor ['runtime-construct-scalar-set 'runtime-construct-character-set]]
      (let [value (if (= constructor 'runtime-construct-character-set) \a :valid)
            input (vec (concat (repeat 1029 value) [opaque]))
            result (invoke constructor input)]
        ;; A late invalid member is rejected before the earlier duplicates compare.
        (is (= :set-literal-native-data-required (:reason result)))))
    (is (zero? @callbacks))))

(deftest runtime-set-conversion-validates-native-inputs
  (let [callbacks (atom 0)
        deny (fn [] (swap! callbacks inc) (throw (AssertionError. "foreign conversion callback")))
        foreign (fn [interface]
                  (java.lang.reflect.Proxy/newProxyInstance
                   (.getClassLoader interface) (into-array Class [interface clojure.lang.IObj])
                   (reify java.lang.reflect.InvocationHandler (invoke [_ _ _ _] (deny)))))
        foreign-vector (foreign clojure.lang.IPersistentVector)
        foreign-list (foreign clojure.lang.IPersistentList)
        opaque (proxy [Object] [] (hashCode [] (deny)) (equals [_] (deny)) (toString [] (deny)))
        cycle (list :a :b)
        bad-view (clojure.lang.APersistentVector$SubVector. nil [] 0 1)
        functions ['runtime-convert-vector-set 'runtime-convert-keyword-list-set]
        convert (fn [value] {:op :builtin-call :function 'set :args [{:op :literal :value value}]})]
    (set-fixture-field! cycle "_rest" cycle)
    (doseq [constructor functions
            value [foreign-vector foreign-list [opaque] (list opaque) cycle bad-view
                   (with-meta [:valid] {:hidden opaque}) (with-meta '(:valid) {:hidden opaque})]]
      (let [result (invoke constructor value)]
        (is (= :set-conversion-native-data-required (:reason result)))))
    (doseq [value [foreign-vector foreign-list [opaque] cycle bad-view
                   (with-meta [:valid] {:hidden opaque})]]
      (is (= :set-conversion-native-data-required
             (get-in (invoke 'p15-s23-runtime-evaluate-closed-instruction (convert value) {} "") [0 :reason]))))
    (is (zero? @callbacks))
    (doseq [[constructor value expected]
            [['runtime-convert-vector-set (with-meta (subvec [(Object.) :a :a (Object.)] 1 3) {:tag :inert}) #{:a}]
             ['runtime-convert-vector-set (with-meta ["a" "a"] {:tag :inert}) #{"a"}]
             ['runtime-convert-keyword-list-set (with-meta '(:a :a :b) {:tag :inert}) #{:a :b}]]]
      (let [metadata (meta value) result (invoke constructor value)]
        (is (= :accepted (:status result)))
        (is (= expected (:value result)))
        (is (identical? metadata (meta value)))))
    (is (zero? @callbacks))))

(deftest runtime-set-conversion-preserves-complete-large-results
  (let [callbacks (atom 0)
        opaque (proxy [Object] []
                 (hashCode [] (swap! callbacks inc) (throw (AssertionError. "conversion member hashed")))
                 (equals [_] (swap! callbacks inc) (throw (AssertionError. "conversion member compared")))
                 (toString [] (swap! callbacks inc) (throw (AssertionError. "conversion member printed"))))]
    (doseq [mode [:string :keyword :list]]
      (let [constructor (if (= mode :list) 'runtime-convert-keyword-list-set 'runtime-convert-vector-set)
            carrier #(if (= mode :list) (apply list %) (vec %))
            members (mapv #(if (= mode :string) (str "member-" %) (keyword (str "member-" %))) (range 1030))
            input (carrier (into members members))
            expected (set members)
            built (invoke constructor input)
            evaluate (fn [value]
                       (invoke 'p15-s23-runtime-evaluate-closed-instruction
                               {:op :builtin-call :function 'set :args [{:op :literal :value value}]} {} ""))
            long-value (if (= mode :string) (apply str (repeat 80000 "x"))
                           (keyword (apply str (repeat 80000 "x"))))]
        (is (= :accepted (:status built)))
        (is (true? (= expected (:value built))))
        (is (true? (= [expected "" :normal] (evaluate input))))
        (is (= #{long-value} (:value (invoke constructor (carrier [long-value long-value])))))
        (is (= [#{long-value} "" :normal] (evaluate (carrier [long-value long-value]))))
        (let [invalid (carrier (concat members [opaque]))]
          (is (= :set-conversion-native-data-required (:reason (invoke constructor invalid))))
          (is (= :set-conversion-native-data-required (get-in (evaluate invalid) [0 :reason]))))
        (doseq [bad [nil true 1 [] {}]]
          (is (= :rejected (:status (invoke constructor (carrier (concat members [bad])))))))
        (is (zero? @callbacks))))))
