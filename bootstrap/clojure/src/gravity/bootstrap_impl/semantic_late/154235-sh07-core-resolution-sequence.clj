; Semantic decomposition of committed HEAD reader line 154235.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-sh07-core-resolution-projection!-sequence
 [fn-helper
  match-helper
  try-helper
  bindings-helper
  walk
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
  [items (vec value) operator (first items)]
  (cond
   (= operator 'quote)
   (add-reference! syntax (conj path 0) 'quote :operator scope-chain)
   (= operator 'syntax-quote)
   (add-reference! syntax (conj path 0) operator :operator scope-chain)
   (contains? '#{defconst def} operator)
   (do
    (add-reference! syntax (conj path 0) operator :operator scope-chain)
    (doseq
     [index (range 2 (count items))]
     (walk syntax (get items index) (conj path index) scope-chain :expression)))
   (= operator 'defn)
   (throw
    (ex-info
     "Unexpanded defn reached SH-07 projection"
     {:id "C6-VERIFY", :stage :core-lowering, :source-path source-path, :reason :unexpanded-defn}))
   (= operator 'fn)
   (fn-helper
    walk
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
    position)
   (= operator 'match)
   (match-helper
    walk
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
    position)
   (= operator 'try)
   (try-helper
    walk
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
    position)
   (contains? '#{let loop} operator)
   (bindings-helper
    walk
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
    position)
   :else
   (doseq
    [index (range (count items))]
    (let
     [item (get items index)]
     (walk syntax item (conj path index) scope-chain (if (zero? index) :operator position)))))))
