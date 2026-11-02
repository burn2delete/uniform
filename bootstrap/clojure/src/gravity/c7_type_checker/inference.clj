(ns gravity.c7-type-checker.inference
  "Local deterministic inference and core type products for hosted Stage0 C7.")

(defn literal-type
  [value]
  (cond
    (nil? value) "Nil"
    (true? value) "Boolean"
    (false? value) "Boolean"
    (integer? value) "I64"
    (float? value) "F64"
    (string? value) "String"
    (keyword? value) "Keyword"
    (symbol? value) "Symbol"
    (vector? value) "Vector[Dynamic]"
    (map? value) "Map[Keyword, Dynamic]"
    (set? value) "Set[Dynamic]"
    (seq? value) "List[Dynamic]"
    :else "Dynamic"))

(defn node-operator
  [node]
  (get-in node [:children :operator]))

(defn node-type
  [literal-type node-operator node]
  (let [operator (node-operator node)]
    (case (:form node)
      :literal (literal-type (:value node))
      quote "Syntax"
      :symbol "BindingRef"
      def "Var"
      fn "Fn[Dynamic]->Dynamic"
      let "Dynamic"
      do "Dynamic"
      if "Dynamic"
      match "Dynamic"
      try "Dynamic"
      throw "Never"
      loop "Dynamic"
      recur "Never"
      var "VarRef"
      set! "Unit"
      :declared-primitive (if (:unsafe-metadata node)
                            "UnsafeIsland[Dynamic]"
                            "PrimitiveResult")
      :call (case operator
              dynamic/value "Dynamic"
              dynamic/cast "CheckedCast[String]"
              generic/id "Generic[T]"
              protocol/value "ProtocolValue"
              schema/derive "SchemaDerived"
              schema/validate "Validated[Schema]"
              "Dynamic")
      "Dynamic")))

(defn type-fact
  [node-type node]
  {:artifact :gravity/c7-type-fact
   :fact-id (str "c7-type-" (:node-id node))
   :core-node (:node-id node)
   :source (:source node)
   :type (node-type node)
   :type-source :local-deterministic-inference
   :profile (:profile node)
   :target (:target node)
   :effects (:effects node)
   :capabilities (:capabilities node)
   :ownership {:mode :borrowed :resource :nonlinear}
   :layout {:representation (case (:profile node)
                              :hosted :managed-object
                              :native :layout-required
                              :kernel :explicit-layout-required
                              :firmware :fixed-layout-required
                              :hardware :synthesizable-layout-required
                              :abstract)
            :status :recorded}
   :diagnostics []})

(defn type-environment
  [type-facts]
  {:artifact :gravity/c7-type-environment
   :types (into (sorted-map)
                (map (fn [fact] [(:core-node fact) (:type fact)])
                     type-facts))
   :locals (into (sorted-map)
                 (keep (fn [fact]
                         (when (= "BindingRef" (:type fact))
                           [(:core-node fact)
                            {:type (:type fact)
                             :mutability :immutable
                             :ownership :borrowed}]))
                       type-facts))
   :status :complete})

(defn constraint-ledger
  [type-facts]
  {:artifact :gravity/c7-constraint-ledger
   :constraints
   (mapv (fn [idx fact]
           {:constraint-id (str "c7-constraint-" idx)
            :kind :type-assignment
            :source-node (:core-node fact)
            :producer-rule :local-inference
            :dependencies [(:core-node fact)]
            :solution (:type fact)
            :invalidation [:core-node :binding-table :profile-contract]
            :status :solved})
         (range)
         type-facts)
   :status :solved})

(defn function-table
  [nodes]
  {:artifact :gravity/c7-function-type-table
   :functions
   (mapv (fn [node]
           {:fn-id (:node-id node)
            :params (vec (repeat (count (get-in node [:children :params]))
                                 "Dynamic"))
            :return "Dynamic"
            :latent-effects (:effects node)
            :capabilities (:capabilities node)
            :ownership-constraints [:borrowed-captures-preserved]
            :profile-constraints [(:profile node)]
            :throws #{"String"}
            :source (:source node)
            :status :typed})
         (filter #(= 'fn (:form %)) nodes))
   :status :complete})
