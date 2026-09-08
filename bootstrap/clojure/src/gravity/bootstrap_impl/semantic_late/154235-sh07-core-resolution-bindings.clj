; Semantic decomposition of committed HEAD reader line 154235.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-sh07-core-resolution-projection!-bindings
 [walk
  projected-syntax-id
  remember-scope!
  consume-scope!
  add-reference!
  scope-by-syntax-id
  declaration-syntax-by-upstream-id
  source-path
  syntax
  items
  operator
  path
  scope-chain
  position]
 (let
  [binding-vector
   (second items)
   pairs
   (if (vector? binding-vector) (partition 2 binding-vector) [])
   _
   (add-reference! syntax (conj path 0) operator :operator scope-chain)
   nested
   (loop
    [remaining (seq pairs) binding-index 0 active scope-chain]
    (if
     (empty? remaining)
     active
     (let
      [[binding-pattern initializer]
       (first remaining)
       binding-path
       (conj path 1 (* binding-index 2))
       binding-paths
       (sh06-let-loop-binding-paths binding-pattern binding-path)
       _
       (walk syntax initializer (conj path 1 (inc (* binding-index 2))) active :expression)
       next-active
       (if
        (or (symbol? binding-pattern) (vector? binding-pattern))
        (let
         [scope
          (consume-scope! syntax (first active) binding-paths)]
         (when
          (vector? binding-pattern)
          (swap!
           scope-by-syntax-id
           assoc
           (projected-syntax-id syntax binding-path binding-pattern)
           (:sh07/projected-scope-id scope)))
         (doseq
          [[binding projected] (map vector (:bindings scope) binding-paths)]
          (let
           [syntax-id
            (projected-syntax-id syntax (:path projected) (:name projected))]
           (swap! scope-by-syntax-id assoc syntax-id (:sh07/projected-scope-id scope))
           (swap!
            declaration-syntax-by-upstream-id
            assoc
            (:binding-syntax-id binding)
            syntax-id)))
         (cons (:scope-id scope) active))
        active)]
      (recur (next remaining) (inc binding-index) next-active))))]
  (doseq
   [index (range 2 (count items))]
   (walk syntax (get items index) (conj path index) nested :expression))))
