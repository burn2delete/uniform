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
  (loop [index 0]
    (when (< index (count parameters))
      (if (= ':- (get parameters index))
        (do (let [type (get parameters (inc index))
                  type-path (conj path parameter-index (inc index))]
              (if (symbol? type)
                (walk syntax type type-path nested :type)
                (when (and (seq? type) (= 2 (count type)) (every? symbol? type))
                  (remember-scope! syntax type-path type nested)
                  (doseq [[child item] (map-indexed vector type)]
                    (walk syntax item (conj type-path child) nested :type)))))
            (recur (+ index 2)))
        (recur (inc index)))))
  (let [header-index (inc parameter-index)
        annotated? (= ':- (get items header-index))]
    (when (and annotated? (symbol? (get items (inc header-index))) (not= ':- (get items (inc header-index))))
      (walk syntax (get items (inc header-index)) (conj path (inc header-index)) nested :type))
    (doseq [index (range (+ header-index (if annotated? 2 0)) (count items))]
      (walk syntax (get items index) (conj path index) nested :expression)))))
