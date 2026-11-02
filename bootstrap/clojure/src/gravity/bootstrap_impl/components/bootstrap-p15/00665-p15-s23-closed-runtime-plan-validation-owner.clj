(defn p15-s23-closed-runtime-plan-validation!
  "Validate the exact public closed-plan subset before the Gravity executor is\n  invoked.  The walk is iterative, bounded, and carries lexical bindings plus\n  conservative scalar type facts, so a hostile plan cannot reach recursive\n  Gravity evaluation or fail through the host stack."
  [source-path requested-target plan]
  (let [entrypoint (:entrypoint plan) definition (get-in plan [:functions entrypoint])]
    (when-not (and
                (symbol? entrypoint)
                (map? definition)
                (integer? (:arity definition))
                (zero? (:arity definition))
                (vector? (:params definition))
                (empty? (:params definition))
                (vector? (:instructions definition)))
      (p15-s23-stage2-runtime-executor-fail!
        "P15S23X002"
        source-path
        definition
        {:target requested-target, :missing-fact :closed-plan-entrypoint-shape}))
    (loop [pending (vec
                     (map
                       (fn [instruction]
                         {:instruction instruction,
                          :locals #{},
                          :local-types {},
                          :depth 1})
                       (:instructions definition)))
           visited 0
           observed-operation-set #{}]
      (if-let [{:keys [instruction locals local-types depth]} (peek pending)]
        (let [pending (pop pending)
              visited (inc visited)
              op (when (map? instruction) (:op instruction))
              observed-operation-set (conj observed-operation-set op)]
          (when (or
                  (> depth p15-s23-closed-runtime-max-depth)
                  (> visited p15-s23-closed-runtime-max-nodes))
            (p15-s23-stage2-runtime-executor-fail!
              "P15S23X002"
              source-path
              instruction
              {:target requested-target,
               :missing-fact :closed-plan-bounds,
               :maximum-depth p15-s23-closed-runtime-max-depth,
               :maximum-nodes p15-s23-closed-runtime-max-nodes,
               :observed-depth depth,
               :observed-nodes visited}))
          (when-not (and
                      (map? instruction)
                      (contains? p15-s23-closed-runtime-operations op))
            (p15-s23-stage2-runtime-executor-fail!
              "P15S23X002"
              source-path
              instruction
              {:target requested-target,
               :missing-fact :closed-plan-operation,
               :unsupported-operation op}))
          (let [[next-pending next-visited next-observed-operation-set] (__gravity_bootstrap_closed_runtime_instruction_step
                                                                          source-path
                                                                          requested-target
                                                                          plan
                                                                          instruction
                                                                          locals
                                                                          local-types
                                                                          depth
                                                                          pending
                                                                          visited
                                                                          observed-operation-set
                                                                          op)]
            (recur next-pending next-visited next-observed-operation-set)))
        {:artifact :gravity/p15-s23-runtime-closed-plan-validation-record,
         :status :complete,
         :entrypoint entrypoint,
         :operation-set p15-s23-closed-runtime-operations,
         :observed-operation-set observed-operation-set,
         :node-count visited,
         :maximum-depth p15-s23-closed-runtime-max-depth,
         :maximum-nodes p15-s23-closed-runtime-max-nodes}))))
