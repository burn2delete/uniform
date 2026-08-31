; Semantic decomposition of committed HEAD reader line 154235.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-sh07-core-resolution-projection!-fn
 [walk
  projected-syntax-id
  remember-scope!
  consume-scope!
  add-reference!
  scope-by-syntax-id
  declaration-syntax-by-upstream-id
  source-path
  syntax
  value
  items
  operator
  path
  scope-chain
  position]
 (let
  [named?
   (symbol? (second items))
   parameter-index
   (if named? 2 1)
   parameters
   (get items parameter-index [])
   binding-paths
   (cond->
    (sh07-core-parameter-binding-paths parameters (conj path parameter-index))
    named?
    (conj {:name (second items), :path (conj path 1)}))
   scope
   (consume-scope! syntax (first scope-chain) binding-paths)
   nested
   (cons (:scope-id scope) scope-chain)]
  (swap!
   scope-by-syntax-id
   assoc
   (projected-syntax-id syntax path value)
   (:sh07/projected-scope-id scope))
  (add-reference! syntax (conj path 0) 'fn :operator scope-chain)
  (remember-scope! syntax (conj path parameter-index) parameters nested)
  (doseq
   [[binding projected] (map vector (:bindings scope) binding-paths)]
   (let
    [syntax-id (projected-syntax-id syntax (:path projected) (:name projected))]
    (swap! scope-by-syntax-id assoc syntax-id (:sh07/projected-scope-id scope))
    (swap! declaration-syntax-by-upstream-id assoc (:binding-syntax-id binding) syntax-id)))
  (doseq
   [index (range (if named? 3 2) (count items))]
   (walk syntax (get items index) (conj path index) nested :expression))))
