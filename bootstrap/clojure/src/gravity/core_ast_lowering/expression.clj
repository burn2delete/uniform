(ns gravity.core-ast-lowering.expression
  "Core expression dispatch. Recursive dependencies route through the facade.")

(defn lower-core-expr
  [ops counter module syntax form context]
  (let [{:keys [next-node-id assert-core-operator! assert-recur-target!
                assert-set-target! assert-throw-legal! core-node
                lower-core-expr lower-sequential-body lower-match-clauses
                combine-effects form-effect]} ops
        id (next-node-id counter)]
    (cond
      (seq? form)
      (let [op (first form)]
        (assert-core-operator! module syntax form)
        (assert-recur-target! module syntax form context)
        (assert-set-target! module syntax form)
        (assert-throw-legal! module syntax form)
        (case op
          quote
          (core-node id :quote syntax form
                     {:value (second form) :evaluation-order []})

          if
          (let [[_ test then else] form
                children [(lower-core-expr counter module syntax test context)
                          (lower-core-expr counter module syntax then context)
                          (lower-core-expr counter module syntax else context)]]
            (core-node id :if syntax form
                       {:children children
                        :evaluation-order [:condition :then-or-else]
                        :effects (apply combine-effects
                                        (form-effect form)
                                        (map :effects children))}))

          do
          (let [children (lower-sequential-body counter module syntax
                                                (rest form) context)]
            (core-node id :do syntax form
                       {:children children
                        :evaluation-order
                        (mapv (fn [idx] [:expr idx])
                              (range (count children)))
                        :effects (apply combine-effects
                                        (form-effect form)
                                        (map :effects children))}))

          let
          (let [[_ bindings & body] form
                binding-pairs (partition 2 bindings)
                binding-nodes
                (mapv (fn [[name expr]]
                        {:name name
                         :initializer
                         (lower-core-expr
                          counter module syntax expr context)})
                      binding-pairs)
                body-nodes
                (lower-sequential-body counter module syntax body context)]
            (core-node id :let syntax form
                       {:bindings binding-nodes
                        :children body-nodes
                        :evaluation-order
                        (concat (mapv (fn [[name _]] [:binding name])
                                      binding-pairs)
                                (mapv (fn [idx] [:body idx])
                                      (range (count body-nodes))))
                        :effects
                        (apply combine-effects
                               (form-effect form)
                               (concat (map (comp :effects :initializer)
                                            binding-nodes)
                                       (map :effects body-nodes)))}))

          fn
          (let [[_ params & body] form
                body-nodes
                (lower-sequential-body
                 counter module syntax body
                 (assoc context :recur-arity (count params)))
                latent-effects
                (apply combine-effects (map :effects body-nodes))]
            (core-node id :fn syntax form
                       {:params params
                        :children body-nodes
                        :latent-effects latent-effects
                        :evaluation-order [:call-arguments-left-to-right]}))

          loop
          (let [[_ bindings & body] form
                recur-arity (/ (count bindings) 2)
                binding-pairs (partition 2 bindings)
                binding-nodes
                (mapv (fn [[name expr]]
                        {:name name
                         :initializer
                         (lower-core-expr
                          counter module syntax expr context)})
                      binding-pairs)
                body-nodes
                (lower-sequential-body
                 counter module syntax body
                 (assoc context :recur-arity recur-arity))]
            (core-node id :loop syntax form
                       {:bindings binding-nodes
                        :recur-arity recur-arity
                        :children body-nodes
                        :evaluation-order
                        (concat
                         (mapv (fn [[name _]] [:loop-binding name])
                               binding-pairs)
                         (mapv (fn [idx] [:body idx])
                               (range (count body-nodes))))}))

          recur
          (core-node id :recur syntax form
                     {:arguments
                      (lower-sequential-body counter module syntax
                                             (rest form) context)
                      :target-arity (:recur-arity context)
                      :evaluation-order [:arguments-left-to-right]})

          def
          (let [[_ name value] form
                value-node
                (lower-core-expr counter module syntax value context)]
            (core-node id :def syntax form
                       {:name name :value value-node
                        :evaluation-order [:initializer]
                        :effects (:effects value-node)}))

          ;; Legacy L2 accepts this compile-time def even though it is absent
          ;; from core-forms. Preserve that historical lowering exactly.
          defconst
          (let [[_ name value] form
                value-node
                (lower-core-expr counter module syntax value context)]
            (core-node id :def syntax form
                       {:name name :value value-node
                        :compile-time-binding? true
                        :evaluation-order [:compile-time-initializer]
                        :effects (:effects value-node)}))

          var
          (core-node id :var syntax form
                     {:name (second form) :evaluation-order []})

          set!
          (let [[_ target value] form
                value-node
                (lower-core-expr counter module syntax value context)]
            (core-node id :set! syntax form
                       {:target target :value value-node
                        :evaluation-order [:value]
                        :effects (combine-effects #{:state/write}
                                                  (:effects value-node))}))

          try
          (let [[_ body & handlers] form
                body-node
                (lower-core-expr counter module syntax body context)]
            (core-node id :try syntax form
                       {:body body-node :handlers handlers
                        :evaluation-order [:body :matching-handler]
                        :effects (:effects body-node)}))

          throw
          (let [[_ value] form
                value-node
                (lower-core-expr counter module syntax value context)]
            (core-node id :throw syntax form
                       {:value value-node
                        :evaluation-order [:value]
                        :effects (combine-effects #{:error/throw}
                                                  (:effects value-node))}))

          match
          (let [[_ value & clauses] form
                value-node
                (lower-core-expr counter module syntax value context)
                lowered-clauses
                (lower-match-clauses
                 counter module syntax clauses context)]
            (core-node id :match syntax form
                       {:value value-node
                        :clauses lowered-clauses
                        :evaluation-order [:scrutinee :selected-clause]
                        :effects
                        (apply combine-effects
                               (:effects value-node)
                               (concat
                                (map #(get-in % [:guard :effects] #{})
                                     lowered-clauses)
                                (map #(get-in % [:body :effects] #{})
                                     lowered-clauses)))}))

          ;; A non-reserved list is a call. Lowering the raw operator with the
          ;; arguments preserves the legacy node-id and evaluation order.
          (let [children
                (lower-sequential-body
                 counter module syntax form context)]
            (core-node id :call syntax form
                       {:operator op
                        :arguments (vec (rest children))
                        :evaluation-order
                        [:operator :arguments-left-to-right]
                        :effects (apply combine-effects
                                        (form-effect form)
                                        (map :effects children))}))))

      (symbol? form)
      (core-node id :symbol syntax form
                 {:name form :evaluation-order []})

      :else
      (core-node id :literal syntax form
                 {:value form :evaluation-order []}))))
