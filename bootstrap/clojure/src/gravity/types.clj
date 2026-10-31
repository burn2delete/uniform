(ns gravity.types
  "Seed type products and explicit current C7 runtime entry points. Semantics remain Gravity-owned.")

(def
 ^{:private true} namespace-contract
 '{:public-api
   {compile-plan
    {:arglists ([emitter source-path source-text operations]),
     :admission :none,
     :current-gravity-runtime-data-only? true,
     :operation-inputs [:compile-plan],
     :authenticates-inputs? false,
     :proves-canonical-types? false,
     :grants-authority? false},
    check-primitives
    {:arglists ([runner plan core operations]),
     :admission :none,
     :current-gravity-runtime-data-only? true,
     :operation-inputs [:execute-function],
     :authenticates-inputs? false,
     :proves-canonical-types? false,
     :grants-authority? false},
    verify-primitives
    {:arglists ([runner plan core candidate operations]),
     :admission :none,
     :current-gravity-runtime-data-only? true,
     :operation-inputs [:execute-function],
     :authenticates-inputs? false,
     :proves-canonical-types? false,
     :grants-authority? false},
    verify-function-result
    {:arglists ([runner plan request candidate operations]),
     :admission :none,
     :current-gravity-runtime-data-only? true,
     :operation-inputs [:execute-function],
     :authenticates-inputs? false,
     :proves-canonical-types? false,
     :grants-authority? false},
    literal-type
    {:arglists ([value]),
     :admission :none,
     :seed-compatibility-data-only? true,
     :authenticates-inputs? false,
     :proves-canonical-types? false,
     :grants-authority? false},
    node-operator
    {:arglists ([node]),
     :admission :none,
     :seed-compatibility-data-only? true,
     :authenticates-inputs? false,
     :proves-canonical-types? false,
     :grants-authority? false},
    node-type
    {:arglists ([literal-type node-operator node]),
     :admission :none,
     :seed-compatibility-data-only? true,
     :authenticates-inputs? false,
     :proves-canonical-types? false,
     :grants-authority? false},
    type-fact
    {:arglists ([node-type node]),
     :admission :none,
     :seed-compatibility-data-only? true,
     :authenticates-inputs? false,
     :proves-canonical-types? false,
     :grants-authority? false},
    type-environment
    {:arglists ([type-facts]),
     :admission :none,
     :seed-compatibility-data-only? true,
     :authenticates-inputs? false,
     :proves-canonical-types? false,
     :grants-authority? false},
    constraint-ledger
    {:arglists ([type-facts]),
     :admission :none,
     :seed-compatibility-data-only? true,
     :authenticates-inputs? false,
     :proves-canonical-types? false,
     :grants-authority? false},
    function-table
    {:arglists ([nodes]),
     :admission :none,
     :seed-compatibility-data-only? true,
     :authenticates-inputs? false,
     :proves-canonical-types? false,
     :grants-authority? false}},
   :dependency-direction
   {:requires [],
    :consumed-by [gravity.c7-type-checker.inference gravity.bootstrap],
    :forbids
    [gravity.bootstrap
     gravity.cli
     gravity.release
     gravity.c7-type-checker]},
   :legacy-status-labels-authoritative? false,
   :governing-contracts [D1 D2 D3 D8 D9 L5 L17 C1 C7 C15 BOOT8 TEST2],
   :unsupported
   [:native-ingress-authentication
    :callback-validation
    :complete-canonical-l5-c7-type-checking
    :integer-width-proof
    :general-constraint-solving
    :profile-legality
    :layout-legality
    :ownership-legality
    :throws-inference
    :full-source-pipeline
    :authority-admission
    :migration
    :release
    :self-hosting
    :seed-retirement],
   :artifact-inputs
   [:owner-supplied-native-hosted-core-node-views
    :explicit-type-classifier-callbacks
    :type-fact-data
    :explicit-emitter-source-path-and-text
    :compiler-plan-and-native-core-or-type-result-data
    :explicit-compiler-and-runtime-operations],
   :namespace gravity.types,
   :diagnostic-ownership
   {:stage [],
    :upstream [:core-node-source-and-origin-data],
    :existing-c7-owner
    [:typed-core-verification :type-diagnostic-decisions]
    :propagated [:compiler-and-runtime-diagnostics :gravity-c7-diagnostics]},
   :artifact-outputs
   [:seed-literal-and-node-classifications
    :node-type-fact-data
    :type-environment-data
    :ordered-assignment-constraint-data
    :function-signature-data
    :current-c7-executable-plan-data
    :unauthenticated-primitive-typed-template
    :c7-replay-report-data],
   :contract-boundary :seed-type-products-and-current-gravity-runtime})

(defn compile-plan [emitter source-path source-text operations]
  ((:compile-plan operations) emitter source-path source-text))

(defn check-primitives [runner plan core operations]
  ((:execute-function operations) runner plan 'sh08-type-core-artifact [core]))

(defn verify-primitives [runner plan core candidate operations]
  ((:execute-function operations) runner plan 'sh08-verify-type-result [core candidate]))

(defn verify-function-result [runner plan request candidate operations]
  ((:execute-function operations) runner plan 'sh08-verify-function-type-result [request candidate]))

(defn
 literal-type
 [value]
 (cond
  (nil? value)
  "Nil"
  (true? value)
  "Boolean"
  (false? value)
  "Boolean"
  (integer? value)
  "I64"
  (float? value)
  "F64"
  (string? value)
  "String"
  (keyword? value)
  "Keyword"
  (symbol? value)
  "Symbol"
  (vector? value)
  "Vector[Dynamic]"
  (map? value)
  "Map[Keyword, Dynamic]"
  (set? value)
  "Set[Dynamic]"
  (seq? value)
  "List[Dynamic]"
  :else
  "Dynamic"))

(defn node-operator [node] (get-in node [:children :operator]))

(defn
 node-type
 [literal-type node-operator node]
 (let
  [operator (node-operator node)]
  (case
   (:form node)
   :literal
   (literal-type (:value node))
   quote
   "Syntax"
   :symbol
   "BindingRef"
   def
   "Var"
   fn
   "Fn[Dynamic]->Dynamic"
   let
   "Dynamic"
   do
   "Dynamic"
   if
   "Dynamic"
   match
   "Dynamic"
   try
   "Dynamic"
   throw
   "Never"
   loop
   "Dynamic"
   recur
   "Never"
   var
   "VarRef"
   set!
   "Unit"
   :declared-primitive
   (if
    (:unsafe-metadata node)
    "UnsafeIsland[Dynamic]"
    "PrimitiveResult")
   :call
   (case
    operator
    dynamic/value
    "Dynamic"
    dynamic/cast
    "CheckedCast[String]"
    generic/id
    "Generic[T]"
    protocol/value
    "ProtocolValue"
    schema/derive
    "SchemaDerived"
    schema/validate
    "Validated[Schema]"
    "Dynamic")
   "Dynamic")))

(defn
 type-fact
 [node-type node]
 {:capabilities (:capabilities node),
  :diagnostics [],
  :layout
  {:representation
   (case
    (:profile node)
    :hosted
    :managed-object
    :native
    :layout-required
    :kernel
    :explicit-layout-required
    :firmware
    :fixed-layout-required
    :hardware
    :synthesizable-layout-required
    :abstract),
   :status :recorded},
  :core-node (:node-id node),
  :type (node-type node),
  :source (:source node),
  :effects (:effects node),
  :ownership {:mode :borrowed, :resource :nonlinear},
  :type-source :local-deterministic-inference,
  :artifact :gravity/c7-type-fact,
  :target (:target node),
  :profile (:profile node),
  :fact-id (str "c7-type-" (:node-id node))})

(defn
 type-environment
 [type-facts]
 {:artifact :gravity/c7-type-environment,
  :types
  (into
   (sorted-map)
   (map (fn [fact] [(:core-node fact) (:type fact)]) type-facts)),
  :locals
  (into
   (sorted-map)
   (keep
    (fn
     [fact]
     (when
      (= "BindingRef" (:type fact))
      [(:core-node fact)
       {:type (:type fact),
        :mutability :immutable,
        :ownership :borrowed}]))
    type-facts)),
  :status :complete})

(defn
 constraint-ledger
 [type-facts]
 {:artifact :gravity/c7-constraint-ledger,
  :constraints
  (mapv
   (fn
    [idx fact]
    {:constraint-id (str "c7-constraint-" idx),
     :kind :type-assignment,
     :source-node (:core-node fact),
     :producer-rule :local-inference,
     :dependencies [(:core-node fact)],
     :solution (:type fact),
     :invalidation [:core-node :binding-table :profile-contract],
     :status :solved})
   (range)
   type-facts),
  :status :solved})

(defn
 function-table
 [nodes]
 {:artifact :gravity/c7-function-type-table,
  :functions
  (mapv
   (fn
    [node]
    {:capabilities (:capabilities node),
     :latent-effects (:effects node),
     :ownership-constraints [:borrowed-captures-preserved],
     :return "Dynamic",
     :throws #{"String"},
     :params
     (vec
      (repeat (count (get-in node [:children :params])) "Dynamic")),
     :source (:source node),
     :status :typed,
     :profile-constraints [(:profile node)],
     :fn-id (:node-id node)})
   (filter (fn* [p1__137#] (= 'fn (:form p1__137#))) nodes)),
  :status :complete})

(doseq
 [operation
  [#'compile-plan
   #'check-primitives
   #'verify-primitives
   #'verify-function-result
   #'literal-type
   #'node-operator
   #'node-type
   #'type-fact
   #'type-environment
   #'constraint-ledger
   #'function-table]]
 (alter-meta!
  operation
  assoc
  :gravity/namespace-contract
  namespace-contract))
