(ns gravity.self-hosting.sh08-inference-occurs-model-test
  "Compiler-free equation model and shared internal-DATA fixtures.
  Model outcomes are not execution or acceptance of the Gravity solver."
  (:require [clojure.test :refer [deftest is testing]]))

(def cases
  [{:name :self-growth :equations [[:x [:vector [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :expected true}
   {:name :nested-growth :equations [[:x [:vector [:vector [:ref :x]]]]]
    :bindings {:x :gravity.type/integer} :expected true}
   {:name :mutual-growth :equations [[:x [:ref :y]] [:y [:vector [:ref :x]]]]
    :bindings {:x :gravity.type/integer :y :gravity.type/integer} :expected true}
   {:name :alias-fixed-point :equations [[:x [:ref :y]] [:y [:ref :x]]]
    :bindings {:x :gravity.type/integer :y :gravity.type/integer} :expected false}
   {:name :alias-oscillation :equations [[:x [:ref :y]] [:y [:ref :x]]]
    :bindings {:x :gravity.type/integer :y :gravity.type/string} :expected false}
   {:name :unresolved :equations [[:x [:vector [:ref :x]]]]
    :bindings {} :expected false}
   {:name :never :equations [[:x [:vector [:ref :x]]]]
    :bindings {:x :gravity.type/never} :expected false}
   {:name :open-dependency :equations [[:x [:vector [:ref :outside]]]]
    :bindings {:x :gravity.type/integer :outside :gravity.type/integer} :expected false}
   {:name :constructor-leading-to-alias-cycle
    :equations [[:x [:vector [:ref :y]]] [:y [:ref :z]] [:z [:ref :y]]]
    :bindings {:x :gravity.type/integer :y :gravity.type/integer :z :gravity.type/integer}
    :expected false}
   {:name :duplicate-binding :equations [[:x [:vector [:ref :x]]] [:x [:ref :outside]]]
    :bindings {:x :gravity.type/integer} :expected false}
   {:name :definition-overlap :equations [[:x [:vector [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :definitions [{:binding-id :x}] :expected false}
   {:name :parameter-overlap :equations [[:x [:vector [:ref :x]]]]
    :bindings {:x :gravity.type/integer}
    :functions {:f {:parameter-binding-ids [:x]}} :expected false}
   {:name :match-reseeding :equations [[:x [:vector [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :context {:boolean-matches [{}]} :expected false}
   {:name :loop-transfer :equations [[:x [:vector [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :context {:loop-core {}} :expected false}
   {:name :intrinsic-reference :equations [[:x [:vector [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :intrinsic-reference? true :expected false}
   {:name :nonunary-vector :equations [[:x [:vector [:ref :x] [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :expected false}
   {:name :opaque-call :equations [[:x [:call [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :expected false}])

(def set-cases
  [{:name :set-self-growth :equations [[:x [:set [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :expected true}
   {:name :mixed-nested-growth :equations [[:x [:set [:vector [:ref :x]]]]]
    :bindings {:x :gravity.type/integer} :expected true}
   {:name :mixed-mutual-growth
    :equations [[:x [:vector [:ref :y]]] [:y [:set [:ref :x]]]]
    :bindings {:x :gravity.type/integer :y :gravity.type/integer} :expected true}
   {:name :set-unresolved :equations [[:x [:set [:ref :x]]]]
    :bindings {} :expected false}
   {:name :set-never :equations [[:x [:set [:ref :x]]]]
    :bindings {:x :gravity.type/never} :expected false}
   {:name :set-open-dependency :equations [[:x [:set [:ref :outside]]]]
    :bindings {:x :gravity.type/integer :outside :gravity.type/integer} :expected false}
   {:name :set-leading-to-alias-cycle
    :equations [[:x [:set [:ref :y]]] [:y [:ref :z]] [:z [:ref :y]]]
    :bindings {:x :gravity.type/integer :y :gravity.type/integer :z :gravity.type/integer}
    :expected false}
   {:name :set-intrinsic-reference :equations [[:x [:set [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :intrinsic-reference? true :expected false}
   {:name :set-match-reseeding :equations [[:x [:set [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :context {:boolean-matches [{}]} :expected false}
   {:name :nonunary-set :equations [[:x [:set [:ref :x] [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :expected false}])

(defn input
  "Materialize fixture syntax in postorder for the actual C7 test harness."
  [{:keys [equations context intrinsic-reference?] :as case}]
  (let [nodes (atom [])
        emit (fn emit [term path]
               (let [id (keyword (str "occurs-" path))
                     [kind & operands] term
                     children (if (= :ref kind) []
                                (mapv (fn [i operand] (emit operand (str path "-" i)))
                                      (range) operands))
                     node (if (= :ref kind)
                            {:node-id id :core-form :reference :children []
                             :attributes {:binding-id (first operands)}}
                            {:node-id id
                             :core-form (if (#{:vector :set} kind) :collection-literal :call)
                             :children children :attributes {:literal-kind kind}})]
                 (swap! nodes conj node)
                 id))
        lexical (mapv (fn [i [binding term]]
                        {:binding-id binding :initializer-node-id (emit term (str i))})
                      (range) equations)
        operators (when intrinsic-reference?
                    (into {} (for [node @nodes :when (= :reference (:core-form node))]
                               [(:node-id node) {}])))]
    (assoc case :nodes @nodes :lexical lexical
           :context (cond-> (or context {}) operators (assoc :ordering-operators operators)))))

(defn- term-edge [term]
  (case (first term)
    :ref {:dependency (second term) :constructor? false}
    (:vector :set) (when (= 2 (count term))
              (when-let [edge (term-edge (second term))]
                (assoc edge :constructor? true)))
    nil))

(defn model-occurs?
  "Independent graph oracle: trim all noncycle vertices, then inspect only
  surviving cycle edges for constructors. No Gravity code is evaluated."
  [{:keys [equations bindings context definitions functions intrinsic-reference?]}]
  (let [ids (mapv first equations)
        blocked (concat (map :binding-id definitions)
                        (mapcat :parameter-binding-ids (vals functions)))]
    (if (or (seq (:boolean-matches context)) (map? (:loop-core context))
            intrinsic-reference? (not= (count ids) (count (set ids)))
            (some (set ids) blocked))
      false
      (loop [graph (into {} (keep (fn [[id term]]
                                   (when (and (contains? bindings id)
                                              (not (#{:gravity.type/unknown :gravity.type/never}
                                                    (get bindings id))))
                                     (when-let [edge (term-edge term)] [id edge]))) equations))]
        (let [incoming (set (map :dependency (vals graph)))
              trimmed (into {} (filter (fn [[id edge]]
                                         (and (incoming id) (contains? graph (:dependency edge)))) graph))]
          (if (= graph trimmed)
            (boolean (some :constructor? (vals graph)))
            (recur trimmed)))))))

(defn- substitute [term bindings]
  (case (first term)
    :ref (get bindings (second term) :gravity.type/unknown)
    (:vector :set) (let [values (mapv #(substitute % bindings) (rest term))]
              (cond
                (some #{:gravity.type/never} values) :gravity.type/never
                (some #{:gravity.type/unknown} values) :gravity.type/unknown
                :else {:kind (if (= :set (first term)) :gravity.type/set :gravity.type/vector)
                       :element-types values :shape :literal}))
    :gravity.type/unknown))

(defn- step [case bindings]
  (into bindings (map (fn [[id term]] [id (substitute term bindings)]) (:equations case))))

(deftest structural-cycle-model-has-positive-and-negative-controls
  (doseq [case cases]
    (testing (name (:name case))
      (is (= (:expected case) (model-occurs? case))))))

(deftest substitution-distinguishes-growth-oscillation-and-fixed-points
  (let [by-name (into {} (map (juxt :name identity)) cases)]
    (doseq [id [:self-growth :nested-growth :mutual-growth]]
      (let [case (get by-name id)
            states (take 9 (iterate #(step case %) (:bindings case)))]
        (is (= 9 (count (set states))))))
    (let [case (get by-name :alias-oscillation) seed (:bindings case)]
      (is (not= seed (step case seed)))
      (is (= seed (step case (step case seed)))))
    (doseq [id [:alias-fixed-point :unresolved :never]]
      (let [case (get by-name id) once (step case (:bindings case))]
        (is (= once (step case once)))))))

(deftest graph-length-is-not-an-admission-allowance
  (doseq [length [1 7 80]]
    (let [ids (mapv #(keyword (str "b" %)) (range length))
          cycle {:equations (mapv (fn [i id]
                                    [id (if (zero? i)
                                          [:vector [:ref (get ids (mod (inc i) length))]]
                                          [:ref (get ids (mod (inc i) length))])])
                                  (range) ids)
                 :bindings (zipmap ids (repeat :gravity.type/integer))}]
      (is (true? (model-occurs? cycle)))
      (is (false? (model-occurs? (assoc-in cycle [:equations 0 1] [:ref :outside])))))))

(deftest unary-set-and-mixed-substitution-controls
  (doseq [case set-cases]
    (testing (name (:name case))
      (is (= (:expected case) (model-occurs? case)))
      (when (:expected case)
        (let [states (take 9 (iterate #(step case %) (:bindings case)))]
          (is (= 9 (count (set states))))))))
  (doseq [case (filter #(#{:set-unresolved :set-never :set-open-dependency} (:name %)) set-cases)]
    (let [once (step case (:bindings case))]
      (is (= once (step case once)))))
  (let [case (first set-cases)
        typed (get (step case (:bindings case)) :x)]
    (is (= {:kind :gravity.type/set :element-types [:gravity.type/integer] :shape :literal} typed))))
