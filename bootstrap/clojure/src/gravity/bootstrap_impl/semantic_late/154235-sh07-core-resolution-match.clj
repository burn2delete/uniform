; Semantic decomposition of committed HEAD reader line 154235.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-sh07-core-resolution-projection!-match
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
 (do
  (add-reference! syntax (conj path 0) 'match :operator scope-chain)
  (when (< 1 (count items)) (walk syntax (get items 1) (conj path 1) scope-chain :expression))
  (loop
   [pattern-index 2]
   (when
    (< (+ pattern-index 1) (count items))
    (let
     [pattern
      (get items pattern-index)
      branch-index
      (+ pattern-index 1)
      branch
      (get items branch-index)
      pattern-path
      (conj path pattern-index)]
     (cond
      (and (symbol? pattern) (not= '_ pattern))
      (let
       [scope
        (consume-scope! syntax (first scope-chain) [{:name pattern, :path pattern-path}])
        syntax-id
        (projected-syntax-id syntax pattern-path pattern)
        nested
        (cons (:scope-id scope) scope-chain)]
       (swap! scope-by-syntax-id assoc syntax-id (:sh07/projected-scope-id scope))
       (swap!
        declaration-syntax-by-upstream-id
        assoc
        (get-in scope [:bindings 0 :binding-syntax-id])
        syntax-id)
       (walk syntax branch (conj path branch-index) nested :expression))
      (and (vector? pattern) (sh06-fixed-vector-pattern? pattern))
      (when-let
       [binding-paths (sh06-unique-match-binding-paths pattern pattern-path)]
       (if
        (empty? binding-paths)
        (walk syntax branch (conj path branch-index) scope-chain :expression)
        (let
         [scope
          (consume-scope! syntax (first scope-chain) binding-paths)
          nested
          (cons (:scope-id scope) scope-chain)]
         (doseq
          [[binding projected] (map vector (:bindings scope) binding-paths)]
          (let
           [syntax-id (projected-syntax-id syntax (:path projected) (:name projected))]
           (swap! scope-by-syntax-id assoc syntax-id (:sh07/projected-scope-id scope))
           (swap! declaration-syntax-by-upstream-id assoc (:binding-syntax-id binding) syntax-id)))
         (walk syntax branch (conj path branch-index) nested :expression))))
      (or
       (= '_ pattern)
       (nil? pattern)
       (true? pattern)
       (false? pattern)
       (number? pattern)
       (char? pattern)
       (string? pattern)
       (keyword? pattern))
      (walk syntax branch (conj path branch-index) scope-chain :expression)
      :else
      nil)
     (recur (+ pattern-index 2)))))))
