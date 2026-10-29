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
