; Semantic decomposition of committed HEAD reader line 154235.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/let
 [semantic-late-sh07-core-resolution-projection!-fn
  semantic-late-sh07-core-resolution-projection!-fn
  semantic-late-sh07-core-resolution-projection!-match
  semantic-late-sh07-core-resolution-projection!-match
  semantic-late-sh07-core-resolution-projection!-try
  semantic-late-sh07-core-resolution-projection!-try
  semantic-late-sh07-core-resolution-projection!-bindings
  semantic-late-sh07-core-resolution-projection!-bindings
  semantic-late-sh07-core-resolution-projection!-sequence
  semantic-late-sh07-core-resolution-projection!-sequence
  semantic-late-sh07-core-resolution-projection!-make-traversal
  semantic-late-sh07-core-resolution-projection!-make-traversal
  semantic-late-sh07-core-resolution-projection!-upstream-parity
  semantic-late-sh07-core-resolution-projection!-upstream-parity
  semantic-late-sh07-core-resolution-projection!-projection
  semantic-late-sh07-core-resolution-projection!-projection]
 (defn
  sh07-core-resolution-projection!
  [source-path
   source-revision-id
   executable-syntax
   trees
   traces
   authenticated-sh06-request
   resolved-analysis]
  (let
   [upstream-scopes
    (vec (:lexical-scopes authenticated-sh06-request))
    upstream-references
    (vec (:references authenticated-sh06-request))
    resolved-references
    (vec (:resolution-table resolved-analysis))
    resolved-by-reference
    (into {} (map (juxt :syntax-id identity)) resolved-references)
    resolved-binding-by-id
    (into {} (map (juxt :binding-id identity)) (:binding-table resolved-analysis))
    namespace-root-scope-id
    (reader-canonical-hash
     {:domain :gravity/sh07-projected-namespace-root-scope-v1,
      :source-revision-id source-revision-id,
      :namespace (get-in resolved-analysis [:module-contract :namespace])})
    scope-index
    (atom 0)
    reference-index
    (atom 0)
    projected-scope-by-upstream
    (atom {})
    scope-by-syntax-id
    (atom {})
    declaration-syntax-by-upstream-id
    (atom {})
    occurrences
    (atom [])
    trace-by-root
    (into {} (map (juxt :output-def-syntax-id identity)) traces)
    root-by-upstream-id
    (into
     {}
     (map (fn [[syntax tree]] [(:syntax/id syntax) (:root tree)]))
     (map vector executable-syntax trees))
    projected-syntax-id
    (fn
     [syntax path value]
     (sh07-core-projected-syntax-id
      source-revision-id
      (:sh07/root-syntax-id syntax)
      path
      (sh07-core-value-kind value)
      (get trace-by-root (:sh07/root-syntax-id syntax))))
    remember-scope!
    (fn
     [syntax path value scope-chain]
     (swap!
      scope-by-syntax-id
      assoc
      (projected-syntax-id syntax path value)
      (or (get @projected-scope-by-upstream (first scope-chain)) namespace-root-scope-id)))
    consume-scope!
    (fn
     [syntax parent-scope-id bindings]
     (let
      [index
       @scope-index
       scope
       (get upstream-scopes index)
       projected-scope-id
       (reader-canonical-hash
        {:domain :gravity/sh07-projected-lexical-scope-v1,
         :source-revision-id source-revision-id,
         :ordinal index,
         :parent-scope-id
         (or (get @projected-scope-by-upstream parent-scope-id) namespace-root-scope-id),
         :binding-names (mapv :name bindings)})]
      (when-not
       (and
        scope
        (= (:syntax/id syntax) (:owner-syntax-id scope))
        (= parent-scope-id (:parent-scope-id scope))
        (=
         (mapv :name bindings)
         (mapv (fn* [p1__1294#] (get-in p1__1294# [:name])) (:bindings scope))))
       (throw
        (ex-info
         "SH-07 lexical scope projection is not bijective"
         {:id "C6-VERIFY",
          :stage :core-lowering,
          :source-path source-path,
          :reason :sh06-lexical-scope-projection-mismatch,
          :scope-index index})))
      (swap! scope-index inc)
      (swap! projected-scope-by-upstream assoc (:scope-id scope) projected-scope-id)
      (assoc scope :sh07/projected-scope-id projected-scope-id)))
    add-reference!
    (fn
     [syntax path symbol position scope-chain]
     (let
      [index
       @reference-index
       upstream
       (get upstream-references index)
       resolved
       (get resolved-by-reference (:syntax-id upstream))
       resolved-binding
       (get resolved-binding-by-id (:binding-id resolved))
       expected-upstream-id
       (reader-canonical-hash
        {:domain :gravity/sh06-reference-syntax-v1,
         :owner-syntax-id (:syntax/id syntax),
         :ordinal (inc index),
         :symbol symbol,
         :position position})
       projected-id
       (projected-syntax-id syntax path symbol)]
      (when-not
       (and
        upstream
        resolved
        (= expected-upstream-id (:syntax-id upstream))
        (= symbol (:symbol upstream) (:symbol resolved))
        (= position (:position upstream) (:position resolved))
        (= (:syntax-id upstream) (:syntax-id resolved))
        (= (vec scope-chain) (:scope-chain upstream))
        (= (:semantic-span upstream) (:semantic-span resolved))
        resolved-binding
        (or
         (not= :lexical (:binding-class resolved-binding))
         (some #{(:scope-id resolved-binding)} scope-chain)))
       (throw
        (ex-info
         "SH-07 reference projection is not bijective"
         {:id "C6-VERIFY",
          :stage :core-lowering,
          :source-path source-path,
          :reason :sh06-reference-projection-mismatch,
          :reference-index index,
          :symbol symbol,
          :position position})))
      (swap! reference-index inc)
      (swap!
       occurrences
       conj
       {:reference-syntax-id projected-id,
        :symbol symbol,
        :position position,
        :binding-id (:binding-id resolved),
        :resolution-order (:resolution-order resolved),
        :source-span (sh07-core-semantic-span (:source-span resolved))})))]
   (clojure.core/let
    [walk
     (semantic-late-sh07-core-resolution-projection!-make-traversal
      semantic-late-sh07-core-resolution-projection!-sequence
      semantic-late-sh07-core-resolution-projection!-fn
      semantic-late-sh07-core-resolution-projection!-match
      semantic-late-sh07-core-resolution-projection!-try
      semantic-late-sh07-core-resolution-projection!-bindings
      projected-syntax-id
      remember-scope!
      consume-scope!
      add-reference!
      scope-by-syntax-id
      declaration-syntax-by-upstream-id
      source-path)]
    (doseq [syntax executable-syntax] (walk syntax (:form syntax) [] [] :expression)))
   (semantic-late-sh07-core-resolution-projection!-upstream-parity
    {:upstream-references upstream-references,
     :scope-by-syntax-id scope-by-syntax-id,
     :declaration-syntax-by-upstream-id declaration-syntax-by-upstream-id,
     :occurrences occurrences,
     :projected-scope-by-upstream projected-scope-by-upstream,
     :source-path source-path,
     :source-revision-id source-revision-id,
     :resolved-analysis resolved-analysis,
     :upstream-scopes upstream-scopes,
     :root-by-upstream-id root-by-upstream-id,
     :reference-index reference-index,
     :resolved-references resolved-references,
     :trees trees,
     :scope-index scope-index})
   (semantic-late-sh07-core-resolution-projection!-projection
    {:upstream-references upstream-references,
     :scope-by-syntax-id scope-by-syntax-id,
     :declaration-syntax-by-upstream-id declaration-syntax-by-upstream-id,
     :occurrences occurrences,
     :projected-scope-by-upstream projected-scope-by-upstream,
     :source-path source-path,
     :source-revision-id source-revision-id,
     :resolved-analysis resolved-analysis,
     :upstream-scopes upstream-scopes,
     :root-by-upstream-id root-by-upstream-id,
     :reference-index reference-index,
     :resolved-references resolved-references,
     :trees trees,
     :scope-index scope-index}))))
