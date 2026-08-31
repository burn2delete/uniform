; Semantic decomposition of committed HEAD reader line 154235.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-sh07-core-resolution-projection!-make-traversal
 [seq-helper
  fn-helper
  match-helper
  try-helper
  bindings-helper
  projected-syntax-id
  remember-scope!
  consume-scope!
  add-reference!
  scope-by-syntax-id
  declaration-syntax-by-upstream-id
  source-path]
 (clojure.core/letfn
  [(walk
    [syntax value path scope-chain position]
    (remember-scope! syntax path value scope-chain)
    (cond
     (symbol? value)
     (add-reference! syntax path value position scope-chain)
     (seq? value)
     (seq-helper
      fn-helper
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
      (vec value)
      (first value)
      path
      scope-chain
      position)
     (map? value)
     (doseq
      [[index [key child]] (map-indexed vector value)]
      (walk syntax key (conj path (* 2 index)) scope-chain :expression)
      (walk syntax child (conj path (inc (* 2 index))) scope-chain :expression))
     (set? value)
     (let
      [ordered
       (vec (sort-by pr-str value))
       path-by-value
       (into {} (map-indexed (fn [index item] [item index])) ordered)]
      (doseq
       [item value]
       (walk syntax item (conj path (get path-by-value item)) scope-chain position)))
     (coll? value)
     (doseq
      [index (range (count value))]
      (walk syntax (nth value index) (conj path index) scope-chain position))
     :else
     nil))]
  walk))
