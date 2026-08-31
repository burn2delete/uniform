; Semantic decomposition of committed HEAD reader line 154235.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-sh07-core-resolution-projection!-try
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
  (add-reference! syntax (conj path 0) 'try :operator scope-chain)
  (doseq
   [index (range 1 (count items))]
   (let
    [item
     (get items index)
     item-path
     (conj path index)
     clause-items
     (when (seq? item) (vec item))
     clause-operator
     (first clause-items)]
    (cond
     (= clause-operator 'catch)
     (do
      (add-reference! syntax (conj item-path 0) 'catch :operator scope-chain)
      (when
       (< 1 (count clause-items))
       (walk syntax (get clause-items 1) (conj item-path 1) scope-chain :type))
      (let
       [binding-name (get clause-items 2)]
       (when
        (symbol? binding-name)
        (let
         [binding-path
          (conj item-path 2)
          scope
          (consume-scope! syntax (first scope-chain) [{:name binding-name, :path binding-path}])
          syntax-id
          (projected-syntax-id syntax binding-path binding-name)
          nested
          (cons (:scope-id scope) scope-chain)]
         (swap! scope-by-syntax-id assoc syntax-id (:sh07/projected-scope-id scope))
         (swap!
          declaration-syntax-by-upstream-id
          assoc
          (get-in scope [:bindings 0 :binding-syntax-id])
          syntax-id)
         (doseq
          [handler-index (range 3 (count clause-items))]
          (walk
           syntax
           (get clause-items handler-index)
           (conj item-path handler-index)
           nested
           :expression))))))
     (= clause-operator 'finally)
     (do
      (add-reference! syntax (conj item-path 0) 'finally :operator scope-chain)
      (doseq
       [cleanup-index (range 1 (count clause-items))]
       (walk
        syntax
        (get clause-items cleanup-index)
        (conj item-path cleanup-index)
        scope-chain
        :expression)))
     :else
     (walk syntax item item-path scope-chain :expression))))))
