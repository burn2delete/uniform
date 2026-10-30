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
    :bindings {:x :gravity.type/integer} :expected true}
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
    :bindings {:x :gravity.type/integer} :expected true}])

(def map-cases
  [{:name :map-self-growth :equations [[:x [:map [:keyword :k] [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :expected true}
   {:name :map-set-nested-growth :equations [[:x [:map [:keyword :k] [:set [:ref :x]]]]]
    :bindings {:x :gravity.type/integer} :expected true}
   {:name :map-vector-mutual-growth
    :equations [[:x [:map [:keyword :k] [:ref :y]]] [:y [:vector [:ref :x]]]]
    :bindings {:x :gravity.type/integer :y :gravity.type/integer} :expected true}
   {:name :map-acyclic :equations [[:x [:map [:keyword :k] [:ref :outside]]]]
    :bindings {:x :gravity.type/integer :outside :gravity.type/integer} :expected false}
   {:name :map-unresolved :equations [[:x [:map [:keyword :k] [:ref :x]]]]
    :bindings {} :expected false}
   {:name :map-never :equations [[:x [:map [:keyword :k] [:ref :x]]]]
    :bindings {:x :gravity.type/never} :expected false}
   {:name :map-dynamic-key :equations [[:x [:map [:ref :key] [:ref :x]]]]
    :bindings {:x :gravity.type/integer :key :gravity.type/keyword} :expected true}
   {:name :map-string-key :equations [[:x [:map [:string "k"] [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :expected true}
   {:name :map-odd-arity :equations [[:x [:map [:keyword :k]]]]
    :bindings {:x :gravity.type/integer} :expected false}
   {:name :map-multiple-members
    :equations [[:x [:map [:keyword :k] [:ref :x] [:keyword :j] [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :expected true}
   {:name :map-definition-overlap :equations [[:x [:map [:keyword :k] [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :definitions [{:binding-id :x}] :expected false}
   {:name :map-parameter-overlap :equations [[:x [:map [:keyword :k] [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :functions {:f {:parameter-binding-ids [:x]}} :expected false}
   {:name :map-intrinsic-value :equations [[:x [:map [:keyword :k] [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :intrinsic-reference? true :expected false}
   {:name :map-loop-transfer :equations [[:x [:map [:keyword :k] [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :context {:loop-core {}} :expected false}
   {:name :map-leading-to-alias-cycle
    :equations [[:x [:map [:keyword :k] [:ref :y]]] [:y [:ref :z]] [:z [:ref :y]]]
    :bindings {:x :gravity.type/integer :y :gravity.type/integer :z :gravity.type/integer} :expected false}])

(def prefix-vector-cases
  ;; A false expectation means no witness in this family, not input acceptance.
  [{:name :prefix-vector-self-growth :equations [[:x [:vector [:keyword :k] [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :expected true}
   {:name :prefix-vector-set-growth :equations [[:x [:set [:vector [:keyword :k] [:ref :x]]]]]
    :bindings {:x :gravity.type/integer} :expected true}
   {:name :prefix-vector-map-mutual-growth
    :equations [[:x [:vector [:keyword :k] [:ref :y]]] [:y [:map [:keyword :j] [:ref :x]]]]
    :bindings {:x :gravity.type/integer :y :gravity.type/integer} :expected true}
   {:name :prefix-vector-acyclic :equations [[:x [:vector [:keyword :k] [:ref :outside]]]]
    :bindings {:x :gravity.type/integer :outside :gravity.type/integer} :expected false}
   {:name :prefix-vector-unresolved :equations [[:x [:vector [:keyword :k] [:ref :x]]]]
    :bindings {} :expected false}
   {:name :prefix-vector-never :equations [[:x [:vector [:keyword :k] [:ref :x]]]]
    :bindings {:x :gravity.type/never} :expected false}
   {:name :prefix-vector-dynamic-prefix :equations [[:x [:vector [:ref :prefix] [:ref :x]]]]
    :bindings {:x :gravity.type/integer :prefix :gravity.type/keyword} :expected true}
   {:name :prefix-vector-string-prefix :equations [[:x [:vector [:string "k"] [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :expected true}
   {:name :prefix-vector-reversed-members :equations [[:x [:vector [:ref :x] [:keyword :k]]]]
    :bindings {:x :gravity.type/integer} :expected true}
   {:name :prefix-vector-extra-member
    :equations [[:x [:vector [:keyword :k] [:ref :x] [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :expected true}
   {:name :prefix-vector-definition-overlap :equations [[:x [:vector [:keyword :k] [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :definitions [{:binding-id :x}] :expected false}
   {:name :prefix-vector-parameter-overlap :equations [[:x [:vector [:keyword :k] [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :functions {:f {:parameter-binding-ids [:x]}} :expected false}
   {:name :prefix-vector-intrinsic :equations [[:x [:vector [:keyword :k] [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :intrinsic-reference? true :expected false}
   {:name :prefix-vector-loop-transfer :equations [[:x [:vector [:keyword :k] [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :context {:loop-core {}} :expected false}
   {:name :prefix-vector-leading-to-alias-cycle
    :equations [[:x [:vector [:keyword :k] [:ref :y]]] [:y [:ref :z]] [:z [:ref :y]]]
    :bindings {:x :gravity.type/integer :y :gravity.type/integer :z :gravity.type/integer} :expected false}])

; The three former unsupported constant-position cases above now have positive
; witnesses. Multiple validated evolving members are now supported as well;
; opaque or incomplete members still block the whole equation. These fixture
; samples do not model every character carrier or source Set uniqueness.
(def literal-types
  {:nil :gravity.type/nil :boolean :gravity.type/bool :integer :gravity.type/integer
   :decimal :gravity.type/decimal :ratio :gravity.type/ratio :character :gravity.type/character
   :string :gravity.type/string :keyword :gravity.type/keyword :symbol :gravity.type/symbol})

(defn- fixed-term? [[kind value :as term]]
  (and (= 2 (count term)) (contains? literal-types kind)
       (or (not= :character kind) (char? value))))

(def constants
  [[:nil nil] [:boolean true] [:integer 1] [:decimal 1.5M] [:ratio 1/2]
   [:character \a] [:string "s"] [:keyword :k] [:symbol 's]])

(def fixed-member-cases
  (let [positions (for [kind [:vector :set :map] position (range (inc (count constants)))]
                    {:name (keyword (str (name kind) "-position-" position))
                     :equations [[:x (into [kind] (concat (take position constants)
                                                         [[:ref :x]] (drop position constants)))]]
                     :bindings {:x :gravity.type/integer} :expected true})]
    (vec (concat positions
      (for [[label additions]
            [[:fixed-members-definition-overlap {:definitions [{:binding-id :x}]}]
             [:fixed-members-parameter-overlap {:functions {:f {:parameter-binding-ids [:x]}}}]
             [:fixed-members-intrinsic {:intrinsic-reference? true}]
             [:fixed-members-loop-transfer {:context {:loop-core {}}}]
             [:fixed-members-match-seeding {:context {:boolean-matches [{}]}}]]]
        (merge (first positions) additions {:name label :expected false}))
      (for [case positions]
        (-> case
            (assoc :name (keyword (str (name (:name case)) "-acyclic")) :expected false :stable? true
                   :bindings {:x :gravity.type/integer :outside :gravity.type/integer})
            (update-in [:equations 0 1] #(mapv (fn [term] (if (= [:ref :x] term) [:ref :outside] term)) %))))
      [{:name :mixed-fixed-members-growth
        :equations [[:x [:map [:keyword :k] [:set [:integer 3] [:vector [:string "s"] [:ref :x] [:nil nil]]]
                         [:boolean true] [:symbol 's]]]]
        :bindings {:x :gravity.type/integer} :expected true}
       {:name :two-dynamic-members :equations [[:x [:vector [:ref :x] [:ref :outside]]]]
        :bindings {:x :gravity.type/integer :outside :gravity.type/integer} :expected true}
       {:name :opaque-sibling :equations [[:x [:vector [:ref :x] [:call [:ref :outside]]]]]
        :bindings {:x :gravity.type/integer :outside :gravity.type/integer} :expected false}
       {:name :opaque-selected-child :equations [[:x [:vector [:keyword :k] [:call [:ref :x]]]]]
        :bindings {:x :gravity.type/integer} :expected false}
       {:name :unknown-literal-sibling :equations [[:x [:vector [:ref :x] [:unsupported-literal :bad]]]]
        :bindings {:x :gravity.type/integer} :expected false}
       {:name :invalid-character-sibling :equations [[:x [:vector [:ref :x] [:character 65]]]]
        :bindings {:x :gravity.type/integer} :expected false}
       {:name :all-fixed-members :equations [[:x (into [:vector] constants)]]
        :bindings {:x :gravity.type/integer} :expected false :stable? true}
       {:name :fixed-members-unresolved :equations [[:x [:vector [:integer 1] [:ref :x] [:boolean true]]]]
        :bindings {} :expected false :stable? true}
       {:name :fixed-members-never :equations [[:x [:vector [:integer 1] [:ref :x] [:boolean true]]]]
        :bindings {:x :gravity.type/never} :expected false :stable? true}]))))

(def multiple-dependency-cases
  [{:name :repeated-evolving-member :equations [[:x [:vector [:ref :x] [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :expected true}
   {:name :distinct-mutual-members
    :equations [[:x [:vector [:ref :y] [:ref :z]]] [:y [:ref :x]] [:z [:integer 1]]]
    :bindings {:x :gravity.type/integer :y :gravity.type/integer :z :gravity.type/integer} :expected true}
   {:name :cycle-in-later-member
    :equations [[:x [:map [:ref :outside] [:set [:ref :y] [:ref :x]]]] [:y [:ref :z]] [:z [:ref :y]]]
    :bindings {:x :gravity.type/integer :y :gravity.type/integer :z :gravity.type/integer
               :outside :gravity.type/keyword} :expected true}
   {:name :branching-constructor-tail-into-alias-cycle
    :equations [[:x [:vector [:ref :y] [:ref :z]]] [:y [:ref :z]] [:z [:ref :y]]]
    :bindings {:x :gravity.type/integer :y :gravity.type/integer :z :gravity.type/integer} :expected false :stable? true}
   {:name :multiple-acyclic-members
    :equations [[:x [:vector [:ref :y] [:ref :z]]] [:y [:integer 1]] [:z [:string "s"]]]
    :bindings {:x :gravity.type/integer :y :gravity.type/integer :z :gravity.type/string} :expected false :stable? true}
   {:name :nested-fixed-sibling
    :equations [[:x [:vector [:map [:keyword :k] [:integer 1]] [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :expected true}
   {:name :unknown-sibling-blocks-equation :equations [[:x [:vector [:ref :x] [:ref :outside]]]]
    :bindings {:x :gravity.type/integer} :expected false :stable? true}
   {:name :never-sibling-blocks-equation :equations [[:x [:vector [:ref :x] [:ref :outside]]]]
    :bindings {:x :gravity.type/integer :outside :gravity.type/never} :expected false :stable? true}
   {:name :opaque-sibling-blocks-equation :equations [[:x [:vector [:ref :x] [:call [:ref :outside]]]]]
    :bindings {:x :gravity.type/integer :outside :gravity.type/integer} :expected false}
   {:name :odd-map-blocks-equation :equations [[:x [:map [:ref :x] [:ref :x] [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :expected false}])

(defn input
  "Materialize fixture syntax in postorder for the actual C7 test harness."
  [{:keys [equations context intrinsic-reference?] :as case}]
  (let [nodes (atom [])
        emit (fn emit [term path]
               (let [id (keyword (str "occurs-" path))
                     [kind & operands] term
                     literal? (or (contains? literal-types kind) (= :unsupported-literal kind))
                     children (if (or (= :ref kind) literal?) []
                                (mapv (fn [i operand] (emit operand (str path "-" i)))
                                      (range) operands))
                     node (if (= :ref kind)
                            {:node-id id :core-form :reference :children []
                             :attributes {:binding-id (first operands)}}
                            (if literal?
                              {:node-id id :core-form :literal :children []
                               :attributes {:literal-kind kind :value (first operands)}}
                            {:node-id id
                             :core-form (if (#{:vector :set :map} kind) :collection-literal :call)
                             :children children :attributes {:literal-kind kind}}))]
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

(def definition-alias-cases
  [{:name :definition-alias-constructor-cycle
    :equations [[:x [:ref :y]] [:y [:vector [:ref :x]]]] :definition-ids #{:x :y}
    :bindings {:x :gravity.type/integer :y :gravity.type/integer} :expected true}
   {:name :definition-branching-constructor-cycle
    :equations [[:x [:map [:ref :y] [:ref :x]]] [:y [:set [:ref :x]]]] :definition-ids #{:x :y}
    :bindings {:x :gravity.type/integer :y :gravity.type/integer} :expected true}
   {:name :lexical-constructor-through-definition-alias
    :equations [[:x [:vector [:ref :y]]] [:y [:ref :x]]] :definition-ids #{:y}
    :bindings {:x :gravity.type/integer :y :gravity.type/integer} :expected true}
   {:name :definition-constructor-through-lexical-alias
    :equations [[:x [:ref :y]] [:y [:set [:ref :x]]]] :definition-ids #{:y}
    :bindings {:x :gravity.type/integer :y :gravity.type/integer} :expected true}
   {:name :definition-alias-fixed-point
    :equations [[:x [:ref :y]] [:y [:ref :x]]] :definition-ids #{:x :y}
    :bindings {:x :gravity.type/integer :y :gravity.type/integer} :expected false :stable? true}
   {:name :definition-alias-oscillation
    :equations [[:x [:ref :y]] [:y [:ref :x]]] :definition-ids #{:x :y}
    :bindings {:x :gravity.type/integer :y :gravity.type/string} :expected false :oscillation? true}
   {:name :definition-alias-unresolved
    :equations [[:x [:ref :y]] [:y [:vector [:ref :x]]]] :definition-ids #{:x :y}
    :bindings {} :expected false :stable? true}
   {:name :definition-alias-never
    :equations [[:x [:ref :y]] [:y [:set [:ref :x]]]] :definition-ids #{:x :y}
    :bindings {:x :gravity.type/never :y :gravity.type/never} :expected false :stable? true}
   {:name :definition-alias-acyclic
    :equations [[:x [:ref :y]] [:y [:vector [:ref :outside]]]] :definition-ids #{:x :y}
    :bindings {:x :gravity.type/integer :y :gravity.type/integer :outside :gravity.type/integer}
    :expected false :stable? true}
   {:name :definition-alias-opaque
    :equations [[:x [:ref :y]] [:y [:call [:ref :x]]]] :definition-ids #{:x :y}
    :bindings {:x :gravity.type/integer :y :gravity.type/integer} :expected false}
   {:name :definition-alias-parameter-producer
    :equations [[:x [:ref :y]] [:y [:vector [:ref :x]]]] :definition-ids #{:x :y}
    :bindings {:x :gravity.type/integer :y :gravity.type/integer}
    :functions {:f {:parameter-binding-ids [:x]}} :expected false}
   {:name :definition-alias-intrinsic
    :equations [[:x [:ref :y]] [:y [:vector [:ref :x]]]] :definition-ids #{:x :y}
    :bindings {:x :gravity.type/integer :y :gravity.type/integer}
    :intrinsic-reference? true :expected false}
   {:name :definition-alias-loop-transfer
    :equations [[:x [:ref :y]] [:y [:vector [:ref :x]]]] :definition-ids #{:x :y}
    :bindings {:x :gravity.type/integer :y :gravity.type/integer}
    :context {:loop-core {}} :expected false}])

(defn definition-input
  "Build matched def producers around the existing internal-DATA fixture."
  [{:keys [definition-ids] :as case}]
  (let [base (input case)
        records (filterv #(contains? definition-ids (:binding-id %)) (:lexical base))
        definitions (mapv (fn [record]
                            {:binding-id (:binding-id record)
                             :value-node-id (:initializer-node-id record)
                             :core-node-id (keyword (str "def-" (name (:initializer-node-id record))))
                             :name (symbol (name (:binding-id record)))}) records)
        wrappers (mapv (fn [d] {:node-id (:core-node-id d) :core-form :def
                                :children [(:value-node-id d)] :resolved-binding-ids [(:binding-id d)]
                                :attributes {:name (:name d)}}) definitions)]
    (assoc base :nodes (into (:nodes base) wrappers)
           :lexical (filterv #(not (contains? definition-ids (:binding-id %))) (:lexical base))
           :definitions (into (vec (:definitions base)) definitions))))

(defn- term-edges [term bindings]
  (case (first term)
    :ref (when (and (= 2 (count term)) (contains? bindings (second term))
                    (not (#{:gravity.type/unknown :gravity.type/never} (get bindings (second term)))))
           [{:dependency (second term) :constructor? false}])
    (:vector :set :map)
    (when (or (not= :map (first term)) (even? (count (rest term))))
      (let [members (mapv #(term-edges % bindings) (rest term))]
        (when (every? some? members)
          (mapv #(assoc % :constructor? true) (mapcat identity members)))))
    (when (fixed-term? term) [])))

(defn model-occurs?
  "Independent graph oracle: compute transitive reachability, then ask whether
  a constructor edge can return to its source. No Gravity code is evaluated."
  [{:keys [equations bindings context definitions functions intrinsic-reference?]}]
  (let [ids (mapv first equations)
        blocked (concat (map :binding-id definitions)
                        (mapcat :parameter-binding-ids (vals functions)))]
    (if (or (seq (:boolean-matches context)) (map? (:loop-core context))
            intrinsic-reference? (not= (count ids) (count (set ids)))
            (some (set ids) blocked))
      false
      (let [graph (into {} (keep (fn [[id term]]
                                  (when (and (contains? bindings id)
                                             (not (#{:gravity.type/unknown :gravity.type/never} (get bindings id))))
                                    (when-let [edges (term-edges term bindings)] [id edges]))) equations))
            reachable (loop [paths (into {} (map (fn [[id edges]] [id (set (map :dependency edges))]) graph))]
                        (let [expanded (into {} (map (fn [[id targets]]
                                                      [id (into targets (mapcat #(get paths %) targets))]) paths))]
                          (if (= paths expanded) paths (recur expanded))))]
        (boolean (some (fn [[id edges]]
                         (some #(and (:constructor? %)
                                     (or (= id (:dependency %))
                                         (contains? (get reachable (:dependency %)) id))) edges)) graph))))))

(defn- substitute [term bindings]
  (if (contains? literal-types (first term))
    (if (fixed-term? term) (get literal-types (first term)) :gravity.type/unknown)
  (case (first term)
    :ref (get bindings (second term) :gravity.type/unknown)
    (:vector :set) (let [values (mapv #(substitute % bindings) (rest term))]
              (cond
                (some #{:gravity.type/never} values) :gravity.type/never
                (some #{:gravity.type/unknown} values) :gravity.type/unknown
                :else {:kind (if (= :set (first term)) :gravity.type/set :gravity.type/vector)
                       :element-types values :shape :literal}))
    :map (let [values (mapv #(substitute % bindings) (rest term))]
           (cond
             (odd? (count values)) :gravity.type/unknown
             (some #{:gravity.type/never} values) :gravity.type/never
             (some #{:gravity.type/unknown} values) :gravity.type/unknown
             :else {:kind :gravity.type/map :member-types values :shape :literal}))
    :gravity.type/unknown)))

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

(deftest single-keyword-map-model-and-substitution-controls
  (doseq [case map-cases]
    (testing (name (:name case))
      (is (= (:expected case) (model-occurs? case)))
      (when (:expected case)
        (let [states (take 9 (iterate #(step case %) (:bindings case)))]
          (is (= 9 (count (set states))))))))
  (doseq [case (filter #(#{:map-acyclic :map-unresolved :map-never} (:name %)) map-cases)]
    (let [once (step case (:bindings case))]
      (is (= once (step case once)))))
  (let [case (first map-cases)]
    (is (= {:kind :gravity.type/map
            :member-types [:gravity.type/keyword :gravity.type/integer] :shape :literal}
           (get (step case (:bindings case)) :x)))))

(deftest keyword-prefix-vector-model-and-substitution-controls
  (doseq [case prefix-vector-cases]
    (testing (name (:name case))
      (is (= (:expected case) (model-occurs? case)))
      (when (:expected case)
        (let [states (take 9 (iterate #(step case %) (:bindings case)))]
          (is (= 9 (count (set states))))))))
  (doseq [case (filter #(#{:prefix-vector-acyclic :prefix-vector-unresolved :prefix-vector-never} (:name %)) prefix-vector-cases)]
    (let [once (step case (:bindings case))]
      (is (= once (step case once)))))
  (let [case (first prefix-vector-cases)]
    (is (= {:kind :gravity.type/vector
            :element-types [:gravity.type/keyword :gravity.type/integer] :shape :literal}
           (get (step case (:bindings case)) :x)))))

(deftest fixed-member-extraction-and-substitution-controls
  ;; This changed-boundary selection covers conserved dispositions as well as
  ;; newly supported member positions. It does not rerun the old test vars.
  (doseq [case (concat cases set-cases map-cases prefix-vector-cases fixed-member-cases)]
    (testing (name (:name case))
      (is (= (:expected case) (model-occurs? case)))
      (when (:expected case)
        (let [states (take 9 (iterate #(step case %) (:bindings case)))]
          (is (= 9 (count (set states))))))
      (when (:stable? case)
        (let [once (step case (:bindings case))]
          (is (= once (step case once)))))))
  (doseq [width [0 1 2 17 96] position (distinct [0 width])]
    (let [members (mapv #(vector :keyword (keyword (str "k" %))) (range width))
          term (into [:vector] (concat (take position members) [[:ref :x]] (drop position members)))
          case {:equations [[:x term]] :bindings {:x :gravity.type/integer}}]
      (is (true? (model-occurs? case))))))

(deftest multiple-dependency-graph-and-substitution-controls
  (doseq [case (concat cases set-cases map-cases prefix-vector-cases fixed-member-cases multiple-dependency-cases)]
    (testing (name (:name case))
      (is (= (:expected case) (model-occurs? case)))
      (when (:expected case)
        (is (= 7 (count (set (take 7 (iterate #(step case %) (:bindings case))))))))
      (when (:stable? case)
        (let [settled (nth (iterate #(step case %) (:bindings case)) (inc (count (:equations case))))]
          (is (= settled (step case settled)))))))
  (let [case (first multiple-dependency-cases)
        result (get (step case (:bindings case)) :x)]
    (is (= {:kind :gravity.type/vector :element-types [:gravity.type/integer :gravity.type/integer]
            :shape :literal} result)))
  ;; Alias oscillation is a repeated state without constructor growth; a
  ;; constructor tail into that SCC still does not make its aliases infinite.
  (let [case (first (filter #(= :alias-oscillation (:name %)) cases))
        seed (:bindings case)]
    (is (false? (model-occurs? case)))
    (is (not= seed (step case seed)))
    (is (= seed (step case (step case seed))))))

(deftest definition-alias-equations-have-independent-growth-and-nongrowth-controls
  (doseq [case definition-alias-cases]
    (testing (name (:name case))
      (is (= (:expected case) (model-occurs? case)))
      (when (:expected case)
        (is (= 7 (count (set (take 7 (iterate #(step case %) (:bindings case))))))))
      (when (:stable? case)
        (let [settled (nth (iterate #(step case %) (:bindings case)) 4)]
          (is (= settled (step case settled)))))
      (when (:oscillation? case)
        (let [seed (:bindings case)]
          (is (not= seed (step case seed)))
          (is (= seed (step case (step case seed))))))))
  (let [case (first definition-alias-cases)
        built (definition-input case)
        ;; The prior lexical-only witness has no roots in this example.
        lexical-only (assoc case :equations [] :definitions (:definitions built))]
    (is (empty? (:lexical built)))
    (is (= 2 (count (:definitions built))))
    (is (false? (model-occurs? lexical-only)))
    (is (true? (model-occurs? case)))))

(deftest alias-seed-authority-is-not-determined-by-a-type-map
  ;; A finite countermodel, not a proposed Gravity SCC join rule. Both values
  ;; remain in the observations; no representative or recovery type is chosen.
  (let [equations [[:x [:ref :y]] [:y [:ref :x]]]
        observed {:x :gravity.type/integer :y :gravity.type/string}
        case {:equations equations :bindings observed}
        candidates [:gravity.type/integer :gravity.type/string]
        solutions (fn [required]
                    (set (for [type candidates
                               :let [assignment {:x type :y type}]
                               :when (every? (fn [[id expected]] (= expected (get assignment id))) required)]
                           assignment)))]
    (is (not= observed (step case observed)))
    (is (= observed (step case (step case observed))))
    (is (false? (model-occurs? case)))
    (is (= #{{:x :gravity.type/integer :y :gravity.type/integer}
             {:x :gravity.type/string :y :gravity.type/string}}
           (solutions {})))
    (is (empty? (solutions observed)))
    (is (= #{{:x :gravity.type/integer :y :gravity.type/integer}}
           (solutions {:x :gravity.type/integer :y :gravity.type/integer})))
    (is (= #{{:x :gravity.type/integer :y :gravity.type/integer}}
           (solutions {:x :gravity.type/integer})))
    (is (= observed (:bindings case))))
  (let [stable {:equations [[:x [:ref :y]] [:y [:ref :x]]]
                :bindings {:x :gravity.type/integer :y :gravity.type/integer}}
        unresolved (assoc stable :bindings {})]
    (is (= (:bindings stable) (step stable (:bindings stable))))
    (is (= {:x :gravity.type/unknown :y :gravity.type/unknown} (step unresolved {})))
    (is (false? (model-occurs? unresolved))))
  (let [partial {:equations [[:x [:ref :y]] [:y [:ref :x]]]
                 :bindings {:x :gravity.type/integer :y :gravity.type/unknown}}
        seed (:bindings partial)]
    (is (not= seed (step partial seed)))
    (is (= seed (step partial (step partial seed))))
    (is (false? (model-occurs? partial))))
  (doseq [case [(first (filter #(= :mutual-growth (:name %)) cases))
               (first (filter #(= :duplicate-binding (:name %)) cases))
               (first (filter #(= :opaque-call (:name %)) cases))]]
    (is (= (:expected case) (model-occurs? case))))
  (let [callback {:equations [[:x [:call [:ref :callback] [:ref :y]]] [:y [:ref :x]]]
                  :bindings {:x :gravity.type/integer :y :gravity.type/string
                             :callback {:function-id :external :parameters [] :return :gravity.type/integer}}}]
    ;; A callback/call result is not an alias equation merely because the call
    ;; syntax mentions a member of the component. No call semantics are run.
    (is (false? (model-occurs? callback)))))

(defn alias-component-model
  "Compiler-free SCC oracle using mutual transitive reachability. Seed values
  cannot create an alias edge or supply source authority."
  [{:keys [equations definitions functions intrinsic-reference? context]}]
  (let [ids (mapv first equations) owned (set ids)
        blocked (set (concat (map :binding-id definitions)
                             (mapcat :parameter-binding-ids (vals functions))))]
    (if (or (not= (count ids) (count owned)) (some blocked ids) intrinsic-reference?
            (seq (:boolean-matches context)) (map? (:loop-core context))) #{}
      (let [graph (into {} (for [[id term] equations
                                :when (and (= :ref (first term)) (= 2 (count term)) (owned (second term)))]
                            [id #{(second term)}]))
            paths (loop [paths graph]
                    (let [next (into {} (for [[id targets] paths]
                                         [id (into targets (mapcat paths targets))]))]
                      (if (= paths next) paths (recur next))))]
        (set (for [id ids :when ((get paths id #{}) id)]
               (set (filter #(and ((get paths id #{}) %) ((get paths % #{}) id)) ids))))))))

(deftest source-alias-components-do-not-depend-on-provisional-seed-values
  (let [equations [[:x [:ref :y]] [:y [:ref :x]]]
        component #{#{:x :y}}]
    (doseq [bindings [{} {:x :gravity.type/integer :y :gravity.type/string}
                      {:x :gravity.type/integer :y :gravity.type/integer}
                      {:x :gravity.type/integer :y :gravity.type/unknown}
                      {:x :gravity.type/never :y :gravity.type/never}]]
      (is (= component (alias-component-model {:equations equations :bindings bindings}))))
    (is (= component (alias-component-model {:equations (into [[:tail [:ref :x]]] equations)})))
    (is (= #{#{:x :y} #{:z}} (alias-component-model {:equations (conj equations [:z [:ref :z]])})))
    (doseq [addition [{:definitions [{:binding-id :x}]} {:functions {:f {:parameter-binding-ids [:x]}}}
                     {:intrinsic-reference? true} {:context {:loop-core {}}} {:context {:boolean-matches [{}]}}]]
      (is (= #{} (alias-component-model (merge {:equations equations} addition)))))
    (doseq [equations [[[:x [:ref :y]] [:y [:vector [:ref :x]]]]
                       [[:x [:ref :y]] [:y [:call [:ref :callback] [:ref :x]]]]
                       [[:x [:ref :y]] [:x [:ref :y]] [:y [:ref :x]]]
                       [[:x [:ref :outside]]]]]
      (is (= #{} (alias-component-model {:equations equations}))))))

(defn alias-declaration-conjunction-model
  "Finite constraint-satisfaction oracle. Fixture source identities are DATA,
  not authentication. No candidate type is assigned to a source binding."
  [{:keys [sources requirements observations] :as case}]
  (let [atomic #{:gravity.type/i8 :gravity.type/string :gravity.type/bool}
        evidence (mapv (fn [r]
                         (let [matches (filter #(= (:source-id r) (:id %)) sources)]
                           (when (and (= 1 (count matches))
                                      (= (select-keys r [:binding-id :type :producer :relation])
                                         (select-keys (first matches) [:binding-id :type :producer :relation]))
                                      (= :parameter-declaration (:producer r))
                                      (= :declared-parameter-compatibility (:relation r)))
                             (first matches)))) requirements)
        checked (filter #(atomic (:type %)) evidence)
        solutions (set (filter (fn [candidate] (every? #(= candidate (:type %)) checked)) atomic))
        status (cond
                 (empty? (alias-component-model case)) :unsupported-component
                 (some nil? evidence) :invalid-source-premise
                 (and (seq checked) (empty? solutions)) :conflicting-scalar-declarations
                 (< (count checked) (count requirements)) :unsupported
                 (empty? checked) :unconstrained
                 :else :compatible-scalar-declarations)]
    {:status status :requirements requirements :observations observations
     :normalization :unresolved :inferred-type :gravity.type/unknown}))

(deftest mandatory-alias-constraints-are-conjoined-without-selecting-a-seed
  (let [source (fn [id binding type]
                 {:id id :binding-id binding :type type :producer :parameter-declaration
                  :relation :declared-parameter-compatibility})
        requirement (fn [s] (assoc (dissoc s :id) :source-id (:id s)))
        x (source :sx :x :gravity.type/i8) y (source :sy :y :gravity.type/i8)
        base {:equations [[:tail [:ref :x]] [:x [:ref :y]] [:y [:ref :x]]]
              :sources [x y] :requirements (mapv requirement [x y])
              :observations {:x :gravity.type/i8 :y :gravity.type/string}}
        check (fn [case status]
                (let [r (alias-declaration-conjunction-model case)]
                  (is (= status (:status r)))
                  (is (= (:requirements case) (:requirements r)))
                  (is (= (:observations case) (:observations r)))
                  (is (= :unresolved (:normalization r)))))]
    (check base :compatible-scalar-declarations)
    (check (assoc base :requirements [] :sources []) :unconstrained)
    (let [other (source :sy :y :gravity.type/string)
          conflict (assoc base :sources [x other] :requirements (mapv requirement [x other]))]
      (check conflict :conflicting-scalar-declarations)
      (check (update conflict :requirements #(vec (reverse %))) :conflicting-scalar-declarations)
      (check (assoc conflict :observations {:x :gravity.type/string :y :gravity.type/i8}) :conflicting-scalar-declarations)
      (let [pending (source :st :tail :gravity.type/map)]
        (check (assoc conflict :sources [x other pending] :requirements (mapv requirement [x other pending]))
               :conflicting-scalar-declarations)))
    (let [tail (source :st :tail :gravity.type/string)]
      (check (assoc base :sources [x y tail] :requirements (mapv requirement [x y tail]))
             :conflicting-scalar-declarations))
    (let [bare (source :sx :x :gravity.type/map)
          refined (source :sy :y {:kind :gravity.type/map :member-types [] :shape :literal})]
      ;; Raw inequality is not a Map boundary compatibility proof or conflict.
      (check (assoc base :sources [bare refined] :requirements (mapv requirement [bare refined])) :unsupported))
    (doseq [changed [(assoc-in base [:requirements 0 :type] :gravity.type/string)
                     (assoc-in base [:requirements 0 :source-id] :missing)
                     (update base :sources conj x)
                     (assoc-in base [:requirements 0 :producer] :caller-asserted-authority)
                     (assoc-in base [:requirements 0 :relation] :all-seeds-are-equality)]]
      (check changed :invalid-source-premise))
    (doseq [equations [[[:x [:ref :y]] [:y [:vector [:ref :x]]]]
                       [[:x [:call [:ref :callback] [:ref :y]]] [:y [:ref :x]]]
                       [[:x [:ref :y]] [:x [:ref :y]] [:y [:ref :x]]]]]
      (check (assoc base :equations equations) :unsupported-component))
    (check (assoc base :cached-proof {:status :accepted :type :gravity.type/string})
           :compatible-scalar-declarations)))

(defn alias-import-source-fixture []
  (let [id #(apply str (repeat 64 %))
        record {:definition-binding-id (id \5) :function-syntax-id (id \1)
                :function-core-node-id (id \2) :body-core-node-id (id \3)
                :definition-name 'imported :fixed-arity 0 :parameter-binding-ids []}
        function {:artifact :gravity/c7-function-type :function-id (id \1)
                  :function-core-node-id (id \2) :body-core-node-id (id \3)
                  :definition-name 'imported :fixed-arity 0 :parameter-binding-ids [] :parameters []
                  :return :gravity.type/i8 :status :inferred}
        imported {:status :accepted :binding-id (id \0) :definition-binding-id (id \5)
                  :producer-sh06-artifact-id (id \6) :producer-sh07-artifact-id (id \7)
                  :origin {:ordinary-module-artifact-id (id \6)} :function-type function
                  :producer-identity {:sh07-shaped-artifact-id (id \7) :function-records [record]
                                      :function-type-table [function]}}]
    {:binding-id (id \0) :type function :producer :imported-function
     :required-relation :imported-function-type :source-records {(id \0) imported}}))

(defn alias-handler-source-fixture []
  (let [id #(apply str (repeat 64 %))
        record {:core-node-id (id \1) :catch-binding-id (id \0) :handler-core-node-id (id \2)
                :catch-clause-syntax-id (id \3) :error-type-binding-id (id \4)}
        clause {:record record :handler-core-node-id (id \2) :error-type :gravity.type/i8}]
    {:binding-id (id \0) :type :gravity.type/i8 :producer :handler-binding
     :required-relation :source-handler-binding-type :source-records {(id \1) [clause]}}))

(defn alias-seed-source-join-model
  "Relational source-witness oracle; matching is not authentication or an
  admitted alias grounding fact. Fixture identifiers are not canonical IDs."
  [{:keys [binding-id type producer required-relation source-records] :as constraint}]
  (let [digest? #(boolean (and (string? %) (re-matches #"[0-9a-f]{64}" %)))
        scalar? #{:gravity.type/nil :gravity.type/bool :gravity.type/integer :gravity.type/decimal
                  :gravity.type/ratio :gravity.type/character :gravity.type/string :gravity.type/keyword
                  :gravity.type/symbol :gravity.type/i8 :gravity.type/i16 :gravity.type/i32 :gravity.type/i64
                  :gravity.type/u8 :gravity.type/u16 :gravity.type/u32 :gravity.type/u64 :gravity.type/f32 :gravity.type/f64}
        imported? (= :imported-function producer)
        relation (get {:imported-function :imported-function-type :handler-binding :source-handler-binding-type} producer)
        exported [:artifact :function-id :function-core-node-id :body-core-node-id :definition-name
                  :parameter-binding-ids :parameters :return :fixed-arity :signature :latent-effects :capabilities
                  :ownership-constraints :profile-constraints :thrown-error-effects :status]
        witnesses
        (when (and relation (= relation required-relation) (map? source-records))
          (if imported?
            (for [[key imported] source-records
                  record (get-in imported [:producer-identity :function-records])
                  function (get-in imported [:producer-identity :function-type-table])
                  :when (and (= key binding-id (:binding-id imported)) (= :accepted (:status imported))
                             (= (:definition-binding-id imported) (:definition-binding-id record))
                             (= 1 (count (filter #(= (:definition-binding-id imported) (:definition-binding-id %))
                                                (get-in imported [:producer-identity :function-records]))))
                             (= 1 (count (filter #(= (:function-syntax-id record) (:function-id %))
                                                (get-in imported [:producer-identity :function-type-table]))))
                             (= (:producer-sh06-artifact-id imported) (get-in imported [:origin :ordinary-module-artifact-id]))
                             (= (:producer-sh07-artifact-id imported) (get-in imported [:producer-identity :sh07-shaped-artifact-id]))
                             (every? digest? [key (:definition-binding-id imported) (:producer-sh06-artifact-id imported)
                                              (:producer-sh07-artifact-id imported)])
                             (= type (:function-type imported) (select-keys function exported))
                             (= :gravity/c7-function-type (:artifact function)) (= :inferred (:status function))
                             (every? digest? [(:function-syntax-id record) (:function-core-node-id record) (:body-core-node-id record)])
                             (= 3 (count (set [(:function-syntax-id record) (:function-core-node-id record) (:body-core-node-id record)])))
                             (every? digest? (:parameter-binding-ids record))
                             (= (count (:parameter-binding-ids record)) (count (set (:parameter-binding-ids record))))
                             (= (:parameter-binding-ids record) (mapv :binding-id (:parameters function)))
                             (scalar? (:return function)) (every? #(scalar? (:type %)) (:parameters function))
                             (= (:function-syntax-id record) (:function-id function))
                             (= (:function-core-node-id record) (:function-core-node-id function))
                             (= (:body-core-node-id record) (:body-core-node-id function))
                             (= (:definition-name record) (:definition-name function))
                             (= (:parameter-binding-ids record) (:parameter-binding-ids function))
                             (= (:fixed-arity record) (:fixed-arity function)
                                (count (:parameter-binding-ids record)) (count (:parameters function)))
                             (if (contains? record :signature)
                               (let [signature (:signature record) ds (:parameters signature) ps (:parameters function)]
                                 (and (map? signature) (vector? ds) (= signature (:signature function))
                                      (= (count ds) (count ps))
                                      (every? true? (map #(or (nil? %1) (= (:type-id %1) (:type %2))) ds ps))
                                      (or (nil? (:return signature)) (= (get-in signature [:return :type-id]) (:return function)))))
                               (not (contains? function :signature))))]
              [key record function])
            (when (every? vector? (vals source-records))
              (for [[owner clauses] source-records clause clauses
                    :let [record (:record clause)]
                    :when (= binding-id (:catch-binding-id record))]
                [owner clause]))))
        valid? (and (= 1 (count witnesses))
                    (or imported?
                        (let [[owner clause] (first witnesses) record (:record clause)]
                          (and (= type (:error-type clause)) (= owner (:core-node-id record))
                               (= (:handler-core-node-id clause) (:handler-core-node-id record))
                               (every? digest? [binding-id owner (:handler-core-node-id clause)
                                                (:catch-clause-syntax-id record) (:error-type-binding-id record)])))))]
    {:status (if valid? :unsupported :invalid-source-premise) :constraint constraint
     :normalization :unresolved :grounding :not-established}))

(deftest import-and-handler-seed-source-joins-do-not-confer-alias-grounding
  (doseq [fixture [(alias-import-source-fixture) (alias-handler-source-fixture)]]
    (let [r (alias-seed-source-join-model fixture)]
      (is (= :unsupported (:status r))) (is (= fixture (:constraint r)))
      (is (= :unresolved (:normalization r))) (is (= :not-established (:grounding r))))
    (doseq [changed [(assoc fixture :binding-id "missing") (assoc fixture :type :gravity.type/string)
                     (assoc fixture :source-records {}) (assoc fixture :source-records [])
                     (assoc fixture :producer :caller-authority) (assoc fixture :required-relation :trusted-seed)]]
      (is (= :invalid-source-premise (:status (alias-seed-source-join-model changed)))))
    (is (= :unsupported (:status (alias-seed-source-join-model (assoc fixture :authority :accepted))))))
  (let [fixture (alias-import-source-fixture) id (:binding-id fixture)
        unknown (-> fixture (assoc-in [:type :return] :gravity.type/unknown)
                    (assoc-in [:source-records id :function-type :return] :gravity.type/unknown)
                    (assoc-in [:source-records id :producer-identity :function-type-table 0 :return] :gravity.type/unknown))]
    (is (= :unsupported (:status (alias-seed-source-join-model
                                  (assoc-in fixture [:source-records id :producer-identity :function-type-table 0 :non-exported-note] :retained)))))
    (doseq [changed [unknown (assoc-in fixture [:source-records id :status] :pending)
                     (assoc-in fixture [:source-records id :producer-sh07-artifact-id] (apply str (repeat 64 \8)))
                     (assoc-in fixture [:source-records id :producer-sh06-artifact-id] (apply str (repeat 64 \9)))
                     (update-in fixture [:source-records id :producer-identity :function-records] #(conj % (first %)))
                     (update-in fixture [:source-records id :producer-identity :function-type-table] #(conj % (first %)))
                     (update-in fixture [:source-records id :producer-identity :function-records]
                                #(conj % (assoc (first %) :function-syntax-id (apply str (repeat 64 \8)))))
                     (update-in fixture [:source-records id :producer-identity :function-type-table]
                                #(conj % (assoc (first %) :body-core-node-id (apply str (repeat 64 \8)))))
                     (assoc-in fixture [:source-records id :producer-identity :function-type-table 0 :body-core-node-id] "wrong")]]
      (is (= :invalid-source-premise (:status (alias-seed-source-join-model changed))))))
  (let [fixture (alias-handler-source-fixture) owner (first (keys (:source-records fixture)))]
    (doseq [changed [(update-in fixture [:source-records owner] #(conj % (first %)))
                     (assoc-in fixture [:source-records (apply str (repeat 64 \8))]
                               [(assoc-in (first (get-in fixture [:source-records owner])) [:record :core-node-id]
                                          (apply str (repeat 64 \8)))])
                     (assoc-in fixture [:source-records owner 0 :error-type] :gravity.type/string)
                     (assoc-in fixture [:source-records owner 0 :record :core-node-id] "wrong")
                     (assoc-in fixture [:source-records owner 0 :handler-core-node-id] "wrong")
                     (assoc-in fixture [:source-records owner 0 :record :catch-clause-syntax-id] "wrong")]]
      (is (= :invalid-source-premise (:status (alias-seed-source-join-model changed)))))))

(defn atomic-handler-constraint [type digit]
  (let [fixture (alias-handler-source-fixture)
        owner (first (keys (:source-records fixture))) binding (apply str (repeat 64 digit))]
    (-> fixture (assoc :binding-id binding :type type)
        (assoc-in [:source-records owner 0 :record :catch-binding-id] binding)
        (assoc-in [:source-records owner 0 :error-type] type))))

(defn handler-scalar-conjunction-model
  "Enumerate the atomic catalog's simultaneous solutions, rather than copying
  the Gravity checker's adjacent-pair mismatch walk. No solution is assigned."
  [{:keys [source-seed-conjunction] :as premise}]
  (let [catalog (zipmap '[I8 I16 I32 I64 U8 U16 U32 U64 F32 F64 Bool String Symbol Keyword Ratio]
                       [:gravity.type/i8 :gravity.type/i16 :gravity.type/i32 :gravity.type/i64
                        :gravity.type/u8 :gravity.type/u16 :gravity.type/u32 :gravity.type/u64
                        :gravity.type/f32 :gravity.type/f64 :gravity.type/bool :gravity.type/string
                        :gravity.type/symbol :gravity.type/keyword :gravity.type/ratio])
        atomic (set (vals catalog))
        checks
        (mapv (fn [{:keys [binding-id type producer required-relation source-records] :as c}]
                (cond
                  (= producer :handler-binding)
                  (let [join (alias-seed-source-join-model c)]
                    (if (= :invalid-source-premise (:status join)) :invalid
                      (if (atomic type) :scalar :unsupported)))
                  (= producer :parameter-declaration)
                  (let [ds (for [record source-records
                                 [id d] (map vector (:parameter-binding-ids record) (get-in record [:signature :parameters]))
                                 :when (= binding-id id)] d)
                        d (first ds)]
                    (cond
                      (not (and (= required-relation :declared-parameter-compatibility)
                                (= 1 (count ds)) (= type (:type-id d)))) :invalid
                      (= type (get catalog (:type-symbol d) ::missing)) :scalar
                      :else :unsupported))
                  (and (= producer :imported-function) (= required-relation :imported-function-type))
                  (if (= :invalid-source-premise (:status (alias-seed-source-join-model c))) :invalid :unsupported)
                  (and (= producer :recursive-or-higher-order-premise) (= required-relation :producer-premise-specific)) :unsupported
                  :else :invalid)) source-seed-conjunction)
        scalar (map first (filter #(= :scalar (second %)) (map vector source-seed-conjunction checks)))
        solutions (filter (fn [candidate] (every? #(= candidate (:type %)) scalar)) atomic)
        status (cond
                 (some #{:invalid} checks) :invalid-source-premise
                 (and (seq scalar) (empty? solutions)) :conflicting-scalar-declarations
                 (some #{:unsupported} checks) :unsupported
                 (empty? checks) :unconstrained
                 :else :compatible-scalar-declarations)]
    (assoc premise :status status :scalar-conflict? (boolean (and (seq scalar) (empty? solutions)))
           :normalization :unresolved :grounding :not-established)))

(deftest atomic-handler-and-declaration-obligations-share-a-conjunction-not-a-ground
  (let [a (atomic-handler-constraint :gravity.type/i8 \0)
        b (atomic-handler-constraint :gravity.type/i8 \5)
        declaration {:binding-id :p :type :gravity.type/i8 :producer :parameter-declaration
                     :required-relation :declared-parameter-compatibility
                     :source-records [{:parameter-binding-ids [:p]
                                       :signature {:parameters [{:type-symbol 'I8 :type-id :gravity.type/i8}]}}]}
        observations {(:binding-id a) :gravity.type/string (:binding-id b) :gravity.type/bool}
        check (fn [constraints expected]
                (let [premise {:source-seed-conjunction constraints :provisional-observations observations}
                      r (handler-scalar-conjunction-model premise)]
                  (is (= expected (:status r)))
                  (is (= constraints (:source-seed-conjunction r)))
                  (is (= observations (:provisional-observations r)))
                  (is (= :unresolved (:normalization r))) (is (= :not-established (:grounding r))) r))]
    (check [a b declaration] :compatible-scalar-declarations)
    (check [declaration b a] :compatible-scalar-declarations)
    (let [different (atomic-handler-constraint :gravity.type/string \5)]
      (check [a different] :conflicting-scalar-declarations)
      (check [different a] :conflicting-scalar-declarations)
      (check [declaration different] :conflicting-scalar-declarations)
      (let [r (check [a (alias-import-source-fixture) different] :conflicting-scalar-declarations)]
        (is (:scalar-conflict? r)))
      (let [r (check [a different (assoc a :required-relation :trusted)] :invalid-source-premise)]
        (is (:scalar-conflict? r))))
    (doseq [type [:gravity.type/i8 :gravity.type/i16 :gravity.type/i32 :gravity.type/i64
                  :gravity.type/u8 :gravity.type/u16 :gravity.type/u32 :gravity.type/u64
                  :gravity.type/f32 :gravity.type/f64 :gravity.type/bool :gravity.type/string
                  :gravity.type/symbol :gravity.type/keyword :gravity.type/ratio]]
      (check [(atomic-handler-constraint type \0) (atomic-handler-constraint type \5)] :compatible-scalar-declarations))
    ;; Coherent non-catalog records do not gain compatibility from raw equality.
    (doseq [type [:gravity.type/unknown :gravity.type/never :gravity.type/nil :gravity.type/integer
                  :gravity.type/map :gravity.type/list {:kind :gravity.type/vector :member-types []}]]
      (check [(atomic-handler-constraint type \0)] :unsupported))
    (check [a (assoc a :authority :accepted)] :compatible-scalar-declarations)
    (check [a (assoc b :role :incoming-alias)] :compatible-scalar-declarations)
    (check [a (alias-import-source-fixture)] :unsupported)
    (check [a (assoc b :type :gravity.type/string)] :invalid-source-premise)))

(defn closed-alias-grounding-model
  "Actual-shape internal DATA only. Transitive closure distinguishes reachable
  literal initializers from cycles; it does not execute or normalize Gravity."
  [{:keys [nodes lexical-bindings parameter-binding-ids] :as input}]
  (let [ids (mapv :binding-id lexical-bindings) index (into {} (map (juxt :node-id identity) nodes))
        unique? (and (= (count ids) (count (set ids)))
                     (= (count nodes) (count index))
                     (not-any? (set parameter-binding-ids) ids)
                     (every? #(contains? index (:initializer-node-id %)) lexical-bindings))]
    (if-not unique? {:status :invalid-source-census :input input}
      (let [bindings (set ids)
            initializers (into {} (map (fn [r] [(:binding-id r) (index (:initializer-node-id r))]) lexical-bindings))
            edges (into {} (for [[binding node] initializers
                                 :let [dependency (get-in node [:attributes :binding-id])]
                                 :when (and (= :reference (:core-form node)) (= [] (:children node))
                                            (bindings dependency))] [binding dependency]))
            roots (set (for [[binding node] initializers
                             :when (and (= :literal (:core-form node)) (= [] (:children node))
                                        (= :integer (get-in node [:attributes :literal-kind]))
                                        (integer? (get-in node [:attributes :value])))] binding))
            reach (loop [relation (set (map vec edges))]
                    (let [next (into relation (for [[a b] relation [c d] relation :when (= b c)] [a d]))]
                      (if (= relation next) relation (recur next))))
            components (set (for [a ids :when (reach [a a])]
                              (set (filter #(and (reach [a %]) (reach [% a])) ids))))
            grounded (set (for [a ids :when (some #(or (= a %) (reach [a %])) roots)] a))]
        {:status :classified :input input :edges edges :literal-roots roots
         :components components :grounded-binding-ids grounded
         :component-grounding (into {} (map (fn [c] [c (set (filter grounded c))]) components))}))))

(defn actual-alias-grounding-data [dependencies]
  (let [bindings (mapv #(keyword (str "binding-" %)) (range (count dependencies)))
        node-ids (mapv #(keyword (str "initializer-" %)) (range (count dependencies)))]
    {:nodes (mapv (fn [node-id dependency]
                    (if (= :literal dependency)
                      {:node-id node-id :core-form :literal :children [] :attributes {:literal-kind :integer :value 1}}
                      {:node-id node-id :core-form :reference :children [] :attributes {:binding-id (get bindings dependency)}}))
                  node-ids dependencies)
     :lexical-bindings (mapv (fn [binding node-id] {:binding-id binding :initializer-node-id node-id}) bindings node-ids)}))

(deftest closed-unique-alias-cycles-cannot-also-have-a-literal-initializer-anchor
  ;; Enumerate every functional initializer graph on one through four bindings,
  ;; where each initializer is either one ordinary reference or a literal.
  (doseq [size (range 1 5)
          dependencies (reduce (fn [prefixes _] (for [p prefixes d (conj (vec (range size)) :literal)] (conj p d)))
                               [[]] (range size))]
    (let [data (actual-alias-grounding-data dependencies) r (closed-alias-grounding-model data)]
      (is (= :classified (:status r)))
      (is (every? empty? (vals (:component-grounding r))))
      (is (every? (fn [c] (every? #(c (get (:edges r) %)) c)) (:components r)))))
  (let [cycle (actual-alias-grounding-data [1 0])
        tail (actual-alias-grounding-data [1 0 0])
        anchored (actual-alias-grounding-data [1 :literal])
        anchor-plus-cycle (actual-alias-grounding-data [1 0 :literal])
        root-id :binding-0]
    (is (= #{#{:binding-0 :binding-1}} (:components (closed-alias-grounding-model cycle))))
    (is (= #{} (:grounded-binding-ids (closed-alias-grounding-model tail))))
    (is (= #{:binding-0 :binding-1} (:grounded-binding-ids (closed-alias-grounding-model anchored))))
    (is (= #{} (:components (closed-alias-grounding-model anchored))))
    (is (= #{:binding-2} (:grounded-binding-ids (closed-alias-grounding-model anchor-plus-cycle))))
    ;; Extra source obligations or compatible prior observations do not change
    ;; an initializer, nor establish value existence for a circular equation.
    (let [extra (assoc cycle :observations {:binding-0 :gravity.type/i8 :binding-1 :gravity.type/string}
                      :mandatory-contributions [(atomic-handler-constraint :gravity.type/i8 \0)]
                      :grounding :caller-approved)
          r (closed-alias-grounding-model extra)]
      (is (= extra (:input r)))
      (is (= (:components (closed-alias-grounding-model cycle)) (:components r)))
      (is (= #{} (:grounded-binding-ids r))))
    ;; A second producer is not a way to preserve the cycle and graft a root.
    (let [second-producer (-> cycle
                              (update :nodes conj {:node-id :literal :core-form :literal :children []
                                                   :attributes {:literal-kind :integer :value 1}})
                              (update :lexical-bindings conj {:binding-id root-id :initializer-node-id :literal}))]
      (is (= :invalid-source-census (:status (closed-alias-grounding-model second-producer)))))
    (doseq [bad [(update cycle :nodes conj (first (:nodes cycle)))
                 (assoc cycle :parameter-binding-ids [root-id])
                 (assoc-in cycle [:lexical-bindings 0 :initializer-node-id] :missing)]]
      (is (= :invalid-source-census (:status (closed-alias-grounding-model bad)))))))
