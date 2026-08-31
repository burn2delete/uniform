; Semantic decomposition of committed HEAD reader line 151848.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/let
 [semantic-late-sh06-resolution-analysis-inputs-parameter-names
  semantic-late-sh06-resolution-analysis-inputs-parameter-names
  semantic-late-sh06-resolution-analysis-inputs-make-traversal
  semantic-late-sh06-resolution-analysis-inputs-make-traversal]
 (defn
  sh06-resolution-analysis-inputs
  [module sh05-artifact overrides]
  (let
   [scopes
    (atom [])
    references
    (atom [])
    next-scope
    (atom 0)
    next-reference
    (atom 0)
    shadow-forbidden
    (set (get-in overrides [:shadow-policy :forbidden]))
    add-reference!
    (fn
     [symbol scope-chain syntax position span]
     (when
      (symbol? symbol)
      (let
       [ordinal
        (swap! next-reference inc)
        namespace-part
        (namespace symbol)
        simple-name
        (clojure.core/symbol (name symbol))]
       (swap!
        references
        conj
        {:syntax-id
         (reader-canonical-hash
          {:domain :gravity/sh06-reference-syntax-v1,
           :owner-syntax-id (:syntax/id syntax),
           :ordinal ordinal,
           :symbol symbol,
           :position position}),
         :symbol symbol,
         :qualifier (when namespace-part (clojure.core/symbol namespace-part)),
         :name simple-name,
         :position position,
         :scope-chain (vec scope-chain),
         :semantic-span (sh06-resolution-semantic-span span ordinal),
         :source-span span}))))
    add-scope!
    (fn
     [owner-syntax-id parent-scope-id local-names span]
     (let
      [ordinal
       (swap! next-scope inc)
       scope-id
       (reader-canonical-hash
        {:domain :gravity/sh06-lexical-scope-v1,
         :owner-syntax-id owner-syntax-id,
         :ordinal ordinal,
         :parent-scope-id parent-scope-id})
       bindings
       (mapv
        (fn
         [binding-ordinal name]
         {:name name,
          :kind :local,
          :semantic-span {:scope-ordinal ordinal, :binding-ordinal binding-ordinal},
          :source-span span,
          :binding-syntax-id
          (reader-canonical-hash
           {:domain :gravity/sh06-local-binding-v1,
            :scope-id scope-id,
            :ordinal binding-ordinal,
            :name name}),
          :allow-shadow? (not (contains? shadow-forbidden name))})
        (range)
        local-names)]
      (swap!
       scopes
       conj
       {:scope-id scope-id,
        :parent-scope-id parent-scope-id,
        :owner-syntax-id owner-syntax-id,
        :bindings bindings})
      scope-id))]
   (clojure.core/let
    [walk-form
     (semantic-late-sh06-resolution-analysis-inputs-make-traversal
      semantic-late-sh06-resolution-analysis-inputs-parameter-names
      add-reference!
      add-scope!)]
    (doseq
     [syntax (:expanded-syntax-stream sh05-artifact)]
     (let
      [form (:form syntax)]
      (when-not (and (seq? form) (= 'ns (first form))) (walk-form form [] syntax :expression))))
    {:lexical-scopes @scopes, :references @references}))))
