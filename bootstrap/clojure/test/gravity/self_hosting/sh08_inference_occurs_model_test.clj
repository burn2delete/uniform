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
    :bindings {:x :gravity.type/integer :key :gravity.type/keyword} :expected false}
   {:name :map-string-key :equations [[:x [:map [:string "k"] [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :expected true}
   {:name :map-odd-arity :equations [[:x [:map [:keyword :k]]]]
    :bindings {:x :gravity.type/integer} :expected false}
   {:name :map-multiple-members
    :equations [[:x [:map [:keyword :k] [:ref :x] [:keyword :j] [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :expected false}
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
    :bindings {:x :gravity.type/integer :prefix :gravity.type/keyword} :expected false}
   {:name :prefix-vector-string-prefix :equations [[:x [:vector [:string "k"] [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :expected true}
   {:name :prefix-vector-reversed-members :equations [[:x [:vector [:ref :x] [:keyword :k]]]]
    :bindings {:x :gravity.type/integer} :expected true}
   {:name :prefix-vector-extra-member
    :equations [[:x [:vector [:keyword :k] [:ref :x] [:ref :x]]]]
    :bindings {:x :gravity.type/integer} :expected false}
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
; witnesses. Their fixtures remain; malformed and evolving-sibling cases stay
; negative. These fixture samples do not model every character carrier.
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
        :bindings {:x :gravity.type/integer :outside :gravity.type/integer} :expected false}
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

(defn- term-edge [term]
  (case (first term)
    :ref {:dependency (second term) :constructor? false}
    (:vector :set :map)
    (when (or (not= :map (first term)) (even? (count (rest term))))
      (let [dependencies (remove fixed-term? (rest term))]
        (when (= 1 (count dependencies))
          (when-let [edge (term-edge (first dependencies))]
            (assoc edge :constructor? true)))))
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
