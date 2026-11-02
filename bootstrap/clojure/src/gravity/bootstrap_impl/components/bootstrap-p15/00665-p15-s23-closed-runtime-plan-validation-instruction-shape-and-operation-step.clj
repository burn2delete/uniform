(defn- __gravity_bootstrap_closed_runtime_instruction_step [source-path
                                                            requested-target
                                                            plan
                                                            instruction
                                                            locals
                                                            local-types
                                                            depth
                                                            pending
                                                            visited
                                                            observed-operation-set
                                                            op]
  (case
    op
    :literal
    (do
      (when-not (c-backend-runtime-literal? (:value instruction))
        (p15-s23-stage2-runtime-executor-fail!
          "P15S23X002"
          source-path
          instruction
          {:target requested-target, :missing-fact :closed-plan-literal}))
      [pending visited observed-operation-set])
    :quote
    (do
      (when-not (c-backend-runtime-literal? (:value instruction))
        (p15-s23-stage2-runtime-executor-fail!
          "P15S23X002"
          source-path
          instruction
          {:target requested-target, :missing-fact :closed-plan-literal}))
      [pending visited observed-operation-set])
    :local
    (do
      (when-not (and
                  (symbol? (:name instruction))
                  (contains? locals (:name instruction)))
        (p15-s23-stage2-runtime-executor-fail!
          "P15S23X002"
          source-path
          instruction
          {:target requested-target,
           :missing-fact :closed-plan-local-binding,
           :local (:name instruction)}))
      [pending visited observed-operation-set])
    :builtin-call
    (let [args (:args instruction)
          function (:function instruction)
          observed-arity (when (vector? args) (count args))
          comparison? (contains? '#{= < <= > >=} function)
          operand-types (when (and comparison? (vector? args))
                          (mapv
                            #(p15-s23-closed-runtime-inferred-type
                              %
                              local-types
                              (inc depth))
                            args))]
      (when-not (and (vector? args) (or (= 'str function) comparison?))
        (p15-s23-stage2-runtime-executor-fail!
          "P15S23X002"
          source-path
          instruction
          {:target requested-target,
           :missing-fact :closed-plan-builtin,
           :function function,
           :actual-arity observed-arity}))
      (when-not (if comparison? (= 2 observed-arity) (contains? #{1 2} observed-arity))
        (p15-s23-stage2-runtime-fail-call-arity!
          "L2-BUILTIN-ARITY"
          plan
          function
          (if (coll? args) args [])
          (if comparison? 2 "1 or 2")))
      (when (and comparison? (not= [:gravity/integer :gravity/integer] operand-types))
        (p15-s23-stage2-runtime-executor-fail!
          "P15S23X002"
          source-path
          instruction
          {:target requested-target,
           :missing-fact :closed-plan-binary-integer-comparison-operands,
           :function function,
           :operand-types operand-types}))
      [(into
         pending
         (map
           (fn [child]
             {:instruction child,
              :locals locals,
              :local-types local-types,
              :depth (inc depth)})
           args))
       visited
       observed-operation-set])
    :println
    (let [args (:args instruction) observed-arity (when (vector? args) (count args))]
      (when-not (and
                  (vector? args)
                  (<= observed-arity p15-s23-closed-runtime-max-nodes)
                  (= :io/write (:effect instruction))
                  (= :io/stdout (:capability instruction)))
        (p15-s23-stage2-runtime-executor-fail!
          "P15S23X002"
          source-path
          instruction
          {:target requested-target,
           :missing-fact :closed-plan-println-contract,
           :actual-arity observed-arity}))
      [(into
         pending
         (map
           (fn [child]
             {:instruction child,
              :locals locals,
              :local-types local-types,
              :depth (inc depth)})
           args))
       visited
       observed-operation-set])
    :do
    (do
      (when-not (vector? (:body instruction))
        (p15-s23-stage2-runtime-executor-fail!
          "P15S23X002"
          source-path
          instruction
          {:target requested-target, :missing-fact :closed-plan-do-shape}))
      [(into
         pending
         (map
           (fn [child]
             {:instruction child,
              :locals locals,
              :local-types local-types,
              :depth (inc depth)})
           (:body instruction)))
       visited
       observed-operation-set])
    :if
    (do
      (when-not (and
                  (map? (:test instruction))
                  (map? (:then instruction))
                  (map? (:else instruction)))
        (p15-s23-stage2-runtime-executor-fail!
          "P15S23X002"
          source-path
          instruction
          {:target requested-target, :missing-fact :closed-plan-if-shape}))
      [(into
         pending
         (map
           (fn [child]
             {:instruction child,
              :locals locals,
              :local-types local-types,
              :depth (inc depth)})
           [(:test instruction) (:then instruction) (:else instruction)]))
       visited
       observed-operation-set])
    :let
    (let [bindings (:bindings instruction) body (:body instruction)]
      (when-not (and
                  (vector? bindings)
                  (vector? body)
                  (every?
                    #(and (map? %) (symbol? (:name %)) (map? (:expr %)))
                    bindings))
        (p15-s23-stage2-runtime-executor-fail!
          "P15S23X002"
          source-path
          instruction
          {:target requested-target, :missing-fact :closed-plan-let-shape}))
      (let [binding-names (mapv :name bindings)
            duplicate (first
                        (for
                          [name
                           binding-names
                           :when
                           (> (count (filter #{name} binding-names)) 1)]
                          name))
            next-locals (into locals binding-names)
            next-local-types (reduce
                               (fn [types binding]
                                 (assoc
                                   types
                                   (:name binding)
                                   (p15-s23-closed-runtime-inferred-type
                                     (:expr binding)
                                     types
                                     (inc depth))))
                               local-types
                               bindings)]
        (when duplicate
          (p15-s23-stage2-runtime-executor-fail!
            "P15S23X002"
            source-path
            instruction
            {:target requested-target,
             :missing-fact :closed-plan-let-binding,
             :duplicate-local duplicate}))
        (let [binding-frames (:frames
                               (reduce
                                 (fn [{:keys [frames scope types]} binding]
                                   {:frames
                                    (conj
                                      frames
                                      {:instruction (:expr binding),
                                       :locals scope,
                                       :local-types types,
                                       :depth (inc depth)}),
                                    :scope (conj scope (:name binding)),
                                    :types
                                    (assoc
                                      types
                                      (:name binding)
                                      (p15-s23-closed-runtime-inferred-type
                                        (:expr binding)
                                        types
                                        (inc depth)))})
                                 {:frames [], :scope locals, :types local-types}
                                 bindings))
              body-frames (map
                            (fn [child]
                              {:instruction child,
                               :locals next-locals,
                               :local-types next-local-types,
                               :depth (inc depth)})
                            body)]
          [(into pending (concat binding-frames body-frames))
           visited
           observed-operation-set])))))
