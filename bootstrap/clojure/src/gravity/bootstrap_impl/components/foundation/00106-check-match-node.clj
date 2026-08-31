

(defn check-match-node
  [checker ctx node]
  (let [value-fact (check-typed-node checker ctx (:value node))
        clauses (:clauses node)
        patterns (mapv :pattern clauses)
        seen-default? (atom false)
        branch-facts
        (mapv (fn [clause]
                (let [pattern (:pattern clause)
                      branch-index (:branch-index clause)
                      bindings (vec (pattern-bindings pattern))
                      family (pattern-family pattern)]
                  (when @seen-default?
                    (let [diagnostic {:match-node (:node-id node)
                                      :branch-index branch-index
                                      :pattern pattern
                                      :diagnostic "L7-UNREACHABLE"}]
                      (record-checker! checker :unreachable-branch-diagnostics diagnostic)
                      (typed-diagnostic! "L7-UNREACHABLE"
                                         "match branch can never be selected"
                                         node
                                         "Move the default branch after reachable branches or remove the unreachable branch."
                                         diagnostic)))
                  (when (wildcard-pattern? pattern)
                    (reset! seen-default? true))
                  (when-let [binding (duplicate-binding pattern)]
                    (typed-diagnostic! "L7-DUP-BINDING"
                                       "pattern binds the same name more than once"
                                       node
                                       "Use distinct names or a future equality-pattern form."
                                       {:binding binding
                                        :pattern pattern}))
                  (when (literal-pattern-incompatible? (:type value-fact) pattern)
                    (typed-diagnostic! "L7-PATTERN-TYPE"
                                       "pattern is incompatible with the scrutinee type"
                                       node
                                       "Use a pattern whose type can match the scrutinee."
                                       {:scrutinee-type (:type value-fact)
                                        :pattern pattern
                                        :pattern-type (literal-pattern-type pattern)}))
                  (when (and (= :map (pattern-kind pattern))
                             (untrusted-type? (:type value-fact)))
                    (typed-diagnostic! "L7-UNVALIDATED-SHAPE"
                                       "untrusted data is matched as a closed shape without validation"
                                       node
                                       "Validate external data through a schema boundary before matching required keys."
                                       {:scrutinee-type (:type value-fact)
                                        :pattern pattern}))
                  (when (= "MoveLinear" (constructor-name pattern))
                    (typed-diagnostic! "L7-LINEAR-MOVE"
                                       "pattern moves a linear value without ownership proof"
                                       node
                                       "Borrow the value or provide ownership evidence before moving it in a pattern."
                                       {:pattern pattern}))
                  (let [branch-ctx (child-context ctx)
                        binding-type (narrowed-type (:type value-fact) pattern)]
                    (doseq [binding bindings]
                      (swap! branch-ctx assoc-in [:variable-types binding] binding-type)
                      (record-checker! checker :branch-type-narrowing-table
                                       {:match-node (:node-id node)
                                        :branch-index branch-index
                                        :binding binding
                                        :narrowed-type binding-type
                                        :pattern-family family})
                      (record-checker! checker :pattern-ownership-facts
                                       {:match-node (:node-id node)
                                        :branch-index branch-index
                                        :binding binding
                                        :mode (if (= :linear family)
                                                :borrow-or-bind-linear
                                                :borrow-or-bind)}))
                    (when (and (= :map (pattern-kind pattern))
                               (validated-type? (:type value-fact)))
                      (record-checker! checker :pattern-schema-validation-links
                                       {:match-node (:node-id node)
                                        :branch-index branch-index
                                        :schema (:type value-fact)
                                        :validation-boundary :validated}))
                    (let [guard-fact (when-let [guard (:guard clause)]
                                       (let [guard-ctx (permissive-pattern-context branch-ctx)
                                             fact (check-typed-node checker guard-ctx guard)]
                                         (when (guard-effects-illegal? ctx fact)
                                           (typed-diagnostic! "L7-GUARD-EFFECT"
                                                              "guard effect is not legal for the active context"
                                                              node
                                                              "Declare the guard effect and capability, use a pure guard, or move the effect outside the pattern."
                                                              {:guard-effects (:effects fact)
                                                               :guard-capabilities (:capabilities fact)
                                                               :declared-effects (:declared-effects @ctx)
                                                               :declared-capabilities (:declared-capabilities @ctx)}))
                                         fact))
                          body-fact (check-typed-node checker branch-ctx (:body clause))
                          branch-effects (set/union (:effects body-fact)
                                                    (or (:effects guard-fact) #{}))
                          branch-capabilities (set/union (:capabilities body-fact)
                                                         (or (:capabilities guard-fact) #{}))]
                      (record-checker! checker :branch-effect-summary
                                       {:match-node (:node-id node)
                                        :branch-index branch-index
                                        :guard-effects (or (:effects guard-fact) #{})
                                        :branch-effects branch-effects
                                        :branch-capabilities branch-capabilities})
                      (assoc body-fact
                             :effects branch-effects
                             :capabilities branch-capabilities
                             :guard-fact guard-fact
                             :pattern pattern
                             :branch-index branch-index)))))
              clauses)
        has-default? (boolean (some wildcard-pattern? patterns))
        constructors (constructor-coverage patterns)]
    (when (and (closed-result-match? patterns)
               (not has-default?)
               (not (exhaustive-result-match? patterns)))
      (typed-diagnostic! "L7-NONEXHAUSTIVE"
                         "closed match lacks a required constructor case"
                         node
                         "Cover all closed constructors or add an explicit default where policy allows it."
                         {:constructors constructors
                          :required #{"Ok" "Err"}}))
    (when (and (= :formal (:profile @ctx))
               (not has-default?)
               (not (exhaustive-result-match? patterns)))
      (typed-diagnostic! "L7-NONEXHAUSTIVE"
                         "formal profile requires total pattern matching"
                         node
                         "Cover every closed case or provide an explicit partiality proof."
                         {:profile (:profile @ctx)}))
    (record-checker! checker :match-decision-trees
                     {:match-node (:node-id node)
                      :scrutinee-type (:type value-fact)
                      :pattern-families (vec (set (map pattern-family patterns)))
                      :branches (mapv (fn [clause]
                                        {:branch-index (:branch-index clause)
                                         :pattern (:pattern clause)
                                         :pattern-kind (pattern-kind (:pattern clause))
                                         :pattern-family (pattern-family (:pattern clause))
                                         :guarded? (boolean (:guard clause))
                                         :source-span (:source-span node)})
                                      clauses)})
    (record-checker! checker :exhaustiveness-report
                     {:match-node (:node-id node)
                      :profile (:profile @ctx)
                      :constructors constructors
                      :has-default has-default?
                      :closed-result-match? (boolean (closed-result-match? patterns))
                      :exhaustive? (or has-default?
                                       (not (closed-result-match? patterns))
                                       (exhaustive-result-match? patterns))})
    (typed-fact checker node (common-type branch-facts)
                (set/union (:effects value-fact) (collect-fact-effects branch-facts))
                (set/union (:capabilities value-fact) (collect-fact-capabilities branch-facts))
                {:children (vec (cons value-fact branch-facts))
                 :clauses clauses})))